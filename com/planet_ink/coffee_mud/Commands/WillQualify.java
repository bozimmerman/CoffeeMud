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

public class WillQualify extends BaseAbleLister{
	public WillQualify() {}
	private String[] access={getScr("WillQualify","cmd1")};
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
			for (Enumeration a = CMLib.ableMapper().getClassAbles(Class); a.hasMoreElements(); ) 
			{
				AbilityMapper.AbilityMapping cimable=(AbilityMapper.AbilityMapping)a.nextElement();
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
    					thisLine.append("^N[^H" + CMStrings.padRight("" + l, 3) + "^?] "
    					        + CMStrings.padRight("^<HELP^>"+A.name()+"^</HELP^>", 19) + " "
    					        + CMStrings.padRight(A.requirements()+(cimable.autoGain?" *":""), (col == 2) ? 12 : 13));
                    }
				}
			}
			if (thisLine.length() > 0) 
			{
				if (msg.length() == 0)
				        msg.append(getScr("WillQualify","qualhdr"));
				msg.append(thisLine);
			}
		}
		if (msg.length() == 0)
		        return msg;
		msg.insert(0, prefix);
		msg.append(getScr("WillQualify","autogrant"));
		return msg;
	}

	public boolean execute(MOB mob, Vector commands)
	                throws java.io.IOException
	{
		StringBuffer msg=new StringBuffer("");
		String willQualErr = getScr("WillQualify","willqualinst");
		if((commands.size()<2)||((commands.size()>1)&&(!CMath.isNumber((String)commands.elementAt(1)))))
		{
			mob.tell(willQualErr); 
			return false;
		}
		// # is param 1, class name is param 2+
		int level=CMath.s_int((String)commands.elementAt(1));
		if (level > 0) 
		{
			
			String className=mob.charStats().getCurrentClass().ID();
			if(commands.size()>2) className=CMParms.combine(commands,2);
			CharClass C=CMClass.findCharClass(className);
			if (C == null) 
			{
			        mob.tell(getScr("WillQualify","noclass"));
			        return false;
			}
			msg.append(getScr("WillQualify","atlvlannounce",""+level,C.ID()));
			msg.append(getQualifiedAbilities(mob,C.ID(),level,""));
			if(!mob.isMonster())
			    mob.session().wraplessPrintln(msg.toString());
			return false;
		}
		mob.tell(willQualErr);
		return false;
	}
}
