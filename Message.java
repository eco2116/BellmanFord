/**
 * Created by fieldlens on 4/27/15.
 */

import java.io.Serializable;

public class Message implements Serializable {

    public static final int ROUTE_UPDATE = 0, LINK_UP = 1, LINK_DOWN = 2, LINK_CHANGE_COST = 3, FILE_TRANSFER = 4;

    private int type;
    private String text;

    public Message(int type, String text) {
        this.type = type;
        this.text = text;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String toString() {
        return "MSG- type: " + this.type + " text: " + this.text;
    }

}
