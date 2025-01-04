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
   Copyright 2024-2025 Bo Zimmerman

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
public class Skill_ScrollFamiliarity extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_ScrollFamiliarity";
	}

	private final static String localizedName = CMLib.lang().L("Scroll Familiarity");

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
		return CAN_ITEMS;
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
		return Ability.ACODE_SKILL | Ability.DOMAIN_ARCANELORE;
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

	protected Set<Item> familiar = new LimitedTreeSet<Item>(CMProps.getMillisPerMudHour()*1000,100,false);

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if((affected instanceof MOB)
		&&(msg.target() instanceof Scroll)
		&&(msg.source()==affected)
		&&(((msg.targetMinor()==CMMsg.TYP_GET)&&familiar.contains(msg.target()))
			||((msg.targetMinor()==CMMsg.TYP_READ)&&(CMLib.flags().flaggedAffects(msg.source(), Ability.FLAG_DIVINING).size()>0)))
		&&(((Scroll)msg.target()).fetchEffect(ID())==null))
		{
			final Skill_ScrollFamiliarity me1 = this;
			msg.addTrailerRunnable(new Runnable()
			{
				final Scroll s = (Scroll)msg.target();
				final Skill_ScrollFamiliarity me = me1;
				final MOB invoker = msg.source();
				@Override
				public void run()
				{
					if(s.owner() == invoker)
					{
						final Skill_ScrollFamiliarity famA = (Skill_ScrollFamiliarity)me.copyOf();
						famA.readyTimeout = me.familiar.contains(s) ? 0 : (System.currentTimeMillis()+(CMProps.getTickMillis() * (100-me.proficiency())));
						famA.newName="";
						famA.invoker=invoker;
						s.addEffect(famA);
						famA.makeNonUninvokable();
						s.recoverPhyStats();
					}
				}
			});
		}
	}

	protected long readyTimeout = 0;
	protected String newName="";
	protected Room lastOkRoom = null;
	protected volatile boolean norecurse=false;

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(!(affected instanceof Scroll))
			return;
		if(norecurse)
			return;
		try
		{
			norecurse=true;
			if(invoker()==null)
			{
				if(((Scroll)affected).owner() instanceof Room)
				{
					final Room R = (Room)((Scroll)affected).owner();
					final MOB M = CMLib.law().getPropertyOwner(CMLib.law().getPropertyRecord(R));
					if((M == null)||(M.fetchAbility(ID())==null))
					{
						affected.delEffect(this);
						return;
					}
					setInvoker(M);
					lastOkRoom=R;
				}
				else
				if(((Scroll)affected).owner() instanceof MOB)
				{
					final MOB M = (MOB)((Scroll)affected).owner();
					setInvoker(M);
				}
				else
					return;
				final Skill_ScrollFamiliarity origA = (Skill_ScrollFamiliarity)invoker().fetchEffect(ID());
				if(origA != null)
					origA.familiar.add((Item)affected);
				else
				{
					setInvoker(null);
					return; // might be that the invoker is not yet loaded into the game
				}
			}
			if((((Scroll)affected).owner() instanceof Room)
			&&(((Scroll)affected).owner() != lastOkRoom)
			&&(invoker() != null))
			{
				final Room R = (Room)((Scroll)affected).owner();
				final MOB M = CMLib.law().getPropertyOwner(CMLib.law().getPropertyRecord(R));
				if(M != invoker())
				{
					affected.delEffect(this);
					return;
				}
				lastOkRoom=R;
			}
			if((((Scroll)affected).owner() != invoker())
			&&(((Scroll)affected).owner() != lastOkRoom))
			{
				affected.delEffect(this);
				return;
			}
			if(readyTimeout > 0)
			{
				if((System.currentTimeMillis()>readyTimeout)&&(invoker() != null))
				{
					readyTimeout=0;
					final Ability A = invoker().fetchEffect(ID());
					if(A!=null)
						A.helpProficiency(invoker(), 0);
					final Skill_ScrollFamiliarity origA = (Skill_ScrollFamiliarity)invoker().fetchEffect(ID());
					if(origA != null)
						origA.familiar.add((Item)affected);
				}
				else
					return;
			}
			if(((Scroll)affected).getSpells().size()==0)
				return;
			if(newName.length()==0)
			{
				newName = L("a scroll of @x1",CMLib.english().toEnglishStringList(new ConvertingList<Ability,String>(((Scroll)affected).getSpells(),
						new Converter<Ability,String>()
						{

							@Override
							public String convert(final Ability obj)
							{
								return (obj==null)?"":obj.name();
							}
						}
						)));
			}
			affectableStats.setName(newName);
		}
		finally
		{
			norecurse=false;
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(this.readyTimeout>0)
		{
			if((System.currentTimeMillis()>this.readyTimeout)
			&&(affected != null))
				affected.recoverPhyStats();
		}
		return true;
	}
}
