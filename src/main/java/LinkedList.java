package dsproject;
import java.io.File;



public class LinkedList
{
	
	Node head;
	int counter=0;
	
	public LinkedList()
	{
		head = null;
	}
	
	public boolean isEmpty()
	{
		return head==null;
	}
	public int size(){
		return counter;
	}
	public boolean insertf(File obj)
	{
		if (isEmpty())
		{
			head= new Node();
			head.setNext(new Node(obj));
			counter++;
			//print();
			return true;
		}
		else
		{
			Node newNode = new Node(obj);
			if(head.getNext() == null){
				head.setNext(newNode);
			}
			else{
				Node temp = head.getNext();

				head.setNext(newNode);
				newNode.setNext(temp);


			}

			if(head == null){
				System.out.println("NULLLLLL");
			}
			counter++;
			//print();
			return true;
		}
		
	}
	
	public boolean insertb(File obj)
	{
		if (isEmpty())
		{
			head= new Node(obj);
			return true;
		}
		else
		{
			Node temp=head;
			
			while (temp.getNext()!=null){				
				temp=temp.getNext();
			}
			Node newNode = new Node(obj);
			temp.setNext(newNode);
			if(temp.getNext() == null){
				System.out.println("NULL");
			}
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
				for (int a=0; a<index-1;a++)
				{
					temp=temp.getNext();
				}
				temp2=temp.next;
				Node temp1=temp.getNext();
				temp1=new Node(obj);
				temp1.next=temp2;
				counter++;
				return true;
			}
	
		}
		return false;
		
	}
	
	public File returna(int index)// returns data at given position
	{
		Node temp=head;
		if(isEmpty())
		{
			System.out.println("Empty List");
			
		}
		else
		{
			if(index > counter)
				System.out.println("Out of bounds");
			else
			{
				for (int a=0 ; a <= index;a++){
					temp=temp.getNext();					
				}
				if(temp == null){
					System.out.println("NULL @ " + index + "/" + counter );

				}
				//print();				
				return temp.getData();					

			
			}
		}

		return temp.getData();	
	}
		
		
	public void concat(LinkedList obj){
		//print();
		if(this == obj){
			System.out.println("equals");
			return;
		}
		int size = obj.size();
		for(int counter1 = 0; counter1< size; counter1++){
//			System.out.println("Start: ");
			File temp = obj.returna(counter1);
			if(temp!=null){
				//System.out.println("Adding " + temp.getName());
				boolean result = insertf(temp);
				if(!result)
					System.out.println("Not added");
//				System.out.println("End ");
//				print();
			}
		}
	}
	public void print(){
		Node temp = head;
		while(temp != null){
			if(temp.getData() != null){
				System.out.print(temp.getData().getAbsoluteFile().toString() + " ");
			}
			
			temp = temp.getNext();
		}
		System.out.println(" size: " + counter);
	}
	
}