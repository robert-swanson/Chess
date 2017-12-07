package chess;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import chess.Board.RuleSet.GameMode;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Piece;
import chess.pieces.Queen;
import javafx.animation.Animation.Status;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Manages the graphics and user actions
 * @author Robert Swanson
 */
public class App extends Application {
	/**
	 * Handles displaying game messages to the user
	 * @author Robert Swanson
	 */
	public class Messages{
		public Label label;
		public Board.State gameState;
		private Timeline tl;
		private Duration duration;
		FadeTransition fade;

		private void updateTimeLine(Duration d){
			duration = d;
			tl = new Timeline(new KeyFrame(
					d,
					ae -> fade()));
		}
		public Messages(Label l) {
			label = l;
			fade = new FadeTransition();
			updateTimeLine(Duration.ZERO);
		}
		public void invalidMove(){
			message("Invalid Move", Duration.seconds(3));
		}
		public void notYourTurn(){
			message("It's not your turn",Duration.seconds(3));
		}
		public void gameOver(){
			progress.set(0.0);
			if(gameState == Board.State.BLACKWON)
				message("Black Won!", Duration.INDEFINITE);
			else if(gameState == Board.State.WHITEWON)
				message("White Won!", Duration.INDEFINITE);
			else if(gameState == Board.State.STALEMATE)
				message("Stalemate!",Duration.INDEFINITE);
			else
				message("Game Over ERROR", Duration.seconds(3));
		}

		private void message(String message, Duration d){
			tl.stop();
			fade.stop();
			updateTimeLine(d);
			label.setOpacity(1);
			label.setText(message);
			tl.play();
		}
		private void fade(){
			if(duration == Duration.INDEFINITE)
				return;
			fade = new FadeTransition();
			fade.setNode(label);
			fade.setDuration(Duration.seconds(1));
			fade.setFromValue(1.0);
			fade.setToValue(0.0);
			label.setOpacity(1);
			fade.setOnFinished(e -> label.setText(""));
			fade.play();
		}
		private void clear(){
			label.setText("");
			tl.stop();
			fade.stop();
		}
	}

	//Constants
	final double Animation_Duration = .2; 
	final int stop = -1;

	//Gameplay
	double step;
	Board board;
	SimpleBooleanProperty allowance;
	int running;
	ArrayList<Transition> animations;
	SimpleDoubleProperty progress;
	Move aisMove;
	Status AIStatus;

	ArrayList<Integer> histHash = new ArrayList<>();

	Tester tester;

	//UI
	BorderPane masterLayout;
	StackPane layout;
	Canvas canvas;
	BorderPane timerPanel;
	Stage window;
	Messages messages;
	HashMap<Point, ImageView> whiteIcons;
	HashMap<Point, ImageView> blackIcons;
	HBox buttons;
	Label topTime;
	Label botTime;
	SimpleIntegerProperty turnTimeD;
	StackPane clock;
	ImageView hand;
	RotateTransition timer;
	Timeline tl;
	Label turnTime;
	ImageView pauseTime;
	boolean paused = false;
	ImageView coverPause;
	ImageView stopStart;
	Line turnIndicator;	
	Point selected;

	public static void main(String[] args){
		launch(args);
	}
	@Override
	public void stop() throws Exception {
		allowance.set(false);
		System.out.println("Terminating Program");
		super.stop();
	}

