package com.planet_ink.coffee_mud.web;
import java.io.*;
import java.net.*;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.interfaces.*;

public class HTTPserver extends Thread
{
	public INI page=null;

	public static final float HOST_VERSION_MAJOR=(float)1.0;
	public static final float HOST_VERSION_MINOR=(float)0.0;
	public static Vector webMacros=null;

	// this gets sent in HTTP response
	//  also used by @WEBSERVERVERSION@
	public final static String ServerVersionString = "CoffeeMUD-HTTPserver/" + HOST_VERSION_MAJOR + "." + HOST_VERSION_MINOR;
	
	public boolean isOK = false;
	
	public ServerSocket servsock=null;

	private Host mud;
	private String partialName;
	
	String serverDir = null;
	String serverTemplateDir = null;
	
	private static boolean displayedBlurb=false;
	
	public FileGrabber pageGrabber=new FileGrabber(this);
	public FileGrabber templateGrabber=new FileGrabber(this);
	
	public HTTPserver(Host a_mud, String a_name)
	{
		super("HTTP-"+a_name);
		partialName = a_name;		//name without prefix
		mud = a_mud;

		
		if (!initServer())
			isOK = false;
		else
			isOK = true;
	}	

	public String getPartialName()	{return partialName;}
	public Host getMUD()	{return mud;}
	public String getServerDir() {return serverDir;}
	public String getServerTemplateDir() {return serverTemplateDir;}
	
	private boolean initServer()
	{
		if (!loadPropPage())
		{
			Log.errOut(getName(),"ERROR: HTTPserver unable to read ini file.");
			return false;
		}
		
		if (page.getStr("PORT").length()==0)
		{
			Log.errOut(getName(),"ERROR: required parameter missing: PORT");
			return false;
		}
		if (page.getStr("DEFAULTFILE").length()==0)
		{
			Log.errOut(getName(),"ERROR: required parameter missing: DEFAULTFILE");
			return false;
		}
		if (page.getStr("VIRTUALPAGEEXTENSION").length()==0)
		{
			Log.errOut(getName(),"ERROR: required parameter missing: VIRTUALPAGEEXTENSION");
			return false;
		}

		if (page.getStr("BASEDIRECTORY").length()==0)
		{
			serverDir = new String ("web" + File.separatorChar + partialName);
		}
		else
		{
			serverDir = page.getStr("BASEDIRECTORY");
		}

		// don't want any trailing / chars
		serverDir = FileGrabber.fixDirName(serverDir);

/*		if (serverDir.charAt(serverDir.length()-1) == File.separatorChar)
		{
			if (serverDir.length() > 1)
				serverDir = serverDir.substring(0,serverDir.length()-2);
			else
				serverDir = "";
		}
*/
		if (!pageGrabber.setBaseDirectory(serverDir))
		{
			Log.errOut(getName(),"Could not set server base directory: "+serverDir);
			return false;
		}

		if (page.getStr("TEMPLATEDIRECTORY").length()==0)
		{
//			serverTemplateDir = new String ("web" + File.separatorChar + partialName + ".templates");
			serverTemplateDir = new String (serverDir + ".templates");
		}
		else
		{
			serverTemplateDir = page.getStr("TEMPLATEDIRECTORY");
		}

		
		// don't want any trailing / chars
		serverTemplateDir = FileGrabber.fixDirName(serverTemplateDir);

/*		if (serverTemplateDir.charAt(serverTemplateDir.length()-1) == File.separatorChar)
		{
			if (serverTemplateDir.length() > 1)
				serverTemplateDir = serverTemplateDir.substring(0,serverTemplateDir.length()-2);
			else
				serverTemplateDir = "";
		}
*/
		if (!templateGrabber.setBaseDirectory(serverTemplateDir))
		{
			Log.errOut(getName(),"Could not set server template directory: "+serverTemplateDir);
			return false;
		}


		addVirtualDirectories();

		
		if (!displayedBlurb)
		{
			displayedBlurb = true;
			Log.sysOut(getName(),"CoffeeMud built-in HTTPserver v"+HOST_VERSION_MAJOR+"."+HOST_VERSION_MINOR);
			Log.sysOut(getName(),"Written and (c) 2002 Jeff Kamenek");
		}

		return true;
	}


