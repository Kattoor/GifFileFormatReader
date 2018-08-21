package extensioninformation;

import util.Buffer;

public class GraphicControlExtension {

    private int blockSize;
    private int packed;
    private int transparentColorFlag;
    private int userInputFlag;
    private int disposalMethod;
    private int reserved;
    private int delayTime;
    private int colorIndex;
    private int terminator;

    public GraphicControlExtension(Buffer data) {
        blockSize = data.readByte(); // 04 for graphiccontrolextension
        packed = data.readByte();
        transparentColorFlag = packed & 0b1;
        userInputFlag = (packed & 0b10) >> 1;
        disposalMethod = (packed & 0b11100) >> 2;
        reserved = (packed & 0b11100000) >> 5;
        delayTime = data.readWord();
        colorIndex = data.readByte();
        terminator = data.readByte();
    }

    public int getBlockSize() {
        return blockSize;
    }

    public int getPacked() {
        return packed;
    }

    public int getTransparentColorFlag() {
        return transparentColorFlag;
    }

    public int getUserInputFlag() {
        return userInputFlag;
    }

    public int getDisposalMethod() {
        return disposalMethod;
    }

    public int getReserved() {
        return reserved;
    }

    public int getDelayTime() {
        return delayTime;
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public int getTerminator() {
        return terminator;
    }
}
