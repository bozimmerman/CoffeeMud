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
   Copyright 2010-2018 Bo Zimmerman

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

public class TemporaryAffects extends StdAbility
{
	@Override
	public String ID()
	{
		return "TemporaryAffects";
	}

	private final static String	localizedName	= CMLib.lang().L("Temporary Affects");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS | CAN_ITEMS | CAN_EXITS | CAN_ROOMS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS | CAN_ITEMS | CAN_EXITS | CAN_ROOMS;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	private static final String[]	triggerStrings	= I(new String[] { "TEMPORARYAFFECTS" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PROPERTY;
	}

	protected boolean			initialized	= false;

	protected List<Object[]> affects = new SVector<Object[]>();

	@Override
	public String displayText()
	{
		final StringBuilder str = new StringBuilder("");
		for(final Object[] A : affects)
		{
			if(A[0] instanceof Ability)
				str.append(((Ability)A[0]).displayText());
		}
		return str.toString();
	}

	@Override
	public int abstractQuality()
	{
		for(final Object[] A : affects)
		{
			if(A[0] instanceof Ability)
			{
				if(((Ability)A[0]).abstractQuality()==Ability.QUALITY_MALICIOUS)
					return Ability.QUALITY_MALICIOUS;
			}
		}
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public long flags()
	{
		long flag=0;
		for(final Object[] A : affects)
		{
			if(A[0] instanceof Ability)
				flag |=((Ability)A[0]).flags();
		}
		return flag;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected==null)
			return;
		for(final Object[] A : affects)
		{
			if(A[0] instanceof StatsAffecting)
				((StatsAffecting)A[0]).affectPhyStats(affected, affectableStats);
		}
	}

	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected, affectableStats);
		if(affected==null)
			return;
		for(final Object[] A : affects)
		{
			if(A[0] instanceof StatsAffecting)
				((StatsAffecting)A[0]).affectCharStats(affected, affectableStats);
		}
	}

	@Override
	public void affectCharState(MOB affected, CharState affectableStats)
	{
		super.affectCharState(affected, affectableStats);
		if(affected==null)
			return;
		for(final Object[] A : affects)
		{
			if(A[0] instanceof StatsAffecting)
				((StatsAffecting)A[0]).affectCharState(affected, affectableStats);
		}
	}

	public void unAffectAffected(Object[] Os)
	{
		final CMObject O = (CMObject)Os[0];
		final Physical P=affected;
		if(O instanceof Ability)
		{
			((Ability)O).unInvoke();
			((Ability)O).destroy();
		}
		affects.remove(Os);
		if(P != null)
			P.recoverPhyStats();
		if(P instanceof MOB)
		{
			((MOB)P).recoverCharStats();
			((MOB)P).recoverMaxState();
		}
	}

	@Override
	public void unInvoke()
	{
		if(affected==null)
			return;
		for(final Object[] A : affects)
			unAffectAffected(A);
		affects.clear();
		super.unInvoke();
	}

	@Override
	public void setMiscText(String txt)
	{
		super.setMiscText("");
		if(txt.startsWith("-"))
		{
			txt=txt.substring(1).toLowerCase().trim();
			for(final Object[] A : affects)
			{
				if(((CMObject)A[0]).ID().toLowerCase().equals(txt))
				{
					unAffectAffected(A);
					return;
				}
			}
			for(final Object[] A : affects)
			{
				if((A[0] instanceof Ability)
				&&(((Environmental)A[0]).name().toLowerCase().startsWith(txt)))
				{
					unAffectAffected(A);
					return;
				}
			}
			for(final Object[] A : affects)
			{
				if((A[0] instanceof Behavior)
				&&(((Behavior)A[0]).name().toLowerCase().startsWith(txt)))
				{
					unAffectAffected(A);
					return;
				}
			}
		}
		else
		if(txt.trim().length()>0)
		{
			if(txt.startsWith("+"))
				txt=txt.substring(1);
			else
			{
				for(final Object[] A : affects)
					unAffectAffected(A);
			}

			int x=txt.indexOf(' ');
			if(x<0)
				return;
			final String abilityStr=txt.substring(0,x).trim();
			String numTicksStr=txt.substring(x+1).trim();
			String parms="";
			x=numTicksStr.indexOf(' ');
			if(x>0)
			{
				parms=numTicksStr.substring(x+1).trim();
				numTicksStr=numTicksStr.substring(0,x);
			}
			CMObject A=CMClass.getAbility(abilityStr);
			if(A==null)
				A=CMClass.getBehavior(abilityStr);
			if(A==null)
				A=CMClass.findAbility(abilityStr);
			if(A==null)
				A=CMClass.findBehavior(abilityStr);
			if(A!=null)
			{
				affects.add(new Object[]{A,new int[]{CMath.s_int(numTicksStr)}});
				if(A instanceof Ability)
					((Ability)A).setMiscText(parms);
				if((A instanceof Behavior) && (affected instanceof PhysicalAgent))
					((Behavior)A).setParms(parms);
				finishInit(A);
			}
		}
	}

	@Override
	public void setAffectedOne(Physical P)
	{
		super.setAffectedOne(P);
		if((affects!=null)&&(!initialized))
			for(final Object[] set : affects)
				finishInit((CMObject)set[0]);
	}

	public void finishInit(CMObject A)
	{
		if(affected == null)
			return;
		if(A instanceof Ability)
		{
			((Ability)A).makeNonUninvokable();
			((Ability)A).makeLongLasting();
			((Ability)A).setAffectedOne(affected);
		}
		if((A instanceof Behavior) && (affected instanceof PhysicalAgent))
			((Behavior)A).startBehavior((PhysicalAgent)affected);
		if(affected != null)
			affected.recoverPhyStats();
		if(affected instanceof MOB)
		{
			((MOB)affected).recoverCharStats();
			((MOB)affected).recoverMaxState();
		}
		initialized=true;
	}

	public boolean destroyIfNecessary()
	{
		final Physical E=affected;
		if((affects.size()==0) && (E != null))
		{
			unInvoke();
			E.delEffect(this);
			return true;
		}
		return false;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(destroyIfNecessary())
			return true;
		for(final Object[] A : affects)
		{
			if(!((MsgListener)A[0]).okMessage(myHost, msg))
				return false;
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(destroyIfNecessary())
			return;
		for(final Object[] A : affects)
			((MsgListener)A[0]).executeMsg(myHost, msg);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(destroyIfNecessary())
			return false;
		if(!super.tick(ticking, tickID))
			return false;
		super.makeLongLasting();
		for(final Object[] A : affects)
		{
			if(!((Tickable)A[0]).tick(ticking, tickID))
				unAffectAffected(A);
			else
			if((--((int[])A[1])[0])<=0)
				unAffectAffected(A);
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<3)
		{
			mob.tell(L("Specify a target, a property, number of ticks, and (optionally) some misc text!"));
			return false;
		}
		final Vector<String> V=new XVector<String>(commands.get(0));
		final Physical target=getAnyTarget(mob,V,givenTarget, Wearable.FILTER_ANY);
		if(target==null)
			return false;
		commands.remove(0);

		final String abilityStr = commands.get(0);
		CMObject A=CMClass.getAbility(abilityStr);
		if(A==null)
			A=CMClass.getBehavior(abilityStr);
		if(A==null)
			A=CMClass.findAbility(abilityStr);
		if(A==null)
			A=CMClass.findBehavior(abilityStr);
		if(A==null)
		{
			mob.tell(L("No such ability or behavior as @x1!",abilityStr));
			return false;
		}
		final String numTicks=commands.get(1).trim();
		if((!CMath.isInteger(numTicks)) ||(CMath.s_int(numTicks)<=0))
		{
			mob.tell(L("'@x1' is not a number of ticks!",numTicks));
			return false;
		}
		final String parms=CMParms.combine(commands, 2);

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		TemporaryAffects T = (TemporaryAffects)target.fetchEffect(ID());
		if(T==null)
		{
			T=(TemporaryAffects)this.newInstance();
			T.affects=new SVector<Object[]>();
			T.startTickDown(mob, target, 10);
			T = (TemporaryAffects)target.fetchEffect(ID());
		}
		if(T!=null)
		{
			T.setMiscText("+"+A.ID()+" "+numTicks.trim()+" "+parms.trim());
			T.makeLongLasting();
		}
		return true;
	}
}
