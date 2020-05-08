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
   Copyright 2020-2020 Bo Zimmerman

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
public class Spell_PlanarBanish extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_PlanarBanish";
	}

	private final static String localizedName = CMLib.lang().L("Planar Banish");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_COSMOLOGY;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_MOVING;
	}

	@Override
	public void unInvoke()
	{
		super.unInvoke();
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if((CMLib.flags().getPlaneOfExistence(mob) == null)
			||(!(target instanceof MOB))
			||(CMLib.flags().getPlaneOfExistence(((MOB)target).getStartRoom()) != null))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		final Area areaA=R.getArea();
		if((CMLib.flags().getPlaneOfExistence(mob) == null)
		||(!(areaA instanceof SubArea)))
		{
			mob.tell(L("This magic would not work here."));
			return false;
		}
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		if(CMLib.flags().getPlaneOfExistence(target.getStartRoom()) != null)
		{
			mob.tell(L("This magic would not work on the @x1.",target.name()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			if(R.show(mob,null,this,somanticCastCode(mob,null,auto),L(auto?"":"^S<S-NAME> flail(s) <S-HIS-HER> arms(s) around.^?")))
			{
				final Set<MOB> set=target.getGroupMembers(new HashSet<MOB>());
				for (final Object element : set)
				{
					final MOB targetM=(MOB)element;

					final CMMsg msg=CMClass.getMsg(mob,targetM,this,somanticCastCode(mob,targetM,auto),null);
					if((R==targetM.location())
					&&(R.okMessage(mob,msg))
					&&(mob.mayIFight(targetM))
					&&((CMLib.flags().getPlaneOfExistence(targetM.getStartRoom()) == null)))
					{
						R.send(mob,msg);
						if(msg.value()<=0)
						{
							if(targetM.isInCombat())
								CMLib.commands().postFlee(targetM,"");
							R.show(targetM,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> vanish(es)!"));
							targetM.curState().setMovement(targetM.curState().getMovement()/2);
							final Ability A=CMClass.getAbility("Chant_EelShock");
							if(A!=null)
								A.startTickDown(mob, targetM, 3);
							targetM.recoverPhyStats();
							final Area supArea = ((SubArea)areaA).getSuperArea();
							for(int i=0;i<20;i++)
							{
								final Room backR=supArea.getRandomProperRoom();
								if(CMLib.flags().canAccess(targetM, backR))
								{
									backR.bringMobHere(targetM, false);
									if(!targetM.isMonster())
										CMLib.commands().postLook(targetM, true);
								}
							}
						}
					}
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> flail(s) <S-HIS-HER> arms at <T-NAME>, but nothing happens."));

		return success;
	}
}
