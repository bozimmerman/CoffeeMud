package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_Trainer extends Property
{
	public Prop_Trainer()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Good training MOB";
	}

	public Environmental newInstance()
	{
		return new Prop_Trainer();
	}

	public String accountForYourself()
	{ return "Trainer";	}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		affectableStats.setStrength(25);
		affectableStats.setWisdom(25);
		affectableStats.setIntelligence(25);
		affectableStats.setCharisma(25);
		affectableStats.setConstitution(25);
		affectableStats.setDexterity(25);
	}


}
