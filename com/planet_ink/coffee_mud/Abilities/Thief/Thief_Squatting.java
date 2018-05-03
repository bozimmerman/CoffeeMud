package com.planet_ink.coffee_mud.Abilities.Thief;
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
public class Thief_Squatting extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Squatting";
	}

	private final static String	localizedName	= CMLib.lang().L("Squatting");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Squatting)");

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
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SQUAT", "SQUATTING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT | USAGE_MANA;
	}

	protected boolean	failed	= false;
	protected Room		room	= null;
	private LandTitle	title	= null;

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_CRIMINAL;
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			if(((msg.sourceMinor()==CMMsg.TYP_QUIT)
				||(msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
				||((msg.targetMinor()==CMMsg.TYP_EXPIRE)&&(msg.target()==msg.source().location()))
				||(msg.sourceMinor()==CMMsg.TYP_ROOMRESET))
			&&(msg.source().location()!=null))
			{
				failed=true;
				unInvoke();
			}
		}
		return super.okMessage(host,msg);
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if(msg.source()==mob)
			{
				if((msg.target()==mob.location()) && (msg.targetMinor()==CMMsg.TYP_LEAVE))
				{
					failed=true;
					unInvoke();
				}
				else
				if((msg.sourceMinor()==CMMsg.TYP_DEATH)||(msg.sourceMinor()==CMMsg.TYP_QUIT))
				{
					failed=true;
					unInvoke();
				}
			}
			else
			if((CMLib.flags().isStanding(mob))||(mob.location()!=room))
			{
				failed=true;
				unInvoke();
			}
		}
		super.executeMsg(host,msg);
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		super.unInvoke();

		if((canBeUninvoked())&&(mob.location()!=null))
		{
			if((failed)||(!CMLib.flags().isSitting(mob))||(room==null)||(title==null)||(mob.location()!=room))
				mob.tell(L("You are no longer squatting."));
			else
			if(title.getOwnerName().length()>0)
			{
				mob.tell(L("Your squat has succeeded.  This property no longer belongs to @x1.",title.getOwnerName()));
				title.setOwnerName("");
				title.updateTitle();
				title.updateLot(null);
			}
			else
			{
				mob.tell(L("Your squat has succeeded.  This property now belongs to you."));
				title.setOwnerName(mob.Name());
				title.updateTitle();
				title.updateLot(new XVector<String>(mob.name()));
			}
		}
		failed=false;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> already squatting."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(CMLib.law().doesHavePriviledgesHere(mob,mob.location()))
		{
			mob.tell(L("This is your place already!"));
			return false;
		}
		final LandTitle T=CMLib.law().getLandTitle(mob.location());
		boolean confirmed=false;
		for(final Enumeration<Ability> a=mob.location().effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A==T)
				confirmed=true;
		}
		if(T==null)
		{
			mob.tell(L("This property is not available for sale, and cannot be squatted upon."));
			return false;
		}
		MOB warnMOB=null;
		if((T.getOwnerName().length()>0)&&(!T.getOwnerName().startsWith("#")))
		{
			final Clan C=CMLib.clans().getClan(T.getOwnerName());
			if(C==null)
			{
				final MOB M=CMLib.players().getLoadPlayer(T.getOwnerName());
				if(M!=null)
					warnMOB=M;
			}
			else
			{
				for(final Session S : CMLib.sessions().localOnlineIterable())
				{
					if((S.mob()!=null)
					&&(S.mob()!=mob)
					&&(S.mob().getClanRole(C.clanID())!=null))
						warnMOB=S.mob();
				}
			}
			if((warnMOB==null)||(!CMLib.flags().isInTheGame(warnMOB,true)))
			{
				mob.tell(L("The owners must be in the game for you to begin squatting."));
				return false;
			}
		}
		if(!confirmed)
		{
			mob.tell(L("You cannot squat on an area for sale."));
			return false;
		}
		if(!CMLib.flags().isSitting(mob))
		{
			mob.tell(L("You must be sitting!"));
			return false;
		}

		final boolean success=proficiencyCheck(mob,0,auto);

		final CMMsg msg=CMClass.getMsg(mob,null,this,auto?CMMsg.MASK_ALWAYS:CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,auto?"":L("<S-NAME> start(s) squatting."));
		if(!success)
			return beneficialVisualFizzle(mob,null,auto?"":L("<S-NAME> can't seem to get comfortable here."));
		else
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			failed=false;
			room=mob.location();
			title=T;
			beneficialAffect(mob,target,asLevel,(CMProps.getIntVar(CMProps.Int.TICKSPERMUDMONTH)));
			if(warnMOB!=null)
				warnMOB.tell(L("You've heard a rumor that someone is squatting on @x1's property.",T.getOwnerName()));
		}
		return success;
	}
}
