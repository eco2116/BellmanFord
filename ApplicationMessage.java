/**
 * Created by evanoconnor on 5/9/15.
 */
public class ApplicationMessage extends Message {

    private Node destination;
    private byte[] fragment;
    private boolean EOF;

    public ApplicationMessage(int type, String message) {
        super(type, message);
        this.EOF = false;
    }

    public void setEOF(boolean EOF) { this.EOF = EOF; }

    public boolean getEOF() { return this.EOF; }

    public Node getDestination() {
        return destination;
    }

    public void setDestination(Node destination) {
        this.destination = destination;
    }

    public byte[] getFragment() {
        return fragment;
    }

    public void setFragment(byte[] fragment) {
        this.fragment = fragment;
    }

}
