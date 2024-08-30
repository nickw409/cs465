class MultithreadingDemo extends Thread {
  public void run()
  {
    try
    {
      System.out.println("Thread " + Thread.currentThread().threadId() 
                          + " is running");
    }
    catch (Exception e)
    {
      System.out.println(e);
    }
  }
}

public class Multithreading {
  public static void main(String[] args)
  {
    for (int i = 0; i < 20; i++)
    {
      MultithreadingDemo thread = new MultithreadingDemo();
      thread.start();
    }
  }
}