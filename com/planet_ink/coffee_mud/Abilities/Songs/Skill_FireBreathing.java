package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;

public class Skill_FireBreathing extends BardSkill
{
	public String ID() { return "Skill_FireBreathing"; }
	public String name(){ return "Fire Breathing";}
	public int quality(){return Ability.MALICIOUS;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	private static final String[] triggerStrings = {"FIREBREATHING","FIREBREATH"};
	public String[] triggerStrings(){return triggerStrings;}
	public int maxRange(){return 5;}
	public int minRange(){return 0;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		Item fireSource=null;
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if((Sense.isOnFire(I))
			&&(!I.amWearingAt(Item.INVENTORY))
			&&(I.container()==null))
			{
				fireSource=I;
				break;
			}
		}

		if((!auto)&&(fireSource==null))
		{
			mob.tell("You need to be holding some fire source to breathe fire.");
			return false;
		}
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MASK_MALICIOUS|CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_GENERAL:0),(auto?"Suddenly flames come up and attack <T-HIM-HER>!^?":((fireSource!=null)?"^S<S-NAME> hold(s) "+fireSource.name()+" up and puff(s) fire at <T-NAMESELF>!^?":"<S-NAME> breath(es) fire at <T-NAMESELF>!^?"))+CommonStrings.msp("fireball.wav",40));
			FullMsg msg2=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_FIRE|(auto?CMMsg.MASK_GENERAL:0),null);
			if((mob.location().okMessage(mob,msg))&&((mob.location().okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
                int numDice = (int)Math.round(Util.div(adjustedLevel(mob),2.0))+1;
				int damage = Dice.roll(3, numDice, 0);
				if(msg2.value()>0)
					damage = (int)Math.round(Util.div(damage,2.0));

				if(target.location()==mob.location())
					MUDFight.postDamage(mob,target,this,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,"The flames <DAMAGE> <T-NAME>!");
			}
			fireSource.destroy();
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to breathe fire at <T-NAMESELF>, but only puff(s) smoke.");


		// return whether it worked
		return success;
	}
}
