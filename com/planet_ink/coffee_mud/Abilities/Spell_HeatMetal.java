package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Items.Armor.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Spell_HeatMetal extends Spell
	implements AlterationDevotion
{
	public Spell_HeatMetal()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Heat Metal";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="";

		malicious=true;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(11);

		addQualifyingClass(new Mage().ID(),11);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_HeatMetal();
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
	}

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		for(int i=0;i<mob.inventorySize();i++)
		{
			Item item=mob.fetchInventory(i);
			if((!item.amWearingAt(Item.INVENTORY))&&(item.material()==Item.METAL))
			{
				int damage=Dice.roll(1,6,1);
				TheFight.doDamage(mob,damage);
				mob.location().show(mob,null,Affect.VISUAL_WNOISE,item.name()+" "+TheFight.hitWord(Weapon.TYPE_BURNING,damage)+" <S-NAME>!");
				if((item.amWearingAt(Item.WIELD))||(item.amWearingAt(Item.HELD)))
				{
					if(Dice.rollPercentage()>(100-mob.charStats().getStrength()))
					{
						FullMsg msg=new FullMsg(mob,item,null,Affect.HANDS_DROP,Affect.HANDS_DROP,Affect.VISUAL_WNOISE,"<S-NAME> drop(s) "+item.name()+"!");
						if(mob.location().okAffect(msg))
						{
							item.remove();
							mob.location().send(mob,msg);
						}
					}
				}
			}
		}

		return true;
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
	}



	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.STRIKE_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> invoke(s) a spell upon <T-NAME>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
					success=maliciousAffect(mob,target,0,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAME>, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}