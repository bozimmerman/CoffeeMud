package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;


public class Spell_MagicalAura extends Spell
	implements AlterationDevotion
{

	public Spell_MagicalAura()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Magical Aura";
		displayText="(Magical Aura)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(1);

		addQualifyingClass(new Mage().ID(),1);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_MagicalAura();
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_BONUS);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null))
			return;
		if(affected instanceof MOB)
			((MOB)affected).tell("Your magical aura fades.");

		super.unInvoke();

	}


	public boolean invoke(MOB mob, Vector commands)
	{
		String aroundWhat=CommandProcessor.combine(commands,0);
		if(aroundWhat.trim().length()==0)
		{
			mob.tell("Put a magical aura around what?");
			return false;
		}
		Environmental target=mob.location().fetchFromMOBRoom(mob,null,aroundWhat);
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+((String)commands.elementAt(0))+"' here.");
			return false;
		}
		if(target.fetchAffect(this.ID())!=null)
		{
			mob.tell("There is already a magical aura around "+target.name()+".");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.VISUAL_WNOISE,"<S-NAME> invoke(s) a magical aura around <T-NAME>.");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,0);
		}
		else
			beneficialFizzle(mob,target,"<S-NAME> attempt(s) to invoke a magical aura, but fail(s).");

		return success;
	}
}