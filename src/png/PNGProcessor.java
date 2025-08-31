package png;

import model.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.CRC32;

public class PNGProcessor {
    private static final byte[] PNG_SIGNATURE = {
            (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    public List<PNGChunk> readPNGChunks(File file) throws IOException {
        List<PNGChunk> chunks = new ArrayList<>();

        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            // Check PNG signature
            byte[] signature = new byte[8];
            dis.readFully(signature);
            if (!Arrays.equals(signature, PNG_SIGNATURE)) {
                throw new IOException("Not a valid PNG file");
            }

            // Read chunks
            while (dis.available() > 0) {
                int length = dis.readInt();
                byte[] typeBytes = new byte[4];
                dis.readFully(typeBytes);
                String type = new String(typeBytes, StandardCharsets.US_ASCII);

                byte[] data = new byte[length];
                if (length > 0) {
                    dis.readFully(data);
                }

                int crc = dis.readInt();

                chunks.add(new PNGChunk(length, type, data, crc));

                if ("IEND".equals(type)) {
                    break;
                }
            }
        }

        return chunks;
    }

    public void writePNGChunks(File file, List<PNGChunk> chunks) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
            // Write PNG signature
            dos.write(PNG_SIGNATURE);

            CRC32 crc32 = new CRC32();
            for (PNGChunk chunk : chunks) {
                // Write length
                dos.writeInt(chunk.getLength());

                // Write type
                dos.writeBytes(chunk.getType());

                // Write data
                if (chunk.getLength() > 0) {
                    dos.write(chunk.getData());
                }

                // Calculate and write CRC
                crc32.reset();
                crc32.update(chunk.getType().getBytes(StandardCharsets.US_ASCII));
                if (chunk.getData() != null) {
                    crc32.update(chunk.getData());
                }
                dos.writeInt((int)crc32.getValue());
            }
        }
    }

    public List<TextChunk> extractTextChunks(List<PNGChunk> pngChunks) {
        List<TextChunk> textChunks = new ArrayList<>();

        for (PNGChunk chunk : pngChunks) {
            if ("tEXt".equals(chunk.getType())) {
                TextChunk textChunk = parseTextChunk(chunk.getData());
                textChunks.add(textChunk);
            }
        }

        return textChunks;
    }

    private TextChunk parseTextChunk(byte[] data) {
        int nullPos = -1;
        for (int i = 0; i < data.length; i++) {
            if (data[i] == 0) {
                nullPos = i;
                break;
            }
        }

        if (nullPos > 0 && nullPos < data.length - 1) {
            String keyword = new String(data, 0, nullPos);
            String text = new String(data, nullPos + 1, data.length - nullPos - 1);
            boolean isEncrypted = text.startsWith("RSA:");

            return new TextChunk(keyword, text, isEncrypted);
        }

        return new TextChunk("Comment", new String(data), false);
    }

    public PNGChunk createTextChunk(TextChunk textChunk) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(textChunk.getKeyword().getBytes(StandardCharsets.US_ASCII));
        baos.write(0);
        baos.write(textChunk.getText().getBytes(StandardCharsets.UTF_8));

        byte[] data = baos.toByteArray();
        return new PNGChunk(data.length, "tEXt", data, 0);
    }

    public List<PNGChunk> buildPNGWithTextChunks(List<PNGChunk> originalChunks,
                                                 List<TextChunk> textChunks) throws IOException {
        List<PNGChunk> newChunks = new ArrayList<>();

        // Copy non-text chunks (except IEND)
        for (PNGChunk chunk : originalChunks) {
            if (!"tEXt".equals(chunk.getType()) && !"IEND".equals(chunk.getType())) {
                newChunks.add(chunk);
            }
        }

        // Add text chunks
        for (TextChunk textChunk : textChunks) {
            newChunks.add(createTextChunk(textChunk));
        }

        // Add IEND
        for (PNGChunk chunk : originalChunks) {
            if ("IEND".equals(chunk.getType())) {
                newChunks.add(chunk);
                break;
            }
        }

        return newChunks;
    }
}