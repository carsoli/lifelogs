package application.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.TilePane;

/**
 * @author Carol Soliman
 * @since June 2018
 * */

public class CustomScrollPane extends ScrollPane {

	public CustomScrollPane (Scene mainScene) {
		this.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); 
		this.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); 
		this.setFitToWidth(true);
//		this.prefHeightProperty().bind(mainScene.heightProperty());
		this.prefWidthProperty().bind(mainScene.widthProperty());
		
		TilePane tpContent = new TilePane();
		tpContent.setAlignment(Pos.TOP_CENTER);
		tpContent.setPadding(new Insets(5, 5, 5, 5));
		tpContent.setHgap(5);
		tpContent.setVgap(5);
		this.setContent(tpContent);
	}
	
}
