package com.planet_ink.coffee_mud.Abilities.Properties;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.SecretFlag;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2025 Bo Zimmerman

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
public class Prop_Trainer extends Prop_StatTrainer
{
	@Override
	public String ID()
	{
		return "Prop_Trainer";
	}

	@Override
	public String name()
	{
		return "THE Training MOB";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public String accountForYourself()
	{
		return "Trainer";
	}

	private boolean built=false;

	private void addCharClassIfNotFound(final MOB mob, final CharClass C)
	{
		boolean found=false;
		for(int n=0;n<mob.baseCharStats().numClasses();n++)
		{
			if(mob.baseCharStats().getMyClass(n).ID().equals(C.ID()))
			{
				found = true;
				break;
			}
		}
		if((!found)&&(C.availabilityCode()!=0))
		{
			mob.baseCharStats().setCurrentClass(C);
			mob.baseCharStats().setClassLevel(C,0);
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((!built)&&(affected instanceof MOB))
		{
			built=true;
			CharClass C=null;
			final MOB mob=(MOB)affected;
			CharClass currC=mob.charStats().getCurrentClass();
			final ArrayList<CharClass> allowedClasses=new ArrayList<CharClass>(1);
			final ArrayList<ExpertiseLibrary.ExpertiseDefinition> allowedExpertises=new ArrayList<ExpertiseLibrary.ExpertiseDefinition>(1);
			final Vector<String> V=CMParms.parse(text());
			boolean alsoSkills=false;
			boolean noExper=false;
			String s=null;
			for(int v=0;v<V.size();v++)
			{
				s=V.elementAt(v);
				if(s.equalsIgnoreCase("all"))
					continue;
				C=CMClass.getCharClass(s);
				if(C!=null)
				{
					if((v>0)&&(V.elementAt(v-1).equalsIgnoreCase("ALL")))
					{
						final String baseClass=C.baseClass();
						for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
						{
							C=c.nextElement();
							if((C.baseClass().equalsIgnoreCase(baseClass))
							&&(!allowedClasses.contains(C)))
								allowedClasses.add(C);
						}
					}
					else
						allowedClasses.add(C);
				}
				else
				{
					final ExpertiseLibrary.ExpertiseDefinition def=CMLib.expertises().getDefinition(s);
					if(def!=null)
						allowedExpertises.add(def);
					else
					if(s.equalsIgnoreCase("skill")||s.equalsIgnoreCase("skills"))
						alsoSkills=true;
					else
					if(s.equalsIgnoreCase("noexper")||s.equalsIgnoreCase("noexpertise"))
						noExper=true;
				}
			}
			if(allowedClasses.size()==0)
			{
				for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
					allowedClasses.add(c.nextElement());
			}
			if((allowedExpertises.size()==0)&&(!noExper))
			{
				for(final Enumeration<ExpertiseLibrary.ExpertiseDefinition> e=CMLib.expertises().definitions();e.hasMoreElements();)
					allowedExpertises.add(e.nextElement());
			}
			for(int c=0;c<allowedClasses.size();c++)
			{
				C=allowedClasses.get(c);
				addCharClassIfNotFound(mob,C);
			}
			for(int e=0;e<allowedExpertises.size();e++)
				mob.addExpertise(allowedExpertises.get(e).ID());
			if(alsoSkills && (allowedClasses.size()>0))
			{
				mob.baseCharStats().setAllClassInfo("StdCharClass", "0");
				for(int c=0;c<allowedClasses.size();c++)
				{
					currC=allowedClasses.get(c);
					mob.baseCharStats().setCurrentClass(currC);
					mob.charStats().setCurrentClass(currC);
					mob.baseCharStats().setClassLevel(currC,mob.basePhyStats().level()/allowedClasses.size());
					mob.charStats().setClassLevel(currC,mob.basePhyStats().level()/allowedClasses.size());
					currC.startCharacter(mob, true, false);
					final Map<String,Ability> myAbles=new HashMap<String,Ability>();
					for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
						if(A!=null)
							myAbles.put(A.ID(),A);
					}
					final String className=mob.baseCharStats().getCurrentClass().ID();
					for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
					{
						Ability A=a.nextElement();
						if((A!=null)
						&&(CMLib.ableMapper().qualifiesByLevel(mob,A))
						&&(CMLib.ableMapper().availableToTheme(A.ID(),Area.THEME_FANTASY,true))
						&&(!myAbles.containsKey(A.ID())))
						{
							final SecretFlag secret = CMLib.ableMapper().getSecretSkill(className,true,A.ID());
							if((secret!=SecretFlag.SECRET)
							&&((secret!=SecretFlag.MASKED)
								||(CMLib.masking().maskCheck(CMLib.ableMapper().getExtraMask(className, true, A.ID()), mob, true))))
							{
								A=(Ability)A.copyOf();
								A.setSavable(false);
								A.setProficiency(100);
								A.setProficiency(CMLib.ableMapper().getMaxProficiency(mob,true,A.ID()));
								myAbles.put(A.ID(),A);
								mob.addAbility(A);
							}
						}
					}
				}
			}
			else
			{
				mob.baseCharStats().setCurrentClass(currC);
				mob.charStats().setCurrentClass(currC);
			}
			mob.recoverCharStats();
			mob.recoverPhyStats();
			mob.recoverMaxState();
		}
		return super.tick(ticking,tickID);
	}
}
