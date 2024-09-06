package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2024-2024 Bo Zimmerman

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
public class Thief_MyUrchins extends StdAbility
{
	@Override
	public String ID()
	{
		return "Thief_MyUrchins";
	}

	private final static String localizedName = CMLib.lang().L("My Urchins");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	private static final String[] triggerStrings = I(new String[] { "MYURCHINS"});

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_STREETSMARTS;
	}

	@Override
	public int usageType()
	{
		return USAGE_MANA|USAGE_MOVEMENT;
	}

	protected Set<MOB> myUrchins = Collections.synchronizedSet(new HashSet<MOB>());

	private static final Map<String,WeakArrayList<MOB>> allUrchins = new Hashtable<String,WeakArrayList<MOB>>();

	public static void addNewUrchin(final MOB mob, final MOB M)
	{
		final List<MOB> myUrchins = getMyUrchins(mob);
		if(!myUrchins.contains(M))
			myUrchins.add(M);
	}

	public static void removeLostUrchin(final MOB mob, final MOB M)
	{
		final List<MOB> myUrchins = getMyUrchins(mob);
		myUrchins.remove(M);
	}

	public static boolean isMyUrchin(final MOB M, final MOB mob)
	{
		if((M!=null)
		&&(M.getLiegeID().equalsIgnoreCase(mob.Name()))
		&&(M.fetchBehavior("Thiefness")!=null)
		&&(M.fetchBehavior("Scavenger")!=null))
			return true;
		return false;
	}

	public static MOB myUrchin(final Room R, final MOB mob, final int which)
	{
		int urchinNum=0;
		if(R!=null)
		{
			for(int i=0;i<R.numInhabitants();i++)
			{
				final MOB M=R.fetchInhabitant(i);
				if(isMyUrchin(M,mob))
				{
					if(urchinNum==which)
						return M;
					urchinNum++;
				}
			}
		}
		return null;
	}

	public static List<Room> myAreaUrchinRooms(final MOB mob, final Area A)
	{
		final List<Room> myUrchinRooms = myUrchinRooms(mob);
		final Vector<Room> V=new Vector<Room>();
		try
		{
			if(A!=null)
			{
				for(final Iterator<Room> r=myUrchinRooms.iterator();r.hasNext();)
				{
					final Room R=r.next();
					if((A.inMyMetroArea(R.getArea())) && (!V.contains(R)))
						V.addElement(R);
				}
			}
		}
		catch (final NoSuchElementException e)
		{
		}
		return V;
	}

	private static void tryRoom(final MOB mob, final Room R, final List<MOB> hisUrchins, final Set<MOB> alreadyDone)
	{
		if(R!=null)
		{
			for(int i=0;i<R.numInhabitants();i++)
			{
				final MOB M=R.fetchInhabitant(i);
				if(isMyUrchin(M,mob))
				{
					if(!alreadyDone.contains(M))
					{
						alreadyDone.add(M);
						hisUrchins.add(M);
					}
				}
			}
			for(int i=0;i<R.numItems();i++)
			{
				final Item I = R.getItem(i);
				if(I instanceof Boardable)
				{
					final Area A=((Boardable)I).getArea();
					if(A!=null)
					{
						for(final Enumeration<Room> r=A.getFilledProperMap();r.hasMoreElements();)
						{
							try
							{
								final Room R2=r.nextElement();
								if(R2 != null)
									tryRoom(mob, R2, hisUrchins, alreadyDone);
							}
							catch (final NoSuchElementException e)
							{
							}
						}
					}
				}
			}
		}
	}

	public static List<MOB> getMyUrchins(final MOB mob)
	{
		List<MOB> myUrchins = allUrchins.get(mob.Name());
		if(myUrchins == null)
		{
			final WeakArrayList<MOB> hisUrchins = new WeakArrayList<MOB>();
			final Set<MOB> alreadyDone = new HashSet<MOB>();
			for(final Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
			{
				try
				{
					final Room R=r.nextElement();
					if(R!=null)
						tryRoom(mob,R,hisUrchins,alreadyDone);
				}
				catch (final NoSuchElementException e)
				{
				}
			}
			myUrchins = hisUrchins;
			allUrchins.put(mob.Name(), hisUrchins);
		}
		return myUrchins;
	}

	public static List<MOB> getMyUrchins(final MOB mob, final Collection<Room> rooms)
	{
		final List<MOB> myUrchins = getMyUrchins(mob);
		final Vector<MOB> V=new Vector<MOB>(); // return value
		if(rooms == null)
			return V;
		for(int i=0;i<myUrchins.size();i++)
		{
			try
			{
				final MOB M=myUrchins.get(i);
				if(M!=null)
				{
					final Room R=CMLib.map().roomLocation(M);
					if((R!=null)&&(rooms.contains(R))&&(!V.contains(M)))
						V.addElement(M);
				}
			}
			catch(final IndexOutOfBoundsException e)
			{
			}
		}
		return myUrchins;
	}

	public static List<Room> myUrchinRooms(final MOB mob)
	{
		final List<MOB> myUrchins = getMyUrchins(mob);
		final Vector<Room> V=new Vector<Room>(); // return value
		for(int i=0;i<myUrchins.size();i++)
		{
			try
			{
				final MOB M=myUrchins.get(i);
				if(M!=null)
				{
					final Room R=CMLib.map().roomLocation(M);
					if((R!=null)&&(!V.contains(R)))
						V.addElement(R);
				}
			}
			catch(final IndexOutOfBoundsException e)
			{
			}
		}
		return V;
	}

	enum UrchiField
	{
		Name(20),
		Lvl(4),
		Room_Name(30),
		Wealth(8),
		Health(7),
		Wanted(7)
		;
		public int len;
		private UrchiField(final int x)
		{
			len=x;
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final boolean success=proficiencyCheck(mob,0,auto);

		if(!success)
			commonTelL(mob,"Your recollection fails you.");
		else
		{
			final CMMsg msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_THINK,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final List<UrchiField> fields = new ArrayList<UrchiField>();
				fields.add(UrchiField.Name);
				final int xlevel = super.getXLEVELLevel(mob);
				if(xlevel > 0)
					fields.add(UrchiField.Lvl);
				if(xlevel > 2)
					fields.add(UrchiField.Room_Name);
				if(xlevel > 3)
					fields.add(UrchiField.Wealth);
				if(xlevel > 5)
					fields.add(UrchiField.Health);
				if(xlevel > 8)
					fields.add(UrchiField.Wanted);
				final int[] cols = new int[fields.size()];
				for(int x=0;x<fields.size();x++)
					cols[x] = CMLib.lister().fixColWidth(fields.get(x).len, mob);
				final StringBuffer yourUrchins=new StringBuffer("^H");
				for(int x=0;x<fields.size();x++)
				{
					final UrchiField f = fields.get(x);
					yourUrchins.append(CMStrings.padRight(L(f.name().replace('_', ' ')), cols[x]));
				}
				yourUrchins.append("^?\n\r^H");
				for(int x=0;x<fields.size();x++)
					yourUrchins.append(CMStrings.repeat('-', cols[x]));
				yourUrchins.append("^?\n\r");
				final List<Room> V=myUrchinRooms(mob);
				for(int v=0;v<V.size();v++)
				{
					final Room R=V.get(v);
					if(R!=null)
					{
						int i=0;
						MOB M=myUrchin(R,mob,0);
						while(M!=null)
						{
							for(int x=0;x<fields.size();x++)
							{
								final UrchiField f = fields.get(x);
								String val;
								switch(f)
								{
								case Health:
									val=""+M.curState().getHitPoints()+"hp";
									break;
								case Lvl:
									val=""+M.phyStats().level();
									break;
								case Name:
									val=M.name();
									break;
								case Room_Name:
									val=R.displayText(M);
									break;
								case Wanted:
									{
										val=L("No");
										final LegalBehavior B = CMLib.law().getLegalBehavior(R.getArea());
										final Area lA = CMLib.law().getLegalObject(R.getArea());
										if((B!=null)&&(lA != null))
										{
											final List<LegalWarrant> warrants = B.getWarrantsOf(lA, M);
											if((warrants != null)&&(warrants.size()>0))
												val=L("Yes");
										}
									}
									break;
								case Wealth:
									val=CMLib.beanCounter().abbreviatedPrice(M, CMLib.beanCounter().getTotalAbsoluteNativeValue(M));
									break;
								default:
									val="";
									break;
								}
								if(x==fields.size()-1)
									yourUrchins.append(CMStrings.limit(val, cols[x]));
								else
									yourUrchins.append(CMStrings.padRight(val, cols[x]));
							}
							yourUrchins.append("\n\r");
							M=myUrchin(R,mob,++i);
						}
					}
				}
				if(V.size()==0)
					commonTelL(mob,"You don't seem to recall any urchins sworn to you.");
				else
					commonTelL(mob,yourUrchins.toString());
			}
		}
		return success;
	}
}

