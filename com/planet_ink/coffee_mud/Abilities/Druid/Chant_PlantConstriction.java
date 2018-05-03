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
   Copyright 2004-2018 Bo Zimmerman

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

public class Chant_PlantConstriction extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_PlantConstriction";
	}

	private final static String localizedName = CMLib.lang().L("Plant Constriction");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTCONTROL;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Plant Constriction)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(10);
	}

	@Override
	public int minRange()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public boolean bubbleAffect()
	{
		return true;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public void unInvoke()
	{
		Item I=null;
		if(affected instanceof Item)
			I=(Item)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(I!=null)&&(I.owner() instanceof MOB)
		&&(!I.amWearingAt(Wearable.IN_INVENTORY)))
		{
			final MOB mob=(MOB)I.owner();
			if((!mob.amDead())
			&&(CMLib.flags().isInTheGame(mob,false)))
			{
				mob.tell(L("@x1 loosens its grip on you and falls off.",I.name(mob)));
				I.setRawWornCode(0);
				mob.location().moveItemTo(I,ItemPossessor.Expire.Player_Drop);
			}
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		Item I=null;
		if(affected instanceof Item)
			I=(Item)affected;
		if((canBeUninvoked())&&(I!=null)&&(I.owner() instanceof MOB)
		&&(I.amWearingAt(Wearable.WORN_LEGS)||I.amWearingAt(Wearable.WORN_ARMS)))
		{
			final MOB mob=(MOB)I.owner();
			if((!mob.amDead())
			&&(mob.isMonster())
			&&(CMLib.flags().isInTheGame(mob,false)))
				CMLib.commands().postRemove(mob,I,false);
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(!super.okMessage(host,msg))
			return false;
		if((msg.targetMinor()==CMMsg.TYP_REMOVE)
		&&(msg.target()==affected)
		&&(affected instanceof Item)
		&&(((Item)affected).amWearingAt(Wearable.WORN_LEGS)||((Item)affected).amWearingAt(Wearable.WORN_ARMS)))
		{
			if(CMLib.dice().rollPercentage()>(msg.source().charStats().getStat(CharStats.STAT_STRENGTH)*4))
			{
				msg.source().location().show(msg.source(),affected,CMMsg.MSG_OK_VISUAL,L("<S-NAME> struggle(s) to remove <T-NAME> and fail(s)."));
				return false;
			}
		}
		return true;
	}

	@Override
	public void affectPhyStats(Physical aff, PhyStats affectableStats)
	{
		if((aff instanceof MOB)&&(affected instanceof Item)
		&&(((MOB)aff).isMine(affected))
		&&((Item)affected).amWearingAt(Wearable.WORN_ARMS))
			affectableStats.setSpeed(affectableStats.speed()/2.0);
	}

	@Override
	public void affectCharState(MOB aff, CharState affectableState)
	{
		if((affected instanceof Item)
		&&(aff.isMine(affected))
		&&((Item)affected).amWearingAt(Wearable.WORN_LEGS))
			affectableState.setMovement(affectableState.getMovement()/2);
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			final Item myPlant=Druid_MyPlants.myPlant(mob.location(),mob,0);
			if(myPlant==null)
				return Ability.QUALITY_INDIFFERENT;
			if(target instanceof MOB)
			{
				final Vector<Long> positionChoices=new Vector<Long>();
				if(((MOB)target).getWearPositions(Wearable.WORN_ARMS)>0)
					positionChoices.addElement(Long.valueOf(Wearable.WORN_ARMS));
				if(((MOB)target).getWearPositions(Wearable.WORN_LEGS)>0)
					positionChoices.addElement(Long.valueOf(Wearable.WORN_LEGS));
				if(positionChoices.size()==0)
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		Item myPlant=Druid_MyPlants.myPlant(mob.location(),mob,0);
		if(myPlant==null)
		{
			if(auto)
				myPlant=new Chant_SummonPlants().buildPlant(mob,mob.location());
			else
			{
				mob.tell(L("There doesn't appear to be any of your plants here to choke with."));
				return false;
			}
		}
		final Vector<Long> positionChoices=new Vector<Long>();
		if(target.getWearPositions(Wearable.WORN_ARMS)>0)
			positionChoices.addElement(Long.valueOf(Wearable.WORN_ARMS));
		if(target.getWearPositions(Wearable.WORN_LEGS)>0)
			positionChoices.addElement(Long.valueOf(Wearable.WORN_LEGS));
		if(positionChoices.size()==0)
		{
			if(!auto)
				mob.tell(L("Ummm, @x1 doesn't have arms or legs to constrict...",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{

			final CMMsg msg = CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> chant(s) at <T-NAME> while pointing at @x1!^?",myPlant.name()));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.moveItemTo(myPlant);
				final Long II=positionChoices.elementAt(CMLib.dice().roll(1,positionChoices.size(),-1));
				myPlant.setRawWornCode(II.longValue());
				if(II.longValue()==Wearable.WORN_ARMS)
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("@x1 jumps up and wraps itself around <S-YOUPOSS> arms!",myPlant.name()));
				else
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("@x1 jumps up and wraps itself around <S-YOUPOSS> legs!",myPlant.name()));
				beneficialAffect(mob,myPlant,asLevel,20);
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> chant(s) at <T-NAME>, but the magic fizzles."));

		// return whether it worked
		return success;
	}
}
