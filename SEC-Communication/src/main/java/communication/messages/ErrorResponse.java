package communication.messages;

public class ErrorResponse {
    private final String type;
    private final String errorMessage;

    public ErrorResponse(String type, String errorMessage){
        this.type = type;
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
               "type='" + type + '\'' +
               ", errorMessage='" + errorMessage + '\'' +
               '}';
    }
}
