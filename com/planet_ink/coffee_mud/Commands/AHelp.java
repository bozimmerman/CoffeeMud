package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

public class AHelp extends StdCommand
{
	public AHelp(){}

	private String[] access={"ARCHELP","AHELP"};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String helpStr=Util.combine(commands,1);
		if(MUDHelp.getArcHelpFile().size()==0)
		{
			mob.tell("No archon help is available.");
			return false;
		}
		StringBuffer thisTag=null;
		if(helpStr.length()==0)
		{
			thisTag=Resources.getFileResource("help"+File.separatorChar+"arc_help.txt");
			if((thisTag!=null)&&(helpStr.equalsIgnoreCase("more")))
			{
				StringBuffer theRest=(StringBuffer)Resources.getResource("arc_help.therest");
				if(theRest==null)
				{
					Vector V=new Vector();
					theRest=new StringBuffer("\n\rProperties:\n\r");
					for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
					{
						Ability A=(Ability)a.nextElement();
						if((A!=null)&&((A.classificationCode()&Ability.ALL_CODES)==Ability.PROPERTY))
							V.addElement(A.ID());
					}
					theRest.append(CMLister.fourColumns(V));
					V=new Vector();
					theRest=new StringBuffer("\n\rDiseases:\n\r");
					for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
					{
						Ability A=(Ability)a.nextElement();
						if((A!=null)&&((A.classificationCode()&Ability.ALL_CODES)==Ability.DISEASE))
							V.addElement(A.ID());
					}
					theRest.append(CMLister.fourColumns(V));
					theRest=new StringBuffer("\n\rPoisons:\n\r");
					for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
					{
						Ability A=(Ability)a.nextElement();
						if((A!=null)&&((A.classificationCode()&Ability.ALL_CODES)==Ability.POISON))
							V.addElement(A.ID());
					}
					theRest.append(CMLister.fourColumns(V));
					theRest.append("\n\r\n\rBehaviors:\n\r");
					V=new Vector();
					for(Enumeration b=CMClass.behaviors();b.hasMoreElements();)
					{
						Behavior B=(Behavior)b.nextElement();
						if(B!=null) V.addElement(B.ID());
					}
					theRest.append(CMLister.fourColumns(V)+"\n\r");
					Resources.submitResource("arc_help.therest",theRest);
				}
				thisTag=new StringBuffer(thisTag.toString());
				thisTag.append(theRest);
			}
		}
		else
			thisTag=MUDHelp.getHelpText(helpStr,MUDHelp.getArcHelpFile(),mob);
		if(thisTag==null)
		{
			mob.tell("No archon help is available on '"+helpStr+"'.\nEnter 'COMMANDS' for a command list, or 'TOPICS' for a complete list.");
			Log.errOut("Help",mob.Name()+" wanted archon help on "+helpStr);
		}
		else
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(thisTag.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"AHELP");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
