package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_NoRecall extends Property
{
	public Prop_NoRecall()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Recall Neuralizing";
		canAffectCode=Ability.CAN_ROOMS|Ability.CAN_AREAS;
	}

	public Environmental newInstance()
	{
		return new Prop_NoRecall();
	}

	public String accountForYourself()
	{ return "No Recall Field";	}

	public boolean okAffect(Affect affect)
	{
		if(affect.sourceMinor()==affect.TYP_RECALL)
		{
			if((affect.source()!=null)&&(affect.source().location()!=null))
				affect.source().location().show(affect.source(),null,Affect.MSG_OK_ACTION,"<S-NAME> attempt(s) to recall, but the magic fizzles.");
			return false;
		}
		return super.okAffect(affect);
	}
}
