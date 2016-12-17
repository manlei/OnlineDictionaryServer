import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

/**
 * Created by Eric on 2016/11/25.
 */
/*
class YoudaoTranslate
    get and resolve the data Youdao API provided.
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
        //System.out.println(sb.toString());

        bw.close();
        osw.close();
        os.close();
        br.close();
        isr.close();
        is.close();


        //resolve json
        WORD wd=new WORD();
        wd.translator=name;
        wd.word=text;
        JSONObject jsonObj=(JSONObject)new JSONParser().parse(sb.toString());
        //basic meanings
        if(jsonObj.get("basic")==null) {
            wd.usPhonetic="";
            wd.ukPhonetic="";
        }
        else {
            JSONObject basic = (JSONObject) jsonObj.get("basic");
            if(basic.get("us-phonetic")!=null)
                wd.usPhonetic = basic.get("us-phonetic").toString();
            else
                wd.usPhonetic="";
            if(basic.get("uk-phonetic")!=null)
                wd.ukPhonetic = basic.get("uk-phonetic").toString();
            else
                wd.ukPhonetic="";
            JSONArray explains = (JSONArray) basic.get("explains");
            for (int i = 0; i < explains.size(); ++i) {
                wd.explains.add(explains.get(i).toString());
            }
        }
        //web meanings
        if(jsonObj.get("web")!=null) {
            JSONArray web = (JSONArray) jsonObj.get("web");
            for(int i=0;i<web.size();++i) {
                JSONObject item=(JSONObject)web.get(i);
                String temp=item.get("key").toString().toLowerCase();
                if(temp.equals(text.toLowerCase())) {
                    JSONArray values=(JSONArray)item.get("value");
                    StringBuilder entry=new StringBuilder();
                    entry.append("Web ");
                    for(int j=0;j<values.size();++j) {
                        entry.append(values.get(j).toString()+"；");
                    }
                    wd.explains.add(entry.toString());
                }
            }
        }
        if(wd.explains.isEmpty())
            wd.explains.add("无法查到此单词");
        System.out.println(wd);
        return wd;
    }
}
