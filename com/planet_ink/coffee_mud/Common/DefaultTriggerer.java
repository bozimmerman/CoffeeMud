package com.planet_ink.coffee_mud.Common;
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
import com.planet_ink.coffee_mud.MOBS.interfaces.Deity.RitualType;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
import java.lang.ref.*;

/*
   Copyright 2022-2022 Bo Zimmerman

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
public class DefaultTriggerer implements Triggerer
{
	protected final static Map<String,Trigger[]> ritualCache = new Hashtable<String,Trigger[]>();

	protected Map<String, TrigTracker>	trackers	= new Hashtable<String, TrigTracker>();
	protected Map<Object, Trigger[]>	rituals		= new SHashtable<Object, Trigger[]>();
	protected List<TrigState>			waitingFor	= Collections.synchronizedList(new LinkedList<TrigState>());
	protected Set<String>				ignoreOf	= new LimitedTreeSet<String>();
	protected String					holyName	= "Unknown";
	protected int						version		= 0;
	protected Map<String,List<Social>>	socialsMap	= new Hashtable<String,List<Social>>();

	private final static Object[]	trackingNothing	= new Object[0];
	private final static MOB[]		trackingNoone	= new MOB[0];

	public DefaultTriggerer()
	{
		version = TrigSignal.sig;
	}

	@Override
	public String ID()
	{
		return "DefaultTriggerer";
	}

	@Override
	public CMObject newInstance()
	{
		return new DefaultTriggerer();
	}

	@Override
	public String name()
	{
		return holyName;
	}

	@Override
	public Triggerer setName(final String name)
	{
		this.holyName = name;
		return this;
	}

	@Override
	public boolean isObsolete()
	{
		return (version != TrigSignal.sig);
	}

	@Override
	public void setObsolete()
	{
		version = -1;
	}

	@Override
	public boolean isDisabled()
	{
		return rituals.size()==0;
	}

	/**
	 * Separator enum constants for ritual definitions.
	 * @author Bo Zimmerman
	 *
	 */
	private enum TriggConnector
	{
		AND,
		OR
	}

	protected static class Trigger
	{
		public TriggerCode	triggerCode	= TriggerCode.SAY;
		public String		parm1		= null;
		public String		parm2		= null;
		public Social		soc			= null;
		public Social		socT		= null;
		public int			cmmsgCode	= -1;
		public Trigger		orConnect	= null;
		public boolean		addArgs		= false;
	}

	protected final class TrigTracker
	{
		private final Map<Object, TrigState>states	= new LimitedTreeMap<Object, TrigState>(120000,100,false);
		private final Set<Object>			compl	= new LimitedTreeSet<Object>(CMProps.getTickMillis()*2,10,false);
		private final Reference<MOB>		charM;

		public TrigTracker(final MOB mob)
		{
			this.charM = new WeakReference<MOB>(mob);

		}

		public TrigState getState(final Object key)
		{
			final MOB mob=charM.get();
			if(mob==null)
			{
				states.clear();
				return null;
			}
			if(states.containsKey(key))
				return states.get(key);
			return null;
		}

		public TrigState getCreateState(final Object key)
		{
			final MOB mob=charM.get();
			TrigState state = getState(key);
			if((state != null)||(mob==null))
				return state;
			state = new TrigState(mob, key, holyName);
			states.put(key, state);
			return state;
		}
	}

	protected class TrigState
	{
		private volatile int			completed	= -1;
		private final String			holyName;
		private final Reference<MOB>	charM;
		private volatile long			time		= System.currentTimeMillis();
		public volatile long			waitExpire	= -1;
		public final Object				key;
		public List<String>				args		= null;

		public TrigState(final MOB charM, final Object key, final String holyName)
		{
			this.charM = new WeakReference<MOB>(charM);
			this.key=key;
			this.holyName = holyName;
		}

		public List<String> args()
		{
			if(args==null)
				args=new Vector<String>(1);
			return args;
		}

		public synchronized void setCompleted()
		{
			completed++;
			time=System.currentTimeMillis();
		}

		public void setIgnore(final boolean truefalse)
		{
			synchronized(ignoreOf)
			{
				final MOB charM=this.charM.get();
				if(charM != null)
				{
					if(truefalse)
						ignoreOf.add(charM.Name());
					else
						ignoreOf.remove(charM.Name());
				}
			}
		}

		public void setWait(final long expiration)
		{
			synchronized(waitingFor)
			{
				if(charM.get() != null)
				{
					waitExpire=expiration;
					if(expiration<0)
						waitingFor.remove(this);
					else
						waitingFor.add(this);
				}
			}
		}
	}

	protected boolean isIgnoring(final MOB mob)
	{
		synchronized(ignoreOf)
		{
			return ignoreOf.contains(mob.Name());
		}
	}

	@Override
	public void addTrigger(final Object key, String trigger, final Map<String, List<Social>> socials, final List<String> errors)
	{
		trigger=trigger.toUpperCase().trim();
		if(DefaultTriggerer.ritualCache.containsKey(trigger))
		{
			rituals.put(key, DefaultTriggerer.ritualCache.get(trigger));
			return;
		}
		TriggConnector previousConnector=TriggConnector.AND;
		if(trigger.equals("-"))
		{
			DefaultTriggerer.ritualCache.put(trigger, new Trigger[0]);
			rituals.put(key, new Trigger[0]);
			return;
		}

		final List<Trigger> putHere = new ArrayList<Trigger>();
		Trigger prevDT=null;
		while(trigger.length()>0)
		{
			final int div1=trigger.indexOf('&');
			final int div2=trigger.indexOf('|');
			int div=div1;

			if((div2>=0)&&((div<0)||(div2<div)))
				div=div2;
			String trig=null;
			if(div<0)
			{
				trig=trigger;
				trigger="";
			}
			else
			{
				trig=trigger.substring(0,div).trim();
				trigger=trigger.substring(div+1);
			}
			if(trig.length()>0)
			{
				final Vector<String> V=CMParms.parse(trig);
				if(V.size()>1)
				{
					Trigger DT=new Trigger();
					final String cmd=V.firstElement();
					TriggerCode T;
					if(cmd.endsWith("+"))
					{
						DT.addArgs=true;
						T=(TriggerCode)CMath.s_valueOf(TriggerCode.class, cmd.substring(0,cmd.length()-1));
					}
					else
						T = (TriggerCode)CMath.s_valueOf(TriggerCode.class, cmd);
					if(T==null)
					{
						for(final TriggerCode RT : TriggerCode.values())
						{
							if(RT.name().startsWith(cmd))
							{
								T=RT;
								break;
							}
						}
					}
					if(T==null)
					{
						if(errors!=null)
							errors.add("Illegal trigger: '"+cmd+"','"+trig+"'");
						DT=null;
						break;
					}
					else
					{
						DT.cmmsgCode=this.getCMMsgCode(T);
						switch(T)
						{
						case SAY:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case TIME:
						{
							DT.triggerCode=T;
							DT.parm1=""+CMath.s_int(CMParms.combine(V,1));
							break;
						}
						case WAIT:
						{
							DT.triggerCode=T;
							DT.parm1=""+CMath.s_int(CMParms.combine(V,1));
							break;
						}
						case YOUSAY:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case OTHERSAY:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case ALLSAY:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case PUTTHING:
						{
							DT.triggerCode=T;
							if(V.size()<3)
							{
								Log.errOut(name(),"Illegal trigger: "+trig);
								DT=null;
								break;
							}
							DT.parm1=CMParms.combine(V,1,V.size()-2);
							DT.parm2=V.lastElement();
							break;
						}
						case SOCIAL:
						{
							DT.triggerCode=T;
							if(V.size()<2)
							{
								Log.errOut(name(),"Illegal trigger: "+trig);
								DT=null;
								break;
							}
							DT.parm1=V.get(1).toUpperCase().trim();
							if(V.size()>2)
								DT.parm2=V.get(2).toUpperCase().trim();
							else
								DT.parm2="";
							Social soc = null;
							if(DT.parm2.equals("*"))
							{
								List<Social> lst = socials.get(DT.parm1);
								if((lst == null)||(lst.size()==0))
									lst = CMLib.socials().getSocialsSet(DT.parm1);
								if((lst != null)&&(lst.size()>0))
								{
									for(final Social S : lst)
									{
										if(S.name().equalsIgnoreCase(S.baseName()))
											soc = S;
										else
										if(S.targetName().equalsIgnoreCase("<T-NAME>"))
											DT.socT = S;
									}
									if(soc == null)
										soc = lst.get(0);
								}
							}
							if(soc == null)
							{
								soc = CMLib.socials().fetchSocialFromSet(socials,CMParms.parse((DT.parm1+" "+DT.parm2).trim()),true,true);
								if(soc == null)
									soc = CMLib.socials().fetchSocial((DT.parm1+" "+DT.parm2).trim(),true);
							}
							if(soc == null)
							{
								if(DT.parm2.length()>0)
								{
									soc = CMLib.socials().fetchSocialFromSet(socials,new XArrayList<String>(DT.parm1,"<T-NAME>",DT.parm2),true,true);
									if(soc == null)
										soc = CMLib.socials().fetchSocial(DT.parm1+" <T-NAME> "+DT.parm2,true);
								}
								if(soc == null)
								{
									Log.errOut(name(),"Illegal social in: "+trig);
									DT=null;
									break;
								}
							}
							if(socials.containsKey(soc.baseName()))
								socialsMap.put(soc.baseName(), socials.get(soc.baseName()));
							DT.soc = soc;
							break;
						}
						case BURNTHING:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case PUTVALUE:
						{
							DT.triggerCode=T;
							if(V.size()<3)
							{
								if(errors!=null)
									errors.add("Illegal trigger: "+trig);
								DT=null;
								break;
							}
							DT.parm1=""+CMath.s_int(V.elementAt(1));
							DT.parm2=CMParms.combine(V,2);
							break;
						}
						case BURNVALUE:
						{
							DT.triggerCode=T;
							if(V.size()<3)
							{
								if(errors!=null)
									errors.add("Illegal trigger: "+trig);
								DT=null;
								break;
							}
							DT.parm1=""+CMath.s_int(CMParms.combine(V,1));
							break;
						}
						case BURNMATERIAL:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							final int cd = RawMaterial.CODES.FIND_StartsWith(DT.parm1);
							boolean found=cd>=0;
							if(found)
								DT.parm1=""+cd;
							else
							{
								final RawMaterial.Material m=RawMaterial.Material.startsWith(DT.parm1);
								if(m!=null)
								{
									DT.parm1=""+m.mask();
									found=true;
								}
							}
							if(!found)
							{
								if(errors!=null)
									errors.add("Unknown material: "+trig);
								DT=null;
								break;
							}
							break;
						}
						case PUTMATERIAL:
						{
							DT.triggerCode=T;
							if(V.size()<3)
							{
								if(errors!=null)
									errors.add("Illegal trigger: "+trig);
								DT=null;
								break;
							}
							DT.parm1=V.elementAt(1);
							DT.parm2=CMParms.combine(V,2);
							final int cd = RawMaterial.CODES.FIND_StartsWith(DT.parm1);
							boolean found=cd>=0;
							if(found)
								DT.parm1=""+cd;
							else
							if(!found)
							{
								final RawMaterial.Material m=RawMaterial.Material.startsWith(DT.parm1);
								if(m!=null)
								{
									DT.parm1=""+m.mask();
									found=true;
								}
							}
							if(!found)
							{
								if(errors!=null)
									errors.add("Unknown material: "+trig);
								DT=null;
								break;
							}
							break;
						}
						case EAT:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case READING:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case RANDOM:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case CHECK:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case DRINK:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case INROOM:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case RIDING:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case CAST:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							if(CMClass.findAbility(DT.parm1)==null)
							{
								if(errors!=null)
									errors.add("Illegal SPELL in: "+trig);
								DT=null;
								break;
							}
							break;
						}
						case EMOTE:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case SITTING:
						{
							DT.triggerCode=T;
							break;
						}
						case STANDING:
						{
							DT.triggerCode=T;
							break;
						}
						case SLEEPING:
						{
							DT.triggerCode=T;
							break;
						}
						default:
						{
							if(errors!=null)
								errors.add("Illegal trigger: '"+cmd+"','"+trig+"'");
							DT=null;
							break;
						}
						}
					}
					if(DT==null)
						return;
					if(previousConnector==TriggConnector.AND)
						putHere.add(DT);
					else
					if(prevDT!=null)
						prevDT.orConnect=DT;
					if(div==div1)
						previousConnector=TriggConnector.AND;
					else
						previousConnector=TriggConnector.OR;
					prevDT=DT;
				}
				else
				{
					if(errors!=null)
						errors.add("Illegal trigger (need more parameters): "+trig);
					return;
				}
			}
		}
		// check for valid starter
		if(putHere.size()>0)
		{
			int firstActiveCode=-1;
			for(int i=0;i<putHere.size();i++)
			{
				Trigger r = putHere.get(i);
				boolean active=false;
				while(r != null)
				{
					active = active || (r.cmmsgCode>0);
					r=r.orConnect;
				}
				if(active)
				{
					firstActiveCode = i;
					break;
				}
			}
			if(firstActiveCode > 0)
			{
				final Trigger gone = putHere.remove(firstActiveCode);
				putHere.add(0, gone);
			}
		}
		final Trigger[] finalTriggs = putHere.toArray(new Trigger[putHere.size()]);
		DefaultTriggerer.ritualCache.put(trigger, finalTriggs);
		rituals.put(key, finalTriggs);
	}

	protected TrigTracker getTrigTracker(final MOB mob)
	{
		synchronized(trackers)
		{
			if(trackers.containsKey(mob.Name()))
			{
				final TrigTracker tracker = trackers.get(mob.Name());
				if((tracker.charM.get()!=null)
				&&((tracker.states.size()>0)||(tracker.compl.size()>0)))
					return tracker;
				trackers.remove(mob.Name());
			}
		}
		return null;
	}

	protected TrigTracker getCreateTrigTracker(final MOB mob)
	{
		TrigTracker tracker = getTrigTracker(mob);
		if(tracker != null)
			return tracker;
		tracker = new TrigTracker(mob);
		synchronized(trackers)
		{
			trackers.put(mob.Name(), tracker);
		}
		return tracker;
	}

	protected TrigState getCreateTrigState(final MOB mob, final Object key)
	{
		final TrigTracker tracker = getCreateTrigTracker(mob);
		final TrigState state = tracker.getCreateState(key);
		return state;
	}

	protected TrigState getTrigState(final MOB mob, final Object key)
	{
		final TrigTracker tracker = getTrigTracker(mob);
		if(tracker == null)
			return null;
		return tracker.getState(key);
	}

	protected void clearState(final MOB mob, final Object type)
	{
		final TrigTracker tracker = getTrigTracker(mob);
		if(tracker != null)
			tracker.states.remove(type);
	}

	protected String L(final String str, final String ... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

	@Override
	public String getTriggerDesc(final Object key)
	{
		final Trigger[] triggers = rituals.get(key);
		if((triggers==null)||(triggers.length==0))
			return L("Never");
		final StringBuffer buf=new StringBuffer("");
		for(int v=0;v<triggers.length;v++)
		{
			Trigger DT=triggers[v];
			while(DT != null)
			{
				if(v>0)
					buf.append(", "+((DT==triggers[v])?L("and "):L("or ")));
				switch(DT.triggerCode)
				{
				case SAY:
					buf.append(L("the player should say '@x1'",DT.parm1.toLowerCase()));
					break;
				case READING:
					if(DT.parm1.equals("0"))
						buf.append(L("the player should read something"));
					else
						buf.append(L("the player should read '@x1'",DT.parm1.toLowerCase()));
					break;
				case SOCIAL:
				{
					if(DT.soc == null)
						buf.append(L("the player should do the impossible"));
					else
					if(DT.parm2.equals("*"))
						buf.append(L("the player should @x1",DT.soc.baseName().toLowerCase()));
					else
						buf.append(L("the player should @x1",(DT.soc.baseName().toLowerCase()+" "+DT.soc.getTargetDesc()).trim()));
					break;
				}
				case TIME:
					buf.append(L("the hour of the day is @x1",DT.parm1.toLowerCase()));
					break;
				case PUTTHING:
					buf.append(L("the player should put @x1 in @x2",DT.parm1.toLowerCase(),DT.parm2.toLowerCase()));
					break;
				case BURNTHING:
					buf.append(L("the player should burn @x1",DT.parm1.toLowerCase()));
					break;
				case DRINK:
					buf.append(L("the player should drink @x1",DT.parm1.toLowerCase()));
					break;
				case EAT:
					buf.append(L("the player should eat @x1",DT.parm1.toLowerCase()));
					break;
				case INROOM:
					{
					if(DT.parm1.equalsIgnoreCase("holy")
					||DT.parm1.equalsIgnoreCase("unholy")
					||DT.parm1.equalsIgnoreCase("balance"))
						buf.append(L("the player should be in the deities room of infused @x1-ness.",DT.parm1.toLowerCase()));
					else
					{
						final Room R=CMLib.map().getRoom(DT.parm1);
						if(R==null)
							buf.append(L("the player should be in some unknown place"));
						else
							buf.append(L("the player should be in '@x1'",R.displayText(null)));
					}
					}
					break;
				case RIDING:
					buf.append(L("the player should be on @x1",DT.parm1.toLowerCase()));
					break;
				case CAST:
					{
					final Ability A=CMClass.findAbility(DT.parm1);
					if(A==null)
						buf.append(L("the player should cast '@x1'",DT.parm1));
					else
						buf.append(L("the player should cast '@x1'",A.name()));
					}
					break;
				case EMOTE:
					buf.append(L("the player should emote '@x1'",DT.parm1.toLowerCase()));
					break;
				case RANDOM:
					buf.append(DT.parm1+"% of the time");
					break;
				case WAIT:
					buf.append(L("wait @x1 seconds",""+((CMath.s_int(DT.parm1)*CMProps.getTickMillis())/1000)));
					break;
				case YOUSAY:
					buf.append(L("then you will automatically say '@x1'",DT.parm1.toLowerCase()));
					break;
				case OTHERSAY:
					buf.append(L("then all others will say '@x1'",DT.parm1.toLowerCase()));
					break;
				case ALLSAY:
					buf.append(L("then all will say '@x1'",DT.parm1.toLowerCase()));
					break;
				case CHECK:
					buf.append(CMLib.masking().maskDesc(DT.parm1));
					break;
				case PUTVALUE:
					buf.append(L("the player should put an item worth at least @x1 in @x2",DT.parm1.toLowerCase(),DT.parm2.toLowerCase()));
					break;
				case PUTMATERIAL:
					{
						String material="something";
						final int t=CMath.s_int(DT.parm1);
						RawMaterial.Material m;
						if(((t&RawMaterial.RESOURCE_MASK)==0)
						&&((m=RawMaterial.Material.findByMask(t))!=null))
							material=m.desc().toLowerCase();
						else
						if(RawMaterial.CODES.IS_VALID(t))
							material=RawMaterial.CODES.NAME(t).toLowerCase();
						buf.append(L("the player puts an item made of @x1 in @x2",material,DT.parm2.toLowerCase()));
					}
					break;
				case BURNMATERIAL:
					{
						String material="something";
						final int t=CMath.s_int(DT.parm1);
						RawMaterial.Material m;
						if(((t&RawMaterial.RESOURCE_MASK)==0)
						&&((m=RawMaterial.Material.findByMask(t))!=null))
							material=m.desc().toLowerCase();
						else
						if(RawMaterial.CODES.IS_VALID(t))
							material=RawMaterial.CODES.NAME(t).toLowerCase();
						buf.append(L("the player should burn an item made of @x1",material));
					}
					break;
				case BURNVALUE:
					buf.append(L("the player should burn an item worth at least @x1",DT.parm1.toLowerCase()));
					break;
				case SITTING:
					buf.append(L("the player should sit down"));
					break;
				case STANDING:
					buf.append(L("the player should stand up"));
					break;
				case SLEEPING:
					buf.append(L("the player should go to sleep"));
					break;
				}
				DT=DT.orConnect;
			}
		}
		return buf.toString();
	}

	@Override
	public void setIgnoreTracking(final MOB mob, final boolean truefalse)
	{
		synchronized(ignoreOf)
		{
			if(truefalse)
				ignoreOf.add(mob.Name());
			else
				ignoreOf.remove(mob.Name());
		}
	}

	@Override
	public void deleteTracking(final MOB mob, final Object key)
	{
		this.clearState(mob, key);
	}

	@Override
	public CMMsg genNextAbleTrigger(final MOB mob, final Object key, final boolean force)
	{
		if((mob == null)||(mob.amDead()))
			return null;
		final Trigger[] triggers = rituals.get(key);
		if((triggers==null)||(triggers.length==0))
			return null;
		final TrigTracker tracker = this.getCreateTrigTracker(mob);
		if(tracker == null)
			return null;
		final TrigState trigState;
		if(force)
			trigState = tracker.getCreateState(key);
		else
			trigState = tracker.getState(key);
		if(trigState==null)
			return null;
		final int completed =trigState.completed;
		if(completed>=triggers.length)
			return null;
		final Trigger DT=triggers[completed+1];
		// in an OR-condition, we always just do the first one....
		switch(DT.triggerCode)
		{
		case SAY:
			return CMClass.getMsg(mob, CMMsg.MASK_ALWAYS|CMMsg.MSG_SPEAK, L("^T<S-NAME> say(s) '@x1'.^N",DT.parm1));
		case TIME:
			trigState.setCompleted();
			return null;
		case RANDOM:
			trigState.setCompleted();
			return null;
		case YOUSAY:
			return null;
		case ALLSAY:
			return null;
		case OTHERSAY:
			return null;
		case WAIT:
		{
			final long waitDuration=CMath.s_long(DT.parm1)*CMProps.getTickMillis();
			if(System.currentTimeMillis()>(trigState.time+waitDuration))
				return CMClass.getMsg(mob, CMMsg.MSG_OK_ACTION, null); // force the wait to be evaluated
			return null;
		}
		case CHECK:
			trigState.setCompleted();
			return null;
		case PUTTHING:
		{
			final Item I=CMClass.getBasicItem("GenItem");
			final Item cI=CMClass.getBasicItem("GenContainer");
			if(DT.parm1.equals("0"))
				I.setName(L("Something"));
			else
				I.setName(DT.parm1);
			if(DT.parm1.equals("0"))
				cI.setName(L("Something"));
			else
				cI.setName(DT.parm2);
			return CMClass.getMsg(mob, cI, I, CMMsg.MASK_ALWAYS|CMMsg.MSG_PUT, L("<S-NAME> put(s) <O-NAME> into <T-NAME>."));
		}
		case BURNTHING:
		{
			final Item I=CMClass.getBasicItem("GenItem");
			if(DT.parm1.equals("0"))
				I.setName(L("Something"));
			else
				I.setName(DT.parm1);
			return CMClass.getMsg(mob, I, null, CMMsg.MASK_ALWAYS|CMMsg.MASK_MOVE|DT.cmmsgCode, L("<S-NAME> burn(s) <T-NAME>."));
		}
		case READING:
		{
			final Item I=CMClass.getBasicItem("GenItem");
			if(DT.parm1.equals("0"))
				I.setName(L("Something"));
			else
				I.setName(DT.parm1);
			return CMClass.getMsg(mob, I, null, CMMsg.MASK_ALWAYS|CMMsg.MSG_READ, L("<S-NAME> read(s) <T-NAME>."));
		}
		case SOCIAL:
		{
			Social soc;
			if(DT.parm2.equals("*"))
			{
				soc = (mob.getVictim()!=null) ? DT.socT : null;
				if(soc == null)
					soc = DT.soc;
			}
			else
				soc = DT.soc;
			if(soc != null)
			{
				final MOB target;
				if(soc.targetName().equals("<T-NAME>"))
				{
					target=mob.getVictim();
					if(target==null)
						return CMClass.getMsg(mob,target,soc,CMMsg.MSG_OK_VISUAL,soc.getFailedTargetMessage(), CMMsg.NO_EFFECT, null, CMMsg.NO_EFFECT, null);
					return CMClass.getMsg(mob,target,soc,CMMsg.MSG_OK_VISUAL,soc.getSourceMessage(),soc.getTargetMessage(),soc.getOthersMessage());
				}
				else
					return CMClass.getMsg(mob,null,soc,CMMsg.MSG_OK_VISUAL,soc.getSourceMessage(),soc.getTargetMessage(),soc.getOthersMessage());
			}
			break;
		}
		case DRINK:
		{
			final Item I=CMClass.getBasicItem("GenItem");
			if(DT.parm1.equals("0"))
				I.setName(L("Something"));
			else
				I.setName(DT.parm1);
			return CMClass.getMsg(mob, I, null, CMMsg.MASK_ALWAYS|CMMsg.MSG_DRINK, L("<S-NAME> drink(s) <T-NAME>."));
		}
		case EAT:
		{
			final Item I=CMClass.getBasicItem("GenItem");
			if(DT.parm1.equals("0"))
				I.setName(L("Something"));
			else
				I.setName(DT.parm1);
			return CMClass.getMsg(mob, I, null, DT.cmmsgCode, L("<S-NAME> eat(s) <T-NAME>."));
		}
		case INROOM:
			trigState.setCompleted();
			return null;
		case RIDING:
			trigState.setCompleted();
			return null;
		case CAST:
		{
			final Ability A=CMClass.getAbility(DT.parm1);
			if(A!=null)
				return CMClass.getMsg(mob, null, A, DT.cmmsgCode, L("<S-NAME> do(es) '@x1'",A.name()));
			return null;
		}
		case EMOTE:
			return CMClass.getMsg(mob, null, null, DT.cmmsgCode, L("<S-NAME> do(es) '@x1'",DT.parm1));
		case PUTVALUE:
		{
			final Item cI=CMClass.getBasicItem("GenContainer");
			if(DT.parm2.equals("0"))
				cI.setName(L("Something"));
			else
				cI.setName(DT.parm2);
			final Item I=CMClass.getBasicItem("GenItem");
			I.setName(L("valuables"));
			I.setBaseValue(CMath.s_int(DT.parm1));
			return CMClass.getMsg(mob, cI, I, CMMsg.MASK_ALWAYS|CMMsg.MSG_PUT, L("<S-NAME> put(s) <O-NAME> in <T-NAME>."));
		}
		case PUTMATERIAL:
		case BURNMATERIAL:
		{
			final Item cI=CMClass.getBasicItem("GenContainer");
			if(DT.parm2.equals("0"))
				cI.setName(L("Something"));
			else
				cI.setName(DT.parm2);
			final Item I=CMLib.materials().makeItemResource(CMath.s_int(DT.parm1));
			return CMClass.getMsg(mob, cI, I, CMMsg.MASK_ALWAYS|CMMsg.MASK_HANDS|DT.cmmsgCode, L("<S-NAME> put(s) <O-NAME> in <T-NAME>."));
		}
		case BURNVALUE:
		{
			final Item I=CMClass.getBasicItem("GenItem");
			I.setName(L("valuables"));
			I.setBaseValue(CMath.s_int(DT.parm1));
			return CMClass.getMsg(mob, I, null, CMMsg.MASK_ALWAYS|CMMsg.MASK_HANDS|DT.cmmsgCode, L("<S-NAME> burn(s) <T-NAME>."));
		}
		case SITTING:
			if(!CMLib.flags().isSitting(mob))
				return CMClass.getMsg(mob, CMMsg.MSG_SIT, L("<S-NAME> sit(s)."));
			return null;
		case STANDING:
			if(!CMLib.flags().isStanding(mob))
				return CMClass.getMsg(mob, CMMsg.MSG_STAND, L("<S-NAME> stand(s)."));
			return null;
		case SLEEPING:
			if(!CMLib.flags().isSleeping(mob))
				return CMClass.getMsg(mob, CMMsg.MSG_SLEEP, L("<S-NAME> sleep(s)."));
			return null;
		}
		return null;
	}

	protected int getCMMsgCode(final TriggerCode trig)
	{
		switch(trig)
		{
		case SAY:
			return CMMsg.TYP_SPEAK;
		case PUTTHING:
			return CMMsg.TYP_PUT;
		case BURNMATERIAL:
			return CMMsg.TYP_FIRE;
		case BURNTHING:
			return CMMsg.TYP_FIRE;
		case EAT:
			return CMMsg.TYP_EAT;
		case DRINK:
			return CMMsg.TYP_DRINK;
		case CAST:
			return CMMsg.TYP_CAST_SPELL;
		case EMOTE:
			return CMMsg.TYP_EMOTE;
		case PUTVALUE:
			return CMMsg.TYP_PUT;
		case PUTMATERIAL:
			return CMMsg.TYP_PUT;
		case BURNVALUE:
			return CMMsg.TYP_FIRE;
		case READING:
			return CMMsg.TYP_READ;
		case SOCIAL:
			return CMMsg.MSG_OK_ACTION;
		case INROOM:
		case TIME:
		case RIDING:
		case SITTING:
		case STANDING:
		case SLEEPING:
		case RANDOM:
		case CHECK:
		case WAIT:
		case YOUSAY:
		case OTHERSAY:
		case ALLSAY:
			return -999;
		}
		return -999;
	}

	@Override
	public boolean isTracking(final MOB mob, final Object key)
	{
		final TrigTracker tracker = getTrigTracker(mob);
		if(tracker == null)
			return false;
		return tracker.states.containsKey(key);
	}

	@Override
	public boolean isTracking(final Object key, final CMMsg msg)
	{
		final MOB mob=msg.source();
		if(isIgnoring(mob))
			return false;
		final TrigTracker tracker = getTrigTracker(mob);
		final TrigState state = (tracker != null)?tracker.states.get(key):null;
		final int peekIndex = (state!=null)?(state.completed+1):0;
		final Trigger[] triggers = rituals.get(key);
		if(peekIndex >= triggers.length-1)
			return true;
		Trigger trig = triggers[peekIndex];
		while(trig != null)
		{
			if((trig.cmmsgCode<0)
			||(trig.cmmsgCode==msg.sourceMinor())
			||((msg.tool() instanceof Social)&&(trig.triggerCode==TriggerCode.SOCIAL)))
				return true;
			trig = trig.orConnect;
		}
		return false;
	}

	@Override
	public Object[] whichTracking(final CMMsg msg)
	{
		final MOB mob=msg.source();
		if(isIgnoring(mob))
			return trackingNothing;
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_ENTER:
		case CMMsg.TYP_LEAVE:
		case CMMsg.TYP_LOOK:
			return trackingNothing;
		default:
			break;
		}
		//TODO: THIS! This is what needs to be better.
		List<Object> readyList=null;
		for(final Object key : rituals.keySet())
		{
			if(isTracking(key, msg))
			{
				if(readyList == null)
					readyList = new ArrayList<Object>(1);
				readyList.add(key);
			}
		}
		if(readyList != null)
			return readyList.toArray();
		return trackingNothing;
	}

	protected String targName(final Environmental target)
	{
		if((target instanceof Item)||(target instanceof MOB))
		{
			final Room R=CMLib.map().roomLocation(target);
			if(R==null)
				return "$"+target.Name()+"$";
			if((target instanceof Item)
			&&(((Item)target).owner() !=R))
				return "$"+target.Name()+"$";
			return R.getContextName(target);
		}
		else
		if(target instanceof Room)
			return ((Room)target).displayText(null);
		else
			return target.Name();
	}

	public boolean containsString(final String toSrchStr, final String srchForStr)
	{
		if((srchForStr==null)||(srchForStr.length()==0)||(srchForStr.equals("0"))||(srchForStr.equals("*")))
			return true;
		return CMLib.english().containsString(toSrchStr, srchForStr);
	}

	protected TrigState stepGetCompleted(final Object key, final CMMsg msg)
	{
		if(isIgnoring(msg.source()))
			return null;
		final Trigger[] triggers=rituals.get(key);
		final TrigState state = getCreateTrigState(msg.source(), key);
		if((triggers == null)||(state==null))
			return null;
		if(state.completed>=triggers.length-1)
			return state;
		Trigger DT=triggers[state.completed+1];
		boolean yup = false;
		while((DT != null)&&(!yup))
		{
			if((msg.sourceMinor()==DT.cmmsgCode)
			||(DT.cmmsgCode==-999)
			||((DT.triggerCode==TriggerCode.SOCIAL)&&(msg.tool() instanceof Social)))
			{
				switch(DT.triggerCode)
				{
				case SAY:
					if((msg.sourceMessage()!=null)
					&&(msg.sourceMessage().toUpperCase().indexOf(DT.parm1)>0))
					{
						if(DT.addArgs)
						{
							String str = CMStrings.getSayFromMessage(msg.sourceMessage());
							final int x=str.toUpperCase().indexOf(DT.parm1);
							if(x>=0)
								str=str.substring(x+DT.parm1.length()).trim();
							state.args().addAll(CMParms.parse(str));
						}
						yup=true;
					}
					break;
				case TIME:
					if((msg.source().location()!=null)
					&&(msg.source().location().getArea().getTimeObj().getHourOfDay()==CMath.s_int(DT.parm1)))
					   yup=true;
					break;
				case RANDOM:
					if(CMLib.dice().rollPercentage()<=CMath.s_int(DT.parm1))
						yup=true;
					break;
				case YOUSAY:
					yup=true;
					try
					{
						if(DT.addArgs)
							state.args().addAll(CMParms.parse(DT.parm1));
						state.setIgnore(true);
						CMLib.commands().postSay(msg.source(),null,CMStrings.capitalizeAndLower(DT.parm1));
					}
					finally
					{
						state.setIgnore(false);
					}
					break;
				case ALLSAY:
				{
					final Room R=msg.source().location();
					if(R!=null)
					{
						if(DT.addArgs)
							state.args().addAll(CMParms.parse(DT.parm1));
						yup=true;
						for(int m=0;m<R.numInhabitants();m++)
						{
							final MOB M=R.fetchInhabitant(m);
							if(M!=null)
							{
								yup=true;
								try
								{
									state.setIgnore(true);
									CMLib.commands().postSay(M,null,CMStrings.capitalizeAndLower(DT.parm1));
								}
								finally
								{
									state.setIgnore(false);
								}
							}
						}
					}
					break;
				}
				case OTHERSAY:
				{
					final Room R=msg.source().location();
					if(R!=null)
					{
						if(DT.addArgs)
							state.args().addAll(CMParms.parse(DT.parm1));
						yup=true;
						for(int m=0;m<R.numInhabitants();m++)
						{
							final MOB M=R.fetchInhabitant(m);
							if((M!=null)&&(M!=msg.source()))
							{
								yup=true;
								try
								{
									state.setIgnore(true);
									CMLib.commands().postSay(M,null,CMStrings.capitalizeAndLower(DT.parm1));
								}
								finally
								{
									state.setIgnore(false);
								}
							}
						}
					}
					break;
				}
				case WAIT:
				{
					final long waitExpires=state.time+CMath.s_long(DT.parm1)*CMProps.getTickMillis();
					if(System.currentTimeMillis()>waitExpires)
					{
						yup=true;
						state.setWait(-1);
					}
					else
					{
						if(CMSecurity.isDebugging(CMSecurity.DbgFlag.RITUALS))
							Log.debugOut(msg.source().Name()+" still waiting ("+(state.completed+1)+"/"+triggers.length+") ");
						state.setWait(waitExpires);
						return null; // since we set the wait, there's no reason to look further
					}
					break;
				}
				case CHECK:
					if(CMLib.masking().maskCheck(DT.parm1,msg.source(),true))
					{
						if(DT.addArgs && (msg.target()!=null))
							state.args().add(targName(msg.target()));
						yup=true;
					}
					break;
				case PUTTHING:
					if((msg.target() instanceof Container)
					&&(msg.tool() instanceof Item)
					&&(containsString(msg.tool().name(),DT.parm1))
					&&(containsString(msg.target().name(),DT.parm2)))
					{
						if(DT.addArgs && (msg.target()!=null))
							state.args().add(targName(msg.target()));
						yup=true;
					}
					break;
				case BURNTHING:
				case READING:
				case DRINK:
				case EAT:
					if((msg.target()!=null)
					&&(DT.parm1.equals("0")||containsString(msg.target().name(),DT.parm1)))
					{
						if(DT.addArgs && (msg.target()!=null))
							state.args().add(targName(msg.target()));
						yup=true;
					}
					break;
				case SOCIAL:
					if((msg.tool() instanceof Social)
					&&(DT.soc != null)
					&&(((Social)msg.tool()).baseName().equals(DT.soc.baseName())))
					{
						if((msg.tool() == DT.soc)||(DT.parm2.equals("*")))
						{
							if(DT.addArgs && (msg.target()!=null))
								state.args().add(targName(msg.target()));
							yup=true;
						}
					}
					break;
				case INROOM:
					if(msg.source().location()!=null)
					{
						if(DT.parm1.equalsIgnoreCase("holy")
						||DT.parm1.equalsIgnoreCase("unholy")
						||DT.parm1.equalsIgnoreCase("balance"))
						{
							yup=(state.holyName!=null)
								&&(state.holyName.equalsIgnoreCase(CMLib.law().getClericInfused(msg.source().location())));
							if(yup)
							{
								if(DT.addArgs)
									state.args().add("here");
							}
						}
						else
						if(msg.source().location().roomID().equalsIgnoreCase(DT.parm1))
						{
							yup=true;
							if(DT.addArgs)
								state.args().add("here");
						}
					}
					break;
				case RIDING:
					if((msg.source().riding()!=null)
					&&(containsString(msg.source().riding().name(),DT.parm1)))
					{
						yup=true;
						if(DT.addArgs)
							state.args().add(targName(msg.source().riding()));
					}
					break;
				case CAST:
					if((msg.tool()!=null)
					&&((msg.tool().ID().equalsIgnoreCase(DT.parm1))
					||(containsString(msg.tool().name(),DT.parm1))))
					{
						yup=true;
						if(DT.addArgs && (msg.target()!=null))
							state.args().add(targName(msg.target()));
					}
					break;
				case EMOTE:
					if((msg.sourceMessage()!=null)&&(msg.sourceMessage().toUpperCase().indexOf(DT.parm1)>0))
					{
						yup=true;
						if(DT.addArgs)
						{
							final int x=msg.sourceMessage().indexOf(">");
							if(DT.addArgs)
							{
								state.args().add(CMStrings.removeColors(
										(x>0)?msg.sourceMessage().substring(x+1):msg.sourceMessage()));
							}
						}
					}
					break;
				case PUTVALUE:
					if((msg.tool() instanceof Item)
					&&(((Item)msg.tool()).baseGoldValue()>=CMath.s_int(DT.parm1))
					&&(msg.target() instanceof Container)
					&&(containsString(msg.target().name(),DT.parm2)))
					{
						yup=true;
						if(DT.addArgs && (msg.target()!=null))
							state.args().add(targName(msg.target()));
					}
					break;
				case PUTMATERIAL:
					if((msg.tool() instanceof Item)
					&&(((((Item)msg.tool()).material()&RawMaterial.RESOURCE_MASK)==CMath.s_int(DT.parm1))
						||((((Item)msg.tool()).material()&RawMaterial.MATERIAL_MASK)==CMath.s_int(DT.parm1)))
					&&(msg.target() instanceof Container)
					&&(containsString(msg.target().name(),DT.parm2)))
					{
						yup=true;
						if(DT.addArgs && (msg.target()!=null))
							state.args().add(targName(msg.target()));
					}
					break;
				case BURNMATERIAL:
					if((msg.target() instanceof Item)
					&&(((((Item)msg.target()).material()&RawMaterial.RESOURCE_MASK)==CMath.s_int(DT.parm1))
						||((((Item)msg.target()).material()&RawMaterial.MATERIAL_MASK)==CMath.s_int(DT.parm1))))
					{
						yup=true;
						if(DT.addArgs && (msg.target()!=null))
							state.args().add(targName(msg.target()));
					}
					break;
				case BURNVALUE:
					if((msg.target() instanceof Item)
					&&(((Item)msg.target()).baseGoldValue()>=CMath.s_int(DT.parm1)))
					{
						yup=true;
						if(DT.addArgs && (msg.target()!=null))
							state.args().add(targName(msg.target()));
					}
					break;
				case SITTING:
					yup=CMLib.flags().isSitting(msg.source());
					break;
				case STANDING:
					yup=(CMLib.flags().isStanding(msg.source()));
					break;
				case SLEEPING:
					yup=CMLib.flags().isSleeping(msg.source());
					break;
				}
			}
			if(yup)
			{
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.RITUALS))
					Log.debugOut(msg.source().Name()+" completed "+DT.triggerCode.name()+" ("+(state.completed+1)+"/"+triggers.length+") ");
				state.setCompleted();
				if(state.completed>=triggers.length-1)
				{
					final TrigTracker tracker = getTrigTracker(msg.source());
					if(tracker != null)
						tracker.compl.add(key);
					clearState(msg.source(),key);
					return state;
				}
				else
				{
					DT=triggers[state.completed+1];
					yup=false;
					// try this one now!
				}
			}
			else
				DT=DT.orConnect;
		}
		return null;
	}

	@Override
	public boolean isCompleted(final Object key, final CMMsg msg)
	{
		return stepGetCompleted(key, msg) != null;
	}

	@Override
	public Object[] whichCompleted(final Object[] keys, final CMMsg msg)
	{
		if(isIgnoring(msg.source()))
			return trackingNothing;
		List<Object> readyList=null;
		for(final Object key : keys)
		{
			if(isCompleted(key, msg))
			{
				if(readyList == null)
					readyList = new ArrayList<Object>(1);
				readyList.add(key);
			}
		}
		if(readyList != null)
			return readyList.toArray();
		return trackingNothing;
	}

	@Override
	public Pair<Object,List<String>> getCompleted(final Object[] keys, final CMMsg msg)
	{
		if(isIgnoring(msg.source()))
			return null;
		for(final Object key : keys)
		{
			final TrigState state = stepGetCompleted(key, msg);
			if(state != null)
				return new Pair<Object,List<String>>(key, state.args());
		}
		return null;
	}

	@Override
	public Object[] getInProgress(final MOB mob)
	{
		if(isIgnoring(mob))
			return trackingNothing;
		final TrigTracker tracker = getTrigTracker(mob);
		if(tracker == null)
			return trackingNothing;
		if(tracker.states.size()==0)
			return trackingNothing;
		List<Object> inProgress = null;
		for(final Object key : tracker.states.keySet())
		{
			final TrigState state = tracker.states.get(key);
			if(state.completed>=0)
			{
				if(inProgress == null)
					inProgress  = new ArrayList<Object>(1);
				inProgress.add(key);
			}
		}
		return (inProgress == null) ? trackingNothing : inProgress.toArray();
	}

	@Override
	public boolean wasCompletedRecently(final MOB mob, final Object key)
	{
		if(isIgnoring(mob))
			return false;
		final TrigTracker tracker = getTrigTracker(mob);
		if(tracker == null)
			return false;
		final boolean comp = tracker.compl.contains(key);
		if(comp)
			tracker.compl.remove(key);
		return comp;
	}

	@Override
	public MOB[] whosDoneWaiting()
	{
		if(waitingFor.size()>0)
		{
			synchronized(waitingFor)
			{
				if(waitingFor.size()==0)
					return trackingNoone;
				List<MOB> waitDoneList=null;
				final long now=System.currentTimeMillis();
				for (final Iterator<TrigState> s = waitingFor.iterator();s.hasNext();)
				{
					final TrigState S = s.next();
					if(now > S.waitExpire)
					{
						if(waitDoneList == null)
							waitDoneList=new ArrayList<MOB>(1);
						final MOB M=S.charM.get();
						if(M!=null)
							waitDoneList.add(M);
						s.remove();
						S.waitExpire=-1;
					}
				}
				if(waitDoneList != null)
					return waitDoneList.toArray(new MOB[waitDoneList.size()]);
			}
		}
		return trackingNoone;
	}

	@Override
	public boolean hasTrigger(final Object key)
	{
		return rituals.containsKey(key);
	}

	@Override
	public Map<String,List<Social>> getSocialSets()
	{
		return socialsMap;
	}

	@Override
	public CMObject copyOf()
	{
		final DefaultTriggerer me;
		try
		{
			me = (DefaultTriggerer) this.clone();
			me.trackers	= new Hashtable<String, TrigTracker>();
			me.rituals = new SHashtable<Object, Trigger[]>();
			me.rituals.putAll(rituals);
			me.socialsMap = new SHashtable<String,List<Social>>();
			me.socialsMap.putAll(socialsMap);
			me.waitingFor = new SLinkedList<TrigState>();
			me.ignoreOf	= new LimitedTreeSet<String>();
		}
		catch (final CloneNotSupportedException e)
		{
			return newInstance();
		}
		return me;
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public int compareTo(final CMObject o)
	{
		return o==this?0:(o.hashCode()<hashCode()?1:-1);
	}
}
