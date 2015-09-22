import java.io.Serializable;

/**
 * Created by evanoconnor on 5/6/15.
 */
public class DataPacket implements Serializable {

    private Node sender;
    private Message msg;

    public DataPacket(Node sender, Message msg) {
        this.sender = sender;
        this.msg = msg;
    }

    public Node getSender() {
        return sender;
    }

    public void setSender(Node sender) {
        this.sender = sender;
    }

    public Message getMsg() {
        return msg;
    }

    public void setMsg(Message msg) {
        this.msg = msg;
    }

    public String toString() {
        return "Data Packet\nMessage\n" + this.msg.toString() + "\nSender\n" + this.sender.toString();
    }
}
