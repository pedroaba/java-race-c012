package pedroaba.java.race.ui.images;

public class BananaImage extends Image {
    public String name = "Banana!";
    public BananaImage() {
        this.imagePath = ImagePaths.BananaImage;
    }

    @Override
    public String getName() {
        return name;
    }
}
