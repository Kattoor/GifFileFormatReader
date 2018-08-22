package util;

import java.util.ArrayList;
import java.util.List;

public class LWZDecompressor {

    public static List<Integer> decompress(List<Integer> bytes, int codeLength, List<List<Integer>> dictionary) {
        int bitsToRead = codeLength + 1;
        int bitOffset = bitsToRead;

        dictionary = dictionary.subList(0, (int) Math.pow(2, codeLength) + 2);

        int code = getCode(bytes, bitOffset, bitsToRead);

        List<Integer> previousCodeValue;
        List<Integer> currentCodeValue = dictionary.get(code);

        List<Integer> indices = new ArrayList<>();

        indices.add(currentCodeValue.get(0));
        previousCodeValue = new ArrayList<>(currentCodeValue);

        bitOffset = bitsToRead * 2;

        boolean end = false;
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
                currentCodeValue = dictionary.get(c);
                indices.add(currentCodeValue.get(0));
                previousCodeValue = new ArrayList<>(dictionary.get(c));
            } else if (code == (Math.pow(2, codeLength) + 1))
                end = true;

            if (code != (Math.pow(2, codeLength))) {

                if (dictionary.size() > code) {
                    currentCodeValue = dictionary.get(code);
                    indices.addAll(new ArrayList<>(currentCodeValue));
                    int k = currentCodeValue.get(0);
                    List<Integer> toAdd = new ArrayList<>(previousCodeValue);
                    toAdd.add(k);
                    if (dictionary.size() != 4096)
                        dictionary.add(toAdd);
                    if (dictionary.size() == Math.pow(2, bitsToRead) && bitsToRead < 12)
                        bitsToRead++;
                    previousCodeValue = new ArrayList<>(currentCodeValue);
                } else {
                    int k = previousCodeValue.get(0);
                    List<Integer> toAdd = new ArrayList<>(previousCodeValue);
                    toAdd.add(k);
                    indices.addAll(new ArrayList<>(toAdd));
                    if (dictionary.size() != 4096)
                        dictionary.add(new ArrayList<>(toAdd));
                    if (dictionary.size() == Math.pow(2, bitsToRead) && bitsToRead < 12)
                        bitsToRead++;
                    previousCodeValue = new ArrayList<>(dictionary.get(code));
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
