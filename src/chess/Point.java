package chess;

public class Point{
	public int x;
	public int y;

	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return String.format("(%d, %d)", x, y);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Point))
			return false;
		Point p = (Point)obj;
		return (x == p.x && y == p.y);
	}
	@Override
	public int hashCode() {
		return x + y*10;
	}
	public boolean isValid(){
		return(x >= 0 && x < 8 && y >= 0 && y < 8);
	}
}

