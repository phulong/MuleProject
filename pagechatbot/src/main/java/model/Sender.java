package model;

public class Sender {	
		  private String PSID;
		  private String message;
		  
		  public Sender (String PSID, String message){	
			  this.PSID = PSID;
			  this.message = message;
		  }
		  
		  public String getPSID (){
			  return this.PSID;
		  }
		  public void setPSID (String PSID){
			  this.PSID = PSID;
		  }
		  public String getMessage (){
			  return this.message;
		  }
		  public void setMessage (String message ){
			  this.message = message;
		  }
}
