package io.islnd.android.islnd.messaging.crypto;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

import io.islnd.android.islnd.messaging.ProtoSerializable;
import io.islnd.android.islnd.messaging.message.Message;

public class EncryptedMessage extends AsymmetricEncryptedData {

    private final String mailbox;

    public EncryptedMessage(Message message, Key recipientPublicKey, PrivateKey authorPrivateKey) {
        super(message, recipientPublicKey, authorPrivateKey);
        this.mailbox = message.getMailbox();
    }

    public EncryptedMessage(String mailbox, String blob) {
        super(blob);
        this.mailbox = mailbox;
        this.blob = blob;
    }

    public String getMailbox() {
        return mailbox;
    }

    @Override
    public Message decryptAndVerify(Key privateKey, PublicKey authorPublicKey) throws InvalidSignatureException {
        SignedObject signedObject = SignedObject.fromProto(
                ObjectEncrypter.decryptAsymmetric(
                        this.blob,
                        privateKey));
        if (!CryptoUtil.verifySignedObject(signedObject, authorPublicKey)) {
            throw new InvalidSignatureException();
        }

        return Message.fromProto(signedObject.getObject());
    }
}
