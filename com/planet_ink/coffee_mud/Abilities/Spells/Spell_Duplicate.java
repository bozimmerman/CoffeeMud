package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Duplicate extends Spell
{
	public String ID() { return "Spell_Duplicate"; }
	public String name(){return "Duplicate";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Spell_Duplicate();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ALTERATION;}
	protected int overrideMana(){return Integer.MAX_VALUE;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;

		if(!mob.isMine(target))
		{
			mob.tell("You'll need to pick it up first.");
			return false;
		}
		if(target instanceof ClanItem)
		{
			mob.tell("Clan items can not be duplicated.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int multiPlier=5+target.envStats().weight();
		multiPlier+=(target.numEffects()*10);
		multiPlier+=(target instanceof Potion)?10:0;
		multiPlier+=(target instanceof Pill)?10:0;
		multiPlier+=(target instanceof Wand)?5:0;

		int expLoss=(target.envStats().level()*multiPlier);
		mob.tell("You lose "+expLoss+" experience points.");
		MUDFight.postExperience(mob,null,null,-expLoss,false);

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> hold(s) <T-NAMESELF> and cast(s) a spell.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,target.name()+" blurs and divides into two!");
				Item newTarget=(Item)target.copyOf();
				newTarget.recoverEnvStats();
				if(target.owner() instanceof MOB)
					((MOB)target.owner()).addInventory(newTarget);
				else
				if(target.owner() instanceof Room)
					((Room)target.owner()).addItemRefuse(newTarget,Item.REFUSE_PLAYER_DROP);
				else
					mob.addInventory(newTarget);
				target.recoverEnvStats();
				mob.recoverEnvStats();
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> hold(s) <T-NAMESELF> tightly and incant(s), the spell fizzles.");


		// return whether it worked
		return success;
	}
}