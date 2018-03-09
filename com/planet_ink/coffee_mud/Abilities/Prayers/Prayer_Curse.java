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
   Copyright 2001-2018 Bo Zimmerman

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

public class Prayer_Curse extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Curse";
	}

	private final static String localizedName = CMLib.lang().L("Curse");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Cursed)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_CURSING;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS|Ability.CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS|Ability.CAN_ITEMS;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected==null)
			return;
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_EVIL);
		final int xlvl=super.getXLEVELLevel(invoker());
		if(affected instanceof MOB)
			affectableStats.setArmor(affectableStats.armor()+5+(2*xlvl));
		else
		if(affected instanceof Item)
			affectableStats.setAbility(affectableStats.ability()-1);
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
		{
			if(canBeUninvoked())
			if((affected instanceof Item)&&(((Item)affected).owner()!=null)&&(((Item)affected).owner() instanceof MOB)&&(super.canBeUninvoked()))
				((MOB)((Item)affected).owner()).tell(L("The curse on @x1 is lifted.",((Item)affected).name()));
			super.unInvoke();
			return;
		}
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell(L("The curse is lifted."));
		super.unInvoke();
	}

	public static Item getSomething(MOB mob, boolean blessedOnly)
	{
		final Vector<Item> good=new Vector<Item>();
		final Vector<Item> great=new Vector<Item>();
		Item target=null;
		for(int i=0;i<mob.numItems();i++)
		{
			final Item I=mob.getItem(i);
			if((!blessedOnly)||(isBlessed(I)))
			{
				if(I.amWearingAt(Wearable.IN_INVENTORY))
					good.addElement(I);
				else
					great.addElement(I);
			}
		}
		if(great.size()>0)
			target=great.elementAt(CMLib.dice().roll(1,great.size(),-1));
		else
		if(good.size()>0)
			target=good.elementAt(CMLib.dice().roll(1,good.size(),-1));
		return target;
	}

	public static void endLowerBlessings(Physical target, int level)
	{
		final List<Ability> V=CMLib.flags().domainAffects(target,Ability.DOMAIN_BLESSING);
		for(int v=0;v<V.size();v++)
		{
			final Ability A=V.get(v);
			if(CMLib.ableMapper().lowestQualifyingLevel(A.ID())<=level)
				A.unInvoke();
		}
	}

	public static boolean isBlessed(Item item)
	{
		return CMLib.flags().domainAffects(item,Ability.DOMAIN_BLESSING).size()>0;
	}

	public static void endLowerCurses(Physical target, int level)
	{
		final List<Ability> V=CMLib.flags().domainAffects(target,Ability.DOMAIN_CURSING);
		for(int v=0;v<V.size();v++)
		{
			final Ability A=V.get(v);
			if(CMLib.ableMapper().lowestQualifyingLevel(A.ID())<level)
				A.unInvoke();
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto)|CMMsg.MASK_MALICIOUS,auto?L("<T-NAME> <T-IS-ARE> cursed!"):L("^S<S-NAME> curse(s) <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					final Item I=getSomething(mob,true);
					if(I!=null)
					{
						endLowerBlessings(I,CMLib.ableMapper().lowestQualifyingLevel(ID()));
						I.recoverPhyStats();
					}
					endLowerBlessings(target,CMLib.ableMapper().lowestQualifyingLevel(ID()));
					success=maliciousAffect(mob,target,asLevel,0,-1)!=null;
					target.recoverPhyStats();
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to curse <T-NAMESELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
