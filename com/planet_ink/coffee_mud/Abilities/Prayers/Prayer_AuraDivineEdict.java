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
public class Prayer_AuraDivineEdict extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_AuraDivineEdict";
	}

	private final static String localizedName = CMLib.lang().L("Aura of the Divine Edict");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Edict Aura)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_EVANGELISM;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
	}

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
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("The divine edict aura around <S-NAME> fades."));
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
			msg.source().tell(L("@x1 DEMANDS NO FIGHTING!",godName));
			msg.source().makePeace(true);
			return false;
		}
		else
		if((msg.source()==invoker())
		&&(msg.targetMinor()==CMMsg.TYP_SPEAK)
		&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
		&&(msg.target() instanceof MOB)
		&&(((MOB)msg.target()).phyStats().level()<invoker().phyStats().level()+(super.getXLEVELLevel(invoker())*2))
		&&(proficiencyCheck(invoker(),0,false))
		&&(msg.sourceMessage()!=null))
		{
			final String said = CMStrings.getSayFromMessage(msg.sourceMessage());
			if((said!=null)
			&&(CMStrings.isUpperCase(said))
			&&(msg.source().location()!=null))
			{
				final String contextName = msg.source().location().getContextName(msg.target());
				final Vector<String> V=CMParms.parse("ORDER \""+contextName+"\" "+said);
				@SuppressWarnings("unchecked")
				final CMObject O=CMLib.english().findCommand((MOB)msg.target(),(List<String>)V.clone());
				if((!((MOB)msg.target()).isMonster())
				&&(CMClass.classID(O).equalsIgnoreCase("DROP")
				   ||CMClass.classID(O).equalsIgnoreCase("SELL")
				   ||CMClass.classID(O).equalsIgnoreCase("GIVE")))
				{
				   msg.source().tell(L("The divine care not about such orders."));
				   return false;
				}
				noRecurse=true;
				final String oldLiege=((MOB)msg.target()).getLiegeID();
				try
				{
					((MOB)msg.target()).setLiegeID(msg.source().Name());
					msg.source().doCommand(V,MUDCmdProcessor.METAFLAG_FORCED);
				}
				finally
				{
					((MOB)msg.target()).setLiegeID(oldLiege);
					noRecurse=false;
				}
				return false;
			}
		}
		noRecurse=false;
		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected==null)||(!(affected instanceof Room)))
			return super.tick(ticking,tickID);

		if(!super.tick(ticking,tickID))
			return false;
		if(invoker()==null)
			return true;

		final Room R=invoker().location();
		for(int i=0;i<R.numInhabitants();i++)
		{
			final MOB M=R.fetchInhabitant(i);
			if((M!=null)&&(M.isInCombat()))
			{
				M.tell(L("@x1 DEMANDS NO FIGHTING!",invoker().getWorshipCharID().toUpperCase()));
				M.makePeace(true);
			}
		}
		return true;
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
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,L("The aura of the divine edict is already with <S-NAME>."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> @x1 for the aura of the divine edict.^?",prayWord(mob)));
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
			return beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 for an aura of divine edict, but <S-HIS-HER> plea is not answered.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
