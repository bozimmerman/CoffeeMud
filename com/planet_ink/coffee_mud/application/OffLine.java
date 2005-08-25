    package com.planet_ink.coffee_mud.application;
    import java.io.*;
    import java.net.*;
    import java.util.*;
    import java.sql.*;
    import com.planet_ink.coffee_mud.interfaces.*;
    import com.planet_ink.coffee_mud.utils.DVector;
    import com.planet_ink.coffee_mud.common.Resources;
    import com.planet_ink.coffee_mud.utils.Util;
    import com.planet_ink.coffee_mud.common.INI;;
    
    /* 
    Copyright 2000-2005 Bo Zimmerman

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
        t.interrupt();
    }


    private static boolean initHost(Thread t)
    {

        if (!isOK)
        {
            t.interrupt();
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
                bindAddr = null;
            }
        }

        try
        {
            servsock=new ServerSocket(port, q_len, bindAddr);

            System.out.println("Off-Line Server started on port: "+port);
            if (bindAddr != null)
                System.out.println("Off-Line Server bound to: "+bindAddr.toString());
            serverIsRunning = true;

            while(true)
            {
                state=0;
                sock=servsock.accept();
                state=1;

                if (acceptConnections)
                {
                    String address="unknown";
                    try{address=sock.getInetAddress().getHostAddress().trim();}catch(Exception e){}
                    System.out.println("Got a connection from "+address+" on port "+port);
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
    
                    accessed.addElement(address,new Long(System.currentTimeMillis()));
                    if(proceed!=0)
                    {
                        System.out.println("Blocking a connection from "+address+" on port "+port);
                        PrintWriter out = new PrintWriter(sock.getOutputStream());
                        out.println("\n\rOFFLINE: Blocked\n\r");
                        out.flush();
                        if(proceed==2)
                            out.println("\n\rYour address has been blocked temporarily due to excessive invalid connections.  Please try back in "+(Math.round(LastConnectionDelay/60000))+" minutes, and not before.\n\r\n\r");
                        else
                            out.println("\n\rYou are unwelcome.  No one likes you here. Go away.\n\r\n\r");
                        out.flush();
                        out.close();
                        sock = null;
                    }
                    else
                    {
                        state=2;
                        StringBuffer offLineText=Resources.getFileResource("text"+File.separatorChar+"down.txt");
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
                    StringBuffer rejectText=Resources.getFileResource("text"+File.separatorChar+"offline.txt");
                    PrintWriter out = new PrintWriter(sock.getOutputStream());
                    out.flush();
                    out.println(rejectText);
                    out.flush();
                    out.close();
                    try{Thread.sleep(250);}catch(Exception e){}
                    sock = null;
                }
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
    public static int activeCount(ThreadGroup tGroup)
    {
        int realAC=0;
        int ac = tGroup.activeCount();
        Thread tArray[] = new Thread [ac+1];
        tGroup.enumerate(tArray);
        for (int i = 0; i<ac; ++i)
        {
            if (tArray[i] != null && tArray[i].isAlive())
                realAC++;
        }
        return realAC;
    }

    public static int killCount(ThreadGroup tGroup, Thread thisOne)
    {
        int killed=0;

        int ac = tGroup.activeCount();
        Thread tArray[] = new Thread [ac+1];
        tGroup.enumerate(tArray);
        for (int i = 0; i<ac; ++i)
        {
            if (tArray[i] != null && tArray[i].isAlive() && (tArray[i] != thisOne))
            {
                tArray[i].interrupt();
                try{Thread.sleep(500);}catch(Exception e){}
                killed++;
            }
        }
        return killed;
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
        INI page=null;
        
        String nameID="";
        String iniFile="coffeemud.ini";
        if(a.length>0)
        {
            for(int i=0;i<a.length;i++)
                nameID+=" "+a[i];
            nameID=nameID.trim();
            Vector V=Util.paramParse(nameID);
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
            nameID=Util.combine(V,0);
        }
        if(nameID.length()==0) nameID="Unnamed CoffeeMud";
        try
        {
            while(true)
            {
                page=INI.loadPropPage(iniFile);
                if ((page==null)||(!page.loaded))
                {
                    System.out.println("ERROR: Unable to read ini file: '"+iniFile+"'.");
                    System.exit(-1);
                }
                
                isOK = true;
                bind=page.getStr("BIND");

                System.out.println();
                System.out.println("CoffeeMud Off-Line");
                System.out.println("(C) 2000-2005 Bo Zimmerman");
                System.out.println("http://coffeemud.zimmers.net");

                if(OffLine.isOK)
                {
                    mudThreads=new Vector();
                    String ports=page.getProperty("PORT");
                    int pdex=ports.indexOf(",");
                    while(pdex>0)
                    {
                        OffLine mud=new OffLine();
                        mud.acceptConnections=false;
                        mud.port=Util.s_int(ports.substring(0,pdex));
                        ports=ports.substring(pdex+1);
                        mud.start();
                        mudThreads.addElement(mud);
                        pdex=ports.indexOf(",");
                    }
                    OffLine mud=new OffLine();
                    mud.acceptConnections=false;
                    mud.port=Util.s_int(ports);
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
}
