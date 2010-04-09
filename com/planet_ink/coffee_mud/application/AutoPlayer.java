package com.planet_ink.coffee_mud.application;
import java.util.*;
import java.io.*;
import java.net.*;
import java.util.regex.*;

public class AutoPlayer 
{
	private BufferedReader 			in = null;
	@SuppressWarnings("unused")
	private BufferedOutputStream 	out = null;
	private LinkedList<String> 		buffer = new LinkedList<String>();
	
	public LinkedList<String> bufferFill() throws IOException
	{
		String s;
		LinkedList<String> temp = new LinkedList<String>();
		while((s=in.readLine()) != null)
			temp.add(globalReactionary(s));
		return temp;
	}
	
	public void fileBuffer(LinkedList<String> newBuf)
	{
		buffer.addAll(newBuf);
		while(buffer.size() > 1000)
			buffer.removeFirst();
	}

	public String globalReactionary(String s)
	{
		return s;
	}
	
	public String waitFor(String regEx) throws IOException
	{
		long waitUntil = System.currentTimeMillis() + (60 * 1000);
		while(System.currentTimeMillis() < waitUntil)
		{
			LinkedList<String> readBuf = bufferFill();
			try
			{
				for(String s : readBuf)
				{
					if(Pattern.matches(regEx, s))
						return s;
				}
			}
			finally
			{
				fileBuffer(readBuf);
			}
		}
		throw new IOException("wait for "+regEx+" timed out.");
	}
	
	public boolean login(String host, int port, String username)
	{
		try
		{
			Socket s=new Socket(host,port);
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			out = new BufferedOutputStream(s.getOutputStream());
			
		}
		catch(java.io.IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public static void main(String[] args)
	{
		System.out.println("Not yet implemented.");
		AutoPlayer player = new AutoPlayer();
		String name="boobie";
		String host="localhost";
		int port = 23;
		if(!player.login(host,port,name))
			System.err.println("Failed login");
		else
		{
			
		}
	}
}