	private void addVirtualDirectories()
	{
		for (Enumeration e = page.keys() ; e.hasMoreElements() ;)
		{
			String s = (String) e.nextElement();
			
//Log.sysOut(getName(),s);
			// nb: hard-coded!
			if (s.startsWith("MOUNT/"))
			{
				// nb: hard-coded! - leaves in '/'
				String v = s.substring(5);
				
				pageGrabber.addVirtualDirectory(v,page.getStr(s));
			}
		}
	}
	
	private boolean loadPropPage()
	{
		if (page==null || !page.loaded)
		{
			String fn = "web" + File.separatorChar + getPartialName() + ".ini";
			page=new INI( mud.getCommonPropPage(), fn);
			if(!page.loaded)
			{
				Log.errOut(getName(),"failed to load " + fn);
				return false;
			}
		}
		
		return true;
	}

	public void run()
	{
		int q_len = 6;
		Socket sock=null;
		boolean serverOK = false;

		if (!isOK)	return;
		if ((page == null) || (!page.loaded))
		{
			Log.errOut(getName(),"ERROR: HTTPserver will not run with no properties. WebServer shutting down.");
			isOK = false;
			return;
		}
		

		//jef - get backlog value
		if (page.getInt("BACKLOG") > 0)
			q_len = page.getInt("BACKLOG");

		//jef - address to bind too (may be null for ALL)
		InetAddress bindAddr = null;

		//jef - get bind address
		if (page.getStr("BIND") != null && page.getStr("BIND").length() > 0)
		{
			try
			{
				bindAddr = InetAddress.getByName(page.getStr("BIND"));
			}
			catch (UnknownHostException e)
			{
				Log.errOut(getName(),"ERROR: Could not bind to address " + page.getStr("BIND"));
				bindAddr = null;
			}
		}

		try
		{
			servsock=new ServerSocket(page.getInt("PORT"), q_len, bindAddr);

			Log.sysOut(getName(),"Started on port: "+page.getInt("PORT"));
			if (bindAddr != null)
				Log.sysOut(getName(),"Bound to: "+bindAddr.toString());
			
			
			serverOK = true;

			while(true)
			{
				sock=servsock.accept();
				
				// process the request - pass .ini data for mime types
				ProcessHTTPrequest W=new ProcessHTTPrequest(sock,this,page);
				W.equals(W); // this prevents an initialized by never used error
				// nb - ProcessHTTPrequest is a Thread, but it .start()s in the constructor
				//  if succeeds - no need to .start() it here
				sock = null;
			}
		}
		catch(Throwable t)
		{
			// jef: if we've been interrupted, servsock will be null
			//   and serverOK will be true
			if((t!=null)&&(t instanceof Exception))
				Log.errOut(getName(),((Exception)t).getMessage());


			// jef: this prevents initHost() from running if run() has failed (eg socket in use)
			if (!serverOK)
				isOK = false;
		}
		
//		Log.sysOut(getName(),"Cleaning up.");

		try
		{
			if(servsock!=null)
				servsock.close();
			if(sock!=null)
				sock.close();
		}
		catch(IOException e)
		{
		}

		Log.sysOut(getName(),"Thread stopped!");
	}


	// sends shutdown message to both log and optional session
	// then just calls interrupt

	public void shutdown(Session S)
	{
		Log.sysOut(getName(),"Shutting down.");
		if (S != null)
			S.println( getName() + " shutting down.");
		this.interrupt();
	}

	public void shutdown()	{shutdown(null);}


	
	// interrupt does NOT interrupt the ServerSocket.accept() call...
	//  override it so it does
	public void interrupt()
	{
		if(servsock!=null)
		{
			try
			{
				servsock.close();
				//jef: we MUST set it to null
				// (so run() can tell it was interrupted & didn't have an error)
				servsock = null;
			}
			catch(IOException e)
			{
			}
		}
		super.interrupt();
	}

	public int getPort()
	{
		return page.getInt("PORT");
	}
	
	public String getPortStr()
	{
		return page.getStr("PORT");
	}
	
	public static boolean loadWebMacros()
	{
		String prefix="com"+File.separatorChar+"planet_ink"+File.separatorChar+"coffee_mud"+File.separatorChar;
		webMacros=CMClass.loadVectorListToObj(prefix+"web"+File.separatorChar+"macros"+File.separatorChar, "");
		Log.sysOut("WEB","WebMacros loaded  : "+webMacros.size());
		if(webMacros.size()==0) return false;
		return true;
	}

	public static void unloadWebMacros()
	{
		webMacros=new Vector();
	}

}
