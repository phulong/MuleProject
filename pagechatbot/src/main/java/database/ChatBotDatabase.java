package database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;

import org.mule.api.MuleMessage;

import model.ChatBot;
import model.Sender;

import model.ChatBot;
public class ChatBotDatabase {
	
	 
	 private Connection connect() throws ClassNotFoundException {
	        // SQLite connection string
	     Class.forName("org.sqlite.JDBC");
        String url = "jdbc:sqlite:home/data.db";
	        Connection conn = null;
	        try {
	            conn = DriverManager.getConnection(url);
	        } catch (SQLException e) {
	            System.out.println(e.getMessage());
	        }
	        return conn;
	    }
	 
	 public int insert(ChatBot chatbot) {
		    int cnt = 0;
	        String sql = "INSERT INTO chat  VALUES(?,?,?)";
	        try {
	                Connection conn = this.connect();
	        		PreparedStatement pre  = conn.prepareStatement(sql);
	        		pre.setString(1,null);
	        		pre.setString(2,chatbot.GetUserChat());
	        		pre.setString(3,chatbot.GetBotChat());
	        		
	        		cnt = pre.executeUpdate();
	              /*  Statement stmt  = conn.createStatement();
	                ResultSet rs    = stmt.executeQuery(sql)){
	                cnt++;*/
	        		cnt++;
	                 
	           } catch (SQLException | ClassNotFoundException e) {
	               System.out.println(e.getMessage());
	           }	
	        return cnt	;
	    }
	 
	 
	 public ArrayList<ChatBot>  select( ) {
		    ArrayList<ChatBot> list =  new ArrayList<ChatBot>(); 
	        String sql = "select * from chat";
	        try (Connection conn = this.connect();

          Statement stmt  = conn.createStatement();
	                ResultSet rs    = stmt.executeQuery(sql)){	               
	        	    ChatBot cb = null;
	               // loop through the result set
	               while (rs.next()) {
	            	   
	            	   cb = new ChatBot(rs.getInt("id"), rs.getString("user_chat"),rs.getString("bot_chat"));
	            	   
	            	   list.add(cb);
	               }
	           } catch (SQLException | ClassNotFoundException e) {
	               System.out.println(e.getMessage());
	           }
	        return list;
	     
	    }
	 
	 public ChatBot reply(String text) throws ClassNotFoundException{			
		    text = text.toLowerCase();
		    ChatBot chatbot = null;
			String sql = "SELECT * FROM chat where user_chat = '"+text+"'";			
	        
	        try (Connection conn = this.connect();
	             Statement stmt  = conn.createStatement();
	             ResultSet rs    = stmt.executeQuery(sql)){
	             
	            // loop through the result set
	            while (rs.next()) {
	            	chatbot =  new ChatBot (rs.getInt("id"),rs.getString("user_chat"),rs.getString("bot_chat"));
	             }
	        } catch (SQLException e) {
	            System.out.println(e.getMessage());
	        }
	        if (chatbot==null){
	        	String[] asArray = {"Hey, Welcome to my chat bot page. My purpose is to chat with people. Please approve for me. Thank you very much.","Of course! But I am in the process of building. Please approve for me. Thank you very much.","Hey, Welcome to my chat bot page. My purpose is to chat with people. Please approve for me. Thank you very much."}; 
	    		Random rand = new Random();
	    		int randomNum, minimum = 0, maximum =asArray.length;
	    		randomNum = minimum + rand.nextInt((maximum - minimum));
	    		String bot_chat = asArray[randomNum];
	        	chatbot = new ChatBot(text,bot_chat);
	        	return chatbot;
	        } else 
	        return chatbot;
	    }
	 
	 public int delete(String id) {
	        String sql = "DELETE FROM chat WHERE id = ?";
	        int cnt = 0;
	        try  
	        {
	                Connection conn = this.connect();
	                PreparedStatement pstmt = conn.prepareStatement(sql);
	                pstmt.setString(1,id);
	                pstmt.executeUpdate();
	            // execute the delete statement
	               pstmt.executeUpdate();
	               
	               cnt +=1;
	  
	        } catch (SQLException | ClassNotFoundException e) {
	            System.out.println(e.getMessage());
	        }
	        return cnt;
	    }
	 
	 public ChatBot SelectWhere(int id) throws ClassNotFoundException{			
		   
		    ChatBot chatbot = null;
			String sql = "SELECT * FROM chat where id ="+id;			
	        
	        try (Connection conn = this.connect();
	        	 PreparedStatement pre = conn.prepareStatement(sql);
	             Statement stmt  = conn.createStatement();
	             ResultSet rs    = stmt.executeQuery(sql)){
	             
	            // loop through the result set
	            while (rs.next()) {
	            	chatbot =  new ChatBot (rs.getInt("id"),rs.getString("user_chat"),rs.getString("bot_chat"));
	             }
	        } catch (SQLException e) {
	            System.out.println(e.getMessage());
	        }
	        
	        if (chatbot!=null){	        	
	        	
	        	return chatbot;
	        } else return null;
	    }
	 
	 public int Update(ChatBot chatbot) {
		    int cnt = 0;
		    String sql = "UPDATE chat SET user_chat = ? , "
	                + "bot_chat = ? "
	                + "WHERE id = ?";
	        try {
	                Connection conn = this.connect();
	        		PreparedStatement pre  = conn.prepareStatement(sql);
	        		pre.setString(1,chatbot.GetUserChat());
	        		pre.setString(2,chatbot.GetBotChat());
	        		pre.setInt(3,chatbot.GetId());
	        		
	        		cnt = pre.executeUpdate();
	              /*  Statement stmt  = conn.createStatement();
	                ResultSet rs    = stmt.executeQuery(sql)){
	                cnt++;*/
	        		cnt++;
	                 
	           } catch (SQLException | ClassNotFoundException e) {
	               System.out.println(e.getMessage());
	           }	
	        return cnt	;
	    }

}
