package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2019-2020 Bo Zimmerman

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
public class Spell_MassRepairingAura extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_MassRepairingAura";
	}

	private final static String	localizedName	= CMLib.lang().L("Mass Repairing Aura");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS|CAN_MOBS|CAN_ROOMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_ABJURATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int overrideMana()
	{
		return 100;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Mass Repairing Aura)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	public static final int	REPAIR_MAX		= 30;
	public int				repairDown		= REPAIR_MAX;
	public int				adjustedLevel	= 1;

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_BONUS);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		repairDown-=adjustedLevel;
		if((repairDown<=0))
		{
			repairDown=REPAIR_MAX;
			final List<Item> choices=new ArrayList<Item>();
			if(affected instanceof Item)
				choices.add((Item)affected);
			else
			if(affected instanceof ItemPossessor)
				choices.addAll(this.getChoices((ItemPossessor)affected));
			for(final Item I : choices)
			{
				if((I.subjectToWearAndTear())&&(I.usesRemaining()<100))
				{
					if(I.owner() instanceof Room)
						((Room)I.owner()).showHappens(CMMsg.MSG_OK_VISUAL,I,L("<S-NAME> is magically repairing itself."));
					else
					if(I.owner() instanceof MOB)
						((MOB)I.owner()).tell(L("@x1 is magically repairing itself.",I.name()));
					I.setUsesRemaining(I.usesRemaining()+1+(super.getXLEVELLevel(invoker())/3));
				}
			}
		}
		return true;
	}

	protected List<Item> getChoices(final ItemPossessor owner)
	{
		final List<Item> choices=new ArrayList<Item>();
		final List<Item> inventory=new ArrayList<Item>(owner.numItems());
		Item I=null;
		for(int i=0;i<owner.numItems();i++)
		{
			I=owner.getItem(i);
			if((I!=null)
			&&(I.subjectToWearAndTear())
			&&(I.fetchEffect("Spell_RepairingAura")==null)
			&&(I.fetchEffect("Spell_MassRepairingAura")==null)
			&&(I.fetchEffect(ID())==null))
			{
				if(I.amWearingAt(Wearable.IN_INVENTORY))
					inventory.add(I);
				else
					choices.add(I);
			}
		}
		List<Item> chooseFrom=inventory;
		if(choices.size()<3)
			inventory.addAll(choices);
		else
			chooseFrom=choices;
		return chooseFrom;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_ANY);
		if(target==null)
			return false;
		if((target.fetchEffect(this.ID())!=null)
		||(target.fetchEffect("Spell_RepairingAura")!=null)
		||(target.fetchEffect("Spell_MassRepairingAura")!=null))
		{
			mob.tell(L("@x1 is already repairing!",target.name(mob)));
			return false;
		}
		if((!(target instanceof Item))&&(!(target instanceof MOB))&&(!(target instanceof Room)))
		{
			mob.tell(L("@x1 would not be affected by this spell.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(target instanceof Item)
		{
		}
		else
		if(target instanceof MOB)
		{
			final List<Item> chooseFrom = this.getChoices((MOB)target);
			if(chooseFrom.size()<1)
				success=false;
		}
		else
		if(target instanceof Room)
		{
			final List<Item> chooseFrom = this.getChoices((Room)target);
			if(chooseFrom.size()<1)
				success=false;
		}

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),auto?"":L("^S<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, incanting.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
				mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,L("<T-NAME> attain(s) a massive repairing aura."));
				final Spell_MassRepairingAura A=(Spell_MassRepairingAura)target.fetchEffect(ID());
				if(A!=null)
					A.adjustedLevel=adjustedLevel(mob,asLevel);
				target.recoverPhyStats();
				mob.recoverPhyStats();
				mob.location().recoverRoomStats();
			}
		}
		else
			beneficialVisualFizzle(mob,target,L("<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, incanting, but nothing happens."));

		// return whether it worked
		return success;
	}
}
