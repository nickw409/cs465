package appserver.satellite;

import appserver.job.Job;
import appserver.comm.ConnectivityInfo;
import appserver.job.UnknownToolException;
import appserver.comm.Message;
import static appserver.comm.MessageTypes.JOB_REQUEST;
import appserver.job.Tool;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private ConnectivityInfo classLoaderInfo = new ConnectivityInfo();
    private HTTPClassLoader classLoader = null;
    private Hashtable<String, Tool> toolsCache = null;

    public Satellite(String satellitePropertiesFile, String classLoaderPropertiesFile, String serverPropertiesFile) {
        try {
            // read this satellite's properties and populate satelliteInfo object
            PropertyHandler satelliteProps = new PropertyHandler(satellitePropertiesFile);
            satelliteInfo.setName(satelliteProps.getProperty("NAME"));
            //satelliteInfo.setHost(satelliteProps.getProperty("host"));
            satelliteInfo.setPort(Integer.parseInt(satelliteProps.getProperty("PORT")));

            // read properties of the application server and populate serverInfo object
            PropertyHandler serverProps = new PropertyHandler(serverPropertiesFile);
            serverInfo.setHost(serverProps.getProperty("HOST"));
            serverInfo.setPort(Integer.parseInt(serverProps.getProperty("PORT")));

            // read properties of the code server and create class loader
            PropertyHandler classLoaderProps = new PropertyHandler(classLoaderPropertiesFile);
            classLoaderInfo.setHost(classLoaderProps.getProperty("HOST"));
            classLoaderInfo.setPort(Integer.parseInt(classLoaderProps.getProperty("PORT")));
            classLoader = new HTTPClassLoader(classLoaderInfo.getHost(), classLoaderInfo.getPort());

            // create tools cache
            toolsCache = new Hashtable<String, Tool>();

        } catch (IOException e) {
            Logger.getLogger(Satellite.class.getName()).log(Level.SEVERE, "Error reading properties files", e);
        }
    }

    @Override
    public void run() {
        try {
            // register this satellite with the SatelliteManager on the server
            
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

        try {
            if (toolObject == null) {
                // Loading tool class specified
                Class<?> toolClass = classLoader.findClass(toolClassString);
                // Creating a new object of the tool class
                toolObject = (Tool) toolClass.getDeclaredConstructor().newInstance();
                // Adding object to cache for quick loading
                toolsCache.put(toolClassString, toolObject);
            }
        } catch (Exception ex) {
            Logger.getLogger(SatelliteThread.class.getName()).log(Level.SEVERE, "Error getting tool object", ex);
        }

        return toolObject;
    }

    public static void main(String[] args) {
        // start the satellite
        Satellite satellite = new Satellite(args[0], args[1], args[2]);
        satellite.start();
    }
}
