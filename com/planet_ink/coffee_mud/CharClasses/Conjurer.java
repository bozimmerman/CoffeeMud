package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Conjurer extends SpecialistMage
{
	public String ID(){return "Conjurer";}
	public String name(){return "Conjurer";}
	public int domain(){return Ability.DOMAIN_CONJURATION;}
	public int opposed(){return Ability.DOMAIN_TRANSMUTATION;}
	public int availabilityCode(){return Area.THEME_FANTASY;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	private static boolean myAbilitiesLoaded=false;
		
	public Conjurer()
	{
		super();
		if(!myAbilitiesLoaded)
		{
			myAbilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Spellcraft",false);
			CMAble.addCharAbilityMapping(ID(),8,"Spell_SummonMarker",25,true);
			CMAble.addCharAbilityMapping(ID(),11,"Spell_Scatter",25,true);
			CMAble.addCharAbilityMapping(ID(),13,"Spell_WaterCannon",25,true);
			CMAble.addCharAbilityMapping(ID(),18,"Spell_MarkerSummoning",25,true);
			CMAble.addCharAbilityMapping(ID(),20,"Spell_MarkerPortal",25,true);
			CMAble.addCharAbilityMapping(ID(),23,"Spell_ConjureNexus",25,true);
			CMAble.addCharAbilityMapping(ID(),25,"Spell_FlamingEnsnarement",25,true);
			CMAble.addCharAbilityMapping(ID(),30,"Spell_SummonArmy",25,true);
		}
	}
}
