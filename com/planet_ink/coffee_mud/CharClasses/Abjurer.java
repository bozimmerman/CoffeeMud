package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Abjurer extends SpecialistMage
{
	public String ID(){return "Abjurer";}
	public String name(){return "Abjurer";}
	public int domain(){return Ability.DOMAIN_ABJURATION;}
	public int opposed(){return Ability.DOMAIN_ENCHANTMENT;}
	public int availabilityCode(){return Area.THEME_FANTASY;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	private static boolean myAbilitiesLoaded=false;
	
	public Abjurer()
	{
		super();
		if(!myAbilitiesLoaded)
		{
			myAbilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Spellcraft",false);
			CMAble.addCharAbilityMapping(ID(),9,"Spell_SongShield",25,true);
			CMAble.addCharAbilityMapping(ID(),11,"Spell_ResistBludgeoning",25,true);
			CMAble.addCharAbilityMapping(ID(),14,"Spell_Counterspell",25,true);
			CMAble.addCharAbilityMapping(ID(),15,"Spell_ResistPiercing",25,true);
			CMAble.addCharAbilityMapping(ID(),17,"Spell_ChantShield",25,true);
			CMAble.addCharAbilityMapping(ID(),19,"Spell_ResistSlashing",25,true);
			CMAble.addCharAbilityMapping(ID(),20,"Spell_PrayerShield",25,true);
			CMAble.addCharAbilityMapping(ID(),22,"Spell_ResistIndignities",25,true);
			CMAble.addCharAbilityMapping(ID(),30,"Spell_AchillesArmor",25,true);
		}
	}
}
