package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_NoRecall extends Property
{
	public String ID() { return "Prop_NoRecall"; }
	public String name(){ return "Recall Neuralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS;}

	public String accountForYourself()
	{ return "No Recall Field";	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(msg.sourceMinor()==CMMsg.TYP_RECALL)
		{
			if((msg.source()!=null)&&(msg.source().location()!=null))
				msg.source().location().show(msg.source(),null,CMMsg.MSG_OK_ACTION,"<S-NAME> attempt(s) to recall, but the magic fizzles.");
			return false;
		}
		return super.okMessage(myHost,msg);
	}
}
