package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Diviner extends SpecialistMage
{
	public String ID(){return "Diviner";}
	public String name(){return "Diviner";}
	public int domain(){return Ability.DOMAIN_DIVINATION;}
	public int opposed(){return Ability.DOMAIN_ILLUSION;}
	public boolean playerSelectable(){	return true;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	private static boolean myAbilitiesLoaded=false;
		
	public Diviner()
	{
		super();
		if(!myAbilitiesLoaded)
		{
			myAbilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Spellcraft",false);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_AnalyzeDweomer",25,true);
			CMAble.addCharAbilityMapping(ID(),15,"Spell_KnowOrigin",25,true);
			CMAble.addCharAbilityMapping(ID(),17,"Spell_DetectScrying",25,true);
			CMAble.addCharAbilityMapping(ID(),19,"Spell_Breadcrumbs",25,true);
			CMAble.addCharAbilityMapping(ID(),21,"Spell_KnowPain",25,true);
			CMAble.addCharAbilityMapping(ID(),22,"Spell_KnowBliss",25,true);
			CMAble.addCharAbilityMapping(ID(),24,"Spell_DetectAmbush",25,true);
			CMAble.addCharAbilityMapping(ID(),25,"Spell_TrueSight",25,true);
			CMAble.addCharAbilityMapping(ID(),30,"Spell_FutureDeath",25,true);
		}
	}
}
