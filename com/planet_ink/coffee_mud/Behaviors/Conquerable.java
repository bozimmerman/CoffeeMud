package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.PlayerCode;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;

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
public class Conquerable extends Arrest
{
	@Override
	public String ID()
	{
		return "Conquerable";
	}

	@Override
	protected boolean defaultModifiableNames()
	{
		return false;
	}

	@Override
	protected String getLawParms()
	{
		return "custom";
	}

	protected String			savedHoldingClan	= "";
	protected String			prevHoldingClan		= "";
	protected String			holdingClan			= "";
	protected Map<String,int[]>	clanControlPoints	= new STreeMap<String,int[]>();
	protected PairList<MOB,MOB>	assaults			= new PairSVector<MOB,MOB>();
	protected ExpireTreeSet<MOB>noMultiFollows		= new ExpireTreeSet<MOB>();
	protected int				totalControlPoints	= -1;
	protected Area				myArea				= null;
	protected String			journalName			= "";
	protected boolean			allowLaw			= false;
	protected boolean			switchOwnership		= false;
	protected int				revoltFails			= 0;
	protected long				waitToReload		= 0;
	protected long				conquestDate		= 0;
	protected volatile int		loyaltyBonus		= 0;
	protected int				revoltDown			= REVOLTFREQ;
	protected static final int	REVOLTFREQ			= (int) ((TimeManager.MILI_DAY) / CMProps.getTickMillis());
	protected int				checkDown			= 0;
	protected static final int	CHECKFREQ			= 10;
	protected int				pointDown			= 0;
	protected static final int	POINTFREQ			= (int) ((10 * 60000) / CMProps.getTickMillis());
	protected int				fightDown			= 0;
	protected static final int	FIGHTFREQ			= 2;
	protected int				conqPtLvlDiff		= Integer.MAX_VALUE;

	protected PairList<ClanItem,ItemPossessor>		clanItems = new PairSVector<ClanItem,ItemPossessor>();

	@Override
	public boolean isFullyControlled()
	{
		return ((holdingClan.length()>0)&&((System.currentTimeMillis()-conquestDate)>CONTROLTIME));
	}

	@Override
	public String accountForYourself()
	{
		return "conquerability";
	}

	@Override
	public String rulingOrganization()
	{
		return holdingClan;
	}

	@Override
	public int addGetLoyaltyBonus(final int delta)
	{
		this.loyaltyBonus += delta;
		return this.loyaltyBonus;
	}

	@Override
	public CMObject copyOf()
	{
		final Conquerable obj=(Conquerable)super.copyOf();
		obj.clanItems=new PairVector<ClanItem,ItemPossessor>(clanItems);
		obj.clanControlPoints=new STreeMap<String,int[]>(clanControlPoints);
		obj.assaults=new PairSVector<MOB,MOB>();
		obj.assaults.addAll(assaults);
		obj.noMultiFollows=new ExpireTreeSet<MOB>(noMultiFollows);
		return obj;
	}

