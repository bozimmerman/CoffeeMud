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
public class Transmuter extends SpecialistMage
{
	public String ID(){return "Transmuter";}
	public String name(){return "Transmuter";}
	public int domain(){return Ability.DOMAIN_TRANSMUTATION;}
	public int opposed(){return Ability.DOMAIN_CONJURATION;}
	public int areaSelectablility(){return Area.THEME_FANTASY;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	private static boolean myAbilitiesLoaded=false;
	
	public Transmuter()
	{
		super();
		if(!myAbilitiesLoaded)
		{
			myAbilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Spellcraft",false);
			CMAble.addCharAbilityMapping(ID(),3,"Spell_CauseStink",25,true);
			CMAble.addCharAbilityMapping(ID(),8,"Spell_MassWaterbreath",25,true);
			CMAble.addCharAbilityMapping(ID(),10,"Spell_Misstep",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),14,"Spell_Sonar",25,true);
			CMAble.addCharAbilityMapping(ID(),17,"Spell_Grow",25,true);
			CMAble.addCharAbilityMapping(ID(),21,"Spell_Toadstool",25,true);
			CMAble.addCharAbilityMapping(ID(),22,"Spell_AddLimb",25,true);
			CMAble.addCharAbilityMapping(ID(),23,"Spell_Transformation",25,true);
			CMAble.addCharAbilityMapping(ID(),24,"Spell_BigMouth",25,true);
			CMAble.addCharAbilityMapping(ID(),25,"Spell_MassFly",25,true);
			CMAble.addCharAbilityMapping(ID(),30,"Spell_Clone",25,true);
		}
	}
}
