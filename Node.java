/**
 * Created by fieldlens on 4/27/15.
 */

import java.io.Serializable;
import java.util.HashMap;

public class Node implements Serializable {

    private String ip;
    private int port;
    //private boolean isDead;
    private int timeout;

    private HashMap<Node, Double> linkedUpNeighbors;
    private HashMap<Node, Double> linkedDownNeighbors;

    //private HashMap<Node, Double> destinations;

    public Node(String ip, int port) {
        //this.isDead = false;
        this.ip = ip;
        this.port = port;
        this.linkedUpNeighbors = new HashMap<Node, Double>();
        this.linkedDownNeighbors = new HashMap<Node, Double>();
        //this.destinations = new HashMap<Node, Double>();
    }
/*
    public boolean isDead() {
        return this.isDead;
    }
*/
    public void addNewNeighbor(Node n, Double w) {
        this.linkedUpNeighbors.put(n, w);
    }

    public boolean linkUpNeighbors(Node n) {

        // Can't already be linked up
        if(this.linkedUpNeighbors.containsKey(n)) {
            return false;
        }

        // Must have previously been linked down
        Double w;
        if((w = this.linkedDownNeighbors.get(n)) == null) {
            return false;
        }

        // Successful linkup
        this.linkedUpNeighbors.put(n, w);
        //n.setDead(false);
        return true;

    }

    public boolean linkDownNeighbors(Node n) {

        // Can't already be linked down
        if(this.linkedDownNeighbors.containsKey(n)) {
            return false;
        }

        // Must have previously been linked up
        Double w;
        if((w = this.linkedUpNeighbors.get(n)) == null) {
            return false;
        }

        // Successful link down
        this.linkedDownNeighbors.put(n,w);
        //n.setDead(true);
        return true;

    }


    public boolean linkChangeCost(Node n, Double w) {

        // Link must be up
        if(this.linkedDownNeighbors.containsKey(n) || !this.linkedUpNeighbors.containsKey(n)) {
            return false;
        }

        // Successful change cost
        this.linkedUpNeighbors.remove(n);
        this.linkedUpNeighbors.put(n, w);
        return true;

    }


    public void neighborsAsStrings() {
        System.out.println("NEIGHBORS AS STRINGS");
        System.out.println("down...");
        for(Node n : this.linkedDownNeighbors.keySet()) {
            System.out.println(n);
            System.out.println(this.linkedDownNeighbors.get(n));
        }
        System.out.println("up...");
        for(Node n : this.linkedUpNeighbors.keySet()) {
            System.out.println(n);
            System.out.println(this.linkedUpNeighbors.get(n));
        }
    }



    // TODO: add this more places
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Node))return false;
        Node otherNode = (Node) other;
        if(this.getIp().equals(otherNode.getIp()) && (this.getPort() == otherNode.getPort())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.ip.hashCode() + this.port;
    }

    public String toString() {
        return "IP: " + this.ip + " on Port: " + this.port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }


    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public HashMap<Node, Double> getLinkedUpNeighbors() {
        return linkedUpNeighbors;
    }

    public void setLinkedUpNeighbors(HashMap<Node, Double> linkedUpNeighbors) {
        this.linkedUpNeighbors = linkedUpNeighbors;
    }
/*
    public void setDead(boolean isDead) {

        this.isDead = isDead;

    }*/

    public HashMap<Node, Double> getLinkedDownNeighbors() {
        return linkedDownNeighbors;
    }

    public void setLinkedDownNeighbors(HashMap<Node, Double> linkedDownNeighbors) {
        this.linkedDownNeighbors = linkedDownNeighbors;
    }


}

