package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Extinguish extends Prayer
{
	public String ID() { return "Prayer_Extinguish"; }
	public String name(){ return "Extinguish";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_ROOMS|Ability.CAN_EXITS;}
	public int quality(){ return OK_OTHERS;}
	public int holyQuality(){ return HOLY_GOOD;}
	public Environmental newInstance(){	return new Prayer_Extinguish();}

	public static void endIt(MOB source, Environmental target)
	{
		for(int a=target.numAffects()-1;a>=0;a--)
		{
			Ability A=target.fetchAffect(a);
			if(A!=null)
			{
				boolean notOne=true;
				for(int i=0;i<fireSpells.length;i++)
					if(A.ID().equalsIgnoreCase(fireSpells[i]))
					{
						A.unInvoke();
						notOne=false;
						break;
					}
				if(notOne)
				if(A.ID().equalsIgnoreCase("Spell_SummonElemental")
				&&(A.text().toUpperCase().indexOf("FIRE")>=0))
				{
					A.unInvoke();
					notOne=false;
				}
			}
		}
		if((target instanceof MOB)
		&&(((MOB)target).charStats().getMyRace().ID().equals("FireElemental")))
			ExternalPlay.postDeath(source,(MOB)target,null);
		if((target instanceof Light)&&(((Light)target).isLit()))
		{
			((Light)target).tick(target,Host.LIGHT_FLICKERS);
			((Light)target).light(false);
		}
	}

	public static final String[] fireSpells={
		"Burning", 
		"Prayer_CurseMetal",
		"Spell_HeatMetal",
		"Prayer_FlameWeapon",
		"Prayer_Demonshield",
		"Prayer_ProtCold",
		"Spell_ResistCold",
		"Spell_ColdWard",
		"Spell_Flameshield",
		"Spell_WallOfFire",
		"Chant_SummonFire"
		};
	
	public static boolean isBurning(Environmental item)
	{
		for(int i=0;i<fireSpells.length;i++)
			if(item.fetchAffect(fireSpells[i])!=null)
				return true;
		Ability A=item.fetchAffect("Spell_SummonElemental");
		if((A!=null)&&(A.text().toUpperCase().indexOf("FIRE")>=0))
			return true;
		if((item instanceof MOB)&&(((MOB)item).charStats().getMyRace().ID().equals("FireElemental")))
			return true;
		if(item instanceof MOB)
		{
			MOB mob=(MOB)item;
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item I=mob.fetchInventory(i);
				if((I!=null)&&(isBurning(I)))
					return true;
			}
		}
		if((item instanceof Light)&&(((Light)item).isLit()))
			return true;
			
		return false;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget, Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" to extinguish <T-NAMESELF>.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				endIt(mob,target);
				target.recoverEnvStats();
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" to extinguish <T-NAMESELF>, but nothing happens.");
		// return whether it worked
		return success;
	}
}
