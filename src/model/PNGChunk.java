package model;

public class PNGChunk {
    private int length;
    private String type;
    private byte[] data;
    private int crc;

    public PNGChunk(int length, String type, byte[] data, int crc) {
        this.length = length;
        this.type = type;
        this.data = data;
        this.crc = crc;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getCrc() {
        return crc;
    }

    public void setCrc(int crc) {
        this.crc = crc;
    }
}