package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;


public class Song_Strength extends Song
{
	private int amount=0;

	public Song_Strength()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Strength";
		displayText="(Song of Strength)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(21);

		addQualifyingClass(new Bard().ID(),21);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Strength();
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(invoker==null) return;
		if(affected==invoker)
			affectableStats.setStrength((int)Math.round(affectableStats.getStrength()-amount));
		else
			affectableStats.setStrength((int)Math.round(affectableStats.getStrength()+amount));
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		amount=Util.s_int(CommandProcessor.combine(commands,0));

		if(amount<=0)
		{
			mob.tell(mob,null,"Sing about how much strength?");
			return false;
		}

		if(amount>=mob.charStats().getStrength())
		{
			mob.tell(mob,null,"You can't sing away that much strength.");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;
		return true;
	}
}
