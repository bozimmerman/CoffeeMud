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
public class Transmuter extends SpecialistMage
{
	@Override
	public String ID()
	{
		return "Transmuter";
	}

	private final static String localizedStaticName = CMLib.lang().L("Transmuter");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int domain()
	{
		return Ability.DOMAIN_TRANSMUTATION;
	}

	@Override
	public int opposed()
	{
		return Ability.DOMAIN_CONJURATION;
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
		CMLib.ableMapper().delCharAbilityMapping(ID(),"Spell_MagicMissile");

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_DeadenSmell",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Spell_CauseStink",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_ShrinkMouth",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Spell_LightSensitivity",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_Spellcraft",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Spell_MassWaterbreath",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Spell_Misstep",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Spell_Sonar",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Spell_Grow",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Spell_LedFoot",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Spell_Toadstool",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Spell_AddLimb",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Spell_PolymorphSelf",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Spell_BigMouth",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Spell_Transformation",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Spell_Clone",25,true);
	}
}
