package server.data;

import java.util.HashMap;

public class ServerData {
    private HashMap<String, Account> accounts;

    public ServerData(){
        accounts = new HashMap<>();
    }

    public void openAccount(String publicKey){
        accounts.put(publicKey, new Account(publicKey));
    }

    public int getNumberOfAccounts(){
        return accounts.size();
    }
}
