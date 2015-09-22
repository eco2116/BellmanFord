/**
 * Created by fieldlens on 4/27/15.
 */

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.io.*;
import java.net.*;

public class Client {

    private Node self;

    private DistanceVector distanceVector;
    private ArrayList<DistanceVector> otherVectors;

    private HashMap<Node, NodeWeight> neighborTemp;
    private DatagramSocket recvSock;

    private DatagramSocket sendSock;
    public Date init;


    public Client() {

        this.neighborTemp = new HashMap<Node, NodeWeight>();
        this.otherVectors = new ArrayList<DistanceVector>();
        try {
            this.sendSock = new DatagramSocket();
        } catch(SocketException e) {
            e.printStackTrace();
        }
        this.init = new Date();
    }

    public static void main(String[] args)  {

        //DatagramSocket sock;

        try {
            Client client = Client.readClientFile(args[0]);

            //System.out.println("Initial DV: ");
            //System.out.println(client.distanceVector.toString());

            //System.out.println("ME: "+ client.self.toString());
            ListenThread listenThread = client.new ListenThread();
            listenThread.start();

            // Start the timer for distance vector updates
            Timeout timeout = client.new Timeout();
            timeout.start();

            // Parse commands into link messages
            while (true) {
                System.out.print(">");
                Scanner scanner = new Scanner(System.in);
                String readLine = scanner.nextLine();
                String[] splitLine = readLine.split(" ");

                if (splitLine[0].equalsIgnoreCase("linkup")) {
                    if (splitLine.length != 3) {
                        System.out.println("Usage: LINKUP <ip> <port>");
                    } else {
                        client.sendLinkPacket(client.self, new Node(splitLine[1], Integer.parseInt(splitLine[2])),
                                LinkMessage.LINK_UP, "");
                    }


                } else if (splitLine[0].equalsIgnoreCase("linkdown")) {
                    if (splitLine.length != 3) {
                        System.out.println("Usage: LINKDOWN <ip> <port>");
                    } else {
                        client.sendLinkPacket(client.self, new Node(splitLine[1], Integer.parseInt(splitLine[2])),
                                LinkMessage.LINK_DOWN, "");
                    }

                } else if (splitLine[0].equalsIgnoreCase("changecost")) {
                    if (splitLine.length != 4) {
                        System.out.println("Usage: CHANGECOST <ip> <port> <cost>");

                    } else {
                        client.sendLinkPacket(client.self, new Node(splitLine[1], Integer.parseInt(splitLine[2])),
                                LinkMessage.LINK_CHANGE_COST, splitLine[3]);
                    }

                } else if (splitLine[0].equalsIgnoreCase("showrt")) {
                    System.out.println(client.distanceVector.toString());

                } else if (splitLine[0].equalsIgnoreCase("transfer")) {
                    if (splitLine.length != 4) {
                        System.out.println("Usage: TRANSFER <filename> <ip> <port>");
                    } else {
                        // Send file details to File Helper
                        String[] fNameArray = splitLine[1].split("/");
                        FileHelper fh = new FileHelper(new Node(splitLine[2], Integer.parseInt(splitLine[3])),
                                new File(splitLine[1]), fNameArray[fNameArray.length - 1], client);
                        if (fh.sendFragments()) {
                            System.out.println("File sent successfully");
                        }
                    }
                } else if (splitLine[0].equalsIgnoreCase("close")) {

                    // Exit in a non-error state
                    System.exit(0);

                } else {
                    System.out.println("Error: That is not a valid command.");
                }

            }
        } catch (IOException e) {
            System.err.println("IOException " + e);
        }
    }

