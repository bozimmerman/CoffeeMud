package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.SlaveryLibrary.GeasStep;
import com.planet_ink.coffee_mud.Libraries.interfaces.SlaveryLibrary.GeasSteps;
import com.planet_ink.coffee_mud.Libraries.interfaces.SlaveryLibrary.Step;
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

import java.io.IOException;
import java.util.*;

/*
   Copyright 2005-2018 Bo Zimmerman

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

public class SlaveryParser extends StdLibrary implements SlaveryLibrary
{
	@Override
	public String ID()
	{
		return "SlaveryParser";
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		return true;
	}

	protected Object[]				fpmap				= null;

	// these should be checked after pmap prelim check.
	public static final String[] universalStarters={
		"go ",
		"go and ",
		"i want you to ",
		"i command you to ",
		"i order you to ",
		"you are commanded to ",
		"please ",
		"to ",
		"you are ordered to "};

	public static final String[] responseStarters={
		"try at the",
		"its at the",
		"it`s at the",
		"over at the",
		"at the",
		"go to",
		"go to the",
		"find the",
		"go ",
		"try to the",
		"over to the",
		"you`ll have to go",
		"you`ll have to find the",
		"you`ll have to find",
		"look at the",
		"look at",
		"look to",
		"look to the",
		"look for",
		"look for the",
		"search at the",
		"search at",
		"search to the",
		"look",
		"i saw one at",
		"he`s",
		"it`s",
		"she`s",
		"hes",
		"shes",
		"its",
	};
	
	public static String[] universalRejections={
		"dunno",
		"nope",
		"not",
		"never",
		"nowhere",
		"noone",
		"can`t",
		"cant",
		"don`t",
		"dont",
		"no"
	};

	//codes:
	//%m mob name (anyone)
	//%i item name (anything)
	//%g misc parms
	//%c casters name
	//%s social name
	//%k skill command word
	//%r room name
	//%d a direction
	// * match anything
	public static final String[][] pmap=
	{
		// below is killing
		{"kill %m","mobfind %m;kill %m"},
		{"find and kill %m","mobfind %m;kill %m"},
		{"murder %m","mobfind %m;kill %m"},
		{"find and murder %m","mobfind %m;kill %m"},
		{"find and destroy %m","mobfind %m;kill %m"},
		{"find %m and kill him","mobfind %m;kill %m"},
		{"find %m and kill her","mobfind %m;kill %m"},
		{"search and destroy %m","mobfind %m;kill %m"},
		{"destroy %i","itemfind %i;recall"},
		{"find and destroy %i","mobfind %i;recall"},
		{"search and destroy %i","mobfind %i;recall"},
		{"destroy %m","mobfind %m;kill %m"},
		{"assassinate %m","mobfind %m; kill %m"},
		{"find and assassinate %m","mobfind %m; kill %m"},
		// below is socials
		{"find and %s %m","mobfind %m;%s %m"},
		{"%s %m","mobfind %m;%s %m"},
		// below is item fetching
//DROWN, DROWN YOURSELF, DROWN IN A LAKE, SWIM, SWIM AN OCEAN, CLIMB A MOUNTAIN, 
//CLIMB A TREE, CLIMB <X>, SWIM <x>, HANG YOURSELF, CRAWL <x>
//BLOW YOUR NOSE, VOMIT, PUKE, THROW UP, KISS MY ASS, KISS <CHAR> <Body part>
//TELL <CHAR> <WHAT>
		{"bring %i","itemfind %i;mobfind %c;give %i %c"},
		{"bring %m %i","itemfind %i;mobfind %m;give %i %m"},
		{"bring %i to %m","itemfind %i;mobfind %m;give %i %m"},
		{"bring me %i","itemfind %i;mobfind %c;give %i %c"},
		{"bring my %i","itemfind %i;mobfind %c;give %i %c"},
		{"make %i","itemfind %i;mobfind %c;give %i %c"},
		{"make %m %i","itemfind %i;mobfind %m;give %i %m"},
		{"make %i for %m","itemfind %i;mobfind %m;give %i %m"},
		{"make me %i","itemfind %i;mobfind %c;give %i %c"},
		{"give %i","itemfind %i;mobfind %c;give %i %c"},
		{"give %m %i","itemfind %i;mobfind %m;give %i %m"},
		{"give %i to %m","itemfind %i;mobfind %m;give %i %m"},
		{"give me %i","itemfind %i;mobfind %c;give %i %c"},
		{"give your %i","itemfind %i;mobfind %c;give %i %c"},
		{"give %m your %i","itemfind %i;mobfind %m;give %i %m"},
		{"give your %i to %m","itemfind %i;mobfind %m;give %i %m"},
		{"give me your %i","itemfind %i;mobfind %c;give %i %c"},
		{"buy %i","itemfind %i;mobfind %c;give %i %c"},
		{"buy %m %i","itemfind %i;mobfind %m;give %i %m"},
		{"buy %i to %m","itemfind %i;mobfind %m;give %i %m"},
		{"buy me %i","itemfind %i;mobfind %c;give %i %c"},
		{"find me %i","itemfind %i;mobfind %c;give %i %c"},
		{"find my %i","itemfind %i;mobfind %c;give %i %c"},
		{"find %i for %m","itemfind %i;mobfind %m;give %i %m"},
		{"find %m %i","itemfind %i;mobfind %m;give %i %m"},
		{"fetch me %i","itemfind %i;mobfind %c;give %i %c"},
		{"fetch my %i","itemfind %i;mobfind %c;give %i %c"},
		{"fetch %i for %m","itemfind %i;mobfind %m;give %i %m"},
		{"fetch %m %i","itemfind %i;mobfind %m;give %i %m"},
		{"get me %i","itemfind %i;mobfind %c;give %i %c"},
		{"get my %i","itemfind %i;mobfind %c;give %i %c"},
		{"get %i for %m","itemfind %i;mobfind %m;give %i %m"},
		{"get %m %i","itemfind %i;mobfind %m;give %i %m"},
		{"get %i","itemfind %i"},
		{"deliver %i to %m","itemfind %i;mobfind %m;give %i %m"},
		{"deliver my %i to %m","itemfind %i;mobfind %m;give %i %m"},
		// below are eats, drinks
		{"eat %i","itemfind %i;eat %i"},
		{"consume %i","itemfind %i;eat %i"},
		{"stuff yourself with %i","itemfind %i;eat %i"},
		{"drink %i","itemfind %i;drink %i"},
		// below are gos, and find someone (and report back where), take me to, show me
		{"go %d","%d;"},
		{"walk %d","%d;"},
		{"move %d","%d;"},
		{"go to %r","find %r;sit"},
		{"report to %r","find %r;sit"},
		{"walk to %r","find %r;sit"},
		{"find %r","find %r;"},
		{"find %r","find %r;"},
		{"show me the way to %r","say follow me;find %r;"},
		{"show me how to get to %r","say follow me;find %r;"},
		{"show me how to get %r","say follow me;find %r;"},
		{"take me to %r","say follow me;find %r;"},
		// follow someone around (but not FOLLOW)
		// simple commands: hold, lock, unlock, read, channel
		{"hold %i","itemfind %i;hold %i"},
		{"lock %i","itemfind %i;lock %i"},
		{"unlock %i","itemfind %i;unlock %i"},
		{"read %i","itemfind %i;read %i"},
		{"gossip %g","gossip %g"},
		// more simpletons: say sit sleep stand wear x, wield x
		{"sleep","sleep"},
		{"sit","sit"},
		{"stand","stand"},
		{"sit down","sit"},
		{"stand up","stand"},
		{"wear %i","itemfind %i;wear %i"},
		{"wield %i","itemfind %i;wield %i"},
		// below are sit x sleep x mount x enter x
		{"sit %i","itemfind %i;sit %i"},
		{"sleep %i","itemfind %i;sleep %i"},
		{"mount %i","itemfind %i;mount %i"},
		{"mount %m","mobfind %m;mount %m"},
		// below are learns, practices, teaches, etc..
		// below are tells, say tos, report tos,
		{"tell %m %g","mobfind %m;sayto %m %g"},
		{"say %g to %m","mobfind %m;sayto %m %g"},
		{"tell %g to %m","mobfind %m;sayto %m %g"},
		// below are skill usages
		{"%k %i","itemfind %i;%k %i"},
		{"%k %m","mobfind %m;%k %m"},
		{"%k %g %i","itemfind %i;%k %g %i"},
		{"%k %g %m","mobfind %m;%k %g %m"},
		// below are silly questions
		{"where %*","say You want me to answer where? I don't know where!"},
		{"who %*","say You want me to answer who? I don't know who!"},
		{"when %*","say You want me to answer when? I don't know when!"},
		{"what %*","say You want me to answer what? I don't know what!"},
		{"why %*","say You want me to answer why? I don't know why!"},
		// other miscellaneous commands
		{"%k 2 times","%k;%k"}, // expand this smartly
		{"%k *","%k *"},
		{"%k","%k"}
	};
	
	@Override
	public List<Map<String,String>> findMatch(MOB mob, List<String> prereq)
	{
		final List<Map<String,String>> possibilities=new Vector<Map<String,String>>();
		Map<String,String> map=new Hashtable<String,String>();
		if(fpmap==null)
		{
			fpmap=new Object[pmap.length];
			for(int p=0;p<pmap.length;p++)
				fpmap[p]=CMParms.toStringArray(CMParms.parse(pmap[p][0]));
		}
		String[] chk=null;
		final String[] req=CMParms.toStringArray(prereq);
		boolean reject=false;
		int ci=0,ri=0;
		final CMObject[] commands=new CMObject[req.length];
		final Social[] socials=new Social[req.length];
		for(int i=0;i<req.length;i++)
		{
			socials[i]=CMLib.socials().fetchSocial(req[i],true);
			commands[i]=CMLib.english().findCommand(mob,new XVector<String>(req[i].toUpperCase()));
		}
		for(int p=0;p<fpmap.length;p++)
		{
			chk=(String[])fpmap[p];
			ci=0;ri=0;
			reject=false;
			while((!reject)&&(ci<chk.length)&&(ri<req.length))
			{
				if(chk[ci].equals(req[ri]))
				{
					ci++; ri++;
					reject=false;
				}
				else
				if(chk[ci].charAt(0)=='%')
				{
					switch(chk[ci].charAt(1))
					{
					case 's':
					{
						if(socials[ri]==null)
							reject=true;
						else
						{
							map.put("%s",req[ri]);
							reject=false;
							ci++;
							ri++;
						}
						break;
					}
					case 'd':
					{
						if(CMLib.directions().getGoodDirectionCode(req[ri])<0)
							reject=true;
						else
						{
							map.put("%d",req[ri]);
							reject=false;
							ci++;
							ri++;
						}
						break;
					}
					case 'm':
					case 'g':
					case '*':
					case 'r':
					case 'i':
					{
						final String code=chk[ci];
						final int remain=chk.length-ci;
						String str=req[ri];
						ri++;
						ci++;
						reject=false;
						while(ri<=(req.length-remain))
						{
							String nxt="";
							if(ci<chk.length)
							{
								nxt=chk[ci];
								if(nxt.startsWith("%"))
									nxt="";
							}
							if((nxt.length()>0)
							&&(ri<req.length)
							&&(req[ri].equals(nxt)))
								break;
							if(ri<req.length)
								str=str+" "+req[ri];
							ri++;
						}
						map.put(code,str);
						break;
					}
					case 'k':
					{
						if(commands[ri]==null)
							reject=true;
						else
						{
							map.put("%k",req[ri]);
							reject=false;
							ci++;
							ri++;
						}
						break;
					}
					default:
						break;
					}
				}
				else
					reject=true;
			}
			if((reject)||(ci!=chk.length)||(ri!=req.length))
			{
				map.clear();
				continue;
			}
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.GEAS))
				Log.debugOut("GEAS","POSS-"+pmap[p][1]);
			map.put("INSTR",pmap[p][1]);
			possibilities.add(map);
			map=new Hashtable<String,String>();
		}
		return possibilities;
	}

	@Override
	public String cleanWord(String s)
	{
		final char[] chars=".,;!?'".toCharArray();
		for(int x=0;x<chars.length;x++)
		{
			for(int i=0;i<chars.length;i++)
			{
				while(s.startsWith(""+chars[i]))
					s=s.substring(1).trim();
				while(s.endsWith(""+chars[i]))
					s=s.substring(0,s.length()-1).trim();
			}
		}
		return s;
	}

	@Override
	public GeasSteps processRequest(MOB you, MOB me, String req)
	{
		Vector<String> REQ=CMParms.parse(req.toLowerCase().trim());
		for(int v=0;v<REQ.size();v++)
			REQ.setElementAt(cleanWord(REQ.elementAt(v)),v);
		List<Map<String,String>> poss=findMatch(me,REQ);
		if(poss.size()==0)
		{
			req=CMParms.combine(REQ,0);
			boolean doneSomething=true;
			boolean didAnything=false;
			while(doneSomething)
			{
				doneSomething=false;
				for (final String universalStarter : universalStarters)
				{
					if(req.startsWith(universalStarter))
					{
						doneSomething=true;
						didAnything=true;
						req=req.substring(universalStarters.length).trim();
					}
				}
			}
			if(didAnything)
			{
				REQ=CMParms.parse(req);
				poss=findMatch(me,REQ);
			}
		}
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.GEAS))
			Log.debugOut("GEAS","POSSTOTAL-"+poss.size());
		final GeasSteps geasSteps=new GeasStepsImpl(you,me);
		if(poss.size()==0)
		{
			final GeasStepImpl g=new GeasStepImpl(geasSteps);
			g.que.add(CMParms.parse("wanderquery "+req));
			geasSteps.add(g);
		}
		else
		{
			for(int i=0;i<poss.size();i++)
			{
				final GeasStepImpl g=new GeasStepImpl(geasSteps);
				final Map<String,String> map=poss.get(i);
				final List<String> all=CMParms.parseSemicolons(map.get("INSTR"),true);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.GEAS))
					Log.debugOut("GEAS",CMParms.toListString(all));
				g.que=new Vector<List<String>>();
				for(int a=0;a<all.size();a++)
					g.que.add(CMParms.parse(all.get(a)));
				if(you!=null)
					map.put("%c",you.name());
				map.put("%n",me.name());
				for(int q=0;q<g.que.size();q++)
				{
					final List<String> V=g.que.get(q);
					for(int v=0;v<V.size();v++)
					{
						final String s=V.get(v);
						if(s.startsWith("%"))
							V.set(v,CMLib.english().cleanArticles(map.get(s.trim())));
					}
				}
				geasSteps.add(g);
			}
		}
		return geasSteps;
	}
	
	public static class GeasStepsImpl extends Vector<GeasStep> implements GeasSteps
	{
		public static final long	serialVersionUID	= Long.MAX_VALUE;
		
		public Set<Room>botheredPlaces	= new HashSet<Room>();
		public Set<MOB>	botheredMOBs	= new HashSet<MOB>();
		public boolean	done			= false;
		public MOB		you				= null;
		public MOB		me				= null;

		public GeasStepsImpl(MOB you1, MOB me1)
		{
			you=you1;
			me=me1;
		}
		
		@Override
		public Set<MOB> getBotheredMobs()
		{
			return botheredMOBs;
		}

		@Override
		public MOB stepperM()
		{
			return me;
		}

		@Override
		public boolean isDone()
		{
			return done;
		}
		
		@Override
		public void step()
		{
			String say=null;
			boolean moveFlag=false;
			boolean holdFlag=false;
			String ss=null;
			GeasStep sg=null;

			if(!done)
			for(int s=0;s<size();s++)
			{
				final GeasStep G=elementAt(s);
				ss=G.step();
				if(ss.equalsIgnoreCase("DONE"))
				{
					done=true;
					break;
				}
				if(ss.equalsIgnoreCase("HOLD"))
				{
					removeElementAt(s);
					insertElementAt(G,0);
					holdFlag=true;
					break;
				}
				else
				if(ss.equalsIgnoreCase("MOVE"))
					moveFlag=true;
				else
				if(ss.startsWith("1"))
				{
					say=ss;
					sg=G;
				}
				else
				if(ss.startsWith("0"))
				{
					if(say==null)
					{
						say=ss;
						sg=G;
					}
				}

			}
			if(!holdFlag)
			{
				if((say!=null)&&(sg!=null)&&(ss!=null))
				{
					if(!sg.botherIfAble(ss.substring(1)))
					{
						sg.setStep(Step.EVAL);
						move(CMath.s_int(""+ss.charAt(0)));
					}
					else
						sg.setStep(Step.INT1);
				}
				else
				if(moveFlag)
					move(0);
			}
		}

		@Override
		public void move(int moveCode)
		{
			if(!botheredPlaces.contains(me.location()))
				botheredPlaces.add(me.location());
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.GEAS))
				Log.debugOut("GEAS","BEINGMOBILE: "+moveCode);
			if(moveCode==0)
			{
				if(!CMLib.tracking().beMobile(me,true,true,false,true,null,botheredPlaces))
					CMLib.tracking().beMobile(me,true,true,false,false,null,null);
			}
			else
			{
				if(!CMLib.tracking().beMobile(me,true,true,true,false,null,botheredPlaces))
					CMLib.tracking().beMobile(me,true,true,false,false,null,null);
			}
		}

		@Override
		public boolean sayResponse(MOB speaker, MOB target, String response)
		{
			for(int s=0;s<size();s++)
			{
				final GeasStep G=elementAt(s);
				if(G.getBotheredMob()!=null)
					return G.sayResponse(speaker,target,response);
			}
			return false;
		}
	}

	public static class GeasStepImpl implements GeasStep
	{
		public List<List<String>>	que			= new Vector<List<String>>();
		public Step					step		= Step.EVAL;
		public MOB					bothering	= null;
		public GeasSteps			mySteps		= null;
		public MOB					you			= null;

		public GeasStepImpl(GeasSteps gs)
		{
			mySteps=gs;
		}

		@Override
		public MOB getBotheredMob()
		{
			return bothering;
		}
		
		@Override
		public boolean botherIfAble(String msgOrQ)
		{
			final MOB me=mySteps.stepperM();
			bothering=null;
			if((me==null)||(me.location()==null))
				return false;
			if((msgOrQ!=null)
			&&(!CMLib.flags().isAnimalIntelligence(me)))
			{
				for(int m=0;m<me.location().numInhabitants();m++)
				{
					final MOB M=me.location().fetchInhabitant(m);
					if((M!=null)
					&&(M!=me)
					&&(!CMLib.flags().isAnimalIntelligence(M))
					&&(!mySteps.getBotheredMobs().contains(M)))
					{
						CMLib.commands().postSay(me,M,msgOrQ,false,false);
						bothering=M;
						mySteps.getBotheredMobs().add(M);
						if(CMSecurity.isDebugging(CMSecurity.DbgFlag.GEAS))
							Log.debugOut("GEAS","BOTHERING: "+bothering.name());
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public void setStep(Step step)
		{
			this.step = step;
		}
		
		@Override
		public boolean sayResponse(MOB speaker, MOB target, String response)
		{
			final MOB me=mySteps.stepperM();
			if((speaker!=null)
			&&(speaker!=me)
			&&(bothering!=null)
			&&(speaker==bothering)
			&&(step!=Step.EVAL)
			&&((target==null)||(target==me)))
			{
				for (final String universalRejection : universalRejections)
				{
					if(CMLib.english().containsString(response,universalRejection))
					{
						CMLib.commands().postSay(me,speaker,CMLib.lang().L("Ok, thanks anyway."),false,false);
						return true;
					}
				}
				boolean starterFound=false;
				response=response.toLowerCase().trim();
				for (final String responseStarter : responseStarters)
				{
					if(response.startsWith(responseStarter))
					{
						starterFound=true;
						response=response.substring(responseStarter.length()).trim();
					}
				}
				if((!starterFound)
				&&(speaker.isMonster())
				&&(CMLib.dice().rollPercentage()<10))
					return false;
				if(response.trim().length()==0)
					return false;
				bothering=null;
				que.add(0,CMParms.parse("find150 \""+response+"\""));
				step=Step.EVAL;
				return true;
			}
			return false;
		}

		@Override
		public String step()
		{
			final MOB me=mySteps.stepperM();
			if(me==null)
				return "DONE";
			final Room R=me.location();
			if(R==null)
				return "HOLD";
			if(que.size()==0)
			{
				step=Step.ALLDONE;
				return "DONE";
			}
			final List<String> cur=que.get(0);
			if(cur.size()==0)
			{
				step=Step.EVAL;
				que.remove(0);
				return "HOLD";
			}
			final String s=cur.get(0);
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.GEAS))
				Log.debugOut("GEAS","STEP-"+s);
			if(s.equalsIgnoreCase("itemfind"))
			{
				String item=CMParms.combine(cur,1);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.GEAS))
					Log.debugOut("GEAS","ITEMFIND: "+item);
				if((CMath.isNumber(item)&&(CMath.s_int(item)>0)))
				{
					if(CMLib.beanCounter().getTotalAbsoluteNativeValue(me)>=(CMath.s_int(item)))
					{
						step=Step.EVAL;
						que.remove(0);
						CMLib.commands().postSay(me,null,CMLib.lang().L("I got the money!"),false,false);
						return "HOLD";
					}
					item="coins";
				}

				// do I already have it?
				Item I=me.findItem(item);
				if((I!=null)
				&&(CMLib.flags().canBeSeenBy(I,me)))
				{
					step=Step.EVAL;
					if(!I.amWearingAt(Wearable.IN_INVENTORY))
					{
						CMLib.commands().postRemove(me,I,false);
						return "HOLD";
					}
					if(I.container()!=null)
					{
						CMLib.commands().postGet(me,I.container(),I,false);
						return "HOLD";
					}
					que.remove(0);
					CMLib.commands().postSay(me,null,CMLib.lang().L("I got @x1!",I.name(me)),false,false);
					return "HOLD";
				}
				// is it just sitting around?
				I=R.findItem(null,item);
				if((I!=null)
				&&(CMLib.flags().canBeSeenBy(I,me)))
				{
					step=Step.EVAL;
					CMLib.commands().postGet(me,null,I,false);
					return "HOLD";
				}
				// is it in a container?
				I=R.findItem(item);
				if((I!=null)
				&&(I.container()!=null)
				&&(I.container().isOpen()))
				{
					step=Step.EVAL;
					CMLib.commands().postGet(me,I.container(),I,false);
					return "HOLD";
				}
				// is it up for sale?
				for(int m=0;m<R.numInhabitants();m++)
				{
					final MOB M=R.fetchInhabitant(m);
					if((M!=null)&&(M!=me)&&(CMLib.flags().canBeSeenBy(M,me)))
					{
						I=M.findItem(null,item);
						if((I!=null)&&(!I.amWearingAt(Wearable.IN_INVENTORY)))
						{
							switch(step)
							{
							case EVAL:
								{
									CMLib.commands().postSay(me,M,CMLib.lang().L("I must have '@x1.  Give it to me now.",I.name(me)),false,false);
									step=Step.INT1;
									return "HOLD";
								}
							case INT1:
								{
									step=Step.INT2;
									return "HOLD";
								}
							case INT2:
								{
									CMLib.commands().postSay(me,M,CMLib.lang().L("I MUST HAVE '@x1.  GIVE IT TO ME NOW!!!!",I.name(me).toUpperCase()),false,false);
									step=Step.INT3;
									return "HOLD";
								}
							case INT3:
								{
									step=Step.INT4;
									return "HOLD";
								}
							case INT4:
								{
									CMLib.combat().postAttack(me,M,me.fetchWieldedItem());
									step=Step.EVAL;
									return "HOLD";
								}
							default:
								break;
							}
						}
						final ShopKeeper sk=CMLib.coffeeShops().getShopKeeper(M);
						if((!item.equals("coins"))
						&&(sk!=null)
						&&(sk.getShop().getStock(item,me)!=null))
						{
							final Environmental E=sk.getShop().getStock(item,me);
							if((E!=null)&&(E instanceof Item))
							{
								double price=CMLib.coffeeShops().sellingPrice(M,me,E,sk,sk.getShop(), true).absoluteGoldPrice;
								if(price<=CMLib.beanCounter().getTotalAbsoluteShopKeepersValue(me,M))
								{
									me.enqueCommand(CMParms.parse("BUY \""+E.name()+"\""),MUDCmdProcessor.METAFLAG_FORCED|MUDCmdProcessor.METAFLAG_ORDER,0);
									step=Step.EVAL;
									return "HOLD";
								}
								price=price-CMLib.beanCounter().getTotalAbsoluteShopKeepersValue(me,M);
								que.add(0,CMParms.parse("itemfind "+CMLib.beanCounter().nameCurrencyShort(M,price)));
								CMLib.commands().postSay(me,null,CMLib.lang().L("Damn, I need @x1.",CMLib.beanCounter().nameCurrencyShort(M,price)),false,false);
								step=Step.EVAL;
								return "HOLD";
							}
						}
					}
				}
				// if asked someone something, give them time to respond.
				if ((bothering != null) 
				&& (step.ordinal() > Step.EVAL.ordinal()) 
				&& (step.ordinal() <= Step.INT4.ordinal()) 
				&& (!bothering.isMonster()))
				{
					step = step.nextStep();
					return "HOLD";
				}
				step=Step.EVAL;
				return "0Can you tell me where to find "+CMParms.combine(cur,1)+"?";
			}
			else
			if(s.equalsIgnoreCase("mobfind"))
			{
				String name=CMParms.combine(cur,1);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.GEAS))
					Log.debugOut("GEAS","MOBFIND: "+name);
				if(name.equalsIgnoreCase("you"))
					name=me.name();
				if(name.equalsIgnoreCase("yourself"))
					name=me.name();
				if(you!=null)
				{
					if(name.equals("me"))
						name=you.name();
					if(name.equals("myself"))
						name=you.name();
					if(name.equals("my"))
						name=you.name();
				}

				MOB M=R.fetchInhabitant(name);
				if(M==me)
					M=R.fetchInhabitant(name+".2");
				if((M!=null)&&(M!=me)&&(CMLib.flags().canBeSeenBy(M,me)))
				{
					if(CMSecurity.isDebugging(CMSecurity.DbgFlag.GEAS))
						Log.debugOut("GEAS","MOBFIND-FOUND: "+name);
					step=Step.EVAL;
					que.remove(0);
					return "HOLD";
				}

				// if asked someone something, give them time to respond.
				if((bothering!=null)
				&&(step.ordinal()>Step.EVAL.ordinal())
				&&(step.ordinal()<=Step.INT4.ordinal())
				&&(!bothering.isMonster()))
				{
					if(CMSecurity.isDebugging(CMSecurity.DbgFlag.GEAS))
						Log.debugOut("GEAS","MOBFIND-RESPONSEWAIT: "+bothering.name());
					step = step.nextStep();
					return "HOLD";
				}
				step=Step.EVAL;
				int code=0;
				if((you!=null)&&(you.name().equalsIgnoreCase(name)))
					code=1;
				return code+"Can you tell me where to find "+name+"?";
			}
			else
			if(s.toLowerCase().startsWith("find"))
			{
				String name=CMParms.combine(cur,1);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.GEAS))
					Log.debugOut("GEAS","FIND: "+name);
				if(name.equalsIgnoreCase("you"))
					name=me.name();
				if(name.equalsIgnoreCase("yourself"))
					name=me.name();
				if(you!=null)
				{
					if(name.equals("me"))
						name=you.name();
					if(name.equals("myself"))
						name=you.name();
					if(name.equals("my"))
						name=you.name();
				}
				final int dirCode=CMLib.directions().getGoodDirectionCode(CMParms.parse(name).firstElement());
				if((dirCode>=0)&&(R.getRoomInDir(dirCode)!=null))
				{
					if(CMParms.parse(name).size()>1)
						cur.set(1,CMParms.combine(CMParms.parse(name),1));
					step=Step.EVAL;
					que.remove(0);
					CMLib.tracking().walk(me,dirCode,false,false);
					return "HOLD";
				}

				if(CMLib.english().containsString(R.name(),name)
				||CMLib.english().containsString(R.displayText(),name)
				||CMLib.english().containsString(R.description(),name))
				{
					step=Step.EVAL;
					que.remove(0);
					return "HOLD";
				}
				final MOB M=R.fetchInhabitant(name);
				if((M!=null)
				&&(M!=me)
				&&(CMLib.flags().canBeSeenBy(M,me)))
				{
					step=Step.EVAL;
					que.remove(0);
					return "HOLD";
				}
				// is it just sitting around?
				final Item I=R.findItem(null,name);
				if((I!=null)
				&&(CMLib.flags().canBeSeenBy(I,me)))
				{
					step=Step.EVAL;
					CMLib.commands().postGet(me,null,I,false);
					return "HOLD";
				}
				if((s.length()>4)
				&&(CMath.isNumber(s.substring(4))))
				{
					int x=CMath.s_int(s.substring(4));
					if((--x)<0)
					{
						que.remove(0);
						step=Step.EVAL;
						return "HOLD";
					}
					cur.set(0,"find"+x);
				}

				// if asked someone something, give them time to respond.
				if ((bothering != null) 
				&& (step.ordinal() > Step.EVAL.ordinal()) 
				&& (step.ordinal() <= Step.INT4.ordinal()) 
				&& (!bothering.isMonster()))
				{
					step = step.nextStep();
					return "HOLD";
				}
				step=Step.EVAL;
				if(s.length()>4)
					return "0Can you tell me where to find "+name+"?";
				return "MOVE";
			}
			else
			if(s.equalsIgnoreCase("wanderquery"))
			{
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.GEAS))
					Log.debugOut("GEAS","WANDERQUERY: "+CMParms.combine(cur,1));
				// if asked someone something, give them time to respond.
				if ((bothering != null) 
				&& (step.ordinal() > Step.EVAL.ordinal()) 
				&& (step.ordinal() <= Step.INT4.ordinal()) 
				&& (!bothering.isMonster()))
				{
					step = step.nextStep();
					return "HOLD";
				}
				step=Step.EVAL;
				return "Can you help me "+CMParms.combine(cur,1)+"?";
			}
			else
			{
				step=Step.EVAL;
				que.remove(0);
				me.enqueCommand(cur,MUDCmdProcessor.METAFLAG_FORCED|MUDCmdProcessor.METAFLAG_ORDER,0);
				return "HOLD";
			}
		}
	}
}
