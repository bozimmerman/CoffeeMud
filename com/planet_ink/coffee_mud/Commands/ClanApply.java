package com.planet_ink.coffee_mud.Commands;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.system.MOBloader;
import com.planet_ink.coffee_mud.Commands.base.*;

public class ClanApply implements Command
{
  protected Vector accessWords=new Vector();

  public ClanApply()
  {
    accessWords.addElement(new String("clanapply"));
  }


	public boolean execute(MOB mob, Vector commands)
	{
    String qual=Util.combine(commands,1).toUpperCase();
    StringBuffer msg=new StringBuffer("");
    if(qual.length()>0)
    {
      if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
      {
        Clan C=Clans.getClan(qual);
        if(C!=null)
        {
          if(AbilityHelper.zapperCheck(C.getAcceptanceSettings(),mob))
          {
            MOBloader.DBUpdateClan(mob.ID(), C.ID(), Clan.POS_APPLICANT);
            Vector msgVector=new Vector();
            msgVector.addElement(new String("CLANTALK"));
            msgVector.addElement(new String("New Applicant: "+mob.ID()));
            Channels.channel(mob,msgVector,true);
          }
          else
          {
            msg.append("You are not of the right qualities to join "+C.ID());
          }
        }
        else
        {
          msg.append("There is no clan named '"+qual+"'.");
        }
      }
      else
      {
        msg.append("You are already a member of "+mob.getClanID()+". You need to resign from your before you can apply to another.");
      }
    }
    else
    {
      msg.append("You haven't specified which clan you are applying to.");
    }
    mob.tell(msg.toString());
    return false;
  }


  public Vector getAccessWords() {return accessWords;}

}