    private void sendLinkPacket(Node sender, Node receiver, int msgType, String cost) {

        // Format the message
        Message sendMsg;
        if (msgType == LinkMessage.LINK_DOWN || msgType == LinkMessage.LINK_UP) {
            sendMsg = new LinkMessage(msgType, "");
        } else {
            sendMsg = new LinkMessage(msgType, cost);
        }

        switch (msgType) {

            case LinkMessage.LINK_UP:
                //System.out.println("linkup");
                Node newNode = new Node(receiver.getIp(), receiver.getPort());
                /*
                if(neighborTemp.containsKey(newNode)) {
                    System.out.println("linkup failed");
                    return;
                }*/
                distanceVector.linkUp(newNode, "");
                if(!neighborTemp.containsKey(newNode)) {
                    System.out.println("that is not your neighbor");
                    return;
                }
                neighborTemp.get(newNode).setDead(false);
                break;

            case LinkMessage.LINK_DOWN:
                //System.out.println("linkdown");
                newNode = new Node(receiver.getIp(), receiver.getPort());
                if(!neighborTemp.containsKey(newNode)) {
                    System.out.println("that is not your neighbor");
                    return;
                }
                distanceVector.linkDown(newNode);
                neighborTemp.get(receiver).setDead(true);
                break;

            case LinkMessage.LINK_CHANGE_COST:
                //System.out.println("change cost");
                newNode = new Node(receiver.getIp(), receiver.getPort());
                distanceVector.put(newNode, new NodeWeight(newNode, Double.parseDouble(cost)));
                neighborTemp.get(newNode).setDead(false);
                break;
        }

        // Send message
        DataPacket dp = new DataPacket(sender, sendMsg);
        this.sendTo(this.sendSock, dp, receiver.getIp(), receiver.getPort());
    }

