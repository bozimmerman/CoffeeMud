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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.net.*;
import java.io.*;

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
            StringBuffer msg=new StringBuffer("\033[1z<SHUTDOWN "+a[2]+" "+a[3]);
            if(a.length>=5)
                msg.append(" "+!(CMath.s_bool(a[4])));
            if(a.length>=6)
                for(int i=5;i<a.length;i++)
                msg.append(" "+a[i]);
            Socket sock=new Socket(a[0],CMath.s_int(a[1]));
            OutputStream rawout=sock.getOutputStream();
            rawout.write((msg.toString()+">\n").getBytes());
            rawout.flush();
            BufferedReader in=new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String read="";
            while(!read.startsWith("\033[1z<"))
            	read=in.readLine();
            System.out.println(read.substring("\033[1z<".length()));
        }
        catch(Exception e){e.printStackTrace();}
    }
}
