package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_StatTrainer extends Property
{
	public Prop_StatTrainer()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Good training MOB";
	}

	public Environmental newInstance()
	{
		return new Prop_StatTrainer();
	}

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
