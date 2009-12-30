package com.planet_ink.coffee_mud.core.http;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.LanguageLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.net.*;
import java.io.IOException;
import java.util.*;


/*
   Portions Copyright 2002 Jeff Kamenek
   Portions Copyright 2002-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class HTTPserver extends Thread implements MudHost
{
    protected CMProps page=null;

    protected static final float HOST_VERSION_MAJOR=(float)1.0;
    protected static final float HOST_VERSION_MINOR=(float)0.3;
    protected static CMProps webCommon=null;

	// this gets sent in HTTP response
	//  also used by @WEBSERVERVERSION@
	public final static String ServerVersionString = "CoffeeMud HTTPserver/" + HOST_VERSION_MAJOR + "." + HOST_VERSION_MINOR;

    protected boolean isOK = false;
    protected final long startupTime = System.currentTimeMillis();

    protected boolean isAdminServer = false;

    protected ServerSocket servsock=null;

	private MudHost mud;
	protected String partialName;
    private static final String[] STATUS_STRINGS={"waiting","processing","done"};
    private int state=0;
    private int myPort=27744;
    private int myServerNumber=0;
    private boolean acceptConnections=true;

    protected String serverDir = null;
    protected String serverTemplateDir = null;

    protected FileGrabber pageGrabber=new FileGrabber(this);
    protected FileGrabber templateGrabber=new FileGrabber(this);
    public DVector activeRequests=new DVector(2);

	public HTTPserver(MudHost a_mud, String a_name, int num)
	{
		super("HTTP-"+a_name+((num>0)?""+(num+1):""));
		partialName = a_name;		//name without prefix
		mud = a_mud;
		myServerNumber=num;
		setDaemon(true);
		if (!initServer(num))
			isOK = false;
		else
			isOK = true;
	}

	public String getPartialName()	{return partialName;}
	public MudHost getMUD()	{return mud;}
	public String getServerDir() {return serverDir;}
	public String getServerTemplateDir() {return serverTemplateDir;}
    public long getUptimeSecs() { return (System.currentTimeMillis()-startupTime)/1000;}

	public Properties getCommonPropPage()
	{
		if (webCommon==null || !webCommon.loaded)
		{
			webCommon=new CMProps ("web/common.ini");
			if(!webCommon.loaded)
				Log.errOut("HTTPserver","Unable to load common.ini!");
		}
		return webCommon;
	}

	protected boolean initServer(int which)
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
		if(which>0)
		{
			Vector V=CMParms.parseCommas(page.getStr("PORT"),true);
			if(which>=V.size())
			{
				Log.errOut(getName(),"ERROR: not enough PORT entries to support #"+(which+1));
				return false;
			}
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
			serverDir = "web/" + partialName;
		}
		else
		{
			serverDir = page.getStr("BASEDIRECTORY");
		}

		// don't want any trailing / chars
		serverDir = FileGrabber.fixDirName(serverDir);

		if (!pageGrabber.setBaseDirectory(serverDir))
		{
			Log.errOut(getName(),"Could not set server base directory: "+serverDir);
			return false;
		}

		if (page.getStr("TEMPLATEDIRECTORY").length()==0)
			serverTemplateDir = serverDir + ".templates";
		else
			serverTemplateDir = page.getStr("TEMPLATEDIRECTORY");

		// don't want any trailing / chars
		serverTemplateDir = FileGrabber.fixDirName(serverTemplateDir);

		if (!templateGrabber.setBaseDirectory(serverTemplateDir))
		{
			Log.errOut(getName(),"Could not set server template directory: "+serverTemplateDir);
			return false;
		}

		addVirtualDirectories();


		return true;
	}

	public Hashtable getVirtualDirectories(){return pageGrabber.getVirtualDirectories();}

	private void addVirtualDirectories()
	{
		for (Enumeration e = page.keys() ; e.hasMoreElements() ;)
		{
			String s = (String) e.nextElement();

			// nb: hard-coded!
			if (s.startsWith("MOUNT/"))
			{
				// nb: hard-coded! - leaves in '/'
				String v = s.substring(5);

				pageGrabber.addVirtualDirectory(v,page.getStr(s));
			}
		}
	}

	protected boolean loadPropPage()
	{
		if (page==null || !page.loaded)
		{
			String fn = "web/" + getPartialName() + ".ini";
			page=new CMProps(getCommonPropPage(), fn);
			if(!page.loaded)
			{
				Log.errOut(getName(),"failed to load " + fn);
				return false;
			}
		}

		return true;
	}

    public void acceptConnection(Socket sock) 
        throws SocketException, IOException
    {
        if(acceptConnections)
        {
            while(CMLib.threads().isAllSuspended()) {
                try { Thread.sleep(1000); } catch(Exception e) { throw new IOException(e.getMessage());}
            }
            state=1;
            ProcessHTTPrequest W=new ProcessHTTPrequest(sock,this,page,isAdminServer);
            W.equals(W); // this prevents an initialized by never used error
            // nb - ProcessHTTPrequest is a Thread, but it .start()s in the constructor
            //  if succeeds - no need to .start() it here
        }
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


		if (page.getInt("BACKLOG") > 0)
			q_len = page.getInt("BACKLOG");

		InetAddress bindAddr = null;


		if (page.getStr("ADMIN") != null && page.getStr("ADMIN").equalsIgnoreCase("true"))
			isAdminServer = true;
		if (page.getStr("BIND") != null && page.getStr("BIND").length() > 0)
		{
			try
			{
				bindAddr = InetAddress.getByName(page.getStr("BIND"));
			}
			catch (UnknownHostException e)
			{
				Log.errOut(getName(),"ERROR: Could not bind to address " + page.getStr("BIND"));
			}
		}

		try
		{
			Vector allports=CMParms.parseCommas(page.getStr("PORT"),true);
			myPort=CMath.s_int((String)allports.elementAt(myServerNumber));
			servsock=new ServerSocket(myPort, q_len, bindAddr);

			Log.sysOut(getName(),"Started on port: "+myPort);
			if (bindAddr != null)
				Log.sysOut(getName(),"Bound to: "+bindAddr.toString());


			serverOK = true;

			while(true)
			{
                state=0;
				sock=servsock.accept();
				acceptConnection(sock);
				sock = null;
			}
		}
		catch(Exception e)
		{
			// jef: if we've been interrupted, servsock will be null
			//   and serverOK will be true
			Log.errOut(getName(),e.getMessage());


			// jef: this prevents initHost() from running if run() has failed (eg socket in use)
			if (!serverOK)
				isOK = false;
		}
        state=2;
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

		//Log.sysOut(getName(),"Thread stopped!");
	}


	// sends shutdown message to both log and optional session
	// then just calls interrupt

	public void shutdown(Session S)
	{
		Log.sysOut(getName(),"Shutting down.");
		if (S != null)
			S.println( getName() + " shutting down.");
		CMLib.killThread(this,500,1);
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

	public int totalPorts()
	{
		return CMParms.parseCommas(page.getStr("PORT"),true).size();
	}
	public int getPort()
	{
		return myPort;
	}

	public String getPortStr()
	{
		return page.getStr("PORT");
	}
    public String getHost(){return getName();}
    public void shutdown(Session S, boolean keepItDown, String externalCommand){
        shutdown(S);
    }
    public String getStatus()
    {
        return STATUS_STRINGS[state];
    }
    public void setAcceptConnections(boolean truefalse){ acceptConnections=truefalse;}
    public boolean isAcceptingConnections(){ return acceptConnections;}

    public String getLanguage() 
    {
    	String lang = CMProps.instance().getStr("LANGUAGE").toUpperCase().trim();
    	if(lang.length()==0) return "English";
    	for(int i=0;i<LanguageLibrary.ISO_LANG_CODES.length;i++)
    		if(lang.equals(LanguageLibrary.ISO_LANG_CODES[i][0]))
    			return LanguageLibrary.ISO_LANG_CODES[i][1];
    	return "English";
    }

    public Vector getOverdueThreads()
    {
    	Vector V=new Vector();
    	long time=System.currentTimeMillis();
    	synchronized(activeRequests)
    	{
	    	for(int a=activeRequests.size()-1;a>=0;a--)
	    	{
	    		ProcessHTTPrequest P=(ProcessHTTPrequest)activeRequests.elementAt(a, 1);
	    		long pTime=((Long)activeRequests.elementAt(a,2)).longValue();
	    		if((time-pTime)>(60*1000*60))
	    		{
	    			V.addElement(P);
	    			activeRequests.removeElementsAt(a);
	    		}
	    	}
    	}
    	return V;
    }

    public String executeCommand(String cmd)
        throws Exception
    {
        throw new Exception("Not implemented");
    }
}
