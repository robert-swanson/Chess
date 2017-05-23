package chess.pieces;

import java.util.ArrayList;

import chess.Board;
import chess.Move;
import chess.Point;

public class King extends Piece
{	
	public King(boolean c)
	{
		super(100, c);
		hasMoved = false;
	}
	
	public boolean getHasMoved(){
		return hasMoved;
	}

	//Still to do
	public ArrayList<Move> getMoves(Board board, Point pos)
	{
		ArrayList<Move> moves = new ArrayList<Move>();
		
		for (int i = 0; i < 8; i++)
		{
			Point to = pos.getNewPoint(1, i);
			if(to.isInBoard())
				moves.add(new Move(pos, to, board));
		}
		board.addCastleMoves(moves, isWhite());
		board.setCaptures(moves);
		return moves;
	}
}
