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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
public class TemporaryAffects extends StdAbility
{
	public String ID() { return "TemporaryAffects"; }
	public String name(){ return "Temporary Affects";}
	protected int canAffectCode(){return CAN_MOBS | CAN_ITEMS | CAN_EXITS | CAN_ROOMS;}
	protected int canTargetCode(){return CAN_MOBS | CAN_ITEMS | CAN_EXITS | CAN_ROOMS;}
	public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings = {"TEMPORARYAFFECTS"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_PROPERTY;}
	protected boolean initialized=false;

	protected SVector<Object[]> affects = new SVector<Object[]>();
	
	public String displayText()
	{ 
		StringBuilder str = new StringBuilder("");
		for(Object[] A : affects)
			if(A[0] instanceof Ability)
				str.append(((Ability)A[0]).displayText());
		return str.toString();
	}
	public int abstractQuality()
	{
		for(Object[] A : affects)
			if(A[0] instanceof Ability)
				if(((Ability)A[0]).abstractQuality()==Ability.QUALITY_MALICIOUS)
					return Ability.QUALITY_MALICIOUS;
		return Ability.QUALITY_INDIFFERENT;
	}
	public long flags()
	{
		long flag=0;
		for(Object[] A : affects)
			if(A[0] instanceof Ability)
				flag |=((Ability)A[0]).flags();
		return flag;
	}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected==null) return;
		for(Object[] A : affects)
			if(A[0] instanceof StatsAffecting)
				((StatsAffecting)A[0]).affectPhyStats(affected, affectableStats);
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected, affectableStats);
		if(affected==null) return;
		for(Object[] A : affects)
			if(A[0] instanceof StatsAffecting)
				((StatsAffecting)A[0]).affectCharStats(affected, affectableStats);
	}

	public void affectCharState(MOB affected, CharState affectableStats)
	{
		super.affectCharState(affected, affectableStats);
		if(affected==null) return;
		for(Object[] A : affects)
			if(A[0] instanceof StatsAffecting)
				((StatsAffecting)A[0]).affectCharState(affected, affectableStats);
	}

	public void unAffectAffected(Object[] Os)
	{
		CMObject O = (CMObject)Os[0];
		Physical P=affected;
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
	
	public void unInvoke()
	{
		if(affected==null)
			return;
		for(Object[] A : affects)
			unAffectAffected(A);
		affects.clear();
		super.unInvoke();
	}

	public void setMiscText(String txt)
	{
		super.setMiscText("");
		if(txt.startsWith("-"))
		{
			txt=txt.substring(1).toLowerCase().trim();
			for(Object[] A : affects)
				if(((CMObject)A[0]).ID().toLowerCase().equals(txt))
				{
					unAffectAffected(A);
					return;
				}
			for(Object[] A : affects)
				if((A[0] instanceof Ability)
				&&(((Environmental)A[0]).name().toLowerCase().startsWith(txt)))
				{
					unAffectAffected(A);
					return;
				}
			for(Object[] A : affects)
				if((A[0] instanceof Behavior)
				&&(((Behavior)A[0]).name().toLowerCase().startsWith(txt)))
				{
					unAffectAffected(A);
					return;
				}
		}
		else
		if(txt.trim().length()>0)
		{
			if(txt.startsWith("+"))
				txt=txt.substring(1);
			else
				for(Object[] A : affects)
					unAffectAffected(A);
				
			int x=txt.indexOf(' ');
			if(x<0) return;
			String abilityStr=txt.substring(0,x).trim();
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
				affects.addElement(new Object[]{A,new int[]{CMath.s_int(numTicksStr)}});
				if(A instanceof Ability)
					((Ability)A).setMiscText(parms);
				if((A instanceof Behavior) && (affected instanceof PhysicalAgent))
					((Behavior)A).setParms(parms);
				finishInit(A);
			}
		}
	}
	
	public void setAffectedOne(Physical P)
	{
		super.setAffectedOne(P);
		if((affects!=null)&&(!initialized))
			for(Object[] set : affects)
				finishInit((CMObject)set[0]);
	}
	
	
	public void finishInit(CMObject A)
	{
		if(affected == null) return;
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
		Physical E=affected;
		if((affects.size()==0) && (E != null))
		{
			unInvoke();
			E.delEffect(this);
			return true;
		}
		return false;
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(destroyIfNecessary())
			return true;
		for(Object[] A : affects)
			if(!((MsgListener)A[0]).okMessage(myHost, msg))
				return false;
		return true;
	}
	
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(destroyIfNecessary())
			return;
		for(Object[] A : affects)
			((MsgListener)A[0]).executeMsg(myHost, msg);
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(destroyIfNecessary())
			return false;
		if(!super.tick(ticking, tickID))
			return false;
		super.makeLongLasting();
		for(Object[] A : affects)
			if(!((Tickable)A[0]).tick(ticking, tickID))
				unAffectAffected(A);
			else
			if((--((int[])A[1])[0])<=0)
				unAffectAffected(A);
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<3)
		{
			mob.tell("Specify a target, a property, number of ticks, and (optionally) some misc text!");
			return false;
		}
		Vector V=new XVector(commands.firstElement());
		Physical target=getAnyTarget(mob,V,givenTarget, asLevel);
		if(target==null) return false;
		commands.removeElementAt(0);
		
		String abilityStr = (String)commands.firstElement();
		CMObject A=CMClass.getAbility(abilityStr);
		if(A==null) 
			A=CMClass.getBehavior(abilityStr);
		if(A==null) 
			A=CMClass.findAbility(abilityStr);
		if(A==null) 
			A=CMClass.findBehavior(abilityStr);
		if(A==null)
		{
			mob.tell("No such ability or behavior as "+abilityStr+"!");
			return false;
		}
		String numTicks=((String)commands.elementAt(1)).trim();
		if((!CMath.isInteger(numTicks)) ||(CMath.s_int(numTicks)<=0))
		{
			mob.tell("'"+numTicks+"' is not a number of ticks!");
			return false;
		}
		String parms=CMParms.combine(commands, 2);

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
