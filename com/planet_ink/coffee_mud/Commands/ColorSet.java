package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary.Color;
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
   Copyright 2004-2016 Bo Zimmerman

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
	public ColorSet()
	{
	}

	private final String[]	access	= I(new String[] { "COLORSET" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public String colorDescription(String code)
	{
		final StringBuffer buf=new StringBuffer("");
		String what=CMLib.color().translateANSItoCMCode(code);
		while((what!=null)&&(what.length()>1))
		{
			for(Color C : Color.values())
			{
				if(what.charAt(1)==C.getCodeChar())
				{
					buf.append("^"+C.getCodeChar()+CMStrings.capitalizeAndLower(C.name()));
					break;
				}
			}
			if(what.indexOf('|')>0)
			{
				what=what.substring(what.indexOf('|')+1);
				buf.append(L("^N=background, foreground="));
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
				@Override
				public void showPrompt()
				{
					callBack.showPrompt();
				}

				@Override
				public void timedOut()
				{
					callBack.timedOut();
				}

				@Override 
				public void callBack()
				{
					callBack.setInput("-1");
					if(this.input.length()>0)
					{
						for(int ii=0;ii<set.length;ii++)
						{
							if(Color.values()[ii].name().toUpperCase().startsWith(this.input.toUpperCase()))
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

	public void makeColorChanges(final List<Pair<String,Integer>> theSet, final PlayerStats pstats, final Session session, final String[][] clookup)
	{
		String newChanges="";
		final String[] common=CMLib.color().standardColorLookups();
		for (final Pair<String,Integer> element : theSet)
		{
			final int c=element.second.intValue();
			if(c<128)
			{
				if(!clookup[0][c].equals(common[c]))
					newChanges+=((char)c)+CMLib.color().translateANSItoCMCode(clookup[0][c])+"#";
			}
			else
			{
				if(!clookup[0][c].equals(common['Q']))
					newChanges+="("+c+")"+CMLib.color().translateANSItoCMCode(clookup[0][c])+"#";
			}
		}
		pstats.setColorStr(newChanges);
		clookup[0]=session.getColorCodes().clone();
	}

	@Override
	public boolean execute(final MOB mob, List<String> commands, int metaFlags)
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
			mob.tell(L("Your colors have been changed back to default."));
			return false;
		}
		
		List<String> allExtendedColorsList = new ArrayList<String>();
		List<String> allBasicColorsList = new ArrayList<String>();
		for(Color C : Color.values())
		{
			if(C.isExtendedColor())
			{
				allExtendedColorsList.add(Character.toString(C.getCodeChar()));
				if(C.isBasicColor())
				{
					allBasicColorsList.add(Character.toString(C.getCodeChar()));
				}
			}
		}
		
		final String[] COLOR_ALLEXTENDEDCOLORCODELETTERS = allExtendedColorsList.toArray(new String[allExtendedColorsList.size()]);
		final String[] COLOR_ALLBASICCOLORCODELETTERS = allBasicColorsList.toArray(new String[allBasicColorsList.size()]);
		
		if(clookup[0]==null)
			return false;
		final List<Pair<String,Integer>> theSet= new ArrayList<Pair<String,Integer>>();
		theSet.add(new Pair<String,Integer>("Normal Text",Integer.valueOf('N')));
		theSet.add(new Pair<String,Integer>("Highlighted Text",Integer.valueOf('H')));
		theSet.add(new Pair<String,Integer>("Your Fight Text",Integer.valueOf('f')));
		theSet.add(new Pair<String,Integer>("Fighting You Text",Integer.valueOf('e')));
		theSet.add(new Pair<String,Integer>("Other Fight Text",Integer.valueOf('F')));
		theSet.add(new Pair<String,Integer>("Spells",Integer.valueOf('S')));
		theSet.add(new Pair<String,Integer>("Emotes",Integer.valueOf('E')));
		theSet.add(new Pair<String,Integer>("Says",Integer.valueOf('T')));
		theSet.add(new Pair<String,Integer>("Tells",Integer.valueOf('t')));
		theSet.add(new Pair<String,Integer>("Room Titles",Integer.valueOf('O')));
		theSet.add(new Pair<String,Integer>("Room Descriptions",Integer.valueOf('L')));
		theSet.add(new Pair<String,Integer>("Weather",Integer.valueOf('J')));
		theSet.add(new Pair<String,Integer>("Doors",Integer.valueOf('d')));
		theSet.add(new Pair<String,Integer>("Items",Integer.valueOf('I')));
		theSet.add(new Pair<String,Integer>("MOBs",Integer.valueOf('M')));
		theSet.add(new Pair<String,Integer>("Channel Colors",Integer.valueOf('Q')));
		for(int i=0;i<CMLib.channels().getNumChannels();i++)
		{
			if((clookup[0][128+i]!=null)&&(clookup[0][128+i].length()>0))
				theSet.add(new Pair<String,Integer>(CMLib.channels().getChannelNames()[i],Integer.valueOf(128+i)));
		}

		final InputCallback[] IC=new InputCallback[1];
		IC[0]=new InputCallback(InputCallback.Type.PROMPT,"")
		{
			@Override 
			public void showPrompt()
			{
				final StringBuffer buf=new StringBuffer("");
				for(int i=0;i<theSet.size();i++)
				{
					buf.append("\n\r^H"+CMStrings.padLeft(""+(i+1),2)+"^N) "+CMStrings.padRight(theSet.get(i).first,20)+": ");
					buf.append(colorDescription(clookup[0][theSet.get(i).second.intValue()]));
					buf.append("^N");
				}
				session.println(buf.toString());
				session.promptPrint(L("Enter Number, channel name, or RETURN: "));
			}

			@Override
			public void timedOut()
			{
			}

			@Override public void callBack()
			{
				if(input.trim().length()==0)
					return;
				if(!CMath.isInteger(input.trim()))
				{
					String potChannelName = CMLib.channels().findChannelName(input.trim());
					if(potChannelName != null) 
					{
						int code = CMLib.channels().getChannelIndex(potChannelName);
						if(code >=0)
						{
							Pair<String,Integer> newEntry = null;
							for(int x=0;x<theSet.size();x++)
							{
								final Pair<String,Integer> entry = theSet.get(x);
								if(entry.first.equals(potChannelName))
								{
									newEntry = entry;
									input = ""+(x+1);
								}
							}
							if(newEntry == null)
							{
								newEntry = new Pair<String,Integer>(potChannelName,Integer.valueOf(128+code));
								clookup[0][128+code]=clookup[0]['Q'];
								theSet.add(newEntry);
								input = ""+theSet.size();
							}
						}
					}
				}
				if(input.trim().length()==0)
					return;
				final int num=CMath.s_int(input.trim())-1;
				if((num<0)
				||(num>=theSet.size()))
					mob.tell(L("That is not a valid entry!"));
				else
				{
					final StringBuffer buf=new StringBuffer("");
					buf.append("\n\r\n\r^c"+CMStrings.padLeft(""+(num+1),2)+"^N) "+CMStrings.padRight(theSet.get(num).first,20)+": ");
					final int colorCodeNum = theSet.get(num).second.intValue();
					buf.append(colorDescription(clookup[0][colorCodeNum]));
					if((colorCodeNum!='Q') && (colorCodeNum < 128))
					{
						buf.append(L("^N\n\rAvailable Colors: "));
						for(int ii=0;ii<Color.values().length;ii++)
						{
							Color C = Color.values()[ii];
							if(C.isBasicColor())
							{
								if(ii>0)
									buf.append(", ");
								buf.append("^"+C.getCodeChar()+CMStrings.capitalizeAndLower(C.name()));
							}
						}
						session.println(buf.toString()+"^N");
						pickColor(mob,COLOR_ALLBASICCOLORCODELETTERS,new InputCallback(InputCallback.Type.PROMPT,"")
						{
							@Override
							public void showPrompt()
							{
								session.promptPrint(L("Enter Name of New Color: "));
							}

							@Override
							public void timedOut()
							{
							}

							@Override public void callBack()
							{
								final int colorNum=CMath.s_int(this.input);
								if(colorNum<0)
									mob.tell(L("That is not a valid color!"));
								else
								{
									clookup[0][colorCodeNum]=clookup[0][Color.values()[colorNum].getCodeChar()];
									makeColorChanges(theSet, pstats, session, clookup);
								}
								session.prompt(IC[0].reset());
							}
						});
					}
					else
					{
						buf.append(L("^N\n\r\n\rAvailable Background Colors: "));
						boolean first=true;
						for(Color C : Color.values())
						{
							if(C.isExtendedColor()
							&& Character.isUpperCase(C.getCodeChar()))
							{
								if(first)
									first=false; else buf.append(", ");
								if(C==Color.BLACK)
									buf.append("^"+Color.WHITE.getCodeChar()+CMStrings.capitalizeAndLower(C.name()));
								else
									buf.append("^"+C.getCodeChar()+CMStrings.capitalizeAndLower(C.name()));
							}
						}
						buf.append(L("^N\n\rAvailable Foreground Colors: "));
						first=true;
						for(Color C : Color.values())
						{
							if((C.isBasicColor())
							&&(Character.isLowerCase(C.getCodeChar())))
							{
								if(first)
									first=false; else buf.append(", ");
								buf.append("^"+C.getCodeChar()+CMStrings.capitalizeAndLower(C.name()));
							}
						}
						session.println(buf.toString()+"^N");
						pickColor(mob,COLOR_ALLEXTENDEDCOLORCODELETTERS,new InputCallback(InputCallback.Type.PROMPT,"")
						{
							@Override
							public void showPrompt()
							{
								session.promptPrint(L("Enter Name of Background Color: "));
							}

							@Override
							public void timedOut()
							{
							}

							@Override public void callBack()
							{
								final int colorNum1=CMath.s_int(this.input);
								if((colorNum1<0)||(!Character.isUpperCase(Color.values()[colorNum1].getCodeChar())))
								{
									mob.tell(L("That is not a valid Background color!"));
									session.prompt(IC[0].reset());
								}
								else
								{
									pickColor(mob,COLOR_ALLBASICCOLORCODELETTERS,new InputCallback(InputCallback.Type.PROMPT,"")
									{
										@Override
										public void showPrompt()
										{
											session.promptPrint(L("Enter Name of Foreground Color: "));
										}

										@Override
										public void timedOut()
										{
										}

										@Override public void callBack()
										{
											final int colorNum2=CMath.s_int(this.input);
											if((colorNum2<0)||(Character.isUpperCase(Color.values()[colorNum2].getCodeChar())))
												mob.tell(L("That is not a valid Foreground color!"));
											else
											{
												clookup[0][colorCodeNum]=CMLib.color().translateCMCodeToANSI("^"+Color.values()[colorNum1].getCodeChar()+"|^"+Color.values()[colorNum2].getCodeChar());
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

	@Override public boolean canBeOrdered(){return true;}


}
