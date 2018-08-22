package util;

import java.util.ArrayList;
import java.util.List;


public class LWZDecompressor {

    public static List<ColorAndIndex> decompress(List<Integer> bytes, int codeLength, List<List<Integer>> dict) {
        int bitsToRead = codeLength + 1;
        int bitOffset = bitsToRead;

        dict = dict.subList(0, (int) Math.pow(2, codeLength) + 2);
        List<List<ColorAndIndex>> dictionary = new ArrayList<>();
        for (int i = 0; i < dict.size(); i++) {
            List<ColorAndIndex> l = new ArrayList<>();
            l.add(new ColorAndIndex(dict.get(i).get(0), i));
            dictionary.add(l);
        }

        int code = getCode(bytes, bitOffset, bitsToRead);

        List<ColorAndIndex> previousCodeValue;
        List<ColorAndIndex> currentCodeValue = new ArrayList<>();
        currentCodeValue.addAll(dictionary.get(code));

        List<ColorAndIndex> indices = new ArrayList<>();

        indices.add(currentCodeValue.get(0));
        previousCodeValue = new ArrayList<>(currentCodeValue);

        bitOffset = bitsToRead * 2;

        boolean end = false;
        while (!end) {
            code = getCode(bytes, bitOffset, bitsToRead);
            bitOffset += bitsToRead;
            if (code == (Math.pow(2, codeLength))) {
                bitsToRead = (codeLength + 1);
                List<List<ColorAndIndex>> newDictionary = new ArrayList<>();
                for (int j = 0; j < Math.pow(2, codeLength) + 2; j++)
                    newDictionary.add(dictionary.get(j));
                dictionary = newDictionary;
                int c = getCode(bytes, bitOffset, bitsToRead);
                bitOffset += bitsToRead;
                currentCodeValue = new ArrayList<>(dictionary.get(c));
                indices.add(currentCodeValue.get(0));
                previousCodeValue = new ArrayList<>(dictionary.get(c));
            } else if (code == (Math.pow(2, codeLength) + 1))
                end = true;

            if (code != (Math.pow(2, codeLength))) {
                if (dictionary.size() > code) {
                    currentCodeValue = new ArrayList<>(dictionary.get(code));
                    indices.addAll(new ArrayList<>(currentCodeValue));
                    ColorAndIndex k = currentCodeValue.get(0);
                    List<ColorAndIndex> toAdd = new ArrayList<>();
                    for (ColorAndIndex colorAndIndex : previousCodeValue)
                        toAdd.add(new ColorAndIndex(colorAndIndex.getColor(), colorAndIndex.getIndex()));
                    toAdd.add(k);
                    if (dictionary.size() != 4096)
                        dictionary.add(toAdd);
                    if (dictionary.size() == Math.pow(2, bitsToRead) && bitsToRead < 12)
                        bitsToRead++;
                    previousCodeValue = new ArrayList<>(currentCodeValue);
                } else {
                    ColorAndIndex k = previousCodeValue.get(0);
                    List<ColorAndIndex> toAdd = new ArrayList<>();
                    for (ColorAndIndex colorAndIndex : previousCodeValue)
                        toAdd.add(new ColorAndIndex(colorAndIndex.getColor(), colorAndIndex.getIndex()));
                    toAdd.add(k);
                    for (ColorAndIndex colorAndIndex : toAdd)
                        indices.add(new ColorAndIndex(colorAndIndex.getColor(), colorAndIndex.getIndex()));
                    if (dictionary.size() != 4096)
                        dictionary.add(new ArrayList<>(toAdd));
                    if (dictionary.size() == Math.pow(2, bitsToRead) && bitsToRead < 12)
                        bitsToRead++;
                    previousCodeValue = new ArrayList<>();
                    for (ColorAndIndex colorAndIndex : dictionary.get(code))
                        previousCodeValue.add(new ColorAndIndex(colorAndIndex.getColor(), colorAndIndex.getIndex()));
                }
            }
        }

        return indices;
    }

    private static int getCode(List<Integer> bytes, int bitOffset, int amountOfBitsToRead) {
        int bitsRemainingInFirstByte = 8 - (bitOffset % 8);
        int bitsToReadFromFirstByte = Math.min(bitsRemainingInFirstByte, amountOfBitsToRead);

        int amountOfBitsWeStillNeed = bitsRemainingInFirstByte >= amountOfBitsToRead ? 0 : amountOfBitsToRead - bitsRemainingInFirstByte;
        int bitsToReadFromLastByte = amountOfBitsWeStillNeed == 8 ? 8 : amountOfBitsWeStillNeed % 8;

        int amountOfBytesToReadFrom = (int) (Math.ceil((amountOfBitsToRead - bitsRemainingInFirstByte) / 8f) + 1);
        int firstByteIndex = bitOffset / 8;

        List<Integer> bytesToRead = bytes.subList(firstByteIndex, firstByteIndex + amountOfBytesToReadFrom);

        if (bitsToReadFromFirstByte < bitsRemainingInFirstByte && (bitOffset % 8 == 0))
            return ((bytesToRead.get(0) & (int) (Math.pow(2, bitsToReadFromFirstByte) - 1)));
        else {
            int firstByteValue = ((bytesToRead.get(0) & (256 - (int) Math.pow(2, 8 - bitsToReadFromFirstByte))) >> (8 - bitsToReadFromFirstByte));
            int bitsReadAlready = bitsRemainingInFirstByte;

            if (bytesToRead.size() == 1)
                return firstByteValue;
            if (bytesToRead.size() == 2) {
                firstByteValue = firstByteValue | ((bytesToRead.get(1) & (int) (Math.pow(2, bitsToReadFromLastByte) - 1)) << bitsReadAlready);
                bitsReadAlready += bitsToReadFromLastByte;
                return firstByteValue;
            }
            if (bytesToRead.size() == 3) {
                firstByteValue = firstByteValue | ((bytesToRead.get(1) & 0xff) << bitsReadAlready);
                bitsReadAlready += 8;
                firstByteValue = firstByteValue | ((bytesToRead.get(2) & (int) (Math.pow(2, bitsToReadFromLastByte) - 1)) << bitsReadAlready);
                bitsReadAlready += bitsToReadFromLastByte;
                return firstByteValue;
            }
        }
        return -10000;
    }
}
