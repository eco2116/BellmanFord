import java.io.Serializable;

/**
 * Created by evanoconnor on 4/30/15.
 */
public class NodeWeight implements Comparable<NodeWeight>, Serializable {

    private Node node;
    private Double weight;
    boolean isDead;

    public NodeWeight(Node n, Double w) {
        this.isDead = false;
        this.node = n;
        this.weight = w;
    }

    public void setDead(boolean isDead) { this.isDead = isDead; }

    public boolean isDead() { return this.isDead; }

    public Double getWeight() {
        return isDead() ? Double.MAX_VALUE : this.weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public int compareTo(NodeWeight other) {
        if(this.node.equals(other.node) && this.weight.equals(other.weight)) {
            return 0;
        } else {
            return -1;
        }
    }

    public String toString() {
        return "Node =\n" + this.node.toString() + "\nWeight =\n" + this.weight.toString();
    }


}
