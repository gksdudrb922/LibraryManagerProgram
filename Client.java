package cse3040fp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	static class ClientSender extends Thread
	{
		private Socket socket;
		private DataOutputStream out;
		private String name;
		ClientSender(Socket socket,String name)
		{
			this.socket=socket;
			try
			{
				out=new DataOutputStream(socket.getOutputStream());
				this.name=name;
			}
			catch(Exception e) {}
		}
		@SuppressWarnings("all")
		public void run()
		{
			Scanner scanner=new Scanner(System.in);
			try
			{
				if(out!=null)
				{
					out.writeUTF(name);
				}
				while(out!=null)
				{
					try
					{
						sleep(100);
					}
					catch(InterruptedException e) {}
					System.out.print(name+">> ");
					String command=scanner.nextLine();
					if(command.equals("add"))
					{
						commandAdd(command,scanner);
					}
					else if(command.equals("borrow"))
					{
						commandBorrow(command,scanner);
					}
					else if(command.equals("return"))
					{
						commandReturn(command,scanner);
					}
					else if(command.equals("info"))
					{
						commandInfo(command);
					}
					else if(command.equals("search"))
					{
						commandSearch(command,scanner);
					}
					else
					{
						System.out.println("[available commands]");
						System.out.println("add: add a new book to the list of books.");
						System.out.println("borrow: borrow a book from the library.");
						System.out.println("return: return a book to the library.");
						System.out.println("info: show list of books I am currently borrowing.");
						System.out.println("search: search for books.");
					}
				}
			}
			catch(IOException e) {}
		}
		public void commandAdd(String command,Scanner scanner)
		{
			try
			{
				System.out.print("add-title> ");
				String title=scanner.nextLine().trim();
				if(title.isEmpty())
				{
					return;
				}
				System.out.print("add-author> ");
				String author=scanner.nextLine().trim();
				if(author.isEmpty())
				{
					return;
				}
				out.writeUTF(command+"\t"+title+"\t"+author);
			}
			catch(IOException e) {}
		}
		public void commandBorrow(String command,Scanner scanner)
		{
			try
			{
				System.out.print("borrow-title> ");
				String title=scanner.nextLine().trim();
				if(title.isEmpty())
				{
					return;
				}
				out.writeUTF(command+"\t"+title);
			}
			catch(IOException e) {}
		}
		public void commandReturn(String command,Scanner scanner)
		{
			try
			{
				System.out.print("return-title> ");
				String title=scanner.nextLine().trim();
				if(title.isEmpty())
				{
					return;
				}
				out.writeUTF(command+"\t"+title);
			}
			catch(IOException e) {}
		}
		public void commandInfo(String command)
		{
			try
			{
				out.writeUTF(command);
			}
			catch(IOException e) {}
		}
		public void commandSearch(String command,Scanner scanner)
		{
			try
			{
				String target;
				while(true)
				{
					System.out.print("search-string> ");
					target=scanner.nextLine();
					if(target.isEmpty())
					{
						return;
					}
					if(target.length()>=3)
					{
						break;
					}
					System.out.println("Search string must be longer than 2 characters.");
				}
				out.writeUTF(command+"\t"+target);
			}
			catch(IOException e) {}
		}
	}
	static class ClientReceiver extends Thread
	{
		private Socket socket;
		private DataInputStream in;
		
		ClientReceiver(Socket socket)
		{
			this.socket=socket;
			try
			{
				in=new DataInputStream(socket.getInputStream());
			}
			catch(IOException e) {}
		}
		
		public void run()
		{
			while(in!=null)
			{
				try
				{
					System.out.println(in.readUTF());
				}
				catch(IOException e) {}
			}
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(args.length!=2)
		{
			System.out.println("Please give the IP address and port number as arguments.");
			System.exit(0);
		}
		try
		{
			Socket socket=new Socket(args[0],Integer.valueOf(args[1]).intValue());
			System.out.println("connected to server.");
			Scanner scanner=new Scanner(System.in);
			String userID;
			while(true)
			{
				System.out.print("Enter userID>> ");
				userID=scanner.nextLine();
				if(check_userID(userID)==false)
				{
					System.out.println("UserID must be a single word with lowercase alphabets and numbers.");
					continue;
				}
				System.out.println("Hello "+userID+"!");
				break;
			}
			Thread sender=new Thread(new ClientSender(socket,userID));
			Thread receiver=new Thread(new ClientReceiver(socket));
			sender.start();
			receiver.start();
		}
		catch(ConnectException ce)
		{
			System.out.println("Connection establishment failed.");
			System.exit(0);
		}
		catch(IOException ie)
		{
			ie.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public static boolean check_userID(String userID)
	{
		if(userID.isEmpty())
			return false;
		String[] words=userID.split(" ");
		if(words.length>=2)
			return false;
		char[] letters=userID.toCharArray();
		for(int i=0;i<letters.length;i++)
		{
			if(!((letters[i]>='a'&&letters[i]<='z')||(letters[i]>='0'&&letters[i]<='9')))
				return false;
		}
		return true;
	}

}
