package server.data;

import java.sql.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


public class ServerData {
    private Connection c = null;
    private final int replicaNumber;

    public ServerData(int replicaNumber){
        this.replicaNumber = replicaNumber;
        connectToDatabase(replicaNumber);
    }

    // Sets connection to Database to autoCommit as false to allow for transactional behaviour
    public void connectToDatabase(int replicaNumber){
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:SEC" + replicaNumber + ".db");
            c.setAutoCommit(false);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Connected to database successfully");
    }


    public void openAccount(String publicKey){
        PreparedStatement pstmt = null;
        try {
            pstmt = c.prepareStatement("INSERT INTO accounts (public_key, balance, ts) VALUES (?,?,?)");
            pstmt.setString(1, publicKey);
            pstmt.setInt(2, 10);
            pstmt.setInt(3, 0);
            pstmt.executeUpdate();
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
                if (pstmt != null){
                    pstmt.close();
                }
            }
            catch (SQLException e){
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }

        }
    }

    public String sendAmount(String sender, String receiver, int amount){
        if (sender.equals(receiver)) {
            return "Can't send money to itself!";
        }
        if (amount <= 0) {
            return "The amount of money sent needs to be higher than zero!";
        }
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
        PreparedStatement pstmt = null;
        try{
            String sql_insert = "INSERT INTO transfers (source_key, receiver_key, amount, pending, timestamp) VALUES (?, ?, ?, ?, ?);";
            pstmt = c.prepareStatement(sql_insert);
            pstmt.setString(1, sender);
            pstmt.setString(2, receiver);
            pstmt.setInt(3, amount);
            pstmt.setInt(4, 1);
            pstmt.setString(5,timestamp_string);
            pstmt.executeUpdate();
            pstmt.close();

            String sql_update = "UPDATE accounts set balance = ? where public_key = ? ;";
            pstmt = c.prepareStatement(sql_update);
            pstmt.setInt(1, senderAccount.balance());
            pstmt.setString(2, sender);
            pstmt.executeUpdate();
            pstmt.close();
            c.commit();

            String sql_updateTs = "UPDATE accounts set ts = ? where public_key = ? ;";
            pstmt= c.prepareStatement(sql_updateTs);
            pstmt.setInt(1, senderAccount.getTs() + 1);
            pstmt.setString(2, sender);
            pstmt.executeUpdate();
            pstmt.close();
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
                if (pstmt != null){
                    pstmt.close();
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
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;
        String sql_accounts;
        String sql_transfers;
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        try {
            sql_accounts = "SELECT * FROM accounts WHERE public_key = ? ;";
            pstmt1 = c.prepareStatement(sql_accounts);
            pstmt1.setString(1, publicKey);
            rs1 = pstmt1.executeQuery();
            while (rs1.next()) {
                account = new Account(rs1.getString("public_key"), rs1.getInt("balance"), rs1.getInt("ts"));
            }
            sql_transfers = "SELECT * FROM transfers WHERE source_key = ? OR receiver_key = ? ;";
            pstmt2 = c.prepareStatement(sql_transfers);
            pstmt2.setString(1, publicKey);
            pstmt2.setString(2, publicKey);
            rs2 = pstmt2.executeQuery();
            while ( rs2.next() ){
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
                if (pstmt1 != null){
                    pstmt1.close();
                }
                if (pstmt2 != null){
                    pstmt2.close();
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
        if (sender.equals(receiver)) {
            return "You can't accept a transfer from yourself to yourself!";
        }
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
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;
        PreparedStatement pstmt3 = null;
        String sql_balance;
        String sql_transfers;
        try{

            sql_balance = "UPDATE accounts SET balance = ? WHERE public_key = ? ;";
            pstmt1 = c.prepareStatement(sql_balance);
            pstmt1.setInt(1, receiverAccount.balance());
            pstmt1.setString(2, receiver);
            pstmt1.executeUpdate();

            System.out.println(receiverAccount.balance());
            System.out.println(receiver);

            sql_transfers = "UPDATE transfers SET pending = ? WHERE source_key = ? AND receiver_key = ? AND timestamp = ? ;";
            pstmt2 = c.prepareStatement(sql_transfers);
            pstmt2.setInt(1, 0);
            pstmt2.setString(2, sender);
            pstmt2.setString(3, receiver);
            pstmt2.setString(4, Transfer.DateToString(pendingTransfer.transfer().getTimestamp()));
            pstmt2.executeUpdate();

            System.out.println(sender);
            System.out.println(receiver);
            System.out.println(Transfer.DateToString(pendingTransfer.transfer().getTimestamp()));

            String sql_updateTs = "UPDATE accounts set ts = ? where public_key = ? ;";
            pstmt3= c.prepareStatement(sql_updateTs);
            pstmt3.setInt(1, receiverAccount.getTs() + 1);
            pstmt3.setString(2, receiver);
            pstmt3.executeUpdate();
            pstmt3.close();

            c.commit();
        }catch (SQLException e){
            try{
                System.out.println("Rollback was done!");
                System.out.println(e.getMessage());
                c.rollback();
            }
            catch (SQLException e2){
                System.err.println( e2.getClass().getName() + ": " + e2.getMessage() );
                System.exit(0);
            }
        }
        finally {
            try {
                if (pstmt1 != null){
                    pstmt1.close();
                }
                if (pstmt2 != null){
                    pstmt2.close();
                }
            }
            catch (SQLException e){
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }
        }
        return "Transfer received with success!";
    }

    public void saveOperation(String operation){
        PreparedStatement pstmt = null;
        String sql_operation = null;
        try {
            sql_operation = "INSERT INTO operations (signature) VALUES (?);";
            pstmt = c.prepareStatement(sql_operation);
            pstmt.setString(1, operation);
            pstmt.executeUpdate();
            c.commit();
        }
        catch (SQLException e){
            try{
                c.rollback();
            }
            catch (SQLException e2){
                System.err.println( e2.getClass().getName() + ": " + e2.getMessage() );
                System.exit(0);
            }
        }
    }

}
