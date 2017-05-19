package chess;

import chess.pieces.Piece;

/**
 * Describes a move, the piece moving, and if a piece was captured
 */
public class Move
{
	Board board;
	Piece piece;
	Point from;
	Point to;

	Piece capturedPiece;
	boolean checkMated;
	
	boolean specialMove;

	public Move(Point from, Point to){
		this(from, to, null);
	}
	public Move(Point from, Point to, Board board)
	{
		this(from, to, board, false);
	}
	
	//Might need to add the piece;
	public Move(Point from, Point to, Board board, boolean specialMove)
	{
		this.from = from;
		this.to = to;
		this.board = board;
		this.specialMove = specialMove;
		//TODO
		//Check to see if there is a piece at the "to" point, if there is then make it the caputured piece
	}

	/**
	 * Checks if move can be made, and sets rules such as checkMated,
	 * invalidated castle, and captured piece
	 * 
	 * @return Boolean indicated whether the move can be made
	 */
	public boolean validateMove()
	{
		if (!from.isInBoard() || !to.isInBoard())
			return false;

		return false;
		// TODO validateMove
	}

	/**
	 * Does the move to the board
	 */
	public void doMove()
	{
		// TODO doMove
	}

	/**
	 * Undoes the move to the board, and replaced captured pieces
	 */
	public void undoMove()
	{
		// TODO undoMove
	}

	@Override
	public String toString()
	{
		String rv = String.format("%s %s -> %s", piece.toString(), from.toString(), to.toString());
		if (capturedPiece != null)
		{
			rv += String.format(", Captured %s", capturedPiece.toString());
		}
		if (checkMated)
		{
			rv += ", checkmated opponent";
		}
		return rv;
	}
	
	@Override
	public boolean equals(Object obj) {		//Ignores properties
		if(obj instanceof Move){
			Move m = (Move)obj;
			return m.from == from && m.to == to && m.piece == piece;
		}
		return false;
	}
}
