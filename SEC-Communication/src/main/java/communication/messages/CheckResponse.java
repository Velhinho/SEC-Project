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

    @Override
    public String toString() {
        String beginning = "{" + "balance=" + balance + ",";
        ArrayList<PendingTransfer> transfersNotReceived = transfers;
        transfersNotReceived.removeIf(pendingTransfer -> pendingTransfer.getReceived() == 1);
        StringBuilder transfers = new StringBuilder(beginning + "transfers=[");
        for(int i = 0; i < transfersNotReceived.size(); i++){
            transfers.append(transfersNotReceived.get(i).toString());
            if (i != transfersNotReceived.size() - 1){
                transfers.append(",");
            }
        }
        transfers.append("]}");
        return transfers.toString();
    }
}
