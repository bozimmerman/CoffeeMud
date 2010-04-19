package com.planet_ink.coffee_mud.application;
import java.util.*;
import java.io.*;
import java.net.*;
import java.util.regex.*;
/*
Copyright 2000-2010 Bo Zimmerman

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
/**
 * Work in progress.
 * @author Bo Zimmerman
 *
 */
public class AutoPlayTester
{
	private BufferedReader 			in = null;
	private BufferedWriter		 	out = null;
	private LinkedList<String> 		buffer = new LinkedList<String>();
	@SuppressWarnings("unused")
	private boolean 				mudUsesAccountSystem=false;
	
	public LinkedList<String> bufferFill() throws IOException
	{
		LinkedList<String> temp = new LinkedList<String>();
		int c;
		StringBuffer buf=new StringBuffer("");
		int lastc=0;
		
		try
		{
			while((c=in.read()) >=0)
			{
				if(c==13 || c==10)
				{
					if((c==13 && lastc != 10)
					||(c==10 && lastc != 13))
					{
						temp.add(globalReactionary(buf.toString()));
						buf.setLength(0);
					}
				}
				else
					buf.append((char)c);
				lastc=c;
			}
		}
		catch(Exception e)
		{
			
		}
		if(buf.length()>0)
			temp.add(globalReactionary(buf.toString()));
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
		System.out.println(s);
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
	
	public void writeln(String s) throws IOException
	{
		try{Thread.sleep(1000);}catch(Exception e){}
		out.write(s+"\n");
		out.flush();
	}
	
	public boolean login(String host, int port, String username)
	{
		try
		{
			Socket sock=new Socket(host,port);
			sock.setSoTimeout(1000);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			String s = waitFor("(?>Name|Account name).*");
			if(!s.toLowerCase().startsWith("Name"))
				mudUsesAccountSystem = true;
			writeln("Zac");
			s=waitFor("(?>Password.*|(.+does not exist.))");
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
		AutoPlayTester player = new AutoPlayTester();
		String name="boobie";
		String host="localhost";
		int port = 5555;
		if(!player.login(host,port,name))
			System.err.println("Failed login");
		else
		{
			
		}
	}
}
