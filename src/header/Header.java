package header;

import util.Buffer;

public class Header {

    private int screenWidth, screenHeight;
    private int packed;
    private int backgroundColor;
    private int aspectRatio;

    public Header(Buffer data) {

        screenWidth = data.readWord();
        screenHeight = data.readWord();
        packed = data.readByte();
        backgroundColor = data.readByte();
        aspectRatio = data.readByte();
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public int getPacked() {
        return packed;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getAspectRatio() {
        return aspectRatio;
    }
}
