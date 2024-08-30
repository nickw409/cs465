package message;

import java.io.Serializable;

public class Message implements Serializable, MessageType {
  private MessageType type;
  private Object content;

  public Message(MessageType type, Object content) {
    this.type = type;
    this.content = content;
  }

  public MessageType getType() {
    return type;
  }

  public Object getContent() {
    return content;
  }

  public void setType(MessageType type) {
    this.type = type;
  }

  public void setContent(Object content) {
    this.content = content;
  }
  
}