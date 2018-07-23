package application.view;

import application.utils.Constants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;

public class TabPaneItem extends Tab{
	private int index;
	
	public TabPaneItem(int index, String title, boolean isCloseable) {
		this.setIndex(index); 
		this.setText(title);
		this.setClosable(isCloseable);
		VBox mContent = new VBox();
		mContent.setPadding(new Insets(0, 0, 0, 0));
		mContent.setAlignment(Pos.TOP_CENTER);
		mContent.setStyle(Constants.BG_BLACK);
		this.setContent(mContent);
		
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
}
