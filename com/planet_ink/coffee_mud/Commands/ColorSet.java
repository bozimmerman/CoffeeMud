package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class ColorSet extends StdCommand
{
	public ColorSet(){}

	private String[] access={"COLORSET"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(mob.session()==null) return false;
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return false;
		String[] clookup=(String[])mob.session().clookup().clone();
		if((commands.size()>1)
		   &&("DEFAULT".startsWith(Util.combine(commands,1).toUpperCase())))
		{
			pstats.setColorStr("");
			mob.tell("Your colors have been changed back to default.");
			return false;
		}
		if(clookup==null) return false;
		String[][] theSet={{"Normal Text","N"},
						   {"Highlighted Text","H"},
						   {"Fight Text","F"},
						   {"Spells","S"},
						   {"Emotes","E"},
						   {"Says","T"},
						   {"Tells","t"},
						   {"Room Titles","O"},
						   {"Room Descriptions","L"},
						   {"Doors","d"},
						   {"Items","I"},
						   {"MOBs","M"},
						   {"Channel Foreground","q"}
		};
		String[][] theColors={{"White","w"},
							  {"Green","g"},
							  {"Blue","b"},
							  {"Red","r"},
							  {"Yellow","y"},
							  {"Cyan","c"},
							  {"Purple","p"},
							  {"Grey","W"},
							  {"Dark Green","G"},
							  {"Dark Blue","B"},
							  {"Dark Red","R"},
							  {"Dark Yellow","Y"},
							  {"Dark Cyan","C"},
							  {"Dark Purple","P"}};
		String numToChange="!";
		while(numToChange.length()>0)
		{
			StringBuffer buf=new StringBuffer("");
			for(int i=0;i<theSet.length;i++)
			{
				buf.append("\n\r^H"+Util.padLeft(""+(i+1),2)+"^N) "+Util.padRight(theSet[i][0],20)+": ");
				String what=clookup[theSet[i][1].charAt(0)];
				if(what!=null)
				for(int ii=0;ii<theColors.length;ii++)
					if(what.equals(clookup[theColors[ii][1].charAt(0)]))
						buf.append("^"+theColors[ii][1]+theColors[ii][0]);
				buf.append("^N");
			}
			mob.session().println(buf.toString());
			numToChange=mob.session().prompt("Enter Number or RETURN: ","");
			int num=Util.s_int(numToChange);
			if(numToChange.length()==0) break;
			if((num<=0)||(num>theSet.length))
				mob.tell("That is not a valid entry!");
			else
			{
				num--;
				buf=new StringBuffer("");
				buf.append("\n\r^c"+Util.padLeft(""+(num+1),2)+"^N)"+Util.padRight(theSet[num][0],20)+":");
				String what=clookup[theSet[num][1].charAt(0)];
				if(what!=null)
				for(int ii=0;ii<theColors.length;ii++)
					if(what.equals(clookup[theColors[ii][1].charAt(0)]))
						buf.append("^"+theColors[ii][1]+theColors[ii][0]);
				buf.append("^N\n\rAvailable Colors:");
				for(int ii=0;ii<theColors.length;ii++)
					buf.append("\n\r^"+theColors[ii][1]+theColors[ii][0]);
				mob.session().println(buf.toString()+"^N");
				String newColor=mob.session().prompt("Enter Name of New Color: ","");
				if(newColor.length()>0)
				{
					int colorNum=-1;
					for(int ii=0;ii<theColors.length;ii++)
						if(theColors[ii][0].toUpperCase().startsWith(newColor.toUpperCase()))
						{
							colorNum=ii; break;
						}
					if(colorNum<0)
						mob.tell("That is not a valid color!");
					else
					{
						clookup[theSet[num][1].charAt(0)]=clookup[theColors[colorNum][1].charAt(0)];
						String newChanges="";
						String[] common=CommonStrings.standardColorLookups();
						for(int i=0;i<theSet.length;i++)
						{
							char c=theSet[i][1].charAt(0);
							if(!clookup[c].equals(common[c]))
								for(int ii=0;ii<theColors.length;ii++)
									if(common[theColors[ii][1].charAt(0)].equals(clookup[c]))
									{
										newChanges+=c+"^"+theColors[ii][1]+"#";
										break;
									}
						}
						pstats.setColorStr(newChanges);
						clookup=(String[])mob.session().clookup().clone();
					}
				}
			}
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
