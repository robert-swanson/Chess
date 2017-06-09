package chess;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
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
		boolean checkCheckAtEnd;
		
		public Stratagy() {
			depth = 2;
			alphaBeta = true;
			transpositionTable = false;
			killerHeuristic =  false;
			killerHeuristicDepth = 0;
			iterativeDeepening = false;
			iterativedeepeningDepth = 0;
			addRand = false;
			checkCheckAtEnd = false;
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
	ArrayList<Integer> bannedMoves;
	int[] diagnostics;
	
	//NODE
	public PrintWriter logger;
	public Node parent;

	
	public AI(Board board, boolean player, SimpleBooleanProperty a) {
		try {
			logger = new PrintWriter("ChessTree.txt");
		} catch (FileNotFoundException e1) {
			System.err.println("Could'nt create logger");
			e1.printStackTrace();
		}
		halt = false;
		allowance = a;
		allowance.addListener(e -> respondToKill());
		this.board = board;
		this.player = player;
		stratagy = new Stratagy();
		bannedMoves = new ArrayList<>();
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
		diagnostics = new int[stratagy.depth];
		parent = new Node(true);
		
		boolean me = board.turn;
		ArrayList<Move> moves = getMoves(me);
		board.removeCheckMoves(moves);
		updateBanned(4, 2);
		for(Move m: moves.toArray(new Move[moves.size()]))
			if(bannedMoves.contains(m.hashCode()) && moves.size() > 1){
				moves.remove(m);
				System.out.println("Broke Cycle");
			}
		if(moves.size() == 0)
			return null;
		if(stratagy.depth == 0){	//Random Moves
			randomize(moves);
			return moves.get(0);
		}
		double alpha = -1000;
		double beta = 1000;
		
		Move best = moves.get(0);
		diagnostics[0] = moves.size();
		for(Move m: moves){
			Node child = new Node(false, m);	//NODE
			
			m.doMove();
			m.score = minimax(alpha, beta, me, 2, child);
			best = setBest(best, m, true, true);
			
			child.score = m.score;				//NODE
			parent.children.add(child);			//NODE
				
			m.undoMove();
			if(stratagy.alphaBeta){
				if(alpha < m.score)
					alpha = m.score;
				if(alpha > beta){
					updateGameState(board);
					return best;
				}
			}
		}
		updateGameState(board);
		printDiagnostics();
		
		return best;
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
	private double minimax(double alpha, double beta, boolean me, int depth, Node parent){		//NODE
		boolean maximizer = me == board.turn;
		boolean aB = stratagy.alphaBeta;
		ArrayList<Move> moves = getMoves(board.turn);
		if(depth > stratagy.depth || moves.isEmpty())
			return score(me, null);
		Move best = moves.get(0);
		diagnostics[depth-1] += moves.size();
		for(Move m: moves){
			m.doMove();
			
			Node child = new Node(maximizer, m);					//NODE
			
			m.score = minimax(alpha, beta, me, depth+1, child);
			m.undoMove();
			
			child.score = m.score;									//NODE
			parent.children.add(child);								//NODE
			
			setBest(best, m, maximizer, false);
			if((maximizer && m.score > best.score)||(!maximizer && m.score < best.score))
				best = m;
			if(aB && maximizer && alpha < m.score)
				alpha = m.score;
			else if(aB && beta < m.score)
				beta = m.score;
			if(aB && alpha > beta){
				return (maximizer ? alpha : beta);
			}
		}
		return best.score;
//		if(maximizer)
//			return getMaxMove(moves).score;
//		return getMinMove(moves).score;
		//TODO Make minimax with alphabeta
	}
	private Move setBest(Move a, Move b, boolean maximizer, boolean top){
		if(maximizer){
			if(b.score > a.score){
				progressScore(b);
				return b;
			}
		}
		else{
			if(b.score < a.score){
				progressScore(b);
				return b;
			}
		}
		if(top && b.score ==  a.score){
			if(b.putsPlayerInCheck(!b.me) && !a.putsPlayerInCheck(!a.me)){
				progressScore(b);
				return b;
			}
			progressScore(b);
			if(a.progressScore < b.progressScore){
				return b;
			}
			else if(stratagy.addRand && Math.random()>.5){
				progressScore(b);
				return b;
			}
		}
		return a;
	}
	private void updateBanned(int maxLength, int maxRep){
		bannedMoves = new ArrayList<>();
		ArrayList<Integer> history = new ArrayList<>();
		for(Move m: board.history)
			history.add(m.hashCode());
		for(int l = 4; l <= maxLength; l += 2){
			if(history.size() < l * maxRep)
				continue;
			int[] pattern = new int[l];
			boolean patternBroken = false;
			for(int rep = 1; rep <= maxRep && !patternBroken; rep++){
				int[] pat = new int[l];
				for(int i = l-1; i >= 0 && !patternBroken; i--){
					int last = history.size()-1;
					int prevPat = (rep-1)*l;
					int fin = last - prevPat - (l-1) + i;
					pat[i] = history.get(fin);
				}
				if(rep == 1)
					pattern = pat;
				else if(!Arrays.equals(pat, pattern))
					patternBroken = true;
				else if(rep == maxRep){
					bannedMoves.add(pattern[0]);
				}
			}
		}
	}

	private Move getMaxMove(ArrayList<Move> moves){
		if(moves.size() == 0)
			return null;
		boolean me = moves.get(0).me;
		HashMap<Double, ArrayList<Move>> moveMap = new HashMap<>();
		Double highest = new Double(-1000);
		Double lowest = new Double(1000);
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
			Double key = (Double)highest;
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
			highest = fixFloatIssues(highest - .01);
		}
		return null;
	}
	
	private Move getMinMove(ArrayList<Move> moves){
		if(moves.size() == 0)
			return null;
		boolean me = moves.get(0).me;
		HashMap<Double, ArrayList<Move>> moveMap = new HashMap<>();
		Double highest = new Double(-1000);
		Double lowest = new Double(1000);
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
		while(lowest <= highest){
			Double key = (Double)highest;
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
			lowest = fixFloatIssues(lowest + .1);
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
	public double score(boolean maximizingPlayer, Move m){
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
		for(Piece p: board.getPieces(maximizingPlayer).values()){
			Point pos = p.position;
			int dist = (p.isWhite() == board.rules.topPlayer) ? pos.y-1 : 6 - pos.y;  
			score = fixFloatIssues(score + p.getValue(dist));
		}
		if(m != null)
			m.progressScore = score;
		for(Piece p: board.getPieces(!maximizingPlayer).values()){
			int dist = (p.isWhite() == board.rules.topPlayer) ? p.position.y-1 : 6 - p.position.y;  
			score =  fixFloatIssues(score - p.getValue(dist));
		}
		if(m != null)
			m.score = score;
		
		return score;
		//TODO Make score
	}
	
	public double progressScore(Move m){
		double score = 0;
		for(Piece p: board.getPieces(m.me).values()){
			int dist = (p.isWhite() == board.rules.topPlayer) ? p.position.y-1 : 6 - p.position.y;  
			score = fixFloatIssues(score + p.getValue(dist));
		}
		m.progressScore = score;
		return score;
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
	public static Double fixFloatIssues(Double n){
		return Double.parseDouble(String.format("%.2f", n));
	}
	public void printDiagnostics(){
		System.out.println("Diagnostics:");
		System.out.printf("Depth 0, Branching Factor: %d\n",diagnostics[0]);
		int totalNodes = diagnostics[0];
		int avBranchTotal = 0;
		int avBranchCount = 0;
		for(int d = 1; d < diagnostics.length; d++){
			double branch = (double)diagnostics[d]/diagnostics[d-1];
			System.out.printf("Depth %d, Nodes: %d, Average Branching Factor: %.3f\n", d, diagnostics[d], branch);
			totalNodes += diagnostics[d];
			avBranchTotal += branch;
			avBranchCount++;
		}
		System.out.printf("Total Nodes: %d, Total Average Branching Factor: %.3f\n", totalNodes, ((double)avBranchTotal)/((double)avBranchCount));
	}
	class Node{
		ArrayList<Node> children;
		double score = 0;
		String move = "";
		boolean maximizing;

		Node(boolean maximizing){
			this.maximizing = maximizing;
			children = new ArrayList<Node>();
		}
		Node(boolean maximizing, Move m){
			move = m.toString();
			this.maximizing = maximizing;
			children = new ArrayList<Node>();
		}
		@Override
		public String toString() {
			return String.format("Player: %s, Score: %(d", (maximizing)?"b":"w",score);
		}
		public void print(){
			try {
				logger = new PrintWriter("ChessTree.txt");
//				logger.println(System.currentTimeMillis());
			} catch (FileNotFoundException e) {
				System.out.println("Couldn't remake logger");
				e.printStackTrace();
			}
			print(0,-1,new ArrayList<Number>(0));
			logger.flush();
		}
		private void print(int indent,int max, ArrayList<Number> lasts){
			if(max >= 0 && indent > max) return;
			if(indent == 0) logger.printf("%s: Move Score: %(.2f\n", (maximizing ? "MAX" : "MIN"), score);
			if(children==null)
				return;
			int num = 0;
			for(Node child: children){
				boolean isLast = false;
				ArrayList<Number> nLasts = new ArrayList<Number>(lasts);
				if(children.size() == ++num){
					nLasts.add((Number)indent);
					isLast = true;
				}
				for(int j = 0; j < indent; j++){
					if(lasts.contains(j)){
						logger.print("      ");
					}
					else{
						logger.print("|     ");
					}
				}
				if(isLast)
					logger.print("L_____");
				else
					logger.print("|-----");
				logger.printf("%s: Score: %(.2f, Move: %s %s\n",child.maximizing ? "MAX" : "MIN", child.score, (!maximizing ? "Black" : "White"), child.move); //Flipped because its the children
									
				child.print(indent+1,max,nLasts);
			}
		}
	}
}
