package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.MiscMagic.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Spell_RechargeWand extends Spell
	implements EnchantmentDevotion
{
	public Spell_RechargeWand()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Recharge Wand";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(17);

		addQualifyingClass(new Mage().ID(),17);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_RechargeWand();
	}

	public boolean invoke(MOB mob, Vector commands)
	{

		if(commands.size()<1)
		{
			mob.tell("Recharge what?");
			return false;
		}
		Environmental target=mob.location().fetchFromMOBRoom(mob,null,CommandProcessor.combine(commands,0));
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+((String)commands.elementAt(0))+"' here.");
			return false;
		}
		if(!(target instanceof Wand))
		{
			mob.tell("You can't recharge that.");
			return false;
		}
		if(mob.curState().getMana()<mob.maxState().getMana())
		{
			mob.tell("You need to be at full mana to cast this.");
			return false;
		}

		if(mob.getExperience()<(mob.envStats().level()-1)*1000)
		{
			mob.tell("You need to gain more experience before you can cast this.");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;

		mob.curState().setMana(0);
		mob.setExperience(mob.getExperience()-50);


		boolean success=profficiencyCheck(0);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> chant(s) at <T-NAME> as sweat beads form on <S-HIS-HER> forhead.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.VISUAL_ONLY,"<T-NAME> glow(s) brightly!");
				((Wand)target).setUsesRemaining(((Wand)target).usesRemaining()+5);
			}

		}
		else
			beneficialFizzle(mob,target,"<S-NAME> chant(s) at <T-NAME>, looking more frustrated every minute.");


		// return whether it worked
		return success;
	}
}
