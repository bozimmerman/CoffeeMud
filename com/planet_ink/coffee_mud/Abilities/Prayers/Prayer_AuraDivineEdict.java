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

@SuppressWarnings({"unchecked","rawtypes"})
public class Prayer_AuraDivineEdict extends Prayer
{
	@Override public String ID() { return "Prayer_AuraDivineEdict"; }
	@Override public String name(){ return "Aura of the Divine Edict";}
	@Override public String displayText(){ return "(Edict Aura)";}
	@Override protected int canAffectCode(){return Ability.CAN_MOBS;}
	@Override protected int canTargetCode(){return 0;}
	@Override public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_EVANGELISM;}
	@Override public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_SELF;}
	protected int overridemana(){return Ability.COST_ALL;}
	@Override public long flags(){return Ability.FLAG_HOLY;}
	protected String godName="the gods";
	protected boolean noRecurse=false;


	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();

		if((canBeUninvoked())&&(mob.location()!=null)&&(!mob.amDead()))
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,_("The divine edict aura around <S-NAME> fades."));
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected==null)||(!(affected instanceof MOB))||(noRecurse))
			return true;

		if(CMath.bset(msg.sourceMajor(),CMMsg.MASK_MALICIOUS)
		   ||CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
		{
			msg.source().tell(_("@x1 DEMANDS NO FIGHTING!",godName));
			msg.source().makePeace();
			return false;
		}
		else
		if((msg.source()==invoker())
		&&(msg.targetMinor()==CMMsg.TYP_SPEAK)
		&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
		&&(msg.target() instanceof MOB)
		&&(((MOB)msg.target()).phyStats().level()<invoker().phyStats().level()+(super.getXLEVELLevel(invoker())*2))
		&&(msg.sourceMessage()!=null)
		&&(CMStrings.getSayFromMessage(msg.sourceMessage().toUpperCase()).equals(CMStrings.getSayFromMessage(msg.sourceMessage()))))
		{
			final Vector<String> V=CMParms.parse("ORDER \""+msg.target().Name()+"\" "+CMStrings.getSayFromMessage(msg.sourceMessage()));
			final CMObject O=CMLib.english().findCommand((MOB)msg.target(),(List)V.clone());
			if((!((MOB)msg.target()).isMonster())
			&&(CMClass.classID(O).equalsIgnoreCase("DROP")
			   ||CMClass.classID(O).equalsIgnoreCase("SELL")
			   ||CMClass.classID(O).equalsIgnoreCase("GIVE")))
			{
			   msg.source().tell(_("The divine care not about such orders."));
			   return false;
			}
			noRecurse=true;
			final String oldLiege=((MOB)msg.target()).getLiegeID();
			((MOB)msg.target()).setLiegeID(msg.source().Name());
			msg.source().doCommand(V,Command.METAFLAG_FORCED);
			((MOB)msg.target()).setLiegeID(oldLiege);
			noRecurse=false;
			return false;
		}
		noRecurse=false;
		return true;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof Room)))
			return super.tick(ticking,tickID);

		if(!super.tick(ticking,tickID))
			return false;
		if(invoker()==null) return true;

		final Room R=invoker().location();
		for(int i=0;i<R.numInhabitants();i++)
		{
			final MOB M=R.fetchInhabitant(i);
			if((M!=null)&&(M.isInCombat()))
			{
				M.tell(_("@x1 DEMANDS NO FIGHTING!",invoker().getWorshipCharID().toUpperCase()));
				M.makePeace();
			}
		}
		return true;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,_("The aura of the divine edict is already with <S-NAME>."));
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
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for the aura of the divine edict.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				godName="THE GODS";
				if(mob.getWorshipCharID().length()>0)
					godName=mob.getWorshipCharID().toUpperCase();
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for an aura of divine edict, but <S-HIS-HER> plea is not answered.");


		// return whether it worked
		return success;
	}
}
