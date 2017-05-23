package chess;

import chess.pieces.King;
import chess.pieces.Piece;

/**
 * Describes a move, the piece moving, and if a piece was captured
 */
public class Move
{
	Board board;
	Piece piece;
	public Point from;
	public Point to;

	private Piece capturedPiece;
	boolean capturedKing;
	
	boolean specialMove;

	public Move(Point from, Point to){
		this(from, to, null);
	}
	public Move(Point from, Point to, Board board)
	{
		this(from, to, board, false);
	}
	
	public Piece getCapture(){
		return capturedPiece;
	}
	public void setCapture(Piece cap){
		capturedPiece = cap;
		if(cap instanceof King){
			capturedKing = true;
			System.out.println(this);
		}
	}
	
	public Move(Point from, Point to, Board board, boolean specialMove)
	{
		this.from = from;
		this.to = to;
		this.board = board;
		this.specialMove = specialMove;
		this.piece = board.getPiece(from);
		
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
	public boolean doMove()
	{
		boolean captured = false;
		if(piece.isWhite() && board.whitePieces.containsKey(from)){
			board.whitePieces.put(to, board.whitePieces.remove(from));
			if(board.blackPieces.containsKey(to)){
				board.blackPieces.remove(to);
				captured = true;
			}
		}
		else if(board.blackPieces.containsKey(from)){
			board.blackPieces.put(to, board.blackPieces.remove(from));
			if(board.whitePieces.containsKey(to)){
				board.whitePieces.remove(to);
				captured = true;
			}
		}
		board.turn = !board.turn;
		return captured;
	}

	/**
	 * Undoes the move to the board, and replaced captured pieces
	 */
	public Piece undoMove()
	{
		boolean me = piece.isWhite();
		if(me && board.whitePieces.containsKey(to)){
			board.whitePieces.put(from, board.whitePieces.remove(to));
		}
		else if(!me && board.blackPieces.containsKey(from)){
			board.blackPieces.put(from, board.blackPieces.remove(from));
		}
		if(capturedPiece != null)
			board.putPiece(capturedPiece, to, !me);
		return capturedPiece;
	}

	@Override
	public String toString()
	{
		String rv = String.format("%s -> %s",from.toString(), to.toString());
		if (capturedPiece != null)
		{
			rv += String.format(", Captured %s", capturedPiece.toString());
		}
		return rv;
	}
	
	@Override
	public boolean equals(Object obj) {		//Ignores properties
		if(obj instanceof Move){
			Move m = (Move)obj;
			return m.from.equals(from) && m.to.equals(to) && m.piece.equals(piece);
		}
		return false;
	}
}
