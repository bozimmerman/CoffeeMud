package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ATopics extends StdCommand
{
	public ATopics(){}

	private String[] access={"ARCTOPICS","ATOPICS"};
	public String[] getAccessWords(){return access;}

	public static void doTopics(MOB mob, Properties rHelpFile, String helpName, String resName)
	{
		StringBuffer topicBuffer=(StringBuffer)Resources.getResource(resName);
		if(topicBuffer==null)
		{
			topicBuffer=new StringBuffer();

			Vector reverseList=new Vector();
			for(Enumeration e=rHelpFile.keys();e.hasMoreElements();)
			{
				String ptop = (String)e.nextElement();
				String thisTag=rHelpFile.getProperty(ptop);
				if ((thisTag==null)||(thisTag.length()==0)||(thisTag.length()>=35)
					|| (rHelpFile.getProperty(thisTag)== null) )
						reverseList.addElement(ptop);
			}

			Collections.sort((java.util.List)reverseList);
			topicBuffer=new StringBuffer("Help topics: \n\r\n\r");
			topicBuffer.append(CMLister.fourColumns(reverseList));
			topicBuffer=new StringBuffer(topicBuffer.toString().replace('_',' '));
			Resources.submitResource(resName,topicBuffer);
		}
		if((topicBuffer!=null)&&(mob!=null)&&(!mob.isMonster()))
			mob.session().rawPrintln(topicBuffer.toString()+"\n\r\n\rEnter "+helpName+" (TOPIC NAME) for more information.",23);
	}


	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isASysOp(mob.location()))
		{
			mob.tell("You are not powerful enough to care.");
			return false;
		}
		Properties arcHelpFile=MUDHelp.getArcHelpFile();
		if(arcHelpFile.size()==0)
		{
			if(mob!=null)
				mob.tell("No archon help is available.");
			return false;
		}

		doTopics(mob,arcHelpFile,"AHELP", "ARCHON TOPICS");
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean arcCommand(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
