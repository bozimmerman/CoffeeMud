package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DisFlag;
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
   Copyright 2005-2018 Bo Zimmerman

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
public class PlayerClass extends StdCharClass
{
	@Override
	public String ID()
	{
		return "PlayerClass";
	}

	private final static String localizedStaticName = CMLib.lang().L("PlayerClass");

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
	public boolean showThinQualifyList()
	{
		return true;
	}

	private static boolean abilitiesLoaded = false;

	public boolean loaded()
	{
		return abilitiesLoaded;
	}

	public void setLoaded(boolean truefalse)
	{
		abilitiesLoaded = truefalse;
	}

	public PlayerClass()
	{
		super();
		for(final int i: CharStats.CODES.BASECODES())
			maxStatAdj[i]=7;
	}

	@Override
	public int availabilityCode()
	{
		return 0;
	}

	@Override
	public String getStatQualDesc()
	{
		return "";
	}

	@Override
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(!quiet)
			mob.tell(L("This class cannot be learned."));
		return false;
	}

	private boolean isSkill(int classCode)
	{
		switch(classCode&Ability.ALL_ACODES)
		{
		case Ability.ACODE_COMMON_SKILL:
		case Ability.ACODE_DISEASE:
		case Ability.ACODE_POISON:
		case Ability.ACODE_SKILL:
		case Ability.ACODE_THIEF_SKILL:
		case Ability.ACODE_TRAP:
		case Ability.ACODE_LANGUAGE:
		case Ability.ACODE_PROPERTY:
		case Ability.ACODE_TECH:
			return true;
		case Ability.ACODE_CHANT:
		case Ability.ACODE_PRAYER:
		case Ability.ACODE_SPELL:
		case Ability.ACODE_SUPERPOWER:
			return false;
		}
		return true;
	}

	private List<String> makeRequirements(LinkedList<List<String>> prevSets, Ability A)
	{
		for(final Iterator<List<String>> i=prevSets.descendingIterator();i.hasNext();)
		{
			final List<String> prevSet=i.next();
			final List<String> reqSet=new Vector<String>();
			for(final String prevID : prevSet)
			{
				final Ability pA=CMClass.getAbility(prevID);
				if(A.classificationCode()==pA.classificationCode())
					reqSet.add(pA.ID());
			}
			if(reqSet.size()==0)
			{
				for(final String prevID : prevSet)
				{
					final Ability pA=CMClass.getAbility(prevID);
					if((A.classificationCode()&Ability.ALL_ACODES)==(pA.classificationCode()&Ability.ALL_ACODES))
						reqSet.add(pA.ID());
				}
			}
			if(reqSet.size()==0)
			{
				final boolean aIsSkill=isSkill(A.classificationCode());
				for(final String prevID : prevSet)
				{
					final Ability pA=CMClass.getAbility(prevID);
					if(aIsSkill==isSkill(pA.classificationCode()))
						reqSet.add(pA.ID());
				}
			}
			if(reqSet.size()>0)
				return reqSet;
		}
		return new Vector<String>();
	}

	@Override
	public void startCharacter(MOB mob, boolean isBorrowedClass, boolean verifyOnly)
	{
		if(!loaded())
		{
			setLoaded(true);
			final LinkedList<CharClass> charClassesOrder=new LinkedList<CharClass>();
			final HashSet<String> names=new HashSet<String>();
			for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
			{
				final CharClass C=c.nextElement();
				if(C.baseClass().equals(C.ID()) 
				&& (!C.baseClass().equalsIgnoreCase("Archon"))
				&& (!C.baseClass().equalsIgnoreCase("PlayerClass"))
				&& (!C.baseClass().equalsIgnoreCase("Qualifier"))
				&& (!C.baseClass().equalsIgnoreCase("StdCharClass")))
				{
					names.add(C.ID());
					charClassesOrder.add(C);
				}
			}
			for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
			{
				final CharClass C=c.nextElement();
				if(!names.contains(C.ID()) && names.contains(C.baseClass()))
					charClassesOrder.add(C);
			}
			for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
			{
				final CharClass C=c.nextElement();
				if(C.baseClass().equals("Commoner") && (!names.contains(C.ID())))
					charClassesOrder.add(C);
			}

			for(final CharClass C : charClassesOrder)
			{
				final LinkedList<List<String>> prevSets=new LinkedList<List<String>>();
				for(int lvl=1;lvl<CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL);lvl++)
				{
					final List<String> curSet=CMLib.ableMapper().getLevelListings(C.ID(), false, lvl);
					for(final String ID : curSet)
					{
						final String defaultParam=CMLib.ableMapper().getDefaultParm(C.ID(), true, ID);
						if(CMLib.ableMapper().getQualifyingLevel(ID(), false, ID)<0)
						{
							final Ability A=CMClass.getAbility(ID);
							if(A==null)
							{
								Log.errOut("Unknonwn class: "+ID);
								continue;
							}
							List<String> reqSet=makeRequirements(prevSets,A);
							int level=0;
							if(!this.leveless() && (!CMSecurity.isDisabled(DisFlag.LEVELS)))
								level=CMLib.ableMapper().lowestQualifyingLevel(A.ID());
							if(level<0)
								level=0;
							CMLib.ableMapper().addCharAbilityMapping(ID(), 0, ID, 0, defaultParam, false, false, reqSet, "");
						}
					}
					if(curSet.size()>0)
						prevSets.add(curSet);
				}
			}
		}
		super.startCharacter(mob, false, verifyOnly);
	}

	@Override
	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);
		if(mob.playerStats()==null)
		{
			final List<AbilityMapper.AbilityMapping> V=CMLib.ableMapper().getUpToLevelListings(ID(),
												mob.charStats().getClassLevel(ID()),
												false,
												false);
			for(final AbilityMapper.AbilityMapping able : V)
			{
				final Ability A=CMClass.getAbility(able.abilityID());
				if((A!=null)
				&&(!CMLib.ableMapper().getAllQualified(ID(),true,A.ID()))
				&&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID())))
					giveMobAbility(mob,A,CMLib.ableMapper().getDefaultProficiency(ID(),true,A.ID()),CMLib.ableMapper().getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
			}
		}
	}

}
