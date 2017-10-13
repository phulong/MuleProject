

public class Story {
	private int id;
	private String user_chat;
	private String bot_chat;
	
	public void setStory (int id, String user_chat , String bot_chat)
	{
		this.id = id;
		this.user_chat = user_chat;
		this.bot_chat = bot_chat;
	}
	public Story getStory (){
		return this;
	}
	public int GetID (){
		return this.id;
	}
	public String GetUserChat () {
		return this.user_chat;
	}
	public String GetBotChat() {
		return this.bot_chat;
	}
	
}
	
