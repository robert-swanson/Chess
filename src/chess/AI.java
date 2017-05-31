package chess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import chess.pieces.Piece;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Maneges the logic for the chess AI
 */
public class AI {
	public class Stratagy{
		int depth;
		boolean alphaBeta;
		boolean transpositionTable;
		boolean killerHeuristic;
		int killerHeuristicDepth;
		boolean iterativeDeepening;
		int iterativedeepeningDepth;
		
		public Stratagy() {
			depth = 5;
			alphaBeta = true;
			transpositionTable = false;
			killerHeuristic =  false;
			killerHeuristicDepth = 0;
			iterativeDeepening = false;
			iterativedeepeningDepth = 0;
		}
		@Override
		public String toString() {
		return String.format("Stratagy\n"
				+ "Depth: %d\n"
				+ "AlphaBeta: %b\n"
				+ "Transposition Table: %b\n"
				+ "Killer Heuristic: %b Depth: %d\n"
				+ "Iterative Deepening: %b Depth: %d\n",depth, alphaBeta, transpositionTable, killerHeuristic, killerHeuristicDepth, iterativeDeepening, iterativedeepeningDepth);
		}
	}
	Board board;
	Stratagy stratagy;
	boolean player;
	SimpleBooleanProperty allowance;
	boolean halt;
	
//	public SimpleIntegerProperty skill;
	
	public AI(Board board, boolean player, SimpleBooleanProperty a) {
		halt = false;
		allowance = a;
		allowance.addListener(e -> respondToKill());
		this.board = board;
		this.player = player;
		stratagy = new Stratagy();
	}
	
	/**
	 * Determines the legal moves a player can make
	 * @param player
	 * The player
	 * @param board
	 * The board
	 * @return
	 * An arrayList containing all the legal moves
	 */
	public static ArrayList<Move> getLegalMoves(boolean player, Board board){
		return new ArrayList<>();
		//TODO Make static getLegalMoves
	}
	
	/**
	 * Determines the best move to make
	 * @return
	 * The best move
	 */
	public Move getBestMove(){
		boolean me = board.turn;
		HashMap<Point, Piece> pieces = board.getPieces(me);
		ArrayList<Move> moves = new ArrayList<>();
		for(Point point: pieces.keySet().toArray(new Point[pieces.size()])){
			Piece piece = board.getPiece(point, me);
			moves.addAll(piece.getMoves(board, point));
			
			if(!allowance.get() || halt){
				return null;
			}
		}
		board.removeCheckMoves(moves);
		int rand = 100 % moves.size();
//		int rand = (int) (Math.random() * moves.size());
		return moves.get(rand);
		
		//TODO Make getBestMove
	}
	
	/**
	 * A recursive method that determines the score of a particiular move
	 * @param alpha
	 * The minimum value for move to be acceptable
	 * @param beta
	 * The maximum value for move to be acceptable
	 * @return
	 * The number score of the move
	 */
	private double minimax(double alpha, double beta){
		return 0;
		//TODO Make minimax with alphabeta
	}
	
	/**
	 * Determines all the legal moves a particular player can make on the current board state
	 * @param player
	 * The player
	 * @return
	 * An arrayList conataning all the legal moves
	 */
	private ArrayList<Move> getMoves(boolean player){
		return new ArrayList<>();
		//TODO Make dynamic getMoves
	}
	
	/**
	 * Evaluates the current board according to the piece values
	 * @return
	 * The total score in the perspective of the AI
	 */
	private double score(){
		return 0;
		//TODO Make score
	}
	
	public void respondToKill(){
		halt = !allowance.get();
	}
	public void reactivate(){
		halt = false;
	}
	public static void updateGameState(Board board){
		boolean turn = board.turn;
		if(turn){
			ArrayList<Move> moves = new ArrayList<>();
			int s = board.whitePieces.size();
			Point[] points = board.whitePieces.keySet().toArray(new Point[s]);
			for(Point p: points){
				ArrayList<Move> piecesMoves = new ArrayList<>();
				Piece piece = board.getPiece(p, true);
				piecesMoves.addAll(piece.getMoves(board, p));
				board.removeCheckMoves(piecesMoves);
				moves.addAll(piecesMoves);
				if(moves.size() > 0)
					break;
			}
			if(moves.size() == 0){
				if(board.history.peek().putsPlayerInCheck(turn))
					board.gameState = Board.State.BLACKWON;
				else
					board.gameState = Board.State.STALEMATE;
			}
			else
				board.gameState = Board.State.INPROGRESS;
		}
		else{
			ArrayList<Move> moves = new ArrayList<>();
			int s = board.blackPieces.size();
			Point[] points = board.blackPieces.keySet().toArray(new Point[s]);
			for(Point p: points){
				ArrayList<Move> piecesMoves = new ArrayList<>();
				Piece piece = board.getPiece(p, false);
				piecesMoves.addAll(piece.getMoves(board, p));
				board.removeCheckMoves(piecesMoves);
				moves.addAll(piecesMoves);
				if(moves.size() > 0)
					break;
			}
			if(moves.size() == 0){
				System.out.println("Game Over");
				if(board.history.peek().putsPlayerInCheck(turn))
					board.gameState = Board.State.WHITEWON;
				else
					board.gameState = Board.State.STALEMATE;
			}
			else
				board.gameState = Board.State.INPROGRESS;
		}
	}
	
}
