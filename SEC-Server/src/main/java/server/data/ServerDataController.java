package server.data;

import communication.messages.PendingTransfer;
import communication.messages.Transfer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class ServerDataController {
    private Connection c = null;

    public ServerDataController(){
        connectToDatabase();
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
    }

    public String sendAmount(String sender, String receiver, int amount){

        Date timestamp = new Date(System.currentTimeMillis());
        String timestamp_string = Transfer.DateToString(timestamp);
        Account senderAccount = getAccount(sender);
        if (senderAccount == null){
            return "The sender account doesn't exist!";
        }
        int sender_balance = senderAccount.balance();
        if (sender_balance - amount < 0){
            return "Not enough balance to send that amount!";
        }
        senderAccount.changingBalance(-amount);
        Account receiverAccount = getAccount(receiver);
        if (receiverAccount == null){
            return "The receiver account doesn't exist!";
        }
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
        return "Transfer made with success.";
    }

    public Account getAccount(String publicKey){
        Account account = null;
        Transfer transfer = null;
        try {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM accounts WHERE public_key = \"" + publicKey + "\";");
            while (rs.next()) {
                account = new Account(rs.getString("public_key"), rs.getInt("balance"));
            }

            stmt.close();
            stmt = c.createStatement();
            rs = stmt.executeQuery("SELECT * FROM transfers WHERE source_key = \"" + publicKey + "\" OR receiver_key = \"" + publicKey + "\";");
            while ( rs.next() ){
                int transfer_id = rs.getInt("transfer_id");
                String source_key = rs.getString("source_key");
                String receiver_key = rs.getString("receiver_key");
                int amount = rs.getInt("amount");
                int pending = rs.getInt("pending");
                String timestamp = rs.getString("timestamp");
                transfer = new Transfer(source_key, receiver_key, amount, timestamp);
                if(pending == 0){
                    account.addTransfer(transfer);
                }
                else{
                    account.addPendingTransfer(transfer);
                }
            }
        }
        catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return account;
    }

    public String receiveAmount(String sender, String receiver) {
        Account senderAccount = getAccount(sender);
        if (senderAccount == null){
            return "The sender account doesn't exist!";
        }
        Account receiverAccount = getAccount(receiver);
        if (receiverAccount == null){
            return "The receiver account doesn't exist!";
        }
        List<PendingTransfer> transfers =  receiverAccount.getPendingTransfers()
                .stream()
                .filter(d -> d.transfer().sender().equals(sender) && d.transfer().receiver().equals(receiver))
                .collect(Collectors.toList());
        transfers.sort(Comparator.comparing(d -> d.transfer().getTimestamp()));
        Collections.sort(transfers, Collections.reverseOrder());
        if (transfers.size() <= 0){
            return "No transfers to  receive!";
        }
        PendingTransfer pendingTransfer = transfers.get(0);
        receiverAccount.changingBalance(pendingTransfer.transfer().amount());
        try{
            Statement stmt = c.createStatement();
            String sql = "UPDATE accounts set balance = " + receiverAccount.balance() + " where public_key = \"" + receiver + "\";";
            stmt.executeUpdate(sql);
            stmt.close();
            stmt = c.createStatement();
            sql = "UPDATE transfers set pending = " + 0 +
                    " where source_key = \"" + sender +
                    "\" and receiver_key = \"" + receiver +
                    "\" and timestamp = \"" + Transfer.DateToString(pendingTransfer.transfer().getTimestamp()) + "\";";
            stmt.executeUpdate(sql);
            stmt.close();
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return "Transfer receive with success!";
    }

}
