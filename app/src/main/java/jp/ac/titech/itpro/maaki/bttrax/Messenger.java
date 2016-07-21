package jp.ac.titech.itpro.maaki.bttrax;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;

import java.io.Closeable;
import java.io.IOException;

public class Messenger implements Parcelable {
    private final static String FIELD_CONTENT = "content";

    public String content;

    public Messenger(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return content;
    }

    private Messenger(Parcel in) {
        content = in.readString();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(content);
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
            String content = null;
            String sender = null;
            reader.beginObject();
            while (reader.hasNext()) {
                switch (reader.nextName()) {
                    case FIELD_CONTENT:
                        if (reader.peek() == JsonToken.NULL) {
                            reader.skipValue();
                            content = null;
                        }
                        else
                            content = reader.nextString();
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }
            reader.endObject();
            return new Messenger(content);
        }
    }

    public static class Writer implements Closeable {
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
            writer.name(FIELD_CONTENT);
            if (message.content == null)
                writer.nullValue();
            else
                writer.value(message.content);
            writer.endObject();
        }
    }

}
