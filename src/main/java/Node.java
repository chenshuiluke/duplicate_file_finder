
import java.io.File;

public class Node
{
  File data;
  Node next;
  
  public Node()
  {
	  data= null;
	  next=null;
	  
  }
  
  public Node(File data, Node next)
  {
	  this.data=data;
	  this.next=next;
  }
  
  
  public Node(File data)
  {
	  this.data=data;
  }
  
  public Node(Node next)
  {
	  this.next=next;
  }
  
  public Node getNext()
  {
	  return next;
  }
  
  public File getData()
  {
	  return data;
  }


}