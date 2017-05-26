package chess;
import java.util.ArrayList;
import java.util.HashMap;

import com.sun.scenario.Settings;

import chess.Board.RuleSet.GameMode;
import chess.pieces.Piece;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Maneges the graphics and user actions
 * @author robertswanson
 */
public class App extends Application {
	public class Messages{
		public GridPane gPane;
		public Board.State gameState;
		
		public Messages(GridPane gP) {
			gPane = gP;
		}
		public void invalidMove(String s){
			
		}
		public void invalidMove(){
			invalidMove("");
		}
		public void notYourTurn(){
			
		}
		public void gameOver(){
			
		}
	}
	
	Stage window;
	Messages messages;
	HashMap<Point, ImageView> whiteIcons;
	HashMap<Point, ImageView> blackIcons;

	double step;
	Canvas canvas;
	StackPane layout;
	Board board;
	
	Point selected;
	
	public static void main(String[] args){
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		board = new Board();
		whiteIcons = new HashMap<>();
		blackIcons = new HashMap<>();

//Graphics
		messages = new Messages(new GridPane());
		
		window = primaryStage;
		window.setTitle("Chess");
		window.setWidth(1000);
		window.setHeight(1000);
		window.setMinHeight(550);
		window.setMinWidth(450);
		
	//Initialize
	
		
		//Buttons
		HBox buttons = new HBox(10);
		buttons.setAlignment(Pos.CENTER);
		buttons.setPadding(new Insets(10,0,10,0));
		
		Button reset = new Button("Restart");
		reset.setOnAction(e -> reset());
		
		Button sync = new Button("Syncronize");
		sync.setOnAction(e -> {
			initiatePieces();
			resize();
			setupAnimation(.0001, 0);
		});
		
		Button print = new Button("Print");
		print.setOnAction(e -> board.print());
		
		Button undo = new Button("Undo");
		undo.setOnAction(e -> {
			undo();
		});
		
		Button sButton = new Button("Settings");
		sButton.setOnAction(e -> {
			SettingsView s = new SettingsView(board);
			s.display();
			reset();
		});
		
		buttons.getChildren().addAll(reset, sync, print, undo, sButton);

		
		//Stack Pane
		layout = new StackPane();
		canvas = new Canvas(100,100);
		
		window.widthProperty().addListener(e -> resize());
		window.heightProperty().addListener(e -> resize());

		step = canvas.getWidth()/8;
		
		//Initialize
		initiateBoard();
		layout.getChildren().add(canvas);
		
		initiatePieces();
				
		//Master Layout
		BorderPane masterLayout = new BorderPane();
		masterLayout.setCenter(layout);
		masterLayout.setBottom(buttons);
		masterLayout.setTop(messages.gPane);
		
		Scene board = new Scene(masterLayout);
		board.setFill(Color.LIGHTGREY);
		window.setScene(board);
		
		//Mouse Handler
		canvas.setOnMouseClicked(e -> {
			click(e.getX(), e.getY(),null);
		});
		
		
		//Show the Window
		window.show();
		
		resize();
		setupAnimation(.5,.3);

	}
	/**
	 * Called when the window is resized. It fits the canvas and reinstansiates the pieces to fit
	 */
	private void resize(){
		double size = Double.min(window.getHeight()-150, window.getWidth()-40);
		canvas.setWidth(size);
		canvas.setHeight(size);
		step = size/8;
		initiateBoard();
		for(ImageView icon: blackIcons.values()){
			icon.setFitWidth(step);
		}
		for(ImageView icon: whiteIcons.values()){
			icon.setFitWidth(step);
		}
		setupAnimation(.0001, 0);
	}
	
	/**
	 * Draws the board on the canvas
	 */
	private void initiateBoard(){
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		
		boolean startBlack = true;
		for(int y = 0; y < 8; y++){
			boolean black = startBlack;
			for(int x = 0; x < 8; x++){
				Color color = black ? Color.BROWN : Color.WHITE;
				gc.setFill(color);
				gc.fillRect(step*x, step*y, step, step);
				black = !black;
			}
			startBlack = !startBlack;		
		}
		if(selected != null)
			select(selected);
	}
	
	/**
	 * Creates and adds an icon for every piece on the board
	 */
	private void initiatePieces(){
		layout.getChildren().removeAll(blackIcons.values());
		layout.getChildren().removeAll(whiteIcons.values());
		blackIcons.clear();
		whiteIcons.clear();
		
		for(Point point: board.blackPieces.keySet()){
			Piece piece = board.blackPieces.get(point);
			ImageView icon = initPiece(point, false, piece);
			
			blackIcons.put(point, icon);
			layout.getChildren().add(icon);
		}
		for(Point point: board.whitePieces.keySet()){
			Piece piece = board.whitePieces.get(point);
			ImageView icon = initPiece(point, true, piece);
			whiteIcons.put(point, icon);
			layout.getChildren().add(icon);
		}		
	}
	private ImageView initPiece(Point p, boolean player, Piece piece){
		String color = (player ? "White_" : "Black_") + piece.toString() + ".png";
		ImageView icon = new ImageView(getClass().getResource(color).toString());
		icon.setPreserveRatio(true);
		icon.setVisible(false);
		icon.setOnMouseClicked(e-> click(0,0,p));
		return icon;
	}
	
