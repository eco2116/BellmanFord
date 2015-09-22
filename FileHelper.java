/**
 * Created by evanoconnor on 5/9/15.
 */

import java.io.*;
import java.net.DatagramSocket;
import java.util.Arrays;

public class FileHelper {

    private File file;
    private String fileName;
    private Node node;
    private DistanceVector dv;
    private FileInputStream fis;
    private static final int MAX_SIZE = 2048;
    private Client client;

    public FileHelper() {

    }

    public FileHelper(Node n, File f, String fileName, Client client) {
        this.client = client;
        this.dv = client.getDistanceVector();
        this.node = n;
        this.file = f;
        this.fileName = fileName;
        try {
            this.fis = new FileInputStream(this.file);
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean sendFragments() {

        // Verify next hop is available
        Node nextHop;
        if((nextHop = dv.getConnections().get(this.node).getNode()) == null) {
            return false;
        }
        System.out.println(nextHop);
        byte[] buffer = new byte[MAX_SIZE];
        int lengthRead;
        try {

            // Split up file into fragments
            while ((lengthRead = this.fis.read(buffer, 0, MAX_SIZE)) > -1) {

                ApplicationMessage fileMessage = new ApplicationMessage(Message.FILE_TRANSFER, fileName);
                fileMessage.setDestination(this.node);

                if(lengthRead > MAX_SIZE) {

                    // Copy fragments of the file
                    fileMessage.setFragment(buffer);

                } else {

                    // Copy final partially filled buffer
                    byte[] finalPacket = Arrays.copyOf(buffer, lengthRead);
                    fileMessage.setFragment(finalPacket);
                }

                // Send fragment to next hop
                DataPacket dp = new DataPacket(client.getSelf(), fileMessage);
                client.sendTo(client.getSendSock(), dp, nextHop.getIp(), nextHop.getPort());
            }

            // Indicate End of File/ Sucessful transmission
            ApplicationMessage am = new ApplicationMessage(ApplicationMessage.FILE_TRANSFER, fileName);
            am.setDestination(node);
            am.setEOF(true);
            DataPacket dp = new DataPacket(client.getSelf(), am);
            client.sendTo(client.getSendSock(), dp, nextHop.getIp(), nextHop.getPort());


        } catch(IOException e) {
            e.printStackTrace();
        }


        return true;
    }

    public boolean forward(ApplicationMessage am, DistanceVector distanceVector, DatagramSocket ds) {
        Node nextHop;

        if((nextHop = distanceVector.getConnections().get(am.getDestination()).getNode()) == null) {
            return false;
        } else {
            // Send message containing fragment to next hop
            DataPacket dp = new DataPacket(distanceVector.getOwnerNode(), am);
            Client client = new Client();
            client.sendTo(ds, dp, nextHop.getIp(), nextHop.getPort());
            System.out.println("Next hop: " + nextHop.getIp() + ":" + nextHop.getPort());
        }
        return true;
    }

    public boolean transfer(ApplicationMessage am, DistanceVector distanceVector, DatagramSocket ds) {
        //System.out.println("Packet received");
        //System.out.println("Source: "+ distanceVector.getOwnerNode().getIp() + ":" + distanceVector.getOwnerNode().getPort());
        Node destination = am.getDestination();
        System.out.println("Destination: " + destination.getIp() + ":" + destination.getPort());

        if(distanceVector.getOwnerNode().equals(destination)) {
            try {
                File file = new File(am.getText());

                // Got the last fragment of the file
                if (am.getEOF()) {
                    System.out.print("Successfully retrieved the file!\n>");
                    return true;
                }

                // Write fragment to file output stream
                FileOutputStream fos = new FileOutputStream(file, true);
                fos.write(am.getFragment());

            } catch(FileNotFoundException f) {
                System.out.println("File was not found...");
                return false;
            } catch(IOException e) {
                System.out.println("Encountered an IO Exception...");
                return false;
            }
        } else {
            // Try forwarding fragment other nodes
            if(!forward(am, distanceVector, ds)) {

                // No path found-failure
                System.out.println("No path to send your file. ");
                return false;
            }
        }
        return true;
    }

}
