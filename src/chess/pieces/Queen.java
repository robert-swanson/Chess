package chess.pieces;

import java.util.ArrayList;

import chess.Board;
import chess.Move;
import chess.Point;

public class Queen extends Piece
{
	public Queen(Point p)
	{
		super(9, p);
	}

	public ArrayList<Move> getMoves(Board board)
	{
		ArrayList<Move> moves = new ArrayList<Move>();
		
		return moves;
	}
}
