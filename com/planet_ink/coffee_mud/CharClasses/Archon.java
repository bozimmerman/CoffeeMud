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

import java.util.*;

/*
   Copyright 2001-2018 Bo Zimmerman

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
public class Archon extends StdCharClass
{
	@Override
	public String ID()
	{
		return "Archon";
	}

	private final static String localizedStaticName = CMLib.lang().L("Archon");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public String baseClass()
	{
		return ID();
	}

	@Override
	public boolean leveless()
	{
		return true;
	}

	public Archon()
	{
		super();
		for(final int i : CharStats.CODES.BASECODES())
			maxStatAdj[i]=7;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"AnimalTaming",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"AnimalTrading",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"AnimalTraining",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Domesticating",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"InstrumentMaking",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"PlantLore",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Scrapping",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"AstroEngineering",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Welding",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Irrigation",100,"",true,true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Common",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Resistance",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Archon_Multiwatch",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Archon_MatrixPossess",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Archon_Wrath",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Archon_Hush",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Archon_Freeze",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Archon_Record",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Archon_Infect",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Archon_Stinkify",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Archon_Banish",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Archon_Accuse",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Archon_Metacraft",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Archon_Injure",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Amputation",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_AlterTime",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_MoveSky",true);
	}

	@Override
	public int availabilityCode()
	{
		return 0;
	}

	@Override
	public String getStatQualDesc()
	{
		return "Must be granted by another Archon.";
	}

	@Override
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(!quiet)
			mob.tell(L("This class cannot be learned."));
		return false;
	}

	public static final String[] ARCHON_IMMUNITIES=
		{
		"Spell_Scry",
		"Thief_Listen",
		"Spell_Claireaudience",
		"Spell_Clairevoyance",
		"Spell_Enthrall",
		"Spell_Charm",
		"Skill_Befriend",
		"Chant_CharmAnimal",
		"Chant_StoneFriend",
		"Thief_Steal",
		"Thief_PlantItem"
		};

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(msg.target()==myHost)
		{
			if((msg.tool() instanceof Ability)
			&&((CMParms.indexOf(ARCHON_IMMUNITIES,msg.tool().ID())>=0)
				||((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_DISEASE)
				||((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_POISON)))
			{
				//((MOB)msg.target()).tell(L("You are immune to @x1.",msg.tool().name()));
				if(msg.source()!=msg.target())
				{
					msg.source().tell(msg.source(),msg.target(),msg.tool(),L("<T-NAME> is immune to <O-NAME>."));
				}
				return false;
			}
			else
			if((msg.targetMinor()==CMMsg.TYP_ORDER)
			&&(!CMSecurity.isASysOp(msg.source())))
			{
				msg.source().tell(msg.source(),msg.target(),msg.tool(),L("You can't order <T-NAME> to do anything."));
				return false;
			}
		}
		
		return super.okMessage(myHost, msg);
	}

	@Override
	public List<Item> outfit(MOB myChar)
	{
		if(outfitChoices==null)
		{
			final Weapon w=CMClass.getWeapon("ArchonStaff");
			if(w == null)
				return new Vector<Item>();
			outfitChoices=new Vector<Item>();
			outfitChoices.add(w);
		}
		return outfitChoices;
	}

	@Override
	public void startCharacter(MOB mob, boolean isBorrowedClass, boolean verifyOnly)

	{
		// archons ALWAYS use borrowed abilities
		super.startCharacter(mob, true, verifyOnly);
		if(verifyOnly)
			grantAbilities(mob,true);
	}

	@Override
	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		final boolean allowed=CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.ALLSKILLS);
		if((!allowed)&&(mob.playerStats()!=null)&&(!mob.playerStats().getSecurityFlags().contains(CMSecurity.SecFlag.ALLSKILLS,false)))
		{
			final List<String> oldSet=CMParms.parseSemicolons(mob.playerStats().getSetSecurityFlags(null),true);
			if(!oldSet.contains(CMSecurity.SecFlag.ALLSKILLS.name()))
			{
				oldSet.add(CMSecurity.SecFlag.ALLSKILLS.name());
				mob.playerStats().getSetSecurityFlags(CMParms.toSemicolonListString(oldSet));
			}
		}
		super.grantAbilities(mob,isBorrowedClass);
		if((!allowed)&&(mob.playerStats()!=null)&&(mob.playerStats().getSecurityFlags().contains(CMSecurity.SecFlag.ALLSKILLS,false)))
		{
			final List<String> oldSet=CMParms.parseSemicolons(mob.playerStats().getSetSecurityFlags(null),true);
			if(oldSet.contains(CMSecurity.SecFlag.ALLSKILLS.name()))
			{
				oldSet.remove(CMSecurity.SecFlag.ALLSKILLS.name());
				mob.playerStats().getSetSecurityFlags(CMParms.toSemicolonListString(oldSet));
			}
		}
	}
}
