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
   Copyright 2001-2018 Bo Zimmerman

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

public class Spell_WizardLock extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_WizardLock";
	}

	private final static String localizedName = CMLib.lang().L("Wizard Lock");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Wizard Locked)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS|CAN_EXITS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS|Ability.CAN_EXITS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ALTERATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(affected==null)
			return true;

		if(!super.okMessage(myHost,msg))
			return false;

		final MOB mob=msg.source();
		if(((!msg.amITarget(affected))&&(msg.tool()!=affected))
		||(msg.source()==invoker())
		||(CMLib.law().doesHavePriviledgesHere(mob,msg.source().location())
			&&(text().toUpperCase().indexOf("MALICIOUS")<0))
		||((msg.target() instanceof Exit)
			&&(CMLib.law().doesHavePriviledgesInThisDirection(mob,msg.source().location(),(Exit)msg.target()))
			&&(text().toUpperCase().indexOf("MALICIOUS")<0)))
				return true;

		switch(msg.targetMinor())
		{
		case CMMsg.TYP_OPEN:
			mob.tell(L("@x1 appears to be magically locked.",affected.name()));
			return false;
		case CMMsg.TYP_UNLOCK:
			mob.tell(L("@x1 appears to be magically locked.",affected.name()));
			return false;
		case CMMsg.TYP_JUSTICE:
		{
			if(!msg.targetMajor(CMMsg.MASK_DELICATE))
				return true;
		}
		//$FALL-THROUGH$
		case CMMsg.TYP_DELICATE_HANDS_ACT:
			mob.tell(L("@x1 appears to be magically protected.",affected.name()));
			return false;
		default:
			break;
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof Exit)
			{
				final Exit exit=(Exit)affected;
				exit.setDoorsNLocks(exit.hasADoor(),!exit.hasADoor(),exit.defaultsClosed(),
									exit.hasALock(),exit.hasALock(),exit.defaultsLocked());
			}
			else
			if(affected instanceof Container)
			{
				final Container container=(Container)affected;
				container.setDoorsNLocks(container.hasADoor(),!container.hasADoor(),container.defaultsClosed(),container.hasALock(),container.hasALock(),container.defaultsLocked());
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell(L("Wizard Lock what?."));
			return false;
		}
		final String targetName=CMParms.combine(commands,0);

		Physical target=null;
		final int dirCode=CMLib.directions().getGoodDirectionCode(targetName);
		if(dirCode>=0)
			target=mob.location().getExitInDir(dirCode);
		if(target==null)
			target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null)
			return false;

		if((!(target instanceof Container))&&(!(target instanceof Exit)))
		{
			mob.tell(L("You can't lock that."));
			return false;
		}

		if(target instanceof Container)
		{
			final Container container=(Container)target;
			if((!container.hasADoor())||(!container.hasALock()))
			{
				mob.tell(L("You can't lock that!"));
				return false;
			}
		}
		else
		if(target instanceof Exit)
		{
			final Exit exit=(Exit)target;
			if(!exit.hasADoor())
			{
				mob.tell(L("You can't lock that!"));
				return false;
			}
		}

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(L("@x1 is already magically locked!",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),auto?"":L("^S<S-NAME> point(s) <S-HIS-HER> finger at <T-NAMESELF>, incanting.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target instanceof Exit)
				{
					final Exit exit=(Exit)target;
					exit.setDoorsNLocks(exit.hasADoor(),false,exit.defaultsClosed(),
										exit.hasALock(),true,exit.defaultsLocked());
					final Room R=mob.location();
					Room R2=null;
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						if(R.getExitInDir(d)==target)
						{
							R2=R.getRoomInDir(d);
							break;
						}
					}
					if((CMLib.law().doesOwnThisLand(mob,R))
					||((R2!=null)&&(CMLib.law().doesOwnThisLand(mob,R2))))
					{
						target.addNonUninvokableEffect((Ability)copyOf());
						CMLib.database().DBUpdateExits(R);
					}
					else
						beneficialAffect(mob,target,asLevel,Ability.TICKS_ALMOST_FOREVER);
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("<T-NAME> look(s) shut tight!"));
				}
				else
				if(target instanceof Container)
				{
					beneficialAffect(mob,target,asLevel,Ability.TICKS_ALMOST_FOREVER);
					final Container container=(Container)target;
					container.setDoorsNLocks(container.hasADoor(),false,container.defaultsClosed(),container.hasALock(),true,container.defaultsLocked());
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("<T-NAME> look(s) shut tight!"));
				}
				final Ability lock=target.fetchEffect(ID());
				if(lock != null)
				{
					lock.setMiscText(Integer.toString(mob.phyStats().level()));
					if(target instanceof Exit)
					{
						final Room R=mob.location();
						if(!CMLib.law().doesHavePriviledgesHere(mob,R))
						{
							for(final Enumeration<Ability> a=R.effects();a.hasMoreElements();)
							{
								final Ability A=a.nextElement();
								if((A instanceof LandTitle)
								   &&(((LandTitle)A).getOwnerName().length()>0))
									lock.setMiscText(lock.text()+" MALICIOUS");
							}
						}
					}
				}
			}

		}
		else
			beneficialVisualFizzle(mob,target,L("<S-NAME> point(s) at <T-NAMESELF>, incanting, but nothing happens."));

		// return whether it worked
		return success;
	}
}
