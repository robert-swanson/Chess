package chess.pieces;

import java.util.ArrayList;

import chess.Board;
import chess.Move;
import chess.Point;

public class Bishop extends Piece
{
	public Bishop(boolean c)
	{
		super(3,c);
	}

	public ArrayList<Move> getMoves(Board board, Point pos)
	{
		ArrayList<Move> moves = new ArrayList<Move>();
		
		return moves;
	}
}
