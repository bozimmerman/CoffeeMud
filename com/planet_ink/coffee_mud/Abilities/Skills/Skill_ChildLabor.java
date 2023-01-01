package com.planet_ink.coffee_mud.Abilities.Skills;
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
   Copyright 2022-2023 Bo Zimmerman

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
public class Skill_ChildLabor extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_ChildLabor";
	}

	private final static String localizedName = CMLib.lang().L("Child Labor");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
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
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_LEGAL;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	protected volatile int		kidTickMax	= -1;
	protected volatile int		kidTickDown	= 0;
	protected volatile MOB		craftM		= null;
	protected volatile String	craftAID	= null;
	protected volatile Ability	craftA		= null;
	protected final List<MOB>	kids		= new Vector<MOB>();

	protected int countOrphans(final MOB notM, final Room R)
	{
		if((R != null)
		&&(R.numInhabitants()>1))
		{
			final List<MOB> kids = new ArrayList<MOB>();
			final CMFlagLibrary flags = CMLib.flags();
			for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
			{
				final MOB M = m.nextElement();
				if((M != null)
				&&(!M.isPlayer())
				&&(M != notM)
				&&(flags.isChild(M))
				&&(flags.isAliveAwakeMobileUnbound(M, true)))
					kids.add(M);
			}
			this.kids.clear();
			this.kids.addAll(kids);
			return this.kids.size();
		}
		return 0;
	}

	protected void shutdown()
	{
		this.kidTickMax = -1;
		this.kidTickDown = 0;
		this.craftM = null;
		this.craftA = null;
		this.craftAID = null;
	}

	protected Ability confirmChildLabor()
	{
		synchronized(this)
		{
			final MOB craftM = this.craftM;
			final Ability craftA = this.craftA;
			final String craftAID = this.craftAID;
			if((craftA != null) && (craftM != null))
				return craftM.fetchEffect(craftA.ID());
			if((craftAID != null) && (craftM != null))
				return craftM.fetchEffect(craftAID);
			if(craftAID != null)
				shutdown();
		}
		return null;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if((msg.target() instanceof Room)
		&&((msg.targetMinor()==CMMsg.TYP_ENTER)
			||(msg.targetMinor()==CMMsg.TYP_LEAVE)
			||(msg.targetMinor()==CMMsg.TYP_RECALL))
		&&(msg.target()==mob.location()))
		{
			kidTickMax = -1;
			if(msg.source()==mob)
				shutdown();
		}
		if((msg.tool() instanceof Ability)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL)
		&&(((((Ability)msg.tool()).classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL)
			||((((Ability)msg.tool()).classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_GATHERINGSKILL)))
		{
			if((confirmChildLabor() == null)
			&&(CMLib.law().doesAnyoneHavePrivilegesHere(mob, "", mob.location()))
			&&(super.proficiencyCheck(mob, 0, false)))
			{
				this.craftAID = msg.tool().ID();
				this.craftM = msg.source();
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		final Ability laborA = confirmChildLabor();
		if(laborA != null)
		{
			if(this.craftA == null)
				this.craftA = laborA;
			final MOB craftM = this.craftM;
			if(kidTickMax < 0)
			{
				if(craftM == null)
					shutdown();
				else
				{
					final int kids = countOrphans(craftM, craftM.location());
					if(kids > 0)
					{
						kidTickMax = CMath.s_int(laborA.getStat("TICKDOWN"));
						if(kidTickMax <= 0)
							shutdown();
						else
						{
							final int xlevel = super.getXLEVELLevel(invoker());
							if(xlevel < kidTickMax)
								kidTickMax = Math.max(kidTickMax-xlevel / kids,2);
							else
								kidTickMax = 2;
							this.kidTickDown = kidTickMax;
							final Room R=craftM.location();
							if(R != null)
								R.show(craftM, null, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> put(s) @x1 children to work!",""+kidTickMax));
						}
					}
					else
						shutdown();
				}
			}
			if((kidTickMax > 0)
			&&(--this.kidTickDown <= 0))
			{
				this.kidTickDown = kidTickMax;
				if(!laborA.tick(ticking, Tickable.TICKID_MOB))
					shutdown();
				else
				if(this.kids.size()>0)
				{
					final MOB M = kids.get(CMLib.dice().roll(1,kids.size(),-1));
					final Room R=M.location();
					if((R != null)
					&&(CMLib.flags().isAliveAwakeMobileUnbound(M,true)))
						R.show(M, null, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> toil(s) away."));
				}
			}
		}
		return true;
	}

}
