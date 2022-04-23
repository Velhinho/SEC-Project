package communication.messages;

import com.google.gson.JsonObject;

import java.util.ArrayList;

public class WriteBackCheckRequest {
    private final ArrayList<JsonObject> sendAmountRequests;
    private final String key;


    public WriteBackCheckRequest(ArrayList<JsonObject> sendAmountRequests, String key){
        this.sendAmountRequests = sendAmountRequests;
        this.key = key;
    }

    public ArrayList<JsonObject> getSendAmountRequests() {
        return sendAmountRequests;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "WriteBackCheckRequest{" +
               "sendAmountRequests=" + sendAmountRequests +
               ", key='" + key + '\'' +
               '}';
    }
}
