package com.planet_ink.coffee_mud.Commands;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.system.MOBloader;
import com.planet_ink.coffee_mud.Commands.base.*;
import com.planet_ink.coffee_mud.MOBS.StdMOB;

public class ClanAssign implements Command
{
  protected Vector accessWords=new Vector();

  public ClanAssign()
  {
    accessWords.addElement(new String("clanassign"));
  }

	public boolean execute(MOB mob, Vector commands)
	{
    String qual=((String)commands.elementAt(1)).toUpperCase();
    String pos=((String)commands.elementAt(2)).toUpperCase();
    StringBuffer msg=new StringBuffer("");
    Clan C=null;
    boolean found=false;
    int newPos;
    if(qual.length()>0)
    {
      if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
      {
        msg.append("You aren't even a member of a clan.");
      }
      else
      {
        C=Clans.getClan(mob.getClanID());
        if((mob.getClanRole()==Clan.POS_BOSS)||(mob.getClanRole()==Clan.POS_LEADER))
        {
          Vector apps=C.getMemberList();
          if(apps.size()<1)
          {
            mob.tell("There are no members in your clan");
            return false;
          }
          for(int q=0;q<apps.size();q++)
          {
           if(((String)apps.elementAt(q)).equalsIgnoreCase(qual))
           {
             found=true;
           }
          }
          if(found)
          {
            MOB M=null;
            for(int s=0;s<Sessions.size();s++)
            {
              Session S=(Session)Sessions.elementAt(s);
              if(S.mob().ID().equalsIgnoreCase(qual))
              {
                M=S.mob();
              }
            }
            if(M==null)
            {
              M=new StdMOB();
              M.setName(qual);
              if(MOBloader.DBReadUserOnly(M))
              {
                newPos=changeRole(M,C,pos);
                mob.tell(M.ID()+" has been assigned to be a "+Clans.getRoleName(newPos,false,false)+" of Clan '"+C.ID()+"'.");
                return false;
              }
              else
              {
                mob.tell(qual+" was not found.  Could not change Clan role.");
                return false;
              }
            }
            else
            {
              newPos=changeRole(M,C,pos);
              mob.tell(M.ID()+" has been assigned to be a "+Clans.getRoleName(newPos,false,false)+" of Clan '"+C.ID()+"'.");
              M.tell("You have been assigned to be a"+Clans.getRoleName(newPos,false,false)+" of Clan '"+C.ID()+"'.");
              return false;
            }
          }
          else
          {
            msg.append(qual+" isn't a member of your clan.");
          }
        }
        else
        {
          msg.append("You aren't in the right position assign anyone in your clan.");
        }
      }
    }
    else
    {
      msg.append("You haven't specified which member you are assigning a new role to.");
    }
    mob.tell(msg.toString());
    return false;
  }

  private int changeRole(MOB mob, Clan clan, String position)
  {
    int newPos=0;
    if(position.equalsIgnoreCase("BOSS"))
    {
      newPos=Clan.POS_BOSS;
    }
    else
    if(position.equalsIgnoreCase("LEADER"))
    {
      newPos=Clan.POS_LEADER;
    }
    else
    if(position.equalsIgnoreCase("TREASURER"))
    {
      newPos=Clan.POS_TREASURER;
    }
    else
    if(position.equalsIgnoreCase("MEMBER"))
    {
      newPos=Clan.POS_MEMBER;
    }
    else
    {
      mob.tell("Unknown role '"+position+"'");
      return 0;
    }
    Vector msgVector=new Vector();
    msgVector.addElement(new String("CLANTALK"));
    msgVector.addElement(new String(mob.ID()+" changed from "+Clans.getRoleName(mob.getClanRole(),true,false)+" to "+Clans.getRoleName(newPos,true,false)));
    Channels.channel(mob,msgVector,true);
    MOBloader.DBUpdateClan(mob.ID(), clan.ID(), newPos);
    return newPos;
  }

  public Vector getAccessWords() {return accessWords;}

}