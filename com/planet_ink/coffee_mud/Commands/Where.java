package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.MQLException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.UpdateSet;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.regex.Pattern;

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
public class Where extends StdCommand
{
	public Where()
	{
	}

	private final String[]	access	= I(new String[] { "WHERE" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	protected void whereAdd(final PairList<Area,Integer> V, final Area area, final int i)
	{
		if(V.containsFirst(area))
			return;

		for(int v=0;v<V.size();v++)
		{
			if(V.get(v).second.intValue()>i)
			{
				V.add(v,area,Integer.valueOf(i));
				return;
			}
		}
		V.add(area,Integer.valueOf(i));
	}

	public boolean canShowTo(final MOB showTo, final MOB show)
	{
		if((show!=null)
		&&(show.session()!=null)
		&&(showTo!=null)
		&&(((show.phyStats().disposition()&PhyStats.IS_CLOAKED)==0)
			||((CMSecurity.isAllowedAnywhere(showTo,CMSecurity.SecFlag.CLOAK)||CMSecurity.isAllowedAnywhere(showTo,CMSecurity.SecFlag.WIZINV))
				&&(showTo.phyStats().level()>=show.phyStats().level()))))
			return true;
		return false;
	}

	public String cataMark(final Environmental E)
	{
		if(E==null)
			return "";
		if(CMLib.catalog().isCatalogObj(E))
			return "^g";
		return "";
	}

	public String getRoomDesc(final MOB mob, final Room R, final Environmental E)
	{
		if((mob!=null)
		&&(mob.location()==R)
		&&(E instanceof Item)
		&&(((Item)E).container()!=null))
		{
			String msg = "^H*HERE*^?";
			Container C=((Item)E).container();
			int tries=99;
			while((C!=null)&&((--tries)>0))
			{
				msg += "@"+C.name();
				C=C.container();
			}
			return msg;
		}
		return CMLib.map().getDescriptiveExtendedRoomID(R);
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		boolean overrideSet = false;
		if((commands.size()>1)&&(commands.get(1).equals("!")))
			overrideSet=commands.remove(commands.get(1));

		final int firstColWidth = CMLib.lister().fixColWidth(25,mob);
		if((CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.WHERE))
		&&(!overrideSet))
		{
			final StringBuffer lines=new StringBuffer("^x");
			lines.append(CMStrings.padRight(L("Name"),firstColWidth)+"| ");
			lines.append(CMStrings.padRight(L("Location"),17)+"^.^N\n\r");
			String who=CMParms.combineQuoted(commands,1);
			if(who.length()==0)
			{
				for(final Session S : CMLib.sessions().localOnlineIterable())
				{
					final MOB mob2=S.mob();
					if(canShowTo(mob,mob2))
					{
						final Room R=mob2.location();
						if(CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.WHERE))
						{
							lines.append("^N^!"+CMStrings.padRight(mob2.Name(),firstColWidth)+"^N| ");
							if(R != null )
							{
								lines.append(R.displayText(mob));
								lines.append(" (^<LSTROOMID^>"+getRoomDesc(mob,R,mob)+"^</LSTROOMID^>)");
							}
							else
								lines.append("^!(no location)^?");
							lines.append("^N\n\r");
						}
					}
					else
					{
						lines.append(CMStrings.padRight(L("NAMELESS"),firstColWidth)+"| ");
						lines.append("NOWHERE");
						lines.append("^N\n\r");
					}
				}
			}
			else
			{
				boolean areaFlag=false;
				if(who.toUpperCase().startsWith("AREA "))
				{
					areaFlag=true;
					who=who.substring(5).trim();
				}
				if(who.toUpperCase().startsWith("SELECT:"))
				{
					lines.setLength(0);
					try
					{
						final List<Map<String,Object>> res=CMLib.percolator().doMQLSelectObjects(areaFlag?(mob.location().getArea()):null, who);
						if(res.size()==0)
							lines.append("(empty set)");
						else
						{
							for(int line=0;line<res.size();line++)
							{
								lines.append("----- Row #"+line+"\n\r");
								for(final String key : res.get(line).keySet())
								{
									final Object o=res.get(line).get(key);
									if(o instanceof String)
										lines.append("     "+CMStrings.padRight(key, 10)+": "+o.toString()+"\n\r");
									else
									if(o instanceof Environmental)
									{
										final Environmental E=(Environmental)o;
										final Room R=CMLib.map().roomLocation(E);
										final String loc=(R==null)?"":("@"+CMLib.map().getApproximateExtendedRoomID(R));
										lines.append("    "+CMStrings.padRight(E.ID(), 10)+": "+E.name()+loc+"\n\r");
									}
									else
									if((o instanceof List)
									&&(((List<?>)o).size()==2)
									&&(((List<?>)o).get(0) instanceof Modifiable)
									&&(((List<?>)o).get(1) instanceof String))
									{
										final Modifiable M = (Modifiable)((List<?>)o).get(0);
										final String parm = (String)((List<?>)o).get(1);
										final String parmName = (key.equals(".")?parm:key);
										lines.append("     "+CMStrings.padRight(parmName, 10)+": "+M.getStat(parm)+"\n\r");
									}
									else
										lines.append("     "+CMStrings.padRight(key, 10)+": "+o.toString()+"\n\r");
								}
							}
						}
					}
					catch(final MQLException e)
					{
						final ByteArrayOutputStream bout=new ByteArrayOutputStream();
						final PrintStream pw=new PrintStream(bout);
						e.printStackTrace(pw);
						pw.flush();
						lines.append(e.getMessage()+"\n\r"+bout.toString());
					}
				}
				else
				if(who.toUpperCase().startsWith("UPDATE:"))
				{
					lines.setLength(0);
					try
					{
						final List<UpdateSet> res=CMLib.percolator().doMQLUpdateObjects(areaFlag?(mob.location().getArea()):null, who);
						if(res.size()==0)
							lines.append("(nothing to do)");
						else
						{
							lines.append("Update preview:\n\r");
							for(final UpdateSet o : res)
							{
								if(o.first instanceof Environmental)
									lines.append(o.first.name()+" ("+o.first.ID()+") @"+CMLib.map().getApproximateExtendedRoomID(CMLib.map().roomLocation((Environmental)o.first))+":\n\r");
								else
									lines.append(o.first.name()+" ("+o.first.ID()+"):\n\r");
								lines.append("  OLD: "+o.second+"="+o.first.getStat(o.second)).append("\n\r");
								lines.append("  NEW: "+o.second+"="+o.third).append("\n\r");
							}
							final Runnable doUpdate = new Runnable()
							{
								final List<UpdateSet> todo=res;

								@Override
								public void run()
								{
									for(final UpdateSet o : todo)
									{
										o.first.setStat(o.second, o.third);
										if(o.first instanceof Environmental)
										{
											Environmental E=(Environmental)o.first;
											final Room R=CMLib.map().roomLocation(E);
											if((R!=null) && R.isSavable() && (R.roomID().length()>0))
											{
												Log.infoOut(mob.name()+" modified "+E.name()+" at "+R.roomID());
												if(E instanceof Ability)
													E=((Ability)E).affecting();
												if(E instanceof Room)
													CMLib.database().DBUpdateRoom(R);
												else
												if(E instanceof Item)
												{
													final Item I=(Item)E;
													if((I.owner() instanceof Room)
													&&(I.databaseID().length()>0))
														CMLib.database().DBUpdateItem(R.roomID(), I);
													else
													if((I.owner() instanceof MOB)
													&&(((MOB)I.owner()).databaseID().length()>0)
													&&(((MOB)I.owner()).getStartRoom()!=null)
													&&(((MOB)I.owner()).getStartRoom().roomID().length()>0))
														CMLib.database().DBUpdateMOB(((MOB)I.owner()).getStartRoom().roomID(), (MOB)I.owner());
												}
												else
												if((E instanceof MOB)
												&&(((MOB)E).databaseID().length()>0))
													CMLib.database().DBUpdateMOB(R.roomID(), (MOB)E);
											}
										}
									}
								}
							};
							final Session session = mob.session();
							if(session!=null)
							{
								final InputCallback callBack = new InputCallback(InputCallback.Type.CONFIRM,"N",0)
								{
									@Override
									public void showPrompt()
									{
										session.promptPrint(L("\n\rSave the above changes (y/N)? "));
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
											doUpdate.run();
										}
									}
								};
								session.wraplessPrintln(lines.toString());
								lines.setLength(0);
								session.prompt(callBack);
							}
						}
					}
					catch(final MQLException e)
					{
						final ByteArrayOutputStream bout=new ByteArrayOutputStream();
						final PrintStream pw=new PrintStream(bout);
						e.printStackTrace(pw);
						pw.flush();
						lines.append(e.getMessage()+"\n\r"+bout.toString());
					}
				}
				else
				{
					boolean mobOnly=false;
					boolean itemOnly=false;
					boolean roomOnly=false;
					boolean exitOnly=false;
					boolean zapperMask=false;
					boolean zapperMask2=false;
					MaskingLibrary.CompiledZMask compiledZapperMask=null;
					// no else here, EVAR!!!
					if((who.toUpperCase().startsWith("ROOM "))
					||(who.toUpperCase().startsWith("ROOMS")))
					{
						roomOnly=true;
						who=who.substring(5).trim();
					}

					if((who.toUpperCase().startsWith("EXIT "))
					||(who.toUpperCase().startsWith("EXITS")))
					{
						exitOnly=true;
						who=who.substring(5).trim();
					}
					else
					if((who.toUpperCase().startsWith("ITEM "))
					||(who.toUpperCase().startsWith("ITEMS")))
					{
						itemOnly=true;
						who=who.substring(5).trim();
					}
					else
					if((who.toUpperCase().startsWith("MOB "))
					||(who.toUpperCase().startsWith("MOBS")))
					{
						mobOnly=true;
						who=who.substring(4).trim();
					}
					else
					if(who.toUpperCase().startsWith("MOBMASK ")||who.toUpperCase().startsWith("MOBMASK="))
					{
						mobOnly=true;
						zapperMask=true;
						who=who.substring(8).trim();
						String desc=CMLib.masking().maskDesc(who);
						if(desc.toLowerCase().startsWith("anyone") && (who.length()>0))
						{
							desc+="... and I doubt you meant that.";
							mob.tell(L("^xMask entered:^?^.^N @x1\n\r",desc));
							return false;
						}
						mob.tell(L("^xMask used:^?^.^N @x1\n\r",desc));
						compiledZapperMask=CMLib.masking().maskCompile(who);
					}
					else
					if(who.toUpperCase().startsWith("ITEMMASK ")||who.toUpperCase().startsWith("ITEMMASK="))
					{
						itemOnly=true;
						zapperMask=true;
						who=who.substring(9).trim();
						String desc=CMLib.masking().maskDesc(who);
						if(desc.toLowerCase().startsWith("anyone") && (who.length()>0))
						{
							desc+="... and I doubt you meant that.";
							mob.tell(L("^xMask entered:^?^.^N @x1\n\r",desc));
							return false;
						}
						mob.tell(L("^xMask used:^?^.^N @x1\n\r",desc));
						compiledZapperMask=CMLib.masking().maskCompile(who);
					}
					else
					if(who.toUpperCase().startsWith("MOBMASK2 ")||who.toUpperCase().startsWith("MOBMASK2="))
					{
						mobOnly=true;
						zapperMask2=true;
						String desc=CMLib.masking().maskDesc(who);
						if(desc.equalsIgnoreCase("Anyone") && (who.length()>0))
						{
							desc+="... and I doubt you meant that.";
							mob.tell(L("^xMask entered:^?^.^N @x1\n\r",desc));
							return false;
						}
						mob.tell(L("^xMask used:^?^.^N @x1\n\r",desc));
						who=who.substring(9).trim();
					}
					else
					if(who.toUpperCase().startsWith("ITEMMASK2 ")||who.toUpperCase().startsWith("ITEMMASK2="))
					{
						itemOnly=true;
						zapperMask2=true;
						String desc=CMLib.masking().maskDesc(who);
						if(desc.equalsIgnoreCase("Anyone") && (who.length()>0))
						{
							desc+="... and I doubt you meant that.";
							mob.tell(L("^xMask entered:^?^.^N @x1\n\r",desc));
							return false;
						}
						mob.tell(L("^xMask used:^?^.^N @x1\n\r",desc));
						who=who.substring(10).trim();
					}

					MultiEnumeration<Room> r=new MultiEnumeration<Room>((roomOnly||exitOnly)?CMLib.map().rooms():CMLib.map().roomsFilled());
					if(who.toUpperCase().startsWith("AREA ")||areaFlag)
					{
						r=new MultiEnumeration<Room>((roomOnly||exitOnly)?mob.location().getArea().getProperMap():mob.location().getArea().getFilledProperMap());
						areaFlag=true;
					}
					if(who.toUpperCase().startsWith("AREA "))
						who=who.substring(5).trim();
					Room R = null;

					try
					{
						if(who.startsWith("\"")&&who.endsWith("\"")&&(who.length()>2))
							who=who.substring(1,who.length()-1);
						final Session sess = mob.session();
						for(;r.hasMoreElements();)
						{
							if((sess != null)&&(sess.isStopped()))
								break;
							R=r.nextElement();
							if((R!=null)&&(CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.WHERE))&&(CMLib.flags().canAccess(mob,R.getArea())))
							{
								if((!mobOnly)&&(!itemOnly)&&(!exitOnly))
								{
									if((who.length()==0)
									||CMLib.english().containsString(R.displayText(),who)
									||CMLib.english().containsString(R.description(),who))
									{
										lines.append("^!"+CMStrings.padRight("*",firstColWidth)+"^?| ");
										lines.append(R.displayText(mob));
										lines.append(" (^<LSTROOMID^>"+getRoomDesc(mob,R,R)+"^</LSTROOMID^>)");
										lines.append("\n\r");
									}
								}
								if(exitOnly)
								{
									for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
									{
										final Exit E=R.getRawExit(d);
										if((E!=null)
										&&((who.length()==0)
											||((E.Name().length()>0)&&(CMLib.english().containsString(E.Name(),who)))
											||(E.hasADoor() && (E.doorName().length()>0)&& CMLib.english().containsString(E.doorName(),who))
											||(CMLib.english().containsString(E.viewableText(mob,R).toString(),who))))
										{
											lines.append("^!"+CMStrings.padRight(CMLib.directions().getDirectionName(d),firstColWidth)+"^N| ");
											lines.append(" (^<LSTROOMID^>"+getRoomDesc(mob,R,E)+"^</LSTROOMID^>)");
											lines.append("\n\r");
										}
									}
								}
								if((!mobOnly)&&(!roomOnly)&&(!exitOnly))
								{
									for(int i=0;i<R.numItems();i++)
									{
										final Item I=R.getItem(i);
										if((areaFlag)
										&&(I instanceof Boardable))
										{
											final Area A=((Boardable)I).getArea();
											final Enumeration<Room> Ar=(A==null)?null:A.getProperMap();
											if(Ar!=null)
												r.addEnumeration(Ar);
										}
										if((zapperMask)&&(itemOnly))
										{
											if(CMLib.masking().maskCheck(compiledZapperMask,I,true))
											{
												lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(mob),firstColWidth)+"^N| ");
												lines.append(R.displayText(mob));
												lines.append(" (^<LSTROOMID^>"+getRoomDesc(mob,R,I)+"^</LSTROOMID^>)");
												lines.append("\n\r");
											}
										}
										else
										if((zapperMask2)&&(itemOnly))
										{
											if(CMLib.masking().maskCheck(who,I,true))
											{
												lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(mob),firstColWidth)+"^N| ");
												lines.append(R.displayText(mob));
												lines.append(" (^<LSTROOMID^>"+getRoomDesc(mob,R,I)+"^</LSTROOMID^>)");
												lines.append("\n\r");
											}
										}
										else
										if((who.length()==0)
										||(CMLib.english().containsString(I.name(),who))
										||(CMLib.english().containsString(I.displayText(),who))
										||(CMLib.english().containsString(I.description(),who)))
										{
											lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(mob),firstColWidth)+"^N| ");
											lines.append(R.displayText(mob));
											lines.append(" (^<LSTROOMID^>"+getRoomDesc(mob,R,I)+"^</LSTROOMID^>)");
											lines.append("\n\r");
										}
									}
								}
								for(int m=0;m<R.numInhabitants();m++)
								{
									final MOB M=R.fetchInhabitant(m);
									if((M!=null)&&((M.isMonster())||(canShowTo(mob,M))))
									{
										if((!itemOnly)&&(!roomOnly)&&(!exitOnly))
										{
											if((zapperMask)&&(mobOnly))
											{
												if(CMLib.masking().maskCheck(compiledZapperMask,M,true))
												{
													lines.append("^!"+CMStrings.padRight(cataMark(M)+M.name(mob),firstColWidth)+"^N| ");
													lines.append(R.displayText(mob));
													lines.append(" (^<LSTROOMID^>"+getRoomDesc(mob,R,M)+"^</LSTROOMID^>)");
													lines.append("\n\r");
												}
											}
											else
											if((zapperMask2)&&(mobOnly))
											{
												if(CMLib.masking().maskCheck(who,M,true))
												{
													lines.append("^!"+CMStrings.padRight(cataMark(M)+M.name(mob),firstColWidth)+"^N| ");
													lines.append(R.displayText(mob));
													lines.append(" (^<LSTROOMID^>"+getRoomDesc(mob,R,M)+"^</LSTROOMID^>)");
													lines.append("\n\r");
												}
											}
											else
											if((who.length()==0)
											||(CMLib.english().containsString(M.name(),who))
											||(CMLib.english().containsString(M.displayText(),who))
											||(CMLib.english().containsString(M.description(),who)))
											{
												lines.append("^!"+CMStrings.padRight(cataMark(M)+M.name(mob),firstColWidth)+"^N| ");
												lines.append(R.displayText(mob));
												lines.append(" (^<LSTROOMID^>"+getRoomDesc(mob,R,M)+"^</LSTROOMID^>)");
												lines.append("\n\r");
											}
										}
										if((!mobOnly)&&(!roomOnly)&&(!exitOnly))
										{
											for(int i=0;i<M.numItems();i++)
											{
												final Item I=M.getItem(i);
												if((areaFlag)
												&&(I instanceof Boardable))
												{
													final Area A=((Boardable)I).getArea();
													final Enumeration<Room> Ar=(A==null)?null:A.getProperMap();
													if(Ar!=null)
														r.addEnumeration(Ar);
												}
												if((zapperMask)&&(itemOnly))
												{
													if(CMLib.masking().maskCheck(compiledZapperMask,I,true))
													{
														lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(mob),firstColWidth)+"^N| ");
														lines.append("INV: "+cataMark(M)+M.name(mob)+"^N");
														lines.append(" (^<LSTROOMID^>"+getRoomDesc(mob,R,I)+"^</LSTROOMID^>)");
														lines.append("\n\r");
													}
												}
												else
												if((zapperMask2)&&(itemOnly))
												{
													if(CMLib.masking().maskCheck(who,I,true))
													{
														lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(mob),firstColWidth)+"^N| ");
														lines.append("INV: "+cataMark(M)+M.name(mob)+"^N");
														lines.append(" (^<LSTROOMID^>"+getRoomDesc(mob,R,I)+"^</LSTROOMID^>)");
														lines.append("\n\r");
													}
												}
												else
												if((who.length()==0)
												||(CMLib.english().containsString(I.name(),who))
												||(CMLib.english().containsString(I.displayText(),who))
												||(CMLib.english().containsString(I.description(),who)))
												{
													lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(mob),firstColWidth)+"^N| ");
													lines.append("INV: "+cataMark(M)+M.name(mob)+"^N");
													lines.append(" (^<LSTROOMID^>"+getRoomDesc(mob,R,I)+"^</LSTROOMID^>)");
													lines.append("\n\r");
												}
											}
											final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
											if(SK!=null)
											for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
											{
												final Environmental E=i.next();
												if((areaFlag)
												&&(E instanceof Boardable))
												{
													final Area A=((Boardable)E).getArea();
													final Enumeration<Room> Ar=(A==null)?null:A.getProperMap();
													if(Ar!=null)
														r.addEnumeration(Ar);
												}
												if((zapperMask)&&(E instanceof Item)&&(itemOnly))
												{
													if(CMLib.masking().maskCheck(compiledZapperMask,E,true))
													{
														lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),firstColWidth)+"^N| ");
														lines.append("SHOP: "+cataMark(M)+M.name(mob)+"^N");
														lines.append(" (^<LSTROOMID^>"+getRoomDesc(mob,R,E)+"^</LSTROOMID^>)");
														lines.append("\n\r");
													}
												}
												else
												if((zapperMask)&&(E instanceof MOB)&&(mobOnly))
												{
													if(CMLib.masking().maskCheck(compiledZapperMask,E,true))
													{
														lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),firstColWidth)+"^N| ");
														lines.append("SHOP: "+cataMark(M)+M.name(mob)+"^N");
														lines.append(" (^<LSTROOMID^>"+getRoomDesc(mob,R,E)+"^</LSTROOMID^>)");
														lines.append("\n\r");
													}
												}
												else
												if((zapperMask2)&&(E instanceof Item)&&(itemOnly))
												{
													if(CMLib.masking().maskCheck(who,E,true))
													{
														lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),firstColWidth)+"^N| ");
														lines.append("SHOP: "+cataMark(M)+M.name(mob)+"^N");
														lines.append(" (^<LSTROOMID^>"+getRoomDesc(mob,R,E)+"^</LSTROOMID^>)");
														lines.append("\n\r");
													}
												}
												else
												if((zapperMask2)&&(E instanceof MOB)&&(mobOnly))
												{
													if(CMLib.masking().maskCheck(who,E,true))
													{
														lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),firstColWidth)+"^N| ");
														lines.append("SHOP: "+cataMark(M)+M.name(mob)+"^N");
														lines.append(" (^<LSTROOMID^>"+getRoomDesc(mob,R,E)+"^</LSTROOMID^>)");
														lines.append("\n\r");
													}
												}
												else
												if((who.length()==0)
												||(CMLib.english().containsString(E.name(),who))
												||(CMLib.english().containsString(E.displayText(),who))
												||(CMLib.english().containsString(E.description(),who)))
												{
													lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),firstColWidth)+"^N| ");
													lines.append("SHOP: "+cataMark(M)+M.name(mob)+"^N");
													lines.append(" (^<LSTROOMID^>"+getRoomDesc(mob,R,E)+"^</LSTROOMID^>)");
													lines.append("\n\r");
												}
											}
										}
									}
								}
							}
						}
					}
					catch (final NoSuchElementException nse)
					{
					}
				}
			}
			mob.tell(lines.toString()+"^.");
		}
		else
		{
			int alignment=mob.fetchFaction(CMLib.factions().getAlignmentID());
			for(int i=commands.size()-1;i>=0;i--)
			{
				final String s=commands.get(i);
				if(s.equalsIgnoreCase("good"))
				{
					alignment=CMLib.factions().getAlignMedianFacValue(Faction.Align.GOOD);
					commands.remove(i);
				}
				else
				if(s.equalsIgnoreCase("neutral"))
				{
					alignment=CMLib.factions().getAlignMedianFacValue(Faction.Align.NEUTRAL);
					commands.remove(i);
				}
				else
				if(s.equalsIgnoreCase("evil"))
				{
					alignment=CMLib.factions().getAlignMedianFacValue(Faction.Align.EVIL);
					commands.remove(i);
				}
			}

			final int adjust=CMath.s_int(CMParms.combine(commands,1));
			final PairArrayList<Area,Integer> levelsV=new PairArrayList<Area,Integer>();
			final PairArrayList<Area,Integer> mobsV=new PairArrayList<Area,Integer>();
			final PairArrayList<Area,Integer> alignV=new PairArrayList<Area,Integer>();
			final int moblevel=mob.phyStats().level()+adjust;
			for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
			{
				final Area A=a.nextElement();
				if((CMLib.flags().canAccess(mob,A))
				&&(CMLib.flags().canBeLocated(A)))
				{
					int median=A.getPlayerLevel();
					if(median==0)
						median=A.getIStat(Area.Stats.MED_LEVEL);
					int medianDiff=0;
					final int diffLimit=6;
					if((median<(moblevel+diffLimit))
					&&((median>=(moblevel-diffLimit))))
					{
						if(mob.phyStats().level()>=median)
							medianDiff=(int)Math.round(9.0*CMath.div(median,moblevel));
						else
							medianDiff=(int)Math.round(10.0*CMath.div(moblevel,median));
					}
					whereAdd(levelsV,A,medianDiff);

					whereAdd(mobsV,A,A.getIStat(Area.Stats.POPULATION));

					final int align=A.getIStat(Area.Stats.MED_ALIGNMENT);
					final int alignDiff=((int)Math.abs((double)(alignment-align)));
					whereAdd(alignV,A,alignDiff);
				}
			}
			final StringBuffer msg=new StringBuffer(L("You are currently in: ^H@x1^?\n\r",mob.location().getArea().name()));
			if((!CMSecurity.isDisabled(CMSecurity.DisFlag.ROOMVISITS))
			&&(mob.playerStats()!=null))
			{
				msg.append(L("You have explored @x1% of this area and @x2% of the world.\n\r",
						""+mob.playerStats().percentVisited(mob,mob.location().getArea()),
						""+mob.playerStats().percentVisited(mob,null)));
			}
			final PairArrayList<Area,Integer> scores=new PairArrayList<Area,Integer>();
			for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
			{
				final Area A=a.nextElement();
				if(CMLib.flags().canAccess(mob,A))
				{
					int index=levelsV.indexOfFirst(A);
					if(index>=0)
					{
						final Integer I=levelsV.get(index).second;
						if((I!=null)&&(I.intValue()!=0))
						{
							int score=(index+1);
							index=mobsV.indexOfFirst(A);
							if(index>=0)
								score+=(index+1);

							index=alignV.indexOfFirst(A);
							if(index>=0)
								score+=(index+1);
							whereAdd(scores,A,score);
						}
					}
				}
			}
			if(scores.size()==0)
				msg.append(L("There appear to be no other areas on this world.\n\r"));
			else
			{
				msg.append(L("\n\r^HThe best areas for you to try appear to be: ^?\n\r\n\r"));
				msg.append("^x"+CMStrings.padRight(L("Area Name"),35)+CMStrings.padRight(L("Level"),6)+CMStrings.padRight(L("Alignment"),20)+CMStrings.padRight(L("Pop"),10)+"^.^?\n\r");
				final List<Area> finalScoreList = new ArrayList<Area>();
				for(int i=scores.size()-1;((i>=0)&&(i>=(scores.size()-15)));i--)
					finalScoreList.add(scores.get(i).first);
				final int mobLevel=mob.phyStats().level();
				Collections.sort(finalScoreList,new Comparator<Area>()
				{
					@Override
					public int compare(final Area o1, final Area o2)
					{
						int median1=o1.getPlayerLevel();
						if(median1==0)
							median1=o1.getIStat(Area.Stats.MED_LEVEL);
						int median2=o2.getPlayerLevel();
						if(median2==0)
							median2=o2.getIStat(Area.Stats.MED_LEVEL);
						final int lvlDiff1=Math.abs(mobLevel - median1);
						final int lvlDiff2=Math.abs(mobLevel - median2);
						return lvlDiff1==lvlDiff2?0:(lvlDiff1>lvlDiff2)?1:-1;
					}

				});
				for(final Area A : finalScoreList)
				{
					int lvl=A.getPlayerLevel();
					if(lvl==0)
						lvl=A.getIStat(Area.Stats.MED_LEVEL);
					final int align=A.getIStat(Area.Stats.MED_ALIGNMENT);

					msg.append(CMStrings.padRight(A.name().replace('`', '\''),35))
					   .append(CMStrings.padRight(Integer.toString(lvl),6))
					   .append(CMStrings.padRight(CMLib.factions().getRange(CMLib.factions().getAlignmentID(), align).name(),20))
					   .append(CMStrings.padRight(Integer.toString(A.getIStat(Area.Stats.POPULATION)),10))
					   .append("\n\r");
				}
				msg.append(L("\n\r\n\r^HEnter 'HELP (AREA NAME) for more information.^?"));
			}
			if(!mob.isMonster())
				mob.session().colorOnlyPrintln(msg.toString()+"\n\r");
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}
}
