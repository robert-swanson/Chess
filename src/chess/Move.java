package chess;

import java.util.ArrayList;

import chess.pieces.King;
import chess.pieces.Piece;

/**
 * Describes a move, the piece moving, and if a piece was captured
 */
public class Move
{
	Board board;
	Piece piece;
	public Piece changedTo;
	public Point from;
	public Point to;
	public boolean me;

	private Piece capturedPiece;
	boolean capturedKing;
	Boolean checks;
	
	boolean firstMove;
	public boolean castlingMove;

	public Move(Move m){
		this.board = m.board;
		this.piece = m.piece;
		this.changedTo = m.changedTo;
		this.from = m.from;
		this.to = m.to;
		this.me = m.me;
		this.capturedKing = m.capturedKing;
		this.checks = m.checks;
		this.firstMove = m.firstMove;
		this.castlingMove = m.castlingMove;
	}
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
		this.castlingMove = false;
		changedTo = null;
		me = piece.isWhite();
	}
	
	public Piece getCapture(){
		return capturedPiece;
	}
	public void setCapture(Piece cap){
		capturedPiece = cap;
		if(cap instanceof King){
			capturedKing = true;
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
		board.turn = !board.turn;
		Boolean wFrom = board.getWhoOccupiesAt(from);
		Boolean wTo = board.getWhoOccupiesAt(to);
		boolean captured = wTo != null && wTo == !me;
		if(wFrom != null && wFrom == me && (wTo == null || wTo == !me)){
			board.putPiece(board.removePiece(from, me), to);
			if(captured)
				board.removePiece(to, !me);
		}
		if(firstMove)
			board.getPiece(to).hasMoved = true;
		if(castlingMove){
			boolean left = to.x < 3;
			int y = me == board.rules.topPlayer ? 0 : 7;
			if(left){
				board.putPiece(board.removePiece(new Point(0, y), me), new Point(2, y));
			}
			else{
				board.putPiece(board.removePiece(new Point(7, y), me), new Point(4, y));
			}
		}
		else if(changedTo != null)
			board.putPiece(changedTo, to);
		if(checks != null && checks && board.rules.cantCastleAfterCheck)
			board.getKing(!me).hasMoved = true;
		return captured;
	}

	/**
	 * Undoes the move to the board, and replaced captured pieces
	 */
	public Piece undoMove()
	{
		board.turn = !board.turn;
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
		if(checks != null && checks)
			board.getKing(!me).hasMoved = false;
		if(castlingMove){
			boolean left = to.x < 4;
			int y = me == board.rules.topPlayer ? 0 : 7;
			if(left)
				board.putPiece(board.removePiece(new Point(2, y), me), new Point(0, y));
			else
				board.putPiece(board.removePiece(new Point(4, y), me), new Point(7, y));

		}
		if(changedTo != null)
			board.putPiece(piece, from);
		return capturedPiece;
	}
	
	public boolean putsPlayerInCheck(boolean color){
		doMove();
		ArrayList<Point> points = new ArrayList<>();
		if(color)
			points.addAll(board.blackPieces.keySet());
		else
			points.addAll(board.whitePieces.keySet());
		for(Point p: points){
			Piece piece = board.getPiece(p);
			for(Move move: piece.getMoves(board, p)){
				if(move.capturedKing){
					undoMove();
					if(board.rules.cantCastleAfterCheck && !board.getKing(!me).hasMoved)
						checks = true;
					return true;
				}
					
			}
		}
		undoMove();
		checks = false;
		return false;
	}

	@Override
	public String toString()
	{
		String rv = String.format("%s %s -> %s",piece, from.toString(), to.toString());
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
			boolean t = changedTo != null;
			boolean i = m.changedTo != null;
			if(t && i)
				t = changedTo.equals(m.changedTo);
				
			return m.from.equals(from) && m.to.equals(to) && m.piece.equals(piece) && t == i;
		}
		return false;
	}
}
