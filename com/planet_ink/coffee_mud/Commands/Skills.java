package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skills extends BaseAbleLister
{
	public Skills(){}

	private String[] access={"SKILLS","SK"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		StringBuffer msg=new StringBuffer("");
		Vector V=new Vector();
		V.addElement(new Integer(Ability.THIEF_SKILL));
		V.addElement(new Integer(Ability.SKILL));
		V.addElement(new Integer(Ability.COMMON_SKILL));
		msg.append("\n\r^HYour skills:^? "+getAbilities(mob,V,-1,true)+"\n\r");
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(msg.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
