package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Teleport extends Spell
{
	public String ID() { return "Spell_Teleport"; }
	public String name(){return "Teleport";}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){return new Spell_Teleport();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}
	public long flags(){return Ability.FLAG_TRANSPORTING;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		if((auto||mob.isMonster())&&((commands.size()<1)||(((String)commands.firstElement()).equals(mob.name()))))
		{
			commands.clear();
			commands.addElement(CMMap.getRandomArea().Name());
		}
		if(commands.size()<1)
		{
			mob.tell("Teleport to what area?");
			return false;
		}
		String areaName=Util.combine(commands,0).trim().toUpperCase();
		Vector candidates=new Vector();
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room room=(Room)r.nextElement();
			if((room.getArea().name().toUpperCase().startsWith(areaName))
			&&(Sense.canAccess(mob,room)))
				candidates.addElement(room);
		}

		if(candidates.size()==0)
		{
			mob.tell("You don't know of an area called '"+Util.combine(commands,0)+"'.");
			return false;
		}

		Room newRoom=null;
		int tries=0;
		while((tries<20)&&(newRoom==null))
		{
			newRoom=(Room)candidates.elementAt(Dice.roll(1,candidates.size(),-1));
			FullMsg enterMsg=new FullMsg(mob,newRoom,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null);
			Session session=mob.session();
			mob.setSession(null);
			if(!newRoom.okMessage(mob,enterMsg))
				newRoom=null;
			mob.setSession(session);
			tries++;
		}

		if(newRoom==null)
		{
			mob.tell("Your magic seems unable to take you to that area.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),"^S<S-NAME> invoke(s) a teleportation spell.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				HashSet h=properTargets(mob,givenTarget,false);
				if(h==null) return false;

				Room thisRoom=mob.location();
				for(Iterator f=h.iterator();f.hasNext();)
				{
					MOB follower=(MOB)f.next();
					FullMsg enterMsg=new FullMsg(follower,newRoom,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,"<S-NAME> appears in a puff of smoke."+CommonStrings.msp("appear.wav",10));
					FullMsg leaveMsg=new FullMsg(follower,thisRoom,this,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC,"<S-NAME> disappear(s) in a puff of smoke.");
					if(thisRoom.okMessage(follower,leaveMsg)&&newRoom.okMessage(follower,enterMsg))
					{
						if(follower.isInCombat())
						{
							CommonMsgs.flee(follower,("NOWHERE"));
							follower.makePeace();
						}
						thisRoom.send(follower,leaveMsg);
						newRoom.bringMobHere(follower,false);
						newRoom.send(follower,enterMsg);
						follower.tell("\n\r\n\r");
						CommonMsgs.look(follower,true);
					}
				}
			}

		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to invoke transportation, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
