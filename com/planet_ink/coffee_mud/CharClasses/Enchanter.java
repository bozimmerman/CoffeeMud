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
public class Enchanter extends SpecialistMage
{
	public String ID(){return "Enchanter";}
	public String name(){return "Enchanter";}
	public int domain(){return Ability.DOMAIN_ENCHANTMENT;}
	public int opposed(){return Ability.DOMAIN_ABJURATION;}
	public boolean playerSelectable(){	return true;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	private static boolean myAbilitiesLoaded=false;
	
	public Enchanter()
	{
		super();
		if(!myAbilitiesLoaded)
		{
			myAbilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Spellcraft",false);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_Fatigue",25,true);
			CMAble.addCharAbilityMapping(ID(),7,"Spell_ManaBurn",25,true);
			CMAble.addCharAbilityMapping(ID(),10,"Spell_MindLight",25,"",false,true);
			CMAble.addCharAbilityMapping(ID(),14,"Spell_Alarm",25,true);
			CMAble.addCharAbilityMapping(ID(),16,"Spell_MindFog",25,true);
			CMAble.addCharAbilityMapping(ID(),18,"Spell_Enthrall",25,true);
			CMAble.addCharAbilityMapping(ID(),23,"Spell_LowerResists",25,true);
			CMAble.addCharAbilityMapping(ID(),25,"Spell_RogueLimb",25,true);
			CMAble.addCharAbilityMapping(ID(),30,"Spell_Permanency",true);
		}
	}
}
