package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2014-2018 Bo Zimmerman

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

public class Spell_ChanneledMissiles extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_ChanneledMissiles";
	}

	private final static String	localizedName	= CMLib.lang().L("Channeled Missiles");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Channeling Missile spell)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(1);
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_CONJURATION;
	}

	protected Runnable	channelingClass	= null;

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return;

		final MOB mob=(MOB)affected;

		if(msg.amISource(mob)
		&&(abilityCode()==0)
		&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
		&&(msg.sourceMajor()>0)
		&&(msg.othersMinor()!=CMMsg.TYP_LOOK)
		&&(msg.othersMinor()!=CMMsg.TYP_EXAMINE)
		&&(msg.othersMajor()>0)
		&&(msg.tool()!=this)
		&&((msg.othersMajor(CMMsg.MASK_SOUND)&&msg.othersMajor(CMMsg.MASK_MOUTH))
			||msg.othersMajor(CMMsg.MASK_HANDS)
			||msg.othersMajor(CMMsg.MASK_MOVE))
		&&((!(msg.tool() instanceof Ability))||(((Ability)msg.tool()).isNowAnAutoEffect())))
		{
			unInvoke();
			mob.recoverPhyStats();
		}
		super.executeMsg(myHost, msg);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affecting()==null)||(!(affecting() instanceof MOB))||(this.channelingClass==null))
			return false;
		final MOB mob=(MOB)affecting();
		if(!super.tick(ticking,tickID))
			return false;
		if(mob.getVictim()==null)
		{
			unInvoke();
			mob.recoverPhyStats();
		}
		else
			this.channelingClass.run();
		return true;
	}

	@Override
	public void affectPhyStats(Physical affectedEnv, PhyStats affectableStats)
	{
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_AUTO_ATTACK);
		super.affectPhyStats(affectedEnv, affectableStats);
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		final MOB mob=(affected instanceof MOB)?((MOB)affected):null;

		super.unInvoke();

		if(mob==null)
			return;

		if((canBeUninvoked()&&(!mob.amDead())))
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> stop(s) channeling missiles."));
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB[] target=new MOB[]{this.getTarget(mob,commands,givenTarget)};
		if(target[0]==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final int numMissiles=((int)Math.round(Math.floor(CMath.div(adjustedLevel(mob,asLevel),10)))+1);
			final Room R=target[0].location();
			if(R!=null)
			{
				final CMMsg msg=CMClass.getMsg(mob,target[0],this,somanticCastCode(mob,target[0],auto),null);
				if(R.okMessage(mob,msg))
				{
					R.send(mob,msg);
					beneficialAffect(mob, mob, asLevel, CHAIN_LENGTH);
					final Spell_ChanneledMissiles thisSpellA = (Spell_ChanneledMissiles)mob.fetchEffect(ID());
					if(thisSpellA!=null)
					{
						thisSpellA.channelingClass = new Runnable()
						{

							@Override
							public void run()
							{
								for(int i=0;(i<numMissiles) && (target[0].location()==R);i++)
								{
									final CMMsg msg=CMClass.getMsg(mob,target[0],thisSpellA,somanticCastCode(mob,target[0],auto),(i==0)?L((auto?"Magic missiles appear hurling full speed at <T-NAME>!":"^S<S-NAME> channel(s) magic missiles toward(s) <T-NAMESELF>!^?")+CMLib.protocol().msp("spelldam2.wav",40)):null);
									if((mob.location()!=null)&&(mob.location().okMessage(mob,msg)))
									{
										mob.location().send(mob,msg);
										if(msg.value()<=0)
										{
											final int damage = CMLib.dice().roll(1,11,11);
											if(target[0].location()==mob.location())
												CMLib.combat().postDamage(mob,target[0],thisSpellA,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,Weapon.TYPE_BURSTING,L((i==0)?"^SThe missile ":"^SAnother missile ")+"<DAMAGE> <T-NAME>!^?");
										}
									}
									if((target[0]==null)||(target[0].amDead())||(target[0].location()!=R))
									{
										target[0]=thisSpellA.getTarget(mob,commands,givenTarget,true,false);
										if(target[0]==null)
											break;
										if(target[0].amDead())
											break;
									}
								}
								if((target[0]==null)||(target[0].amDead())||(target[0].location()!=R))
								{
									thisSpellA.channelingClass=null;
									thisSpellA.unInvoke();
									mob.recoverPhyStats();
								}
								else
								{
									if(mob.getVictim()==null)
										mob.setVictim(target[0]);
									if(target[0].getVictim()==null)
										target[0].setVictim(mob);
								}
	
							}
						};
						thisSpellA.channelingClass.run();
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target[0],L("<S-NAME> point(s) at <T-NAMESELF>, but fizzle(s) the spell."));

		// return whether it worked
		return success;
	}
}
