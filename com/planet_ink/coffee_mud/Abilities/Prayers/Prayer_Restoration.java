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

public class Prayer_Restoration extends Prayer implements MendingSkill
{
	@Override
	public String ID()
	{
		return "Prayer_Restoration";
	}

	private final static String localizedName = CMLib.lang().L("Restoration");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_OTHERS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_HEALING;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY|Ability.FLAG_HEALINGMAGIC;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL;
	}

	@Override
	public boolean supportsMending(Physical item)
	{
		if(!(item instanceof MOB))
			return false;

		if(((((MOB)item).curState()).getHitPoints()<(((MOB)item).maxState()).getHitPoints()))
			return true;
		final MOB caster=CMClass.getFactoryMOB();
		caster.basePhyStats().setLevel(CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL));
		caster.phyStats().setLevel(CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL));
		if(
			(item.fetchEffect("Amputation")!=null)
		||(item.fetchEffect("Injury")!=null)
		||(item.fetchEffect("BrokenLimbs")!=null)
		||(item.fetchEffect("Fighter_AtemiStrike")!=null)
		||(item.fetchEffect("Undead_EnergyDrain")!=null)
		||(item.fetchEffect("Undead_WeakEnergyDrain")!=null)
		||(item.fetchEffect("Undead_ColdTouch")!=null)
		||((new Prayer_RestoreSmell().returnOffensiveAffects(caster,item)).size()>0)
		||((new Prayer_RestoreVoice().returnOffensiveAffects(caster,item)).size()>0)
		||((Prayer_RemovePoison.returnOffensiveAffects(item)).size()>0)
		||((new Prayer_Freedom().returnOffensiveAffects(caster,item)).size()>0)
		||((new Prayer_CureBlindness().returnOffensiveAffects(caster,item)).size()>0)
		||((new Prayer_CureDeafness().returnOffensiveAffects(caster,item)).size()>0)
		)
		{
			caster.destroy();
			return true;
		}
		caster.destroy();
		return false;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if(!supportsMending(target))
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> become(s) surrounded by a bright light."):L("^S<S-NAME> @x1 over <T-NAMESELF> for restorative healing.^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final int healing=target.maxState().getHitPoints()-target.curState().getHitPoints();
				if(healing>0)
				{
					CMLib.combat().postHealing(mob,target,this,healing,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,null);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> look(s) much healthier!"));
					target.recoverCharStats();
					target.recoverPhyStats();
					target.recoverMaxState();
				}
				Ability A;
				A=target.fetchEffect("Bleeding");
				if(A!=null)
				{
					target.delEffect(A);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> bleeding stops!"));
					A=target.fetchAbility(A.ID());
					if(A!=null)
						target.delAbility(A);
					target.recoverCharStats();
					target.recoverPhyStats();
					target.recoverMaxState();
				}

				A=target.fetchEffect("Amputation");
				if(A!=null)
				{
					target.delEffect(A);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> missing parts are restored!"));
					A=target.fetchAbility(A.ID());
					if(A!=null)
						target.delAbility(A);
					target.recoverCharStats();
					target.recoverPhyStats();
					target.recoverMaxState();
				}

				A=target.fetchEffect("BrokenLimbs");
				if(A!=null)
				{
					target.delEffect(A);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> broken limbs mend!"));
					A=target.fetchAbility(A.ID());
					if(A!=null)
						target.delAbility(A);
					target.recoverCharStats();
					target.recoverPhyStats();
					target.recoverMaxState();
				}

				A=target.fetchEffect("Injury");
				if(A!=null)
				{
					target.delEffect(A);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> injuries are healed!"));
					A=target.fetchAbility(A.ID());
					if(A!=null)
						target.delAbility(A);
					target.recoverCharStats();
					target.recoverPhyStats();
					target.recoverMaxState();
				}

				A=target.fetchEffect("Fighter_AtemiStrike");
				if((A!=null)&&(A.canBeUninvoked()))
				{
					target.delEffect(A);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> atemi damage is healed!"));
					target.recoverCharStats();
					target.recoverPhyStats();
					target.recoverMaxState();
				}

				A=target.fetchEffect("Undead_EnergyDrain");
				if(A!=null)
				{
					A.unInvoke();
					target.delEffect(A);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> lost levels are restored!"));
					target.recoverCharStats();
					target.recoverPhyStats();
					target.recoverMaxState();
				}
				A=target.fetchEffect("Undead_WeakEnergyDrain");
				if(A!=null)
				{
					A.unInvoke();
					target.delEffect(A);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> lost levels are restored!"));
					target.recoverCharStats();
					target.recoverPhyStats();
					target.recoverMaxState();
				}
				A=target.fetchEffect("Undead_ColdTouch");
				if(A!=null)
				{
					A.unInvoke();
					target.delEffect(A);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> <S-IS-ARE> no longer cold and weak!"));
					target.recoverCharStats();
					target.recoverPhyStats();
					target.recoverMaxState();
				}
				List<Ability> offensiveAffects=new Prayer_RestoreSmell().returnOffensiveAffects(mob,target);
				if(offensiveAffects.size()>0)
				{
					for(int a=offensiveAffects.size()-1;a>=0;a--)
						offensiveAffects.get(a).unInvoke();
					mob.location().showOthers(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> can smell again!"));
					target.recoverCharStats();
					target.recoverPhyStats();
					target.recoverMaxState();
				}
				offensiveAffects=new Prayer_RestoreVoice().returnOffensiveAffects(mob,target);
				if(offensiveAffects.size()>0)
				{
					for(int a=offensiveAffects.size()-1;a>=0;a--)
						offensiveAffects.get(a).unInvoke();
					mob.location().showOthers(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> can speak again!"));
					target.recoverCharStats();
					target.recoverPhyStats();
					target.recoverMaxState();
				}
				offensiveAffects=Prayer_RemovePoison.returnOffensiveAffects(target);
				if(offensiveAffects.size()>0)
				{
					for(int a=offensiveAffects.size()-1;a>=0;a--)
						offensiveAffects.get(a).unInvoke();
					mob.location().showOthers(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> is cured of <S-HIS-HER> poisonous afflication!"));
					target.recoverCharStats();
					target.recoverPhyStats();
					target.recoverMaxState();
				}
				offensiveAffects=new Prayer_Freedom().returnOffensiveAffects(mob,target);
				if(offensiveAffects.size()>0)
				{
					for(int a=offensiveAffects.size()-1;a>=0;a--)
						offensiveAffects.get(a).unInvoke();
					mob.location().showOthers(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> can move again!"));
					target.recoverCharStats();
					target.recoverPhyStats();
					target.recoverMaxState();
				}
				offensiveAffects=new Prayer_CureDisease().returnOffensiveAffects(target);
				if(offensiveAffects.size()>0)
				{
					for(int a=offensiveAffects.size()-1;a>=0;a--)
						offensiveAffects.get(a).unInvoke();
					mob.location().showOthers(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> is cured of <S-HIS-HER> disease!"));
					target.recoverCharStats();
					target.recoverPhyStats();
					target.recoverMaxState();
				}
				offensiveAffects=new Prayer_CureBlindness().returnOffensiveAffects(mob,target);
				if(offensiveAffects.size()>0)
				{
					for(int a=offensiveAffects.size()-1;a>=0;a--)
						offensiveAffects.get(a).unInvoke();
					mob.location().showOthers(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> can see again!"));
					target.recoverCharStats();
					target.recoverPhyStats();
					target.recoverMaxState();
				}
				offensiveAffects=new Prayer_CureDeafness().returnOffensiveAffects(mob,target);
				if(offensiveAffects.size()>0)
				{
					for(int a=offensiveAffects.size()-1;a>=0;a--)
						offensiveAffects.get(a).unInvoke();
					mob.location().showOthers(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> can hear again!"));
					target.recoverCharStats();
					target.recoverPhyStats();
					target.recoverMaxState();
				}
				mob.location().recoverRoomStats();
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 over <T-NAMESELF>, but @x2 does not heed.",prayWord(mob),hisHerDiety(mob)));

		// return whether it worked
		return success;
	}
}
