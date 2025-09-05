*** Begin Patch
*** Add File: d:\wavle\Chess-Video-Game\chess-project\src\main\chess\Launcher.java
+package chess;
+
+import javax.swing.SwingUtilities;
+
+public class Launcher {
+	public static void main(String[] args) {
+		Board.initialiseBoard();
+		Board.initialisePieces();
+
+		SwingUtilities.invokeLater(() -> {
+			ChessUI ui = new ChessUI();
+			ui.setVisible(true);
+			ui.addWindowListener(new java.awt.event.WindowAdapter() {
+				@Override
+				public void windowClosed(java.awt.event.WindowEvent e) {
+					ui.shutdown();
+				}
+			});
+		});
+	}
+}
+
