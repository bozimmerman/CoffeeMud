package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;


public class Song_Lethargy extends Song
{

	public Song_Lethargy()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Lethargy";
		displayText="(Song of Lethargy)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		malicious=true;

		baseEnvStats().setLevel(18);

		addQualifyingClass(new Bard().ID(),18);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Lethargy();
	}
	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;
		if(invoker==affected) return;

		affectableStats.setSpeed(Util.div(affectableStats.speed(),2.0));
	}
}
