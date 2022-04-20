package communication.messages;

public class TimeStampRequest {
    private final String key;

    public TimeStampRequest(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
