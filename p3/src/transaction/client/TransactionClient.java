package transaction.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import transaction.comm.Message;
import static transaction.comm.MessageTypes.SHUTDOWN;
import static transaction.comm.MessageTypes.TRANSACTION_ABORTED;
import static transaction.comm.MessageTypes.TRANSACTION_COMMITTED;
import utils.PropertyHandler;
import static utils.TerminalColors.ABORT_COLOR;
import static utils.TerminalColors.COMMIT_COLOR;
import static utils.TerminalColors.RESET_COLOR;
import static utils.TerminalColors.RESTARTED_COLOR;

/**
 *
 * @author wolfdieterotte
 */
public class TransactionClient extends Thread {

    public static int numberTransactions;
    public static int numberAccounts;
    public static int initialBalance;

    public static int sleepMilliseconds;

    public static String host;
    public static int port;

    public ArrayList<Thread> threads = new ArrayList();
    public static boolean restartTransactions = true;

    /**
     * Default Constructor
     *
     * @param clientPropertiesFile Containing properties on the client side
     * @param serverPropertiesFile Containing properties on the server side -
     * still need to know #accounts/initial balance
     */
    public TransactionClient(String clientPropertiesFile, String serverPropertiesFile) {
        Properties serverProperties;
        Properties clientProperties;

        try {
            serverProperties = new PropertyHandler(serverPropertiesFile);
            host = serverProperties.getProperty("HOST");
            port = Integer.parseInt(serverProperties.getProperty("PORT"));
            
            numberAccounts = Integer.parseInt(serverProperties.getProperty("NUMBER_ACCOUNTS"));
            initialBalance = Integer.parseInt(serverProperties.getProperty("INITIAL_BALANCE"));

            clientProperties    = new PropertyHandler(clientPropertiesFile);
            numberTransactions  = Integer.parseInt(clientProperties.getProperty("NUMBER_TRANSACTIONS"));
            restartTransactions = Boolean.valueOf(clientProperties.getProperty("RESTART_TRANSACTIONS"));
            sleepMilliseconds   = Integer.parseInt(clientProperties.getProperty("SLEEP_MILLISECONDS"));
        } catch (IOException | NumberFormatException ex) {
            // whatever
        }

        System.err.println("Properties Summary");
        System.err.println("\t" + "HOST:               " + host);
        System.err.println("\t" + "PORT:               " + port);
        System.err.println("\t" + "NUMBER_ACCOUNTS:    " + numberAccounts);
        System.err.println("\t" + "NUMBER_TRANSACTIONS:" + numberTransactions);
    }

    /**
     * Implementation of runnable, just to get things going in main() - not used in a Thread
     */
    @Override
    public void run() {

        int transactionCounter;
        Thread currentThread;

        Socket serverConnection;
        ObjectOutputStream writeToNet;

        // create all the threads that execute transactions
        for (transactionCounter = 0; transactionCounter < numberTransactions; transactionCounter++) {
            currentThread = new TransactionThread();
            threads.add(currentThread);
            currentThread.start();
        }

        // joining all threads, don't want to finish prematurely
        Iterator<Thread> threadIterator = threads.iterator();
        while (threadIterator.hasNext()) {
            try {
                threadIterator.next().join();
            } catch (InterruptedException ex) {
                Logger.getLogger(TransactionClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        System.out.println("============================================================= WE ARE DONE, SHUTTING DOWN =============================================================");

        // here we have finished all transactions, let's shutdown server
        try {
            serverConnection = new Socket(host, port);
            writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());
            writeToNet.writeObject(new Message(SHUTDOWN, null));
            serverConnection.close();
        } catch (IOException ex) {
            System.err.println("[TransactionServerProxy.openTransaction] Error occurred");
        }
    }

    /**
     * Helper class representing a thread running one transaction
     */
    public class TransactionThread extends Thread {

        public TransactionThread() {
        }

        /**
         * Overriding run() in Thread
         */
        @Override
        public void run() {

            int transactionID;
            int priorTransactionID = 0;

            int accountFrom;
            int accountTo;

            int amount;
            int balance;

            int returnStatus;

            // setting up the accounts involved in the transfer plus amount to be transferred
            accountFrom = (int) Math.floor(Math.random() * numberAccounts);
            accountTo = (int) Math.floor(Math.random() * numberAccounts);
            amount = (int) Math.ceil(Math.random() * initialBalance);
            
            // running in a loop, in case the same transaction needs to be restarted
            do {
                // open transaction
                TransactionServerProxy transaction = new TransactionServerProxy(host, port);
                transactionID = transaction.openTransaction();

                // check if we are here the first time or in a restart
                if (priorTransactionID == 0) {
                    System.out.println("Transaction #" + transactionID + " started, transfer $" + amount + ": " + accountFrom + "->" + accountTo);
                } else {
                    System.out.println("\t\tPrior transaction #" + priorTransactionID + " " + RESTARTED_COLOR + "RESTARTED" + RESET_COLOR + " as transaction #" + transactionID + ", transfer $" + amount + ": " + accountFrom + "->" + accountTo);
                    //System.out.println("Prior transaction #" + priorTransactionID + " restarted as transaction #" + transactionID + ", transfer $" + amount + ": " + accountFrom + "->" + accountTo);
                }

                balance = transaction.read(accountFrom);
                transaction.write(accountFrom, balance - amount);

                balance = transaction.read(accountTo);
                transaction.write(accountTo, balance + amount);

                // close transaction - we may have a TRANSACTION_ABORTED
                returnStatus = transaction.closeTransaction();

                switch (returnStatus) {
                    case TRANSACTION_COMMITTED:
                        System.out.println("Transaction #" + transactionID + " " + COMMIT_COLOR + "COMMITTED" + RESET_COLOR);
                        break;
                    case TRANSACTION_ABORTED:
                        System.out.println("\tTransaction #" + transactionID + " " + ABORT_COLOR + "ABORTED" + RESET_COLOR);
                        priorTransactionID = transactionID;
                        break;
                    default:
                    // cannot occur
                }

            } while ((returnStatus != TRANSACTION_COMMITTED) && restartTransactions); // get back and restart transaction, if failed
        }
    }

    /**
     * Get things rolling
     *
     * @param args
     */
    public static void main(String[] args) {
        (new TransactionClient("../../config/TransactionClient.properties", "../../config/TransactionServer.properties")).start();
    }
}
