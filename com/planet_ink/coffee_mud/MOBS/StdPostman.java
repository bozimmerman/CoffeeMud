package com.planet_ink.coffee_mud.MOBS;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2005-2018 Bo Zimmerman

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

public class StdPostman extends StdShopKeeper implements PostOffice
{
	@Override
	public String ID()
	{
		return "StdPostman";
	}

	protected double	minimumPostage		= 1.0;
	protected double	postagePerPound		= 1.0;
	protected double	holdFeePerPound		= 1.0;
	protected double	feeForNewBox		= 50.0;
	protected int		maxMudMonthsHeld	= 12;
	private long		postalWaitTime		= -1;

	protected static Map<String,Long>	postalTimes	= new Hashtable<String,Long>();
	
	public StdPostman()
	{
		super();
		username="a postman";
		setDescription("He\\`s making a speedy delivery!");
		setDisplayText("The local postman is waiting to serve you.");
		CMLib.factions().setAlignment(this,Faction.Align.GOOD);
		setMoney(0);
		whatIsSoldMask=ShopKeeper.DEAL_POSTMAN;
		basePhyStats.setWeight(150);
		setWimpHitPoint(0);

		baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,16);
		baseCharStats().setStat(CharStats.STAT_CHARISMA,25);

		basePhyStats().setArmor(0);

		baseState.setHitPoints(1000);

