package chess;

/**
 * Describes a move, the piece moving, and if a piece was captured
 */
public class Move {
	Board board;
	Board.Piece piece;
	Point from;
	Point to;
	
	Board.Piece capturedPiece;
	boolean invalidatedLeftCastle;
	boolean invalidatedRightCastle;
	boolean checkMated;
	
	public Move(Point from, Point to, Board board) {
		this.from = from;
		this.to = to;
		this.board = board;
	}
	
	/**
	 * Checks if move can be made, and sets rules such as checkMated, invalidated castle, and captured piece
	 * @return
	 * Boolean indicated whether the move can be made
	 */
	public boolean validateMove(){
		if(!from.isValid() || !to.isValid())
			return false;
		
		return false;
		//TODO validateMove
	}
	
	/**
	 * Does the move to the board
	 */
	public void doMove(){
		//TODO doMove
	}
	
	/**
	 * Undoes the move to the board
	 */
	public void undoMove(){
		//TODO undoMove
	}
	@Override
	public String toString() {
		String rv = String.format("%s %s -> %s", piece.toString(), from.toString(), to.toString());
		if(capturedPiece != null){
			rv += String.format(", Captured %s", capturedPiece.toString());
		}
		if(invalidatedLeftCastle){
			rv += ", invalidated left castle";
		}
		if(invalidatedRightCastle){
			rv += ", invalidated right castle";
		}
		if(checkMated){
			rv += ", checkmated opponent";
		}
		return rv;
	}
}
