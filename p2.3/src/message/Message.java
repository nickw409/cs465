package message;

import java.io.Serializable;

public class Message implements Serializable, MessageTypes
{
  private int type;
  private Object content;

  // Constructors
  public Message(int type, Object content) 
  {
    this.type = type;
    this.content = content;
  }

  // Type getter method
  public int getType() 
  {
    return type;
  }

  // Content getter method
  public Object getContent() 
  {
    return content;
  }
}