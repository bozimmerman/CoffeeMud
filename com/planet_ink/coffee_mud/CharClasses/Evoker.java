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
public class Evoker extends SpecialistMage
{
	public String ID(){return "Evoker";}
	public String name(){return "Evoker";}
	public int domain(){return Ability.DOMAIN_EVOCATION;}
	public int opposed(){return Ability.DOMAIN_ALTERATION;}
	public int areaSelectablility(){return Area.THEME_FANTASY;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	private static boolean myAbilitiesLoaded=false;
	
	public Evoker()
	{
		super();
		if(!myAbilitiesLoaded)
		{
			myAbilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Spellcraft",false);
			CMAble.addCharAbilityMapping(ID(),9,"Spell_ContinualLight",25,true);
			CMAble.addCharAbilityMapping(ID(),11,"Spell_Shockshield",25,true);
			CMAble.addCharAbilityMapping(ID(),13,"Spell_IceLance",25,true);
			CMAble.addCharAbilityMapping(ID(),15,"Spell_Ignite",25,true);
			CMAble.addCharAbilityMapping(ID(),17,"Spell_ForkedLightning",25,true);
			CMAble.addCharAbilityMapping(ID(),19,"Spell_Levitate",25,true);
			CMAble.addCharAbilityMapping(ID(),21,"Spell_IceStorm",25,true);
			CMAble.addCharAbilityMapping(ID(),22,"Spell_Shove",25,true);
			CMAble.addCharAbilityMapping(ID(),23,"Spell_Blademouth",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),25,"Spell_LimbRack",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Spell_MassDisintegrate",25,true);
		}
	}
}
