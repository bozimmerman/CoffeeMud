package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;


public class Song_Clumsiness extends Song
{

	public Song_Clumsiness()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Clumsiness";
		displayText="(Song of Clumsiness)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		malicious=true;

		baseEnvStats().setLevel(6);

		addQualifyingClass(new Bard().ID(),6);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Clumsiness();
	}
	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;
		if(affected==invoker) return;

		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-((int)Math.round(invoker.envStats().level())*3));
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(invoker==null) return;
		if(affected==invoker) return;

		affectableStats.setDexterity((int)Math.round(Util.div(affectableStats.getDexterity(),2.0)));
	}
}
