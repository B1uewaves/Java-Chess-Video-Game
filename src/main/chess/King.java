package chess;

public class King extends Piece{
	private PieceColour colour;
	private String symbol;

	public King(PieceColour pc){
		if (pc.equals(PieceColour.WHITE)){
			this.colour=PieceColour.WHITE;
			this.symbol="♔";
		}
		else if (pc.equals(PieceColour.BLACK)){
			this.colour=PieceColour.BLACK;
			this.symbol="♚";
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
		boolean outbound = (i0 < 0 || i0 > 7 || j0 < 0 || j0 > 7 || i1 < 0 || i1 > 7 || j1 < 0 || j1 > 7);
		// in bound?
		if (outbound) {
			return false;
		}

		// valid destination?
		if (Board.hasPiece(i1, j1) && Board.getPiece(i1, j1).getColour() == this.colour) {
			return false;
		}
		
		// Valid straigh or diagonal?
		boolean isStraight = (i0==i1) || (j0==j1);
		boolean isDiagonal = Math.abs(i1 - i0) == Math.abs(j1 - j0);

		//move only one?
		boolean oneUpdown = Math.abs(i0-i1) ==1 && j0==j1;
		boolean leftright = Math.abs(j0-j1) ==1 && i0==i1;
		boolean oneDiagonal = Math.abs(i0-i1) ==1 && Math.abs(j0-j1) ==1;

		if(!(isStraight && oneUpdown) &&!(isStraight && leftright) && !(isDiagonal&&oneDiagonal)) {
			return false;
		}




		return true;
	}


		

}
