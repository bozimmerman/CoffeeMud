package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.PlayerAccount.AccountFlag;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

/*
   Copyright 2004-2024 Bo Zimmerman

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
public class Config extends StdCommand
{
	public Config()
	{
	}

	private final String[] access=I(new String[]{"CONFIG","AUTO","CONFIGURATION"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		String postStr="";
		final int maxAttribLen = 15;
		boolean quieter=false;
		if((commands!=null)
		&&(commands.size()>2)
		&&(commands.get(1).equalsIgnoreCase("QUIET")))
			quieter = commands.remove(1) != null;

		if((commands!=null)&&(commands.size()>1))
		{
			final String name=commands.get(1);
			MOB.Attrib finalA=null;
			for(final MOB.Attrib a : MOB.Attrib.values())
			{
				if(name.equalsIgnoreCase(a.getName()))
					finalA=a;
			}
			if(finalA==null)
			{
				if(name.equalsIgnoreCase("HELP"))
				{
					final String rest=CMParms.combine(commands,2).toUpperCase().trim();
					final List<String> sorted = new ArrayList<String>();
					for(final MOB.Attrib a : MOB.Attrib.values())
					{
						if((a==MOB.Attrib.SYSOPMSGS)&&(!(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.SYSMSGS))))
							continue;
						if((a==MOB.Attrib.AUTOMAP)&&(CMProps.getIntVar(CMProps.Int.AWARERANGE)<=0))
							continue;
						sorted.add(a.getName());
					}
					sorted.add("LINEWRAP");
					sorted.add("PAGEBREAK");
					sorted.add("AUTOTELLNOTIFY");
					Collections.sort(sorted);
					final Object rawHelp=CMLib.help().getHelpFile().get("CONFIG_HELP_OPTIONS");
					if((!(rawHelp instanceof String))||(((String)rawHelp).length()==0))
						mob.tell(L("No help!"));
					else
					{
						final Properties P=new Properties();
						P.load(new ByteArrayInputStream(rawHelp.toString().getBytes()));
						for(final MOB.Attrib a : MOB.Attrib.values())
						{
							int x=a.getName().indexOf(' ');
							if(x>0)
							{
								String val=P.getProperty(a.getName().substring(0,x).trim());
								if(val != null)
								{
									x=val.lastIndexOf('=');
									if(x>0)
										val=val.substring(x+1);
									P.put(a.getName(), val);
								}
							}
						}
						final StringBuilder m=new StringBuilder("");
						for(final String aStr : sorted)
						{
							final String aHelp=P.getProperty(aStr);
							if((aHelp != null)
							&&(aHelp.length()>0)
							&&((rest==null)||(aStr.indexOf(rest)>=0)))
							{
								m.append("^H"+CMStrings.padRight(aStr,maxAttribLen)+"^N: ");
								m.append(aHelp).append("\n\r");
							}
						}
						mob.tell(m.toString());
					}
					return true;
				}
				else
				if(name.equalsIgnoreCase("COPY"))
				{
					final String whomName=(commands.size()>2)?CMParms.combine(commands,2):"";
					if(whomName.length()==0)
						mob.tell(L("Copy whose settings?"));
					else
					if(!CMLib.players().playerExists(whomName))
						mob.tell(L("Player '@x1' doesn't exist.",whomName));
					else
					{
						final boolean unloadAfter = CMLib.players().isLoadedPlayer(whomName);
						final MOB M=CMLib.players().getLoadPlayer(whomName);
						if((M!=null)
						&&(M!=mob)
						&&(!mob.isMonster()))
						{
							try
							{
								final Session sess = mob.session();
								final java.util.List<String> changes = new ArrayList<String>();
								for(final MOB.Attrib a : MOB.Attrib.values())
								{
									if(M.isAttributeSet(a) != mob.isAttributeSet(a))
										changes.add(a.name());
								}
								if(mob.playerStats().getPageBreak()!=M.playerStats().getPageBreak())
									changes.add("PAGEBREAK");
								if(mob.playerStats().getWrap()!=M.playerStats().getWrap())
									changes.add("LINEWRAP");
								final MOB M1=mob;
								final MOB M2=M;
								final Config C=this;
								if(changes.size()==0)
									mob.tell(L("Your configuration already matches @x1s.",M.name()));
								else
								sess.prompt(new InputCallback(InputCallback.Type.CONFIRM,"N",0)
								{
									final Session S=sess;
									final MOB mob=M1;
									final MOB M=M2;
									final Config configC = C;
									@Override
									public void showPrompt()
									{
										S.promptPrint(L("\n\rCopy the config settings '@x1' from player @x2 (y/N)? ",
												CMLib.english().toEnglishStringList(changes),
												M.name()));
									}
									@Override
									public void timedOut()
									{
									}
									@Override
									public void callBack()
									{
										if(this.input.equals("Y"))
										{
											if((mob == null)||(M==null))
												return;
											final PlayerStats opStats =mob.playerStats();
											final PlayerStats mpStats =M.playerStats();
											if((opStats == null)||(mpStats==null))
												return;
											try
											{
												for(final MOB.Attrib a : MOB.Attrib.values())
												{
													if(M.isAttributeSet(a) != mob.isAttributeSet(a))
													{
														final Vector<String> newCmd = new XVector<String>("CONFIG","QUIET",a.getName());
														configC.execute(mob, newCmd, metaFlags);
													}
												}
												if(opStats.getPageBreak()!=mpStats.getPageBreak())
												{
													final Vector<String> newCmd = new XVector<String>("CONFIG","QUIET","PAGEBREAK",""+mpStats.getPageBreak());
													configC.execute(mob, newCmd, metaFlags);
												}
												if(opStats.getWrap()!=mpStats.getWrap())
												{
													final Vector<String> newCmd = new XVector<String>("CONFIG","QUIET","LINEWRAP",""+mpStats.getWrap());
													configC.execute(mob, newCmd, metaFlags);
												}
												mob.tell(L("Configuration copied and active."));
											}
											catch(final IOException e)
											{
												Log.errOut(e);
											}
										}
									}
								});
							}
							finally
							{
								if(unloadAfter
								&&(M!=null)
								&&((M.session()==null)||(M.session().isStopped())))
									CMLib.players().unloadOfflinePlayer(M);
							}
						}
					}
					return true;
				}
				else
				if(name.equalsIgnoreCase("TELNETGA"))
					finalA=MOB.Attrib.TELNET_GA;
				else
				if(name.equalsIgnoreCase("TELNET_GA"))
					finalA=MOB.Attrib.TELNET_GA;
				else
				if(name.equalsIgnoreCase("LIST"))
				{
					final StringBuilder str=new StringBuilder("");
					for(final MOB.Attrib A : MOB.Attrib.values())
						str.append(A.getName()).append(", ");
					postStr=L("Options include: @x1.",str.substring(0,str.length()-2));
					mob.tell(postStr);
					return true;
				}
				else
				if(name.equalsIgnoreCase("LINEWRAP"))
				{
					final String newWrap=(commands.size()>2)?CMParms.combine(commands,2):"";
					int newVal=mob.playerStats().getWrap();
					if((CMath.isInteger(newWrap))&&(CMath.s_int(newWrap)>10))
						newVal=CMath.s_int(newWrap);
					else
					if("DISABLED".startsWith(newWrap.toUpperCase())&&(newWrap.length()>0))
						newVal=0;
					else
					if("OFF".startsWith(newWrap.toUpperCase())&&(newWrap.length()>0))
						newVal=0;
					else
					{
						mob.tell(L("'@x1' is not a valid linewrap setting. Enter a number larger than 10 or 'disable'.",newWrap));
						return false;
					}
					mob.playerStats().setWrap(newVal);
					postStr=L("Configuration option change: LINEWRAP");
				}
				else
				if(name.equalsIgnoreCase("PAGEBREAK"))
				{
					final String newBreak=(commands.size()>2)?CMParms.combine(commands,2):"";
					int newVal=mob.playerStats().getWrap();
					if((CMath.isInteger(newBreak))&&(CMath.s_int(newBreak)>0))
						newVal=CMath.s_int(newBreak);
					else
					if("DISABLED".startsWith(newBreak.toUpperCase())&&(newBreak.length()>0))
						newVal=0;
					else
					{
						mob.tell(L("'@x1' is not a valid pagebreak setting. Enter a number larger than 0 or 'disable'.",newBreak));
						return false;
					}
					mob.playerStats().setPageBreak(newVal);
					postStr=L("Configuration option change: PAGEBREAK");
				}
				else
				if((name.equalsIgnoreCase("AUTOTELLNOTIFY"))
				&&(mob.playerStats()!=null)
				&&(mob.playerStats().getAccount()!=null))
				{
					final String parm=(commands.size()>2)?CMParms.combine(commands,2):"";
					final PlayerAccount acct = mob.playerStats().getAccount();
					if((!acct.isSet(PlayerAccount.AccountFlag.AUTOTELLNOTIFY) && (parm.length()==0))||(parm.equalsIgnoreCase("ON")))
					{
						acct.setFlag(PlayerAccount.AccountFlag.AUTOTELLNOTIFY, true);
						postStr=L("Configuration flag toggled: AUTOTELLNOTIFY");
					}
					else
					if((acct.isSet(PlayerAccount.AccountFlag.AUTOTELLNOTIFY) && (parm.length()==0))||(parm.equalsIgnoreCase("OFF")))
					{
						acct.setFlag(PlayerAccount.AccountFlag.AUTOTELLNOTIFY, false);
						postStr=L("Configuration flag toggled: AUTOTELLNOTIFY");
					}
				}
				else
					postStr=L("Unknown configuration flag '@x1'.",name);
			}
			else
			{
				postStr=L("Configuration flag toggled: "+finalA.getName());
				final boolean newSet = !mob.isAttributeSet(finalA);
				switch(finalA)
				{
				case ANSI:
					if(mob.session() != null)
					{
						mob.session().setClientTelnetMode(Session.TELNET_ANSI,newSet);
						mob.session().setServerTelnetMode(Session.TELNET_ANSI,newSet);
					}
					break;
				case ANSI16:
					if(mob.session() != null)
					{
						mob.session().setClientTelnetMode(Session.TELNET_ANSI16,newSet);
						mob.session().setServerTelnetMode(Session.TELNET_ANSI16,newSet);
					}
					break;
				case AUTOASSIST:
					break;
				case AUTODRAW:
					break;
				case AUTOEXITS:
					break;
				case AUTOFORWARD:
				{
					final PlayerStats pStats = mob.playerStats();
					if((pStats != null)
					&&(pStats.getAccount() != null))
					{
						pStats.getAccount().setFlag(AccountFlag.NOAUTOFORWARD, newSet);
						CMLib.database().DBUpdateAccount(pStats.getAccount());
					}
					break;
				}
				case PRIVACY:
					break;
				case AUTOGOLD:
					break;
				case AUTOGUARD:
					break;
				case AUTOIMPROVE:
					break;
				case AUTOLOOT:
					break;
				case AUTOMAP:
					break;
				case AUTOMELEE:
					break;
				case AUTOATTACK:
					if(newSet)
					{
						if((CMProps.getIntVar(CMProps.Int.COMBATSYSTEM) == CombatLibrary.CombatSystem.TURNBASED.ordinal())
						||(CMProps.getIntVar(CMProps.Int.COMBATSYSTEM) == CombatLibrary.CombatSystem.MANUAL.ordinal()))
						{
							mob.tell(L("Because of the combat system, this config doesn't really apply."));
							return false;
						}
					}
					break;
				case AUTONOTIFY:
					break;
				case AUTORUN:
					break;
				case AUTOWEATHER:
					break;
				case BRIEF:
					break;
				case COMPRESS:
					break;
				case DAILYMESSAGE:
					break;
				case MXP:
					if(mob.session() != null)
					{
						mob.session().changeTelnetMode(Session.TELNET_MXP,newSet);
						mob.session().setClientTelnetMode(Session.TELNET_MXP,newSet);
					}
					break;
				case NOBATTLESPAM:
					break;
				case NOREPROMPT:
					break;
				case NOSPAM:
					break;
				case NOFOLLOW:
					break;
				case NOTEACH:
					break;
				case PLAYERKILL:
					break;
				case QUIET:
					break;
				case SOUND:
					if(mob.session() != null)
					{
						mob.session().changeTelnetMode(Session.TELNET_MSP,newSet);
						mob.session().setClientTelnetMode(Session.TELNET_MSP,newSet);
					}
					break;
				case SYSOPMSGS:
					break;
				case TELNET_GA:
					if(mob.session() != null)
					{
						mob.session().changeTelnetMode(Session.TELNET_GA,newSet);
						mob.session().setClientTelnetMode(Session.TELNET_GA,newSet);
					}
					break;
				default:
					break;
				}
				mob.setAttribute(finalA, newSet);
			}
			//mob.tell(postStr);
		}

		final StringBuffer msg=new StringBuffer(L("^HYour configuration flags:^?\n\r"));
		final List<MOB.Attrib> sorted = new XVector<MOB.Attrib>(MOB.Attrib.values());
		Collections.sort(sorted,new Comparator<MOB.Attrib>()
		{
			@Override
			public int compare(final Attrib o1, final Attrib o2)
			{
				return o1.getName().compareTo(o2.getName());
			}
		});
		final Set<String> xtrasDone = new HashSet<String>();
		int col=0;
		for(final MOB.Attrib a : sorted)
		{
			if((a==MOB.Attrib.SYSOPMSGS)&&(!(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.SYSMSGS))))
				continue;
			if((a==MOB.Attrib.AUTOMAP)&&(CMProps.getIntVar(CMProps.Int.AWARERANGE)<=0))
				continue;

			if(mob.playerStats()!=null)
			{
				if((!xtrasDone.contains("LINEWRAP"))
				&&(a.getName().compareTo("LINEWRAP")>0))
				{
					final String wrap=(mob.playerStats().getWrap()!=0)?(""+mob.playerStats().getWrap()):"Disabled";
					final StringBuilder m=new StringBuilder("^W"+CMStrings.padRight(L("LINEWRAP"),maxAttribLen)+"^N: ^w"+wrap);
					if((mob.session()!=null)&&(mob.playerStats().getWrap() != mob.session().getWrap()))
						m.append(" ("+mob.session().getWrap()+")");
					if(++col==2)
					{
						msg.append(m.toString());
						msg.append("\n\r");
						col=0;
					}
					else
						msg.append(CMStrings.padRight(m.toString(), 40));
					xtrasDone.add("LINEWRAP");
				}
				else
				if((!xtrasDone.contains("PAGEBREAK"))
				&&(a.getName().compareTo("PAGEBREAK")>0))
				{
					final String pageBreak=(mob.playerStats().getPageBreak()!=0)?(""+mob.playerStats().getPageBreak()):"^rDisabled";
					final StringBuilder m=new StringBuilder("^W"+CMStrings.padRight(L("PAGEBREAK"),maxAttribLen)+"^N: ^w"+pageBreak);
					if(++col==2)
					{
						msg.append(m.toString());
						msg.append("\n\r");
						col=0;
					}
					else
						msg.append(CMStrings.padRight(m.toString(), 40));
					xtrasDone.add("PAGEBREAK");
				}
			}


			final StringBuilder m=new StringBuilder("");
			m.append("^W"+CMStrings.padRight(a.getName(),maxAttribLen)+"^N: ");
			boolean set=mob.isAttributeSet(a);
			if(a.isAutoReversed())
				set=!set;
			m.append(set?L("^gON"):L("^rOFF"));
			if(++col==2)
			{
				msg.append(m.toString());
				msg.append("\n\r");
				col=0;
			}
			else
				msg.append(CMStrings.padRight(m.toString(), 40));
		}
		if(CMProps.isUsingAccountSystem()
		&&(mob.playerStats()!=null)
		&&(mob.playerStats().getAccount()!=null))
		{
			final StringBuilder m=new StringBuilder("");
			m.append("^W"+CMStrings.padRight("AUTOTELLNOTIFY",maxAttribLen)+"^N: ");
			final boolean set=mob.playerStats().getAccount().isSet(PlayerAccount.AccountFlag.AUTOTELLNOTIFY);
			m.append(set?L("^gON"):L("^rOFF"));
			if(++col==2)
			{
				msg.append(m.toString());
				msg.append("\n\r");
				col=0;
			}
			else
				msg.append(CMStrings.padRight(m.toString(), 40));
		}

		msg.append("^N");
		msg.append(L("\n\rUse CONFIG HELP (X) for more information.\n\r"));
		if(!quieter)
			mob.tell(msg.toString());
		mob.tell(postStr);
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

}
