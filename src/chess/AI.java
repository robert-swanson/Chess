package chess;

import java.util.ArrayList;

import javafx.beans.property.SimpleIntegerProperty;

public class AI {
	Board board;
	boolean player;
	
	public SimpleIntegerProperty skill;
	
	public AI(Board board, boolean player) {
		this.board = board;
		this.player = player;
	}
	
	public static ArrayList<Move> getLegalMoves(boolean player, Board board){
		return new ArrayList<>();
		//TODO Make static getLegalMoves
	}
	
	public Move getBestMove(){
		return new Move(new Point(0, 0), new Point(0, 0), board);
		//TODO Make getBestMove
	}
	private double minimax(double alpha, double beta){
		return 0;
		//TODO Make minimax with alphabeta
	}
	
	private ArrayList<Move> getMoves(){
		return new ArrayList<>();
		//TODO Make dynamic getMoves
	}
	
	private double score(){
		return 0;
		//TODO Make score
	}
	
}
