package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Gate extends Spell
{
	public Spell_Gate()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Gate";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(13);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Gate();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_CONJURATION;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		if(commands.size()<1)
		{
			mob.tell("Gate to whom?");
			return false;
		}
		String areaName=Util.combine(commands,0).trim().toUpperCase();

		if(mob.location().fetchInhabitant(areaName)!=null)
		{
			mob.tell("Better look around first.");
			return false;
		}

		Room newRoom=null;
		MOB target=null;
		for(int m=0;m<CMMap.map.size();m++)
		{
			Room room=(Room)CMMap.map.elementAt(m);
			target=room.fetchInhabitant(areaName);
			if(target!=null)
			{
				newRoom=room;
				break;
			}
		}

		if(newRoom==null)
		{
			mob.tell("You can't seem to fixate on '"+Util.combine(commands,0)+"', perhaps they don't exist?");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,"<S-NAME> invoke(s) a teleportation spell.");
			if((mob.location().okAffect(msg))&&(newRoom.okAffect(msg)))
			{
				mob.location().send(mob,msg);
				Hashtable h=ExternalPlay.properTargets(this,mob);
				if(h==null) return false;

				Room thisRoom=mob.location();
				for(Enumeration f=h.elements();f.hasMoreElements();)
				{
					MOB follower=(MOB)f.nextElement();
					if((follower.isMonster())||(follower==mob))
					{
						FullMsg enterMsg=new FullMsg(follower,newRoom,this,Affect.MSG_ENTER,null,Affect.MSG_ENTER,null,Affect.MSG_ENTER,"<S-NAME> appear(s) in a burst of light.");
						FullMsg leaveMsg=new FullMsg(follower,thisRoom,this,Affect.MSG_LEAVE|Affect.MASK_MAGIC,"<S-NAME> disappear(s) in a burst of light.");
						if(thisRoom.okAffect(leaveMsg)&&newRoom.okAffect(enterMsg))
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

		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to invoke transportation, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
