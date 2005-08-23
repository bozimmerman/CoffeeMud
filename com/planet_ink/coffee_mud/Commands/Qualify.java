package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Qualify extends BaseAbleLister
{
	public Qualify(){}

	private String[] access={"QUALIFY","QUAL"};
	public String[] getAccessWords(){return access;}

	public StringBuffer getQualifiedAbilities(MOB able, int ofType, int ofDomain, String prefix)
	{
		Vector V=new Vector();
		int mask=Ability.ALL_CODES;
		if(ofDomain>=0)
		{
			mask=Ability.ALL_CODES|Ability.ALL_DOMAINS;
			ofType=ofType|ofDomain;
		}
		V.addElement(new Integer(ofType));
		return getQualifiedAbilities(able,V,mask,prefix);
	}

	public StringBuffer getQualifiedAbilities(MOB able,
											  Vector ofTypes,
											  int mask,
											  String prefix)
	{
		int highestLevel=0;
		StringBuffer msg=new StringBuffer("");
		for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
		{
			Ability A=(Ability)a.nextElement();
			int level=CMAble.qualifyingLevel(able,A);
			if((CMAble.qualifiesByLevel(able,A))
			&&(!CMAble.getSecretSkill(able,A.ID()))
			&&(level>highestLevel)
			&&(level<(CMAble.qualifyingClassLevel(able,A)+1))
			&&(able.fetchAbility(A.ID())==null)
			&&(ofTypes.contains(new Integer(A.classificationCode()&mask))))
				highestLevel=level;
		}
		int col=0;
		for(int l=0;l<=highestLevel;l++)
		{
			StringBuffer thisLine=new StringBuffer("");
			for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
			{
				Ability A=(Ability)a.nextElement();
				if((CMAble.qualifiesByLevel(able,A))
				   &&(CMAble.qualifyingLevel(able,A)==l)
				   &&(!CMAble.getSecretSkill(able,A.ID()))
				   &&(able.fetchAbility(A.ID())==null)
				   &&(ofTypes.contains(new Integer(A.classificationCode()&mask))))
				{
					if((++col)>2)
					{
						thisLine.append("\n\r");
						col=1;
					}
					thisLine.append("^N[^H"+Util.padRight(""+l,3)+"^?] "
					+Util.padRight("^<HELP^>"+A.name()+"^</HELP^>",19)+" "
					+Util.padRight(A.requirements(),(col==2)?12:13));
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

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		StringBuffer msg=new StringBuffer("");
		String qual=Util.combine(commands,1);
		if((qual.length()==0)||(qual.equalsIgnoreCase("SKILLS"))||(qual.equalsIgnoreCase("SKILL")))
			msg.append(getQualifiedAbilities(mob,Ability.SKILL,-1,"\n\r^HGeneral Skills:^? "));
		if((qual.length()==0)||(qual.equalsIgnoreCase("COMMON SKILLS"))||(qual.equalsIgnoreCase("COMMON")))
			msg.append(getQualifiedAbilities(mob,Ability.COMMON_SKILL,-1,"\n\r^HCommon Skills:^? "));
		if((qual.length()==0)||(qual.equalsIgnoreCase("THIEVES"))||(qual.equalsIgnoreCase("THIEF"))||(qual.equalsIgnoreCase("THIEF SKILLS")))
			msg.append(getQualifiedAbilities(mob,Ability.THIEF_SKILL,-1,"\n\r^HThief Skills:^? "));
		if((qual.length()==0)||(qual.equalsIgnoreCase("SPELLS"))||(qual.equalsIgnoreCase("SPELL"))||(qual.equalsIgnoreCase("MAGE")))
			msg.append(getQualifiedAbilities(mob,Ability.SPELL,-1,"\n\r^HSpells:^? "));
		if((qual.length()==0)||(qual.equalsIgnoreCase("PRAYERS"))||(qual.equalsIgnoreCase("PRAYER"))||(qual.equalsIgnoreCase("CLERIC")))
			msg.append(getQualifiedAbilities(mob,Ability.PRAYER,-1,"\n\r^HPrayers:^? "));
		if((qual.length()==0)||(qual.equalsIgnoreCase("POWERS"))||(qual.equalsIgnoreCase("POWER"))||(qual.equalsIgnoreCase("SUPER POWERS"))||(qual.equalsIgnoreCase("SUPER POWER")))
			msg.append(getQualifiedAbilities(mob,Ability.SUPERPOWER,-1,"\n\r^HSuper Powers:^? "));
		if((qual.length()==0)||(qual.equalsIgnoreCase("EVIL DEEDS"))||(qual.equalsIgnoreCase("EVIL DEED")))
			msg.append(getQualifiedAbilities(mob,Ability.SUPERPOWER,-1,"\n\r^Evil Deeds:^? "));
		if((qual.length()==0)||(qual.equalsIgnoreCase("CHANTS"))||(qual.equalsIgnoreCase("CHANT"))||(qual.equalsIgnoreCase("DRUID")))
			msg.append(getQualifiedAbilities(mob,Ability.CHANT,-1,"\n\r^HDruidic Chants:^? "));
		if((qual.length()==0)||(qual.equalsIgnoreCase("SONGS"))||(qual.equalsIgnoreCase("SONG"))||(qual.equalsIgnoreCase("BARD")))
			msg.append(getQualifiedAbilities(mob,Ability.SONG,-1,"\n\r^HSongs:^? "));
		if((qual.length()==0)||(qual.equalsIgnoreCase("LANGS"))||(qual.equalsIgnoreCase("LANG"))||(qual.equalsIgnoreCase("LANGUAGES")))
			msg.append(getQualifiedAbilities(mob,Ability.LANGUAGE,-1,"\n\r^HLanguages:^? "));
		int domain=-1;
		String domainName="";
		if(qual.length()>0)
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
				domainName=Util.capitalizeAndLower(Ability.DOMAIN_DESCS[domain>>5]);
				msg.append(getQualifiedAbilities(mob,Ability.SPELL,domain,"\n\r^H"+domainName+" spells:^? "));
			}
		}
		boolean classesFound=false;
		if((!CommonStrings.getVar(CommonStrings.SYSTEM_MULTICLASS).startsWith("NO"))
		&&(mob!=null)
		&&((qual.length()==0)
			||(qual.equalsIgnoreCase("CLASS"))
			||(qual.equalsIgnoreCase("CLASSES"))))
		{
			int col=0;
			StringBuffer msg2=new StringBuffer("");
			for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
			{
				CharClass C=(CharClass)c.nextElement();
				StringBuffer thisLine=new StringBuffer("");
				if(CommonStrings.isTheme(C.availabilityCode())
				&&(mob.charStats().getCurrentClass()!=C)
				&&(C.qualifiesForThisClass(mob,true)))
				{
					if((++col)>2)
					{
						thisLine.append("\n\r");
						col=1;
					}
					thisLine.append("^N[^H"+Util.padRight(""+1,3)+"^?] "
					+Util.padRight("^<HELP^>"+C.name()+"^</HELP^>",19)+" "
					+Util.padRight("1 train",(col==2)?12:13));
				}
				if(thisLine.length()>0)
				{
					if(msg2.length()==0)
						msg2.append("\n\r^HClasses:^? \n\r^N[^HLvl^?] Name                Requires     [^HLvl^?] Name                Requires\n\r");
					classesFound=true;
					msg2.append(thisLine);
				}
			}
			msg.append(msg2.toString());
		}

		if(msg.length()==0)
		{
			if(qual.length()>0)
				mob.tell("You don't appear to qualify for any '"+qual+"'. Parameters to the QUALIFY command include SKILLS, THIEF, COMMON, SPELLS, PRAYERS, CHANTS, SONGS, or LANGS.");
			else
				mob.tell("You don't appear to qualify for anything! Parameters to the QUALIFY command include SKILLS, THIEF, COMMON, SPELLS, PRAYERS, CHANTS, SONGS, or LANGS.");
		}
		else
		if(!mob.isMonster())
		{
			mob.session().wraplessPrintln("^!You now qualify for the following unknown abilities:^?"+msg.toString());
			mob.tell("\n\rUse the GAIN command with your teacher to gain new skills and spells.");
			if(classesFound) mob.tell("\n\rUse the TRAIN command to train for a new class.");
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
