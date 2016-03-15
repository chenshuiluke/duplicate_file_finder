import java.io.File;



public class LinkedList
{
	
	Node head;
	static int counter=0;
	
	public LinkedList()
	{
		head = null;
	}
	
	public boolean isEmpty()
	{
		return head==null;
	}
	
	public boolean insertf(File obj)
	{
		if (isEmpty())
		{
			head= new Node(obj);
			counter++;
			return true;
		}
		else
		{
			head= new Node(obj, head);
			counter++;
			return true;
		}
		
	}
	
	public boolean insertb(File obj)
	{
		if (isEmpty())
		{
			head= new Node(obj);
			counter++;
			return true;
		}
		else
		{
			Node temp=head;
			
			while (temp.next!=null){
				
				temp=temp.next;
			}
			
			temp.next=new Node(obj);
			counter++;
			return true;
		}
		
		
	}
	
	public boolean inserta(File obj, int index)// insert anywhere
	{
		Node temp=new Node();
		Node temp2= new Node();
		
		
		if(isEmpty())
		{
			head= new Node(obj);
			return true;
		}
		else
		{
			if(index>(counter+1))
				System.out.println("Out of bounds");
			else
			{
				for (int a=1; a<index-1;a++)
				{
					temp=temp.getNext();
				}
				temp2=temp.next;
				Node temp1=temp.getNext();
				temp1=new Node(obj);
				temp1.next=temp2;
			}
			return true;	
		}
		
		
	}
	
	public File returna(File obj, int index)// returns data at given position
	{
		Node temp=null;
		if(isEmpty())
		{
			System.out.println("Empty List");
			
		}
		else
		{
			if(index>counter)
				System.out.println("Out of bounds");
			else
			{
				for (int a=0; a<=index;a++)
				temp=temp.getNext();
				
			
			}
				 
		}
		return temp.getData();
		
	}
		
		
	
	
	
}