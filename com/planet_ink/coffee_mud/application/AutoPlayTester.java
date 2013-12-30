package com.planet_ink.coffee_mud.application;
import java.util.*;
import java.io.*;
import java.net.*;
import java.util.regex.*;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
/*
Copyright 2000-2014 Bo Zimmerman

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
	private Socket 				sock=null;
	private BufferedReader 		in = null;
	private BufferedWriter		out = null;
	private LinkedList<String> 	inbuffer = new LinkedList<String>();
	private LinkedList<String> 	outbuffer = new LinkedList<String>();
	private String 				name="boobie";
	private String 				host="localhost";
	private int 				port = 5555;
	private String				filename="resources/autoplayer/autoplay.js";
	
	public AutoPlayTester(String host, int port, String charName, String script)
	{
		this.host=host;
		this.port=port;
		this.name=charName;
		this.filename=script;
	}
	
	public LinkedList<String> bufferFill() throws IOException
	{
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
						inbuffer.add(globalReactionary(buf.toString()));
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
			inbuffer.add(globalReactionary(buf.toString()));
		return inbuffer;
	}
	
	public String globalReactionary(String s)
	{
		System.out.println(s);
		return s;
	}
	
	public String[] waitFor(String regEx, int num) throws IOException
	{
		long waitUntil = System.currentTimeMillis() + (60 * 1000);
		StringBuilder buildUp=new StringBuilder("");
		Pattern p=Pattern.compile(regEx);
		while(System.currentTimeMillis() < waitUntil)
		{
			bufferFill();
			if(inbuffer.size()==0)
			{
				try{Thread.sleep(100);}catch(Exception e){}
			}
			else
			{
				String s=inbuffer.removeFirst();
				outbuffer.add(s);
				if(buildUp.length()>0)
					buildUp.append(" ");
				buildUp.append(s);
				while(outbuffer.size() > 1000)
					outbuffer.removeFirst();
				Matcher m=p.matcher(s);
				if(!m.matches())
					m=p.matcher(buildUp.toString());
				if(m.matches())
				{
					if(m.groupCount()>=num)
					{
						String[] set=new String[num];
						for(int i=0;i<num;i++)
							set[i]=m.group(i+1);
						return set;
					}
					return new String[]{s};
				}
			}
		}
		throw new IOException("wait for "+regEx+" timed out.");
	}
	
	public void writeln(String s) throws IOException
	{
		System.out.println(s);
		try{Thread.sleep(500);}catch(Exception e){}
		out.write(s+"\n");
		out.flush();
	}
	
	public boolean login()
	{
		try
		{
			sock=new Socket(host,port);
			sock.setSoTimeout(100);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			try{Thread.sleep(1000);}catch(Exception e){}
			return true;
		}
		catch(java.io.IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public String getJavaScript(String filename)
	{
		StringBuilder js=new StringBuilder("");
		try
		{
			BufferedReader br=new BufferedReader(new FileReader(filename));
			String s=br.readLine();
			while(s!=null)
			{
				if(s.trim().startsWith("//include "))
					js.append(getJavaScript(s.trim().substring(10)));
				else
					js.append(s).append("\n");
				s=br.readLine();
			}
			br.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		return js.toString();
	}
	
	public void run()
	{
		System.out.println("Executing: "+filename);
		String js=getJavaScript(filename);
		
		Context cx = Context.enter();
		try
		{
			JScriptEvent scope = new JScriptEvent(this);
			cx.initStandardObjects(scope);
			scope.defineFunctionProperties(JScriptEvent.functions, JScriptEvent.class,
										   ScriptableObject.DONTENUM);
			cx.evaluateString(scope, js.toString(),"<cmd>", 1, null);
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
		public static final String[] functions={ "tester", "toJavaString", "writeLine", "login", "stdout", 
												 "stderr", "waitFor", "waitForMultiMatch", "startsWith", 
												 "name","rand","sleep"};
		public AutoPlayTester tester() { return testObj;}
		public String toJavaString(Object O){return Context.toString(O);}
		public boolean startsWith(Object O1, Object O2){ try { return toJavaString(O1).startsWith(toJavaString(O2)); } catch(Exception e) {return false; } }
		public boolean login(){ return testObj.login();}
		public String name() { return testObj.name;}
		public void stdout(Object O) { try { System.out.println(toJavaString(O)); } catch(Exception e) { } }
		public void sleep(Object O) { try { Thread.sleep(Long.valueOf(toJavaString(O)).longValue()); } catch(Exception e) { } }
		public void stderr(Object O) { try { System.err.println(toJavaString(O)); } catch(Exception e) { } }
		public int rand(int x){ int y=(int)Math.round(Math.floor(Math.random() * ((x)-0.001))); return (y>0)?y:-y;}
		public Object waitFor(Object regexO)
		{
			try {
				return testObj.waitFor(toJavaString(regexO),1)[0];
			} catch(Exception e) { return null; }
		}
		public Object waitForMultiMatch(Object regexO, Object numMatches)
		{
			try {
				return testObj.waitFor(toJavaString(regexO),Integer.parseInt(toJavaString(numMatches)));
			} catch(Exception e) { return null; }
		}
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
