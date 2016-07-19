package jp.ac.titech.itpro.maaki.bttrax;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;

import java.io.Closeable;
import java.io.IOException;

public class Messenger implements Parcelable {
    private final static String FIELD_SEQ = "seq";
    private final static String FIELD_TIME = "time";
    private final static String FIELD_CONTENT = "content";
    private final static String FIELD_SENDER = "sender";

    public int seq;
    public long time;
    public String content;
    public String sender;

    public Messenger(int seq, long time, String content, String sender) {
        this.seq = seq;
        this.time = time;
        this.content = content;
        this.sender = sender;
    }

    @Override
    public String toString() {
        return content;
    }

    private Messenger(Parcel in) {
        seq = in.readInt();
        time = in.readLong();
        content = in.readString();
        sender = in.readString();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(seq);
        dest.writeLong(time);
        dest.writeString(content);
        dest.writeString(sender);
    }

    public static final Parcelable.Creator<Messenger> CREATOR =
            new Parcelable.Creator<Messenger>() {

                @Override
                public Messenger createFromParcel(Parcel source) {
                    return new Messenger(source);
                }

                @Override
                public Messenger[] newArray(int size) {
                    return new Messenger[size];
                }
            };


    public static class Reader implements Closeable {
        private final static String TAG = "ChagMessage.Reader";
        private final JsonReader reader;

        public Reader(JsonReader reader) {
            if (reader == null)
                throw new NullPointerException("reader is null");
            this.reader = reader;
        }

        @Override
        public void close() throws IOException {
            reader.close();
        }

        public boolean hasNext() throws IOException {
            return reader.hasNext();
        }

        public void beginArray() throws IOException {
            reader.beginArray();
        }

        public void endArray() throws IOException {
            reader.endArray();
        }

        public Messenger read() throws IOException {
            int seq = -1;
            long time = -1;
            String content = null;
            String sender = null;
            reader.beginObject();
            while (reader.hasNext()) {
                switch (reader.nextName()) {
                    case FIELD_SEQ:
                        seq = reader.nextInt();
                        break;
                    case FIELD_TIME:
                        time = reader.nextLong();
                        break;
                    case FIELD_CONTENT:
                        if (reader.peek() == JsonToken.NULL) {
                            reader.skipValue();
                            content = null;
                        }
                        else
                            content = reader.nextString();
                        break;
                    case FIELD_SENDER:
                        if (reader.peek() == JsonToken.NULL) {
                            reader.skipValue();
                            sender = null;
                        }
                        else
                            sender = reader.nextString();
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }
            reader.endObject();
            return new Messenger(seq, time, content, sender);
        }
    }

    public static class Writer implements Closeable {
        private final static String TAG = "ChatMessage.Writer";
        private final JsonWriter writer;

        public Writer(JsonWriter writer) {
            if (writer == null)
                throw new NullPointerException("writer is null");
            this.writer = writer;
        }

        @Override
        public void close() throws IOException {
            writer.close();
        }

        public void flush() throws IOException {
            writer.flush();
        }

        public void beginArray() throws IOException {
            writer.beginArray();
        }

        public void endArray() throws IOException {
            writer.endArray();
        }

        public void write(Messenger message) throws IOException {
            writer.beginObject();
            writer.name(FIELD_SEQ).value(message.seq);
            writer.name(FIELD_TIME).value(message.time);
            writer.name(FIELD_CONTENT);
            if (message.content == null)
                writer.nullValue();
            else
                writer.value(message.content);
            writer.name(FIELD_SENDER);
            if (message.sender == null)
                writer.nullValue();
            else
                writer.value(message.sender);
            writer.endObject();
        }
    }

}
