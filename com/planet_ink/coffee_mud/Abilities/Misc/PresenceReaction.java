package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class PresenceReaction extends StdAbility
{
	public String ID(){return "PresenceReaction";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	protected MOB reactToM=null;
	protected boolean startedManaging=false;
	protected String previousMood = null;
	protected String reactToName=null;
	protected Vector<Object[]> unmanagedYet=new Vector<Object[]>();
	protected Vector<CMObject> managed = new Vector<CMObject>();

	public PresenceReaction()
	{
		super();
		super.makeLongLasting();
		super.savable=false;
		super.canBeUninvoked=false;
	}
	protected void cloneFix(Ability E){
		expertise=null;
		reactToM=null;
		previousMood=null;
		reactToName=null;
		reactToM=null;
		affected=null;
		invoker=null;
		unmanagedYet=new Vector<Object[]>();
		managed = new Vector<CMObject>();
	}

	public void addAffectOrBehavior(String substr)
	{
		int x=substr.indexOf('=');
		if(x>=0)
		{
			String nam=substr.substring(0,x);
			if(nam.trim().length()==0) {
				reactToName=substr.substring(1);
				return;
			}
			Behavior B=CMClass.getBehavior(nam);
			if(B!=null)
			{
				B.setSavable(false);
				Object[] SET=new Object[]{B,substr.substring(x+1)};
				unmanagedYet.addElement(SET);
				return;
			}
			Ability A=CMClass.getAbility(nam);
			if(A!=null)
			{
				A.setSavable(false);
				A.makeNonUninvokable();
				Object[] SET=new Object[]{A,substr.substring(x+1)};
				unmanagedYet.addElement(SET);
				return;
			}
			Command C=CMClass.getCommand(nam);
			if(C!=null)
			{
				Object[] SET=new Object[]{C,substr.substring(x+1)};
				unmanagedYet.addElement(SET);
			}
		}
	}
	
	public void setMiscText(String parms)
	{
		if(parms.startsWith("+"))
			addAffectOrBehavior(parms.substring(1));
		else
		{
			Vector parsed=CMParms.parseAny(parms,"~~",true);
			for(Enumeration e=parsed.elements();e.hasMoreElements();)
				addAffectOrBehavior((String)e.nextElement());
		}
	}

	public boolean okMessage(Environmental affecting, CMMsg msg)
	{
		for(CMObject O : managed)
			if(O instanceof MsgListener)
				if(!((MsgListener)O).okMessage(affecting, msg))
					return false;
		return super.okMessage(affecting,msg);
	}

	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		for(CMObject O : managed)
			if(O instanceof MsgListener)
				((MsgListener)O).executeMsg(affecting, msg);
		super.executeMsg(affecting,msg);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		for(CMObject O : managed)
			if(O instanceof StatsAffecting)
				((StatsAffecting)O).affectEnvStats(affected, affectableStats);
	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
		for(CMObject O : managed)
			if(O instanceof StatsAffecting)
				((StatsAffecting)O).affectCharStats(affectedMob, affectableStats);
	}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{
		for(CMObject O : managed)
			if(O instanceof StatsAffecting)
				((StatsAffecting)O).affectCharState(affectedMob, affectableMaxState);
	}
	protected synchronized boolean shutdownPresence(MOB affected)
	{
		Room R=affected.location();
		if(((R==null)||(reactToM==null)||(!R.isInhabitant(reactToM))))
		{
			MOB M=(MOB)super.affected;
			for(CMObject O : managed)
			{
				if((O!=null)&&(O.ID().equals("Mood")))
				{
					if((previousMood!=null)&&(affected instanceof MOB))
					{
						try
						{
							Command C=CMClass.getCommand("Mood");
							if(C!=null)
								C.execute(M,CMParms.parse("MOOD "+previousMood),0);
						} catch(Exception e){}
					}
				}
				else
				if(O instanceof Environmental)
					((Environmental)O).destroy();
			}
			affected.delEffect(this);
		}
		unmanagedYet.clear();
		managed.clear();
		return false;
	}
	
	protected boolean initializeManagedObjects(MOB affected)
	{
		if(unmanagedYet.size()==0) 
			return false;
		boolean didAnything=false;
		Vector<Object[]> commands = new Vector<Object[]>();
		while(unmanagedYet.size()>0)
		{
			Object[] thing=unmanagedYet.remove(0);
			if(thing[0] instanceof Ability)
			{
				if(((Ability)thing[0]).ID().equalsIgnoreCase("Mood"))
				{
					previousMood="";
					Ability A=affected.fetchEffect("Mood");
					if(A!=null) previousMood=A.text();
					if(previousMood.trim().length()==0)
						previousMood="NORMAL";
				}
				Ability A=(Ability)thing[0];
				A.setAffectedOne(affected);
				A.setMiscText((String)thing[1]);
				managed.add(A);
				didAnything=true;
				continue;
			}
			if(thing[0] instanceof Behavior)
			{
				Behavior B=(Behavior)thing[0];
				B.startBehavior(affected);
				B.setParms((String)thing[1]);
				managed.add(B);
				didAnything=true;
				continue;
			}
			if(thing[0] instanceof Command)
			{
				commands.add(thing);
				continue;
			}
		}
		unmanagedYet = commands;
		if(didAnything)
		{
			affected.recoverCharStats();
			affected.recoverEnvStats();
			affected.recoverMaxState();
		}
		return didAnything;
	}

	protected void initializeAllManaged(MOB affected)
	{
		if(unmanagedYet.size()==0) return;
		initializeManagedObjects(affected);
		while(unmanagedYet.size()>0)
		{
			Object[] thing=unmanagedYet.remove(0);
			if(thing[0] instanceof Command)
			{
				Command C=(Command)thing[0];
				try
				{
					String cmdparms=C.getAccessWords()[0]+" "+CMStrings.replaceAll((String)thing[1],"<TARGET>",reactToM.Name());
					affected.enqueCommand(CMParms.parse(cmdparms),Command.METAFLAG_FORCED, 0);
				} catch(Exception e){}
				managed.add(C);
				continue;
			}
		}
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(tickID!=Tickable.TICKID_MOB) return true;
		if(reactToM==null)
		{
			// dont combine this if with the above
			if((affected instanceof MOB)&&(reactToName!=null))
				reactToM=((MOB)affected).location().fetchInhabitant(reactToName);
			if(reactToM==null)
				return shutdownPresence((MOB)affected);
		}
		else
		if(this.affected instanceof MOB)
		{
			MOB affected=(MOB)this.affected;
			if((affected.location()!=reactToM.location())
				||(affected.amDead())
				||(reactToM.amDead())
				||(affected.amDestroyed())
				||(reactToM.amDestroyed())
				||(!CMLib.flags().isInTheGame(affected, true))
				||(!CMLib.flags().isInTheGame(reactToM, true)))
					return shutdownPresence(affected);
			initializeAllManaged(affected);
			for(CMObject O : managed)
				if(O instanceof Tickable)
					((Tickable)O).tick(ticking, tickID);
		}
		return true;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental target, boolean auto, int asLevel)
	{
		if(target==null)
		{
			PresenceReaction A=(PresenceReaction)mob.fetchEffect(ID());
			if(A!=null) 
				A.shutdownPresence(mob);
			if(affected==mob)
				shutdownPresence(mob);
			return A!=null;
		}
		if(!(target instanceof MOB)) return false;
		PresenceReaction A=(PresenceReaction)this.copyOf();
		A.reactToM=(MOB)target;
		for(Object O : commands)
			A.addAffectOrBehavior((String)O);
		commands.clear();
		commands.addElement(A);
		if(auto)
		{
			synchronized(mob)
			{
				if(mob.fetchEffect(ID())==null)
				{
					mob.addNonUninvokableEffect(A);
					A.initializeManagedObjects(mob);
				}
				return true;
			}
		}
		else
		{
			A.makeLongLasting();
			A.makeNonUninvokable();
			A.setAffectedOne(mob);
			A.initializeManagedObjects(mob);
 			return true;
		}
		
	}
}
