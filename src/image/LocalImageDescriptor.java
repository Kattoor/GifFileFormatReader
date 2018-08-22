package image;

import util.Buffer;

public class LocalImageDescriptor {

    private int left, top, width, height;
    private int packed;
    private int localColorTableFlag;
    private int interlaceFlag;
    private int sortFlag;
    private int reserved;
    private int sizeOfLocalTableEntry;
    private int amountOfColorTableEntries;

    public LocalImageDescriptor(Buffer data) {
        left = data.readWord();
        top = data.readWord();
        width = data.readWord();
        height = data.readWord();
        System.out.println(left + ", " + top + ", " + width + ", " + height);
        packed = data.readByte();
        localColorTableFlag = packed & 0b1;
        interlaceFlag = (packed & 0b10) >> 1;
        sortFlag = (packed & 0b100) >> 2;
        reserved = (packed & 0b11000) >> 3;
        sizeOfLocalTableEntry = (packed & 0b11100000) >> 5;
        amountOfColorTableEntries = localColorTableFlag == 1 ? 1 << (sizeOfLocalTableEntry + 1) : 0;
    }

    public int getLeft() {
        return left;
    }

    public int getTop() {
        return top;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getPacked() {
        return packed;
    }

    public int getLocalColorTableFlag() {
        return localColorTableFlag;
    }

    public int getInterlaceFlag() {
        return interlaceFlag;
    }

    public int getSortFlag() {
        return sortFlag;
    }

    public int getReserved() {
        return reserved;
    }

    public int getSizeOfLocalTableEntry() {
        return sizeOfLocalTableEntry;
    }

    public int getAmountOfColorTableEntries() {
        return amountOfColorTableEntries;
    }
}
