package message;

import java.io.Serializable;

public class Message implements Serializable, MessageType 
{
  private int type;
  private Object content;

  public Message(int type, Object content) 
  {
    this.type = type;
    this.content = content;
  }

  public int getType() 
  {
    return type;
  }

  public Object getContent() 
  {
    return content;
  }
}