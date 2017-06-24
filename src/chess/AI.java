package chess;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import chess.pieces.Piece;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;

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
		int checkDepth;
		
		boolean addRand;
		
		public Stratagy() {
			depth = 3;
			alphaBeta = true;
			transpositionTable = true;
			killerHeuristic =  false;
			killerHeuristicDepth = 0;
			iterativeDeepening = true;
			iterativedeepeningDepth = 3;
			addRand = true;
			checkDepth = 2;
		}
		public Stratagy(String importString){
			this();
			String[] strat = importString.split(":");
			if(strat.length != 4)
				return;
			depth = Integer.parseInt(strat[0]);
			checkDepth = Integer.parseInt(strat[1]);
			alphaBeta = Boolean.parseBoolean(strat[2]);
			addRand = Boolean.parseBoolean(strat[3]);
		}
		@Override
		public String toString() {
		return String.format("Stratagy\n"
				+ "Depth: %d\n"
				+ "Check Depth: %d\n"
				+ "AlphaBeta: %b\n"
				+ "Add Random Element: %b\n"
				+ "Transposition Table: %b\n"
				+ "Killer Heuristic: %b Depth: %d\n"
				+ "Iterative Deepening: %b Depth: %d\n",depth, checkDepth, alphaBeta, addRand,  transpositionTable, killerHeuristic, killerHeuristicDepth, iterativeDeepening, iterativedeepeningDepth);
		}
		
		public String export(){
			return String.format("%d:%d:%b:%b", depth, checkDepth, alphaBeta, addRand);
		}
	}
	Board board;
	Stratagy stratagy;
	boolean player;
	SimpleBooleanProperty allowance;
	boolean halt;
	ArrayList<Integer> bannedMoves;
	int[] diagnostics;
	int avBranchTotal;
	int avBranchCount;
	final int progressDepth = 2;
	
	SimpleDoubleProperty progress;
	
	public double confidence;
	public int[][] keys;
	HashMap<Integer, Double>[] transpositionTable;
	Move best;
	
	
	//NODE
	public PrintWriter logger;
	public Node parent;

	
	@SuppressWarnings("unchecked")
	public AI(Board board, boolean player, SimpleBooleanProperty a, SimpleDoubleProperty p) {
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
		progress = p;
		
		transpositionTable = new HashMap[stratagy.depth];
		keys = new int[64][16];
		for(int pos = 0; pos < 64; pos++){
			for(int i = 0; i < 16; i++){
				keys[pos][i] = ThreadLocalRandom.current().nextInt();
			}
		}
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
		ArrayList<Move> moves = new ArrayList<>();
		for(Point p: board.getPieces(player).keySet()){
			moves.addAll(board.getPiece(p).getMoves(board, p));
		}
		board.addCastleMoves(moves, player);
		board.removeCheckMoves(moves);
		return moves;
	}
	
	/**
	 * Determines the best move to make
	 * @return
	 * The best move
	 */
	@SuppressWarnings("unchecked")
	public Move getBestMove(){
		if(stratagy.iterativeDeepening){
			return iterativeGetBestMove();
		}
//		if(transpositionTable.length != stratagy.depth)
		transpositionTable = new HashMap[stratagy.depth];
		for(int d = 0; d < stratagy.depth; d++){
//			if(transpositionTable[d] == null)
			transpositionTable[d] = new HashMap<>();
		}
		progress.set(0.0);						//PROGRESS
		board.startTimer();
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
		
		best = moves.get(0);
		diagnostics[0] = moves.size();
		int index = 0;
		double step = 1.0/moves.size();			//PROGRESS
		for(Move m: moves){
			Node child = new Node(false, m);	//NODE
			child.alpha = alpha;
			child.beta = beta;
			m.doMove();
			m.score = minimax(alpha, beta, me, 1, stratagy.depth, child, step);
			m.undoMove();
			best = setBest(best, m, true, true);
			
			child.score = m.score;				//NODE
			parent.children.add(child);			//NODE
				
			index++;
			if(stratagy.alphaBeta){
				if(alpha < m.score)
					alpha = m.score;
				if(alpha > beta){
					progress.set(progress.get() + (moves.size()-index) * step); //PROGRESS
					return best;
				}
			}
		}		
		confidence = best.score;
		board.endTimer();
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
	private double minimax(double alpha, double beta, boolean me, int depth, int maxDepth, Node parent, double whole){		//NODE/PROGRESS
		boolean maximizer = me == board.turn;
		boolean aB = stratagy.alphaBeta;
		ArrayList<Move> moves = getMoves(board.turn);
		
		
		if(maxDepth >= depth)
			board.removeCheckMoves(moves);
			
		double step = whole/moves.size();			//PROGRESS
		if(depth > maxDepth || moves.isEmpty()){
			double score = score(me, null, moves);
			return score;
		}
		if(stratagy.iterativeDeepening && depth != maxDepth && !(depth > stratagy.iterativedeepeningDepth))
			sortMoves(moves, depth);
		Move best = moves.get(0);
		int index = 0;
		for(Move m: moves){
			if(!allowance.get() && best != null)
				return best.score;
			Node child = new Node(!maximizer, m);					//NODE
			child.alpha = alpha;									//NODE
			child.beta = beta;										//NODE
			m.doMove();
			int hash = board.hashCode(keys);
			if(stratagy.transpositionTable && (!stratagy.iterativeDeepening || depth > stratagy.iterativedeepeningDepth) && transpositionTable[depth-1].containsKey(hash)){
				m.score = transpositionTable[depth-1].get(hash);
				child.table = true;
			}
			else{
				m.score = minimax(alpha, beta, me, depth+1, maxDepth, child, step);
			}
			if(stratagy.transpositionTable)
				tranpositionAdd(depth-1, hash, m.score);
			m.undoMove();

			child.score = m.score;									//NODE
			parent.children.add(child);								//NODE
			best = setBest(best, m, maximizer, false);
			if((maximizer && m.score > best.score)||(!maximizer && m.score < best.score))
				best = m;
			if(aB && maximizer && alpha < m.score)
				alpha = m.score;
			else if(aB && !maximizer && beta > m.score)
				beta = m.score;
			if(aB && (alpha > beta || (alpha == beta && !(alpha == best.score)))){
				if(depth <= progressDepth){
					progress.set(progress.get() + (moves.size()-index) * step); //PROGRESS
				}
				child.pruned = true;
				best = m;
				return m.score;
//				return (maximizer ? alpha : beta);
			}
			index++;
		}
		if(depth == progressDepth){
			progress.set(progress.get()+whole);						//PROGRESS
		}
		return best.score;
	}
	@SuppressWarnings("unchecked")
	private Move iterativeGetBestMove(){
		transpositionTable = new HashMap[stratagy.depth];
		best = null;
		for(int d = 1; d <= stratagy.depth; d++){
			if(!allowance.get() && best != null)
				return best;
			boolean finalCalc = (d > stratagy.iterativedeepeningDepth);
			for(int de = 0; de < stratagy.depth; de++){		//Transposition Table
				if(transpositionTable[de] == null)
				transpositionTable[de] = new HashMap<>();
			}
			
			progress.set(0.0);								//PROGRESS
			parent = new Node(true);						//Node
			
			boolean me = board.turn;						//Moves
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
			if(best == null)
				best = moves.get(0);
			if(stratagy.depth == 0){						//Random Moves
				randomize(moves);
				return moves.get(0);
			}
			double alpha = -1000;							//Alpha-Beta
			double beta = 1000;
			
			int index = 0;
			double step = 1.0/moves.size();			//PROGRESS
			for(Move m: moves){
				if(!allowance.get())
					return best;
				Node child = new Node(false, m);	//NODE
				child.alpha = alpha;
				child.beta = beta;
				m.doMove();
				m.score = minimax(alpha, beta, me, 1, finalCalc ? stratagy.depth : d, child, step);
				m.undoMove();
				best = setBest(best, m, true, true);
				
				child.score = m.score;				//NODE
				parent.children.add(child);			//NODE
					
				index++;
				if(stratagy.alphaBeta){
					if(alpha < m.score)
						alpha = m.score;
					if(alpha > beta){
						progress.set(progress.get() + (moves.size()-index) * step); //PROGRESS
						return best;
					}
				}
			}		
			confidence = best.score;
			if(finalCalc)
				break;
		}
		return best;
	}
	private void sortMoves(ArrayList<Move> moves, int depth){
		for(int i = 0; i < moves.size(); i++){
			Move m = moves.get(i);
			m.doMove();
			int hash = board.hashCode(keys);
			m.undoMove();
			Double score = transpositionTable[depth].get(hash);
			if(score != null){
				m.score = score;
			}
			else
				return;
		}
		Collections.sort(moves);
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

/*	private Move getMaxMove(ArrayList<Move> moves){
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
*/	
	private void randomize(ArrayList<Move> moves){
		for(int i = 0; i < moves.size(); i++){
			int rand = (int)(Math.random() * moves.size());
			Move temp = moves.get(i);
			moves.set(i, moves.get(rand));
			moves.set(rand, temp);
		}
	}
	
	private void tranpositionAdd(int depth, int hash, double value){
		HashMap<Integer, Double> map = transpositionTable[depth];
		if(map.size()>100000){
			map.remove(map.keySet().iterator().next());
		}
		if(map.size() > 100001)
			System.out.println("BIG");
		map.put(hash, value);
	}
	
	
	/**
	 * Determines all the legal moves a particular player can make on the current board state
	 * @param player
	 * The player
	 * @return
	 * An arrayList containing all the legal moves
	 */
	private ArrayList<Move> getMoves(boolean me){
		HashMap<Point, Piece> pieces = board.getPieces(me);
		ArrayList<Move> moves = new ArrayList<>();
		board.removeCastleOutOfCheck(moves, me);
		for(Point point: pieces.keySet().toArray(new Point[pieces.size()])){
			Piece piece = board.getPiece(point, me);
			moves.addAll(piece.getMoves(board, point));
		}
		return moves;
	}
	
	/**
	 * Evaluates the current board according to the piece values
	 * @return
	 * The total score in the perspective of the AI
	 */
	public double score(boolean maximizingPlayer, Move m, ArrayList<Move> moves){
		updateGameState(board, moves);
		
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
	public static void updateGameState(Board board, ArrayList<Move> moves){
		boolean turn = board.turn;
			if(moves == null){
				moves = new ArrayList<>();
				int s = board.getPieces(turn).size();
				Point[] points = board.getPieces(turn).keySet().toArray(new Point[s]);
				for(Point p: points){
					ArrayList<Move> piecesMoves = new ArrayList<>();
					Piece piece = board.getPiece(p, turn);
					piecesMoves.addAll(piece.getMoves(board, p));
					board.removeCheckMoves(piecesMoves);
					moves.addAll(piecesMoves);
					if(moves.size() > 0)
						break;
				}
			}
			if(moves.size() == 0){
				if(board.isInCheck(turn))
					board.gameState = turn ? Board.State.BLACKWON : Board.State.WHITEWON;
				else
					board.gameState = Board.State.STALEMATE;
			}
			else
				board.gameState = Board.State.INPROGRESS;
	}
	public static Double fixFloatIssues(Double n){
		return Double.parseDouble(String.format("%.2f", n));
	}
	public void printDiagnostics(){
		System.out.println("Diagnostics:");
		System.out.printf("Depth 0, Branching Factor: %d\n",diagnostics[0]);
		int totalNodes = diagnostics[0];
		avBranchTotal = 0;
		avBranchCount = 0;
		for(int d = 1; d < diagnostics.length; d++){
			double branch = (double)diagnostics[d]/diagnostics[d-1];
			System.out.printf("Depth %d, Nodes: %d, Average Branching Factor: %.3f\n", d, diagnostics[d], branch);
			totalNodes += diagnostics[d];
			avBranchTotal += branch;
			avBranchCount++;
		}
		System.out.printf("Total Nodes: %d, Total Average Branching Factor: %.3f\n", totalNodes, ((double)avBranchTotal)/((double)avBranchCount));
	}
	@Override
	public String toString() {
		return board.toString();
	}
	class Node{
		ArrayList<Node> children;
		double score = 0;
		String move = "";
		boolean maximizing;
		boolean table;
		boolean pruned;
		double alpha;
		double beta;

		Node(boolean maximizing){
			this.maximizing = maximizing;
			children = new ArrayList<Node>();
			table = false;
			pruned = false;
			alpha = -999;
			beta = 999;
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
					logger.print("L___");
				else
					logger.print("|---");
				for(int i = 0; i < 3-indent; i++){
					if(isLast)
						System.out.print("_");
					else
						System.out.print("_");
				}
				logger.printf("%s, Score: %.1f, Alpha:%.1f, Beta:%.1f, Move: %s, %s%s\n",child.maximizing ? "MAX" : "MIN", child.score, child.alpha, child.beta, child.move, child.table ? "Table" : "", child.pruned ? "XXX" : ""); //Flipped because its the children
									
				child.print(indent+1,max,nLasts);
			}
		}
	}
}