		recoverMaxState();
		resetToMaxState();
		recoverPhyStats();
		recoverCharStats();
	}

	@Override
	public double minimumPostage()
	{
		return minimumPostage;
	}

	@Override
	public void setMinimumPostage(double d)
	{
		minimumPostage = d;
	}

	@Override
	public double postagePerPound()
	{
		return postagePerPound;
	}

	@Override
	public void setPostagePerPound(double d)
	{
		postagePerPound = d;
	}

	@Override
	public double holdFeePerPound()
	{
		return holdFeePerPound;
	}

	@Override
	public void setHoldFeePerPound(double d)
	{
		holdFeePerPound = d;
	}

	@Override
	public double feeForNewBox()
	{
		return feeForNewBox;
	}

	@Override
	public void setFeeForNewBox(double d)
	{
		feeForNewBox = d;
	}

	@Override
	public int maxMudMonthsHeld()
	{
		return maxMudMonthsHeld;
	}

	@Override
	public void setMaxMudMonthsHeld(int months)
	{
		maxMudMonthsHeld = months;
	}

	@Override
	public void addSoldType(int mask)
	{
		setWhatIsSoldMask(CMath.abs(mask));
	}

	@Override
	public void setWhatIsSoldMask(long newSellCode)
	{
		super.setWhatIsSoldMask(newSellCode);
		if(!isSold(ShopKeeper.DEAL_CLANPOSTMAN))
			whatIsSoldMask=ShopKeeper.DEAL_POSTMAN;
		else
			whatIsSoldMask=ShopKeeper.DEAL_CLANPOSTMAN;
	}

	@Override
	public String postalChain()
	{
		return text();
	}

	@Override
	public void setPostalChain(String name)
	{
		setMiscText(name);
	}

	@Override
	public String postalBranch()
	{
		return CMLib.map().getExtendedRoomID(getStartRoom());
	}

	@Override
	public String getSenderName(MOB mob, Clan.Function func, boolean checked)
	{
		if(isSold(ShopKeeper.DEAL_CLANPOSTMAN))
		{
			final Pair<Clan,Integer> clanPair=CMLib.clans().findPrivilegedClan(mob, func);
			if(clanPair!=null)
				return clanPair.first.clanID();
			else
			if(checked)
			{
				if(mob.clans().iterator().hasNext())
				{
					CMLib.commands().postSay(this,mob,L("I'm sorry, you aren't authorized by your clan to do that."),true,false);
					return null;
				}
				else
				{
					CMLib.commands().postSay(this,mob,L("I'm sorry, I only deal with clans."),true,false);
					return null;
				}
			}
		}
		return mob.Name();
	}

	@Override
	public void addToBox(String mob, Item thisThang, String from, String to, long holdTime, double COD)
	{
		final String name=thisThang.ID();
		CMLib.catalog().updateCatalogIntegrity(thisThang);
		CMLib.database().DBCreatePlayerData(mob,
				postalChain(),
				postalBranch()+";"+thisThang+Math.random(),
				from+";"
				+to+";"
				+holdTime+";"
				+COD+";"
				+name+";"
				+CMLib.coffeeMaker().getPropertiesStr(thisThang,true));
	}

	@Override
	public boolean delFromBox(String boxName, Item thisThang)
	{
		final List<PlayerData> V=getBoxRowPDData(boxName);
		boolean found=false;
		for(int v=V.size()-1;v>=0;v--)
		{
			final DatabaseEngine.PlayerData PD=V.get(v);
			if((PD!=null)
			&&(PD.key().startsWith(postalBranch()+";")))
			{
				final Item I=makeItem(parsePostalItemData(PD.xml()));
				if(I==null)
					continue;
				if(thisThang.sameAs(I))
				{
					found=true;
					CMLib.database().DBDeletePlayerData(PD.who(),PD.section(),PD.key());
					break;
				}
			}
		}
		return found;
	}

	@Override
	public void emptyBox(String boxName)
	{
		CMLib.database().DBDeletePlayerData(boxName,postalChain());
	}

	@Override
	public Map<String, String> getOurOpenBoxes(String boxName)
	{
		final Hashtable<String,String> branches=new Hashtable<String,String>();
		final List<PlayerData> V=CMLib.database().DBReadPlayerData(boxName,postalChain());
		if(V==null)
			return branches;
		for(int v=0;v<V.size();v++)
		{
			final DatabaseEngine.PlayerData PD=V.get(v);
			if(PD!=null)
			{
				final String key=PD.key();
				final int x=key.indexOf('/');
				if(x>0)
					branches.put(key.substring(0,x),key.substring(x+1));
			}
		}
		return branches;
	}

	@Override
	public void createBoxHere(String boxName, String forward)
	{
		if(!getOurOpenBoxes(boxName).containsKey(postalBranch()))
		{
			CMLib.database().DBCreatePlayerData(boxName,
					postalChain(),
					postalBranch()+"/"+forward,
					"50");
		}
	}

	@Override
	public void deleteBoxHere(String boxName)
	{
		final List<PlayerData> V=getBoxRowPDData(boxName);
		if(V==null)
			return;
		for(int v=0;v<V.size();v++)
		{
			final DatabaseEngine.PlayerData PD=V.get(v);
			if((PD!=null)
			&&(PD.key().startsWith(postalBranch()+"/")))
			{
				CMLib.database().DBDeletePlayerData(PD.who(),PD.section(),PD.key());
			}
		}
	}

	public Vector<PlayerData> getAllLocalBoxPD(String boxName)
	{
		final List<PlayerData> V=getBoxRowPDData(boxName);
		final Vector<PlayerData> mine=new Vector<PlayerData>();
		for(int v=0;v<V.size();v++)
		{
			final DatabaseEngine.PlayerData PD=V.get(v);
			if((PD!=null)
			&&(PD.key().startsWith(postalBranch()+";")))
			{
				mine.addElement(PD);
			}
		}
		return mine;
	}

	public List<PlayerData> getBoxRowPDData(String mob)
	{
		return CMLib.database().DBReadPlayerData(mob,postalChain());
	}

	@Override
	public Item findBoxContents(String boxName, String likeThis)
	{
		final List<PlayerData> V=getBoxRowPDData(boxName);
		for(int v=0;v<V.size();v++)
		{
			final DatabaseEngine.PlayerData PD=V.get(v);
			if((PD!=null)
			&&(PD.key().startsWith(postalBranch()+";")))
			{
				final Item I=makeItem(parsePostalItemData(PD.xml()));
				if(I==null)
					continue;
				if(CMLib.english().containsString(I.Name(),likeThis))
					return I;
			}
		}
		return null;
	}

	public MailPiece findExactBoxData(String boxName, Item likeThis)
	{
		final List<PlayerData> V=getBoxRowPDData(boxName);
		for(int v=0;v<V.size();v++)
		{
			final DatabaseEngine.PlayerData PD=V.get(v);
			if((PD!=null)
			&&(PD.key().startsWith(postalBranch()+";")))
			{
				final Item I=makeItem(parsePostalItemData(PD.xml()));
				if(I==null)
					continue;
				if(I.sameAs(likeThis))
					return parsePostalItemData(PD.xml());
			}
		}
		return null;
	}

	@Override
	public MailPiece parsePostalItemData(String data)
	{
		final MailPiece piece = new MailPiece();
		for(int i=0;i<5;i++)
		{
			final int x=data.indexOf(';');
			if(x<0)
			{
				Log.errOut("StdPostman","Man formed postal data: "+data);
				return null;
			}
			switch(i)
			{
			case 0:
				piece.from = data.substring(0, x);
				break;
			case 1:
				piece.to = data.substring(0, x);
				break;
			case 2:
				piece.time = data.substring(0, x);
				break;
			case 3:
				piece.cod = data.substring(0, x);
				break;
			case 4:
				piece.classID = data.substring(0, x);
				break;
			}
			data=data.substring(x+1);
		}
		piece.xml = data;
		return piece;
	}

	protected Item makeItem(MailPiece data)
	{
		if(data ==  null)
			return null;
		final Item I=CMClass.getItem(data.classID);
		if(I!=null)
		{
			CMLib.coffeeMaker().setPropertiesStr(I,data.xml,true);
			I.recoverPhyStats();
			I.text();
			return I;
		}
		return null;
	}

	protected int getChargeableWeight(Item I)
	{
		if(I==null)
			return 0;
		int chargeableWeight=0;
		if(I.phyStats().weight()>0)
			chargeableWeight=(I.phyStats().weight()-1);
		return chargeableWeight;
	}

	protected double getSimplePostage(int chargeableWeight)
	{
		if(getStartRoom()==null)
			return 0.0;
		return minimumPostage()+CMath.mul(postagePerPound(),chargeableWeight);
	}

	protected double getHoldingCost(MailPiece data, int chargeableWeight)
	{
		if(data== null)
			return 0.0;
		if(getStartRoom()==null)
			return 0.0;
		double amt=0.0;
		final TimeClock TC=CMLib.time().localClock(getStartRoom());
		final long time=System.currentTimeMillis()-CMath.s_long(data.time);
		final long millisPerMudMonth=TC.getDaysInMonth()*CMProps.getMillisPerMudHour()*TC.getHoursInDay();
		if(time<=0)
			return amt;
		amt+=CMath.mul(CMath.mul(Math.floor(CMath.div(time,millisPerMudMonth)),holdFeePerPound()),chargeableWeight);
		return amt;
	}

	protected double getCODChargeForPiece(MailPiece data)
	{
		if(data==null)
			return 0.0;
		final int chargeableWeight=getChargeableWeight(makeItem(data));
		final double COD=CMath.s_double(data.cod);
		double amt=0.0;
		if(COD>0.0)
			amt=getSimplePostage(chargeableWeight)+COD;
		return amt+getHoldingCost(data,chargeableWeight);
	}

	protected String getBranchPostableTo(String toWhom, String branch, Map<String, String> allBranchBoxes)
	{
		String forward=allBranchBoxes.get(branch);
		if(forward==null)
			return null;
		if(forward.equalsIgnoreCase(toWhom))
			return branch;
		final PostOffice P=CMLib.map().getPostOffice(postalChain(),forward);
		if(P!=null)
		{
			forward=allBranchBoxes.get(P.postalBranch());
			if((forward!=null)&&forward.equalsIgnoreCase(toWhom))
				return P.postalBranch();
		}
		return null;
	}

	@Override
	public String findProperBranch(String toWhom)
	{
		if(CMLib.players().getLoadPlayer(toWhom)!=null)
		{
			final MOB M=CMLib.players().getLoadPlayer(toWhom);
			if(M.getStartRoom()!=null)
			{
				final Map<String,String> allBranchBoxes=getOurOpenBoxes(toWhom);
				final PostOffice P=CMLib.map().getPostOffice(postalChain(),M.getStartRoom().getArea().Name());
				String branch=null;
				if(P!=null)
				{
					branch=getBranchPostableTo(toWhom,P.postalBranch(),allBranchBoxes);
					if(branch!=null)
						return branch;
					if(allBranchBoxes.size()==0)
					{
						P.createBoxHere(toWhom,toWhom);
						return P.postalBranch();
					}
				}
				branch=getBranchPostableTo(toWhom,postalBranch(),allBranchBoxes);
				if(branch!=null)
					return branch;
				for(final String tryBranch : allBranchBoxes.keySet())
				{
					branch=getBranchPostableTo(toWhom,tryBranch,allBranchBoxes);
					if(branch!=null)
						return branch;
				}
				if(P!=null)
				{
					P.deleteBoxHere(toWhom);
					P.createBoxHere(toWhom,toWhom);
					return P.postalBranch();
				}
			}
		}
		else
		if(CMLib.clans().getClan(toWhom)!=null)
		{
			final Map<String,String> allBranchBoxes=getOurOpenBoxes(toWhom);
			String branch=getBranchPostableTo(toWhom,postalBranch(),allBranchBoxes);
			if(branch!=null)
				return branch;
			for(final String tryBranch : allBranchBoxes.keySet())
			{
				branch=getBranchPostableTo(toWhom,tryBranch,allBranchBoxes);
				if(branch!=null)
					return branch;
			}
		}
		return null;
	}

	public long postalWaitTime()
	{
		if((postalWaitTime<0)&&(getStartRoom()!=null))
			postalWaitTime=10038;
		else
		if((postalWaitTime==10038)&&(getStartRoom()!=null))
			postalWaitTime=(getStartRoom().getArea().getTimeObj().getHoursInDay())*CMProps.getMillisPerMudHour();
		return postalWaitTime;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return true;

		if((tickID==Tickable.TICKID_MOB)&&(getStartRoom()!=null))
		{
			boolean proceed=false;
			// handle interest by watching the days go by...
			// each chain is handled by one branch,
			// since pending mail all looks the same.
			Long L=postalTimes.get(postalChain()+"/"+postalBranch());
			if((L==null)||(L.longValue()<System.currentTimeMillis()))
			{
				proceed=(L!=null);
				L=Long.valueOf(System.currentTimeMillis()+postalWaitTime());
				postalTimes.remove(postalChain()+"/"+postalBranch());
				postalTimes.put(postalChain()+"/"+postalBranch(),L);
			}
			if(proceed)
			{
				List<PlayerData> V=getBoxRowPDData(postalChain());
				// first parse all the pending mail,
				// and remove it from the sorter
				final Vector<MailPiece> parsed=new Vector<MailPiece>();
				if(V==null)
					V=new Vector<PlayerData>();
				for(int v=0;v<V.size();v++)
				{
					final DatabaseEngine.PlayerData PD=V.get(v);
					parsed.addElement(parsePostalItemData(PD.xml()));
					CMLib.database().DBDeletePlayerData(PD.who(),PD.section(),PD.key());
				}
				PostOffice P=null;
				for(int v=0;v<parsed.size();v++)
				{
					final MailPiece V2=parsed.elementAt(v);
					final String toWhom=V2.to;
					String deliveryBranch=findProperBranch(toWhom);
					if(deliveryBranch!=null)
					{
						P=CMLib.map().getPostOffice(postalChain(),deliveryBranch);
						final Item I=makeItem(V2);
						if((P!=null)&&(I!=null))
						{
							P.addToBox(toWhom,I,V2.from,V2.to,CMath.s_long(V2.time),CMath.s_double(V2.cod));
							continue;
						}
					}
					final String fromWhom=V2.from;
					deliveryBranch=findProperBranch(fromWhom);
					if(deliveryBranch!=null)
					{
						P=CMLib.map().getPostOffice(postalChain(),deliveryBranch);
						final Item I=makeItem(V2);
						if((P!=null)&&(I!=null))
							P.addToBox(fromWhom,I,V2.to,"POSTMASTER",System.currentTimeMillis(),0.0);
					}
				}
				V=CMLib.database().DBReadPlayerSectionData(postalChain());
				TimeClock TC=null;
				if(getStartRoom()!=null)
					TC=getStartRoom().getArea().getTimeObj();
				if((TC!=null)&&(maxMudMonthsHeld()>0))
				{
					for(int v=0;v<V.size();v++)
					{
						final DatabaseEngine.PlayerData V2=V.get(v);
						if(V2.key().startsWith(postalBranch()+";"))
						{
							final MailPiece data=parsePostalItemData(V2.xml());
							if((data!=null)&&(getStartRoom()!=null))
							{
								final long time=System.currentTimeMillis()-CMath.s_long(data.time);
								final long millisPerMudMonth=TC.getDaysInMonth()*CMProps.getMillisPerMudHour()*TC.getHoursInDay();
								if(time>0)
								{
									final int months=(int)Math.round(Math.floor(CMath.div(time,millisPerMudMonth)));
									if(months>maxMudMonthsHeld())
									{
										final Item I=makeItem(data);
										CMLib.database().DBDeletePlayerData(V2.who(),V2.section(),V2.key());
										if(I!=null)
											getShop().addStoreInventory(I);
									}
								}
							}
						}
					}
				}
			}
		}
		return true;
	}

	public void autoGive(MOB src, MOB tgt, Item I)
	{
		CMMsg msg2=CMClass.getMsg(src,I,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null);
		location().send(this,msg2);
		msg2=CMClass.getMsg(tgt,I,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null);
		location().send(this,msg2);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob=msg.source();
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GIVE:
			case CMMsg.TYP_DEPOSIT:
				if(CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
				{
					if(msg.tool() instanceof Container)
						((Container)msg.tool()).emptyPlease(true);
					final Session S=msg.source().session();
					if((!msg.source().isMonster())&&(S!=null)&&(msg.tool() instanceof Item))
					{
						autoGive(msg.source(),this,(Item)msg.tool());
						if(isMine(msg.tool()))
						{
							final StdPostman me=this;
							S.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
							{
								@Override
								public void showPrompt()
								{
									S.promptPrint(L("Address this to whom? "));
								}

								@Override
								public void timedOut()
								{
									autoGive(me,msg.source(),(Item)msg.tool());
								}

								@Override public void callBack()
								{
									if((this.input!=null)&&(this.input.length()>0)
									&&((CMLib.players().getLoadPlayer(this.input)!=null)||(CMLib.clans().findClan(this.input)!=null)))
									{
										final String fromWhom=getSenderName(msg.source(),Clan.Function.DEPOSIT,false);
										final String toWhom;
										if(CMLib.players().getLoadPlayer(this.input)!=null)
											toWhom=CMLib.players().getLoadPlayer(this.input).Name();
										else
											toWhom=CMLib.clans().findClan(this.input).name();
										final double amt=getSimplePostage(getChargeableWeight((Item)msg.tool()));
										S.prompt(new InputCallback(InputCallback.Type.CHOOSE,"P","CP\n",0)
										{
											@Override public void showPrompt() {
												S.promptPrint(L("Postage on this will be @x1.\n\rWould you like to P)ay this now, or be C)harged on delivery (c/P)?",CMLib.beanCounter().nameCurrencyShort(me,amt)));
											}

											@Override
											public void timedOut()
											{
												autoGive(me,msg.source(),(Item)msg.tool());
											}

											@Override public void callBack()
											{
												final String choice=this.input.trim().toUpperCase();
												if(choice.startsWith("C"))
												{
													S.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
													{
														@Override
														public void showPrompt()
														{
															S.promptPrint(L("Enter COD amount (@x1): ",CMLib.beanCounter().getDenominationName(CMLib.beanCounter().getCurrency(me),CMLib.beanCounter().getLowestDenomination(CMLib.beanCounter().getCurrency(me)))));
														}

														@Override
														public void timedOut()
														{
															autoGive(me,msg.source(),(Item)msg.tool());
														}

														@Override public void callBack()
														{
															final String CODstr=this.input;
															if((CODstr.length()==0)||(!CMath.isNumber(CODstr))||(CMath.s_double(CODstr)<=0.0))
															{
																CMLib.commands().postSay(me,mob,L("That is not a valid amount."),true,false);
																autoGive(me,msg.source(),(Item)msg.tool());
															}
															else
															{
																final Coins currency=CMLib.beanCounter().makeBestCurrency(CMLib.beanCounter().getCurrency(me),CMLib.beanCounter().getLowestDenomination(CMLib.beanCounter().getCurrency(me))*(CMath.s_double(CODstr)));
																final double COD=currency.getTotalValue();
																addToBox(postalChain(),(Item)msg.tool(),fromWhom,toWhom,System.currentTimeMillis(),COD);
																CMLib.commands().postSay(me,mob,L("I'll deliver that for ya right away!"),true,false);
																((Item)msg.tool()).destroy();
															}
														}
													});
												}
												else
												if((amt>0.0)&&(CMLib.beanCounter().getTotalAbsoluteShopKeepersValue(msg.source(),me)<amt))
												{
													CMLib.commands().postSay(me,mob,L("You can't afford postage."),true,false);
													autoGive(me,msg.source(),(Item)msg.tool());
												}
												else
												{
													CMLib.beanCounter().subtractMoney(mob,CMLib.beanCounter().getCurrency(me),amt);
													addToBox(postalChain(),(Item)msg.tool(),fromWhom,toWhom,System.currentTimeMillis(),0.0);
													CMLib.commands().postSay(me,mob,L("I'll deliver that for ya right away!"),true,false);
													((Item)msg.tool()).destroy();
												}
											}
										});

									}
									else
									{
										CMLib.commands().postSay(me,mob,L("That is not a valid player or clan name."),true,false);
										autoGive(me,msg.source(),(Item)msg.tool());
									}
								}
							});
						}
						else
							CMLib.commands().postSay(this,mob,L("I can't seem to deliver @x1.",msg.tool().name()),true,false);
					}
				}
				return;
			case CMMsg.TYP_WITHDRAW:
				if(CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
				{
					final String fromWhom=getSenderName(msg.source(),Clan.Function.WITHDRAW,false);
					final Item old=(Item)msg.tool();
					MailPiece data=findExactBoxData(fromWhom,(Item)msg.tool());
					if((data==null)
					&&(!isSold(ShopKeeper.DEAL_CLANPOSTMAN))
					&&(msg.source().isMarriedToLiege()))
						data=findExactBoxData(msg.source().getLiegeID(),(Item)msg.tool());
					if(data==null)
						CMLib.commands().postSay(this,mob,L("You want WHAT? Try LIST."),true,false);
					else
					{
						if((!delFromBox(fromWhom,old))
						&&(!isSold(ShopKeeper.DEAL_CLANPOSTMAN))
						&&(msg.source().isMarriedToLiege()))
							delFromBox(msg.source().getLiegeID(),old);
						final double totalCharge=getCODChargeForPiece(data);
						if((totalCharge>0.0)&&(CMLib.beanCounter().getTotalAbsoluteShopKeepersValue(msg.source(),this)>=totalCharge))
						{

							CMLib.beanCounter().subtractMoney(msg.source(),totalCharge);
							final double COD=CMath.s_double(data.cod);
							Coins returnMoney=null;
							if(COD>0.0)
								returnMoney=CMLib.beanCounter().makeBestCurrency(this,COD);
							if(returnMoney!=null)
							{
								CMLib.commands().postSay(this,mob,L("The COD amount of @x1 has been sent back to @x2.",returnMoney.Name(),data.from),true,false);
								addToBox(postalChain(),returnMoney,data.to,data.from,System.currentTimeMillis(),0.0);
								CMLib.commands().postSay(this,mob,L("The total charge on that was a COD charge of @x1 plus @x2 postage and holding fees.",returnMoney.Name(),CMLib.beanCounter().nameCurrencyShort(this,totalCharge-COD)),true,false);
							}
							else
								CMLib.commands().postSay(this,mob,L("The total charge on that was @x1 in holding/storage fees.",CMLib.beanCounter().nameCurrencyShort(this,totalCharge)),true,false);
						}
						CMLib.commands().postSay(this,mob,L("There ya go!"),true,false);
						if(location()!=null)
							location().addItem(old,ItemPossessor.Expire.Player_Drop);
						final CMMsg msg2=CMClass.getMsg(mob,old,this,CMMsg.MSG_GET,null);
						if(location().okMessage(mob,msg2))
							location().send(mob,msg2);
					}
				}
				return;
			case CMMsg.TYP_VALUE:
			case CMMsg.TYP_SELL:
			case CMMsg.TYP_VIEW:
				super.executeMsg(myHost,msg);
				return;
			case CMMsg.TYP_BUY:
				super.executeMsg(myHost,msg);
				return;
			case CMMsg.TYP_SPEAK:
			{
				super.executeMsg(myHost,msg);
				String str=CMStrings.getSayFromMessage(msg.targetMessage());
				if((str!=null)&&(str.trim().equalsIgnoreCase("open")))
				{
					final String theName=getSenderName(msg.source(),Clan.Function.DEPOSIT,false);
					if(getOurOpenBoxes(theName).containsKey(postalBranch()))
					{
						if(isSold(ShopKeeper.DEAL_CLANPOSTMAN))
							CMLib.commands().postSay(this,mob,L("@x1 already has a box open here!",CMStrings.capitalizeFirstLetter(theName)),true,false);
						else
							CMLib.commands().postSay(this,mob,L("You already have a box open here!"),true,false);
					}
					else
					if(feeForNewBox()>0.0)
					{
						if(CMLib.beanCounter().getTotalAbsoluteShopKeepersValue(msg.source(),this)<feeForNewBox())
						{
							CMLib.commands().postSay(this,mob,L("Too bad you can't afford it."),true,false);
							return;
						}
						CMLib.beanCounter().subtractMoney(msg.source(),CMLib.beanCounter().getCurrency(this),feeForNewBox());
						createBoxHere(theName,theName);
						if(isSold(ShopKeeper.DEAL_CLANPOSTMAN))
							CMLib.commands().postSay(this,mob,L("A box has been opened for @x1.",CMStrings.capitalizeFirstLetter(theName)),true,false);
						else
							CMLib.commands().postSay(this,mob,L("A box has been opened for you."),true,false);
					}
					else
					{
						createBoxHere(theName,theName);
						if(isSold(ShopKeeper.DEAL_CLANPOSTMAN))
							CMLib.commands().postSay(this,mob,L("A box has been opened for @x1.",CMStrings.capitalizeFirstLetter(theName)),true,false);
						else
							CMLib.commands().postSay(this,mob,L("A box has been opened for you."),true,false);
					}
				}
				else
				if((str!=null)&&(str.trim().equalsIgnoreCase("close")))
				{
					final String theName=getSenderName(msg.source(),Clan.Function.WITHDRAW,false);
					if(!getOurOpenBoxes(theName).containsKey(postalBranch()))
					{
						if(isSold(ShopKeeper.DEAL_CLANPOSTMAN))
							CMLib.commands().postSay(this,mob,L("@x1 does not have a box here!",CMStrings.capitalizeFirstLetter(theName)),true,false);
						else
							CMLib.commands().postSay(this,mob,L("You don't have a box open here!"),true,false);
					}
					else
					if(getAllLocalBoxPD(theName).size()>0)
						CMLib.commands().postSay(this,mob,L("That box has pending items which must be removed first."),true,false);
					else
					{
						deleteBoxHere(theName);
						CMLib.commands().postSay(this,mob,L("That box is now closed."),true,false);
					}
				}
				else
				if((str!=null)&&(str.toUpperCase().trim().startsWith("FORWARD")))
				{
					final String theName=getSenderName(msg.source(),Clan.Function.WITHDRAW,false);
					str=str.trim().substring(7).trim();
					if(!getOurOpenBoxes(theName).containsKey(postalBranch()))
					{
						if(isSold(ShopKeeper.DEAL_CLANPOSTMAN))
							CMLib.commands().postSay(this,mob,L("@x1 does not have a box here!",CMStrings.capitalizeFirstLetter(theName)),true,false);
						else
							CMLib.commands().postSay(this,mob,L("You don't have a box open here!"),true,false);
					}
					else
					if(str.toUpperCase().equalsIgnoreCase("STOP") || str.toUpperCase().equalsIgnoreCase("UNFORWARD") || str.toUpperCase().equalsIgnoreCase("END"))
					{
						final Map<String,String> allOpenBoxes = getOurOpenBoxes(theName);
						if(allOpenBoxes.containsKey(postalBranch()))
						{
							deleteBoxHere(theName);
							CMLib.commands().postSay(this,mob,L("Ok, mail send here will no longer be forwarded anywhere."),true,false);
						}
					}
					else
					{
						final Area A=CMLib.map().findAreaStartsWith(str);
						if(A==null)
						{
							CMLib.commands().postSay(this,mob,L("I don't know of an area called '@x1' to forward to.",str),true,false);
							final Map<String,String> allOpenBoxes = getOurOpenBoxes(theName);
							if(allOpenBoxes.containsKey(postalBranch()))
								CMLib.commands().postSay(this,mob,L("Top stop forwarding, try FORWARD STOP."),true,false);
						}
						else
						{
							final PostOffice P=CMLib.map().getPostOffice(postalChain(),A.Name());
							if(P==null)
								CMLib.commands().postSay(this,mob,L("I'm sorry, we don't have a branch in @x1.",A.name()),true,false);
							else
							if(!P.getOurOpenBoxes(theName).containsKey(P.postalBranch()))
							{
								if(isSold(ShopKeeper.DEAL_CLANPOSTMAN))
									CMLib.commands().postSay(this,mob,L("I'm sorry, @x1 does not have a box at our branch in @x2.",CMStrings.capitalizeFirstLetter(theName),A.name()),true,false);
								else
									CMLib.commands().postSay(this,mob,L("I'm sorry, you don't have a box at our branch in @x1.",A.name()),true,false);
							}
							else
							{
								deleteBoxHere(theName);
								createBoxHere(theName,P.postalBranch());
								CMLib.commands().postSay(this,mob,L("Ok, mail will now be forwarded to our branch in @x1.",A.name()),true,false);
							}
						}
					}
				}
				return;
			}
			case CMMsg.TYP_LIST:
			{
				super.executeMsg(myHost,msg);
				if(CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
				{
					Vector<PlayerData> V=null;
					final String theName=getSenderName(msg.source(),Clan.Function.DEPOSIT_LIST,false);
					if(isSold(ShopKeeper.DEAL_CLANPOSTMAN))
						V=getAllLocalBoxPD(theName);
					else
					{
						V=getAllLocalBoxPD(theName);
						if(mob.isMarriedToLiege())
						{
							final Vector<PlayerData> PDV=getAllLocalBoxPD(mob.getLiegeID());
							if((PDV!=null)&&(PDV.size()>0))
								V.addAll(PDV);
						}
					}

					TimeClock C=CMLib.time().localClock(getStartRoom());
					boolean codCharge=false;
					if(V.size()==0)
						mob.tell(L("\n\rYour postal box is presently empty."));
					else
					{
						StringBuffer str=new StringBuffer("");
						str.append(L("\n\rItems in your postal box here:\n\r"));
						str.append(L("^x[COD     ][From           ][Sent           ][Item                        ]^.^N"));
						mob.tell(str.toString());
						for(int i=0;i<V.size();i++)
						{
							final DatabaseEngine.PlayerData PD=V.elementAt(i);
							final MailPiece pieces=parsePostalItemData(PD.xml());
							final Item I=makeItem(pieces);
							if(I==null)
								continue;
							str=new StringBuffer("^N");
							if(getCODChargeForPiece(pieces)>0.0)
							{
								codCharge=true;
								str.append("["+CMStrings.padRight(""+CMLib.beanCounter().abbreviatedPrice(this,getCODChargeForPiece(pieces)),8)+"]");
							}
							else
								str.append("[        ]");
							str.append("["+CMStrings.padRight(pieces.from,15)+"]");
							final TimeClock C2=C.deriveClock(CMath.s_long(pieces.time));
							str.append("["+CMStrings.padRight(C2.getShortestTimeDescription(),15)+"]");
							str.append("["+CMStrings.padRight(I.Name(),28)+"]");
							mob.tell(str.toString()+"^T");
						}
					}
					final StringBuffer str=new StringBuffer("\n\r^N");
					if(codCharge)
						str.append(L("* COD charges above include all shipping costs.\n\r"));
					str.append(L("* This branch charges minimum @x1 postage for first pound.\n\r",CMLib.beanCounter().nameCurrencyShort(this,minimumPostage())));
					str.append(L("* An additional @x1 per pound is charged for packages.\n\r",CMLib.beanCounter().nameCurrencyShort(this,postagePerPound())));
					str.append(L("* A charge of @x1 per pound per month is charged for holding.\n\r",CMLib.beanCounter().nameCurrencyShort(this,holdFeePerPound())));
					str.append("* To forward your mail, 'say \""+name()+"\" \"forward <areaname>\"'.\n\r");
					str.append(L("* To close your box, 'say \"@x1\" close'.\n\r",name()));
					mob.tell(str.toString());
				}
				return;
			}
			default:
				break;
			}
		}
		else
		if(msg.sourceMinor()==CMMsg.TYP_RETIRE)
			emptyBox(msg.source().Name());
		super.executeMsg(myHost,msg);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob=msg.source();
		if((msg.targetMinor()==CMMsg.TYP_EXPIRE)
		&&(msg.target()==location())
		&&(CMLib.flags().isInTheGame(this,true)))
			return false;
		else
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GIVE:
			case CMMsg.TYP_DEPOSIT:
				{
					if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this))
						return false;
					if(msg.tool()==null)
						return false;
					final String senderName=getSenderName(msg.source(),Clan.Function.DEPOSIT,true);
					if(senderName==null)
						return false;
					if(!(msg.tool() instanceof Item))
					{
						mob.tell(L("@x1 doesn't look interested.",mob.charStats().HeShe()));
						return false;
					}
					if(CMLib.flags().isEnspelled((Item)msg.tool()) 
					|| CMLib.flags().isOnFire((Item)msg.tool())
					||(msg.tool() instanceof CagedAnimal))
					{
						mob.tell(this,msg.tool(),null,L("<S-HE-SHE> refuses to accept <T-NAME> for delivery."));
						return false;
					}
				}
				return true;
			case CMMsg.TYP_WITHDRAW:
				{
					if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this))
						return false;
					final String senderName=getSenderName(msg.source(),Clan.Function.WITHDRAW,true);
					if(senderName==null)
						return false;
					if((msg.tool()==null)||(!(msg.tool() instanceof Item)))
					{
						CMLib.commands().postSay(this,mob,L("What do you want? I'm busy!"),true,false);
						return false;
					}
					if((msg.tool()!=null)&&(!msg.tool().okMessage(myHost,msg)))
						return false;
					MailPiece data=findExactBoxData(senderName,(Item)msg.tool());
					if((data==null)
					&&(!isSold(ShopKeeper.DEAL_CLANPOSTMAN))
					&&(msg.source().isMarriedToLiege()))
						data=findExactBoxData(msg.source().getLiegeID(),(Item)msg.tool());
					if(data==null)
					{
						CMLib.commands().postSay(this,mob,L("You want WHAT? Try LIST."),true,false);
						return false;
					}
					final double totalCharge=getCODChargeForPiece(data);
					if(CMLib.beanCounter().getTotalAbsoluteShopKeepersValue(msg.source(),this)<totalCharge)
					{
						CMLib.commands().postSay(this,mob,L("The total charge to receive that item is @x1. You don't have enough.",CMLib.beanCounter().nameCurrencyShort(this,totalCharge)),true,false);
						return false;
					}
				}
				return true;
			case CMMsg.TYP_VALUE:
			case CMMsg.TYP_SELL:
			case CMMsg.TYP_VIEW:
				return super.okMessage(myHost,msg);
			case CMMsg.TYP_BUY:
				return super.okMessage(myHost,msg);
			case CMMsg.TYP_LIST:
			{
				if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this))
					return false;
				final String senderName=getSenderName(msg.source(),Clan.Function.DEPOSIT_LIST,true);
				if(senderName==null)
					return false;
				if((!getOurOpenBoxes(senderName).containsKey(postalBranch()))
				&&((isSold(ShopKeeper.DEAL_CLANPOSTMAN))
				   ||(!msg.source().isMarriedToLiege())
				   ||(!getOurOpenBoxes(msg.source().getLiegeID()).containsKey(postalBranch()))))
				{
					if((!isSold(ShopKeeper.DEAL_CLANPOSTMAN))
					&&(msg.source().getStartRoom().getArea()==getStartRoom().getArea()))
					{
						createBoxHere(msg.source().Name(),msg.source().Name());
						return true;
					}
					final StringBuffer str=new StringBuffer("");
					if(isSold(ShopKeeper.DEAL_CLANPOSTMAN))
						str.append(L("@x1 does not have a postal box at this branch, I'm afraid.",CMStrings.capitalizeFirstLetter(senderName)));
					else
						str.append(L("You don't have a postal box at this branch, I'm afraid."));
					if(postalChain().length()>0)
						str.append(L("\n\rThis branch is part of the @x1 postal chain.",postalChain()));
					CMLib.commands().postSay(this,mob,str.toString()+"^T",true,false);
					mob.tell(L("Use 'say \"@x1\" open' to open a box here@x2",name(),((feeForNewBox()<=0.0)?".":(" for "+CMLib.beanCounter().nameCurrencyShort(this,feeForNewBox())+"."))));
					return false;
				}
				return true;
			}
			default:
				break;
			}
		}
		return super.okMessage(myHost,msg);
	}
}
