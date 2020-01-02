package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2017-2020 Bo Zimmerman

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
public class Shaming extends StdAbility
{
	@Override
	public String ID()
	{
		return "Shaming";
	}

	private final static String	localizedName	= CMLib.lang().L("Shaming");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Public Shaming)");

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
		return Ability.ACODE_PROPERTY;
	}

	protected boolean	global		= false;
	protected Area		shameArea	= null;

	public boolean isShamedRightNow(final MOB mob)
	{
		if(mob == null)
			return false;
		final Room R=mob.location();
		if(R==null)
			return false;
		if(shameArea == null)
		{
			if(this.global)
				return true;
			Area A=CMLib.map().getStartArea(mob);
			final LegalBehavior B=CMLib.law().getLegalBehavior(A);
			if(B!=null)
			{
				final Area A2=CMLib.law().getLegalObject(A);
				if(A2 != null)
					A=A2;
			}
			if(A!=null)
			{
				shameArea=A;
				setMiscText(A.Name());
			}
			else
			{
				global=true;
				setMiscText("GLOBAL");
			}
		}
		if(shameArea.inMyMetroArea(R.getArea()))
			return true;
		if((mob.getStartRoom() != null)
		&& (R.getArea() == mob.getStartRoom().getArea()))
			return true;
		return false;
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		this.shameArea=null;
		this.global=false;
		super.setMiscText(newMiscText);
		if(newMiscText.length()>0)
		{
			if(newMiscText.equalsIgnoreCase("GLOBAL"))
				this.global=true;
			else
			{
				final Area A=CMLib.map().getArea(newMiscText);
				if(A!=null)
				{
					this.shameArea=A;
				}
			}
		}
	}

	public void doShamingThing(final MOB srcM, final MOB tgtM)
	{
		if((srcM==null)||(tgtM==null))
			return;
		final Room sR=srcM.location();
		final Room tR=tgtM.location();
		if((sR != tR)
		|| (sR==null)
		|| (!CMLib.flags().isAliveAwakeMobile(srcM, true)))
			return;
		switch(CMLib.dice().roll(1, 12, 0))
		{
		case 1:
			sR.show(srcM, tgtM, CMMsg.MASK_ALWAYS|CMMsg.MSG_QUIETMOVEMENT,
					L("<S-NAME> avert(s) <S-HIS-HER> gaze from <T-NAME>."));
			break;
		case 2:
			sR.show(srcM, tgtM, CMMsg.MASK_ALWAYS|CMMsg.MSG_QUIETMOVEMENT,
					L("<S-NAME> avoid(s) <T-NAME>."));
			break;
		case 3:
		case 4:
			sR.show(srcM, tgtM, CMMsg.MASK_ALWAYS|CMMsg.MSG_QUIETMOVEMENT,
					L("<S-NAME> ignore(s) <T-NAME>."));
			break;
		case 5:
			sR.show(srcM, tgtM, CMMsg.MASK_ALWAYS|CMMsg.MSG_QUIETMOVEMENT,
					L("<S-NAME> frown(s) at <T-NAME>."));
			break;
		case 6:
			sR.show(srcM, tgtM, CMMsg.MASK_ALWAYS|CMMsg.MSG_QUIETMOVEMENT,
					L("<S-NAME> gaze(s) disapprovingly at <T-NAME>."));
			break;
		case 7:
			sR.show(srcM, tgtM, CMMsg.MASK_ALWAYS|CMMsg.MSG_QUIETMOVEMENT,
					L("<S-NAME> quietly shame(s) <T-NAME>."));
			break;
		case 8:
			sR.show(srcM, tgtM, CMMsg.MASK_ALWAYS|CMMsg.MSG_QUIETMOVEMENT,
					L("<S-NAME> scowl(s) at <T-NAME>."));
			break;
		case 9:
			sR.show(srcM, tgtM, CMMsg.MASK_ALWAYS|CMMsg.MSG_QUIETMOVEMENT,
					L("<S-NAME> quietly glower(s) at <T-NAME>."));
			break;
		case 10:
			sR.show(srcM, tgtM, CMMsg.MASK_ALWAYS|CMMsg.MSG_QUIETMOVEMENT,
					L("<S-NAME> turn(s) <S-HIS-HER> back on <T-NAME>."));
			break;
		case 11:
			sR.show(srcM, tgtM, CMMsg.MASK_ALWAYS|CMMsg.MSG_QUIETMOVEMENT,
					L("<S-NAME> turn(s) away from <T-NAME>."));
			break;
		case 12:
			sR.show(srcM, tgtM, CMMsg.MASK_ALWAYS|CMMsg.MSG_QUIETMOVEMENT,
					L("<S-NAME> avoid(s) eye contact with <T-NAME>."));
			break;
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if(isShamedRightNow(mob)
			&&(!msg.isSource(CMMsg.MASK_ALWAYS)))
			{
				final Room R=mob.location();
				if(msg.amISource(mob))
				{
					switch(msg.sourceMinor())
					{
					case CMMsg.TYP_LIST:
					case CMMsg.TYP_VIEW:
					case CMMsg.TYP_BUY:
					case CMMsg.TYP_SELL:
					case CMMsg.TYP_GIVE:
					case CMMsg.TYP_BID:
					case CMMsg.TYP_DEPOSIT:
					case CMMsg.TYP_WITHDRAW:
					case CMMsg.TYP_BORROW:
						if(msg.target() instanceof MOB)
						{
							doShamingThing((MOB)msg.target(), mob);
							return false;
						}
						break;
					}
				}
				else
				if(msg.amITarget(mob)
				|| (mob.isPlayer() && (R.numPCInhabitants()==1)))
				{
					switch(msg.targetMinor())
					{
					case CMMsg.TYP_SPEAK:
						doShamingThing(msg.source(), mob);
						break;
					case CMMsg.TYP_LIST:
					case CMMsg.TYP_VIEW:
					case CMMsg.TYP_BUY:
					case CMMsg.TYP_SELL:
					case CMMsg.TYP_BID:
					case CMMsg.TYP_GIVE:
					case CMMsg.TYP_DEPOSIT:
					case CMMsg.TYP_WITHDRAW:
					case CMMsg.TYP_BORROW:
						doShamingThing(msg.source(), mob);
						return false;
					}
					if(msg.othersMajor(CMMsg.MASK_CHANNEL))
					{
						doShamingThing(msg.source(), mob);
						msg.source().tell(L("You must not speak to or about @x1.",mob.charStats().himher()));
						return false;
					}
					if(msg.tool() instanceof Social)
					{
						doShamingThing(msg.source(), mob);
						msg.source().tell(L("You must not interact with @x1.",mob.charStats().himher()));
						return false;
					}
				}
				else
				{
					switch(msg.sourceMinor())
					{
					case CMMsg.TYP_SPEAK:
						if((msg.sourceMessage()!=null)
						&&(msg.sourceMessage().length()>0))
						{
							final String say=CMStrings.getSayFromMessage(msg.sourceMessage());
							if((say != null)
							&&(CMStrings.containsWord(say.toLowerCase(), mob.Name().toLowerCase())))
							{
								doShamingThing(msg.source(), mob);
								return false;
							}
						}
					}
					if(msg.othersMajor(CMMsg.MASK_CHANNEL))
					{
						String say=CMStrings.getSayFromMessage(msg.othersMessage());
						if(say==null)
							say=CMStrings.getSayFromMessage(msg.targetMessage());
						if((say!=null)
						&&(CMStrings.containsWord(say.toLowerCase(), mob.Name().toLowerCase())))
						{
							doShamingThing(msg.source(), mob);
							msg.source().tell(L("You must not speak to or about @x1.",mob.charStats().himher()));
							return false;
						}
					}
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell(L("Your period of public shaming has ended."));
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if((commands.size()<1) && (givenTarget == null))
		{
			mob.tell(L("Shame whom?"));
			return false;
		}

		final MOB target = getTarget(mob, commands, givenTarget);
		if (target == null)
			return false;
		if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
			return false;
		final boolean success = proficiencyCheck(mob, 0, auto);
		if (success)
		{
			if (mob.location().show(mob, target, this, CMMsg.TYP_GENERAL, auto ? null : L("<S-NAME> publicly shame(s) <T-NAME>.")))
			{
				final Shaming S=(Shaming)beneficialAffect(mob, target, asLevel, 0);
				if(S!=null)
				{
					Area A=mob.location().getArea();
					final LegalBehavior B=CMLib.law().getLegalBehavior(A);
					if(B!=null)
					{
						final Area A2=CMLib.law().getLegalObject(A);
						if(A2 != null)
							A=A2;
					}
					S.setMiscText(A.Name());
					if(A!=null)
						S.shameArea=A;
				}

			}
		}
		else
		if (!auto)
			return beneficialVisualFizzle(mob, target, L("<S-NAME> attempt(s) to publicly shame <T-NAMESELF>, but fail(s)!"));
		return true;
	}
}
