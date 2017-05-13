package chess;
import java.util.HashMap;

import javafx.scene.shape.Circle;

public class Board {
		public enum Piece{
		PAWN(1), KNIGHT(3), BISHOP(3), ROOK(7), QUEEN(9), KING(100);
		private double value;

		Piece(double value){
			this.value = value;
		}

		public double getValue(){
			return value;
		}

		@Override
		public String toString() {
			switch (this) {
			case PAWN:
				return "Pawn";
			case KNIGHT:
				return "Knight";
			case BISHOP:
				return "Bishop";
			case ROOK:
				return "Rook";
			case QUEEN:
				return "Queen";
			case KING:
				return "King";
			default:
				return "Unkown";
			}
		}
	}

	HashMap<Point, Piece> whitePieces;
	HashMap<Point, Piece> blackPieces;
	
	public boolean turn;

	public Board(boolean topPlayer) {
		whitePieces  = new HashMap<>();
		blackPieces  = new HashMap<>();
		turn = true;

		//Rooks
		for(int x = 0; x < 8; x++){
			whitePieces.put(new Point(x, (topPlayer ? 1 : 6)), Piece.PAWN);
			blackPieces.put(new Point(x, (topPlayer ? 6 : 1)), Piece.PAWN);
		}

		int whiteY = topPlayer ? 0 : 7;
		int blackY = topPlayer ? 7 : 0;

		whitePieces.put(new Point(0, whiteY), Piece.ROOK);
		whitePieces.put(new Point(1, whiteY), Piece.KNIGHT);
		whitePieces.put(new Point(2, whiteY), Piece.BISHOP);
		if(topPlayer){
			whitePieces.put(new Point(3, whiteY), Piece.QUEEN);
			whitePieces.put(new Point(4, whiteY), Piece.KING);
		}
		else{
			whitePieces.put(new Point(3, whiteY), Piece.KING);
			whitePieces.put(new Point(4, whiteY), Piece.QUEEN);
		}
		whitePieces.put(new Point(5, whiteY), Piece.BISHOP);
		whitePieces.put(new Point(6, whiteY), Piece.KNIGHT);
		whitePieces.put(new Point(7, whiteY), Piece.ROOK);
		
		blackPieces.put(new Point(0, blackY), Piece.ROOK);
		blackPieces.put(new Point(1, blackY), Piece.KNIGHT);
		blackPieces.put(new Point(2, blackY), Piece.BISHOP);
		if(topPlayer){
			blackPieces.put(new Point(3, blackY), Piece.QUEEN);
			blackPieces.put(new Point(4, blackY), Piece.KING);
		}
		else{
			blackPieces.put(new Point(3, blackY), Piece.KING);
			blackPieces.put(new Point(4, blackY), Piece.QUEEN);
		}
		blackPieces.put(new Point(5, blackY), Piece.BISHOP);
		blackPieces.put(new Point(6, blackY), Piece.KNIGHT);
		blackPieces.put(new Point(7, blackY), Piece.ROOK);
		
	}
	
	public void move(Point from, Point to){			//Temporary, for testing graphics
		if(whitePieces.containsKey(from)){
			whitePieces.put(to, whitePieces.remove(from));
		}
		else if(blackPieces.containsKey(from)){
			blackPieces.put(to, blackPieces.remove(from));
		}
		turn = !turn;
	}
}
