package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.Items.Armor.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.db.*;

public class Archon extends StdCharClass
{
	public Archon()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		for(int i=0;i<=5;i++)
			maxStat[i]=25;
		name=myID;
	}
	
	public boolean playerSelectable()
	{
		return false;
	}
	
	public boolean qualifiesForThisClass(MOB mob)
	{
		return false;
	}
	
	public void logon(MOB mob)
	{
		super.logon(mob);
		for(int a=0;a<MUD.abilities.size();a++)
		{
			Ability A=(Ability)MUD.abilities.elementAt(a);
			if(A.qualifyingLevel(mob)>0)
			{
				giveMobAbility(mob,A);
				A=mob.fetchAbility(A.ID());
				if(A!=null)
					A.setProfficiency(100);
			}
		}
	};
	
	public void newCharacter(MOB mob)
	{
		super.newCharacter(mob);
		logon(mob);
	}
	
	public void level(MOB mob)
	{
		mob.tell("You leveled... not that it matters.");
	}
}
