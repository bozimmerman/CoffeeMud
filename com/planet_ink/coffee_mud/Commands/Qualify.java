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
   Copyright 2000-2014 Bo Zimmerman

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

	private final String[] access={"QUALIFY","QUAL"};
	@Override public String[] getAccessWords(){return access;}

	public StringBuffer getQualifiedAbilities(MOB viewerM,
											  MOB ableM,
											  int ofType,
											  int ofDomain,
											  String prefix,
											  boolean shortOnly)
	{
		final HashSet<Integer> V=new HashSet<Integer>();
		int mask=Ability.ALL_ACODES;
		if(ofDomain>=0)
		{
			mask=Ability.ALL_ACODES|Ability.ALL_DOMAINS;
			ofType=ofType|ofDomain;
		}
		V.add(Integer.valueOf(ofType));
		return getQualifiedAbilities(viewerM,ableM,V,mask,prefix,shortOnly);
	}

	public StringBuffer getQualifiedAbilities(MOB viewerM,
											  MOB ableM,
											  HashSet<Integer> ofTypes,
											  int mask,
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
			&&(ofTypes.contains(Integer.valueOf(A.classificationCode()&mask)))
			&&(CMLib.ableMapper().getCommonSkillRemainder(ableM, A).specificSkillLimit > 0)
			&&(ableM.fetchAbility(A.ID())==null)
			&&(!checkUnMet || CMLib.ableMapper().getUnmetPreRequisites(ableM,A).size()==0))
				highestLevel=level;
		}
		int col=0;
		final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(3.0,viewerM);
		final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(19.0,viewerM);
		final int COL_LEN3=ListingLibrary.ColFixer.fixColWidth(12.0,viewerM);
		final int COL_LEN4=ListingLibrary.ColFixer.fixColWidth(13.0,viewerM);
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
				   &&(ofTypes.contains(Integer.valueOf(A.classificationCode()&mask)))
				   &&(CMLib.ableMapper().getCommonSkillRemainder(ableM, A).specificSkillLimit > 0)
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
					msg.append("\n\r^N[^HLvl^?] Name                Requires     [^HLvl^?] Name                Requires\n\r");
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
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		final StringBuffer msg=new StringBuffer("");
		final String qual=CMParms.combine(commands,1).toUpperCase();
		final boolean shortOnly=false;
		final boolean showAll=qual.length()==0;
		if(showAll||("SKILLS".startsWith(qual)))
			msg.append(getQualifiedAbilities(mob,mob,Ability.ACODE_SKILL,-1,"\n\r^HGeneral Skills:^? ",shortOnly));
		if(showAll||("COMMON SKILLS").startsWith(qual))
			msg.append(getQualifiedAbilities(mob,mob,Ability.ACODE_COMMON_SKILL,-1,"\n\r^HCommon Skills:^? ",shortOnly));
		if(showAll||("THIEVES SKILLS".startsWith(qual))||"THIEF SKILLS".startsWith(qual))
			msg.append(getQualifiedAbilities(mob,mob,Ability.ACODE_THIEF_SKILL,-1,"\n\r^HThief Skills:^? ",shortOnly));
		if(showAll||"SPELLS".startsWith(qual)||"MAGE SPELLS".startsWith(qual))
			msg.append(getQualifiedAbilities(mob,mob,Ability.ACODE_SPELL,-1,"\n\r^HSpells:^? ",shortOnly));
		if(showAll||"PRAYERS".startsWith(qual)||"CLERICAL PRAYERS".startsWith(qual))
			msg.append(getQualifiedAbilities(mob,mob,Ability.ACODE_PRAYER,-1,"\n\r^HPrayers:^? ",shortOnly));
		if(showAll||"POWERS".startsWith(qual)||"SUPER POWERS".startsWith(qual))
			msg.append(getQualifiedAbilities(mob,mob,Ability.ACODE_SUPERPOWER,-1,"\n\r^HSuper Powers:^? ",shortOnly));
		if(showAll||"TECHS".startsWith(qual)||"TECH SKILLS".startsWith(qual))
			msg.append(getQualifiedAbilities(mob,mob,Ability.ACODE_TECH,-1,"\n\r^HTech Skills:^? ",shortOnly));
		if(showAll||"CHANTS".startsWith(qual)||"DRUID CHANTS".startsWith(qual))
			msg.append(getQualifiedAbilities(mob,mob,Ability.ACODE_CHANT,-1,"\n\r^HDruidic Chants:^? ",shortOnly));
		if(showAll||"SONGS".startsWith(qual)||"BARD SONGS".startsWith(qual))
			msg.append(getQualifiedAbilities(mob,mob,Ability.ACODE_SONG,-1,"\n\r^HSongs:^? ",shortOnly));
		if(showAll||"LANGUAGES".startsWith(qual)||"LANGS".startsWith(qual))
			msg.append(getQualifiedAbilities(mob,mob,Ability.ACODE_LANGUAGE,-1,"\n\r^HLanguages:^? ",shortOnly));
		int domain=-1;
		String domainName="";
		if(!showAll)
		{
			for(int i=1;i<Ability.DOMAIN_DESCS.length;i++)
				if(Ability.DOMAIN_DESCS[i].startsWith(qual.toUpperCase()))
				{ domain=i<<5; break;}
				else
				if((Ability.DOMAIN_DESCS[i].indexOf('/')>=0)
				&&(Ability.DOMAIN_DESCS[i].substring(Ability.DOMAIN_DESCS[i].indexOf('/')+1).startsWith(qual.toUpperCase())))
				{ domain=i<<5; break;}
			if(domain>0)
			{
				domainName=CMStrings.capitalizeAndLower(Ability.DOMAIN_DESCS[domain>>5]);
				msg.append(getQualifiedAbilities(mob,mob,Ability.ACODE_SPELL,domain,"\n\r^H"+domainName+" spells:^? ",shortOnly));
			}
		}
		boolean classesFound=false;
		final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(3.0,mob);
		final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(19.0,mob);
		final int COL_LEN3=ListingLibrary.ColFixer.fixColWidth(12.0,mob);
		final int COL_LEN4=ListingLibrary.ColFixer.fixColWidth(13.0,mob);
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
					+CMStrings.padRight("1 train",(col==2)?COL_LEN3:COL_LEN4));
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
				if(mob.fetchExpertise(V.get(v).ID)!=null)
					V.remove(v);
			if(V.size()>0)
			{
				if(showAll)
				{
					msg.append("\n\r^HExpertises:^?\n\r");
					ExpertiseLibrary.ExpertiseDefinition def=null;
					int col=0;
					final int COL_LEN=ListingLibrary.ColFixer.fixColWidth(25.0,mob);
					for(int e=0;e<V.size();e++)
					{
						def=V.get(e);
						if(def.name.length()>=COL_LEN)
						{
							if(col>=2)
							{
								msg.append("\n\r");
								col=0;
							}
							msg.append(CMStrings.padRightPreserve("^<HELP^>"+def.name+"^</HELP^>",COL_LEN));
							final int spaces=(COL_LEN*2)-def.name.length();
							for(int i=0;i<spaces;i++) msg.append(" ");
							col++;
						}
						else
							msg.append(CMStrings.padRight("^<HELP^>"+def.name+"^</HELP^>",COL_LEN));
						if((++col)>=3)
						{
							msg.append("\n\r");
							col=0;
						}
					}
					if(!msg.toString().endsWith("\n\r")) msg.append("\n\r");
				}
				else
				{
					final StringBuffer msg2=new StringBuffer("\n\r^HExpertises:^?\n\rName                          Requires\n\r");
					ExpertiseLibrary.ExpertiseDefinition def=null;
					String req=null;
					String prefix=null;
					final int COL_LEN=ListingLibrary.ColFixer.fixColWidth(30.0,mob);
					for(int v=0;v<V.size();v++)
					{
						def=V.get(v);
						req=CMLib.masking().maskDesc(def.finalRequirements(),true);
						prefix="^<HELP^>"+def.name+"^</HELP^>";
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
				if(qual.length()>0)
					mob.tell("You don't appear to qualify for any '"+qual+"'. Parameters to the QUALIFY command include SKILLS, THIEF, COMMON, SPELLS, PRAYERS, CHANTS, SONGS, EXPERTISES, or LANGUAGES.");
				else
					mob.tell("You don't appear to qualify for anything! Parameters to the QUALIFY command include SKILLS, THIEF, COMMON, SPELLS, PRAYERS, CHANTS, SONGS, EXPERTISES, or LANGUAGES.");
			}
			else
			if(!mob.isMonster())
			{
				final AbilityMapper.AbilityLimits limits = CMLib.ableMapper().getCommonSkillRemainders(mob);
				if(limits.commonSkills < Integer.MAX_VALUE/2)
					msg.append("\n\r^HYou may learn ^w"+limits.commonSkills+"^H more common skills.^N");
				if(limits.craftingSkills < Integer.MAX_VALUE/2)
					msg.append("\n\r^HYou may learn ^w"+limits.craftingSkills+"^H more crafting skills.^N");
				if(limits.nonCraftingSkills < Integer.MAX_VALUE/2)
					msg.append("\n\r^HYou may learn ^w"+limits.nonCraftingSkills+"^H more non-crafting common skills.^N");
				mob.session().wraplessPrintln("^!You now qualify for the following unknown abilities:^?"+msg.toString());
				mob.tell("\n\rUse the GAIN command with your teacher to gain new skills, spells, and expertises.");
				if(classesFound)
					mob.tell("\n\rUse the TRAIN command to train for a new class.");
			}
		}
		return false;
	}

	@Override public boolean canBeOrdered(){return true;}


}
