package com.planet_ink.coffee_mud.application;
import java.net.*;
import java.io.*;
import com.planet_ink.coffee_mud.utils.Util;

public class Shutdown
{

    public Shutdown()
    {
        super();
    }

    public static void main(String a[])
    {
        if(a.length<4)
        {
            System.out.println("Command usage: Shutdown <host> <port> <username> <password> (<true/false for reboot> <external command>)");
            return;
        }
        try
        {
            StringBuffer msg=new StringBuffer("SHUTDOWN "+a[2]+" "+a[3]);
            if(a.length>=5)
                msg.append(" "+!(Util.s_bool(a[4])));
            if(a.length>=6)
                for(int i=5;i<a.length;i++)
                msg.append(" "+a[i]);
            
            Socket sock=new Socket(a[0],Util.s_int(a[1]));
            OutputStream rawout=sock.getOutputStream();
            rawout.write((msg.toString()+"\n\r").getBytes());
            rawout.flush();
            Thread.sleep(1000);
        }
        catch(Exception e){e.printStackTrace();}
    }
}
