package com.planet_ink.coffee_mud.Commands;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Commands.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.system.MOBloader;
import com.planet_ink.coffee_mud.MOBS.StdMOB;

public class ClanDetails implements Command
{
  protected Vector accessWords=new Vector();

  public ClanDetails()
  {
    accessWords.addElement(new String("clandetails"));
  }


	public boolean execute(MOB mob, Vector commands)
	{
		String qual=Util.combine(commands,1).toUpperCase();
    StringBuffer msg=new StringBuffer("");
    if(qual.length()>0)
    {
      boolean found=false;
      for(int j=0;j<Clans.size();j++)
      {
        Clan C=Clans.elementAt(j);
        if(CoffeeUtensils.containsString(C.ID(), qual))
        {
          msg.append(createDetailString(C,mob));
          found=true;
        }
      }
      if(!found)
      {
        msg.append("No clan was found by the name of '"+qual+"'.\n\r");
      }
    }
    else
    {
      msg.append("You need to specify which clan you would like details on.\n\r");
    }
    mob.tell(msg.toString());
    return false;
	}

  public String createDetailString(Clan C, MOB mob)
  {
    StringBuffer msg=new StringBuffer("");
    msg.append("Clan Profile: "+C.ID()+"\n\r"
              +"-----------------------------------------------------------------\n\r"
              +C.getPremise()+"\n\r"
              +"-----------------------------------------------------------------\n\r"
              +Util.padRight(Clans.getRoleName(Clan.POS_BOSS,true,true),16)+": "+crewList(C,Clan.POS_BOSS)+"\n\r"
              +Util.padRight(Clans.getRoleName(Clan.POS_LEADER,true,true),16)+": "+crewList(C,Clan.POS_LEADER)+"\n\r"
              +Util.padRight(Clans.getRoleName(Clan.POS_TREASURER,true,true),16)+": "+crewList(C,Clan.POS_TREASURER)+"\n\r"
              +"Total Members   : "+C.getSize()+"\n\r"
              +"Clan Alignment  : "+CommonStrings.alignmentStr(C.getAlign())+"\n\r");
    if(mob.getClanID().equalsIgnoreCase(C.ID()))
    {
      msg.append("-----------------------------------------------------------------\n\r"
                +Util.padRight(Clans.getRoleName(Clan.POS_MEMBER,true,true),16)+": "+crewList(C,Clan.POS_MEMBER)+"\n\r");
      if((mob.getClanRole()==Clan.POS_BOSS)||(mob.getClanRole()==Clan.POS_LEADER))
      {
        msg.append("-----------------------------------------------------------------\n\r"
                +Util.padRight(Clans.getRoleName(Clan.POS_APPLICANT,true,true),16)+": "+crewList(C,Clan.POS_APPLICANT)+"\n\r");
      }
    }

    if((mob.getBitmap()&MOB.ATT_SYSOPMSGS)>0)
    {
      msg.append("-----------------------------------------------------------------\n\r"
                +Util.padRight(Clans.getRoleName(Clan.POS_MEMBER,true,true),16)+": "+crewList(C,Clan.POS_MEMBER)+"\n\r");
      msg.append("-----------------------------------------------------------------\n\r"
                +Util.padRight(Clans.getRoleName(Clan.POS_APPLICANT,true,true),16)+": "+crewList(C,Clan.POS_APPLICANT)+"\n\r");
    }
    return msg.toString();
  }

  public String crewList(Clan C, int posType)
  {
    StringBuffer list=new StringBuffer("");
    Vector Members=new Vector();
    MOB m;

    Members = C.getMemberList(posType);
    Members.trimToSize();
    if(Members.size()>1)
    {
      for(int j=0;j<(Members.size() - 1);j++)
      {
        list.append(Members.elementAt(j)+", ");
      }
      list.append("and "+Members.lastElement());
    }
    else
    if(Members.size()>0)
    {
      list.append((String)Members.firstElement());
    }

    return list.toString();
  }

  public Vector getAccessWords() {return accessWords;}

}