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

	public String accountForYourself()
	{ return "Stats Trainer";	}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.STRENGTH,25);
		affectableStats.setStat(CharStats.WISDOM,25);
		affectableStats.setStat(CharStats.INTELLIGENCE,25);
		affectableStats.setStat(CharStats.CHARISMA,25);
		affectableStats.setStat(CharStats.CONSTITUTION,25);
		affectableStats.setStat(CharStats.DEXTERITY,25);
	}


}
