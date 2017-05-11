package chess;
import java.util.HashMap;

import javafx.scene.shape.Circle;

public class Board {
	public class Point{
		public int x;
		public int y;

		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString() {
			return String.format("(%d, %d)", x, y);
		}
	}
	public enum Piece{
		rook(1), knight(3), bishop(3), castle(7), queen(9), king(100);
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
			case rook:
				return "Rook";
			case knight:
				return "Knight";
			case bishop:
				return "Bishop";
			case castle:
				return "Castle";
			case queen:
				return "Queen";
			case king:
				return "King";
			default:
				return "Unkown";
			}
		}
	}

	HashMap<Point, Piece> whitePieces;
	HashMap<Point, Piece> blackPieces;

	public Board(boolean topPlayer) {
		whitePieces  = new HashMap<>();
		blackPieces  = new HashMap<>();

		//Rooks
		for(int x = 0; x < 8; x++){
			whitePieces.put(new Point(x, (topPlayer ? 1 : 6)), Piece.rook);
			blackPieces.put(new Point(x, (topPlayer ? 6 : 1)), Piece.rook);
		}

		int whiteY = topPlayer ? 0 : 7;
		int blackY = topPlayer ? 7 : 0;

		whitePieces.put(new Point(0, whiteY), Piece.castle);
		whitePieces.put(new Point(1, whiteY), Piece.knight);
		whitePieces.put(new Point(2, whiteY), Piece.bishop);
		whitePieces.put(new Point(3, whiteY), Piece.queen);
		whitePieces.put(new Point(4, whiteY), Piece.king);
		whitePieces.put(new Point(5, whiteY), Piece.bishop);
		whitePieces.put(new Point(6, whiteY), Piece.king);
		whitePieces.put(new Point(7, whiteY), Piece.castle);
		
		blackPieces.put(new Point(0, blackY), Piece.castle);
		blackPieces.put(new Point(1, blackY), Piece.knight);
		blackPieces.put(new Point(2, blackY), Piece.bishop);
		blackPieces.put(new Point(3, blackY), Piece.queen);
		blackPieces.put(new Point(4, blackY), Piece.king);
		blackPieces.put(new Point(5, blackY), Piece.bishop);
		blackPieces.put(new Point(6, blackY), Piece.king);
		blackPieces.put(new Point(7, blackY), Piece.castle);
		
	}
}
