package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;


public class Song_Dexterity extends Song
{
	private int amount=0;

	public Song_Dexterity()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Dexterity";
		displayText="(Song of Dexterity)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(14);

		addQualifyingClass(new Bard().ID(),14);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Dexterity();
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(invoker==null) return;
		if(affected==invoker)
			affectableStats.setDexterity((int)Math.round(affectableStats.getDexterity()-amount));
		else
			affectableStats.setDexterity((int)Math.round(affectableStats.getDexterity()+amount));
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		amount=Util.s_int(CommandProcessor.combine(commands,0));

		if(amount<=0)
		{
			mob.tell(mob,null,"Sing about how much dexterity?");
			return false;
		}

		if(amount>=mob.charStats().getDexterity())
		{
			mob.tell(mob,null,"You can't sing away that much dexterity.");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;
		return true;
	}
}
