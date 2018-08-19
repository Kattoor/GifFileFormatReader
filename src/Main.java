import javafx.application.Application;

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
        List<Integer> decompressed = new ArrayList<>();
        do {
            int count = readByte();
            int[] bytes = new int[count];
            if (count == 0)
                end = true;
            else {
                for (int i = 0; i < count; i++)
                    bytes[i] = readByte();
                decompressed.addAll(decompress(bytes));
            }
        } while (!end);

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



        /*total.forEach(integer -> {
            if (colorTable.length < integer)
                System.out.println("WTF");
        });
        System.out.println(total.size());*/
    }

    List<Integer> total = new ArrayList<>();
    Map<Integer, List<Integer>> codeTable = new HashMap<>();


    List<List<Integer>> dictionary = new ArrayList<>();
    List<Integer> indices = new ArrayList<>();
    List<Integer> usedCodes = new ArrayList<>();
    private int bitsToRead = 8;
    private int counter = 0;
    private List<Integer> decompress(int[] bytes) {
        int code = bytes[1];
        usedCodes.add(code);
        List<Integer> previousCodeValue = new ArrayList<Integer>();
        List<Integer> currentCodeValue = dictionary.get(code);

        indices.add(currentCodeValue.get(0));

        for (int i = 2; i < bytes.length; i++) {
            code = bytes[i];
            // 0000 0000 1111 1111
            // 0000 0000 1111 1111
            // 1011 1011 0
            if (bitsToRead == 9) {
                int first = bytes[i + (counter / 8)];
                int second = bytes[i + 1 + (counter / 8)];

                int bytesFromFirst = first & Integer.parseInt("11111111".substring(counter % 8), 2);
                int bytesFromLast = second & Integer.parseInt("11111111".substring(8 - ((counter % 8) + 1)) + "00000000".substring(8 - (counter % 8)), 2);
                code = (bytesFromFirst << 8) + bytesFromLast;

                counter++;
            }
            usedCodes.add(code);

            if (dictionary.size() > code) {
                currentCodeValue = dictionary.get(code);
                indices.addAll(new ArrayList<>(currentCodeValue));
                int k = currentCodeValue.get(0);
                List<Integer> toAdd = new ArrayList<>(previousCodeValue);
                toAdd.add(k);
                dictionary.add(toAdd);
                if (dictionary.size() == 256) {
                    bitsToRead = 9;
                }
                previousCodeValue = new ArrayList<>(currentCodeValue);
            } else {
                int k = previousCodeValue.get(0);
                List<Integer> toAdd = new ArrayList<>(previousCodeValue);
                toAdd.add(k);
                indices.addAll(new ArrayList<>(toAdd));
                dictionary.add(new ArrayList<>(toAdd));
                if (dictionary.size() == 256) {
                    bitsToRead = 9;
                }
                previousCodeValue = new ArrayList<>(dictionary.get(code));
            }
        }

        return indices;


/*


        int code = bytes[1];
        List<Integer> indexStream = new ArrayList<>(codeTable.get(code));

        for (int i = 2; i < bytes.length; i++) {
            code = bytes[i];
            int previousCode = bytes[i - 1];
            if (codeTable.containsKey(code)) {
                indexStream.addAll(codeTable.get(code));
                int k = codeTable.get(code).get(0);
                List<Integer> l = codeTable.get(previousCode);
                List<Integer> list = new ArrayList<>(l);
                list.add(k);
                codeTable.put(codeTable.size(), list);
            } else {
                int k = codeTable.get(previousCode).get(0);
                List<Integer> list = new ArrayList<>(codeTable.get(previousCode));
                list.add(k);
                indexStream.addAll(list);
                codeTable.put(codeTable.size(), list);
            }
        }

        total.addAll(indexStream);*/
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
