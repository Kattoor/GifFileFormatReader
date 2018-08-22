package extensioninformation;

import util.Buffer;

public class ExtensionInformation {

    private Extension extension;

    public ExtensionInformation(Buffer data) {
        int extensionClassificationByte = data.readByte();
        switch (extensionClassificationByte) {
            case 0xF9:
                this.extension = new GraphicControlExtension(data);
                break;
            case 0xFF:
                this.extension = new ApplicationExtension(data);
                break;
            default:
                this.extension = null;
                break;
        }
    }

    public Extension getExtension() {
        return extension;
    }
}
