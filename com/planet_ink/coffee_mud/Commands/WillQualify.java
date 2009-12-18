package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
 * <p>Title: False Realities Flavored CoffeeMUD</p>
 * <p>Description: The False Realities Version of CoffeeMUD</p>
 * <p>Copyright: Copyright (c) 2004 Jeremy Vyska</p>
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * <p>you may not use this file except in compliance with the License.
 * <p>You may obtain a copy of the License at
 *
 * <p>       http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * <p>distributed under the License is distributed on an "AS IS" BASIS,
 * <p>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>See the License for the specific language governing permissions and
 * <p>limitations under the License.
 * <p>Company: http://www.falserealities.com</p>
 * @author FR - Jeremy Vyska; CM - Bo Zimmerman
 * @version 1.0.0.0
 */

@SuppressWarnings("unchecked")
public class WillQualify  extends Skills
{
	public WillQualify() {}
	private String[] access={"WILLQUALIFY"};
	public String[] getAccessWords(){return access;}

	public StringBuffer getQualifiedAbilities(MOB able, 
											  String Class,
	                                          int maxLevel, 
	                                          String prefix,
	                                          Vector types)
	{
		int highestLevel = maxLevel;
		StringBuffer msg = new StringBuffer("");
		int col = 0;
        DVector DV=CMLib.ableMapper().getClassAllowsList(Class);
		for (int l = 0; l <= highestLevel; l++) 
		{
			StringBuffer thisLine = new StringBuffer("");
			for (Enumeration a = CMLib.ableMapper().getClassAbles(Class,true); a.hasMoreElements(); ) 
			{
				AbilityMapper.AbilityMapping cimable=(AbilityMapper.AbilityMapping)a.nextElement();
				if((cimable.qualLevel ==l)&&(!cimable.isSecret))
				{
					Ability A=CMClass.getAbility(cimable.abilityName);
					if((A!=null)
                    &&((types.size()==0)
						||(types.contains(Integer.valueOf(A.classificationCode()&Ability.ALL_ACODES)))
						||(types.contains(Integer.valueOf(A.classificationCode()&Ability.ALL_DOMAINS)))))
					{
						if ( (++col) > 2) 
						{
						    thisLine.append("\n\r");
						    col = 1;
						}
    					thisLine.append("^N[^H" + CMStrings.padRight("" + l, 3) + "^?] "
    					        + CMStrings.padRight("^<HELP^>"+A.name()+"^</HELP^>", 19) + " "
    					        + CMStrings.padRight(A.requirements()+(cimable.autoGain?" *":""), (col == 2) ? 12 : 13));
					}
				}
			}
			ExpertiseLibrary.ExpertiseDefinition E=null;
			Integer qualLevel=null;
			for(int d=0;d<DV.size();d++)
			{
				qualLevel=(Integer)DV.elementAt(d,2);
				E=CMLib.expertises().getDefinition((String)DV.elementAt(d,1));
				if(E!=null)
				{
	            	int minLevel=E.getMinimumLevel();
	            	if(minLevel<qualLevel.intValue())
	            		minLevel=qualLevel.intValue();
	            	if(minLevel==l)
	            	{
						if((types.size()==0)
						||types.contains("EXPERTISE")
						||types.contains("EXPERTISES")
						||types.contains(E.ID.toUpperCase())
						||types.contains(E.name.toUpperCase()))
						{
							if ( (++col) > 2) 
							{
							    thisLine.append("\n\r");
							    col = 1;
							}
							thisLine.append("^N[^H" + CMStrings.padRight("" + l, 3) + "^?] "
							        + CMStrings.padRight("^<HELP^>"+E.name+"^</HELP^>", 19) + " "
							        + CMStrings.padRight(E.costDescription(), (col == 2) ? 12 : 13));
						}
	            	}
				}
			}
			if (thisLine.length() > 0) 
			{
				if (msg.length() == 0)
				        msg.append("\n\r^N[^HLvl^?] Name                Requires     [^HLvl^?] Name                Requires\n\r");
				msg.append(thisLine);
			}
		}
		if (msg.length() == 0)
		        return msg;
		msg.insert(0, prefix);
		msg.append("\n\r* This skill is automatically granted.");
		return msg;
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
	                throws java.io.IOException
	{
		StringBuffer msg=new StringBuffer("");
		String willQualErr = "Specify level, class, and or skill-type:  WILLQUALIFY ([LEVEL]) ([CLASS NAME]) ([SKILL TYPE]).";
		int level=CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL);
		CharClass C=mob.charStats().getCurrentClass();
		Vector types=new Vector();
		if(commands.size()>0) commands.removeElementAt(0);
		if((commands.size()>0)&&(CMath.isNumber((String)commands.firstElement())))
		{
			level=CMath.s_int((String)commands.firstElement());
			if(level<0)
			{
				mob.tell(willQualErr);
				return false;
			}
			commands.removeElementAt(0);
		}
		if(commands.size()>0)
		{
			CharClass C2=CMClass.findCharClass((String)commands.firstElement());
			if(C2!=null){ C=C2;commands.removeElementAt(0);}
		}
		while(commands.size()>0)
		{
			String str=((String)commands.firstElement()).toUpperCase().trim();
			int x=CMParms.indexOf(Ability.ACODE_DESCS,str);
			if(x<0) x=CMParms.indexOf(Ability.ACODE_DESCS,str.replace(' ','_'));
			if(x>=0)
				types.addElement(Integer.valueOf(x));
			else
			{
				x=CMParms.indexOf(Ability.DOMAIN_DESCS,str);
				if(x<0)
					x=CMParms.indexOf(Ability.DOMAIN_DESCS,str.replace(' ','_'));
				if(x<0)
				{
					if((CMLib.expertises().findDefinition(str,false)==null)
					&&!str.equalsIgnoreCase("EXPERTISE")
					&&!str.equalsIgnoreCase("EXPERTISES"))
					{
						mob.tell("'"+str+"' is not a valid skill type, domain, expertise, or character class.");
						mob.tell(willQualErr);
						return false;
					}
					types.addElement(str.toUpperCase().trim());
				}
				else
					types.addElement(Integer.valueOf(x<<5));
			}
			commands.removeElementAt(0);
		}
		
		msg.append("At level "+level+" of class '"+C.name()+"', you could qualify for:\n\r");
		msg.append(getQualifiedAbilities(mob,C.ID(),level,"",types));
		if(!mob.isMonster())
		    mob.session().wraplessPrintln(msg.toString());
		return false;
	}
}
