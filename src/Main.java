import extensioninformation.Extension;
import extensioninformation.ExtensionInformation;
import extensioninformation.GraphicControlExtension;
import header.GifHeader;
import image.Image;
import util.Buffer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        new Main();
    }

    private Buffer data;

    private GifHeader gifHeader;
    private List<Object> components;
    private List<Extension> extensions;
    private List<Image> images;

    private List<List<Integer>> dictionary;

    private Main() throws IOException {
        File file = new File("hamburger.gif");
        data = new Buffer(Files.readAllBytes(file.toPath()));
        data.incrementPointer(6);

        gifHeader = new GifHeader(data);
        dictionary = gifHeader.getDictionary();

        components = new ArrayList<>();
        extensions = new ArrayList<>();
        images = new ArrayList<>();

        readExtensionsAndImages();
    }

    private BufferedImage img;

    private void readExtensionsAndImages() throws IOException {
        while (true) {
            int identificationByte = data.readByte();
            switch (identificationByte) {
                case 0x2C:
                    Object previousComponent = components.get(components.size() - 1);
                    GraphicControlExtension gcExtension = previousComponent instanceof GraphicControlExtension ? (GraphicControlExtension) previousComponent : null;

                    Image currentImage = new Image(data, gifHeader.getHeader().getBackgroundColor(), gcExtension, dictionary);

                    components.add(currentImage);
                    images.add(currentImage);

                    if (img == null) {
                        img = currentImage.getImageData().getBufferedImage();
                        ImageIO.write(img, "PNG", new File("frame-" + images.size() + ".png"));
                    } else {
                        Graphics g = img.getGraphics();
                        Image image = images.get(images.size() - 1);
                        g.drawImage(image.getImageData().getBufferedImage(), image.getLocalImageDescriptor().getLeft(), image.getLocalImageDescriptor().getTop(), image.getLocalImageDescriptor().getWidth(), image.getLocalImageDescriptor().getHeight(), null);
                        ImageIO.write(img, "PNG", new File("frame-" + images.size() + ".png"));
                    }
                    break;
                case 0x21:
                    Extension extension = new ExtensionInformation(data).getExtension();
                    components.add(extension);
                    extensions.add(extension);
                    break;
            }
        }
    }
}
