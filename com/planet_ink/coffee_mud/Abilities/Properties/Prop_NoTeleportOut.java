package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;

public class Prop_NoTeleportOut extends Property
{
	public String ID() { return "Prop_NoTeleportOut"; }
	public String name(){ return "Teleport OUT OF Spell Neutralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS;}
	public Environmental newInstance(){	return new Prop_NoTeleportOut();}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;

		if((affect.tool()!=null)
		&&(affect.tool() instanceof Ability)
		&&(affect.source()!=null)
		&&(affect.source().location()!=null)
		&&(affect.source().location()!=affected)
		&&(affect.source().location().getArea()!=affected)
		&&(Util.bset(((Ability)affect.tool()).flags(),Ability.FLAG_TRANSPORTING)))
		{
			affect.source().location().showHappens(Affect.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
			return false;
		}
		return true;
	}
}
