package com.planet_ink.coffee_mud.Abilities.Prayers;
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
   Copyright 2022-2025 Bo Zimmerman

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
public class Prayer_SenseBombs extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_SenseBombs";
	}

	private final static String localizedName = CMLib.lang().L("Sense Bombs");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Sensing Bombs)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NEUTRAL | Ability.FLAG_DIVINING;
	}
	Room lastRoom=null;

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
			lastRoom=null;
		super.unInvoke();
		if(canBeUninvoked())
			mob.tell(L("Your senses are no longer sensitive to bombs."));
	}

	public String trapCheck(final Physical P)
	{
		if(P!=null)
		{
			final Trap T=CMLib.utensils().fetchMyTrap(P);
			if((T!=null)&&(T.isABomb()))
				return L("@x1 is a bomb.\n\r",P.name());
		}
		return "";
	}

	public String trapHere(final MOB mob, final Physical P)
	{
		final StringBuffer msg=new StringBuffer("");
		if(P==null)
			return msg.toString();
		if((P instanceof Room)
		&&(CMLib.flags().canBeSeenBy(P,mob)))
		{
			msg.append(trapCheck(P));
			final Room R=(Room)P;
			for(int i=0;i<R.numItems();i++)
			{
				final Item I=R.getItem(i);
				if((I!=null)&&(I.container()==null))
					msg.append(trapHere(mob,I));
			}
			for(int m=0;m<R.numInhabitants();m++)
			{
				final MOB M=R.fetchInhabitant(m);
				if((M!=null)&&(M!=mob))
					msg.append(trapHere(mob,M));
			}
		}
		else
		if((P instanceof Container)
		&&(CMLib.flags().canBeSeenBy(P,mob)))
		{
			final Container C=(Container)P;
			final List<Item> V=C.getDeepContents();
			for(int v=0;v<V.size();v++)
			{
				if(trapCheck(V.get(v)).length()>0)
					msg.append(L("@x1 contains a bomb.\n",C.name()));
			}
		}
		else
		if((P instanceof Item)&&(CMLib.flags().canBeSeenBy(P,mob)))
			msg.append(trapCheck(P));
		else
		if((P instanceof MOB)&&(CMLib.flags().canBeSeenBy(P,mob)))
		{
			for(int i=0;i<((MOB)P).numItems();i++)
			{
				final Item I=((MOB)P).getItem(i);
				if(trapCheck(I).length()>0)
					return P.name()+" is carrying a bomb.\n";
			}
			final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(P);
			if(SK!=null)
			{
				for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
				{
					final Environmental E2=i.next();
					if(E2 instanceof Item)
					{
						if(trapCheck((Item)E2).length()>0)
							return P.name()+" has a bomb in stock.\n";
					}
				}
			}
		}
		return msg.toString();
	}

	public void messageTo(final MOB mob)
	{
		final String here=trapHere(mob,mob.location());
		if(here.length()>0)
			mob.tell(here);
		else
		{
			String last="";
			String dirs="";
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				final Room R=mob.location().getRoomInDir(d);
				final Exit E=mob.location().getExitInDir(d);
				if((R!=null)
						&&(E!=null)
				&&(trapHere(mob,R).length()>0))
				{
					if(last.length()>0)
						dirs+=", "+last;
					last=CMLib.directions().getFromCompassDirectionName(d);
				}
			}
			if((dirs.length()==0)&&(last.length()>0))
				mob.tell(L("You sense a bomb to @x1.",last));
			else
			if((dirs.length()>2)&&(last.length()>0))
				mob.tell(L("You sense a bomb to @x1, and @x2.",dirs.substring(2),last));
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_MOB)
		   &&(affected instanceof MOB)
		   &&(((MOB)affected).location()!=null)
		   &&((lastRoom==null)||(((MOB)affected).location()!=lastRoom)))
		{
			lastRoom=((MOB)affected).location();
			messageTo((MOB)affected);
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			failureTell(mob,target,auto,L("<S-NAME> <S-IS-ARE> already sensing bombs."));
			return false;
		}
		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> gain(s) sensitivity to bombs!"):L("^S<S-NAME> @x1, and gain(s) sensitivity to bombs!^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> @x1, but nothing happens.",prayWord(mob)));

		return success;
	}
}
