package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Languages extends BaseAbleLister
{
	public Languages(){}
	
	private String[] access={"LANGUAGES","LANGS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\r^HLanguages known:^? "+getAbilities(mob,Ability.LANGUAGE,-1,true)+"\n\r");
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(msg.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
