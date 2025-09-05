*** Begin Patch
*** Add File: d:\wavle\Chess-Video-Game\chess-project\src\main\chess\ChessUI.java
+package chess;
+
+import javax.swing.JFrame;
+import javax.swing.JPanel;
+import javax.swing.JLabel;
+import javax.swing.SwingUtilities;
+import javax.swing.WindowConstants;
+import java.awt.BorderLayout;
+import java.awt.Color;
+import java.awt.Dimension;
+import java.awt.Font;
+import java.awt.Graphics;
+import java.awt.Point;
+import java.awt.event.MouseAdapter;
+import java.awt.event.MouseEvent;
+import java.util.concurrent.ExecutorService;
+import java.util.concurrent.Executors;
+import java.util.concurrent.ScheduledExecutorService;
+import java.util.concurrent.TimeUnit;
+
+public class ChessUI extends JFrame {
+	private final BoardPanel boardPanel;
+	private final JLabel statusLabel;
+	private final JLabel clockLabel;
+
+	private final ExecutorService moveExecutor = Executors.newSingleThreadExecutor();
+	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
+
+	private volatile char[][] boardSnapshot = new char[8][8];
+	private volatile boolean whitesTurn = true;
+	private volatile boolean gameEnded = false;
+
+	// clocks in milliseconds
+	private volatile long whiteMillis = 5 * 60 * 1000;
+	private volatile long blackMillis = 5 * 60 * 1000;
+
+	// selection state on EDT only
+	private Point selectedSquare = null; // (col, row)
+
+	public ChessUI() {
+		super("Chess (Swing + Threads)");
+		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
+		setLayout(new BorderLayout());
+
+		statusLabel = new JLabel("White to move");
+		statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
+		clockLabel = new JLabel(formatClocks());
+		clockLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
+
+		boardPanel = new BoardPanel();
+		boardPanel.setPreferredSize(new Dimension(480, 480));
+		boardPanel.addMouseListener(new MouseAdapter() {
+			@Override
+			public void mouseClicked(MouseEvent e) {
+				if (gameEnded) return;
+				Point sq = toBoardSquare(e.getX(), e.getY(), boardPanel.getWidth(), boardPanel.getHeight());
+				if (sq == null) return;
+				handleSquareClick(sq);
+			}
+		});
+
+		add(boardPanel, BorderLayout.CENTER);
+		JPanel south = new JPanel(new BorderLayout());
+		south.add(statusLabel, BorderLayout.WEST);
+		south.add(clockLabel, BorderLayout.EAST);
+		add(south, BorderLayout.SOUTH);
+
+		pack();
+		setLocationRelativeTo(null);
+
+		// initialize snapshot from current Board on worker, then repaint
+		moveExecutor.submit(() -> {
+			refreshSnapshotFromBoard();
+			SwingUtilities.invokeLater(boardPanel::repaint);
+		});
+
+		// clock ticking
+		scheduler.scheduleAtFixedRate(() -> {
+			if (gameEnded) return;
+			if (whitesTurn) {
+				whiteMillis = Math.max(0, whiteMillis - 100);
+				if (whiteMillis == 0) endGameOnWorker("Black wins on time");
+			} else {
+				blackMillis = Math.max(0, blackMillis - 100);
+				if (blackMillis == 0) endGameOnWorker("White wins on time");
+			}
+			SwingUtilities.invokeLater(() -> clockLabel.setText(formatClocks()));
+		}, 100, 100, TimeUnit.MILLISECONDS);
+	}
+
+	private void endGameOnWorker(String message) {
+		gameEnded = true;
+		SwingUtilities.invokeLater(() -> statusLabel.setText(message));
+	}
+
+	private void handleSquareClick(Point sq) {
+		if (selectedSquare == null) {
+			selectedSquare = sq;
+			boardPanel.setSelected(sq);
+			boardPanel.repaint();
+		} else {
+			Point origin = selectedSquare;
+			Point dest = sq;
+			selectedSquare = null;
+			boardPanel.setSelected(null);
+			boardPanel.repaint();
+
+			// process move on worker thread to keep EDT responsive
+			moveExecutor.submit(() -> processMove(origin, dest));
+		}
+	}
+
+	private void processMove(Point origin, Point dest) {
+		if (gameEnded) return;
+
+		int j0 = origin.x; // column 0..7
+		int i0 = origin.y; // row 0..7
+		int j1 = dest.x;
+		int i1 = dest.y;
+
+		if (!Board.hasPiece(i0, j0)) return;
+		Piece p = Board.getPiece(i0, j0);
+		if (p == null) return;
+		if (whitesTurn && p.getColour() != PieceColour.WHITE) return;
+		if (!whitesTurn && p.getColour() != PieceColour.BLACK) return;
+
+		if (p.isLegitMove(i0, j0, i1, j1)) {
+			boolean kingCaptured = Board.movePiece(i0, j0, i1, j1, p);
+			refreshSnapshotFromBoard();
+			whitesTurn = !whitesTurn;
+			final boolean end = kingCaptured;
+			SwingUtilities.invokeLater(() -> {
+				boardPanel.repaint();
+				statusLabel.setText(end ? (p.getColour() == PieceColour.WHITE ? "WHITES WIN" : "BLACKS WIN") : (whitesTurn ? "White to move" : "Black to move"));
+			});
+			if (kingCaptured) gameEnded = true;
+		} else {
+			// illegal move: update status briefly
+			SwingUtilities.invokeLater(() -> statusLabel.setText("Illegal move" + (whitesTurn ? ", White to move" : ", Black to move")));
+		}
+	}
+
+	private void refreshSnapshotFromBoard() {
+		char[][] snap = new char[8][8];
+		for (int i = 0; i < 8; i++) {
+			for (int j = 0; j < 8; j++) {
+				char c = ' ';
+				if (Board.hasPiece(i, j)) {
+					Piece p = Board.getPiece(i, j);
+					if (p != null) c = p.getSymbol();
+				}
+				snap[i][j] = c;
+			}
+		}
+		boardSnapshot = snap;
+	}
+
+	private String formatClocks() {
+		return "White " + formatMillis(whiteMillis) + "  |  Black " + formatMillis(blackMillis);
+	}
+
+	private static String formatMillis(long ms) {
+		long totalSec = ms / 1000;
+		long min = totalSec / 60;
+		long sec = totalSec % 60;
+		return String.format("%02d:%02d", min, sec);
+	}
+
+	private static Point toBoardSquare(int x, int y, int w, int h) {
+		int size = Math.min(w, h);
+		int offsetX = (w - size) / 2;
+		int offsetY = (h - size) / 2;
+		if (x < offsetX || y < offsetY || x >= offsetX + size || y >= offsetY + size) return null;
+		int cell = size / 8;
+		int col = (x - offsetX) / cell;
+		int row = (y - offsetY) / cell;
+		return new Point(col, row);
+	}
+
+	public void shutdown() {
+		gameEnded = true;
+		scheduler.shutdownNow();
+		moveExecutor.shutdownNow();
+	}
+
+	private class BoardPanel extends JPanel {
+		private Point selected;
+
+		public void setSelected(Point p) { this.selected = p; }
+
+		@Override
+		protected void paintComponent(Graphics g) {
+			super.paintComponent(g);
+			int size = Math.min(getWidth(), getHeight());
+			int offsetX = (getWidth() - size) / 2;
+			int offsetY = (getHeight() - size) / 2;
+			int cell = size / 8;
+
+			for (int row = 0; row < 8; row++) {
+				for (int col = 0; col < 8; col++) {
+					boolean dark = (row + col) % 2 == 1;
+					g.setColor(dark ? new Color(118, 150, 86) : new Color(238, 238, 210));
+					g.fillRect(offsetX + col * cell, offsetY + row * cell, cell, cell);
+					if (selected != null && selected.x == col && selected.y == row) {
+						g.setColor(new Color(255, 215, 0, 128));
+						g.fillRect(offsetX + col * cell, offsetY + row * cell, cell, cell);
+					}
+					char sym = boardSnapshot[row][col];
+					if (sym != ' ') {
+						g.setColor(Color.BLACK);
+						g.setFont(new Font(Font.MONOSPACED, Font.BOLD, cell - 20));
+						String s = String.valueOf(sym);
+						int strW = g.getFontMetrics().stringWidth(s);
+						int strH = g.getFontMetrics().getAscent();
+						int cx = offsetX + col * cell + (cell - strW) / 2;
+						int cy = offsetY + row * cell + (cell + strH) / 2 - 6;
+						g.drawString(s, cx, cy);
+					}
+				}
+			}
+		}
+	}
+}
+
