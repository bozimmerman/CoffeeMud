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
public class Spell_IceSheet extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_IceSheet";
	}

	private final static String	localizedName	= CMLib.lang().L("Ice Sheet");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Ice Sheet spell)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ROOMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_CONJURATION;
	}

	private Item theSheet = null;
	
	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		final Room room=(Room)affected;
		if(canBeUninvoked())
			room.showHappens(CMMsg.MSG_OK_VISUAL, L("The ice sheet melts."));
		if(theSheet != null)
		{
			theSheet.destroy();
			theSheet = null;
		}
		super.unInvoke();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof Room)))
			return false;
		final Room room=(Room)affected;
		if(msg.source().location()==room)
		{
			final MOB mob=msg.source();
			if(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
			{
				if((room.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
				||(room.domainType()==Room.DOMAIN_INDOORS_UNDERWATER))
				{
					mob.tell(L("You are frozen in the ice sheet and can't even blink."));
					return false;
				}
				else
				if(((msg.sourceMajor(CMMsg.MASK_MOVE)))
				&&((theSheet!=null)&&(room.isContent(theSheet))))
				{
					if((!CMLib.flags().isInFlight(mob))
					&&(CMLib.dice().rollPercentage()>((msg.source().charStats().getStat(CharStats.STAT_DEXTERITY)*3)+25)))
					{
						int oldDisposition=mob.basePhyStats().disposition();
						oldDisposition=oldDisposition&(~(PhyStats.IS_SLEEPING|PhyStats.IS_SNEAKING|PhyStats.IS_SITTING|PhyStats.IS_CUSTOM));
						mob.basePhyStats().setDisposition(oldDisposition|PhyStats.IS_SITTING);
						mob.recoverPhyStats();
						room.show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> slip(s) on the ice."));
						return false;
					}
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if((mob != null)&&(target instanceof MOB))
		{
			if(CMLib.flags().isFlying(target))
				return Ability.QUALITY_INDIFFERENT;
			final Set<MOB> grp=mob.getGroupMembers(new HashSet<MOB>());
			grp.remove(mob);
			for(final MOB M : grp)
			{
				if(!CMLib.flags().isFlying(M))
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob, target);
	}
	
	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof Room)))
			return;
		super.executeMsg(myHost,msg);
		if((msg.target()==affected)
		&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE)))
		{
			final MOB mob=msg.source();
			final Room room=(Room)affected;
			msg.addTrailerMsg(CMClass.getMsg(mob,room,null,CMMsg.MSG_OK_VISUAL,L("\n\r<T-NAME> is covered in ice."),null,null));
		}
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		// sleeping for a room disables any special characteristic (as of water)
		if(affected instanceof Room)
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SLEEPING);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final Physical target = mob.location();

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(mob,null,null,L("An Ice Sheet is already here!"));
			return false;
		}

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{

			String msgStr=L("the ground becomes covered in ice!");
			if(CMLib.flags().isWateryRoom(mob.location()))
				msgStr=L("the water freezes over!");
			if(auto)
				msgStr=Character.toUpperCase(msgStr.charAt(0))+msgStr.substring(1);
			final CMMsg msg = CMClass.getMsg(mob, target, this, somanticCastCode(mob,target,auto),L(auto?"":"^S<S-NAME> speak(s) and gesture(s) and ")+msgStr+"^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Spell_IceSheet sheet=(Spell_IceSheet)beneficialAffect(mob,mob.location(),asLevel,0);
				if(sheet != null)
				{
					sheet.theSheet = CMClass.getBasicItem("StdItem");
					sheet.theSheet.setName("an ice sheet");
					sheet.theSheet.setDisplayText("an enormous ice sheet covers the ground here");
					CMLib.flags().setGettable(sheet.theSheet, false);
					mob.location().addItem(sheet.theSheet);
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> speak(s) about the cold, but the spell fizzles."));

		// return whether it worked
		return success;
	}
}
