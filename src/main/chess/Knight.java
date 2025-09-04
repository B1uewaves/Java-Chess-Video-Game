package chess;

public class Knight extends Piece{
	private PieceColour colour;
	private String symbol;

 	public Knight(PieceColour pc){
		if (pc.equals(PieceColour.WHITE)){
			this.colour=PieceColour.WHITE;
			this.symbol="♘";
		}
		else if (pc.equals(PieceColour.BLACK)){
			this.colour=PieceColour.BLACK;
			this.symbol="♞";
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

		// check within board
		if (i0 < 0 || i0 > 7 || j0 < 0 || j0 > 7 || i1 < 0 || i1 > 7 || j1 < 0 || j1 > 7) {
			return false;
		}

		//check destination not occupied
		if (Board.hasPiece(i1, j1) == true) {
			return false;
		}
				
		//move in L-shape
		if ((Math.abs(i1 - i0)==2 && Math.abs(j1 - j0)==1) || (Math.abs(j1 - j0)==2 && Math.abs(i1 - i0)==1)) {
			return true;
		}

		return false;
	}
}
