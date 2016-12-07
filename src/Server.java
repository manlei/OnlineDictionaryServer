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

    public void InitializeTranslator() {
        t.add(new BingTranslate(0));
        t.add(new JinshanTranslate(0));
        t.add(new YoudaoTranslate(0));
    }

    public void sortTranslator() {
        Collections.sort(t);
    }
    public void run() {
        System.out.println(id+" is running.");

        //Initialize translator
        InitializeTranslator();

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
            //log in
            case 'l':
                login();
                break;
        }
    }

    public void selectTranslator() {
        // sync database
    }

    public void queryWord() throws Exception {
        ArrayList<WORD> answer=new ArrayList<>(3);
        //read user's votes from database
        //read user's enable from database
        sortTranslator();
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
        // TODO: 2016/11/25
        // sync database
    }

    public void login() {
        // TODO: 2016/11/25
        // database operation
    }
}