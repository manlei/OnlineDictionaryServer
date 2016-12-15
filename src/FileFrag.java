import java.io.Serializable;

/**
 * Created by Eric on 2016/12/15.
 */
public class FileFrag implements Serializable {
    public byte[] Bytes;
    public int length;
    public FileFrag(byte[] inputBytes,int length) {
        Bytes=inputBytes.clone();
        this.length=length;
    }
}
