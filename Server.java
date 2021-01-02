package cse3040fp;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

class Book implements Comparable<Book>
{
	private String title;
	private String author;
	private String borrower;
	Book(String title,String author,String borrower)
	{
		this.title=title;
		this.author=author;
		this.borrower=borrower;
	}
	public void setTitle(String title)
	{
		this.title=title;
	}
	public void setAuthor(String author)
	{
		this.author=author;
	}
	public void setBorrower(String borrower)
	{
		this.borrower=borrower;
	}
	public String getTitle()
	{
		return this.title;
	}
	public String getAuthor()
	{
		return this.author;
	}
	public String getBorrower()
	{
		return this.borrower;
	}
	public int compareTo(Book b)
	{
		
		return this.title.compareToIgnoreCase(b.title);
	}
	public String toString()
	{
		return title+"\t"+author+"\t"+borrower;
	}
}

public class Server {
	private HashMap<String,DataOutputStream> clients;
	private ArrayList<Book> books; 
	private BufferedReader br;
	Server()
	{
		clients=new HashMap<>();
		books=new ArrayList<>();
		Collections.synchronizedMap(clients);
		try
		{
			br=new BufferedReader(new FileReader("books.txt"));
			while(true)
			{
				String line=br.readLine();
				if(line==null) break;
				String[] token=line.split("\t");
				books.add(new Book(token[0],token[1],token[2]));
			}
			Collections.sort(books);
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch(IOException e) {}
	}
	public void start(String[] args)
	{	
		ServerSocket serverSocket=null;
		Socket socket=null;
		try
		{
			serverSocket=new ServerSocket(Integer.valueOf(args[0]).intValue());
			System.out.println("server has started.");
			while(true)
			{
				socket=serverSocket.accept();
				System.out.println("a new connection from ["+socket.getInetAddress()+":"+socket.getPort()+"]");
				ServerReceiver thread=new ServerReceiver(socket);
				thread.start();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	void sendToAll(String msg)
	{
		Iterator<String> it=clients.keySet().iterator();
		while(it.hasNext())
		{
			try
			{
				DataOutputStream out=(DataOutputStream)clients.get(it.next());
				out.writeUTF(msg);
			}
			catch(IOException e) {}
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(args.length!=1)
		{
			System.out.println("Please give the port number as an argument.");
			System.exit(0);
		}

		new Server().start(args);
	}
	class ServerReceiver extends Thread
	{
		private Socket socket;
		private DataInputStream in;
		private DataOutputStream out;
		
		ServerReceiver(Socket socket)
		{	
			this.socket=socket;
			try
			{
				in=new DataInputStream(socket.getInputStream());
				out=new DataOutputStream(socket.getOutputStream());
			}
			catch(IOException e) {}
		}
		public void run()
		{
			String name="";
			try
			{
				name=in.readUTF();
				clients.put(name, out);
				while(in!=null)
				{
					String command_line=in.readUTF();
					String[] token=command_line.split("\t");
					if(token[0].equals("add"))
					{
						addBook(token);
					}
					else if(token[0].equals("borrow"))
					{
						borrowBook(token,name);
					}
					else if(token[0].equals("return"))
					{
						returnBook(token,name);
					}
					else if(token[0].equals("info"))
					{
						infoBook(name);
					}
					else if(token[0].equals("search"))
					{
						searchBook(token);
					}
				}
			}
			catch(IOException e)
			{
				
			}
			finally
			{
				clients.remove(name);
				System.out.println("["+socket.getInetAddress()+":"+socket.getPort()+"]"+" has disconnected.");
			}
		}
		public void addBook(String[] token)
		{
			try
			{
				int i;
				for(i=0;i<books.size();i++)
				{
					if(token[1].equalsIgnoreCase(books.get(i).getTitle()))
					{
						out.writeUTF("The book already exists in the list.");
						break;
					}
				}
				if(i==books.size())
				{
					books.add(new Book(token[1],token[2],"-"));
					Collections.sort(books);
					setFile(books);
					out.writeUTF("A new book added to the list.");
				}	
			}
			catch(IOException e) {}
		}
		public void borrowBook(String[] token,String name)
		{
			try
			{
				int i;
				for(i=0;i<books.size();i++)
				{
					if(token[1].equalsIgnoreCase(books.get(i).getTitle())&&books.get(i).getBorrower().equals("-"))
					{
						books.get(i).setBorrower(name);
						setFile(books);
						out.writeUTF("You borrowed a book. - "+books.get(i).getTitle());
						break;
					}
				}
				if(i==books.size())
				{
					out.writeUTF("The book is not available.");
				}
			}
			catch(IOException e) {}
		}
		public void returnBook(String[] token,String name)
		{
			try
			{
				int i;
				for(i=0;i<books.size();i++)
				{
					if(token[1].equalsIgnoreCase(books.get(i).getTitle())&&books.get(i).getBorrower().equals(name))
					{
						books.get(i).setBorrower("-");
						setFile(books);
						out.writeUTF("You returned a book. - "+books.get(i).getTitle());
						break;
					}
				}
				if(i==books.size())
				{
					out.writeUTF("You did not borrow the book.");
				}
			}
			catch(IOException e) {}
		}
		public void infoBook(String name)
		{
			String[] str=new String[books.size()];
			int count=0;
			try
			{
				int i;
				for(i=0;i<books.size();i++)
				{
					if(books.get(i).getBorrower().equals(name))
					{
						str[count]=(count+1)+". "+books.get(i).getTitle()+", "+books.get(i).getAuthor();
						count++;
					}
				}
				out.writeUTF("You are currently borrowing "+count+" books:");
				for(i=0;i<count;i++)
				{
					out.writeUTF(str[i]);
				}
			}
			catch(IOException e) {}
		}
		public void searchBook(String[] token)
		{
			String[] str=new String[books.size()];
			int count=0;
			try
			{
				int i;
				for(i=0;i<books.size();i++)
				{
					if(books.get(i).getTitle().toLowerCase().indexOf(token[1].toLowerCase())!=-1||books.get(i).getAuthor().toLowerCase().indexOf(token[1].toLowerCase())!=-1)
					{
						str[count]=(count+1)+". "+books.get(i).getTitle()+", "+books.get(i).getAuthor();
						count++;
					}
				}
				out.writeUTF("You search matched "+count+" results.");
				for(i=0;i<count;i++)
				{
					out.writeUTF(str[i]);
				}
			}
			catch(IOException e) {}
		}
		public void setFile(ArrayList<Book> books)
		{
			PrintWriter pw=null;
			try
			{
				pw=new PrintWriter("books.txt");
			}
			catch(FileNotFoundException e)
			{
				e.printStackTrace();
			}
			for(int i=0;i<books.size();i++)
			{
				pw.println(books.get(i));
			}
			pw.close();
		}
	}

}