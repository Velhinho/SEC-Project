package server.data;

import communication.messages.PendingTransfer;
import communication.messages.Transfer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/*
public class ServerData {
    private ConcurrentHashMap<String, Account> accounts;

    public ServerData(){
        accounts = new ConcurrentHashMap<>();
    }

    public void openAccount(String publicKey){
        accounts.put(publicKey, new Account(publicKey,));
    }

    public void sendAmount(String sender, String receiver, int amount){
        var transfer = new Transfer(sender,receiver,amount);
        var senderAccount = accounts.get(sender);
        var receiverAccount = accounts.get(receiver);
        senderAccount.addPendingTransfer(transfer);
        receiverAccount.addPendingTransfer(transfer);
    }

    public int getNumberOfAccounts(){
        return accounts.size();
    }

    public void receiveAmount(String sender, String receiver) {
        var senderAccount = accounts.get(sender);
        var receiverAccount = accounts.get(receiver);
        senderAccount.acceptPendingTransferAsSender(receiver);
        receiverAccount.acceptPendingTransferAsReceiver(sender);
    }

    public List<Transfer> auditAccount(String publicKey){
        Account account = accounts.get(publicKey);
        if (account == null) {
            return new ArrayList<Transfer>();
        }
        else {
            return account.getTransfers();
        }
    }

    public List<PendingTransfer> checkAccountTransfers(String publicKey){
        Account account = accounts.get(publicKey);
        if (account == null) {
            return new ArrayList<PendingTransfer>();
        }
        else {
            return account.getPendingTransfers();
        }
    }

    public int checkAccountBalance(String publicKey){
        Account account = accounts.get(publicKey);
        if (account == null) {
            return 0;
        }
        else {
            return account.balance();
        }
    }

    public ConcurrentHashMap<String, Account> getAccounts() {
        return accounts;
    }
}*/
