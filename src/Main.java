import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        new Main();
    }

    private byte[] data;
    private int pointer;

    private int packed;
    private int amountOfColorTableEntries;

    private int width;
    private int height;

    private Main() throws IOException {
        File file = new File("hamburger.gif");
        data = Files.readAllBytes(file.toPath());
        pointer = 6;

        readHeader();
        next();
    }

    private void next() {
        while (true) {
            int nextByte = readByte();
            switch (nextByte) {
                case 0x2C:
                    readBody();
                    break;
                case 0x21:
                    readExtensionInformation();
                    break;
            }
        }
    }

    private void readHeader() {
        readHeaderInHeader();
        readLogicalScreenDescriptionInHeader();
        readGlobalColorTableInHeader();
    }

    private void readHeaderInHeader() {
        int screenWidth = readWord();
        int screenHeight = readWord();
        packed = readByte(); // 1100 0100
        int backgroundColor = readByte(); // 15 => 21
        int aspectRatio = readByte();
    }

    private void readLogicalScreenDescriptionInHeader() {
        int globalColorTableSize = packed & 0b111;
        int colorTableSortFlag = (packed & 0b1000) >> 3;
        int colorResolution = (packed & 0b1110000) >> 4; // 4
        int globalColorTableFlag = (packed & 0b10000000) >> 7;
        amountOfColorTableEntries = 1 << (globalColorTableSize + 1); // 32
    }

    private int[][] colorTable;

    private void readGlobalColorTableInHeader() {
        colorTable = new int[amountOfColorTableEntries][3];
        for (int i = 0; i < amountOfColorTableEntries; i++) {
            int red = readByte();
            int green = readByte();
            int blue = readByte();
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

    private void readExtensionInformation() {
        int nextByte = readByte();
        switch (nextByte) {
            case 0xF9:
                readGraphicsControlExtensionBlock();
                break;
            case 0xFF:
                readApplicationExtensionBlock();
                break;
        }
    }

    private void readApplicationExtensionBlock() {
        int blockSize = readByte(); // 0B for applicationextensionblock
        int[] identifier = {readByte(), readByte(), readByte(), readByte(), readByte(), readByte(), readByte(), readByte()};
        int[] authentCode = {readByte(), readByte(), readByte()};

        int applicationDataSize = readByte(); // size of coming sub blocks in bytes 3C
        boolean lastSubBlockReached = applicationDataSize == 0;
        while (!(lastSubBlockReached)) {
            for (int i = 0; i < applicationDataSize; i++)
                readByte();
            applicationDataSize = readByte();
            lastSubBlockReached = applicationDataSize == 0;
        }

        /*int subBlockDataCount = 0;
        while (!lastSubBlockReached) {
            int amountOfBytesInThisSubBlock = readByte();
            subBlockDataCount++;
            if (amountOfBytesInThisSubBlock == 0)
                lastSubBlockReached = true;
            else {
                for (int i = 0; i < amountOfBytesInThisSubBlock; i++) {
                    readByte(); // ignore sub block data for now
                    subBlockDataCount++;
                }
            }
            if (subBlockDataCount == applicationDataSize)
                lastSubBlockReached = true;
        }*/

        // int terminator = readByte();
    }

    private void readGraphicsControlExtensionBlock() {
        int blockSize = readByte();     // 04 for graphicscontrolextensionblock
        int packed = readByte();
        int transparentColorFlag = packed & 0b1;
        int userInputFlag = (packed & 0b10) >> 1;
        int disposalMethod = (packed & 0b11100) >> 2;
        int reserved = (packed & 0b11100000) >> 5;
        int delayTime = readWord();
        int colorIndex = readByte();
        int terminator = readByte();
    }

    private void readBody() {
        /* foreach frame, do: */
        readImage();
    }

    private void readImage() {
        readImageDescriptor();
    }

    private void readImageDescriptor() {
        int left = readWord();
        int top = readWord();
        width = readWord();
        height = readWord();
        int packed = readByte();
        int localColorTableFlag = packed & 0b1;
        int interlaceFlag = (packed & 0b10) >> 1;
        int sortFlag = (packed & 0b100) >> 2;
        int reserved = (packed & 0b11000) >> 3;
        int sizeOfLocalTableEntry = (packed & 0b11100000) >> 5;
        if (localColorTableFlag == 1) {
            // use local color table
            getLocalColorTable(1 << (sizeOfLocalTableEntry + 1));
        } else {
            // use global color table
        }
        getImageData();
    }

    private void getLocalColorTable(int amountOfEntries) {
        int[][] localColorTable = new int[amountOfEntries][3];
        for (int i = 0; i < amountOfEntries; i++) {
            int red = readByte();
            int green = readByte();
            int blue = readByte();
            localColorTable[i] = new int[]{red, green, blue};
        }
    }

    private void getImageData() {
        int codeLength = readByte();
        boolean end = false;
        List<Integer> bytes = new ArrayList<>();
        do {
            int count = readByte();
            if (count == 0)
                end = true;
            else {
                for (int i = 0; i < count; i++)
                    bytes.add(readByte());
            }
        } while (!end);

        List<Integer> decompressed = new ArrayList<>(decompress(bytes, codeLength));

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (y * width + x < decompressed.size()) {
                    int rgb = decompressed.get(y * width + x);
                    image.setRGB(x, y, rgb);
                }
            }
        }

        try {
            ImageIO.write(image, "PNG", new File("test.png"));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                Thread.sleep(10000000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    List<Integer> total = new ArrayList<>();
    Map<Integer, List<Integer>> codeTable = new HashMap<>();


    private List<List<Integer>> dictionary = new ArrayList<>();
    private List<Integer> indices = new ArrayList<>();
    private List<Integer> usedCodes = new ArrayList<>();

    private int getCode(List<Integer> bytes, int bitOffset, int amountOfBitsToRead) {
        int bitsRemainingInFirstByte = 8 - (bitOffset % 8);
        int bitsToReadFromFirstByte = Math.min(bitsRemainingInFirstByte, amountOfBitsToRead);
        int amountOfBytesToReadFrom = (int) (Math.ceil((amountOfBitsToRead - bitsRemainingInFirstByte) / 8f) + 1);
        int amountOfBytesWeStillNeed = bitsRemainingInFirstByte >= amountOfBitsToRead ? 0 : amountOfBitsToRead - bitsRemainingInFirstByte;
        int bitsInLastByte = amountOfBytesWeStillNeed == 8 ? 8 : amountOfBytesWeStillNeed % 8;
        int firstByteIndex = bitOffset / 8;
        List<Integer> bytesToRead = bytes.subList(firstByteIndex, firstByteIndex + amountOfBytesToReadFrom);

        if (bitsToReadFromFirstByte < bitsRemainingInFirstByte && (bitOffset % 8 == 0)) {
            int firstByteValue = ((bytesToRead.get(0) & (int) (Math.pow(2, bitsToReadFromFirstByte) - 1)));

            return firstByteValue;
        } else {
            int firstByteValue = ((bytesToRead.get(0) & (256 - (int) Math.pow(2, 8 - bitsToReadFromFirstByte))) >> (8 - bitsToReadFromFirstByte));
            int bitsReadAlready = bitsRemainingInFirstByte;

            if (bytesToRead.size() == 1)
                return firstByteValue;
            if (bytesToRead.size() == 2) {
                firstByteValue = firstByteValue | ((bytesToRead.get(1) & (int) (Math.pow(2, bitsInLastByte) - 1)) << bitsReadAlready);
                bitsReadAlready += bitsInLastByte;
                return firstByteValue;
            }
            if (bytesToRead.size() == 3) {
                firstByteValue = firstByteValue | ((bytesToRead.get(1) & 0xff) << bitsReadAlready);
                bitsReadAlready += 8;
                firstByteValue = firstByteValue | ((bytesToRead.get(2) & (int) (Math.pow(2, bitsInLastByte) - 1)) << bitsReadAlready);
                bitsReadAlready += bitsInLastByte;
                return firstByteValue;

            }
        }
        return -10000;
    }

    private List<Integer> decompress(List<Integer> bytes, int codeLength) {
        int bitsToRead = codeLength + 1;
        int bitOffset = bitsToRead;
        int code = getCode(bytes, bitOffset, bitsToRead);
        usedCodes.add(code);

        List<Integer> previousCodeValue = new ArrayList<>();
        List<Integer> currentCodeValue = dictionary.get(code);

        indices.add(currentCodeValue.get(0));
        previousCodeValue = new ArrayList<>(currentCodeValue);

        bitOffset = bitsToRead * 2;

        boolean end = false;
        try {
            while (!end) {
                code = getCode(bytes, bitOffset, bitsToRead);
                bitOffset += bitsToRead;
                if (code == (Math.pow(2, codeLength))) {
                    bitsToRead = (codeLength + 1);
                    List<List<Integer>> newDictionary = new ArrayList<>();
                    for (int j = 0; j < Math.pow(2, codeLength) + 2; j++)
                        newDictionary.add(dictionary.get(j));
                    dictionary = newDictionary;
                    int c = getCode(bytes, bitOffset, bitsToRead);
                    bitOffset += bitsToRead;
                    usedCodes.add(c);
                    currentCodeValue = dictionary.get(c);
                    indices.add(currentCodeValue.get(0));
                    previousCodeValue = new ArrayList<>(dictionary.get(c));
                } else if (code == (Math.pow(2, codeLength) + 1))
                    end = true;

                usedCodes.add(code);

                if (code != (Math.pow(2, codeLength))) {

                    if (dictionary.size() > code) {
                        currentCodeValue = dictionary.get(code);
                        indices.addAll(new ArrayList<>(currentCodeValue));
                        int k = currentCodeValue.get(0);
                        List<Integer> toAdd = new ArrayList<>(previousCodeValue);
                        toAdd.add(k);
                        dictionary.add(toAdd);
                        if (dictionary.size() == Math.pow(2, bitsToRead) && bitsToRead < 12)
                            bitsToRead++;
                        previousCodeValue = new ArrayList<>(currentCodeValue);
                    } else {
                        int k = previousCodeValue.get(0);
                        List<Integer> toAdd = new ArrayList<>(previousCodeValue);
                        toAdd.add(k);
                        indices.addAll(new ArrayList<>(toAdd));
                        dictionary.add(new ArrayList<>(toAdd));
                        if (dictionary.size() == Math.pow(2, bitsToRead) && bitsToRead < 12)
                            bitsToRead++;
                        previousCodeValue = new ArrayList<>(dictionary.get(code));
                    }
                }
            }
        } catch (Exception e) {
            return indices;
        }
        return indices;
    }

    private int readByte() {
        int packed = data[pointer++];
        return packed & 0xFF;
    }

    private int readWord() {
        int first = data[pointer++];
        int second = data[pointer++];
        return ((second << 8) | (first & 0xFF));
    }
}
