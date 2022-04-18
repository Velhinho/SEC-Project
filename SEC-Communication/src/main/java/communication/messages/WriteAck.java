package communication.messages;

public class WriteAck {
    private final String content;
    private final long wts;

    public WriteAck(String content, long wts){
        this.content = content;
        this.wts = wts;
    }

    public String getContent() {
        return content;
    }

    public long getWts() {
        return wts;
    }

    @Override
    public String toString() {
        return "WriteAck{" +
               "content='" + content + '\'' +
               ", wts=" + wts +
               '}';
    }
}
