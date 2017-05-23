package chess;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import chess.pieces.Bishop;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Piece;
import chess.pieces.Queen;
import chess.pieces.Rook;

/**
 * Manages the black and white pieces
 */
public class Board {
	public static class RuleSet{
		enum TimeLimit{
			off,total,turn;
			int seconds;
			int minutes;
			private TimeLimit() {
				seconds = 0;
				minutes = 0;
			}
			@Override
			public String toString() {
				switch (this) {
				case off:
					return "Off";
				case total:
					return String.format("Total: %dm %ds",minutes, seconds);
				case turn:
					return String.format("Turn: %dm %ds",minutes, seconds);
				default:
					return "Other";
				}
			}
		}
		enum GameMode{
			pvp, pvc, cvc;
			@Override
			public String toString() {
				switch (this) {
				case pvp:
					return "Player vs Player";
				case pvc:
					return "Player vs Computer";
				case cvc:
					return "Computer vs Computer";
				default:
					return "Unkown GameMode";
				}
			}
		}
		public GameMode mode;
		public boolean cantCastleThroughCheck;
		public boolean cantCastleAfterCheck;
		public boolean topPlayer;
		public boolean computerPlayer;
		
		TimeLimit timeLimit;

		public RuleSet() {
			mode = GameMode.pvp;
			cantCastleThroughCheck = true;
			cantCastleAfterCheck = false;
			topPlayer = false;
			timeLimit = TimeLimit.off;
			computerPlayer = false;
		}
		@Override
		public String toString() {
		return String.format("Rules\n"
				+ "Mode: %s\n"
				+ "Can't Castle Through Check: %b\n"
				+ "Can't Castle After Check: %b\n"
				+ "Top Player: %s\n"
				+ "Computer Player: %s\n"
				+ "Time Limit: %s\n", mode, cantCastleThroughCheck, cantCastleAfterCheck, (topPlayer ? "White" : "Black"), (computerPlayer ? "White" : "Black"), timeLimit);
		}
	}
	/**
	 * Describes a piece on the board
	 */
	HashMap<Point, Piece> whitePieces;
	HashMap<Point, Piece> blackPieces;

	AI black;
	AI white;

	public RuleSet rules;

	public Stack<Move> history;

	public boolean turn;

	/**
	 * Initailizes the pieces on the board according to what player is on the top
	 * @param topPlayer
	 */
	public Board() {
		black = new AI(this, false);
		white = new AI(this, true);
		rules = new RuleSet();
		setUpBoard();
	}
	
	public void setUpBoard(){
		boolean topPlayer = rules.topPlayer;
		whitePieces  = new HashMap<>();
		blackPieces  = new HashMap<>();
		turn = true;

		history = new Stack<>();

		//Pawns
		for(int x = 0; x < 8; x++){
			whitePieces.put(new Point(x, (topPlayer ? 1 : 6)), new Pawn(true));
			blackPieces.put(new Point(x, (topPlayer ? 6 : 1)), new Pawn(false));
		}

		int whiteY = topPlayer ? 0 : 7;
		int blackY = topPlayer ? 7 : 0;

		whitePieces.put(new Point(0, whiteY), new Rook(true));
		whitePieces.put(new Point(1, whiteY), new Knight(true));
		whitePieces.put(new Point(2, whiteY), new Bishop(true));
		if(topPlayer){
			whitePieces.put(new Point(3, whiteY), new Queen(true));
			whitePieces.put(new Point(4, whiteY), new King(true));
		}
		else{
			whitePieces.put(new Point(3, whiteY), new King(true));
			whitePieces.put(new Point(4, whiteY), new Queen(true));
		}
		whitePieces.put(new Point(5, whiteY), new Bishop(true));
		whitePieces.put(new Point(6, whiteY), new Knight(true));
		whitePieces.put(new Point(7, whiteY), new Rook(true));

		blackPieces.put(new Point(0, blackY), new Rook(false));
		blackPieces.put(new Point(1, blackY), new Knight(false));
		blackPieces.put(new Point(2, blackY), new Bishop(false));
		if(topPlayer){
			blackPieces.put(new Point(3, blackY), new Queen(false));
			blackPieces.put(new Point(4, blackY), new Bishop(false));
		}
		else{
			blackPieces.put(new Point(3, blackY), new King(false));
			blackPieces.put(new Point(4, blackY), new Queen(false));
		}
		blackPieces.put(new Point(5, blackY), new Bishop(false));
		blackPieces.put(new Point(6, blackY), new Knight(false));
		blackPieces.put(new Point(7, blackY), new Rook(false));
		System.out.println(getPiece(new Point(3,7)));
	}
	

	/**
	 * Determines if a move is valid and if so, changes the board accordingly
	 * @param from
	 * The beginning position of the piece
	 * @param to
	 * The ending position of the piece
	 */
	public boolean move(Move m){
		ArrayList<Move> moves = new ArrayList<>();
		moves.add(m);
		setCaptures(moves);
		history.push(m);
		return m.doMove();
	}
	
	public Move undo(){
		Move rv = history.pop();
		rv.undoMove();
		turn = !turn;
		return rv;
	}

	/**
	 * Returns the piece at a specific location, returns null if there is no piece there
	 * @param p
	 * The point we are looking at
	 */
	public Piece getPiece(Point p)
	{
		if(whitePieces.containsKey(p))
			return whitePieces.get(p);
		else if(blackPieces.containsKey(p))
			return blackPieces.get(p);
		else
			return null;
	}
	public void putPiece(Piece piece, Point pos, boolean color){
		if(color)
			whitePieces.put(pos, piece);
		else
			blackPieces.put(pos, piece);
	}
	public Boolean getWhoOccupiesAt(Point p){
		if(whitePieces.containsKey(p))
			return true;
		else if(blackPieces.containsKey(p))
			return false;
		else
			return null;
	}
	public King getKing(boolean color){
		if(color){
			for(Piece p: whitePieces.values())
				if(p instanceof King)
					return (King)p;
		}
		else{
			for(Piece p: blackPieces.values())
				if(p instanceof King)
					return (King)p;
		}
		System.out.println("No King for player " + color);
		return new King(false);
	}
	public void setCaptures(ArrayList<Move> moves){
		for(Move m: moves){
			Boolean me = getWhoOccupiesAt(m.from);
			Boolean to = getWhoOccupiesAt(m.to);
			if(to != null && to == !me)
				m.setCapture(getPiece(m.to));
		}
	}

	public boolean playerHasPieceAt(boolean player, Point pos){
		return getPiece(pos).isWhite() == player;
	}
	
	public void removeCheckMoves(ArrayList<Move> moves){
		if(moves.size() == 0) return;
		boolean me = getWhoOccupiesAt(moves.get(0).from);
		if(me){
			for(Move initial: moves){
				
			}
		}
	}
}