	/**
	 * Animates the pieces coming out from the center
	 * @param duration
	 * The duration of the animation
	 * @param delay
	 * The delay before the animation plays
	 */
	private void setupAnimation(double duration, double delay){
		ArrayList<Point> points = new ArrayList<>();
		points.addAll(board.blackPieces.keySet());
		points.addAll(board.whitePieces.keySet());
		
		for(Point p:points){
			ImageView icon;
			if(whiteIcons.containsKey(p)){
				icon = whiteIcons.get(p);
			}
			else if(blackIcons.containsKey(p)){
				icon = blackIcons.get(p);
			}
			else{
				System.err.printf("No icon at %s\n", p.toString());
				continue;
			}
			icon.setVisible(true);
			
			double[] t = getLayoutCoord(p);
			double diff = step/2;
			
			Line path = new Line(diff, diff, t[0], t[1]);
			PathTransition move = new PathTransition();
			move.setPath(path);
			move.setDuration(Duration.seconds(duration));
			move.setDelay(Duration.seconds(delay));
			move.setNode(icon);
			move.play();
		}
	}
	
	/**
	 * Animates a move
	 * @param from
//	 * The point describing where the piece starts
	 * @param to
	 * The point describing where the piece starts
	 * @param duration
	 * The duration of the animation
	 */
	private void animateMove(Point from, Point to, double duration){
		ImageView icon;
		if(whiteIcons.containsKey(from)){
			icon = whiteIcons.remove(from);
			icon.setOnMouseClicked(e-> click(0,0,to));
			whiteIcons.put(to, icon);
		}
		else if(blackIcons.containsKey(from)){
			icon = blackIcons.remove(from);
			icon.setOnMouseClicked(e-> click(0,0,to));
			blackIcons.put(to, icon);
		}
		else{
			System.err.printf("Cannot Animate piece at %s because it isn't there\n",from.toString());
			return;
		}
		double[] f = getLayoutCoord(from);
		double[] t = getLayoutCoord(to);
		Line path = new Line(f[0], f[1], t[0], t[1]);
		PathTransition move = new PathTransition();
		move.setPath(path);
		move.setDuration(Duration.seconds(duration));
		move.setNode(icon);
		move.play();
	}
	private void animateCapture(Point pos, double duration, boolean color){
		ImageView icon;
		if(color)
			icon = whiteIcons.get(pos);
		else
			icon = blackIcons.get(pos);
		RotateTransition rotate = new RotateTransition(Duration.seconds(duration),icon);
		rotate.setByAngle(200);
		FadeTransition fade = new FadeTransition(Duration.seconds(duration), icon);
		fade.setFromValue(1.0);
		fade.setToValue(0.0);
		ParallelTransition capture = new ParallelTransition(rotate, fade);
		capture.setOnFinished(e -> {
			if(color)
				whiteIcons.remove(pos);
			else
				blackIcons.remove(pos);
			layout.getChildren().remove(icon);
		});
		capture.play();
	}
	private void unCapture(ImageView piece, double duration, Point pos){
		double[] gp = getLayoutCoord(pos);
		Line path = new Line(1, 1 , gp[0], gp[1]);
		PathTransition move = new PathTransition();
		move.setPath(path);
		move.setDuration(Duration.millis(10));
		move.setNode(piece);

		
		FadeTransition fade = new FadeTransition();
		fade.setNode(piece);
		fade.setFromValue(0.0);
		fade.setToValue(1.0);
		fade.setDuration(Duration.seconds(duration));
		
		SequentialTransition uncap = new SequentialTransition(move, fade);
		uncap.play();

	}
	/**
	 * Gets the coordinate relative to the middle of the layout: (0,0) = Middle of window
	 * @param p
	 * The point to be converted
	 * @return
	 * An int[]{x,y} describing where the point is in relation to the layout
	 */
	private double[] getLayoutCoord(Point p){
		double start = 0 - step * 3;
		return new double[]{(start + step * p.x), (start + step * p.y)};
	}
	
	/**
	 * Gets the coordinate relative to the canvas: (0,0) = top left corner
	 * The point to be converted
	 * @return
	 * An int[]{x,y} describing where the point is in relation to the canvas
	 */
	private double[] getCanvasCoord(Point p){
		return new double[]{(step * p.x), (step * p.y)};
	}
	
	/**
	 * Gets the point of a click on the canvas
	 * @param x
	 * x value of the click
	 * @param y
	 * y value of the click
	 * @return
	 * the position on the board clicked
	 */
	private Point getPoint(double x, double y){
		x /= step;
		y /= step;
		return new Point((int)x, (int)y);
	}
	
