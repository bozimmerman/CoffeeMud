package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_GroveWalk extends Chant
{
	public String ID() { return "Chant_GroveWalk"; }
	public String name(){ return "Grove Walk";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_GroveWalk();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<1)
		{
			mob.tell("You must specify the name of the location of another grove where there is a druidic monument.");
			return false;
		}
		String areaName=Util.combine(commands,0).trim().toUpperCase();


		Room newRoom=null;
		boolean hereok=false;
		for(Enumeration e=CMMap.rooms();e.hasMoreElements();)
		{
			Room R=(Room)e.nextElement();
			if(Sense.canAccess(mob,R))
				for(int i=0;i<R.numItems();i++)
				{
					Item I=R.fetchItem(i);
					if((I!=null)&&(I.ID().equals("DruidicMonument")))
					{
						if(R==mob.location())
							hereok=true;
						if(CoffeeUtensils.containsString(R.displayText(),areaName))
						   newRoom=R;
						break;
					}
				}
			if((newRoom!=null)&&(hereok)) break;
		}
		if(!hereok)
		{
			mob.tell("There is no druidic monument here.  You can only use this chant in a druidic grove.");
			return false;
		}
		if(newRoom==null)
		{
			mob.tell("You can't seem to fixate on a place called '"+Util.combine(commands,0)+"', perhaps it is not a grove?");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,newRoom,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) and walk(s) around.^?");
			if((mob.location().okAffect(mob,msg))&&(newRoom.okAffect(mob,msg)))
			{
				mob.location().send(mob,msg);
				Hashtable h=ExternalPlay.properTargets(this,mob,false);
				if(h==null) return false;

				Room thisRoom=mob.location();
				for(Enumeration f=h.elements();f.hasMoreElements();)
				{
					MOB follower=(MOB)f.nextElement();
					FullMsg enterMsg=new FullMsg(follower,newRoom,this,Affect.MSG_ENTER,null,Affect.MSG_ENTER,null,Affect.MSG_ENTER,"<S-NAME> emerge(s) from around the stones.");
					FullMsg leaveMsg=new FullMsg(follower,thisRoom,this,Affect.MSG_LEAVE|Affect.MASK_MAGIC,"<S-NAME> disappear(s) around the stones.");
					if(thisRoom.okAffect(follower,leaveMsg)&&newRoom.okAffect(follower,enterMsg))
					{
						if(follower.isInCombat())
						{
							ExternalPlay.flee(follower,"NOWHERE");
							follower.makePeace();
						}
						thisRoom.send(follower,leaveMsg);
						newRoom.bringMobHere(follower,false);
						newRoom.send(follower,enterMsg);
						follower.tell("\n\r\n\r");
						ExternalPlay.look(follower,null,true);
					}
				}
			}

		}
		else
			beneficialVisualFizzle(mob,newRoom,"<S-NAME> chant(s) and walk(s) around, but nothing happens.");


		// return whether it worked
		return success;
	}
}
