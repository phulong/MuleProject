package service;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Chat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mule.api.MuleMessage;


import database.ChatBotDatabase;
import model.ChatBot;
import model.Sender;

import model.ChatBot;
public class ChatBotService {
	private ChatBotDatabase database;
	private Sender sd;
	MuleMessage mulemessage;
	public static Logger logger = Logger.getLogger(ChatBotService.class);
	public ChatBotService (){
		database =  new ChatBotDatabase();
	}		
	
	public String  selectAll (){						
		ArrayList<ChatBot> list = database.select();
		
		JSONArray array =  new JSONArray();
		
		System.out.println("ID   User_chat   Bot_chat");
		for (int i = 0  ;  i < list.size(); i++){
			
			//System.out.println(list.get(i).GetId()+"|"+list.get(i).GetUserChat()+"|"+ list.get(i).GetBotChat());
			JSONObject object = new JSONObject();
			object.put("id",list.get(i).GetId());
			object.put("user_chat",list.get(i).GetUserChat());
			object.put("bot_chat",list.get(i).GetBotChat());
			array.put(object);
		}
		return array.toString();
		
	}	
	
	
	public String insertChatBot (String user_chat, String bot_chat){
		ChatBot chatbot =  new ChatBot(user_chat.toLowerCase(),bot_chat);
		int cnt = 0 ;
		cnt = database.insert(chatbot);
		if (cnt>0){
			return "Insert Success";
			//logger.info("Insert Success");
						}
		else {
			return "Insert no success";
			//logger.info("Insert not success");
		}
	}	
	

