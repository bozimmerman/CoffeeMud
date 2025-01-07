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
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

import org.w3c.dom.Text;

/*
   Copyright 2024-2024 Bo Zimmerman

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
public class TriggeredAffects extends StdAbility implements TriggeredAffect
{
	@Override
	public String ID()
	{
		return "TriggeredAffects";
	}

	private final static String	localizedName	= CMLib.lang().L("Triggered Affects");

	@Override
	public String name()
	{
		if((affected != null)
		&&(affected.fetchEffect(ID())==this)
		&&(affects.size()>0))
		{
			for(final TriggeredAffect p : affects)
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
	public int triggerMask()
	{
		return com.planet_ink.coffee_mud.Abilities.interfaces.TriggeredAffect.TRIGGER_ALWAYS;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	private static final String[]	triggerStrings	= I(new String[] { "TRIGGEREDAFFECTS" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public String accountForYourself()
	{
		if(affects.size()>0)
		{
			final StringBuilder str=new StringBuilder("");
			for(final TriggeredAffect p : affects)
			{
				if(p.first instanceof Ability)
					str.append(((Ability)p.first).accountForYourself()).append("\n\r");
			}
			return str.toString().trim();
		}
		else
			return name();
	}

	@Override
	public int classificationCode()
	{
		if(affects.size()>0)
		{
			for(final TriggeredAffect p : affects)
			{
				if(p.first instanceof Ability)
					return ((Ability)p.first).classificationCode();
			}
		}
		return Ability.ACODE_PROPERTY;
	}

	protected class TriggeredAffect
	{
		public Object		first		= null;
		public int[]		attribParms	= new int[0];
		public boolean		invoke		= false;
		public short		onTrigger	= 1; // 1=add, -1=remove
		public Triggerer	trigger		= null;
		public String		message		= null;
		public int			ticks		= 0;
		public volatile int	tickDown	= 0;
		public TriggeredAffect(final Object o, final short ot, final String trigStr, final int ticks, final boolean i)
		{
			this.first=o;
			this.onTrigger=ot;
			this.trigger = (Triggerer)CMClass.getCommon("DefaultTriggerer");
			trigger.addTrigger(o, trigStr, new HashMap<String,List<Social>>(), null);
			this.ticks=ticks;
			this.invoke=i;
		}
	}

	protected SVector<TriggeredAffect>	naffects	= new SVector<TriggeredAffect>();
	protected SVector<TriggeredAffect>	affects		= new SVector<TriggeredAffect>();

	@Override
	public String displayText()
	{
		final StringBuilder str = new StringBuilder("");
		for(int p=0;p<affects.size();p++)
		{
			final TriggeredAffect P = affects.getSafe(p);
			if((P!=null) && (P.first instanceof Ability))
				str.append(((Ability)P.first).displayText());
		}
		return str.toString();
	}

	@Override
	public int abstractQuality()
	{
		for(int p=0;p<affects.size();p++)
		{
			final TriggeredAffect P = affects.getSafe(p);
			if((P!=null) && (P.first instanceof Ability))
			{
				if(((Ability)P.first).abstractQuality()==Ability.QUALITY_MALICIOUS)
					return Ability.QUALITY_MALICIOUS;
			}
		}
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public long flags()
	{
		long flag=0;
		for(int p=0;p<affects.size();p++)
		{
			final TriggeredAffect P = affects.getSafe(p);
			if((P!=null) && (P.first instanceof Ability))
				flag |=((Ability)P.first).flags();
		}
		return flag;
	}

	protected MOB fixM = null;

	protected MOB getFixMob()
	{
		if(fixM == null)
		{
			synchronized(this)
			{
				if(fixM == null)
				{
					fixM = CMClass.getMOB("StdMOB");
				}
			}
		}
		return fixM;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected==null)
			return;
		for(int p=0;p<affects.size();p++)
		{
			final TriggeredAffect P = affects.getSafe(p);
			if((P!=null) && (P.first instanceof StatsAffecting))
				((StatsAffecting)P.first).affectPhyStats(affected, affectableStats);
		}
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected, affectableStats);
		if(affected==null)
			return;
		for(int p=0;p<affects.size();p++)
		{
			final TriggeredAffect P = affects.getSafe(p);
			if((P!=null) && (P.first instanceof StatsAffecting))
				((StatsAffecting)P.first).affectCharStats(affected, affectableStats);
		}
	}

	@Override
	public void affectCharState(final MOB affected, final CharState affectableStats)
	{
		super.affectCharState(affected, affectableStats);
		if(affected==null)
			return;
		for(int p=0;p<affects.size();p++)
		{
			final TriggeredAffect P = affects.getSafe(p);
			if((P!=null) && (P.first instanceof StatsAffecting))
				((StatsAffecting)P.first).affectCharState(affected, affectableStats);
		}
	}

	public void unAffectAffected(final TriggeredAffect Os)
	{
		synchronized(affects)
		{
			affects.remove(Os);
		}
		synchronized(naffects)
		{
			naffects.add(Os);
		}
		final Physical P=affected;
		if(P == null)
			return;
		Os.tickDown = Os.ticks;
		final Object O = Os.first;
		if((O instanceof Ability)&&(((Ability)O).affecting()==P))
			((Ability)O).unInvoke();
		if((O instanceof Attrib)
		&& (P instanceof MOB)
		&& (Os.attribParms.length>1))
			((MOB)P).setAttribute((Attrib)O, Os.attribParms[1]==0?true:false);
		P.recoverPhyStats();
		if(P instanceof MOB)
		{
			((MOB)P).recoverCharStats();
			((MOB)P).recoverMaxState();
		}
	}

	public void reAffectAffected(final TriggeredAffect Os)
	{
		synchronized(naffects)
		{
			naffects.remove(Os);
		}
		synchronized(affects)
		{
			affects.add(Os);
		}
		final Physical P=affected;
		if(P == null)
			return;
		Os.tickDown = Os.ticks;
		Object A = Os.first;
		if(A instanceof Ability)
		{
			if(Os.invoke)
			{
				final MOB fixM = getFixMob();
				synchronized(fixM)
				{
					final boolean wasEffect = fixM.fetchEffect(((Ability)A).ID()) != null;
					if(!wasEffect)
					{
						fixM.basePhyStats().setLevel(P.phyStats().level());
						fixM.phyStats().setLevel(P.phyStats().level());
						fixM.setLocation(CMLib.map().roomLocation(P));
						((Ability)A).invoke(fixM, P, true, 0);
						final Ability effA = fixM.fetchEffect(((Ability)A).ID());
						if((effA != null)&&(!effA.isNowAnAutoEffect()))
						{
							fixM.delEffect(effA);
							Os.first = effA;
							A=effA;
						}
					}
				}
			}
			((Ability)A).makeNonUninvokable();
			((Ability)A).makeLongLasting();
			((Ability)A).setAffectedOne(P);
		}
		if((A instanceof Behavior) && (P instanceof PhysicalAgent))
			((Behavior)A).startBehavior((PhysicalAgent)P);
		P.recoverPhyStats();
		if(P instanceof MOB)
		{
			((MOB)P).recoverCharStats();
			((MOB)P).recoverMaxState();
		}
	}

	protected void eraseSelf()
	{
		for(int p=affects.size()-1;p>=0;p--)
		{
			final TriggeredAffect P = affects.getSafe(p);
			if(P!=null)
				unAffectAffected(P);
		}
		affects.clear();
		naffects.clear();
	}

	@Override
	public void unInvoke()
	{
		if((affected==null)||(!super.canBeUninvoked()))
			return;
		eraseSelf();
		super.unInvoke();
		destroy();
	}

	protected void unInvokeByName(final String txt)
	{
		for(int p=0;p<affects.size();p++)
		{
			final TriggeredAffect P = affects.getSafe(p);
			if(P==null)
				continue;
			if((P.first instanceof CMObject)
			&&(((CMObject)P.first).ID().toLowerCase().equals(txt)))
			{
				unAffectAffected(P);
				return;
			}
			else
			if((P.first instanceof Ability)
			&&(((Environmental)P.first).name().toLowerCase().startsWith(txt)))
			{
				unAffectAffected(P);
				return;
			}
			else
			if((P.first instanceof Behavior)
			&&(((Behavior)P.first).name().toLowerCase().startsWith(txt)))
			{
				unAffectAffected(P);
				return;
			}
			else
			if((P.first instanceof AmbianceAdder)
			&&(txt.equalsIgnoreCase("AMBIANCE")))
			{
				unAffectAffected(P);
				return;
			}
			else
			if((P.first instanceof Attrib)
			&&(txt.equalsIgnoreCase(((Attrib)P.first).name())))
			{
				unAffectAffected(P);
				return;
			}
		}
	}

	private static String[] VALID_PARMS = new String[] {"ID","PARMS","TICKS","TRIGGER","ONTRIGGER","IMEANIT","MESSAGE","INVOKE"};

	protected void reportError(final String msg)
	{
		final String whom;
		if(affected != null)
			whom=" on "+affected.name()+" @"+CMLib.map().getApproximateExtendedRoomID(CMLib.map().roomLocation(affected));
		else
			whom="";
		Log.errOut(msg+" on "+ID()+whom);
	}

	public void addTriggeredAffect(final String txt)
	{
		final Map<String,String> values = CMParms.parseEQParms(txt, VALID_PARMS);
		final String id = values.get("ID");
		final String parms = values.containsKey("PARMS")?values.get("PARMS"):"";
		final boolean imeanit=values.containsKey("IMEANIT")?CMath.s_bool(values.get("IMEANIT")):false;
		final boolean invoke=values.containsKey("INVOKE")?CMath.s_bool(values.get("INVOKE")):false;
		final String tickstr = values.get("TICKS");
		final String trigger = values.get("TRIGGER");
		final String ontrigger = values.get("ONTRIGGER");
		final String message = values.get("MESSAGE");
		if((id==null)||(id.trim().length()==0))
		{
			reportError("Missing ID in '"+txt+"'");
			return;
		}
		if((tickstr==null)||(tickstr.trim().length()==0)||(CMath.s_int(tickstr)<=0))
		{
			reportError("Missing or bad TICKS in '"+txt+"'");
			return;
		}
		if((trigger==null)||(trigger.trim().length()==0))
		{
			reportError("Missing TRIGGER in '"+txt+"'");
			return;
		}
		if((ontrigger==null)||(ontrigger.trim().length()==0)
		||((!ontrigger.equalsIgnoreCase("ADD"))&&(!ontrigger.equalsIgnoreCase("REMOVE"))))
		{
			reportError("Missing or incorrect ONTRIGGER (ADD/REMOVE) in '"+txt+"'");
			return;
		}

		final short onTrigger = ontrigger.equalsIgnoreCase("ADD") ? (short)1 : (short)-1;
		final Triggerer trig = (Triggerer)CMClass.getCommon("DefaultTriggerer");
		final List<String> errors = new ArrayList<String>();
		trig.addTrigger(id, trigger, new HashMap<String,List<Social>>(), errors);
		if(errors.size()>0)
		{
			reportError("Bad trigger in '"+txt+"' ("+errors.get(0)+")");
			return;
		}

		if(id.equalsIgnoreCase("AMBIANCE"))
		{
			if(parms.length()>0)
			{
				final AmbianceAdder A=new AmbianceAdder();
				A.setMiscText(parms);
				final TriggeredAffect ta = new TriggeredAffect(A,onTrigger,trigger,CMath.s_int(tickstr),invoke);
				ta.message=message;
				if(onTrigger>0)
					unAffectAffected(ta);
				else
					reAffectAffected(ta);
			}
		}
		else
		{
			Object A=CMClass.getAbility(id);
			if(A == null)
				A=CMClass.getBehavior(id);
			if(A == null)
				A = CMath.s_valueOf(Attrib.class, id.toUpperCase().trim());
			if(A == null)
				A=CMClass.findAbility(id);
			if(A == null)
				A=CMClass.findBehavior(id);
			if(A != null)
			{
				final TriggeredAffect ta;
				if((A instanceof Attrib) && (affected instanceof MOB))
				{
					final Attrib attrib = (Attrib)A;
					final MOB M = (MOB)affected;
					final boolean mset = M.isAttributeSet(attrib);
					boolean set = mset;
					if((parms.length()>0) && CMath.isBool(parms))
						set = CMath.s_bool(parms);
					else
						set = !mset;
					ta = new TriggeredAffect(A,onTrigger,trigger,CMath.s_int(tickstr),invoke);
					ta.attribParms = new int[] {set?0:1,mset?0:1};
					M.setAttribute(attrib, set);
				}
				else
				{
					ta=new TriggeredAffect(A,onTrigger,trigger,CMath.s_int(tickstr),invoke);
					if(A instanceof Ability)
						((Ability)A).setMiscText(parms);
					if((A instanceof Behavior)
					&& ((affected instanceof PhysicalAgent)||imeanit))
						((Behavior)A).setParms(parms);
				}
				ta.message=message;
				if(affected == null)
					unAffectAffected(ta);
				else
				if(onTrigger>0)
					unAffectAffected(ta);
				else
					reAffectAffected(ta);
			}
		}
	}

	@Override
	public synchronized void setMiscText(String txt)
	{
		final Physical affected = super.affected;
		if((affected == null)||(txt.length()==0))
		{
			super.setMiscText(txt);
			return;
		}
		txt=txt.trim();
		if(txt.startsWith("-"))
		{
			unInvokeByName(txt.substring(1).toLowerCase().trim());
			return;
		}
		if(txt.startsWith("+"))
			txt=txt.substring(1).trim();
		else
			this.eraseSelf();
		if(txt.length()>0)
		{
			super.setMiscText(txt);
			if(txt.startsWith("("))
			{
				int depth = 1;
				int start = 1;
				for(int i=1;i<txt.length();i++)
				{
					if(txt.charAt(i)=='(')
					{
						if(depth == 0)
							start=i+1;
						depth++;
					}
					else
					if(txt.charAt(i)==')')
					{
						depth=(depth<=0)?0:depth-1;
						if(depth == 0)
							addTriggeredAffect(txt.substring(start,i));
					}
				}
				if(depth>0)
					addTriggeredAffect(txt.substring(start,txt.length()));
			}
			else
				addTriggeredAffect(txt);
		}
	}


	@Override
	public void destroy()
	{
		super.destroy();
		CMLib.threads().deleteTick(this,-1);
	}

	@Override
	public void setAffectedOne(final Physical P)
	{
		super.setAffectedOne(P);
		if(P == null)
			destroy();
		else
		if(text().trim().length()>0)
			this.setMiscText(text());
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		for(int p=0;p<affects.size();p++)
		{
			final TriggeredAffect P = affects.getSafe(p);
			if((P!=null)&&(P.first instanceof MsgListener))
			{
				if(!((MsgListener)P.first).okMessage(myHost, msg))
					return false;
			}
		}
		return true;
	}

	protected boolean isTriggered(final TriggeredAffect p, final CMMsg msg)
	{
		final MOB src=msg.source();
		final MOB fixM = getFixMob();
		boolean ret = false;
		synchronized(fixM)
		{
			try
			{
				fixM.setLocation(src.location());
				msg.setSource(fixM);
				final Object[] whichTracking = p.trigger.whichTracking(msg);
				if(whichTracking.length>0)
				{
					final Triad<MOB,Object,List<String>> comps = p.trigger.getCompleted(fixM, whichTracking, msg);
					if((comps != null)&&(comps.first!=null))
						ret = true;
				}
			}
			finally
			{
				msg.setSource(src);
			}
		}
		if(ret)
		{
			if((p.message!=null)
			&&(p.message.length()>0)
			&&(src.location()!=null))
				src.location().show(src, null, CMMsg.MSG_OK_VISUAL,p.message);
			if(!CMLib.threads().isTicking(this, Tickable.TICKID_PROPERTY_SPECIAL))
				CMLib.threads().startTickDown(this, Tickable.TICKID_PROPERTY_SPECIAL, 1);
		}
		return ret;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		for(int i=affects.size()-1;i>=0;i--)
		{
			final TriggeredAffect P = affects.getSafe(i);
			if((P!=null)
			&&(P.onTrigger==-1) // means remove on trigger
			&&(this.isTriggered(P, msg)))
				this.unAffectAffected(P);
		}
		for(int p=0;p<affects.size();p++)
		{
			final TriggeredAffect P = affects.getSafe(p);
			if((P!=null)&&(P.first instanceof MsgListener))
			{
				((MsgListener)P.first).executeMsg(myHost, msg);
			}
		}
		for(int i=naffects.size()-1;i>=0;i--)
		{
			final TriggeredAffect P = naffects.getSafe(i);
			if((P!=null)
			&&(P.onTrigger==1) // means add on trigger
			&&(this.isTriggered(P, msg)))
				this.reAffectAffected(P);
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(tickID == Tickable.TICKID_PROPERTY_SPECIAL)
		{
			boolean tickMattered = false;
			for(int p=naffects.size()-1;p>=0;p--)
			{
				final TriggeredAffect P=naffects.getSafe(p);
				if(P == null)
					continue;
				switch(P.onTrigger)
				{
				case 1: // wait for your trigger
					break;
				case -1:
					if(--P.tickDown<=0)
						reAffectAffected(P);
					else
						tickMattered=true;
					break;
				}
			}
			for(int p=affects.size()-1;p>=0;p--)
			{
				final TriggeredAffect P=affects.getSafe(p);
				if(P == null)
					continue;
				if(P.onTrigger==1)
				{
					if(--P.tickDown<=0)
						unAffectAffected(P);
					else
						tickMattered=true;
				}
			}
			return tickMattered;
		}
		else
		{
			for(int p=0;p<affects.size();p++)
			{
				final TriggeredAffect P = affects.getSafe(p);
				if((P!=null)&&(P.first instanceof Tickable))
				{
					final Tickable ticker = (ticking == this)?affected:ticking;
					((Tickable)P.first).tick(ticker, tickID);
				}
			}
		}
		if(affected instanceof MOB)
		{
			for(int p=0;p<affects.size();p++)
			{
				final TriggeredAffect P = affects.getSafe(p);
				if((P!=null)
				&&(P.first instanceof Attrib)
				&&(P.attribParms.length>0))
					((MOB)affected).setAttribute((Attrib)P.first, P.attribParms[0]==0?true:false);
			}
		}
		return true;
	}
}
