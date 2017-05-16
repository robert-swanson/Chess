package chess.pieces;

import java.util.ArrayList;

import chess.Board;
import chess.Move;
import chess.Point;

public class Bishop extends Piece
{
	public Bishop(Point p)
	{
		super(3, p);
	}

	public ArrayList<Move> getMoves(Board board)
	{
		ArrayList<Move> moves = new ArrayList<Move>();
		
		return moves;
	}
}
