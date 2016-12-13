import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Eric on 2016/11/25.
 */
public class Server {
    private int port=8000;
    private int id=0;
    private Database db;
    public Server() {

    }

    public Server(int port) {
        this.port=port;
    }

    public void Service() {
        db=new Database();
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                ++id;
                new Thread(new ServerThread(socket,id,db)).start();
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}

class ServerThread implements Runnable {
    private Socket socket;
    private int id;
    private Database db;
    private Message request;
    //three translator
    protected ArrayList<Translator> t=new ArrayList<>(3);
    private ObjectInputStream objectFromClient;
    private ObjectOutputStream objectToClient;

    public ServerThread(Socket socket,int id,Database db) {
        this.socket=socket;
        this.id=id;
        this.db=db;
    }

    public void run() {
        System.out.println(id+" is running.");

        //Initialize translator
        t.add(new BingTranslate(0));
        t.add(new JinshanTranslate(0));
        t.add(new YoudaoTranslate(0));

        try {
            objectFromClient = new ObjectInputStream(socket.getInputStream());
            objectToClient = new ObjectOutputStream(socket.getOutputStream());
            while (true) {
                readAMessage();
            }
        }
        catch (Exception ex) {
            try {
                objectFromClient.close();
                objectToClient.close();
                socket.close();
            }
            catch (Exception ioex) {
                ioex.printStackTrace();
            }
        }
        System.out.println(id+" is over.");
    }

    public void readAMessage() throws Exception {
        request=(Message)objectFromClient.readObject();
        System.out.println(request.toString());
        switch(request.type.charAt(0)) {
            //select translator
            case 's':
                selectTranslator();
                break;
            //query a word
            case 'q':
                queryWord();
                break;
            //vote a translator
            case 'v':
                voteTranslator();
                break;
            //login and logout
            case 'l':
                if(request.type.charAt(1)=='i')
                    login();
                else if(request.type.charAt(1)=='o')
                    logout();
                break;
            //register
            case 'r':
                register();
                break;
            //other online users
            case 'u':
                getOnlineUsers();
                break;
        }
    }

    public void selectTranslator() {
        boolean enableBing=(request.text.charAt(0)-'0')!=0;
        boolean enableJinshan=(request.text.charAt(1)-'0')!=0;
        boolean enableYoudao=(request.text.charAt(2)-'0')!=0;
        db.setEnableBing(request.clientName,enableBing);
        db.setEnableJinshan(request.clientName,enableJinshan);
        db.setEnableYoudao(request.clientName,enableYoudao);
    }

    public void queryWord() throws Exception {
        ArrayList<WORD> answer=new ArrayList<>(3);
        //read user's votes from database
        //read user's enable from database
        if(!request.clientName.equals("null")) {
            for(int i=0;i<3;++i) {
                if(t.get(i).name.equals("Bing")) {
                    t.get(i).isEnable=db.getEnableBing(request.clientName);
                    t.get(i).votes=db.getBingVotes(request.clientName);
                    //System.out.println(t.get(i).isEnable+"\t"+t.get(i).votes);
                }
                if(t.get(i).name.equals("Jinshan")) {
                    t.get(i).isEnable=db.getEnableJinshan(request.clientName);
                    t.get(i).votes=db.getJinshanVotes(request.clientName);
                    //System.out.println(t.get(i).isEnable+"\t"+t.get(i).votes);
                }
                if(t.get(i).name.equals("Youdao")) {
                    t.get(i).isEnable=db.getEnableYoudao(request.clientName);
                    t.get(i).votes=db.getYoudaoVotes(request.clientName);
                    //System.out.println(t.get(i).isEnable+"\t"+t.get(i).votes);
                }
            }
            Collections.sort(t);
        }

        if(t.get(0).isEnable)
            answer.add(t.get(0).getTranslation(request.text));
        if(t.get(1).isEnable)
            answer.add(t.get(1).getTranslation(request.text));
        if(t.get(2).isEnable)
            answer.add(t.get(2).getTranslation(request.text));
        objectToClient.writeObject(answer);
        objectToClient.flush();
    }

    public void voteTranslator() {
        if(request.text.equals("Bing"))
            db.voteBing(request.clientName);
        if(request.text.equals("Jinshan"))
            db.voteJinshan(request.clientName);
        if(request.text.equals("Youdao"))
            db.voteYoudao(request.clientName);
    }

    public void login() throws Exception {
        String args[]=request.text.split("\t");
        boolean result=db.login(args[0],args[1]);
        Message m=new Message(request.clientName,"rli",""+result);
        objectToClient.writeObject(m);
        objectToClient.flush();
    }

    public void logout() throws Exception {
        boolean result=db.logout(request.clientName);
        Message m=new Message(request.clientName,"rlo",""+result);
        objectToClient.writeObject(m);
        objectToClient.flush();
    }

    public void register() throws Exception {
        String args[]=request.text.split("\t");
        boolean result=db.addUser(args[0],args[1]);
        Message m=new Message(request.clientName,"rr",""+result);
        objectToClient.writeObject(m);
        objectToClient.flush();
    }

    public void getOnlineUsers() throws Exception {
        String result=db.getOnlineUsers(request.clientName);
        Message m=new Message(request.clientName,"ru",""+result);
        objectToClient.writeObject(m);
        objectToClient.flush();
    }
}