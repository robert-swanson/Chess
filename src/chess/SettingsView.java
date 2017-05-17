package chess;

import java.util.HashMap;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class SettingsView{
	Stage window;
	
	HashMap<String, Control> options;
	
	SettingsView(){
		options = new HashMap<>();
		options.put("alphabeta", new CheckBox("alphabeta"));
		options.put("Top Player", new CheckBox("Top Player"));
		options.put("Depth", new Slider(0, 8, 4));
	}
	
	public void display(){
		Stage window = new Stage();
		
		window.initModality(Modality.APPLICATION_MODAL);
		window.setTitle("Settings");
		window.setMinWidth(250);
		window.setMinHeight(80+options.size()*40);
		
		BorderPane layout = new BorderPane();
		
		Label l = new Label();
		l.setText("Settings");
		
		Button ok = new Button("OK");
		BorderPane.setAlignment(ok, Pos.CENTER);
		BorderPane.setMargin(ok, new Insets(10,20,10,20));
		ok.setOnAction(e -> {
			System.out.println("OK");
			window.close();
		});
		
		//Options
		VBox optionPanel = new VBox();
		optionPanel.setAlignment(Pos.CENTER);
		
		for(String option: options.keySet()){
			Control control = options.get(option);
			if(control instanceof Slider){
				Slider s = (Slider)control;
				Label sL = new Label("");
				Label title = new Label(option);
				sL.textProperty().bind(s.valueProperty().asString("%.0f"));
				HBox.setMargin(sL, new Insets(10,10,10,10));
				HBox labelBox = new HBox();
				labelBox.setAlignment(Pos.CENTER);
				labelBox.getChildren().addAll(title, s,sL);
				
				optionPanel.getChildren().add(labelBox);
				
			}
			else if(control instanceof CheckBox){
				optionPanel.getChildren().add(control);
			}
			else{
				optionPanel.getChildren().add(control);	
			}
		}
		
		optionPanel.getChildren().forEach((node) -> {
			VBox.setMargin(node, new Insets(10, 20, 10, 20));
			
		});
		
		
		layout.setBottom(ok);
		layout.setCenter(optionPanel);
		
		Scene s = new Scene(layout);
		window.setScene(s);
		window.showAndWait();
	}
}
