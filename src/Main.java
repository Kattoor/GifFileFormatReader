import extensioninformation.Extension;
import extensioninformation.ExtensionInformation;
import header.GifHeader;
import image.Image;
import util.Buffer;

import javax.imageio.ImageIO;
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
    private List<Extension> extensions;
    private List<Image> images;

    private List<List<Integer>> dictionary;

    private Main() throws IOException {
        File file = new File("hamburger.gif");
        data = new Buffer(Files.readAllBytes(file.toPath()));
        data.incrementPointer(6);

        gifHeader = new GifHeader(data);
        dictionary = gifHeader.getDictionary();
        extensions = new ArrayList<>();
        images = new ArrayList<>();

        readExtensionsAndImages();
    }

    private void readExtensionsAndImages() {
        while (true) {
            int identificationByte = data.readByte();
            switch (identificationByte) {
                case 0x2C:
                    System.out.println("Pointer before reading image " + (images.size() + 1) + ": " + Integer.toHexString(data.getPointer()));
                    images.add(new Image(data, dictionary));
                    System.out.println("Pointer after reading image " + (images.size()) + ": " + Integer.toHexString(data.getPointer()));
                    try {
                        ImageIO.write(images.get(images.size() - 1).getImageData().getBufferedImage(), "PNG", new File("frame-" + images.size() + ".png"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 0x21:
                    System.out.println("Pointer before reading extension " + (extensions.size() + 1) + ": " + Integer.toHexString(data.getPointer()));
                    extensions.add(new ExtensionInformation(data).getExtension());
                    System.out.println("Pointer after reading extension " + (extensions.size()) + ": " + Integer.toHexString(data.getPointer()));
                    break;
            }
        }
    }
}
