package com.planet_ink.siplet.applet;
import com.planet_ink.siplet.support.*;
import com.jcraft.jzlib.*;
import java.applet.Applet;
import java.awt.*;
import java.net.*;
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
public class Siplet extends Applet
{
    public final static boolean debugDataOut=false;
    
    public final static long serialVersionUID=7;
    public static final float VERSION_MAJOR=(float)2.0;
    public static final long  VERSION_MINOR=8;
    protected StringBuffer buf=new StringBuffer("");
    protected String lastURL="coffeemud.net";
    protected int lastPort=23;
    protected Socket sock=null;
    protected InputStream rawin=null;
    protected BufferedReader[] in;
    protected DataOutputStream out;
    protected boolean connected=false;
    protected TelnetFilter Telnet=new TelnetFilter(this);

    protected StringBuffer buffer;
    protected int sillyCounter=0;


    public void init()
    {
        buffer = new StringBuffer();
    }

    public String info()
    {
        return "Siplet V"+VERSION_MAJOR+"."+VERSION_MINOR+" (C)2005-2010 Bo Zimmerman";
    }

    public void start()
    {
        //addItem("starting siplet "+VERSION_MAJOR+"."+VERSION_MINOR+" ");
    }

    public void stop()
    {
        //addItem("!stopped siplet!");
    }

    public void destroy()
    {
    }
    
    public Siplet create() { return new Siplet();}

    public void addItem(String newWord)
    {
        System.out.println(newWord);
        buffer.append(newWord);
        repaint();
    }

    public void paint(Graphics g)
    {
        g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
        g.drawString(buffer.toString(), 5, 15);
    }

    public boolean connectToURL(){ return connectToURL(lastURL,lastPort);}
    public boolean connectToURL(String url, int port)
    {
        connected=false;
        if(sock!=null)
        	disconnectFromURL();
        try
        {
            lastURL=url;
            lastPort=port;
            //addItem("connecting to "+url+":"+port+" ");
            sock=new Socket(InetAddress.getByName(url),port);
            Thread.sleep(100);
            rawin=sock.getInputStream();
            in=new BufferedReader[1];
            in[0]=new BufferedReader(new InputStreamReader(sock.getInputStream(),"iso-8859-1"));
            out=new DataOutputStream(sock.getOutputStream());
            Telnet=new TelnetFilter(this);
            connected=true;
        }
        catch(Exception e)
        {
            ByteArrayOutputStream bw=new ByteArrayOutputStream();
            PrintWriter pw=new PrintWriter(new OutputStreamWriter(bw));
            e.printStackTrace(pw);
            pw.flush();
            //addItem(bw.toString());
            return false;
        }
        return true;
    }
    public void disconnectFromURL()
    {
        connected=false;
        try {
            if(out!=null)
            {
            	out.write(new byte[]{(byte)255,(byte)253,18}); //iac, iacdo, logout 
            	out.flush();
            }
        } catch(Exception e) { }
        try {
            if((in!=null)&&(in[0]!=null))
                in[0].close();
        } catch(Exception e) { }
        try {
            if(out!=null)
                out.close();
        } catch(Exception e) { }
        try {
            if(sock!=null)
                sock.close();
        } catch(Exception e) { }
        in=null;
        out=null;
        sock=null;
    }

    public void sendData(String data)
    {
        if(connected)
        {
            try
            {
                if(sock.isClosed())
                    disconnectFromURL();
                else
                if(!sock.isConnected())
                    disconnectFromURL();
                else
                {
                    out.writeBytes(data+"\n\r");
                    out.flush();
                }
            }
            catch(IOException e)
            {
                disconnectFromURL();
            }
        }
    }
    public String getJScriptCommands()
    { return Telnet.getEnquedJScript();}

    public String getURLData()
    {
        synchronized(buf)
        {
            String s=Telnet.getEnquedResponses();
            if(s.length()>0)  sendData(s);
            int endAt=Telnet.HTMLFilter(buf);
            String data=null;
            if(buf.length()==0) return "";
            if(endAt<0) endAt=buf.length();
            if(endAt==0) return "";
            if(Telnet.isUIonHold()) return "";
            if(endAt<buf.length())
            {
                data=buf.substring(0,endAt);
                buf.delete(0,endAt);
            }
            else
            {
                data=buf.toString();
                buf.setLength(0);
            }
            if(debugDataOut) if(data.length()>0) System.out.println("/DATA="+data);
            return data;
        }
    }


    public boolean isConnectedToURL(){return connected;}

    public void readURLData()
    {
        try
        {
            while(connected
            &&in[0].ready()
            &&(!sock.isClosed())
            &&(sock.isConnected()))
            {
                try
                {
                	Telnet.TelnetRead(buf,rawin,in);
                }
                catch(java.io.InterruptedIOException e)
                {
                    disconnectFromURL();
                    return;
                }
                catch(Exception e)
                {
                    if(e instanceof com.jcraft.jzlib.ZStreamException)
                    {
                        disconnectFromURL();
                        try{Thread.sleep(100);}catch(Exception e2){}
                        connectToURL();
                    }
                    else
                    {
                        disconnectFromURL();
                        return;
                    }
                }
            }
            if(sock.isClosed())
                disconnectFromURL();
            else
            if(!sock.isConnected())
                disconnectFromURL();
            else
            if(buf.length()>0)
                Telnet.TelenetFilter(buf,out,rawin,in);

        }
        catch(Exception e)
        {
            disconnectFromURL();
            return;
        }
    }
}
