package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;


public class Song_Rage extends Song
{

	public Song_Rage()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Rage";
		displayText="(Song of Rage)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		malicious=true;
		mindAttack=true;

		baseEnvStats().setLevel(7);

		addQualifyingClass(new Bard().ID(),7);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Rage();
	}
	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;
		if(affected==invoker) return;
		affectableStats.setDamage(affectableStats.damage()+affected.envStats().level());
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-((int)Math.round(invoker.envStats().level())*5));
		affectableStats.setArmor(affectableStats.armor()+((int)Math.round(invoker.envStats().level())*5));
	}


	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if(affect.amISource(invoker)) return true;
		if(affect.sourceCode()!=Affect.MOVE_FLEE) return true;
		if(affect.source().fetchAffect(this.ID())==null) return true;

		affect.source().tell(affect.source(),null,"You are too enraged to flee.");
		return false;
	}

}
