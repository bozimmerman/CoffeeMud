package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Dexterity extends Song
{
	public String ID() { return "Song_Dexterity"; }
	public String name(){ return "Dexterity";}
	public String displayText(){ return "(Song of Dexterity)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	private int amount=0;
	public Environmental newInstance(){	return new Song_Dexterity();}

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
