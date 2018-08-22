package image;

import util.Buffer;
import util.LWZDecompressor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageData {

    private int width, height;

    private List<List<Integer>> dictionary;
    private BufferedImage bufferedImage;

    public ImageData(Buffer data, int width, int height, List<List<Integer>> dictionary) {
        this.width = width;
        this.height = height;
        this.dictionary = dictionary;
        readImageData(data);
    }

    private void readImageData(Buffer data) {
        int codeLength = data.readByte();
        boolean end = false;
        List<Integer> bytes = new ArrayList<>();
        do {
            int count = data.readByte();
            if (count == 0)
                end = true;
            else {
                for (int i = 0; i < count; i++)
                    bytes.add(data.readByte());
            }
        } while (!end);

        List<Integer> decompressed = new ArrayList<>(LWZDecompressor.decompress(bytes, codeLength, dictionary));

        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (y * width + x < decompressed.size()) {
                    int rgb = decompressed.get(y * width + x);
                    bufferedImage.setRGB(x, y, rgb);
                }
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<List<Integer>> getDictionary() {
        return dictionary;
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }
}
