package application.view;

import application.utils.Constants;
import javafx.geometry.Insets;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * 
 * @author Carol Soliman
 * @since June, 2018
 */
public class ImageToggleButton extends ToggleButton{
	private Image unselectedImg = null;
	private Image selectedImg = null; 
	private ImageView imageView = null; 
	
	public ImageToggleButton(String unselectedImgName, String selectedImageName) {
		this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		this.setPadding(new Insets(5,10,5,10));
		
		try {
			final Image unselectedImg = new Image(Constants.ASSETS_URL.toString() + unselectedImgName);
			this.unselectedImg = unselectedImg;
			final Image selectedImg = new Image(Constants.ASSETS_URL.toString() + selectedImageName);
			this.selectedImg = selectedImg;
			
			this.imageView = new ImageView(unselectedImg);
			this.setGraphic(imageView);
			
		} catch(NullPointerException e1) {
			System.out.println("Button Image URL is null");
			e1.printStackTrace();
		} catch(IllegalArgumentException e2) {
			System.out.println("Button Image URL is invalid or unsupported"
					+ "OR attempting to access Image View outside of the scope of the Button Icon");
			e2.printStackTrace();
		}
	}
	
	public void setSelectedBG() {
		this.imageView = new ImageView(this.selectedImg);
		this.setGraphic(imageView);
		
	}
	
	public void setUnselectedBG() {
		//when selected = false
		this.imageView = new ImageView(this.unselectedImg);
		this.setGraphic(imageView);
		
	}
}
