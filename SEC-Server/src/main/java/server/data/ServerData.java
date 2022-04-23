package server.data;

import communication.messages.AcceptedTransfer;
import communication.messages.PendingTransfer;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class ServerData {
    private Connection c = null;
    private final int replicaNumber;

    public ServerData(int replicaNumber, String reset){
        this.replicaNumber = replicaNumber;
        connectToDatabase(reset);
    }

    /**
     * Sets connection to the database
     * @param reset Decides if the database is reset when connected to
     */

    // Sets connection to Database to autoCommit as false to allow for transactional behaviour
    public void connectToDatabase(String reset){
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:SEC" + replicaNumber + ".db");
            if(c != null){
                DatabaseMetaData meta = c.getMetaData();

            }
            c.setAutoCommit(false);
            createTables(reset);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Connected to database SEC" + replicaNumber + ".db successfully");
    }

    /**
     * Creates the tables in the database
     * @param reset If reset = "yes" then tables are dropped before creating the database.
     */

    public void createTables(String reset){
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

            if(reset.equals("yes")) {

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

            }

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
                            "sender_signature TEXT NOT NULL,\n" +
                            "receiver_signature TEXT NOT NULL,\n" +
                            "rid INTEGER  NOT NULL,\n" +
                            "wts INTEGER  NOT NULL);";
            preparedStatement1 = c.prepareStatement(sql_transfers);
            preparedStatement1.execute();
            preparedStatement1.close();

            sql_pendingTransfers = "CREATE TABLE IF NOT EXISTS pending_transfers\n" +
                                   "(transfer_id INTEGER PRIMARY KEY AUTOINCREMENT ,\n" +
                                   "source_key           TEXT    NOT NULL,\n" +
                                   "receiver_key           TEXT    NOT NULL,\n" +
                                   "amount            INT     NOT NULL,\n" +
                                   "sender_signature TEXT NOT NULL,\n" +
                                   "rid INTEGER  NOT NULL,\n" +
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

    /**
     * Opens an account on the database
     * @param publicKey The publicKey of the account
     */

    public void openAccount(String publicKey){
        PreparedStatement pstmt = null;
        try {
            pstmt = c.prepareStatement("INSERT INTO accounts (public_key, balance, ts) VALUES (?,?,?)");
            pstmt.setString(1, publicKey);
            pstmt.setInt(2, 10);
            pstmt.setLong(3, 0);
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

    public String sendAmount(String sender, String receiver, int amount, long wts, String signature, long rid){
        Account senderAccount = getAccount(sender);
        if (senderAccount == null){
            return "The sender account doesn't exist!";
        }
        long ts = senderAccount.getTs();
        System.out.println("Transfers currently in database: " + getCurrentTransfersFromAccount(sender));
        System.out.println("wts: " + wts);
        if(getCurrentTransfersFromAccount(sender).contains(wts)){
            System.out.println("Transfer with wts=" + wts + " was rejected.");
            return "Unneeded Transfer";
        }
        if(wts <= ts && getCurrentTransfersFromAccount(sender).contains(wts)){
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
            String sql_insert = "INSERT INTO pending_transfers (source_key, receiver_key, amount, sender_signature, wts, rid) VALUES (?, ?, ?, ?, ?, ?);";
            pstmt = c.prepareStatement(sql_insert);
            pstmt.setString(1, sender);
            pstmt.setString(2, receiver);
            pstmt.setInt(3, amount);
            pstmt.setString(4, signature);
            pstmt.setLong(5, wts);
            pstmt.setLong(6, rid);
            pstmt.executeUpdate();
            pstmt.close();

            String sql_update = "UPDATE accounts set balance = ? where public_key = ? ;";
            pstmt = c.prepareStatement(sql_update);
            pstmt.setInt(1, senderAccount.balance());
            pstmt.setString(2, sender);
            pstmt.executeUpdate();
            pstmt.close();

            if (wts > ts) {
                String sql_update_ts = "UPDATE accounts set ts = ? where public_key = ? ;";
                pstmt = c.prepareStatement(sql_update_ts);
                pstmt.setLong(1, wts);
                pstmt.setString(2, sender);
                pstmt.executeUpdate();
                pstmt.close();
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

    /**
     * Returns an account from the database
     * @param publicKey The publicKey of the wanted account
     * @return the Account with the publicKey if it exists, otherwise returns null
     */

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
                String senderSignature = rs2.getString("sender_signature");
                String receiverSignature = rs2.getString("receiver_signature");
                long wts = rs2.getLong("wts");
                long rid = rs2.getLong("rid");
                acceptedTransfer = new AcceptedTransfer(source_key, receiver_key, amount, senderSignature, receiverSignature, wts, rid );
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
                String senderSignature = rs3.getString("sender_signature");
                long wts = rs3.getLong("wts");
                long rid = rs3.getLong("rid");
                pendingTransfer = new PendingTransfer(source_key, receiver_key, amount, senderSignature, wts, rid);
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

    public String receiveAmount(String sender, String receiver, long wts, String signature, long rid) {
        Account senderAccount = getAccount(sender);
        if (senderAccount == null){
            return "The sender account doesn't exist!";
        }
        Account receiverAccount = getAccount(receiver);
        if (receiverAccount == null){
            return "The receiver account doesn't exist!";
        }
        long ts = receiverAccount.getTs();
        if(getCurrentTransfersFromAccount(receiver).contains(wts)){
            return "Unneeded Transfer";
        }
        if(wts <= ts && getCurrentTransfersFromAccount(receiver).contains(wts)){
            return "Wrong ts!";
        }
        List<PendingTransfer> transfers =  receiverAccount.getPendingTransfers()
                .stream()
                .filter(d -> d.sender().equals(sender) && d.receiver().equals(receiver))
                .collect(Collectors.toList());
        System.out.println(transfers);
        transfers.sort(Collections.reverseOrder());
        if (transfers.size() <= 0){
            return "No transfers to  receive!";
        }
        PendingTransfer pendingTransfer = transfers.get(0);
        Long transfers_ts = pendingTransfer.getWts();
        receiverAccount.changingBalance(pendingTransfer.amount());
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;
        PreparedStatement pstmt3 = null;
        PreparedStatement pstmt4 = null;

        String sql_balance;
        String sql_delete_transfers;
        String sql_insert_transfers;
        String sql_update_ts;

        try {

            sql_balance = "UPDATE accounts SET balance = ? WHERE public_key = ? ;";
            pstmt1 = c.prepareStatement(sql_balance);
            pstmt1.setInt(1, receiverAccount.balance());
            pstmt1.setString(2, receiver);
            pstmt1.executeUpdate();
            pstmt1.close();

            sql_delete_transfers = "DELETE FROM pending_transfers WHERE source_key = ? AND receiver_key = ? AND wts = ? ;";
            pstmt2 = c.prepareStatement(sql_delete_transfers);
            pstmt2.setString(1, sender);
            pstmt2.setString(2, receiver);
            pstmt2.setLong(3, transfers_ts);
            pstmt2.executeUpdate();
            pstmt2.close();

            sql_insert_transfers = "INSERT INTO transfers (source_key, receiver_key, amount, sender_signature, receiver_signature, wts, rid) VALUES (?,?,?,?,?,?,?);";
            pstmt3 = c.prepareStatement(sql_insert_transfers);
            pstmt3.setString(1, sender);
            pstmt3.setString(2, receiver);
            pstmt3.setInt(3, pendingTransfer.amount());
            pstmt3.setString(4, pendingTransfer.getSignature());
            pstmt3.setString(5, signature);
            pstmt3.setLong(6, wts);
            pstmt3.setLong(7, rid);

            pstmt3.executeUpdate();
            pstmt3.close();

            if (wts > ts){
                sql_update_ts = "UPDATE accounts set ts = ? where public_key = ? ;";
                pstmt4 = c.prepareStatement(sql_update_ts);
                pstmt4.setLong(1, wts);
                pstmt4.setString(2, receiver);
                pstmt4.executeUpdate();
                pstmt4.close();
             }

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

    public ArrayList<Long> getCurrentTransfersFromAccount(String accountKey){
        ArrayList<Long> currentTransfers = new ArrayList<>();
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        String sql_pendingTransfers_wts;
        String sql_transfers_wts;

        try {

            sql_pendingTransfers_wts = "SELECT wts FROM pending_transfers WHERE source_key = ?;";
            pstmt1 = c.prepareStatement(sql_pendingTransfers_wts);
            pstmt1.setString(1, accountKey);
            rs1 = pstmt1.executeQuery();
            while (rs1.next()) {
                Long wts = rs1.getLong("wts");
                currentTransfers.add(wts);
            }

            sql_transfers_wts = "SELECT wts FROM transfers WHERE receiver_key = ?;";
            pstmt2 = c.prepareStatement(sql_transfers_wts);
            pstmt2.setString(1, accountKey);
            rs2 = pstmt2.executeQuery();
            while (rs2.next()) {
                Long wts = rs2.getLong("wts");
                currentTransfers.add(wts);
            }
            c.commit();
        }
        catch (SQLException e1){
            try {
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
                if (pstmt1 != null) {
                    pstmt1.close();
                }
                if (pstmt2 != null) {
                    pstmt2.close();
                }
                if (rs1 != null) {
                    rs1.close();
                }
                if (rs2 != null) {
                    rs2.close();
                }
            }catch (SQLException e){
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }

        }
        System.out.println("currentTransfers: " + currentTransfers);
        return currentTransfers;

    }

    /*
    public ArrayList<Long> getCurrentTransfers(){
        ArrayList<Long> currentTransfers = new ArrayList<>();
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        String sql_pendingTransfers_wts;
        String sql_transfers_wts;

        try {

            sql_pendingTransfers_wts = "SELECT wts FROM pending_transfers;";
            pstmt1 = c.prepareStatement(sql_pendingTransfers_wts);
            rs1 = pstmt1.executeQuery();
            while (rs1.next()) {
                Long wts = rs1.getLong("wts");
                currentTransfers.add(wts);
            }

            sql_transfers_wts = "SELECT wts FROM transfers;";
            pstmt2 = c.prepareStatement(sql_transfers_wts);
            rs2 = pstmt2.executeQuery();
            while (rs2.next()) {
                Long wts = rs2.getLong("wts");
                currentTransfers.add(wts);
            }
            c.commit();
        }
        catch (SQLException e1){
            try {
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
                if (pstmt1 != null) {
                    pstmt1.close();
                }
                if (pstmt2 != null) {
                    pstmt2.close();
                }
                if (rs1 != null) {
                    rs1.close();
                }
                if (rs2 != null) {
                    rs2.close();
                }
            }catch (SQLException e){
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }

        }
        return currentTransfers;


    }*/

}
