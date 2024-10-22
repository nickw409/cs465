package transaction.server.account;

import java.util.ArrayList;


/**
 *
 * @author wolfdieterotte
 */
public class AccountManager
{
    
    private static ArrayList<Account> accounts;
    static int numberAccounts;
    static int initialBalance;
    
    public AccountManager(int numberAccounts, int initialBalance) 
    {
        accounts = new ArrayList();
        AccountManager.numberAccounts = numberAccounts;
        AccountManager.initialBalance = initialBalance;
        int accountIndex;
        
        for (accountIndex = 0; accountIndex < numberAccounts; accountIndex++) 
        {
            accounts.add(new Account(accountIndex, initialBalance));
        }
    }
    
    
    public Account getAccount(int accountNumber) 
    {
        return accounts.get(accountNumber);
    }
    
    
    public ArrayList<Account> getAccounts()
    {
        return accounts;
    }
    
    
    public int read (int accountNumber)
    {        
        return (getAccount(accountNumber))._read();
    }
    
    
    public void write (int accountNumber, int balance) 
    {
        (getAccount(accountNumber))._write(balance);
    }
}
