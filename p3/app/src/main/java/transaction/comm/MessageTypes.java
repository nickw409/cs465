
package transaction.comm;

/**
 * Interface [MessageTypes] Defines the different message types used in the application.
 * Any entity using objects of class Message needs to implement this interface.
 * 
 * @author Dr.-Ing. Wolf-Dieter Otte
 */
public interface MessageTypes {
    
    // "coordinator" interface, see book
    // messages sent from client, implemented by coordinator
    public static final int OPEN_TRANSACTION  = 1;
    public static final int CLOSE_TRANSACTION = 2; // returns TRANSACTION_COMMITTED or TRANSACTION_ABORTED, see below
    public static final int ABORT_TRANSACTION = 3; // not implemented
    
    public static final int READ_REQUEST  = 4;
    public static final int WRITE_REQUEST = 5;
    
    // Flags sent from server in response to a client's CLOSE_TRANSACTION    
    public static final int TRANSACTION_COMMITTED = 6;
    public static final int TRANSACTION_ABORTED   = 7;
    
    // message sent from the client to the server to signal shutdown
    public static final int SHUTDOWN   = 8;
}
