package pedroaba.java.race.ui.images;

public class BoostImage extends Image {
    public String name = "Boost!";
    public BoostImage() {
        this.imagePath = ImagePaths.BoostImage;
    }

    @Override
    public String getName() {
        return name;
    }
}
