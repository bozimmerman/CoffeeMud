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

public class Spell_Disenchant extends Spell
	implements EnchantmentDevotion
{
	public Spell_Disenchant()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Disenchant";

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
		return new Spell_Disenchant();
	}

	public boolean invoke(MOB mob, Vector commands)
	{

		if(commands.size()<1)
		{
			mob.tell("Disenchant what?");
			return false;
		}
		Environmental target=mob.location().fetchFromMOBRoom(mob,null,CommandProcessor.combine(commands,0));
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+((String)commands.elementAt(0))+"' here.");
			return false;
		}
		if(!(target instanceof Item))
		{
			mob.tell("You can't disenchant that!");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> hold(s) <T-NAME> and chant(s).");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(target.envStats().ability()<=0)
					mob.tell(target.name()+" doesn't seem to be enchanted.");
				else
				{
					mob.location().show(mob,target,Affect.VISUAL_WNOISE,target.name()+" fades and becomes dull!");
					target.baseEnvStats().setLevel(target.baseEnvStats().level()-(baseEnvStats().level()*2));
					if(target.baseEnvStats().level()<=0)
						target.baseEnvStats().setLevel(1);
					target.baseEnvStats().setAbility(0);
					if(Sense.isABonusItems(target))
						target.baseEnvStats().setDisposition(target.baseEnvStats().disposition()-Sense.IS_BONUS);
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