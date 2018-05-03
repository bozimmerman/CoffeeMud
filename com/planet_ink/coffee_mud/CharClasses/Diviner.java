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
public class Diviner extends SpecialistMage
{
	@Override
	public String ID()
	{
		return "Diviner";
	}

	private final static String localizedStaticName = CMLib.lang().L("Diviner");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int domain()
	{
		return Ability.DOMAIN_DIVINATION;
	}

	@Override
	public int opposed()
	{
		return Ability.DOMAIN_ILLUSION;
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
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Skill_Spellcraft",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_AnalyzeDweomer",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Spell_SolveMaze",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Spell_GroupStatus",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_DetectWeaknesses",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Spell_PryingEye",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Spell_Telepathy",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Spell_NaturalCommunion",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Spell_DetectTraps",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Spell_ArmsLength",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Spell_SpyingStone",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Titling",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Spell_DetectScrying",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Spell_HearThoughts",25,"",true,false,CMParms.parseSemicolons("Spell_Telepathy",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Spell_KnowOrigin",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Spell_KnowFate",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Spell_DiviningEye",25,"",true,false,CMParms.parseSemicolons("Spell_PryingEye",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Spell_SpottersOrders",25,"",true,false,CMParms.parseSemicolons("Spell_DetectWeaknesses",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Spell_Breadcrumbs",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Spell_FindDirections",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Spell_KnowPain",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Spell_KnowBliss",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Spell_DeathWarning",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Spell_DetectAmbush",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Spell_TrueSight",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Spell_FutureDeath",25,true);
	}
}
