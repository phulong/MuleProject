package model;

public class ChatBot {

	
	private int id;
	private String user_chat;
	private String bot_chat;
	
	public ChatBot (){
		
	}
	public ChatBot (int id, String user_chat , String bot_chat)
	{
		this.id = id;
		this.user_chat = user_chat;
		this.bot_chat = bot_chat;
	}	
	
	public ChatBot ( String user_chat , String bot_chat)
	{		
		this.user_chat = user_chat;
		this.bot_chat = bot_chat;
	}		
	
	public int GetId (){
		return this.id;
	}
	public void SetId (int id ){
		this.id = id;
	}
	public String GetUserChat () {
		return this.user_chat;
	}
	public void SetUserChat (String user_chat){
		this.user_chat = user_chat;
	}
	public String GetBotChat() {
		return this.bot_chat;
	}
    public void SetBotChat (String bot_chat){
    	this.bot_chat = bot_chat;
    }
}
