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

import java.util.*;

/*
   Copyright 2023-2023 Bo Zimmerman

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
public class Skill_RopeTricks extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_RopeTricks";
	}

	private final static String localizedName = CMLib.lang().L("Rope Tricks");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Doing Rope Tricks)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"ROPETRICKS"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_BINDING;
	}

	Vector<MOB> mobsHitUp=new Vector<MOB>();
	int tickTock=0;

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		super.executeMsg(host,msg);
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if((msg.source()==mob)
			&&(msg.target()==mob.location())
			&&(msg.targetMinor()==CMMsg.TYP_LEAVE))
				unInvoke();
			else
			if((CMLib.flags().isStanding(mob))||(CMLib.flags().isSleeping(mob)))
				unInvoke();
			else
			if((msg.amITarget(mob))&&(msg.targetMinor()==CMMsg.TYP_GIVE))
				msg.addTrailerMsg(CMClass.getMsg(mob,msg.source(),CMMsg.MSG_SPEAK,L("^T<S-NAME> say(s) 'Thank you!' to <T-NAME> ^?")));
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if(affected instanceof MOB)
		{
			tickTock++;
			if(tickTock<2)
				return true;
			tickTock=0;
			final MOB mob=(MOB)affected;
			final Room R = mob.location();
			if(R==null)
				return false;
			if((!CMLib.flags().isStanding(mob))
			||(!CMLib.flags().isAliveAwakeMobileUnbound(mob, true))
			||(mob.isInCombat()))
			{
				unInvoke();
				return false;
			}
			final Item ropeI;
			if(CMLib.flags().isARope(mob.fetchWieldedItem()))
				ropeI=mob.fetchWieldedItem();
			else
			if(CMLib.flags().isARope(mob.fetchHeldItem()))
				ropeI=mob.fetchHeldItem();
			else
			{
				unInvoke();
				return false;
			}
			for(int i=0;i<R.numInhabitants();i++)
			{
				final MOB mob2=R.fetchInhabitant(i);
				if((mob2!=null)
				&&(CMLib.flags().canBeSeenBy(mob2,mob))
				&&(mob2!=mob)
				&&(!mobsHitUp.contains(mob2))
				&&(proficiencyCheck(mob,0,false)))
				{
					switch(CMLib.dice().roll(1,10,0))
					{
					case 1:
						R.show(mob, null, ropeI, CMMsg.MSG_NOISYMOVEMENT,
								L("<S-NAME> whirl(s) <S-HIS-HER> <O-NAME> above <S-HIS-HER> head."));
						break;
					case 2:
						R.show(mob, null, ropeI, CMMsg.MSG_NOISYMOVEMENT,
								L("<S-NAME> whirl(s) <S-HIS-HER> <O-NAME> above <random audience member-POSS> head."));
						break;
					case 3:
						R.show(mob, null, ropeI, CMMsg.MSG_NOISYMOVEMENT,
								L("<S-NAME> whirl(s) <S-HIS-HER> <O-NAME> around <S-HIS-HER> body."));
						break;
					case 4:
						R.show(mob, null, ropeI, CMMsg.MSG_NOISYMOVEMENT,
								L("<S-NAME> whirl(s) <S-HIS-HER> <O-NAME> around <random audience member-POSS> body."));
						break;
					case 5:
						R.show(mob, null, ropeI, CMMsg.MSG_NOISYMOVEMENT,
								L("<S-NAME> dance(s) with <S-HIS-HER> his <O-NAME>."));
						break;
					case 6:
						R.show(mob, null, ropeI, CMMsg.MSG_NOISYMOVEMENT,
								L("<S-NAME> toss(es) <S-HIS-HER> his <O-NAME> in the air and catch(es) it."));
						break;
					case 7:
						R.show(mob, null, ropeI, CMMsg.MSG_NOISYMOVEMENT,
								L("<S-NAME> use(s) <S-HIS-HER> his <O-NAME> to grab a small item."));
						break;
					case 8:
						R.show(mob, null, ropeI, CMMsg.MSG_NOISYMOVEMENT,
								L("<S-NAME> use(s) <S-HIS-HER> his <O-NAME> to form a circle in the air."));
						break;
					case 9:
						R.show(mob, null, ropeI, CMMsg.MSG_NOISYMOVEMENT,
								L("<S-NAME> use(s) <S-HIS-HER> his <O-NAME> to form a circle on the ground."));
						break;
					case 10:
						R.show(mob, null, ropeI, CMMsg.MSG_NOISYMOVEMENT,
								L("<S-NAME> do(es) <O-NAME> jig."));
						break;
					}
					if(CMLib.dice().rollPercentage()>(mob2.charStats().getSave(CharStats.STAT_SAVE_JUSTICE)+(CMLib.flags().isGood(mob)?10:0)))
					{
						double total=CMLib.beanCounter().getTotalAbsoluteNativeValue(mob2);
						if(total>1.0)
						{
							total=total/(20.0-getXLEVELLevel(mob));
							if(total<1.0)
								total=1.0;
							final Coins C=CMLib.beanCounter().makeBestCurrency(mob2,total);
							if(C!=null)
							{
								CMLib.beanCounter().subtractMoney(mob2,total);
								mob2.addItem(C);
								mob2.doCommand(CMParms.parse("GIVE \""+C.name()+"\" \""+mob.Name()+"\""),MUDCmdProcessor.METAFLAG_FORCED);
								if(!C.amDestroyed())
									C.putCoinsBack();
							}
						}
					}

					mobsHitUp.addElement(mob2);
					break;
				}
			}
			if((mobsHitUp.size()>0)&&(CMLib.dice().rollPercentage()<5))
				mobsHitUp.removeElementAt(0);
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		super.unInvoke();

		if((canBeUninvoked())&&(mob.location()!=null))
			mob.tell(L("You stop doing rope tricks."));
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(ID())!=null)
		{
			failureTell(mob,target,auto,L("<S-NAME> <S-IS-ARE> already doing rope tricks!"));
			return false;
		}

		if(!CMLib.flags().isStanding(mob))
		{
			mob.tell(L("You must be standing!"));
			return false;
		}
		if(!CMLib.flags().isACityRoom(mob))
		{
			mob.tell(L("You must be on a city street to panhandle."));
			return false;
		}

		final Item ropeI;
		if(CMLib.flags().isARope(mob.fetchWieldedItem()))
			ropeI=mob.fetchWieldedItem();
		else
		if(CMLib.flags().isARope(mob.fetchHeldItem()))
			ropeI=mob.fetchHeldItem();
		else
		{
			mob.tell(L("You aren't wielding or holding a rope or lasso!"));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		final CMMsg msg=CMClass.getMsg(mob,ropeI,this,auto?CMMsg.MASK_ALWAYS:CMMsg.MSG_NOISYMOVEMENT,CMMsg.MSG_NOISYMOVEMENT,CMMsg.MSG_NOISYMOVEMENT,
				auto?"":L("<S-NAME> start(s) doing rope tricks with <T-NAME>."));
		if(!success)
			return beneficialVisualFizzle(mob,null,auto?"":L("<S-NAME> can't seem to get <S-HIS-HER> rope tricks started."));
		else
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,asLevel,0);
		}
		return success;
	}
}
