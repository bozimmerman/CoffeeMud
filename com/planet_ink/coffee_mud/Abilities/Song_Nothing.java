package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
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

		addQualifyingClass(new Bard().ID(),1);

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

	public boolean invoke(MOB mob, Vector commands)
	{
		boolean foundOne=false;
		for(int a=0;a<mob.numAffects();a++)
		{
			Ability A=(Ability)mob.fetchAffect(a);
			if(A instanceof Song)
				foundOne=true;
		}
		unsing(mob);
		if(!foundOne)
		{
			mob.tell(mob,null,"You aren't singing.");
			return true;
		}

		mob.location().show(mob,null,Affect.SOUND_MAGIC,"<S-NAME> stop(s) singing.");
		mob.location().recoverRoomStats();
		return true;
	}
}