	/**
	 * Handles the click event at either the given point or canvas coordinates
	 * @param x
	 * Canvas x coordinate of click
	 * @param y
	 * Canvas y coordinate of click
	 * @param p
	 * Point clicked
	 */
	private void click(double x, double y, Point p){
		if(board.rules.mode.toString().equals("Computer vs Computer"))
			return;
		Point clicked;
		if(p == null)
			clicked = getPoint(x, y);
		else
			clicked = p;
		
		if(selected == null){ //Initial selection
			if(!board.getIsAIPlayer() && playerHasPiece(clicked, board.turn))
				select(clicked);	
		}
		else{
			if(!playerHasPiece(clicked, board.turn)){	//Move
				Move m = new Move(selected, clicked, board);
				move(m);
				deSelect();
			}
			else{										//Reselected
				deSelect();
				select(clicked);
			}
		}
	}
	
	private void move(Move m){
		if(board.gameState != Board.State.INPROGRESS){
			messages.gameOver();
			return;
		}
		if(!playerHasPiece(m.from, board.turn)){
			messages.notYourTurn();
			return;
		}
		else if(board.getIsAIPlayer()){
			messages.notYourTurn();
			return;
		}
		else{
			ArrayList<Move> moves = board.getPiece(m.from).getMoves(board, m.from);
			int i = moves.indexOf(m);
			if(i >= 0 && !moves.get(i).putsPlayerInCheck(m.me)){
				m = moves.get(i);
				if(m.putsPlayerInCheck(!m.me)){
					System.out.println("Check");
				}
				if(board.move(m)){	//Moves returns if capture piece
					if (m.me){
						animateCapture(m.to, .5, false);
					}
					else
						animateCapture(m.to, .5, true);
				}
				animateMove(m.from, m.to, .5);
				if(m.castlingMove){
					boolean left = m.to.x < 3;
					int y = m.me == board.rules.topPlayer ? 0 : 7;
					if(left){
						animateMove(new Point(0, y), new Point(2, y), .5);
					}
					else{
						animateMove(new Point(7, y), new Point(4, y), .5);
					}
				}
				messages.setTurn(board.turn);
				AIMove aimove = new AIMove(board);
				if(board.gameState != Board.State.INPROGRESS){
					messages.gameOver();
				}
				else
					aimove.start();
			}
			else
				messages.invalidMove();
		}
	}
	
	/**
	 * Draws a box on the board to show the point is selected
	 * @param p
	 */
	private void select(Point p){
		selected = p;
		double[] grid = getCanvasCoord(p);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setStroke(Color.YELLOW);
		double width = step/10;
		gc.setLineWidth(width);
		gc.strokeRect(grid[0]+width/2, grid[1]+width/2 ,step-width, step-width);
	}
	
	/**
	 * Clears any selected points
	 */
	private void deSelect(){
		selected = null;
		canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		initiateBoard();
	}
	
	/**
	 * Resets the board
	 */
	private void reset(){
		deSelect();
		board.setUpBoard();
		initiatePieces();
		resize();
		setupAnimation(.5,0);

	}
	
	private void undo(){
		if(board.history.isEmpty())
			System.out.println("Cant Undo");
		else{
			Move m = board.history.peek();
			board.undo();
			animateMove(m.to, m.from, .5);
			if(m.getCapture() != null){
				Piece piece = m.getCapture();
				ImageView icon = initPiece(m.to, piece.isWhite(), piece);
				icon.setVisible(true);
				layout.getChildren().add(icon);
				if(piece.isWhite())
					whiteIcons.put(m.to, icon);
				else
					blackIcons.put(m.to, icon);
				icon.setFitWidth(step);
				unCapture(icon, .5, m.to);
			}
			if(m.castlingMove){
				boolean left = m.to.x < 4;
				int y = m.me == board.rules.topPlayer ? 0 : 7;
				if(left)
					animateMove(new Point(2, y), new Point(0, y), .5);
				else
					animateMove(new Point(4, y), new Point(7, y), .5);
			}
			board.print();
		}
	}
	
	/**
	 * Determines if the given player has a piece at the given point
	 * @param p
	 * The point in question
	 * @param player
	 * The player
	 * @return
	 * returns true if the player has a piece there
	 */
	private boolean playerHasPiece(Point p, boolean player){
		if(player){
			return board.whitePieces.containsKey(p);
		}
		return board.blackPieces.containsKey(p);
	}
	@Override
	public String toString() {
		return board.toString();
	}
	public static void SetUpVBox(VBox layout){
		layout.setAlignment(Pos.CENTER);
		layout.setSpacing(10);
	}
	public static void SetUpHBox(HBox layout){
		layout.setAlignment(Pos.CENTER);
		layout.setSpacing(10);
	}
	public static void SetUpTextField(TextField text){
		text.setMaxWidth(60);
	}
	public static void SetMargins(VBox layout){
		
		VBox.setMargin(layout.getChildren().get(0), new Insets(10,0,0,0));
		VBox.setMargin(layout.getChildren().get(layout.getChildren().size()-1), new Insets(0,0,10,0));

	}
	
	class AIMove extends Thread{
		AI ai;
		boolean valid;
		public AIMove(Board board) {
			valid = board.getIsAIPlayer();
			if(board.turn)
				ai = board.white;
			else
				ai = board.black;
		}
		@Override
		public void run() {
			System.out.println("RUNING");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("RAN");
		}
	}
}
