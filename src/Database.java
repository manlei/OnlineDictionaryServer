import java.sql.*;

/**
 * Created by Eric on 2016/12/6.
 */
public class Database {
    private String driver="com.mysql.jdbc.Driver";
    private String url="jdbc:mysql://localhost:3306/dictionary?"+"user=dict&password=dict1234&useUnicode=true&characterEncoding=utf8";
    private Connection conn=null;
    private Statement stmt;
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

    public void close() {
        try {
            stmt.close();
            conn.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //judge a user whether exists
    public boolean judgeUserExist(String name) {
        String sql="select * from User where NAME='"+name+"'";
        boolean result=false;
        try {
            PreparedStatement pstmt=conn.prepareStatement(sql);
            ResultSet rs=pstmt.executeQuery();
            pstmt.close();
            if(rs.next())
                result=true;
        }
        catch(Exception ex) {
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
    public boolean modifyPassword(String name,String pass,String newPass) {
        boolean result=false;
        try {
            String update="update User set PASS='"+newPass+"' where NAME='"+name+"'&& PASS='"+pass+"'";
            if(stmt.executeUpdate(update)>0) {
                result=true;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    //login
    public boolean login(String name,String pass) {
        boolean result=false;
        try {
            String query = "select * from User where NAME='"+name+"'&& PASS='"+pass+"'";
            PreparedStatement pstmt;
            pstmt=conn.prepareStatement(query);
            ResultSet rs=pstmt.executeQuery();
            pstmt.close();
            if(rs.next()) {
                String update="update User set STATE='1' where NAME='" + name + "'";
                if(stmt.executeUpdate(update)>0) {
                    //login successfully
                    result = true;
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
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
            pstmt.close();
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
            pstmt.close();
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
            pstmt.close();
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
            pstmt.close();
            if(rs.next()) {
                result = Integer.parseInt(rs.getString("BING"));
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
            pstmt.close();
            if(rs.next()) {
                result = Integer.parseInt(rs.getString("JINSHAN"));
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
            pstmt.close();
            if(rs.next()) {
                result = Integer.parseInt(rs.getString("YOUDAO"));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
}