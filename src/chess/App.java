package chess;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

public class App extends Application {
	
	Stage window;
	HashMap<Point, ImageView> whiteIcons;
	HashMap<Point, ImageView> blackIcons;

	double step;
	Canvas canvas;
	StackPane layout;
	Board board;
	
	SimpleIntegerProperty skill;
	boolean topPlayer = false;
	Point selected;
	StringProperty message;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		board = new Board(false);
		whiteIcons = new HashMap<>();
		blackIcons = new HashMap<>();

//Graphics
		message = new SimpleStringProperty();
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
		
		Slider skill = new Slider(0, 8, 4);
		this.skill = new SimpleIntegerProperty();
		this.skill.bind(skill.valueProperty());
		Label skillLabel = new Label();
		skillLabel.textProperty().bind(Bindings.format("%.0f", skill.valueProperty()));
		
		CheckBox bottomPlayer = new CheckBox("White at Bottom");
		bottomPlayer.setOnAction(e -> topPlayer = !bottomPlayer.isSelected());
		bottomPlayer.setSelected(true);
		
		buttons.getChildren().addAll(reset,skill,skillLabel,bottomPlayer);

		//Message
		Label mess = new Label();
		mess.textProperty().bind(message);
		mess.setPadding(new Insets(10,0,10,0));
		mess.setFont(new Font("Arial", 24));
		
		HBox messageBox = new HBox();
		messageBox.setAlignment(Pos.CENTER);
		messageBox.getChildren().add(mess);
		
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
		
		System.out.println(blackIcons.get(new Point(7, 1)).xProperty());
		
		//Master Layout
		BorderPane masterLayout = new BorderPane();
		masterLayout.setCenter(layout);
		masterLayout.setBottom(buttons);
		masterLayout.setTop(messageBox);
		
		Scene board = new Scene(masterLayout);
		board.setFill(Color.LIGHTGREY);
		window.setScene(board);
		
		//Mouse Handler
		canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
			System.out.println("Left");
		});
		canvas.setOnMouseClicked(e -> {
			System.out.println("Clicked");
			click(e.getX(), e.getY(),null);
		});
		
		
		//Show the Window
		window.show();
		
		setupAnimation(.5,.3);
	}
	
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
	private void initiatePieces(){
		layout.getChildren().removeAll(blackIcons.values());
		layout.getChildren().removeAll(whiteIcons.values());
		blackIcons.clear();
		whiteIcons.clear();
		
		for(Point point: board.blackPieces.keySet()){
			Board.Piece piece = board.blackPieces.get(point);
			ImageView icon = initPiece(point, false, piece);
			
			blackIcons.put(point, icon);
			layout.getChildren().add(icon);
		}
		for(Point point: board.whitePieces.keySet()){
			Board.Piece piece = board.whitePieces.get(point);
			ImageView icon = initPiece(point, true, piece);
			whiteIcons.put(point, icon);
			layout.getChildren().add(icon);
		}		
	}
	private ImageView initPiece(Point p, boolean player, Board.Piece piece){
		String color = (player ? "White_" : "Black_") + piece.toString() + ".png";
		ImageView icon = new ImageView(getClass().getResource(color).toString());
		icon.setPreserveRatio(true);
		icon.setFitWidth(step);
		icon.setVisible(false);
		icon.setOnMouseClicked(e-> click(0,0,p));
		return icon;
	}
	
	
	public static void main(String[] args){
		launch(args);
	}
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
				System.err.printf("No icon at %s", p.toString());
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
	private double[] getLayoutCoord(Point p){
		double start = 0 - step * 3;
		return new double[]{(start + step * p.x), (start + step * p.y)};
	}
	private double[] getCanvasCoord(Point p){
		return new double[]{(step * p.x), (step * p.y)};
	}
	private Point getPoint(double x, double y){
		//FIXME fix getPoint to layout click instead of canvas click
		double start = 0 - step * 3;
		x /= step;
		y /= step;
		return new Point((int)x, (int)y);
	}
	private void click(double x, double y, Point p){
		Point clicked;
		if(p == null)
			clicked = getPoint(x, y);
		else
			clicked = p;
		
		if(!clicked.isValid()){							//Clicked Outside Board
			System.out.println(clicked);
			return;
		}
		if(selected == null){							//Was not already selected
			if(playerHasPiece(clicked, board.turn)){	//Clicked Own Piece
				select(clicked);	
			}
		}
		else{
			if(!playerHasPiece(clicked, board.turn)){	//Tried Move
				animateMove(selected, clicked, .5);
				board.move(selected, clicked);
				deSelect();
				message.set(String.format("It is %s's turn", (board.turn ? "White" : "Black")));
			}
			else{										//Reselected
				deSelect();
				select(clicked);
			}
		}
	}
	private void select(Point p){
		selected = p;
		double[] grid = getCanvasCoord(p);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setStroke(Color.YELLOW);
		double width = step/10;
		gc.setLineWidth(width);
		gc.strokeRect(grid[0]+width/2, grid[1]+width/2 ,step-width, step-width);
	}
	private void deSelect(){
		selected = null;
		canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		initiateBoard();
	}
	private void reset(){
		deSelect();
		board = new Board(topPlayer);
		initiatePieces();
		setupAnimation(.5,0);
	}
	private boolean playerHasPiece(Point p, boolean player){
		if(player){
			return board.whitePieces.containsKey(p);
		}
		return board.blackPieces.containsKey(p);
	}
	private Board.Piece getPiece(Point p, boolean player){
		if(player){
			return board.whitePieces.get(p);
		}
		return board.blackPieces.get(p);
	}
}
