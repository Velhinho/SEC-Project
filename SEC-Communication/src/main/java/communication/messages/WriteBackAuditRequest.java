package communication.messages;

import com.google.gson.JsonObject;

import java.util.ArrayList;

public class WriteBackAuditRequest {

    private final ArrayList<JsonObject> receiveAmountRequests;
    private final String key;


    public WriteBackAuditRequest(ArrayList<JsonObject> receiveAmountRequests, String key){
        this.receiveAmountRequests = receiveAmountRequests;
        this.key = key;
    }

    public ArrayList<JsonObject> getReceiveAmountRequests() {
        return receiveAmountRequests;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "WriteBackAuditRequest{" +
               "receiveAmountRequests=" + receiveAmountRequests +
               ", key='" + key + '\'' +
               '}';
    }
}
