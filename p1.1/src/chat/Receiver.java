package chat;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Receiver extends Thread{
    static ServerSocket receiverSocket = null;
    static String userName = null;

    //constructor
    public Receiver()
    {
        try
        {
            receiverSocket = new ServerSocket(ChatClient.myNodeInfo.getPort());
        }
        catch (IOException ex)
        {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, "Creating Receiver socket Failed", ex);
        }
    }

    @Override
    public void run()
    {
        //server loop
        while(true)
        {
            try
            {
                (new ReceiverWorker(receiverSocket.accept())).start();
            }
            catch(IOException e)
            {
                System.err.println("Receiver run: error acception client");
            }
        }
    }
}
