import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

/**
 * Created by Eric on 2016/11/25.
 */
public class JinshanTranslate extends Translator{
    public JinshanTranslate(int votes) {
        name="Jinshan";
        this.votes=votes;
        isEnable=true;
    }
    public WORD getTranslation(String text)throws Exception {

        //get request
        URL url=new URL("http://dict-co.iciba.com/api/dictionary.php?w="+text+"&type=json&key=5940D3DB42250545909B1E10B819C845");
        HttpURLConnection connection=(HttpURLConnection) url.openConnection();
        connection.addRequestProperty("encoding","UTF-8");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");

        InputStream is=connection.getInputStream();
        InputStreamReader isr=new InputStreamReader(is,"UTF-8");
        BufferedReader br=new BufferedReader(isr);

        String line;
        StringBuilder sb=new StringBuilder();
        while((line=br.readLine())!=null) {
            sb.append(line);
        }

        br.close();
        isr.close();
        is.close();

        //resolve json
        WORD wd=new WORD();
        wd.translator=name;
        wd.word=text;
        JSONObject jsonObj=(JSONObject)new JSONParser().parse(sb.toString());
        JSONObject symbols=(JSONObject)((JSONArray) jsonObj.get("symbols")).get(0);
        wd.usPhonetic=symbols.get("ph_am").toString();
        wd.ukPhonetic=symbols.get("ph_en").toString();
        JSONArray parts=(JSONArray)symbols.get("parts");
        Iterator<Object> it=parts.iterator();
        while (it.hasNext()) {
            JSONObject jsonTemp=(JSONObject)it.next();
            StringBuilder entry=new StringBuilder(jsonTemp.get("part").toString()+" ");
            JSONArray means=(JSONArray)jsonTemp.get("means");
            for(int i=0;i<means.size();++i) {
                entry.append(means.get(i)+";");
            }
            wd.explains.add(entry.toString());
        }
        return wd;
    }
}
