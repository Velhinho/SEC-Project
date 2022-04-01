package server.data;

import communication.messages.PendingTransfer;
import communication.messages.Transfer;

import java.io.IOException;
import java.sql.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


public class ServerDataControllerTransactions {
    private Connection c = null;

    public ServerDataControllerTransactions(){
        connectToDatabase();
    }

    // Sets connection to Database to autoCommit as false to allow for transactional behaviour
    public void connectToDatabase(){
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:SEC.db");
            c.setAutoCommit(false);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Connected to database successfully");
    }


    public void openAccount(String publicKey){
        Statement stmnt = null;
        try {
            stmnt = c.createStatement();
            String sql = "INSERT INTO accounts (public_key, balance) " +
                    "VALUES (\"" +  publicKey + "\", 10);";
            stmnt.executeUpdate(sql);
            c.commit();
        }catch (SQLException e){
            try{
                c.rollback();
            }
            catch (SQLException e2){
                System.err.println( e2.getClass().getName() + ": " + e2.getMessage() );
                System.exit(0);
            }
        }
        finally {
            try {
                if (stmnt != null){
                    stmnt.close();
                }
            }
            catch (SQLException e){
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }

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
        Statement stmt = null;
        try{
            stmt = c.createStatement();
            String sql = "INSERT INTO transfers (source_key, receiver_key, amount, pending, timestamp) " +
                    "VALUES (\"" +  sender + "\",\"" + receiver + "\"," + amount +"," + 1 + ",\"" + timestamp_string +  "\");";
            stmt.executeUpdate(sql);
            stmt.close();
            stmt = c.createStatement();
            sql = "UPDATE accounts set balance = " + senderAccount.balance() + " where public_key = \"" + sender + "\";";
            stmt.executeUpdate(sql);
            c.commit();
        } catch (SQLException e){
            try{
                c.rollback();
            }
            catch (SQLException e2){
                System.err.println( e2.getClass().getName() + ": " + e2.getMessage() );
                System.exit(0);
            }
        } finally {
            try {
                if (stmt != null){
                    stmt.close();
                }
            }
            catch (SQLException e){
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }
        }
        return "Transfer made with success.";
    }

    public Account getAccount(String publicKey){
        Account account = null;
        Transfer transfer = null;
        Statement stmt1 = null;
        Statement stmt2 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        try {
            stmt1 = c.createStatement();
            rs1 = stmt1.executeQuery("SELECT * FROM accounts WHERE public_key = \"" + publicKey + "\";");
            while (rs1.next()) {
                account = new Account(rs1.getString("public_key"), rs1.getInt("balance"));
            }
            stmt2 = c.createStatement();
            rs2 = stmt2.executeQuery("SELECT * FROM transfers WHERE source_key = \"" + publicKey + "\" OR receiver_key = \"" + publicKey + "\";");
            while ( rs2.next() ){
                int transfer_id = rs2.getInt("transfer_id");
                String source_key = rs2.getString("source_key");
                String receiver_key = rs2.getString("receiver_key");
                int amount = rs2.getInt("amount");
                int pending = rs2.getInt("pending");
                String timestamp = rs2.getString("timestamp");
                transfer = new Transfer(source_key, receiver_key, amount, timestamp);
                if(pending == 0){
                    account.addTransfer(transfer);
                }
                else{
                    account.addPendingTransfer(transfer);
                }
            }
            c.commit();
        } catch (SQLException e){
            try{
                c.rollback();
            }
            catch (SQLException e2){
                System.err.println( e2.getClass().getName() + ": " + e2.getMessage() );
                System.exit(0);
            }
        }
        finally {
            try {
                if (stmt1 != null){
                    stmt1.close();
                }
                if (stmt2 != null){
                    stmt2.close();
                }
                if (rs1 != null){
                    rs1.close();
                }
                if (rs2 != null){
                    rs2.close();
                }
            }
            catch (SQLException e){
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }
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
        Statement stmt1 = null;
        Statement stmt2 = null;
        try{
            stmt1 = c.createStatement();
            String sql = "UPDATE accounts set balance = " + receiverAccount.balance() + " where public_key = \"" + receiver + "\";";
            stmt1.executeUpdate(sql);
            stmt2 = c.createStatement();
            sql = "UPDATE transfers set pending = " + 0 +
                    " where source_key = \"" + sender +
                    "\" and receiver_key = \"" + receiver +
                    "\" and timestamp = \"" + Transfer.DateToString(pendingTransfer.transfer().getTimestamp()) + "\";";
            stmt2.executeUpdate(sql);
            c.commit();
        }catch (SQLException e){
            try{
                c.rollback();
            }
            catch (SQLException e2){
                System.err.println( e2.getClass().getName() + ": " + e2.getMessage() );
                System.exit(0);
            }
        }
        finally {
            try {
                if (stmt1 != null){
                    stmt1.close();
                }
                if (stmt2 != null){
                    stmt2.close();
                }
            }
            catch (SQLException e){
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }
        }
        return "Transfer received with success!";
    }

}
