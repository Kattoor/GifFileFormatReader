package extensioninformation;

import util.Buffer;

public class ExtensionInformation {

    public ExtensionInformation(Buffer data) {
        int extensionClassificationByte = data.readByte();
        switch (extensionClassificationByte) {
            case 0xF9:
                new GraphicControlExtension(data);
                break;
            case 0xFF:
                new ApplicationExtension(data);
                break;
        }
    }
}
