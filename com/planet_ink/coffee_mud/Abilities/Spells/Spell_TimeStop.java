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
   Copyright 2002-2018 Bo Zimmerman

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

public class Spell_TimeStop extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_TimeStop";
	}

	private final static String localizedName = CMLib.lang().L("Time Stop");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Time is Stopped)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ROOMS|CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	protected int overrideMana()
	{
		return 100;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ALTERATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	protected Vector<MOB> fixed=new Vector<MOB>();

	@Override
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		fixed=new Vector<MOB>();
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected!=null)&&(canBeUninvoked()))
		{
			if(affected instanceof Room)
			{
				final Room room=(Room)affected;
				room.showHappens(CMMsg.MSG_OK_VISUAL, L("Time starts moving again..."));
				if(invoker!=null)
				{
					final Ability me=invoker.fetchEffect(ID());
					if(me!=null)
						me.unInvoke();
				}
				CMLib.threads().resumeTicking(room,-1);
				for(int i=0;i<fixed.size();i++)
				{
					final MOB mob2=fixed.elementAt(i);
					CMLib.threads().resumeTicking(mob2,-1);
				}
				fixed=new Vector<MOB>();
			}
			else
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				CMLib.threads().resumeTicking(mob,-1);
				if(mob.location()!=null)
				{
					mob.location().show(mob, null, CMMsg.MSG_OK_VISUAL, L("Time starts moving again..."));
					final Ability me=mob.location().fetchEffect(ID());
					if(me!=null)
						me.unInvoke();
				}
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)
		   &&(affected instanceof Room))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ENTER:
			case CMMsg.TYP_LEAVE:
			case CMMsg.TYP_FLEE:
				if(msg.source()==invoker)
					msg.source().tell(L("You cannot travel beyond the time stopped area."));
				else
					msg.source().tell(L("Nothing just happened.  You didn't do that."));
				return false;
			default:
				if((msg.source() == invoker)
				&&(msg.target() != invoker)
				&&(msg.target() instanceof MOB)
				&&((CMath.bset(msg.sourceMajor(),CMMsg.MASK_MALICIOUS))
				||(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
				||(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))))
				{
					if(invoker.getVictim()==null)
						invoker.setVictim((MOB)msg.target());
				}
				else
				if((msg.source()!=invoker)
				   &&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
				   &&(!CMath.bset(msg.targetMajor(),CMMsg.MASK_ALWAYS)))
				{
					msg.source().tell(L("Time is stopped. Nothing just happened.  You didn't do that."));
					return false;
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final Physical target = mob.location();

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(mob,null,null,L("Time has already been stopped here!"));
			return false;
		}

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{

			CMMsg msg = CMClass.getMsg(mob, target, this,somanticCastCode(mob,target,auto),L((auto?"T":"^S<S-NAME> speak(s) and gesture(s) and t")+"ime suddenly STOPS!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					final Room room=mob.location();
					fixed=new Vector<MOB>();
					final Set<MOB> grpMembers = mob.getGroupMembers(new HashSet<MOB>());
					for(int m=0;m<room.numInhabitants();m++)
					{
						final MOB mob2=room.fetchInhabitant(m);
						if((mob2!=mob)&&(mob.mayIFight(mob2)))
						{
							msg=CMClass.getMsg(mob,mob2,this,CMMsg.MASK_MALICIOUS|CMMsg.TYP_MIND,null);
							if(!grpMembers.contains(mob2))
							{
								if(room.okMessage(mob, msg))
								{
									room.send(mob, msg);
									if(msg.value()>0)
										return false;
								}
								else
									return beneficialVisualFizzle(mob,null,L("<S-NAME> incant(s) for awhile, but the spell fizzles."));
							}
						}
					}
					CMLib.threads().suspendTicking(room,-1);
					for(int m=0;m<room.numInhabitants();m++)
					{
						final MOB mob2=room.fetchInhabitant(m);
						if(mob2!=mob)
						{
							fixed.addElement(mob2);
							CMLib.threads().suspendTicking(mob2,-1);
						}
					}
					beneficialAffect(mob,room,asLevel,2);
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> incant(s) for awhile, but the spell fizzles."));

		// return whether it worked
		return success;
	}
}
