package server.data;

import communication.messages.PendingTransfer;
import communication.messages.Transfer;

import javax.swing.plaf.nimbus.State;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.sql.DriverManager;
import java.sql.Connection;


public class ServerDataDB {

    private Connection c = null;
    private ConcurrentHashMap<String, Account> accounts;

    public ServerDataDB(){
        connectToDatabase();
        getAccountsFromDatabase();
    }

    public void connectToDatabase(){
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:SEC.db");
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Connected to database successfully");
    }


    public void openAccount(String publicKey){
        try {
            Statement stmnt = c.createStatement();
            String sql = "INSERT INTO accounts (public_key, balance) " +
                    "VALUES (\"" +  publicKey + "\", 10);";
            stmnt.executeUpdate(sql);
            stmnt.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        getAccountsFromDatabase();
    }

    public void getAccountsFromDatabase() {
        accounts = new ConcurrentHashMap<>();
        Account account;

        try{
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM accounts;" );

            while ( rs.next() ) {
                int account_id = rs.getInt("account_id");
                String publicKey = rs.getString("public_key");
                int balance  = rs.getInt("balance");

                account = new Account(publicKey, account_id, balance);
                accounts.put(publicKey, account);
                System.out.println( "account_id = " + account_id);
                System.out.println( "public_key = " + publicKey );
                System.out.println( "balance = " + balance );
                System.out.println();
            }
            rs.close();
            stmt.close();
            System.out.println(accounts);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    /*

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
    }*/
}
