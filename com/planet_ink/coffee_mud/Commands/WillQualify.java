package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/**
 * <p>Title: False Realities Flavored CoffeeMUD</p>
 * <p>Description: The False Realities Version of CoffeeMUD</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: http://falserealities.game-host.org</p>
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
					thisLine.append("^N[^H" + Util.padRight("" + l, 3) + "^?] "
					        + Util.padRight(A.name(), 19) + " "
					        + Util.padRight(A.requirements(), (col == 2) ? 12 : 13));
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
		msg.append("\n\r");
		return msg;
	}

	public boolean execute(MOB mob, Vector commands)
	                throws java.io.IOException
	{
		StringBuffer msg=new StringBuffer("");
		String willQualErr = "Specify level and class:  WILLQUALIFY [LEVEL] [CLASS NAME].";
		if(commands.size()<3){ mob.tell(willQualErr); return false;}
		// # is param 1, class name is param 2+
		int level=Util.s_int((String)commands.elementAt(1));
		if (level > 0) 
		{
			String className=Util.combine(commands,2);
			CharClass C=CMClass.getCharClass(className);
			if (C == null) 
			{
			        mob.tell("No class found by that name.");
			        return false;
			}
			msg.append("At level "+level+" of class '"+C.ID()+"', you could qualify for:\n\r");
			msg.append(getQualifiedAbilities(mob,C.ID(),level,""));
			if(!mob.isMonster())
			    mob.session().unfilteredPrintln(msg.toString());
			return false;
		}
		else
		{
			mob.tell(willQualErr);
			return false;
		}
	}
}
