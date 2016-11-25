/**
 * Created by Eric on 2016/11/25.
 */
public class Main {
    public static void main(String[] args) throws Exception{
        Server dicServer=new Server();
        while(true) {
            dicServer.readAMessage();
        }
    }
}