    /*
    UDP Object Sender
     */
    public void sendTo(DatagramSocket ds, DataPacket dp, String host, int destinationPort) {
        try {
            InetAddress address = InetAddress.getByName(host);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(5000);
            ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
            os.flush();
            os.writeObject(dp);
            os.flush();

            // Retrieve the byte array
            byte[] buffer = byteStream.toByteArray();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, destinationPort);
            //int byteCount = packet.getLength();
            ds.send(packet);
            os.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    UDP Object Receiver
     */

    public DataPacket receiveObject(DatagramSocket socket) {
        try {
            byte[] buffer = new byte[5000];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            //int byteCount = packet.getLength();
            ByteArrayInputStream byteStream = new ByteArrayInputStream(buffer);
            ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));
            Object o = is.readObject();
            is.close();
            return (DataPacket) o;
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Client readClientFile(String name) throws IOException {

        Client returnClient = new Client();

        // Read in local data
        Scanner scanner = new Scanner(new File(name));
        String[] data = scanner.nextLine().split(" ");

        Node me = new Node(InetAddress.getLocalHost().getHostAddress(), Integer.parseInt(data[0]));
        me.setTimeout(Integer.parseInt(data[1]));
        returnClient.distanceVector = new DistanceVector(me);

        // Read in neighbor data
        while(scanner.hasNextLine()) {

            data = scanner.nextLine().split(" ");
            String[] nodeData = data[0].split(":");

            Node neighbor = new Node(nodeData[0], Integer.parseInt(nodeData[1]));
            Double weight = Double.parseDouble(data[1]);

            returnClient.neighborTemp.put(neighbor, new NodeWeight(neighbor, weight));
            returnClient.distanceVector.put(neighbor, new NodeWeight(neighbor, weight));

            //me.addNewNeighbor(neighbor, weight);

            //System.out.println("neighbor " + neighbor.toString() + " weight " + data[1]);

        }
        returnClient.self = me;
        returnClient.recvSock = new DatagramSocket(me.getPort());

        return returnClient;

    }

    private void timeoutNode(Node dead) {
        System.out.println("TIMEDOUTTT" + dead);
        //neighborTemp.get(dead).setDead(true);
        for(java.util.Map.Entry<Node, NodeWeight> nw : distanceVector) {
            if(nw.getKey().equals(dead) || nw.getValue().getNode().equals(dead)) {
                distanceVector.linkDown(nw.getKey());
                System.out.println("nw" + nw.getKey());
            }
        }
        System.out.println("linked down to " + distanceVector);

    }

    private void linkChangeCost(Node n, String cost) {
        Node newNode = new Node(n.getIp(), n.getPort());
        distanceVector.put(newNode, new NodeWeight(newNode, Double.parseDouble(cost)));
        neighborTemp.remove(newNode);
        neighborTemp.put(newNode, new NodeWeight(newNode, Double.parseDouble(cost)));

    }

    private void linkUp(Node n) {
        distanceVector.linkUp(n, "");
        neighborTemp.get(n).setDead(false);
    }

    public void linkDown(Node n) {
        distanceVector.linkDown(n);
        neighborTemp.get(n).setDead(true);
    }


    // Employ Bellman Ford algorithm for updating DistanceVectors
    private ArrayList<DistanceVector> refreshNeighbors() {

        DistanceVector tempDV;
        ArrayList<DistanceVector> down = new ArrayList<DistanceVector>();

        Iterator<Node> nodeIterator = neighborTemp.keySet().iterator();
        while (nodeIterator.hasNext()) {
            Node nextNode = nodeIterator.next();

            tempDV = new DistanceVector(new Node(self.getIp(), self.getPort()));

            long elapsed = TimeUnit.SECONDS.convert((this.init.getTime() - new Date().getTime()),
                    TimeUnit.MILLISECONDS);

            if (elapsed > 3 * this.self.getTimeout() && !otherVectors.contains(new DistanceVector(
                    new Node(nextNode.getIp(), nextNode.getPort())))) {

                System.out.println("TIMEOUT 1");
                nodeIterator.remove();
                timeoutNode(nextNode);
                continue;
            }

            // Poison reverse
            for (java.util.Map.Entry<Node, NodeWeight> pair : distanceVector) {

                tempDV.put(pair.getKey(), new NodeWeight(pair.getValue().getNode(), pair.getValue().getWeight()));


                // look for other nodes that act as hops on route
                if (!pair.getKey().equals(pair.getValue().getNode())) {
                    if (nextNode.equals(pair.getValue().getNode())) {
                        //System.out.println("Poison: Node = " + nextNode.getPort() +
                        //        " dest " + pair.getKey().getPort() + " hop " + pair.getValue().getNode().getPort());
                        tempDV.linkDown(pair.getKey());

                    }
                }

            }

            // Send route update message to alive nodes containing updated DistanceVector
            if ((!neighborTemp.get(nextNode).isDead()) && !distanceVector.getOwnerNode().equals(nextNode)) {
                //System.out.println("DV TO SEND to " + nextNode + "is" + tempDV);
                NetworkMessage nm = new NetworkMessage(NetworkMessage.ROUTE_UPDATE, "");
                nm.setDistanceVector(tempDV);
                DataPacket dp = new DataPacket(self, nm);
                sendTo(sendSock, dp, nextNode.getIp(), nextNode.getPort());
            }
            //System.out.println("sending to " + nextNode + tempDV);
        }

        tempDV = new DistanceVector(new Node(self.getIp(), self.getPort()));

        for (Node node : neighborTemp.keySet()) {
            //System.out.println("neighbor temp key set" + node);

            // Only add live nodes to temp distance vector
            //System.out.println(distanceVector.getConnections());

            if (!distanceVector.getConnections().get(node).isDead()) {
                //System.out.println("lll " + distanceVector.getConnections().get(node));
                //System.out.println("three"+tempDV);
                tempDV.put(node, new NodeWeight(node, neighborTemp.get(node).getWeight()));
            }
        }

        // for updating distance vectors
        for (DistanceVector dv : otherVectors) {

            Date dvDate = dv.getDate();
            Date now = new Date();
            long elapsed = TimeUnit.SECONDS.convert(now.getTime() - dvDate.getTime(),
                    TimeUnit.MILLISECONDS);

            // Kill node if update not received after 3 "timeouts"
            if (elapsed > 3 * self.getTimeout()) {

                //System.out.println("Killing node : " + dv.getOwnerNode().toString());
                //linkDown(dv.getOwnerNode());
                timeoutNode(dv.getOwnerNode());
                down.add(dv);
                continue;
            }
            //System.out.println("tempDV " +tempDV);
            for (java.util.Map.Entry<Node, NodeWeight> pair : dv) {
                Node destination = pair.getKey();
                Node hop = dv.getOwnerNode();
                Double distanceToHop = neighborTemp.get(hop).getWeight();
                //System.out.println("distance to hop " + distanceToHop + " and dest: " + hop.toString());

                Double distanceFromHop = dv.getConnections().get(destination).getWeight();
                //System.out.println("distance from hop " + distanceFromHop + " and dest: " + destination.toString());

                Double totalDistance = distanceFromHop + distanceToHop;


                NodeWeight currentNodeWeight;

                if ((currentNodeWeight = tempDV.getConnections().get(destination)) != null) {

                    //System.out.println("total " + totalDistance);
                    //System.out.println("current node weight " + currentNodeWeight.getWeight());

                    if (currentNodeWeight.getWeight() > totalDistance) {
                        //System.out.println("UPDATINGGGGG");
                        Node nxt = tempDV.getConnections().get(hop).getNode();

                        tempDV.put(destination, new NodeWeight(nxt, totalDistance));
                    }
                } else {

                    tempDV.put(destination, new NodeWeight(hop, totalDistance));
                }

            }
        }
        //System.out.println("DISSSSTANCE VECTORRRR" + tempDV);
        distanceVector = tempDV;

        return down;

    }



class ListenThread extends Thread {

        public void run() {

            // Loop for receiving/handling data
            while (true) {

                DataPacket receivedPkt = receiveObject(recvSock);

                // Detect message type and handle it accordingly
                switch(receivedPkt.getMsg().getType()) {

                    // Link Layer Protocols
                    case LinkMessage.LINK_UP:
                        //System.out.println("got a link up " + receivedPkt.getSender());
                        linkUp(receivedPkt.getSender());
                        break;
                    case LinkMessage.LINK_DOWN:
                        //System.out.println("got a link down " +receivedPkt.getSender());
                        linkDown(receivedPkt.getSender());
                        break;
                    case LinkMessage.LINK_CHANGE_COST:
                        linkChangeCost(receivedPkt.getSender(), receivedPkt.getMsg().getText());
                        break;

                    // Network Layer Protocols
                    case NetworkMessage.ROUTE_UPDATE:
                        //System.out.println("rt : " + receivedPkt.getSender());
                        //linkUp(receivedPkt.getSender());

                        DistanceVector recvd = ((NetworkMessage) receivedPkt.getMsg()).getDistanceVector();
                        //System.out.println("network msg received from "+receivedPkt.getSender() + "\n" + recvd.toString());
                        recvd.setDate(new Date());
                        routeUpdate(recvd);
                        break;

                    // Applicaiton Layer Protocols
                    case ApplicationMessage.FILE_TRANSFER:
                        //System.out.println("Packet received\nSource: "+receivedPkt.getSender().getIp()
                          //      +":"+receivedPkt.getSender().getPort());
                        FileHelper fh = new FileHelper();
                        if(fh.transfer((ApplicationMessage) receivedPkt.getMsg(), distanceVector, sendSock)) {
                            break;
                        }
                        break;

                }
            }
        }

        private void routeUpdate(DistanceVector dv) {

            // Add new nodes upon first route update
            if(!neighborTemp.containsKey(dv.getOwnerNode())) {
                //System.out.println("new node rt update " + dv.getOwnerNode());
                neighborTemp.put(dv.getOwnerNode(), new NodeWeight(dv.getOwnerNode(),
                        dv.getConnections().get(self).getWeight()));

                distanceVector.put(dv.getOwnerNode(), new NodeWeight(dv.getOwnerNode(),
                        dv.getConnections().get(self).getWeight()));
            }
            // Replace old DV with new one
            int i;
            for (i = 0; i < otherVectors.size(); i++) {
                if (dv.getOwnerNode().equals(otherVectors.get(i).getOwnerNode())) {
                    //System.out.println("replacing other");
                    otherVectors.remove(i);
                    break;
                }
            }
            otherVectors.add(dv);

        }
    }

    class Timeout extends Thread {

        public void run() {
            //send heart beat every HEART_RATE seconds
            Timer timer = new Timer();
            timer.schedule(new VectorUpdate(), 0, 1000 * self.getTimeout());
        }

        class VectorUpdate extends TimerTask {

            public void run() {

                // Remove DVs of dead nodes
                ArrayList<DistanceVector> down = refreshNeighbors();
                /*
                System.out.println("old");
                for(DistanceVector d : otherVectors) {
                    System.out.println(d);
                }
*/
                for (DistanceVector dv : down) {
                    otherVectors.remove(dv);
                    //neighborTemp.remove(dv.getOwnerNode());
                    //System.out.println("removing DV for node: " + dv.getOwnerNode());
                }
/*
                System.out.println("new");
                for(DistanceVector d : otherVectors) {
                    System.out.println(d);
                }
*/

            }
        }

    }

    public Node getSelf() {
        return self;
    }

    public void setSelf(Node self) {
        this.self = self;
    }


    public DistanceVector getDistanceVector() {
        return distanceVector;
    }

    public void setDistanceVector(DistanceVector distanceVector) {
        this.distanceVector = distanceVector;
    }


    public DatagramSocket getSendSock() {
        return sendSock;
    }

    public void setSendSock(DatagramSocket sendSock) {
        this.sendSock = sendSock;
    }

}
