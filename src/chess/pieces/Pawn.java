package chess.pieces;

import java.util.ArrayList;

import chess.Board;
import chess.Move;
import chess.Point;

public class Pawn extends Piece
{
	private boolean hasMoved;

	public Pawn(Point pos)
	{
		super(1, pos);
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
	public ArrayList<Move> getMoves(Board board)
	{
		ArrayList<Move> moves = new ArrayList<Move>();

//		if(!hasMoved)
//		{
//			moves.add(new Move(pos, pos.getNewPoint(2, 0), board, true));
//			moves.add(new Move(pos, pos.getNewPoint(1, 0), board, true));
//			
//			Piece p = board.getPiece(pos.getNewPoint(1, 7));
//			if(p.color != this.color)
//				moves.add(new Move(pos, pos.getNewPoint(1, 7), board, true));
//			
//			Piece p2 = board.getPiece(pos.getNewPoint(1, 1));
//			if(p.color != this.color)
//				moves.add(new Move(pos, pos.getNewPoint(1, 1), board, true));
//		}
//		else
//		{
//			moves.add(new Move(pos, pos.getNewPoint(1, 0), board));
//
//			Piece p = board.getPiece(pos.getNewPoint(1, 7));
//			if(p.color != this.color)
//				moves.add(new Move(pos, pos.getNewPoint(1, 7), board));
//			
//			Piece p2 = board.getPiece(pos.getNewPoint(1, 1));
//			if(p.color != this.color)
//				moves.add(new Move(pos, pos.getNewPoint(1, 1), board));
//		}

		return moves;
	}
}
