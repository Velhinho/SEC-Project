package communication.messages;

public class ReadValue {
    private final String value;
    private final long ts;
    private final long rid;

    public ReadValue(String value, long ts, long rid){
        this.value = value;
        this.ts = ts;
        this.rid = rid;
    }

    public String getValue() {
        return value;
    }

    public long getTs() {
        return ts;
    }

    public long getRid() {
        return rid;
    }

    @Override
    public String toString() {
        return "ReadValue{" +
               "value='" + value + '\'' +
               ", ts=" + ts +
               ", rid=" + rid +
               '}';
    }
}
