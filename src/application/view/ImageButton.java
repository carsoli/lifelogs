package application.view;

import application.utils.Constants;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * @author Carol Soliman
 * @since June, 2018
 * */
public class ImageButton extends Button{
	
	public ImageButton(String imgName) {
		this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		this.setPadding(new Insets(5,10,5,10));
		
		try {
			final Image img = new Image(Constants.ASSETS_URL.toString() + imgName);
			final ImageView imgView = new ImageView(img);
			this.setGraphic(imgView);
		} catch(NullPointerException e1) {
			System.out.println("Button Image URL is null");
			e1.printStackTrace();
		} catch(IllegalArgumentException e2) {
			System.out.println("Button Image URL is invalid or unsupported"
					+ "OR attempting to access Image View outside of the scope of the Button Icon");
			e2.printStackTrace();
		}
		
	}

}
