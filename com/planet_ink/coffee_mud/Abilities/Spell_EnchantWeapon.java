package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.Armor.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Spell_EnchantWeapon extends Spell
	implements EnchantmentDevotion
{
	public Spell_EnchantWeapon()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Enchant Weapon";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(22);

		addQualifyingClass(new Mage().ID(),22);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_EnchantWeapon();
	}

	public boolean invoke(MOB mob, Vector commands)
	{

		if(commands.size()<1)
		{
			mob.tell("Enchant what?");
			return false;
		}
		Environmental target=mob.location().fetchFromMOBRoom(mob,null,CommandProcessor.combine(commands,0));
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+((String)commands.elementAt(0))+"' here.");
			return false;
		}
		if(!(target instanceof Weapon))
		{
			mob.tell("You can't enchant that with an Enchant Weapon spell!");
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

		mob.setExperience(mob.getExperience()-50);

		mob.curState().setMana(0);

		boolean success=profficiencyCheck(0);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> hold(s) <T-NAME> and chant(s).");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(target.envStats().ability()>5)
					mob.tell(target.name()+" cannot be enchanted further.");
				else
				{
					mob.location().show(mob,target,Affect.VISUAL_WNOISE,target.name()+" glows!");
					target.baseEnvStats().setAbility(target.baseEnvStats().ability()+1);
					target.baseEnvStats().setLevel(target.baseEnvStats().level()+2);
					target.recoverEnvStats();
				}
			}

		}
		else
			beneficialFizzle(mob,target,"<S-NAME> hold(s) <T-NAME> and chant(s), looking very frustrated.");


		// return whether it worked
		return success;
	}
}