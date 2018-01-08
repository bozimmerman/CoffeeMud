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
   Copyright 2005-2018 Bo Zimmerman

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

public class Prayer_AuraFear extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_AuraFear";
	}

	private final static String localizedName = CMLib.lang().L("Aura of Fear");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Fear Aura)");

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
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS|Ability.CAN_ROOMS|Ability.CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS|Ability.CAN_ROOMS|Ability.CAN_ITEMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY;
	}

	private int ratingTickDown=4;

	public Prayer_AuraFear()
	{
		super();

		ratingTickDown = 4;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		final Room R=CMLib.map().roomLocation(affected);
		final Environmental E=affected;

		super.unInvoke();

		if((canBeUninvoked())&&(R!=null)&&(E!=null))
			R.showHappens(CMMsg.MSG_OK_VISUAL,L("The fearful aura around @x1 fades.",E.name()));
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(affected==null)
			return super.tick(ticking,tickID);

		if((--ratingTickDown)>=0)
			return super.tick(ticking,tickID);
		ratingTickDown=4;
		final Room R=CMLib.map().roomLocation(affected);
		if(R==null)
			return super.tick(ticking,tickID);

		HashSet<MOB> H=null;
		if((invoker()!=null)&&(invoker().location()==R))
		{
			H=new HashSet<MOB>();
			invoker().getGroupMembers(H);
			H.add(invoker());
		}
		if((affected instanceof MOB)&&(affected!=invoker()))
		{
			if(H==null)
				H=new HashSet<MOB>();
			((MOB)affected).getGroupMembers(H);
			H.add((MOB)affected);
		}
		for(int i=0;i<R.numInhabitants();i++)
		{
			final MOB M=R.fetchInhabitant(i);
			final MOB blame=((invoker!=null)&&(invoker!=M))?invoker:M;
			if((M!=null)&&((H==null)||(!H.contains(M))))
			{
				if(CMLib.dice().rollPercentage()<M.charStats().getSave(CharStats.STAT_SAVE_MIND))
					R.show(M,null,affected,CMMsg.MASK_EYES|CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> shudder(s) at the sight of <O-NAME>."));
				else
				{
					// do that fear thing
					// sit and cringe, or flee if mobile
					if(M.isMonster())
					{
						if((!CMLib.flags().isMobile(M))||(!M.isInCombat()))
						{
							final Command C=CMClass.getCommand("Sit");
							try
							{
								if(C!=null) C.execute(M,new XVector<String>("Sit"),MUDCmdProcessor.METAFLAG_FORCED);
							}
							catch(final Exception e)
							{
							}
							if(CMLib.flags().isSitting(M))
							{
								R.show(M,null,affected,CMMsg.MASK_EYES|CMMsg.MSG_HANDS|CMMsg.MASK_SOUND,L("<S-NAME> cringe(s) in fear at the sight of <O-NAME>."));
								final Ability A=CMClass.getAbility("Spell_Fear");
								if(A!=null)
									A.startTickDown(blame,M,Ability.TICKS_ALMOST_FOREVER);
							}
						}
						else
						if(M.isInCombat())
						{
							R.show(M,null,affected,CMMsg.MASK_EYES|CMMsg.MSG_NOISE,L("<S-NAME> scream(s) in fear at the sight of <O-NAME>."));
							final Command C=CMClass.getCommand("Flee");
							try
							{
								if(C!=null) C.execute(M,new XVector<String>("Flee"),MUDCmdProcessor.METAFLAG_FORCED);
							}
							catch(final Exception e)
							{
							}
						}
						else
						{
							R.show(M,null,affected,CMMsg.MASK_EYES|CMMsg.MSG_NOISE,L("<S-NAME> scream(s) in fear at the sight of <O-NAME>."));
							CMLib.tracking().beMobile(M,false,true,false,false,null,null);
						}
					}
					else
					{
						if(M.isInCombat())
						{
							R.show(M,null,affected,CMMsg.MASK_EYES|CMMsg.MSG_NOISE,L("<S-NAME> scream(s) in fear at the sight of <O-NAME>."));
							final Command C=CMClass.getCommand("Flee");
							try
							{
								if(C!=null) C.execute(M,new XVector<String>("Flee"),MUDCmdProcessor.METAFLAG_FORCED);
							}
							catch(final Exception e)
							{
							}
						}
						else
						{
							R.show(M,null,affected,CMMsg.MASK_EYES|CMMsg.MSG_NOISE,L("<S-NAME> scream(s) in fear at the sight of <O-NAME>."));
							CMLib.tracking().beMobile(M,false,true,false,false,null,null);
							if(M.location()==R)
							{
								R.show(M,null,affected,CMMsg.MASK_EYES|CMMsg.MSG_HANDS|CMMsg.MASK_SOUND,L("<S-NAME> cringe(s) in fear at the sight of <O-NAME>."));
								final Ability A=CMClass.getAbility("Spell_Fear");
								if(A!=null)
									A.startTickDown(blame,M,Ability.TICKS_ALMOST_FOREVER);
							}
						}
					}
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(L("The aura of fear is already surrounding @x1.",target.name(mob)));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			int affectType=verbalCastCode(mob,target,auto);
			if((mob==target)&&(CMath.bset(affectType,CMMsg.MASK_MALICIOUS)))
				affectType=CMath.unsetb(affectType,CMMsg.MASK_MALICIOUS);
			final CMMsg msg=CMClass.getMsg(mob,target,this,affectType,auto?"":L("^S<S-NAME> @x1 for an aura of fear to surround <T-NAMESELF>.^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("An aura descends over <T-NAME>!"));
				maliciousAffect(mob,target,asLevel,0,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> @x1 for an aura of fear, but <S-HIS-HER> plea is not answered.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}

