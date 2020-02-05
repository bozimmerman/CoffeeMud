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
   Copyright 2010-2020 Bo Zimmerman

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
		if((affected != null)
		&&(affected.fetchEffect(ID())==this)
		&&(affects.size()>0))
		{
			for(final Pair<Object,int[]> p : affects)
			{
				if(p.first instanceof Ability)
					return ((Ability)p.first).name();
			}
		}
		return localizedName;
	}

	@Override
	public String Name()
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
		if(affects.size()>0)
		{
			for(final Pair<Object,int[]> p : affects)
			{
				if(p.first instanceof Ability)
					return ((Ability)p.first).classificationCode();
			}
		}
		return Ability.ACODE_PROPERTY;
	}

	protected boolean			initialized	= false;

	protected List<String>				bindings	= new SVector<String>();
	protected List<Pair<Object, int[]>>	affects		= new SVector<Pair<Object, int[]>>();

	@Override
	public String displayText()
	{
		final StringBuilder str = new StringBuilder("");
		for(final Pair<Object,int[]> p : affects)
		{
			if(p.first instanceof Ability)
				str.append(((Ability)p.first).displayText());
		}
		return str.toString();
	}

	@Override
	public int abstractQuality()
	{
		for(final Pair<Object,int[]> p : affects)
		{
			if(p.first instanceof Ability)
			{
				if(((Ability)p.first).abstractQuality()==Ability.QUALITY_MALICIOUS)
					return Ability.QUALITY_MALICIOUS;
			}
		}
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public long flags()
	{
		long flag=0;
		for(final Pair<Object,int[]> p : affects)
		{
			if(p.first instanceof Ability)
				flag |=((Ability)p.first).flags();
		}
		return flag;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected==null)
			return;
		for(final Pair<Object,int[]> p : affects)
		{
			if(p.first instanceof StatsAffecting)
				((StatsAffecting)p.first).affectPhyStats(affected, affectableStats);
		}
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected, affectableStats);
		if(affected==null)
			return;
		for(final Pair<Object,int[]> p : affects)
		{
			if(p.first instanceof StatsAffecting)
				((StatsAffecting)p.first).affectCharStats(affected, affectableStats);
		}
	}

	@Override
	public void affectCharState(final MOB affected, final CharState affectableStats)
	{
		super.affectCharState(affected, affectableStats);
		if(affected==null)
			return;
		for(final Pair<Object,int[]> p : affects)
		{
			if(p.first instanceof StatsAffecting)
				((StatsAffecting)p.first).affectCharState(affected, affectableStats);
		}
	}

	public void unAffectAffected(final Pair<Object,int[]> Os)
	{
		final Object O = Os.first;
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
		for(final Pair<Object,int[]> p : affects)
			unAffectAffected(p);
		affects.clear();
		bindings.clear();
		super.unInvoke();
	}

	protected class AmbianceAdder implements StatsAffecting
	{
		private final String ambiance;

		public AmbianceAdder(final String ambiance)
		{
			this.ambiance=ambiance;
		}

		@Override
		public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
		{
			affectableStats.addAmbiance(ambiance);
		}

		@Override
		public void affectCharStats(final MOB affectedMob, final CharStats affectableStats)
		{
		}

		@Override
		public void affectCharState(final MOB affectedMob, final CharState affectableMaxState)
		{
		}
	}

	@Override
	public void setMiscText(String txt)
	{
		super.setMiscText("");
		if(txt.startsWith("-"))
		{
			txt=txt.substring(1).toLowerCase().trim();
			for(final Pair<Object,int[]> p : affects)
			{
				if((p.first instanceof CMObject)
				&&(((CMObject)p.first).ID().toLowerCase().equals(txt)))
				{
					unAffectAffected(p);
					return;
				}
			}
			for(final Pair<Object,int[]> p : affects)
			{
				if((p.first instanceof Ability)
				&&(((Environmental)p.first).name().toLowerCase().startsWith(txt)))
				{
					unAffectAffected(p);
					return;
				}
			}
			for(final Pair<Object,int[]> p : affects)
			{
				if((p.first instanceof Behavior)
				&&(((Behavior)p.first).name().toLowerCase().startsWith(txt)))
				{
					unAffectAffected(p);
					return;
				}
			}
			for(final Pair<Object,int[]> p : affects)
			{
				if((p.first instanceof AmbianceAdder)
				&&(txt.equalsIgnoreCase("AMBIANCE")))
				{
					unAffectAffected(p);
					return;
				}
			}
			if(bindings.contains(txt))
			{
				bindings.remove(txt);
				return;
			}
		}
		else
		if(txt.trim().length()>0)
		{
			if(txt.startsWith("+"))
			{
				txt=txt.substring(1);
				if(txt.toUpperCase().startsWith("BINDTO "))
				{
					final String name=txt.substring(7).trim();
					this.bindings.add(name.toLowerCase());
					return;
				}
			}
			else
			{
				for(final Pair<Object,int[]> p : affects)
					unAffectAffected(p);
				bindings.clear();
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
			if(abilityStr.equalsIgnoreCase("AMBIANCE"))
			{
				if(parms.length()>0)
				{
					final AmbianceAdder A=new AmbianceAdder(parms);
					affects.add(new Pair<Object,int[]>(A,new int[] { CMath.s_int(numTicksStr)}));
					finishInit(A);
				}
			}
			else
			{
				CMObject A=CMClass.getAbility(abilityStr);
				if(A==null)
					A=CMClass.getBehavior(abilityStr);
				if(A==null)
					A=CMClass.findAbility(abilityStr);
				if(A==null)
					A=CMClass.findBehavior(abilityStr);
				if(A!=null)
				{
					affects.add(new Pair<Object,int[]>(A,new int[] { CMath.s_int(numTicksStr)}));
					if(A instanceof Ability)
						((Ability)A).setMiscText(parms);
					if((A instanceof Behavior) && (affected instanceof PhysicalAgent))
						((Behavior)A).setParms(parms);
					finishInit(A);
				}
			}
		}
	}

	@Override
	public void setAffectedOne(final Physical P)
	{
		super.setAffectedOne(P);
		if((affects!=null)
		&&(!initialized))
		{
			for(final Pair<Object,int[]> p : affects)
				finishInit(p.first);
		}
	}

	public void finishInit(final Object A)
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
		for(final Pair<Object,int[]> p : affects)
		{
			if(p.first instanceof MsgListener)
			{
				if(!((MsgListener)p.first).okMessage(myHost, msg))
					return false;
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(destroyIfNecessary())
			return;
		if((msg.target() instanceof Room)
		&&((msg.targetMinor()==CMMsg.TYP_LEAVE)||(msg.sourceMinor()==CMMsg.TYP_RECALL)))
		{
			if((bindings.size()>0) && (affected != null) && initialized)
			{
				final Room R=CMLib.map().roomLocation(affected);
				if(R!=null)
				{
					for(final String binding : bindings)
					{
						if(R.fetchFromRoomFavorMOBs(null, binding)==null)
							bindings.remove(binding);
					}
					if(bindings.size()==0)
					{
						this.unInvoke();
						return;
					}
				}
			}
		}
		for(final Pair<Object,int[]> p : affects)
		{
			if(p.first instanceof MsgListener)
			{
				((MsgListener)p.first).executeMsg(myHost, msg);
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(destroyIfNecessary())
			return false;
		if(!super.tick(ticking, tickID))
			return false;
		super.makeLongLasting();
		for(final Pair<Object,int[]> p : affects)
		{
			if(p.first instanceof Tickable)
			{
				if(!((Tickable)p.first).tick(ticking, tickID))
					unAffectAffected(p);
				else
				{
					synchronized(p)
					{
						--p.second[0];
					}
					if(p.second[0]<=0)
						unAffectAffected(p);
				}
			}
		}
		if((bindings.size()>0) && (affected != null) && initialized)
		{
			final Room R=CMLib.map().roomLocation(affected);
			if(R!=null)
			{
				for(final String binding : bindings)
				{
					if(R.fetchFromRoomFavorMOBs(null, binding)==null)
						bindings.remove(binding);
				}
				if(bindings.size()==0)
				{
					this.unInvoke();
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if((commands.size()<2)
		||((commands.size()<3)&&(givenTarget==null)))
		{
			mob.tell(L("Specify a target, a property, number of ticks, and (optionally) some misc text"));
			mob.tell(L("Begin the first  property with ; to separate multiple entries by ;"));
			return false;
		}
		final Physical target;
		if(givenTarget != null)
			target=givenTarget;
		else
		{
			final Vector<String> V=new XVector<String>(commands.get(0));
			target=getAnyTarget(mob,V,givenTarget, Wearable.FILTER_ANY);
			if(target==null)
				return false;
			commands.remove(0);
		}

		final List<List<String>> sets=new LinkedList<List<String>>();
		if(commands.get(0).startsWith(";"))
		{
			if(commands.get(0).trim().length()<=1)
				commands.remove(0);
			else
				commands.set(0, commands.get(0).substring(1).trim());
			final String combined = CMParms.combineQuoted(commands,0);
			final List<String> cmdsets=CMParms.parseSemicolons(combined, true);
			for(final String cmdset : cmdsets)
				sets.add(CMParms.parse(cmdset));
		}
		else
			sets.add(commands);
		for(final List<String> set : sets)
		{
			String abilityStr = set.get(0);
			if(abilityStr.startsWith("+"))
				abilityStr=abilityStr.substring(1);
			final String numTicks;
			final String parms;
			if(abilityStr.equalsIgnoreCase("BINDTO"))
			{
				numTicks="";
				abilityStr=abilityStr.toUpperCase().trim();
				parms=CMParms.combine(set,1);
			}
			else
			{
				if(abilityStr.equalsIgnoreCase("AMBIANCE"))
				{
					abilityStr=abilityStr.toUpperCase().trim();
				}
				else
				{
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
					abilityStr=A.ID();
				}
				numTicks=set.get(1).trim();
				if((!CMath.isInteger(numTicks)) ||(CMath.s_int(numTicks)<=0))
				{
					mob.tell(L("'@x1' is not a number of ticks!",numTicks));
					return false;
				}
				parms=CMParms.combineQuoted(set, 2);
			}

			TemporaryAffects T = (TemporaryAffects)target.fetchEffect(ID());
			if(T==null)
			{
				T=(TemporaryAffects)this.newInstance();
				T.affects=new SVector<Pair<Object,int[]>>();
				T.startTickDown(mob, target, 10);
				T.makeLongLasting();
				T = (TemporaryAffects)target.fetchEffect(ID());
			}
			if(T!=null)
			{
				T.setMiscText("+"+abilityStr+" "+numTicks.trim()+" "+parms.trim());
				T.makeLongLasting();
			}
		}
		return true;
	}
}
