package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/**
 * Title: False Realities Flavored CoffeeMUD
 * Description: The False Realities Version of CoffeeMUD
 * Copyright: Copyright (c) 2004 Jeremy Vyska
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Company: http://www.falserealities.com
 * @author FR - Jeremy Vyska; CM - Bo Zimmerman
 * @version 1.0.0.0
 */

public class WillQualify  extends Skills
{
	public WillQualify() {}
	private final String[] access=I(new String[]{"WILLQUALIFY"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public StringBuffer getQualifiedAbilities(MOB viewerM,
											  MOB ableM,
											  String classID,
											  String raceID,
											  int maxLevel,
											  String prefix,
											  HashSet<Object> types)
	{
		final int highestLevel = maxLevel;
		final StringBuffer msg = new StringBuffer("");
		int col = 0;
		final int COL_LEN1=CMLib.lister().fixColWidth(3.0,viewerM);
		final int COL_LEN2=CMLib.lister().fixColWidth(19.0,viewerM);
		final int COL_LEN3=CMLib.lister().fixColWidth(12.0,viewerM);
		final int COL_LEN4=CMLib.lister().fixColWidth(13.0,viewerM);
		final List<AbilityMapper.QualifyingID> DV=CMLib.ableMapper().getClassAllowsList(classID);
		for (int l = 0; l <= highestLevel; l++)
		{
			final StringBuffer thisLine = new StringBuffer("");
			@SuppressWarnings("unchecked")
			final Enumeration<AbilityMapper.AbilityMapping> emur = new MultiEnumeration<AbilityMapper.AbilityMapping>(
				new Enumeration[]
				{
					CMLib.ableMapper().getClassAbles(classID,true),
					CMLib.ableMapper().getClassAbles(raceID,false)
				}
			);
			for (final Enumeration<AbilityMapper.AbilityMapping> a = emur; a.hasMoreElements(); )
			{
				final AbilityMapper.AbilityMapping cimable=a.nextElement();
				if((cimable.qualLevel() ==l)&&(!cimable.isSecret()))
				{
					final Ability A=CMClass.getAbility(cimable.abilityID());
					if((A!=null)
					&&((types.size()==0)
						||(types.contains(Integer.valueOf(A.classificationCode()&Ability.ALL_ACODES)))
						||(types.contains(Integer.valueOf(A.classificationCode()&Ability.ALL_DOMAINS))))
					&&(CMLib.ableComponents().getSpecialSkillLimit(ableM, A).specificSkillLimit() > 0))
					{
						if ( (++col) > 2)
						{
							thisLine.append("\n\r");
							col = 1;
						}
						thisLine.append("^N[^H" + CMStrings.padRight("" + l, COL_LEN1) + "^?] "
								+ CMStrings.padRight("^<HELP^>"+A.name()+"^</HELP^>", COL_LEN2) + " "
								+ CMStrings.padRight(A.requirements(viewerM)+(cimable.autoGain()?" *":""), (col == 2) ? COL_LEN3 : COL_LEN4));
					}
				}
			}
			ExpertiseLibrary.ExpertiseDefinition E=null;
			Integer qualLevel=null;
			for(final AbilityMapper.QualifyingID qID : DV)
			{
				qualLevel=Integer.valueOf(qID.qualifyingLevel());
				E=CMLib.expertises().getDefinition(qID.ID());
				if(E!=null)
				{
					int minLevel=E.getMinimumLevel();
					if(minLevel<qualLevel.intValue())
						minLevel=qualLevel.intValue();
					if((minLevel==l)
					&&((types.size()==0)
						||types.contains("EXPERTISE")
						||types.contains("EXPERTISES")
						||types.contains(E.ID().toUpperCase())
						||types.contains(E.name().toUpperCase())))
					{
						if ( (++col) > 2)
						{
							thisLine.append("\n\r");
							col = 1;
						}
						thisLine.append("^N[^H" + CMStrings.padRight("" + l, 3) + "^?] "
								+ CMStrings.padRight("^<HELP^>"+E.name()+"^</HELP^>", 19) + " "
								+ CMStrings.padRight(E.costDescription(), (col == 2) ? 12 : 13));
					}
				}
			}
			if (thisLine.length() > 0)
			{
				if (msg.length() == 0)
						msg.append(L("\n\r^N[^HLvl^?] Name                Requires     [^HLvl^?] Name                Requires\n\r"));
				msg.append(thisLine);
			}
		}
		if (msg.length() == 0)
				return msg;
		msg.insert(0, prefix);
		msg.append(L("\n\r* This skill is automatically granted."));
		return msg;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
					throws java.io.IOException
	{
		final StringBuffer msg=new StringBuffer("");
		final String willQualErr = "Specify level, class, and or skill-type:  WILLQUALIFY ([LEVEL]) ([CLASS NAME]) ([SKILL TYPE]).";
		int level=CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL);
		CharClass C=mob.charStats().getCurrentClass();
		final HashSet<Object> types=new HashSet<Object>();
		if(commands.size()>0)
			commands.remove(0);
		if((commands.size()>0)&&(CMath.isNumber(commands.get(0))))
		{
			level=CMath.s_int(commands.get(0));
			if(level<0)
			{
				mob.tell(willQualErr);
				return false;
			}
			commands.remove(0);
		}
		if(commands.size()>0)
		{
			final CharClass C2=CMClass.findCharClass(commands.get(0));
			if (C2 != null)
			{
				C = C2;
				commands.remove(0);
			}
		}
		while(commands.size()>0)
		{
			final String str=commands.get(0).toUpperCase().trim();
			final String bothStr=(commands.size()<2) ? str : 
				commands.get(0).toUpperCase().trim() + " " + commands.get(1).toUpperCase().trim();
			int x=CMParms.indexOf(Ability.ACODE_DESCS,str);
			if(x<0)
				x=CMParms.indexOf(Ability.ACODE_DESCS,str.replace(' ','_'));
			if(x>=0)
			{
				commands.remove(0);
				types.add(Integer.valueOf(x));
				continue;
			}
			else
			{
				x=CMParms.indexOf(Ability.ACODE_DESCS,bothStr);
				if(x<0)
					x=CMParms.indexOf(Ability.ACODE_DESCS,bothStr.replace(' ','_'));
				if(x>=0)
				{
					
					commands.remove(0);
					commands.remove(0);
					types.add(Integer.valueOf(x));
					continue;
				}
			}
			
			x=CMParms.indexOf(Ability.DOMAIN_DESCS,str);
			if(x<0)
				x=CMParms.indexOf(Ability.DOMAIN_DESCS,str.replace(' ','_'));
			if(x>=0)
			{
				commands.remove(0);
				types.add(Integer.valueOf(x<<5));
				continue;
			}
			else
			{
				x=CMParms.indexOf(Ability.DOMAIN_DESCS,bothStr);
				if(x<0)
					x=CMParms.indexOf(Ability.DOMAIN_DESCS,bothStr.replace(' ','_'));
				if(x>=0)
				{
					commands.remove(0);
					commands.remove(0);
					types.add(Integer.valueOf(x<<5));
					continue;
				}
			}
				
			if((CMLib.expertises().findDefinition(str,false)!=null)
			||str.equalsIgnoreCase("EXPERTISE")
			||str.equalsIgnoreCase("EXPERTISES"))
			{
				commands.remove(0);
				types.add(str.toUpperCase().trim());
				continue;
			}
			else
			if((CMLib.expertises().findDefinition(bothStr,false)!=null))
			{
				commands.remove(0);
				commands.remove(0);
				types.add(bothStr.toUpperCase().trim());
				continue;
			}
			mob.tell(L("'@x1' is not a valid skill type, domain, expertise, or character class.",str));
			mob.tell(willQualErr);
			return false;
		}

		msg.append(L("At level @x1 of class '@x2', you could qualify for:\n\r",""+level,C.name()));
		final String raceID = mob.baseCharStats().getMyRace().ID();
		msg.append(getQualifiedAbilities(mob,mob,C.ID(),raceID,level,"",types));
		if(!mob.isMonster())
			mob.session().wraplessPrintln(msg.toString());
		return false;
	}
}
