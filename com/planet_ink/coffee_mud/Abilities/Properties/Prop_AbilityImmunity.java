package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_AbilityImmunity extends Property
{
	public String ID() { return "Prop_AbilityImmunity"; }
	public String name(){ return "Ability Immunity";}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_ROOMS|Ability.CAN_EXITS;}
	public Environmental newInstance(){	return new Prop_AbilityImmunity();}
	public String accountForYourself() { return "Immunity";	}
	private Vector diseases=new Vector();
	private Vector messages=new Vector();

	public void setMiscText(String newText)
	{
		diseases=Util.parseSemicolons(newText.toUpperCase(),true);
		for(int d=0;d<diseases.size();d++)
		{
			String s=(String)diseases.elementAt(d);
			int x=s.indexOf("=");
			if(x<0)	
				messages.addElement("");
			else
			{
				diseases.setElementAt(s.substring(0,x).trim(),d);
				messages.addElement(s.substring(x+1).trim());
			}
		}
		super.setMiscText(newText);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if ( (msg.source() != null)
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
					{
						String s=(String)messages.elementAt(i);
						if(s.length()>0)
							msg.source().tell(msg.source(),msg.target(),msg.tool(),s);
						else
							msg.source().tell("<T-NAME> seems immune to <O-NAME>.");
					}
					return false;
				}
			}
		}
		return super.okMessage(myHost, msg);
	}
}
