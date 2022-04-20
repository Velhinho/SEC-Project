package communication.messages;

import java.util.ArrayList;

public class CheckResponse {
    private final int balance;
    private final ArrayList<PendingTransfer> transfers;
    private final String type;

    public CheckResponse(int balance, ArrayList<PendingTransfer> transfers, String type){
        this.balance = balance;
        this.transfers = transfers;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public ArrayList<PendingTransfer> getTransfers() {
        return transfers;
    }

    public int getBalance() {
        return balance;
    }
}
