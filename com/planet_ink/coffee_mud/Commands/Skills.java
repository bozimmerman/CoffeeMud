package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.SecFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2024 Bo Zimmerman

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
public class Skills extends StdCommand
{
	public Skills()
	{
	}

	protected static final int[] playerAcodes = new int[]
	{
		Ability.ACODE_PRAYER,
		Ability.ACODE_SPELL,
		Ability.ACODE_SKILL,
		Ability.ACODE_COMMON_SKILL,
		Ability.ACODE_CHANT,
		Ability.ACODE_LANGUAGE,
		Ability.ACODE_SONG,
		Ability.ACODE_SUPERPOWER,
		Ability.ACODE_TECH,
		Ability.ACODE_THIEF_SKILL
	};
	private final String[]	access	= I(new String[] { "SKILLS", "SK" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	protected boolean parsedOutIndividualSkill(final MOB mob, final String qual, final int acode)
	{
		return parsedOutIndividualSkill(mob, qual, new XVector<Integer>(Integer.valueOf(acode)));
	}

	protected boolean parsedOutIndividualSkill(final MOB mob, final String qual, final List<Integer> acodes)
	{
		if((qual==null)||(qual.length()==0)||(qual.equalsIgnoreCase("all")))
			return false;
		if(qual.length()>0)
		{
			for(int i=1;i<Ability.DOMAIN.DESCS.size();i++)
			{
				if(Ability.DOMAIN.DESCS.get(i).replace('_',' ').equalsIgnoreCase(qual))
					return false;
				else
				if((Ability.DOMAIN.DESCS.get(i).replace('_',' ').indexOf('/')>=0)
				&&(Ability.DOMAIN.DESCS.get(i).replace('_',' ').substring(Ability.DOMAIN.DESCS.get(i).indexOf('/')+1).equalsIgnoreCase(qual)))
					return false;
			}
		}
		if(acodes==null)
		{
			if(qual.length()>0)
			{
				for(int i=0;i<Ability.ACODE.DESCS.size();i++)
				{
					if(Ability.ACODE.DESCS.get(i).replace('_',' ').equalsIgnoreCase(qual))
						return false;
					else
					if((Ability.ACODE.DESCS.get(i).replace('_',' ').indexOf('/')>=0)
					&&(Ability.ACODE.DESCS.get(i).replace('_',' ').substring(Ability.ACODE.DESCS.get(i).indexOf('/')+1).equalsIgnoreCase(qual)))
						return false;
				}
			}
		}
		final boolean isSysOp = CMSecurity.isAllowedEverywhere(mob, SecFlag.ALLSKILLS);
		final List<Ability> ableVs = new XArrayList<Ability>(mob.allAbilities());
		Ability A;
		A=(Ability)CMLib.english().fetchEnvironmental(ableVs,qual,true);
		if((A==null)&&(qual.indexOf('$')<0))
			A=(Ability)CMLib.english().fetchEnvironmental(ableVs,"$"+qual,false);
		if(A==null)
			A=(Ability)CMLib.english().fetchEnvironmental(ableVs,qual,false);
		if(A==null)
			A=CMClass.findAbility(qual);
		if((A!=null)
		&&((acodes==null)||(acodes.contains(Integer.valueOf(A.classificationCode()&Ability.ALL_ACODES)))))
		{
			final Ability A2=mob.fetchAbility(A.ID());
			if(A2==null)
				mob.tell(L("You don't know '@x1'.",A.name()));
			else
			{
				final int prowessCode = CMProps.getIntVar(CMProps.Int.COMBATPROWESS);
				final boolean useWords=CMProps.Int.Prowesses.SKILL_PROFICIENCY.is(prowessCode);
				int level=CMLib.ableMapper().qualifyingLevel(mob,A2);
				if(level<0)
					level=0;
				final StringBuffer line=new StringBuffer("");
				line.append("\n\rLevel ^!"+level+"^?:\n\r");
				int proficiency = A2.proficiency();
				proficiency += mob.charStats().getAbilityAdjustment("PROF+"+A2.ID().toUpperCase());
				proficiency += mob.charStats().getAbilityAdjustment("PROF+"+Ability.ACODE.DESCS.get(A2.classificationCode()&Ability.ALL_ACODES));
				proficiency += mob.charStats().getAbilityAdjustment("PROF+"+Ability.DOMAIN.DESCS.get((A2.classificationCode()&Ability.ALL_DOMAINS)>> 5));
				proficiency += mob.charStats().getAbilityAdjustment("PROF+*");
				if(proficiency>100)
					proficiency=100;
				if(useWords)
				{
					final String message = CMLib.help().getRPProficiencyStr(proficiency);
					line.append("^<HELP^>"+A2.name()+"^</HELP^>");
					line.append(" ("+CMStrings.padRight(message,3)+")");
				}
				else
				if(A.isSavable()||isSysOp)
				{
					line.append("^N[^H"+CMStrings.padRight(Integer.toString(proficiency),3)+"%^?]^N ");
					line.append("^<HELP^>"+A2.name()+"^</HELP^>");
				}
				else
				{
					line.append("^N[^k"+CMStrings.padRight(Integer.toString(proficiency),3)+"%^?]^N ");
					line.append("^<HELP^>^k"+A2.name()+"^?^</HELP^>");
				}
				line.append("^?\n\r");
				if(mob.session()!=null)
					mob.session().wraplessPrintln(line.toString());
			}
			return true;
		}
		return false;
	}

	protected int parseOutLevel(final List<String> commands)
	{
		if((commands.size()>1)
		&&(CMath.isNumber(commands.get(commands.size()-1))))
		{
			final int x=CMath.s_int(commands.get(commands.size()-1));
			commands.remove(commands.size()-1);
			return x;
		}
		return -1;
	}

	/**
	 * Returns whether there are any crossings between a particular Ability type
	 * and a particular Ability domain.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#abilityCode()
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#DOMAIN.DESCS
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#ACODE_DESCS
	 *
	 * @param domain the domain mask
	 * @param acode the ability code
	 * @return true if they meet somewhere
	 */
	public boolean isDomainIncludedInAnyAbility(final int domain, final int acode)
	{
		@SuppressWarnings("unchecked")
		Map<Integer, Set<Integer>> completeDomainMap = (Map<Integer, Set<Integer>>)Resources.getResource("SYSTEM_ABLEDOMAINMAP");
		if(completeDomainMap == null)
		{
			completeDomainMap = new SHashtable<Integer,Set<Integer>>();
			Resources.submitResource("SYSTEM_ABLEDOMAINMAP",completeDomainMap);
		}
		STreeSet<Integer> V=(STreeSet<Integer>)completeDomainMap.get(Integer.valueOf(domain));
		if(V==null)
		{
			Ability A=null;
			V=new STreeSet<Integer>();
			for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
			{
				A=e.nextElement();
				if(((A.classificationCode()&Ability.ALL_DOMAINS)==domain)
				&&(!V.contains(Integer.valueOf((A.classificationCode()&Ability.ALL_ACODES)))))
					V.add(Integer.valueOf((A.classificationCode()&Ability.ALL_ACODES)));
			}
			completeDomainMap.put(Integer.valueOf(domain),V);
		}
		return V.contains(Integer.valueOf(acode));
	}

	protected void parseDomainInfo(final MOB mob, final List<String> commands, final List<Integer> acodes, final int[] level, final int[] domain, final String[] domainName)
	{
		level[0]=parseOutLevel(commands);
		final String qual=CMParms.combine(commands,1).toUpperCase();
		domain[0]=-1;
		if(qual.length()>0)
		{
			for(int i=1;i<Ability.DOMAIN.DESCS.size();i++)
			{
				if(Ability.DOMAIN.DESCS.get(i).replace('_',' ').startsWith(qual))
				{
					domain[0] = i << 5;
					break;
				}
				else
				if((Ability.DOMAIN.DESCS.get(i).replace('_',' ').indexOf('/')>=0)
				&&(Ability.DOMAIN.DESCS.get(i).replace('_',' ').substring(Ability.DOMAIN.DESCS.get(i).indexOf('/')+1).startsWith(qual)))
				{
					domain[0] = i << 5;
					break;
				}
			}
		}
		if(domain[0]>0)
			domainName[0]=Ability.DOMAIN.DESCS.get(domain[0]>>5).toLowerCase();
		if((domain[0]<0)&&(qual.length()>0))
		{
			StringBuffer domains=new StringBuffer("");
			domains.append("\n\rValid schools/domains are: ");
			for(int i=1;i<Ability.DOMAIN.DESCS.size();i++)
			{
				boolean found=acodes==null?true:false;
				if(acodes!=null)
				{
					for(int a=0;a<acodes.size();a++)
						found=found||isDomainIncludedInAnyAbility(i<<5,acodes.get(a).intValue());
				}
				if(found)
					domains.append(Ability.DOMAIN.DESCS.get(i).toLowerCase().replace('_',' ')+", ");
			}
			if(domains.toString().endsWith(", "))
				domains=new StringBuffer(domains.substring(0,domains.length()-2));
			if(!mob.isMonster())
				mob.session().wraplessPrintln(domains.toString()+"\n\r");
		}
		else
		if(qual.length()>0)
			domainName[0]+=" ";
	}

	protected boolean pickUniqueFlag(final List<String> commands, final boolean uniqueOnly)
	{
		if(!uniqueOnly)
		{
			if((commands.size()>0)&&(commands.get(0).equalsIgnoreCase("UNIQUE")))
			{
				commands.remove(0);
				return true;
			}
		}
		return uniqueOnly;
	}

	protected boolean isUnique(final String abilityID, final String classID, final String raceID)
	{
		if(CMLib.ableMapper().getAllAbleMap(abilityID) != null)
			return false;
		final Set<String> quals = CMLib.ableMapper().getQualifyingEntities(abilityID);
		if(quals.size()==0)
			return true;
		if(quals.contains(classID))
		{
			for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
			{
				final CharClass C=c.nextElement();
				if(quals.contains(C.ID())
				&& (!C.ID().equals(classID))
				&& (!(C instanceof ArchonOnly)))
					return false;
			}
		}
		if(quals.contains(raceID))
		{
			for(final Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
			{
				final Race R=r.nextElement();
				if(quals.contains(R.ID())
				&& (!R.ID().equals(raceID)))
					return false;
			}
		}
		return true;
	}

	protected void parseTypeInfo(final MOB mob, final List<String> commands, final int[] level, final int[] type, final String[] typeName)
	{
		level[0]=parseOutLevel(commands);
		final String qual=CMParms.combine(commands,1).toUpperCase();
		type[0]=-1;
		if(qual.length()>0)
		for(int i=0;i<Ability.ACODE.DESCS.size();i++)
		{
			if(Ability.ACODE.DESCS.get(i).replace('_',' ').startsWith(qual))
			{
				type[0] = i;
				break;
			}
			else
			if((Ability.ACODE.DESCS.get(i).replace('_',' ').indexOf('/')>=0)
			&&(Ability.ACODE.DESCS.get(i).replace('_',' ').substring(Ability.ACODE.DESCS.get(i).indexOf('/')+1).startsWith(qual)))
			{
				type[0] = i;
				break;
			}
		}
		if(type[0]>0)
			typeName[0]=Ability.ACODE.DESCS.get(type[0]>>5).toLowerCase();
		if((type[0]<0)&&(qual.length()>0))
		{
			for(int i=1;i<Ability.DOMAIN.DESCS.size();i++)
			{
				if(Ability.DOMAIN.DESCS.get(i).replace('_',' ').equalsIgnoreCase(qual))
					return;
				else
				if((Ability.DOMAIN.DESCS.get(i).replace('_',' ').indexOf('/')>=0)
				&&(Ability.DOMAIN.DESCS.get(i).replace('_',' ').substring(Ability.DOMAIN.DESCS.get(i).indexOf('/')+1).equalsIgnoreCase(qual)))
					return;
			}
			StringBuffer types=new StringBuffer("");
			types.append("\n\rValid ability types are: ");
			for(int i=0;i<playerAcodes.length;i++)
			{
				types.append(Ability.ACODE.DESCS.get(playerAcodes[i]).toLowerCase().replace('_',' ')+", ");
			}
			if(types.toString().endsWith(", "))
				types=new StringBuffer(types.substring(0,types.length()-2));
			if(!mob.isMonster())
				mob.session().wraplessPrintln(types.toString()+"\n\r");
		}
		else
		if(qual.length()>0)
			typeName[0]+=" ";
	}

	protected StringBuilder getAbilities(final MOB viewerM, final MOB ableM, int ofType, final int ofDomain, final boolean addQualLine, final int maxLevel)
	{
		final ArrayList<Integer> V=new ArrayList<Integer>();
		int mask=Ability.ALL_ACODES;
		if(ofDomain>=0)
		{
			mask=Ability.ALL_ACODES|Ability.ALL_DOMAINS;
			ofType=ofType|ofDomain;
		}
		V.add(Integer.valueOf(ofType));
		return getAbilities(viewerM,ableM,V,mask,addQualLine,maxLevel);
	}

	protected final static Comparator<Ability> nameComparator=new Comparator<Ability>()
	{
		@Override
		public int compare(final Ability o1, final Ability o2)
		{
			if(o1==null)
				return o2==null?0:-1;
			if(o2==null)
				return 1;
			return o1.Name().compareTo(o2.Name());
		}
	};

	protected StringBuilder getAbilities(final MOB viewerM, final MOB ableM, final List<Integer> ofTypes, final int mask, final boolean addQualLine, final int maxLevel)
	{
		final int prowessCode = CMProps.getIntVar(CMProps.Int.COMBATPROWESS);
		final boolean useWords=CMProps.Int.Prowesses.SKILL_PROFICIENCY.is(prowessCode);
		final int COL_LEN1=CMLib.lister().fixColWidth(3.0,viewerM);
		final int COL_LEN2=CMLib.lister().fixColWidth(31.0,viewerM);
		final int COL_LEN3=CMLib.lister().fixColWidth(31.0,viewerM);
		//final int COL_LEN2=useWords? CMLib.lister().fixColWidth(19.0,viewerM) : CMLib.lister().fixColWidth(18.0,viewerM);
		//final int COL_LEN3=useWords? CMLib.lister().fixColWidth(17.0,viewerM) : CMLib.lister().fixColWidth(18.0,viewerM);
		int highestLevel=0;
		final int lowestLevel=ableM.phyStats().level()+1;
		final StringBuilder msg=new StringBuilder("");
		final Integer allAcodes = Integer.valueOf(Ability.ALL_ACODES);
		final boolean isSysOp = CMSecurity.isAllowedEverywhere(ableM, SecFlag.ALLSKILLS);
		for(final Enumeration<Ability> a=ableM.allAbilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			int level=CMLib.ableMapper().qualifyingLevel(ableM,A);
			if(level<0)
				level=0;
			if((A!=null)
			&&(level>highestLevel)
			&&(level<lowestLevel)
			&&(ofTypes.contains(Integer.valueOf(A.classificationCode()&mask))
				||ofTypes.contains(allAcodes)||ofTypes.contains(Integer.valueOf(Ability.ALL_ACODES|(A.classificationCode()&Ability.ALL_DOMAINS)))))
				highestLevel=level;
		}
		if((maxLevel>=0)&&(maxLevel<highestLevel))
			highestLevel=maxLevel;
		final int MAX_COLS=2;//useWords?2:3;
		final List<Ability> sortedAllAbilities = new XVector<Ability>(ableM.allAbilities());
		Collections.sort(sortedAllAbilities,nameComparator);
		for(int l=0;l<=highestLevel;l++)
		{
			final StringBuilder thisLine=new StringBuilder("");
			int col=0;

			for(final Ability A : sortedAllAbilities)
			{
				int level=CMLib.ableMapper().qualifyingLevel(ableM,A);
				if(level<0)
					level=0;
				if((A!=null)
				&&(level==l)
				&&(ofTypes.contains(Integer.valueOf(A.classificationCode()&mask))
					||ofTypes.contains(allAcodes)||ofTypes.contains(Integer.valueOf(Ability.ALL_ACODES|(A.classificationCode()&Ability.ALL_DOMAINS)))))
				{
					if(thisLine.length()==0)
					{
						if(useWords)
							thisLine.append("\n\r^!Level ^H"+l+"^N:\n\r");
						else
							thisLine.append("\n\rLevel ^!"+l+"^?:\n\r");
					}
					col++;
					int proficiency = A.proficiency();
					proficiency += ableM.charStats().getAbilityAdjustment("PROF+"+A.ID().toUpperCase());
					proficiency += ableM.charStats().getAbilityAdjustment("PROF+"+Ability.ACODE.DESCS.get(A.classificationCode()&Ability.ALL_ACODES));
					proficiency += ableM.charStats().getAbilityAdjustment("PROF+"+Ability.DOMAIN.DESCS.get((A.classificationCode()&Ability.ALL_DOMAINS)>> 5));
					proficiency += ableM.charStats().getAbilityAdjustment("PROF+*");
					if(proficiency>100)
						proficiency=100;
					String tagOpen="^<HELP^>";
					String tagClose="^</HELP^>";
					if(!A.isSavable()&&!isSysOp)
					{
						tagOpen+="^k";
						tagClose="^?"+tagClose;
					}
					if(!useWords)
					{
						if(A.isSavable()||isSysOp)
							thisLine.append("^N[^H").append(CMStrings.padRight(Integer.toString(proficiency),COL_LEN1));
						else
							thisLine.append("^N[^k").append(CMStrings.padRight(Integer.toString(proficiency),COL_LEN1));
						thisLine.append("%^?]^N");
						thisLine.append(" ");//+(A.isAutoInvoked()?"^H.^N":" ")
						if(col < MAX_COLS)
							thisLine.append(CMStrings.padRight(tagOpen,A.name(),tagClose,COL_LEN2));
						else
						{
							thisLine.append(CMStrings.limit(tagOpen,A.name(),tagClose+"\n\r",COL_LEN3));
							col=0;
						}
					}
					else
					{
						final String color = (A.isSavable()||isSysOp)?"^H":"^k";
						thisLine.append(CMStrings.padRight(tagOpen,A.name(),tagClose,COL_LEN2));
						final String message = CMLib.help().getRPProficiencyStr(proficiency);
						if(col < MAX_COLS)
							thisLine.append(CMStrings.padRight("^N("+color,message,"^?)^N",COL_LEN3));
						else
						{
							thisLine.append(CMStrings.limit("^N("+color,message,"^?)^N\n\r",COL_LEN3));
							col=0;
						}
					}
				}
			}
			if(thisLine.length()>0)
			{
				final String line=thisLine.toString();
				if(line.endsWith("\n\r"))
					msg.append(line);
				else
					msg.append(line).append("\n\r");
			}
		}
		if(msg.length()==0)
			msg.append(L("^!None!^?"));
		else
		if(addQualLine)
			msg.append(L("\n\r\n\rUse QUALIFY to see additional skills you can GAIN."));// ^H.^N = passive/auto-invoked."));
		return msg;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final StringBuilder msg=new StringBuilder("");
		final Vector<Integer> V=new Vector<Integer>();
		V.add(Integer.valueOf(Ability.ACODE_THIEF_SKILL));
		V.add(Integer.valueOf(Ability.ACODE_SKILL));
		V.add(Integer.valueOf(Ability.ACODE_COMMON_SKILL));
		final String qual=CMParms.combine(commands,1).toUpperCase();
		if(parsedOutIndividualSkill(mob,qual,V))
			return true;
		final int[] level=new int[1];
		final int[] domain=new int[1];
		final String[] domainName=new String[1];
		domainName[0]="";
		level[0]=-1;
		parseDomainInfo(mob,commands,V,level,domain,domainName);
		int mask=Ability.ALL_ACODES;
		if(domain[0]>=0)
		{
			mask=mask|Ability.ALL_DOMAINS;
			for(int v=0;v<V.size();v++)
				V.setElementAt(Integer.valueOf(V.get(v).intValue()+domain[0]),v);
		}
		if((domain[0]>=0)||(qual.length()==0))
			msg.append(L("\n\r^HYour @x1skills:^? @x2",domainName[0].replace('_',' '),getAbilities(mob,mob,V,mask,true,level[0]).toString()));
		if(!mob.isMonster())
			mob.session().wraplessPrintln(msg.toString());
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
