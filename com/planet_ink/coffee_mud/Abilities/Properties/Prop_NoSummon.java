package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;

public class Prop_NoSummon extends Property
{
	public String ID() { return "Prop_NoSummon"; }
	public String name(){ return "Summon Spell Neutralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Prop_NoSummon();}

	public final static String spellList=
		(" Spell_Summon Spell_SummonEnemy Spell_SummonSteed Spell_SummonFlyer "
		 +"Spell_SummonMonster Spell_DemonGate Chant_SummonMount "
		 +"Chant_SummonAnimal Spell_PhantomHound ").toUpperCase();

	public String text(){ return spellList; }

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
		&&(text().toUpperCase().indexOf(affect.tool().ID().toUpperCase())>=0))
		{
			affect.source().location().showHappens(Affect.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
			return false;
		}
		return true;
	}
}
