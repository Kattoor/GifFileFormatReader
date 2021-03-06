package image;

import extensioninformation.GraphicControlExtension;
import util.Buffer;
import util.ColorAndIndex;
import util.LWZDecompressor;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ImageData {

    private int width, height;
    private int backgroundColorIndex;
    private GraphicControlExtension graphicControlExtension;

    private List<List<Integer>> dictionary;
    private BufferedImage bufferedImage;

    public ImageData(Buffer data, int width, int height, int backgroundColorIndex, GraphicControlExtension graphicControlExtension, List<List<Integer>> dictionary) {
        this.width = width;
        this.height = height;
        this.backgroundColorIndex = backgroundColorIndex;
        this.graphicControlExtension = graphicControlExtension;
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

        List<ColorAndIndex> decompressed = new ArrayList<>(LWZDecompressor.decompress(bytes, codeLength, dictionary));

        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (y * width + x < decompressed.size()) {
                    ColorAndIndex colorAndIndex = decompressed.get(y * width + x);
                    if (graphicControlExtension == null || graphicControlExtension.getTransparentColorFlag() == 0 || colorAndIndex.getIndex() != graphicControlExtension.getTransparentColorIndex())
                        bufferedImage.setRGB(x, y, colorAndIndex.getColor() | 0xff000000);
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
