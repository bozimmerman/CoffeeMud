package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;

public class Prop_NoSummon extends Property
{
	public String ID() { return "Prop_NoSummon"; }
	public String name(){ return "Summon Spell Neutralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Prop_NoSummon();}
	
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;

		if((affect.tool()!=null)
		&&(affect.tool() instanceof Ability)
		&&(affect.source()!=null)
		&&(affect.source().location()!=null)
		&&((affect.source().location()==affected)
		   ||(affect.source().location().getArea()==affected))
		&&(Util.bset(((Ability)affect.tool()).flags(),Ability.FLAG_SUMMONING)))
		{
			affect.source().location().showHappens(Affect.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
			return false;
		}
		return true;
	}
}
