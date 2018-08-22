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

    public Buffer getData() {
        return data;
    }

    public Header getHeader() {
        return header;
    }

    public LogicalScreenDescriptor getLogicalScreenDescriptor() {
        return logicalScreenDescriptor;
    }

    public GlobalColorTable getGlobalColorTable() {
        return globalColorTable;
    }
}
