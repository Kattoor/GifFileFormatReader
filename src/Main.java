import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {
        new Main();
    }

    private byte[] data;
    private int pointer;

    private int packed;
    private int amountOfColorTableEntries;

    private Main() throws IOException {
        File file = new File("yugioh.gif");
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
                    /*System.out.println(pointer);
                    System.out.println(Integer.toHexString(pointer));*/
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
        packed = readByte();
        int backgroundColor = readByte();
        int aspectRatio = readByte();
    }

    private void readLogicalScreenDescriptionInHeader() {
        int globalColorTableSize = packed & 0b111;
        int colorTableSortFlag = (packed & 0b1000) >> 3;
        int colorResolution = (packed & 0b1110000) >> 4;
        int globalColorTableFlag = (packed & 0b10000000) >> 7;
        amountOfColorTableEntries = 1 << (globalColorTableSize + 1);
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
            int rgb = i[0]; //
            rgb = (rgb << 8) + i[1];
            rgb = (rgb << 8) + i[2];
            List<Integer> l = new ArrayList<>();
            l.add(rgb);
            dictionary.add(l);
        }
        List<Integer> clearCode = new ArrayList<>();
        clearCode.add(-1);
        dictionary.add(clearCode);
        List<Integer> endOfInformationCode = new ArrayList<>();
        endOfInformationCode.add(-2);
        dictionary.add(endOfInformationCode);
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
        int blockSize = readByte();     // 0Bh for  for applicationextensionblock
        int[] identifier = {readByte(), readByte(), readByte(), readByte(), readByte(), readByte(), readByte(), readByte()};
        int[] authentCode = {readByte(), readByte(), readByte()};

        int applicationDataSize = readByte(); // size of coming sub blocks in bytes
        boolean lastSubBlockReached = applicationDataSize == 0;
        int subBlockDataCount = 0;
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
        }

        int terminator = readByte();
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
        int width = readWord();
        int height = readWord();
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
        readByte();
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

        List<Integer> decompressed = new ArrayList<>(decompress(bytes));

        BufferedImage image = new BufferedImage(500, 375, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 375; y++) {
            for (int x = 0; x < 500; x++) {
                /*value = red;
        value = (value << 8) + green;
        value = (value << 8) + blue;*/
                /*int[] ints = new int[]{0, 0, 0};
                if (y * 375 + x < total.size() && total.get(y * 375 + x) >= 0)
                    ints = colorTable[total.get(y * 375 + x)];
                int rgb = ints[0];
                rgb = (rgb << 8) + ints[1];
                rgb = (rgb << 8) + ints[2];*/
                //System.out.println(y + " " + x) ;
                if (y * 500 + x < decompressed.size()) {
                    int rgb = decompressed.get(y * 500 + x);
                    image.setRGB(x, y, rgb);
                }
            }
        }

        try {
            ImageIO.write(image, "PNG", new File("test2.png"));
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


    List<List<Integer>> dictionary = new ArrayList<>();
    List<Integer> indices = new ArrayList<>();
    List<Integer> usedCodes = new ArrayList<>();
    private int bitsToRead = 8;
    int bitOffset = 0;

    int resetted = -1;

    private List<Integer> decompress(List<Integer> bytes) {
        int code = bytes.get(1);
        usedCodes.add(code);
        List<Integer> previousCodeValue = new ArrayList<Integer>();
        List<Integer> currentCodeValue = dictionary.get(code);

        indices.add(currentCodeValue.get(0));

        bitOffset = 16;

        try {
            for (int i = 2; i < bytes.size(); i++) {
                if (bitsToRead == 8) {
                    code = bytes.get(bitOffset / 8);
                    if (code == 129 || code == 128)
                        System.out.println("hi");
                } else if (bitsToRead == 9) {
                    int first = bytes.get(bitOffset / 8);
                    int second = bytes.get((bitOffset / 8) + 1);
                    int mod = bitOffset % 8;
                    if (mod == 0)
                        code = ((first & 0b11111111) | ((second & 0b00000001) << 8));
                    else if (mod == 1)
                        code = (((first & 0b11111110) >> 1) | ((second & 0b00000011) << 7));
                    else if (mod == 2)
                        code = (((first & 0b11111100) >> 2) | ((second & 0b00000111) << 6));
                    else if (mod == 3)
                        code = (((first & 0b11111000) >> 3) | ((second & 0b00001111) << 5));
                    else if (mod == 4)
                        code = (((first & 0b11110000) >> 4) | ((second & 0b00011111) << 4));
                    else if (mod == 5)
                        code = (((first & 0b11100000) >> 5) | ((second & 0b00111111) << 3));
                    else if (mod == 6)
                        code = (((first & 0b11000000) >> 6) | ((second & 0b01111111) << 2));
                    else if (mod == 7)
                        code = (((first & 0b10000000) >> 7) | ((second & 0b11111111) << 1));
                    if (code == 129 || code == 128)
                        System.out.println("hi");
                } else if (bitsToRead == 10) {
                    int first = bytes.get(bitOffset / 8);
                    int second = bytes.get((bitOffset / 8) + 1);
                    int third = bytes.get((bitOffset / 8) + 2);
                    int mod = bitOffset % 8;
                    if (mod == 0)
                        code = ((first & 0b11111111) | ((second & 0b00000011) << 8)); //
                    else if (mod == 1)
                        code = (((first & 0b11111110) >> 1) | ((second & 0b00000111) << 7));
                    else if (mod == 2)
                        code = (((first & 0b11111100) >> 2) | ((second & 0b00001111) << 6)); //
                    else if (mod == 3)
                        code = (((first & 0b11111000) >> 3) | ((second & 0b00011111) << 5));
                    else if (mod == 4)
                        code = (((first & 0b11110000) >> 4) | ((second & 0b00111111) << 4)); //
                    else if (mod == 5)
                        code = (((first & 0b11100000) >> 5) | ((second & 0b01111111) << 3));
                    else if (mod == 6)
                        code = (((first & 0b11000000) >> 6) | ((second & 0b11111111) << 2));
                    else if (mod == 7)
                        code = (((first & 0b10000000) >> 7) | ((second & 0b11111111) << 1) | ((third & 0b00000001) << 9));
                    if (code == 129 || code == 128)
                        System.out.println("hi");
                } else if (bitsToRead == 11) {
                    int first = bytes.get(bitOffset / 8);
                    int second = bytes.get((bitOffset / 8) + 1);
                    int third = bytes.get((bitOffset / 8) + 2);
                    int mod = bitOffset % 8;
                    if (mod == 0)
                        code = ((first & 0b11111111) | ((second & 0b00000111) << 8)); //
                    else if (mod == 1)
                        code = (((first & 0b11111110) >> 1) | ((second & 0b00001111) << 7));
                    else if (mod == 2)
                        code = (((first & 0b11111100) >> 2) | ((second & 0b00011111) << 6)); //
                    else if (mod == 3)
                        code = (((first & 0b11111000) >> 3) | ((second & 0b00111111) << 5));
                    else if (mod == 4)
                        code = (((first & 0b11110000) >> 4) | ((second & 0b01111111) << 4)); //
                    else if (mod == 5)
                        code = (((first & 0b11100000) >> 5) | ((second & 0b11111111) << 3));
                    else if (mod == 6)
                        code = (((first & 0b11000000) >> 6) | ((second & 0b11111111) << 2) | ((third & 0b00000001) << 10));
                    else if (mod == 7)
                        code = (((first & 0b10000000) >> 7) | ((second & 0b11111111) << 1) | ((third & 0b00000011) << 9));
                    if (code == 129 || code == 128)
                        System.out.println("hi");
                } else if (bitsToRead == 12) {
                    int first = bytes.get(bitOffset / 8);
                    int second = bytes.get((bitOffset / 8) + 1);
                    int third = 0;
                    if (bytes.size() > (bitOffset / 8) + 2)
                        bytes.get((bitOffset / 8) + 2);
                    int mod = bitOffset % 8;
                    if (mod == 0)
                        code = ((first & 0b11111111) | ((second & 0b00001111) << 8));
                    else if (mod == 1)
                        code = (((first & 0b11111110) >> 1) | ((second & 0b00011111) << 7));
                    else if (mod == 2)
                        code = (((first & 0b11111100) >> 2) | ((second & 0b00111111) << 6));
                    else if (mod == 3)
                        code = (((first & 0b11111000) >> 3) | ((second & 0b01111111) << 5));
                    else if (mod == 4)
                        code = (((first & 0b11110000) >> 4) | ((second & 0b11111111) << 4));
                    else if (mod == 5)
                        code = (((first & 0b11100000) >> 5) | ((second & 0b11111111) << 3) | ((third & 0b00000001) << 10));
                    else if (mod == 6)
                        code = (((first & 0b11000000) >> 6) | ((second & 0b11111111) << 2) | ((third & 0b00000011) << 9));
                    else if (mod == 7)
                        code = (((first & 0b10000000) >> 7) | ((second & 0b11111111) << 1) | ((third & 0b00000111) << 8));
                    if (code == 129 || code == 128) {
                        bitsToRead = 8;
                        bitOffset += 12;
                        List<List<Integer>> newDictionary = new ArrayList<>();
                        for (int j = 0; j < 130; j++)
                            newDictionary.add(dictionary.get(j));
                        dictionary = newDictionary;

                        int c = bytes.get((bitOffset) / 8);
                        usedCodes.add(c);
                        currentCodeValue = dictionary.get(c);
                        indices.add(currentCodeValue.get(0));

                        // currentCodeValue = new ArrayList<>();
                        //currentCodeValue.add((int) Math.pow(2, 8) - 1);
                        // previousCodeValue = new ArrayList<>();

                        //  currentCodeValue = new ArrayList<>();
                        // previousCodeValue = new ArrayList<>();
                        //   resetted = 0;
                    }
                }

                bitOffset += bitsToRead;

                if (resetted > -1)
                    resetted++;

                System.out.println(resetted);

                usedCodes.add(code);

                try {

                    if (code != 128) {

                        if (dictionary.size() > code) {
                            if (indices.size() > 22990)
                                System.out.println("ok");
                            currentCodeValue = dictionary.get(code);
                            indices.addAll(new ArrayList<>(currentCodeValue));
                            int k = currentCodeValue.get(0);
                            List<Integer> toAdd = new ArrayList<>(previousCodeValue);
                            toAdd.add(k);
                            dictionary.add(toAdd);
                            if (dictionary.size() == Math.pow(2, bitsToRead) && bitsToRead < 12) {
                                // if (code == 75)
                                //      return indices;
                                // else
                                bitsToRead++;
                            }
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
                } catch (Exception e) {
                    return indices;
                }
            }
        } catch (Exception ex) {
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
