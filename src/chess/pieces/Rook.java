package chess.pieces;

import java.util.ArrayList;

import chess.Board;
import chess.Move;
import chess.Point;

public class Rook extends Piece
{
	private boolean hasMoved;
	
	public Rook(boolean c)
	{
		super(5, c);
		hasMoved = false;
	}

	public ArrayList<Move> getMoves(Board board, Point pos)
	{
		ArrayList<Move> moves = new ArrayList<Move>();
		
		return moves;
	}
}
