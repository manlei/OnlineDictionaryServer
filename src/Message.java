import java.io.Serializable;

/**
 * Created by Eric on 2016/11/26.
 */
// client request message
public class Message implements Serializable {
    public String clientName;
    public String type;
    public String text;
    public Message(String clientName,String type,String text) {
        this.clientName=clientName;
        this.type=type;
        this.text=text;
    }
    public String toString() {
        StringBuilder sb=new StringBuilder(clientName+",");
        sb.append(type);
        sb.append(","+text);
        return sb.toString();
    }
}
