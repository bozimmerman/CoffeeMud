package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2014 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class ColorSet extends StdCommand
{
	public ColorSet(){}

	private final String[] access={"COLORSET"};
	public String[] getAccessWords(){return access;}
	
	public String colorDescription(String code)
	{
		StringBuffer buf=new StringBuffer("");
		String what=CMLib.color().translateANSItoCMCode(code);
		while((what!=null)&&(what.length()>1))
		{
			for(int ii=0;ii<ColorLibrary.COLOR_ALLEXTENDEDCOLORCODELETTERS.length;ii++)
				if(what.charAt(1)==ColorLibrary.COLOR_ALLEXTENDEDCOLORCODELETTERS[ii].charAt(0))
				{
					buf.append("^"+ColorLibrary.COLOR_ALLEXTENDEDCOLORCODELETTERS[ii]+CMStrings.capitalizeAndLower(ColorLibrary.COLOR_ALLCOLORNAMES[ii]));
					break;
				}
			if(what.indexOf('|')>0)
			{
				what=what.substring(what.indexOf('|')+1);
				buf.append("^N=background, foreground=");
			}
			else
				what=null;
		}
		return buf.toString();
	}

	protected void pickColor(final MOB mob, final String[] set, final InputCallback callBack)
	{
		if(mob.session()!=null)
		{
			mob.session().prompt(new InputCallback(InputCallback.Type.PROMPT,"")
			{
				@Override public void showPrompt() { callBack.showPrompt(); }
				@Override public void timedOut() { callBack.timedOut(); }
				@Override public void callBack()
				{
					callBack.setInput("-1");
					if(this.input.length()>0)
					{
						for(int ii=0;ii<set.length;ii++)
						{
							if(ColorLibrary.COLOR_ALLCOLORNAMES[ii].toUpperCase().startsWith(this.input.toUpperCase()))
							{
								callBack.setInput(""+ii);
								break;
							}
						}
					}
					callBack.callBack();
				}
			});
		}
	}

	public void makeColorChanges(final String[][] theSet, final PlayerStats pstats, final Session session, final String[][] clookup)
	{
		String newChanges="";
		String[] common=CMLib.color().standardColorLookups();
		for(int i=0;i<theSet.length;i++)
		{
			char c=theSet[i][1].charAt(0);
			if(!clookup[0][c].equals(common[c]))
				newChanges+=c+CMLib.color().translateANSItoCMCode(clookup[0][c])+"#";
		}
		pstats.setColorStr(newChanges);
		clookup[0]=session.getColorCodes().clone();
	}
	
	public boolean execute(final MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		final Session session=mob.session();
		if(session==null) 
			return false;
		final PlayerStats pstats=mob.playerStats();
		if(pstats==null) 
			return false;
		final String[][] clookup=new String[][]{session.getColorCodes().clone()};
		if((commands.size()>1)
		&&("DEFAULT".startsWith(CMParms.combine(commands,1).toUpperCase())))
		{
			pstats.setColorStr("");
			mob.tell("Your colors have been changed back to default.");
			return false;
		}
		if(clookup[0]==null) 
			return false;
		final
		String[][] theSet={{"Normal Text","N"},
						   {"Highlighted Text","H"},
						   {"Your Fight Text","f"},
						   {"Fighting You Text","e"},
						   {"Other Fight Text","F"},
						   {"Spells","S"},
						   {"Emotes","E"},
						   {"Says","T"},
						   {"Tells","t"},
						   {"Room Titles","O"},
						   {"Room Descriptions","L"},
						   {"Weather","J"},
						   {"Doors","d"},
						   {"Items","I"},
						   {"MOBs","M"},
						   {"Channel Colors","Q"}
		};
		
		final InputCallback[] IC=new InputCallback[1];
		IC[0]=new InputCallback(InputCallback.Type.PROMPT,"")
		{
			@Override public void showPrompt() 
			{
				StringBuffer buf=new StringBuffer("");
				for(int i=0;i<theSet.length;i++)
				{
					buf.append("\n\r^H"+CMStrings.padLeft(""+(i+1),2)+"^N) "+CMStrings.padRight(theSet[i][0],20)+": ");
					buf.append(colorDescription(clookup[0][theSet[i][1].charAt(0)]));
					buf.append("^N");
				}
				session.println(buf.toString());
				session.promptPrint("Enter Number or RETURN: ");
			}
			@Override public void timedOut() {}
			@Override public void callBack() 
			{
				if(input.trim().length()==0) 
					return;
				final int num=CMath.s_int(input.trim())-1;
				if(input.trim().length()==0) 
					return;
				if((num<0)||(num>=theSet.length))
					mob.tell("That is not a valid entry!");
				else
				{
					StringBuffer buf=new StringBuffer("");
					buf.append("\n\r\n\r^c"+CMStrings.padLeft(""+(num+1),2)+"^N) "+CMStrings.padRight(theSet[num][0],20)+": ");
					buf.append(colorDescription(clookup[0][theSet[num][1].charAt(0)]));
					if(theSet[num][1].charAt(0)!='Q')
					{
						buf.append("^N\n\rAvailable Colors: ");
						for(int ii=0;ii<ColorLibrary.COLOR_ALLNORMALCOLORCODELETTERS.length;ii++)
						{
							if(ii>0) buf.append(", ");
							buf.append("^"+ColorLibrary.COLOR_ALLNORMALCOLORCODELETTERS[ii]+CMStrings.capitalizeAndLower(ColorLibrary.COLOR_ALLCOLORNAMES[ii]));
						}
						session.println(buf.toString()+"^N");
						pickColor(mob,ColorLibrary.COLOR_ALLNORMALCOLORCODELETTERS,new InputCallback(InputCallback.Type.PROMPT,"")
						{
							@Override public void showPrompt() { session.promptPrint("Enter Name of New Color: "); }
							@Override public void timedOut() { }
							@Override public void callBack()
							{
								int colorNum=CMath.s_int(this.input);
								if(colorNum<0)
									mob.tell("That is not a valid color!");
								else
								{
									clookup[0][theSet[num][1].charAt(0)]=clookup[0][ColorLibrary.COLOR_ALLNORMALCOLORCODELETTERS[colorNum].charAt(0)];
									makeColorChanges(theSet, pstats, session, clookup);
								}
								session.prompt(IC[0].reset());
							}
						});
					}
					else
					{
						buf.append("^N\n\r\n\rAvailable Background Colors: ");
						boolean first=true;
						for(int ii=0;ii<ColorLibrary.COLOR_ALLEXTENDEDCOLORCODELETTERS.length;ii++)
							if(Character.isUpperCase(ColorLibrary.COLOR_ALLEXTENDEDCOLORCODELETTERS[ii].charAt(0)))
							{
								if(first)first=false; else buf.append(", ");
								if(ColorLibrary.COLOR_ALLEXTENDEDCOLORCODELETTERS[ii]==ColorLibrary.COLOR_BLACK)
									buf.append("^"+ColorLibrary.COLOR_WHITE+CMStrings.capitalizeAndLower(ColorLibrary.COLOR_ALLCOLORNAMES[ii]));
								else
									buf.append("^"+ColorLibrary.COLOR_ALLEXTENDEDCOLORCODELETTERS[ii]+CMStrings.capitalizeAndLower(ColorLibrary.COLOR_ALLCOLORNAMES[ii]));
							}
						buf.append("^N\n\rAvailable Foreground Colors: ");
						first=true;
						for(int ii=0;ii<ColorLibrary.COLOR_ALLNORMALCOLORCODELETTERS.length;ii++)
							if(Character.isLowerCase(ColorLibrary.COLOR_ALLNORMALCOLORCODELETTERS[ii].charAt(0)))
							{
								if(first)first=false; else buf.append(", ");
								buf.append("^"+ColorLibrary.COLOR_ALLNORMALCOLORCODELETTERS[ii]+CMStrings.capitalizeAndLower(ColorLibrary.COLOR_ALLCOLORNAMES[ii]));
							}
						session.println(buf.toString()+"^N");
						pickColor(mob,ColorLibrary.COLOR_ALLEXTENDEDCOLORCODELETTERS,new InputCallback(InputCallback.Type.PROMPT,"")
						{
							@Override public void showPrompt() { session.promptPrint("Enter Name of Background Color: "); }
							@Override public void timedOut() { }
							@Override public void callBack()
							{
								final int colorNum1=CMath.s_int(this.input);
								if((colorNum1<0)||(!Character.isUpperCase(ColorLibrary.COLOR_ALLEXTENDEDCOLORCODELETTERS[colorNum1].charAt(0))))
								{
									mob.tell("That is not a valid Background color!");
									session.prompt(IC[0].reset());
								}
								else
								{
									pickColor(mob,ColorLibrary.COLOR_ALLNORMALCOLORCODELETTERS,new InputCallback(InputCallback.Type.PROMPT,"")
									{
										@Override public void showPrompt() { session.promptPrint("Enter Name of Foreground Color: "); }
										@Override public void timedOut() { }
										@Override public void callBack()
										{
											int colorNum2=CMath.s_int(this.input);
											if((colorNum2<0)||(Character.isUpperCase(ColorLibrary.COLOR_ALLNORMALCOLORCODELETTERS[colorNum2].charAt(0))))
												mob.tell("That is not a valid Foreground color!");
											else
											{
												clookup[0][theSet[num][1].charAt(0)]=CMLib.color().translateCMCodeToANSI("^"+ColorLibrary.COLOR_ALLEXTENDEDCOLORCODELETTERS[colorNum1]+"|^"+ColorLibrary.COLOR_ALLNORMALCOLORCODELETTERS[colorNum2]);
												makeColorChanges(theSet, pstats, session, clookup);
											}
											session.prompt(IC[0].reset());
										}
									});
								}
							}
						});
					}
				}
			}
		};
		
		session.prompt(IC[0]);
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
