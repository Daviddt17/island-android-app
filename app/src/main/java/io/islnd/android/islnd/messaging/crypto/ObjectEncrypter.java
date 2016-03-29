package io.islnd.android.islnd.messaging.crypto;


import java.security.Key;
import java.util.Arrays;

import io.islnd.android.islnd.messaging.Decoder;
import io.islnd.android.islnd.messaging.Encoder;
import io.islnd.android.islnd.messaging.ProtoSerializable;

public class ObjectEncrypter {

    private static final int CHUNK_SIZE = 190;
    private static final String DELIMITER = "_";

    private static Encoder encoder = new Encoder();
    private static Decoder decoder = new Decoder();

    public static String encryptSymmetric(ProtoSerializable o, Key key) {
        byte[] bytes = o.toByteArray();
        byte[] encryptedBytes = CryptoUtil.encryptSymmetric(bytes, key);
        return encoder.encodeToString(encryptedBytes);
    }

    public static String encryptAsymmetric(ProtoSerializable object, Key key) {
        byte[] bytes = object.toByteArray();
        byte[][] chunks = divideIntoChunks(bytes);
        byte[][] encryptedChunks = new byte[chunks.length][];
        for (int i = 0; i < chunks.length; i++) {
            encryptedChunks[i] = CryptoUtil.encryptAsymmetricWithOAEP(chunks[i], key);
        }

        return combineChunksIntoString(encryptedChunks);
    }

    public static byte[] decryptSymmetric(String string, Key key) {
        byte[] encryptedBytes = decoder.decode(string);
        return CryptoUtil.decryptSymmetric(encryptedBytes, key);
    }

    public static byte[] decryptAsymmetric(String string, Key key) {
        byte[][] encryptedChunks = getChunksFromString(string);

        byte[][] chunks = new byte[encryptedChunks.length][];
        for (int i = 0; i < chunks.length; i++) {
            chunks[i] = CryptoUtil.decryptAsymmetricWithOAEP(encryptedChunks[i], key);
        }

        return combineChunksIntoObject(chunks);
    }

    private static byte[] combineChunksIntoObject(byte[][] chunks) {
        int totalLength = 0;
        for (int i = 0; i < chunks.length; i++) {
            totalLength += chunks[i].length;
        }

        byte[] object = new byte[totalLength];
        for (int i = 0; i < chunks.length; i++) {
            for (int j = 0; j < chunks[i].length; j++) {
                object[i * CHUNK_SIZE + j] = chunks[i][j];
            }
        }

        return object;
    }

    private static byte[][] getChunksFromString(String string) {
        String[] tokens = string.split(DELIMITER);

        byte[][] chunks = new byte[tokens.length][];
        for (int i = 0; i < chunks.length; i++) {
            chunks[i] = decoder.decode(tokens[i]);
        }

        return chunks;
    }

    private static byte[][] divideIntoChunks(byte[] bytes) {
        int numberOfChunks = (int) Math.ceil((double)bytes.length / CHUNK_SIZE);
        byte[][] chunks = new byte[numberOfChunks][];
        for (int i = 0; i < numberOfChunks; i++) {
            int firstIndex = CHUNK_SIZE * i;
            int lastIndex = Math.min(bytes.length, CHUNK_SIZE * (i + 1));
            chunks[i] = Arrays.copyOfRange(bytes, firstIndex, lastIndex);
        }

        return chunks;
    }

    private static String combineChunksIntoString(byte[][] encryptedChunks) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < encryptedChunks.length; i++) {
            sb.append(encoder.encodeToString(encryptedChunks[i]));
            if (i != encryptedChunks.length - 1) {
                sb.append(DELIMITER);
            }
        }

        return sb.toString();
    }
}
