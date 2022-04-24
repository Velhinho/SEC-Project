import client.ClientExecutor;
import client.Register;
import client.commands.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import communication.crypto.KeyConversion;
import communication.messages.*;
import org.junit.jupiter.api.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClientTest {

    static Register register1;
    static Register register2;
    static Register register3;
    static ClientExecutor clientExecutor1;
    static ClientExecutor clientExecutor2;
    static ClientExecutor clientExecutor3;
    static KeyPair keyPair1;
    static KeyPair keyPair2;
    static KeyPair keyPair3;
    static int writeOperations = 0;

    @BeforeAll
    static void setupTests() throws Exception{
        /*Generate Cryptographic Stuff for Tests*/
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        KeyPairGenerator keyPairGen2 = KeyPairGenerator.getInstance("RSA");
        KeyPairGenerator keyPairGen3 = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);
        keyPairGen2.initialize(2048);
        keyPairGen3.initialize(2048);
        keyPair1 = keyPairGen.generateKeyPair();
        keyPair2 = keyPairGen2.generateKeyPair();
        keyPair3 =  keyPairGen3.generateKeyPair();

        int f = 1;

        clientExecutor1 = new ClientExecutor(keyPair1, f);
        clientExecutor2 = new ClientExecutor(keyPair2, f);
        clientExecutor3 = new ClientExecutor(keyPair3, f);

        register1 = clientExecutor1.getWts(KeyConversion.keyToString(keyPair1.getPublic()));
        register2 = clientExecutor2.getWts(KeyConversion.keyToString(keyPair2.getPublic()));
        register3 = clientExecutor3.getWts(KeyConversion.keyToString(keyPair3.getPublic()));
    }

    @Test
    @Order(1)
    void openAnAccount() throws Exception {
        String sender =  KeyConversion.keyToString(keyPair1.getPublic());
        OpenCommand openCommand = new OpenCommand(sender);
        assertEquals("Account Opened With Success!", clientExecutor1.runCommand(register1, openCommand));
    }

    @Test
    @Order(2)
    void openSameAccount() throws Exception {
        String sender =  KeyConversion.keyToString(keyPair1.getPublic());
        OpenCommand openCommand = new OpenCommand(sender);
        assertEquals("Account with Public Key = " + sender + "already exists", clientExecutor1.runCommand(register1, openCommand));
    }

    @Test
    @Order(3)
    void openAnotherAccount() throws Exception {
        String sender =  KeyConversion.keyToString(keyPair2.getPublic());
        OpenCommand openCommand = new OpenCommand(sender);
        assertEquals("Account Opened With Success!", clientExecutor2.runCommand(register2, openCommand));
    }

    //SendTests

    @Test
    @Order(4)
    void SendAmountSendMoreThanBalanceAmount() throws Exception {
        String sender =  KeyConversion.keyToString(keyPair1.getPublic());
        String receiver = KeyConversion.keyToString(keyPair2.getPublic());
        SendCommand sendCommand = new SendCommand(sender, receiver, 15);
        assertEquals("Not enough balance to send that amount!", clientExecutor1.runCommand(register1, sendCommand));
    }

    @Test
    @Order(5)
    void SendAmountSenderDoesNotExist() throws Exception {
        String sender =  KeyConversion.keyToString(keyPair3.getPublic());
        String receiver = KeyConversion.keyToString(keyPair2.getPublic());
        SendCommand sendCommand = new SendCommand(sender, receiver, 5);
        assertEquals("The sender account doesn't exist!", clientExecutor3.runCommand(register3, sendCommand));
    }

    @Test
    @Order(6)
    void SendAmountReceiverDoesNotExist() throws Exception {
        String sender =  KeyConversion.keyToString(keyPair1.getPublic());
        String receiver = KeyConversion.keyToString(keyPair3.getPublic());
        SendCommand sendCommand = new SendCommand(sender, receiver, 5);
        assertEquals("The receiver account doesn't exist!", clientExecutor1.runCommand(register1, sendCommand));
    }

    @Test
    @Order(7)
    void Client1SendAmountToClient2() throws Exception {
        String sender =  KeyConversion.keyToString(keyPair1.getPublic());
        String receiver = KeyConversion.keyToString(keyPair2.getPublic());
        SendCommand sendCommand = new SendCommand(sender, receiver, 5);
        assertEquals("Transfer made with success.", clientExecutor1.runCommand(register1, sendCommand));
    }

    @Test
    @Order(8)
    void Client2SendAmountToClient1() throws Exception {
        String sender =  KeyConversion.keyToString(keyPair2.getPublic());
        String receiver = KeyConversion.keyToString(keyPair1.getPublic());
        SendCommand sendCommand = new SendCommand(sender, receiver, 5);
        assertEquals("Transfer made with success.", clientExecutor2.runCommand(register2, sendCommand));
    }

    // Check
    @Test
    @Order(9)
    void checkAccountButNoAccount() throws Exception {
        String sender =  KeyConversion.keyToString(keyPair3.getPublic());
        CheckCommand checkCommand = new CheckCommand(sender);
        assertEquals("Account with public key = " + sender + " does not exist", clientExecutor3.runCommand(register3, checkCommand));
    }

    @Test
    @Order(10)
    void checkAccount1() throws Exception {
        String publicKey =  KeyConversion.keyToString(keyPair1.getPublic());
        String publicKey2 = KeyConversion.keyToString(keyPair2.getPublic());
        PendingTransfer transfer = new PendingTransfer(publicKey, publicKey2, 5, null, 0, 0);
        PendingTransfer transfer2 = new PendingTransfer(publicKey2, publicKey, 5, null, 0,0);
        ArrayList<PendingTransfer> transfers = new ArrayList<>();
        transfers.add(transfer2);
        transfers.add(transfer);
        CheckResponse checkResponse = new CheckResponse(5,transfers,"Check");
        CheckCommand checkCommand = new CheckCommand(publicKey);
        assertEquals(checkResponse.toString(), clientExecutor1.runCommand(register1, checkCommand));
    }

    @Test
    @Order(11)
    void checkAccount2() throws Exception {
        String publicKey =  KeyConversion.keyToString(keyPair1.getPublic());
        String publicKey2 = KeyConversion.keyToString(keyPair2.getPublic());
        PendingTransfer transfer = new PendingTransfer(publicKey, publicKey2, 5, null, 0, 0);
        PendingTransfer transfer2 = new PendingTransfer(publicKey2, publicKey, 5, null, 0,0);
        ArrayList<PendingTransfer> transfers = new ArrayList<>();
        transfers.add(transfer2);
        transfers.add(transfer);
        CheckResponse checkResponse = new CheckResponse(5,transfers,"Check");
        CheckCommand checkCommand = new CheckCommand(publicKey2);
        assertEquals(checkResponse.toString(), clientExecutor2.runCommand(register2, checkCommand));
    }


    //Receive Tests
    @Test
    @Order(12)
    void ReceiveAmountSenderDoesNotExist() throws Exception {
        String sender = KeyConversion.keyToString(keyPair3.getPublic());
        String receiver = KeyConversion.keyToString(keyPair1.getPublic());
        ReceiveCommand receiveCommand = new ReceiveCommand(sender, receiver);
        assertEquals("The sender account doesn't exist!", clientExecutor1.runCommand(register1, receiveCommand));
    }

    @Test
    @Order(13)
    void ReceiveAmountReceiverDoesNotExist() throws Exception {
        String sender = KeyConversion.keyToString(keyPair1.getPublic());
        String receiver = KeyConversion.keyToString(keyPair3.getPublic());
        ReceiveCommand receiveCommand = new ReceiveCommand(sender, receiver);
        assertEquals("The receiver account doesn't exist!", clientExecutor3.runCommand(register3, receiveCommand));
    }

    @Test
    @Order(14)
    void ReceiveAmountClient1Client2() throws Exception {
        String sender = KeyConversion.keyToString(keyPair2.getPublic());
        String receiver = KeyConversion.keyToString(keyPair1.getPublic());
        ReceiveCommand receiveCommand = new ReceiveCommand(sender, receiver);
        assertEquals("Transfer received with success!", clientExecutor1.runCommand(register1, receiveCommand));
    }

    @Test
    @Order(15)
    void ReceiveAmountClient2Client1() throws Exception {
        String sender = KeyConversion.keyToString(keyPair1.getPublic());
        String receiver = KeyConversion.keyToString(keyPair2.getPublic());
        ReceiveCommand receiveCommand = new ReceiveCommand(sender, receiver);
        assertEquals("Transfer received with success!", clientExecutor2.runCommand(register2, receiveCommand));
    }

    @Test
    @Order(16)
    void AuditAnAccountThatDoesntExist() throws Exception {
        String publicKey3String = KeyConversion.keyToString(keyPair3.getPublic());
        AuditCommand auditCommand = new AuditCommand(publicKey3String);
        assertEquals( "Account with public key = " + publicKey3String + " does not exist", clientExecutor3.runCommand(register3, auditCommand));
    }

    @Test
    @Order(17)
    void AuditClient1() throws Exception{
        String publicKey =  KeyConversion.keyToString(keyPair1.getPublic());
        String publicKey2 = KeyConversion.keyToString(keyPair2.getPublic());
        AcceptedTransfer transfer = new AcceptedTransfer(publicKey, publicKey2, 5, null,  0,0);
        AcceptedTransfer transfer2 = new AcceptedTransfer(publicKey2, publicKey, 5, null, 0,0);
        ArrayList<AcceptedTransfer> transfers = new ArrayList<>();
        transfers.add(transfer);
        transfers.add(transfer2);
        AuditResponse auditResponse = new AuditResponse(transfers,"Audit");
        AuditCommand auditCommand = new AuditCommand(publicKey);
        assertEquals(auditResponse.toString()
                , clientExecutor1.runCommand(register1, auditCommand));
    }

    @Test
    @Order(18)
    void AuditClient2() throws Exception{
        String publicKey =  KeyConversion.keyToString(keyPair1.getPublic());
        String publicKey2 = KeyConversion.keyToString(keyPair2.getPublic());
        AcceptedTransfer transfer = new AcceptedTransfer(publicKey, publicKey2, 5, null,  0,0);
        AcceptedTransfer transfer2 = new AcceptedTransfer(publicKey2, publicKey, 5, null, 0,0);
        ArrayList<AcceptedTransfer> transfers = new ArrayList<>();
        transfers.add(transfer);
        transfers.add(transfer2);
        AuditResponse auditResponse = new AuditResponse(transfers,"Audit");
        AuditCommand auditCommand = new AuditCommand(publicKey2);
        assertEquals(auditResponse.toString()
                , clientExecutor2.runCommand(register2, auditCommand));
    }

    @Test
    @Order(19)
    void Client2AuditsClient1() throws Exception{
        String publicKey =  KeyConversion.keyToString(keyPair1.getPublic());
        String publicKey2 = KeyConversion.keyToString(keyPair2.getPublic());
        AcceptedTransfer transfer = new AcceptedTransfer(publicKey, publicKey2, 5, null,  0,0);
        AcceptedTransfer transfer2 = new AcceptedTransfer(publicKey2, publicKey, 5, null, 0,0);
        ArrayList<AcceptedTransfer> transfers = new ArrayList<>();
        transfers.add(transfer);
        transfers.add(transfer2);
        AuditResponse auditResponse = new AuditResponse(transfers,"Audit");
        AuditCommand auditCommand = new AuditCommand(publicKey);
        assertEquals(auditResponse.toString()
                , clientExecutor2.runCommand(register2, auditCommand));
    }

    @Test
    @Order(20)
    void Client1AuditsClient2() throws Exception{
        String publicKey =  KeyConversion.keyToString(keyPair1.getPublic());
        String publicKey2 = KeyConversion.keyToString(keyPair2.getPublic());
        AcceptedTransfer transfer = new AcceptedTransfer(publicKey, publicKey2, 5, null, 0,0);
        AcceptedTransfer transfer2 = new AcceptedTransfer(publicKey2, publicKey, 5, null,0,0);
        ArrayList<AcceptedTransfer> transfers = new ArrayList<>();
        transfers.add(transfer);
        transfers.add(transfer2);
        AuditResponse auditResponse = new AuditResponse(transfers,"Audit");
        AuditCommand auditCommand = new AuditCommand(publicKey2);
        assertEquals(auditResponse.toString()
                , clientExecutor1.runCommand(register1, auditCommand));
    }

    @Test
    @Order(21)
    void ProofOfWorkIsGenerated() throws Exception{
        String sender =  KeyConversion.keyToString(keyPair3.getPublic());
        CheckCommand checkCommand = new CheckCommand(sender);
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("command", JsonParser.parseString(gson.toJson(checkCommand)));
        int difficulty = 1;
        byte [] hash = Register.proofOfWork(jsonObject,difficulty);
        assertTrue(Register.matchesDifficulty(hash, difficulty));
    }



    /*

    @Test
    @Order(10)
    void checkAccount1() throws Exception {
        assertEquals("Account Balance: " + 5 + " \n"
                     + "["
                     + "PendingTransfer[" +
                     "transfer=" + "Transfer[" +
                     "sender=" + KeyConversion.keyToString(publicKey) + ", " +
                     "receiver=" + KeyConversion.keyToString(publicKey2) + ", " +
                     "amount=" + 5 + ']' + ']' +
                     "," + " PendingTransfer[" +
                     "transfer=" + "Transfer[" +
                     "sender=" + KeyConversion.keyToString(publicKey2) + ", " +
                     "receiver=" + KeyConversion.keyToString(publicKey) + ", " +
                     "amount=" + 5 + ']' + ']'
                     + "]", clientSide.checkAccount(publicKey));
    }

    @Test
    @Order(11)
    void checkAccount2() throws Exception {
        assertEquals("Account Balance: " + 5 + " \n"
                     + "["
                     + "PendingTransfer[" +
                     "transfer=" + "Transfer[" +
                     "sender=" + KeyConversion.keyToString(publicKey) + ", " +
                     "receiver=" + KeyConversion.keyToString(publicKey2) + ", " +
                     "amount=" + 5 + ']' + ']' +
                     "," + " PendingTransfer[" +
                     "transfer=" + "Transfer[" +
                     "sender=" + KeyConversion.keyToString(publicKey2) + ", " +
                     "receiver=" + KeyConversion.keyToString(publicKey) + ", " +
                     "amount=" + 5 + ']' + ']'
                     + "]", clientSide2.checkAccount(publicKey2));
    }

    @Test
    @Order(12)
    void ReceiveAmountSenderDoesNotExist() throws Exception {
        assertEquals("The sender account doesn't exist!", clientSide2.receiveAmountRequest(publicKey3, publicKey2));
    }

    @Test
    @Order(13)
    void ReceiveAmountReceiverDoesNotExist() throws Exception {
        assertEquals("The receiver account doesn't exist!", clientSide3.receiveAmountRequest(publicKey, publicKey3));
    }

    @Test
    @Order(14)
    void ReceiveAmountClient1Client2() throws Exception {
        assertEquals("Transfer received with success!", clientSide.receiveAmountRequest(publicKey2, publicKey));
    }

    @Test
    @Order(15)
    void ReceiveAmountClient2Client1() throws Exception {
        assertEquals("Transfer received with success!", clientSide2.receiveAmountRequest(publicKey, publicKey2));
    }

    @Test
    @Order(16)
    void AuditAnAccountThatDoesntExist() throws Exception {
        String publicKey3String = KeyConversion.keyToString(publicKey3);
        assertEquals( "Account with public key = " + publicKey3String + " does not exist", clientSide.audit(publicKey3));
    }


    @Test
    @Order(17)
    void AuditClient1() throws Exception{
        assertEquals("transfers = "  + "["
                     + "Transfer[" +
                     "sender=" + KeyConversion.keyToString(publicKey) + ", " +
                     "receiver=" + KeyConversion.keyToString(publicKey2) + ", " +
                     "amount=" + 5 + ']' + ',' +
                     " Transfer[" +
                     "sender=" + KeyConversion.keyToString(publicKey2) + ", " +
                     "receiver=" + KeyConversion.keyToString(publicKey) + ", " +
                     "amount=" + 5 + ']' + ']'
                , clientSide2.audit(publicKey));
    }

    @Test
    @Order(18)
    void AuditClient2() throws Exception{
        assertEquals("transfers = "  + "["
                     + "Transfer[" +
                     "sender=" + KeyConversion.keyToString(publicKey) + ", " +
                     "receiver=" + KeyConversion.keyToString(publicKey2) + ", " +
                     "amount=" + 5 + ']' + ',' +
                     " Transfer[" +
                     "sender=" + KeyConversion.keyToString(publicKey2) + ", " +
                     "receiver=" + KeyConversion.keyToString(publicKey) + ", " +
                     "amount=" + 5 + ']' + ']'
                , clientSide.audit(publicKey2));
    }

    */


}

