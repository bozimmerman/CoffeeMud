package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_NarrowLedge extends Property
{
	public String ID() { return "Prop_NarrowLedge"; }
	public String name(){ return "The Narrow Ledge";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_EXITS;}
	public Environmental newInstance(){	return new Prop_NarrowLedge();}
	
	protected int check=16;
	protected String name="the narrow ledge";
	
	public String accountForYourself()
	{ return "Very narrow";	}

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		check=getParmVal(newText,"check",16);
		name=getParmStr(newText,"name","the narrow ledge");
	}
	
	public void affect(Affect msg)
	{
		if((msg.targetMinor()==Affect.TYP_ENTER)
		||(msg.targetMinor()==Affect.TYP_LEAVE))
		{
			MOB mob=msg.source();
			if(Sense.isInFlight(mob)) return;
			if(Dice.roll(1,check,-mob.charStats().getStat(CharStats.DEXTERITY))>0)
				msg.addTrailerMsg(new FullMsg(mob,null,Affect.MSG_DEATH,"<S-NAME> fall(s) off "+name+" to <S-HIS-HER> death!!"));
		}
		super.affect(msg);
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		// always disable flying restrictions!
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SLEEPING);
	}
}
