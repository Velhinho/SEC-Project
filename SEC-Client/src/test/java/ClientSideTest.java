import client.Register;
import client.commands.OpenCommand;
import communication.channel.BroadcastChannel;
import communication.channel.Channel;
import communication.channel.ClientChannel;
import communication.crypto.KeyConversion;
import org.junit.jupiter.api.*;

import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientSideTest {

    static final PublicKey serverPublicKey = KeyConversion.stringToKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDR5XF" +
                                                                       "Qum9i0YS5clSPUpc4tVsd/fr383tXqhEu3+vYAi0ORqFQ/7h6ZlSH66xO6etg9Z1reyjsSo81t9rt1jg8Jo3JGhDf053e" +
                                                                       "8KDXr9HJgqLSZPi1VJtlvJV4jZ4xBdKtsG0A95XA/CeA3JaQB8ZmV5mY8qj/SRIWanS4JT7kzQIDAQAB");


    static Socket socket;
    static Register register;
    static ClientChannel clientChannel;
    static ArrayList<Channel> channels;
    static BroadcastChannel broadcastChannel;
    static KeyPair keyPair;
    static PublicKey publicKey;
    static PrivateKey privateKey;

    static Socket socket2;
    static Register register2;
    static ClientChannel clientChannel2;
    static ArrayList<Channel> channels2;
    static BroadcastChannel broadcastChannel2;
    static KeyPair keyPair2;
    static PublicKey publicKey2;
    static PrivateKey privateKey2;

    static Socket socket3;
    static Register register3;
    static ClientChannel clientChannel3;
    static ArrayList<Channel> channels3;
    static BroadcastChannel broadcastChannel3;
    static KeyPair keyPair3;
    static PublicKey publicKey3;
    static PrivateKey privateKey3;


    @BeforeAll
    static void setupTests() throws Exception{
        /*Generate Cryptographic Stuff for Tests*/
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        KeyPairGenerator keyPairGen2 = KeyPairGenerator.getInstance("RSA");
        KeyPairGenerator keyPairGen3 = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);
        keyPairGen2.initialize(2048);
        keyPairGen3.initialize(2048);
        keyPair = keyPairGen.generateKeyPair();
        keyPair2 = keyPairGen2.generateKeyPair();
        keyPair3 =  keyPairGen3.generateKeyPair();
        publicKey = keyPair.getPublic();
        publicKey2 = keyPair2.getPublic();
        privateKey = keyPair.getPrivate();
        privateKey2 = keyPair2.getPrivate();
        publicKey3 = keyPair3.getPublic();
        privateKey3 = keyPair3.getPrivate();

    }

    @BeforeEach
    void setupSockets() throws Exception{
        socket = new Socket("localhost", 8080);
        socket2 = new Socket("localhost", 8080);
        socket3 = new Socket("localhost", 8080);

        clientChannel = new ClientChannel(socket, privateKey);
        clientChannel2 = new ClientChannel(socket2, privateKey2);
        clientChannel3 = new ClientChannel(socket3, privateKey3);

        channels.add(clientChannel);
        channels2.add(clientChannel2);
        channels3.add(clientChannel3);

        broadcastChannel = new BroadcastChannel(channels);
        broadcastChannel2 = new BroadcastChannel(channels2);
        broadcastChannel3 = new BroadcastChannel(channels3);

        register = new Register(broadcastChannel, 0);
        register2 = new Register(broadcastChannel2, 0);
        register3 = new Register(broadcastChannel3, 0);
    }

    @AfterEach
    void closeSocket() throws Exception{
        register.getBroadcastChannel().closeSocket();
        register2.getBroadcastChannel().closeSocket();
        register3.getBroadcastChannel().closeSocket();
    }


    @Test
    @Order(1)
    void openAnAccount() throws Exception {
        String publicKeyString = KeyConversion.keyToString(publicKey);
        OpenCommand openCommand = new OpenCommand(publicKeyString);
        openCommand.execCommand(register);
        assertEquals("Account Opened With Success!", ));
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
        String publicKeyString = KeyConversion.keyToString(publicKey2);
        OpenCommand openCommand = new OpenCommand(publicKeyString);
        openCommand.execCommand(register2);;
        assertEquals("Account Opened With Success!",);
    }


    @Test
    @Order(4)
    void SendAmountSendMoreThanBalanceAmount() throws Exception {
        assertEquals("Not enough balance to send that amount!", clientSide.sendAmountRequest(publicKey, publicKey2, 15));
    }

    @Test
    @Order(5)
    void SendAmountSenderDoesNotExist() throws Exception {
        assertEquals("The sender account doesn't exist!", clientSide3.sendAmountRequest(publicKey3, publicKey, 5));
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

}