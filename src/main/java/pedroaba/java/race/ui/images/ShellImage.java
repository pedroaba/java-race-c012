package pedroaba.java.race.ui.images;

public class ShellImage extends Image {
    public String name = "Red Shell";
    public ShellImage() {
        this.imagePath = ImagePaths.ShellImage;
    }

    @Override
    public String getName() {
        return name;
    }
}
