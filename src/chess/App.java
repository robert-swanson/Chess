package chess;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.*;
import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.scene.canvas.*;

public class App extends Application {
	
	Stage window;
	HashMap<Point, ImageView> whiteIcons;
	HashMap<Point, ImageView> blackIcons;
	Board board;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		board = new Board(false);
		
		whiteIcons = new HashMap<>();
		blackIcons = new HashMap<>();
	//Graphics
		window = primaryStage;
		window.setTitle("Chess");
		
		//Initialize
		StackPane layout = new StackPane();
//		Pane layout = new Pane();
		Canvas canvas = new Canvas(500, 500);
		
		initiateBoard(canvas);
		layout.getChildren().add(canvas);
		
		initiatePieces(layout, canvas.getWidth()/8);
		
		Scene board = new Scene(layout);
		board.setFill(Color.LIGHTGREY);
		window.setScene(board);
		
		//Show the Window
		window.setWidth(800);
		window.setHeight(800);
		window.show();
				
	}
	
	private void initiateBoard(Canvas canvas){
		GraphicsContext gc = canvas.getGraphicsContext2D();
		double xStep = canvas.getWidth()/8;
		double yStep = canvas.getHeight()/8;
		
		boolean startBlack = true;
		for(int y = 0; y < 8; y++){
			boolean black = startBlack;
			for(int x = 0; x < 8; x++){
				Color color = black ? Color.BROWN : Color.WHITE;
				gc.setFill(color);
				gc.fillRect(xStep*x, yStep*y, xStep, yStep);
				black = !black;
			}
			startBlack = !startBlack;	
		}
	}
	private void initiatePieces(Pane layout, double Step){
		for(Point point: board.blackPieces.keySet()){
			Board.Piece piece = board.blackPieces.get(point);
			ImageView icon = initPiece(point, false, piece, Step);
			blackIcons.put(point, icon);
			layout.getChildren().add(icon);
		}
		for(Point point: board.whitePieces.keySet()){
			Board.Piece piece = board.whitePieces.get(point);
			ImageView icon = initPiece(point, true, piece, Step);
			whiteIcons.put(point, icon);
			layout.getChildren().add(icon);
		}		
		//TODO: Download icons and initiate them
	}
	private ImageView initPiece(Point p, boolean player, Board.Piece piece, double Step){
		String color = player ? "White_" : "Black_";
		ImageView icon = new ImageView(getClass().getResource(color + piece.toString() + ".png").toString());
		icon.setPreserveRatio(true);
		icon.setFitWidth(.9*Step);
		return icon;
	}
	
	
	public static void main(String[] args){
		launch(args);
	}
}
