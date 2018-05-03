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
   Copyright 2003-2018 Bo Zimmerman

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
public class Prayer_CurseMetal extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_CurseMetal";
	}

	private final static String	localizedName	= CMLib.lang().L("Curse Metal");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Cursed)");

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
	protected int canAffectCode()
	{
		return CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS | CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER | Ability.DOMAIN_CURSING;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY | Ability.FLAG_HEATING;
	}

	protected Vector<Item>	affectedItems	= new Vector<Item>();

	@Override
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		affectedItems=new Vector<Item>();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(affected==null)
			return true;
		if(!(affected instanceof Item))
			return true;
		if(!msg.amITarget(affected))
			return true;

		if(msg.targetMajor(CMMsg.MASK_HANDS))
		{
			msg.source().tell(L("@x1 is filled with unholy heat!",affected.name()));
			return false;
		}
		return true;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID!=Tickable.TICKID_MOB)
			return true;
		if(!(affected instanceof MOB))
			return true;
		if(invoker==null)
			return true;

		final MOB mob=(MOB)affected;

		for(int i=0;i<mob.numItems();i++)
		{
			final Item item=mob.getItem(i);
			if((item!=null)
			&&(!item.amWearingAt(Wearable.IN_INVENTORY))
			&&(CMLib.flags().isMetal(item))
			&&(item.container()==null)
			&&(!mob.amDead()))
			{
				final MOB invoker=(invoker()!=null) ? invoker() : mob;
				final int damage=CMLib.dice().roll(1,6+super.getXLEVELLevel(invoker())+(2*super.getX1Level(invoker())),1);
				CMLib.combat().postDamage(invoker,mob,this,damage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BURSTING,item.name()+" <DAMAGE> <T-NAME>!");
				if(CMLib.dice().rollPercentage()<mob.charStats().getStat(CharStats.STAT_STRENGTH))
				{
					CMLib.commands().postDrop(mob,item,false,false,false);
					if(!mob.isMine(item))
					{
						item.addEffect((Ability)this.copyOf());
						affectedItems.addElement(item);
						break;
					}
				}
			}
		}
		if((!mob.isInCombat())&&(mob.isMonster())&&(mob!=invoker)&&(invoker!=null)&&(mob.location()==invoker.location())&&(mob.location().isInhabitant(invoker))&&(CMLib.flags().canBeSeenBy(invoker,mob)))
			CMLib.combat().postAttack(mob,invoker,mob.fetchWieldedItem());
		return true;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
		{
			super.unInvoke();
			return;
		}

		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				for(int i=0;i<affectedItems.size();i++)
				{
					final Item I=affectedItems.elementAt(i);
					Ability A=I.fetchEffect(this.ID());
					for(int x=0;(x<3) && (A!=null);x++)
					{
						I.delEffect(A);
						A=I.fetchEffect(this.ID());
					}
				}
			}
		}
		super.unInvoke();
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
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> @x1 to curse <T-NAMESELF>.^?",prayForWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					final int duration = adjustMaliciousTickdownTime(mob,target,adjustedLevel(mob,asLevel),asLevel);
					success=maliciousAffect(mob,target,asLevel,duration,-1)!=null;
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> @x1 to curse <T-NAMESELF>, but the spell fizzles.",prayForWord(mob)));

		// return whether it worked
		return success;
	}
}
