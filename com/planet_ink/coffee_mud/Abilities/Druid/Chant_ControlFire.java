package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_ControlFire extends Chant
{
	public Chant_ControlFire()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Control Fire";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Control Fire spell)";

		quality=Ability.MALICIOUS;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(8);
		maxRange=5;
		minRange=0;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Chant_ControlFire();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		boolean success=profficiencyCheck(0,auto);
		
		Item fireSource=null;
		for(int i=0;i<target.inventorySize();i++)
		{
			Item I=target.fetchInventory(i);
			if((Sense.isOnFire(I))&&(I.container()==null))
			{
				fireSource=I;
				break;
			}
		}
		
		if(fireSource==null)
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().fetchItem(i);
			if((Sense.isOnFire(I))&&(I.container()==null))
			{
				fireSource=I;
				break;
			}
		}

		if((success)&&(fireSource!=null))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"A lightning bolt streaks out of the sky!":"<S-NAME> chant(s) to <T-NAMESELF>.  Suddenly "+fireSource.name()+" flares up and attacks <T-HIM-HER>!");
			FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_FIRE|(auto?Affect.ACT_GENERAL:0),null);
			if((mob.location().okAffect(msg))&&((mob.location().okAffect(msg2))))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
                int numDice = (int)Math.round(Util.div(adjustedLevel(mob),2.0));
				int damage = Dice.roll(numDice, 6, 10);
				if((!msg.wasModified())||(msg2.wasModified()))
					damage = (int)Math.round(Util.div(damage,2.0));

				if(target.location()==mob.location())
					ExternalPlay.postDamage(mob,target,this,damage,Affect.ACT_GENERAL|Affect.TYP_FIRE,Weapon.TYPE_BURNING,"The flames <DAMAGE> <T-NAME>!");
			}
			fireSource.destroyThis();
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
