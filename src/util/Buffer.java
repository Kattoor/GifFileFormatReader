package util;

public class Buffer {

    private byte[] bytes;
    private int pointer = 0;

    public Buffer(byte[] bytes) {
        this.bytes = bytes;
    }

    public int readByte() {
        int packed = bytes[pointer++];
        return packed & 0xFF;
    }

    public int readWord() {
        int first = bytes[pointer++];
        int second = bytes[pointer++];
        return ((second << 8) | (first & 0xFF));
    }

    public void incrementPointer(int incrementBy) {
        pointer += incrementBy;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int getPointer() {
        return pointer;
    }
}
