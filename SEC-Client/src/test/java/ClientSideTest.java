import client.ClientSide;
import communication.channel.Channel;
import communication.channel.PlainChannel;
import communication.channel.SignedChannel;
import communication.crypto.KeyConversion;
import org.junit.jupiter.api.*;

import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientSideTest {

    static Socket socket;
    static SignedChannel channel;
    static ClientSide clientSide;
    static KeyPair keyPair;
    static PublicKey publicKey;
    static PrivateKey privateKey;
    static Socket socket2;
    static SignedChannel channel2;
    static ClientSide clientSide2;
    static KeyPair keyPair2;
    static PublicKey publicKey2;
    static PrivateKey privateKey2;
    static final PublicKey serverPublicKey = KeyConversion.stringToKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDR5XF" +
                                                                       "Qum9i0YS5clSPUpc4tVsd/fr383tXqhEu3+vYAi0ORqFQ/7h6ZlSH66xO6etg9Z1reyjsSo81t9rt1jg8Jo3JGhDf053e" +
                                                                       "8KDXr9HJgqLSZPi1VJtlvJV4jZ4xBdKtsG0A95XA/CeA3JaQB8ZmV5mY8qj/SRIWanS4JT7kzQIDAQAB");
    static PublicKey publicKey3;


    @BeforeAll
    static void setupTests() throws Exception{
        /*Generate New Keys for Test*/
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        KeyPairGenerator keyPairGen2 = KeyPairGenerator.getInstance("RSA");
        KeyPairGenerator keyPairGen3 = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);
        keyPairGen2.initialize(2048);
        keyPairGen3.initialize(2048);
        keyPair = keyPairGen.generateKeyPair();
        keyPair2 = keyPairGen2.generateKeyPair();
        publicKey = keyPair.getPublic();
        publicKey2 = keyPair2.getPublic();
        privateKey = keyPair.getPrivate();
        privateKey2 = keyPair2.getPrivate();
        publicKey3 = keyPairGen3.generateKeyPair().getPublic();
        /* Generates the socket, channel and clientSide */
        socket = new Socket("localhost", 8080);
        socket2 = new Socket("localhost", 8080);
        channel = new SignedChannel(socket, serverPublicKey, privateKey);
        channel2 = new SignedChannel(socket2, serverPublicKey, privateKey2);
        clientSide = new ClientSide(channel, publicKey);
        clientSide2 = new ClientSide(channel2, publicKey2);
        System.out.println(publicKey);
        /* Sends Public Keys */
        clientSide.sendPublicKey();
        clientSide2.sendPublicKey();
    }

    //OpenAccounts Test

    @Test
    @Order(1)
    void openAnAccount() throws Exception {
        assertEquals("Account Opened With Success!",clientSide.openAccount(publicKey));
    }

    @Test
    @Order(2)
    void openAnAlreadyOpenAccount() throws Exception {
        String publicKeyString = KeyConversion.keyToString(publicKey);
        assertEquals("Account with Public Key = " + publicKeyString + "already exists" ,clientSide.openAccount(publicKey));
    }

    @Test
    @Order(3)
    void openAnotherAccount() throws Exception {
        assertEquals("Account Opened With Success!",clientSide2.openAccount(publicKey2));
    }

    //SendAmount Tests

    @Test
    @Order(4)
    void SendAmountSendMoreThanBalanceAmount() throws Exception {
        assertEquals("Not enough balance to send that amount!", clientSide.sendAmountRequest(publicKey, publicKey2, 15));
    }

    @Test
    @Order(5)
    void SendAmountSenderDoesNotExist() throws Exception {
        assertEquals("The sender account doesn't exist!", clientSide.sendAmountRequest(publicKey3, publicKey2, 5));
    }

    @Test
    @Order(6)
    void SendAmountReceiverDoesNotExist() throws Exception {
        assertEquals("The receiver account doesn't exist!", clientSide.sendAmountRequest(publicKey, publicKey3, 5));
    }

    @Test
    @Order(7)
    void Client1SendAmountToClient2() throws Exception {
        assertEquals("Transfer made with success.", clientSide.sendAmountRequest(publicKey, publicKey2, 5));
    }

    @Test
    @Order(8)
    void Client2SendAmountToClient1() throws Exception {
        assertEquals("Transfer made with success.", clientSide2.sendAmountRequest(publicKey2, publicKey, 5));
    }

    //CheckAccount Test
    @Test
    @Order(9)
    void checkAccountButNoAccount() throws Exception {
        assertEquals("Account with public key = " + KeyConversion.keyToString(publicKey3) + " does not exist", clientSide.checkAccount(publicKey3));
    }

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

    //ReceiveAmount
    @Test
    @Order(12)
    void ReceiveAmountSenderDoesNotExist() throws Exception {
        assertEquals("The sender account doesn't exist!", clientSide.receiveAmountRequest(publicKey3, publicKey2));
    }

    @Test
    @Order(13)
    void ReceiveAmountReceiverDoesNotExist() throws Exception {
        assertEquals("The receiver account doesn't exist!", clientSide.receiveAmountRequest(publicKey, publicKey3));
    }

    @Test
    @Order(14)
    void ReceiveAmountClient1Client2() throws Exception {
        assertEquals("Transfer receive with success!", clientSide.receiveAmountRequest(publicKey2, publicKey));
    }

    @Test
    @Order(15)
    void ReceiveAmountClient2Client1() throws Exception {
        assertEquals("Transfer receive with success!", clientSide2.receiveAmountRequest(publicKey, publicKey2));
    }

    //Audit Tests
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
                     "amount=" + 5 + ']' + ']',clientSide.audit(publicKey2));
    }

    @AfterAll
    static void closeStuff() throws Exception {
        clientSide.getChannel().closeSocket();
        clientSide2.getChannel().closeSocket();
    }

}