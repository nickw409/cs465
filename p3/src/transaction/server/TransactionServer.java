package transaction.server;

import transaction.server.transaction.TransactionManager;
import transaction.server.account.AccountManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import transaction.server.account.Account;
import utils.PropertyHandler;

/**
 * Class that represents a transaction server
 * 
 * @author Dr.-Ing. Wolf-Dieter Otte
 */
public class TransactionServer implements Runnable
{
    // manager objects
    public static AccountManager accountManager = null;
    public static TransactionManager transactionManager = null;
    
    // the server socket to accept incoming clients' requests
    public static ServerSocket serverSocket = null;

    // flag indicating to keep running the server loop
    static boolean keepgoing = true;

    // flag for logging purposes. decides to either show logs from a transaction's perspective
    // or reflecting the program execution
    public static boolean transactionView;

    // unique counter to number log message, so they can be ordered how they occurred
    static int messageCounter = 0;

    
    /**
     * Constructor
     * 
     * @param serverPropertiesFile file containing server-side configuration information
     */
    public TransactionServer(String serverPropertiesFile) 
    {

        Properties serverProperties = null;
        
        // variables holding config information
        int numberAccounts;
        int initialBalance;

        // get properties
        try 
        {
            serverProperties = new PropertyHandler(serverPropertiesFile);
        } catch (IOException e) 
        {
            System.out.println("[TransactionServer.TransactionServer] Didn't find properties file \"" + serverPropertiesFile + "\"");
            System.exit(1);
        }

        // create transaction manager
        transactionView = Boolean.parseBoolean(serverProperties.getProperty("TRANSACTION_VIEW"));
        TransactionServer.transactionManager = new TransactionManager();
        System.out.println("[TransactionServer.TransactionServer] TransactionManager created");

        // create account manager
        numberAccounts = Integer.parseInt(serverProperties.getProperty("NUMBER_ACCOUNTS"));
        initialBalance = Integer.parseInt(serverProperties.getProperty("INITIAL_BALANCE"));
        
        TransactionServer.accountManager = new AccountManager(numberAccounts, initialBalance);
        System.out.println("[TransactionServer.TransactionServer] AccountManager created");

        // create server socket
        try 
        {
            serverSocket = new ServerSocket(Integer.parseInt(serverProperties.getProperty("PORT")));
            System.out.println("[TransactionServer.TransactionServer] ServerSocket created");
        } catch (IOException ex) 
        {
            System.err.println("[TransactionServer.TransactionServer] Could not create server socket");
            System.exit(1);
        }
    }

    
    /**
     * Run the server loop, when done, print out summery
     */
    @Override
    public void run() 
    {
        // run server loop
        while (keepgoing) 
        {
            // run server loop
            try 
            {
                transactionManager.runTransaction(serverSocket.accept());
            } 
            catch (SocketException e) 
            {
                // we get here when we close the socket from the outside
                System.out.println("[TransactionServer.run] Socket closed, shutting down ...");
            } 
            catch (IOException ex) 
            {
                System.err.println("IOException in server loop");
            }
        }
        
        // we are done with the server loop, wait a second for things to calm down
        try 
        {
            Thread.sleep(1000);
        } 
        catch (InterruptedException ex) 
        {
            // ignore
        }   
        
        // print out summary
        printOutSummary();
    }
    
    
    /**
     * Helper method called indirectly by client by sending a SHUTDOWN message to server,
     * which is received by a TransactionManagerWorker that in turn will call this method.
     * 
     * The method is static to avoid passing on this server's object reference
     */
    public static void shutDown()
    {
        try {
            keepgoing = false;
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(TransactionServer.class.getName()).log(Level.SEVERE, null, ex);
        }    
    }
  
    
    /**
     * Print out summary
     */
    public void printOutSummary()
    {
        /*
        System.out.println("\n\n======================================= ABORTED TRANSACTIONS INFORMATION =======================================");

        StringBuffer abortedTransactionsLogs = new StringBuffer();
        Iterator<Transaction> abortedTransactionsIterator = TransactionServer.transactionManager.getAbortedTransactions().iterator();
        Transaction abortedTransaction;

        while (abortedTransactionsIterator.hasNext())
        {
            abortedTransaction = abortedTransactionsIterator.next();
            abortedTransactionsLogs.append(abortedTransaction.getLog()).append("\n");
        }

        System.out.print(abortedTransactionsLogs);
        */
        
        System.out.println("\n\n======================================= BRANCH TOTAL =======================================");

        ArrayList<Account> accounts = TransactionServer.accountManager.getAccounts();
        Iterator<Account> accountIterator = accounts.iterator();
        Account account;
        int total = 0;

        while (accountIterator.hasNext())
        {
            account = accountIterator.next();
            total += TransactionServer.accountManager.read(account.getNumber());
            System.out.print(account.getNumber() + ":" + account._read() + "$ ");
        }

        System.out.println("---> $" + total + "\n\n");        
    }
    
    
    /**
     * main()
     * @param args 
     */
    public static void main(String[] args) 
    {
        if (args.length == 1) {
            new TransactionServer(args[0]).run();
        } else {
            new TransactionServer("../../config/TransactionServer.properties").run();
        }
    }


    /**
     * Helper method to return a system-wide, unique counter for logging purposes
     * 
     * @return the counter value
     */
    public static synchronized int getMessageCount()
    {
        return ++messageCounter;
    }
}
    
    

