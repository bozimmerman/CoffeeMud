package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;

public class Prop_NoCharm extends Property
{
	public String ID() { return "Prop_NoCharm"; }
	public String name(){ return "Charm Spell Neutralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Prop_NoCharm();}
	
	public final static String spellList=
		(" Spell_Charm Spell_Enthrall Chant_CharmAnimal ").toUpperCase();
	
	public String text(){ return spellList; }

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if((affect.tool()!=null)
		&&(affect.tool() instanceof Ability)
		&&(affect.source()!=null)
		&&(affect.source().location()!=null)
		&&((affect.source().location()==affected)
		   ||(affect.source().location().getArea()==affected))
		&&(text().toUpperCase().indexOf(affect.tool().ID().toUpperCase())>=0))
		{
			affect.source().location().showHappens(Affect.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
			return false;
		}
		return true;
	}
}
