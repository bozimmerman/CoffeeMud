	package com.planet_ink.coffee_mud.application;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMFile.CMVFSDir;
import com.planet_ink.coffee_mud.core.CMFile.CMVFSFile;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.database.DBConnector;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.JournalMetaData;
import com.planet_ink.coffee_mud.Libraries.interfaces.LanguageLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinnerPlayer;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

	import java.net.*;
import java.util.*;
import java.sql.*;
import java.io.*;

	/*
	Copyright 2000-2018 Bo Zimmerman

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

public class OffLine extends Thread implements MudHost
{
	public static Vector<OffLine> mudThreads=new Vector<OffLine>();
	public static DVector accessed=new DVector(2);
	public static Vector<String> autoblocked=new Vector<String>();

	public static boolean serverIsRunning = false;
	public static boolean isOK = false;

	public boolean acceptConnections=false;
	public String host="MyHost";
	public static String bind="";
	public static String ports="";
	public int port=5555;
	public int state=0;
	ServerSocket servsock=null;
	protected final long startupTime = System.currentTimeMillis();

	public OffLine()
	{
		super("MUD-OffLineServer");
	}

	@Override
	public ThreadGroup threadGroup()
	{
		return Thread.currentThread().getThreadGroup();
	}

	public static void fatalStartupError(Thread t, int type)
	{
		String errorInternal=null;
		switch(type)
		{
		case 1:
			errorInternal="ERROR: initHost() will not run without properties. Exiting.";
			break;
		case 2:
			break;
		case 3:
			break;
		case 4:
			errorInternal="Fatal exception. Exiting.";
			break;
		case 5:
			errorInternal="OffLine Server did not start. Exiting.";
			break;
		default:
			break;
		}
		System.out.println(errorInternal);
		CMLib.killThread(t,500,1);
	}

	private static boolean initHost(Thread t)
	{

		if (!isOK)
		{
			CMLib.killThread(t,500,1);
			return false;
		}

		while (!serverIsRunning && isOK)
		{
			CMLib.s_sleep(1000);
		}
		if (!isOK)
		{
			fatalStartupError(t,5);
			return false;
		}

		for(int i=0;i<mudThreads.size();i++)
			mudThreads.elementAt(i).acceptConnections=true;
		System.out.println("Initialization complete.");
		return true;
	}

	private void closeSocks(Socket sock, BufferedReader in, PrintWriter out)
	{
		try
		{
			if(sock!=null)
			{
				if(out!=null)
					out.flush();
				sock.shutdownInput();
				sock.shutdownOutput();
				if(out!=null)
					out.close();
				sock.close();
			}
			in=null;
			out=null;
			sock=null;
		}
		catch(final IOException e)
		{
		}
	}

	public StringBuffer getFile(String fileName)
	{
		StringBuffer offLineText=(StringBuffer)Resources.getResource(fileName);
		if(offLineText==null)
		{
			offLineText=new StringBuffer("");
			FileInputStream fin = null;
			try
			{
				fin = new FileInputStream(fileName);
				while(fin.available()>0)
					offLineText.append((char)fin.read());
				Resources.submitResource(fileName,offLineText);
			}
			catch(final Exception e){e.printStackTrace();}
			finally
			{
				try
				{
					if ( fin != null )
					{
						fin.close();
						fin = null;
					}
				}
				catch( final IOException ignore )
				{

				}
			}
		}
		return offLineText;
	}

	@Override
	public void acceptConnection(Socket sock)
	throws SocketException, IOException
{
		sock.setSoLinger(true,3);
		state=1;

		if (acceptConnections)
		{
			String address="unknown";
			try
			{
				address=sock.getInetAddress().getHostAddress().trim();
			}
			catch(final Exception e)
			{
			}
			System.out.println("Connection from "+address+": "+port);
			// now see if they are banned!
			int proceed=0;

			int numAtThisAddress=0;
			final long ConnectionWindow=(180*1000);
			final long LastConnectionDelay=(5*60*1000);
			boolean anyAtThisAddress=false;
			final int maxAtThisAddress=6;
			try
			{
				for(int a=accessed.size()-1;a>=0;a--)
				{
					if((((Long)accessed.elementAt(a,2)).longValue()+LastConnectionDelay)<System.currentTimeMillis())
						accessed.removeElementAt(a);
					else
					if(((String)accessed.elementAt(a,1)).trim().equalsIgnoreCase(address))
					{
						anyAtThisAddress=true;
						if((((Long)accessed.elementAt(a,2)).longValue()+ConnectionWindow)>System.currentTimeMillis())
							numAtThisAddress++;
					}
				}
				if(autoblocked.contains(address.toUpperCase()))
				{
					if(!anyAtThisAddress)
						autoblocked.remove(address.toUpperCase());
					else
						proceed=2;
				}
				else
				if(numAtThisAddress>=maxAtThisAddress)
				{
					autoblocked.addElement(address.toUpperCase());
					proceed=2;
				}
			}
			catch(final java.lang.ArrayIndexOutOfBoundsException e)
			{
			}

			accessed.addElement(address,Long.valueOf(System.currentTimeMillis()));
			if(proceed!=0)
			{
				System.out.println("Blocking a connection from "+address+" on port "+port);
				final PrintWriter out = new PrintWriter(sock.getOutputStream());
				out.println("\n\rOFFLINE: Blocked\n\r");
				out.flush();
				if(proceed==2)
					out.println("\n\rYour address has been blocked temporarily due to excessive invalid connections.  Please try back in " + (LastConnectionDelay/60000) + " minutes, and not before.\n\r\n\r");
				else
					out.println("\n\rYou are unwelcome.  No one likes you here. Go away.\n\r\n\r");
				out.flush();
				out.close();
				sock = null;
			}
			else
			{
				state=2;
				final String fileName="resources"+File.separator+"text"+File.separator+"down.txt";
				final StringBuffer offLineText=getFile(fileName);
				try
				{
					sock.setSoTimeout(300);
					final OutputStream rawout=sock.getOutputStream();
					final InputStream rawin=sock.getInputStream();
					rawout.write('\n');
					rawout.write('\n');
					rawout.flush();

					//out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(rawout, "UTF-8")));
					//in = new BufferedReader(new InputStreamReader(rawin, "UTF-8"));
					BufferedReader in;
					PrintWriter out;
					out = new PrintWriter(new OutputStreamWriter(rawout,"iso-8859-1"));
					in = new BufferedReader(new InputStreamReader(rawin,"iso-8859-1"));

					if(offLineText!=null)
						out.print(offLineText);
					out.flush();
					CMLib.s_sleep(250);
					closeSocks(sock,in,out);
				}
				catch(final SocketException e)
				{
				}
				catch(final IOException e)
				{
				}
				closeSocks(sock,null,null);
				sock=null;
			}
		}
		else
		{
			final String fileName="resources"+File.separator+"text"+File.separator+"offline.txt";
			final StringBuffer rejectText=getFile(fileName);
			final PrintWriter out = new PrintWriter(sock.getOutputStream());
			out.flush();
			out.println(rejectText);
			out.flush();
			out.close();
			CMLib.s_sleep(250);
			sock = null;
		}
	}

	@Override
	public void run()
	{
		final int q_len = 6;
		Socket sock=null;
		serverIsRunning = false;
		CMLib.initialize(); // forces this thread to HAVE a library
		Resources.initialize();
		if (!isOK)
		{
			System.err.println("Cancelling MUD server on port "+port);
			return;
		}

		InetAddress bindAddr = null;

		if (bind.length() > 0)
		{
			try
			{
				bindAddr = InetAddress.getByName(bind);
			}
			catch (final UnknownHostException e)
			{
				System.err.println("ERROR: MUD Server could not bind to address " + bind);
			}
		}

		try
		{
			servsock=new ServerSocket(port, q_len);
			System.out.println("Off-Line Server started on port: "+port);
			if (bindAddr != null)
				System.out.println("Off-Line Server bound to: "+bindAddr.toString());
			serverIsRunning = true;

			while(true)
			{
				try
				{
					state=0;
					sock=servsock.accept();
					acceptConnection(sock);
				}
				catch(final Exception t)
				{
					if((!(t instanceof java.net.SocketException))
					||(t.getMessage()==null)
					||(t.getMessage().toLowerCase().indexOf("socket closed")<0))
					{
						t.printStackTrace(System.err);
					}
				}
			}
		}
		catch(final Exception t)
		{
			t.printStackTrace(System.err);
			if (!serverIsRunning)
				isOK = false;
		}

		System.out.println("Off-Line Server cleaning up.");

		try
		{
			if(servsock!=null)
				servsock.close();
			if(sock!=null)
				sock.close();
		}
		catch(final IOException e)
		{
		}

		System.out.println("Off-Line Server on port "+port+" stopped!");
	}

	@Override
	public String getStatus()
	{
		return "OFFLINE";
	}

	@Override
	public void shutdown(Session S, boolean keepItDown, String externalCommand)
	{
		interrupt(); // kill the damn archon thread.
	}

	public static void defaultShutdown()
	{
	}

	@Override
	public void interrupt()
	{
		if(servsock!=null)
		{
			try
			{
				servsock.close();
				servsock = null;
			}
			catch(final IOException e)
			{
			}
		}
		super.interrupt();
	}

	@Override
	public String getHost()
	{
		return host;
	}

	@Override
	public int getPort()
	{
		return port;
	}

	public static void main(String a[])
	{
		CMProps page=null;
		CMLib.initialize(); // forces this thread to HAVE a library

		String nameID="";
		String iniFile="coffeemud.ini";
		if(a.length>0)
		{
			for (final String element : a)
				nameID+=" "+element;
			nameID=nameID.trim();
			final List<String> V=CMParms.cleanParameterList(nameID);
			for(int v=0;v<V.size();v++)
			{
				final String s=V.get(v);
				if(s.toUpperCase().startsWith("BOOT=")&&(s.length()>5))
				{
					iniFile=s.substring(5);
					V.remove(v);
					v--;
				}
			}
			nameID=CMParms.combine(V,0);
		}
		if(nameID.length()==0)
			nameID="Unnamed CoffeeMud";
		try
		{
			while(true)
			{
				page=CMProps.loadPropPage(iniFile);
				if ((page==null)||(!page.isLoaded()))
				{
					System.out.println("ERROR: Unable to read ini file: '"+iniFile+"'.");
					System.exit(-1);
					return;
				}

				isOK = true;
				bind=page.getStr("BIND");

				System.out.println();
				System.out.println("CoffeeMud Off-Line");
				System.out.println("(C) 2000-2018 Bo Zimmerman");
				System.out.println("http://www.coffeemud.org");

				if(OffLine.isOK)
				{
					mudThreads=new Vector<OffLine>();
					String ports=page.getProperty("PORT");
					int pdex=ports.indexOf(',');
					while(pdex>0)
					{
						final OffLine mud=new OffLine();
						mud.acceptConnections=false;
						mud.port=CMath.s_int(ports.substring(0,pdex));
						ports=ports.substring(pdex+1);
						mud.start();
						mudThreads.addElement(mud);
						pdex=ports.indexOf(',');
					}
					final OffLine mud=new OffLine();
					mud.acceptConnections=false;
					mud.port=CMath.s_int(ports);
					mud.start();
					mudThreads.addElement(mud);
				}

				final StringBuffer str=new StringBuffer("");
				for(int m=0;m<mudThreads.size();m++)
				{
					final MudHost mud=mudThreads.elementAt(m);
					str.append(" "+mud.getPort());
				}
				ports=str.toString();

				if(initHost(Thread.currentThread()))
					mudThreads.firstElement().join();

				System.gc();
				System.runFinalization();

			}
		}
		catch(final InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void setAcceptConnections(boolean truefalse)
	{
		acceptConnections=truefalse;
	}

	@Override
	public boolean isAcceptingConnections()
	{
		return acceptConnections;
	}

	@Override
	public List<Runnable> getOverdueThreads()
	{
		return new Vector<Runnable>();
	}

	@Override
	public long getUptimeSecs()
	{
		return (System.currentTimeMillis()-startupTime)/1000;
	}

	@Override
	public String getLanguage()
	{
		return "English";
	}

	@Override
	public String executeCommand(String cmd)
		throws Exception
	{
		throw new Exception("Not implemented");
	}
}
