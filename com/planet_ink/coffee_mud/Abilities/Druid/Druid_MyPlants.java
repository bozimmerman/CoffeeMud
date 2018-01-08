package com.planet_ink.coffee_mud.Abilities.Druid;
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
   Copyright 2002-2018 Bo Zimmerman

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
public class Druid_MyPlants extends StdAbility
{
	@Override
	public String ID()
	{
		return "Druid_MyPlants";
	}

	private final static String localizedName = CMLib.lang().L("My Plants");

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

	private static final String[] triggerStrings = I(new String[] { "MYPLANTS", "PLANTS" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_NATURELORE;
	}

	private static final Map<String,WeakArrayList<Item>> allPlants = new Hashtable<String,WeakArrayList<Item>>();

	public static void addNewPlant(MOB mob, Item I)
	{
		final List<Item> myPlants = getMyPlants(mob);
		if(!myPlants.contains(I))
			myPlants.add(I);
	}

	public static void removeLostPlant(MOB mob, Item I)
	{
		final List<Item> myPlants = getMyPlants(mob);
		myPlants.remove(I);
	}

	public static boolean isMyPlant(Item I, MOB mob)
	{
		if((I!=null)
		&&(I.rawSecretIdentity().equals(mob.Name()))
		&&(I.owner()!=null)
		&&(I.owner() instanceof Room))
		{
			for(final Enumeration<Ability> a=I.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A!=null)
				&&((A.invoker()==mob)||(A.text().equals(mob.Name())))
				&&(A instanceof Chant_SummonPlants))
					return true;
			}
		}
		return false;
	}

	public static Ability getMyPlantsSpell(Item I, MOB mob)
	{
		if((I!=null)
		&&(I.rawSecretIdentity().equals(mob.Name()))
		&&(I.owner()!=null)
		&&(I.owner() instanceof Room))
		{
			for(final Enumeration<Ability> a=I.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A!=null)
				&&((A.invoker()==mob)||(A.text().equals(mob.Name())))
				&&(A instanceof Chant_SummonPlants))
					return A;
			}
		}
		return null;
	}

	public static Item myPlant(Room R, MOB mob, int which)
	{
		int plantNum=0;
		if(R!=null)
		{
			for(int i=0;i<R.numItems();i++)
			{
				final Item I=R.getItem(i);
				if(isMyPlant(I,mob))
				{
					if(plantNum==which)
						return I;
					plantNum++;
				}
			}
		}
		return null;
	}

	public static List<Room> myAreaPlantRooms(MOB mob, Area A)
	{
		final List<Room> myPlantRooms = myPlantRooms(mob);
		final Vector<Room> V=new Vector<Room>();
		try
		{
			if(A!=null)
			{
				for(final Iterator<Room> r=myPlantRooms.iterator();r.hasNext();)
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

	private static void tryRoom(final MOB mob, final Room R, final List<Item> hisPlants, final Set<Item> alreadyDone)
	{
		if(R!=null)
		{
			for(int i=0;i<R.numItems();i++)
			{
				final Item I=R.getItem(i);
				if(isMyPlant(I,mob))
				{
					if(!alreadyDone.contains(I))
					{
						alreadyDone.add(I);
						hisPlants.add(I);
					}
				}
				else
				if(I instanceof BoardableShip)
				{
					Area A=((BoardableShip)I).getShipArea();
					if(A!=null)
					{
						for(Enumeration<Room> r=A.getFilledProperMap();r.hasMoreElements();)
						{
							try
							{
								Room R2=r.nextElement();
								if(R2 != null)
									tryRoom(mob, R2, hisPlants, alreadyDone);
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

	public static List<Item> getMyPlants(MOB mob)
	{
		List<Item> myPlants = allPlants.get(mob.Name());
		if(myPlants == null)
		{
			final WeakArrayList<Item> hisPlants = new WeakArrayList<Item>();
			final Set<Item> alreadyDone = new HashSet<Item>();
			for(final Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
			{
				try
				{
					final Room R=r.nextElement();
					if(R!=null)
						tryRoom(mob,R,hisPlants,alreadyDone);
				}
				catch (final NoSuchElementException e)
				{
				}
			}
			myPlants = hisPlants;
			allPlants.put(mob.Name(), hisPlants);
		}
		return myPlants;
	}

	public static List<Item> getMyPlants(final MOB mob, Collection<Room> rooms)
	{
		final List<Item> myPlants = getMyPlants(mob);
		final Vector<Item> V=new Vector<Item>();
		if(rooms == null)
			return V;
		for(int i=0;i<myPlants.size();i++)
		{
			try
			{
				Item I=myPlants.get(i);
				if(I!=null)
				{
					final Room R=CMLib.map().roomLocation(I);
					if((R!=null)&&(rooms.contains(R))&&(!V.contains(I)))
						V.addElement(I);
				}
			}
			catch(IndexOutOfBoundsException e)
			{
			}
		}
		return myPlants;
	}

	public static List<Room> myPlantRooms(MOB mob)
	{
		final List<Item> myPlants = getMyPlants(mob);
		final Vector<Room> V=new Vector<Room>();
		for(int i=0;i<myPlants.size();i++)
		{
			try
			{
				Item I=myPlants.get(i);
				if(I!=null)
				{
					final Room R=CMLib.map().roomLocation(I);
					if((R!=null)&&(!V.contains(R)))
						V.addElement(R);
				}
			}
			catch(IndexOutOfBoundsException e)
			{
			}
		}
		return V;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final boolean success=proficiencyCheck(mob,0,auto);

		if(!success)
			mob.tell(L("Your plant senses fail you."));
		else
		{
			final CMMsg msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_QUIETMOVEMENT|CMMsg.MASK_MAGIC,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final StringBuffer yourPlants=new StringBuffer("");
				int plantNum=0;
				final int[] cols=
				{
					CMLib.lister().fixColWidth(3,mob.session()),
					CMLib.lister().fixColWidth(20,mob.session()),
					CMLib.lister().fixColWidth(40,mob.session())
				};
				final List<Room> V=myPlantRooms(mob);
				for(int v=0;v<V.size();v++)
				{
					final Room R=V.get(v);
					if(R!=null)
					{
						int i=0;
						Item I=myPlant(R,mob,0);
						while(I!=null)
						{
							yourPlants.append(CMStrings.padRight(""+(++plantNum),cols[0])+" ");
							yourPlants.append(CMStrings.padRight(I.name(),cols[1])+" ");
							yourPlants.append(CMStrings.padRight(R.displayText(mob),cols[2]));
							yourPlants.append("\n\r");
							I=myPlant(R,mob,++i);
						}
					}
				}
				if(V.size()==0)
					mob.tell(L("You don't sense that there are ANY plants which are attuned to you."));
				else
					mob.tell(L("### Plant Name           Location\n\r@x1",yourPlants.toString()));
			}
		}
		return success;
	}
}

