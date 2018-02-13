package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "Conjurer";
	}

	private final static String localizedStaticName = CMLib.lang().L("Conjurer");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int domain()
	{
		return Ability.DOMAIN_CONJURATION;
	}

	@Override
	public int opposed()
	{
		return Ability.DOMAIN_TRANSMUTATION;
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().delCharAbilityMapping(ID(),"Spell_IronGrip");

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_SummonMarker",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Spell_WatchfulHound",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_WaterCannon",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Spell_InsectPlague",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_Spellcraft",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Spell_ConjureAmmunition",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Spell_Scatter",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Spell_ChanneledMissiles",0,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Spell_WordRecall",0,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Spell_FlamingSword",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Spell_MarkerSummoning",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Spell_AcidSpray",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Spell_MarkerPortal",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Spell_TeleportObject",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Spell_ConjureNexus",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Spell_FlamingEnsnarement",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Spell_SummonArmy",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),35,"Spell_Planeshift",0,true);
	}
}
