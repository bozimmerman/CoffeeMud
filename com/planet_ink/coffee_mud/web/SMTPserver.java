package com.planet_ink.coffee_mud.web;
import java.io.*;
import java.net.*;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.interfaces.*;

public class SMTPserver extends Thread
{
	public INI page=null;

	public static final float HOST_VERSION_MAJOR=(float)1.0;
	public static final float HOST_VERSION_MINOR=(float)0.3;
	public static Hashtable webMacros=null;
	public static INI iniPage=null;
	public ServerSocket servsock=null;
	public boolean isOK = false;
	private MudHost mud;
	private String serverDir = null;
	private static boolean displayedBlurb=false;
	public final static String ServerVersionString = "CoffeeMud SMTPserver/" + HOST_VERSION_MAJOR + "." + HOST_VERSION_MINOR;

	public SMTPserver(MudHost a_mud)
	{
		super("SMTP");
		mud = a_mud;


		if (!initServer())
			isOK = false;
		else
			isOK = true;
	}

	public MudHost getMUD()	{return mud;}
	public String getServerDir() {return serverDir;}

	public Properties getCommonPropPage()
	{
		if (iniPage==null || !iniPage.loaded)
		{
			iniPage=new INI("web" + File.separatorChar + "common.ini");
			if(!iniPage.loaded)
				Log.errOut("SMTPserver","Unable to load common.ini!");
		}
		return iniPage;
	}

	private boolean initServer()
	{
		if (!loadPropPage())
		{
			Log.errOut(getName(),"ERROR: SMTPserver unable to read ini file.");
			return false;
		}

		if (page.getStr("PORT").length()==0)
		{
			Log.errOut(getName(),"ERROR: required parameter missing: PORT");
			return false;
		}

		if (page.getStr("BASEDIRECTORY").length()==0)
		{
			serverDir = new String ("web" + File.separatorChar + "smtp");
		}
		else
		{
			serverDir = page.getStr("BASEDIRECTORY");
		}

		// don't want any trailing / chars
		serverDir = FileGrabber.fixDirName(serverDir);

		if (!displayedBlurb)
		{
			displayedBlurb = true;
			Log.sysOut(getName(),"SMTPserver (C)2004 Bo Zimmerman");
		}

		return true;
	}

	private boolean loadPropPage()
	{
		if (page==null || !page.loaded)
		{
			String fn = "web" + File.separatorChar + "smtp.ini";
			page=new INI(getCommonPropPage(), fn);
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
			Log.errOut(getName(),"ERROR: SMTPserver will not run with no properties. Shutting down.");
			isOK = false;
			return;
		}


		if (page.getInt("BACKLOG") > 0)
			q_len = page.getInt("BACKLOG");

		InetAddress bindAddr = null;


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

				//ProcessSMTPrequest W=new ProcessSMTPrequest(sock,this,page,isAdminServer);
				//W.equals(W); // this prevents an initialized by never used error
				// nb - ProcessSMTPrequest is a Thread, but it .start()s in the constructor
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
}