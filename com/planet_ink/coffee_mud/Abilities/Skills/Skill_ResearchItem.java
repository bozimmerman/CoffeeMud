package com.planet_ink.coffee_mud.Abilities.Skills;
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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2002-2021 Bo Zimmerman

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
public class Skill_ResearchItem extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_ResearchItem";
	}

	private final static String	localizedName	= CMLib.lang().L("Research Item");

	@Override
	public String name()
	{
		return localizedName;
	}

	protected String	displayText	= L("(Researching)");

	@Override
	public String displayText()
	{
		return displayText;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "RESEARCHITEM" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	@Override
	public void unInvoke()
	{
		if(!unInvoked)
		{
			final Physical affected=this.affected;
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if(itemsFound.size()==0)
					mob.tell(L("Your research fails to find anything on '@x1'.",what));
				else
				{
					for(final String found : itemsFound)
						mob.tell(found);
				}
			}
		}
		super.unInvoke();
	}

	protected final List<String> itemsFound=new Vector<String>();
	protected Room theRoom = null;
	protected Iterator<Room> checkIter=null;
	protected String what = "";
	protected int ticksToRemain = 0;
	protected int numBooksInRoom = 1;
	protected int numRoomsToDo = 0;

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		itemsFound.clear();
		numBooksInRoom = 1;
		what = newMiscText;
		ticksToRemain = 0;
		theRoom=null;
		numRoomsToDo=0;
		checkIter=null;
		final Physical affected = this.affected;
		if(affected instanceof MOB)
			numBooksInLibrary((MOB)affected); // sets the appropriate variables
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID==Tickable.TICKID_MOB)
		{
			final Physical affected=this.affected;
			if(!(affected instanceof MOB))
				return true;
			final MOB mob=(MOB)affected;
			Room R;
			synchronized(this)
			{
				R=theRoom;
			}
			if(R == null)
			{
				if(numBooksInLibrary(mob)==0)
				{
					mob.tell(L("You fail researching."));
					unInvoke();
					return false;
				}
				R=theRoom;
				if(R==null)
				{
					unInvoke();
					return false;
				}
			}
			if(!R.isInhabitant(mob)
			||(mob.isInCombat())
			||(!CMLib.flags().canBeSeenBy(R, mob))
			||(!CMLib.flags().isAliveAwakeMobileUnbound(mob,true)))
			{
				this.itemsFound.clear();
				mob.tell(L("You stop researching."));
				unInvoke();
				return false;
			}
			if((tickDown==4)&&(checkIter != null)&&(!checkIter.hasNext()))
			{
				if(!R.show(mob,null,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> <S-IS-ARE> almost done researching '@x1'",what)))
				{
					unInvoke();
					return false;
				}
			}
			else
			if((tickDown%4)==0)
			{
				if(!R.show(mob,null,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> continue(s) researching '@x1'",what)))
				{
					unInvoke();
					return false;
				}
			}
			if(checkIter==null)
			{
				final HashSet<Area> areas=new HashSet<Area>();
				Area A=null;
				final HashSet<Area> areasTried=new HashSet<Area>();
				int numAreas = 0;
				numAreas=(int)Math.round(CMath.mul(CMLib.map().numAreas(),0.90))+1;
				if(numAreas>CMLib.map().numAreas())
					numAreas=CMLib.map().numAreas();
				int tries=numAreas*numAreas;
				while((areas.size()<numAreas)&&(((--tries)>0)))
				{
					A=CMLib.map().getRandomArea();
					if((A!=null)&&(!areasTried.contains(A)))
					{
						areasTried.add(A);
						if((CMLib.flags().canAccess(mob,A))
						&&(!CMath.bset(A.flags(),Area.FLAG_INSTANCE_CHILD)))
							areas.add(A);
						else
							numAreas--;
					}
				}
				final TrackingLibrary.TrackingFlags flags=CMLib.tracking().newFlags();
				final int range=25 + (2*super.getXLEVELLevel(mob))+(10*super.getXMAXRANGELevel(mob));
				final List<Room> checkSet=CMLib.tracking().getRadiantRooms(mob.location(),flags,range);
				checkIter=checkSet.iterator();
				numRoomsToDo=(checkSet.size()/(tickDown-1))+1;
				return true;
			}
			else
			if((tickDown > 1)&&(checkIter.hasNext()))
			{
				final int maxFound=1+(super.getXLEVELLevel(mob));
				// look for it!
				MOB inhab=null;
				Environmental item=null;
				Room room=null;
				ShopKeeper SK=null;
				if(tickDown<3)
					numRoomsToDo=Integer.MAX_VALUE;
				final CMFlagLibrary flags=CMLib.flags();
				final LegalLibrary law=CMLib.law();
				final WorldMap map=CMLib.map();
				for (int r=0;(r<numRoomsToDo) && checkIter.hasNext();r++)
				{
					room=map.getRoom(checkIter.next());
					if((!flags.canAccess(mob,room))
					||((law.isLandOwnable(room))&&(!law.doesHavePriviledgesHere(mob, room))))
						continue;

					item=room.findItem(null,what);
					if((item!=null)
					&&(flags.canBeLocated((Item)item)))
					{
						final String str=L("@x1 is in a place called '@x2' in @x3.",item.name(),room.displayText(mob),room.getArea().name());
						itemsFound.add(str);
						if(item instanceof Physical)
						{
							ticksToRemain += (2*((Physical)item).phyStats().level())-adjustedLevel(mob,0)-numBooksInRoom;
							if(ticksToRemain<0)
								ticksToRemain=0;
						}
					}
					for(int i=0;i<room.numInhabitants();i++)
					{
						inhab=room.fetchInhabitant(i);
						if(inhab==null)
							break;
						if(((!flags.isCloaked(inhab))
						||((CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.CLOAK)||CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.WIZINV))
							&&(mob.phyStats().level()>=inhab.phyStats().level()))))
						{
							item=inhab.findItem(what);
							SK=CMLib.coffeeShops().getShopKeeper(inhab);
							if((item==null)&&(SK!=null))
								item=SK.getShop().getStock(what,mob);
							if((item instanceof Item)
							&&(flags.canBeLocated((Item)item)))
							{
								final CMMsg msg2=CMClass.getMsg(mob,inhab,this,verbalCastCode(mob,null,false),null);
								if(room.okMessage(mob,msg2))
								{
									room.send(mob,msg2);
									if(item instanceof Physical)
									{
										ticksToRemain += (2*((Physical)item).phyStats().level())-adjustedLevel(mob,0)-numBooksInRoom;
										if(ticksToRemain <0)
											ticksToRemain=0;
									}
									final String str=L("@x1@x2 is being carried by @x3 in a place called '@x4' in @x5."
											,item.name(),"",inhab.name(),room.displayText(mob),room.getArea().name());
									itemsFound.add(str);
									break;
								}
							}
						}
					}
					if(itemsFound.size()>=maxFound)
						break;
					while(itemsFound.size()>maxFound)
						itemsFound.remove(CMLib.dice().roll(1,itemsFound.size(),-1));
				}
				if((!checkIter.hasNext())||(tickDown==2)) // set the final time remaning
					tickDown += ticksToRemain;
			}
		}
		return true;
	}

	protected int numBooksInLibrary(final MOB mob)
	{
		if(mob==null)
			return 0;
		final Room R=mob.location();
		if(R==null)
			return 0;
		if(theRoom == null)
		{
			numBooksInRoom = 0;
			theRoom=R;
			if(CMLib.english().containsString(R.displayText(), "library"))
				numBooksInRoom += 10;
			for(final Enumeration<Item> i=R.items();i.hasMoreElements();)
			{
				final Item I=i.nextElement();
				if((I instanceof Book)
				&&(((Book)I).getUsedPages()>0))
					numBooksInRoom++;
			}
			for(final Enumeration<Item> i=mob.items();i.hasMoreElements();)
			{
				final Item I=i.nextElement();
				if((I instanceof Book)
				&&(((Book)I).getUsedPages()>0))
					numBooksInRoom++;
			}
			for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
			{
				final MOB M=m.nextElement();
				if(M==null)
					continue;
				final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
				if(SK==null)
					continue;
				final CoffeeShop shop=SK.getShop();
				if(shop!=null)
				{
					for(final Iterator<Environmental> i=shop.getStoreInventory();i.hasNext();)
					{
						final Environmental I=i.next();
						if((I instanceof Book)
						&&(((Book)I).getUsedPages()>0))
							numBooksInRoom++;
					}
				}
			}
		}
		return numBooksInRoom;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final String itemName=CMParms.combine(commands);
		if(itemName.trim().length()==0)
		{
			mob.tell(L("Research what item?"));
			return false;
		}
		final Room R=mob.location();
		if(R==null)
			return false;

		if(this.numBooksInLibrary(mob)==0)
		{
			mob.tell(L("I don't think you'll get much research done here."));
			return false;
		}

		if(mob.isInCombat())
		{
			mob.tell(L("You are too busy to do that right now."));
			return false;
		}

		if(!CMLib.flags().canBeSeenBy(R, mob))
		{
			mob.tell(L("You need to be able to see to do that."));
			return false;
		}
		if(!CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
		{
			mob.tell(L("You can't do that right now."));
			return false;
		}

		if(!super.invoke(mob, commands, givenTarget, auto, asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,mob.isMonster()?null:L("<S-NAME> begin(s) to research the location of '@x1'.",itemName));
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				final Skill_ResearchItem researchA = (Skill_ResearchItem)beneficialAffect(mob,mob,asLevel,10);
				if(researchA != null)
				{
					researchA.tickDown=10; // override any expertise!
					researchA.setMiscText(itemName);
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to engage in research, but can't get started."));
		// return whether it worked
		return success;
	}
}
