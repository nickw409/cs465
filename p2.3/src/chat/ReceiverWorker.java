package chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;
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

        NodeInfo participantInfo = null;
        Iterator <NodeInfo> participantsIterator;

        //open object streams
        try
        {
            writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());
            readFromNet = new ObjectInputStream(serverConnection.getInputStream());

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
            
            case MessageTypes.LEAVE:
                // remove this participant's info
                NodeInfo leavingParticipantInfo = (NodeInfo) message.getContent();
                if (ChatServer.participants.remove(leavingParticipantInfo))
                {
                    System.err.println(leavingParticipantInfo.getName() + " removed");
                }
                else
                {
                    System.err.println(leavingParticipantInfo.getName() + " not found");
                }
                
                // show who left
                System.out.print(leavingParticipantInfo.getName() + " left. Remaining participants: ");

                // print out all remaining participants
                participantsIterator = ChatClient.participants.iterator();
                while(participantsIterator.hasNext())
                {
                    participantInfo = participantsIterator.next();
                    System.out.print(participantInfo.name + " ");
                }
                System.out.println();

                break;
            
            case MessageTypes.JOIN:
                // read participant's NodeInfo
                NodeInfo joiningParticipantNodeInfo = (NodeInfo) message.getContent();

                // add this client to list of participants
                ChatServer.participants.add(joiningParticipantNodeInfo);

                // show who joined
                System.out.print(joiningParticipantNodeInfo.getName() + " joined. All current participants: ");

                // print out all current participants
                participantsIterator = ChatServer.participants.iterator();
                while(participantsIterator.hasNext())
                {
                    participantInfo = participantsIterator.next();
                    System.out.print(participantInfo.name + " ");
                }
                System.out.println();

                break;
            
            case MessageTypes.JOINED:
                break;
            
            case MessageTypes.INFO:
                break;
            default:
        }
    }
}
