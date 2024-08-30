package message;
/**
 * MessageType
 */
public interface MessageType {
  public static final int JOIN = 0;
  public static final int LEAVE = 1;
  public static final int NOTE = 2;
  public static final int SHUTDOWN = 3;
  public static final int SHUTDOWN_ALL = 4;
}