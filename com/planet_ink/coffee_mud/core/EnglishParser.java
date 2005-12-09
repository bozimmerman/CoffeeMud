package com.planet_ink.coffee_mud.utils;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

import java.io.IOException;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class EnglishParser extends Scriptable implements Tickable
{
	private EnglishParser(){};
	public String ID(){return "EnglishParser";}
	public String name(){return "CoffeeMuds English Parser";}
	public long getTickStatus(){return Tickable.STATUS_NOT;}
    public CMObject newInstance(){return this;}
    public CMObject copyOf(){return this;}
    public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	
	public static final int FLAG_STR=0;
	public static final int FLAG_DOT=1;
	public static final int FLAG_ALL=2;
	
	public final static int STEP_EVAL=0;
	public final static int STEP_INT1=1;
	public final static int STEP_INT2=2;
	public final static int STEP_INT3=3;
	public final static int STEP_INT4=4;
	public final static int STEP_INT5=5;
	public final static int STEP_ALLDONE=-999;
	
	public boolean tick(Tickable ticking, int tickID)
	{
		return true;
	}

	// these should be checked after pmap prelim check.
	protected static final String[] universalStarters={
		"go ",
		"go and ",
		"i want you to ",
		"i command you to ",
		"i order you to ",
		"you are commanded to ",
		"please ",
		"to ",
	    "you are ordered to "};

	protected static final String[] responseStarters={
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
	protected static String[] universalRejections={
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
	// * match anything
	protected static final String[][] pmap={
		// below is killing
		{"kill %m","mobfind %m;kill %m"},
		{"find and kill %m","mobfind %m;kill %m"},
		{"murder %m","mobfind %m;kill %m"},
		{"find and murder %m","mobfind %m;kill %m"},
		{"find and destroy %m","mobfind %m;kill %m"},
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
//DROWN, DROWN YOURSELF, DROWN IN A LAKE, SWIM, SWIM AN OCEAN, CLIMB A MOUNTAIN, CLIMB A TREE, CLIMB <X>, SWIM <x>, HANG YOURSELF, CRAWL <x>
//BLOW YOUR NOSE, VOMIT, PUKE, THROW UP, KISS MY ASS, KISS <CHAR> <Body part>
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
		{"why %*","say You want me to answer why? I don't know why!"}
	};

	private static Object[] fpmap=null;
	
	private static Vector findMatch(MOB mob, Vector prereq)
	{
		Vector possibilities=new Vector();
		Hashtable map=new Hashtable();
		if(fpmap==null)
		{
		    fpmap=new Object[pmap.length];
			for(int p=0;p<pmap.length;p++)
				fpmap[p]=Util.toStringArray(Util.parse(pmap[p][0]));
		}
		String[] chk=null;
		String[] req=Util.toStringArray(prereq);
		boolean reject=false;
		int ci=0,ri=0;
		Object[] commands=new Object[req.length];
		Social[] socials=new Social[req.length];
		for(int i=0;i<req.length;i++)
		{
		    socials[i]=Socials.FetchSocial(req[i],true);
		    commands[i]=findCommand(mob,Util.makeVector(req[i].toUpperCase()));
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
					case 'm':
					case 'g':
					case '*':
					case 'r':
					case 'i':
						String code=chk[ci];
						int remain=chk.length-ci;
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
					case 'k':
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
			if(CMSecurity.isDebugging("GEAS"))
				Log.debugOut("GEAS","POSS-"+pmap[p][1]);
			map.put("INSTR",pmap[p][1]);
			possibilities.addElement(map);
			map=new Hashtable();
		}
		return possibilities;
	}

	private static String cleanWord(String s)
	{
		String chars=".,;!?'";
		for(int x=0;x<chars.length();x++)
			for(int i=0;i<chars.length();i++)
			{
				while(s.startsWith(""+chars.charAt(i)))
					s=s.substring(1).trim();
				while(s.endsWith(""+chars.charAt(i)))
					s=s.substring(0,s.length()-1).trim();
			}
		return s;
	}

	public static String cleanArticles(String s)
	{
		String[] articles={"a","an","all of","some one","a pair of","one of","all","the","some"};
        boolean didSomething=true;
		while(didSomething)
		{
            didSomething=false;
			for(int a=0;a<articles.length;a++)
			{
				if(s.toLowerCase().startsWith(articles[a]+" "))
                {
                    didSomething=true;
					s=s.substring(articles[a].length()+1);
                }
			}
		}
		return s;
	}

	public static geasSteps processRequest(MOB you, MOB me, String req)
	{
		Vector REQ=Util.parse(req.toLowerCase().trim());
		for(int v=0;v<REQ.size();v++)
			REQ.setElementAt(cleanWord((String)REQ.elementAt(v)),v);
		Vector poss=findMatch(me,REQ);
		if(poss.size()==0)
		{
			req=Util.combine(REQ,0);
			boolean doneSomething=true;
			boolean didAnything=false;
			while(doneSomething)
			{
				doneSomething=false;
				for(int i=0;i<universalStarters.length;i++)
					if(req.startsWith(universalStarters[i]))
					{
						doneSomething=true;
						didAnything=true;
						req=req.substring(universalStarters.length).trim();
					}
			}
			if(didAnything)
			{
				REQ=Util.parse(req);
				poss=findMatch(me,REQ);
			}
		}
		if(CMSecurity.isDebugging("GEAS"))
			Log.debugOut("GEAS","POSSTOTAL-"+poss.size());
		geasSteps geasSteps=new geasSteps(you,me);
		if(poss.size()==0)
		{
			geasStep g=new geasStep(geasSteps);
			g.que.addElement("wanderquery "+req);
			geasSteps.addElement(g);
		}
		else
		{
		    for(int i=0;i<poss.size();i++)
		    {
				geasStep g=new geasStep(geasSteps);
				Hashtable map=(Hashtable)poss.elementAt(i);
				Vector all=Util.parseSemicolons((String)map.get("INSTR"),true);
				if(CMSecurity.isDebugging("GEAS"))
					Log.debugOut("GEAS",Util.toStringList(all));
				g.que=new Vector();
				for(int a=0;a<all.size();a++)
					g.que.addElement(Util.parse((String)all.elementAt(a)));
				if(you!=null)	map.put("%c",you.name());
				map.put("%n",me.name());
				for(int q=0;q<g.que.size();q++)
				{
					Vector V=(Vector)g.que.elementAt(q);
					for(int v=0;v<V.size();v++)
					{
						String s=(String)V.elementAt(v);
						if(s.startsWith("%"))
							V.setElementAt(cleanArticles((String)map.get(s.trim())),v);
					}
				}
				geasSteps.addElement(g);
		    }
		}
		return geasSteps;
	}

	public static class geasSteps extends Vector
	{
	    public static final long serialVersionUID=Long.MAX_VALUE;
		public Vector bothered=new Vector();
		public boolean done=false;
		public MOB you=null;
		public MOB me=null;
		
		public geasSteps(MOB you1, MOB me1)
		{
		    you=you1;
		    me=me1;
		}
		
		public void step()
		{
		    String say=null;
		    boolean moveFlag=false;
		    boolean holdFlag=false;
		    String ss=null;
		    geasStep sg=null;
		    
		    if(!done)
		    for(int s=0;s<size();s++)
		    {
		        geasStep G=(geasStep)elementAt(s);
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
		        if((say!=null)&&(sg!=null))
		        {
		            if(!sg.botherIfAble(ss.substring(1)))
		            {
		                sg.step=STEP_EVAL;
		                move(Util.s_int(""+ss.charAt(0)));
		            }
		            else
		                sg.step=STEP_INT1;
		        }
		        else
		        if(moveFlag)
		            move(0);
		    }
		}
		public void move(int moveCode)
		{
			if(!bothered.contains(me.location()))
			    bothered.addElement(me.location());
			if(CMSecurity.isDebugging("GEAS"))
				Log.debugOut("GEAS","BEINGMOBILE: "+moveCode);
			if(moveCode==0)
			{
				if(!MUDTracker.beMobile(me,true,true,false,true,null,bothered))
					MUDTracker.beMobile(me,true,true,false,false,null,null);
			}
			else
			{
				if(!MUDTracker.beMobile(me,true,true,true,false,null,bothered))
					MUDTracker.beMobile(me,true,true,false,false,null,null);
			}
		}

		public boolean sayResponse(MOB speaker, MOB target, String response)
		{
		    for(int s=0;s<size();s++)
		    {
		        geasStep G=(geasStep)elementAt(s);
		        if(G.bothering!=null)
		            return G.sayResponse(speaker,target,response);
		    }
		    return false;
		}
	}
	
	public static class geasStep
	{
		public Vector que=new Vector();
		public int step=STEP_EVAL;
		public MOB bothering=null;
		public geasSteps mySteps=null;
		public MOB you=null;
		
		public geasStep(geasSteps gs)
		{
		    mySteps=gs;
		}

		public boolean botherIfAble(String msgOrQ)
		{
		    MOB me=mySteps.me;
			bothering=null;
			if((me==null)||(me.location()==null)) 
			    return false;
			if((msgOrQ!=null)&&(!Sense.isAnimalIntelligence(me)))
				for(int m=0;m<me.location().numInhabitants();m++)
				{
					MOB M=me.location().fetchInhabitant(m);
					if((M!=null)
					&&(M!=me)
					&&(!Sense.isAnimalIntelligence(M))
					&&(!mySteps.bothered.contains(M)))
					{
						CommonMsgs.say(me,M,msgOrQ,false,false);
						bothering=M;
						mySteps.bothered.addElement(M);
						if(CMSecurity.isDebugging("GEAS"))
							Log.debugOut("GEAS","BOTHERING: "+bothering.name());
						return true;
					}
				}
			return false;
		}
		
		public boolean sayResponse(MOB speaker, MOB target, String response)
		{
		    MOB me=mySteps.me;
			if((speaker!=null)
			&&(speaker!=me)
			&&(bothering!=null)
			&&(speaker==bothering)
			&&(step!=STEP_EVAL)
			&&((target==null)||(target==me)))
			{
				for(int s=0;s<universalRejections.length;s++)
				{
					if(containsString(response,universalRejections[s]))
					{
						CommonMsgs.say(me,speaker,"Ok, thanks anyway.",false,false);
						return true;
					}
				}
				boolean starterFound=false;
				response=response.toLowerCase().trim();
				for(int i=0;i<responseStarters.length;i++);
					for(int s=0;s<responseStarters.length;s++)
					{
						if(response.startsWith(responseStarters[s]))
						{
							starterFound=true;
							response=response.substring(responseStarters[s].length()).trim();
						}
					}
				if((!starterFound)&&(speaker.isMonster())&&(Dice.rollPercentage()<10))
					return false;
				if(response.trim().length()==0)
					return false;
				bothering=null;
				que.insertElementAt(Util.parse("find150 \""+response+"\""),0);
				step=STEP_EVAL;
				return true;
			}
			return false;
		}

		public String step()
		{
		    MOB me=mySteps.me;
			if(que.size()==0)
			{
				step=STEP_ALLDONE;
				return "DONE";
			}
			Vector cur=(Vector)que.firstElement();
			if(cur.size()==0)
			{
				step=STEP_EVAL;
				que.removeElementAt(0);
				return "HOLD";
			}
			String s=(String)cur.firstElement();
			if(CMSecurity.isDebugging("GEAS"))
				Log.debugOut("GEAS","STEP-"+s);
			if(s.equalsIgnoreCase("itemfind"))
			{
				String item=Util.combine(cur,1);
				if(CMSecurity.isDebugging("GEAS"))
					Log.debugOut("GEAS","ITEMFIND: "+item);
				if((Util.isNumber(item)&&(Util.s_int(item)>0)))
				{
					if(BeanCounter.getTotalAbsoluteNativeValue(me)>=new Integer(Util.s_int(item)).doubleValue())
					{
						step=STEP_EVAL;
						que.removeElementAt(0);
						CommonMsgs.say(me,null,"I got the money!",false,false);
						return "HOLD";
					}
					item="coins";
				}

				// do I already have it?
				Item I=me.fetchInventory(item);
				if((I!=null)&&(Sense.canBeSeenBy(I,me)))
				{
					step=STEP_EVAL;
					if(!I.amWearingAt(Item.INVENTORY))
					{
						CommonMsgs.remove(me,I,false);
						return "HOLD";
					}
					if(I.container()!=null)
					{
						CommonMsgs.get(me,I.container(),I,false);
						return "HOLD";
					}
					que.removeElementAt(0);
					CommonMsgs.say(me,null,"I got "+I.name()+"!",false,false);
					return "HOLD";
				}
				// is it just sitting around?
				I=me.location().fetchItem(null,item);
				if((I!=null)&&(Sense.canBeSeenBy(I,me)))
				{
					step=STEP_EVAL;
					CommonMsgs.get(me,null,I,false);
					return "HOLD";
				}
				// is it in a container?
				I=me.location().fetchAnyItem(item);
				if((I!=null)&&(I.container()!=null)
				   &&(I.container() instanceof Container)
				   &&(((Container)I.container()).isOpen()))
				{
					step=STEP_EVAL;
					CommonMsgs.get(me,I.container(),I,false);
					return "HOLD";
				}
				// is it up for sale?
				for(int m=0;m<me.location().numInhabitants();m++)
				{
					MOB M=me.location().fetchInhabitant(m);
					if((M!=null)&&(M!=me)&&(Sense.canBeSeenBy(M,me)))
					{
						I=M.fetchInventory(null,item);
						if((I!=null)&&(!I.amWearingAt(Item.INVENTORY)))
						{
							if(step==STEP_EVAL)
							{
								CommonMsgs.say(me,M,"I must have '"+I.name()+".  Give it to me now.",false,false);
								step=STEP_INT1;
								return "HOLD";
							}
							else
							if(step==STEP_INT1)
							{
								step=STEP_INT2;
								return "HOLD";
							}
							else
							if(step==STEP_INT2)
							{
								CommonMsgs.say(me,M,"I MUST HAVE '"+I.name().toUpperCase()+".  GIVE IT TO ME NOW!!!!",false,false);
								step=STEP_INT3;
								return "HOLD";
							}
							else
							if(step==STEP_INT3)
							{
								step=STEP_INT4;
								return "HOLD";
							}
							else
							if(step==STEP_INT4)
							{
								MUDFight.postAttack(me,M,me.fetchWieldedItem());
								step=STEP_EVAL;
								return "HOLD";
							}
						}
						ShopKeeper sk=CoffeeShops.getShopKeeper(M);
						if((!item.equals("coins"))&&(sk!=null)&&(sk.getStock(item,me)!=null))
						{
							Environmental E=sk.getStock(item,me);
							if((E!=null)&&(E instanceof Item))
							{
                                double price=CoffeeShops.sellingPrice(M,me,E,sk,true).absoluteGoldPrice;
								if(price<=BeanCounter.getTotalAbsoluteShopKeepersValue(me,M))
								{
									me.enqueCommand(Util.parse("BUY \""+E.name()+"\""),0);
									step=STEP_EVAL;
									return "HOLD";
								}
								price=price-BeanCounter.getTotalAbsoluteShopKeepersValue(me,M);
								que.insertElementAt(Util.parse("itemfind "+BeanCounter.nameCurrencyShort(M,price)),0);
								CommonMsgs.say(me,null,"Damn, I need "+BeanCounter.nameCurrencyShort(M,price)+".",false,false);
								step=STEP_EVAL;
								return "HOLD";
							}
						}
					}
				}
				// if asked someone something, give them time to respond.
				if((bothering!=null)&&(step>STEP_EVAL)&&(step<=STEP_INT4)&&(!bothering.isMonster()))
				{	step++; return "HOLD";}
				step=STEP_EVAL;
				return "0Can you tell me where to find "+Util.combine(cur,1)+"?";
			}
			else
			if(s.equalsIgnoreCase("mobfind"))
			{
				String name=Util.combine(cur,1);
				if(CMSecurity.isDebugging("GEAS"))
					Log.debugOut("GEAS","MOBFIND: "+name);
				if(name.equalsIgnoreCase("you")) name=me.name();
				if(name.equalsIgnoreCase("yourself")) name=me.name();
				if(you!=null)
				{
					if(name.equals("me")) name=you.name();
					if(name.equals("myself")) name=you.name();
					if(name.equals("my")) name=you.name();
				}

				MOB M=me.location().fetchInhabitant(name);
				if(M==me) M=me.location().fetchInhabitant(name+".2");
				if((M!=null)&&(M!=me)&&(Sense.canBeSeenBy(M,me)))
				{
					if(CMSecurity.isDebugging("GEAS"))
						Log.debugOut("GEAS","MOBFIND-FOUND: "+name);
					step=STEP_EVAL;
					que.removeElementAt(0);
					return "HOLD";
				}

				// if asked someone something, give them time to respond.
				if((bothering!=null)&&(step>STEP_EVAL)&&(step<=STEP_INT4)&&(!bothering.isMonster()))
				{	
					if(CMSecurity.isDebugging("GEAS"))
						Log.debugOut("GEAS","MOBFIND-RESPONSEWAIT: "+bothering.name());
					step++; 
					return "HOLD";
				}
				step=STEP_EVAL;
				int code=0;
				if((you!=null)&&(you.name().equalsIgnoreCase(name)))
					code=1;
				return code+"Can you tell me where to find "+name+"?";
			}
			else
			if(s.toLowerCase().startsWith("find"))
			{
				String name=Util.combine(cur,1);
				if(CMSecurity.isDebugging("GEAS"))
					Log.debugOut("GEAS","FIND: "+name);
				if(name.equalsIgnoreCase("you")) name=me.name();
				if(name.equalsIgnoreCase("yourself")) name=me.name();
				if(you!=null)
				{
					if(name.equals("me")) name=you.name();
					if(name.equals("myself")) name=you.name();
					if(name.equals("my")) name=you.name();
				}
				int dirCode=Directions.getGoodDirectionCode((String)Util.parse(name).firstElement());
				if((dirCode>=0)&&(me.location()!=null)&&(me.location().getRoomInDir(dirCode)!=null))
				{
					if(Util.parse(name).size()>1)
						cur.setElementAt(Util.combine(Util.parse(name),1),1);
					step=STEP_EVAL;
					MUDTracker.move(me,dirCode,false,false);
					return "HOLD";
				}

				if(containsString(me.location().name(),name)
				   ||containsString(me.location().displayText(),name)
				   ||containsString(me.location().description(),name))
				{
					step=STEP_EVAL;
					que.removeElementAt(0);
					return "HOLD";
				}
				MOB M=me.location().fetchInhabitant(name);
				if((M!=null)&&(M!=me)&&(Sense.canBeSeenBy(M,me)))
				{
					step=STEP_EVAL;
					que.removeElementAt(0);
					return "HOLD";
				}
				// is it just sitting around?
				Item I=me.location().fetchItem(null,name);
				if((I!=null)&&(Sense.canBeSeenBy(I,me)))
				{
					step=STEP_EVAL;
					CommonMsgs.get(me,null,I,false);
					return "HOLD";
				}
				if((s.length()>4)&&(Util.isNumber(s.substring(4))))
				{
					int x=Util.s_int(s.substring(4));
					if((--x)<0)
					{
						que.removeElementAt(0);
						step=STEP_EVAL;
						return "HOLD";
					}
					cur.setElementAt("find"+x,0);
				}

				// if asked someone something, give them time to respond.
				if((bothering!=null)&&(step>STEP_EVAL)&&(step<=STEP_INT4)&&(!bothering.isMonster()))
				{	step++; return "HOLD";}
				step=STEP_EVAL;
				if(s.length()>4)
					return "0Can you tell me where to find "+name+"?";
			    return "MOVE";
			}
			else
			if(s.equalsIgnoreCase("wanderquery"))
			{
				if(CMSecurity.isDebugging("GEAS"))
					Log.debugOut("GEAS","WANDERQUERY: "+Util.combine(cur,1));
				// if asked someone something, give them time to respond.
				if((bothering!=null)&&(step>STEP_EVAL)&&(step<=STEP_INT4)&&(!bothering.isMonster()))
				{	step++; return "HOLD";}
				step=STEP_EVAL;
				return "Can you help me "+Util.combine(cur,1)+"?";
			}
			else
			{
				step=STEP_EVAL;
				que.removeElementAt(0);
				me.enqueCommand(cur,0);
				return "HOLD";
			}
		}
	}
	public static Object findCommand(MOB mob, Vector commands)
	{
		if((mob==null)
		||(commands==null)
		||(mob.location()==null)
		||(commands.size()==0))
			return null;

		String firstWord=((String)commands.elementAt(0)).toUpperCase();
        
		if((firstWord.length()>1)&&(!Character.isLetterOrDigit(firstWord.charAt(0))))
		{
			commands.insertElementAt(((String)commands.elementAt(0)).substring(1),1);
			commands.setElementAt(""+firstWord.charAt(0),0);
			firstWord=""+firstWord.charAt(0);
		}
		
		// first, exacting pass
		Command C=CMClass.findCommandByTrigger(firstWord,true);
		if((C!=null)
        &&(C.securityCheck(mob))
        &&(!CMSecurity.isDisabled("COMMAND_"+CMClass.className(C).toUpperCase()))) 
            return C;

		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if(A.triggerStrings()!=null)
				for(int t=0;t<A.triggerStrings().length;t++)
					if((A.triggerStrings()[t].equalsIgnoreCase(firstWord))
                    &&(!CMSecurity.isDisabled("ABILITY_"+A.ID().toUpperCase())))
						return A;
		}

		Social social=Socials.FetchSocial(commands,true);
		if(social!=null) return social;

		for(int c=0;c<ChannelSet.getNumChannels();c++)
		{
			if(ChannelSet.getChannelName(c).equalsIgnoreCase(firstWord))
			{
				C=CMClass.getCommand("Channel");
				if((C!=null)&&(C.securityCheck(mob))) return C;
			}
			else
			if(("NO"+ChannelSet.getChannelName(c)).equalsIgnoreCase(firstWord))
			{
				C=CMClass.getCommand("NoChannel");
				if((C!=null)&&(C.securityCheck(mob))) return C;
			}
		}
		
        for(int c=0;c<ChannelSet.getNumCommandJournals();c++)
        {
            if(ChannelSet.getCommandJournalName(c).equalsIgnoreCase(firstWord))
            {
                C=CMClass.getCommand("CommandJournal");
                if((C!=null)&&(C.securityCheck(mob))) return C;
            }
        }
        
		// second, inexacting pass
		C=CMClass.findCommandByTrigger(firstWord,false);
        if((C!=null)
        &&(C.securityCheck(mob))
        &&(!CMSecurity.isDisabled("COMMAND_"+CMClass.className(C).toUpperCase()))) 
            return C;

		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if(A.triggerStrings()!=null)
				for(int t=0;t<A.triggerStrings().length;t++)
					if((A.triggerStrings()[t].toUpperCase().startsWith(firstWord))
                    &&(!CMSecurity.isDisabled("ABILITY_"+A.ID().toUpperCase())))
					{
						commands.setElementAt(A.triggerStrings()[t],0);
						return A;
					}
		}

		social=Socials.FetchSocial(commands,false);
		if(social!=null)
		{
			commands.setElementAt(social.ID(),0);
			return social;
		}
		
		for(int c=0;c<ChannelSet.getNumChannels();c++)
		{
			if(ChannelSet.getChannelName(c).startsWith(firstWord))
			{
				commands.setElementAt(ChannelSet.getChannelName(c),0);
				C=CMClass.getCommand("Channel");
				if((C!=null)&&(C.securityCheck(mob))) return C;
			}
			else
			if(("NO"+ChannelSet.getChannelName(c)).startsWith(firstWord))
			{
				commands.setElementAt("NO"+ChannelSet.getChannelName(c),0);
				C=CMClass.getCommand("NoChannel");
				if((C!=null)&&(C.securityCheck(mob))) return C;
			}
		}
        
        for(int c=0;c<ChannelSet.getNumCommandJournals();c++)
        {
            if(ChannelSet.getCommandJournalName(c).startsWith(firstWord))
            {
                C=CMClass.getCommand("CommandJournal");
                if((C!=null)&&(C.securityCheck(mob))) return C;
            }
        }
		return null;
	}

	private static boolean evokedBy(Ability thisAbility, String thisWord)
	{
		for(int i=0;i<thisAbility.triggerStrings().length;i++)
		{
			if(thisAbility.triggerStrings()[i].equalsIgnoreCase(thisWord))
				return true;
		}
		return false;
	}

	private static boolean evokedBy(Ability thisAbility, String thisWord, String secondWord)
	{
		for(int i=0;i<thisAbility.triggerStrings().length;i++)
		{
			if(thisAbility.triggerStrings()[i].equalsIgnoreCase(thisWord))
			{
				if((thisAbility.name().toUpperCase().startsWith(secondWord)))
					return true;
			}
		}
		return false;
	}

	public static Ability getToEvoke(MOB mob, Vector commands)
	{
		String evokeWord=((String)commands.elementAt(0)).toUpperCase();

		boolean foundMoreThanOne=false;
		Ability evokableAbility=null;
		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability thisAbility=mob.fetchAbility(a);
			if((thisAbility!=null)
			&&(evokedBy(thisAbility,evokeWord)))
            {
				if(evokableAbility!=null)
				{
					foundMoreThanOne=true;
					evokableAbility=null;
					break;
				}
				evokableAbility=thisAbility;
            }
		}

		if((evokableAbility!=null)&&(commands.size()>1))
		{
			int classCode=evokableAbility.classificationCode()&Ability.ALL_CODES;
			switch(classCode)
			{
			case Ability.SPELL:
			case Ability.SONG:
			case Ability.PRAYER:
			case Ability.CHANT:
				evokableAbility=null;
				foundMoreThanOne=true;
				break;
			default:
				break;
			}
		}

		if(evokableAbility!=null)
			commands.removeElementAt(0);
		else
		if((foundMoreThanOne)&&(commands.size()>1))
		{
			commands.removeElementAt(0);
			foundMoreThanOne=false;
			String secondWord=((String)commands.elementAt(0)).toUpperCase();
			for(int a=0;a<mob.numAbilities();a++)
			{
				Ability thisAbility=mob.fetchAbility(a);
				if((thisAbility!=null)
				&&(evokedBy(thisAbility,evokeWord,secondWord.toUpperCase())))
				{
					if(thisAbility.name().equalsIgnoreCase(secondWord))
					{
						evokableAbility=thisAbility;
						foundMoreThanOne=false;
						break;
					}
					else
					if(evokableAbility!=null)
						foundMoreThanOne=true;
					else
						evokableAbility=thisAbility;
				}
			}
			if((evokableAbility!=null)&&(!foundMoreThanOne))
				commands.removeElementAt(0);
			else
			if((foundMoreThanOne)&&(commands.size()>1))
			{
				String secondAndThirdWord=secondWord+" "+((String)commands.elementAt(1)).toUpperCase();

				for(int a=0;a<mob.numAbilities();a++)
				{
					Ability thisAbility=mob.fetchAbility(a);
					if((thisAbility!=null)
					   &&(evokedBy(thisAbility,evokeWord,secondAndThirdWord.toUpperCase())))
					{
						evokableAbility=thisAbility;
						break;
					}
				}
				if(evokableAbility!=null)
				{
					commands.removeElementAt(0);
					commands.removeElementAt(0);
				}
			}
			else
			{
				for(int a=0;a<mob.numAbilities();a++)
				{
					Ability thisAbility=mob.fetchAbility(a);
					if((thisAbility!=null)
					&&(evokedBy(thisAbility,evokeWord))
					&&(thisAbility.name().toUpperCase().indexOf(" "+secondWord.toUpperCase())>0))
					{
						evokableAbility=thisAbility;
						commands.removeElementAt(0);
						break;
					}
				}
			}
		}
		return evokableAbility;
	}

    public static boolean preEvoke(MOB mob, Vector commands)
    {
        commands=(Vector)commands.clone();
        Ability evokableAbility=getToEvoke(mob,commands);
        if(evokableAbility==null)
        {
            mob.tell(getScr("AbilityEvoker","evokeerr1"));
            return false;
        }
        if((CMAble.qualifyingLevel(mob,evokableAbility)>=0)
        &&(!CMAble.qualifiesByLevel(mob,evokableAbility)))
        {
            mob.tell(getScr("AbilityEvoker","evokeerr2"));
            return false;
        }
        return evokableAbility.preInvoke(mob,commands,null,false,0);
    }
	public static void evoke(MOB mob, Vector commands)
	{
		Ability evokableAbility=getToEvoke(mob,commands);
		if(evokableAbility==null)
		{
			mob.tell(getScr("AbilityEvoker","evokeerr1"));
			return;
		}
		if((CMAble.qualifyingLevel(mob,evokableAbility)>=0)
		&&(!CMAble.qualifiesByLevel(mob,evokableAbility)))
		{
			mob.tell(getScr("AbilityEvoker","evokeerr2"));
			return;
		}
		evokableAbility.invoke(mob,commands,null,false,0);
	}

	public static boolean containsString(String toSrchStr, String srchStr)
	{
		if(srchStr.equalsIgnoreCase("all")) return true;
		if(srchStr.equalsIgnoreCase(toSrchStr)) return true;
        if(Util.stripPunctuation(srchStr).trim().equalsIgnoreCase(Util.stripPunctuation(toSrchStr).trim())) 
            return true;
        boolean topOnly=false;
        if(srchStr.startsWith("$")&&(srchStr.length()>1))
        {
            srchStr=srchStr.substring(1);
            topOnly=true;
        }
		int tos=0;
		int tolen=toSrchStr.length();
		int srlen=srchStr.length();
		boolean found=false;
		while((!found)&&(tos<tolen))
		{
			for(int x=0;x<srlen;x++)
			{
				if(tos>=tolen)
				{
					if(srchStr.charAt(x)=='$')
						found=true;
					break;
				}

				switch(toSrchStr.charAt(tos))
				{
				case '^':
					tos=tos+2;
					break;
				case ',':
				case '?':
				case '!':
				case '.':
				case ';':
					tos++;
					break;
				}
				switch(srchStr.charAt(x))
				{
				case '^': x=x+2;
					break;
				case ',':
				case '?':
				case '!':
				case '.':
				case ';': x++;
					break;
				}
				if(x<srlen)
				{
					if(tos<tolen)
					{
						if(Character.toUpperCase(srchStr.charAt(x))!=Character.toUpperCase(toSrchStr.charAt(tos)))
							break;
						else
						if(x==(srlen-1))
						   found=true;
						else
							tos++;
					}
					else
					if(srchStr.charAt(x)=='$')
						found=true;
					else
						break;
				}
				else
				{
					found=true;
					break;
				}
			}
            if((topOnly)&&(!found)) break;
			while((!found)&&(tos<tolen)&&(Character.isLetter(toSrchStr.charAt(tos))))
				tos++;
			tos++;
		}
		return found;
	}
	
	public static String bumpDotNumber(String srchStr)
	{
		Object[] flags=fetchFlags(srchStr);
		if(flags==null) return srchStr;
		if(((Boolean)flags[FLAG_ALL]).booleanValue())
			return srchStr;
		if(((Integer)flags[FLAG_DOT]).intValue()==0)
			return "1."+((String)flags[FLAG_STR]);
		return (((Integer)flags[FLAG_DOT]).intValue()+1)+"."+((String)flags[FLAG_STR]);
	}
	
	public static Object[] fetchFlags(String srchStr)
	{
		if(srchStr.length()==0) return null;
		if((srchStr.length()<2)||(srchStr.equalsIgnoreCase("THE")))
		   return null;
		Object[] flags=new Object[3];
		
		boolean allFlag=false;
		if(srchStr.toUpperCase().startsWith("ALL "))
		{
			srchStr=srchStr.substring(4);
			allFlag=true;
		}
		else
		if(srchStr.equalsIgnoreCase("ALL"))
			allFlag=true;

		int dot=srchStr.lastIndexOf(".");
		int occurrance=0;
		if(dot>0)
		{
			String sub=srchStr.substring(dot+1);
			occurrance=Util.s_int(sub);
			if(occurrance>0)
				srchStr=srchStr.substring(0,dot);
			else
			{
				dot=srchStr.indexOf(".");
				sub=srchStr.substring(0,dot);
				occurrance=Util.s_int(sub);
				if(occurrance>0)
					srchStr=srchStr.substring(dot+1);
				else
					occurrance=0;
			}
		}
		flags[0]=srchStr;
		flags[1]=new Integer(occurrance);
		flags[2]=new Boolean(allFlag);
		return flags;
	}

	public static Environmental fetchEnvironmental(Vector list, String srchStr, boolean exactOnly)
	{
		Object[] flags=fetchFlags(srchStr);
		if(flags==null) return null;
		
		srchStr=(String)flags[FLAG_STR];
		int myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
		boolean allFlag=((Boolean)flags[FLAG_ALL]).booleanValue();
		
		if(exactOnly)
		{
			try
			{
				for(int i=0;i<list.size();i++)
				{
					Environmental thisThang=(Environmental)list.elementAt(i);
					if(thisThang.ID().equalsIgnoreCase(srchStr)
					   ||thisThang.name().equalsIgnoreCase(srchStr)
					   ||thisThang.Name().equalsIgnoreCase(srchStr))
						if((!allFlag)||(thisThang.displayText().length()>0))
							if((--myOccurrance)<=0)
								return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		else
		{
			myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
			try
			{
				for(int i=0;i<list.size();i++)
				{
					Environmental thisThang=(Environmental)list.elementAt(i);
					if((containsString(thisThang.name(),srchStr)||containsString(thisThang.Name(),srchStr))
					   &&((!allFlag)||(thisThang.displayText().length()>0)))
						if((--myOccurrance)<=0)
							return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
			myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
			try
			{
				for(int i=0;i<list.size();i++)
				{
					Environmental thisThang=(Environmental)list.elementAt(i);
					if((!(thisThang instanceof Ability))
					&&(thisThang.displayText().length()>0)
					&&(containsString(thisThang.displayText(),srchStr)))
						if((--myOccurrance)<=0)
							return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		return null;
	}

	public static Environmental fetchEnvironmental(Hashtable list, String srchStr, boolean exactOnly)
	{
		Object[] flags=fetchFlags(srchStr);
		if(flags==null) return null;
		
		srchStr=(String)flags[FLAG_STR];
		int myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
		boolean allFlag=((Boolean)flags[FLAG_ALL]).booleanValue();

		if(list.get(srchStr)!=null)
			return (Environmental)list.get(srchStr);
		if(exactOnly)
		{
			for(Enumeration e=list.elements();e.hasMoreElements();)
			{
				Environmental thisThang=(Environmental)e.nextElement();
				if(thisThang.ID().equalsIgnoreCase(srchStr)
				||thisThang.Name().equalsIgnoreCase(srchStr)
				||thisThang.name().equalsIgnoreCase(srchStr))
					if((!allFlag)||(thisThang.displayText().length()>0))
						if((--myOccurrance)<=0)
							return thisThang;
			}
		}
		else
		{
			myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
			for(Enumeration e=list.elements();e.hasMoreElements();)
			{
				Environmental thisThang=(Environmental)e.nextElement();
				if((containsString(thisThang.name(),srchStr)||containsString(thisThang.Name(),srchStr))
				&&((!allFlag)||(thisThang.displayText().length()>0)))
					if((--myOccurrance)<=0)
						return thisThang;
			}
			myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
			for(Enumeration e=list.elements();e.hasMoreElements();)
			{
				Environmental thisThang=(Environmental)e.nextElement();
				if((thisThang.displayText().length()>0)&&(containsString(thisThang.displayText(),srchStr)))
					if((--myOccurrance)<=0)
						return thisThang;
			}
		}
		return null;
	}

	public static Environmental fetchEnvironmental(Environmental[] list, String srchStr, boolean exactOnly)
	{
		Object[] flags=fetchFlags(srchStr);
		if(flags==null) return null;
		
		srchStr=(String)flags[FLAG_STR];
		int myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
		boolean allFlag=((Boolean)flags[FLAG_ALL]).booleanValue();
		
		if(exactOnly)
		{
			for(int i=0;i<list.length;i++)
			{
				Environmental thisThang=list[i];
				if(thisThang!=null)
					if(thisThang.ID().equalsIgnoreCase(srchStr)
					||thisThang.Name().equalsIgnoreCase(srchStr)
					||thisThang.name().equalsIgnoreCase(srchStr))
						if((!allFlag)||(thisThang.displayText().length()>0))
							if((--myOccurrance)<=0)
								return thisThang;
			}
		}
		else
		{
			myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
			for(int i=0;i<list.length;i++)
			{
				Environmental thisThang=list[i];
				if(thisThang!=null)
					if((containsString(thisThang.name(),srchStr)||containsString(thisThang.Name(),srchStr))
					   &&((!allFlag)||(thisThang.displayText().length()>0)))
						if((--myOccurrance)<=0)
							return thisThang;
			}
			myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
			for(int i=0;i<list.length;i++)
			{
				Environmental thisThang=list[i];
				if((thisThang!=null)&&(thisThang.displayText().length()>0))
					if(containsString(thisThang.displayText(),srchStr))
						if((--myOccurrance)<=0)
							return thisThang;
			}
		}
		return null;
	}

	public static Item fetchAvailableItem(Vector list, String srchStr, Item goodLocation, int wornReqCode, boolean exactOnly)
	{
		Object[] flags=fetchFlags(srchStr);
		if(flags==null) return null;
		
		srchStr=(String)flags[FLAG_STR];
		int myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
		boolean allFlag=((Boolean)flags[FLAG_ALL]).booleanValue();
		
		if(exactOnly)
		{
			try
			{
				for(int i=0;i<list.size();i++)
				{
					Item thisThang=(Item)list.elementAt(i);
					boolean beingWorn=!thisThang.amWearingAt(Item.INVENTORY);

					if((thisThang.container()==goodLocation)
					&&((wornReqCode==Item.WORN_REQ_ANY)||(beingWorn&(wornReqCode==Item.WORN_REQ_WORNONLY))||((!beingWorn)&&(wornReqCode==Item.WORN_REQ_UNWORNONLY)))
					&&(thisThang.ID().equalsIgnoreCase(srchStr)
					   ||(thisThang.Name().equalsIgnoreCase(srchStr))
					   ||(thisThang.name().equalsIgnoreCase(srchStr))))
						if((!allFlag)||(thisThang.displayText().length()>0))
							if((--myOccurrance)<=0)
								return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		else
		{
			try
			{
				for(int i=0;i<list.size();i++)
				{
					Item thisThang=(Item)list.elementAt(i);
					boolean beingWorn=!thisThang.amWearingAt(Item.INVENTORY);

					if((thisThang.container()==goodLocation)
					&&((wornReqCode==Item.WORN_REQ_ANY)||(beingWorn&(wornReqCode==Item.WORN_REQ_WORNONLY))||((!beingWorn)&&(wornReqCode==Item.WORN_REQ_UNWORNONLY)))
					&&((containsString(thisThang.name(),srchStr)||containsString(thisThang.Name(),srchStr))
					   &&((!allFlag)||(thisThang.displayText().length()>0))))
						if((--myOccurrance)<=0)
							return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
			myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
			try
			{
				for(int i=0;i<list.size();i++)
				{
					Item thisThang=(Item)list.elementAt(i);
					boolean beingWorn=!thisThang.amWearingAt(Item.INVENTORY);
					if((thisThang.container()==goodLocation)
					&&(thisThang.displayText().length()>0)
					&&((wornReqCode==Item.WORN_REQ_ANY)||(beingWorn&(wornReqCode==Item.WORN_REQ_WORNONLY))||((!beingWorn)&&(wornReqCode==Item.WORN_REQ_UNWORNONLY)))
					&&(containsString(thisThang.displayText(),srchStr)))
						if((--myOccurrance)<=0)
							return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		return null;
	}

	public static Environmental fetchAvailable(Vector list, String srchStr, Item goodLocation, int wornReqCode, boolean exactOnly)
	{
		Object[] flags=fetchFlags(srchStr);
		if(flags==null) return null;
		
		srchStr=(String)flags[FLAG_STR];
		int myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
		boolean allFlag=((Boolean)flags[FLAG_ALL]).booleanValue();
		
	    Environmental E=null;
	    Item thisThang=null;
		if(exactOnly)
		{
			try
			{
				for(int i=0;i<list.size();i++)
				{
				    E=(Environmental)list.elementAt(i);
				    if(E instanceof Item)
				    {
						thisThang=(Item)E;
						boolean beingWorn=!thisThang.amWearingAt(Item.INVENTORY);
	
						if((thisThang.container()==goodLocation)
						&&((wornReqCode==Item.WORN_REQ_ANY)||(beingWorn&(wornReqCode==Item.WORN_REQ_WORNONLY))||((!beingWorn)&&(wornReqCode==Item.WORN_REQ_UNWORNONLY)))
						&&(thisThang.ID().equalsIgnoreCase(srchStr)
						   ||(thisThang.Name().equalsIgnoreCase(srchStr))
						   ||(thisThang.name().equalsIgnoreCase(srchStr))))
							if((!allFlag)||(thisThang.displayText().length()>0))
								if((--myOccurrance)<=0)
									return thisThang;
				    }
				    else
					if(E.ID().equalsIgnoreCase(srchStr)
					||E.Name().equalsIgnoreCase(srchStr)
					||E.name().equalsIgnoreCase(srchStr))
						if((!allFlag)||(E.displayText().length()>0))
							if((--myOccurrance)<=0)
								return E;
				    }
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		else
		{
			try
			{
				for(int i=0;i<list.size();i++)
				{
					E=(Environmental)list.elementAt(i);
					if(E instanceof Item)
					{
					    thisThang=(Item)E;
						boolean beingWorn=!thisThang.amWearingAt(Item.INVENTORY);
	
						if((thisThang.container()==goodLocation)
						&&((wornReqCode==Item.WORN_REQ_ANY)||(beingWorn&(wornReqCode==Item.WORN_REQ_WORNONLY))||((!beingWorn)&&(wornReqCode==Item.WORN_REQ_UNWORNONLY)))
						&&((containsString(thisThang.name(),srchStr)||containsString(thisThang.Name(),srchStr))
						   &&((!allFlag)||(thisThang.displayText().length()>0))))
							if((--myOccurrance)<=0)
								return thisThang;
					}
					else
					if((containsString(E.name(),srchStr)||containsString(E.Name(),srchStr))
				    &&((!allFlag)||(E.displayText().length()>0)))
						if((--myOccurrance)<=0)
							return E;
					    
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
			myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
			try
			{
				for(int i=0;i<list.size();i++)
				{
					E=(Environmental)list.elementAt(i);
					if(E instanceof Item)
					{
					    thisThang=(Item)E;
						boolean beingWorn=!thisThang.amWearingAt(Item.INVENTORY);
						if((thisThang.container()==goodLocation)
						&&(thisThang.displayText().length()>0)
						&&((wornReqCode==Item.WORN_REQ_ANY)||(beingWorn&(wornReqCode==Item.WORN_REQ_WORNONLY))||((!beingWorn)&&(wornReqCode==Item.WORN_REQ_UNWORNONLY)))
						&&(containsString(thisThang.displayText(),srchStr)))
							if((--myOccurrance)<=0)
								return thisThang;
					}
					else
					if((E.displayText().length()>0)
					&&(containsString(E.displayText(),srchStr)))
						if((--myOccurrance)<=0)
							return E;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		return null;
	}

	public static Environmental parseShopkeeper(MOB mob, Vector commands, String error)
	{
		if(commands.size()==0)
		{
            if(error.length()>0) mob.tell(error);
			return null;
		}
		commands.removeElementAt(0);

		Vector V=CoffeeShops.getAllShopkeepers(mob.location(),mob);
		if(V.size()==0)
		{
            if(error.length()>0) mob.tell(error);
			return null;
		}
		if(V.size()>1)
		{
			if(commands.size()<2)
			{
                if(error.length()>0) mob.tell(error);
				return null;
			}
            String what=Util.combine(commands,0);
            Environmental shopkeeper=EnglishParser.fetchEnvironmental(V,what,false);
            if((shopkeeper==null)&&(what.equals("shop")||what.equals("the shop")))
                for(int v=0;v<V.size();v++)
                    if(V.elementAt(v) instanceof Area)
                    { shopkeeper=(Environmental)V.elementAt(v); break;}
			if((shopkeeper!=null)&&(CoffeeShops.getShopKeeper(shopkeeper)!=null)&&(Sense.canBeSeenBy(shopkeeper,mob)))
				commands.removeElementAt(commands.size()-1);
			else
			{
				mob.tell("You don't see anyone called '"+(String)commands.lastElement()+"' here buying or selling.");
				return null;
			}
			return shopkeeper;
		}
		Environmental shopkeeper=(Environmental)V.firstElement();
		if(commands.size()>1)
		{
			MOB M=mob.location().fetchInhabitant((String)commands.lastElement());
			if((M!=null)&&(CoffeeShops.getShopKeeper(M)!=null)&&(Sense.canBeSeenBy(M,mob)))
			{
				shopkeeper=M;
				commands.removeElementAt(commands.size()-1);
			}
		}
		return shopkeeper;
	}
	
	public static Vector fetchItemList(Environmental from,
									   MOB mob,
                                       Item container,
                                       Vector commands,
                                       int preferredLoc,
                                       boolean visionMatters)
	{
		int addendum=1;
		String addendumStr="";
		Vector V=new Vector();
		
		int maxToItem=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(Util.s_int((String)commands.firstElement())>0))
		{
			maxToItem=Util.s_int((String)commands.firstElement());
			commands.setElementAt("all",0);
		}
		
		String name=Util.combine(commands,0);
		boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
		if(name.toUpperCase().startsWith("ALL.")){ allFlag=true; name="ALL "+name.substring(4);}
		if(name.toUpperCase().endsWith(".ALL")){ allFlag=true; name="ALL "+name.substring(0,name.length()-4);}
		do
		{
			Environmental item=null;
			if(from instanceof MOB)
			{
				if(preferredLoc==Item.WORN_REQ_UNWORNONLY)
					item=((MOB)from).fetchCarried(container,name+addendumStr);
				else
				if(preferredLoc==Item.WORN_REQ_WORNONLY)
					item=((MOB)from).fetchWornItem(name+addendumStr);
				else
					item=((MOB)from).fetchInventory(null,name+addendumStr);
			}
			else
			if(from instanceof Room)
				item=((Room)from).fetchFromMOBRoomFavorsItems(mob,container,name+addendumStr,preferredLoc);
			if((item!=null)
			&&(item instanceof Item)
			&&((!visionMatters)||(Sense.canBeSeenBy(item,mob))||(item instanceof Light))
			&&(!V.contains(item)))
				V.addElement(item);
			if(item==null) return V;
			addendumStr="."+(++addendum);
		}
		while((allFlag)&&(addendum<=maxToItem));
		return V;
	}
	
	public static long numPossibleGold(Environmental mine, String itemID)
	{
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		if(Util.isInteger(itemID))
		{
            long num=Util.s_long(itemID);
		    if(mine instanceof MOB)
		    {
		        Vector V=BeanCounter.getStandardCurrency((MOB)mine,BeanCounter.getCurrency(mine));
		        for(int v=0;v<V.size();v++)
		            if(((Coins)V.elementAt(v)).getNumberOfCoins()>=num)
		                return num;
		        V=BeanCounter.getStandardCurrency((MOB)mine,null);
		        for(int v=0;v<V.size();v++)
		            if(((Coins)V.elementAt(v)).getNumberOfCoins()>=num)
		                return num;
		    }
		    return Util.s_long(itemID);
		}
	    Vector V=Util.parse(itemID);
	    if((V.size()>1)
	    &&((Util.isInteger((String)V.firstElement()))
        &&(matchAnyCurrencySet(Util.combine(V,1))!=null)))
	        return Util.s_long((String)V.firstElement());
	    else
	    if((V.size()>1)&&(((String)V.firstElement()).equalsIgnoreCase("all")))
	    {
	        String currency=matchAnyCurrencySet(Util.combine(V,1));
	        if(currency!=null)
	        {
	            if(mine instanceof MOB)
	            {
		            Vector V2=BeanCounter.getStandardCurrency((MOB)mine,currency);
		            double denomination=EnglishParser.matchAnyDenomination(currency,Util.combine(V,1));
		            Coins C=null;
		            for(int v2=0;v2<V2.size();v2++)
		            {
		                C=(Coins)V2.elementAt(v2);
		                if(C.getDenomination()==denomination)
		                    return C.getNumberOfCoins();
		            }
	            }
	            return 1;
	        }
	    }
	    else
	    if((V.size()>0)&&(matchAnyCurrencySet(Util.combine(V,0))!=null))
	        return 1;
		return 0;
	}
	public static String numPossibleGoldCurrency(Environmental mine, String itemID)
	{
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		if(Util.isInteger(itemID))
		{
		    long num=Util.s_long(itemID);
            if(mine instanceof MOB)
            {
    	        Vector V=BeanCounter.getStandardCurrency((MOB)mine,BeanCounter.getCurrency(mine));
    	        for(int v=0;v<V.size();v++)
    	            if(((Coins)V.elementAt(v)).getNumberOfCoins()>=num)
    	                return ((Coins)V.elementAt(v)).getCurrency();
    	        V=BeanCounter.getStandardCurrency((MOB)mine,null);
    	        for(int v=0;v<V.size();v++)
    	            if(((Coins)V.elementAt(v)).getNumberOfCoins()>=num)
    	                return ((Coins)V.elementAt(v)).getCurrency();
            }
            return BeanCounter.getCurrency(mine);
		}
	    Vector V=Util.parse(itemID);
	    if((V.size()>1)&&(Util.isInteger((String)V.firstElement())))
	        return EnglishParser.matchAnyCurrencySet(Util.combine(V,1));
	    else
	    if((V.size()>1)&&(((String)V.firstElement()).equalsIgnoreCase("all")))
	        return matchAnyCurrencySet(Util.combine(V,1));
	    else
	    if(V.size()>0)
	        return EnglishParser.matchAnyCurrencySet(Util.combine(V,0));
		return BeanCounter.getCurrency(mine);
	}
    
    
	public static double numPossibleGoldDenomination(Environmental mine, String currency, String itemID)
	{
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		if(Util.isInteger(itemID))
		{
		    long num=Util.s_long(itemID);
            if(mine instanceof MOB)
            {
    	        Vector V=BeanCounter.getStandardCurrency((MOB)mine,currency);
    	        for(int v=0;v<V.size();v++)
    	            if(((Coins)V.elementAt(v)).getNumberOfCoins()>=num)
    	                return ((Coins)V.elementAt(v)).getDenomination();
            }
		    return BeanCounter.getLowestDenomination(currency);
		}
	    Vector V=Util.parse(itemID);
	    if((V.size()>1)&&(Util.isInteger((String)V.firstElement())))
	        return matchAnyDenomination(currency,Util.combine(V,1));
	    else
	    if((V.size()>1)&&(((String)V.firstElement()).equalsIgnoreCase("all")))
	        return matchAnyDenomination(currency,Util.combine(V,1));
	    else
	    if(V.size()>0)
	        return matchAnyDenomination(currency,Util.combine(V,0));
		return 0;
	}
	
	public static String matchAnyCurrencySet(String itemID)
	{
	    Vector V=BeanCounter.getAllCurrencies();
	    Vector V2=null;
	    for(int v=0;v<V.size();v++)
	    {
	        V2=BeanCounter.getDenominationNameSet((String)V.elementAt(v));
	        for(int v2=0;v2<V2.size();v2++)
	        {
	            String s=(String)V2.elementAt(v2);
	            if(s.toLowerCase().endsWith("(s)")) 
	                s=s.substring(0,s.length()-3)+"s";
	            if(containsString(s,itemID))
	                return (String)V.elementAt(v);
	        }
	    }
	    return null;
	}
	
	public static double matchAnyDenomination(String currency, String itemID)
	{
        DVector V2=BeanCounter.getCurrencySet(currency);
        itemID=itemID.toUpperCase();
        String s=null;
        if(V2!=null)
        for(int v2=0;v2<V2.size();v2++)
        {
            s=((String)V2.elementAt(v2,2)).toUpperCase();
            if(s.endsWith("(S)")) 
                s=s.substring(0,s.length()-3)+"S";
            if(containsString(s,itemID))
                return ((Double)V2.elementAt(v2,1)).doubleValue();
            else
            if((s.length()>0)
            &&(containsString(s,itemID)))
                return ((Double)V2.elementAt(v2,1)).doubleValue();
        }
	    return 0.0;
	}
	
	public static Item possibleRoomGold(MOB seer, Room room, Item container, String itemID)
	{
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		long gold=0;
		if(Util.isInteger(itemID))
		{
		    gold=Util.s_long(itemID);
		    itemID="";
		}
		else
		{
		    Vector V=Util.parse(itemID);
		    if((V.size()>1)&&(Util.isInteger((String)V.firstElement())))
		        gold=Util.s_long((String)V.firstElement());
		    else
		        return null;
		    itemID=Util.combine(V,1);
		}
		if(gold>0)
		{
			for(int i=0;i<room.numItems();i++)
			{
				Item I=room.fetchItem(i);
				if((I.container()==container)
				&&(I instanceof Coins)
				&&(Sense.canBeSeenBy(I,seer))
				&&((itemID.length()==0)||(containsString(I.name(),itemID))))
				{
					if(((Coins)I).getNumberOfCoins()<=gold)
						return I;
					((Coins)I).setNumberOfCoins(((Coins)I).getNumberOfCoins()-gold);
					Coins C=(Coins)CMClass.getItem("StdCoins");
					C.setCurrency(((Coins)I).getCurrency());
					C.setNumberOfCoins(gold);
					C.setDenomination(((Coins)I).getDenomination());
					C.setContainer(container);
					C.recoverEnvStats();
					room.addItem(C);
					C.setDispossessionTime(I.dispossessionTime());
					return C;
				}
			}
		}
		return null;
	}

	public static Item bestPossibleGold(MOB mob, Container container, String itemID)
	{
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		long gold=0;
		double denomination=0.0;
		String currency=BeanCounter.getCurrency(mob);
		if(Util.isInteger(itemID))
		{
		    gold=Util.s_long(itemID);
	        Vector V=BeanCounter.getStandardCurrency(mob,BeanCounter.getCurrency(mob));
	        boolean skipNextCheck=false;
	        for(int v=0;v<V.size();v++)
	            if(((Coins)V.elementAt(v)).getNumberOfCoins()>=gold)
	            {
	                currency=((Coins)V.elementAt(v)).getCurrency();
	                denomination=((Coins)V.elementAt(v)).getDenomination();
	                break;
	            }
	        if(!skipNextCheck)
	        {
		        V=BeanCounter.getStandardCurrency(mob,null);
		        for(int v=0;v<V.size();v++)
		            if(((Coins)V.elementAt(v)).getNumberOfCoins()>=gold)
		            {
		                currency=((Coins)V.elementAt(v)).getCurrency();
		                denomination=((Coins)V.elementAt(v)).getDenomination();
		                break;
		            }
	        }
		}
		else
		{
		    Vector V=Util.parse(itemID);
		    if(V.size()<1) return null;
		    if((!Util.isInteger((String)V.firstElement()))
		    &&(!((String)V.firstElement()).equalsIgnoreCase("all")))
		        V.insertElementAt("1",0);
		    Item I=mob.fetchInventory(container,Util.combine(V,1));
		    if(I instanceof Coins)
		    {
		        if(((String)V.firstElement()).equalsIgnoreCase("all"))
		            gold=((Coins)I).getNumberOfCoins();
		        else
			        gold=Util.s_long((String)V.firstElement());
		        currency=((Coins)I).getCurrency();
		        denomination=((Coins)I).getDenomination();
		    }
		    else
		        return null;
		}
		if(gold>0)
		{
			if(BeanCounter.getNumberOfCoins(mob,currency,denomination)>=gold)
			{
			    BeanCounter.subtractMoney(mob,currency,denomination,Util.mul(denomination,gold));
			    Coins C=(Coins)CMClass.getItem("StdCoins");
			    C.setCurrency(currency);
			    C.setDenomination(denomination);
			    C.setNumberOfCoins(gold);
				C.recoverEnvStats();
				mob.addInventory(C);
				return C;
			}
			mob.tell("You don't have that many "+BeanCounter.getDenominationName(currency,denomination)+".");
			Vector V=BeanCounter.getStandardCurrency(mob,currency);
			for(int v=0;v<V.size();v++)
			    if(((Coins)V.elementAt(v)).getDenomination()==denomination)
			        return (Item)V.elementAt(v);
		}
		return null;
	}

	public static Vector possibleContainers(MOB mob, Vector commands, int wornReqCode, boolean withContentOnly)
	{
		Vector V=new Vector();
		if(commands.size()==1)
			return V;

		int fromDex=-1;
		int containerDex=commands.size()-1;
		for(int i=commands.size()-2;i>0;i--)
		    if(((String)commands.elementAt(i)).equalsIgnoreCase("from"))
		    { 
		        fromDex=i; 
			    containerDex=i+1;
			    if(((containerDex+1)<commands.size())
			    &&((((String)commands.elementAt(containerDex)).equalsIgnoreCase("all"))
			    ||(Util.s_int((String)commands.elementAt(containerDex))>0)))
			        containerDex++;
			    break;
			}
		
		String possibleContainerID=Util.combine(commands,containerDex);
		    
		boolean allFlag=false;
		String preWord="";
		if(possibleContainerID.equalsIgnoreCase("all"))
			allFlag=true;
		else
		if(containerDex>1)
			preWord=(String)commands.elementAt(containerDex-1);

		int maxContained=Integer.MAX_VALUE;
		if(Util.s_int(preWord)>0)
		{
			maxContained=Util.s_int(preWord);
			commands.setElementAt("all",containerDex-1);
			containerDex--;
			preWord="all";
		}

		if(preWord.equalsIgnoreCase("all")){ allFlag=true; possibleContainerID="ALL "+possibleContainerID;}
		else
		if(possibleContainerID.toUpperCase().startsWith("ALL.")){ allFlag=true; possibleContainerID="ALL "+possibleContainerID.substring(4);}
		else
		if(possibleContainerID.toUpperCase().endsWith(".ALL")){ allFlag=true; possibleContainerID="ALL "+possibleContainerID.substring(0,possibleContainerID.length()-4);}

		int addendum=1;
		String addendumStr="";
		do
		{
			Environmental thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,possibleContainerID+addendumStr,wornReqCode);
			if((thisThang!=null)
			&&(thisThang instanceof Item)
			&&(((Item)thisThang) instanceof Container)
			&&((!withContentOnly)||(((Container)thisThang).getContents().size()>0))
            &&(Sense.canBeSeenBy(thisThang,mob)||mob.isMine(thisThang)))
			{
				V.addElement(thisThang);
				if(V.size()==1)
				{
				    while((fromDex>=0)&&(commands.size()>fromDex))
						commands.removeElementAt(fromDex);
				    while(commands.size()>containerDex)
						commands.removeElementAt(containerDex);
					preWord="";
				}
			}
			if(thisThang==null)
			    return V;
			addendumStr="."+(++addendum);
		}
		while((allFlag)&&(addendum<=maxContained));
		return V;
	}

	public static Item possibleContainer(MOB mob, Vector commands, boolean withStuff, int wornReqCode)
	{
		if(commands.size()==1)
			return null;

		int fromDex=-1;
		int containerDex=commands.size()-1;
		for(int i=commands.size()-2;i>=1;i--)
		    if(((String)commands.elementAt(i)).equalsIgnoreCase("from"))
		    { fromDex=i; containerDex=i+1;  break;}
		String possibleContainerID=Util.combine(commands,containerDex);
		
		Environmental thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,possibleContainerID,wornReqCode);
		if((thisThang!=null)
		&&(thisThang instanceof Item)
		&&(((Item)thisThang) instanceof Container)
		&&((!withStuff)||(((Container)thisThang).getContents().size()>0)))
		{
		    while((fromDex>=0)&&(commands.size()>fromDex))
				commands.removeElementAt(fromDex);
		    while(commands.size()>containerDex)
				commands.removeElementAt(containerDex);
			return (Item)thisThang;
		}
		return null;
	}

    public static String promptText(MOB mob, String oldVal, int showNumber, int showFlag, String FieldDisp)
    throws IOException
    {
        return promptText(mob,oldVal,showNumber,showFlag,FieldDisp,false,false);
    }
    public static String promptText(MOB mob, String oldVal, int showNumber, int showFlag, String FieldDisp, boolean emptyOK)
    throws IOException
    {
        return promptText(mob,oldVal,showNumber,showFlag,FieldDisp,emptyOK,false);
    }
    public static String promptText(MOB mob, String oldVal, int showNumber, int showFlag, String FieldDisp, boolean emptyOK, boolean rawPrint)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return oldVal;
        if(rawPrint)
            mob.session().rawPrintln(showNumber+". "+FieldDisp+": '"+oldVal+"'.");
        else
            mob.tell(showNumber+". "+FieldDisp+": '"+oldVal+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return oldVal;
        String newName=mob.session().prompt("Enter a new value "+(emptyOK?"(or NULL)":"")+"\n\r:","");
        if((newName.equalsIgnoreCase("null"))&&(emptyOK))
            return "";
        else
        if(newName.length()>0)
            return newName;
        else
        {
            mob.tell("(no change)");
            return oldVal;
        }
    }
    public static boolean promptBool(MOB mob, boolean oldVal, int showNumber, int showFlag, String FieldDisp)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return oldVal;
        mob.tell(showNumber+". "+FieldDisp+": '"+oldVal+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return oldVal;
        String newName=mob.session().prompt("Enter true or false:","");
        if(newName.toUpperCase().startsWith("T")||newName.toUpperCase().startsWith("F"))
            return newName.toUpperCase().startsWith("T");
        mob.tell("(no change)");
        return oldVal;
    }
    
    public static double promptDouble(MOB mob, double oldVal, int showNumber, int showFlag, String FieldDisp)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return oldVal;
        mob.tell(showNumber+". "+FieldDisp+": '"+oldVal+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return oldVal;
        String newName=mob.session().prompt("Enter a new value:","");
        if(Util.isNumber(newName))
            return Util.s_double(newName);
        mob.tell("(no change)");
        return oldVal;
    }
    
    public static int promptInteger(MOB mob, int oldVal, int showNumber, int showFlag, String FieldDisp)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return oldVal;
        mob.tell(showNumber+". "+FieldDisp+": '"+oldVal+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return oldVal;
        String newName=mob.session().prompt("Enter a new value:","");
        if(Util.isInteger(newName))
            return Util.s_int(newName);
        mob.tell("(no change)");
        return oldVal;
    }
}
