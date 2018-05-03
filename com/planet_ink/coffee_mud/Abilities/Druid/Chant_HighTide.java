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

public class Chant_HighTide extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_HighTide";
	}

	private final static String	localizedName	= CMLib.lang().L("High Tide");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Slipping Around)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_WATERCONTROL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(10);
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

	protected boolean	oncePerRd	= false;

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		oncePerRd = false;
		return super.tick(ticking, tickID);
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SITTING);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return super.okMessage(myHost,msg);
		final MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(msg.sourceMinor()==CMMsg.TYP_STAND)
		&&(mob.location()!=null))
		{
			if(!oncePerRd)
			{
				oncePerRd=true;
				mob.location().show(mob,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> attempt(s) to stand up, and slips!"));
			}
			return false;
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			if((mob.location()!=null)&&(!mob.amDead()))
			{
				final CMMsg msg=CMClass.getMsg(mob,null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> regain(s) <S-HIS-HER> feet as the tide recedes."));
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					CMLib.commands().postStand(mob,true);
				}
			}
			else
				mob.tell(L("The water under your feet recedes."));
		}
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				final Set<MOB> h=properTargets(mob,target,false);
				if(h==null)
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	public int getWaterRoomDir(Room mobR)
	{
		for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
		{
			if((d!=Directions.UP)&&(d!=Directions.DOWN))
			{
				final Room R=mobR.getRoomInDir(d);
				final Exit E=mobR.getExitInDir(d);
				if((R!=null)&&(E!=null)&&(E.isOpen()))
				{
					if(CMLib.flags().isWateryRoom(R))
					{
						return d;
					}
					final Room R2=R.getRoomInDir(d);
					final Exit E2=R.getExitInDir(d);
					if((R2!=null)&&(E2!=null)&&(E2.isOpen()) && (CMLib.flags().isWateryRoom(R2)))
					{
						return d;
					}
				}
			}
		}
		return -1;
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room mobR=mob.location();
		if(mobR==null)
			return false;
		if(CMLib.flags().isWateryRoom(mobR))
		{
			mob.tell(L("This chant won't be noticeable ON the water."));
			return false;
		}
		int waterDir = -1;
		if(mobR.getArea() instanceof BoardableShip)
		{
			if((mobR.domainType()&Room.INDOORS)==0)
			{
				Item I=((BoardableShip)mobR.getArea()).getShipItem();
				if((I!=null)&&(I.owner() instanceof Room))
				{
					Room R=(Room)I.owner();
					if(CMLib.flags().isWateryRoom(R))
						waterDir = CMLib.dice().roll(1, 4, -1);
					else
						waterDir = getWaterRoomDir(R);
				}
			}
		}
		else
			waterDir = getWaterRoomDir(mobR);
		
		if(waterDir < 0)
		{
			mob.tell(L("There is no shore nearby to call in the tide from."));
			return false;
		}
		
		final Set<MOB> h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell(L("There doesn't appear to be anyone here worth sending the tide at."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			if(mob.location().show(mob,null,this,verbalCastCode(mob,null,auto),
					L(auto?"The tide rushes in from @x1":
						"^S<S-NAME> chant(s) thunderously as the tide rushes in tide rushes in from @x1.^?",CMLib.directions().getFromCompassDirectionName(waterDir))+CMLib.protocol().msp("earthquake.wav",40)))
			{
				for (final Object element : h)
				{
					final MOB target=(MOB)element;

					final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),null);
					if(CMLib.flags().isInFlight(target))
						mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> seem(s) unaffected."));
					else
					if((mob.location().okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
					{
						mob.location().send(mob,msg);
						if(msg.value()<=0)
						{
							if(target.charStats().getBodyPart(Race.BODY_LEG)>0)
							{
								success=maliciousAffect(mob,target,asLevel,3,-1)!=null;
								if(success)
								{
									if(target.location()==mob.location())
										CMLib.combat().postDamage(mob,target,this,20,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,-1,L("The water rushes underneath <T-YOUPOSS> feet cause(s) <T-HIM-HER> to fall!"));
								}
							}
							else
								mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> seem(s) unaffected by the rushing water."));
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,L("<S-NAME> chant(s) thunderously, but nothing happens."));

		// return whether it worked
		return success;
	}
}
