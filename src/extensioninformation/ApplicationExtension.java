package extensioninformation;

import util.Buffer;

public class ApplicationExtension implements Extension {

    private int[] identifier;
    private int[] authCode;

    public ApplicationExtension(Buffer data) {
        int blockSize = data.readByte(); // 0B for applicationextension
        identifier = new int[]{data.readByte(), data.readByte(), data.readByte(), data.readByte(), data.readByte(), data.readByte(), data.readByte(), data.readByte()};
        authCode = new int[]{data.readByte(), data.readByte(), data.readByte()};

        int applicationDataSize = data.readByte();
        boolean lastSubBlockReached = applicationDataSize == 0;
        while (!(lastSubBlockReached)) {
            for (int i = 0; i < applicationDataSize; i++)
                data.readByte(); // todo: handle the data
            applicationDataSize = data.readByte();
            lastSubBlockReached = applicationDataSize == 0;
        }
    }

    public int[] getIdentifier() {
        return identifier;
    }

    public int[] getAuthCode() {
        return authCode;
    }
}
