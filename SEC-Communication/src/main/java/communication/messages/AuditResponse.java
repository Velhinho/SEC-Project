package communication.messages;

import java.util.ArrayList;

public class AuditResponse {
    private final ArrayList<AcceptedTransfer> transfers;
    private final String type;

    public AuditResponse(ArrayList<AcceptedTransfer> transfers, String type){
        this.transfers = transfers;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public ArrayList<AcceptedTransfer> getTransfers() {
        return transfers;
    }

    @Override
    public String toString() {
        return "AuditResponse{" +
               "transfers=" + transfers +
               ", type='" + type + '\'' +
               '}';
    }
}
