import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eric on 2016/11/25.
 */
/*
class WORD
    define the translation format transferred between the server and the client.
 */
public class WORD implements Serializable{
    public String translator;
    public String word;
    public String usPhonetic;
    public String ukPhonetic;
    public List<String> explains=new ArrayList<>();
    public String toString() {
        StringBuilder sb=new StringBuilder(translator+","+word+","+usPhonetic+","+ukPhonetic);
        for(int i=0;i<explains.size();++i){
            sb.append(","+explains.get(i));
        }
        return sb.toString();
    }
}
