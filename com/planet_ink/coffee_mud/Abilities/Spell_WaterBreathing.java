package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.Locales.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;


public class Spell_WaterBreathing extends Spell
	implements AbjurationDevotion
{

	public Spell_WaterBreathing()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Water Breathing";
		displayText="(Water Breathing)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(4);

		addQualifyingClass(new Mage().ID(),4);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_WaterBreathing();
	}
	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		mob.tell(mob,null,"Your gills disappear.");
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if((!Sense.canBreathe(mob))&&(mob.location() instanceof UnderWater))
			affectableStats.setSensesMask(affectableStats.sensesMask()-Sense.CAN_BREATHE);
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=getTarget(mob,commands);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> whistle(s) to <T-NAME>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.VISUAL_WNOISE,"<T-NAME> grow(s) a pair of gills!");
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialFizzle(mob,target,"<S-NAME> whistle(s) to <T-NAME>, but nothing happens.");

		return success;
	}
}
