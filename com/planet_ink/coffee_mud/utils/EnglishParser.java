package com.planet_ink.coffee_mud.utils;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class EnglishParser extends Scriptable implements Tickable
{
	private EnglishParser(){};
	public String ID(){return "EnglishParser";}
	public String name(){return "CoffeeMuds English Parser";}
	public long getTickStatus(){return Tickable.STATUS_NOT;}
	public boolean tick(Tickable ticking, int tickID)
	{
		return true;
	}

	// these should be checked after pmap prelim check.
	private static String[] universalStarters={
		"go ",
		"go and ",
		"i want you to ",
		"i command you to ",
		"i order you to ",
		"you are commanded to ",
		"please ",
		"to ",
	    "you are ordered to "};

	private static String[] responseStarters={
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
	private static String[] universalRejections={
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
	private static String[][] pmap={
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
		{"tell %m %g","mobfind %m;say %m %g"},
		{"say %g to %m","mobfind %m;say %m %g"},
		{"tell %g to %m","mobfind %m;say %m %g"},
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

	private static Vector findMatch(Vector req)
	{
		Vector possibilities=new Vector();
		Hashtable map=new Hashtable();
		for(int p=0;p<pmap.length;p++)
		{
			Vector chk=Util.parse((String)pmap[p][0]);
			int ci=0,ri=0;
			boolean reject=false;
			while((!reject)&&(ci<chk.size())&&(ri<req.size()))
			{
				if(chk.elementAt(ci).equals(req.elementAt(ri)))
				{ ci++; ri++; reject=false; continue;}
				if(((String)chk.elementAt(ci)).startsWith("%"))
				{
					switch(((String)chk.elementAt(ci)).charAt(1))
					{
					case 's':
						if(Socials.FetchSocial((String)req.elementAt(ri),true)==null)
							reject=true;
						else
						{
							map.put("%s",req.elementAt(ri));
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
						String code=(String)chk.elementAt(ci);
						int remain=chk.size()-ci;
						String str=(String)req.elementAt(ri);
						ri++;
						ci++;
						reject=false;
						while(ri<=(req.size()-remain))
						{
							String nxt="";
							if(ci<chk.size())
							{
								nxt=(String)chk.elementAt(ci);
								if(nxt.startsWith("%"))
									nxt="";
							}
							if((nxt.length()>0)
							&&(ri<req.size())
							&&(req.elementAt(ri).equals(nxt)))
							   break;
							if(ri<req.size())
								str=str+" "+((String)req.elementAt(ri));
							ri++;
						}
						map.put(code,str);
						break;
					case 'k':
						if((Resources.getResource("ABILITY WORDS")!=null)
						&&(!(((Vector)Resources.getResource("ABILITY WORDS")).contains(((String)req.elementAt(ri)).toUpperCase()))))
						   reject=true;
						else
						{
							map.put("%k",req.elementAt(ri));
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
			if((reject)||(ci!=chk.size())||(ri!=req.size()))
			{
				map.clear();
				continue;
			}
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

	private static String cleanArticles(String s)
	{
		String[] articles={"a","an","all of","some one","a pair of","one of","all","the","some"};
		Vector V=Util.parse(s);
		while(V.size()>1)
		{
			for(int a=0;a<articles.length;a++)
			{
				if(s.toLowerCase().startsWith(articles[a]+" "))
					s=s.substring(articles[a].length()+1);
			}
			V=Util.parse(s);
		}
		return s;
	}

	private static boolean foundIn(Vector V, String that)
	{
		for(int v=0;v<V.size();v++)
		{
			if(containsString((String)V.elementAt(v),that))
				return true;
		}
		return false;
	}

	public static geasStep processRequest(MOB you, MOB me, String req)
	{
		Vector REQ=Util.parse(req.toLowerCase().trim());
		for(int v=0;v<REQ.size();v++)
			REQ.setElementAt(cleanWord((String)REQ.elementAt(v)),v);
		Vector poss=findMatch(REQ);
		if(poss.size()==0)
		{
			req=Util.combine(REQ,0);
			boolean doneSomething=true;
			while(doneSomething)
			{
				doneSomething=false;
				for(int i=0;i<universalStarters.length;i++)
					if(req.startsWith(universalStarters[i]))
					{
						doneSomething=true;
						req=req.substring(universalStarters.length).trim();
					}
			}
			REQ=Util.parse(req);
			poss=findMatch(REQ);
		}
		geasStep g=new geasStep();
		if(poss.size()==0)
			g.que.addElement("wanderquery "+req);
		else
		{
			Vector likelys=new Vector();
			Vector itemList=new Vector();
			Vector mobList=new Vector();
			Vector roomStuff=new Vector();
			for(Enumeration e=CMMap.rooms();e.hasMoreElements();)
			{
				Room R=(Room)e.nextElement();
				roomStuff.addElement(R.roomTitle());
				roomStuff.addElement(R.roomDescription());
				for(int i=0;i<R.numItems();i++)
				{
					Item I=R.fetchItem(i);
					if((I!=null)&&(!itemList.contains(I.name())))
					{
					   itemList.addElement(I.name());
					   itemList.addElement(I.displayText());
					}
				}
				for(Enumeration p=CMMap.players();p.hasMoreElements();)
				{
					MOB M=(MOB)p.nextElement();
					if((M!=null)&&(!mobList.contains(M.name())))
					{
					   mobList.addElement(M.name());
					   mobList.addElement(M.displayText());
					}
				}
				for(int m=0;m<R.numInhabitants();m++)
				{
					MOB M=R.fetchInhabitant(m);
					if((M!=null)&&(!mobList.contains(M.name())))
					{
					   mobList.addElement(M.name());
					   mobList.addElement(M.displayText());
					   for(int i=0;i<M.inventorySize();i++)
					   {
							Item I=M.fetchInventory(i);
							if((I!=null)&&(!itemList.contains(I.name())))
							{
							   itemList.addElement(I.name());
							   itemList.addElement(I.displayText());
							}
					   }
					   if(CoffeeUtensils.getShopKeeper(M)!=null)
					   {
						   Vector inven=CoffeeUtensils.getShopKeeper(M).getUniqueStoreInventory();
						   for(int a=0;a<inven.size();a++)
						   {
							   if(inven.elementAt(a) instanceof Item)
							   {
									Item I=(Item)inven.elementAt(a);
									if((I!=null)&&(!itemList.contains(I.name())))
									{
									   itemList.addElement(I.name());
									   itemList.addElement(I.displayText());
									}
							   }
							   else
							   if(inven.elementAt(a) instanceof MOB)
							   {
									MOB M2=(MOB)inven.elementAt(a);
									if((M2!=null)&&(!mobList.contains(M2.name())))
									{
										mobList.addElement(M2.name());
										mobList.addElement(M2.displayText());
									}
							   }
						   }
					   }
					}
				}
			}
			for(int p=0;p<poss.size();p++)
			{
				Hashtable map=(Hashtable)poss.elementAt(p);
				String that=(String)map.get("%m");
				if(that!=null)
				{
					if(!foundIn(mobList,that))
						continue;
				}
				that=(String)map.get("%i");
				if(that!=null)
				{
					if(!foundIn(itemList,that))
						continue;
				}
				that=(String)map.get("%r");
				if(that!=null)
				{
					if(!foundIn(roomStuff,that))
						continue;
				}
				likelys.addElement(map);
			}
			if(likelys.size()==0) likelys=poss;
			Hashtable map=(Hashtable)likelys.elementAt(Dice.roll(1,likelys.size(),-1));
			Vector all=Util.parseSemicolons((String)map.get("INSTR"),true);
			g.que=new Vector();
			for(int a=0;a<all.size();a++)
				g.que.addElement(Util.parse((String)all.elementAt(a)));
			if(you!=null)	map.put("%c",you.name());
			map.put("%n",me.name());
			g.you=you;
			g.me=me;
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
			itemList.clear();
			mobList.clear();
			roomStuff.clear();
		}
		return g;
	}

	public final static int STEP_EVAL=0;
	public final static int STEP_INT1=1;
	public final static int STEP_INT2=2;
	public final static int STEP_INT3=3;
	public final static int STEP_INT4=4;
	public final static int STEP_INT5=5;
	public final static int STEP_ALLDONE=-999;
	public static class geasStep
	{
		public Vector que=new Vector();
		public int step=STEP_EVAL;
		public Vector bothered=new Vector();
		public MOB bothering=null;
		public MOB you=null;
		public MOB me=null;

		public void botherOrMove(String msgOrQ, int moveCode)
		{
			bothering=null;
			if((me==null)||(me.location()==null)) return;
			if(msgOrQ!=null)
				for(int m=0;m<me.location().numInhabitants();m++)
				{
					MOB M=me.location().fetchInhabitant(m);
					if((M!=null)&&(M!=me)&&(!bothered.contains(M)))
					{
						CommonMsgs.say(me,M,msgOrQ,false,false);
						bothering=M;
						bothered.addElement(M);
						return;
					}
				}
			if(!bothered.contains(me.location()))
				bothered.addElement(me.location());
			if(moveCode==0)
			{
				if(!MUDTracker.beMobile(me,true,true,false,true,bothered))
					MUDTracker.beMobile(me,true,true,false,false,null);
			}
			else
			{
				if(!MUDTracker.beMobile(me,true,true,true,false,bothered))
					MUDTracker.beMobile(me,true,true,false,false,null);
			}
		}

		public void sayResponse(MOB speaker, MOB target, String response)
		{
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
						return;
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
					return;
				if(response.trim().length()==0)
					return;
				bothering=null;
				que.insertElementAt(Util.parse("find150 \""+response+"\""),0);
				step=STEP_EVAL;
			}
		}

		public void step()
		{
			if(que.size()==0)
			{
				step=STEP_ALLDONE;
				return;
			}
			Vector cur=(Vector)que.firstElement();
			if(cur.size()==0)
			{
				step=STEP_EVAL;
				que.removeElementAt(0);
				return;
			}
			String s=(String)cur.firstElement();
			if(s.equalsIgnoreCase("itemfind"))
			{
				String item=Util.combine(cur,1);
				if((Util.isNumber(item)&&(Util.s_int(item)>0)))
				{
					if(me.getMoney()>=Util.s_int(item))
					{
						step=STEP_EVAL;
						que.removeElementAt(0);
						CommonMsgs.say(me,null,"I got the money!",false,false);
						return;
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
						return;
					}
					if(I.container()!=null)
					{
						CommonMsgs.get(me,I.container(),I,false);
						return;
					}
					que.removeElementAt(0);
					CommonMsgs.say(me,null,"I got "+I.name()+"!",false,false);
					return;
				}
				// is it just sitting around?
				I=me.location().fetchItem(null,item);
				if((I!=null)&&(Sense.canBeSeenBy(I,me)))
				{
					step=STEP_EVAL;
					CommonMsgs.get(me,null,I,false);
					return;
				}
				// is it in a container?
				I=me.location().fetchAnyItem(item);
				if((I!=null)&&(I.container()!=null)
				   &&(I.container() instanceof Container)
				   &&(((Container)I.container()).isOpen()))
				{
					step=STEP_EVAL;
					CommonMsgs.get(me,I.container(),I,false);
					return;
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
								return;
							}
							else
							if(step==STEP_INT1)
							{
								step=STEP_INT2;
								return;
							}
							else
							if(step==STEP_INT2)
							{
								CommonMsgs.say(me,M,"I MUST HAVE '"+I.name().toUpperCase()+".  GIVE IT TO ME NOW!!!!",false,false);
								step=STEP_INT3;
								return;
							}
							else
							if(step==STEP_INT3)
							{
								step=STEP_INT4;
								return;
							}
							else
							if(step==STEP_INT4)
							{
								MUDFight.postAttack(me,M,me.fetchWieldedItem());
								step=STEP_EVAL;
								return;
							}
						}
						ShopKeeper sk=CoffeeUtensils.getShopKeeper(M);
						if((!item.equals("coins"))&&(sk!=null)&&(sk.getStock(item,me)!=null))
						{
							Environmental E=sk.getStock(item,me);
							if((E!=null)&&(E instanceof Item))
							{
								int price=sk.yourValue(me,E,true)[0];
								if(price<=MoneyUtils.totalMoney(me))
								{
									me.enqueCommand(Util.parse("BUY \""+E.name()+"\""),0);
									step=STEP_EVAL;
									return;
								}
								else
								{
									price=price-MoneyUtils.totalMoney(me);
									que.insertElementAt(Util.parse("itemfind "+sk.yourValue(me,E,true)),0);
									CommonMsgs.say(me,null,"Damn, I need "+price+" gold.",false,false);
									step=STEP_EVAL;
									return;
								}
							}
						}
					}
				}
				// if asked someone something, give them time to respond.
				if((step>STEP_EVAL)&&(step<=STEP_INT4)&&(bothered!=null))
				{	step++; return;}
				step=STEP_EVAL;
				botherOrMove("Can you tell me where to find "+Util.combine(cur,1)+"?",0);
				if(bothered!=null) step=STEP_INT1;
			}
			else
			if(s.equalsIgnoreCase("mobfind"))
			{
				String name=Util.combine(cur,1);
				if(name.equalsIgnoreCase("you")) name=me.name();
				if(name.equalsIgnoreCase("yourself")) name=me.name();
				if(you!=null)
				{
					if(name.equals("me")) name=you.name();
					if(name.equals("myself")) name=you.name();
					if(name.equals("my")) name=you.name();
				}

				MOB M=me.location().fetchInhabitant(name);
				if((M!=null)&&(M!=me)&&(Sense.canBeSeenBy(M,me)))
				{
					step=STEP_EVAL;
					que.removeElementAt(0);
					return;
				}

				// if asked someone something, give them time to respond.
				if((step>STEP_EVAL)&&(bothering!=null)&&((step<=STEP_INT4)||(bothering.isMonster())))
				{	step++; return;}
				step=STEP_EVAL;
				int code=0;
				if((you!=null)&&(you.name().equalsIgnoreCase(name)))
					code=1;
				botherOrMove("Can you tell me where to find "+name+"?",code);
				if(bothering!=null) step=STEP_INT1;
			}
			else
			if(s.toLowerCase().startsWith("find"))
			{
				String name=Util.combine(cur,1);
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
					return;
				}

				if(containsString(me.location().name(),name)
				   ||containsString(me.location().displayText(),name)
				   ||containsString(me.location().description(),name))
				{
					step=STEP_EVAL;
					que.removeElementAt(0);
					return;
				}
				MOB M=me.location().fetchInhabitant(name);
				if((M!=null)&&(M!=me)&&(Sense.canBeSeenBy(M,me)))
				{
					step=STEP_EVAL;
					que.removeElementAt(0);
					return;
				}
				// is it just sitting around?
				Item I=me.location().fetchItem(null,name);
				if((I!=null)&&(Sense.canBeSeenBy(I,me)))
				{
					step=STEP_EVAL;
					CommonMsgs.get(me,null,I,false);
					return;
				}
				if((s.length()>4)&&(Util.isNumber(s.substring(4))))
				{
					int x=Util.s_int(s.substring(4));
					if((--x)<0)
					{
						que.removeElementAt(0);
						step=STEP_EVAL;
						return;
					}
					cur.setElementAt("find"+x,0);
				}

				// if asked someone something, give them time to respond.
				if((step>STEP_EVAL)&&(bothering!=null)&&((step<=STEP_INT4)||(bothering.isMonster())))
				{	step++; return;}
				step=STEP_EVAL;
				if(s.length()>4)
					botherOrMove("Can you tell me where to find "+name+"?",0);
				else
					botherOrMove(null,0);
				if(bothering!=null) step=STEP_INT1;
			}
			else
			if(s.equalsIgnoreCase("wanderquery"))
			{
				// if asked someone something, give them time to respond.
				if((step>STEP_EVAL)&&(bothering!=null)&&((step<=STEP_INT4)||(bothering.isMonster())))
				{	step++; return;}
				step=STEP_EVAL;
				botherOrMove("Can you help me "+Util.combine(cur,1)+"?",0);
				if(bothering!=null) step=STEP_INT1;
			}
			else
			{
				step=STEP_EVAL;
				que.removeElementAt(0);
				me.enqueCommand(cur,0);
				return;
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
		if(C!=null) return C;

		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if(A.triggerStrings()!=null)
				for(int t=0;t<A.triggerStrings().length;t++)
					if(A.triggerStrings()[t].equalsIgnoreCase(firstWord))
						return A;
		}

		Social social=Socials.FetchSocial(commands,true);
		if(social!=null) return social;

		for(int c=0;c<ChannelSet.getNumChannels();c++)
		{
			if(ChannelSet.getChannelName(c).equalsIgnoreCase(firstWord))
			{
				C=CMClass.getCommand("Channel");
				if(C!=null) return C;
			}
			else
			if(("NO"+ChannelSet.getChannelName(c)).equalsIgnoreCase(firstWord))
			{
				C=CMClass.getCommand("NoChannel");
				if(C!=null) return C;
			}
		}
		
		// second, inexacting pass
		// first, exacting pass
		C=CMClass.findCommandByTrigger(firstWord,false);
		if(C!=null) return C;

		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if(A.triggerStrings()!=null)
				for(int t=0;t<A.triggerStrings().length;t++)
					if(A.triggerStrings()[t].toUpperCase().startsWith(firstWord))
						return A;
		}

		social=Socials.FetchSocial(commands,false);
		if(social!=null) return social;
		
		for(int c=0;c<ChannelSet.getNumChannels();c++)
		{
			if(ChannelSet.getChannelName(c).startsWith(firstWord))
			{
				C=CMClass.getCommand("Channel");
				if(C!=null) return C;
			}
			else
			if(("NO"+ChannelSet.getChannelName(c)).startsWith(firstWord))
			{
				C=CMClass.getCommand("NoChannel");
				if(C!=null) return C;
			}
		}
		return null;
	}

	private static boolean evokedBy(Ability thisAbility, String thisWord)
	{
		for(int i=0;i<thisAbility.triggerStrings().length;i++)
		{
			if(((String)thisAbility.triggerStrings()[i]).equalsIgnoreCase(thisWord))
				return true;
		}
		return false;
	}

	private static boolean evokedBy(Ability thisAbility, String thisWord, String secondWord)
	{
		for(int i=0;i<thisAbility.triggerStrings().length;i++)
		{
			if(((String)thisAbility.triggerStrings()[i]).equalsIgnoreCase(thisWord))
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
				if(evokableAbility!=null)
				{
					foundMoreThanOne=true;
					evokableAbility=null;
					break;
				}
				else
					evokableAbility=thisAbility;
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
		evokableAbility.invoke(mob,commands,null,false);
	}

	public static boolean containsString(String toSrchStr, String srchStr)
	{
		if(srchStr.equalsIgnoreCase("all")) return true;
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
			while((!found)&&(tos<tolen)&&(Character.isLetter(toSrchStr.charAt(tos))))
				tos++;
			tos++;
		}
		return found;
	}

	public static Environmental fetchEnvironmental(Vector list, String srchStr, boolean exactOnly)
	{
		if(srchStr.length()==0) return null;
		if((srchStr.length()<2)||(srchStr.equalsIgnoreCase("THE")))
		   return null;

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

		int myOccurrance=occurrance;
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
			myOccurrance=occurrance;
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
			myOccurrance=occurrance;
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
		if(srchStr.length()==0) return null;
		if((srchStr.length()<2)||(srchStr.equalsIgnoreCase("THE")))
		   return null;
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

		if(list.get(srchStr)!=null)
			return (Environmental)list.get(srchStr);
		int myOccurrance=occurrance;
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
			myOccurrance=occurrance;
			for(Enumeration e=list.elements();e.hasMoreElements();)
			{
				Environmental thisThang=(Environmental)e.nextElement();
				if((containsString(thisThang.name(),srchStr)||containsString(thisThang.Name(),srchStr))
				&&((!allFlag)||(thisThang.displayText().length()>0)))
					if((--myOccurrance)<=0)
						return thisThang;
			}
			myOccurrance=occurrance;
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
		if(srchStr.length()==0) return null;
		if((srchStr.length()<2)||(srchStr.equalsIgnoreCase("THE")))
		   return null;
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

		int myOccurrance=occurrance;
		if(exactOnly)
		{
			for(int i=0;i<list.length;i++)
			{
				Environmental thisThang=(Environmental)list[i];
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
			myOccurrance=occurrance;
			for(int i=0;i<list.length;i++)
			{
				Environmental thisThang=(Environmental)list[i];
				if(thisThang!=null)
					if((containsString(thisThang.name(),srchStr)||containsString(thisThang.Name(),srchStr))
					   &&((!allFlag)||(thisThang.displayText().length()>0)))
						if((--myOccurrance)<=0)
							return thisThang;
			}
			myOccurrance=occurrance;
			for(int i=0;i<list.length;i++)
			{
				Environmental thisThang=(Environmental)list[i];
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
		if(srchStr.length()==0) return null;
		if((srchStr.length()<2)||(srchStr.equalsIgnoreCase("THE")))
		   return null;
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
		int myOccurrance=occurrance;
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
			myOccurrance=occurrance;
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

	public static MOB parseShopkeeper(MOB mob, Vector commands, String error)
	{
		if(commands.size()==0)
		{
			mob.tell(error);
			return null;
		}
		commands.removeElementAt(0);

		Vector V=CoffeeUtensils.shopkeepers(mob.location(),mob);
		if(V.size()==0)
		{
			mob.tell(error);
			return null;
		}
		if(V.size()>1)
		{
			if(commands.size()<2)
			{
				mob.tell(error);
				return null;
			}
			MOB shopkeeper=mob.location().fetchInhabitant((String)commands.elementAt(commands.size()-1));
			if((shopkeeper!=null)&&(CoffeeUtensils.getShopKeeper(shopkeeper)!=null)&&(Sense.canBeSeenBy(shopkeeper,mob)))
				commands.removeElementAt(commands.size()-1);
			else
			{
				mob.tell("You don't see anyone called '"+(String)commands.elementAt(commands.size()-1)+"' here buying or selling.");
				return null;
			}
			return shopkeeper;
		}
		else
		{
			MOB shopkeeper=(MOB)V.firstElement();
			if(commands.size()>1)
			{
				MOB M=mob.location().fetchInhabitant((String)commands.elementAt(commands.size()-1));
				if((M!=null)&&(CoffeeUtensils.getShopKeeper(M)!=null)&&(Sense.canBeSeenBy(M,mob)))
				{
					shopkeeper=M;
					commands.removeElementAt(commands.size()-1);
				}
			}
			return shopkeeper;
		}
	}
}
