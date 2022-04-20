package communication.messages;

public class TimeStampResponse {
    private final long ts;
    private final String type;

    public TimeStampResponse(String type, long ts) {
        this.ts = ts;
        this.type = type;
    }

    public long getTs() {
        return ts;
    }
}