	@Override
	/**
	 * Sets up the primary stage with the board in starting position
	 */
	public void start(Stage primaryStage) throws Exception {
		allowance = new SimpleBooleanProperty(true);
		progress = new SimpleDoubleProperty(0.0);
		tester = new Tester(progress,allowance);
		AIStatus = Status.RUNNING;
		animations = new ArrayList<>();
		board = new Board(allowance, progress);
		board = new Board(allowance, progress);
		whiteIcons = new HashMap<>();
		blackIcons = new HashMap<>();

		//Graphics
		Label mess = new Label();
		mess.setFont(new Font("Ubuntu Mono", 24));
		HBox.setMargin(mess, new Insets(10,10,0,10));

		//Top
		VBox top = new VBox(10);
		HBox messBox = new HBox(mess);
		HBox progressBar = new HBox(10);
		
		//Progress Bar
		ProgressBar pBar = new ProgressBar();
		VBox.setMargin(progressBar, new Insets(10,10,0,10));
		pBar.prefWidthProperty().bind(top.widthProperty());
		top.setAlignment(Pos.CENTER);
		pBar.progressProperty().bind(progress);
		
		//Stop Button
		stopStart = new ImageView(getClass().getResource("stop.png").toString());
		stopStart.setPreserveRatio(true);
		stopStart.setVisible(true);
		stopStart.setOnMouseClicked(e-> {
			allowance.set(!allowance.get());
		});
		stopStart.setFitHeight(19);
		progressBar.getChildren().addAll(pBar, stopStart);
		
		
		top.getChildren().addAll(progressBar,messBox);

		messBox.setAlignment(Pos.CENTER);
		messages = new Messages(mess);

		double stageX = 0.0;
		double stageY = 0.0;
		for(Screen screen : Screen.getScreens()){
			Rectangle2D bounds = screen.getBounds();
			if(bounds.getWidth() == 1280.0 && bounds.getHeight() == 800){
				stageX = bounds.getMinX()+bounds.getWidth();
				stageY = bounds.getMinY()+bounds.getHeight();
			}
		}
		window = primaryStage;
		window.setX(stageX);
		window.setY(stageY);
		window.setTitle("Chess");
		window.setWidth(800);
		window.setHeight(750);
		window.setMinHeight(550);
		window.setMinWidth(450);

		try {
//			URL iconURL = App.class.getResource("Black_King.png");
//			java.awt.Image image = new ImageIcon(iconURL).getImage();
//			com.apple.eawt.Application.getApplication().setDockIconImage(image);
		} catch (Exception e) {
			// Won't work on Windows or Linux.
		}

		//Initialize

		initButtonBar();

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
		masterLayout = new BorderPane();
		masterLayout.setCenter(layout);
		masterLayout.setBottom(buttons);
		masterLayout.setTop(top);
		
//		initiateTimerPanel(window.getHeight()-300);
		masterLayout.setLeft(timerPanel);

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
		setupAnimation(Animation_Duration,0);
		window.centerOnScreen();
	}
	
	private void showBoard(){
		layout.getChildren().remove(coverPause);
		for(Point p : board.blackPieces.keySet())
			blackIcons.get(p).setVisible(true);
		for(Point p : board.whitePieces.keySet())
			whiteIcons.get(p).setVisible(true);
	}
	/**
	 * Draws the board on the canvas
	 */
	private void initiateBoard(){
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

		boolean startBlack = false;
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

		double h = canvas.getHeight();
		double y = board.turn == board.rules.topPlayer ?  0: h; 
		gc.setStroke(board.getIsAIPlayer() ? Color.RED : Color.BLUE);
		gc.setLineWidth(step*.1);
		gc.strokeLine(0, y, h, y);
	}

