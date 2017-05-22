package chess.pieces;

import java.util.ArrayList;

import chess.Board;
import chess.Move;
import chess.Point;

public abstract class Piece
{
	protected int value;
	protected boolean color;

	public Piece(int v, boolean c)
	{
		value = v;
		color = c;
	}

	public boolean isWhite()
	{
		return color;
	}

	public int getValue()
	{
		return value;
	}
	protected void checkOptions(ArrayList<Move> moves, Board board){ //Removes moves where own piece already occupies
		for(Move move: moves){
			Boolean from = board.getWhoOccupiesAt(move.from);
			Boolean to = board.getWhoOccupiesAt(move.to);
			if(from.equals(null) || from.equals(to))
				moves.remove(move);
		}
	}
	@Override
	public String toString() {
		if(this instanceof Bishop)
			return "Bishop";
		else if(this instanceof King)
			return "King";
		else if(this instanceof Knight)
			return "Knight";
		else if(this instanceof Pawn)
			return "Pawn";
		else if(this instanceof Queen)
			return "Queen";
		else if(this instanceof Rook)
			return "Rook";
		else
			return "Unkown Piece";
	}
	@Override
	public boolean equals(Object obj) {
		return obj.toString().equals(this.toString());
	}
	public abstract ArrayList<Move> getMoves(Board board, Point pos);

}
