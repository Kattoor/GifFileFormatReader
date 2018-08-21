import extensioninformation.ExtensionInformation;
import header.GifHeader;
import image.Image;
import util.Buffer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        new Main();
    }

    private Buffer data;

    private GifHeader gifHeader;
    private List<List<Integer>> dictionary;

    private Main() throws IOException {
        File file = new File("hamburger.gif");
        data = new Buffer(Files.readAllBytes(file.toPath()));
        data.incrementPointer(6);

        gifHeader = new GifHeader(data);
        dictionary = gifHeader.getDictionary();

        next();
    }

    private void next() {
        while (true) {
            int nextByte = data.readByte();
            switch (nextByte) {
                case 0x2C:
                    new Image(data, dictionary);
                    break;
                case 0x21:
                    new ExtensionInformation(data);
                    break;
            }
        }
    }
}
