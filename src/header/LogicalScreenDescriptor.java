package header;

public class LogicalScreenDescriptor {

    private int globalColorTableSize;
    private int colorTableSortFlag;
    private int colorResolution;
    private int globalColorTableFlag;
    private int amountOfColorTableEntries;

    public LogicalScreenDescriptor(int packed) {
        globalColorTableSize = packed & 0b111;
        colorTableSortFlag = (packed & 0b1000) >> 3;
        colorResolution = (packed & 0b1110000) >> 4;
        globalColorTableFlag = (packed & 0b10000000) >> 7;
        amountOfColorTableEntries = 1 << (globalColorTableSize + 1);
    }

    public int getGlobalColorTableSize() {
        return globalColorTableSize;
    }

    public int getColorTableSortFlag() {
        return colorTableSortFlag;
    }

    public int getColorResolution() {
        return colorResolution;
    }

    public int getGlobalColorTableFlag() {
        return globalColorTableFlag;
    }

    public int getAmountOfColorTableEntries() {
        return amountOfColorTableEntries;
    }
}
