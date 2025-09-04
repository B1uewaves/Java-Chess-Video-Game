package chess;

public class CheckInput {
	
	public static boolean checkCoordinateValidity(String input){
		if (input.length() != 2) {
			return false;
		}
		char row = input.charAt(0);
		char col = input.charAt(1);

		if (row > '8' || row < '1') {
			return false;
		}

		if (col <'a' || col >'h') {
			return false;
		}

		return true;
	}
}
