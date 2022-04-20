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
        connectToDatabase();
    }

    // Sets connection to Database to autoCommit as false to allow for transactional behaviour
    public void connectToDatabase(){
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:SEC" + replicaNumber + ".db");
            if(c != null){
                DatabaseMetaData meta = c.getMetaData();

            }
            c.setAutoCommit(false);
            createTables();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Connected to database SEC" + replicaNumber + ".db successfully");
    }

    public void createTables(){
        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatement1 = null;
        PreparedStatement preparedStatement2 = null;
        PreparedStatement preparedStatement3 = null;
        PreparedStatement preparedStatement4 = null;
        PreparedStatement preparedStatement5 = null;
        String sql_drop_accounts;
        String sql_drop_transfers;
        String sql_drop_pendingTransfers;
        String sql_accounts;
        String sql_transfers;
        String sql_pendingTransfers;
        try{

            sql_drop_accounts = "DROP TABLE IF EXISTS ACCOUNTS";
            preparedStatement3 = c.prepareStatement(sql_drop_accounts);
            preparedStatement3.execute();
            preparedStatement3.close();

            sql_drop_transfers = "DROP TABLE IF EXISTS transfers";
            preparedStatement4 = c.prepareStatement(sql_drop_transfers);
            preparedStatement4.execute();
            preparedStatement4.close();

            sql_drop_pendingTransfers = "DROP TABLE IF EXISTS pending_transfers";
            preparedStatement5 = c.prepareStatement(sql_drop_pendingTransfers);
            preparedStatement5.execute();
            preparedStatement5.close();

            sql_accounts = "CREATE TABLE IF NOT EXISTS ACCOUNTS (\n" +
                           "account_id INTEGER PRIMARY KEY,\n" +
                           "public_key TEXT NOT NULL,\n" +
                           "balance INTEGER NOT NULL,\n" + "" +
                           "ts INTEGER NOT NULL);";
            preparedStatement = c.prepareStatement(sql_accounts);
            preparedStatement.execute();
            preparedStatement.close();

            sql_transfers = "CREATE TABLE IF NOT EXISTS transfers\n" +
                            "(transfer_id INTEGER PRIMARY KEY AUTOINCREMENT ,\n" +
                            "source_key           TEXT    NOT NULL,\n" +
                            "receiver_key           TEXT    NOT NULL,\n" +
                            "amount            INT     NOT NULL,\n" +
                            "timestamp         TEXT NOT NULL,\n" +
                            "sender_signature TEXT NOT NULL,\n" +
                            "receiver_signature TEXT NOT NULL,\n" +
                            "wts INTEGER  NOT NULL);";
            preparedStatement1 = c.prepareStatement(sql_transfers);
            preparedStatement1.execute();
            preparedStatement1.close();

            sql_pendingTransfers = "CREATE TABLE IF NOT EXISTS pending_transfers\n" +
                                   "(transfer_id INTEGER PRIMARY KEY AUTOINCREMENT ,\n" +
                                   "source_key           TEXT    NOT NULL,\n" +
                                   "receiver_key           TEXT    NOT NULL,\n" +
                                   "amount            INT     NOT NULL,\n" +
                                   "timestamp         TEXT NOT NULL,\n" +
                                   "sender_signature TEXT NOT NULL,\n" +
                                   "wts INTEGER NOT NULL);";
            preparedStatement2 = c.prepareStatement(sql_pendingTransfers);
            preparedStatement2.execute();
            preparedStatement2.close();

            c.commit();

        }
        catch (SQLException e){
            try{
                System.out.println("Rollback was Done!");
                c.rollback();
            }
            catch (SQLException e2){
                System.err.println( e2.getClass().getName() + ": " + e2.getMessage() );
                System.exit(0);
            }
        }
        finally {
            try {
                if (preparedStatement != null){
                    preparedStatement.close();
                }
                if (preparedStatement1 != null){
                    preparedStatement1.close();
                }
                if (preparedStatement2 != null){
                    preparedStatement2.close();
                }
                if(preparedStatement3 != null){
                    preparedStatement3.close();
                }
                if(preparedStatement4 != null){
                    preparedStatement4.close();
                }
                if(preparedStatement5 != null){
                    preparedStatement5.close();
                }
            }
            catch (SQLException e){
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }

        }
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
                System.out.println("Rollback was done");
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

    public String sendAmount(String sender, String receiver, int amount, long wts, String signature){
        if (sender.equals(receiver)) {
            return "Can't send money to itself!";
        }
        if (amount <= 0) {
            return "The amount of money sent needs to be higher than zero!";
        }
        Date timestamp = new Date(System.currentTimeMillis());
        String timestamp_string = AcceptedTransfer.DateToString(timestamp);
        Account senderAccount = getAccount(sender);
        if (senderAccount == null){
            return "The sender account doesn't exist!";
        }
        long ts = senderAccount.getTs();
        if(wts < ts){
            return "Wrong ts!";
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
            String sql_insert = "INSERT INTO pending_transfers (source_key, receiver_key, amount, timestamp, sender_signature, wts) VALUES (?, ?, ?, ?, ?, ?);";
            pstmt = c.prepareStatement(sql_insert);
            pstmt.setString(1, sender);
            pstmt.setString(2, receiver);
            pstmt.setInt(3, amount);
            pstmt.setString(4,timestamp_string);
            pstmt.setString(5, signature);
            pstmt.setLong(6, wts);
            pstmt.executeUpdate();
            pstmt.close();

            String sql_update = "UPDATE accounts set balance = ? where public_key = ? ;";
            pstmt = c.prepareStatement(sql_update);
            pstmt.setInt(1, senderAccount.balance());
            pstmt.setString(2, sender);
            pstmt.executeUpdate();
            pstmt.close();

            String sql_update_ts = "UPDATE accounts set ts = ? where public_key = ? ;";
            pstmt = c.prepareStatement(sql_update_ts);
            pstmt.setLong(1, wts);
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
        AcceptedTransfer acceptedTransfer = null;
        PendingTransfer pendingTransfer = null;
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;
        PreparedStatement pstmt3 = null;
        String sql_accounts;
        String sql_pendingTransfers;
        String sql_transfers;
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        ResultSet rs3 = null;
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
                String timestamp = rs2.getString("timestamp");
                String senderSignature = rs2.getString("sender_signature");
                String receiverSignature = rs2.getString("receiver_signature");
                acceptedTransfer = new AcceptedTransfer(source_key, receiver_key, amount, timestamp, senderSignature, receiverSignature );
                account.addTransfer(acceptedTransfer);
            }

            sql_pendingTransfers = "SELECT * FROM pending_transfers WHERE source_key = ? OR receiver_key = ? ;";
            pstmt3 = c.prepareStatement(sql_pendingTransfers);
            pstmt3.setString(1, publicKey);
            pstmt3.setString(2, publicKey);
            rs3 = pstmt3.executeQuery();
            while ( rs3.next() ){
                String source_key = rs3.getString("source_key");
                String receiver_key = rs3.getString("receiver_key");
                int amount = rs3.getInt("amount");
                String timestamp = rs3.getString("timestamp");
                String senderSignature = rs3.getString("sender_signature");
                pendingTransfer = new PendingTransfer(source_key, receiver_key, amount, timestamp, senderSignature);
                account.addPendingTransfer(pendingTransfer);
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
                if (pstmt3 != null){
                    pstmt3.close();
                }
                if (rs1 != null){
                    rs1.close();
                }
                if (rs2 != null){
                    rs2.close();
                }
                if (rs3 != null){
                    rs3.close();
                }
            }
            catch (SQLException e){
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }
        }
        return account;
    }

    public String receiveAmount(String sender, String receiver, long wts, String signature) {
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
        long ts = receiverAccount.getTs();
        if(wts < ts){
            return "Wrong ts!";
        }
        List<PendingTransfer> transfers =  receiverAccount.getPendingTransfers()
                .stream()
                .filter(d -> d.sender().equals(sender) && d.receiver().equals(receiver))
                .collect(Collectors.toList());
        transfers.sort(Comparator.comparing(d -> d.getTimestamp()));
        Collections.sort(transfers, Collections.reverseOrder());
        if (transfers.size() <= 0){
            return "No transfers to  receive!";
        }
        PendingTransfer pendingTransfer = transfers.get(0);
        receiverAccount.changingBalance(pendingTransfer.amount());
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;
        PreparedStatement pstmt3 = null;
        PreparedStatement pstmt4 = null;

        String sql_balance;
        String sql_delete_transfers;
        String sql_insert_transfers;
        String sql_update_ts;

        try{

            sql_balance = "UPDATE accounts SET balance = ? WHERE public_key = ? ;";
            pstmt1 = c.prepareStatement(sql_balance);
            pstmt1.setInt(1, receiverAccount.balance());
            pstmt1.setString(2, receiver);
            pstmt1.executeUpdate();
            pstmt1.close();

            sql_delete_transfers = "DELETE FROM pending_transfers WHERE source_key = ? AND receiver_key = ? AND timestamp = ? ;";
            pstmt2 = c.prepareStatement(sql_delete_transfers);
            pstmt2.setString(1, sender);
            pstmt2.setString(2, receiver);
            pstmt2.setString(3, AcceptedTransfer.DateToString(pendingTransfer.getTimestamp()));
            pstmt2.executeUpdate();
            pstmt2.close();

            sql_insert_transfers = "INSERT INTO transfers (source_key, receiver_key, amount, timestamp, sender_signature, receiver_signature, wts) VALUES (?,?,?,?,?,?,?);";
            pstmt3 = c.prepareStatement(sql_insert_transfers);
            pstmt3.setString(1, sender);
            pstmt3.setString(2, receiver);
            pstmt3.setInt(3, pendingTransfer.amount());
            pstmt3.setString(4, AcceptedTransfer.DateToString(pendingTransfer.getTimestamp()));
            pstmt3.setString(5, pendingTransfer.getSignature());
            pstmt3.setString(6, signature);
            pstmt3.setLong(7, wts);

            pstmt3.executeUpdate();
            pstmt3.close();

            sql_update_ts = "UPDATE accounts set ts = ? where public_key = ? ;";
            pstmt4 = c.prepareStatement(sql_update_ts);
            pstmt4.setLong(1, wts);
            pstmt4.setString(2, receiver);
            pstmt4.executeUpdate();
            pstmt4.close();

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
                if (pstmt3 != null){
                    pstmt3.close();
                }
                if (pstmt4 != null){
                    pstmt4.close();
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
