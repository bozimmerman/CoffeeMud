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

public class Spell_Timeport extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Timeport";
	}

	private final static String	localizedName	= CMLib.lang().L("Timeport");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Time Travelling)");

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
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_ALTERATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	protected final static int mask=
			PhyStats.CAN_NOT_TASTE
			|PhyStats.CAN_NOT_SMELL
			|PhyStats.CAN_NOT_SEE
			|PhyStats.CAN_NOT_HEAR;
	protected final static int mask2=Integer.MAX_VALUE
			-PhyStats.CAN_SEE_BONUS
			-PhyStats.CAN_SEE_DARK
			-PhyStats.CAN_SEE_EVIL
			-PhyStats.CAN_SEE_GOOD
			-PhyStats.CAN_SEE_HIDDEN
			-PhyStats.CAN_SEE_HIDDEN_ITEMS
			-PhyStats.CAN_SEE_INFRARED
			-PhyStats.CAN_SEE_INVISIBLE
			-PhyStats.CAN_SEE_METAL
			-PhyStats.CAN_SEE_SNEAKERS
			-PhyStats.CAN_SEE_VICTIM;

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setSensesMask(mask&mask2);
		affectableStats.setDisposition(PhyStats.IS_NOT_SEEN);
		affectableStats.setDisposition(PhyStats.IS_CLOAKED);
		affectableStats.setDisposition(PhyStats.IS_INVISIBLE);
		affectableStats.setDisposition(PhyStats.IS_HIDDEN);
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		MOB mob=null;
		Room room=null;
		if((affected!=null)&&(canBeUninvoked())&&(affected instanceof MOB))
		{
			mob=(MOB)affected;
			room=mob.location();
			CMLib.threads().resumeTicking(mob,-1);
		}
		super.unInvoke();
		if((room!=null)&&(super.canBeUninvoked()))
			room.show(mob, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> reappear(s)!"));
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			if(!canBeUninvoked())
			{
				msg.source().tell(L("The timeport spell on you fizzles away."));
				affected.delEffect(this);
			}
			else
			if((((msg.sourceMinor()==CMMsg.TYP_QUIT)&&(msg.source()==affected))
				||(msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
				||((msg.targetMinor()==CMMsg.TYP_EXPIRE))
				||(msg.sourceMinor()==CMMsg.TYP_ROOMRESET)))
			{
				unInvoke();
			}
			else
			if(msg.amISource((MOB)affected))
				if((!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
				&&(!CMath.bset(msg.targetMajor(),CMMsg.MASK_ALWAYS)))
				{
					msg.source().tell(L("Nothing just happened.  You are time travelling, and can't do that."));
					return false;
				}
		}
		return super.okMessage(myHost,msg);
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

			final CMMsg msg = CMClass.getMsg(mob, target, this,somanticCastCode(mob,target,auto),L(auto?"":"^S<S-NAME> speak(s) and gesture(s)")+"!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Room room=mob.location();
				target.makePeace(true);
				for(int i=0;i<room.numInhabitants();i++)
				{
					final MOB M=room.fetchInhabitant(i);
					if((M!=null)&&(M.getVictim()==target))
						M.makePeace(true);
				}
				mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> vanish(es)!"));
				CMLib.threads().suspendTicking(target,-1);
				beneficialAffect(mob,target,asLevel,3);
				final Ability A=target.fetchEffect(ID());
				if(A!=null)
					CMLib.threads().startTickDown(A,Tickable.TICKID_MOB,1);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> incant(s) for awhile, but the spell fizzles."));

		// return whether it worked
		return success;
	}
}
