package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.IOException;

public class Poof extends BaseGenerics
{
	public Poof(){}

	private String[] access={"POOF"};
	public String[] getAccessWords(){return access;}

	public boolean errorOut(MOB mob)
	{
		mob.tell("You are not allowed to do that here.");
		return false;
	}
	
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		int showFlag=-1;
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		boolean ok=false;
		while((!ok)&&(mob.playerStats()!=null))
		{
			int showNumber=0;
			String poofIn=genText(mob,mob.playerStats().poofIn(),++showNumber,showFlag,"Poof-in");
			String poofOut=genText(mob,mob.playerStats().poofIn(),++showNumber,showFlag,"Poof-out");
			mob.playerStats().setPoofs(poofIn,poofOut);
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedStartsWith(mob,mob.location(),"GOTO");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
