package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.Items.Weapons.Weapon;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Spell_Flameshield extends Spell
	implements EvocationDevotion
{
	public Spell_Flameshield()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Flameshield";
		displayText="(Flameshield)";

		baseEnvStats().setLevel(9);

		addQualifyingClass(new Mage().ID(),baseEnvStats().level());
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Flameshield();
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		mob.tell("Your flame shield disappears.");
	}

	public void affect(Affect affect)
	{
		super.affect(affect);
		if(invoker==null) return;
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;

		if(affect.target()==affected)
		{
			if((affect.targetCode()==Affect.STRIKE_HANDS)
			||(affect.targetType()==Affect.HANDS))
			{
				if((Dice.rollPercentage()>(affect.source().charStats().getDexterity()*3)))
				{
					FullMsg msg=new FullMsg(affect.source(),affected,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,null);
					if(affect.source().location().okAffect(msg))
					{
						affect.source().location().send(affect.source(),msg);
						if(!msg.wasModified())
						{
							int damage = Dice.roll(2,invoker.envStats().level(),1);
							affect.source().location().show(affect.source(),affected,Affect.VISUAL_WNOISE,"The flame shield around <S-NAME> "+TheFight.hitWord(Weapon.TYPE_BURNING,damage)+" <T-NAME>!");
							TheFight.doDamage(affect.source(),damage);
						}
					}
				}
			}

		}
		return;
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		MOB mob=(MOB)affected;

		affectableStats.setArmor(affectableStats.armor()-mob.envStats().level());
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> chant(s) and wave(s) <S-HIS-HER> arms.  A field of flames erupt(s) around <T-NAME>!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialFizzle(mob,target,"<S-NAME> chant(s) and wave(s) <S-HIS-HER> arms, but only sparks emerge.");


		// return whether it worked
		return success;
	}
}