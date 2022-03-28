package server.data;

import communication.messages.PendingTransfer;
import communication.messages.Transfer;

import javax.swing.plaf.nimbus.State;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
        getTransfersFromDatabase();
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

                account = new Account(publicKey, balance);
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

    public void getTransfersFromDatabase(){
        try {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM transfers;" );

            while ( rs.next() ) {
                int transfer_id = rs.getInt("transfer_id");
                String source_key = rs.getString("source_key");
                String receiver_key = rs.getString("receiver_key");
                int amount = rs.getInt("amount");
                String timestamp = rs.getString("timestamp");
                int pending = rs.getInt("pending");
                Transfer transfer = new Transfer(source_key, receiver_key, amount, timestamp);
                Account source = accounts.get(source_key);
                Account receiver = accounts.get(receiver_key);
                assert source != null;
                assert receiver != null;
                if(pending == 0){
                    source.addTransfer(transfer);
                    receiver.addTransfer(transfer);
                }
                else{
                    PendingTransfer pendingTransfer = new PendingTransfer(transfer);
                    source.addPendingTransfer(transfer);
                    receiver.addPendingTransfer(transfer);
                }
            }
            rs.close();
            stmt.close();

        } catch ( Exception e ){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    public List<Transfer> auditAccount(String publicKey){
        getAccountsFromDatabase();
        getTransfersFromDatabase();
        Account account = accounts.get(publicKey);
        if (account == null) {
            return new ArrayList<Transfer>();
        }
        else {
            return account.getTransfers();
        }
    }

    public List<PendingTransfer> checkAccountTransfers(String publicKey){
        getAccountsFromDatabase();
        getTransfersFromDatabase();
        Account account = accounts.get(publicKey);
        if (account == null) {
            return new ArrayList<PendingTransfer>();
        }
        else {
            return account.getPendingTransfers();
        }
    }

    public int checkAccountBalance(String publicKey){
        getAccountsFromDatabase();
        Account account = accounts.get(publicKey);
        if (account == null) {
            return 0;
        }
        else {
            return account.balance();
        }
    }

    public void sendAmount(String sender, String receiver, int amount){
        Date timestamp = new Date(System.currentTimeMillis());
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        String timestamp_string = formatter.format(timestamp);
        var transfer = new Transfer(sender,receiver,amount, timestamp_string);
        var senderAccount = accounts.get(sender);
        assert senderAccount != null;
        int sender_balance = senderAccount.balance();
        assert sender_balance - amount > 0;
        senderAccount.changingBalance(-amount);
        var receiverAccount = accounts.get(receiver);
        assert receiverAccount != null;
        senderAccount.addPendingTransfer(transfer);
        receiverAccount.addPendingTransfer(transfer);
        try{

            Statement stmt = c.createStatement();
            String sql = "INSERT INTO transfers (source_key, receiver_key, amount, pending, timestamp) " +
                    "VALUES (\"" +  sender + "\",\"" + receiver + "\"," + amount +"," + 1 + ",\"" + timestamp_string +  "\");";
            stmt.executeUpdate(sql);
            stmt.close();
            stmt = c.createStatement();
            sql = "UPDATE accounts set balance = " + senderAccount.balance() + " where public_key = \"" + sender + "\";";
            stmt.executeUpdate(sql);
            stmt.close();

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    public void receiveAmount(String sender, String receiver) {


    }

    /*

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
