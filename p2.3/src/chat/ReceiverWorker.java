package chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Node;

import message.Message;
import message.MessageTypes;


public class ReceiverWorker extends Thread {
    Socket serverConnection = null;
    Socket nodeConnection = null;

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
                if (ChatClient.participants.remove(leavingParticipantInfo))
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
                participantInfo = (NodeInfo) message.getContent();

                // Send INFO message back
                try
                {
                    // open socket to one chat client at a time
                    nodeConnection = new Socket(participantInfo.address, participantInfo.port);

                    // open object streams
                    writeToNet = new ObjectOutputStream(nodeConnection.getOutputStream());
                    readFromNet = new ObjectInputStream(nodeConnection.getInputStream());

                    // Write INFO message
                    writeToNet.writeObject(new Message(MessageTypes.INFO, ChatClient.participants));

                    // close node connection
                    nodeConnection.close();
                }
                catch (Exception err)
                {
                    Logger.getLogger(ReceiverWorker.class.getName()).log(Level.SEVERE, 
                                        "[ReceiverWorker].run Could not open socket to node or send a message", 
                                        err);
                }

                // add this client to list of participants
                ChatClient.participants.add(participantInfo);

                // show who joined
                System.out.print(participantInfo.getName() + " joined. All current participants: ");

                // print out all current participants
                participantsIterator = ChatClient.participants.iterator();
                while(participantsIterator.hasNext())
                {
                    participantInfo = participantsIterator.next();
                    System.out.print(participantInfo.name + " ");
                }
                System.out.println();

                break;
            
            case MessageTypes.JOINED:
                // read participant's NodeInfo
                participantInfo = (NodeInfo) message.getContent();

                // add new node to participants list
                ChatClient.participants.add(participantInfo);

                // print out newly joined node
                System.out.println(participantInfo.getName() + " joined. All current participants: ");

                // printing out all participants
                participantsIterator = ChatClient.participants.iterator();
                while(participantsIterator.hasNext())
                {
                    participantInfo = participantsIterator.next();
                    System.out.print(participantInfo.getName() + " ");
                }
                System.out.println();

                break;
            
            case MessageTypes.INFO:
                // init list for received participants
                List<NodeInfo> newParticipants = (List<NodeInfo>) message.getContent();

                // loop through all received participants and add to participants
                participantsIterator = newParticipants.iterator();
                while(participantsIterator.hasNext())
                {
                    // get next participant
                    participantInfo = participantsIterator.next();
                    // add new participant to participants list
                    ChatClient.participants.add(participantInfo);
                }
                // add original node to participants list
                ChatClient.participants.add(ChatClient.knownNodeInfo);
                
                break;
            default:
        }
    }
}
