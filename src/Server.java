import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Eric on 2016/11/25.
 */
/*
class Server
    the main class of the server.
    connect to the client and establish work thread.
 */
public class Server {
    private int port=8000;
    private int id=0;
    //private LinkedBlockingQueue<Transfer> transfers;
    private List<Transfer> transfers;
    private String defaultPath= FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath()+File.separator+
            "ODS"+File.separator;
    public Server() {

    }

    public void Service() {
        transfers=Collections.synchronizedList(new ArrayList<Transfer>());//shared by all threads
        File defaultDir=new File(defaultPath);
        if(!defaultDir.exists())
            defaultDir.mkdirs();
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                ++id;
                new Thread(new ServerTask(socket,id,transfers,defaultPath)).start();
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}

/*
class ServerTask
    describe the task of the server.
 */
class ServerTask implements Runnable {
    private Socket socket;
    private int id;
    private Database db;
    private List<Transfer> transfers;
    private Message request;
    private String defaultPath;
    private Transfer currentTransfer;
    //three translator
    protected ArrayList<Translator> t=new ArrayList<>(3);
    private ObjectInputStream objectFromClient;
    private ObjectOutputStream objectToClient;

    public ServerTask(Socket socket,int id,List<Transfer> transfers,String defaultPath) {
        this.socket=socket;
        this.id=id;
        this.db=new Database();
        this.transfers=transfers;
        this.defaultPath=defaultPath;
        currentTransfer=null;
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
                if(currentTransfer==null)
                    setCurrentTransfer();
                if(currentTransfer!=null)
                    askClientToReceive();
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
        db.close();
        System.out.println(id+" is over.");
    }

    private void setCurrentTransfer() {
        synchronized (transfers) {
            for(int i=0;i<transfers.size();++i) {
                Transfer temp=transfers.get(i);
                if(temp.receiver.isEmpty())
                    transfers.remove(i);
                else {
                    if (temp.receiver.contains(request.clientName)) {
                        temp.receiver.remove(request.clientName);
                        currentTransfer = temp;
                        break;
                    }
                }
            }
        }
    }

    private void askClientToReceive() throws Exception {
        Message m=new Message(request.clientName,"trf",currentTransfer.sender);
        objectToClient.writeObject(m);
        objectToClient.flush();
    }

    private void readAMessage() throws Exception {
        //close database connection when wait for client
        db.close();

        request=(Message)objectFromClient.readObject();
        System.out.println(request.toString());

        //a new message comes, establish database connection
        db=new Database();

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
            //send a file
            case 't':
                if(request.type.charAt(1)=='s')
                    handleSend();
                else if(request.type.charAt(1)=='r')
                    handleReceive();
                break;
            //client close
            case 'c':
                close();
                break;
            //modify
            case 'm':
                if(request.type.charAt(1)=='p')
                    modifyPassword();
                break;

        }
    }

    private void selectTranslator() {
        boolean enableBing=(request.text.charAt(0)-'0')!=0;
        boolean enableJinshan=(request.text.charAt(1)-'0')!=0;
        boolean enableYoudao=(request.text.charAt(2)-'0')!=0;
        db.setEnableBing(request.clientName,enableBing);
        db.setEnableJinshan(request.clientName,enableJinshan);
        db.setEnableYoudao(request.clientName,enableYoudao);
    }

    private void queryWord() throws Exception {
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
        boolean result=true;
        Message m=new Message(request.clientName,"rq",""+result);
        objectToClient.writeObject(m);
        objectToClient.flush();
        objectToClient.writeObject(answer);
        objectToClient.flush();
    }

    private void voteTranslator() {
        if(request.text.equals("Bing"))
            db.voteBing(request.clientName);
        if(request.text.equals("Jinshan"))
            db.voteJinshan(request.clientName);
        if(request.text.equals("Youdao"))
            db.voteYoudao(request.clientName);
    }

    private void login() throws Exception {
        String args[]=request.text.split("\t");
        String result=db.login(args[0],args[1]);
        Message m=new Message(request.clientName,"rli",result);
        objectToClient.writeObject(m);
        objectToClient.flush();
    }

    private void modifyPassword() throws Exception {
        String args[]=request.text.split("\t");
        String result=db.modifyPassword(args[0],args[1],args[2]);
        Message m=new Message(request.clientName,"rmp",result);
        objectToClient.writeObject(m);
        objectToClient.flush();
    }

    private void logout() throws Exception {
        boolean result=db.logout(request.clientName);
        Message m=new Message(request.clientName,"rlo",""+result);
        objectToClient.writeObject(m);
        objectToClient.flush();
    }

    private void register() throws Exception {
        String args[]=request.text.split("\t");
        boolean result=db.addUser(args[0],args[1]);
        Message m=new Message(request.clientName,"rr",""+result);
        objectToClient.writeObject(m);
        objectToClient.flush();
    }

    private void getOnlineUsers() throws Exception {
        String result=db.getOnlineUsers(request.clientName);
        Message m=new Message(request.clientName,"ru",""+result);
        objectToClient.writeObject(m);
        objectToClient.flush();
    }


    private void handleSend() throws Exception {

        Transfer temp=new Transfer();
        temp.sender=request.clientName;
        StringTokenizer st=new StringTokenizer(request.text);
        Set<String> allReceivers=new HashSet<>();
        while(st.hasMoreElements()) {
            allReceivers.add(st.nextToken());
        }
        temp.receiver=allReceivers;
        SimpleDateFormat sdf=new SimpleDateFormat("yyyymmddHHmmss");
        String filename=request.clientName+sdf.format(new Date());
        temp.toTransfered=new File(defaultPath+filename+".jpg");

        //reply to client : transfer can start
        Message m=new Message(request.clientName,"rts",""+Boolean.TRUE);
        objectToClient.writeObject(m);
        objectToClient.flush();

        //start receive the file data from the client

        FileOutputStream fos=new FileOutputStream(temp.toTransfered);
        LinkedList<FileFrag> fileData=(LinkedList<FileFrag>)objectFromClient.readObject();
        for(int i=0;i<fileData.size();i++) {
            fos.write(fileData.get(i).Bytes,0,fileData.get(i).length);
            fos.flush();
        }

        if(fos!=null)
            fos.close();

        transfers.add(temp);
    }

    private void handleReceive() throws Exception {
        boolean will=Boolean.parseBoolean(request.text);
        if(will) {
            //reply to client : transfer will start
            Message m=new Message(request.clientName,"rtr",""+currentTransfer.sender);
            objectToClient.writeObject(m);
            objectToClient.flush();

            //start send the file data to the client
            LinkedList<FileFrag> fileData=new LinkedList<>();
            FileInputStream fis=new FileInputStream(currentTransfer.toTransfered);
            byte[] sendBytes=new byte[1024];
            int length=0;
            while((length=fis.read(sendBytes,0,sendBytes.length))>0) {
                fileData.add(new FileFrag(sendBytes,length));
            }
            objectToClient.writeObject(fileData);
            objectToClient.flush();

            if(fis!=null)
                fis.close();

        }

        currentTransfer=null;
    }

    private void close() throws Exception {
        Message m=new Message(request.clientName,"rc",""+Boolean.TRUE);
        objectToClient.writeObject(m);
        objectToClient.flush();
    }

}