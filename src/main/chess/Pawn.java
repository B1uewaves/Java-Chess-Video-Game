package chess;

public class Pawn extends Piece{
	private PieceColour colour;
	private String symbol;

	public Pawn(PieceColour pc){
		if (pc.equals(PieceColour.WHITE)){
			this.colour=PieceColour.WHITE;
			this.symbol="♙";
		}
		else if (pc.equals(PieceColour.BLACK)){
			this.colour=PieceColour.BLACK;
			this.symbol="♟";
		}
	}

	public String getSymbol(){
		return symbol;
	}
	public PieceColour getColour(){
		return colour;
	}

	@Override
	public boolean isLegitMove(int i0, int j0, int i1, int j1) {

		// out of bounds ?
		if (i0 < 0 || i0 > 7 || j0 < 0 || j0 > 7 || i1 < 0 || i1 > 7 || j1 < 0 || j1 > 7) {
			return false;
		}
	
		int startRow = (this.colour == PieceColour.BLACK) ? 1 : 6;
		int scale = (this.colour == PieceColour.BLACK) ? 1 : -1; // Black moves down (1), White moves up (-1)
	
		// --- 1. Forward Move ---
		if (j0 == j1) { // Same column, moving forward
			// Move 1 step forward
			if (i1 == i0 + scale && !Board.hasPiece(i1, j1)) {
				return true;
			}
	
			// Move 2 steps forward (only from start position, ensuring both squares are empty)
			if (i0 == startRow && i1 == i0 + 2 * scale && !Board.hasPiece(i0 + scale, j0) && !Board.hasPiece(i1, j1)) {
				return true;
			}
		}
	
		// --- 2. Diagonal Capture ---
		if (Math.abs(j1 - j0) == 1 && i1 == i0 + scale) { // Must move forward diagonally
			if (Board.hasPiece(i1, j1) && Board.getPiece(i1, j1).getColour() != this.colour) {
				return true; // Valid diagonal capture
			}
		}
	
		return false; // All other cases are invalid

	}
}
