package communication.channel;

public class ChannelException extends Exception {
    private final Exception exception;

    public ChannelException(Exception exception) {
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
}
