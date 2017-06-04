package chess.pieces;

import java.util.ArrayList;

import chess.Board;
import chess.Move;
import chess.Point;

public abstract class Piece
{
	protected int value;
	protected boolean color;
	public boolean hasMoved;
	public int moves;

	public Piece(int v, boolean c)
	{
		value = v;
		color = c;
		hasMoved = false;
		moves = 0;
	}

	public boolean isWhite()
	{
		return color;
	}

	public double getValue()
	{
		double rv = value;
		if(this instanceof Pawn)
			rv = Math.round((1 + moves * .1)*100)/100;
		return rv;
	}
	
/*	protected void checkOptions(ArrayList<Move> moves, Board board){ //Removes moves where own piece already occupies
		for(Move move: moves){
			Boolean from = board.getWhoOccupiesAt(move.from);
			Boolean to = board.getWhoOccupiesAt(move.to);
			if(from.equals(null) || from.equals(to))
				moves.remove(move);
		}
	}	*/
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
	public char toChar(){
		if(this instanceof Knight)
			return color ? 'N' : 'n';
		if(color)
			return (Character.toUpperCase(toString().charAt(0)));
		else
			return (Character.toLowerCase(toString().charAt(0)));
	}
	@Override
	public boolean equals(Object obj) {
		return obj.toString().equals(this.toString());
	}
	public abstract ArrayList<Move> getMoves(Board board, Point pos);

}
