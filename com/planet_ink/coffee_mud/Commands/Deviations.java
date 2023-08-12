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
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
* <p>Portions Copyright (c) 2003 Jeremy Vyska</p>
* <p>Portions Copyright (c) 2004-2023 Bo Zimmerman</p>

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
public class Deviations extends StdCommand
{
	public Deviations()
	{
	}

	private final static Class<?>[][] internalParameters=new Class<?>[][]{{Area.class}, {Room.class}};

	private final String[]	access	= I(new String[] { "DEVIATIONS" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	/** a constant used in the Deviations to filter on only items */
	public static final Filterer<Environmental> FILTER_ITEMONLY=new Filterer<Environmental>()
	{
		@Override
		public boolean passesFilter(final Environmental obj)
		{
			return (obj instanceof Item);
		}
	};

	/** a constant used in the Deviations to filter on only items */
	public static final Filterer<Environmental> FILTER_MOBONLY=new Filterer<Environmental>()
	{
		@Override
		public boolean passesFilter(final Environmental obj)
		{
			return (obj instanceof MOB);
		}
	};

	/** a constant used in the Deviations to filter on only items */
	public static final Filterer<Environmental> FILTER_WEAPONONLY=new Filterer<Environmental>()
	{
		@Override
		public boolean passesFilter(final Environmental obj)
		{
			return (obj instanceof Weapon);
		}
	};

	/** a constant used in the Deviations to filter on only items */
	public static final Filterer<Environmental> FILTER_ARMORONLY=new Filterer<Environmental>()
	{
		@Override
		public boolean passesFilter(final Environmental obj)
		{
			return (obj instanceof Armor);
		}
	};

	/** a constant used in the Deviations to filter on only items */
	public static final Filterer<Environmental> FILTER_ALL=new Filterer<Environmental>()
	{
		@Override
		public boolean passesFilter(final Environmental obj)
		{
			return true;
		}
	};

	protected enum DeviationType
	{
		MOBS(FILTER_MOBONLY),
		ITEMS(FILTER_ITEMONLY),
		WEAPONS(FILTER_WEAPONONLY),
		ARMORS(FILTER_ARMORONLY),
		BOTH(FILTER_ALL)
		;
		public Filterer<Environmental> filter;
		private DeviationType(final Filterer<Environmental> filter)
		{
			this.filter = filter;
		}
	}

	/** a constant used in the Deviations to filter on only items */
	public static final Comparator<Environmental> SORTBY_NAME=new Comparator<Environmental>()
	{
		@Override
		public int compare(final Environmental o1, final Environmental o2)
		{
			return o1.Name().toUpperCase().compareTo(o2.Name().toUpperCase());
		}
	};

	/** a constant used in the Deviations to filter on only items */
	public static final Comparator<Environmental> SORTBY_LEVEL=new Comparator<Environmental>()
	{
		@Override
		public int compare(final Environmental o1, final Environmental o2)
		{
			if(o1 instanceof Physical)
			{
				if(o2 instanceof Physical)
				{
					final int l1 = ((Physical)o1).phyStats().level();
					final int l2 = ((Physical)o2).phyStats().level();
					if(l1<l2) return -1;
					if(l1 == l2) return 0;
					return 1;
				}
				else
					return 1;
			}
			else
			if(o2 instanceof Physical)
				return -1;
			return 0;
		}
	};

	/** a constant used in the Deviations to filter on only items */
	public static final Comparator<Environmental> SORTBY_PLEVEL=new Comparator<Environmental>()
	{
		@Override
		public int compare(final Environmental o1, final Environmental o2)
		{
			if(o1 instanceof Item)
			{
				if(o2 instanceof Item)
				{
					final int l1 = CMLib.itemBuilder().timsLevelCalculator((Item)o1);
					final int l2 = CMLib.itemBuilder().timsLevelCalculator((Item)o2);
					if(l1<l2) return -1;
					if(l1 == l2) return 0;
					return 1;
				}
				else
				if(o2 instanceof MOB)
					return -1;
				else
					return 1;
			}
			else
			if(o1 instanceof MOB)
			{
				if(o2 instanceof MOB)
				{
					final int l1 = CMLib.leveler().getPowerLevel((MOB)o1);
					final int l2 = CMLib.leveler().getPowerLevel((MOB)o2);
					if(l1<l2) return -1;
					if(l1 == l2) return 0;
					return 1;
				}
				else
				if(o2 instanceof Item)
					return 1;
				else
					return -1;
			}
			else
			if(o2 instanceof Physical)
				return -1;
			return 0;
		}
	};

	/** a constant used in the Deviations to filter on only items */
	public static final Comparator<Environmental> SORTBY_TYPE=new Comparator<Environmental>()
	{
		private Integer code(final Environmental o)
		{
			if(o instanceof MOB)
				return Integer.valueOf(0);
			if(o instanceof Armor)
				return Integer.valueOf(1);
			if(o instanceof Weapon)
				return Integer.valueOf(2);
			return Integer.valueOf(3);
		}
		@Override
		public int compare(final Environmental o1, final Environmental o2)
		{
			return code(o1).compareTo(code(o2));
		}
	};

	protected enum DevSortBy
	{
		NAME(SORTBY_NAME),
		TYPE(SORTBY_TYPE),
		LEVEL(SORTBY_LEVEL),
		PLEVEL(SORTBY_PLEVEL)
		;
		Comparator<Environmental> comp;
		private DevSortBy(final Comparator<Environmental> comp)
		{
			this.comp=comp;
		}
	}

	protected String mobHeader(final Faction useFaction)
	{
		final StringBuffer str=new StringBuffer();
		str.append("\n\r");
		str.append(CMStrings.padRight(L("Name"),20)+" ");
		str.append(CMStrings.padRight(L("Lvl"),3)+" ");
		str.append(CMStrings.padRight(L("Pwr"),3)+" ");
		str.append(CMStrings.padRight(L("Att"),5)+" ");
		str.append(CMStrings.padRight(L("Dmg"),5)+" ");
		str.append(CMStrings.padRight(L("Armor"),5)+" ");
		str.append(CMStrings.padRight(L("Speed"),5)+" ");
		str.append(CMStrings.padRight(L("Rejv"),4)+" ");
		if(useFaction!=null)
			str.append(CMStrings.padRight(useFaction.name(),5)+" ");
		str.append(CMStrings.padRight(L("Money"),5)+" ");
		str.append(CMStrings.padRight(L("Worn"),5));
		str.append("\n\r");
		return str.toString();
	}

	protected String itemHeader()
	{
		final StringBuffer str=new StringBuffer();
		str.append("\n\r");
		str.append(CMStrings.padRight(L("Name"),20)+" ");
		str.append(CMStrings.padRight(L("Type"),10)+" ");
		str.append(CMStrings.padRight(L("Lvl"),3)+" ");
		str.append(CMStrings.padRight(L("Pwr"),4)+" ");
		str.append(CMStrings.padRight(L("Att"),5)+" ");
		str.append(CMStrings.padRight(L("Dmg"),5)+" ");
		str.append(CMStrings.padRight(L("Armor"),5)+" ");
		str.append(CMStrings.padRight(L("Value"),5)+" ");
		str.append(CMStrings.padRight(L("Rejuv"),5)+" ");
		str.append(CMStrings.padRight(L("Wght."),4));
		str.append("\n\r");
		return str.toString();
	}

	public boolean alreadyDone(final Environmental E, final Environmental actualE,
							   final List<Environmental> itemsDone, final Map<Environmental,Set<String>> locMap)
	{
		if(!locMap.containsKey(E))
			locMap.put(E, new TreeSet<String>());
		locMap.get(E).add(CMLib.map().getExtendedRoomID(CMLib.map().roomLocation(actualE)));
		for(int i=0;i<itemsDone.size();i++)
		{
			if(itemsDone.get(i).sameAs(E))
				return true;
		}
		return false;
	}

	private void delAllEffects(final Item I)
	{
		for(final Enumeration<Ability> a=I.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&(!A.isSavable()))
			{
				A.unInvoke();
				I.delEffect(A);
			}
		}
	}

	private void fillCheckDeviations(final Room R, final Filterer<Environmental> filter, final List<Environmental> check,
			final Map<Environmental,Set<String>> locMap)
	{
		for(int m=0;m<R.numInhabitants();m++)
		{
			final MOB M=R.fetchInhabitant(m);
			if((M!=null)
			&&(!M.isPlayer())
			&&(filter.passesFilter(M))
			&&(M.isSavable())
			&&(!alreadyDone(M,M,check,locMap)))
				check.add(M);
		}
		for(int i=0;i<R.numItems();i++)
		{
			final Item I=R.getItem(i);
			if((I!=null)
			&&(filter.passesFilter(I))
			&&((I instanceof Armor)||(I instanceof Weapon))
			&&(!alreadyDone(I,I,check,locMap)))
			{
				final Item checkI=(Item)I.copyOf();
				CMLib.threads().unTickAll(checkI);
				delAllEffects(checkI);
				checkI.setContainer(null);
				checkI.setOwner(null);
				checkI.recoverPhyStats();
				if(!alreadyDone(checkI,I,check,locMap))
				{
					check.add(checkI);
				}
				else
				{
					checkI.destroy();
				}
			}
		}
		for(int m=0;m<R.numInhabitants();m++)
		{
			final MOB M=R.fetchInhabitant(m);
			if((M!=null)
			&&(!M.isPlayer())
			&&(M.getStartRoom()!=null))
			{
				for(int i=0;i<M.numItems();i++)
				{
					final Item I=M.getItem(i);
					if((I!=null)
					&&(filter.passesFilter(I))
					&&((I instanceof Armor)||(I instanceof Weapon))
					&&(!alreadyDone(I,I,check,locMap)))
					{
						final Item checkI=(Item)I.copyOf();
						CMLib.threads().unTickAll(checkI);
						delAllEffects(checkI);
						checkI.setContainer(null);
						checkI.setOwner(null);
						checkI.recoverPhyStats();
						if(!alreadyDone(checkI,I,check,locMap))
							check.add(checkI);
						else
							checkI.destroy();
					}
				}
				final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
				if(SK!=null)
				{
					for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
					{
						final Environmental E2=i.next();
						if((E2 instanceof Item)
						&&(filter.passesFilter(E2)))
						{
							final Item I=(Item)E2;
							if(((I instanceof Armor)||(I instanceof Weapon))
							&&(!alreadyDone(I,I,check,locMap)))
							{
								final Item checkI=(Item)I.copyOf();
								CMLib.threads().unTickAll(checkI);
								delAllEffects(checkI);
								checkI.setContainer(null);
								checkI.setOwner(null);
								checkI.recoverPhyStats();
								if(!alreadyDone(checkI,I,check,locMap))
									check.add(checkI);
								else
									checkI.destroy();
							}
						}
					}
				}
			}
		}
	}

	protected String getDeviation(final int val, final Map<String, String> vals, final String key)
	{
		if(!vals.containsKey(key))
			return " - ";
		final int val2=CMath.s_int(vals.get(key));
		return getDeviation(val,val2);
	}

	protected String getDeviation(final double val, final double val2)
	{
		if(val==val2)
			return "0%";
		final double oval=val2-val;
		final int pval=(int)Math.round(CMath.div((oval<0)?(oval*-1):oval,val2==0?1:val2)*100.0);
		if(oval>0)
			return "-"+pval+"%";
		return "+"+pval+"%";
	}

	public String getColor(final Physical P)
	{
		final double lvl = P.phyStats().level();
		final double plvl;
		if(P instanceof Item)
			plvl = CMLib.itemBuilder().timsLevelCalculator((Item)P);
		else
		if(P instanceof MOB)
			plvl = CMLib.leveler().getPowerLevel((MOB)P);
		else
			return "";
		if(plvl > (2.5 * lvl))
			return "^r";
		if(plvl > (2.0 * lvl))
			return "^p";
		if(plvl > (1.5 * lvl))
			return "^b";
		if(plvl > (1.2 * lvl))
			return "^g";
		if(plvl > (0.8 * lvl))
			return "^w";
		return "^W";
	}

	public StringBuffer deviations(final MOB mob, final Room mobR, final String rest)
	{
		final List<String> V=CMParms.parse(rest);
		if((V.size()==0)
		||(CMath.s_valueOf(DeviationType.class, V.get(0).toUpperCase().trim())==null))
		{
			final String lst = CMLib.english().toEnglishStringList(DeviationType.class, false);
			return new StringBuffer(L("You must specify whether you want deviations on @x1.",lst));
		}

		final DeviationType type=(DeviationType)CMath.s_valueOf(DeviationType.class, V.get(0).toUpperCase());
		if(V.size()==1)
			return new StringBuffer("You must also specify a mob or item name, or the word room, or the word area.");
		DevSortBy sortBy = DevSortBy.TYPE;
		if((V.size()>3)
		&&(V.get(V.size()-2).equalsIgnoreCase("SORTBY")))
		{
			final String typ = V.get(V.size()-1);
			final DevSortBy cd = (DevSortBy)CMath.s_valueOf(DevSortBy.class, typ.toUpperCase().trim());
			if(cd == null)
			{
				final String lst = CMLib.english().toEnglishStringList(DevSortBy.class, false);
				return new StringBuffer(L("'@x1' is not a valid sort arg, try '@x2'.",typ,lst));
			}
			V.remove(V.size()-1);
			V.remove(V.size()-1);
			sortBy = cd;
		}
		boolean map=false;
		if((V.size()>1)
		&&(V.get(V.size()-1).equalsIgnoreCase("LOCATIONS")))
		{
			map=true;
			V.remove(V.size()-1);
		}

		Faction useFaction=null;
		for(final Enumeration<Faction> e=CMLib.factions().factions();e.hasMoreElements();)
		{
			final Faction F=e.nextElement();
			if(F.showInSpecialReported())
				useFaction=F;
		}
		final Map<Environmental,Set<String>> locMap = new HashMap<Environmental,Set<String>>();
		final String where=V.get(1).toLowerCase();
		final Environmental E=mobR.fetchFromMOBRoomFavorsItems(mob,null,where,Wearable.FILTER_ANY);
		final List<Environmental> check=new ArrayList<Environmental>();
		if(where.equalsIgnoreCase("room"))
			fillCheckDeviations(mobR,type.filter,check,locMap);
		else
		if(where.equalsIgnoreCase("area"))
		{
			for(final Enumeration<Room> r=mobR.getArea().getFilledCompleteMap();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				fillCheckDeviations(R,type.filter,check,locMap);
			}
		}
		else
		if(where.equalsIgnoreCase("metro"))
		{
			final Stack<Area> areasToDo = new Stack<Area>();
			areasToDo.add(mobR.getArea());
			while(areasToDo.size()>0)
			{
				final Area A = areasToDo.pop();
				for(final Enumeration<Room> r=A.getFilledCompleteMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					fillCheckDeviations(R,type.filter,check,locMap);
				}
				for(final Enumeration<Area> a = A.getChildren();a.hasMoreElements();)
					areasToDo.add(a.nextElement());
			}
		}
		else
		if(where.equalsIgnoreCase("world"))
		{
			for(final Enumeration<Room> r=CMLib.map().roomsFilled();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				fillCheckDeviations(R,type.filter,check,locMap);
			}
		}
		else
		if(E==null)
			return new StringBuffer(L("'@x1' is an unknown thing.",where));
		else
		if(!type.filter.passesFilter(E))
			return new StringBuffer(L("'@x1' is not '@x2'.",where,type.name()));
		else
		if(E instanceof Item)
			check.add((Item)E.copyOf());
		else
			check.add(E);
		final StringBuffer str=new StringBuffer("");
		str.append(L("Deviations Report:\n\r"));
		final StringBuffer itemResults = new StringBuffer();
		final StringBuffer mobResults = new StringBuffer();
		Collections.sort(check,sortBy.comp);
		for(int c=0;c<check.size();c++)
		{
			if(check.get(c) instanceof Item)
			{
				final Item I=(Item)check.get(c);
				Weapon W=null;
				final int maxRange;
				if(I instanceof Weapon)
				{
					W=(Weapon)I;
					maxRange=W.getRanges()[1];
				}
				else
					maxRange=I.maxRange();
				final Map<String,String> vals=CMLib.itemBuilder().timsItemAdjustments(
										I,I.phyStats().level(),I.material(),
										I.rawLogicalAnd()?2:1,
										(W==null)?0:W.weaponClassification(),
										maxRange,
										I.rawProperLocationBitmap());
				itemResults.append(getColor(I));
				itemResults.append(CMStrings.padRight(I.name(),20)+" ");
				itemResults.append(CMStrings.padRight(I.ID(),10)+" ");
				itemResults.append(CMStrings.padRight(""+I.phyStats().level(),4));
				itemResults.append(CMStrings.padRight(""+CMLib.itemBuilder().timsLevelCalculator(I),4));
				itemResults.append(CMStrings.padRight(""+getDeviation(
												I.basePhyStats().attackAdjustment(),
												vals,"ATTACK"),5)+" ");
				itemResults.append(CMStrings.padRight(""+getDeviation(
												I.basePhyStats().damage(),
												vals,"DAMAGE"),5)+" ");
				itemResults.append(CMStrings.padRight(""+getDeviation(
												I.basePhyStats().armor(),
												vals,"ARMOR"),5)+" ");
				itemResults.append(CMStrings.padRight(""+getDeviation(
												I.baseGoldValue(),
												vals,"VALUE"),5)+" ");
				itemResults.append(CMStrings.padRight(
						""+(((I.phyStats().rejuv()==PhyStats.NO_REJUV)||(I.phyStats().rejuv()==0))?" -  ":""+I.phyStats().rejuv()),5)+" ");
				if(I instanceof Weapon)
					itemResults.append(CMStrings.padRight(""+I.basePhyStats().weight(),4));
				else
					itemResults.append(CMStrings.padRight(""+getDeviation(
													I.basePhyStats().weight(),
													vals, "WEIGHT"), 4));
				/*
				if(I instanceof Armor)
					itemResults.append(CMStrings.padRight(""+((Armor)I).phyStats().height(),4));
				else
					itemResults.append(CMStrings.padRight(" - ",4)+" ");
				*/
				itemResults.append("\n\r");
				if(map && locMap.containsKey(check.get(c)))
				{
					itemResults.append("     @");
					for(final String s : locMap.get(check.get(c)))
						itemResults.append(s).append("\n\r");
				}
				I.destroy(); // these are always copies, so all good
			}
			else
			{
				final MOB M=(MOB)check.get(c);
				mobResults.append(getColor(M));
				mobResults.append(CMStrings.padRight(M.name(),20)+" ");
				mobResults.append(CMStrings.padRight(""+M.phyStats().level(),4));
				mobResults.append(CMStrings.padRight(""+CMLib.leveler().getPowerLevel(M),4));
				mobResults.append(CMStrings.padRight(""+getDeviation(
												M.basePhyStats().attackAdjustment(),
												CMLib.leveler().getLevelAttack(M)),5)+" ");
				mobResults.append(CMStrings.padRight(""+getDeviation(
												M.basePhyStats().damage(),
												CMLib.leveler().getLevelMOBDamage(M)),5)+" ");
				mobResults.append(CMStrings.padRight(""+getDeviation(
												100.0-M.basePhyStats().armor(),
												100.0-CMLib.leveler().getLevelMOBArmor(M)),5)+" ");
				mobResults.append(CMStrings.padRight(""+getDeviation(
												M.basePhyStats().speed(),
												CMLib.leveler().getLevelMOBSpeed(M)),5)+" ");
				mobResults.append(CMStrings.padRight(""+(((M.phyStats().rejuv()==PhyStats.NO_REJUV)||(M.phyStats().rejuv()==0))?" -  ":""+M.phyStats().rejuv()) ,4)+" ");
				if(useFaction!=null)
					mobResults.append(CMStrings.padRight(""+(M.fetchFaction(useFaction.factionID())==Integer.MAX_VALUE?"N/A":""+M.fetchFaction(useFaction.factionID())),5)+" ");
				final double value = CMLib.beanCounter().getTotalAbsoluteNativeValue(M);
				final double[] range = CMLib.leveler().getLevelMoneyRange(M);
				if(value < range[0])
					mobResults.append(CMStrings.padRight(""+getDeviation(value,range[0]),5)+" ");
				else
				if(value > range[1])
					mobResults.append(CMStrings.padRight(""+getDeviation(value,range[1]),5)+" ");
				else
					mobResults.append(CMStrings.padRight("0%",5)+" ");
				int reallyWornCount = 0;
				for(int j=0;j<M.numItems();j++)
				{
					final Item Iw=M.getItem(j);
					if(!(Iw.amWearingAt(Wearable.IN_INVENTORY)))
						reallyWornCount++;
				}
				mobResults.append(CMStrings.padRight(""+reallyWornCount,5)+" ");
				mobResults.append("\n\r");
				if(map && locMap.containsKey(check.get(c)))
				{
					mobResults.append("     @");
					for(final String s : locMap.get(check.get(c)))
						mobResults.append(s).append("\n\r");
				}
			}
		}
		if(itemResults.length()>0)
			str.append(itemHeader()+itemResults.toString()+"^N");
		if(mobResults.length()>0)
			str.append(mobHeader(useFaction)+mobResults.toString()+"^N");
		return str;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		mob.tell(deviations(mob,mob.location(),CMParms.combine(commands,1)).toString());
		return false;
	}

	@Override
	public Object executeInternal(final MOB mob, final int metaFlags, final Object... args) throws java.io.IOException
	{
		if(!super.checkArguments(internalParameters, args))
			return Boolean.FALSE.toString();

		Room R=null;
		boolean roomTarget=false;
		for(final Object o : args)
		{
			if(o instanceof Area)
			{
				R=((Area)o).getRandomProperRoom();
			}
			else
			if(o instanceof Room)
			{
				roomTarget=true;
				R=(Room)o;
			}
		}
		if(R==null)
			return "";
		else
		if(roomTarget)
			return deviations(mob, R, "BOTH ROOM").toString();
		else
			return deviations(mob, R, "BOTH AREA LOCATIONS").toString();
	}

	@Override
	public boolean securityCheck(final MOB mob)
	{
		return CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDITEMS)
			|| CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN)
			|| CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDMOBS);
	}
}
