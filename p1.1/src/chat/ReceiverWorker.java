package chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;

import java.util.Scanner;

import java.util.logging.Level;
import java.util.logging.Logger;

import message.Message;
import message.MessageTypes;


public class ReceiverWorker extends Thread {
    Socket serverConnection = null;

    ObjectInputStream readFromNet = null;
    ObjectOutputStream writeToNet = null;

    Message message = null;

    public ReceiverWorker(Socket serverConnection)
    {
        this.serverConnection = serverConnection;

        //open object streams
        try
        {
            readFromNet = new ObjectInputStream(serverConnection.getInputStream());
            writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());

            message = (Message)readFromNet.readObject();
        }
        catch(IOException | ClassNotFoundException ex)
        {
            Logger.getLogger(ReceiverWorker.class.getName()).log(Level.SEVERE, "Receiver Worker: Object Streams failed");
            System.exit(1);
        }
        
        //decide course of action based on message recieved
        switch (message.getType())
        {
            case MessageTypes.SHUTDOWN:
                System.out.println("Received shutdown message from server, exiting");

                try
                {
                    serverConnection.close();
                }
                catch (IOException ex)
                {
                    
                }
                System.exit(0);
                break;

            case MessageTypes.NOTE:
                System.out.println((String) message.getContent());

                try{
                    serverConnection.close();
                }
                catch(IOException ex)
                {
                    Logger.getLogger(ReceiverWorker.class.getName()).log(Level.SEVERE, "Receiver Worker: could not get message content");
                }
                break;
            default:
        }
    }
}
