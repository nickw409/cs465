package transaction.server.transaction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import transaction.comm.Message;
import transaction.comm.MessageTypes;
import transaction.server.TransactionServer;
import static utils.TerminalColors.ABORT_COLOR;
import static utils.TerminalColors.COMMIT_COLOR;
import static utils.TerminalColors.OPEN_COLOR;
import static utils.TerminalColors.READ_COLOR;
import static utils.TerminalColors.RESET_COLOR;
import static utils.TerminalColors.WRITE_COLOR;


/**
 * Class representing the (singleton) transaction manager
 * @author Dr.-Ing. Wolf-Dieter Otte
 */
public class TransactionManager implements MessageTypes 
{
    // counter for transaction IDs
    private static int transactionIdCounter = 0;
    
    // lists of transactions
    private static final ArrayList<Transaction>        runningTransactions   = new ArrayList<>();
    private static final HashMap<Integer, Transaction> committedTransactions = new HashMap<>();
    private static final ArrayList<Transaction>        abortedTransactions   = new ArrayList<>();
   
    // transaction number counter specific to OCC
    private static int transactionNumberCounter = 0;


    /**
     * Default constructor, nothing to do
     */
    public TransactionManager() {}
    
    
    /**
     * Helper method returning aborted transactions
     * 
     * @return the list of aborted transactions
     */
    public ArrayList<Transaction> getAbortedTransactions()
    {
        return abortedTransactions;
    }

    
    /**
     * Run the transaction for an incoming client request
     * 
     * @param client Socket object representing connection to client
     */
    public synchronized void runTransaction(Socket client) 
    {
        (new TransactionManagerWorker(client)).start();
    }

    
    /**
     * Validates a transaction according to OCC, implementing backwards validation
     * 
     * @param transaction Transaction to be validated
     * @return a flag indicating whether validation was successful
     */
    public boolean validateTransaction(Transaction transaction)
        {
            int transactionNumber;
            int lastCommittedTransactionNumber;
            int transactionNumberIndex;
            
            ArrayList<Integer> readSet = transaction.getReadSet();
            HashMap<Integer, Integer> checkedTransactionWriteSet;
            Iterator<Integer> readSetIterator;
            
            Transaction checkedTransaction;
            Integer checkedAccount;
            
            // assign a transaction number to this transaction
            // ...            

            // run through all overlapping transactions
            // use transactionNumberIndex as the transaction number of overlapping transaction currently looked at
            for (/* ... */)
            {
                // get transaction with transaction number transactionNumberIndex
                // from committedTransactions -> checkedTransaction
                // ...
                
                // make sure transaction with transactionNumberIndex was not aborted before
                if(checkedTransaction != null)
                {
                    // check our own read set against the write set of the checkedTransaction
                    // ...
                }
            }
                        
            transaction.log("[TransactionManager.validateTransaction] Transaction #" + transaction.getTransactionID() + " successfully validated");
            return true;
        }
     
    
    /**
     * Writes the write set of a transaction into the operational data
     * 
     * @param transaction Transaction to be written
     */
    public void writeTransaction(Transaction transaction)
    {
        HashMap<Integer, Integer> transactionWriteSet = transaction.getWriteSet();
        int account;
        int balance;
         
        // get all the entries of this write set
        for (/* all entries in write set */) {
            // ....
                            
            transaction.log("[TransactionManager.writeTransaction] Transaction #" + transaction.getTransactionID() + " written");                    
        }        
    }

    
    /**
     * Objects of this inner class run transactions, one thread runs one transaction
     * on behalf of a client
     */
    public class TransactionManagerWorker extends Thread 
    {
        // networking communication related fields
        Socket client = null;
        ObjectInputStream readFromNet = null;
        ObjectOutputStream writeToNet = null;
        Message message = null;

        // transaction related fields
        Transaction transaction = null;
        int accountNumber       = 0;
        int balance             = 0;

