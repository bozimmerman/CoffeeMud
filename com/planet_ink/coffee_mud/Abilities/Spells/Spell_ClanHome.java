package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_ClanHome extends Spell
{
	public String ID() { return "Spell_ClanHome"; }
	public String name(){return "Clan Home";}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){return new Spell_ClanHome();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}
	public long flags(){return Ability.FLAG_TRANSPORTING;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Room clanHomeRoom=null;
		if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
		{
			mob.tell("You aren't even a member of a clan.");
			return false;
		}
		else
		{
			Clan C=Clans.getClan(mob.getClanID());
			clanHomeRoom=CMMap.getRoom(C.getRecall());
		}
		if(clanHomeRoom==null)
		{
			mob.tell("Your clan does not have a clan home.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),"^S<S-NAME> invoke(s) a teleportation spell.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				Hashtable h=ExternalPlay.properTargets(this,mob,false);
				if(h==null) return false;

				Room thisRoom=mob.location();
				for(Enumeration f=h.elements();f.hasMoreElements();)
				{
					MOB follower=(MOB)f.nextElement();
					FullMsg enterMsg=new FullMsg(follower,clanHomeRoom,this,Affect.MSG_ENTER,null,Affect.MSG_ENTER,null,Affect.MSG_ENTER,"<S-NAME> appears in a puff of red smoke.");
					FullMsg leaveMsg=new FullMsg(follower,thisRoom,this,Affect.MSG_LEAVE|Affect.MASK_MAGIC,"<S-NAME> disappear(s) in a puff of red smoke.");
					if(thisRoom.okAffect(follower,leaveMsg)&&clanHomeRoom.okAffect(follower,enterMsg))
					{
						if(follower.isInCombat())
						{
							ExternalPlay.flee(follower,"NOWHERE");
							follower.makePeace();
						}
						thisRoom.send(follower,leaveMsg);
						clanHomeRoom.bringMobHere(follower,false);
						clanHomeRoom.send(follower,enterMsg);
						follower.tell("\n\r\n\r");
						ExternalPlay.look(follower,null,true);
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
