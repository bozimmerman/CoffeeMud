package com.planet_ink.coffee_mud.Commands;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.system.MOBloader;
import com.planet_ink.coffee_mud.Commands.base.*;
import com.planet_ink.coffee_mud.MOBS.StdMOB;

public class ClanExile implements Command
{
  protected Vector accessWords=new Vector();

  public ClanExile()
  {
    accessWords.addElement(new String("clanexile"));
  }

	public boolean execute(MOB mob, Vector commands)
	{
    String qual=Util.combine(commands,1).toUpperCase();
    StringBuffer msg=new StringBuffer("");
    Clan C=null;
    boolean found=false;
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
                Vector msgVector=new Vector();
                msgVector.addElement(new String("CLANTALK"));
                msgVector.addElement(new String("Member exiled: "+mob.ID()));
                Channels.channel(mob,msgVector,true);
                MOBloader.DBUpdateClan(qual, "", 0);
                mob.tell(M.ID()+" has been exiled from Clan '"+C.ID()+"'.");
                return false;
              }
              else
              {
                mob.tell(qual+" was not found.  Could not exile from Clan.");
                return false;
              }
            }
            else
            {
              Vector msgVector=new Vector();
              msgVector.addElement(new String("CLANTALK"));
              msgVector.addElement(new String("Member exiled: "+mob.ID()));
              Channels.channel(mob,msgVector,true);
              MOBloader.DBUpdateClan(qual, "", 0);
              mob.tell(M.ID()+" has been exiled from Clan '"+C.ID()+"'.");
              M.tell("You have been exiled from Clan '"+C.ID()+"'.");
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
          msg.append("You aren't in the right position to exile anyone from your clan.");
        }
      }
    }
    else
    {
      msg.append("You haven't specified which member you are exiling.");
    }
    mob.tell(msg.toString());
    return false;
  }


  public Vector getAccessWords() {return accessWords;}

}