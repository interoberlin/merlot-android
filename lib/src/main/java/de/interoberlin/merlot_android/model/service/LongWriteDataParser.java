package de.interoberlin.merlot_android.model.service;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class LongWriteDataParser {
    // <editor-fold defaultstate="collapsed" desc="Members">

    private boolean firstPackage = true;
    private int start = 0;
    private int end = 0;
    private byte[] data;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructor">

    public LongWriteDataParser(byte[] data) {
        this.data = data == null ? new byte[]{} : data;
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    public byte[] getData() {
        //if package is empty
        if (firstPackage && data.length == 0) {
            firstPackage = false;
            return new byte[]{0, 0, 0, 0};
        }
        //if there is no more data return empty array
        if (!firstPackage && end == data.length) return new byte[]{};

        int chunkSize = 16;
        int chunkOffset = 2;

        if (start == 0) {
            int CHUNK_FIRST_SIZE = 14;
            chunkSize = data.length < CHUNK_FIRST_SIZE ? data.length : CHUNK_FIRST_SIZE;
            chunkOffset = 4;
            end = chunkSize;
        } else {
            end = start + chunkSize > data.length ? data.length : start + chunkSize;
            chunkSize = end - start;
        }

        byte[] payload = new byte[chunkSize + chunkOffset];

        byte[] offset = ByteBuffer.allocate(2).putShort((short) (start << 8)).array();
        System.arraycopy(offset, 0, payload, 0, offset.length);

        if (start == 0) {
            byte[] length = ByteBuffer.allocate(2).putShort((short) (data.length << 8)).array();
            System.arraycopy(length, 0, payload, 2, length.length);
        }

        byte[] chunk = Arrays.copyOfRange(data, start, end);
        System.arraycopy(chunk, 0, payload, chunkOffset, chunk.length);

        start += chunkSize;
        firstPackage = false;

        return payload;
    }

    // </editor-fold>
}
