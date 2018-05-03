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
public class Illusionist extends SpecialistMage
{
	@Override
	public String ID()
	{
		return "Illusionist";
	}

	private final static String localizedStaticName = CMLib.lang().L("Illusionist");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int domain()
	{
		return Ability.DOMAIN_ILLUSION;
	}

	@Override
	public int opposed()
	{
		return Ability.DOMAIN_DIVINATION;
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
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_DisguiseUndead",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Spell_ColorSpray",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_DispelDivination",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Spell_MinorImage",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_Spellcraft",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Spell_LesserImage",25,true,new XVector<String>("Spell_MinorImage(75)"));
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Spell_Torture",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Spell_InvisibilitySphere",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Spell_FeignInvisibility",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Spell_DisguiseSelf",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Spell_GreaterImage",25,true,new XVector<String>("Spell_LesserImage(75)"));
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Spell_IllusoryDisease",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Spell_Simulacrum",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Spell_DivineBeauty",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Spell_Phantasm",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Spell_GreaterInvisibility",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Spell_DisguiseOther",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Spell_SuperiorImage",25,true,new XVector<String>("Spell_GreaterImage(75)"));
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Spell_AlternateReality",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Spell_EndlessRoad",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Spell_FeelTheVoid",25,true);
	}
}
