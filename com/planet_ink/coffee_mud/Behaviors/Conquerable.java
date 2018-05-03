package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;

/*
   Copyright 2004-2018 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
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
	protected Vector<ClanItem>	clanItems			= new Vector<ClanItem>();
	protected DVector			clanControlPoints	= new DVector(2);
	protected DVector			assaults			= new DVector(2);
	protected Vector<MOB>		noMultiFollows		= new Vector<MOB>();
	protected int				totalControlPoints	= -1;
	protected Area				myArea				= null;
	protected String			journalName			= "";
	protected boolean			allowLaw			= false;
	protected boolean			switchOwnership		= false;
	protected boolean			REVOLTNOW			= false;
	protected long				waitToReload		= 0;
	protected long				conquestDate		= 0;
	protected int				revoltDown			= REVOLTFREQ;
	protected static final int	REVOLTFREQ			= (int) ((TimeManager.MILI_DAY * 3) / CMProps.getTickMillis());
	protected int				checkDown			= 0;
	protected static final int	CHECKFREQ			= 10;
	protected int				pointDown			= 0;
	protected static final int	POINTFREQ			= (int) ((10 * 60000) / CMProps.getTickMillis());
	protected int				fightDown			= 0;
	protected static final int	FIGHTFREQ			= 2;

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
	public CMObject copyOf()
	{
		final Conquerable obj=(Conquerable)super.copyOf();
		obj.clanItems=(Vector)clanItems.clone();
		obj.clanControlPoints=clanControlPoints.copyOf();
		obj.assaults=assaults.copyOf();
		obj.noMultiFollows=(Vector)noMultiFollows.clone();
		return obj;
	}

	@Override
	public String conquestInfo(Area myArea)
	{
		final StringBuffer str=new StringBuffer("");
		if((totalControlPoints<0)&&(myArea!=null))
			recalculateControlPoints(myArea);
		if((holdingClan.length()==0)||(totalControlPoints<0))
			str.append(L("Area '@x1' is not currently controlled by any clan.\n\r",myArea.name()));
		else
		{
			final Clan C=CMLib.clans().getClan(holdingClan);
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
				endClanRule();
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
			for(int i=0;i<clanControlPoints.size();i++)
			{
				final String clanID=(String)clanControlPoints.elementAt(i,1);
				final int[] ic=(int[])clanControlPoints.elementAt(i,2);
				final Clan C=CMLib.clans().getClan(clanID);
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
	public int getControlPoints(String clanID)
	{
		if((clanID==null)||(clanID.length()==0))
			return 0;
		synchronized(clanControlPoints)
		{
			for(int i=0;i<clanControlPoints.size();i++)
			{
				final String clanID2=(String)clanControlPoints.elementAt(i,1);
				final int[] ic=(int[])clanControlPoints.elementAt(i,2);
				if(clanID2.equalsIgnoreCase(clanID))
				{
					final Clan C=CMLib.clans().getClan(clanID);
					if(C!=null)
						return ic[0];
				}
			}
		}
		return 0;
	}

	@Override
	public int revoltChance()
	{
		if(myArea==null)
			return 100;
		final Clan C=CMLib.clans().getClan(holdingClan);
		if((C==null)||(C.isLoyaltyThroughItems()))
			return calcRevoltChance(myArea);
		return 0;
	}

	@Override
	public void setParms(String newParms)
	{
		super.setParms(newParms);
		journalName=CMParms.getParmStr(newParms,"JOURNAL","");
		allowLaw=CMParms.getParmStr(newParms,"LAW","FALSE").toUpperCase().startsWith("T");
		switchOwnership=CMParms.getParmStr(newParms,"OWNERSHIP","TRUE").toUpperCase().startsWith("T");
		loadAttempt=false;
		clanItems=new Vector<ClanItem>();
		clanControlPoints=new DVector(2);
		assaults=new DVector(2);
		noMultiFollows=new Vector<MOB>();
	}

	@Override
	public void startBehavior(PhysicalAgent E)
	{
		super.startBehavior(E);
		CMLib.map().addGlobalHandler(this, CMMsg.TYP_CLANEVENT);
	}

	@Override
	public boolean isAnyKindOfOfficer(Law laws, MOB M)
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
	public boolean isTheJudge(Law laws, MOB M)
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

	protected synchronized void endClanRule()
	{
		if(holdingClan.length()==0)
			return;
		if((!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
		||(CMSecurity.isDisabled(CMSecurity.DisFlag.CONQUEST)))
			return;
		final Clan C=CMLib.clans().getClan(holdingClan);
		final String worship=getManadatoryWorshipID();
		prevHoldingClan=holdingClan;
		for(int v=0;v<clanItems.size();v++)
		{
			final Item I=clanItems.elementAt(v);
			if((I.owner() instanceof MOB)
			&&(I instanceof ClanItem)
			&&(((ClanItem)I).clanID().equals(holdingClan)))
			{
				final MOB M=(MOB)I.owner();
				if((M.location()!=null)&&(!M.amDead())&&(M.isMonster()))
				{
					M.delItem(I);
					if(M.getClanRole(holdingClan)!=null)
					{
						M.setClan(holdingClan,-1);
						if((worship!=null)&&(M.getWorshipCharID().equals(worship)))
							M.setWorshipCharID("");
					}
					I.setRawWornCode(0);
					I.setContainer(null);
					M.location().addItem(I,ItemPossessor.Expire.Player_Drop);
				}
			}
		}

		if(myArea!=null)
		{
			for(final Enumeration e=myArea.getMetroMap();e.hasMoreElements();)
			{
				final Room R=(Room)e.nextElement();
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
						if((worship!=null)&&(M.getWorshipCharID().equals(worship)))
							M.setWorshipCharID("");
					}
				}
			}
			if(holdingClan.length()>0)
			{
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CONQUEST))
					Log.debugOut("Conquest",holdingClan+" has lost control of "+myArea.name()+".");
				final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CONQUESTS);
				for(int i=0;i<channels.size();i++)
					CMLib.commands().postChannel(channels.get(i),CMLib.clans().clanRoles(),L("@x1 has lost control of @x2.",holdingClan,myArea.name()),false);
				if(journalName.length()>0)
					CMLib.database().DBWriteJournal(journalName,"Conquest","ALL",holdingClan+" loses control of "+myArea.name()+".","See the subject line.");
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
					final ClanItem item=clanItems.elementAt(c);
					if((C==null)
					&&(item.clanID().equalsIgnoreCase(holdingClan))
					&&((!(item.owner() instanceof MOB))||(((MOB)item.owner()).isMonster())))
					{
						item.destroy();
						clanItems.removeElementAt(c);
					}
					else
					if(item.getClanItemType()!=ClanItem.ClanItemType.FLAG)
						deRegisterClanItem(clanItems.elementAt(c));
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

	public int calcItemControlPoints(Area A)
	{
		int itemControlPoints=0;
		synchronized(clanItems)
		{
			for(int i=clanItems.size()-1;i>=0;i--)
			{
				final ClanItem I=clanItems.elementAt(i);
				if((!I.amDestroyed())
				&&(I.owner() instanceof MOB)
				&&(((MOB)I.owner()).isMonster())
				&&(CMLib.flags().isInTheGame((MOB)I.owner(),true))
				&&(A.inMyMetroArea(((MOB)I.owner()).getStartRoom().getArea()))
				&&((holdingClan.length()==0)||(I.clanID().equals(holdingClan)))
				&&(I.getClanItemType()!=ClanItem.ClanItemType.PROPAGANDA))
					itemControlPoints+=((MOB)((Item)I).owner()).phyStats().level();
			}
		}
		return itemControlPoints;
	}

	public int calcRevoltChance(Area A)
	{
		if(totalControlPoints<=0)
			return 0;
		final int itemControlPoints=calcItemControlPoints(A);
		int totalNeeded=(int)Math.round(CMath.mul(0.05,totalControlPoints));
		if(totalNeeded<=0)
			totalNeeded=1;
		final int chance=(int)Math.round(10.0-(CMath.mul(10.0,CMath.div(itemControlPoints,totalNeeded))));
		if(chance<=0)
			return 0;
		return chance;

	}

	protected void announceToArea(Area area, String clanID, int amount)
	{
		for(final Session S : CMLib.sessions().localOnlineIterable())
		{
			if((S.mob()!=null)
			&&(S.mob().location()!=null)
			&&(area.inMyMetroArea(S.mob().location().getArea())))
				S.println(L("@x1 @x2 control points.",clanID,(amount<0?"loses "+(-amount):"gains "+amount)));
		}
	}

	protected boolean hasItemSameAs(MOB M, Item I)
	{
		for(final Enumeration<Item> i=M.items();i.hasMoreElements();)
		{
			if(I.sameAs(i.nextElement()))
				return true;
		}
		return false;
	}

	protected boolean hasItemSameName(MOB M, String name)
	{
		for(final Enumeration<Item> i=M.items();i.hasMoreElements();)
		{
			if(name.equals(i.nextElement().Name()))
				return true;
		}
		return false;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
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
			final Item I=clanItems.elementAt(i);
			if(!I.tick(this,Tickable.TICKID_CLANITEM))
				deRegisterClanItem(I);
			else
			{
				I.setExpirationDate(0);
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
										for(final Enumeration e=A.getMetroMap();e.hasMoreElements();)
										{
											final Room R2=(Room)e.nextElement();
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
						final ClanItem I=clanItems.elementAt(i);
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
					&&(CMLib.clans().getClan(prevHoldingClan)!=null)
					&&(flagFound(A,prevHoldingClan)))
						declareWinner(prevHoldingClan);
					else
						endClanRule();
				}
			}

			if((--revoltDown)<=0)
			{
				revoltDown=Conquerable.REVOLTFREQ;
				if(holdingClan.length()>0)
				{
					final Clan C=CMLib.clans().getClan(holdingClan);
					if((C==null)||(C.isLoyaltyThroughItems()))
					{
						final int chance=calcRevoltChance(A);
						if((REVOLTNOW)&&(chance<100))
						{
							Log.sysOut("Conquerable",A.Name()+" revolted against "+holdingClan+" with "+chance+"% chance");
							if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CONQUEST))
								Log.debugOut("Conquest","The inhabitants of "+myArea.name()+" have revolted against "+holdingClan+" with "+chance+"% chance, after "+calcItemControlPoints(myArea)+" item points of "+totalControlPoints+" control points.");
							final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CONQUESTS);
							for(int i=0;i<channels.size();i++)
								CMLib.commands().postChannel(channels.get(i),CMLib.clans().clanRoles(),L("The inhabitants of @x1 have revolted against @x2.",myArea.name(),holdingClan),false);
							if(journalName.length()>0)
								CMLib.database().DBWriteJournal(journalName,"Conquest","ALL","The inhabitants of "+myArea.name()+" have revolted against "+holdingClan+".","See the subject line.");
							if((prevHoldingClan.length()>0)
							&&(!holdingClan.equalsIgnoreCase(prevHoldingClan))
							&&(CMLib.clans().getClan(prevHoldingClan)!=null)
							&&(flagFound(A,prevHoldingClan)))
								declareWinner(prevHoldingClan);
							else
								endClanRule();
						}
						else
						{
							if(CMLib.dice().rollPercentage()<chance)
							{
								final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CONQUESTS);
								for(int i=0;i<channels.size();i++)
									CMLib.commands().postChannel(channels.get(i),CMLib.clans().clanRoles(),L("There are the rumblings of revolt in @x1.",myArea.name()),false);
							}
						}
					}
				}
			}

			if((--pointDown)<=0)
			{
				pointDown=POINTFREQ;
				// slowly decrease control points over time
				synchronized(clanControlPoints)
				{
					for(int v=clanControlPoints.size()-1;v>=0;v--)
					{
						final int[] pts=(int[])clanControlPoints.elementAt(v,2);
						if(pts[0]<=1)
							clanControlPoints.removeElementAt(v);
						else
							pts[0]--;
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
							final MOB M1=(MOB)assaults.elementAt(0,1);
							final MOB M2=(MOB)assaults.elementAt(0,2);
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
							assaults.removeElementAt(0);
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
		final Clan C=CMLib.clans().getClan(holdingClan);
		if(C==null)
			return null;
		if(C.isWorshipConquest())
		{
			final MOB M=C.getResponsibleMember();
			if((M!=null)&&(M.getWorshipCharID().length()>0))
				return M.getWorshipCharID();
		}
		return null;
	}

	public void recalculateControlPoints(Area A)
	{
		totalControlPoints=0;
		final String worship=getManadatoryWorshipID();
		final Clan C=(holdingClan!=null)?CMLib.clans().getClan(holdingClan):null;
		for(final Enumeration e=A.getMetroMap();e.hasMoreElements();)
		{
			final Room R=(Room)e.nextElement();
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
						if(worship!=null)
							M.setWorshipCharID(worship);
					}
					totalControlPoints+=M.phyStats().level();
				}
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
					final ClanItem I=clanItems.elementAt(i);
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
			&&(msg.tool().ID().startsWith("Prayer_Infuse")))
			{
				if((msg.source().getClanRole(holdingClan)==null)
				||(CMLib.clans().getClan(holdingClan)==null)
				||(!CMLib.clans().getClan(holdingClan).isWorshipConquest()))
				{
					msg.source().tell(L("Only a member of a conquering deity clan can pray for that here."));
					return false;
				}
			}

			if((msg.sourceMinor()==CMMsg.TYP_EXPCHANGE)
			&&((msg.source().getStartRoom()==null)||(!myArea.inMyMetroArea(msg.source().getStartRoom().getArea())))
			&&(msg.value()>0))
			{
				final Clan holdingC=CMLib.clans().getClan(holdingClan);
				if(holdingC.getTaxes()!=0)
				{
					final MOB target=(msg.target() instanceof MOB)?(MOB)msg.target():null;
					final int lossAmt=(int)Math.round(CMath.mul(msg.value(),holdingC.getTaxes()));
					final int clanAmt=(int)Math.round(CMath.mul(CMLib.leveler().adjustedExperience(msg.source(),target,msg.value()),holdingC.getTaxes()));
					if(lossAmt>0)
					{
						msg.setValue(msg.value()-lossAmt);
						holdingC.adjExp(clanAmt);
						holdingC.update();
					}
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

	protected void declareWinner(String clanID)
	{
		if((holdingClan.equals(clanID))||(totalControlPoints<0))
			return;
		final Clan C=CMLib.clans().findClan(clanID);
		if((C==null)||(!C.getGovernment().isConquestEnabled()))
			return;

		final MOB mob=CMLib.map().getFactoryMOBInAnyRoom();
		mob.setName(clanID);
		if(myArea!=null)
		{
			for(final Enumeration e=myArea.getMetroMap();e.hasMoreElements();)
			{
				if(!((Room)e.nextElement()).show(mob,myArea,null,CMMsg.MSG_AREAAFFECT,null,CMMsg.MSG_AREAAFFECT,L("CONQUEST"),CMMsg.MSG_AREAAFFECT,null))
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
			endClanRule();

		revoltDown=REVOLTFREQ;
		holdingClan=clanID;
		synchronized(clanControlPoints)
		{
			clanControlPoints.clear();
		}
		if(myArea!=null)
		{
			LandTitle areaTitle=CMLib.law().getLandTitle(myArea);
			if(this.switchOwnership 
			&& (areaTitle!=null) 
			&& (!areaTitle.getOwnerName().equalsIgnoreCase(holdingClan)))
				areaTitle.setOwnerName(holdingClan);
			final String worship=getManadatoryWorshipID();
			for(final Enumeration e=myArea.getMetroMap();e.hasMoreElements();)
			{
				final Room R=(Room)e.nextElement();
				if(R!=null)
				{
					LandTitle T=CMLib.law().getLandTitle(R);
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
							if(worship!=null)
								M.setWorshipCharID(worship);
						}
					}
				}
			}
			final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CONQUESTS);
			for(int i=0;i<channels.size();i++)
				CMLib.commands().postChannel(channels.get(i),CMLib.clans().clanRoles(),L("@x1 gains control of @x2.",holdingClan,myArea.name()),false);
			if(journalName.length()>0)
				CMLib.database().DBWriteJournal(journalName,"Conquest","ALL",holdingClan+" gains control of "+myArea.name()+".","See the subject line.");
			conquestDate=System.currentTimeMillis();
		}
	}

	protected void registerClanItem(ClanItem I)
	{
		synchronized(clanItems)
		{
			if(!clanItems.contains(I))
				clanItems.addElement(I);
			if((I.owner() instanceof Room)
			&&(I.container()!=null))
				I.setContainer(null);
			I.setExpirationDate(0);
		}
	}

	protected void deRegisterClanItem(Item I)
	{
		synchronized(clanItems)
		{
			try
			{
				clanItems.removeElement(I);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	protected boolean flagFound(Area A, String clanID)
	{
		final Clan C=CMLib.clans().findClan(clanID);
		if((C==null)||(!C.getGovernment().isConquestEnabled()))
			return false;
		return flagFound(A,C);
	}

	protected boolean flagFound(Area A, Clan C)
	{
		if((C==null)||(!C.getGovernment().isConquestEnabled()))
			return false;
		synchronized(clanItems)
		{
			for(int i=0;i<clanItems.size();i++)
			{
				final ClanItem I=clanItems.elementAt(i);
				if((I.clanID().equals(C.clanID()))
				&&(!I.amDestroyed())
				&&(I.getClanItemType()==ClanItem.ClanItemType.FLAG))
				{
					final Room R=CMLib.map().roomLocation(I);
					if((R!=null)&&((A==null)||(A.inMyMetroArea(R.getArea()))))
						return true;
				}
			}
		}
		if((holdingClan.length()>0)&&(holdingClan.equalsIgnoreCase(C.clanID()))&&(myArea!=null))
		{
			// make a desperation check if we are talking about the holding clan.
			Room R=null;
			Item I=null;
			for(final Enumeration e=myArea.getCompleteMap();e.hasMoreElements();)
			{
				R=(Room)e.nextElement();
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
	public void setControlPoints(String clanID, int newControlPoints)
	{
		synchronized(clanControlPoints)
		{
			int index=-1;
			for(int v=0;v<clanControlPoints.size();v++)
			{
				if(((String)clanControlPoints.elementAt(v,1)).equalsIgnoreCase(clanID))
				{
					index = v;
					break;
				}
			}
			if(index>=0)
				clanControlPoints.setElementAt(index,2,new int[]{newControlPoints});
			else
				clanControlPoints.addElement(clanID,new int[]{newControlPoints});
			if(newControlPoints>=totalControlPoints)
				declareWinner(clanID);
		}
	}

	protected boolean changeControlPoints(String clanID, int amount, Room notifyRoom)
	{
		synchronized(clanControlPoints)
		{
			int index=-1;
			for(int v=0;v<clanControlPoints.size();v++)
			{
				if(((String)clanControlPoints.elementAt(v,1)).equalsIgnoreCase(clanID))
				{
					index=v;
					break;
				}
			}
			if((holdingClan.length()>0)
			&&(!clanID.equals(holdingClan))
			&&(CMLib.clans().getClanRelations(clanID,holdingClan)!=Clan.REL_WAR)
			&&(CMLib.clans().getClanRelations(holdingClan,clanID)!=Clan.REL_WAR))
				return false;
			announceToArea(myArea,clanID,amount);
			if(index<0)
			{
				if((holdingClan.length()>0)
				&&(!clanID.equals(holdingClan))
				&&(myArea!=null))
				{
					for(final Enumeration e=myArea.getMetroMap();e.hasMoreElements();)
					{
						final Room R=(Room)e.nextElement();
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
					clanControlPoints.addElement(clanID,i);
					if(i[0]>=totalControlPoints)
						declareWinner(clanID);
				}
			}
			else
			{
				final int[] i=(int[])clanControlPoints.elementAt(index,2);
				i[0]+=amount;
				if(i[0]<=0)
					clanControlPoints.removeElementAt(index);
				else
				if(i[0]>=totalControlPoints)
					declareWinner((String)clanControlPoints.elementAt(index,1));
			}
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CONQUEST))
			{
				index=clanControlPoints.indexOf(clanID);
				if(index<0)
					Log.debugOut(clanID+" is not getting their points calculated.");
				else
				{
					final int[] i=(int[])clanControlPoints.elementAt(index,2);
					if(i==null)
						Log.debugOut(clanID+" is not getting their points calculated.");
					else
						Log.debugOut(clanID+" now has "+i[0]+" control points of "+totalControlPoints+" in "+myArea.name()+".");
				}
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
		if((msg.sourceMinor()==CMMsg.TYP_CLANEVENT)
		&&(msg.sourceMessage().startsWith("-")))
		{
			if((holdingClan!=null)&&(holdingClan.equalsIgnoreCase(msg.sourceMessage().substring(1))))
			{
				if(debugging)
					Log.debugOut("Conquest",holdingClan+" no longer exists.");
				endClanRule();
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
					if(noMultiFollows.size()>=70)
						noMultiFollows.removeElementAt(0);
					noMultiFollows.addElement(msg.source());
					if(msg.target() instanceof MOB)
						killer=(MOB)msg.target();
				}
				else
				if(msg.tool() instanceof MOB)
					killer=(MOB)msg.tool();
				if((killer!=null)&&(R!=null))
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
						if((killer.getClanRole(holdingClan)==null)
						&&(flagFound((Area)myHost,killerClan)))
						{
							if(killerClan!=null)
							{
								int level=msg.source().phyStats().level();
								if(killerClan.isWorshipConquest()
								&&(killer.getWorshipCharID().equals(msg.source().getWorshipCharID())))
									level=(level>1)?level/2:level;
								if(debugging)
									Log.debugOut("Conquest",killerClan.getName()+" gain "+level+" points by killing "+msg.source().name());
								changeControlPoints(killerClan.clanID(),level,killer.location());
							}
							else
							if(followerM!=null)
							{
								final Clan killerFollowerClan=CMLib.clans().findConquerableClan(followerM);
								if(killerFollowerClan!=null)
								{
									final Clan C=killerFollowerClan;
									int level=msg.source().phyStats().level();
									if(C.isWorshipConquest()
									&&(killer.amFollowing().getWorshipCharID().equals(msg.source().getWorshipCharID())))
										level=(level>1)?level/2:level;
									if(debugging)
										Log.debugOut("Conquest",killerFollowerClan.getName()+" gain "+level+" points by killing "+msg.source().name());
									changeControlPoints(killerFollowerClan.clanID(),level,killer.location());
								}
							}
						}
					}
					else // a foreigner was killed
					if((holdingClan.length()>0)
					&&(killer.getClanRole(holdingClan)!=null) // killer is from holding clan
					&&((srcC=CMLib.clans().findConquerableClan(msg.source()))!=null) 	// killed is a conquesting one
					&&(msg.source().getClanRole(holdingClan)==null)
					&&(flagFound((Area)myHost,srcC)))
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
			&&(((MOB)msg.target()).getStartRoom()!=null)))
			{
				if((srcC.isWorshipConquest())
				&&(((Area)myHost).inMyMetroArea(((MOB)msg.target()).getStartRoom().getArea()))
				&&(msg.source().getClanRole(holdingClan)==null)
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
			&&(msg.source().getClanRole(holdingClan)==null))
			{
				final Clan C=CMLib.clans().getClan(holdingClan);
				if(C!=null)
					msg.source().setClan(C.clanID(),C.getGovernment().getAcceptPos());
				final String worship=getManadatoryWorshipID();
				if(worship!=null)
					msg.source().setWorshipCharID(worship);
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
				final Clan C=CMLib.clans().getClan(holdingClan);
				if(C==null)
				{
					if(debugging)
						Log.debugOut("Conquest",holdingClan+" no longer exists.");
					endClanRule();
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
						&&(!assaults.contains(M))
						&&(msg.source().getClanRole(holdingClan)==null)
						&&(CMLib.clans().findConquerableClan(msg.source())!=null))
							assaults.addElement(M,msg.source());
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
						final ClanItem I=clanItems.elementAt(i);
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
				clanControlPoints=new DVector(2);
				data.append("</ACITEMS>");
				CMLib.database().DBReCreatePlayerData(myArea.name(),"CONQITEMS","CONQITEMS/"+myArea.name(),data.toString());
			}
		}
	}

	@Override
	protected boolean isAnUltimateAuthorityHere(MOB M, Law laws)
	{
		if((M==null)
		||(holdingClan.length()==0)
		||(!allowLaw)
		||(totalControlPoints<0)
		||(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED)))
			return false;
		final Clan C=CMLib.clans().getClan(holdingClan);
		if(C==null)
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CONQUEST))
				Log.debugOut("Conquest",holdingClan+" no longer exists.");
			endClanRule();
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
		endClanRule();
		return false;
	}
}
