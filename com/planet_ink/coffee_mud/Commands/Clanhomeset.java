package com.planet_ink.coffee_mud.Commands;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.Commands.base.*;

public class Clanhomeset implements Command
{
  protected Vector accessWords=new Vector();

  public Clanhomeset()
  {
    accessWords.addElement(new String("clanhomeset"));
  }

	public boolean execute(MOB mob, Vector commands)
	{
    LandTitle l=null;
    Room R=mob.location();

    if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
    {
      mob.tell("You aren't even a member of a clan.");
      return false;
    }
    else
    {
      Clan C=Clans.getClan(mob.getClanID());
      if((mob.getClanRole()==Clan.POS_BOSS)||(mob.getClanRole()==Clan.POS_LEADER))
      {
        for(int a=0;a<R.numAffects();a++)
        {
          if(R.fetchAffect(a) instanceof LandTitle)
            l=(LandTitle)R.fetchAffect(a);
        }
        if(l==null)
        {
          mob.tell("Your clan does not own this room.");
          return false;
        }
        else
        {
          if(l.landOwner().equalsIgnoreCase(mob.getClanID()))
          {
            C.setRecall(R.ID());
            C.update();
          }
          else
          {
            mob.tell("Your clan does not own this room.");
            return false;
          }
        }
      }
      else
      {
        mob.tell("You aren't in the right position to set your clan's home.");
        return false;
      }
    }
    return false;
  }

  public Vector getAccessWords() {return accessWords;}

}