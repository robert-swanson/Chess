package chess;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.scene.canvas.*;

public class App extends Application {
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
	}

	Stage window;
	HashMap<Point, ImageView> whiteIcons;
	HashMap<Point, ImageView> blackIcons;
	Board board;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		board = new Board(false);
	//Graphics
		window = primaryStage;
		window.setTitle("Chess");
		
		//Initialize
		StackPane layout = new StackPane();
		Canvas canvas = new Canvas(500, 500);
		
		initiateBoard(canvas);
		
		

		layout.getChildren().add(canvas);
		Scene board = new Scene(layout);
		board.setFill(Color.LIGHTGREY);
		window.setScene(board);
		
		//Show the Window
		window.setWidth(800);
		window.setHeight(800);
		window.show();
	}
	
	public static void initiateBoard(Canvas canvas){
		GraphicsContext gc = canvas.getGraphicsContext2D();
		double xStep = canvas.getWidth()/8;
		double yStep = canvas.getHeight()/8;
		
		boolean startBlack = true;
		for(int y = 0; y < 8; y++){
			boolean black = startBlack;
			for(int x = 0; x < 8; x++){
				Color color = black ? Color.BLACK : Color.WHITE;
				gc.setFill(color);
				gc.fillRect(xStep*x, yStep*y, xStep, yStep);
				black = !black;
			}
			startBlack = !startBlack;	
		}
		
		
		gc.setFill(Color.BLACK);
		Circle ball = new Circle(5);
		ball.setCenterX(40);
		
	}
	public static void initiatePieces(Canvas canvas){
		GraphicsContext gc = canvas.getGraphicsContext2D();
		double xStep = canvas.getWidth()/8;
		double yStep = canvas.getHeight()/8;
		
		//TODO: Download icons and initiate them
	}
	
	
	public static void main(String[] args){
		launch(args);
	}
}
