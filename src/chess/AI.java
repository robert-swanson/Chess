package chess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import chess.Board.State;
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
		
		boolean addRand;
		
		public Stratagy() {
			depth = 2;
			alphaBeta = true;
			transpositionTable = false;
			killerHeuristic =  false;
			killerHeuristicDepth = 0;
			iterativeDeepening = false;
			iterativedeepeningDepth = 0;
			addRand = false;
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
		ArrayList<Move> moves = getMoves(me);
		if(moves.size() == 0)
			return null;
		if(stratagy.depth == 0){	//Random Moves
			randomize(moves);
			return moves.get(0);
		}
		double alpha = -1000;
		double beta = 1000;
		for(Move m: moves){
			m.doMove();
			m.score = minimax(alpha, beta, me, 2);
			System.out.println(m.score);
			m.undoMove();
			if(stratagy.alphaBeta){
				if(alpha < m.score)
					alpha = m.score;
				if(alpha > beta){
					updateGameState(board);
					return getMaxMove(moves);
				}
			}
		}
		updateGameState(board);
		return getMaxMove(moves);
	}
	
	/**
	 * A recursive method that determines the score of a particular move
	 * @param alpha
	 * The minimum value for move to be acceptable
	 * @param beta
	 * The maximum value for move to be acceptable
	 * @return
	 * The number score of the move
	 */
	private double minimax(double alpha, double beta, boolean me, int depth){
		boolean maximizer = me == board.turn;
		boolean aB = stratagy.alphaBeta;
		if(depth >= stratagy.depth)
			return score(me);
		ArrayList<Move> moves = getMoves(board.turn);
		for(Move m: moves){
			m.doMove();
			m.score = minimax(alpha, beta, me, depth+1);
			m.undoMove();
			if(aB && maximizer && alpha < m.score)
				alpha = m.score;
			else if(aB && beta < m.score)
				beta = m.score;
			if(aB && alpha > beta){
				return (maximizer ? alpha : beta);
			}
		}
		return 0;
		//TODO Make minimax with alphabeta
	}	

	private Move getMaxMove(ArrayList<Move> moves){
		if(moves.size() == 0)
			return null;
		boolean me = moves.get(0).me;
		HashMap<Double, ArrayList<Move>> moveMap = new HashMap<>();
		double highest = -1000;
		double lowest = 1000;
		for(Move m: moves){
			Double key = m.score;
			if(!moveMap.containsKey(key))
				moveMap.put(key, new ArrayList<>());
			moveMap.get(key).add(m);
			if(key > highest)
				highest = key;
			if(key < lowest)
				lowest = key;
		}
		while(highest >= lowest){
			Number key = (Number)highest;
			if(moveMap.containsKey(key)){
				if(stratagy.addRand)
					randomize(moveMap.get(key));
				for(Move m: moveMap.get(key)){
					if(m == null){
						moves.remove(m);
						continue;
					}
					if(!m.putsPlayerInCheck(me))
						return m;
					else
						moves.remove(m);
				}
			}
			highest-= .05;
		}
		return null;
	}
	
	private Move getMinMove(ArrayList<Move> moves){
		if(moves.size() == 0)
			return null;
		boolean me = moves.get(0).me;
		HashMap<Number, ArrayList<Move>> moveMap = new HashMap<>();
		double highest = Double.MIN_VALUE;
		double lowest = Double.MAX_VALUE;
		for(Move m: moves){
			Number key = m.score;
			if(!moveMap.containsKey(key))
				moveMap.put(key, new ArrayList<>());
			moveMap.get(key).add(m);
			if(key.intValue() > highest)
				highest = key.intValue();
			if(key.intValue() < lowest)
				lowest = key.intValue();
		}
		while(lowest <= highest){
			Double key = (Double)lowest;
			if(moveMap.containsKey(key)){
				
				if(stratagy.addRand)
					randomize(moveMap.get(key));
				for(Move m: moveMap.get(key)){
					if(m == null){
						moves.remove(m);
						continue;
					}
					if(!m.putsPlayerInCheck(me))
						return m;
					else
						moves.remove(m);
				}
			}
			lowest++;
		}
		return null;
	}
	
	private void randomize(ArrayList<Move> moves){
		for(int i = 0; i < moves.size(); i++){
			int rand = (int)(Math.random() * moves.size());
			Move temp = moves.get(i);
			moves.set(i, moves.get(rand));
			moves.set(rand, temp);
		}
	}
	
	
	
	/**
	 * Determines all the legal moves a particular player can make on the current board state
	 * @param player
	 * The player
	 * @return
	 * An arrayList conataning all the legal moves
	 */
	private ArrayList<Move> getMoves(boolean me){
		HashMap<Point, Piece> pieces = board.getPieces(me);
		ArrayList<Move> moves = new ArrayList<>();
		for(Point point: pieces.keySet().toArray(new Point[pieces.size()])){
			Piece piece = board.getPiece(point, me);
			moves.addAll(piece.getMoves(board, point));
			if(!allowance.get() || halt){
				return null;
			}
		}
		return moves;
	}
	
	/**
	 * Evaluates the current board according to the piece values
	 * @return
	 * The total score in the perspective of the AI
	 */
	private double score(boolean maximizingPlayer){
		updateGameState(board);
		
		switch(board.gameState){
		case BLACKWON:
			return maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;
		case WHITEWON:
			return maximizingPlayer ? Integer.MAX_VALUE : Integer.MIN_VALUE;
		case STALEMATE:
			return 0;
		default:
		}
		
		double score = 0;
		for(Piece p: board.getPieces(maximizingPlayer).values())
			score += p.getValue();
		for(Piece p: board.getPieces(!maximizingPlayer).values())
			score -= p.getValue();
		return score;
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
				if(board.isInCheck(turn))
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
