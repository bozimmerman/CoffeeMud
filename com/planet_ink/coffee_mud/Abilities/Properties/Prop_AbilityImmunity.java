package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_AbilityImmunity extends Property
{
	public String ID() { return "Prop_AbilityImmunity"; }
	public String name(){ return "Ability Immunity";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Prop_AbilityImmunity();}
	public String accountForYourself() { return "Immunity";	}
	private Vector diseases=new Vector();

	public void setMiscText(String newText)
	{
		diseases=Util.parseSemicolons(newText.toUpperCase(),true);
		super.setMiscText(newText);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if ( (msg.source() != null)
		&& (affected instanceof MOB)
	    && (msg.target() != null)
	    && (msg.tool() != null)
	    && (msg.amITarget(affected))
	    && (msg.tool() instanceof Ability ))
		{
			Ability d = (Ability)msg.tool();
			for(int i = 0; i < diseases.size(); i++)
			{
				if((EnglishParser.containsString(d.ID(),((String)diseases.elementAt(i))))
				||(EnglishParser.containsString(d.name(),((String)diseases.elementAt(i)))))
				{
					if(msg.target() instanceof MOB)
						((MOB)msg.target()).tell("You are immune to "+msg.tool().name()+".");
					if(msg.source()!=msg.target())
						msg.source().tell(affected.name()+" is immune to "+msg.tool().name()+".");
					else
					return false;
				}
			}
		}
		return super.okMessage(myHost, msg);
	}
}
