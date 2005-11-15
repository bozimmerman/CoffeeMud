package com.planet_ink.siplet.applet;
import com.planet_ink.siplet.support.*;
import java.applet.Applet;
import java.awt.*;
import java.net.*;
import java.io.*;

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
public class Siplet extends Applet 
{
    public final static boolean debugDataOut=false;
    
    public final static long serialVersionUID=6;
    public static final float VERSION_MAJOR=(float)2.0;
    public static final long  VERSION_MINOR=1;
    protected StringBuffer buf=new StringBuffer("");
    protected Socket sock=null;
    protected BufferedReader in;
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
        return "Siplet V"+VERSION_MAJOR+"."+VERSION_MINOR+" (C)2005 Bo Zimmerman";
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

    public boolean connectToURL(){ return connectToURL("coffeemud.homeip.net",23);}
    public boolean connectToURL(String url, int port)
    {
        connected=false;
        try
        {
            //addItem("connecting to "+url+":"+port+" ");
            sock=new Socket(InetAddress.getByName(url),port);
            in=new BufferedReader(new InputStreamReader(sock.getInputStream()));
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
        try
        {
            if(in!=null)
                in.close();
            if(out!=null)
                out.close();
            if(sock!=null)
                sock.close();
        }
        catch(Exception e)
        {
        }
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
                data=buf.substring(0,endAt).toString();
                buf.delete(0,endAt);
            }
            else
            {
                data=buf.toString();
                buf.setLength(0);
            }
            if(debugDataOut) if(data.length()>0) System.out.println(data);
            return data;
        }
    }
    
    
    public boolean isConnectedToURL(){return connected;}
    
    public void readURLData()
    {
        try
        {
            while(connected
            &&in.ready()
            &&(!sock.isClosed())
            &&(sock.isConnected()))
            {
                try
                {
                    char c=(char)in.read();
                    buf.append(c);
                    if(c==65535)
                    {
                        disconnectFromURL();
                        return;
                    }
                }
                catch(java.io.InterruptedIOException e)
                {
                    disconnectFromURL();
                    return;
                }
                catch(Exception e)
                {
                    disconnectFromURL();
                    return;
                }
            }
            if(sock.isClosed()) 
                disconnectFromURL();
            else
            if(!sock.isConnected())
                disconnectFromURL();
            else
            if(buf.length()>0)
                Telnet.TelenetFilter(buf,out);

        }
        catch(Exception e)
        {
            disconnectFromURL();
            return;
        }
    }
}
