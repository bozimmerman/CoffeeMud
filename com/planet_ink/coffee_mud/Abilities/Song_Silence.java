package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;


public class Song_Silence extends Song
{

	public Song_Silence()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Silence";
		displayText="(Song of Silence)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		malicious=true;

		baseEnvStats().setLevel(13);

		addQualifyingClass(new Bard().ID(),13);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Silence();
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;
		if(affected==invoker) return;

		affectableStats.setSensesMask(affectableStats.sensesMask()|Sense.CAN_HEAR);
		affectableStats.setSensesMask(affectableStats.sensesMask()|Sense.CAN_SPEAK);
	}
}
