package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;

public class Prop_NoTeleport extends Property
{
	public String ID() { return "Prop_NoTeleport"; }
	public String name(){ return "Teleport INTO Spell Neutralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS;}
	public Environmental newInstance(){	return new Prop_NoTeleport();}
	
	public final static String spellList=
		(" Spell_Gate Spell_Teleport Spell_Portal Chant_PlantPass "
		 +" Spell_Cogniportive").toUpperCase();
	
	public String text(){ return spellList; }

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if((affect.tool()!=null)
		&&(affect.tool() instanceof Ability)
		&&(affect.source()!=null)
		&&(affect.source().location()!=null)
		&&((affect.source().location()!=affected)
		   &&(affect.source().location().getArea()!=affected))
		&&(text().toUpperCase().indexOf(affect.tool().ID().toUpperCase())>=0))
		{
			affect.source().location().showHappens(Affect.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
			return false;
		}
		return true;
	}
}
