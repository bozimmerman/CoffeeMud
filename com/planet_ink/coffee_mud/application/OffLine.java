    package com.planet_ink.coffee_mud.application;
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
    import java.util.*;
    import java.sql.*;
import java.io.*;

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

@SuppressWarnings("unchecked")
public class OffLine extends Thread implements MudHost
{
    public static Vector mudThreads=new Vector();
    public static DVector accessed=new DVector(2);
    public static Vector autoblocked=new Vector();

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

    public static void fatalStartupError(Thread t, int type)
    {
        String str=null;
        switch(type)
        {
        case 1:
            str="ERROR: initHost() will not run without properties. Exiting.";
            break;
        case 2:
            break;
        case 3:
            break;
        case 4:
            str="Fatal exception. Exiting.";
            break;
        case 5:
            str="OffLine Server did not start. Exiting.";
            break;
        default:
            break;
        }
        System.out.println(str);
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
        }
        if (!isOK)
        {
            fatalStartupError(t,5);
            return false;
        }

        for(int i=0;i<mudThreads.size();i++)
            ((OffLine)mudThreads.elementAt(i)).acceptConnections=true;
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
        catch(IOException e)
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
        	try{
	        	fin = new FileInputStream(fileName);
	        	while(fin.available()>0)
	        		offLineText.append((char)fin.read());
	        	Resources.submitResource(fileName,offLineText);
        	}
        	catch(Exception e){e.printStackTrace();}
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

    public void acceptConnection(Socket sock)
    throws SocketException, IOException
{
        sock.setSoLinger(true,3);
        state=1;

        if (acceptConnections)
        {
            String address="unknown";
            try{address=sock.getInetAddress().getHostAddress().trim();}catch(Exception e){}
            System.out.println("Connection from "+address+": "+port);
            // now see if they are banned!
            int proceed=0;

            int numAtThisAddress=0;
            long ConnectionWindow=(180*1000);
            long LastConnectionDelay=(5*60*1000);
            boolean anyAtThisAddress=false;
            int maxAtThisAddress=6;
            try{
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
            }catch(java.lang.ArrayIndexOutOfBoundsException e){}

            accessed.addElement(address,Long.valueOf(System.currentTimeMillis()));
            if(proceed!=0)
            {
                System.out.println("Blocking a connection from "+address+" on port "+port);
                PrintWriter out = new PrintWriter(sock.getOutputStream());
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
                String fileName="resources"+File.separator+"text"+File.separator+"down.txt";
                StringBuffer offLineText=getFile(fileName);
                try
                {
                    sock.setSoTimeout(300);
                    OutputStream rawout=sock.getOutputStream();
                    InputStream rawin=sock.getInputStream();
                    rawout.write('\n');
                    rawout.write('\n');
                    rawout.flush();


                    //out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(rawout, "UTF-8")));
                    //in = new BufferedReader(new InputStreamReader(rawin, "UTF-8"));
                    BufferedReader in;
                    PrintWriter out;
                    out = new PrintWriter(new OutputStreamWriter(rawout,"iso-8859-1"));
                    in = new BufferedReader(new InputStreamReader(rawin,"iso-8859-1"));

                    if(offLineText!=null) out.print(offLineText);
                    out.flush();
                    try{Thread.sleep(250);}catch(Exception e){}
                    closeSocks(sock,in,out);
                }
                catch(SocketException e)
                {
                }
                catch(IOException e)
                {
                }
                closeSocks(sock,null,null);
                sock=null;
            }
        }
        else
        {
            String fileName="resources"+File.separator+"text"+File.separator+"offline.txt";
            StringBuffer rejectText=getFile(fileName);
            PrintWriter out = new PrintWriter(sock.getOutputStream());
            out.flush();
            out.println(rejectText);
            out.flush();
            out.close();
            try{Thread.sleep(250);}catch(Exception e){}
            sock = null;
        }
    }
    
