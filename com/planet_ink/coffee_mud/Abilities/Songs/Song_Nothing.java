package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Nothing extends Song
{

	public Song_Nothing()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Nothing";
		displayText="(Song of Nothing at All)";
		miscText="";

		skipStandardSongInvoke=true;
		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(1);

		addQualifyingClass("Bard",1);

		setProfficiency(100);
		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Nothing();
	}

	public void setProfficiency(int newProfficiency)
	{
		super.setProfficiency(100);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		boolean foundOne=false;
		for(int a=0;a<mob.numAffects();a++)
		{
			Ability A=(Ability)mob.fetchAffect(a);
			if((A!=null)&&(A instanceof Song))
				foundOne=true;
		}
		unsing(mob);
		if(!foundOne)
		{
			mob.tell(mob,null,auto?"There is no song playing.":"You aren't singing.");
			return true;
		}

		mob.location().show(mob,null,Affect.MSG_NOISE,auto?"Silence.":"<S-NAME> stop(s) singing.");
		mob.location().recoverRoomStats();
		return true;
	}
}