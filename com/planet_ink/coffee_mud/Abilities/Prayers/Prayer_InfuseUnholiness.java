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
   Copyright 2004-2020 Bo Zimmerman

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
public class Prayer_InfuseUnholiness extends Prayer implements Deity.DeityWorshipper
{
	@Override
	public String ID()
	{
		return "Prayer_InfuseUnholiness";
	}

	private final static String localizedName = CMLib.lang().L("Infuse Unholiness");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Infused Unholiness)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_EVANGELISM;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_ROOMS|Ability.CAN_EXITS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_ROOMS|Ability.CAN_EXITS;
	}

	protected int serviceRunning=0;

	@Override
	public int abilityCode()
	{
		return serviceRunning;
	}

	@Override
	public void setAbilityCode(final int newCode)
	{
		serviceRunning=newCode;
	}

	protected volatile String deityName=null;

	@Override
	public String getWorshipCharID()
	{
		return text();
	}

	@Override
	public void setWorshipCharID(final String newVal)
	{
		setMiscText((newVal == null)?"":newVal);
	}

	@Override
	public void setDeityName(final String newDeityName)
	{
		deityName=newDeityName;
	}

	@Override
	public String deityName()
	{
		if(deityName!=null)
			return deityName;
		return getWorshipCharID();
	}

	@Override
	public Deity getMyDeity()
	{
		if (text().length() == 0)
			return null;
		return CMLib.map().getDeity(text());
	}

	@Override
	public void finalize() throws Throwable
	{
		final Physical affected=this.affected;
		if((affected instanceof Places)&&(text().length()>0))
			CMLib.map().deregisterHolyPlace(text(),(Places)affected);
		this.affected=null;
		super.finalize();
	}

	protected void alterHolyPlaceRegistration(final Physical affected)
	{
		if(text().length()>0)
		{
			if((affected == null)&&(this.affected instanceof Places))
				CMLib.map().deregisterHolyPlace(text(),(Places)this.affected);
			else
			if(affected instanceof Places)
				CMLib.map().registerHolyPlace(text(),(Places)affected);
		}
	}

	@Override
	public void setAffectedOne(final Physical affected)
	{
		alterHolyPlaceRegistration(affected);
		super.setAffectedOne(affected);
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		alterHolyPlaceRegistration(this.affected);
	}


	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_EVIL);
		if(CMath.bset(affectableStats.disposition(),PhyStats.IS_GOOD))
			affectableStats.setDisposition(affectableStats.disposition()-PhyStats.IS_GOOD);
		affectableStats.addAmbiance("#EVIL");
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null))
			return;
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
				((MOB)affected).tell(L("Your infused unholiness fades."));
		}

		super.unInvoke();

	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(serviceRunning==0)
			return super.okMessage(myHost, msg);
		if(((msg.targetMajor() & CMMsg.MASK_MALICIOUS)==CMMsg.MASK_MALICIOUS)
		&&(msg.target() instanceof MOB))
		{
			if(msg.source().charStats().getWorshipCharID().equalsIgnoreCase(((MOB)msg.target()).charStats().getWorshipCharID()))
			{
				msg.source().tell(L("Not right now -- you're in a service."));
				msg.source().makePeace(true);
				((MOB)msg.target()).makePeace(true);
				return false;
			}
		}
		if((msg.sourceMinor() == CMMsg.TYP_LEAVE)&&(msg.source().isMonster()))
		{
			msg.source().tell(L("Not right now -- you're in a service."));
			return false;
		}
		return super.okMessage(myHost, msg);
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		Physical target;
		if((givenTarget == null)
		&&(CMParms.combine(commands,0).equalsIgnoreCase("room")||CMParms.combine(commands,0).equalsIgnoreCase("here")))
			target=mob.location();
		else
			target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_ANY);
		if(target==null)
			return false;

		String deityName=null;
		if(CMLib.law().getClericInfusion(target)!=null)
		{

			if(target instanceof Room)
				deityName=CMLib.law().getClericInfused(target);
			if(deityName!=null)
				mob.tell(L("There is already an infused aura of @x1 around @x2.",deityName,target.name(mob)));
			else
				mob.tell(L("There is already an infused aura around @x1.",target.name(mob)));
			return false;
		}

		deityName=mob.baseCharStats().getWorshipCharID();
		if(target instanceof Room)
		{
			if(deityName.length()==0)
			{
				mob.tell(L("The faithless may not infuse unholiness in a room."));
				return false;
			}
			final Area A=mob.location().getArea();
			Room R=null;
			for(final Enumeration<Room> e=A.getMetroMap();e.hasMoreElements();)
			{
				R=e.nextElement();
				if(deityName.equalsIgnoreCase(CMLib.law().getClericInfused(target)))
				{
					mob.tell(L("There is already an unholy place of @x1 in this area at @x2.",deityName,R.displayText(mob)));
					return false;
				}
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),
					auto?L("An unholy aura appears around <T-NAME>."):L("^S<S-NAME> @x1 to infuse an unholy aura around <T-NAMESELF>.^?",prayForWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if((deityName!=null)&&(deityName.length()>0))
					setMiscText(deityName);
				if((target instanceof Room)
				&&(CMLib.law().doesOwnThisLand(mob,((Room)target))))
				{
					target.addNonUninvokableEffect((Ability)this.copyOf());
					CMLib.database().DBUpdateRoom((Room)target);
				}
				else
					beneficialAffect(mob,target,asLevel,0);
				target.recoverPhyStats();
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 to infuse an unholy aura in <T-NAMESELF>, but fail(s).",prayForWord(mob)));

		return success;
	}
}
