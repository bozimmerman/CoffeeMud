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
   Copyright 2023-2025 Bo Zimmerman

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
public class Spell_StoreAromas extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_StoreAromas";
	}

	private final static String localizedName = CMLib.lang().L("Store Aromas");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Store Aromas)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS|CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS|CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ALTERATION;
	}

	protected Set<String> aromas = new TreeSet<String>();

	@Override
	public void unInvoke()
	{
		final Physical affected = canBeUninvoked()?this.affected:null;
		super.unInvoke();
		// undo the affects of this spell
		if(affected!=null)
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if((mob.location()!=null)
				&&(!mob.amDead()))
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> regain(s) <S-HIS-HER> normal scent."));
			}
			else
			if(affected instanceof Item)
			{
				final Item item=(Item)affected;
				if(item.owner()!=null)
				{
					if(item.owner() instanceof Room)
					{
						if(aromas.size()==0)
							((Room)item.owner()).showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 regains its normal scent.",item.name()));
						else
						{
							final MOB M = CMClass.getFactoryMOB(item.name(),1,(Room)item.owner());
							try
							{
								for(final String str : aromas)
									((Room)item.owner()).show(M,null,null,CMMsg.TYP_AROMA,str);
							}
							finally
							{
								M.destroy();
							}
						}
					}
					else
					if((item.owner() instanceof MOB)
					&&(((CMLib.flags()).canSmell((MOB)item.owner()))))
					{
						if(aromas.size()==0)
							((MOB)item.owner()).tell(L("@x1 regains its normal scent.",item.name()));
						else
						{
							final MOB M = CMClass.getFactoryMOB(item.name(),1,(Room)item.owner());
							try
							{
								for(final String str : aromas)
									((MOB)item.owner()).tell(M,null,null,str);
							}
							finally
							{
								M.destroy();
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
	}

	@Override
	public void affectPhyStats(final Physical host, final PhyStats affectedStats)
	{
		super.affectPhyStats(host,affectedStats);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(((msg.source()==affected)
			||((!(affected instanceof MOB))&&(msg.source().Name().equals(affected.Name()))))
		&&(msg.sourceMinor()==CMMsg.TYP_AROMA)
		&&(msg.othersMessage()!=null)
		&&(msg.othersMessage().length()>0))
		{
			if(aromas.size()<(Math.max(10,adjustedLevel(invoker(),0)/2)))
				aromas.add(msg.othersMessage());
			return false;
		}
		if((msg.sourceMinor()==CMMsg.TYP_SNIFF)
		&&(msg.target()==affected))
		{
			msg.setSourceCode(msg.sourceMajor()|CMMsg.TYP_OK_VISUAL);
			if(msg.targetMinor()==CMMsg.TYP_SNIFF)
				msg.setTargetCode(msg.targetMajor()|CMMsg.TYP_OK_VISUAL);
			if(msg.othersMinor()==CMMsg.TYP_SNIFF)
				msg.setOthersCode(msg.othersMajor()|CMMsg.TYP_OK_VISUAL);
		}
		return super.okMessage(myHost, msg);
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if((mob!=null)
		&&(target != null)
		&&(mob != target))
		{
			if(!mob.getGroupMembers(new HashSet<MOB>()).contains(target)
			||(mob.mayIFight((MOB)target)))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if((success)&&((target instanceof MOB)||(target instanceof Item)))
		{
			int castCode = somaticCastCode(mob,target,auto);
			if((mob!=target)
			&&(target instanceof MOB)
			&&(!mob.getGroupMembers(new HashSet<MOB>()).contains(target)))
				castCode |= CMMsg.MASK_MALICIOUS;
			final CMMsg msg=CMClass.getMsg(mob,target,this,castCode,auto?L("<T-NAME> seem(s) less smelly."):L("^S<S-NAME> cast(s) a spell on <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
					beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> attempt(s) to cast a spell, but fail(s)."));

		return success;
	}
}
