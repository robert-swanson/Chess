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
	
	boolean firstMove;

	public Move(Point from, Point to){
		this(from, to, null);
	}
	public Move(Point from, Point to, Board board)
	{
		this.from = from;
		this.to = to;
		this.board = board;
		this.piece = board.getPiece(from);
		this.firstMove = !piece.hasMoved;
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
		Boolean wFrom = board.getWhoOccupiesAt(from);
		Boolean wTo = board.getWhoOccupiesAt(to);
		boolean captured = wTo != null && wTo == !piece.isWhite();
		boolean me = piece.isWhite();
		if(wFrom != null && wFrom == me && (wTo == null || wTo == !me)){
			board.putPiece(board.removePiece(from, me), to);
			if(captured)
				board.removePiece(to, !me);
			board.turn = !board.turn;
		}
		if(firstMove)
			board.getPiece(to).hasMoved = true;
		
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
		else if(!me && board.blackPieces.containsKey(to)){
			board.blackPieces.put(from, board.blackPieces.remove(to));
		}
		if(capturedPiece != null)
			board.putPiece(capturedPiece, to);
		if(firstMove)
			board.getPiece(from).hasMoved = false;
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
