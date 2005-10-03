package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
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

public class WillQualify extends BaseAbleLister{
	public WillQualify() {}
	private String[] access={"WILLQUALIFY"};
	public String[] getAccessWords(){return access;}

	public StringBuffer getQualifiedAbilities(MOB able, String Class,
	                                          int maxLevel, String prefix)
	{
		int highestLevel = maxLevel;
		StringBuffer msg = new StringBuffer("");
		int col = 0;
		for (int l = 0; l <= highestLevel; l++) 
		{
			StringBuffer thisLine = new StringBuffer("");
			for (Enumeration a = CMAble.getClassAbles(Class); a.hasMoreElements(); ) 
			{
				CMAble cimable=(CMAble)a.nextElement();
				if((cimable.qualLevel ==l)&&(!cimable.isSecret))
				{
					if ( (++col) > 2) 
					{
					    thisLine.append("\n\r");
					    col = 1;
					}
					Ability A=CMClass.getAbility(cimable.abilityName);
					if(A!=null)
                    {
    					thisLine.append("^N[^H" + Util.padRight("" + l, 3) + "^?] "
    					        + Util.padRight("^<HELP^>"+A.name()+"^</HELP^>", 19) + " "
    					        + Util.padRight(A.requirements()+(cimable.autoGain?" *":""), (col == 2) ? 12 : 13));
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

	public boolean execute(MOB mob, Vector commands)
	                throws java.io.IOException
	{
		StringBuffer msg=new StringBuffer("");
		String willQualErr = "Specify level and class:  WILLQUALIFY [LEVEL] ([CLASS NAME]).";
		if((commands.size()<2)||((commands.size()>1)&&(!Util.isNumber((String)commands.elementAt(1)))))
		{
			mob.tell(willQualErr); 
			return false;
		}
		// # is param 1, class name is param 2+
		int level=Util.s_int((String)commands.elementAt(1));
		if (level > 0) 
		{
			
			String className=mob.charStats().getCurrentClass().ID();
			if(commands.size()>2) className=Util.combine(commands,2);
			CharClass C=CMClass.findCharClass(className);
			if (C == null) 
			{
			        mob.tell("No class found by that name.");
			        return false;
			}
			msg.append("At level "+level+" of class '"+C.ID()+"', you could qualify for:\n\r");
			msg.append(getQualifiedAbilities(mob,C.ID(),level,""));
			if(!mob.isMonster())
			    mob.session().wraplessPrintln(msg.toString());
			return false;
		}
		mob.tell(willQualErr);
		return false;
	}
}
