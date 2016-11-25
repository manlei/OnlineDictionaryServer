import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Eric on 2016/11/25.
 */
public class YoudaoTranslate extends Translator{
    public YoudaoTranslate(int votes) {
        name="Youdao";
        this.votes=votes;
        isEnable=true;
    }
    public WORD getTranslation(String text) throws Exception{

        //post request
        URL url=new URL("http://fanyi.youdao.com/openapi.do");
        HttpURLConnection connection=(HttpURLConnection) url.openConnection();
        connection.addRequestProperty("encoding","UTF-8");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        OutputStream os=connection.getOutputStream();
        OutputStreamWriter osw=new OutputStreamWriter(os);
        BufferedWriter bw=new BufferedWriter(osw);

        bw.write("keyfrom=SocialDic&key=1251033521&type=data&doctype=json&version=1.1&q="+text);
        bw.flush();

        InputStream is=connection.getInputStream();
        InputStreamReader isr=new InputStreamReader(is,"UTF-8");
        BufferedReader br=new BufferedReader(isr);

        String line;
        StringBuilder sb=new StringBuilder();
        while((line=br.readLine())!=null) {
            sb.append(line);
        }

        bw.close();
        osw.close();
        os.close();
        br.close();
        isr.close();
        is.close();

        //resolve json
        JSONObject jsonObj=(JSONObject)new JSONParser().parse(sb.toString());
        JSONObject basic=(JSONObject)jsonObj.get("basic");
        WORD wd=new WORD();
        wd.translator=name;
        wd.word=text;
        wd.usPhonetic=basic.get("us-phonetic").toString();
        wd.ukPhonetic=basic.get("uk-phonetic").toString();
        JSONArray explains=(JSONArray) basic.get("explains");
        for(int i=0;i<explains.size();++i) {
            wd.explains.add(explains.get(i).toString());
        }
        return wd;
    }
}
