package cs1302.gallery;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

/**
 * ImageLoader for the GalleryApp class.
 */
public class ImageLoader extends VBox {

    ImageView imageView;
    Image image;

    /**
     * Constructor for the ImageLoader class.
     * @param url  the url for the image object
     */
    public ImageLoader(String url) {
        super();
        image = new Image(url, 100, 100, false, false);
        imageView = new ImageView(image); //adds image to image view
        this.getChildren().add(imageView);
    } //ImageLoader

    /**
     * Sets the image for the image view.
     * @param url  the url for the image
     */
    public void updateImage(String url) {
        Image i = new Image(url, 100, 100, false, false);
        imageView.setImage(i); //updates the image in the image view
    } //updateImage
} //ImageLoader
