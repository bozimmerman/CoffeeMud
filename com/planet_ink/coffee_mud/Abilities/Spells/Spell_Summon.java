package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Summon extends Spell
{
	public String ID() { return "Spell_Summon"; }
	public String name(){return "Summon";}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Spell_Summon();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		if(commands.size()<1)
		{
			mob.tell("Summon whom?");
			return false;
		}
		String areaName=Util.combine(commands,0).trim().toUpperCase();

		if(mob.location().fetchInhabitant(areaName)!=null)
		{
			mob.tell("Better look around first.");
			return false;
		}

		Room oldRoom=null;
		MOB target=null;
		for(int m=0;m<CMMap.numRooms();m++)
		{
			Room room=CMMap.getRoom(m);
			target=room.fetchInhabitant(areaName);
			if(target!=null)
			{
				oldRoom=room;
				break;
			}
		}

		if(oldRoom==null)
		{
			mob.tell("You can't seem to fixate on '"+Util.combine(commands,0)+"', perhaps they don't exist?");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int adjustment=target.envStats().level();
		if(target.isMonster()) adjustment=adjustment*3;
		boolean success=profficiencyCheck(-adjustment,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> summon(s) <T-NAME> in a mighty cry!^?");
			if((mob.location().okAffect(msg))&&(oldRoom.okAffect(msg)))
			{
				mob.location().send(mob,msg);

				MOB follower=target;
				Room newRoom=mob.location();
				FullMsg enterMsg=new FullMsg(follower,newRoom,this,Affect.MSG_ENTER,null,Affect.MSG_ENTER,null,Affect.MSG_ENTER,"<S-NAME> appears in a burst of light.");
				FullMsg leaveMsg=new FullMsg(follower,oldRoom,this,Affect.MSG_LEAVE|Affect.MASK_MAGIC,"<S-NAME> disappear(s) in a great summoning swirl.");
				if(oldRoom.okAffect(leaveMsg)&&newRoom.okAffect(enterMsg))
				{
					follower.makePeace();
					oldRoom.send(follower,leaveMsg);
					newRoom.bringMobHere(follower,false);
					newRoom.send(follower,enterMsg);
					follower.tell("\n\r\n\r");
					ExternalPlay.look(follower,null,true);
				}
			}

		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to summon '"+areaName+"', but fail(s).");


		// return whether it worked
		return success;
	}
}