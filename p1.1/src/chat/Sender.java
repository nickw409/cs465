package chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import java.util.Scanner;

import java.util.logging.Level;
import java.util.logging.Logger;

import message.Message;
import message.MessageTypes;

public class Sender extends Thread implements MessageTypes{
    
    Socket serverConnection = null;
    Scanner userInput = new Scanner(System.in);
    String inputLine = null;
    boolean hasJoined;

    //constructor
    public Sender()
    {
        userInput = new Scanner(System.in);
        hasJoined = false;
    }

    @Override
    public void run()
    {
        ObjectOutputStream writeToNet;
        ObjectInputStream readFromNet;

        while(true)
        {
            //get user input
            inputLine = userInput.nextLine();

            //
            if( inputLine.startsWith("JOIN"))
            {
                //ignore, if we already joined a chat
                if( hasJoined == true)
                {
                    System.err.println("You have already joined a chat...");
                    continue;
                }

                String[] connectivityInfo = inputLine.split("[ ]+");

                try
                {
                    ChatClient.serverNodeInfo = new NodeInfo(connectivityInfo[1], Integer.parseInt(connectivityInfo[1]));
                }
                catch (ArrayIndexOutOfBoundsException ex)
                {

                }

                if(ChatClient.serverNodeInfo == null)
                {
                    System.err.println("[Sender].run No server connectivity information");
                    continue;
                }

                try{
                    serverConnection = new Socket(ChatClient.serverNodeInfo.getAddress(), ChatClient.serverNodeInfo.getPort());

                    readFromNet = new ObjectInputStream(serverConnection.getInputStream());
                    writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());

                    writeToNet.writeObject(new Message(JOIN, ChatClient.myNodeInfo));
                    
                    serverConnection.close();
                }
                catch( IOException ex)
                {
                    Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, "Error connectiong to server or opening or writing/reading object streams otr closing connection", ex);
                    continue;
                }

                hasJoined = true;
                System.out.println("Joined Chat ...");
            }
            else if (inputLine.startsWith("LEAVE"))
            {
                if (hasJoined == false)
                {
                    System.err.println("You have not joined a chat yet ...");
                    continue;
                }

                try
                {
                    serverConnection = new Socket(ChatClient.serverNodeInfo.getAddress(), ChatClient.serverNodeInfo.getPort());

                    readFromNet = new ObjectInputStream(serverConnection.getInputStream());
                    writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());

                    writeToNet.writeObject(new Message(LEAVE, ChatClient.myNodeInfo));

                    serverConnection.close();
                }
                catch (IOException ex)
                {
                    Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, "Error connection to server streams or closing connection", ex);
                    continue;
                }

                //we are out
                hasJoined = false;

                System.out.println("Left chat ...");
            }
            else if(inputLine.startsWith("SHUTDOWN ALL"))
            {
                if( hasJoined == false)
                {
                    System.err.println("To shut down the whole chat, you first need to join ...");
                    continue;
                }
                
                try
                {
                    serverConnection = new Socket(ChatClient.serverNodeInfo.getAddress(), ChatClient.serverNodeInfo.getPort());

                    readFromNet = new ObjectInputStream(serverConnection.getInputStream());
                    writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());

                    writeToNet.writeObject(new Message(SHUTDOWN_ALL, ChatClient.myNodeInfo));

                    serverConnection.close();

                    System.out.println("Left chat ...");
                }
                catch (IOException ex)
                {
                    Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, "Error in sending shutdown msg", ex);
                    continue;
                }
                
            }
            else if(inputLine.startsWith("SHUTDOWN"))
            {
                if( hasJoined == true)
                {
                    try
                    {
                        serverConnection = new Socket(ChatClient.serverNodeInfo.getAddress(), ChatClient.serverNodeInfo.getPort());

                        readFromNet = new ObjectInputStream(serverConnection.getInputStream());
                        writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());

                        writeToNet.writeObject(new Message(SHUTDOWN, ChatClient.myNodeInfo));

                        serverConnection.close();
                    }
                    catch (IOException ex)
                    {
                        Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, "Error in sending shutdown all msg", ex);
                        continue;
                    }
                }
                System.out.println("Exiting ...\n");
                System.exit(0);
            }
            else
            {
                if( hasJoined == false)
                {
                    System.err.println("You have not joined a chat yet ...");
                    continue;
                }
                
                try
                {
                    serverConnection = new Socket(ChatClient.serverNodeInfo.getAddress(), ChatClient.serverNodeInfo.getPort());

                    readFromNet = new ObjectInputStream(serverConnection.getInputStream());
                    writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());

                    writeToNet.writeObject(new Message(NOTE, ChatClient.myNodeInfo.getName() + ":" + inputLine));

                    serverConnection.close();

                    System.out.println("Message sent ...");
                }
                catch (IOException ex)
                {
                    Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, "Error in sending msg", ex);
                    continue;
                }
                
            }
            
        }
    }  
}
