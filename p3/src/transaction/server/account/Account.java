package transaction.server.account;

/**
 *
 * @author wolfdieterotte
 */
public class Account 
{
    
    private int balance;
    private final int number;
    
    
    public Account (int number, int initialBalance) 
    {
        this.balance = initialBalance;
        this.number = number;
    }

    /**
     * Low level read operation
     * @return 
     */
    public synchronized int _read() 
    {
        return balance;
    }
    
    
    /**
     * Low level write operation
     * @param balance Write this balance to account
     */
    public void _write(int balance) 
    {
        this.balance = balance;
    }

    
    /**
     * Return the number of this account object
     * 
     * @return the account number 
     */
    public int getNumber() 
    {
        return number;
    }    
}
