package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
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
		quality=Ability.BENEFICIAL_OTHERS;

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
			affectableStats.setStat(CharStats.DEXTERITY,(int)Math.round(affectableStats.getStat(CharStats.DEXTERITY)-amount));
		else
			affectableStats.setStat(CharStats.DEXTERITY,(int)Math.round(affectableStats.getStat(CharStats.DEXTERITY)+amount));
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		amount=Util.s_int(Util.combine(commands,0));

		if(amount<=0)
		{
			mob.tell(mob,null,"Sing about how much dexterity?");
			return false;
		}

		if(amount>=mob.charStats().getStat(CharStats.DEXTERITY))
		{
			mob.tell(mob,null,"You can't sing away that much dexterity.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		return true;
	}
}