        // flag for jumping out of while loop after this transaction closed
        boolean keepgoing = true;

        
        // the constructor just opens up the network channels
        private TransactionManagerWorker(Socket client) 
        {
            this.client = client;
            // setting up object streams
            try 
            {
                readFromNet = new ObjectInputStream(client.getInputStream());
                writeToNet  = new ObjectOutputStream(client.getOutputStream());
            }
            catch (IOException e)
            {
                System.err.println("[TransactionManagerWorker.run] Failed to open object streams");
                System.exit(1);
            }
        }

        
        @Override
        public void run() 
        {
            // loop is left when transaction closes
            while (keepgoing) 
            {
                // reading message
                try 
                {
                    message = (Message) readFromNet.readObject();
                } 
                catch (IOException | ClassNotFoundException e) 
                {
                    System.out.println("[TransactionManagerWorker.run] Message could not be read from object stream.");
                    System.exit(1);
                }

                // processing message
                switch (message.getType()) 
                {

                    // -------------------------------------------------------------------------------------------
                    case OPEN_TRANSACTION:
                    // -------------------------------------------------------------------------------------------

                        // synchronize on the runningTransactions
                        synchronized (runningTransactions) 
                        {
                            // create new transaction and assign a new transaction ID
                            // most importantly, pass in the last assigned transaction number
                            // ...
                            
                            // add the new transaction to ArrayList runningTransactions
                            // ...
                        }

                        // write back transactionID to client
                        // ...

                        // add log
                        transaction.log("[TransactionManagerWorker.run] " + OPEN_COLOR + "OPEN_TRANSACTION" + RESET_COLOR + " #" + transaction.getTransactionID());

                        break;

                        
                    // -------------------------------------------------------------------------------------------
                    case CLOSE_TRANSACTION:
                    // -------------------------------------------------------------------------------------------
                        
                        synchronized (runningTransactions)
                        {
                            // remove transaction from ArrayList runningTransactions
                            // ...

                            // the BIG thing, we enter validation phase and, if successful, the update phase
                            if (validateTransaction(transaction))
                            {
                                // add this transaction to committedTransactions
                                // important step! information used in other transactions' validations, if they overlap with this one
                                // ...
                                
                                // this is the update phase ... write data to operational data in one go
                                // ...
                                
                                // tell client that transaction committed
                                // ...      

                                // add log committed
                                transaction.log("[TransactionManagerWorker.run] " + COMMIT_COLOR + "CLOSE_TRANSACTION"+ RESET_COLOR + " #" + transaction.getTransactionID() + " - COMMITTED");
                            }
                            else
                            {
                                // validation failed, abort this transaction
                                // there is not anything that is done explicitly, aborting is essentially doing nothing
                                abortedTransactions.add(transaction);

                                // tell client that transaction was aborted
                                // ...

                                // add log aborted
                                transaction.log("[TransactionManagerWorker.run] " + ABORT_COLOR + "CLOSE_TRANSACTION"+ RESET_COLOR + " #" + transaction.getTransactionID() + " - ABORTED");
                            }
                        }

                        // regardless whether the transaction committed or aborted, shut down network connections
                        // ...

                        // finally print out the transaction's log
                        if (TransactionServer.transactionView) 
                        {
                            System.out.println(transaction.getLog());
                        }

                        break;

                        
                    // -------------------------------------------------------------------------------------------
                    case READ_REQUEST:
                    // -------------------------------------------------------------------------------------------
 
                        // get account number
                        // ...
                        
                        // add log pre read
                        transaction.log("[TransactionManagerWorker.run] "+ READ_COLOR + "READ_REQUEST" + RESET_COLOR + " >>>>>>>>>>>>>>>>>>>> account #" + accountNumber);

                        // read balance from account
                        // ======>
                        balance = transaction.read(accountNumber);
                        // <======

                        // confirm read to client
                        // ...

                        // add log post read
                        transaction.log("[TransactionManagerWorker.run] "+ READ_COLOR + "READ_REQUEST" + RESET_COLOR + " <<<<<<<<<<<<<<<<<<<< account #" + accountNumber + ", balance $" + balance);

                        break;

                        
                    // -------------------------------------------------------------------------------------------
                    case WRITE_REQUEST:
                    // -------------------------------------------------------------------------------------------

                        // get the message content: account number and balance to write
                        // ....
                        
                        // add log pre write
                        transaction.log("[TransactionManagerWorker.run] " + WRITE_COLOR + "WRITE_REQUEST" + RESET_COLOR + " >>>>>>>>>>>>>>>>>>> account #" + accountNumber + ", balance to write $" + balance);
                        
                        /// do the write 
                        // ======>
                        transaction.write(accountNumber, balance);
                        // <======

                        // write back old balance to client
                        // ....

                        // add log post write
                        transaction.log("[TransactionManagerWorker.run] " + WRITE_COLOR + "WRITE_REQUEST" + RESET_COLOR + " <<<<<<<<<<<<<<<<<<<< account #" + accountNumber + ", wrote $" + balance);

                        break;

                        
                    // -------------------------------------------------------------------------------------------
                    case ABORT_TRANSACTION:
                    // -------------------------------------------------------------------------------------------
                        
                        // this is a client side abort! ignore ...
                        synchronized (runningTransactions) 
                        {
                            // remove transaction from runningTransactions
                            runningTransactions.remove(transaction);
                        }
                        
                        // shut down
                        try {
                            readFromNet.close();
                            writeToNet.close();
                            client.close();
                            
                            keepgoing = false; //stop loop
                        } catch (IOException e) {
                            System.out.println("[TransactionManagerWorker.run] ABORT_TRANSACTION - Error when closing connection to client");
                        }

                        // add log abort
                        transaction.log("[TransactionManagerWorker.run] " + ABORT_COLOR + "ABORT_TRANSACTION" + RESET_COLOR + " #" + transaction.getTransactionID() + " - ABORTED by client");

                        // final printout of all the transaction's logs
                        if (TransactionServer.transactionView) {
                            System.out.println(transaction.getLog());
                        }
                       
                        break;
                        
                    // -------------------------------------------------------------------------------------------
                    case SHUTDOWN:
                    // -------------------------------------------------------------------------------------------

                        TransactionServer.shutDown();
                        
                        // bail out
                        keepgoing = false;
                        
                        break;                       

                        
                    // -------------------------------------------------------------------------------------------
                    default: // message not implemented
                    // -------------------------------------------------------------------------------------------

                        System.out.println("[TransactionManagerWorker.run] Warning: Message type not implemented");
                }
            }
        }
    }
}
