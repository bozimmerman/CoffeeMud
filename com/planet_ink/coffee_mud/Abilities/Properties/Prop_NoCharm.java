package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;


public class Prop_NoCharm extends Property
{
	public String ID() { return "Prop_NoCharm"; }
	public String name(){ return "Charm Spell Neutralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Prop_NoCharm();}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.tool()!=null)
		&&(msg.tool() instanceof Ability)
		&&(msg.source()!=null)
		&&(msg.source().location()!=null)
		&&((msg.source().location()==affected)
		   ||(msg.source().location().getArea()==affected))
		&&(Util.bset(((Ability)msg.tool()).flags(),Ability.FLAG_CHARMING)))
		{
			msg.source().location().showHappens(CMMsg.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
			return false;
		}
		return true;
	}
}
