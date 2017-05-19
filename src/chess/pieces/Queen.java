package chess.pieces;

import java.util.ArrayList;

import chess.Board;
import chess.Move;
import chess.Point;

public class Queen extends Piece
{
	public Queen(boolean c)
	{
		super(9, c);
	}

	public ArrayList<Move> getMoves(Board board, Point pos)
	{
		ArrayList<Move> moves = new ArrayList<Move>();
		
		return moves;
	}
}
