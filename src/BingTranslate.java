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
/*
class BingTranslate
    get and resolve the data Bing API provided.
 */
public class BingTranslate extends Translator{
    public BingTranslate(int votes) {
        name="Bing";
        this.votes=votes;
        isEnable=true;
    }
    public WORD getTranslation(String text) throws Exception{

        //get request
        URL url=new URL("http://xtk.azurewebsites.net/BingDictService.aspx?Word="+text);
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
        //System.out.println(sb.toString());

        br.close();
        isr.close();
        is.close();


        //resolve json
        WORD wd=new WORD();
        wd.translator=name;
        wd.word=text;
        JSONObject jsonObj=(JSONObject)new JSONParser().parse(sb.toString());
        if(jsonObj.get("pronunciation")==null) {
            wd.usPhonetic="";
            wd.ukPhonetic="";
        }
        else {
            JSONObject pron=(JSONObject) jsonObj.get("pronunciation");
            if (pron.get("AmE") != null)
                wd.usPhonetic = pron.get("AmE").toString();
            else
                wd.usPhonetic = "";
            if (pron.get("BrE") != null)
                wd.ukPhonetic = pron.get("BrE").toString();
            else
                wd.ukPhonetic = "";
        }
        if(jsonObj.get("defs")==null)
            wd.explains.add("无法查到此单词");
        else {
            JSONArray defs = (JSONArray) (jsonObj.get("defs"));
            Iterator<Object> it = defs.iterator();
            while (it.hasNext()) {
                JSONObject jsonTemp = (JSONObject) it.next();
                String entry = jsonTemp.get("pos").toString() + " " + jsonTemp.get("def");
                wd.explains.add(entry);
            }
        }
        System.out.println(wd);
        return wd;
    }
}
