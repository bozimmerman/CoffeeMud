package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
	
	public String colorDescription(String code)
	{
	    StringBuffer buf=new StringBuffer("");
		String what=CMColor.translateANSItoCMCode(code);
		while((what!=null)&&(what.length()>1))
		{
			for(int ii=0;ii<CMColor.COLOR_ALLEXTENDEDCOLORCODELETTERS.length;ii++)
				if(what.charAt(1)==CMColor.COLOR_ALLEXTENDEDCOLORCODELETTERS[ii].charAt(0))
				{
					buf.append("^"+CMColor.COLOR_ALLEXTENDEDCOLORCODELETTERS[ii]+Util.capitalizeAndLower(CMColor.COLOR_ALLCOLORNAMES[ii]));
					break;
				}
		    if(what.indexOf("|")>0)
		    {
		        what=what.substring(what.indexOf("|")+1);
		        buf.append("^N=background, foreground=");
		    }
		    else
		        what=null;
		}
		return buf.toString();
	}

	private int pickColor(MOB mob, String[] set, String prompt)
	throws java.io.IOException
	{
		String newColor=mob.session().prompt(prompt,"");
		int colorNum=-1;
		if(newColor.length()>0)
		{
			colorNum=-1;
			for(int ii=0;ii<set.length;ii++)
				if(CMColor.COLOR_ALLCOLORNAMES[ii].toUpperCase().startsWith(newColor.toUpperCase()))
				{
					colorNum=ii; 
					break;
				}
		}
		return colorNum;
	}
	
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
						   {"Channel Colors","Q"}
		};
		String numToChange="!";
		while(numToChange.length()>0)
		{
			StringBuffer buf=new StringBuffer("");
			for(int i=0;i<theSet.length;i++)
			{
				buf.append("\n\r^H"+Util.padLeft(""+(i+1),2)+"^N) "+Util.padRight(theSet[i][0],20)+": ");
				buf.append(colorDescription(clookup[theSet[i][1].charAt(0)]));
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
				buf.append("\n\r\n\r^c"+Util.padLeft(""+(num+1),2)+"^N) "+Util.padRight(theSet[num][0],20)+": ");
				buf.append(colorDescription(clookup[theSet[num][1].charAt(0)]));
				boolean changes=false;
				if(theSet[num][1].charAt(0)!='Q')
				{
					buf.append("^N\n\rAvailable Colors: ");
					for(int ii=0;ii<CMColor.COLOR_ALLNORMALCOLORCODELETTERS.length;ii++)
					{
					    if(ii>0) buf.append(", ");
						buf.append("^"+CMColor.COLOR_ALLNORMALCOLORCODELETTERS[ii]+Util.capitalizeAndLower(CMColor.COLOR_ALLCOLORNAMES[ii]));
					}
					mob.session().println(buf.toString()+"^N");
					int colorNum=pickColor(mob,CMColor.COLOR_ALLNORMALCOLORCODELETTERS,"Enter Name of New Color: ");
					if(colorNum<0)
						mob.tell("That is not a valid color!");
					else
					{
						clookup[theSet[num][1].charAt(0)]=clookup[CMColor.COLOR_ALLNORMALCOLORCODELETTERS[colorNum].charAt(0)];
						changes=true;
					}
				}
				else
				{
					buf.append("^N\n\r\n\rAvailable Background Colors: ");
					boolean first=true;
					for(int ii=0;ii<CMColor.COLOR_ALLEXTENDEDCOLORCODELETTERS.length;ii++)
					    if(Character.isUpperCase(CMColor.COLOR_ALLEXTENDEDCOLORCODELETTERS[ii].charAt(0)))
						{
						    if(first)first=false; else buf.append(", ");
						    if(CMColor.COLOR_ALLEXTENDEDCOLORCODELETTERS[ii]==CMColor.COLOR_BLACK)
								buf.append("^"+CMColor.COLOR_WHITE+Util.capitalizeAndLower(CMColor.COLOR_ALLCOLORNAMES[ii]));
						    else
								buf.append("^"+CMColor.COLOR_ALLEXTENDEDCOLORCODELETTERS[ii]+Util.capitalizeAndLower(CMColor.COLOR_ALLCOLORNAMES[ii]));
						}
					buf.append("^N\n\rAvailable Foreground Colors: ");
					first=true;
					for(int ii=0;ii<CMColor.COLOR_ALLNORMALCOLORCODELETTERS.length;ii++)
					    if(Character.isLowerCase(CMColor.COLOR_ALLNORMALCOLORCODELETTERS[ii].charAt(0)))
						{
						    if(first)first=false; else buf.append(", ");
							buf.append("^"+CMColor.COLOR_ALLNORMALCOLORCODELETTERS[ii]+Util.capitalizeAndLower(CMColor.COLOR_ALLCOLORNAMES[ii]));
						}
					mob.session().println(buf.toString()+"^N");
					int colorNum1=pickColor(mob,CMColor.COLOR_ALLEXTENDEDCOLORCODELETTERS,"Enter Name of Background Color: ");
					if((colorNum1<0)||(!Character.isUpperCase(CMColor.COLOR_ALLEXTENDEDCOLORCODELETTERS[colorNum1].charAt(0))))
						mob.tell("That is not a valid Background color!");
					else
					{
						int colorNum2=pickColor(mob,CMColor.COLOR_ALLNORMALCOLORCODELETTERS,"Enter Name of Foreground Color: ");
						if((colorNum2<0)||(Character.isUpperCase(CMColor.COLOR_ALLNORMALCOLORCODELETTERS[colorNum2].charAt(0))))
							mob.tell("That is not a valid Foreground color!");
						else
						{
						    changes=true;
						    clookup[theSet[num][1].charAt(0)]=CMColor.translateCMCodeToANSI("^"+CMColor.COLOR_ALLEXTENDEDCOLORCODELETTERS[colorNum1]+"|^"+CMColor.COLOR_ALLNORMALCOLORCODELETTERS[colorNum2]);
						}
					}
				}
				if(changes)
				{
					String newChanges="";
					String[] common=CMColor.standardColorLookups();
					for(int i=0;i<theSet.length;i++)
					{
						char c=theSet[i][1].charAt(0);
						if(!clookup[c].equals(common[c]))
							newChanges+=c+CMColor.translateANSItoCMCode(clookup[c])+"#";
					}
					pstats.setColorStr(newChanges);
					clookup=(String[])mob.session().clookup().clone();
				}
			}
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
