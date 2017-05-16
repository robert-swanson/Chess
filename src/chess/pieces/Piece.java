package chess.pieces;

import java.util.ArrayList;

import chess.Board;
import chess.Move;
import chess.Point;

public abstract class Piece
{
	protected int value;
	protected Point pos;
	protected boolean color;
	
	public Piece(int v, Point p)
	{
		value = v;
		pos = p;
	}
	
	public int getValue()
	{
		return value;
	}
	
	public void setPos(Point p)
	{
		pos = p;
	}
	
	public Point getPos()
	{
		return pos;
	}
	
	public abstract ArrayList<Move> getMoves(Board board);
	
}
