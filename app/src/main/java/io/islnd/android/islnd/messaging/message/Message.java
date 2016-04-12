package io.islnd.android.islnd.messaging.message;

import com.google.protobuf.InvalidProtocolBufferException;

import io.islnd.android.islnd.messaging.ProtoSerializable;
import io.islnd.android.islnd.messaging.proto.IslandProto;

public class Message implements Comparable<Message>, ProtoSerializable {

    private final String mailbox;
    private final int type;
    private final String blob;

    public Message(String mailbox, int type, String blob) {
        this.mailbox = mailbox;
        this.type = type;
        this.blob = blob;
    }

    public String getMailbox() {
        return mailbox;
    }

    public int getType() {
        return type;
    }

    public String getBlob() {
        return blob;
    }

    @Override
    public int compareTo(Message another) {
        return type - another.type;
    }

    @Override
    public byte[] toByteArray() {
        return IslandProto.Message.newBuilder()
                .setMailbox(this.mailbox)
                .setType(this.type)
                .setBlob(this.blob)
                .build()
                .toByteArray();
    }

    public static Message fromProto(byte[] bytes) {
        IslandProto.Message message = null;
        try {
            message = IslandProto.Message.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        return new Message(
                message.getMailbox(),
                message.getType(),
                message.getBlob()
        );
    }
}
