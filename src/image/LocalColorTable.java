package image;

import util.Buffer;

public class LocalColorTable {

    private int[][] localColorTable;

    public LocalColorTable(Buffer data, int amountOfColorTableEntries) {
        localColorTable = new int[amountOfColorTableEntries][3];
        for (int i = 0; i < amountOfColorTableEntries; i++) {
            int red = data.readByte();
            int green = data.readByte();
            int blue = data.readByte();
            localColorTable[i] = new int[]{red, green, blue};
        }
    }

    public int[][] getLocalColorTable() {
        return localColorTable;
    }
}
