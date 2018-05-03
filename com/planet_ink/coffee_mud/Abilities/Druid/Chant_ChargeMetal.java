package com.planet_ink.coffee_mud.Abilities.Druid;
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

import java.lang.ref.WeakReference;
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
public class Chant_ChargeMetal extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_ChargeMetal";
	}

	private final static String	localizedName	= CMLib.lang().L("Charge Metal");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Charged)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_ENDURING;
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

	private WeakReference<CMMsg>	lastMsg			= null;

	protected List<Item>			affectedItems	= new Vector<Item>();

	@Override
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		affectedItems=new Vector<Item>();
	}

	@Override
	public CMObject copyOf()
	{
		final Chant_ChargeMetal obj=(Chant_ChargeMetal)super.copyOf();
		obj.affectedItems=new Vector<Item>();
		obj.affectedItems.addAll(affectedItems);
		return obj;
	}

	public Item wieldingMetal(MOB mob)
	{
		for(int i=0;i<mob.numItems();i++)
		{
			final Item item=mob.getItem(i);
			if((item!=null)
			&&(!item.amWearingAt(Wearable.IN_INVENTORY))
			&&(CMLib.flags().isMetal(item))
			&&(item.container()==null)
			&&(!mob.amDead()))
				return item;
		}
		return null;
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

		final Item I=(Item)affected;
		if((I.owner()==null)
		||(!(I.owner() instanceof MOB))
		||(I.amWearingAt(Wearable.IN_INVENTORY)))
			return true;

		final MOB mob=(MOB)I.owner();
		if((!msg.amITarget(mob))
		&&((msg.targetMinor()==CMMsg.TYP_ELECTRIC)
			||((msg.sourceMinor()==CMMsg.TYP_ELECTRIC)&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)))
		&&((lastMsg==null)||(lastMsg.get()!=msg)))
		{
			lastMsg=new WeakReference<CMMsg>(msg);
			msg.source().location().show(mob,null,I,CMMsg.MSG_OK_VISUAL,L("<O-NAME> attracts a charge to <S-NAME>!"));
			if(mob.okMessage(mob, msg))
			{
				msg.modify(msg.source(),
							mob,
							msg.tool(),
							msg.sourceCode(),
							msg.sourceMessage(),
							msg.targetCode(),
							msg.targetMessage(),
							msg.othersCode(),
							msg.othersMessage());
			}
		}
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
					final Item I=affectedItems.get(i);
					Ability A=I.fetchEffect(this.ID());
					for(int x=0;(x<3) && (A!=null); x++)
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
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				final Item I=wieldingMetal((MOB)target);
				if(I==null)
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_ANY);
		if(target==null)
			return false;
		Item I=null;
		if(target instanceof MOB)
			I=wieldingMetal((MOB)target);

		if((target instanceof Item)
		&&(CMLib.flags().isMetal(target)))
			I=(Item)target;
		else
		if(target instanceof Item)
		{
			mob.tell(L("@x1 is not made of metal!",target.name(mob)));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if((success)&&(I!=null))
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> chant(s) upon <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					final int duration = adjustMaliciousTickdownTime(mob,target,adjustedLevel(mob,asLevel),asLevel);
					success=maliciousAffect(mob,I,asLevel,duration,-1)!=null;
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> chant(s) at <T-NAMESELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
