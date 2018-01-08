package com.planet_ink.coffee_mud.Commands;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.ExpertiseDefinition;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class Qualify  extends Skills
{
	public Qualify(){}

	private final String[] access=I(new String[]{"QUALIFY","QUAL"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}
	
	protected final static int SKILL_ANY=-1;
	protected final static int SKILL_CRAFTING_ONLY=-2;
	protected final static int SKILL_BUILDING_ONLY=-3;
	protected final static int SKILL_GATHERING_ONLY=-4;

	public StringBuffer getQualifiedAbilities(MOB viewerM,
											  MOB ableM,
											  int ofType,
											  int ofDomain,
											  String prefix,
											  boolean shortOnly)
	{
		/*
		final HashSet<Integer> V=new HashSet<Integer>();
		int mask=Ability.ALL_ACODES;
		if(ofDomain>=0)
		{
			mask=Ability.ALL_ACODES|Ability.ALL_DOMAINS;
			ofType=ofType|ofDomain;
		}
		V.add(Integer.valueOf(ofType));
		&&(ofTypes.contains(Integer.valueOf(A.classificationCode()&mask)))
		*/
		
		final int checkCode = ofType;
		final int badDomain;
		final int checkDomain;
		switch(ofDomain)
		{
		case SKILL_CRAFTING_ONLY:
			badDomain = -1;
			checkDomain = Ability.DOMAIN_CRAFTINGSKILL;
			break;
		case SKILL_GATHERING_ONLY:
			badDomain = Ability.DOMAIN_CRAFTINGSKILL;
			checkDomain = -1;
			break;
		case SKILL_BUILDING_ONLY:
			badDomain = -1;
			checkDomain = Ability.DOMAIN_BUILDINGSKILL;
			break;
		case SKILL_ANY:
		case 0:
			badDomain = -1;
			checkDomain = -1;
			break;
		default:
			badDomain = -1;
			checkDomain = ofDomain;
			break;
		}
		
		final Filterer<Ability> newFilter=new Filterer<Ability>()
		{
			@Override
			public boolean passesFilter(Ability A) 
			{
				if(((checkDomain < 0) && (A.classificationCode() & Ability.ALL_ACODES) != checkCode))
					return false;
				if((A.classificationCode() & Ability.ALL_DOMAINS) == badDomain)
					return false;
				if((checkDomain > 0) && ((A.classificationCode() & Ability.ALL_DOMAINS) != checkDomain))
					return false;
				return true;
			}
		};
		return getQualifiedAbilities(viewerM,ableM,newFilter,prefix,shortOnly);
	}

	protected Set<Integer> getQualifiedTypes(MOB ableM)
	{
		final Set<Integer> set=new TreeSet<Integer>();
		final boolean checkUnMet=ableM.charStats().getCurrentClass().showThinQualifyList();
		for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			final int level=CMLib.ableMapper().qualifyingLevel(ableM,A);
			if((CMLib.ableMapper().qualifiesByLevel(ableM,A))
			&&(level<(CMLib.ableMapper().qualifyingClassLevel(ableM,A)+1))
			&&(CMLib.ableComponents().getSpecialSkillRemainder(ableM, A).specificSkillLimit() > 0)
			&&(!checkUnMet || CMLib.ableMapper().getUnmetPreRequisites(ableM,A).size()==0))
			{
				Integer acode =Integer.valueOf(A.classificationCode() & Ability.ALL_ACODES);
				Integer dcode =Integer.valueOf(A.classificationCode() & Ability.ALL_DOMAINS);
				if(!set.contains(acode))
					set.add(acode);
				if(!set.contains(dcode))
					set.add(dcode);
			}
		}
		return set;
	}
	
	public StringBuffer getQualifiedAbilities(MOB viewerM,
											  MOB ableM,
											  Filterer<Ability> filter,
											  String prefix,
											  boolean shortOnly)
	{
		int highestLevel=0;
		final StringBuffer msg=new StringBuffer("");
		final boolean checkUnMet=ableM.charStats().getCurrentClass().showThinQualifyList();
		for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			final int level=CMLib.ableMapper().qualifyingLevel(ableM,A);
			if((CMLib.ableMapper().qualifiesByLevel(ableM,A))
			&&(!CMLib.ableMapper().getSecretSkill(ableM,A.ID()))
			&&(level>highestLevel)
			&&(level<(CMLib.ableMapper().qualifyingClassLevel(ableM,A)+1))
			&&(filter.passesFilter(A))
			&&(CMLib.ableComponents().getSpecialSkillRemainder(ableM, A).specificSkillLimit() > 0)
			&&(ableM.fetchAbility(A.ID())==null)
			&&(!checkUnMet || CMLib.ableMapper().getUnmetPreRequisites(ableM,A).size()==0))
				highestLevel=level;
		}
		int col=0;
		final int COL_LEN1=CMLib.lister().fixColWidth(3.0,viewerM);
		final int COL_LEN2=CMLib.lister().fixColWidth(19.0,viewerM);
		final int COL_LEN3=CMLib.lister().fixColWidth(12.0,viewerM);
		final int COL_LEN4=CMLib.lister().fixColWidth(13.0,viewerM);
		for(int l=0;l<=highestLevel;l++)
		{
			final StringBuffer thisLine=new StringBuffer("");
			for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((CMLib.ableMapper().qualifiesByLevel(ableM,A))
				   &&(CMLib.ableMapper().qualifyingLevel(ableM,A)==l)
				   &&(!CMLib.ableMapper().getSecretSkill(ableM,A.ID()))
				   &&(ableM.fetchAbility(A.ID())==null)
				   &&(filter.passesFilter(A))
				   &&(CMLib.ableComponents().getSpecialSkillRemainder(ableM, A).specificSkillLimit() > 0)
				   &&(!checkUnMet || CMLib.ableMapper().getUnmetPreRequisites(ableM,A).size()==0))
				{
					if((++col)>2)
					{
						thisLine.append("\n\r");
						col=1;
					}
					thisLine.append("^N[^H"+CMStrings.padRight(""+l,COL_LEN1)+"^?] "
										   +CMStrings.padRight("^<HELP^>"+A.name()+"^</HELP^>",COL_LEN2)+" "
										   +CMStrings.padRight(A.requirements(viewerM),(col==2)?COL_LEN3:COL_LEN4));
				}
			}
			if(thisLine.length()>0)
			{
				if(msg.length()==0)
					msg.append(L("\n\r^N[^HLvl^?] Name                Requires     [^HLvl^?] Name                Requires\n\r"));
				msg.append(thisLine);
			}
		}
		if(msg.length()==0)
			return msg;
		msg.insert(0,prefix);
		msg.append("\n\r");
		return msg;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		final StringBuffer msg=new StringBuffer("");
		final String qual=CMParms.combine(commands,1).toUpperCase();
		final boolean shortOnly=false;
		final boolean showAll=qual.length()==0;
		int acode=-1;
		int domain=-1;
		if(showAll||("SKILLS".startsWith(qual)))
		{
			acode=Ability.ACODE_SKILL;
			msg.append(getQualifiedAbilities(mob,mob,Ability.ACODE_SKILL,SKILL_ANY,"\n\r^HGeneral Skills:^? ",shortOnly));
		}
		if(showAll||("COMMON SKILLS").startsWith(qual))
		{
			acode=Ability.ACODE_COMMON_SKILL;
			msg.append(getQualifiedAbilities(mob,mob,Ability.ACODE_COMMON_SKILL,SKILL_ANY,"\n\r^HCommon Skills:^? ",shortOnly));
		}
		else 
		if ("CRAFTING SKILLS".startsWith(qual))
		{
			domain=Ability.DOMAIN_CRAFTINGSKILL;
			msg.append(getQualifiedAbilities(mob,mob,Ability.ACODE_COMMON_SKILL,SKILL_CRAFTING_ONLY,"\n\r^HCrafting Skills:^? ",shortOnly));
		}
		else 
		if ("BUILDING SKILLS".startsWith(qual))
		{
			domain=Ability.DOMAIN_BUILDINGSKILL;
			msg.append(getQualifiedAbilities(mob,mob,Ability.ACODE_COMMON_SKILL,SKILL_BUILDING_ONLY,"\n\r^HBuilding Skills:^? ",shortOnly));
		}
		else 
		if ("GATHERING SKILLS".startsWith(qual)
		||"NON CRAFTING SKILLS".startsWith(qual)||"NON-CRAFTING SKILLS".startsWith(qual)||"NONCRAFTING SKILLS".startsWith(qual))
		{
			domain=Ability.DOMAIN_GATHERINGSKILL;
			msg.append(getQualifiedAbilities(mob,mob,Ability.ACODE_COMMON_SKILL,SKILL_GATHERING_ONLY,"\n\r^HNon-Crafting Common Skills:^? ",shortOnly));
		}
		if(showAll||("THIEVES SKILLS".startsWith(qual))||"THIEF SKILLS".startsWith(qual))
		{
			acode=Ability.ACODE_THIEF_SKILL;
			msg.append(getQualifiedAbilities(mob,mob,Ability.ACODE_THIEF_SKILL,SKILL_ANY,"\n\r^HThief Skills:^? ",shortOnly));
		}
		if(showAll||"SPELLS".startsWith(qual)||"MAGE SPELLS".startsWith(qual))
		{
			acode=Ability.ACODE_SPELL;
			msg.append(getQualifiedAbilities(mob,mob,Ability.ACODE_SPELL,SKILL_ANY,"\n\r^HSpells:^? ",shortOnly));
		}
		if(showAll||"PRAYERS".startsWith(qual)||"CLERICAL PRAYERS".startsWith(qual))
		{
			acode=Ability.ACODE_PRAYER;
			msg.append(getQualifiedAbilities(mob,mob,Ability.ACODE_PRAYER,SKILL_ANY,"\n\r^HPrayers:^? ",shortOnly));
		}
		if(showAll||"POWERS".startsWith(qual)||"SUPER POWERS".startsWith(qual))
		{
			acode=Ability.ACODE_SUPERPOWER;
			msg.append(getQualifiedAbilities(mob,mob,Ability.ACODE_SUPERPOWER,SKILL_ANY,"\n\r^HSuper Powers:^? ",shortOnly));
		}
		if(showAll||"TECHS".startsWith(qual)||"TECH SKILLS".startsWith(qual))
		{
			acode=Ability.ACODE_TECH;
			msg.append(getQualifiedAbilities(mob,mob,Ability.ACODE_TECH,SKILL_ANY,"\n\r^HTech Skills:^? ",shortOnly));
		}
		if(showAll||"CHANTS".startsWith(qual)||"DRUID CHANTS".startsWith(qual))
		{
			acode=Ability.ACODE_CHANT;
			msg.append(getQualifiedAbilities(mob,mob,Ability.ACODE_CHANT,SKILL_ANY,"\n\r^HDruidic Chants:^? ",shortOnly));
		}
		if(showAll||"SONGS".startsWith(qual)||"BARD SONGS".startsWith(qual))
		{
			acode=Ability.ACODE_SONG;
			msg.append(getQualifiedAbilities(mob,mob,Ability.ACODE_SONG,SKILL_ANY,"\n\r^HSongs:^? ",shortOnly));
		}
		if(showAll||"LANGUAGES".startsWith(qual)||"LANGS".startsWith(qual))
		{
			acode=Ability.ACODE_LANGUAGE;
			msg.append(getQualifiedAbilities(mob,mob,Ability.ACODE_LANGUAGE,SKILL_ANY,"\n\r^HLanguages:^? ",shortOnly));
		}
		String domainName="";
		if((!showAll)&&(domain<0))
		{
			String uqual=qual.toUpperCase();
			String qual2=uqual.replace(' ','_');
			for(int i=1;i<Ability.DOMAIN_DESCS.length;i++)
			{
				if (Ability.DOMAIN_DESCS[i].startsWith(uqual)
				||Ability.DOMAIN_DESCS[i].startsWith(qual2))
				{
					domain = i << 5;
					break;
				}
				else 
				{
					int x=Ability.DOMAIN_DESCS[i].indexOf('/');
					if ((x >= 0) 
					&& (Ability.DOMAIN_DESCS[i].substring(x + 1).startsWith(uqual)
						||Ability.DOMAIN_DESCS[i].substring(x + 1).startsWith(qual2)))
					{
						domain = i << 5;
						break;
					}
				}
			}
			if(domain>0)
			{
				domainName=CMStrings.capitalizeAllFirstLettersAndLower(Ability.DOMAIN_DESCS[domain>>5].replace('_',' '));
				msg.append(getQualifiedAbilities(mob,mob,Ability.ACODE_SPELL,domain,"\n\r^H"+domainName+" abilities:^? ",shortOnly));
			}
		}
		boolean classesFound=false;
		final int COL_LEN1=CMLib.lister().fixColWidth(3.0,mob);
		final int COL_LEN2=CMLib.lister().fixColWidth(19.0,mob);
		final int COL_LEN3=CMLib.lister().fixColWidth(12.0,mob);
		final int COL_LEN4=CMLib.lister().fixColWidth(13.0,mob);
		if((mob!=null)
		&&(showAll||("CLASSES".startsWith(qual)))
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES)
		&&(!mob.baseCharStats().getMyRace().classless()))
		)
		{
			int col=0;
			final StringBuffer msg2=new StringBuffer("");
			for(final Enumeration c=CMClass.charClasses();c.hasMoreElements();)
			{
				final CharClass C=(CharClass)c.nextElement();
				final StringBuffer thisLine=new StringBuffer("");
				if((mob.charStats().getCurrentClass()!=C)
				&&(CMLib.login().canChangeToThisClass(mob, C, -1)))
				{
					if((++col)>2)
					{
						thisLine.append("\n\r");
						col=1;
					}
					thisLine.append("^N[^H"+CMStrings.padRight(""+1,COL_LEN1)+"^?] "
					+CMStrings.padRight("^<HELP^>"+C.name()+"^</HELP^>",COL_LEN2)+" "
					+CMStrings.padRight(L("1 train"),(col==2)?COL_LEN3:COL_LEN4));
				}
				if(thisLine.length()>0)
				{
					if(msg2.length()==0)
						msg2.append("\n\r^HClasses:^? \n\r^N[^HLvl^?] Name                Requires     [^HLvl^?] Name                Requires\n\r");
					classesFound=true;
					msg2.append(thisLine);
				}
			}
			msg.append(msg2.toString()+"\n\r");
		}

		if((mob!=null)
		&&(showAll
			||(qual.equalsIgnoreCase("EXPS"))
			||("EXPERTISES".startsWith(qual))))
		{
			final List<ExpertiseDefinition> V=CMLib.expertises().myListableExpertises(mob);
			for(int v=V.size()-1;v>=0;v--)
			{
				if(mob.fetchExpertise(V.get(v).ID())!=null)
					V.remove(v);
			}
			if(V.size()>0)
			{
				if(showAll)
				{
					msg.append(L("\n\r^HExpertises:^?\n\r"));
					ExpertiseLibrary.ExpertiseDefinition def=null;
					int col=0;
					final int COL_LEN=CMLib.lister().fixColWidth(25.0,mob);
					for(int e=0;e<V.size();e++)
					{
						def=V.get(e);
						if(def.name().length()>=COL_LEN)
						{
							if(col>=2)
							{
								msg.append("\n\r");
								col=0;
							}
							msg.append(CMStrings.padRightPreserve("^<HELP^>"+def.name()+"^</HELP^>",COL_LEN));
							final int spaces=(COL_LEN*2)-def.name().length();
							for(int i=0;i<spaces;i++) msg.append(" ");
							col++;
						}
						else
							msg.append(CMStrings.padRight("^<HELP^>"+def.name()+"^</HELP^>",COL_LEN));
						if((++col)>=3)
						{
							msg.append("\n\r");
							col=0;
						}
					}
					if(!msg.toString().endsWith("\n\r"))
						msg.append("\n\r");
				}
				else
				{
					final StringBuffer msg2=new StringBuffer("\n\r^HExpertises:^?\n\rName                          Requires\n\r");
					ExpertiseLibrary.ExpertiseDefinition def=null;
					String req=null;
					String prefix=null;
					final int COL_LEN=CMLib.lister().fixColWidth(30.0,mob);
					for(int v=0;v<V.size();v++)
					{
						def=V.get(v);
						req=CMLib.masking().maskDesc(def.finalRequirements(),true);
						prefix="^<HELP^>"+def.name()+"^</HELP^>";
						if(req.length()<=46)
							msg2.append(CMStrings.padRight(prefix,COL_LEN)+req+"\n\r");
						else
						while(req.length()>0)
						{
							final int x=req.indexOf(".  ");
							if(x<0)
							{
								msg2.append(CMStrings.padRight(prefix,COL_LEN)+req+"\n\r");
								req="";
								break;
							}
							msg2.append(CMStrings.padRight(prefix,COL_LEN)+req.substring(0,x+1)+"\n\r");
							prefix=" ";
							req=req.substring(x+1).trim();
						}
					}
					msg.append(msg2.toString());
				}
			}
		}

		if(mob!=null)
		{
			if(msg.length()==0)
			{
				StringBuilder list = new StringBuilder("");
				Set<Integer> qSet = this.getQualifiedTypes(mob);
				for(int i=1;i<Ability.ACODE_DESCS.length;i++)
				{
					if(qSet.contains(Integer.valueOf(i)))
					{
						list.append(Ability.ACODE_DESCS[i]).append(", ");
					}
				}
				for(int i=1;i<Ability.DOMAIN_DESCS.length;i++)
				{
					if(qSet.contains(Integer.valueOf(i << 5)))
					{
						list.append(Ability.DOMAIN_DESCS[i]).append(", ");
					}
				}
				if(list.length()>0)
					list=new StringBuilder(list.substring(0,list.length()-2));
				if(((domain >=0)&&(qSet.contains(Integer.valueOf(domain))))
				||((acode >=0)&&(qSet.contains(Integer.valueOf(acode)))))
					mob.tell(L("You don't appear to qualify for any more '@x1'. Parameters to the QUALIFY command include "+list.toString()+".",qual));
				else
				if(qual.length()>0)
					mob.tell(L("You don't appear to qualify for any '@x1'. Parameters to the QUALIFY command include "+list.toString()+".",qual));
				else
					mob.tell(L("You don't appear to qualify for anything! Parameters to the QUALIFY command include "+list.toString()+"."));
			}
			else
			if(!mob.isMonster())
			{
				final AbilityComponents.AbilityLimits limits = CMLib.ableComponents().getSpecialSkillRemainders(mob);
				if(limits.commonSkills()<0)
					limits.commonSkills(0);
				if(limits.commonSkills() < Integer.MAX_VALUE/2)
					msg.append(L("\n\r^HYou may learn ^w@x1^H more common skills.^N",""+limits.commonSkills()));
				if(limits.craftingSkills()<0)
					limits.craftingSkills(0);
				if(limits.craftingSkills() < Integer.MAX_VALUE/2)
					msg.append(L("\n\r^HYou may learn ^w@x1^H more crafting skills.^N",""+limits.craftingSkills()));
				if(limits.nonCraftingSkills()<0)
					limits.nonCraftingSkills(0);
				if(limits.nonCraftingSkills() < Integer.MAX_VALUE/2)
					msg.append(L("\n\r^HYou may learn ^w@x1^H more non-crafting common skills.^N",""+limits.nonCraftingSkills()));
				if(limits.languageSkills()<0)
					limits.languageSkills(0);
				if(limits.languageSkills() < Integer.MAX_VALUE/2)
					msg.append(L("\n\r^HYou may learn ^w@x1^H more languages.^N",""+limits.languageSkills()));
				
				mob.session().wraplessPrintln(L("^!You now qualify for the following unknown abilities:^?@x1",msg.toString()));
				mob.tell(L("\n\rUse the WILLQUALIFY command to discover what you will qualify for at higher levels."));
				mob.tell(L("\n\rUse the GAIN command with your teacher to gain new skills, spells, and expertises."));
				if(classesFound)
					mob.tell(L("\n\rUse the TRAIN command to train for a new class."));
			}
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}
}
