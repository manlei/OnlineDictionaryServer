import java.sql.*;

/**
 * Created by Eric on 2016/12/6.
 */
/*
class Database
    encapsulate the database operations.
 */
public class Database {
    private static final String driver="com.mysql.jdbc.Driver";
    private static final String url="jdbc:mysql://localhost:3306/dictionary?"+"user=dict&password=dict1234&useUnicode=true&characterEncoding=utf8";
    private Connection conn=null;
    private Statement stmt;

    //establish database connection
    public Database() {
        try {
            Class.forName(driver);
            conn= DriverManager.getConnection(url);
            stmt=conn.createStatement();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //close database connection
    public void close() {
        try {
            if(!stmt.isClosed())
                stmt.close();
            if(!conn.isClosed())
                conn.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //judge a user whether exists
    private boolean judgeUserExist(String name) {
        String query="select * from User where NAME='"+name+"'";
        boolean result=false;
        try {
            PreparedStatement pstmt=conn.prepareStatement(query);
            ResultSet rs=pstmt.executeQuery();
            if(rs.next())
                result=true;
            pstmt.close();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    //judge a user whether online
    private boolean judgeUserOnline(String name) {
        boolean result=false;
        String query="select * from User where NAME='"+name+"'&& STATE='1'";
        try {
            PreparedStatement pstmt= conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
                result=true;
            pstmt.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    //register
    public boolean addUser(String name,String pass) {
        boolean result=false;
        if(judgeUserExist(name)) {
            //user already exists
            return false;
        }
        else {
            try {
                String sql = "insert into User(NAME,PASS,STATE,ENABLE_BING,ENABLE_JINSHAN,ENABLE_YOUDAO,BING,JINSHAN,YOUDAO) values('" + name + "','" + pass + "','0','1','1','1','0','0','0')";
                if(stmt.executeUpdate(sql)>0)
                    result=true;
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return  result;
    }

    //delete user
    public boolean deleteUser(String name) {
        boolean result=false;
        try {
            String sql="delete from User where NAME='"+name+"'";
            if(stmt.executeUpdate(sql)>0)
                result=true;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    //modify password
    public String modifyPassword(String name,String pass,String newPass) {
        boolean result=false;
        StringBuilder sb=new StringBuilder();
        if(judgeUserOnline(name))
            sb.append("AO");
        else {
            try {
                String update = "update User set PASS='" + newPass + "' where NAME='" + name + "'&& PASS='" + pass + "'";
                if (stmt.executeUpdate(update) > 0) {
                    sb.append("MS");
                    result = true;
                }
                else {
                    sb.append("WI");
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        sb.append('\t');
        sb.append(result);
        return sb.toString();
    }

    //login
    public String login(String name,String pass) {
        boolean result=false;
        StringBuilder sb=new StringBuilder();
        if(judgeUserOnline(name))
            sb.append("AO");
        else {
            try {
                String query = "select * from User where NAME='" + name + "'&& PASS='" + pass + "'";
                PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String update = "update User set STATE='1' where NAME='" + name + "'";
                    if (stmt.executeUpdate(update) > 0) {
                        //login successfully
                        sb.append("LS");
                        result = true;
                    }
                }
                else {
                    sb.append("WI");
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        sb.append('\t');
        sb.append(result);
        return sb.toString();
    }

    //logout
    public boolean logout(String name) {
        boolean result=false;
        if(!judgeUserExist(name)) {
            //user does not exist
            return false;
        }
        else {
            try {
                String update = "update User set STATE='0' where NAME='" + name + "'";
                if (stmt.executeUpdate(update)>0) {
                    result = true;
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    //vote translator
    public boolean voteBing(String name) {
        boolean result=false;
        try {
            String query = "select BING from User where NAME='"+name+"'";
            PreparedStatement pstmt=conn.prepareStatement(query);
            ResultSet rs=pstmt.executeQuery();
            if(rs.next()) {
                int num=Integer.parseInt(rs.getString("BING"));
                num++;
                String update="update User set BING='"+num+"' where NAME='" + name + "'";
                if(stmt.executeUpdate(update)>0) {
                    result=true;
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
    public boolean voteJinshan(String name) {
        boolean result=false;
        try {
            String query = "select JINSHAN from User where NAME='"+name+"'";
            PreparedStatement pstmt=conn.prepareStatement(query);
            ResultSet rs=pstmt.executeQuery();
            if(rs.next()) {
                int num=Integer.parseInt(rs.getString("JINSHAN"));
                num++;
                String update="update User set JINSHAN='"+num+"' where NAME='" + name + "'";
                if(stmt.executeUpdate(update)>0) {
                    result=true;
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
    public boolean voteYoudao(String name) {
        boolean result=false;
        try {
            String query = "select YOUDAO from User where NAME='"+name+"'";
            PreparedStatement pstmt=conn.prepareStatement(query);
            ResultSet rs=pstmt.executeQuery();
            if(rs.next()) {
                int num=Integer.parseInt(rs.getString("YOUDAO"));
                num++;
                String update="update User set YOUDAO='"+num+"' where NAME='" + name + "'";
                if(stmt.executeUpdate(update)>0) {
                    result=true;
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    //get votes
    public int getBingVotes(String name) {
        int result=-1;
        try {
            String query = "select BING from User where NAME='"+name+"'";
            PreparedStatement pstmt=conn.prepareStatement(query);
            ResultSet rs=pstmt.executeQuery();
            if(rs.next()) {
                result=rs.getInt("BING");
                //result = Integer.parseInt(rs.getString("BING"));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
    public int getJinshanVotes(String name) {
        int result=-1;
        try {
            String query = "select JINSHAN from User where NAME='"+name+"'";
            PreparedStatement pstmt=conn.prepareStatement(query);
            ResultSet rs=pstmt.executeQuery();
            if(rs.next()) {
                result=rs.getInt("JINSHAN");
                //result = Integer.parseInt(rs.getString("JINSHAN"));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
    public int getYoudaoVotes(String name) {
        int result=-1;
        try {
            String query = "select YOUDAO from User where NAME='"+name+"'";
            PreparedStatement pstmt=conn.prepareStatement(query);
            ResultSet rs=pstmt.executeQuery();
            if(rs.next()) {
                result=rs.getInt("YOUDAO");
                //result = Integer.parseInt(rs.getString("YOUDAO"));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    //choose translators
    public boolean setEnableBing(String name,boolean arg) {
        boolean result=false;
        try {
            if(judgeUserExist(name)) {
                int enable=arg==true?1:0;
                String update = "update User set ENABLE_BING ='"+enable+"' where NAME='"+name+"'";
                if(stmt.executeUpdate(update)>0) {
                    result=true;
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public boolean setEnableJinshan(String name,boolean arg) {
        boolean result=false;
        try {
            if(judgeUserExist(name)) {
                int enable=arg==true?1:0;
                String update = "update User set ENABLE_JINSHAN ='"+enable+"' where NAME='"+name+"'";
                if(stmt.executeUpdate(update)>0) {
                    result=true;
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public boolean setEnableYoudao(String name,boolean arg) {
        boolean result=false;
        try {
            if(judgeUserExist(name)) {
                int enable=arg==true?1:0;
                String update = "update User set ENABLE_YOUDAO ='"+enable+"' where NAME='"+name+"'";
                if(stmt.executeUpdate(update)>0) {
                    result=true;
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    //get which translators are chosen
    public boolean getEnableBing(String name) {
        boolean result=false;
        try {
            String query = "select ENABLE_BING from User where NAME='"+name+"'";
            PreparedStatement pstmt=conn.prepareStatement(query);
            ResultSet rs=pstmt.executeQuery();
            if(rs.next()) {
                result=rs.getBoolean("ENABLE_BING");
                //int enable=Integer.parseInt(rs.getString("ENABLE_BING"));
                //result=enable==0?false:true;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public boolean getEnableJinshan(String name) {
        boolean result=false;
        try {
            String query = "select ENABLE_JINSHAN from User where NAME='"+name+"'";
            PreparedStatement pstmt=conn.prepareStatement(query);
            ResultSet rs=pstmt.executeQuery();
            if(rs.next()) {
                result=rs.getBoolean("ENABLE_JINSHAN");
                //int enable=Integer.parseInt(rs.getString("ENABLE_JINSHAN"));
                //result=enable==0?false:true;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public boolean getEnableYoudao(String name) {
        boolean result=false;
        try {
            String query = "select ENABLE_YOUDAO from User where NAME='"+name+"'";
            PreparedStatement pstmt=conn.prepareStatement(query);
            ResultSet rs=pstmt.executeQuery();
            if(rs.next()) {
                result=rs.getBoolean("ENABLE_YOUDAO");

                //int enable=Integer.parseInt(rs.getString("ENABLE_YOUDAO"));
                //result=enable==0?false:true;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    //get online users
    public String getOnlineUsers(String name) {
        StringBuilder sb=new StringBuilder();
        try {
            String query="select NAME from User where state='1' && NAME!='"+name+"'";
            PreparedStatement pstmt=conn.prepareStatement(query);
            ResultSet rs=pstmt.executeQuery();
            while(rs.next()) {
                sb.append(rs.getString("NAME"));
                sb.append('\t');
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return sb.toString();
    }

}
