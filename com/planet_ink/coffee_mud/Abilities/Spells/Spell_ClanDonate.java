package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_ClanDonate extends Spell
{
	public String ID() { return "Spell_ClanDonate"; }
	public String name(){return "Clan Donate";}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){return new Spell_ClanDonate();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}
	protected int overrideMana(){return 5;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,null,commands,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;
		Room clanDonateRoom=null;
		if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
		{
			mob.tell("You aren't even a member of a clan.");
			return false;
		}
		else
		{
			Clan C=Clans.getClan(mob.getClanID());
			clanDonateRoom=CMMap.getRoom(C.getDonation());
		}
		if(clanDonateRoom==null)
		{
			mob.tell("Your clan does not have a donation home.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),"^S<S-NAME> invoke(s) a donation spell upon <T-NAMESELF>.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().showHappens(Affect.MSG_OK_VISUAL,target.name()+" vanishes!");
				clanDonateRoom.bringItemHere(target,Item.REFUSE_PLAYER_DROP);
				clanDonateRoom.recoverRoomStats();
				clanDonateRoom.showHappens(Affect.MSG_OK_VISUAL,target.name()+" appears!");
				
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke donation upon <T-NAMESELF>, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
