package com.planet_ink.grinder;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.net.*;

class GrinderConnection
{
    public static boolean connected=false;
    public static Socket sock=null;
    public static BufferedReader in;
    public static PrintWriter out;
    public static MUDGrinder myApp=null;
    public static int SOTIMEOUT=250;
    
    public static void sendOut(String output)
    {        
        if(output==null) return;
        if((!connected)||(out==null))
            return;
            
        out.println(output);        
        out.flush();    
    }
    
    public static String connect(MUDGrinder A, String site, int port)
    {        
        try        
        {            
            myApp=A;            
            sock=new Socket(site,port);            
            if(sock!=null)            
            {                
                sock.setSoTimeout(SOTIMEOUT);
                in=new BufferedReader(new InputStreamReader(sock.getInputStream()));
                out=new PrintWriter(sock.getOutputStream());
            }            
            connected=true;        
        }        
        catch(Exception e)        
        {
            connected=false;
            return e.getMessage();
        }
        return "";
    }
        
    public static void disconnect()
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
        // do something to the GUI?
    } 
    
    public static void clearBuf()
    {
        readIn(0,false);
    }
    
    public static StringBuffer sendAndExpectResponse(String sendStr)
    {
        return sendAndExpectResponse(sendStr,5,true);
    }
    public static StringBuffer sendAndExpectBigResponse(String sendStr)
    {
        return sendAndExpectResponse(sendStr,10,true);
    }
    public static StringBuffer sendAndExpectResponse(String sendStr, int minLength, boolean reRead)
    {
        // clear the buffer
        readIn(0,false);
        
        sendOut(sendStr);
        StringBuffer response=readIn(minLength,reRead);
        if(response==null)
            return null;
        if(response.length()<minLength)
            response.append(readIn(minLength-response.length(),true));
        return response;
    }
    
    public static StringBuffer readIn(int minLength, boolean reRead)
    {
        if((!connected)||(in==null))
            return null;
            
        StringBuffer input=new StringBuffer("");
        input.setLength(0);
        try
        {
            while(true)
            {
                 char c=(char)in.read();
                 if((c>0)&&(c!=13))
                 {                            
                    input.append(c);
                    if(c==65535)
                    {
                        disconnect();
                        return null;
                    }
                 }
            }
        }
        catch(InterruptedIOException e)
        {
            if(input.length()<minLength)
            {
                if(reRead)
                {
                    input.append(readIn(minLength-input.length(),false));
                    if(input.length()<minLength)
                        input.append(readIn(minLength-input.length(),false));
                    if(input.length()<minLength)
                        input.append(readIn(minLength-input.length(),false));
                    if(input.length()<minLength)
                        input.append(readIn(minLength-input.length(),false));
                }
                else
                    return input;
            }
            else
                return input;
        }
        catch(IOException e)
        {
			if(e.getMessage().toUpperCase().indexOf("CONNECTION RESET BY PEER")>=0)
			{							
			    disconnect();
			    return null;
			}
        }
        return input;
    }
}