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
public class Evoker extends SpecialistMage
{
	@Override
	public String ID()
	{
		return "Evoker";
	}

	private final static String localizedStaticName = CMLib.lang().L("Evoker");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int domain()
	{
		return Ability.DOMAIN_EVOCATION;
	}

	@Override
	public int opposed()
	{
		return Ability.DOMAIN_ALTERATION;
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
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_ProduceFlame",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Spell_HelpingHand",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_PurgeInvisibility",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Spell_ForcefulHand",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_Spellcraft",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Spell_ContinualLight",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Spell_Shockshield",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Spell_IceLance",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Spell_KineticPulse",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Spell_Ignite",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Spell_ForkedLightning",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Spell_Levitate",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Spell_Pocket",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Spell_IceStorm",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Spell_Shove",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Spell_Blademouth",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Spell_PortalOther",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Spell_LimbRack",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),27,"Spell_Lighthouse",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Spell_MassDisintegrate",25,true);
	}
}
