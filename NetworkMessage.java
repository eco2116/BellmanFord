/**
 * Created by evanoconnor on 4/30/15.
 */
public class NetworkMessage extends Message {

    private DistanceVector distanceVector;

    public NetworkMessage(int type, String message) {
        super(type, message);
    }

    public DistanceVector getDistanceVector() {
        return distanceVector;
    }

    public void setDistanceVector(DistanceVector distanceVector) {
        this.distanceVector = distanceVector;
    }

}
