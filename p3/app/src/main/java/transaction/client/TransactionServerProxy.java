package transaction.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import transaction.comm.Message;
import transaction.comm.MessageTypes;

/**
 * This class represents the proxy that acts on behalf of the transaction server on the client side.
 * It provides an implementation of the coordinator interface to the client, hiding the fact
 * that there is a network in between.
 * From the client's perspective, an object of this class IS the transaction.
 * @author wolfdieterotte
 */
public class TransactionServerProxy implements MessageTypes {

    private String host;
    private int port;
    private Socket serverConnection;
    private ObjectOutputStream writeToNet;
    private ObjectInputStream readFromNet;
    private Integer transactionID = 0;

    /**
     * Constructor
     * @param host IP address of the transaction server
     * @param port port number of the transaction server
     */
    public TransactionServerProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Opens a transaction
     * 
     * @return the transaction ID 
     */
    public int openTransaction() {
        try {
            // Establish connection to the server
            serverConnection = new Socket(host, port);
            writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());
            readFromNet = new ObjectInputStream(serverConnection.getInputStream());

            // Send OPEN_TRANSACTION message to the server
            Message openTransactionMessage = new Message(OPEN_TRANSACTION);
            writeToNet.writeObject(openTransactionMessage);
            writeToNet.flush();

            // Receive transaction ID from server response
            Message response = (Message) readFromNet.readObject();
            transactionID = (Integer) response.getContent();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return transactionID;
    }

    /**
     * Requests this transaction to be closed.
     * 
     * @return the status, i.e., either TRANSACTION_COMMITTED or TRANSACTION_ABORTED
     */
    public int closeTransaction() {
        int returnStatus = TRANSACTION_ABORTED;
        try {
            // Send CLOSE_TRANSACTION message to server
            Message closeTransactionMessage = new Message(CLOSE_TRANSACTION, transactionID);
            writeToNet.writeObject(closeTransactionMessage);
            writeToNet.flush();

            // Receive return status
            Message response = (Message) readFromNet.readObject();
            returnStatus = (Integer) response.getContent();

            // Close the connection to the server
            writeToNet.close();
            readFromNet.close();
            serverConnection.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return returnStatus;
    }

    /**
     * Reading a value from an account
     * 
     * @param accountNumber Account number to read from
     * @return the balance of the account
     */
    public int read(int accountNumber) {
        int balance = 0;
        try {
            // Send READ_REQUEST message to the server
            Message readRequestMessage = new Message(READ_REQUEST, transactionID);
            readRequestMessage.setContent(accountNumber);
            writeToNet.writeObject(readRequestMessage);
            writeToNet.flush();

            // Receive balance from server response
            Message response = (Message) readFromNet.readObject();
            balance = (Integer) response.getContent();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return balance;
    }

    /**
     * Writing value to an account
     * 
     * @param accountNumber Account number to write to
     * @param amount Amount to write
     * @return the prior account balance
     */
    public int write(int accountNumber, int amount) {
        int priorBalance = 0;
        try {
            // Send WRITE_REQUEST message to the server
            Message writeRequestMessage = new Message(WRITE_REQUEST, transactionID);
            writeRequestMessage.setContent(new int[]{accountNumber, amount});
            writeToNet.writeObject(writeRequestMessage);
            writeToNet.flush();

            // Receive prior balance from server response
            Message response = (Message) readFromNet.readObject();
            priorBalance = (Integer) response.getContent();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return priorBalance;
    }
}
