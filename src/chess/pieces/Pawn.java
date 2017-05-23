package chess.pieces;

import java.util.ArrayList;

import chess.Board;
import chess.Move;
import chess.Point;

public class Pawn extends Piece
{
	public Pawn(boolean c)
	{
		super(1, c);
		hasMoved = false;
	}

	public void updateHasMoved()
	{
		hasMoved = true;
	}

	public void resetHasMoved()
	{
		hasMoved = false;
	}

	//TODO check if in bounds
	public ArrayList<Move> getMoves(Board board, Point pos)
	{
		boolean up = board.rules.topPlayer != this.color;
		ArrayList<Move> moves = new ArrayList<Move>();
		Point jump = pos.getNewPoint(2, (up ? 0 : 4));
		Boolean jumpO = board.getWhoOccupiesAt(jump);
		
		Point capLeft = pos.getNewPoint(1, (up ? 7 : 5));
		Boolean capLeftO = board.getWhoOccupiesAt(capLeft);
		
		Point capRight = pos.getNewPoint(1, (up ? 1 : 3));
		Boolean capRightO = board.getWhoOccupiesAt(capRight);
		
		Point forward = pos.getNewPoint(1, (up ? 0 : 4));
		Boolean forwardO = board.getWhoOccupiesAt(forward);
		
		if(!hasMoved && jumpO == null)
			moves.add(new Move(pos, jump, board));
		if(capLeftO != null && capLeftO == !this.color)
			moves.add(new Move(pos, capLeft,board));
		if(capRightO != null && capRightO == !this.color)
			moves.add(new Move(pos, capRight, board));
		if(forwardO == null)
			moves.add(new Move(pos, forward,board));
		
		board.setCaptures(moves);
		return moves;
	}
}
