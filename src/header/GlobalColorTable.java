package header;

import util.Buffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GlobalColorTable {

    private int[][] colorTable;
    private List<List<Integer>> dictionary = new ArrayList<>();

    public GlobalColorTable(Buffer data, int amountOfColorTableEntries) {
        colorTable = new int[amountOfColorTableEntries][3];
        for (int i = 0; i < amountOfColorTableEntries; i++) {
            int red = data.readByte();
            int green = data.readByte();
            int blue = data.readByte();
            colorTable[i] = new int[]{red, green, blue};
        }

        for (int[] i : colorTable) {
            int rgb = i[0];
            rgb = (rgb << 8) + i[1];
            rgb = (rgb << 8) + i[2];
            dictionary.add(Collections.singletonList(rgb));
        }

        dictionary.add(Collections.singletonList(-1));
        dictionary.add(Collections.singletonList(-2));
    }

    public int[][] getColorTable() {
        return colorTable;
    }

    public List<List<Integer>> getDictionary() {
        return dictionary;
    }
}
