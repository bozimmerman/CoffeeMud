package com.planet_ink.coffee_mud.Commands;

import java.util.*;
import java.io.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.system.MOBloader;
import com.planet_ink.coffee_mud.Commands.base.*;

public class ClanResign implements Command
{
  protected Vector accessWords=new Vector();

  public ClanResign()
  {
    accessWords.addElement(new String("clanresign"));
  }

	public boolean execute(MOB mob, Vector commands)
	{
    StringBuffer msg=new StringBuffer("");
    if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
    {
      msg.append("You aren't even a member of a clan.");
    }
    else
    {
      try
      {
        String pwd=mob.session().prompt("Are you absolutely SURE (y/N)?","N");
        if(pwd.equalsIgnoreCase("Y"))
        {
          Vector msgVector=new Vector();
          msgVector.addElement(new String("CLANTALK"));
          msgVector.addElement(new String("Member resigned: "+mob.ID()));
          Channels.channel(mob,msgVector,true);
          MOBloader.DBUpdateClan(mob.ID(), "", 0);
        }
        else
        {
          return false;
        }
      }
      catch(IOException e)
      {
      }
    }
    mob.tell(msg.toString());
    return false;
  }

  public Vector getAccessWords() {return accessWords;}
}