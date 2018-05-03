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
public class Chant_FurCoat extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_FurCoat";
	}

	private final static String	localizedName	= CMLib.lang().L("Fur Coat");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Fur Coat)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_SHAPE_SHIFTING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	Item theArmor=null;

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
		if(theArmor!=null)
		{
			theArmor.destroy();
			mob.location().recoverRoomStats();
		}
		super.unInvoke();
		if(canBeUninvoked())
		{
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> fur coat vanishes."));
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(affected instanceof MOB)
		{
			if((msg.amISource((MOB)affected))||msg.amISource(invoker))
			{
				if(msg.sourceMinor()==CMMsg.TYP_QUIT)
				{
					unInvoke();
					if(msg.source().playerStats()!=null)
						msg.source().playerStats().setLastUpdated(0);
				}
				else
				if(msg.sourceMinor()==CMMsg.TYP_DEATH)
				{
					unInvoke();
				}
			}
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(theArmor==null)
			return true;

		if((msg.source()==theArmor.owner())
		&&(msg.tool() instanceof Druid_ShapeShift))
		{
			unInvoke();
			return true;
		}

		if((theArmor.amWearingAt(Wearable.IN_INVENTORY)
		||(theArmor.owner()==null)
		||(theArmor.owner() instanceof Room)))
			unInvoke();

		final MOB mob=msg.source();
		if(!msg.amITarget(theArmor))
			return true;
		else
		if((msg.targetMinor()==CMMsg.TYP_REMOVE)
		||(msg.targetMinor()==CMMsg.TYP_GET))
		{
			mob.tell(L("The fur coat cannot be removed from where it is."));
			return false;
		}
		return true;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if(Druid_ShapeShift.isShapeShifted((MOB)target))
					return Ability.QUALITY_INDIFFERENT;
				if(((MOB)target).freeWearPositions(Wearable.WORN_TORSO,(short)-2048,(short)0)<=0)
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> already <S-HAS-HAVE> a fur coat."));
			return false;
		}

		if(Druid_ShapeShift.isShapeShifted(target))
		{
			final Race R=target.charStats().getMyRace();
			boolean exception = false;
			for(final RawMaterial m : R.myResources())
			{
				if(m.material() == RawMaterial.RESOURCE_FUR)
					exception = true;
			}
			if(!exception)
			{
				mob.tell(L("You cannot invoke this chant in your present form."));
				return false;
			}
		}

		if((target.freeWearPositions(Wearable.WORN_TORSO,(short)-2048,(short)0)<=0)
		&&(target.getWearPositions(Wearable.WORN_TORSO))>0)
		{
			mob.tell(L("You are already wearing something on your torso!"));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("A thick coat of fur appears on <T-NAME>."):L("^S<S-NAME> chant(s) for a thick coat of fur!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				theArmor=CMClass.getArmor("GenArmor");
				theArmor.setName(L("a fur coat"));
				theArmor.setDisplayText("");
				theArmor.setDescription(L("The coat is made of thick black fur."));
				theArmor.setMaterial(RawMaterial.RESOURCE_FUR);
				theArmor.basePhyStats().setArmor(2*CMLib.ableMapper().qualifyingClassLevel(mob,this));
				final long[] wearCodes = new long[] 
				{
					Wearable.WORN_TORSO, 
					Wearable.WORN_ARMS,
					Wearable.WORN_FEET,
					Wearable.WORN_WAIST,
					Wearable.WORN_LEGS 
				};
				long wornCode = 0;
				for(final long code : wearCodes)
				{
					for(int d=0; d<Race.BODY_WEARVECTOR.length;d++)
					{
						if(Race.BODY_WEARVECTOR[d]==code)
						{
							if(target.charStats().getBodyPart(d)>0)
								wornCode |= code;
							break;
						}
					}
				}
				theArmor.setRawProperLocationBitmap(wornCode);
				theArmor.setRawLogicalAnd(true);
				for(int i=target.numItems()-1;i>=0;i--)
				{
					final Item I=mob.getItem(i);
					if((I.rawWornCode()&wornCode)>0)
						I.unWear();
				}
				final Ability A=CMClass.getAbility("Prop_WearResister");
				if( A != null )
				{
					A.setMiscText("cold");
					theArmor.addNonUninvokableEffect(A);
				}
				theArmor.recoverPhyStats();
				theArmor.text();
				target.addItem(theArmor);
				theArmor.wearAt(wornCode);
				success=beneficialAffect(mob,target,asLevel,0)!=null;
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) for a thick coat of fur, but nothing happen(s)."));

		// return whether it worked
		return success;
	}
}
