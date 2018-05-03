package com.planet_ink.coffee_mud.Abilities.Songs;
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
   Copyright 2014-2018 Bo Zimmerman

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

public class Skill_Monologue extends BardSkill
{
	@Override
	public String ID()
	{
		return "Skill_Monologue";
	}

	private final static String localizedName = CMLib.lang().L("Monologue");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return L("(Listening to "+(invoker()==null?"an actor":invoker().name())+"'s monologue)");
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
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"MONOLOGUE"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_THEATRE;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	private volatile long lastTimeHeard = 0;

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((affected instanceof MOB)&&(affected != invoker()))
		{
			if(CMLib.flags().canBeHeardSpeakingBy(invoker(), (MOB)affected))
			{
				affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_MOVE);
				affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_SPEAK);
			}
		}
	}

	@Override
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if((invoker()!=null)
		&&(msg.targetMinor()==CMMsg.TYP_ENTER) 
		&&(msg.target() == invoker().location())
		&&(invoker().mayIFight(msg.source())))
			addMonologue(invoker(),msg.source(),-1,false);
		if((msg.source() == invoker())
		&&(msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(msg.sourceMessage()!=null)
		&&(msg.sourceMessage().length()>0)
		&&(affected instanceof MOB)
		&&(CMLib.flags().canBeHeardSpeakingBy(msg.source(), (MOB)affected)))
			this.lastTimeHeard=System.currentTimeMillis();
	}

	@Override
	public void unInvoke()
	{
		final Physical affected=this.affected;
		super.unInvoke();
		if(affected != null)
		{
			CMLib.threads().resumeTicking(affected, -1);
			if(affected instanceof MOB)
				((MOB)affected).tell(L("The monologue has ended."));
			affected.delEffect(this);
			CMLib.threads().deleteTick(this, Tickable.TICKID_MOB);
		}
	}
	
	protected void addMonologue(MOB mob, MOB target, int asLevel, boolean auto)
	{
		if(CMLib.flags().canBeHeardSpeakingBy(mob, target))
		{
			final CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MASK_MOVE|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),null);
			if(mob.location().okMessage(mob,msg2))
			{
				mob.location().send(mob,msg2);
				if((msg2.value()<=0)&&(target.fetchEffect(ID())==null))
				{
					Skill_Monologue A=(Skill_Monologue)beneficialAffect(mob,target,asLevel,Ability.TICKS_FOREVER);
					CMLib.threads().startTickDown(A, Tickable.TICKID_MOB, 1);
					A.lastTimeHeard=System.currentTimeMillis();
					CMLib.threads().suspendTicking(target,-1);
					target.location().show(target, mob, CMMsg.MSG_OK_VISUAL, L("<S-NAME> patiently waits for the <T-YOUPOSS> monologue to be completed."));
				}
			}
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if((!CMLib.flags().canBeHeardSpeakingBy(invoker(), mob))
			||((invoker()!=null)&&(mob.location()!=invoker().location()))||(!CMLib.flags().isInTheGame(invoker(),true))
			||((System.currentTimeMillis()-lastTimeHeard)>(4000+(super.getXLEVELLevel(invoker())*1000))))
			{
				unInvoke();
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!CMLib.flags().canSpeak(mob))
		{
			mob.tell(L("You can't speak!"));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final Set<MOB> h=new HashSet<MOB>();
		for(Enumeration<MOB> m=mob.location().inhabitants();m.hasMoreElements();)
			h.add(m.nextElement());
		if(h.size()==1)
		{
			mob.tell(L("There doesn't appear to be an audience."));
			return false;
		}

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MASK_MAGIC|CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),
										auto?"":L("<S-NAME> begin(s) <S-HIS-HER> monologue."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for (final Object element : h)
				{
					final MOB target=(MOB)element;
					if(mob.mayIFight(target))
						addMonologue(mob,target,asLevel,auto);
				}
				mob.tell(L("^XYou must now SAY something at least every 4 seconds to maintain the monologue.^?^."));
			}
			setTimeOfNextCast(mob);
		}
		else
			return maliciousFizzle(mob,null,L("<S-NAME> attempt(s) to begin a monologue, but fail(s)."));
		return success;
	}
}
