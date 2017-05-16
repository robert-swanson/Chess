package chess.pieces;

import java.util.ArrayList;

import chess.Board;
import chess.Move;
import chess.Point;

public class Rook extends Piece
{
	private boolean hasMoved;
	
	public Rook(Point p)
	{
		super(5, p);
		hasMoved = false;
	}

	public ArrayList<Move> getMoves(Board board)
	{
		ArrayList<Move> moves = new ArrayList<Move>();
		
		return moves;
	}
}
