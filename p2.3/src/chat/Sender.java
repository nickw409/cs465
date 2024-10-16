package chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.Scanner;

import java.util.logging.Level;
import java.util.logging.Logger;

import message.Message;
import message.MessageTypes;

public class Sender extends Thread implements MessageTypes {

    Socket serverConnection = null;
    Scanner userInput = new Scanner(System.in);
    String inputLine = null;
    boolean hasJoined;

    // constructor
    public Sender() {
        userInput = new Scanner(System.in);
        hasJoined = false;
    }

    @Override
    public void run() {
        ObjectOutputStream writeToNet;
        ObjectInputStream readFromNet;
        NodeInfo participantInfo = null;
        Iterator<NodeInfo> participantsIterator;
        Socket chatConnection = null;

        while (true) {
            // get user input
            inputLine = userInput.nextLine();

            //
            if (inputLine.startsWith("JOIN")) {
                // ignore, if we already joined a chat
                if (hasJoined == true) {
                    System.err.println("You have already joined a chat...");
                    continue;
                }

                String[] connectivityInfo = inputLine.split("[ ]+");

                try {
                    ChatClient.knownNodeInfo = new NodeInfo(connectivityInfo[1], Integer.parseInt(connectivityInfo[1]));
                } catch (ArrayIndexOutOfBoundsException ex) {

                }

                if (ChatClient.knownNodeInfo == null) {
                    System.err.println("[Sender].run No server connectivity information");
                    continue;
                }

                try {
                    serverConnection = new Socket(ChatClient.knownNodeInfo.getAddress(),
                            ChatClient.knownNodeInfo.getPort());

                    readFromNet = new ObjectInputStream(serverConnection.getInputStream());
                    writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());

                    writeToNet.writeObject(new Message(JOIN, ChatClient.myNodeInfo));

                    serverConnection.close();
                } catch (IOException ex) {
                    Logger.getLogger(Sender.class.getName()).log(Level.SEVERE,
                            "Error connectiong to server or opening or writing/reading object streams otr closing connection",
                            ex);
                    continue;
                }

                while (ChatClient.participants == null) {
                }

                participantsIterator = ChatClient.participants.iterator();

                while (participantsIterator.hasNext()) {

                    // get next participant
                    participantInfo = participantsIterator.next();

                    try {
                        // open socket to one chat client at a time
                        chatConnection = new Socket(participantInfo.address, participantInfo.port);

                        // open object streams
                        writeToNet = new ObjectOutputStream(chatConnection.getOutputStream());
                        readFromNet = new ObjectInputStream(chatConnection.getInputStream());
                        writeToNet.writeObject(new Message(JOINED, ChatClient.myNodeInfo));
                        // close connection to this client
                        chatConnection.close();
                    } catch (IOException ex) {
                        Logger.getLogger(Sender.class.getName()).log(Level.SEVERE,
                                "[Sender].run JOINED Attempt could not open socket to client or send a message",
                                ex);
                    }
                }

                hasJoined = true;
                System.out.println("Joined Chat ...");
            } else if (inputLine.startsWith("LEAVE")) {
                if (hasJoined == false) {
                    System.err.println("You have not joined a chat yet ...");
                    continue;
                }

                participantsIterator = ChatClient.participants.iterator();

                while (participantsIterator.hasNext()) {

                    // get next participant
                    participantInfo = participantsIterator.next();

                    try {
                        // open socket to one chat client at a time
                        chatConnection = new Socket(participantInfo.address, participantInfo.port);

                        // open object streams
                        writeToNet = new ObjectOutputStream(chatConnection.getOutputStream());
                        readFromNet = new ObjectInputStream(chatConnection.getInputStream());
                        writeToNet.writeObject(new Message(LEAVE, ChatClient.myNodeInfo));
                        // close connection to this client
                        chatConnection.close();
                    } catch (IOException ex) {
                        Logger.getLogger(Sender.class.getName()).log(Level.SEVERE,
                                "[Sender].run LEAVE Attempt could not open socket to client or send a message",
                                ex);
                    }
                }

                // we are out
                hasJoined = false;

                System.out.println("Left chat ...");
            } else if (inputLine.startsWith("SHUTDOWN ALL")) {
                if (hasJoined == false) {
                    System.err.println("To shut down the whole chat, you first need to join ...");
                    continue;
                }

                participantsIterator = ChatClient.participants.iterator();

                while (participantsIterator.hasNext()) {

                    // get next participant
                    participantInfo = participantsIterator.next();

                    try {
                        // open socket to one chat client at a time
                        chatConnection = new Socket(participantInfo.address, participantInfo.port);

                        // open object streams
                        writeToNet = new ObjectOutputStream(chatConnection.getOutputStream());
                        readFromNet = new ObjectInputStream(chatConnection.getInputStream());
                        writeToNet.writeObject(new Message(SHUTDOWN, ChatClient.myNodeInfo));
                        // close connection to this client
                        chatConnection.close();
                    } catch (IOException ex) {
                        Logger.getLogger(Sender.class.getName()).log(Level.SEVERE,
                                "[Sender].run SHUTDOWN_ALL Attempt could not open socket to client or send a message",
                                ex);
                    }
                }
                System.out.println("Exiting ...\n");
                System.exit(0);

            } else if (inputLine.startsWith("SHUTDOWN")) {
                if (hasJoined == true) {

                    participantsIterator = ChatClient.participants.iterator();

                    while (participantsIterator.hasNext()) {

                        // get next participant
                        participantInfo = participantsIterator.next();

                        try {
                            // open socket to one chat client at a time
                            chatConnection = new Socket(participantInfo.address, participantInfo.port);

                            // open object streams
                            writeToNet = new ObjectOutputStream(chatConnection.getOutputStream());
                            readFromNet = new ObjectInputStream(chatConnection.getInputStream());
                            writeToNet.writeObject(new Message(LEAVE, ChatClient.myNodeInfo));
                            // close connection to this client
                            chatConnection.close();
                        } catch (IOException ex) {
                            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE,
                                    "[Sender].run SHUTDOWN Attempt could not open socket to client or send a message",
                                    ex);
                        }
                    }
                }
                System.out.println("Exiting ...\n");
                System.exit(0);
            } else {
                if (hasJoined == false) {
                    System.err.println("You have not joined a chat yet ...");
                    continue;
                }

                // run through all participants and send the note to each single one
                participantsIterator = ChatClient.participants.iterator();
                while (participantsIterator.hasNext()) {
                    // get next participant
                    participantInfo = participantsIterator.next();

                    try {
                        // open socket to one chat client at a time
                        chatConnection = new Socket(participantInfo.address, participantInfo.port);

                        writeToNet = new ObjectOutputStream(chatConnection.getOutputStream());
                        readFromNet = new ObjectInputStream(chatConnection.getInputStream());

                        writeToNet.writeObject(new Message(NOTE, ChatClient.myNodeInfo.getName() + ":" + inputLine));

                        System.out.println("Message sent ...");
                        // close connection to this client
                        chatConnection.close();
                    } catch (IOException ex) {
                        Logger.getLogger(Sender.class.getName()).log(Level.SEVERE,
                                "[ChatServerWorker].run Could not open socket to client or send a message",
                                ex);
                    }
                }
            }
        }
    }
}