	/**
	 * Called when the window is resized. It fits the canvas and reinstansiates the pieces to fit
	 */
	private void resize(){
		double size = Double.min(window.getHeight()-150, window.getWidth()-150);

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

	private HBox initButtonBar(){
		//Buttons
		HBox buttons = new HBox(10);
		buttons.setAlignment(Pos.CENTER);
		buttons.setPadding(new Insets(10,0,10,0));

		Button tree = new Button("Tree");
		tree.setOnAction(e -> {
			if(board.turn)
				board.black.parent.print();
			else
				board.white.parent.print();
			try {
				Desktop.getDesktop().open(new File("ChessTree.txt"));
			} catch (IOException e1) {
				System.err.println("No File Named ChessTree.txt");
				e1.printStackTrace();
			}
		});

		Button reanimate = new Button("Show Last");
		reanimate.setOnAction(e -> {
			Move last = board.history.peek();
			undo(.001);
			move(last);
		});

		Button reset = new Button("Restart");
		reset.setOnAction(e -> reset());

		Button sync = new Button("Sync");
		sync.setOnAction(e -> {
			initiatePieces();
			resize();
			setupAnimation(.0001, 0);
		});

		Button print = new Button("Print");
		print.setOnAction(e -> {
			board.print();
		});


		Button undo = new Button("Undo");
		undo.setOnAction(e -> {
			switch(board.rules.mode){
			case cvc:
				if(AIStatus == Status.PAUSED)
					undo(Animation_Duration);
				break;
			case pvc:
				undo(Animation_Duration);
				undo(Animation_Duration);
				break;
			case pvp:
				undo(Animation_Duration);
				break;
			default:
				break;
			}
			initiateBoard();
		});

		ImageView sButton = new ImageView(getClass().getResource("settings.png").toString());
		sButton.setPreserveRatio(true);
		sButton.setVisible(true);
		sButton.setOnMouseClicked(e-> {
			
			SettingsView s = new SettingsView(board);
			if(s.display())  
				reset();
			initButtonBar();
			showBoard();
			if(board.getIsAIPlayer() && AIStatus == Status.RUNNING)
				new AIMove(board).start();
		});


		Button stepT = new Button("Step");
		stepT.setOnAction(e -> {
			if(board.getIsAIPlayer())
				new AIMove(board).start();
		});

		Button pausePlay = new Button(AIStatus == Status.PAUSED ? "Play" : "Pause");
		pausePlay.setOnAction(e -> {
			if(board.rules.mode == GameMode.cvc){
				if(AIStatus == Status.PAUSED){
					pausePlay.setText("Pause");
					AIStatus = Status.RUNNING;
					new AIMove(board).start();
				}
				else{
					pausePlay.setText("Play");
					AIStatus = Status.PAUSED;
					allowance.set(false);
				}
			}
		});

		//		Button copy = new Button("Copy");
		//		copy.setOnAction(e -> {
		//			ClipboardContent content = new ClipboardContent();
		//			content.putString(board.exportToString());
		//			Clipboard.getSystemClipboard().setContent(content);
		//			messages.message("Copied", Duration.seconds(1));
		//		});
		Button hist = new Button("History");
		hist.setOnAction(e -> {
			ClipboardContent content = new ClipboardContent();
			content.putString(board.history.toString());
			Clipboard.getSystemClipboard().setContent(content);
			messages.message(String.format("Copied History to Clipboard, Size: %d",board.history.size()), Duration.seconds(1));
		});

		Button load = new Button("Load");
		load.setOnAction(e -> {
			String content = Clipboard.getSystemClipboard().getString();
			board = new Board(allowance, content, progress);
			initiatePieces();
			resize();
			setupAnimation(.0001, 0);
		});


		Button test = new Button("Test");
		test.setOnAction(e -> {
			if(test.getText().equals("Test")){
				test.setText("Stop");
				tester.start();
				try {
					Desktop.getDesktop().open(new File("Learner.txt"));
				} catch (IOException e1) {
					System.err.println("No File Named ChessTree.txt");
					e1.printStackTrace();
				}
			}
			else{
				allowance.set(false);
				test.setText("Test");
			}

		});
		sButton.fitHeightProperty().set(20);;


		buttons.getChildren().addAll(sButton, reset, hist);
		if(board.rules.undo)
			buttons.getChildren().addAll(undo);
		if(board.rules.mode != GameMode.cvc)
			buttons.getChildren().addAll(reanimate);
		else{
			buttons.getChildren().addAll(pausePlay);
			buttons.getChildren().addAll(stepT);	
		}
		if(board.rules.debug){
			Separator sep = new Separator(Orientation.VERTICAL);
			buttons.getChildren().addAll(sep, print, tree, sync, test);
		}
		//		this.buttons.getChildren().clear();
		//		this.buttons.getChildren().addAll(buttons.getChildren());

		if(this.buttons == null){
			this.buttons = buttons;
		}
		else{
			ObservableList<Node> nodes = this.buttons.getChildren();
			nodes.clear();
			nodes.addAll(buttons.getChildren());
		}
		return buttons;
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
		int i = 0;

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
			if(duration > .001 && ++i == points.size())
				move.setOnFinished(e -> {
					if(board.getIsAIPlayer() && AIStatus == Status.RUNNING)
						new AIMove(board).start();
				});
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
	private Transition animateMove(Point from, Point to, double duration){
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
			System.err.printf("Cannot Animate piece at %s because it isn't there. (Move: %s)\n",from.toString(), board.history.peek());
			return null;
		}
		double[] f = getLayoutCoord(from);
		double[] t = getLayoutCoord(to);
		Line path = new Line(f[0], f[1], t[0], t[1]);
		PathTransition move = new PathTransition();
		move.setPath(path);
		move.setDuration(Duration.seconds(duration));
		move.setNode(icon);

		animations.add(move);
		move.setOnFinished(e -> animations.remove(move));
		return move;
	}
	private Transition animateCapture(Point pos, double duration, boolean color){
		ImageView icon;
		if(color)
			icon = whiteIcons.get(pos);
		else
			icon = blackIcons.get(pos);
		if(icon == null){
			System.err.println("No Point At: " + pos);
			return null;
		}
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
			animations.remove(capture);
		});