	@Override
	public String conquestInfo(final Area myArea)
	{
		final StringBuffer str=new StringBuffer("");
		if(myArea == null)
			return "";
		if(totalControlPoints<0)
			recalculateControlPoints(myArea);
		if((holdingClan.length()==0)||(totalControlPoints<0))
			str.append(L("Area '@x1' is not currently controlled by any clan.\n\r",myArea.name()));
		else
		{
			final Clan C=CMLib.clans().getClanAnyHost(holdingClan);
			if(C!=null)
			{
				if(isFullyControlled())
					str.append(L("Area '@x1' is controlled by @x2 @x3.\n\r",myArea.name(),C.getGovernmentName(),C.name()));
				else
				{
					str.append(L("Area '@x1' is occupied by @x2 @x3.\n\r",myArea.name(),C.getGovernmentName(),C.name()));
					final long remain=CONTROLTIME-(System.currentTimeMillis()-conquestDate);
					final String remainStr=myArea.getTimeObj().deriveEllapsedTimeString(remain);
					str.append(L("Full control will automatically be achieved in @x1.\n\r",remainStr));
				}

				if(C.isLoyaltyThroughItems())
				{
					final int pts=calcItemControlPoints(myArea);
					final int chance=calcRevoltChance(myArea);
					str.append(L("@x1 has handed out clan items here for @x2 loyalty points.\n\r",C.name(),""+pts));
					str.append(L("There is currently a @x1% chance of revolt here.\n\r",""+chance));
				}
			}
			else
			{
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CONQUEST))
					Log.debugOut("Conquest",holdingClan+" has laid waste to "+myArea.name()+".");
				endClanRule(L(" due to losing their clan"));
				str.append(L("This area is laid waste by @x1.\n\r",holdingClan));
			}
		}
		if((totalControlPoints<0)&&(myArea!=null))
			recalculateControlPoints(myArea);
		if(totalControlPoints<0)
			str.append(L("This area has not yet calculated its required control points.\n\r"));
		else
			str.append(L("This area requires @x1 points to control.\n\r",""+totalControlPoints));
		if(clanControlPoints.size()==0)
			str.append(L("There are no control points won at present by any clan.\n\r"));
		synchronized(clanControlPoints)
		{
			for(final String clanID : clanControlPoints.keySet())
			{
				final int[] ic=clanControlPoints.get(clanID);
				final Clan C=CMLib.clans().getClanAnyHost(clanID);
				if((C!=null)&&(C.getGovernment().isConquestEnabled()))
					str.append(L("@x1 @x2 has @x3 control points.\n\r",C.getGovernmentName(),C.name(),""+ic[0]));
			}
		}
		return str.toString();
	}

	@Override
	public int controlPoints()
	{
		if(totalControlPoints>=0)
			return totalControlPoints;
		return 0;
	}

	@Override
	public int getControlPoints(final String clanID)
	{
		if((clanID==null)||(clanID.length()==0))
			return 0;
		synchronized(clanControlPoints)
		{
			final int[] ic=clanControlPoints.get(clanID.toUpperCase().trim());
			if(ic != null)
			{
				final Clan C=CMLib.clans().getClanAnyHost(clanID);
				if(C!=null)
					return ic[0];
			}
		}
		return 0;
	}

	@Override
	public int revoltChance()
	{
		if(myArea==null)
			return 100 + loyaltyBonus;
		final Clan C=CMLib.clans().getClanAnyHost(holdingClan);
		if((C==null)||(C.isLoyaltyThroughItems()))
			return calcRevoltChance(myArea);
		return 0;
	}

	@Override
	public void setParms(final String newParms)
	{
		super.setParms(newParms);
		journalName=CMParms.getParmStr(newParms,"JOURNAL","");
		allowLaw=CMParms.getParmStr(newParms,"LAW","FALSE").toUpperCase().startsWith("T");
		switchOwnership=CMParms.getParmStr(newParms,"OWNERSHIP","TRUE").toUpperCase().startsWith("T");
		loyaltyBonus=CMParms.getParmInt(newParms,"LOYALTY",0);
		conqPtLvlDiff=CMParms.getParmInt(newParms,"LEVELDIFF", CMProps.getIntVar(CMProps.Int.EXPRATE)*2);
		loadAttempt=false;
		clanItems=new PairSVector<ClanItem,ItemPossessor>();
		clanControlPoints=new STreeMap<String,int[]>();
		assaults=new PairSVector<MOB,MOB>();
		noMultiFollows=new ExpireTreeSet<MOB>();
	}

	@Override
	public void startBehavior(final PhysicalAgent E)
	{
		super.startBehavior(E);
		CMLib.map().addGlobalHandler(this, CMMsg.TYP_CLANEVENT);
	}

	@Override
	public boolean isAnyKindOfOfficer(final Law laws, final MOB M)
	{
		if((M!=null)
		&&(allowLaw)
		&&(!CMLib.flags().isAnimalIntelligence(M))
		&&(M.location()!=null)
		&&((!M.isMonster())||CMLib.flags().isMobile(M))
		&&(holdingClan.length()>0)
		&&(M.getClanRole(holdingClan)!=null))
		{
			for(int i=0;i<M.numItems();i++)
			{
				final Item I=M.getItem(i);
				if((I!=null)
				&&(I instanceof ClanItem)
				&&(!I.amWearingAt(Wearable.IN_INVENTORY))
				&&(((ClanItem)I).getClanItemType()==ClanItem.ClanItemType.BANNER))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean isTheJudge(final Law laws, final MOB M)
	{
		if((M!=null)
		&&(allowLaw)
		&&(!CMLib.flags().isAnimalIntelligence(M))
		&&(M.location()!=null)
		&&(holdingClan.length()>0)
		&&(M.getClanRole(holdingClan)!=null))
		{
			for(int i=0;i<M.numItems();i++)
			{
				final Item I=M.getItem(i);
				if((I!=null)
				&&(I instanceof ClanItem)
				&&(!I.amWearingAt(Wearable.IN_INVENTORY))
				&&(((ClanItem)I).getClanItemType()==ClanItem.ClanItemType.GAVEL))
					return true;
			}
		}
		return false;
	}

	protected synchronized void endClanRule(final String reason)
	{
		if(holdingClan.length()==0)
			return;
		if((!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
		||(CMSecurity.isDisabled(CMSecurity.DisFlag.CONQUEST)))
			return;
		final Clan C=CMLib.clans().getClanAnyHost(holdingClan);
		final String worship=getManadatoryWorshipID();
		prevHoldingClan=holdingClan;
		for(int v=0;v<clanItems.size();v++)
		{
			final Item I=clanItems.getFirst(v);
			if((I instanceof ClanItem)
			&&(((ClanItem)I).clanID().equals(holdingClan))
			&&(!I.amDestroyed()))
			{
				if((I instanceof SiegableItem)
				&&(I instanceof Boardable))
					((SiegableItem)I).killMeDead(true);
				else
				if(I.owner() instanceof MOB)
				{
					final MOB M=(MOB)I.owner();
					if((M.location()!=null)&&(!M.amDead())&&(M.isMonster()))
					{
						M.delItem(I);
						if(M.getClanRole(holdingClan)!=null)
						{
							M.setClan(holdingClan,-1);
							if((worship!=null)
							&&(M.baseCharStats().getWorshipCharID().equals(worship)))
							{
								M.baseCharStats().setWorshipCharID("");
								M.charStats().setWorshipCharID("");
							}
						}
						I.setRawWornCode(0);
						I.setContainer(null);
						M.location().addItem(I,ItemPossessor.Expire.Player_Drop);
					}
				}
			}
		}

		if(myArea!=null)
		{
			if(worship!=null)
				Resources.removeResource("PIETY_"+myArea.Name().toUpperCase());
			for(final Enumeration<Room> e=myArea.getMetroMap();e.hasMoreElements();)
			{
				final Room R=e.nextElement();
				for(int i=0;i<R.numInhabitants();i++)
				{
					final MOB M=R.fetchInhabitant(i);
					if((M!=null)
					&&(M.isMonster())
					&&(M.getStartRoom()!=null)
					&&(myArea.inMyMetroArea(M.getStartRoom().getArea()))
					&&(M.getClanRole(holdingClan)!=null))
					{
						M.setClan(holdingClan,-1);
						if((worship!=null)
						&&(M.charStats().getWorshipCharID().equals(worship)))
						{
							M.baseCharStats().setWorshipCharID("");
							M.charStats().setWorshipCharID("");
						}
					}
				}
			}
			if(holdingClan.length()>0)
			{
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CONQUEST))
					Log.debugOut("Conquest",holdingClan+" has lost control of "+myArea.name()+reason+".");
				if(C!=null)
				{
					final MOB cM=CMLib.players().getLoadPlayer(C.getResponsibleMemberName());
					if(cM != null)
						CMLib.achievements().possiblyBumpAchievement(cM, AchievementLibrary.Event.CONQUEREDAREAS, -1, C, myArea);
				}
				final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CONQUESTS, null);
				for(int i=0;i<channels.size();i++)
					CMLib.commands().postChannel(channels.get(i),CMLib.clans().clanRoles(),L("@x1 has lost control of @x2@x3.",holdingClan,myArea.name(),reason),false);
				if(journalName.length()>0)
					CMLib.database().DBWriteJournal(journalName,"Conquest","ALL",L("@x1 loses control of @x2@x3.",holdingClan,myArea.name(),reason),L("See the subject line."));
			}
			final Law laws=getLaws(myArea,false);
			if(laws.lawIsActivated())
			{
				laws.setInternalStr("ACTIVATED","FALSE");
				laws.resetLaw();
				CMLib.database().DBReCreatePlayerData(myArea.Name(),"ARREST",myArea.Name()+"/ARREST",laws.rawLawString());
			}
		}
		synchronized(clanItems)
		{
			try
			{
				for(int c=clanItems.size()-1;c>=0;c--)
				{
					final ClanItem item=clanItems.getFirst(c);
					if((C==null)
					&&(item.clanID().equalsIgnoreCase(holdingClan))
					&&((!(item.owner() instanceof MOB))||(((MOB)item.owner()).isMonster())))
					{
						item.destroy();
						clanItems.remove(c);
					}
					else
					if(item.getClanItemType()!=ClanItem.ClanItemType.FLAG)
						deRegisterClanItem(clanItems.getFirst(c));
				}
			}
			catch (final ArrayIndexOutOfBoundsException x)
			{
			}
			if((C==null)&&(clanItems.size()==0)&&(myArea!=null))
				CMLib.database().DBDeletePlayerData(myArea.name(),"CONQITEMS","CONQITEMS/"+myArea.name());
		}
		holdingClan="";
		conquestDate=0;
	}

	public int calcItemControlPoints(final Area A)
	{
		int itemControlPoints=0;
		synchronized(clanItems)
		{
			for(int i=clanItems.size()-1;i>=0;i--)
			{
				final ClanItem I=clanItems.getFirst(i);
				final ItemPossessor poss = I.owner();
				if((!I.amDestroyed()) && (poss instanceof MOB))
				{
					final MOB mposs = (MOB)poss;
					if((mposs.isMonster())
					&&(CMLib.flags().isInTheGame(mposs,true))
					&&(A.inMyMetroArea(CMLib.map().getStartArea(mposs)))
					&&((holdingClan.length()==0)||(I.clanID().equals(holdingClan)))
					&&(I.getClanItemType()!=ClanItem.ClanItemType.PROPAGANDA))
						itemControlPoints+=mposs.phyStats().level();
				}
			}
		}
		return itemControlPoints;
	}

	public int calcRevoltChance(final Area A)
	{
		if(totalControlPoints<=0)
			return 0;
		final int itemControlPoints=calcItemControlPoints(A);
		int totalNeeded=(int)Math.round(CMath.mul(0.05,totalControlPoints));
		if(totalNeeded<=0)
			totalNeeded=1;
		int chance=(int)Math.round(10.0-(CMath.mul(10.0,CMath.div(itemControlPoints,totalNeeded))));
		synchronized(clanItems)
		{
			for(int i=clanItems.size()-1;i>=0;i--)
			{
				final ClanItem I=clanItems.getFirst(i);
				if((I instanceof Boardable)
				&&(!I.amDestroyed())
				&&(((Boardable)I).getArea()!=null))
					chance -= ((Boardable)I).getArea().numberOfProperIDedRooms();
			}
		}
		chance -=  loyaltyBonus;
		if(chance<=0)
			return 0;
		return chance;
	}

	protected void announceToArea(final Area area, final String clanID, final int amount)
	{
		final Clan C=CMLib.clans().getClanExact(clanID);
		if(C!=null)
		{
			final MOB cM=CMLib.players().getLoadPlayer(C.getResponsibleMemberName());
			if(cM != null)
				CMLib.achievements().possiblyBumpAchievement(cM, AchievementLibrary.Event.CONQUESTPOINTS, amount, C, myArea);
		}
		for(final Session S : CMLib.sessions().localOnlineIterable())
		{
			if((S.mob()!=null)
			&&(S.mob().location()!=null)
			&&(area.inMyMetroArea(S.mob().location().getArea())))
			{
				if(amount > 0)
					S.println(L("@x1 gains @x2 control points.",clanID,""+amount));
				else
				if(amount < 0)
					S.println(L("@x1 Loses @x2 control points.",clanID,""+(-amount)));
			}
		}
	}

	protected boolean hasItemSameAs(final MOB M, final Item I)
	{
		for(final Enumeration<Item> i=M.items();i.hasMoreElements();)
		{
			if(I.sameAs(i.nextElement()))
				return true;
		}
		return false;
	}

	protected boolean hasItemSameName(final MOB M, final String name)
	{
		for(final Enumeration<Item> i=M.items();i.hasMoreElements();)
		{
			if(name.equals(i.nextElement().Name()))
				return true;
		}
		return false;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
		||(CMSecurity.isDisabled(CMSecurity.DisFlag.CONQUEST)))
			return true;

		if(!super.tick(ticking,tickID))
			return false;
		if(tickID!=Tickable.TICKID_AREA)
			return true;
		if(!(ticking instanceof Area))
			return true;
		final Area A=(Area)ticking;

		if(A!=myArea)
			myArea=A;

		for(int i=clanItems.size()-1;i>=0;i--)
		{
			final ClanItem I=clanItems.getFirst(i);
			final ItemPossessor P = clanItems.getSecond(i);
			if(!I.tick(this,Tickable.TICKID_CLANITEM))
				deRegisterClanItem(I);
			else
			{
				I.setExpirationDate(0);
				if((I.amDestroyed())
				||(CMLib.map().areaLocation(I)!=A))
					clanItems.remove(i);
				else
				if((P!=null)
				&&(P.amDestroyed()||(P!=I.owner()))
				&&(I.owner()!=null))
					clanItems.set(i, new Pair<ClanItem,ItemPossessor>(I,I.owner()));
				else
				if((I.owner() instanceof Room)&&(I.container()!=null))
					I.setContainer(null);
			}
		}

		// calculate total control points
		// make sure all intelligent mobs belong to the clan
		if((totalControlPoints<0)
		&&((waitToReload<=0)||(System.currentTimeMillis()>waitToReload))
		&&(myArea!=null))
		{
			final HashSet<Room> doneRooms=new HashSet<Room>();
			clanItems.clear();
			final List<PlayerData> itemSet=CMLib.database().DBReadPlayerData(myArea.name(),"CONQITEMS","CONQITEMS/"+myArea.name());
			if((itemSet!=null)&&(itemSet.size()>0))
			{
				final String data=itemSet.get(0).xml();
				if(CMSecurity.isDebugging(DbgFlag.CONQUEST))
					Log.debugOut(myArea.Name() +": clan items loaded = "+itemSet.size()+", "+((data.indexOf("GenClanFlag")>=0)?"including the flag":"without a flag!"));
				final List<XMLLibrary.XMLTag> xml=CMLib.xml().parseAllXML(data);
				if(xml!=null)
				{
					savedHoldingClan=CMLib.xml().getValFromPieces(xml,"CLANID");
					prevHoldingClan=CMLib.xml().getValFromPieces(xml,"OLDCLANID");
					conquestDate=CMLib.xml().getLongFromPieces(xml,"CLANDATE");
					holdingClan=savedHoldingClan;
					final List<XMLLibrary.XMLTag> allData=CMLib.xml().getContentsFromPieces(xml,"ACITEMS");
					if(allData!=null)
					for(int c=0;c<allData.size();c++)
					{
						final XMLTag iblk=allData.get(c);
						if((iblk.tag().equalsIgnoreCase("ACITEM"))&&(iblk.contents()!=null))
						{
							final List<XMLLibrary.XMLTag> roomData=iblk.contents();
							final String roomID=CMLib.xml().getValFromPieces(roomData,"ROOMID");
							final String MOBname=CMLib.xml().getValFromPieces(roomData,"MOB");
							final Room R=CMLib.map().getRoom(roomID);
							if((R!=null)&&(A.inMyMetroArea(R.getArea())))
							{
								final String iClass=CMLib.xml().getValFromPieces(roomData,"ICLAS");
								final Item newItem=CMClass.getItem(iClass);
								if(newItem!=null)
								{
									newItem.basePhyStats().setLevel(CMLib.xml().getIntFromPieces(roomData,"ILEVL"));
									newItem.basePhyStats().setAbility(CMLib.xml().getIntFromPieces(roomData,"IABLE"));
									newItem.basePhyStats().setRejuv(CMLib.xml().getIntFromPieces(roomData,"IREJV"));
									newItem.setUsesRemaining(CMLib.xml().getIntFromPieces(roomData,"IUSES"));
									newItem.setMiscText(CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(roomData,"ITEXT")));
									newItem.recoverPhyStats();
									MOB foundMOB=null;
									MOB backupMOB=null;
									if(MOBname.length()>0)
									{
										for(int i=0;i<R.numInhabitants();i++)
										{
											final MOB M=R.fetchInhabitant(i);
											if((M!=null)
											&&(M.isMonster())
											&&(M.name().equals(MOBname))
											&&(M.getStartRoom()==R))
											{
												if(!hasItemSameName(M,newItem.Name()))
													foundMOB=M;
												else
												if(!hasItemSameAs(M,newItem))
													backupMOB=M;
												break;
											}
										}
									}
									if((foundMOB==null)&&(MOBname.length()>0))
									{
										for(final Enumeration<Room> e=A.getMetroMap();e.hasMoreElements();)
										{
											final Room R2=e.nextElement();
											for(int i=0;i<R2.numInhabitants();i++)
											{
												final MOB M=R2.fetchInhabitant(i);
												if((M!=null)
												&&(M.isMonster())
												&&(M.name().equals(MOBname))
												&&(M.getStartRoom()==R))
												{
													if(!hasItemSameName(M,newItem.Name()))
														foundMOB=M;
													else
													if(!hasItemSameAs(M,newItem))
														backupMOB=M;
													break;
												}
											}
										}
									}
									if((foundMOB==null)&&(MOBname.length()>0))
										foundMOB=backupMOB;
									if(foundMOB!=null)
									{
										foundMOB.addItem(newItem);
										newItem.wearAt(newItem.rawProperLocationBitmap());
									}
									else
									{
										if(!doneRooms.contains(R))
										{
											doneRooms.add(R);
											for(int i=R.numItems()-1;i>=0;i--)
											{
												final Item I=R.getItem(i);
												if(I instanceof ClanItem)
													I.destroy();
											}
										}
										R.addItem(newItem);
										if(newItem instanceof Boardable)
											((Boardable)newItem).dockHere(R);
									}
									if(newItem instanceof ClanItem)
										registerClanItem((ClanItem)newItem);
								}
							}
						}
					}
				}
			}
			recalculateControlPoints(A);
		}
		else
		{
			if((--checkDown)<=0)
			{
				checkDown=CHECKFREQ;
				// make sure clanitems are truly in the area
				synchronized(clanItems)
				{
					for(int i=clanItems.size()-1;i>=0;i--)
					{
						final ClanItem I=clanItems.getFirst(i);
						if(I==null)
							continue;
						final Room R=CMLib.map().roomLocation(I);
						if(R==null)
							deRegisterClanItem(I);
						else
						if(!A.inMyMetroArea(R.getArea()))
							deRegisterClanItem(I);
						else
						if(I.amDestroyed())
							deRegisterClanItem(I);
						else
						if((I.getClanItemType()==ClanItem.ClanItemType.FLAG)&&(!R.isContent(I)))
							deRegisterClanItem(I);
						else
						{
							I.setExpirationDate(0);
							if((I.owner() instanceof Room)&&(I.container()!=null))
								I.setContainer(null);
						}
					}
				}

				// make sure holding clan still holds
				if((holdingClan.length()>0)
				&&(totalControlPoints>=0)
				&&(!flagFound(A,holdingClan)))
				{
					if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CONQUEST))
						Log.debugOut("Conquest",holdingClan+" has "+totalControlPoints+" points and flag="+flagFound(A,holdingClan));
					if((prevHoldingClan.length()>0)
					&&(!holdingClan.equalsIgnoreCase(prevHoldingClan))
					&&(CMLib.clans().getClanAnyHost(prevHoldingClan)!=null)
					&&(flagFound(A,prevHoldingClan)))
						declareWinner(prevHoldingClan);
					else
						endClanRule(L(" due to a missing flag"));
				}
			}

			if((--revoltDown)<=0)
			{
				revoltDown=Conquerable.REVOLTFREQ;
				if(holdingClan.length()>0)
				{
					final Clan C=CMLib.clans().getClanAnyHost(holdingClan);
					if((C==null)||(C.isLoyaltyThroughItems()))
					{
						final int chance=calcRevoltChance(A);
						loyaltyBonus = 0;
						if(CMLib.dice().rollPercentage()<chance)
						{
							if(revoltFails>0)
							{
								Log.sysOut("Conquerable",A.Name()+" revolted against "+holdingClan+" with "+chance+"% chance");
								if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CONQUEST))
									Log.debugOut("Conquest","The inhabitants of "+myArea.name()+" have revolted against "+holdingClan+" with "+chance+"% chance, after "+calcItemControlPoints(myArea)+" item points of "+totalControlPoints+" control points.");
								final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CONQUESTS, null);
								for(int i=0;i<channels.size();i++)
									CMLib.commands().postChannel(channels.get(i),CMLib.clans().clanRoles(),L("The inhabitants of @x1 have revolted against @x2.",myArea.name(),holdingClan),false);
								if(journalName.length()>0)
									CMLib.database().DBWriteJournal(journalName,"Conquest","ALL","The inhabitants of "+myArea.name()+" have revolted against "+holdingClan+".","See the subject line.");

								synchronized(clanControlPoints)
								{
									clanControlPoints.clear();
								}
								/*
								if((prevHoldingClan.length()>0)
								&&(!holdingClan.equalsIgnoreCase(prevHoldingClan))
								&&(CMLib.clans().getClanAnyHost(prevHoldingClan)!=null)
								&&(flagFound(A,prevHoldingClan)))
									declareWinner(prevHoldingClan);
								else
								*/
									endClanRule(L(" due to revolt"));
								revoltFails=0;
							}
							else
							{
								final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CONQUESTS, null);
								for(int i=0;i<channels.size();i++)
									CMLib.commands().postChannel(channels.get(i),CMLib.clans().clanRoles(),L("There are the rumblings of revolt in @x1.",myArea.name()),false);
								revoltFails=20;
							}
						}
						else
						if(revoltFails>0)
							revoltFails--;
					}
				}
			}

			if((--pointDown)<=0)
			{
				pointDown=POINTFREQ;
				// slowly decrease control points over time
				synchronized(clanControlPoints)
				{
					for(final String clanID : clanControlPoints.keySet())
					{
						final int[] ic=clanControlPoints.get(clanID);
						if(ic[0]<=1)
							clanControlPoints.remove(clanID);
						else
							ic[0]--;
					}
				}
			}

			if((--fightDown)<=0)
			{
				fightDown=FIGHTFREQ;
				if(assaults.size()>0)
				{
					synchronized(assaults)
					{
						while(assaults.size()>0)
						{
							final MOB M1=assaults.getFirst(0);
							final MOB M2=assaults.getSecond(0);
							if((M1!=M2)
							&&(M1.location()==M2.location())
							&&(!M1.isInCombat())
							&&(holdingClan.length()>0)
							&&(M1.getClanRole(holdingClan)!=null)
							&&(M2.getClanRole(holdingClan)==null)
							&&(CMLib.flags().canBeSeenBy(M2,M1)))
							{
								final Vector<String> V=new Vector<String>();
								V.addElement("YELL");
								V.addElement(L(warCrys()[CMLib.dice().roll(1,warCrys().length,-1)]));
								M1.doCommand(V,MUDCmdProcessor.METAFLAG_FORCED);
								CMLib.combat().postAttack(M1,M2,M1.fetchWieldedItem());
							}
							assaults.remove(0);
						}
					}
				}
			}
		}
		return true;
	}

	protected String getManadatoryWorshipID()
	{
		if(holdingClan.length()==0)
			return null;
		final Clan C=CMLib.clans().getClanAnyHost(holdingClan);
		if(C==null)
			return null;
		if(C.isWorshipConquest())
		{
			final String rName = (String)CMLib.players().getPlayerValue(C.getResponsibleMemberName(), PlayerCode.DEITY);
			if((rName!=null)&&(rName.length()>0))
				return rName;
		}
		return null;
	}

	public void recalculateControlPoints(final Area A)
	{
		totalControlPoints=0;
		final String worship=getManadatoryWorshipID();
		final Clan C=(holdingClan!=null)?CMLib.clans().getClanAnyHost(holdingClan):null;
		for(final Enumeration<Room> e=A.getMetroMap();e.hasMoreElements();)
		{
			final Room R=e.nextElement();
			for(int i=0;i<R.numInhabitants();i++)
			{
				final MOB M=R.fetchInhabitant(i);
				if((M!=null)
				&&(M.isMonster())
				&&(M.getStartRoom()!=null)
				&&(A.inMyMetroArea(M.getStartRoom().getArea()))
				&&(!CMLib.flags().isAnimalIntelligence(M)))
				{
					if((C!=null)&&(M.getClanRole(C.clanID())==null))
					{
						M.setClan(C.clanID(),C.getAutoPosition());
						if((worship!=null)
						&&(!M.baseCharStats().getWorshipCharID().equals(worship)))
						{
							M.baseCharStats().setWorshipCharID(worship);
							M.charStats().setWorshipCharID(worship);
						}
					}
					totalControlPoints+=M.phyStats().level();
				}
			}
			for(int i=0;i<R.numItems();i++)
			{
				final Item I=R.getItem(i);
				if((I instanceof ClanItem)
				&&(I instanceof SiegableItem)
				&&(I instanceof Boardable))
					totalControlPoints += ((SiegableItem)I).getMaxHullPoints();
			}
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		final boolean debugging=CMSecurity.isDebugging(CMSecurity.DbgFlag.CONQUEST);
		Clan srcC;

		if((msg.targetMinor()==CMMsg.TYP_EXPIRE)
		&&(msg.target() instanceof Room)
		&&(myArea!=null)
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.CONQUEST))
		&&(totalControlPoints>=0)
		&&((!savedHoldingClan.equals(""))||(!holdingClan.equals(""))))
		{
			synchronized(clanItems)
			{
				for(int i=0;i<clanItems.size();i++)
				{
					final ClanItem I=clanItems.getFirst(i);
					final Room R=CMLib.map().roomLocation(I);
					if((R==msg.target())
					&&(!((Item)I).amDestroyed())
					&&((I.getClanItemType()!=ClanItem.ClanItemType.FLAG)||(R.isContent(I))))
						return false;
				}
			}
		}
		if((holdingClan.length()>0)
		&&(msg.source().getClanRole(holdingClan)!=null)
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.CONQUEST)))
		{
			if((msg.source().isMonster())
			&&(msg.target() instanceof MOB)
			&&(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
			&&(!((MOB)msg.target()).isInCombat())
			&&(msg.source().getVictim()!=msg.target())
			&&(((MOB)msg.target()).getClanRole(holdingClan)!=null)
			&&(!CMLib.flags().isAnimalIntelligence(msg.source())))
			{
				final MOB target=(MOB)msg.target();
				msg.source().tell(L("@x1 is a fellow @x2 member, and you must respect @x3.",target.name(msg.source()),holdingClan,target.charStats().himher()));
				if(target.getVictim()==msg.source())
				{
					target.makePeace(true);
					target.setVictim(null);
				}
				return false;
			}
			else
			if((msg.sourceMinor()==CMMsg.TYP_EXPCHANGE)
			&&(msg.target() instanceof MOB)
			&&(myArea!=null)
			&&(((MOB)msg.target()).getStartRoom()!=null)
			&&(myArea.inMyMetroArea(((MOB)msg.target()).getStartRoom().getArea())))
				msg.setValue(0);
			else
			if((msg.targetMinor()==CMMsg.TYP_ORDER)
			&&(!msg.source().isMonster())
			&&(msg.target() instanceof MOB)
			&&(((MOB)msg.target()).isMonster()))
			{
				Item badge=null;
				Item I=null;
				ClanItem CI=null;
				for(int i=msg.source().numItems()-1;i>=0;i--)
				{
					I=msg.source().getItem(i);
					if(I instanceof ClanItem)
					{
						CI=(ClanItem)I;
						if(CI.getClanItemType()==ClanItem.ClanItemType.LEGALBADGE)
						{
							badge=CI;
							break;
						}
					}
				}
				if(badge==null)
				{

				}
			}
		}
		else // must not be equal because of else to above
		if((holdingClan.length()>0)
		&&(msg.target() instanceof MOB)
		&&(((MOB)msg.target()).amFollowing()==msg.source())
		&&(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
		&&(!((MOB)msg.target()).isInCombat())
		&&(msg.source().getVictim()!=msg.target())
		&&(((MOB)msg.target()).getClanRole(holdingClan)!=null)
		&&(noMultiFollows.contains(msg.target()))
		&&((srcC=CMLib.clans().findConquerableClan(msg.source()))!=null)
		&&(flagFound((Area)myHost,srcC)))
		{
			noMultiFollows.remove(msg.target());
			if(debugging)
				Log.debugOut("Conquest",srcC.getName()+" lose "+(((MOB)msg.target()).phyStats().level())+" points by harming "+msg.target().name());
			changeControlPoints(srcC.clanID(),-((MOB)msg.target()).phyStats().level(),msg.source().location());
		}

		if((holdingClan.length()>0)
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.CONQUEST)))
		{
			if((msg.target() instanceof Room)
			&&(msg.tool() instanceof Ability)
			&&(msg.tool() instanceof Deity.DeityWorshipper) // one of the infusing skills
			&&(msg.sourceMinor()!=CMMsg.TYP_TEACH))
			{

				if((msg.source().getClanRole(holdingClan)==null)
				||(CMLib.clans().getClanAnyHost(holdingClan)==null)
				||(!CMLib.clans().getClanAnyHost(holdingClan).isWorshipConquest()))
				{
					msg.source().tell(L("Only a member of a conquering deity clan can pray for that here."));
					return false;
				}
			}

			if((msg.sourceMinor()==CMMsg.TYP_EXPCHANGE)
			&&((msg.source().getStartRoom()==null)||(!myArea.inMyMetroArea(msg.source().getStartRoom().getArea())))
			&&(msg.value()>0))
			{
				final Clan holdingC=CMLib.clans().getClanAnyHost(holdingClan);
				if(holdingC.getTaxes()!=0)
				{
					final MOB target=(msg.target() instanceof MOB)?(MOB)msg.target():null;
					final int lossAmt=(int)Math.round(CMath.mul(msg.value(),holdingC.getTaxes()));
					final int clanAmt=(int)Math.round(CMath.mul(CMLib.leveler().adjustedExperience(msg.source(),target,msg.value()),holdingC.getTaxes()));
					if(lossAmt>0)
					{
						msg.setValue(msg.value()-lossAmt);
						holdingC.adjExp(msg.source(), clanAmt);
						holdingC.update();
					}
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

	protected void declareWinner(final String clanID)
	{
		if((holdingClan.equals(clanID))||(totalControlPoints<0))
			return;
		Clan C=CMLib.clans().getClanAnyHost(clanID);
		if(C==null)
			C=CMLib.clans().getClanExact(clanID);
		if((C==null)||(!C.getGovernment().isConquestEnabled()))
			return;

		final MOB mob=CMLib.map().getFactoryMOBInAnyRoom();
		mob.setName(clanID);
		if(myArea!=null)
		{
			for(final Enumeration<Room> e=myArea.getMetroMap();e.hasMoreElements();)
			{
				if(!e.nextElement().show(mob,myArea,null,CMMsg.MSG_AREAAFFECT,null,CMMsg.MSG_AREAAFFECT,L("CONQUEST"),CMMsg.MSG_AREAAFFECT,null))
				{
					Log.errOut("Conquest","Conquest was stopped in "+myArea.name()+" for "+clanID+".");
					return;
				}
			}
		}
		mob.destroy();
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CONQUEST))
			Log.debugOut("Conquest","The inhabitants of "+myArea.name()+" are conquered by "+clanID+", vanquishing '"+holdingClan+"'.");
		if(holdingClan.length()>0)
			endClanRule(L(" due to being defeated by "+clanID));

		revoltDown=REVOLTFREQ;
		holdingClan=clanID;
		synchronized(clanControlPoints)
		{
			clanControlPoints.clear();
		}
		if(myArea!=null)
		{
			final LandTitle areaTitle=CMLib.law().getLandTitle(myArea);
			if(this.switchOwnership
			&& (areaTitle!=null)
			&& (!areaTitle.getOwnerName().equalsIgnoreCase(holdingClan)))
				areaTitle.setOwnerName(holdingClan);
			final String worship=getManadatoryWorshipID();
			for(final Enumeration<Room> e=myArea.getMetroMap();e.hasMoreElements();)
			{
				final Room R=e.nextElement();
				if(R!=null)
				{
					final LandTitle T=CMLib.law().getLandTitle(R);
					if(this.switchOwnership
					&& (T!=null)
					&& (T!=areaTitle)
					&& (!T.getOwnerName().equalsIgnoreCase(holdingClan)))
						T.setOwnerName(holdingClan);
					for(int i=0;i<R.numInhabitants();i++)
					{
						final MOB M=R.fetchInhabitant(i);
						if((M!=null)
						&&(M.isMonster())
						&&(M.getStartRoom()!=null)
						&&(myArea.inMyMetroArea(M.getStartRoom().getArea()))
						&&(!CMLib.flags().isAnimalIntelligence(M))
						&&(CMLib.clans().findConquerableClan(M)==null))
						{
							M.setClan(holdingClan,C.getAutoPosition());
							if((worship!=null)
							&&(!M.baseCharStats().getWorshipCharID().equals(worship)))
							{
								M.baseCharStats().setWorshipCharID(worship);
								M.charStats().setWorshipCharID(worship);
							}
						}
					}
				}
			}
			if(C!=null)
			{
				C.bumpTrophyData(Clan.Trophy.MonthlyConquests, 1);
				final MOB cM=CMLib.players().getLoadPlayer(C.getResponsibleMemberName());
				if(cM != null)
					CMLib.achievements().possiblyBumpAchievement(cM, AchievementLibrary.Event.CONQUEREDAREAS, 1, C, myArea);
			}
			if(worship!=null)
				Resources.removeResource("PIETY_"+myArea.Name().toUpperCase());

			final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CONQUESTS, null);
			for(int i=0;i<channels.size();i++)
				CMLib.commands().postChannel(channels.get(i),CMLib.clans().clanRoles(),L("@x1 gains control of @x2.",holdingClan,myArea.name()),false);
			if(journalName.length()>0)
				CMLib.database().DBWriteJournal(journalName,"Conquest","ALL",holdingClan+" gains control of "+myArea.name()+".","See the subject line.");
			conquestDate=System.currentTimeMillis();
		}
	}

	protected void registerClanItem(final ClanItem I)
	{
		synchronized(clanItems)
		{
			if(!clanItems.containsFirst(I))
				clanItems.add(I,I.owner());
			if((I.owner() instanceof Room)
			&&(I.container()!=null))
				I.setContainer(null);
			I.setExpirationDate(0);
			if((I instanceof SiegableItem)
			&&(I instanceof Boardable)
			&&(totalControlPoints>0))
				recalculateControlPoints(myArea);
		}
	}

	protected void deRegisterClanItem(final ClanItem I)
	{
		synchronized(clanItems)
		{
			try
			{
				final int x = clanItems.indexOfFirst(I);
				if(x >= 0)
					clanItems.remove(x);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	protected boolean flagFound(final Area A, final String clanID)
	{
		Clan C=CMLib.clans().getClanExact(clanID);
		if(C==null)
			C=CMLib.clans().fetchClanAnyHost(clanID);
		if((C==null)||(!C.getGovernment().isConquestEnabled()))
			return false;
		return flagFound(A,C);
	}

	protected volatile long	nextFlagScan = System.currentTimeMillis();

	protected boolean flagFound(final Area A, final Clan C)
	{
		if((C==null)||(!C.getGovernment().isConquestEnabled()))
			return false;
		synchronized(clanItems)
		{
			for(int i=0;i<clanItems.size();i++)
			{
				final ClanItem I=clanItems.getFirst(i);
				if((I.clanID().equals(C.clanID()))
				&&(!I.amDestroyed())
				&&(I.getClanItemType()==ClanItem.ClanItemType.FLAG))
				{
					final Room R=CMLib.map().roomLocation(I);
					if((R!=null)
					&&((A==null)||(A.inMyMetroArea(R.getArea()))))
					{
						if(i>0)
						{
							clanItems.remove(i);
							clanItems.add(0, new Pair<ClanItem,ItemPossessor>(I, I.owner()));
						}
						return true;
					}
				}
			}
		}
		if((holdingClan.length()>0)
		&&(holdingClan.equalsIgnoreCase(C.clanID()))
		&&(System.currentTimeMillis() > nextFlagScan)
		&&(myArea!=null))
		{
			nextFlagScan = System.currentTimeMillis() + 360000;
			// make a desperation check if we are talking about the holding clan.
			Room R=null;
			Item I=null;
			for(final Enumeration<Room> e=myArea.getCompleteMap();e.hasMoreElements();)
			{
				R=e.nextElement();
				for(int i=0;i<R.numItems();i++)
				{
					I=R.getItem(i);
					if((I instanceof ClanItem)
					&&(((ClanItem)I).getClanItemType()==ClanItem.ClanItemType.FLAG)
					&&(((ClanItem)I).clanID().equals(C.clanID()))
					&&(!I.amDestroyed()))
					{
						registerClanItem((ClanItem)I);
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void setControlPoints(final String clanID, final int newControlPoints)
	{
		synchronized(clanControlPoints)
		{
			final int[] ic=clanControlPoints.get(clanID.toUpperCase().trim());
			if(ic != null)
				ic[0] = newControlPoints;
			else
				clanControlPoints.put(clanID.toUpperCase().trim(),new int[]{newControlPoints});
			if(newControlPoints>=totalControlPoints)
				declareWinner(clanID);
		}
	}

	protected boolean changeControlPoints(final String clanID, int amount, final Room notifyRoom)
	{
		synchronized(clanControlPoints)
		{
			final int[] ic=clanControlPoints.get(clanID.toUpperCase().trim());
			if((holdingClan.length()>0)
			&&(!clanID.equals(holdingClan)))
			{
				final int rel=CMLib.clans().getCommonClanRelations(clanID,holdingClan);
				if((rel!=Clan.REL_WAR) && (rel!=Clan.REL_HOSTILE))
					return false;
			}
			if(amount > 0)
			{
				final Clan C = CMLib.clans().getClanExact(clanID);
				if(C!=null)
					C.bumpTrophyData(Clan.Trophy.MonthlyControlPoints, amount);
			}
			announceToArea(myArea,clanID,amount);
			if(ic == null)
			{
				if((holdingClan.length()>0)
				&&(!clanID.equals(holdingClan))
				&&(myArea!=null))
				{
					for(final Enumeration<Room> e=myArea.getMetroMap();e.hasMoreElements();)
					{
						final Room R=e.nextElement();
						for(int i=0;i<R.numInhabitants();i++)
						{
							final MOB M=R.fetchInhabitant(i);
							if((M!=null)
							&&(M.isMonster())
							&&(M.getStartRoom()!=null)
							&&(myArea.inMyMetroArea(M.getStartRoom().getArea()))
							&&(M.getClanRole(clanID)!=null))
								amount+=M.phyStats().level();
						}
					}
				}
				if(amount>0)
				{
					final int[] i=new int[1];
					i[0]+=amount;
					clanControlPoints.put(clanID.toUpperCase().trim(),i);
					if(i[0]>=totalControlPoints)
						declareWinner(clanID);
				}
			}
			else
			{
				ic[0]+=amount;
				if(ic[0]<=0)
					clanControlPoints.remove(clanID.toUpperCase().trim());
				else
				if(ic[0]>=totalControlPoints)
					declareWinner(clanID);
			}
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CONQUEST))
			{
				final int[] i=clanControlPoints.get(clanID.toUpperCase().trim());
				if(i==null)
					Log.debugOut(clanID+" is not getting their points calculated.");
				else
					Log.debugOut(clanID+" now has "+i[0]+" control points of "+totalControlPoints+" in "+myArea.name()+".");
			}
		}
		return true;
	}

	protected static String[] warCrys()
	{
		return DEFAULT_WAR_CRYS;
	}

	protected static final String[] DEFAULT_WAR_CRYS=
	{
		"INVADERS! Attack!",
		"We are under attack! To arms!",
		"Destroy the enemy!",
		"War!!!!!"
	};

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		Clan srcC;
		final boolean debugging=CMSecurity.isDebugging(CMSecurity.DbgFlag.CONQUEST);
		if(msg.sourceMinor()==CMMsg.TYP_CLANEVENT)
		{
			if(msg.sourceMessage().equals("CONTROLRESET"))
				recalculateControlPoints(CMLib.map().areaLocation(myArea));
			else
			if(msg.sourceMessage().startsWith("-"))
			{
				final String event=msg.sourceMessage().substring(1);
				if((holdingClan!=null)
				&&(holdingClan.equalsIgnoreCase(event)))
				{
					if(debugging)
						Log.debugOut("Conquest",holdingClan+" no longer exists.");
					endClanRule(L(" due to clan dissolution"));
				}
			}
		}
		else
		if((myHost instanceof Area)
		&&(CMProps.getBoolVar(CMProps.Bool.MUDSTARTED)
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.CONQUEST)))
		&&(totalControlPoints>=0))
		{
			// first look for kills and follows and register the points
			// from those events.  Protect against multi-follows using
			// a queue.
			if((((msg.sourceMinor()==CMMsg.TYP_DEATH)&&(msg.tool() instanceof MOB))
				||((msg.sourceMinor()==CMMsg.TYP_FOLLOW)
					&&(msg.target() instanceof MOB)
					&&(!noMultiFollows.contains(msg.source()))))
			&&(msg.source().isMonster())
			&&(msg.source().getStartRoom()!=null))
			{
				final Room R=msg.source().location();
				MOB killer=null;
				if(msg.sourceMinor()==CMMsg.TYP_FOLLOW)
				{
					if((msg.source().basePhyStats().rejuv()!=0)
					&&(msg.source().basePhyStats().rejuv()!=PhyStats.NO_REJUV))
						noMultiFollows.add(msg.source(),CMProps.getTickMillis()*msg.source().basePhyStats().rejuv());
					else
						noMultiFollows.add(msg.source());
					if(noMultiFollows.size()>10)
					{
						for(final Iterator<MOB> i = noMultiFollows.iterator();i.hasNext();)
							i.next(); // this should clear out the cruft
					}
					if(msg.target() instanceof MOB)
						killer=(MOB)msg.target();
				}
				else
				if(msg.tool() instanceof MOB)
					killer=(MOB)msg.tool();
				if((killer!=null)
				&&(R!=null))
				{
					// make sure followers are picked up
					final HashSet<MOB> killersSeen=new HashSet<MOB>();
					Clan killerClan=CMLib.clans().findConquerableClan(killer);
					while((killerClan==null)
					&&(killer.amFollowing()!=null)
					&&(R.isInhabitant(killer.amFollowing()))
					&&(!killersSeen.contains(killer)))
					{
						killersSeen.add(killer);
						killer=killer.amFollowing();
						killerClan=CMLib.clans().findConquerableClan(killer);
					}

					if(((Area)myHost).inMyMetroArea(msg.source().getStartRoom().getArea()))
					{ // a native was killed
						final MOB followerM=killer.amFollowing();
						if(killer.getClanRole(holdingClan)==null)
						{
							if(((killerClan!=null)
								||((followerM != null)&&((killerClan=CMLib.clans().findConquerableClan(followerM))!=null)))
							&&(flagFound((Area)myHost,killerClan))
							&&(killerClan!=null))
							{
								if((killer.phyStats().level()-msg.source().phyStats().level())<=conqPtLvlDiff)
								{
									int level=msg.source().phyStats().level();
									if(killerClan.isWorshipConquest()
									&&(killer.charStats().getWorshipCharID().equals(msg.source().charStats().getWorshipCharID())))
										level=(level>1)?level/2:level;
									if(debugging)
										Log.debugOut("Conquest",killerClan.getName()+" gain "+level+" points by killing "+msg.source().name());
									changeControlPoints(killerClan.clanID(),level,killer.location());
								}
							}
							else
							{
								// if a non-conquest kill, put any clan items aside
								int i = clanItems.indexOfSecond(msg.source());
								if((i>0)
								&&(msg.source().basePhyStats().rejuv()!=0)
								&&(msg.source().basePhyStats().rejuv()!=PhyStats.NO_REJUV))
								{
									for(;i<clanItems.size();i++)
									{
										final ClanItem I = clanItems.getFirst(i);
										if(((I.owner() == msg.source())||(I.owner() == msg.source().location()))
										&&(clanItems.getSecond(i) == msg.source()))
										{
											I.owner().delItem(I);
											I.removeFromOwnerContainer();
										}
									}
									msg.source().recoverCharStats();
									msg.source().recoverPhyStats();
									msg.source().recoverMaxState();
								}
							}
						}
					}
					else // a foreigner was killed
					if((holdingClan.length()>0)
					&&(killer.getClanRole(holdingClan)!=null) // killer is from holding clan
					&&((srcC=CMLib.clans().findConquerableClan(msg.source()))!=null) 	// killed is a conquesting one
					&&(msg.source().getClanRole(holdingClan)==null)
					&&(flagFound((Area)myHost,srcC))
					&&((killer.phyStats().level()-msg.source().phyStats().level())<=conqPtLvlDiff))
					{
						final Clan rivalClan=srcC;
						if(debugging)
							Log.debugOut("Conquest",rivalClan.getName()+" lose "+(msg.source().phyStats().level())+" points by allowing the death of "+msg.source().name());
						changeControlPoints(rivalClan.clanID(),-msg.source().phyStats().level(),killer.location());
					}
				}
			}
			else
			if(((msg.tool() instanceof Ability)
			&&(msg.tool().ID().equals("Skill_Convert"))
			&&(msg.target() instanceof MOB)
			&&((srcC=CMLib.clans().findConquerableClan(msg.source()))!=null)
			&&(((MOB)msg.target()).isMonster())
			&&(((MOB)msg.target()).getStartRoom()!=null))
			&&(msg.sourceMinor()!=CMMsg.TYP_TEACH))
			{
				if((srcC.isWorshipConquest())
				&&(((Area)myHost).inMyMetroArea(((MOB)msg.target()).getStartRoom().getArea()))
				&&(msg.source().getClanRole(holdingClan)==null)
				&&((msg.source().phyStats().level()-((MOB)msg.target()).phyStats().level())<=conqPtLvlDiff)
				&&(flagFound((Area)myHost,srcC)))
				{
					if(debugging)
						Log.debugOut("Conquest",srcC.getName()+" gain "+(msg.source().phyStats().level())+" points by converting "+msg.source().name());
					changeControlPoints(srcC.clanID(),((MOB)msg.target()).phyStats().level(),msg.source().location());
				}
			}
			else
			if((holdingClan.length()>0)
			&&(msg.source().isMonster())
			&&(msg.sourceMinor()==CMMsg.TYP_LIFE)
			&&(msg.source().getStartRoom()!=null)
			&&(((Area)myHost).inMyMetroArea(msg.source().getStartRoom().getArea()))
			&&(!CMLib.flags().isAnimalIntelligence(msg.source()))
			&&(msg.source().findTattoo("SYSTEM_SUMMONED")==null)
			&&(msg.source().getClanRole(holdingClan)==null))
			{
				final Clan C=CMLib.clans().getClanAnyHost(holdingClan);
				if(C!=null)
					msg.source().setClan(C.clanID(),C.getGovernment().getAcceptPos());
				final String worship=getManadatoryWorshipID();
				if((worship!=null)
				&&(!msg.source().baseCharStats().getWorshipCharID().equals(worship)))
				{
					msg.source().baseCharStats().setWorshipCharID(worship);
					msg.source().charStats().setWorshipCharID(worship);
				}
				int i = clanItems.indexOfSecond(msg.source());
				if((i>0)
				&&(msg.source().basePhyStats().rejuv()!=0)
				&&(msg.source().basePhyStats().rejuv()!=PhyStats.NO_REJUV))
				{
					for(;i<clanItems.size();i++)
					{
						final ClanItem I = clanItems.getFirst(i);
						final ItemPossessor P = clanItems.getSecond(i);
						if((P == msg.source())
						&&(!(I.owner() instanceof MOB)))
						{
							msg.source().moveItemTo(I);
							I.wearIfPossible(msg.source());
						}
					}
					msg.source().recoverCharStats();
					msg.source().recoverPhyStats();
					msg.source().recoverMaxState();
				}
			}

			if(msg.tool() instanceof ClanItem)
				registerClanItem((ClanItem)msg.tool());
			if(msg.target() instanceof ClanItem)
				registerClanItem((ClanItem)msg.target());

			if(((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
			&&(msg.target() instanceof Room)
			&&(holdingClan.length()>0)
			&&((srcC=CMLib.clans().findConquerableClan(msg.source()))!=null)
			&&(msg.source().getClanRole(holdingClan)==null)
			&&(((Room)msg.target()).numInhabitants()>0)
			&&(myArea.inMyMetroArea(((Room)msg.target()).getArea())))
			{
				final Clan C=CMLib.clans().getClanAnyHost(holdingClan);
				if(C==null)
				{
					if(debugging)
						Log.debugOut("Conquest",holdingClan+" no longer exists.");
					endClanRule(L(" due to an unclaimed flag"));
				}
				else
				if(C.getClanRelations(srcC.clanID())==Clan.REL_WAR)
				{
					final Room R=(Room)msg.target();
					for(int i=0;i<R.numInhabitants();i++)
					{
						final MOB M=R.fetchInhabitant(i);
						if((M!=null)
						&&(M.isMonster())
						&&(M.getClanRole(holdingClan)!=null)
						&&(!M.isInCombat())
						&&(!CMLib.flags().isAnimalIntelligence(M))
						&&(CMLib.flags().isAliveAwakeMobileUnbound(M,true))
						&&(CMLib.flags().canBeSeenBy(msg.source(),M))
						&&(!assaults.containsFirst(M))
						&&(msg.source().getClanRole(holdingClan)==null)
						&&(CMLib.clans().findConquerableClan(msg.source())!=null))
							assaults.add(M,msg.source());
					}
				}
			}
		}
		if(((msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
			||(msg.sourceMinor()==CMMsg.TYP_ROOMRESET))
		&&(myArea!=null)
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.CONQUEST)))
		{

			if(msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
				waitToReload=0;
			else
				waitToReload=System.currentTimeMillis()+60000;
			if((totalControlPoints>=0)
			&&((!savedHoldingClan.equals(""))||(!holdingClan.equals(""))))
			{
				totalControlPoints=-1;
				final StringBuffer data=new StringBuffer("");
				data.append(CMLib.xml().convertXMLtoTag("CLANID",holdingClan));
				data.append(CMLib.xml().convertXMLtoTag("OLDCLANID",prevHoldingClan));
				data.append(CMLib.xml().convertXMLtoTag("CLANDATE",conquestDate));
				data.append("<ACITEMS>");
				synchronized(clanItems)
				{
					for(int i=0;i<clanItems.size();i++)
					{
						final ClanItem I=clanItems.getFirst(i);
						final Room R=CMLib.map().roomLocation(I);
						if((R!=null)
						&&(((Area)myHost).inMyMetroArea(R.getArea()))
						&&(!((Item)I).amDestroyed())
						&&((!(I.owner() instanceof MOB))||(((MOB)I.owner()).isMonster()))
						&&((I.getClanItemType()!=ClanItem.ClanItemType.FLAG)||(R.isContent(I))))
						{
							data.append("<ACITEM>");
							if(((Item)I).owner() instanceof Room)
								data.append(CMLib.xml().convertXMLtoTag("ROOMID",CMLib.map().getExtendedRoomID(R)));
							else
							if(((Item)I).owner() instanceof MOB)
							{
								final MOB M=(MOB)((Item)I).owner();
								if((M.getStartRoom()!=null)
								&&(myArea.inMyMetroArea(M.getStartRoom().getArea())))
								{
									data.append(CMLib.xml().convertXMLtoTag("ROOMID",CMLib.map().getExtendedRoomID(M.getStartRoom())));
									data.append(CMLib.xml().convertXMLtoTag("MOB",M.Name()));
								}
							}
							data.append(CMLib.xml().convertXMLtoTag("ICLAS",CMClass.classID(I)));
							data.append(CMLib.xml().convertXMLtoTag("IREJV",I.basePhyStats().rejuv()));
							data.append(CMLib.xml().convertXMLtoTag("IUSES",((Item)I).usesRemaining()));
							data.append(CMLib.xml().convertXMLtoTag("ILEVL",I.basePhyStats().level()));
							data.append(CMLib.xml().convertXMLtoTag("IABLE",I.basePhyStats().ability()));
							data.append(CMLib.xml().convertXMLtoTag("ITEXT",CMLib.xml().parseOutAngleBrackets(I.text())));
							data.append("</ACITEM>");
							((Item)I).destroy();
						}
					}
					clanItems.clear();
				}
				savedHoldingClan="";
				holdingClan="";
				prevHoldingClan="";
				clanControlPoints=new STreeMap<String,int[]>();
				data.append("</ACITEMS>");
				CMLib.database().DBReCreatePlayerData(myArea.name(),"CONQITEMS","CONQITEMS/"+myArea.name(),data.toString());
			}
		}
	}

	@Override
	protected boolean isAnUltimateAuthorityHere(final MOB M, final Law laws)
	{
		if((M==null)
		||(holdingClan.length()==0)
		||(!allowLaw)
		||(totalControlPoints<0)
		||(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED)))
			return false;
		final Clan C=CMLib.clans().getClanAnyHost(holdingClan);
		if(C==null)
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CONQUEST))
				Log.debugOut("Conquest",holdingClan+" no longer exists.");
			endClanRule(L(" due to a lack of legal authority"));
			return false;
		}
		final Pair<Clan,Integer> clanRole=M.getClanRole(C.clanID());
		if(clanRole==null)
			return false;
		return C.getAuthority(clanRole.second.intValue(),Clan.Function.ORDER_CONQUERED)==Clan.Authority.CAN_DO;
	}

	@Override
	protected boolean theLawIsEnabled()
	{
		if((holdingClan.length()==0)
		||(!allowLaw)
		||(totalControlPoints<0)
		||(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
		||(CMSecurity.isDisabled(CMSecurity.DisFlag.ARREST)))
			return false;
		if(flagFound(null,holdingClan))
			return true;
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CONQUEST))
		{
			Log.debugOut("Conquest",holdingClan+" has "+totalControlPoints+" points and flag="
				+flagFound(null,holdingClan)+" in law check.");
		}
		endClanRule(L(" due to illegal conquest"));
		return false;
	}
}
