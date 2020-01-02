package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.MiniJSON.MJSONException;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2017-2020 Bo Zimmerman

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
public class Publishing extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Publishing";
	}

	private final static String	localizedName	= CMLib.lang().L("Publishing");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "PUBLISH", "PUBLISHING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_LEGAL;
	}

	protected double	price	= 1000.0;
	protected Item		found	= null;
	protected boolean	success = false;

	@Override
	protected boolean canBeDoneSittingDown()
	{
		return true;
	}

	public Publishing()
	{
		super();
		displayText=L("You are publishing...");
		verb=L("publishing");
	}

	public MiniJSON.JSONObject getData()
	{
		if(text().trim().length()==0)
			super.setMiscText("{\"lastpub\":0}");
		try
		{
			return new MiniJSON().parseObject(text());
		}
		catch (final MJSONException e)
		{
			Log.errOut(e);
			Log.errOut("Data = '"+text()+"'");
			return new MiniJSON.JSONObject();
		}
	}

	public void setData(final MiniJSON.JSONObject obj)
	{
		super.setMiscText(obj.toString());
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		if(newMiscText.length()>0)
			doStorePublish();
	}

	protected volatile boolean alreadyStored = false;

	protected void doStorePublish()
	{
		if(alreadyStored)
			return;
		alreadyStored = true;
		final MiniJSON.JSONObject obj = getData();
		for(final String itemName : obj.keySet())
		{
			if(obj.containsKey(itemName)
			&&(!itemName.equalsIgnoreCase("lastpub")))
			{
				try
				{
					final MiniJSON.JSONObject bobj = obj.getCheckedJSONObject(itemName);
					if(bobj.containsKey("locs"))
					{
						final String author = bobj.getCheckedString("author");
						final MOB M=CMLib.players().getPlayer(author);
						if(M==null)
						{
							alreadyStored=false;
							return;
						}
						final Object[] locs=bobj.getCheckedArray("locs");
						for(final Object locO : locs)
						{
							final MiniJSON.JSONObject lobj = (MiniJSON.JSONObject)locO;
							final String locName = lobj.getCheckedString("name");
							final String locRoom = lobj.getCheckedString("room");
							final Room R=CMLib.map().getRoom(locRoom);
							if(R!=null)
							{
								Physical P=R.fetchInhabitant(locName);
								if(P==null)
									P=R.findItem(locName);
								if(P!=null)
								{
									final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(P);
									if((SK!=null)
									&&(!SK.getShop().getStoreInventory("$"+itemName+"$").hasNext()))
									{
										final Item shopItem=M.findItem("$"+itemName+"$");
										if(shopItem!=null)
										{
											double price=100;
											int level=30;
											if(bobj.containsKey("price"))
												price=bobj.getCheckedDouble("price").doubleValue();
											if(bobj.containsKey("level"))
												level=bobj.getCheckedLong("level").intValue();
											if(SK instanceof Librarian)
											{
												((Librarian)SK).getBaseLibrary().addStoreInventory((Item)shopItem.copyOf(), level, (int)CMath.round(price));
											}
											else
											{
												SK.getShop().addStoreInventory((Item)shopItem.copyOf(), level, (int)CMath.round(price));
											}
										}
									}
								}
							}
						}
					}
				}
				catch (final MJSONException e)
				{
					Log.errOut(e);
				}
			}
		}
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(!aborted)
			&&(!helping))
			{
				final MOB mob=(MOB)affected;
				if((!success)||(found==null))
					commonTell(mob,L("You messed up your attempt to get published."));
				else
				{
					final ArrayList<Room> rooms=new ArrayList<Room>();
					final PairList<ShopKeeper,Room> shops=new PairVector<ShopKeeper,Room>();
					final TrackingLibrary.TrackingFlags flags;
					flags = CMLib.tracking().newFlags()
							.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
							.plus(TrackingLibrary.TrackingFlag.NOHIDDENAREAS)
							.plus(TrackingLibrary.TrackingFlag.NOHOMES);
					final int radius=10 + (10 * super.getXLEVELLevel(mob));
					CMLib.tracking().getRadiantRooms(mob.location(),rooms,flags,null,radius,null);
					for(int r=0;r<rooms.size();r++)
					{
						final Room R=rooms.get(r);
						final ShopKeeper rSK=CMLib.coffeeShops().getShopKeeper(R);
						if((rSK != null)
						&&(rSK.isSold(ShopKeeper.DEAL_BOOKS)))
							shops.add(rSK,R);
						for(int i=0;i<R.numInhabitants();i++)
						{
							final MOB M=R.fetchInhabitant(i);
							if((M != affected)
							&&(M != null)
							&&(!M.isPlayer()))
							{
								final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
								if((SK != null)
								&&(SK.isSold(ShopKeeper.DEAL_BOOKS)))
								{
									if(M.getStartRoom()!=null)
										shops.add(SK,M.getStartRoom());
									else
										shops.add(SK,R);
								}
							}
						}
					}
					rooms.clear();
					final Publishing realPubA = (Publishing)mob.fetchAbility(ID());
					if(shops.size()==0)
						commonTell(mob,L("There were no appropriate book shops nearby to publish at."));
					else
					{
						int pubbed = 0;
						int already = 0;
						int illegal = 0;
						int failed = 0;
						if(found.fetchEffect("Copyright")==null)
						{
							final Ability copyA=CMClass.getAbility("Copyright");
							if(copyA!=null)
							{
								copyA.setMiscText(mob.Name());
								found.addNonUninvokableEffect(copyA);
							}
						}
						final Item shopItem = (Item)found.copyOf();
						if(shopItem.fetchEffect(ID())==null)
						{
							final Publishing pubA=(Publishing)this.copyOf();
							shopItem.addNonUninvokableEffect(pubA);
						}
						for(final Pair<ShopKeeper,Room> SKs : shops)
						{
							final ShopKeeper SK=SKs.first;
							final Iterator<Environmental> ie=SK.getShop().getStoreInventory("$"+shopItem.Name()+"$");
							boolean proceed = true;
							if(ie.hasNext())
							{
								proceed = false;
								final Environmental E=ie.next();
								if(E instanceof Item)
								{
									final Ability copyrightA=((Item) E).fetchEffect("Copyright");
									if(copyrightA != null)
									{
										if(!copyrightA.text().equals(mob.Name()))
											illegal++;
										else
											already++;
									}
									else
										failed++;
								}
								else
									failed++;
							}
							if(proceed)
							{
								Log.infoOut("The book "+shopItem.Name()+" was published by "+mob.Name()+" to "+CMLib.map().getExtendedRoomID(CMLib.map().roomLocation(SKs.second)));
								pubbed++;
								if(SK instanceof Librarian)
									((Librarian)SK).getBaseLibrary().addStoreInventory((Item)shopItem.copyOf(), adjustedLevel(mob,0), (int)CMath.round(price));
								else
									SK.getShop().addStoreInventory((Item)shopItem.copyOf(), adjustedLevel(mob,0), (int)CMath.round(price));
								final MiniJSON.JSONObject obj=(realPubA!=null)?realPubA.getData():getData();
								if(!obj.containsKey(shopItem.Name()))
									obj.put(shopItem.Name(), new MiniJSON.JSONObject());
								try
								{
									final MiniJSON.JSONObject bookObj=obj.getCheckedJSONObject(shopItem.Name());
									Object[] locs =new Object[0];
									if(bookObj.containsKey("locs"))
										locs=bookObj.getCheckedArray("locs");
									boolean found=false;
									for(final Object o : locs)
									{
										final MiniJSON.JSONObject locObj = (MiniJSON.JSONObject)o;
										if(locObj.getCheckedString("name").equals(SK.Name())
										&&(CMLib.map().getExtendedRoomID(SKs.second)).equals(locObj.getCheckedString("room"))
										&&(bookObj.containsKey("author")))
											found=true;
									}
									if(!found)
									{
										locs=Arrays.copyOf(locs, locs.length+1);
										final MiniJSON.JSONObject locObj=new MiniJSON.JSONObject();
										locObj.put("name", SK.Name());
										locObj.put("room", CMLib.map().getExtendedRoomID(CMLib.map().roomLocation(SKs.second)));
										locs[locs.length-1]=locObj;
										bookObj.put("locs", locs);
										bookObj.put("author", mob.Name());
										bookObj.put("price", Double.valueOf(price));
										bookObj.put("level", Integer.valueOf(adjustedLevel(mob, 0)));
										if(realPubA!=null)
											realPubA.setData(obj);
										else
											setData(obj);
									}
								}
								catch (final MJSONException e)
								{
									Log.errOut(e);
								}
							}
						}
						final StringBuilder str=new StringBuilder(L("Publishing completed. "));
						if(pubbed == 0)
							str.append(L("No copies were placed on local bookshelves.  Perhaps try another city?  "));
						else
						{
							str.append(L("@x1 cop(ys) were placed on local bookshelves.  ",""+pubbed));
							final Room R=mob.location();
							if(R!=null)
							{
								final Area A=R.getArea();
								if(A!=null)
								{
									final TimeClock C=A.getTimeObj();
									final MiniJSON.JSONObject obj=(realPubA!=null)?realPubA.getData():getData();
									obj.put("lastpub", Long.valueOf(C.toHoursSinceEpoc()));
									if(realPubA!=null)
										realPubA.setData(obj);
									else
										setData(obj);
								}
							}
						}
						if(already > 0)
							str.append(L("@x1 establishment(s) already had your book on their shelves.  ",""+already));
						if(illegal > 0)
							str.append(L("@x1 establishment(s) refused because your title was already taken.  ",""+illegal));
						if(failed > 0)
							str.append(L("@x1 establishment(s) refused because your title was used for something else.  ",""+illegal));
						commonTell(mob,str.toString());
					}
				}
			}
		}
		super.unInvoke();
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(affected instanceof Item)
		{
			final Item I=(Item)affected;
			if((msg.tool()==I)
			&&(msg.targetMinor()==CMMsg.TYP_SELL)
			&&(msg.target() != null))
			{
				final Ability copyA = I.fetchEffect("Copyright");
				if((copyA != null)&&(copyA.text().length()>0))
				{
					final MOB M=CMLib.players().getLoadPlayer(copyA.text());
					if(M!=null)
					{
						final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(msg.target());
						if(SK!=null)
						{
							final Publishing pubA=(Publishing)M.fetchAbility("Publishing");
							if(pubA!=null)
							{
								final MiniJSON.JSONObject obj=pubA.getData();
								try
								{
									final MiniJSON.JSONObject bookObj = obj.getCheckedJSONObject(I.Name());
									final Object[] locs=bookObj.getCheckedArray("locs");
									boolean found=false;
									for(final Object o : locs)
									{
										final MiniJSON.JSONObject locObj=(MiniJSON.JSONObject)o;
										final String name=locObj.getCheckedString("name");
										final String room=locObj.getCheckedString("room");
										if(name.equalsIgnoreCase(SK.Name()))
										{
											final Room startR=CMLib.map().getStartRoom(SK);
											if((startR==null)||(room.equalsIgnoreCase(CMLib.map().getExtendedRoomID(startR))))
											{
												found=true;
												break;
											}
										}
									}
									if(found)
									{
										if(bookObj.containsKey("copies_sold"))
										{
											final Long oldVal = bookObj.getCheckedLong("copies_sold");
											if(oldVal.longValue()>0)
												bookObj.put("copies_sold", Long.valueOf(oldVal.longValue() - 1));
										}
										if(bookObj.containsKey("who") && msg.source().isPlayer())
										{
											final Object[] whoms = bookObj.getCheckedArray("who");
											if(CMParms.containsAsString(whoms, msg.source().Name()))
											{
												final List<Object> whomses = new XVector<Object>(Arrays.asList(whoms));
												final int x=CMParms.indexOfAsString(whoms, msg.source().Name());
												if(x >=0)
													whomses.remove(x);
												bookObj.put("who", whomses.toArray(new Object[0]));
											}
										}
										pubA.setData(obj);
									}
								}
								catch (final MJSONException e)
								{
									Log.errOut(e);
								}
							}
						}
					}
				}
			}
			else
			if((msg.tool()==I)
			&&(msg.targetMinor()==CMMsg.TYP_BUY))
			{
				final Ability copyA = I.fetchEffect("Copyright");
				if((copyA != null)&&(copyA.text().length()>0))
				{
					final MOB M=CMLib.players().getLoadPlayer(copyA.text());
					if(M!=null)
					{
						final Publishing pubA=(Publishing)M.fetchAbility("Publishing");
						if(pubA!=null)
						{
							final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(msg.target());
							int royalties = 0;
							if(SK != null)
							{
								royalties = 1;
								final int price=SK.getShop().stockPrice(msg.tool());
								if(price>10)
									royalties = (int)Math.round(CMath.div(price, 10.0));
								CMLib.beanCounter().giveSomeoneMoney(M, CMLib.beanCounter().getCurrency(M), royalties);
							}
							final MiniJSON.JSONObject obj=pubA.getData();
							if(!obj.containsKey(I.Name()))
								obj.put(I.Name(), new MiniJSON.JSONObject());
							try
							{
								final MiniJSON.JSONObject bookObj = obj.getCheckedJSONObject(I.Name());
								if(!bookObj.containsKey("copies_sold"))
									bookObj.put("copies_sold", Integer.valueOf(1));
								else
								{
									final Long oldVal = bookObj.getCheckedLong("copies_sold");
									bookObj.put("copies_sold", Long.valueOf(oldVal.longValue() + 1));
								}
								if(royalties > 0)
								{
									if(!bookObj.containsKey("paid"))
										bookObj.put("paid", Integer.valueOf(royalties));
									else
									{
										final Long oldVal = bookObj.getCheckedLong("paid");
										bookObj.put("paid", Long.valueOf(oldVal.longValue() + royalties));
									}
								}
								if((msg.source().isPlayer())
								&&(M.isPlayer())
								&&(!msg.source().playerStats().getLastIP().equalsIgnoreCase(M.playerStats().getLastIP()))
								&&((M.playerStats().getAccount()==null)||(M.playerStats().getAccount()!=msg.source().playerStats().getAccount())))
								{
									if(!bookObj.containsKey("who"))
										bookObj.put("who", new Object[0]);
									else
									{
										Object[] oldVals = bookObj.getCheckedArray("who");
										if(!CMParms.containsAsString(oldVals, msg.source().Name()))
										{
											oldVals=Arrays.copyOf(oldVals,oldVals.length+1);
											oldVals[oldVals.length-1]=msg.source().Name();
											bookObj.put("who", oldVals);
										}
									}
								}
								pubA.setData(obj);
							}
							catch (final MJSONException e)
							{
								Log.errOut(e);
							}
						}
					}
				}
			}
		}
		else
			super.executeMsg(myHost, msg);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		if((commands.size()<1)
		||((commands.size()==1)&&(!commands.get(0).equalsIgnoreCase("LIST"))))
		{
			commonTell(mob,L("Publish what, at what asking price? If you already published, try LIST."));
			return false;
		}
		if(commands.size()==1)
		{
			final MiniJSON.JSONObject data=getData();
			if((data.keySet().size()==0)
			||((data.size()==1)&&(data.containsKey("lastpub"))))
				commonTell(mob,L("You haven't been published yet."));
			else
			{
				try
				{
					final int index=1;
					final StringBuilder str=new StringBuilder("");
					for(final String bookName : data.keySet())
					{
						if(bookName.equalsIgnoreCase("lastpub"))
							continue;
						final MiniJSON.JSONObject bookObj = data.getCheckedJSONObject(bookName);
						if(!bookObj.containsKey("author"))
							str.append("* This book might need to be republished.\n\r");
						str.append(index+") ^H"+bookName+"^?:\n\r");
						String purchased="0";
						String royalties="0";
						String popularity="0";
						if(bookObj.containsKey("copies_sold"))
							purchased=bookObj.getCheckedLong("copies_sold").toString();
						if(bookObj.containsKey("paid"))
							royalties=CMLib.beanCounter().abbreviatedPrice(mob, bookObj.getCheckedLong("paid").doubleValue());
						if(bookObj.containsKey("who"))
							popularity=bookObj.getCheckedArray("who").length+"";
						str.append(L("Purchased @x1 times for royalties of @x2, and a popularity of @x3.\n\r\n\r",
								purchased, royalties, popularity));
					}
					commonTell(mob,str.toString());
				}
				catch(final MiniJSON.MJSONException e)
				{
					Log.errOut(e);
				}
			}
			return false;
		}

		final Room R=mob.location();
		if(R==null)
			return false;
		final Area A=R.getArea();
		if(A==null)
			return false;
		final TimeClock C=A.getTimeObj();
		final MiniJSON.JSONObject obj=getData();
		if(obj.containsKey("lastpub")
		&&(!CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CMDMOBS))
		&&(!CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.ALLSKILLS)))
		{
			try
			{
				final Long L=obj.getCheckedLong("lastpub");
				final TimeClock lastPubC=(TimeClock)CMClass.getCommon("DefaultTimeClock");
				lastPubC.setFromHoursSinceEpoc(L.longValue());
				if(C.getYear() == lastPubC.getYear())
				{
					if(C.getMonth() <= lastPubC.getMonth())
					{
						commonTell(mob,L("You won't be able to publish any more books this month."));
						return false;
					}
				}
				else
				if(C.getYear() < lastPubC.getYear())
				{
					commonTell(mob,L("You last published in the year @x1?!!",""+lastPubC.getYear()));
					return false;
				}
			}
			catch (final MJSONException e)
			{
				Log.errOut(e);
			}
		}

		int startHere = -1;
		for(int i=commands.size()-1;i>=1;i--)
		{
			if(CMath.isNumber(commands.get(i)))
			{
				startHere=i;
				break;
			}
		}
		if(startHere < 0)
		{
			commonTell(mob,L("You haven't specified an asking price."));
			return false;
		}
		final List<String> remainV=new ArrayList<String>();
		for(int i=0;i<startHere;i++)
			remainV.add(commands.get(i));
		price=CMath.s_int(commands.get(startHere));
		final double denom=CMLib.english().matchAnyDenomination(CMLib.beanCounter().getCurrency(mob), CMParms.combine(commands,startHere+1));
		if(denom != 0)
			price *= denom;
 		final Item target = super.getTarget(mob, mob.location(), givenTarget, remainV, Wearable.FILTER_UNWORNONLY);
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			commonTell(mob,L("You don't seem to have a '@x1'.",CMParms.combine(remainV)));
			return false;
		}

		if((((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PAPER))
		&&(((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_LEATHER))
		&&(target.material()!=RawMaterial.RESOURCE_SILK)
		&&(target.material()!=RawMaterial.RESOURCE_HIDE))
		{
			commonTell(mob,L("You can't publish something like that."));
			return false;
		}
		if(!CMLib.flags().isReadable(target))
		{
			commonTell(mob,L("That's not even readable!"));
			return false;
		}

		/*
		String brand = getBrand(target);
		if((brand==null)||(brand.length()==0))
		{
			commonTell(mob,L("You aren't permitted to publish that."));
			return false;
		}
		*/
		if(!target.isGeneric())
		{
			commonTell(mob,L("You aren't able to publish that."));
			return false;
		}


		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		verb=L("publishing @x1",target.name());
		displayText=L("You are @x1",verb);
		found=target;
		success=true;
		if(!proficiencyCheck(mob,0,auto))
			success=false;
		final int duration=getDuration(30,mob,1,3);
		final CMMsg msg=CMClass.getMsg(mob,target,this,getActivityMessageType(),L("<S-NAME> start(s) publishing <T-NAME>."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
