package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Illusionist extends SpecialistMage
{
	public String ID(){return "Illusionist";}
	public String name(){return "Illusionist";}
	public int domain(){return Ability.DOMAIN_ILLUSION;}
	public int opposed(){return Ability.DOMAIN_DIVINATION;}
	public boolean playerSelectable(){	return true;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	private static boolean myAbilitiesLoaded=false;
	
	public Illusionist()
	{
		super();
		if(!myAbilitiesLoaded)
		{
			myAbilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Spellcraft",false);
			CMAble.addCharAbilityMapping(ID(),18,"Spell_IllusoryDisease",25,true);
			CMAble.addCharAbilityMapping(ID(),21,"Spell_Phantasm",25,true);
			CMAble.addCharAbilityMapping(ID(),22,"Spell_GreaterInvisibility",25,true);
			CMAble.addCharAbilityMapping(ID(),23,"Spell_DivineBeauty",25,true);
			CMAble.addCharAbilityMapping(ID(),24,"Spell_AlternateReality",25,true);
			CMAble.addCharAbilityMapping(ID(),25,"Spell_EndlessRoad",25,true);
			CMAble.addCharAbilityMapping(ID(),30,"Spell_FeelTheVoid",25,true);
		}
	}
}
