/**
 * Created by evanoconnor on 4/28/15.
 */

import java.io.Serializable;
import java.util.HashMap;
import java.util.Date;
import java.util.Iterator;


public class DistanceVector implements Serializable, Iterable<java.util.Map.Entry<Node, NodeWeight>> {

    private Node ownerNode;

    // Key = Destination, Value = { next hop, cost }
    private HashMap<Node, NodeWeight> connections;
    private Date date;

    public DistanceVector(Node self) {

        this.ownerNode = new Node(self.getIp(), self.getPort());
        this.connections = new HashMap<Node, NodeWeight>();
        this.connections.put(this.ownerNode, new NodeWeight(this.ownerNode, 0.0));
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    public void linkUp(Node n, String cost) {
        this.connections.get(n).setDead(false);
    }

    public void linkDown(Node n) {
        this.connections.get(n).setDead(true);
    }


    public void put(Node n, NodeWeight connection) {
        //connection.setDead(false);
        if(this.connections.get(n) != null) {
            this.connections.remove(n);
        }
        this.connections.put(n, connection);
    }

    public Node getOwnerNode() {
        return ownerNode;
    }

    public void setOwnerNode(Node ownerNode) {
        this.ownerNode = ownerNode;
    }

    public HashMap<Node, NodeWeight> getConnections() {
        return connections;
    }

    public void setConnections(HashMap<Node, NodeWeight> connections) {
        this.connections = connections;
    }

    // For displaying routing table
    // TODO: check if the right format
    public String toString() {
        String dvString = new Date().toString() + "\nDistance vector is:\n";
        for(Node n : connections.keySet()) {
            if(!n.equals(this.ownerNode) ) {
                dvString += "Destination = " + n.getIp() +":"+ n.getPort() +
                        ", Cost = " + connections.get(n).getWeight() +
                        ", Link = (" + connections.get(n).getNode().getIp() +":"+
                        connections.get(n).getNode().getPort() + ")\n";
            }
        }
        return dvString;
    }

    @Override
    public Iterator<java.util.Map.Entry<Node, NodeWeight>> iterator(){
        return this.connections.entrySet().iterator();
    }

    @Override
    public boolean equals(Object object) {
        if(this.getOwnerNode().equals(((DistanceVector) object).getOwnerNode())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.ownerNode.hashCode();
    }

}