    public void run()
    {
        int q_len = 6;
        Socket sock=null;
        serverIsRunning = false;

        if (!isOK)  return;

        InetAddress bindAddr = null;

        if (bind.length() > 0)
        {
            try
            {
                bindAddr = InetAddress.getByName(bind);
            }
            catch (UnknownHostException e)
            {
                System.out.println("ERROR: MUD Server could not bind to address " + bind);
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
                state=0;
                sock=servsock.accept();
                acceptConnection(sock);
            }
        }
        catch(Throwable t)
        {
            if((!(t instanceof java.net.SocketException))
            ||(t.getMessage()==null)
            ||(t.getMessage().toLowerCase().indexOf("socket closed")<0))
            {
                t.printStackTrace();
            }

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
        catch(IOException e)
        {
        }

        System.out.println("Off-Line Server on port "+port+" stopped!");
    }
    public String getStatus()
    {
        return "OFFLINE";
    }

    public void shutdown(Session S, boolean keepItDown, String externalCommand)
    {
        interrupt(); // kill the damn archon thread.
    }

    public static void defaultShutdown()
    {
    }
    public void interrupt()
    {
        if(servsock!=null)
        {
            try
            {
                servsock.close();
                servsock = null;
            }
            catch(IOException e)
            {
            }
        }
        super.interrupt();
    }
    public String getHost()
    {
        return host;
    }
    public int getPort()
    {
        return port;
    }

    public static void main(String a[])
    {
        CMProps page=null;

        String nameID="";
        String iniFile="coffeemud.ini";
        if(a.length>0)
        {
            for(int i=0;i<a.length;i++)
                nameID+=" "+a[i];
            nameID=nameID.trim();
            Vector V=CMParms.paramParse(nameID);
            for(int v=0;v<V.size();v++)
            {
                String s=(String)V.elementAt(v);
                if(s.toUpperCase().startsWith("BOOT=")&&(s.length()>5))
                {
                    iniFile=s.substring(5);
                    V.removeElementAt(v);
                    v--;
                }
            }
            nameID=CMParms.combine(V,0);
        }
        if(nameID.length()==0) nameID="Unnamed CoffeeMud";
        try
        {
            while(true)
            {
                page=CMProps.loadPropPage(iniFile);
                if ((page==null)||(!page.loaded))
                {
                    System.out.println("ERROR: Unable to read ini file: '"+iniFile+"'.");
                    System.exit(-1);
                    return;
                }

                isOK = true;
                bind=page.getStr("BIND");

                System.out.println();
                System.out.println("CoffeeMud Off-Line");
                System.out.println("(C) 2000-2010 Bo Zimmerman");
                System.out.println("http://www.coffeemud.org");

                if(OffLine.isOK)
                {
                    mudThreads=new Vector();
                    String ports=page.getProperty("PORT");
                    int pdex=ports.indexOf(",");
                    while(pdex>0)
                    {
                        OffLine mud=new OffLine();
                        mud.acceptConnections=false;
                        mud.port=CMath.s_int(ports.substring(0,pdex));
                        ports=ports.substring(pdex+1);
                        mud.start();
                        mudThreads.addElement(mud);
                        pdex=ports.indexOf(",");
                    }
                    OffLine mud=new OffLine();
                    mud.acceptConnections=false;
                    mud.port=CMath.s_int(ports);
                    mud.start();
                    mudThreads.addElement(mud);
                }

                StringBuffer str=new StringBuffer("");
                for(int m=0;m<mudThreads.size();m++)
                {
                    MudHost mud=(MudHost)mudThreads.elementAt(m);
                    str.append(" "+mud.getPort());
                }
                ports=str.toString();

                if(initHost(Thread.currentThread()))
                    ((OffLine)mudThreads.firstElement()).join();

                System.gc();
                System.runFinalization();

            }
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
    }
    public void setAcceptConnections(boolean truefalse){ acceptConnections=truefalse;}
    public boolean isAcceptingConnections(){ return acceptConnections;}
    public Vector getOverdueThreads(){return new Vector();}
    public long getUptimeSecs() { return (System.currentTimeMillis()-startupTime)/1000;}
    public String getLanguage() { return "English";}

    public String executeCommand(String cmd)
        throws Exception
    {
        throw new Exception("Not implemented");
    }
}
