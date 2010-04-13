package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Qualify  extends Skills
{
	public Qualify(){}

	private String[] access={"QUALIFY","QUAL"};
	public String[] getAccessWords(){return access;}

	public StringBuffer getQualifiedAbilities(MOB able, 
                                              int ofType, 
                                              int ofDomain, 
                                              String prefix,
                                              boolean shortOnly)
	{
		Vector V=new Vector();
		int mask=Ability.ALL_ACODES;
		if(ofDomain>=0)
		{
			mask=Ability.ALL_ACODES|Ability.ALL_DOMAINS;
			ofType=ofType|ofDomain;
		}
		V.addElement(Integer.valueOf(ofType));
		return getQualifiedAbilities(able,V,mask,prefix,shortOnly);
	}

	public StringBuffer getQualifiedAbilities(MOB able,
											  Vector ofTypes,
											  int mask,
											  String prefix,
                                              boolean shortOnly)
	{
		int highestLevel=0;
		StringBuffer msg=new StringBuffer("");
		for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
		{
			Ability A=(Ability)a.nextElement();
			int level=CMLib.ableMapper().qualifyingLevel(able,A);
			if((CMLib.ableMapper().qualifiesByLevel(able,A))
			&&(!CMLib.ableMapper().getSecretSkill(able,A.ID()))
			&&(level>highestLevel)
			&&(level<(CMLib.ableMapper().qualifyingClassLevel(able,A)+1))
			&&(able.fetchAbility(A.ID())==null)
			&&(ofTypes.contains(Integer.valueOf(A.classificationCode()&mask))))
				highestLevel=level;
		}
		int col=0;
		for(int l=0;l<=highestLevel;l++)
		{
			StringBuffer thisLine=new StringBuffer("");
			for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
			{
				Ability A=(Ability)a.nextElement();
				if((CMLib.ableMapper().qualifiesByLevel(able,A))
				   &&(CMLib.ableMapper().qualifyingLevel(able,A)==l)
				   &&(!CMLib.ableMapper().getSecretSkill(able,A.ID()))
				   &&(able.fetchAbility(A.ID())==null)
				   &&(ofTypes.contains(Integer.valueOf(A.classificationCode()&mask))))
				{
					if((++col)>2)
					{
						thisLine.append("\n\r");
						col=1;
					}
					thisLine.append("^N[^H"+CMStrings.padRight(""+l,3)+"^?] "
					+CMStrings.padRight("^<HELP^>"+A.name()+"^</HELP^>",19)+" "
					+CMStrings.padRight(A.requirements(),(col==2)?12:13));
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

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		StringBuffer msg=new StringBuffer("");
		String qual=CMParms.combine(commands,1).toUpperCase();
        boolean shortOnly=false;
        boolean showAll=qual.length()==0;
		if(showAll||("SKILLS".startsWith(qual)))
			msg.append(getQualifiedAbilities(mob,Ability.ACODE_SKILL,-1,"\n\r^HGeneral Skills:^? ",shortOnly));
		if(showAll||("COMMON SKILLS").startsWith(qual))
			msg.append(getQualifiedAbilities(mob,Ability.ACODE_COMMON_SKILL,-1,"\n\r^HCommon Skills:^? ",shortOnly));
		if(showAll||("THIEVES SKILLS".startsWith(qual))||"THIEF SKILLS".startsWith(qual))
			msg.append(getQualifiedAbilities(mob,Ability.ACODE_THIEF_SKILL,-1,"\n\r^HThief Skills:^? ",shortOnly));
		if(showAll||"SPELLS".startsWith(qual)||"MAGE SPELLS".startsWith(qual))
			msg.append(getQualifiedAbilities(mob,Ability.ACODE_SPELL,-1,"\n\r^HSpells:^? ",shortOnly));
		if(showAll||"PRAYERS".startsWith(qual)||"CLERICAL PRAYERS".startsWith(qual))
			msg.append(getQualifiedAbilities(mob,Ability.ACODE_PRAYER,-1,"\n\r^HPrayers:^? ",shortOnly));
		if(showAll||"POWERS".startsWith(qual)||"SUPER POWERS".startsWith(qual))
			msg.append(getQualifiedAbilities(mob,Ability.ACODE_SUPERPOWER,-1,"\n\r^HSuper Powers:^? ",shortOnly));
		if(showAll||"CHANTS".startsWith(qual)||"DRUID CHANTS".startsWith(qual))
			msg.append(getQualifiedAbilities(mob,Ability.ACODE_CHANT,-1,"\n\r^HDruidic Chants:^? ",shortOnly));
		if(showAll||"SONGS".startsWith(qual)||"BARD SONGS".startsWith(qual))
			msg.append(getQualifiedAbilities(mob,Ability.ACODE_SONG,-1,"\n\r^HSongs:^? ",shortOnly));
		if(showAll||"LANGUAGES".startsWith(qual)||"LANGS".startsWith(qual))
			msg.append(getQualifiedAbilities(mob,Ability.ACODE_LANGUAGE,-1,"\n\r^HLanguages:^? ",shortOnly));
		int domain=-1;
		String domainName="";
		if(!showAll)
		{
			for(int i=1;i<Ability.DOMAIN_DESCS.length;i++)
				if(Ability.DOMAIN_DESCS[i].startsWith(qual.toUpperCase()))
				{ domain=i<<5; break;}
				else
				if((Ability.DOMAIN_DESCS[i].indexOf("/")>=0)
				&&(Ability.DOMAIN_DESCS[i].substring(Ability.DOMAIN_DESCS[i].indexOf("/")+1).startsWith(qual.toUpperCase())))
				{ domain=i<<5; break;}
			if(domain>0)
			{
				domainName=CMStrings.capitalizeAndLower(Ability.DOMAIN_DESCS[domain>>5]);
				msg.append(getQualifiedAbilities(mob,Ability.ACODE_SPELL,domain,"\n\r^H"+domainName+" spells:^? ",shortOnly));
			}
		}
		boolean classesFound=false;
		if((mob!=null)
		&&(showAll||("CLASSES".startsWith(qual))))
		{
			int col=0;
			StringBuffer msg2=new StringBuffer("");
			for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
			{
				CharClass C=(CharClass)c.nextElement();
				StringBuffer thisLine=new StringBuffer("");
				if((mob.charStats().getCurrentClass()!=C)
				&&(CMLib.login().canChangeToThisClass(mob, C, -1)))
				{
					if((++col)>2)
					{
						thisLine.append("\n\r");
						col=1;
					}
					thisLine.append("^N[^H"+CMStrings.padRight(""+1,3)+"^?] "
					+CMStrings.padRight("^<HELP^>"+C.name()+"^</HELP^>",19)+" "
					+CMStrings.padRight("1 train",(col==2)?12:13));
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
			Vector V=CMLib.expertises().myListableExpertises(mob);
			for(int v=V.size()-1;v>=0;v--)
				if(mob.fetchExpertise(((ExpertiseLibrary.ExpertiseDefinition)V.elementAt(v)).ID)!=null)
					V.removeElementAt(v);
			if(V.size()>0)
			{
				if(showAll)
				{
					msg.append("\n\r^HExpertises:^?\n\r");
					ExpertiseLibrary.ExpertiseDefinition def=null;
					int col=0;
			        int colWidth=25;
					for(int e=0;e<V.size();e++)
					{
						def=(ExpertiseLibrary.ExpertiseDefinition)V.elementAt(e);
			            if(def.name.length()>=colWidth)
			            {
			            	if(col>=2)
			            	{
				                msg.append("\n\r");
				                col=0;
			            	}
			    			msg.append(CMStrings.padRightPreserve("^<HELP^>"+def.name+"^</HELP^>",colWidth));
			                int spaces=(colWidth*2)-def.name.length();
			                for(int i=0;i<spaces;i++) msg.append(" ");
			                col++;
			            }
			            else
			                msg.append(CMStrings.padRight("^<HELP^>"+def.name+"^</HELP^>",colWidth));
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
					StringBuffer msg2=new StringBuffer("\n\r^HExpertises:^?\n\rName                          Requires\n\r");
					ExpertiseLibrary.ExpertiseDefinition def=null;
	                String req=null;
	                String prefix=null;
					for(int v=0;v<V.size();v++)
					{
						def=(ExpertiseLibrary.ExpertiseDefinition)V.elementAt(v);
	                    req=CMLib.masking().maskDesc(def.finalRequirements(),true);
	                    prefix="^<HELP^>"+def.name+"^</HELP^>";
	                    if(req.length()<=46)
	                        msg2.append(CMStrings.padRight(prefix,30)+req+"\n\r");
	                    else
	                    while(req.length()>0)
	                    {
	                        int x=req.indexOf(".  ");
	                        if(x<0)
	                        {
	                            msg2.append(CMStrings.padRight(prefix,30)+req+"\n\r");
	                            req="";
	                            break;
	                        }
	                        msg2.append(CMStrings.padRight(prefix,30)+req.substring(0,x+1)+"\n\r");
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
				mob.session().wraplessPrintln("^!You now qualify for the following unknown abilities:^?"+msg.toString());
				mob.tell("\n\rUse the GAIN command with your teacher to gain new skills, spells, and expertises.");
				if(classesFound) 
					mob.tell("\n\rUse the TRAIN command to train for a new class.");
			}
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