	public String Update (int id,String user_chat, String bot_chat){
		ChatBot chatbot =  new ChatBot(id,user_chat,bot_chat);
		int cnt = 0 ;
		cnt = database.Update(chatbot);
		if (cnt>0){
			return "Update success";
			//logger.info("Insert Success");
						}
		else {
			return "Update error";
			//logger.info("Insert not success");
		}
	}	
	
	
	public String  ReplyChatBot (String text){
		ChatBot cb = null;
		try {
			cb = database.reply(text);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  //SetValue(cb);
		  return cb.GetBotChat();
	}
	
	public void SetValue (ChatBot cb){	
		MuleMessage mulemessage = null;
		mulemessage.setOutboundProperty("bot_chat",cb.GetBotChat());
	}
	
	
	 public void GetJsonData (MuleMessage muleMessage ) throws Exception{
		 //String data  = js;
		 logger.info(muleMessage.getPayloadAsString());
		 
	 }
	 
	 
	 
	
	    public void ReplyMessage (String message) {	    
	   	    try {
	   	    	    logger.info(message);
	   	    	    String access_token =  "EAABtR6ZCyMxkBAGOE73s3v8q2DEmeiqjv2JcbZBcD5ZB0rKdGGzOu5v45j8tVohDiPTNVF7c3yoVgUHZC7wLn8130EaSFA1ZBb4jQyhQFZCNZCJO3QaBSyjnlLnpnIbN05yxGoQVgW9q5xZAfTZCpapo2VAqC7eTgtS5O8Wvy2XnbdgZDZD";
					String data = message;
					logger.info(data);
					if (data != null){
						JSONObject obj = new JSONObject(data);
						JSONArray a = obj.getJSONArray("entry");
						JSONObject j = a.getJSONObject(0);
						JSONArray messing = j.getJSONArray("messaging");
						JSONObject sender = messing.getJSONObject(0);
						JSONObject kk = sender.getJSONObject("sender");
						JSONObject mesage = sender.getJSONObject("message");
						String text = mesage.getString("text");
						
						String id = kk.getString("id");
						//logger.info(message.getPayloadAsString());
						
						
						logger.info("ID sender: "+id);
						logger.info("Text sender: "+text);
						//String token = "EAABtR6ZCyMxkBAOufD7sdK8aL1ZBfZClft6ZAhZBxHxhNjRMH7ffi5AkROP5wNM6nVh2pbft3G9HQ4ZAqeeXUjJxWiAO8L75ZAux8FrhFWLYVpQd1knCeqLecvZBDYpfTR9PZAceVtEKsqth2HHGWYKiBuiRK8wlV8I3QzL99Hx3cEgZDZD"; 
				    	String query = "https://graph.facebook.com/v2.6/me/messages?access_token="+access_token;
				        // String json = "{\"recipient\":{\"id\":"+id+"},\"message\": {\"text\":\""+text+"\"}}";
				        //String textAns = AnswerText (text);
				    	//String json = "{\"recipient\":{\"id\":"+id+"},\"message\": {\"text\":\""+textAns+"\"}}";
				    	
				    	// String jsonCustom = "{\"recipient\":{\"id\":"+id+"},\"message\":{\"attachments\":[{\"type\":\"image\",\"payload\":{\"url\":\"https://goo.gl/ofcFfW\"}}]}}";
				    	 String jsonCustom = "{\"recipient\":{\"id\":"+id+"},\"message\":{\"attachment\":{\"type\":\"image\",\"payload\":{\"url\":\"http://www.w3schools.com/css/trolltunga.jpg\",\"is_reusable\": true}}}}";
				    	 String jsonTemplete ="{\"recipient\":{\"id\":"+id+"},\"message\":{\"attachment\":{\"type\":\"template\",\"payload\":{\"template_type\":\"generic\",\"elements\":[{\"title\":\"Welcome to Peter\'s Hats\",\"image_url\":\"http://hinhnenso1.com/upload_images/images/anh-dep-my-nhan-ben-be-boi-t133.jpg\",\"subtitle\":\"We\'ve got the right hat for everyone.\",\"default_action\": { \"type\": \"web_url\", \"url\": \"http://hinhnenso1.com/images/blogs/2016/03/small/hinh-anh-nhung-chu-cho-dang-yeu-vo-cung-1459409929.jpg\", \"messenger_extensions\": true, \"webview_height_ratio\": \"tall\", \"fallback_url\": \"http://hinhnenso1.com/images/blogs/2016/03/small/hinh-anh-nhung-chu-cho-dang-yeu-vo-cung-1459409929.jpg\""
				    	            + "},\"buttons\":[ { \"type\":\"web_url\", \"url\":\"https://petersfancybrownhats.com\", \"title\":\"View Website\"},{\"type\":\"postback\", \"title\":\"Start Chatting\",\"payload\":{\"url\":\"http://www.w3schools.com/css/trolltunga.jpg\",\"is_reusable\": true} }]}]}}}" ;
				    	 String templete ="\"attachment\": { \"type\": \"template\",\"payload\": { \"template_type\": \"generic\", \"elements\": [{ \"title\": \"Ai Chat Bot Communities\",  \"subtitle\": \"Communities to Follow\", \"image_url\": \"http://1u88jj3r4db2x4txp44yqfj1.wpengine.netdna-cdn.com/...\",\"buttons\": [{ \"type\": \"web_url\", \"url\": \"https://www.facebook.com/groups/aichatbots/\",\"title\": \"FB Chatbot Group\"}, { \"type\": \"web_url\",\"url\": \"https://www.reddit.com/r/Chat_Bots/\",\"title\": \"Chatbots on Reddit\" },{ \"type\": \"web_url\", \"url\": \"https://twitter.com/aichatbots\", \"title\": \"Chatbots on Twitter\" }], }, { \"title\": \"Chatbots FAQ\",\"subtitle\": \"Aking the Deep Questions\",\"image_url\": \"https://tctechcrunch2011.files.wordpress.com/2016/04/facebook-chatbots.png?w=738\","
				                  +"\"buttons\": [{\"type\": \"postback\", \"title\": \"What's the benefit?\", \"payload\": \"Chatbots make content interactive instead of static\", },{\"type\": \"postback\", \"title\": \"What can Chatbots do\",\"payload\": \"One day Chatbots will control the Internet of Things! You will be able to control your homes temperature with a text\",}, { \"type\": \"postback\", \"title\": \"The Future\", \"payload\": \"Chatbots are fun! One day your BFF might be a Chatbot\", }], },  { \"title\": \"Learning More\", \"subtitle\": \"Aking the Deep Questions\", \"image_url\": \"http://www.brandknewmag.com/wp-content/uploads/2015/12/cortana.jpg\", \"buttons\": [{ \"type\": \"postback\",\"title\": \"AIML\",\"payload\": \"Checkout Artificial Intelligence Mark Up Language. Its easier than you think!\",},{"+
				                  "\"type\": \"postback\",\"title\": \"Machine Learning\",\"payload\": \"Use python to teach your maching in 16D space in 15min\", }, { \"type\": \"postback\",\"title\": \"Communities\", \"payload\": \"Online communities & Meetups are the best way to stay ahead of the curve!\", }], }]    }   } }";
				                        
				    	//logger.info(json);
				        //logger.info(jsonCustom);
				      //  logger.info(jsonTemplete);
				       logger.info(templete);
				
					
			        
					try {
					SendMessage(id, text, access_token);
					}
				
					catch (Exception e) {
						System.out.print(e.toString());
					}
					}
					
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    }
	   	      
	   
	    private JSONObject SendMessage(String id , String text,String access_token) throws IOException, JSONException, ClassNotFoundException {
	        String token = "EAABtR6ZCyMxkBAOufD7sdK8aL1ZBfZClft6ZAhZBxHxhNjRMH7ffi5AkROP5wNM6nVh2pbft3G9HQ4ZAqeeXUjJxWiAO8L75ZAux8FrhFWLYVpQd1knCeqLecvZBDYpfTR9PZAceVtEKsqth2HHGWYKiBuiRK8wlV8I3QzL99Hx3cEgZDZD"; 
	    	String query = "https://graph.facebook.com/v2.6/me/messages?access_token="+access_token;
	    	ChatBot cb  = new ChatBot();    	
	    		
			String textAns = database.reply(text).GetBotChat();
		    String json = "{\"recipient\":{\"id\":"+id+"},\"message\": {\"text\":\""+textAns+"\"}}";
	        String image_url = null;
	        if (text.equals("long")||text.equals("Long")){
	        	  image_url = "https://scontent.fsgn2-1.fna.fbcdn.net/v/t1.0-9/17202878_742732359217952_308369262377668109_n.jpg?oh=63e595f5bd0eb0ed1f8ffbc803525fd1&oe=5A84A2A1";
	        } else  if (text.equals("tram")||text.equals("Tram")||text.equals("Trâm")||text.equals("trâm")){
	        	  image_url = "https://scontent.fsgn2-1.fna.fbcdn.net/v/t1.0-9/18557041_1908776859399246_9196916940469872508_n.jpg?oh=89060c5f3c271a87b908600a977447db&oe=5A3A7B52";
	        }
	        else {
	        	  image_url ="https://upload.wikimedia.org/wikipedia/commons/thumb/7/76/Urval_av_de_bocker_som_har_vunnit_Nordiska_radets_litteraturpris_under_de_50_ar_som_priset_funnits_%282%29.jpg/220px-Urval_av_de_bocker_som_har_vunnit_Nordiska_radets_litteraturpris_under_de_50_ar_som_priset_funnits_%282%29.jpg";
	        }
	        
		    String jsonCustom = "{\"recipient\":{\"id\":"+id+"},\"message\":{\"attachment\":{\"type\":\"image\",\"payload\":{\"url\":\""+image_url+"\",\"is_reusable\": true}}}}";
            String jsonTemplete ="{\"recipient\":{\"id\":"+id+"},\"message\":{\"attachment\":{\"type\":\"template\",\"payload\":{\"template_type\":\"generic\",\"elements\":[{\"title\":\"Welcome to Peter\'s Hats\",\"image_url\":\"http://hinhnenso1.com/upload_images/images/anh-dep-my-nhan-ben-be-boi-t133.jpg\",\"subtitle\":\"We\'ve got the right hat for everyone.\",\"default_action\": { \"type\": \"web_url\", \"url\": \"http://hinhnenso1.com/images/blogs/2016/03/small/hinh-anh-nhung-chu-cho-dang-yeu-vo-cung-1459409929.jpg\", \"messenger_extensions\": true, \"webview_height_ratio\": \"tall\", \"fallback_url\": \"http://hinhnenso1.com/images/blogs/2016/03/small/hinh-anh-nhung-chu-cho-dang-yeu-vo-cung-1459409929.jpg\""
            + "},\"buttons\":[ { \"type\":\"web_url\", \"url\":\"https://petersfancybrownhats.com\", \"title\":\"View Website\"},{\"type\":\"postback\", \"title\":\"Start Chatting\",\"payload\":{\"url\":\"http://www.w3schools.com/css/trolltunga.jpg\",\"is_reusable\": true} }]}]}}}" ;
            String templete ="\"attachment\": { \"type\": \"template\",\"payload\": { \"template_type\": \"generic\", \"elements\": [{ \"title\": \"Ai Chat Bot Communities\",  \"subtitle\": \"Communities to Follow\", \"image_url\": \"http://1u88jj3r4db2x4txp44yqfj1.wpengine.netdna-cdn.com/...\",\"buttons\": [{ \"type\": \"web_url\", \"url\": \"https://www.facebook.com/groups/aichatbots/\",\"title\": \"FB Chatbot Group\"}, { \"type\": \"web_url\",\"url\": \"https://www.reddit.com/r/Chat_Bots/\",\"title\": \"Chatbots on Reddit\" },{ \"type\": \"web_url\", \"url\": \"https://twitter.com/aichatbots\", \"title\": \"Chatbots on Twitter\" }], }, { \"title\": \"Chatbots FAQ\",\"subtitle\": \"Aking the Deep Questions\",\"image_url\": \"https://tctechcrunch2011.files.wordpress.com/2016/04/facebook-chatbots.png?w=738\","
                  +"\"buttons\": [{\"type\": \"postback\", \"title\": \"What's the benefit?\", \"payload\": \"Chatbots make content interactive instead of static\", },{\"type\": \"postback\", \"title\": \"What can Chatbots do\",\"payload\": \"One day Chatbots will control the Internet of Things! You will be able to control your homes temperature with a text\",}, { \"type\": \"postback\", \"title\": \"The Future\", \"payload\": \"Chatbots are fun! One day your BFF might be a Chatbot\", }], },  { \"title\": \"Learning More\", \"subtitle\": \"Aking the Deep Questions\", \"image_url\": \"http://www.brandknewmag.com/wp-content/uploads/2015/12/cortana.jpg\", \"buttons\": [{ \"type\": \"postback\",\"title\": \"AIML\",\"payload\": \"Checkout Artificial Intelligence Mark Up Language. Its easier than you think!\",},{"+
                  "\"type\": \"postback\",\"title\": \"Machine Learning\",\"payload\": \"Use python to teach your maching in 16D space in 15min\", }, { \"type\": \"postback\",\"title\": \"Communities\", \"payload\": \"Online communities & Meetups are the best way to stay ahead of the curve!\", }], }]    }   } }";
            URL url = new URL(query);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setConnectTimeout(5000);
	        conn.setRequestProperty("Content-Type", "application/json");
	        conn.setDoOutput(true);
	        conn.setDoInput(true);
	        conn.setRequestMethod("POST");

	        OutputStream os = conn.getOutputStream();
	        os.write(json.getBytes("UTF-8"));
	        os.close();

	        // read the response
	        InputStream in = new BufferedInputStream(conn.getInputStream());
	        String result = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
	        JSONObject jsonObject = new JSONObject(result);

	        in.close();
	        conn.disconnect(); 
	        return jsonObject;
	}
	    
	    
	    
	    public String TestHtml (){
	        String html = "<tr> <td> Xin chao </td> <td> Xin chao </td> </tr>";
	        return html;
	    }
	    
	    public String Delete(String id) {
	    	if (database.delete(id)==1){
	    		return "Delete success";
	    	}
	    	else return "No success";
	    	
	    }
	    
	    public String selectWhere (int id ) throws ClassNotFoundException{
	    	ChatBot cb = database.SelectWhere(id);			
			JSONObject object = new JSONObject();
			object.put("id",cb.GetId());
			object.put("user_chat",cb.GetUserChat());
			object.put("bot_chat",cb.GetBotChat());		
			return object.toString();
	    }
	
	
}
