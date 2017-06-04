package chess;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import chess.pieces.Bishop;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Piece;
import chess.pieces.Queen;
import chess.pieces.Rook;
import javafx.beans.property.SimpleBooleanProperty;

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
			mode = GameMode.cvc;
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
	public enum State{
		INPROGRESS, WHITEWON, BLACKWON, STALEMATE;
		@Override
		public String toString() {
			switch (this) {
			case INPROGRESS:
				return "Game In Progress";
			case WHITEWON:
				return "White Won!";
			case BLACKWON:
				return "Black Won!";
			case STALEMATE:
				return "Stalemate!";
			default:
				return "Other";
			}
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
	public Boolean allowance;

	public Stack<Move> history;
	public State gameState;

	public boolean turn;

	/**
	 * Initailizes the pieces on the board according to what player is on the top
	 * @param topPlayer
	 */
	public Board(SimpleBooleanProperty allowance) {
		black = new AI(this, false, allowance);
		white = new AI(this, true, allowance);
		rules = new RuleSet();
		setUpBoard();
		gameState = State.INPROGRESS;
	}
	
	public void setUpBoard(){
		boolean topPlayer = rules.topPlayer;
		gameState = State.INPROGRESS;
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
		boolean rv = m.doMove();
		AI.updateGameState(this);
		return rv;
	}
	
	public Move undo(){
		Move rv = history.pop();
		rv.undoMove();
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
	public void putPiece(Piece piece, Point pos){
		if(piece.isWhite())
			whitePieces.put(pos, piece);
		else
			blackPieces.put(pos, piece);
	}
	public Piece getPiece(Point p, boolean color){
		if(color)
			return whitePieces.get(p);
		else
			return blackPieces.get(p);
	}
	public HashMap<Point, Piece> getPieces(boolean color){
		if(color)
			return whitePieces;
		return blackPieces;
	}
	public Piece removePiece(Point p, boolean color){
		if(color)
			return whitePieces.remove(p);
		else
			return blackPieces.remove(p);
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
			if(!(to == null) && to == !me)
				m.setCapture(getPiece(m.to));
		}
	}

	public boolean playerHasPieceAt(boolean player, Point pos){
		return getPiece(pos).isWhite() == player;
	}
	
	public void removeCheckMoves(ArrayList<Move> moves){
		Iterator<Move> itr = moves.iterator();
		while(itr.hasNext()){
			Move m = itr.next();
			if(m.putsPlayerInCheck(m.me))
				itr.remove();
		}
	}
	
	public void addCastleMoves(ArrayList<Move> moves, boolean color){
		int y = rules.topPlayer==color ? 0 : 7;
		King king = getKing(color);
		if(king.hasMoved)
			return;
		
		boolean validL = true;
		Piece left = getPiece(new Point(0, y), color);
		if(left == null || !(left instanceof Rook) || left.hasMoved)
			validL = false;
		for(int x = 2; x > 0 && validL; x--){
			if(getWhoOccupiesAt(new Point(x, y)) != null)
				validL = false;
			if(validL && rules.cantCastleThroughCheck){
				Move test = new Move(new Point(3, y), new Point(x, y), this);
				if(test.putsPlayerInCheck(test.me)){
					validL = false;
				}
			}
		}
		
		boolean validR = true;
		Piece right = getPiece(new Point(7, y), color);
		if(right == null || !(right instanceof Rook) || right.hasMoved)
			validR = false;
		for(int x = 4; x < 7 && validR; x++){
			if(getWhoOccupiesAt(new Point(x, y)) != null)
				validR = false;
			if(validR && rules.cantCastleThroughCheck){
				Move test = new Move(new Point(3, y), new Point(x, y), this);
				if(test.putsPlayerInCheck(test.me)){
					validR = false;
				}
			}
		}
		
		if(validL){
			Move l = new Move(new Point(3, y), new Point(1, y), this);
			l.castlingMove = true;
			moves.add(l);
		}
		if(validR){
			Move r = new Move(new Point(3, y), new Point(5, y), this);
			r.castlingMove = true;
			moves.add(r);
		}
	}
	
	public boolean isInCheck(boolean player){
		ArrayList<Point> points = new ArrayList<>();
		if(player)
			points.addAll(blackPieces.keySet());
		else
			points.addAll(whitePieces.keySet());
		for(Point p: points){
			Piece piece = getPiece(p);
			for(Move move: piece.getMoves(this, p)){
				if(move.capturedKing)
					return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		String out = "";
		for(int y = 0; y < 8; y++){
			for(int x = 0; x < 8; x++){
				Piece p = getPiece(new Point(x, y));
				if(p == null)
					out += ("•");
				else
					out += p.toChar();
			}
			out += "\n";
		}
		out += String.format("It is %s's turn\n",turn ? "White" : "Black");
		return out;
	}
	public void print(){
		System.out.println(this);
		System.out.println(history.size() + ": " + history);
	}
	public AI getAI(){
		if(getIsAIPlayer())
			return turn ? white : black;
		System.err.println("Get AI ERROR");
		return null;
	}
	public boolean getIsAIPlayer(){
		if(rules.mode == RuleSet.GameMode.cvc)
			return true;
		if(rules.mode == RuleSet.GameMode.pvp)
			return false;
		if(rules.computerPlayer == turn)
			return true;
		return false;
	}
}
