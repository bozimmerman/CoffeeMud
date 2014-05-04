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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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

@SuppressWarnings("rawtypes")
public class Prayer_DivineFavor extends Prayer
{
	@Override public String ID() { return "Prayer_DivineFavor"; }
	@Override public String name(){ return "Divine Favor";}
	@Override public String displayText(){ return "(Divine Favor)";}
	@Override public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;}
	@Override public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_SELF;}
	@Override public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	@Override protected int canAffectCode(){return Ability.CAN_MOBS;}
	@Override protected int canTargetCode(){return 0;}
	protected boolean struckDownToday=false;

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell(_("Your fall out of divine favor."));
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if((msg.source()==affected)
		&&(msg.sourceMinor()==CMMsg.TYP_DEATH))
			unInvoke();
		if((msg.source()==affected)
		&&(msg.sourceMinor()==CMMsg.TYP_EXPCHANGE)
		&&(msg.source().getWorshipCharID().length()>0))
		{
			if(msg.value()<0)
				msg.setValue((int)Math.round(CMath.mul(msg.value(),0.9)));
			else
				msg.setValue((int)Math.round(CMath.mul(msg.value(),1.1)));
		}
		return super.okMessage(host,msg);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((affected instanceof MOB)
		&&(((MOB)affected).isInCombat())
		&&(!struckDownToday)
		&&(CMLib.dice().roll(1,1000,0)==1)
		&&(((MOB)affected).getWorshipCharID().length()>0)
		&&(!((MOB)affected).getVictim().getWorshipCharID().equals(((MOB)affected).getWorshipCharID())))
		{
			final MOB deityM=CMLib.map().getDeity(((MOB)affected).getWorshipCharID());
			if(deityM!=null)
			{
				struckDownToday=true;
				((MOB)affected).location().showOthers(deityM,((MOB)affected).getVictim(),null,CMMsg.MSG_OK_ACTION,_("@x1 strike(s) down <T-NAME> with all of <S-HIS-HER> divine fury!",deityM.name()));
				CMLib.combat().postDeath(deityM,((MOB)affected).getVictim(),null);
			}
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Physical target=mob;
		if((auto)&&(givenTarget!=null)) target=givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(mob,target,null,_("<T-NAME> <T-IS-ARE> already affected by @x1.",name()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> become(s) divinely favored.":"^S<S-NAME> "+prayWord(mob)+" for divine favor.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,_("<S-NAME> @x1, but there's no answer.",prayWord(mob)));


		// return whether it worked
		return success;
	}
}
