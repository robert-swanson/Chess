package chess;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Manages the black and white pieces
 */
public class Board {
	/**
	 * Describes a piece on the board
	 */
		public enum Piece{
		PAWN(1), KNIGHT(3), BISHOP(3), ROOK(7), QUEEN(9), KING(100);
		private double value;

		Piece(double value){
			this.value = value;
		}

		/**
		 * Gets the value of the piece
		 * @return
		 * the number value
		 */
		public double getValue(){
			return value;
		}
		
		/**
		 * Returns the possible moves the piece can make given the position of every other piece on the board
		 * @param board
		 * The board
		 * @return
		 * An arrayList of the possible mobes the piece can make
		 */
		public ArrayList<Move> getPossMoves(Board board){
		return new ArrayList<>();
		//TODO Make get poss moves for piece
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
	public boolean topPlayer;
	
	public boolean whiteCanLeftCastle;
	public boolean whiteCanRightCastle;
	public boolean blackCanLeftCastle;
	public boolean blackCanRightCastle;

	/**
	 * Initailizes the pieces on the board according to what player is on the top
	 * @param topPlayer
	 */
	public Board(boolean topPlayer) {
		whitePieces  = new HashMap<>();
		blackPieces  = new HashMap<>();
		turn = true;
		this.topPlayer = topPlayer;
		
		whiteCanLeftCastle = true;
		whiteCanRightCastle = true;
		blackCanLeftCastle = true;
		blackCanRightCastle = true;

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
	
	/**
	 * Determines if a move is valid and if so, changes the board accordingly
	 * @param from
	 * The beginning position of the piece
	 * @param to
	 * The ending position of the piece
	 */
	public void move(Point from, Point to){
		//TODO move piece on board
		if(whitePieces.containsKey(from)){
			whitePieces.put(to, whitePieces.remove(from));
		}
		else if(blackPieces.containsKey(from)){
			blackPieces.put(to, blackPieces.remove(from));
		}
		turn = !turn;
	}
}
