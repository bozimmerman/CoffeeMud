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
public class Prayer_Avatar extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Avatar";
	}

	private final static String	localizedName	= CMLib.lang().L("Avatar");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER | Ability.DOMAIN_COMMUNING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY;
	}

	@Override
	public String displayText()
	{
		if ((invoker() != null) && (invoker().getWorshipCharID().length() > 0))
			return "(You are the AVATAR of " + invoker().getWorshipCharID() + ")";
		return "(You are the AVATAR of the gods)";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
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
			mob.tell(L("Your unholy alliance has been severed."));
	}

	@Override
	public void affectCharState(MOB affectedMOB, CharState affectedState)
	{
		super.affectCharState(affectedMOB,affectedState);
		affectedState.setHitPoints(affectedState.getHitPoints()+200);
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectedStats)
	{
		super.affectPhyStats(affected,affectedStats);
		final int xlvl=2+(int)Math.round(CMath.div(adjustedLevel(invoker(),0),1.5));
		affectedStats.setArmor(affectedStats.armor()-(xlvl));
		affectedStats.setSpeed(affectedStats.speed()+1.0+CMath.mul(0.33,super.getXLEVELLevel(invoker())));
		affectedStats.setAttackAdjustment(affectedStats.attackAdjustment()+(xlvl*2));
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if(mob.getMyDeity()!=null)
				affectedStats.setName(L("@x1, the Avatar of @x2",mob.name(),mob.getMyDeity().name()));
			else
				affectedStats.setName(L("@x1, the Avatar",mob.name()));
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!(affected instanceof MOB))
			return super.tick(ticking,tickID);

		if(!super.tick(ticking,tickID))
			return false;
		final MOB mob=(MOB)affected;
		if(mob.location()!=null)
		{
			if(mob.isInCombat())
			{
				final MOB newvictim=mob.location().fetchRandomInhabitant();
				if(newvictim!=mob)
					mob.setVictim(newvictim);
			}
			else
			{
				MOB attack=null;
				final Room R=mob.location();
				for(int m=0;m<R.numInhabitants();m++)
				{
					final MOB M=R.fetchInhabitant(m);
					if((M!=null)&&(M!=mob)&&(mob.mayPhysicallyAttack(M)))
					{
						attack = M;
						break;
					}
				}
				if(attack==null)
				{
					int dir=-1;
					final Vector<Integer> dirs=new Vector<Integer>();

					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						final Room R2=R.getRoomInDir(d);
						if((R2!=null)
						&&(R.getExitInDir(d)!=null)
						&&(R.getExitInDir(d).isOpen()))
							dirs.addElement(Integer.valueOf(d));
					}
					while(dirs.size()>0)
					{
						final int d=dirs.remove(CMLib.dice().roll(1, dirs.size(), -1)).intValue();
						final Room R2=R.getRoomInDir(d);
						if(R2!=null)
						{
							if((dir<0)||(dir==Directions.UP))
								dir=d;
							for(int m=0;m<R2.numInhabitants();m++)
							{
								final MOB M=R2.fetchInhabitant(m);
								if((M!=null)&&(M!=mob)&&(mob.mayPhysicallyAttack(M)))
								{
									attack = M;
									break;
								}
							}
						}
					}
					if(dir>=0)
					{
						final String godName=mob.getWorshipCharID().length()==0?"Your god":mob.getWorshipCharID();
						mob.tell(L("@x1 directs you @x2.",godName,CMLib.directions().getInDirectionName(dir)));
						CMLib.tracking().walk(mob,dir,false,false);
					}
				}
				if(attack!=null)
					CMLib.combat().postAttack(mob,attack,mob.fetchWieldedItem());
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> already the AVATAR."));
			return false;
		}

		int levels=mob.charStats().getClassLevel("Avatar");
		if(levels<0)
			levels=mob.phyStats().level();
		else
		if(!mob.charStats().getCurrentClass().ID().equals("Avatar"))
		{
			mob.tell(L("You have lost this ability for all time."));
			return false;
		}
		else
			levels=adjustedLevel(mob,asLevel);

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> @x1.^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> become(s) the AVATAR!"));
				beneficialAffect(mob,target,asLevel,levels);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> @x1, but nothing happens.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
