package org.island.messaging.crypto;

import org.island.messaging.Decoder;
import org.island.messaging.Encoder;
import org.island.messaging.ProtoSerializable;

import java.security.Key;
import java.util.Arrays;

public class ObjectSigner {

    private static final Encoder encoder = new Encoder();
    private static final Decoder decoder = new Decoder();

    public static SignedObject sign(ProtoSerializable object, Key key) {
        byte[] digest = CryptoUtil.getDigest(object);
        byte[] encryptedDigest = CryptoUtil.encryptAsymmetric(digest, key);
        return new SignedObject(encoder.encodeToString(object.toByteArray()),
                encoder.encodeToString(encryptedDigest));
    }

    public static boolean verify(SignedObject signedObject, Key key) {
        return verify(signedObject.getObject(), signedObject.getSignature(), key);
    }

    public static boolean verify(String object, String signature, Key key) {
        try {
            byte[] digest = CryptoUtil.getDigest(object);
            byte[] decryptedDigest = CryptoUtil.decryptAsymmetric(decoder.decode(signature), key);
            return Arrays.equals(digest, decryptedDigest);
        } catch (DecryptionErrorException e) {
            //--If we can't decrypt, the data may have been modified, or the key may be incorrect
            return false;
        }
    }
}