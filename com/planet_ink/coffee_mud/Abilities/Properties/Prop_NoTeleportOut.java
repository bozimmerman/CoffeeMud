package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;

public class Prop_NoTeleportOut extends Property
{
	public String ID() { return "Prop_NoTeleportOut"; }
	public String name(){ return "Teleport OUT OF Spell Neutralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.tool()!=null)
		&&(msg.tool() instanceof Ability)
		&&(msg.source()!=null)
		&&(msg.source().location()!=null)
		&&(msg.sourceMinor()!=CMMsg.TYP_ENTER))
		{
			boolean shere=(msg.source().location()==affected)||(msg.source().location().getArea()==affected);
			boolean summon=Util.bset(((Ability)msg.tool()).flags(),Ability.FLAG_SUMMONING);
			boolean teleport=Util.bset(((Ability)msg.tool()).flags(),Ability.FLAG_TRANSPORTING);
			if(((shere)&&(!summon)&&(teleport))
			   ||((!shere)&&(summon)))
			{
				msg.source().location().showHappens(CMMsg.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
				return false;
			}
		}
		return true;
	}
}
