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

public class Spell_RogueLimb extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_RogueLimb";
	}

	private final static String localizedName = CMLib.lang().L("Rogue Limb");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Rogue Limb)");

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
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ENCHANTMENT;
	}

	public MOB rogueLimb=null;

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((rogueLimb!=null)
		&&(affected instanceof MOB))
		{
			if(rogueLimb.location()!=((MOB)affected).location())
			{
				((MOB)affected).location().bringMobHere(rogueLimb,false);
				rogueLimb.setVictim((MOB)affected);
			}
			if((rogueLimb.amFollowing()!=null)
			||(rogueLimb.getVictim()!=affected)
			||(!CMLib.flags().isAliveAwakeMobileUnbound(rogueLimb,true))
			||(!CMLib.flags().isAliveAwakeMobileUnbound((MOB)affected,true))
			||(!CMLib.flags().isInTheGame((MOB)affected,false))
			||(!CMLib.flags().isInTheGame(rogueLimb,false)))
				unInvoke();
		}
		else
			unInvoke();
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((affected==null)
		   ||(!(affected instanceof MOB))
		   ||(rogueLimb==null))
			return true;
		if(msg.amITarget(rogueLimb)
		&&(CMLib.flags().isAliveAwakeMobileUnbound(rogueLimb,true))
		&&(CMLib.flags().isAliveAwakeMobileUnbound((MOB)affected,true))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE))
			CMLib.combat().postDamage(rogueLimb,(MOB)affected,this,msg.value(),CMMsg.MASK_ALWAYS|msg.sourceCode(),Weapon.TYPE_NATURAL,null);
		if(msg.amISource(rogueLimb)
		&&(msg.sourceMinor()==CMMsg.TYP_DEATH))
		{
			unInvoke();
			return false;
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		if((affected!=null)
		&&(affected instanceof MOB))
			((MOB)affected).location().show(((MOB)affected),rogueLimb,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> gain(s) control of <T-NAMESELF>."));
		if((rogueLimb!=null)&&(super.canBeUninvoked()))
		{
			rogueLimb.destroy();
			rogueLimb=null;
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> lose(s) control of <T-HIS-HER> limb!"):L("^S<S-NAME> invoke(s) a powerful spell upon <T-NAMESELF>!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					final Vector<Integer> limbs=new Vector<Integer>();
					final Race theRace=target.charStats().getMyRace();
					for(int i=0;i<Race.BODY_PARTS;i++)
					{
						if((target.charStats().getBodyPart(i)>0)
						&&(i!=Race.BODY_TORSO))
							limbs.addElement(Integer.valueOf(i));
					}
					String limb=null;
					if(limbs.size()==0)
						limb="body part";
					else
						limb=(Race.BODYPARTSTR[limbs.elementAt(CMLib.dice().roll(1,limbs.size(),-1)).intValue()]).toLowerCase();
					rogueLimb=CMClass.getMOB("GenMob");
					rogueLimb.setName(L("@x1's @x2",target.name(),limb));
					rogueLimb.setDisplayText(L("@x1 is misbehaving here.",rogueLimb.name()));
					rogueLimb.basePhyStats().setAttackAdjustment(target.phyStats().attackAdjustment());
					rogueLimb.basePhyStats().setArmor(target.phyStats().armor());
					rogueLimb.baseCharStats().setMyRace(theRace);
					int hp=100;
					if(hp>(target.baseState().getHitPoints()/3))
						hp=(target.baseState().getHitPoints()/3);
					rogueLimb.basePhyStats().setDamage(1);
					rogueLimb.baseState().setHitPoints(100);
					rogueLimb.baseState().setMana(0);
					rogueLimb.baseState().setMovement(100);
					rogueLimb.basePhyStats().setSensesMask(PhyStats.CAN_SEE_DARK);
					rogueLimb.setVictim(target);
					rogueLimb.recoverMaxState();
					rogueLimb.recoverCharStats();
					rogueLimb.recoverPhyStats();
					rogueLimb.resetToMaxState();
					rogueLimb.setStartRoom(null);
					rogueLimb.bringToLife(mob.location(),true);
					CMLib.beanCounter().clearZeroMoney(rogueLimb,null);
					rogueLimb.setMoneyVariation(0);
					rogueLimb.setVictim(target);
					maliciousAffect(mob,target,asLevel,0,-1);
					rogueLimb.setVictim(target);
				}
			}
		}
		else
			mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,L("^S<S-NAME> invoke(s) at <T-NAMESELF>, causing <T-NAME> to twitch, and nothing more."));

		// return whether it worked
		return success;
	}
}
