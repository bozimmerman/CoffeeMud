package com.planet_ink.coffee_mud.application;
import java.util.*;
import java.io.*;
import java.net.*;
import java.util.regex.*;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
/*
Copyright 2000-2011 Bo Zimmerman

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
@SuppressWarnings("unused")
public class AutoPlayTester
{
	private BufferedReader 		in = null;
	private BufferedWriter		out = null;
	private LinkedList<String> 	buffer = new LinkedList<String>();
	private String 				name="boobie";
	private String 				host="localhost";
	private int 				port = 5555;
	private String				filename="/resources/autoplayer/apprentice.js";
	private boolean 			mudUsesAccountSystem=false;
	
	public AutoPlayTester(String host, int port, String charName, String script)
	{
		this.host=host;
		this.port=port;
		this.name=charName;
		this.filename=script;
	}
	
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
	
	public boolean login()
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
		}
		catch(java.io.IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public void run()
	{
        Context cx = Context.enter();
        try
        {
            JScriptEvent scope = new JScriptEvent(this);
            cx.initStandardObjects(scope);
            scope.defineFunctionProperties(JScriptEvent.functions, JScriptEvent.class,
                                           ScriptableObject.DONTENUM);
            cx.evaluateString(scope, "","<cmd>", 1, null);
        }
        catch(Exception e)
        {
            System.err.println("JSCRIPT Error: "+e.getMessage());
        }
        Context.exit();
	}
	
    protected static class JScriptEvent extends ScriptableObject
    {
        public String getClassName(){ return "JScriptEvent";}
        static final long serialVersionUID=43;
        protected AutoPlayTester testObj;
        public static final String[] functions={ "tester", "toJavaString", "writeLine"};
        public AutoPlayTester tester() { return testObj;}
        public String toJavaString(Object O){return Context.toString(O);}
        public boolean writeLine(Object O) 
        {
        	try {
	        	testObj.writeln(toJavaString(O));
	        	return true;
        	} catch(Exception e) { return false; }
        }
        
        public JScriptEvent(AutoPlayTester testObj)
        {
        	this.testObj=testObj;
        }
    }
    
    public final static int s_int(final String INT)
    {
        try{ return Integer.parseInt(INT); }
        catch(Exception e){ return 0;}
    }
    
	public static void main(String[] args)
	{
		System.out.println("Not yet implemented.");
		if(args.length<4)
		{
			System.out.println("AutoPlayTester");
			System.out.println("AutoPlayTester [host] [port] [character name] [script path]");
			System.exit(-1);
		}
		StringBuilder path=new StringBuilder(args[3]);
		for(int i=4;i<args.length;i++)
			path.append(" ").append(args[i]);
		AutoPlayTester player = new AutoPlayTester(args[0],s_int(args[1]),args[2],path.toString());
		player.run();
	}
}
