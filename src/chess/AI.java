package chess;

import java.util.ArrayList;

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
	
	public SimpleIntegerProperty skill;
	
	public AI(Board board, boolean player) {
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
		return new Move(new Point(0, 0), new Point(0, 0), board);
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
	
}
