package communication.crypto;

public class CryptoException extends Exception {
    private final Exception exception;

    public Exception getException() {
        return exception;
    }

    public CryptoException(Exception exception) {
        this.exception = exception;
    }
}