		animations.add(capture);
		return capture;
	}
	private Transition unCapture(ImageView piece, double duration, Point pos){
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

		animations.add(uncap);
		uncap.setOnFinished(e -> animations.remove(uncap));
		return uncap;
	}

	private Transition animateSwitch(Point p, Piece to, double duration, boolean color){

		ImageView remove;
		if(color)
			remove = whiteIcons.remove(p);
		else
			remove = blackIcons.remove(p);
		if(remove == null){
			System.err.println("Can't Switch piece at " + p + " because the icon isn't there");
			return null;
		}
		layout.getChildren().remove(remove);
		FadeTransition fadeOut = new FadeTransition();
		fadeOut.setNode(remove);
		fadeOut.setFromValue(1);
		fadeOut.setToValue(0);
		fadeOut.setDuration(Duration.seconds(duration));

		ImageView toIcon = initPiece(p, color, to);
		if(color)
			whiteIcons.put(p, toIcon);
		else
			blackIcons.put(p, toIcon);
		layout.getChildren().add(toIcon);
		resize();
		toIcon.setVisible(true);

		FadeTransition fadeIn = new FadeTransition();
		fadeIn.setNode(toIcon);
		fadeIn.setFromValue(0);
		fadeIn.setToValue(1);
		fadeIn.setDuration(Duration.seconds(duration));

		//		SequentialTransition switchPiece = new SequentialTransition(fadeOut, fadeIn);
		ParallelTransition switchPiece = new ParallelTransition(fadeOut, fadeIn);

		animations.add(switchPiece);
		switchPiece.setOnFinished(e -> animations.remove(switchPiece));
		return switchPiece;
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
		if(board.gameState != Board.State.INPROGRESS){
			messages.gameOver();
			return;
		}
		if(board.rules.mode.toString().equals("Computer vs Computer")){
			System.out.println("CVC");
			return;	
		}
		Point clicked;
		if(p == null)
			clicked = getPoint(x, y);
		else
			clicked = p;

		if(selected == null){ //Initial selection
			if(!board.getIsAIPlayer() && playerHasPiece(clicked, board.turn))
				select(clicked);
			else if(playerHasPiece(clicked, !board.turn))
				messages.notYourTurn();
			else if(board.getIsAIPlayer())
				messages.message("You Cannot Move For The AI", Duration.seconds(3));
		}
		else{
			if(!playerHasPiece(clicked, board.turn) && !board.getIsAIPlayer() && playerHasPiece(selected, board.turn)){	//Move
				Move m = new Move(selected, clicked, board);
				if(m.piece instanceof Pawn && (m.to.y == 0 || m.to.y == 7)){
					if(new Switcher().display())	//Queen
						m.changedTo = new Queen(m.me, m.to);
					else							//Knight
						m.changedTo = new Knight(m.me, m.to);
				}

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
		if(m == null)
			System.out.println("Cant Move Null");
		if(playerHasPiece(m.from, !board.turn)){
			messages.notYourTurn();
			return;
		}
		else{
			Piece f = board.getPiece(m.from);
			ArrayList<Move> moves = f.getMoves(board, m.from);
			board.addCastleMoves(moves, m.me);
			board.removeCastleOutOfCheck(moves, m.me);
			int i = moves.indexOf(m);
			if(i >= 0 && !moves.get(i).putsPlayerInCheck(m.me)){
				m = moves.get(i);
				if(m.putsPlayerInCheck(!m.me)){
					messages.message("Check", Duration.seconds(3));
				}
				if(board.move(m)){	//Moves returns if capture piece
					if (m.me){
						animateCapture(m.to, Animation_Duration, false).play();
					}
					else
						animateCapture(m.to, Animation_Duration, true).play();
				}
				Transition move = animateMove(m.from, m.to, Animation_Duration);
				if(m.changedTo != null){	//Queen me
					move.setOnFinished(e -> {
						animations.remove(move);
						respondToFinishSwitchMove();
					});

				}
				else
					setOnAIMoveOnFinish(move);
				move.play();

				if(board.history.size() == stop)
					AIStatus = Status.PAUSED;
				//					else if(board.history.size() == stop+5)
				//						AIStatus = Status.RUNNING;


				if(m.castlingMove){
					boolean left = m.to.x < 3;
					int y = m.me == board.rules.topPlayer ? 0 : 7;
					if(left){
						animateMove(new Point(0, y), new Point(3, y), Animation_Duration).play();
					}
					else{
						animateMove(new Point(7, y), new Point(5, y), Animation_Duration).play();
					}
				}
				initiateBoard();
				if(board.gameState != Board.State.INPROGRESS){
					messages.gameState = board.gameState;
					messages.gameOver();
				}
				board.updateIcon();
				int temp = board.hashCode(board.white.keys);
				int index = histHash.indexOf(temp);
				if(index >= 0){
					messages.message(String.format("%s conceding to repetition",board.turn ? "White" : "Black"),Duration.seconds(2));
				}
				histHash.add(temp);
				if(!isAllWell()){
					System.out.println("Icons Disagree");
					AIStatus = Status.PAUSED;
				}
			}
		}
	}
	
	private void setOnAIMoveOnFinish(Transition t){
		t.setOnFinished(e -> {
			if(board.getIsAIPlayer() && board.gameState == Board.State.INPROGRESS && allowance.get() && (AIStatus == Status.RUNNING || board.rules.mode == GameMode.pvc))
				new AIMove(board).start();
			animations.remove(t);
		});
	}
	private void respondToFinishSwitchMove(){
		Move mm = board.history.peek();
		Transition t = animateSwitch(mm.to, mm.changedTo, Animation_Duration, mm.me);
		setOnAIMoveOnFinish(t);
		t.play();
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
		histHash.clear();
		board.avCount = 0;
		board.avTotal = 0;
		//		allowance.set(false);
		messages.clear();
		deSelect();
		board.setUpBoard();
		initiatePieces();
		resize();
		for(Transition p: animations)
			p.stop();
		animations.clear();
		allowance.set(true);
		setupAnimation(Animation_Duration,0);
	}

	private void startAllAnimations(){
		for(Transition trans: animations)
			if(!trans.getStatus().equals(Status.RUNNING))
				trans.play();
	}
	private void undo(double d){
		if(board.white.thinking || board.black.thinking){
			allowance.set(false);
			return;
		}
		if(board.history.isEmpty())
			System.out.println("Cant Undo");
		else{
			histHash.remove(histHash.size()-1);
			deSelect();
			Move m = board.history.peek();
			board.undo();
			if(m.changedTo != null){
				Transition t = animateSwitch(m.to, board.getPiece(m.from), d, m.me);
				t.setOnFinished(e -> {
					animations.remove(t);
					startAllAnimations();
				});
				t.play();
			}
			animateMove(m.to, m.from, d);
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
				unCapture(icon, d, m.to);
			}
			if(m.castlingMove){
				boolean left = m.to.x < 4;
				int y = m.me == board.rules.topPlayer ? 0 : 7;
				if(left)
					animateMove(new Point(3, y), new Point(0, y), d);
				else
					animateMove(new Point(5, y), new Point(7, y), d);
			}
			if(m.changedTo == null)
				startAllAnimations();
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
	public boolean isAllWell(){
		Stack<Piece> pieces = new Stack<>();
		pieces.addAll(board.whitePieces.values());
		pieces.addAll(board.blackPieces.values());
		while(!pieces.isEmpty()){
			Piece p = pieces.pop();
			if(p.isWhite()){
				if(!whiteIcons.containsKey(p.position))
					return false;
			}
			else{
				if(!blackIcons.containsKey(p.position))
					return false;
			}
		}
		return true;
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
			allowance.set(true);
			AI ai = board.getAI();
			Move move = null;
			try{
				move = ai.getBestMove();
			}catch (Exception e) {
				System.err.println(e);
				if(!allowance.get()){
					System.out.println("Calculation Interrupted By User");
					return;
				}
				System.out.println(board.black.stratagy.transpositionTableDepth);
				System.out.println("Calculation Failed");
				throw e;
			}
			if(move != null){
				aisMove = move;
				Timeline tl = new Timeline(new KeyFrame(Duration.millis(1),evt -> {
					move(aisMove);
				}));
				tl.play();
			}
			else{
				System.out.println("Null Move Returned");
			}
			allowance.set(true);
			progress.set(0.0);
		}
	}
}
