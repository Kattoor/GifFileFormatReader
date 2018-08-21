package header;

import util.Buffer;

import java.util.List;

public class GifHeader {

    private Buffer data;

    private Header header;
    private LogicalScreenDescriptor logicalScreenDescriptor;
    private GlobalColorTable globalColorTable;

    public GifHeader(Buffer data) {
        this.data = data;
        header = new Header(data);
        logicalScreenDescriptor = new LogicalScreenDescriptor(header.getPacked());
        globalColorTable = new GlobalColorTable(data, logicalScreenDescriptor.getAmountOfColorTableEntries());
    }

    public List<List<Integer>> getDictionary() {
        return globalColorTable.getDictionary();
    }
}
