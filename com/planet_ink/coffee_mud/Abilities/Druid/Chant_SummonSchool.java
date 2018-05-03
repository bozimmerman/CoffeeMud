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
   Copyright 2016-2018 Bo Zimmerman

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

public class Chant_SummonSchool extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_SummonSchool";
	}

	private final static String	localizedName	= CMLib.lang().L("Summon School");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Summon School)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
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

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_ANIMALAFFINITY;
	}

	private final Random	randomizer		= new Random(System.currentTimeMillis());
	protected int			numberOfImages	= 0;
	protected boolean		notAgain		= false;
	protected String		schoolMemberName= "";

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;

		if((msg.amITarget(mob))
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(mob!=msg.source()))
		{
			if(numberOfImages <= 0)
			{
				unInvoke();
				return true;
			}
			int intAdjustment = (msg.source().charStats().getMaxStat(CharStats.STAT_INTELLIGENCE) 
									- mob.charStats().getStat(CharStats.STAT_INTELLIGENCE))/2;
			if(intAdjustment < 1)
				intAdjustment = 1;

			final int numberOfTargets = numberOfImages + intAdjustment;
			if(randomizer.nextInt() % numberOfTargets >= intAdjustment)
			{
				if(mob.location().show(mob,msg.source(),CMMsg.MSG_NOISYMOVEMENT,L("<T-NAME> attack(s) a member of <S-YOUPOSS> school, making <T-HIM-HER> swim away!")))
					numberOfImages--;
				return false;
			}
		}
		if(numberOfImages<=0)
			unInvoke();
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if(!(affected instanceof MOB))
			return;

		if(notAgain)
			return;

		final MOB mob=(MOB)affected;
		Room R=mob.location();
		if(R==null)
			return;
		if(!CMLib.flags().isUnderWateryRoom(R))
		{
			unInvoke();
		}
		
		if(msg.amISource(mob))
		{
			if((
				(CMath.bset(msg.othersMajor(),CMMsg.MASK_EYES))
				||(CMath.bset(msg.othersMajor(),CMMsg.MASK_MOVE))
				||(CMath.bset(msg.othersMajor(),CMMsg.MASK_MOUTH))
				||(CMath.bset(msg.othersMajor(),CMMsg.MASK_HANDS)))
			&&(msg.othersMessage()!=null)
			&&(msg.targetMinor()!=CMMsg.TYP_DAMAGE)
			&&(msg.othersMessage().length()>0))
			{
				notAgain=true;
				if(numberOfImages<=0)
					unInvoke();
				else
				{
					for(int x=0;x<numberOfImages;x++)
						msg.addTrailerMsg(CMClass.getMsg(mob,msg.target(),msg.tool(),CMMsg.MSG_OK_VISUAL,msg.othersMessage()));
				}
			}
		}
		else
		if((msg.amITarget(mob.location())&&(!msg.amISource(mob))&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE)))
		&&((CMLib.flags().canBeSeenBy(mob,msg.source()))&&(mob.displayText(msg.source()).length()>0)))
		{
			final StringBuffer Say=new StringBuffer("");
			final boolean compress=msg.source().isAttributeSet(MOB.Attrib.COMPRESS);
			for(int i=0;i<numberOfImages;i++)
			{
				Say.append("^M");
				if(compress)
					Say.append(CMLib.flags().getDispositionBlurbs(mob,mob)+"^M ");
				if(mob.displayText(msg.source()).length()>0)
					Say.append(CMStrings.endWithAPeriod(CMStrings.capitalizeFirstLetter(mob.displayText(msg.source()))));
				else
					Say.append(CMStrings.endWithAPeriod(CMStrings.capitalizeFirstLetter(mob.name())));
				if(!compress)
					Say.append(CMLib.flags().getDispositionBlurbs(mob,msg.source())+"^N\n\r");
				else
					Say.append("^N");
			}
			if(Say.toString().length()>0)
			{
				final CMMsg msg2=CMClass.getMsg(msg.source(),null,this,CMMsg.MSG_OK_VISUAL,Say.toString(),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
				msg.addTrailerMsg(msg2);
			}
		}
		notAgain=false;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
			numberOfImages=0;
		super.unInvoke();

		if(canBeUninvoked() && (mob.location()!=null))
			mob.location().show(mob,null,null,CMMsg.MSG_OK_ACTION, (L("<S-YOUPOSS> school swims away.")));
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> already <S-HAS-HAVE> a school here."));
			return false;
		}

		if(!Druid_ShapeShift.isShapeShifted(mob))
		{
			mob.tell(L("You must be in your animal form to call a mate."));
			return false;
		}
		
		Room R=mob.location();
		if(R==null)
			return false;
		if(!CMLib.flags().isUnderWateryRoom(R))
		{
			mob.tell(L("You can only summon a school underwater."));
			return false;
		}
		
		String raceName = mob.charStats().getMyRace().name();
		String raceCat = mob.charStats().getMyRace().racialCategory();
		if((raceCat.equalsIgnoreCase("Fish"))||(raceCat.equalsIgnoreCase("Sea Mammal")))
		{
			// yay
		}
		else
		{
			mob.tell(L("You are not in the proper form to call on a school here."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			if(raceName.equalsIgnoreCase("ClownFish"))
				numberOfImages = CMLib.dice().roll(1,adjustedLevel(mob,asLevel),100);
			else
				numberOfImages = CMLib.dice().roll(1,(int)(Math.round(CMath.div(adjustedLevel(mob,asLevel),3.0))),2);
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),L((auto?"":"^S<S-NAME> chant(s) to the waters, and ")+"suddenly @x1 @x2(s) arrive, forming up as a school around <S-NAME>.^?",""+numberOfImages,raceName.toLowerCase()));
			if(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<5)
				mob.charStats().setStat(CharStats.STAT_INTELLIGENCE, 5);
			if(mob.location().okMessage(mob,msg))
			{
				mob.recoverCharStats();
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
			else
				mob.recoverCharStats();
		}
		else
		{
			numberOfImages = 0;
			return beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) with great force, but nothing more happens."));
		}
		// return whether it worked
		return success;
	}
}
