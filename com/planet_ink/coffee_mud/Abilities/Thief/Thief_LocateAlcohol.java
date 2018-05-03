package com.planet_ink.coffee_mud.Abilities.Thief;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2016-2018 Bo Zimmerman

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

public class Thief_LocateAlcohol extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_LocateAlcohol";
	}

	private final static String localizedName = CMLib.lang().L("Locate Alcohol");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_DIVINATION;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Locating Alcohol)");

	private static final String[]	triggerStrings			= I(new String[] { "LOCATEALCOHOL" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT | USAGE_MANA;
	}

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_TRACKING;
	}

	protected List<Room> theTrail=null;
	public int nextDirection=-2;

	public String alcoholCheck(MOB mob, Item I, StringBuffer msg)
	{
		if(I==null)
			return "";
		if(CMLib.flags().isAlcoholic(I)
		&&(CMLib.flags().canBeSeenBy(I,mob)))
		{
			if((I.container()!=null)&&(I.ultimateContainer(null)!=I))
				msg.append(L("@x1 contains alcohol.\n\r",I.ultimateContainer(null).name()));
			else
				msg.append(L("@x1 contains some sort of alcohol.\n\r",I.name(mob)));
		}
		return msg.toString();
	}

	public String alcoholHere(MOB mob, Environmental E)
	{
		final StringBuffer msg=new StringBuffer("");
		if(E==null)
			return msg.toString();
		if((E instanceof Room)&&(CMLib.flags().canBeSeenBy(E,mob)))
		{
			final Room room=(Room)E;
			{
				for(int i=0;i<room.numItems();i++)
				{
					final Item I=room.getItem(i);
					if(I!=null)
						alcoholCheck(mob,I,msg);
				}
				for(int m=0;m<room.numInhabitants();m++)
				{
					final MOB M=room.fetchInhabitant(m);
					if((M!=null)&&(M!=mob))
						msg.append(alcoholHere(mob,M));
				}
			}
		}
		else
		if((E instanceof Item)&&(CMLib.flags().canBeSeenBy(E,mob)))
		{
			alcoholCheck(mob,(Item)E,msg);
			if(E instanceof Container)
			for(Item I : ((Container)E).getContents())
				alcoholCheck(mob,I,msg);
		}
		else
		if((E instanceof MOB)&&(CMLib.flags().canBeSeenBy(E,mob)))
		{
			for(int i=0;i<((MOB)E).numItems();i++)
			{
				final Item I=((MOB)E).getItem(i);
				final StringBuffer msg2=new StringBuffer("");
				alcoholCheck(mob,I,msg2);
				if(msg2.length()>0)
					return E.name()+" is carrying some alcohol.";
			}
			final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(E);
			if(SK!=null)
			{
				final StringBuffer msg2=new StringBuffer("");
				for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
				{
					final Environmental E2=i.next();
					if(E2 instanceof Item)
						alcoholCheck(mob,(Item)E2,msg2);
					if(msg2.length()>0)
						return E.name()+" has some alcohol in stock.";
				}
			}
		}
		return msg.toString();
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID==Tickable.TICKID_MOB)
		{
			if(nextDirection==-999)
				return true;

			if((theTrail==null)
			||(affected == null)
			||(!(affected instanceof MOB)))
				return false;

			final MOB mob=(MOB)affected;

			if(!CMLib.flags().canSmell(mob))
			{
				mob.tell(L("The alcohol trail fizzles out here."));
				nextDirection=-999;
				unInvoke();
				return false;
			}
			else
			if(nextDirection==999)
			{
				mob.tell(alcoholHere(mob,mob.location()));
				nextDirection=-2;
				unInvoke();
				return false;
			}
			else
			if(nextDirection==-1)
			{
				if(alcoholHere(mob,mob.location()).length()==0)
					mob.tell(L("The alcohol trail fizzles out here."));
				nextDirection=-999;
				unInvoke();
				return false;
			}
			else
			if(nextDirection>=0)
			{
				mob.tell(L("Your smell alcohol @x1.",CMLib.directions().getDirectionName(nextDirection)));
				nextDirection=-2;
			}

		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if(!(affected instanceof MOB))
			return;

		final MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(msg.amITarget(mob.location()))
		&&(CMLib.flags().canBeSeenBy(mob.location(),mob))
		&&(msg.targetMinor()==CMMsg.TYP_LOOK))
			nextDirection=CMLib.tracking().trackNextDirectionFromHere(theTrail,mob.location(),false);
		
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.target()!=null)
		&&(msg.amISource((MOB)affected))
		&&((msg.sourceMinor()==CMMsg.TYP_LOOK)||(msg.sourceMinor()==CMMsg.TYP_EXAMINE)))
		{
			if((msg.tool()!=null)&&(msg.tool().ID().equals(ID())))
			{
				final String str=alcoholHere((MOB)affected,msg.target());
				if(str.length()>0)
				{
					((MOB)affected).tell(str);
					unInvoke();
				}
			}
			else
			if((msg.target()!=null)
			&&(alcoholHere((MOB)affected,msg.target()).length()>0)
			&&(msg.source()!=msg.target()))
			{
				final CMMsg msg2=CMClass.getMsg(msg.source(),msg.target(),this,CMMsg.MSG_LOOK,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,null);
				msg.addTrailerMsg(msg2);
			}
		}
	}

	@Override
	public void affectPhyStats(Physical affectedEnv, PhyStats affectableStats)
	{
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_TRACK);
		super.affectPhyStats(affectedEnv, affectableStats);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> already trying to find a stiff drink."));
			return false;
		}
		if(!CMLib.flags().canSmell(target))
		{
			mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> unable to smell alcohol."));
			return false;
		}
		final List<Ability> V=CMLib.flags().flaggedAffects(mob,Ability.FLAG_TRACKING);
		for (final Ability A : V)
			A.unInvoke();

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final String here=alcoholHere(target,target.location());
		if(here.length()>0)
		{
			target.tell(here);
			return true;
		}

		final boolean success=proficiencyCheck(mob,0,auto);

		TrackingLibrary.TrackingFlags flags;
		flags = CMLib.tracking().newFlags();
		final Vector<Room> rooms=new Vector<Room>();
		final List<Room> checkSet=CMLib.tracking().getRadiantRooms(mob.location(),flags,15+adjustedLevel(mob,asLevel));
		for (final Room R : checkSet)
		{
			final Room R2=CMLib.map().getRoom(R);
			if(R2!=null)
			{
				if(alcoholHere(mob,R2).length()>0)
					rooms.addElement(R2);
			}
		}

		if(rooms.size()>0)
		{
			//TrackingLibrary.TrackingFlags flags;
			flags = CMLib.tracking().newFlags()
					.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
					.plus(TrackingLibrary.TrackingFlag.NOAIR);
			theTrail=CMLib.tracking().findTrailToAnyRoom(target.location(),rooms,flags,50+adjustedLevel(mob,asLevel));
		}

		if((success)&&(theTrail!=null))
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_DELICATE_HANDS_ACT,auto?L("<T-NAME> begin(s) to sense alcohol!"):L("^S<S-NAME> sniff(s) around for signs of a stiff drink.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Thief_LocateAlcohol newOne=(Thief_LocateAlcohol)this.copyOf();
				if(target.fetchEffect(newOne.ID())==null)
					target.addEffect(newOne);
				target.recoverPhyStats();
				newOne.nextDirection=CMLib.tracking().trackNextDirectionFromHere(newOne.theTrail,target.location(),false);
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> sniff(s) around for alcohol, but fail(s)."));

		return success;
	}
}
