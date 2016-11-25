import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Eric on 2016/11/25.
 */
public class Server {
    //three translator
    protected ArrayList<Translator> t=new ArrayList<>(3);
    //input and output stream
    //private DataInputStream fromClient;
    //private DataOutputStream toClient;
    private ObjectInputStream objectFromClient;
    private ObjectOutputStream objectToClient;

    public void InitializeTranslator() {
        //read the votes from the database
        t.add(new BingTranslate(0));
        t.add(new JinshanTranslate(0));
        t.add(new YoudaoTranslate(0));
    }

    public void sortTranslator() {
        Collections.sort(t);
    }
    public Server() {
        try {
            //Initialize translator
            InitializeTranslator();
            sortTranslator();

            ServerSocket serverSocket = new ServerSocket(8000);
            Socket socket = serverSocket.accept();

            //fromClient=new DataInputStream(socket.getInputStream());
            //toClient=new DataOutputStream(socket.getOutputStream());
            objectFromClient=new ObjectInputStream(socket.getInputStream());
            objectToClient=new ObjectOutputStream(socket.getOutputStream());

        }
        catch(IOException ex) {
            //
        }
    }

    public void readAMessage() throws Exception{
            String message = (String)objectFromClient.readObject();
        System.out.println(message);
            char mType=message.charAt(0);
            String text=message.substring(1,message.length());
            switch(mType) {
                //select translator
                case 's':
                    selectTranslator(text);
                    break;
                //query a word
                case 'q':
                    queryWord(text);
                    break;
                //vote a translator
                case 'v':
                    voteTranslator(text);
                    break;
                //log in
                case 'l':
                    login(text);
                    break;
            }

    }

    public void selectTranslator(String text) {
        String[] enable=text.split(";");
        for(int i=0;i<3;++i) {
            t.get(i).isEnable=false;
            for(int j=0;j<enable.length;++i) {
                if(t.get(i).equals(enable[j]))
                    t.get(i).isEnable=true;
            }
        }
    }

    public void queryWord(String text) throws Exception{
        ArrayList<WORD> answer=new ArrayList<>(3);
        if(t.get(0).isEnable)
            answer.add(t.get(0).getTranslation(text));
        if(t.get(1).isEnable)
            answer.add(t.get(1).getTranslation(text));
        if(t.get(2).isEnable)
            answer.add(t.get(2).getTranslation(text));
        objectToClient.writeObject(answer);
    }

    public void voteTranslator(String text) {
        if(t.get(0).name.equals(text))
            t.get(0).votes++;
        if(t.get(1).name.equals(text))
            t.get(1).votes++;
        if(t.get(2).name.equals(text))
            t.get(2).votes++;
        sortTranslator();
        // TODO: 2016/11/25
        // sync database
    }
    public void login(String text) {
        // TODO: 2016/11/25
        // database operation
    }
}
