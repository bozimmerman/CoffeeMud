package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class OutFit extends StdCommand
{
	public OutFit(){}
	
	private String[] access={"OUTFIT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(mob==null) return false;
		if(mob.charStats()==null) return false;
		CharClass C=mob.charStats().getCurrentClass();
		Race R=mob.charStats().getMyRace();
		if(C!=null) C.outfit(mob);
		if(R!=null) R.outfit(mob);
		Command C2=CMClass.getCommand("Equipment");
		if(C2!=null) C2.execute(mob,Util.parse("EQUIPMENT"));
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return false;}
	
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
