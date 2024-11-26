package appserver.satellite;

import appserver.job.Job;
import appserver.comm.ConnectivityInfo;
import appserver.job.UnknownToolException;
import appserver.comm.Message;
import static appserver.comm.MessageTypes.JOB_REQUEST;
import static appserver.comm.MessageTypes.REGISTER_SATELLITE;
import appserver.job.Tool;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.PropertyHandler;

/**
 * Class [Satellite] Instances of this class represent computing nodes that
 * execute jobs by
 * calling the callback method of tool implementation, loading the tool's code
 * dynamically over a network
 * or locally from the cache, if a tool got executed before.
 *
 * @author Dr.-Ing. Wolf-Dieter Otte
 */
public class Satellite extends Thread {

    private ConnectivityInfo satelliteInfo = new ConnectivityInfo();
    private ConnectivityInfo serverInfo = new ConnectivityInfo();
    private HTTPClassLoader classLoader = null;
    private Hashtable<String, Tool> toolsCache = null;

    public Satellite(String satellitePropertiesFile, String classLoaderPropertiesFile, String serverPropertiesFile) {
        try {
            // read this satellite's properties and populate satelliteInfo object
            PropertyHandler satelliteProps = new PropertyHandler(satellitePropertiesFile);
            satelliteInfo.setName(satelliteProps.getProperty("name"));
            satelliteInfo.setHost(satelliteProps.getProperty("host"));
            satelliteInfo.setPort(Integer.parseInt(satelliteProps.getProperty("port")));

            // read properties of the application server and populate serverInfo object
            PropertyHandler serverProps = new PropertyHandler(serverPropertiesFile);
            serverInfo.setHost(serverProps.getProperty("host"));
            serverInfo.setPort(Integer.parseInt(serverProps.getProperty("port")));

            // read properties of the code server and create class loader
            PropertyHandler classLoaderProps = new PropertyHandler(classLoaderPropertiesFile);
            classLoader = new HTTPClassLoader(classLoaderProps.getProperty("codebase"));

            // create tools cache
            toolsCache = new Hashtable<>();

        } catch (IOException e) {
            Logger.getLogger(Satellite.class.getName()).log(Level.SEVERE, "Error reading properties files", e);
        }
    }

    @Override
    public void run() {
        try {
            // register this satellite with the SatelliteManager on the server
            Socket serverSocket = new Socket(serverInfo.getHost(), serverInfo.getPort());
            ObjectOutputStream out = new ObjectOutputStream(serverSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(serverSocket.getInputStream());

            // send registration message
            Message registerMessage = new Message(REGISTER_SATELLITE, satelliteInfo);
            out.writeObject(registerMessage);
            out.flush();

            // close connection
            serverSocket.close();

            // create server socket
            ServerSocket server = new ServerSocket(satelliteInfo.getPort());
            System.out.println("[Satellite] Running on port: " + satelliteInfo.getPort());

            // start taking job requests in a server loop
            while (true) {
                Socket jobRequest = server.accept();
                new SatelliteThread(jobRequest, this).start();
            }

        } catch (IOException e) {
            Logger.getLogger(Satellite.class.getName()).log(Level.SEVERE, "Error in Satellite run", e);
        }
    }

    // inner helper class that is instantiated in above server loop and processes
    // single job requests
    private class SatelliteThread extends Thread {

        Satellite satellite;
        Socket jobRequest;
        ObjectInputStream readFromNet;
        ObjectOutputStream writeToNet;
        Message message;

        SatelliteThread(Socket jobRequest, Satellite satellite) {
            this.jobRequest = jobRequest;
            this.satellite = satellite;
        }

        @Override
        public void run() {
            try {
                // setting up object streams
                readFromNet = new ObjectInputStream(jobRequest.getInputStream());
                writeToNet = new ObjectOutputStream(jobRequest.getOutputStream());

                // reading message
                message = (Message) readFromNet.readObject();

                switch (message.getType()) {
                    case JOB_REQUEST:
                        // processing job request
                        Job job = (Job) message.getContent();
                        String toolName = job.getToolName();
                        Object parameters = job.getParameters();

                        // get tool object
                        Tool tool = satellite.getToolObject(toolName);
                        Object result = tool.go(parameters);

                        // send result back
                        writeToNet.writeObject(result);
                        writeToNet.flush();
                        break;

                    default:
                        System.err.println("[SatelliteThread.run] Warning: Message type not implemented");
                }

            } catch (Exception e) {
                Logger.getLogger(SatelliteThread.class.getName()).log(Level.SEVERE, "Error processing job request", e);
            } finally {
                try {
                    jobRequest.close();
                } catch (IOException e) {
                    Logger.getLogger(SatelliteThread.class.getName()).log(Level.SEVERE,
                            "Error closing job request socket", e);
                }
            }
        }
    }

    /**
     * Aux method to get a tool object, given the fully qualified class string
     * If the tool has been used before, it is returned immediately out of the
     * cache,
     * otherwise it is loaded dynamically
     */
    public Tool getToolObject(String toolClassString)
            throws UnknownToolException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Tool toolObject = toolsCache.get(toolClassString);

        if (toolObject == null) {
            Class<?> toolClass = classLoader.loadClass(toolClassString);
            toolObject = (Tool) toolClass.getDeclaredConstructor().newInstance();
            toolsCache.put(toolClassString, toolObject);
        }

        return toolObject;
    }

    public static void main(String[] args) {
        // start the satellite
        Satellite satellite = new Satellite(args[0], args[1], args[2]);
        satellite.start();
    }
}
