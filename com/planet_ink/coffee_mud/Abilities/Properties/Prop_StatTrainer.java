package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_StatTrainer extends Property
{
	public String ID() { return "Prop_StatTrainer"; }
	public String name(){ return "Good training MOB";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Prop_StatTrainer();}
	private static final int[] all25={25,25,25,25,25,25};
	private int[] stats=all25;

	public String accountForYourself()
	{ return "Stats Trainer";	}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		if(Util.bset(affectedMOB.getBitmap(),MOB.ATT_NOTEACH))
			affectedMOB.setBitmap(Util.unsetb(affectedMOB.getBitmap(),MOB.ATT_NOTEACH));
		for(int i=0;i<stats.length;i++)
			affectableStats.setStat(i,stats[i]);
	}
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		if(newMiscText.length()>0)
		{
			stats=new int[CharStats.NUM_BASE_STATS];
			stats[CharStats.STRENGTH]=super.getParmVal(newMiscText,"STR",25);
			stats[CharStats.INTELLIGENCE]=super.getParmVal(newMiscText,"INT",25);
			stats[CharStats.WISDOM]=super.getParmVal(newMiscText,"WIS",25);
			stats[CharStats.CONSTITUTION]=super.getParmVal(newMiscText,"CON",25);
			stats[CharStats.CHARISMA]=super.getParmVal(newMiscText,"CHA",25);
			stats[CharStats.DEXTERITY]=super.getParmVal(newMiscText,"DEX",25);
		}
	}

}
