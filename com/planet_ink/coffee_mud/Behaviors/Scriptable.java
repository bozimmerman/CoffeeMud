package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Scriptable extends StdBehavior
{
	public String ID(){return "Scriptable";}
	protected int canImproveCode(){return Behavior.CAN_MOBS|Behavior.CAN_ITEMS|Behavior.CAN_ROOMS;}
	private MOB lastToHurtMe=null;
	private Room lastKnownLocation=null;
	private Vector que=new Vector();
	private static final Hashtable funcH=new Hashtable();
	private static final Hashtable methH=new Hashtable();
	private static final Hashtable progH=new Hashtable();
	private Vector oncesDone=new Vector();
	private Hashtable delayTargetTimes=new Hashtable();
	private Hashtable delayProgCounters=new Hashtable();
	private Hashtable lastTimeProgsDone=new Hashtable();
	private Hashtable lastDayProgsDone=new Hashtable();
	private Quests myQuest=null;
	
	public boolean modifyBehavior(MOB mob, Object O)
	{
		if(O instanceof Quests)
			myQuest=(Quests)O;
		else
		if(O instanceof String)
		{
			String s=(String)O;
			if((s.toLowerCase().startsWith("endquest"))
			&&(mob!=null))
			{
				String quest=s.substring(8).trim();
				Vector scripts=getScripts();
				if(!mob.amDead()) lastKnownLocation=mob.location();
				for(int v=0;v<scripts.size();v++)
				{
					Vector script=(Vector)scripts.elementAt(v);
					String trigger="";
					if(script.size()>0)
						trigger=((String)script.elementAt(0)).toUpperCase().trim();
					if((getTriggerCode(trigger)==13) //questtimeprog
					&&(!oncesDone.contains(script))
					&&(Util.getCleanBit(trigger,1).equalsIgnoreCase(quest))
					&&(Util.s_int(Util.getCleanBit(trigger,2))<0))
					{
						oncesDone.addElement(script);
						execute(mob,mob,mob,null,null,script,null);
					}
				}
			}
		}
		return false; 
	}

	private static final String[] progs={
		"GREET_PROG", //1
		"ALL_GREET_PROG", //2
		"SPEECH_PROG", //3
		"GIVE_PROG", //4
		"RAND_PROG", //5
		"ONCE_PROG", //6
		"FIGHT_PROG", //7
		"ENTRY_PROG", //8
		"EXIT_PROG", //9
		"DEATH_PROG", //10
		"HITPRCNT_PROG", //11
		"MASK_PROG", //12
		"QUEST_TIME_PROG", // 13
		"TIME_PROG", // 14
		"DAY_PROG", // 15
		"DELAY_PROG", // 16
		"FUNCTION_PROG", // 17
		"ACT_PROG", // 18
		"BRIBE_PROG", // 19
		"GET_PROG", // 20
		"PUT_PROG", // 21
		"DROP_PROG", // 22
		"WEAR_PROG", // 23
		"REMOVE_PROG", // 24
	};
	private static final String[] funcs={
		"RAND", //1
		"HAS", //2
		"WORN", //3
		"ISNPC", //4
		"ISPC", //5
		"ISGOOD", //6
		"ISNAME", //7
		"ISEVIL", //8
		"ISNEUTRAL", //9
		"ISFIGHT", //10
		"ISIMMORT", //11
		"ISCHARMED", //12
		"STAT", //13
		"AFFECTED", //14
		"ISFOLLOW", //15
		"HITPRCNT", //16
		"INROOM", //17
		"SEX", //18
		"POSITION", //19
		"LEVEL", //20
		"CLASS", //21
		"BASECLASS", //22
		"RACE", //23
		"RACECAT", //24
		"GOLDAMT", //25
		"OBJTYPE", // 26
		"VAR", // 27
		"QUESTWINNER", //28
		"QUESTMOB", // 29
		"QUESTOBJ", // 30
		"ISQUESTMOBALIVE", // 31
		"NUMMOBSINAREA", // 32
		"NUMMOBS", // 33
		"NUMRACESINAREA", // 34
		"NUMRACES", // 35
		"ISHERE", // 36
		"INLOCALE", // 37
		"ISTIME", // 38
		"ISDAY", // 39
		"NUMBER", // 40
		"EVAL", // 41
		"RANDNUM", // 42
		"ROOMMOB", // 43
		"ROOMITEM", // 44
		"NUMMOBSROOM", // 45
		"NUMITEMSROOM", // 46
		"MOBITEM", // 47
		"NUMITEMSMOB", // 48
		"HASTATTOO", // 49
		"ISSEASON", // 50
		"ISWEATHER", // 51
		"GSTAT", // 52
		"INCONTAINER", //53
		"ISALIVE", // 54
		"ISPKILL", // 55
		"NAME" // 56
	};
	private static final String[] methods={
		"MPASOUND", //1
		"MPECHO", //2
		"MPSLAY", //3
		"MPJUNK", //4
		"MPMLOAD", //5
		"MPOLOAD", //6
		"MPECHOAT", //7
		"MPECHOAROUND", //8
		"MPCAST", //9
		"MPKILL", //10
		"MPEXP", //11
		"MPPURGE", //12
		"MPUNAFFECT", //13
		"MPGOTO", //14
		"MPAT", //15
		"MPSET", //16
		"MPTRANSFER", //17
		"MPFORCE", //18
		"IF", //19
		"MPSETVAR", //20
		"MPENDQUEST",//21
		"MPQUESTWIN", //22
		"MPSTARTQUEST", //23
		"MPCALLFUNC", // 24
		"MPBEACON", // 25
		"MPALARM", // 26
		"MPWHILE", // 27
		"MPDAMAGE", // 28
		"MPTRACKTO", // 29
		"MPAFFECT", // 30
		"MPBEHAVE", // 31
		"MPUNBEHAVE",  //32
		"MPTATTOO", // 33
		"BREAK", // 34
		"MPGSET", // 35
		"MPSAVEVAR", // 36
		"MPENABLE", // 37
		"MPDISABLE", // 38
		"MPLOADVAR" // 39
	};

	public Behavior newInstance()
	{
		return new Scriptable();
	}

	protected class ScriptableResponse
	{
		int tickDelay=0;
		MOB s=null;
		Environmental t=null;
		MOB m=null;
		Item pi=null;
		Item si=null;
		Vector scr;
		String message=null;

		public ScriptableResponse(MOB source,
								  Environmental target,
								  MOB monster,
								  Item primaryItem,
								  Item secondaryItem,
								  Vector script,
								  int ticks,
								  String msg)
		{
			s=source;
			t=target;
			m=monster;
			pi=primaryItem;
			si=secondaryItem;
			scr=script;
			tickDelay=ticks;
			message=msg;
		}
		public boolean tickOrGo()
		{
			if((--tickDelay)<=0)
			{
				execute(s,t,m,pi,si,scr,message);
				return true;
			}
			return false;
		}
	}

	public void setParms(String newParms)
	{
		newParms=Util.replaceAll(newParms,"'","`");
		super.setParms(newParms);
		oncesDone.clear();
	}

	private String parseLoads(String text, int depth)
	{
		StringBuffer results=new StringBuffer("");
		String parse=text;
		if(depth>10) return "";  // no including off to infinity
		while(parse.length()>0)
		{
			int y=parse.indexOf("LOAD=");
			if(y>=0)
			{
				results.append(parse.substring(0,y).trim()+"\n");
				int z=parse.indexOf("~",y);
				if(z>0)
				{
					String filename=parse.substring(y+5,z).trim();
					parse=parse.substring(z+1);
					results.append(parseLoads(Resources.getFileResource(filename).toString(),depth+1));
				}
				else
				{
					String filename=parse.substring(y+5).trim();
					results.append(parseLoads(Resources.getFileResource(filename).toString(),depth+1));
					break;
				}
			}
			else
			{
				results.append(parse);
				break;
			}
		}
		return results.toString();
	}

	private Vector parseScripts(String text)
	{
		synchronized(funcH)
		{
			if(funcH.size()==0)
			{
				for(int i=0;i<funcs.length;i++)
					funcH.put(funcs[i],new Integer(i+1));
				for(int i=0;i<methods.length;i++)
					methH.put(methods[i],new Integer(i+1));
				for(int i=0;i<progs.length;i++)
					progH.put(progs[i],new Integer(i+1));
			}
		}
		Vector V=new Vector();
		text=parseLoads(text,0);
		while((text!=null)&&(text.length()>0))
		{
			int y=text.indexOf("~");
			String script="";
			if(y<0)
			{
				script=text.trim();
				text="";
			}
			else
			{
				script=text.substring(0,y).trim();
				text=text.substring(y+1).trim();
			}
			if(script.length()>0)
				V.addElement(script);
		}
		for(int v=0;v<V.size();v++)
		{
			String s=(String)V.elementAt(v);
			Vector script=new Vector();
			while(s.length()>0)
			{
				int y=-1;
				int yy=0;
				while(yy<s.length())
					if((s.charAt(yy)==';')&&((yy<=0)||(s.charAt(yy-1)!='\\'))) {y=yy;break;}
					else
					if(s.charAt(yy)=='\n'){y=yy;break;}
					else
					if(s.charAt(yy)=='\r'){y=yy;break;}
					else yy++;
				String cmd="";
				if(y<0)
				{
					cmd=s.trim();
					s="";
				}
				else
				{
					cmd=s.substring(0,y).trim();
					s=s.substring(y+1).trim();
				}
				if((cmd.length()>0)&&(!cmd.startsWith("#")))
					script.addElement(Util.replaceAll(cmd,"\\;",";"));
				V.setElementAt(script,v);
			}
		}
		return V;
	}

	private Room getRoom(String thisName, Room imHere)
	{
		if(thisName.length()==0) return null;
		Room room=CMMap.getRoom(thisName);
		if((room!=null)&&(room.roomID().equalsIgnoreCase(thisName)))
			return room;
		Room inAreaRoom=null;
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if((R.roomID().endsWith("#"+thisName))
			||(R.roomID().endsWith(thisName)))
			{
				if((imHere!=null)&&(imHere.getArea().Name().equals(R.getArea().Name())))
					inAreaRoom=R;
				else
					room=R;
			}
		}
		if(inAreaRoom!=null) return inAreaRoom;
		if(room!=null) return room;
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if(CoffeeUtensils.containsString(R.displayText(),thisName))
			{
				if((imHere!=null)&&(imHere.getArea().Name().equals(R.getArea().Name())))
					inAreaRoom=R;
				else
					room=R;
			}
		}
		if(inAreaRoom!=null) return inAreaRoom;
		if(room!=null) return room;
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if(CoffeeUtensils.containsString(R.description(),thisName))
			{
				if((imHere!=null)&&(imHere.getArea().Name().equals(R.getArea().Name())))
					inAreaRoom=R;
				else
					room=R;
			}
		}
		if(inAreaRoom!=null) return inAreaRoom;
		if(room!=null) return room;
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if((R.fetchInhabitant(thisName)!=null)
			||(R.fetchItem(null,thisName)!=null))
			{
				if((imHere!=null)&&(imHere.getArea().Name().equals(R.getArea().Name())))
					inAreaRoom=R;
				else
					room=R;
			}
		}
		if(inAreaRoom!=null) return inAreaRoom;
		return room;
	}

	private boolean simpleEvalStr(String arg1, String arg2, String cmp, String cmdName)
	{
		int x=arg1.compareToIgnoreCase(arg2);
		if(cmp.equalsIgnoreCase("=="))
			return (x==0);
		else
		if(cmp.equalsIgnoreCase(">="))
			return (x==0)||(x>0);
		else
		if(cmp.equalsIgnoreCase("<="))
			return (x==0)||(x<0);
		else
		if(cmp.equalsIgnoreCase(">"))
			return (x>0);
		else
		if(cmp.equalsIgnoreCase("<"))
			return (x<0);
		else
		if(cmp.equalsIgnoreCase("!="))
			return (x!=0);
		else
		{
			Log.errOut("Scriptable",cmdName+" Syntax -- "+arg1+" "+cmp+" "+arg2);
			return false;
		}
	}
	
	
	private boolean simpleEval(String arg1, String arg2, String cmp, String cmdName)
	{
		long val1=Util.s_long(arg1);
		long val2=Util.s_long(arg2);
		if(cmp.equalsIgnoreCase("=="))
			return (val1==val2);
		else
		if(cmp.equalsIgnoreCase(">="))
			return val1>=val2;
		else
		if(cmp.equalsIgnoreCase("<="))
			return val1<=val2;
		else
		if(cmp.equalsIgnoreCase(">"))
			return (val1>val2);
		else
		if(cmp.equalsIgnoreCase("<"))
			return (val1<val2);
		else
		if(cmp.equalsIgnoreCase("!="))
			return (val1!=val2);
		else
		{
			Log.errOut("Scriptable",cmdName+" Syntax -- "+val1+" "+cmp+" "+val2);
			return false;
		}
	}
	
	
	private Environmental findSomethingCalledThis(String thisName, Room imHere, boolean mob)
	{
		if(thisName.length()==0) return null;
		Environmental thing=null;
		Environmental areaThing=null;
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			Environmental E=null;
			if(mob)
				E=R.fetchInhabitant(thisName);
			else
			{
				E=R.fetchItem(null,thisName);
				if(E==null)
				for(int i=0;i<R.numInhabitants();i++)
				{
					MOB M=R.fetchInhabitant(i);
					if(M!=null)
					{
						E=M.fetchInventory(null,thisName);
						if((CoffeeUtensils.getShopKeeper(M)!=null)&&(E==null))
							E=CoffeeUtensils.getShopKeeper(M).getStock(thisName,null);
					}
				}
			}
			if(E!=null)
			{
				if((imHere!=null)&&(imHere.getArea().Name().equals(R.getArea().Name())))
					areaThing=E;
				else
					thing=E;
			}
		}
		if(areaThing!=null) return areaThing;
		return thing;
	}

	public Environmental getArgumentItem(String str, MOB source, MOB monster, Environmental target, Item primaryItem, Item secondaryItem, String msg)
	{
		if(str.length()<2) return null;
		if(str.charAt(0)=='$')
		{
			switch(str.charAt(1))
			{
			case 'N':
			case 'n': return source;
			case 'I':
			case 'i': return monster;
			case 'T':
			case 't': return target;
			case 'O':
			case 'o': return primaryItem;
			case 'P':
			case 'p': return secondaryItem;
			case 'F':
			case 'f': if((monster!=null)&&(monster.amFollowing()!=null))
						return monster.amFollowing();
					  else
						return null;
			}
		}
		if(lastKnownLocation!=null)
		{
			str=varify(source,target,monster,primaryItem,secondaryItem,msg,str);
			return lastKnownLocation.fetchFromRoomFavorMOBs(null,str,Item.WORN_REQ_ANY);
		}
		return null;
	}

	public boolean eval(MOB source, Environmental target, MOB monster, Item primaryItem, Item secondaryItem, String msg, String evaluable)
	{
		Vector formatCheck=Util.parse(evaluable);
		for(int i=1;i<(formatCheck.size()-1);i++)
			if((" == >= > < <= => =< != ".indexOf(" "+((String)formatCheck.elementAt(i))+" ")>=0)
			&&(((String)formatCheck.elementAt(i-1)).endsWith(")")))
			{
				String ps=(String)formatCheck.elementAt(i-1);
				ps=ps.substring(0,ps.length()-1);
				if(ps.length()==0) ps=" ";
				formatCheck.setElementAt(ps,i-1);
				
				String os=null;
				if((((String)formatCheck.elementAt(i+1)).startsWith("'")
				   ||((String)formatCheck.elementAt(i+1)).startsWith("`")))
				{
					os="";
					while((i<(formatCheck.size()-1))
					&&((!((String)formatCheck.elementAt(i+1)).endsWith("'"))
					&&(!((String)formatCheck.elementAt(i+1)).endsWith("`"))))
					{
						os+=((String)formatCheck.elementAt(i+1))+" ";
						formatCheck.removeElementAt(i+1);
					}
					os=(os+((String)formatCheck.elementAt(i+1))).trim();
				}
				else
				if((i==(formatCheck.size()-3))
				&&(((String)formatCheck.lastElement()).indexOf("(")<0))
				{
					os=((String)formatCheck.elementAt(i+1))
					+" "+((String)formatCheck.elementAt(i+2));
					formatCheck.removeElementAt(i+2);
				}
				else
					os=(String)formatCheck.elementAt(i+1);
				os=os+")";
				formatCheck.setElementAt(os,i+1);
				i+=2;
			}
		evaluable=Util.combine(formatCheck,0);
		String uevaluable=evaluable.toUpperCase().trim();
		boolean returnable=false;
		while(evaluable.length()>0)
		{
			int y=evaluable.indexOf("(");
			int z=evaluable.indexOf(")");
			if((y<0)||(z<y))
			{
				Log.errOut("Scriptable","Invalid EVAL format: "+evaluable);
				return false;
			}
			String preFab=uevaluable.substring(0,y).trim();
			Integer funcCode=(Integer)funcH.get(preFab);
			if(funcCode==null) funcCode=new Integer(0);
			if(y==0)
			{
				int depth=0;
				int i=0;
				while((++i)<evaluable.length())
				{
					char c=evaluable.charAt(i);
					if((c==')')&&(depth==0))
					{
						String expr=evaluable.substring(1,i);
						evaluable=evaluable.substring(i+1).trim();
						uevaluable=uevaluable.substring(i+1).trim();
						returnable=eval(source,target,monster,primaryItem,secondaryItem,msg,expr);
						break;
					}
					else
					if(c=='(') depth++;
					else
					if(c==')') depth--;
				}
				z=evaluable.indexOf(")");
			}
			else
			if(evaluable.startsWith("!"))
				return !eval(source,target,monster,primaryItem,secondaryItem,msg,evaluable.substring(1).trim());
			else
			if(uevaluable.startsWith("AND "))
				return returnable&&eval(source,target,monster,primaryItem,secondaryItem,msg,evaluable.substring(3).trim());
			else
			if(uevaluable.startsWith("OR "))
				return returnable||eval(source,target,monster,primaryItem,secondaryItem,msg,evaluable.substring(2).trim());
			else
			if((y<0)||(z<y))
			{
				Log.errOut("Scriptable","() Syntax -- "+monster.Name()+", "+evaluable);
				break;
			}
			else
			{
			switch(funcCode.intValue())
			{
			case 1: // rand
			{
				int arg=Util.s_int(evaluable.substring(y+1,z));
				if(Dice.rollPercentage()<arg)
					returnable=true;
				else
					returnable=false;
				break;
			}
			case 2: // has
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),1));
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if(arg2.length()==0)
				{
					Log.errOut("Scriptable","HAS Syntax -- "+monster.Name()+", "+evaluable);
					return returnable;
				}
				if(E==null)
					returnable=false;
				else
				if(E instanceof MOB)
					returnable=(((MOB)E).fetchInventory(arg2)!=null);
				else
				if(E instanceof Item)
					returnable=CoffeeUtensils.containsString(E.name(),arg2);
				else
				if(E instanceof Room)
					returnable=(((Room)E).fetchItem(null,arg2)!=null);
				else
					returnable=false;
				break;
			}
			case 3: // worn
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),1));
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if(arg2.length()==0)
				{
					Log.errOut("Scriptable","WORN Syntax -- "+monster.Name()+", "+evaluable);
					return returnable;
				}
				if(E==null)
					returnable=false;
				else
				if(E instanceof MOB)
					returnable=(((MOB)E).fetchWornItem(arg2)!=null);
				else
				if(E instanceof Item)
					returnable=(CoffeeUtensils.containsString(E.name(),arg2)&&(!((Item)E).amWearingAt(Item.INVENTORY)));
				else
					returnable=false;
				break;
			}
			case 4: // isnpc
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=((MOB)E).isMonster();
				break;
			}
			case 5: // ispc
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=!((MOB)E).isMonster();
				break;
			}
			case 6: // isgood
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=CommonStrings.shortAlignmentStr(((MOB)E).getAlignment()).equalsIgnoreCase("good");
				break;
			}
			case 8: // isevil
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=CommonStrings.shortAlignmentStr(((MOB)E).getAlignment()).equalsIgnoreCase("evil");
				break;
			}
			case 9: // isneutral
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=CommonStrings.shortAlignmentStr(((MOB)E).getAlignment()).equalsIgnoreCase("neutral");
				break;
			}
			case 54: // isalive
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E!=null)&&((E instanceof MOB))&&(!((MOB)E).amDead()))
					returnable=true;
				else
					returnable=false;
				break;
			}
			case 10: // isfight
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=((MOB)E).isInCombat();
				break;
			}
			case 11: // isimmort
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=((MOB)E).isASysOp(((MOB)E).location());
				break;
			}
			case 12: // ischarmed
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=Sense.flaggedAffects(E,Ability.FLAG_CHARMING).size()>0;
				break;
			}
			case 15: // isfollow
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				if(((MOB)E).amFollowing()==null)
					returnable=false;
				else
				if(((MOB)E).amFollowing().location()!=lastKnownLocation)
					returnable=false;
				else
					returnable=true;
				break;
			}
			case 55: // ispkill
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				if(Util.bset(((MOB)E).getBitmap(),MOB.ATT_PLAYERKILL))
					returnable=true;
				else
					returnable=false;
				break;
			}
			case 7: // isname
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),1));
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if(E==null)
					returnable=false;
				else
					returnable=CoffeeUtensils.containsString(E.name(),arg2);
				break;
			}
			case 56: // name
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),2));
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if(E==null)
					returnable=false;
				else
					returnable=simpleEvalStr(E.Name(),arg3,arg2,"NAME");
				break;
			}
			case 14: // affected
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if(E==null)
					returnable=false;
				else
					returnable=(E.fetchAffect(arg2)!=null);
				break;
			}
			case 28: // questwinner
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				Quest Q=Quests.fetchQuest(arg2);
				if(Q==null)
					returnable=false;
				else
				{
					if(E!=null) arg1=E.Name();
					returnable=Q.wasWinner(arg1);
				}
				break;
			}
			case 29: // questmob
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				Quest Q=Quests.fetchQuest(arg2);
				if(Q==null)
					returnable=false;
				else
					returnable=(Q.wasQuestMob(arg1)>=0);
				break;
			}
			case 31: // isquestmobalive
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				Quest Q=Quests.fetchQuest(arg2);
				if(Q==null)
					returnable=false;
				else
				{
					MOB M=null;
					if(Util.s_int(arg1)>0)
						M=Q.getQuestMob(Util.s_int(arg1));
					else
						M=Q.getQuestMob(Q.wasQuestMob(arg1));
					if(M==null) returnable=false;
					else returnable=!M.amDead();
				}
				break;
			}
			case 32: // nummobsinarea
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0)).toUpperCase();
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),2));
				int num=0;
				for(Enumeration e=lastKnownLocation.getArea().getMap();e.hasMoreElements();)
				{
					Room R=(Room)e.nextElement();
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB M=R.fetchInhabitant(m);
						if((M!=null)&&(CoffeeUtensils.containsString(M.name(),arg1)))
							num++;
					}
				}
				returnable=simpleEval(""+num,arg3,arg2,"NUMMOBSINAREA");
				break;
			}
			case 33: // nummobs
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0)).toUpperCase();
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),2));
				int num=0;
				for(Enumeration e=CMMap.rooms();e.hasMoreElements();)
				{
					Room R=(Room)e.nextElement();
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB M=R.fetchInhabitant(m);
						if((M!=null)&&(CoffeeUtensils.containsString(M.name(),arg1)))
							num++;
					}
				}
				returnable=simpleEval(""+num,arg3,arg2,"NUMMOBS");
				break;
			}
			case 34: // numracesinarea
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0)).toUpperCase();
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),2));
				int num=0;
				for(Enumeration e=lastKnownLocation.getArea().getMap();e.hasMoreElements();)
				{
					Room R=(Room)e.nextElement();
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB M=R.fetchInhabitant(m);
						if((M!=null)&&(M.charStats().raceName().equalsIgnoreCase(arg1)))
							num++;
					}
				}
				returnable=simpleEval(""+num,arg3,arg2,"NUMRACESINAREA");
				break;
			}
			case 35: // numraces
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0)).toUpperCase();
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),2));
				int num=0;
				for(Enumeration e=CMMap.rooms();e.hasMoreElements();)
				{
					Room R=(Room)e.nextElement();
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB M=R.fetchInhabitant(m);
						if((M!=null)&&(M.charStats().raceName().equalsIgnoreCase(arg1)))
							num++;
					}
				}
				returnable=simpleEval(""+num,arg3,arg2,"NUMRACES");
				break;
			}
			case 30: // questitem
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				Quest Q=Quests.fetchQuest(arg2);
				if(Q==null)
					returnable=false;
				else
					returnable=(Q.wasQuestItem(arg1)>=0);
				break;
			}
			case 16: // hitprcnt
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),2));
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					Log.errOut("Scriptable","HITPRCNT Syntax -- "+monster.Name()+", "+evaluable);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					double hitPctD=Util.div(((MOB)E).curState().getHitPoints(),((MOB)E).maxState().getHitPoints());
					int val1=(int)Math.round(hitPctD*100.0);
					returnable=simpleEval(""+val1,arg3,arg2,"HITPRCNT");
				}
				break;
			}
			case 50: // isseason
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				returnable=false;
				if(monster.location()!=null)
				for(int a=0;a<Area.SEASON_DESCS.length;a++)
					if((Area.SEASON_DESCS[a]).startsWith(arg1.toUpperCase())
					&&(monster.location().getArea().getSeasonCode()==a))
					{returnable=true; break;}
				break;
			}
			case 51: // isweather
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				returnable=false;
				if(monster.location()!=null)
				for(int a=0;a<Area.WEATHER_DESCS.length;a++)
					if((Area.WEATHER_DESCS[a]).startsWith(arg1.toUpperCase())
					&&(monster.location().getArea().weatherType(monster.location())==a))
					{returnable=true; break;}
				break;
			}
			case 38: // istime
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				if(monster.location()==null)
					returnable=false;
				else
				if(("daytime").startsWith(arg1.toLowerCase())
				&&(monster.location().getArea().getTODCode()==Area.TIME_DAY))
					returnable=true;
				else
				if(("dawn").startsWith(arg1.toLowerCase())
				&&(monster.location().getArea().getTODCode()==Area.TIME_DAWN))
					returnable=true;
				else
				if(("dusk").startsWith(arg1.toLowerCase())
				&&(monster.location().getArea().getTODCode()==Area.TIME_DUSK))
					returnable=true;
				else
				if(("nighttime").startsWith(arg1.toLowerCase())
				&&(monster.location().getArea().getTODCode()==Area.TIME_NIGHT))
					returnable=true;
				else
				if((monster.location().getArea().getTODCode()==Util.s_int(arg1)))
					returnable=true;
				else
					returnable=false;
				break;
			}
			case 39: // isday
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				if((monster.location()!=null)&&(monster.location().getArea().getDayOfMonth()==Util.s_int(arg1)))
					returnable=true;
				else
					returnable=false;
				break;
			}
			case 45: // nummobsroom
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),1));
				if(lastKnownLocation!=null)
					returnable=simpleEval(""+lastKnownLocation.numInhabitants(),arg2,arg1,"NUMMOBSROOM");
				break;
			}
			case 46: // numitemsroom
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),1));
				int ct=0;
				if(lastKnownLocation!=null)
				for(int i=0;i<lastKnownLocation.numItems();i++)
				{
					Item I=lastKnownLocation.fetchItem(i);
					if((I!=null)&&(I.container()==null))
						ct++;
				}
				returnable=simpleEval(""+ct,arg2,arg1,"NUMITEMSROOM");
				break;
			}
			case 47: //mobitem
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),1));
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),2));
				MOB M=null;
				if(lastKnownLocation!=null)
					M=lastKnownLocation.fetchInhabitant(Util.s_int(arg1));
				Item which=null;
				int ct=0;
				if(M!=null)
				for(int i=0;i<M.inventorySize();i++)
				{
					Item I=M.fetchInventory(i);
					if((I!=null)&&(I.container()==null))
					{
						if(ct==Util.s_int(arg2))
						{ which=I; break;}
						ct++;
					}
				}
				if(which==null)
					returnable=false;
				else
					returnable=(CoffeeUtensils.containsString(which.name(),arg3)
								||CoffeeUtensils.containsString(which.Name(),arg3)
								||CoffeeUtensils.containsString(which.displayText(),arg3));
				break;
			}
			case 49: // hastattoo
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if(arg2.length()==0)
				{
					Log.errOut("Scriptable","HASTATTOO Syntax -- "+monster.Name()+", "+evaluable);
					break;
				}
				else
				if((E!=null)&&(E instanceof MOB))
				{
					Ability A=((MOB)E).fetchAbility("Prop_Tattoo");
					if(A!=null)
						returnable=A.text().indexOf(";"+arg2.toUpperCase()+";")>=0;
					else
						returnable=false;
				}
				else
					returnable=false;
				break;
			}
			case 48: // numitemsmob
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),1));
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),2));
				MOB which=null;
				if(lastKnownLocation!=null)
					which=lastKnownLocation.fetchInhabitant(Util.s_int(arg1));
				int ct=0;
				if(which!=null)
				for(int i=0;i<which.inventorySize();i++)
				{
					Item I=which.fetchInventory(i);
					if((I!=null)&&(I.container()==null))
						ct++;
				}
				returnable=simpleEval(""+ct,arg3,arg2,"NUMITEMSMOB");
				break;
			}
			case 43: // roommob
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),1));
				Environmental which=null;
				if(lastKnownLocation!=null)
					which=lastKnownLocation.fetchInhabitant(Util.s_int(arg1));
				if(which==null)
					returnable=false;
				else
					returnable=(CoffeeUtensils.containsString(which.name(),arg2)
								||CoffeeUtensils.containsString(which.Name(),arg2)
								||CoffeeUtensils.containsString(which.displayText(),arg2));
				break;
			}
			case 44: // roomitem
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),1));
				Environmental which=null;
				int ct=0;
				if(lastKnownLocation!=null)
				for(int i=0;i<lastKnownLocation.numItems();i++)
				{
					Item I=lastKnownLocation.fetchItem(i);
					if((I!=null)&&(I.container()==null))
					{
						if(ct==Util.s_int(arg1))
						{ which=I; break;}
						ct++;
					}
				}
				if(which==null)
					returnable=false;
				else
					returnable=(CoffeeUtensils.containsString(which.name(),arg2)
								||CoffeeUtensils.containsString(which.Name(),arg2)
								||CoffeeUtensils.containsString(which.displayText(),arg2));
				break;
			}
			case 36: // ishere
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if(E==null)
					returnable=false;
				else
				if(E instanceof MOB)
					returnable=lastKnownLocation.isInhabitant((MOB)E);
				else
				if(E instanceof Item)
					returnable=(lastKnownLocation.isContent((Item)E)
								||(monster.isMine(E))
								||((source!=null)&&(source.isMine(E))));
				else
					returnable=false;
				break;
			}
			case 17: // inroom
			{
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				String comp="==";
				Environmental E=monster;
				if((" == >= > < <= => =< != ".indexOf(" "+Util.getCleanBit(evaluable.substring(y+1,z),1)+" ")>=0))
				{
					E=getArgumentItem(Util.getCleanBit(evaluable.substring(y+1,z),0),source,monster,target,primaryItem,secondaryItem,msg);
					comp=Util.getCleanBit(evaluable.substring(y+1,z),1);
					arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),2));
				}
				Room R=getRoom(arg2,lastKnownLocation);
				if(E==null)
					returnable=false;
				else
				{
					Room R2=CoffeeUtensils.roomLocation(E);
					if((R==null)&&((arg2.length()==0)||(R2==null)))
						returnable=true;
					else
					if((R==null)||(R2==null))
						returnable=false;
					else
						returnable=simpleEval(CMMap.getExtendedRoomID(R2),CMMap.getExtendedRoomID(R),comp,"INROOM");
				}
				break;
			}
			case 37: // inlocale
			{
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				Environmental E=monster;
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				if(arg2.length()==0)
					returnable=true;
				else
				if(CMClass.className(((MOB)E).location()).toUpperCase().indexOf(arg2.toUpperCase())>=0)
					returnable=true;
				else
					returnable=false;
				break;
			}
			case 18: // sex
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0).toUpperCase());
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=Util.getCleanBit(evaluable.substring(y+1,z),2).toUpperCase();
				if(Util.isNumber(arg3))
					switch(Util.s_int(arg3))
					{
					case 0: arg3="NEUTER"; break;
					case 1: arg3="MALE"; break;
					case 2: arg3="FEMALE"; break;
					}
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					Log.errOut("Scriptable","SEX Syntax -- "+monster.Name()+", "+evaluable);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					String sex=(""+((char)((MOB)E).charStats().getStat(CharStats.GENDER))).toUpperCase();
					if(arg2.equals("=="))
						returnable=arg3.startsWith(sex);
					else
					if(arg2.equals("!="))
						returnable=!arg3.startsWith(sex);
					else
					{
						Log.errOut("Scriptable","SEX Syntax -- "+monster.Name()+", "+evaluable);
						return returnable;
					}
				}
				break;
			}
			case 13: // stat
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=Util.getCleanBit(evaluable.substring(y+1,z),2);
				String arg4=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),3));
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					Log.errOut("Scriptable","STAT Syntax -- "+monster.Name()+", "+evaluable);
					break;
				}
				if(E==null)
					returnable=false;
				else
				{
					boolean found=false;
					String val="";
					for(int i=0;i<E.getStatCodes().length;i++)
					{
						if(E.getStatCodes()[i].equalsIgnoreCase(arg2))
						{
							val=E.getStat(arg2);
							found=true; 
							break;
						}
					}
					if((!found)&&(E instanceof MOB))
					{
						MOB M=(MOB)E;
						for(int i=0;i<CharStats.TRAITS.length;i++)
							if(CharStats.TRAITS[i].equalsIgnoreCase(arg2))
							{
								val=""+M.charStats().getStat(CharStats.TRAITS[i]);
								found=true;
								break;
							}
						if(!found)
						for(int i=0;i<M.curState().getCodes().length;i++)
							if(M.curState().getCodes()[i].equalsIgnoreCase(arg2))
							{
								val=M.curState().getStat(M.curState().getCodes()[i]);
								found=true;
								break;
							}
						if(!found)
						for(int i=0;i<M.envStats().getCodes().length;i++)
							if(M.envStats().getCodes()[i].equalsIgnoreCase(arg2))
							{
								val=M.envStats().getStat(M.envStats().getCodes()[i]);
								found=true;
								break;
							}
					}
					

					if(!found)
					{
						Log.errOut("Scriptable","STAT Syntax -- "+monster.Name()+", unknown stat: "+arg2+" for "+E.name());
						break;
					}

					if(arg3.equals("=="))
						returnable=val.equalsIgnoreCase(arg4);
					else
					if(arg3.equals("!="))
						returnable=!val.equalsIgnoreCase(arg4);
					else
						returnable=simpleEval(val,arg4,arg3,"STAT");
				}
				break;
			}
			case 52: // gstat
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=Util.getCleanBit(evaluable.substring(y+1,z),2);
				String arg4=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),3));
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					Log.errOut("Scriptable","GSTAT Syntax -- "+monster.Name()+", "+evaluable);
					break;
				}
				if(E==null)
					returnable=false;
				else
				{
					boolean found=false;
					String val="";
					if(E instanceof MOB)
					{
						for(int i=0;i<Generic.GENMOBCODES.length;i++)
						{
							if(Generic.GENMOBCODES[i].equalsIgnoreCase(arg2))
							{
								val=Generic.getGenMobStat((MOB)E,Generic.GENMOBCODES[i]);
								found=true; break;
							}
						}
						if(!found)
						{
							MOB M=(MOB)E;
							for(int i=0;i<CharStats.TRAITS.length;i++)
								if(CharStats.TRAITS[i].equalsIgnoreCase(arg2))
								{
									val=""+M.charStats().getStat(CharStats.TRAITS[i]);
									found=true;
									break;
								}
							if(!found)
							for(int i=0;i<M.curState().getCodes().length;i++)
								if(M.curState().getCodes()[i].equalsIgnoreCase(arg2))
								{
									val=M.curState().getStat(M.curState().getCodes()[i]);
									found=true;
									break;
								}
							if(!found)
							for(int i=0;i<M.envStats().getCodes().length;i++)
								if(M.envStats().getCodes()[i].equalsIgnoreCase(arg2))
								{
									val=M.envStats().getStat(M.envStats().getCodes()[i]);
									found=true;
									break;
								}
						}
					}
					else
					if(E instanceof Item)
					{
						for(int i=0;i<Generic.GENITEMCODES.length;i++)
						{
							if(Generic.GENITEMCODES[i].equalsIgnoreCase(arg2))
							{
								val=Generic.getGenItemStat((Item)E,Generic.GENITEMCODES[i]);
								found=true; break;
							}
						}
					}

					if(!found)
					{
						Log.errOut("Scriptable","GSTAT Syntax -- "+monster.Name()+", unknown stat: "+arg2+" for "+E.name());
						break;
					}

					if(arg3.equals("=="))
						returnable=val.equalsIgnoreCase(arg4);
					else
					if(arg3.equals("!="))
						returnable=!val.equalsIgnoreCase(arg4);
					else
						returnable=simpleEval(val,arg4,arg3,"GSTAT");
				}
				break;
			}
			case 19: // position
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=Util.getCleanBit(evaluable.substring(y+1,z),2).toUpperCase();
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					Log.errOut("Scriptable","POSITION Syntax -- "+monster.Name()+", "+evaluable);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					String sex="STANDING";
					if(Sense.isSleeping(E))
						sex="SLEEPING";
					else
					if(Sense.isSitting(E))
						sex="SITTING";
					if(arg2.equals("=="))
						returnable=sex.startsWith(arg3);
					else
					if(arg2.equals("!="))
						returnable=!sex.startsWith(arg3);
					else
					{
						Log.errOut("Scriptable","POSITION Syntax -- "+monster.Name()+", "+evaluable);
						return returnable;
					}
				}
				break;
			}
			case 20: // level
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),2));
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					Log.errOut("Scriptable","LEVEL Syntax -- "+monster.Name()+", "+evaluable);
					return returnable;
				}
				if(E==null)
					returnable=false;
				else
				{
					int val1=E.envStats().level();
					returnable=simpleEval(""+val1,arg3,arg2,"LEVEL");
				}
				break;
			}
			case 21: // class
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),2).toUpperCase());
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					Log.errOut("Scriptable","CLASS Syntax -- "+monster.Name()+", "+evaluable);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					String sex=((MOB)E).charStats().displayClassName().toUpperCase();
					if(arg2.equals("=="))
						returnable=sex.startsWith(arg3);
					else
					if(arg2.equals("!="))
						returnable=!sex.startsWith(arg3);
					else
					{
						Log.errOut("Scriptable","CLASS Syntax -- "+monster.Name()+", "+evaluable);
						return returnable;
					}
				}
				break;
			}
			case 22: // baseclass
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),2).toUpperCase());
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					Log.errOut("Scriptable","CLASS Syntax -- "+monster.Name()+", "+evaluable);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					String sex=((MOB)E).charStats().getCurrentClass().baseClass().toUpperCase();
					if(arg2.equals("=="))
						returnable=sex.startsWith(arg3);
					else
					if(arg2.equals("!="))
						returnable=!sex.startsWith(arg3);
					else
					{
						Log.errOut("Scriptable","CLASS Syntax -- "+monster.Name()+", "+evaluable);
						return returnable;
					}
				}
				break;
			}
			case 23: // race
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),2).toUpperCase());
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					Log.errOut("Scriptable","RACE Syntax -- "+monster.Name()+", "+evaluable);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					String sex=((MOB)E).charStats().raceName().toUpperCase();
					if(arg2.equals("=="))
						returnable=sex.startsWith(arg3);
					else
					if(arg2.equals("!="))
						returnable=!sex.startsWith(arg3);
					else
					{
						Log.errOut("Scriptable","RACE Syntax -- "+monster.Name()+", "+evaluable);
						return returnable;
					}
				}
				break;
			}
			case 24: //racecat
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),2).toUpperCase());
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					Log.errOut("Scriptable","RACECAT Syntax -- "+monster.Name()+", "+evaluable);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					String sex=((MOB)E).charStats().getMyRace().racialCategory().toUpperCase();
					if(arg2.equals("=="))
						returnable=sex.startsWith(arg3);
					else
					if(arg2.equals("!="))
						returnable=!sex.startsWith(arg3);
					else
					{
						Log.errOut("Scriptable","RACECAT Syntax -- "+monster.Name()+", "+evaluable);
						return returnable;
					}
				}
				break;
			}
			case 25: // goldamt
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),2));
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					Log.errOut("Scriptable","GOLDAMT Syntax -- "+monster.Name()+", "+evaluable);
					break;
				}
				if(E==null)
					returnable=false;
				else
				{
					int val1=0;
					if(E instanceof MOB)
						val1=((MOB)E).getMoney();
					else
					if(E instanceof Coins)
						val1=((Coins)E).numberOfCoins();
					else
					if(E instanceof Item)
						val1=((Item)E).value();
					else
					{
						Log.errOut("Scriptable","GOLDAMT Syntax -- "+monster.Name()+", "+evaluable);
						return returnable;
					}

					returnable=simpleEval(""+val1,arg3,arg2,"GOLDAMT");
				}
				break;
			}
			case 26: // objtype
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),2).toUpperCase());
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					Log.errOut("Scriptable","OBJTYPE Syntax -- "+monster.Name()+", "+evaluable);
					return returnable;
				}
				if(E==null)
					returnable=false;
				else
				{
					String sex=CMClass.className(E).toUpperCase();
					if(arg2.equals("=="))
						returnable=sex.indexOf(arg3)>=0;
					else
					if(arg2.equals("!="))
						returnable=sex.indexOf(arg3)<0;
					else
					{
						Log.errOut("Scriptable","OBJTYPE Syntax -- "+monster.Name()+", "+evaluable);
						return returnable;
					}
				}
				break;
			}
			case 27: // var
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1).toUpperCase();
				String arg3=Util.getCleanBit(evaluable.substring(y+1,z),2);
				String arg4=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),3));
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					Log.errOut("Scriptable","VAR Syntax -- "+monster.Name()+", "+evaluable);
					return returnable;
				}
				if(E==null)
					returnable=false;
				else
				{
					Hashtable H=(Hashtable)Resources.getResource("SCRIPTVAR-"+E.Name());
					String val="";
					if(H!=null)
					{
						val=(String)H.get(arg2);
						if(val==null) val="";
					}
					if(arg3.equals("=="))
						returnable=val.equals(arg4);
					else
					if(arg3.equals("!="))
						returnable=!val.equals(arg4);
					else
					if(arg3.equals(">"))
						returnable=Util.s_int(val)>Util.s_int(arg4);
					else
					if(arg3.equals("<"))
						returnable=Util.s_int(val)<Util.s_int(arg4);
					else
					if(arg3.equals(">="))
						returnable=Util.s_int(val)>=Util.s_int(arg4);
					else
					if(arg3.equals("<="))
						returnable=Util.s_int(val)<=Util.s_int(arg4);
					else
					{
						Log.errOut("Scriptable","VAR Syntax -- "+monster.Name()+", "+evaluable);
						return returnable;
					}
				}
				break;
			}
			case 41: // eval
			{
				String val=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				String arg3=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg4=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),2));
				if(arg3.length()==0)
				{
					Log.errOut("Scriptable","EVAL Syntax -- "+monster.Name()+", "+evaluable);
					return returnable;
				}
				if(arg3.equals("=="))
					returnable=val.equals(arg4);
				else
				if(arg3.equals("!="))
					returnable=!val.equals(arg4);
				else
				if(arg3.equals(">"))
					returnable=Util.s_int(val)>Util.s_int(arg4);
				else
				if(arg3.equals("<"))
					returnable=Util.s_int(val)<Util.s_int(arg4);
				else
				if(arg3.equals(">="))
					returnable=Util.s_int(val)>=Util.s_int(arg4);
				else
				if(arg3.equals("<="))
					returnable=Util.s_int(val)<=Util.s_int(arg4);
				else
				{
					Log.errOut("Scriptable","EVAL Syntax -- "+monster.Name()+", "+evaluable);
					return returnable;
				}
				break;
			}
			case 40: // number
			{
				String val=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0)).trim();
				boolean isnumber=(val.length()>0);
				for(int i=0;i<val.length();i++)
					if(!Character.isDigit(val.charAt(i)))
					{ isnumber=false; break;}
				returnable=isnumber;
				break;
			}
			case 42: // randnum
			{
				int arg1=Util.s_int(varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0)).toUpperCase());
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				int arg3=Dice.roll(1,Util.s_int(varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),2))),0);
				returnable=simpleEval(""+arg1,""+arg3,arg2,"RANDNUM");
				break;
			}
			case 53: // incontainer
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E2=getArgumentItem(arg2,source,monster,target,primaryItem,secondaryItem,msg);
				if(E==null)
					returnable=false;
				else
				if(E instanceof MOB)
				{
					if(arg2.length()==0)
						returnable=(((MOB)E).riding()==null);
					else
					if(E2!=null)
						returnable=(((MOB)E).riding()==E2);
					else
						returnable=false;
				}
				else
				if(E instanceof Item)
				{
					if(arg2.length()==0)
						returnable=(((Item)E).container()==null);
					else
					if(E2!=null)
						returnable=(((Item)E).container()==E2);
					else
						returnable=false;
				}
				else
					returnable=false;
				break;
			}
			default:
				Log.errOut("Scriptable","Unknown CMD -- "+monster.Name()+", "+evaluable);
				return returnable;
			}
			if((z>=0)&&(z<=evaluable.length()))
			{
				evaluable=evaluable.substring(z+1).trim();
				uevaluable=uevaluable.substring(z+1).trim();
			}
		}
		}
		return returnable;
	}

	public String functify(MOB source, Environmental target, MOB monster, Item primaryItem, Item secondaryItem, String msg, String evaluable)
	{
		String uevaluable=evaluable.toUpperCase().trim();
		StringBuffer results = new StringBuffer("");
		while(evaluable.length()>0)
		{
			int y=evaluable.indexOf("(");
			int z=evaluable.indexOf(")");
			String preFab=uevaluable.substring(0,y).trim();
			Integer funcCode=(Integer)funcH.get(preFab);
			if(funcCode==null) funcCode=new Integer(0);
			if(y==0)
			{
				int depth=0;
				int i=0;
				while((++i)<evaluable.length())
				{
					char c=evaluable.charAt(i);
					if((c==')')&&(depth==0))
					{
						String expr=evaluable.substring(1,i);
						evaluable=evaluable.substring(i+1);
						uevaluable=uevaluable.substring(i+1);
						results.append(functify(source,target,monster,primaryItem,secondaryItem,msg,expr));
						break;
					}
					else
					if(c=='(') depth++;
					else
					if(c==')') depth--;
				}
				z=evaluable.indexOf(")");
			}
			else
			if((y<0)||(z<y))
			{
				Log.errOut("Scriptable","() Syntax -- "+monster.Name()+", "+evaluable);
				break;
			}
			else
			switch(funcCode.intValue())
			{
			case 1: // rand
			{
				results.append(Dice.rollPercentage());
				break;
			}
			case 2: // has
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				Vector choices=new Vector();
				if(E==null)
					choices=new Vector();
				else
				if(E instanceof MOB)
				{
					for(int i=0;i<((MOB)E).inventorySize();i++)
					{
						Item I=((MOB)E).fetchInventory(i);
						if((I!=null)&&(I.amWearingAt(Item.INVENTORY))&&(I.container()==null))
							choices.addElement(I);
					}
				}
				else
				if(E instanceof Item)
				{
					choices.addElement((Item)E);
					if(E instanceof Container)
						choices=((Container)E).getContents();
				}
				else
				if(E instanceof Room)
				{
					for(int i=0;i<((Room)E).numItems();i++)
					{
						Item I=((Room)E).fetchItem(i);
						if((I!=null)&&(I.container()==null))
							choices.addElement(I);
					}
				}
				if(choices.size()>0)
					results.append(((Item)choices.elementAt(Dice.roll(1,choices.size(),-1))).name());
				break;
			}
			case 3: // worn
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				Vector choices=new Vector();
				if(E==null)
					choices=new Vector();
				else
				if(E instanceof MOB)
				{
					for(int i=0;i<((MOB)E).inventorySize();i++)
					{
						Item I=((MOB)E).fetchInventory(i);
						if((I!=null)&&(!I.amWearingAt(Item.INVENTORY))&&(I.container()==null))
							choices.addElement(I);
					}
				}
				else
				if((E instanceof Item)&&(!(((Item)E).amWearingAt(Item.INVENTORY))))
				{
					choices.addElement((Item)E);
					if(E instanceof Container)
						choices=((Container)E).getContents();
				}
				if(choices.size()>0)
					results.append(((Item)choices.elementAt(Dice.roll(1,choices.size(),-1))).name());
				break;
			}
			case 4: // isnpc
			case 5: // ispc
				results.append("[unimplemented function]");
				break;
			case 6: // isgood
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E!=null)&&((E instanceof MOB)))
					results.append(CommonStrings.alignmentStr(((MOB)E).getAlignment()));
				break;
			}
			case 8: // isevil
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E!=null)&&((E instanceof MOB)))
					results.append(CommonStrings.shortAlignmentStr(((MOB)E).getAlignment()));
				break;
			}
			case 9: // isneutral
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E!=null)&&((E instanceof MOB)))
					results.append(((MOB)E).getAlignment());
				break;
			}
			case 11: // isimmort
				results.append("[unimplemented function]");
				break;
			case 54: // isalive
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E!=null)&&((E instanceof MOB))&&(!((MOB)E).amDead()))
					results.append(((MOB)E).healthText());
				else
					results.append(E.name()+" is dead.");
				break;
			}
			case 55: // ispkill
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E==null)||(!(E instanceof MOB)))
					results.append("false");
				else
				if(Util.bset(((MOB)E).getBitmap(),MOB.ATT_PLAYERKILL))
					results.append("true");
				else
					results.append("false");
				break;
			}
			case 10: // isfight
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E!=null)&&((E instanceof MOB))&&(((MOB)E).isInCombat()))
					results.append(((MOB)E).getVictim().name());
				break;
			}
			case 12: // ischarmed
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if(E!=null)
				{
					Vector V=Sense.flaggedAffects(E,Ability.FLAG_CHARMING);
					for(int v=0;v<V.size();v++)
						results.append((((Ability)V.elementAt(v)).name())+" ");
				}
				break;
			}
			case 15: // isfollow
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E!=null)&&(E instanceof MOB)&&(((MOB)E).amFollowing()!=null)
				&&(((MOB)E).amFollowing().location()==lastKnownLocation))
					results.append(((MOB)E).amFollowing().name());
				break;
			}
			case 56: // name
			case 7: // isname
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if(E!=null)	results.append(E.name());
				break;
			}
			case 14: // affected
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if(E.numAffects()>0)
					results.append(E.fetchAffect(Dice.roll(1,E.numAffects(),-1)).name());
				break;
			}
			case 28: // questwinner
			case 29: // questmob
			case 31: // isquestmobalive
				results.append("[unimplemented function]");
				break;
			case 32: // nummobsinarea
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				int num=0;
				for(Enumeration e=lastKnownLocation.getArea().getMap();e.hasMoreElements();)
				{
					Room R=(Room)e.nextElement();
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB M=R.fetchInhabitant(m);
						if((M!=null)&&(CoffeeUtensils.containsString(M.name(),arg1)))
							num++;
					}
				}
				results.append(num);
				break;
			}
			case 33: // nummobs
			{
				int num=0;
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				for(Enumeration e=CMMap.rooms();e.hasMoreElements();)
				{
					Room R=(Room)e.nextElement();
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB M=R.fetchInhabitant(m);
						if((M!=null)&&(CoffeeUtensils.containsString(M.name(),arg1)))
							num++;
					}
				}
				results.append(num);
				break;
			}
			case 34: // numracesinarea
			{
				int num=0;
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				for(Enumeration e=lastKnownLocation.getArea().getMap();e.hasMoreElements();)
				{
					Room R=(Room)e.nextElement();
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB M=R.fetchInhabitant(m);
						if((M!=null)&&(M.charStats().raceName().equalsIgnoreCase(arg1)))
							num++;
					}
				}
				results.append(num);
				break;
			}
			case 35: // numraces
			{
				int num=0;
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				for(Enumeration e=CMMap.rooms();e.hasMoreElements();)
				{
					Room R=(Room)e.nextElement();
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB M=R.fetchInhabitant(m);
						if((M!=null)&&(M.charStats().raceName().equalsIgnoreCase(arg1)))
							num++;
					}
				}
				results.append(num);
				break;
			}
			case 30: // questitem
				results.append("[unimplemented function]");
				break;
			case 16: // hitprcnt
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E!=null)&&(E instanceof MOB))
				{
					double hitPctD=Util.div(((MOB)E).curState().getHitPoints(),((MOB)E).maxState().getHitPoints());
					int val1=(int)Math.round(hitPctD*100.0);
					results.append(val1);
				}
				break;
			}
			case 50: // isseason
			{
				if(monster.location()!=null)
					results.append(Area.SEASON_DESCS[monster.location().getArea().getSeasonCode()]);
				break;
			}
			case 51: // isweather
			{
				if(monster.location()!=null)
					results.append(Area.WEATHER_DESCS[monster.location().getArea().weatherType(monster.location())]);
				break;
			}
			case 38: // istime
			{
				if(lastKnownLocation!=null)
					results.append(Area.TOD_DESC[lastKnownLocation.getArea().getTODCode()].toLowerCase());
				break;
			}
			case 39: // isday
			{
				if(lastKnownLocation!=null)
					results.append(""+lastKnownLocation.getArea().getDayOfMonth());
				break;
			}
			case 43: // roommob
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				Environmental which=null;
				if(lastKnownLocation!=null)
					which=lastKnownLocation.fetchInhabitant(Util.s_int(arg1));
				if(which!=null)
					results.append(which.name());
				break;
			}
			case 44: // roomitem
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				Environmental which=null;
				int ct=0;
				if(lastKnownLocation!=null)
				for(int i=0;i<lastKnownLocation.numItems();i++)
				{
					Item I=lastKnownLocation.fetchItem(i);
					if((I!=null)&&(I.container()==null))
					{
						if(ct==Util.s_int(arg1))
						{ which=I; break;}
						ct++;
					}
				}
				if(which!=null)
					results.append(which.name());
				break;
			}
			case 45: // nummobsroom
			{
				if(lastKnownLocation!=null)
					results.append(""+lastKnownLocation.numInhabitants());
				break;
			}
			case 46: // numitemsroom
			{
				int ct=0;
				if(lastKnownLocation!=null)
				for(int i=0;i<lastKnownLocation.numItems();i++)
				{
					Item I=lastKnownLocation.fetchItem(i);
					if((I!=null)&&(I.container()==null))
						ct++;
				}
				results.append(""+ct);
				break;
			}
			case 47: //mobitem
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),1));
				MOB M=null;
				if(lastKnownLocation!=null)
					M=lastKnownLocation.fetchInhabitant(Util.s_int(arg1));
				Item which=null;
				int ct=0;
				if(M!=null)
				for(int i=0;i<M.inventorySize();i++)
				{
					Item I=M.fetchInventory(i);
					if((I!=null)&&(I.container()==null))
					{
						if(ct==Util.s_int(arg2))
						{ which=I; break;}
						ct++;
					}
				}
				if(which!=null)
					results.append(which.name());
				break;
			}
			case 48: // numitemsmob
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0));
				MOB which=null;
				if(lastKnownLocation!=null)
					which=lastKnownLocation.fetchInhabitant(Util.s_int(arg1));
				int ct=0;
				if(which!=null)
				for(int i=0;i<which.inventorySize();i++)
				{
					Item I=which.fetchInventory(i);
					if((I!=null)&&(I.container()==null))
						ct++;
				}
				results.append(""+ct);
				break;
			}
			case 36: // ishere
			{
				if(lastKnownLocation!=null)
					results.append(lastKnownLocation.getArea().name());
				break;
			}
			case 17: // inroom
			{
				if(lastKnownLocation!=null)
					results.append(lastKnownLocation.displayText());
				break;
			}
			case 37: // inlocale
			{
				if(lastKnownLocation!=null)
					results.append(lastKnownLocation.name());
				break;
			}
			case 18: // sex
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E!=null)&&(E instanceof MOB))
					results.append(((MOB)E).charStats().genderName());
				break;
			}
			case 13: // stat
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if(E!=null)
				{
					boolean found=false;
					String val="";
					for(int i=0;i<E.getStatCodes().length;i++)
					{
						if(E.getStatCodes()[i].equalsIgnoreCase(arg2))
						{
							val=E.getStat(arg2);
							found=true; break;
						}
					}
					if((!found)&&(E instanceof MOB))
					{
						MOB M=(MOB)E;
						for(int i=0;i<CharStats.TRAITS.length;i++)
							if(CharStats.TRAITS[i].equalsIgnoreCase(arg2))
							{
								val=""+M.charStats().getStat(CharStats.TRAITS[i]);
								found=true;
								break;
							}
						if(!found)
						for(int i=0;i<M.curState().getCodes().length;i++)
							if(M.curState().getCodes()[i].equalsIgnoreCase(arg2))
							{
								val=M.curState().getStat(M.curState().getCodes()[i]);
								found=true;
								break;
							}
						if(!found)
						for(int i=0;i<M.envStats().getCodes().length;i++)
							if(M.envStats().getCodes()[i].equalsIgnoreCase(arg2))
							{
								val=M.envStats().getStat(M.envStats().getCodes()[i]);
								found=true;
								break;
							}
					}

					if(!found)
					{
						Log.errOut("Scriptable","STAT Syntax -- "+monster.Name()+", unknown stat: "+arg2+" for "+E.name());
						break;
					}

					results.append(val);
					break;
				}
				break;
			}
			case 52: // gstat
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if(E!=null)
				{
					boolean found=false;
					String val="";
					if(E instanceof MOB)
					{
						for(int i=0;i<Generic.GENMOBCODES.length;i++)
						{
							if(Generic.GENMOBCODES[i].equalsIgnoreCase(arg2))
							{
								val=Generic.getGenMobStat((MOB)E,Generic.GENMOBCODES[i]);
								found=true; break;
							}
						}
						if(!found)
						{
							MOB M=(MOB)E;
							for(int i=0;i<CharStats.TRAITS.length;i++)
								if(CharStats.TRAITS[i].equalsIgnoreCase(arg2))
								{
									val=""+M.charStats().getStat(CharStats.TRAITS[i]);
									found=true;
									break;
								}
							if(!found)
							for(int i=0;i<M.curState().getCodes().length;i++)
								if(M.curState().getCodes()[i].equalsIgnoreCase(arg2))
								{
									val=M.curState().getStat(M.curState().getCodes()[i]);
									found=true;
									break;
								}
							if(!found)
							for(int i=0;i<M.envStats().getCodes().length;i++)
								if(M.envStats().getCodes()[i].equalsIgnoreCase(arg2))
								{
									val=M.envStats().getStat(M.envStats().getCodes()[i]);
									found=true;
									break;
								}
						}
					}
					else
					if(E instanceof Item)
					{
						for(int i=0;i<Generic.GENITEMCODES.length;i++)
						{
							if(Generic.GENITEMCODES[i].equalsIgnoreCase(arg2))
							{
								val=Generic.getGenItemStat((Item)E,Generic.GENITEMCODES[i]);
								found=true; break;
							}
						}
					}

					if(!found)
					{
						Log.errOut("Scriptable","GSTAT Syntax -- "+monster.Name()+", unknown stat: "+arg2+" for "+E.name());
						break;
					}

					results.append(val);
					break;
				}
				break;
			}
			case 19: // position
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E!=null)&&(E instanceof MOB))
				{
					String sex="STANDING";
					if(Sense.isSleeping(E))
						sex="SLEEPING";
					else
					if(Sense.isSitting(E))
						sex="SITTING";
					results.append(sex);
					break;
				}
				break;
			}
			case 20: // level
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if(E!=null)
					results.append(E.envStats().level());
				break;
			}
			case 21: // class
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E!=null)&&(E instanceof MOB))
				{
					String sex=((MOB)E).charStats().displayClassName().toUpperCase();
					results.append(sex);
				}
				break;
			}
			case 22: // baseclass
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E!=null)&&(E instanceof MOB))
				{
					String sex=((MOB)E).charStats().getCurrentClass().baseClass().toUpperCase();
					results.append(sex);
				}
				break;
			}
			case 23: // race
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E!=null)&&(E instanceof MOB))
				{
					String sex=((MOB)E).charStats().raceName().toUpperCase();
					results.append(sex);
				}
				break;
			}
			case 24: //racecat
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E!=null)&&(E instanceof MOB))
				{
					String sex=((MOB)E).charStats().getMyRace().racialCategory().toUpperCase();
					results.append(sex);
				}
				break;
			}
			case 25: // goldamt
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if(E==null)
					results.append(false);
				else
				{
					int val1=0;
					if(E instanceof MOB)
						val1=((MOB)E).getMoney();
					else
					if(E instanceof Coins)
						val1=((Coins)E).numberOfCoins();
					else
					if(E instanceof Item)
						val1=((Item)E).value();
					else
					{
						Log.errOut("Scriptable","GOLDAMT Syntax -- "+monster.Name()+", "+evaluable);
						return results.toString();
					}
					results.append(val1);
				}
				break;
			}
			case 26: // objtype
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if(E!=null)
				{
					String sex=CMClass.className(E).toLowerCase();
					results.append(sex);
				}
				break;
			}
			case 53: // incontainer
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if(E!=null)
				{
					if(E instanceof MOB)
					{
						if(((MOB)E).riding()!=null)
							results.append(((MOB)E).riding().Name());
					}
					else
					if(E instanceof Item)
					{
						if(((Item)E).riding()!=null)
							results.append(((Item)E).container().Name());
					}
				}
				break;
			}
			case 27: // var
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1).toUpperCase();
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if(E!=null)
				{
					Hashtable H=(Hashtable)Resources.getResource("SCRIPTVAR-"+E.Name());
					String val="";
					if(H!=null)
					{
						val=(String)H.get(arg2);
						if(val==null) val="";
					}
					results.append(val);
				}
				break;
			}
			case 41: // eval
				results.append("[unimplemented function]");
				break;
			case 40: // number
			{
				String val=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0)).trim();
				boolean isnumber=(val.length()>0);
				for(int i=0;i<val.length();i++)
					if(!Character.isDigit(val.charAt(i)))
					{ isnumber=false; break;}
				if(isnumber)
					results.append(Util.s_long(val));
				break;
			}
			case 42: // randnum
			{
				String arg1String=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(evaluable.substring(y+1,z),0)).toUpperCase();
				int arg1=Util.s_int(arg1String);
				results.append(Dice.roll(1,arg1,0));
				break;
			}
			default:
				Log.errOut("Scriptable","Unknown CMD -- "+monster.Name()+", "+evaluable);
				return results.toString();
			}
			if((z>=0)&&(z<=evaluable.length()))
			{
				evaluable=evaluable.substring(z+1).trim();
				uevaluable=uevaluable.substring(z+1).trim();
			}
		}
		return results.toString();
	}

	private MOB getRandomMOB(MOB monster, MOB randMOB, Room room)
	{
		if((room!=null)
		&&(monster!=null)
		&&(room.numInhabitants()>1)
		&&(room.isInhabitant(monster)))
		{
			int tries=0;
			while((++tries)<1000)
			{
				if((randMOB!=null)
				&&(randMOB!=monster)
				&&((!randMOB.isMonster())||(room.numPCInhabitants()==0)))
				   break;
				randMOB=room.fetchInhabitant(Dice.roll(1,room.numInhabitants(),-1));
			}
		}
		return randMOB;
	}

	public String varify(MOB source, Environmental target, MOB monster, Item primaryItem, Item secondaryItem, String msg, String varifyable)
	{
		int t=varifyable.indexOf("$");
		if((monster!=null)&&(monster.location()!=null))
			lastKnownLocation=monster.location();
		Room room=lastKnownLocation;
		MOB randMOB=null;
		while((t>=0)&&(t<varifyable.length()-1))
		{
			char c=varifyable.charAt(t+1);
			String middle="";
			String front=varifyable.substring(0,t);
			String back=varifyable.substring(t+2);
			switch(c)
			{
			case 'a':
				if(room!=null)
					return room.name();
				break;
			case 'i':
				if(monster!=null)
					middle=monster.name();
				break;
			case 'I':
				if(monster!=null)
					middle=monster.displayText();
				break;
			case 'n':
			case 'N':
				if(source!=null)
					middle=source.name();
				break;
			case 't':
			case 'T':
				if(target!=null)
					middle=target.name();
				break;
			case 'r':
			case 'R':
				randMOB=getRandomMOB(monster,randMOB,room);
				if(randMOB!=null)
					middle=randMOB.name();
				break;
			case 'j':
				if(monster!=null)
					middle=monster.charStats().heshe();
				break;
			case 'f':
				if((monster!=null)&&(monster.amFollowing()!=null))
					middle=monster.amFollowing().name();
				break;
			case 'F':
				if((monster!=null)&&(monster.amFollowing()!=null))
					middle=monster.amFollowing().charStats().heshe();
				break;
			case 'e':
				if(source!=null)
					middle=source.charStats().heshe();
				break;
			case 'E':
				if((target!=null)&&(target instanceof MOB))
					middle=((MOB)target).charStats().heshe();
				break;
			case 'J':
				randMOB=getRandomMOB(monster,randMOB,room);
				if(randMOB!=null)
					middle=randMOB.charStats().heshe();
				break;
			case 'k':
				if(monster!=null)
					middle=monster.charStats().hisher();
				break;
			case 'm':
				if(source!=null)
					middle=source.charStats().hisher();
				break;
			case 'M':
				if((target!=null)&&(target instanceof MOB))
					middle=((MOB)target).charStats().hisher();
				break;
			case 'K':
				randMOB=getRandomMOB(monster,randMOB,room);
				if(randMOB!=null)
					middle=randMOB.charStats().hisher();
				break;
			case 'o':
			case 'O':
				if(primaryItem!=null)
					middle=primaryItem.name();
				break;
			case 'g': middle=((msg==null)?"":msg.toLowerCase()); break;
			case 'G': middle=((msg==null)?"":msg); break;
			case 'p':
			case 'P':
				if(secondaryItem!=null)
					middle=secondaryItem.name();
				break;
			case 'l':
				if(room!=null)
				{
					StringBuffer str=new StringBuffer("");
					for(int i=0;i<room.numInhabitants();i++)
					{
						MOB M=room.fetchInhabitant(i);
						if((M!=null)&&(M!=monster)&&(Sense.canBeSeenBy(M,monster)))
						   str.append("\""+M.name()+"\" ");
					}
					middle=str.toString();
					break;
				}
			case 'L':
				if(room!=null)
				{
					StringBuffer str=new StringBuffer("");
					for(int i=0;i<room.numItems();i++)
					{
						Item I=room.fetchItem(i);
						if((I!=null)&&(I.container()==null)&&(Sense.canBeSeenBy(I,monster)))
						   str.append("\""+I.name()+"\" ");
					}
					middle=str.toString();
					break;
				}
			case '<':
				{
					int x=back.indexOf(">");
					if(x>=0)
					{
						String mid=back.substring(0,x);
						int y=mid.indexOf(" ");
						Environmental E=null;
						if(y>=0)
						{
							E=getArgumentItem(mid.substring(0,y).trim(),source,monster,target,primaryItem,secondaryItem,msg);
							mid=mid.substring(y+1).trim();
						}
						if(E!=null)
						{
							middle=null;
							Hashtable H=(Hashtable)Resources.getResource("SCRIPTVAR-"+E.Name());
							if(H!=null)
								middle=(String)H.get(mid);
							if(middle==null) middle="";
						}
						back=back.substring(x+1);
					}
				}
				break;
			case '[':
				{
					middle="";
					int x=back.indexOf("]");
					if(x>=0)
					{
						String mid=back.substring(0,x);
						int y=mid.indexOf(" ");
						if(y>0)
						{
							int num=Util.s_int(mid.substring(0,y));
							mid=mid.substring(y+1).trim();
							Quest Q=Quests.fetchQuest(mid);
							if(Q!=null)	middle=Q.getQuestItemName(num);
						}
						back=back.substring(x+1);
					}
				}
				break;
			case '{':
				{
					middle="";
					int x=back.indexOf("}");
					if(x>=0)
					{
						String mid=back.substring(0,x).trim();
						int y=mid.indexOf(" ");
						if(y>0)
						{
							int num=Util.s_int(mid.substring(0,y));
							mid=mid.substring(y+1).trim();
							Quest Q=Quests.fetchQuest(mid);
							if(Q!=null)	middle=Q.getQuestMobName(num);
						}
						back=back.substring(x+1);
					}
				}
				break;
			case '%':
				{
					middle="";
					int x=back.indexOf("%");
					if(x>=0)
					{
						middle=functify(source,target,monster,primaryItem,secondaryItem,msg,back.substring(0,x).trim());
						back=back.substring(x+1);
					}
				}
				break;
			//case 'a':
			case 'A':
				// unnecessary, since, in coffeemud, this is part of the name
				break;
			}
			if((middle.length()>0)
			&&(back.startsWith("."))
			&&(back.length()>1)
			&&(Character.isDigit(back.charAt(1))))
			{
				int x=1;
				while((x<back.length())
				&&(Character.isDigit(back.charAt(x))))
					x++;
				int y=Util.s_int(back.substring(1,x));
				back=back.substring(x);
				Vector V=Util.parse(middle);
				if((V.size()>0)&&(y>=0))
				{
					if(y>=V.size())
						middle="";
					else
						middle=(String)V.elementAt(y);
				}
			}
			varifyable=front+middle+back;
			t=varifyable.indexOf("$");
		}
		return varifyable;
	}

	public void mpsetvar(String name, String key, String val)
	{
		Hashtable H=(Hashtable)Resources.getResource("SCRIPTVAR-"+name);
		if(H==null)
		{
			H=new Hashtable();
			Resources.submitResource("SCRIPTVAR-"+name,H);
		}
		if(val.equals("++"))
		{
			String num=(String)H.get(key);
			val=new Integer(Util.s_int(num)+1).toString();
		}
		else
		if(val.equals("--"))
		{
			String num=(String)H.get(key);
			val=new Integer(Util.s_int(num)-1).toString();
		}
		else
		if(val.startsWith("+"))
		{
			// add via +XXX form
			val=val.substring(1);
			int amount=Util.s_int(val);
			String num=(String)H.get(key);
			val=new Integer(Util.s_int(num)+amount).toString();
		}
		else
		if(val.startsWith("-"))
		{
			// subtract -XXX form
			val=val.substring(1);
			int amount=Util.s_int(val);
			String num=(String)H.get(key);
			val=new Integer(Util.s_int(num)-amount).toString();
		}
		else
		if(val.startsWith("*"))
		{
			// multiply via *XXX form
			val=val.substring(1);
			int amount=Util.s_int(val);
			String num=(String)H.get(key);
			val=new Integer(Util.s_int(num)*amount).toString();
		}
		else
		if(val.startsWith("/"))
		{
			// divide /XXX form
			val=val.substring(1);
			int amount=Util.s_int(val);
			String num=(String)H.get(key);
			val=new Integer(Util.s_int(num)/amount).toString();
		}
		if(H.containsKey(key))
			H.remove(key);
		if(val.trim().length()>0)
			H.put(key,val);
		if(H.size()==0)
			Resources.removeResource("SCRIPTVAR-"+name);
	}
	
	
	public void execute(MOB source, Environmental target, MOB monster, Item primaryItem, Item secondaryItem, Vector script, String msg)
	{
		for(int si=1;si<script.size();si++)
		{
			String s=((String)script.elementAt(si)).trim();
			String cmd=Util.getCleanBit(s,0).toUpperCase();
			Integer methCode=(Integer)methH.get(cmd);
			if(methCode==null) methCode=new Integer(0);
			if(cmd.length()==0)
				continue;
			else
			switch(methCode.intValue())
			{
			case 19: // if
			{
				String conditionStr=(s.substring(2).trim());
				boolean condition=eval(source,target,monster,primaryItem,secondaryItem,msg,conditionStr);
				Vector V=new Vector();
				V.addElement("");
				int depth=0;
				boolean foundendif=false;
				si++;
				while(si<script.size())
				{
					s=((String)script.elementAt(si)).trim();
					cmd=Util.getCleanBit(s,0).toUpperCase();
					if(cmd.equals("ENDIF")&&(depth==0))
					{
						foundendif=true;
						break;
					}
					else
					if(cmd.equals("ELSE")&&(depth==0))
					{
						condition=!condition;
						if(s.substring(4).trim().length()>0)
						{
							script.setElementAt("ELSE",si);
							script.insertElementAt(s.substring(4).trim(),si+1);
						}
					}
					else
					{
						if(condition)
							V.addElement(s);
						if(cmd.equals("IF"))
							depth++;
						else
						if(cmd.equals("ENDIF"))
							depth--;
					}
					si++;
				}
				if(!foundendif)
				{
					Log.errOut("Scriptable","IF without ENDIF! for "+monster.Name());
					return;
				}
				if(V.size()>1)
				{
					//source.tell("Starting "+conditionStr);
					//for(int v=0;v<V.size();v++)
					//	source.tell("Statement "+((String)V.elementAt(v)));
					execute(source,target,monster,primaryItem,secondaryItem,V,msg);
					//source.tell("Stopping "+conditionStr);
				}
				break;
			}
			case 50: // break;
				return;
			case 1: // mpasound
			{
				String echo=varify(source,target,monster,primaryItem,secondaryItem,msg,s.substring(8).trim());
				lastKnownLocation.show(monster,null,Affect.MSG_OK_ACTION,echo);
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					Room R2=lastKnownLocation.getRoomInDir(d);
					Exit E2=lastKnownLocation.getExitInDir(d);
					if((R2!=null)&&(E2!=null)&&(E2.isOpen()))
						R2.show(monster,null,Affect.MSG_OK_ACTION,echo);
				}
				break;
			}
			case 4: // mpjunk
			{
				s=varify(source,target,monster,primaryItem,secondaryItem,msg,s.substring(6).trim());
				if(s.equalsIgnoreCase("all"))
				{
					while(monster.inventorySize()>0)
					{
						Item I=monster.fetchInventory(0);
						if(I!=null) I.destroy();
					}
				}
				else
				{
					Item I=monster.fetchInventory(s);
					if(I!=null)
						I.destroy();
				}
				break;
			}
			case 2: // mpecho
			{
				if(lastKnownLocation!=null)
					lastKnownLocation.show(monster,null,Affect.MSG_OK_ACTION,varify(source,target,monster,primaryItem,secondaryItem,msg,s.substring(6).trim()));
				break;
			}
			case 13: // mpunaffect
			{
				Environmental newTarget=getArgumentItem(Util.getCleanBit(s,1),source,monster,target,primaryItem,secondaryItem,msg);
				if(newTarget!=null)
				for(int a=newTarget.numAffects()-1;a>=0;a--)
				{
					Ability A=newTarget.fetchAffect(a);
					if(A!=null)
						A.unInvoke();
				}
				break;
			}
			case 3: // mpslay
			{
				Environmental newTarget=getArgumentItem(Util.getCleanBit(s,1),source,monster,target,primaryItem,secondaryItem,msg);
				if((newTarget!=null)&&(newTarget instanceof MOB))
					ExternalPlay.postDeath((MOB)newTarget,monster,null);
				break;
			}
			case 16: // mpset
			{
				Environmental newTarget=getArgumentItem(Util.getCleanBit(s,1),source,monster,target,primaryItem,secondaryItem,msg);
				String arg2=Util.getCleanBit(s,2);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(s,2));
				if(newTarget!=null)
				{
					boolean found=false;
					for(int i=0;i<newTarget.getStatCodes().length;i++)
					{
						if(newTarget.getStatCodes()[i].equalsIgnoreCase(arg2))
						{
							newTarget.setStat(arg2,arg3);
							found=true;
							break;
						}
					}
					if((!found)&&(newTarget instanceof MOB))
					{
						MOB M=(MOB)newTarget;
						for(int i=0;i<CharStats.TRAITS.length;i++)
							if(CharStats.TRAITS[i].equalsIgnoreCase(arg2))
							{
								M.baseCharStats().setStat(i,Util.s_int(arg3));
								M.recoverCharStats();
								found=true;
								break;
							}
						if(!found)
						for(int i=0;i<M.curState().getCodes().length;i++)
							if(M.curState().getCodes()[i].equalsIgnoreCase(arg2))
							{
								M.curState().setStat(arg2,arg3);
								found=true;
								break;
							}
						if(!found)
						for(int i=0;i<M.baseEnvStats().getCodes().length;i++)
							if(M.baseEnvStats().getCodes()[i].equalsIgnoreCase(arg2))
							{
								M.baseEnvStats().setStat(arg2,arg3);
								found=true;
								break;
							}
					}

					if(!found)
					{
						Log.errOut("Scriptable","MPSET Syntax -- "+monster.Name()+", unknown stat: "+arg2+" for "+newTarget.Name());
						break;
					}
				}
				break;
			}
			case 35: // mpgset
			{
				Environmental newTarget=getArgumentItem(Util.getCleanBit(s,1),source,monster,target,primaryItem,secondaryItem,msg);
				String arg2=Util.getCleanBit(s,2);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(s,2));
				if(newTarget!=null)
				{
					boolean found=false;
					if(newTarget instanceof MOB)
					{
						for(int i=0;i<Generic.GENMOBCODES.length;i++)
						{
							if(Generic.GENMOBCODES[i].equalsIgnoreCase(arg2))
							{
								Generic.setGenMobStat((MOB)newTarget,Generic.GENMOBCODES[i],arg3);
								found=true; break;
							}
						}
						if(!found)
						{
							MOB M=(MOB)newTarget;
							for(int i=0;i<CharStats.TRAITS.length;i++)
								if(CharStats.TRAITS[i].equalsIgnoreCase(arg2))
								{
									M.baseCharStats().setStat(i,Util.s_int(arg3));
									M.recoverCharStats();
									found=true;
									break;
								}
							if(!found)
							for(int i=0;i<M.curState().getCodes().length;i++)
								if(M.curState().getCodes()[i].equalsIgnoreCase(arg2))
								{
									M.curState().setStat(arg2,arg3);
									found=true;
									break;
								}
							if(!found)
							for(int i=0;i<M.baseEnvStats().getCodes().length;i++)
								if(M.baseEnvStats().getCodes()[i].equalsIgnoreCase(arg2))
								{
									M.baseEnvStats().setStat(arg2,arg3);
									found=true;
									break;
								}
						}
					}
					else
					if(newTarget instanceof Item)
					{
						for(int i=0;i<Generic.GENITEMCODES.length;i++)
						{
							if(Generic.GENITEMCODES[i].equalsIgnoreCase(arg2))
							{
								Generic.setGenItemStat((Item)newTarget,Generic.GENITEMCODES[i],arg3);
								found=true; break;
							}
						}
					}

					if(!found)
					{
						Log.errOut("Scriptable","MPSET Syntax -- "+monster.Name()+", unknown stat: "+arg2+" for "+newTarget.Name());
						break;
					}
				}
				break;
			}
			case 11: // mpexp
			{
				Environmental newTarget=getArgumentItem(Util.getCleanBit(s,1),source,monster,target,primaryItem,secondaryItem,msg);
				int t=Util.s_int(varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(s,2)));
				if((t>0)&&(newTarget!=null)&&(newTarget instanceof MOB))
				{
					Hashtable group=((MOB)newTarget).getGroupMembers(new Hashtable());
					if(!group.contains(newTarget))
						group.put(newTarget,newTarget);
					for(Enumeration e=group.elements();e.hasMoreElements();)
					{
						MOB M=(MOB)e.nextElement();
						if(M.location()==lastKnownLocation)
							M.charStats().getCurrentClass().gainExperience(M,null,M.getLeigeID(),t,false);
					}
				}
				break;
			}
			case 5: // mpmload
			{
				s=varify(source,target,monster,primaryItem,secondaryItem,msg,s.substring(7).trim());
				MOB m=CMClass.getMOB(s);
				if(lastKnownLocation!=null)
				{
					if(m==null)
					{
						Environmental e=findSomethingCalledThis(s,lastKnownLocation,true);
						if(e instanceof MOB)
							m=(MOB)e;
					}
					if(m!=null)
					{
						m=(MOB)m.copyOf();
						m.recoverEnvStats();
						m.recoverCharStats();
						m.resetToMaxState();
						m.bringToLife(lastKnownLocation,true);
					}
				}
				break;
			}
			case 6: // mpoload
			{
				s=varify(source,target,monster,primaryItem,secondaryItem,msg,s.substring(7).trim());
				if(Util.s_int(s)>0)
					monster.setMoney(monster.getMoney()+Util.s_int(s));
				else
				if(lastKnownLocation!=null)
				{
					Item m=CMClass.getItem(s);
					if(m==null)
					{
						Environmental e=findSomethingCalledThis(s,lastKnownLocation,false);
						if(e instanceof Item)
							m=(Item)e;
					}
					if(m!=null)
					{
						m=(Item)m.copyOf();
						m.recoverEnvStats();
						monster.addInventory(m);
						monster.recoverCharStats();
						monster.recoverEnvStats();
						monster.recoverMaxState();
					}
				}
				break;
			}
			case 7: // mpechoat
			{
				Environmental newTarget=getArgumentItem(Util.getCleanBit(s,1),source,monster,target,primaryItem,secondaryItem,msg);
				if((newTarget!=null)&&(newTarget instanceof MOB)&&(lastKnownLocation!=null))
				{
					s=Util.getPastBit(s,1).trim();
					lastKnownLocation.showSource((MOB)newTarget,null,Affect.MSG_OK_ACTION,varify(source,target,monster,primaryItem,secondaryItem,msg,s));
				}
				break;
			}
			case 8: // mpechoaround
			{
				Environmental newTarget=getArgumentItem(Util.getCleanBit(s,1),source,monster,target,primaryItem,secondaryItem,msg);
				if((newTarget!=null)&&(newTarget instanceof MOB)&&(lastKnownLocation!=null))
				{
					s=Util.getPastBit(s,1).trim();
					lastKnownLocation.showOthers((MOB)newTarget,null,Affect.MSG_OK_ACTION,varify(source,target,monster,primaryItem,secondaryItem,msg,s));
				}
				break;
			}
			case 9: // mpcast
			{
				String cast=Util.getCleanBit(s,1);
				Environmental newTarget=getArgumentItem(Util.getCleanBit(s,2),source,monster,target,primaryItem,secondaryItem,msg);
				Ability A=null;
				if(cast!=null) A=CMClass.findAbility(cast);
				if((newTarget!=null)&&(A!=null))
				{
					A.setProfficiency(100);
					A.invoke(monster,newTarget,false);
				}
				break;
			}
			case 30: // mpaffect
			{
				String cast=Util.getCleanBit(s,1);
				Environmental newTarget=getArgumentItem(Util.getCleanBit(s,2),source,monster,target,primaryItem,secondaryItem,msg);
				String m2=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(s,3));
				Ability A=null;
				if(cast!=null) A=CMClass.findAbility(cast);
				if((newTarget!=null)&&(A!=null))
				{
					A.setMiscText(m2);
					if((A.classificationCode()&Ability.ALL_CODES)==A.PROPERTY)
						newTarget.addNonUninvokableAffect(A);
					else
						A.invoke(monster,Util.parse(m2),newTarget,true);
				}
				break;
			}
			case 31: // mpbehave
			{
				String cast=Util.getCleanBit(s,1);
				Environmental newTarget=getArgumentItem(Util.getCleanBit(s,2),source,monster,target,primaryItem,secondaryItem,msg);
				String m2=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(s,3));
				Behavior A=null;
				if(cast!=null) A=CMClass.getBehavior(cast);
				if((newTarget!=null)&&(A!=null))
				{
					A.setParms(m2);
					newTarget.addBehavior(A);
				}
				break;
			}
			case 32: // mpunbehave
			{
				String cast=Util.getCleanBit(s,1);
				Environmental newTarget=getArgumentItem(Util.getCleanBit(s,2),source,monster,target,primaryItem,secondaryItem,msg);
				if(newTarget!=null)
				{
					Behavior A=newTarget.fetchBehavior(cast);
					if(A!=null) newTarget.delBehavior(A);
				}
				break;
			}
			case 33: // mptattoo
			{
				Environmental newTarget=getArgumentItem(Util.getCleanBit(s,1),source,monster,target,primaryItem,secondaryItem,msg);
				String tattooName=Util.getCleanBit(s,2);
				if((newTarget!=null)&&(tattooName.length()>0)&&(newTarget instanceof MOB))
				{
					MOB themob=(MOB)newTarget;
					boolean tattooMinus=tattooName.startsWith("-");
					if(tattooMinus)	tattooName=tattooName.substring(1);

					Ability A=themob.fetchAbility("Prop_Tattoo");
					if(A==null)
					{
						A=CMClass.getAbility("Prop_Tattoo");
						A.setBorrowed(themob,false);
						themob.addAbility(A);
					}
					int x=A.text().indexOf(";"+tattooName.toUpperCase()+";");
					if(x>=0)
					{
						if(tattooMinus)
							A.setMiscText(A.text().substring(0,x+1)+A.text().substring(x+2+tattooName.length()));
					}
					else
					{
						if(A.text().length()==0) A.setMiscText(";");
						A.setMiscText(A.text()+tattooName.toUpperCase()+";");
					}
				}
				break;
			}
			case 10: // mpkill
			{
				Environmental newTarget=getArgumentItem(Util.getCleanBit(s,1),source,monster,target,primaryItem,secondaryItem,msg);
				if((newTarget!=null)&&(newTarget instanceof MOB))
					monster.setVictim((MOB)newTarget);
				break;
			}
			case 12: // mppurge
			{
				if(lastKnownLocation!=null)
				{
					Environmental E=getArgumentItem(s.substring(7).trim(),source,monster,target,primaryItem,secondaryItem,msg);
					if(E!=null)
					{
						if(E instanceof MOB)
						{
							if(!((MOB)E).isMonster())
								((MOB)E).session().setKillFlag(true);
							else
								((MOB)E).destroy();
						}
						else
						if(E instanceof Item)
						{
							Environmental oE=((Item)E).owner();
							((Item)E).destroy();
							if(oE!=null) oE.recoverEnvStats();
						}
					}
					lastKnownLocation.recoverRoomStats();
				}
				break;
			}
			case 14: // mpgoto
			{
				s=varify(source,target,monster,primaryItem,secondaryItem,msg,s.substring(6).trim());
				if(lastKnownLocation!=null)
				{
					Room goHere=getRoom(s,lastKnownLocation);
					if(goHere!=null)
						goHere.bringMobHere(monster,true);
				}
				break;
			}
			case 15: // mpat
			if(lastKnownLocation!=null)
			{
				Room lastPlace=lastKnownLocation;
				String roomName=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(s,1));
				if(roomName.length()>0)
				{
					s=Util.getPastBit(s,1).trim();
					Room goHere=getRoom(roomName,lastKnownLocation);
					if(goHere!=null)
					{
						goHere.bringMobHere(monster,true);
						Vector V=new Vector();
						V.addElement("");
						V.addElement(s.trim());
						execute(source,target,monster,primaryItem,secondaryItem,V,msg);
						lastPlace.bringMobHere(monster,true);
					}
				}
				break;
			}
			case 17: // mptransfer
			{
				String mobName=Util.getCleanBit(s,1);
				String roomName=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(s,2));
				if((roomName.length()==0)&&(lastKnownLocation!=null))
					roomName=lastKnownLocation.roomID();
				if(roomName.length()>0)
				{
					s=varify(source,target,monster,primaryItem,secondaryItem,msg,mobName);
					Room newRoom=getRoom(roomName,lastKnownLocation);
					if(newRoom!=null)
					{
						Vector V=new Vector();
						if(s.equalsIgnoreCase("all"))
						{
							if(lastKnownLocation!=null)
							{
								for(int x=0;x<lastKnownLocation.numInhabitants();x++)
								{
									MOB m=lastKnownLocation.fetchInhabitant(x);
									if((m!=null)&&(m!=monster)&&(!m.isMonster())&&(!V.contains(m)))
										V.addElement(m);
								}
							}
						}
						else
						{
							MOB findOne=null;
							Area A=null;
							if(lastKnownLocation!=null)
							{
								findOne=lastKnownLocation.fetchInhabitant(s);
								A=lastKnownLocation.getArea();
							}
							if((findOne==null)&&(A!=null))
								for(Enumeration r=A.getMap();r.hasMoreElements();)
								{
									Room R=(Room)r.nextElement();
									findOne=R.fetchInhabitant(s);
									if(findOne!=null) V.addElement(findOne);
								}
							if((findOne!=null)&&(findOne!=monster)&&(!findOne.isMonster()))
								V.addElement(findOne);
						}
						for(int v=0;v<V.size();v++)
						{
							MOB mob=(MOB)V.elementAt(v);
							Hashtable h=mob.getGroupMembers(new Hashtable());
							for(Enumeration e=h.elements();e.hasMoreElements();)
							{
								MOB M=(MOB)e.nextElement();
								if((!V.contains(M))&&(M.location()==mob.location()))
								   V.addElement(M);
							}
						}
						for(int v=0;v<V.size();v++)
						{
							MOB follower=(MOB)V.elementAt(v);
							Room thisRoom=follower.location();
							FullMsg enterMsg=new FullMsg(follower,newRoom,null,Affect.MSG_ENTER,null,Affect.MSG_ENTER,null,Affect.MSG_ENTER,"<S-NAME> appears in a puff of smoke."+CommonStrings.msp("appear.wav",10));
							FullMsg leaveMsg=new FullMsg(follower,thisRoom,null,Affect.MSG_LEAVE|Affect.MASK_MAGIC,"<S-NAME> disappear(s) in a puff of smoke.");
							if(thisRoom.okAffect(follower,leaveMsg)&&newRoom.okAffect(follower,enterMsg))
							{
								if(follower.isInCombat())
								{
									ExternalPlay.flee(follower,"NOWHERE");
									follower.makePeace();
								}
								thisRoom.send(follower,leaveMsg);
								newRoom.bringMobHere(follower,false);
								newRoom.send(follower,enterMsg);
								follower.tell("\n\r\n\r");
								ExternalPlay.look(follower,null,true);
							}
						}
					}

				}
				break;
			}
			case 25: // mpbeacon
			{
				String roomName=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(s,1));
				if((roomName.length()>0)&&(lastKnownLocation!=null))
				{
					s=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(s,2));
					Room newRoom=getRoom(roomName,lastKnownLocation);
					if((newRoom!=null)&&(lastKnownLocation!=null))
					{
						Vector V=new Vector();
						if(s.equalsIgnoreCase("all"))
						{
							for(int x=0;x<lastKnownLocation.numInhabitants();x++)
							{
								MOB m=lastKnownLocation.fetchInhabitant(x);
								if((m!=null)&&(m!=monster)&&(!m.isMonster())&&(!V.contains(m)))
									V.addElement(m);
							}
						}
						else
						{
							MOB findOne=lastKnownLocation.fetchInhabitant(s);
							if((findOne!=null)&&(findOne!=monster)&&(!findOne.isMonster()))
								V.addElement(findOne);
						}
						for(int v=0;v<V.size();v++)
						{
							MOB follower=(MOB)V.elementAt(v);
							if(!follower.isMonster())
								follower.setStartRoom(newRoom);
						}
					}
				}
				break;
			}
			case 18: // mpforce
			{
				Environmental newTarget=getArgumentItem(Util.getCleanBit(s,1),source,monster,target,primaryItem,secondaryItem,msg);
				s=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getPastBit(s,1));
				if((newTarget!=null)&&(newTarget instanceof MOB))
				{
					try
					{
						Vector V=Util.parse(s);
						ExternalPlay.doCommand((MOB)newTarget,V);
					}
					catch(Exception e)
					{
						Log.errOut("Scriptable",e);
					}
				}
			}
			case 20: // mpsetvar
			{
				Environmental E=getArgumentItem(Util.getCleanBit(s,1),source,monster,target,primaryItem,secondaryItem,msg);
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(s,2));
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(s,3));
				if((E!=null)&&(arg2.length()>0))
					mpsetvar(E.Name(),arg2,arg3);
				break;
			}
			case 36: // mpsavevar
			{
				String arg1=Util.getCleanBit(s,1);
				String arg2=Util.getCleanBit(s,2).toUpperCase();
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E!=null)&&(arg2.length()==0))
				{
					Hashtable H=(Hashtable)Resources.getResource("SCRIPTVAR-"+E.Name());
					String val="";
					if(H!=null)
					{
						val=(String)H.get(arg2);
						if(val==null) val="";
					}
					ExternalPlay.DBDeleteData(E.Name(),"SCRIPTABLEVARS",arg2);
					ExternalPlay.DBCreateData(E.Name(),"SCRIPTABLEVARS",arg2,val);
				}
				break;
			}
			case 39: // mploadvar
			{
				String arg1=Util.getCleanBit(s,1);
				String arg2=Util.getCleanBit(s,2).toUpperCase();
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem,msg);
				if((E!=null)&&(arg2.length()==0))
				{
					Vector V=ExternalPlay.DBReadData(E.Name(),"SCRIPTABLEVARS",arg2);
					if((V!=null)&&(V.size()>3))
						mpsetvar(E.Name(),arg2,(String)V.elementAt(3));
				}
				break;
			}
			case 28: // mpdamage
			{
				Environmental newTarget=getArgumentItem(Util.getCleanBit(s,1),source,monster,target,primaryItem,secondaryItem,msg);
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(s,2));
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(s,3));
				String arg4=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(s,4));
				if((newTarget!=null)&&(arg2.length()>0)&&(newTarget instanceof MOB))
				{
					MOB E=(MOB)newTarget;
					int min=Util.s_int(arg2);
					int max=Util.s_int(arg3);
					if(max<min) max=min;
					if(min>0)
					{
						int dmg=(max==min)?min:Dice.roll(1,max-min,min);
						if((dmg>=E.curState().getHitPoints())&&(!arg4.equalsIgnoreCase("kill")))
							dmg=E.curState().getHitPoints()-1;
						if(dmg>0)
							ExternalPlay.postDamage(E,E,null,dmg,Affect.NO_EFFECT,-1,null);
					}
				}
				break;
			}
			case 29: // mptrackto
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(s,1));
				Ability A=CMClass.getAbility("Skill_Track");
				if(A!=null)	A.invoke(monster,Util.parse(arg1),null,true);
				break;
			}
			case 21: //MPENDQUEST
			{
				s=varify(source,target,monster,primaryItem,secondaryItem,msg,s.substring(10).trim());
				Quest Q=Quests.fetchQuest(s);
				if(Q!=null) Q.stopQuest();
				else
					Log.errOut("Scriptable","MPENDQUEST -- unknown quest: "+s);
				break;
			}
			case 23: //MPSTARTQUEST
			{
				s=varify(source,target,monster,primaryItem,secondaryItem,msg,s.substring(12).trim());
				Quest Q=Quests.fetchQuest(s);
				if(Q!=null) Q.startQuest();
				else
					Log.errOut("Scriptable","MPSTARTQUEST -- unknown quest: "+s);
				break;
			}
			case 22: //MPQUESTWIN
			{
				String whoName=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(s,1));
				MOB M=null;
				if(lastKnownLocation!=null)
					M=lastKnownLocation.fetchInhabitant(whoName);
				if(M!=null) whoName=M.Name();
				if(whoName.length()>0)
				{
					s=Util.getCleanBit(s,2);
					Quest Q=Quests.fetchQuest(s);
					if(Q!=null) Q.declareWinner(whoName);
					else
						Log.errOut("Scriptable","MYQUESTWIN -- unknown quest: "+s);
				}
				break;
			}
			case 24: // MPCALLFUNC
			{
				String named=Util.getCleanBit(s,1);
				String parms=Util.getPastBit(s,1).trim();
				boolean found=false;
				Vector scripts=getScripts();
				for(int v=0;v<scripts.size();v++)
				{
					Vector script2=(Vector)scripts.elementAt(v);
					if(script.size()<1) continue;
					String trigger=((String)script2.elementAt(0)).toUpperCase().trim();
					if(getTriggerCode(trigger)==17)
					{
						String fnamed=Util.getCleanBit(trigger,1);
						if(fnamed.equalsIgnoreCase(named))
						{
							found=true;
							execute(source,target,monster,primaryItem,secondaryItem,script2,parms);
							break;
						}
					}
				}
				if(!found)
					Log.errOut("Scriptable","MPCALLFUNC -- "+monster.Name()+", no function: "+named);
				break;
			}
			case 27: // MPWHILE
			{
				String conditionStr=(s.substring(2).trim());
				int x=conditionStr.indexOf("(");
				if(x<0) 
				{
					Log.errOut("Scriptable","MPWHILE -- "+monster.Name()+", no condition: "+s);
					break;
				}
				conditionStr=conditionStr.substring(x+1);
				x=-1;
				int depth=0;
				for(int i=0;i<conditionStr.length();i++)
					if(conditionStr.charAt(i)=='(')
						depth++;
					else
					if((conditionStr.charAt(i)==')')&&((--depth)<0))
					{
						x=i;
						break;
					}
				if(x<0)
				{
					Log.errOut("Scriptable","MPWHILE -- "+monster.Name()+", no closing ')': "+s);
					break;
				}
				String cmd2=conditionStr.substring(x+1).trim();
				conditionStr=conditionStr.substring(0,x);
				Vector vscript=new Vector();
				vscript.addElement("FUNCTION_PROG MPWHILE_"+Math.random());
				vscript.addElement(cmd2);
				long time=System.currentTimeMillis();
				while((eval(source,target,monster,primaryItem,secondaryItem,msg,conditionStr))&&((System.currentTimeMillis()-time)<4000))
					execute(source,target,monster,primaryItem,secondaryItem,vscript,msg);
				if((System.currentTimeMillis()-time)>=4000)
				{
					Log.errOut("Scriptable","MPWHILE -- "+monster.Name()+", 4 second limit exceeded: "+conditionStr);
					break;
				}
				break;
			}
			case 26: // MPALARM
			{
				String time=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(s,1));
				String parms=Util.getPastBit(s,1).trim();
				if(Util.s_int(time)<=0)
				{
					Log.errOut("Scriptable","MPALARM -- "+monster.Name()+", bad time "+time);
					break;
				}
				if(parms.length()==0)
				{
					Log.errOut("Scriptable","MPALARM -- "+monster.Name()+", No command!");
					break;
				}
				Vector vscript=new Vector();
				vscript.addElement("FUNCTION_PROG ALARM_"+time);
				vscript.addElement(parms);
				que.insertElementAt(new ScriptableResponse(source,target,monster,primaryItem,secondaryItem,vscript,Util.s_int(time),msg),0);
				break;
			}
			case 37: // mpenable
			{
				String cast=Util.getCleanBit(s,1);
				Environmental newTarget=getArgumentItem(Util.getCleanBit(s,2),source,monster,target,primaryItem,secondaryItem,msg);
				String p2=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(s,3));
				String m2=varify(source,target,monster,primaryItem,secondaryItem,msg,Util.getCleanBit(s,4));
				Ability A=null;
				if(cast!=null) A=CMClass.getAbility(cast);
				if((newTarget!=null)&&(A!=null)&&(newTarget instanceof MOB))
				{
					A.setProfficiency(Util.s_int(p2));
					A.setMiscText(m2);
					((MOB)newTarget).addAbility(A);
				}
				break;
			}
			case 38: // mpdisable
			{
				String cast=Util.getCleanBit(s,1);
				Environmental newTarget=getArgumentItem(Util.getCleanBit(s,2),source,monster,target,primaryItem,secondaryItem,msg);
				if((newTarget!=null)&&(newTarget instanceof MOB))
				{
					Ability A=((MOB)newTarget).fetchAbility(cast);
					if(A!=null)((MOB)newTarget).delAbility(A);
				}
				break;
			}
			default:
				if(cmd.length()>0)
				{
					try
					{
						Vector V=Util.parse(varify(source,target,monster,primaryItem,secondaryItem,msg,s));
						if(V.size()>0)
							ExternalPlay.doCommand(monster,V);
					}
					catch(Exception e)
					{
						Log.errOut("Scriptable",e);
					}
				}
				break;
			}
		}
	}

	protected Vector getScripts()
	{
		Vector scripts=null;
		if(getParms().length()>100)
			scripts=(Vector)Resources.getResource("PARSED SCRIPTS: "+getParms().substring(0,100)+getParms().length()+getParms().hashCode());
		else
			scripts=(Vector)Resources.getResource("PARSED SCRIPTS: "+getParms());
		if(scripts==null)
		{
			String script=getParms();
			script=Util.replaceAll(script,"`","'");
			scripts=parseScripts(script);
			if(getParms().length()>100)
				Resources.submitResource("PARSED SCRIPTS: "+getParms().substring(0,100)+getParms().length()+getParms().hashCode(),scripts);
			else
				Resources.submitResource("PARSED SCRIPTS: "+getParms(),scripts);
		}
		return scripts;
	}

	public boolean match(String str, String patt)
	{
		if(patt.trim().equalsIgnoreCase("ALL")) 
			return true;
		if(patt.length()==0) 
			return true;
		if(str.length()==0) 
			return false;
		if(str.indexOf(patt)>=0) 
			return true;
		return false;
	}
	
	public void affect(Environmental affecting, Affect affect)
	{
		super.affect(affecting,affect);
		if(affecting==null) return;
		MOB monster=getScriptableMOB(affecting);
		if((monster==null)||(monster.amDead())||(lastKnownLocation==null)) return;
		Item defaultItem=(affecting instanceof Item)?(Item)affecting:null;

		Vector scripts=getScripts();

		if(affect.source()==null) return;

		if(affect.amITarget(monster)
		&&(!affect.amISource(monster))
		&&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&(affect.source()!=monster))
			lastToHurtMe=affect.source();

		for(int v=0;v<scripts.size();v++)
		{
			Vector script=(Vector)scripts.elementAt(v);
			if(script.size()<1) continue;

			String trigger=((String)script.elementAt(0)).toUpperCase().trim();
			switch(getTriggerCode(trigger))
			{
			case 1: // greet_prog
				if((affect.targetMinor()==Affect.TYP_ENTER)
				&&(affect.amITarget(lastKnownLocation))
				&&(!affect.amISource(monster))
				&&(canFreelyBehaveNormal(monster))
				&&(Sense.canSenseMoving(affect.source(),affecting)))
				{
					int prcnt=Util.s_int(Util.getCleanBit(trigger,1));
					if((Dice.rollPercentage()<prcnt)&&(!affect.source().isMonster()))
					{
						que.addElement(new ScriptableResponse(affect.source(),monster,monster,defaultItem,null,script,2,null));
						return;
					}
				}
				break;
			case 2: // allgreet_prog
				if((affect.targetMinor()==Affect.TYP_ENTER)
				&&(affect.amITarget(lastKnownLocation))
				&&(!affect.amISource(monster))
				&&(canActAtAll(monster)))
				{
					int prcnt=Util.s_int(Util.getCleanBit(trigger,1));
					if((Dice.rollPercentage()<prcnt)&&(!affect.source().isMonster()))
					{
						que.addElement(new ScriptableResponse(affect.source(),monster,monster,defaultItem,null,script,2,null));
						return;
					}
				}
				break;
			case 3: // speech_prog
				if((affect.sourceMinor()==Affect.TYP_SPEAK)
				&&(!affect.amISource(monster))
				&&(affect.othersMessage()!=null)
				&&(canFreelyBehaveNormal(monster)))
				{
					String msg=affect.othersMessage().toUpperCase();
					if(msg.indexOf("\'")>=0)
					{
						msg=msg.substring(msg.indexOf("\'")+1);
						if(msg.indexOf("\'")>=0)
							msg=msg.substring(0,msg.indexOf("\'"));
					}
					msg=(" "+msg+" ").toUpperCase();
					trigger=trigger.substring(11).trim();
					if(Util.getCleanBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim();
						if(match(msg,trigger))
						{
							que.addElement(new ScriptableResponse(affect.source(),affect.target(),monster,defaultItem,null,script,2,msg));
							return;
						}
					}
					else
					{
						int num=Util.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=Util.getCleanBit(trigger,i);
							if(msg.indexOf(" "+t+" ")>=0)
							{
								que.addElement(new ScriptableResponse(affect.source(),affect.target(),monster,defaultItem,null,script,2,t));
								return;
							}
						}
					}
				}
				break;
			case 4: // give_prog
				if((affect.targetMinor()==Affect.TYP_GIVE)
				&&((affect.amITarget(monster))||(affect.tool()==affecting))
				&&(!affect.amISource(monster))
				&&(affect.tool() instanceof Item)
				&&(canFreelyBehaveNormal(monster)))
				{
					trigger=trigger.substring(9).trim();
					if(Util.getCleanBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim().toUpperCase();
						if(((" "+trigger+" ").indexOf(affect.tool().Name().toUpperCase())>=0)
						||(trigger.equalsIgnoreCase("ALL")))
						{
							que.addElement(new ScriptableResponse(affect.source(),monster,monster,(Item)affect.tool(),defaultItem,script,2,null));
							return;
						}
					}
					else
					{
						int num=Util.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=Util.getCleanBit(trigger,i).toUpperCase();
							if(((" "+affect.tool().Name().toUpperCase()+" ").indexOf(" "+t+" ")>=0)
							||(t.equalsIgnoreCase("ALL")))
							{
								que.addElement(new ScriptableResponse(affect.source(),monster,monster,(Item)affect.tool(),defaultItem,script,2,null));
								return;
							}
						}
					}
				}
				break;
			case 20: // get_prog
				if((affect.targetMinor()==Affect.TYP_GET)
				&&((affect.amISource(monster))||(affect.amITarget(affecting)))
				&&(affect.target() instanceof Item)
				&&(canFreelyBehaveNormal(monster)))
				{
					trigger=trigger.substring(9).trim();
					if(Util.getCleanBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim().toUpperCase();
						if(((" "+trigger+" ").indexOf(affect.target().Name().toUpperCase())>=0)
						||(trigger.equalsIgnoreCase("ALL")))
						{
							que.addElement(new ScriptableResponse(affect.source(),monster,monster,(Item)affect.target(),defaultItem,script,2,null));
							return;
						}
					}
					else
					{
						int num=Util.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=Util.getCleanBit(trigger,i).toUpperCase();
							if(((" "+affect.target().Name().toUpperCase()+" ").indexOf(" "+t+" ")>=0)
							||(t.equalsIgnoreCase("ALL")))
							{
								que.addElement(new ScriptableResponse(affect.source(),monster,monster,(Item)affect.target(),defaultItem,script,2,null));
								return;
							}
						}
					}
				}
				break;
			case 22: // drop_prog
				if((affect.targetMinor()==Affect.TYP_DROP)
				&&((affect.amISource(monster))||(affect.amITarget(affecting)))
				&&(affect.target() instanceof Item)
				&&(canFreelyBehaveNormal(monster)))
				{
					trigger=trigger.substring(9).trim();
					if(Util.getCleanBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim().toUpperCase();
						if(((" "+trigger+" ").indexOf(affect.target().Name().toUpperCase())>=0)
						||(trigger.equalsIgnoreCase("ALL")))
						{
							que.addElement(new ScriptableResponse(affect.source(),monster,monster,(Item)affect.target(),defaultItem,script,2,null));
							return;
						}
					}
					else
					{
						int num=Util.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=Util.getCleanBit(trigger,i).toUpperCase();
							if(((" "+affect.target().Name().toUpperCase()+" ").indexOf(" "+t+" ")>=0)
							||(t.equalsIgnoreCase("ALL")))
							{
								que.addElement(new ScriptableResponse(affect.source(),monster,monster,(Item)affect.target(),defaultItem,script,2,null));
								return;
							}
						}
					}
				}
				break;
			case 24: // remove_prog
				if((affect.targetMinor()==Affect.TYP_REMOVE)
				&&((affect.amISource(monster))||(affect.amITarget(affecting)))
				&&(affect.target() instanceof Item)
				&&(canFreelyBehaveNormal(monster)))
				{
					trigger=trigger.substring(9).trim();
					if(Util.getCleanBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim().toUpperCase();
						if(((" "+trigger+" ").indexOf(affect.target().Name().toUpperCase())>=0)
						||(trigger.equalsIgnoreCase("ALL")))
						{
							que.addElement(new ScriptableResponse(affect.source(),monster,monster,(Item)affect.target(),defaultItem,script,2,null));
							return;
						}
					}
					else
					{
						int num=Util.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=Util.getCleanBit(trigger,i).toUpperCase();
							if(((" "+affect.target().Name().toUpperCase()+" ").indexOf(" "+t+" ")>=0)
							||(t.equalsIgnoreCase("ALL")))
							{
								que.addElement(new ScriptableResponse(affect.source(),monster,monster,(Item)affect.target(),defaultItem,script,2,null));
								return;
							}
						}
					}
				}
				break;
			case 21: // put_prog
				if((affect.targetMinor()==Affect.TYP_PUT)
				&&((affect.amISource(monster))||(affect.amITarget(affecting)))
				&&(affect.tool() instanceof Item)
				&&(affect.target() instanceof Item)
				&&(canFreelyBehaveNormal(monster)))
				{
					trigger=trigger.substring(9).trim();
					if(Util.getCleanBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim().toUpperCase();
						if(((" "+trigger+" ").indexOf(affect.tool().Name().toUpperCase())>=0)
						||(trigger.equalsIgnoreCase("ALL")))
						{
							que.addElement(new ScriptableResponse(affect.source(),monster,monster,(Item)affect.target(),(Item)affect.tool(),script,2,null));
							return;
						}
					}
					else
					{
						int num=Util.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=Util.getCleanBit(trigger,i).toUpperCase();
							if(((" "+affect.tool().Name().toUpperCase()+" ").indexOf(" "+t+" ")>=0)
							||(t.equalsIgnoreCase("ALL")))
							{
								que.addElement(new ScriptableResponse(affect.source(),monster,monster,(Item)affect.target(),(Item)affect.tool(),script,2,null));
								return;
							}
						}
					}
				}
				break;
			case 23: // wear_prog
				if(((affect.targetMinor()==Affect.TYP_WEAR)
					||(affect.targetMinor()==Affect.TYP_HOLD)
					||(affect.targetMinor()==Affect.TYP_WIELD))
				&&((affect.amISource(monster))||(affect.amITarget(affecting)))
				&&(affect.target() instanceof Item)
				&&(canFreelyBehaveNormal(monster)))
				{
					trigger=trigger.substring(9).trim();
					if(Util.getCleanBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim().toUpperCase();
						if(((" "+trigger+" ").indexOf(affect.target().Name().toUpperCase())>=0)
						||(trigger.equalsIgnoreCase("ALL")))
						{
							que.addElement(new ScriptableResponse(affect.source(),monster,monster,(Item)affect.target(),defaultItem,script,2,null));
							return;
						}
					}
					else
					{
						int num=Util.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=Util.getCleanBit(trigger,i).toUpperCase();
							if(((" "+affect.target().Name().toUpperCase()+" ").indexOf(" "+t+" ")>=0)
							||(t.equalsIgnoreCase("ALL")))
							{
								que.addElement(new ScriptableResponse(affect.source(),monster,monster,(Item)affect.target(),defaultItem,script,2,null));
								return;
							}
						}
					}
				}
				break;
			case 19: // bribe_prog
				if((affect.targetMinor()==Affect.TYP_GIVE)
				&&(affect.amITarget(monster))
				&&(!affect.amISource(monster))
				&&(affect.tool() instanceof Coins)
				&&(canFreelyBehaveNormal(monster)))
				{
					trigger=trigger.substring(10).trim();
					int t=Util.s_int(trigger);
					if((((Coins)affect.tool()).numberOfCoins()>=t)
					||(trigger.equalsIgnoreCase("ALL")))
					{
						que.addElement(new ScriptableResponse(affect.source(),monster,monster,(Item)affect.tool(),defaultItem,script,2,null));
						return;
					}
				}
				break;
			case 8: // entry_prog
				if((affect.targetMinor()==Affect.TYP_ENTER)
				&&(affect.amISource(monster))
				&&(canFreelyBehaveNormal(monster)))
				{
					int prcnt=Util.s_int(Util.getCleanBit(trigger,1));
					if(Dice.rollPercentage()<prcnt)
					{
						que.addElement(new ScriptableResponse(affect.source(),monster,monster,defaultItem,null,script,2,null));
						return;
					}
				}
				break;
			case 9: // exit prog
				if((affect.targetMinor()==Affect.TYP_LEAVE)
				&&(affect.amITarget(lastKnownLocation))
				&&(!affect.amISource(monster))
				&&(canFreelyBehaveNormal(monster)))
				{
					int prcnt=Util.s_int(Util.getCleanBit(trigger,1));
					if((Dice.rollPercentage()<prcnt)&&(!affect.source().isMonster()))
					{
						que.addElement(new ScriptableResponse(affect.source(),monster,monster,defaultItem,null,script,2,null));
						return;
					}
				}
				break;
			case 10: // death prog
				if((affect.sourceMinor()==Affect.TYP_DEATH)
				&&(affect.amISource(monster)))
				{
					MOB src=lastToHurtMe;
					if((src==null)||(src.location()!=monster.location()))
					   src=monster;
					execute(src,monster,monster,defaultItem,null,script,null);
					return;
				}
				break;
			case 12: // mask prog
			case 18: // act prog
				if(!affect.amISource(monster))
				{
					boolean doIt=false;
					String msg=affect.othersMessage();
					if(msg==null) msg=affect.targetMessage();
					if(msg==null) msg=affect.sourceMessage();
					if(msg==null) break;
					msg=" "+msg.toUpperCase()+" ";
					trigger=Util.getPastBit(trigger.trim(),0);
					if(Util.getCleanBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim().toUpperCase();
						if(match(msg,trigger))
							doIt=true;
					}
					else
					{
						int num=Util.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=Util.getCleanBit(trigger,i).trim();
							if(msg.indexOf(" "+t+" ")>=0)
							{
								msg=t;
								doIt=true;
								break;
							}
						}
					}
					if(doIt)
					{
						Item Tool=null;
						if(affect.tool() instanceof Item)
							Tool=(Item)affect.tool();
						if(Tool==null) Tool=defaultItem;
						if(affect.target() instanceof MOB)
							que.addElement(new ScriptableResponse(affect.source(),(MOB)affect.target(),monster,Tool,defaultItem,script,2,msg));
						else
						if(affect.target() instanceof Item)
							que.addElement(new ScriptableResponse(affect.source(),null,monster,Tool,(Item)affect.target(),script,2,msg));
						else
							que.addElement(new ScriptableResponse(affect.source(),null,monster,Tool,defaultItem,script,2,msg));
						return;
					}
				}
				break;
			}
		}
	}

	private int getTriggerCode(String trigger)
	{
		int x=trigger.indexOf(" ");
		Integer I=null;
		if(x<0)
			I=(Integer)progH.get(trigger.toUpperCase().trim());
		else
			I=(Integer)progH.get(trigger.substring(0,x).toUpperCase().trim());
		if(I==null) return 0;
		return I.intValue();
	}

	public MOB backupMOB=null;
	public MOB getScriptableMOB(Tickable ticking)
	{
		MOB mob=null;
		if(ticking instanceof MOB)
		{
			mob=(MOB)ticking;
			if(!mob.amDead())
				lastKnownLocation=mob.location();
		}
		else
		if(ticking instanceof Environmental)
		{
			
			if(CoffeeUtensils.roomLocation((Environmental)ticking)!=null)
				lastKnownLocation=CoffeeUtensils.roomLocation((Environmental)ticking);
			
			if(backupMOB==null)
			{
				backupMOB=CMClass.getMOB("StdMOB");
				if(backupMOB!=null)
				{
					backupMOB.setName(ticking.name());
					backupMOB.setDisplayText(ticking.name()+" is here.");
					backupMOB.setDescription("");
					mob=backupMOB;
					if(backupMOB.location()!=lastKnownLocation)
						backupMOB.setLocation(lastKnownLocation);
				}
			}
			else
			{
				mob=backupMOB;
				if(backupMOB.location()!=lastKnownLocation)
					backupMOB.setLocation(lastKnownLocation);
			}
		}
		return mob;
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		
		MOB mob=getScriptableMOB(ticking);
		Item defaultItem=(ticking instanceof Item)?(Item)ticking:null;
		
		if((mob==null)||(lastKnownLocation==null)) 
			return true;
		
		Vector scripts=getScripts();

		for(int v=0;v<scripts.size();v++)
		{
			Vector script=(Vector)scripts.elementAt(v);
			String trigger="";
			if(script.size()>0)
				trigger=((String)script.elementAt(0)).toUpperCase().trim();
			switch(getTriggerCode(trigger))
			{
			case 5: // rand_Prog
				if(!mob.amDead())
				{
					int prcnt=Util.s_int(Util.getCleanBit(trigger,1));
					if(Dice.rollPercentage()<prcnt)
						execute(mob,mob,mob,defaultItem,null,script,null);
				}
				break;
			case 16: // delay_prog
				if(!mob.amDead())
				{
					int targetTick=-1;
					if(delayTargetTimes.containsKey(new Integer(v)))
						targetTick=((Integer)delayTargetTimes.get(new Integer(v))).intValue();
					else
					{
						int low=Util.s_int(Util.getCleanBit(trigger,1));
						int high=Util.s_int(Util.getCleanBit(trigger,2));
						if(high<low) high=low;
						targetTick=Dice.roll(1,high-low+1,low-1);
						delayTargetTimes.put(new Integer(v),new Integer(targetTick));
					}
					int delayProgCounter=0;
					if(delayProgCounters.containsKey(new Integer(v)))
						delayProgCounter=((Integer)delayProgCounters.get(new Integer(v))).intValue();
					else
						delayProgCounters.put(new Integer(v),new Integer(0));
					if(delayProgCounter==targetTick)
					{
						execute(mob,mob,mob,defaultItem,null,script,null);
						delayProgCounter=-1;
					}
					delayProgCounters.remove(new Integer(v));
					delayProgCounters.put(new Integer(v),new Integer(delayProgCounter+1));
				}
				break;
			case 7: // fightProg
				if((mob.isInCombat())&&(!mob.amDead()))
				{
					int prcnt=Util.s_int(Util.getCleanBit(trigger,1));
					if(Dice.rollPercentage()<prcnt)
						execute(mob.getVictim(),mob,mob,defaultItem,null,script,null);
				}
				break;
			case 11: // hitprcnt
				if((mob.isInCombat())&&(!mob.amDead()))
				{
					int floor=(int)Math.round(Util.mul(Util.div(Util.s_int(Util.getCleanBit(trigger,1)),100.0),mob.maxState().getHitPoints()));
					if(mob.curState().getHitPoints()<=floor)
						execute(mob.getVictim(),mob,mob,defaultItem,null,script,null);
				}
				break;
			case 6: // once_prog
				if(!oncesDone.contains(script))
				{
					oncesDone.addElement(script);
					execute(mob,mob,mob,defaultItem,null,script,null);
				}
				break;
			case 14: // time_prog
				if((mob.location()!=null)
				&&(!mob.amDead()))
				{
					int lastTimeProgDone=-1;
					if(lastTimeProgsDone.containsKey(new Integer(v)))
						lastTimeProgDone=((Integer)lastTimeProgsDone.get(new Integer(v))).intValue();
					int time=mob.location().getArea().getTimeOfDay();
					if(lastTimeProgDone!=time)
					{
						boolean done=false;
						for(int i=1;i<Util.numBits(trigger);i++)
						{
							if(time==Util.s_int(Util.getCleanBit(trigger,i).trim()))
							{
								done=true;
								execute(mob,mob,mob,defaultItem,null,script,null);
								lastTimeProgsDone.remove(new Integer(v));
								lastTimeProgsDone.put(new Integer(v),new Integer(time));
								break;
							}
						}
						if(!done)
							lastDayProgsDone.remove(new Integer(v));
					}
					break;
				}
			case 15: // day_prog
				if((mob.location()!=null)
				&&(!mob.amDead()))
				{
					int lastDayProgDone=-1;
					if(lastDayProgsDone.containsKey(new Integer(v)))
						lastDayProgDone=((Integer)lastDayProgsDone.get(new Integer(v))).intValue();
					int day=mob.location().getArea().getDayOfMonth();
					if(lastDayProgDone!=day)
					{
						boolean done=false;
						for(int i=1;i<Util.numBits(trigger);i++)
						{
							if(day==Util.s_int(Util.getCleanBit(trigger,i)))
							{
								done=true;
								execute(mob,mob,mob,defaultItem,null,script,null);
								lastDayProgsDone.remove(new Integer(v));
								lastDayProgsDone.put(new Integer(v),new Integer(day));
								break;
							}
						}
						if(!done)
							lastDayProgsDone.remove(new Integer(v));
					}
					break;
				}
			case 13: // questtimeprog
				if(!oncesDone.contains(script))
				{
					Quest Q=Quests.fetchQuest(Util.getCleanBit(trigger,1));
					if((Q!=null)&&(Q.running()))
					{
						int time=Util.s_int(Util.getCleanBit(trigger,2));
						if(time>=Q.minsRemaining())
						{
							oncesDone.addElement(script);
							execute(mob,mob,mob,defaultItem,null,script,null);
						}
					}
				}
				break;
			}

			try{
				for(int q=que.size()-1;q>=0;q--)
				{
					ScriptableResponse SB=(ScriptableResponse)que.elementAt(q);
					if(SB.tickOrGo()) que.removeElement(SB);
				}
			}catch(Exception e){}
		}
		return true;
	}
}