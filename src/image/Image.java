package image;

import util.Buffer;

import java.util.List;

public class Image {

    private LocalImageDescriptor localImageDescriptor;
    private LocalColorTable localColorTable;
    private ImageData imageData;

    public Image(Buffer data, int backgroundColorIndex, List<List<Integer>> dictionary) {
        localImageDescriptor = new LocalImageDescriptor(data);
        localColorTable = new LocalColorTable(data, localImageDescriptor.getAmountOfColorTableEntries());
        imageData = new ImageData(data, localImageDescriptor.getWidth(), localImageDescriptor.getHeight(), backgroundColorIndex, 21, dictionary);
    }

    public LocalImageDescriptor getLocalImageDescriptor() {
        return localImageDescriptor;
    }

    public LocalColorTable getLocalColorTable() {
        return localColorTable;
    }

    public ImageData getImageData() {
        return imageData;
    }
}
