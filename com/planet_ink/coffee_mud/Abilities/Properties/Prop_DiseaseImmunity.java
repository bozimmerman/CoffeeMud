package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_DiseaseImmunity extends Property 
{
	public String ID() { return "Prop_DiseaseImmunity"; }
	public String name(){ return "Disease Immunity";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Prop_DiseaseImmunity();}
	public String accountForYourself() { return "Disease Immunity";	}
	private Vector diseases=new Vector();

	public void setMiscText(String newText)
	{
		diseases=Util.parse(newText.toUpperCase());
		super.setMiscText(newText);
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if ( (affect.source() != null)
		&& (affected instanceof MOB) 
	    && (affect.target() != null)
	    && (affect.tool() != null)
	    && (affect.amITarget(affected))
	    && (affect.tool() instanceof DiseaseAffect )) 
		{
			DiseaseAffect d = (DiseaseAffect)affect.tool();
			for(int i = 0; i < diseases.size(); i++) 
			{
				if((CoffeeUtensils.containsString(d.ID(),((String)diseases.elementAt(i))))
				||(CoffeeUtensils.containsString(d.name(),((String)diseases.elementAt(i)))))
				{
					affect.source().tell(affected.name()+" is immune to "+affect.tool().name()+".");
					return false;
				}
			}
		}
		return super.okAffect(myHost, affect);
	}
}
