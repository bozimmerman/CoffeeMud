package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Scriptable extends StdBehavior
{
	public String ID(){return "Scriptable";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	private MOB lastToHurtMe=null;
	private Room lastKnownLocation=null;
	private Vector que=new Vector();
	private static final Hashtable funcH=new Hashtable();
	private static final Hashtable methH=new Hashtable();
	private static final Hashtable progH=new Hashtable();
	private Vector oncesDone=new Vector();

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
		"MASK_PROG" //12
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
		"ISINAREA" // 32
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
		"MPQUESTWIN" //22
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
		
		public ScriptableResponse(MOB source, 
								  Environmental target, 
								  MOB monster, 
								  Item primaryItem, 
								  Item secondaryItem, 
								  Vector script,
								  int ticks)
		{
			s=source;
			t=target;
			m=monster;
			pi=primaryItem;
			si=secondaryItem;
			scr=script;
			tickDelay=ticks;
		}
		public boolean tickOrGo()
		{
			if((--tickDelay)<=0)
			{
				execute(s,t,m,pi,si,scr);
				return true;
			}
			return false;
		}
	}

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		oncesDone.clear();
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
		if(text.toUpperCase().startsWith("LOAD="))
		{
			StringBuffer buf=Resources.getFileResource(text.substring(5));
			if(buf!=null) text=buf.toString();
		}
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
					if(s.charAt(yy)==';'){y=yy;break;}
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
					script.addElement(cmd);
				V.setElementAt(script,v);
			}
		}
		return V;
	}

	private Room getRoom(String thisName, Room imHere)
	{
		if(thisName.length()==0) return null;
		Room room=CMMap.getRoom(thisName);
		if(room!=null) return room;
		Room inAreaRoom=null;
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if((CoffeeUtensils.containsString(R.name(),thisName))
			||(R.ID().endsWith("#"+thisName))
			||(R.fetchFromRoomFavorMOBs(null,thisName,Item.WORN_REQ_UNWORNONLY)!=null))
			{
				if((imHere!=null)&&(imHere.getArea().name().equals(R.getArea().name())))
					inAreaRoom=R;
				else
					room=R;
			}
		}
		if(inAreaRoom!=null) return inAreaRoom;
		return room;
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
						if((M instanceof ShopKeeper)&&(E==null))
							E=((ShopKeeper)M).getStock(thisName,null);
					}
				}
			}
			if(E!=null)
			{
				if((imHere!=null)&&(imHere.getArea().name().equals(R.getArea().name())))
					areaThing=E;
				else
					thing=E;
			}
		}
		if(areaThing!=null) return areaThing;
		return thing;
	}

	public Environmental getArgumentItem(String str, MOB source, MOB monster, Environmental target, Item primaryItem, Item secondaryItem)
	{
		if(str.equalsIgnoreCase("$n"))
			return source;
		else
		if(str.equalsIgnoreCase("$i"))
			return monster;
		else
		if(str.equalsIgnoreCase("$t"))
			return target;
		else
		if(str.equalsIgnoreCase("$o"))
			return primaryItem;
		else
		if(str.equalsIgnoreCase("$p"))
			return secondaryItem;
		return null;
	}
	
	public boolean eval(MOB source, Environmental target, MOB monster, Item primaryItem, Item secondaryItem, String evaluable)
	{
		String uevaluable=evaluable.toUpperCase().trim();
		boolean returnable=false;
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
						returnable=eval(source,target,monster,primaryItem,secondaryItem,expr);
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
				return !eval(source,target,monster,primaryItem,secondaryItem,evaluable.substring(1).trim());
			else
			if(uevaluable.startsWith("AND "))
				return returnable&&eval(source,target,monster,primaryItem,secondaryItem,evaluable.substring(3).trim());
			else
			if(uevaluable.startsWith("OR "))
				return returnable||eval(source,target,monster,primaryItem,secondaryItem,evaluable.substring(2).trim());
			else
			if((y<0)||(z<y))
			{
				Log.errOut("Scriptable","() Syntax -- "+monster.name()+", "+evaluable);
				break;
			}
			else
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
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,Util.getCleanBit(evaluable.substring(y+1,z),1));
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem);
				if(arg2.length()==0)
				{
					Log.errOut("Scriptable","HAS Syntax -- "+monster.name()+", "+evaluable);
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
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,Util.getCleanBit(evaluable.substring(y+1,z),1));
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem);
				if(arg2.length()==0)
				{
					Log.errOut("Scriptable","WORN Syntax -- "+monster.name()+", "+evaluable);
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
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=((MOB)E).isMonster();
				break;
			}
			case 5: // ispc
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=!((MOB)E).isMonster();
				break;
			}
			case 6: // isgood
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=CommonStrings.shortAlignmentStr(((MOB)E).getAlignment()).equalsIgnoreCase("good");
				break;
			}
			case 8: // isevil
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=CommonStrings.shortAlignmentStr(((MOB)E).getAlignment()).equalsIgnoreCase("evil");
				break;
			}
			case 9: // isneutral
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=CommonStrings.shortAlignmentStr(((MOB)E).getAlignment()).equalsIgnoreCase("neutral");
				break;
			}
			case 10: // isfight
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=((MOB)E).isInCombat();
				break;
			}
			case 11: // isimmort
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=((MOB)E).isASysOp(((MOB)E).location());
				break;
			}
			case 12: // ischarmed
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					Ability A=((MOB)E).fetchAffect("Spell_Charm");
					if(A==null) A=((MOB)E).fetchAffect("Spell_Enthrall");
					if(A==null) A=((MOB)E).fetchAffect("Chant_CharmAnimal");
					if(A==null) A=((MOB)E).fetchAffect("Spell_SummonMonster");
					if(A==null) A=((MOB)E).fetchAffect("Spell_DemonGate");
					if(A==null) A=((MOB)E).fetchAffect("Spell_SummonEnemy");
					if(A==null) A=((MOB)E).fetchAffect("Spell_SummonSteed");
					if(A==null) A=((MOB)E).fetchAffect("Spell_SummonFlyer");
					if(A==null) A=((MOB)E).fetchAffect("Spell_SummonArmy");
					returnable=(A!=null);
				}
				break;
			}
			case 15: // isfollow
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=!(((MOB)E).amFollowing()==null)||(((MOB)E).amFollowing().location()!=lastKnownLocation);
				break;
			}
			case 7: // isname
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,Util.getCleanBit(evaluable.substring(y+1,z),1));
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem);
				if(E==null)
					returnable=false;
				else
					returnable=CoffeeUtensils.containsString(E.name(),arg2);
				break;
			}
			case 14: // affected
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem);
				if(E==null)
					returnable=false;
				else
					returnable=(E.fetchAffect(arg2)!=null);
				break;
			}
			case 28: // questwinner
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,Util.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				Quest Q=Quests.fetchQuest(arg2);
				if(Q==null)
					returnable=false;
				else
					returnable=Q.wasWinner(arg1);
				break;
			}
			case 29: // questmob
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,Util.getCleanBit(evaluable.substring(y+1,z),0));
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
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,Util.getCleanBit(evaluable.substring(y+1,z),0));
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
			case 32: // isinarea
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,Util.getCleanBit(evaluable.substring(y+1,z),0)).toUpperCase();
				for(Enumeration e=lastKnownLocation.getArea().getMap();e.hasMoreElements();)
				{
					Room R=(Room)e.nextElement();
					if(R.fetchInhabitant(arg1)!=null) return true;
				}
				break;
			}
			case 30: // questitem
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,Util.getCleanBit(evaluable.substring(y+1,z),0));
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
				String arg3=Util.getCleanBit(evaluable.substring(y+1,z),2);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					Log.errOut("Scriptable","HITPRCNT Syntax -- "+monster.name()+", "+evaluable);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					double hitPctD=Util.div(((MOB)E).curState().getHitPoints(),((MOB)E).maxState().getHitPoints());
					int val1=(int)Math.round(hitPctD*100.0);
					int val2=Util.s_int(arg3);
				
					if(arg2.equalsIgnoreCase("=="))
						returnable=(val1==val2);
					else
					if(arg2.equalsIgnoreCase(">="))
						returnable=(val1>=val2);
					else
					if(arg2.equalsIgnoreCase("<="))
						returnable=(val1<=val2);
					else
					if(arg2.equalsIgnoreCase(">"))
						returnable=(val1>val2);
					else
					if(arg2.equalsIgnoreCase("<"))
						returnable=(val1<val2);
					else
					if(arg2.equalsIgnoreCase("!="))
						returnable=(val1!=val2);
					else
					{
						Log.errOut("Scriptable","HITPRCNT Syntax -- "+monster.name()+", "+evaluable);
						return returnable;
					}
				}
				break;
			}
			case 17: // inroom
			{
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				Environmental E=monster;
				Room R=getRoom(arg2,lastKnownLocation);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				if((R==null)&&(arg2.length()==0))
					returnable=true;
				else
				if(R==null)
					returnable=false;
				else
					returnable=((MOB)E).location().ID().equalsIgnoreCase(R.ID());
				break;
			}
			case 18: // sex
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=Util.getCleanBit(evaluable.substring(y+1,z),2).toUpperCase();
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					Log.errOut("Scriptable","SEX Syntax -- "+monster.name()+", "+evaluable);
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
						Log.errOut("Scriptable","SEX Syntax -- "+monster.name()+", "+evaluable);
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
				String arg4=varify(source,target,monster,primaryItem,secondaryItem,Util.getCleanBit(evaluable.substring(y+1,z),3));
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					Log.errOut("Scriptable","STAT Syntax -- "+monster.name()+", "+evaluable);
					break;
				}
				if(E==null)
					returnable=false;
				else
				{
					boolean found=false;
					for(int i=0;i<E.getStatCodes().length;i++)
					{
						if(E.getStatCodes()[i].equalsIgnoreCase(arg2))
						{
							found=true; break;
						}
					}
				
					if(!found)
					{
						Log.errOut("Scriptable","STAT Syntax -- "+monster.name()+", unknown stat: "+arg2+" for "+E.name());
						break;
					}
						
					if(arg3.equals("=="))
						returnable=E.getStat(arg2).equalsIgnoreCase(arg4);
					else
					if(arg3.equals("!="))
						returnable=!E.getStat(arg2).equalsIgnoreCase(arg4);
					else
					{
						Log.errOut("Scriptable","STAT Syntax -- "+monster.name()+", "+evaluable);
						return returnable;
					}
				}
				break;
			}
			case 19: // position
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=Util.getCleanBit(evaluable.substring(y+1,z),2).toUpperCase();
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					Log.errOut("Scriptable","POSITION Syntax -- "+monster.name()+", "+evaluable);
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
						Log.errOut("Scriptable","POSITION Syntax -- "+monster.name()+", "+evaluable);
						return returnable;
					}
				}
				break;
			}
			case 20: // level
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=Util.getCleanBit(evaluable.substring(y+1,z),2);
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					Log.errOut("Scriptable","LEVEL Syntax -- "+monster.name()+", "+evaluable);
					return returnable;
				}
				if(E==null)
					returnable=false;
				else
				{
					int val1=E.envStats().level();
					int val2=Util.s_int(arg3);
				
					if(arg2.equalsIgnoreCase("=="))
						returnable=(val1==val2);
					else
					if(arg2.equalsIgnoreCase(">="))
						returnable=(val1>=val2);
					else
					if(arg2.equalsIgnoreCase("<="))
						returnable=(val1<=val2);
					else
					if(arg2.equalsIgnoreCase(">"))
						returnable=(val1>val2);
					else
					if(arg2.equalsIgnoreCase("<"))
						returnable=(val1<val2);
					else
					if(arg2.equalsIgnoreCase("!="))
						returnable=(val1!=val2);
					else
					{
						Log.errOut("Scriptable","LEVEL Syntax -- "+monster.name()+", "+evaluable);
						return returnable;
					}
				}
				break;
			}
			case 21: // class
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=Util.getCleanBit(evaluable.substring(y+1,z),2).toUpperCase();
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					Log.errOut("Scriptable","CLASS Syntax -- "+monster.name()+", "+evaluable);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					String sex=((MOB)E).charStats().getCurrentClass().name().toUpperCase();
					if(arg2.equals("=="))
						returnable=sex.startsWith(arg3);
					else
					if(arg2.equals("!="))
						returnable=!sex.startsWith(arg3);
					else
					{
						Log.errOut("Scriptable","CLASS Syntax -- "+monster.name()+", "+evaluable);
						return returnable;
					}
				}
				break;
			}
			case 22: // baseclass
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=Util.getCleanBit(evaluable.substring(y+1,z),2).toUpperCase();
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					Log.errOut("Scriptable","CLASS Syntax -- "+monster.name()+", "+evaluable);
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
						Log.errOut("Scriptable","CLASS Syntax -- "+monster.name()+", "+evaluable);
						return returnable;
					}
				}
				break;
			}
			case 23: // race
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=Util.getCleanBit(evaluable.substring(y+1,z),2).toUpperCase();
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					Log.errOut("Scriptable","RACE Syntax -- "+monster.name()+", "+evaluable);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					String sex=((MOB)E).charStats().getMyRace().name().toUpperCase();
					if(arg2.equals("=="))
						returnable=sex.startsWith(arg3);
					else
					if(arg2.equals("!="))
						returnable=!sex.startsWith(arg3);
					else
					{
						Log.errOut("Scriptable","RACE Syntax -- "+monster.name()+", "+evaluable);
						return returnable;
					}
				}
				break;
			}
			case 24: //racecat
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=Util.getCleanBit(evaluable.substring(y+1,z),2).toUpperCase();
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					Log.errOut("Scriptable","RACECAT Syntax -- "+monster.name()+", "+evaluable);
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
						Log.errOut("Scriptable","RACECAT Syntax -- "+monster.name()+", "+evaluable);
						return returnable;
					}
				}
				break;
			}
			case 25: // goldamt
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,Util.getCleanBit(evaluable.substring(y+1,z),2));
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					Log.errOut("Scriptable","GOLDAMT Syntax -- "+monster.name()+", "+evaluable);
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
						Log.errOut("Scriptable","GOLDAMT Syntax -- "+monster.name()+", "+evaluable);
						return returnable;
					}
				
					int val2=Util.s_int(arg3);
				
					if(arg2.equalsIgnoreCase("=="))
						returnable=(val1==val2);
					else
					if(arg2.equalsIgnoreCase(">="))
						returnable=(val1>=val2);
					else
					if(arg2.equalsIgnoreCase("<="))
						returnable=(val1<=val2);
					else
					if(arg2.equalsIgnoreCase(">"))
						returnable=(val1>val2);
					else
					if(arg2.equalsIgnoreCase("<"))
						returnable=(val1<val2);
					else
					if(arg2.equalsIgnoreCase("!="))
						returnable=(val1!=val2);
					else
					{
						Log.errOut("Scriptable","GOLDAMT Syntax -- "+monster.name()+", "+evaluable);
						return returnable;
					}
				}
				break;
			}
			case 26: // objtype
			{
				String arg1=Util.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=Util.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=Util.getCleanBit(evaluable.substring(y+1,z),2).toUpperCase();
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					Log.errOut("Scriptable","OBJTYPE Syntax -- "+monster.name()+", "+evaluable);
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
						Log.errOut("Scriptable","OBJTYPE Syntax -- "+monster.name()+", "+evaluable);
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
				String arg4=varify(source,target,monster,primaryItem,secondaryItem,Util.getCleanBit(evaluable.substring(y+1,z),3));
				Environmental E=getArgumentItem(arg1,source,monster,target,primaryItem,secondaryItem);
				if((arg2.length()==0)||(arg3.length()==0)||(arg4.length()==0))
				{
					Log.errOut("Scriptable","VAR Syntax -- "+monster.name()+", "+evaluable);
					return returnable;
				}
				if(E==null)
					returnable=false;
				else
				{
					Hashtable H=(Hashtable)Resources.getResource("SCRIPTVAR-"+E.name());
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
						Log.errOut("Scriptable","VAR Syntax -- "+monster.name()+", "+evaluable);
						return returnable;
					}
				}
				break;
			}
			default:
				Log.errOut("Scriptable","Unknown CMD -- "+monster.name()+", "+evaluable);
				return returnable;
			}
			if((z>=0)&&(z<=evaluable.length()))
			{
				evaluable=evaluable.substring(z+1).trim();
				uevaluable=uevaluable.substring(z+1).trim();
			}
		}
		return returnable;
	}

	public String varify(MOB source, Environmental target, MOB monster, Item primaryItem, Item secondaryItem, String varifyable)
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
				while((room!=null)&&(monster!=null)&&(room.numInhabitants()>1)&&(room.isInhabitant(monster))&&((randMOB==null)||(randMOB==monster)))
					randMOB=room.fetchInhabitant(Dice.roll(1,room.numInhabitants(),-1));
				if(randMOB!=null)
					middle=randMOB.name();
				break;
			case 'j':
				if(monster!=null)
					middle=monster.charStats().heshe();
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
				while((room!=null)&&(monster!=null)&&(room.numInhabitants()>1)&&(room.isInhabitant(monster))&&((randMOB==null)||(randMOB==monster)))
					randMOB=room.fetchInhabitant(Dice.roll(1,room.numInhabitants(),-1));
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
				while((room!=null)&&(monster!=null)&&(room.numInhabitants()>1)&&(room.isInhabitant(monster))&&((randMOB==null)||(randMOB==monster)))
					randMOB=room.fetchInhabitant(Dice.roll(1,room.numInhabitants(),-1));
				if(randMOB!=null)
					middle=randMOB.charStats().hisher();
				break;
			case 'o':
			case 'O':
				if(primaryItem!=null)
					middle=primaryItem.name();
				break;
			case 'p':
			case 'P':
				if(secondaryItem!=null)
					middle=secondaryItem.name();
				break;
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
							E=getArgumentItem(mid.substring(0,y).trim(),source,monster,target,primaryItem,secondaryItem);
							mid=mid.substring(y+1).trim();
						}
						if(E!=null)
						{
							middle=null;
							Hashtable H=(Hashtable)Resources.getResource("SCRIPTVAR-"+E.name());
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
			case 'a':
			case 'A':
				// unnecessary, since, in coffeemud, this is part of the name
				break;
			}
			varifyable=front+middle+back;
			t=varifyable.indexOf("$");
		}
		return varifyable;
	}

	public void execute(MOB source, Environmental target, MOB monster, Item primaryItem, Item secondaryItem, Vector script)
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
				boolean condition=eval(source,target,monster,primaryItem,secondaryItem,conditionStr);
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
						{
							V.addElement(s);
						}
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
					Log.errOut("Scriptable","IF without ENDIF! for "+monster.name());
					return;
				}
				if(V.size()>1)
				{
					//source.tell("Starting "+conditionStr);
					//for(int v=0;v<V.size();v++)
					//	source.tell("Statement "+((String)V.elementAt(v)));
					execute(source,target,monster,primaryItem,secondaryItem,V);
					//source.tell("Stopping "+conditionStr);
				}
				break;
			}
			case 1: // mpasound
			{
				String echo=varify(source,target,monster,primaryItem,secondaryItem,s.substring(8).trim());
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
				s=varify(source,target,monster,primaryItem,secondaryItem,s.substring(6).trim());
				if(s.equalsIgnoreCase("all"))
				{
					while(monster.inventorySize()>0)
					{
						Item I=monster.fetchInventory(0);
						if(I!=null) I.destroyThis();
					}
				}
				else
				{
					Item I=monster.fetchInventory(s);
					if(I!=null)
						I.destroyThis();
				}
				break;
			}
			case 2: // mpecho
			{
				lastKnownLocation.show(monster,null,Affect.MSG_OK_ACTION,varify(source,target,monster,primaryItem,secondaryItem,s.substring(6).trim()));
				break;
			}
			case 13: // mpunaffect
			{
				String m=varify(source,target,monster,primaryItem,secondaryItem,s.substring(6).trim());
				MOB newTarget=lastKnownLocation.fetchInhabitant(m);
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
				String m=varify(source,target,monster,primaryItem,secondaryItem,s.substring(6).trim());
				MOB newTarget=lastKnownLocation.fetchInhabitant(m);
				if(newTarget!=null)
					ExternalPlay.postDeath(newTarget,monster,null);
				break;
			}
			case 16: // mpset
			{
				String m=varify(source,target,monster,primaryItem,secondaryItem,Util.getCleanBit(s,1));
				String arg2=Util.getCleanBit(s,2);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,Util.getCleanBit(s,2));
				Environmental E=null;
				E=lastKnownLocation.fetchFromRoomFavorMOBs(null,m,Item.WORN_REQ_ANY);
				if(E!=null)
				{
					boolean found=false;
					for(int i=0;i<E.getStatCodes().length;i++)
					{
						if(E.getStatCodes()[i].equalsIgnoreCase(arg2))
						{
							found=true; break;
						}
					}
				
					if(!found)
					{
						Log.errOut("Scriptable","MPSET Syntax -- "+monster.name()+", unknown stat: "+arg2+" for "+E.name());
						break;
					}
					E.setStat(arg2,arg3);
				}
				break;
			}
			case 11: // mpexp
			{
				String m=varify(source,target,monster,primaryItem,secondaryItem,Util.getCleanBit(s,1));
				MOB newTarget=lastKnownLocation.fetchInhabitant(m);
				int t=Util.s_int(Util.getCleanBit(s,2));
				if((t>0)&&(newTarget!=null))
				{
					Hashtable group=newTarget.getGroupMembers(new Hashtable());
					if(!group.contains(newTarget))
						group.put(newTarget,newTarget);
					for(Enumeration e=group.elements();e.hasMoreElements();)
					{
						MOB M=(MOB)e.nextElement();
						if(M.location()==lastKnownLocation)
							M.charStats().getCurrentClass().gainExperience(M,null,M.getLeigeID(),t);
					}
				}
				break;
			}
			case 5: // mpmload
			{
				s=varify(source,target,monster,primaryItem,secondaryItem,s.substring(7).trim());
				MOB m=CMClass.getMOB(s);
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
				break;
			}
			case 6: // mpoload
			{
				s=s.substring(7).trim();
				if(Util.s_int(s)>0)
					monster.setMoney(monster.getMoney()+Util.s_int(s));
				else
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
					}
				}
				break;
			}
			case 7: // mpechoat
			{
				String m=varify(source,target,monster,primaryItem,secondaryItem,Util.getCleanBit(s,1));
				MOB newTarget=lastKnownLocation.fetchInhabitant(m);
				if(newTarget!=null)
				{
					s=s.substring(s.indexOf(Util.getCleanBit(s,1))+Util.getCleanBit(s,1).length()).trim();
					lastKnownLocation.showSource(newTarget,null,Affect.MSG_OK_ACTION,varify(source,target,monster,primaryItem,secondaryItem,s));
				}
				break;
			}
			case 8: // mpechoaround
			{
				String m=varify(source,target,monster,primaryItem,secondaryItem,Util.getCleanBit(s,1));
				MOB newTarget=lastKnownLocation.fetchInhabitant(m);
				if(newTarget!=null)
				{
					s=s.substring(s.indexOf(Util.getCleanBit(s,1))+Util.getCleanBit(s,1).length()).trim();
					lastKnownLocation.showOthers(newTarget,null,Affect.MSG_OK_ACTION,varify(source,target,monster,primaryItem,secondaryItem,s));
				}
				break;
			}
			case 9: // mpcast
			{
				String cast=Util.getCleanBit(s,1);
				String m=varify(source,target,monster,primaryItem,secondaryItem,Util.getCleanBit(s,2));
				Ability A=null;
				if(cast!=null) A=CMClass.findAbility(cast);
				MOB newTarget=lastKnownLocation.fetchInhabitant(m);
				if((newTarget!=null)&&(A!=null))
				{
					A.setProfficiency(100);
					A.invoke(monster,newTarget,false);
				}
				break;
			}
			case 10: // mpkill
			{
				String m=varify(source,target,monster,primaryItem,secondaryItem,Util.getCleanBit(s,1));
				MOB newTarget=lastKnownLocation.fetchInhabitant(m);
				if(newTarget!=null)
					monster.setVictim(newTarget);
				break;
			}
			case 12: // mppurge
			{
				if(lastKnownLocation!=null)
				{
					s=varify(source,target,monster,primaryItem,secondaryItem,s.substring(7).trim());
					Environmental E=lastKnownLocation.fetchFromRoomFavorItems(null,s,Item.WORN_REQ_UNWORNONLY);
					if(E!=null)
					{
						if(E instanceof MOB)
							((MOB)E).destroy();
						else
						if(E instanceof Item)
							((Item)E).destroyThis();
					}
				}
				break;
			}
			case 14: // mpgoto
			{
				s=varify(source,target,monster,primaryItem,secondaryItem,s.substring(6).trim());
				Room goHere=getRoom(s,lastKnownLocation);
				if(goHere!=null)
					goHere.bringMobHere(monster,true);
				break;
			}
			case 15: // mpat
			{
				Room lastPlace=lastKnownLocation;
				String roomName=Util.getCleanBit(s,1);
				if(roomName.length()>0)
				{
					s=s.substring(s.indexOf(Util.getCleanBit(s,1))+Util.getCleanBit(s,1).length()).trim();
					Room goHere=getRoom(roomName,lastKnownLocation);
					if(goHere!=null)
					{
						goHere.bringMobHere(monster,true);
						try
						{
							Vector V=Util.parse(varify(source,target,monster,primaryItem,secondaryItem,s));
							ExternalPlay.doCommand(monster,V);
						}
						catch(Exception e)
						{
							Log.errOut("Scriptable",e);
						}
						lastPlace.bringMobHere(monster,true);
					}
				}
				break;
			}
			case 17: // mptransfer
			{
				String roomName=Util.getCleanBit(s,1);
				if(roomName.length()>0)
				{
					s=s.substring(s.indexOf(Util.getCleanBit(s,1))+Util.getCleanBit(s,1).length()).trim();
					Room goHere=getRoom(roomName,lastKnownLocation);
					if(goHere!=null)
					{
						if(s.equalsIgnoreCase("all"))
						{
							if(lastKnownLocation!=null)
							{
								MOB findOne=null;
								while(true)
								{
									for(int x=0;x<lastKnownLocation.numInhabitants();x++)
									{
										MOB m=lastKnownLocation.fetchInhabitant(x);
										if((m!=null)&&(m!=monster)&&(!m.isMonster()))
										{
											findOne=m;
											break;
										}
									}
									if(findOne==null) break;
									goHere.bringMobHere(findOne,true);
								}
							}
						}
						else
						{
							MOB findOne=lastKnownLocation.fetchInhabitant(s);
							if((findOne!=null)&&(findOne!=monster)&&(!findOne.isMonster()))
								goHere.bringMobHere(findOne,true);
						}
					}

				}
				break;
			}
			case 18: // mpforce
			{
				String m=varify(source,target,monster,primaryItem,secondaryItem,Util.getCleanBit(s,1));
				s=varify(source,target,monster,primaryItem,secondaryItem,s);
				MOB newTarget=lastKnownLocation.fetchInhabitant(m);
				if(newTarget!=null)
				{
					s=s.substring(s.indexOf(m)+m.length()).trim();
					try
					{
						Vector V=Util.parse(s);
						ExternalPlay.doCommand(newTarget,V);
					}
					catch(Exception e)
					{
						Log.errOut("Scriptable",e);
					}
				}
			}
			case 20: // mpsetvar
			{
				String m=varify(source,target,monster,primaryItem,secondaryItem,Util.getCleanBit(s,1));
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,Util.getCleanBit(s,2));
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,Util.getCleanBit(s,3));
				Environmental E=null;
				E=lastKnownLocation.fetchFromRoomFavorMOBs(null,m,Item.WORN_REQ_ANY);
				if((E!=null)&&(arg2.length()>0))
				{
					Hashtable H=(Hashtable)Resources.getResource("SCRIPTVAR-"+E.name());
					if(H==null)
					{
						H=new Hashtable();
						Resources.submitResource("SCRIPTVAR-"+E.name(),H);
					}
					if(arg3.equals("++"))
					{
						String val=(String)H.get(arg2);
						arg3=new Integer(Util.s_int(val)+1).toString();
					}
					else
					if(arg3.equals("--"))
					{
						String val=(String)H.get(arg2);
						arg3=new Integer(Util.s_int(val)-1).toString();
					}
					if(H.containsKey(arg2))
						H.remove(arg2);
					H.put(arg2,arg3);
				}
				break;
			}
			case 21: //MPENDQUEST
			{
				s=varify(source,target,monster,primaryItem,secondaryItem,s.substring(10).trim());
				Quest Q=Quests.fetchQuest(s);
				if(Q!=null) Q.stopQuest();
				break;
			}
			case 22: //MPQUESTWIN
			{
				String whoName=varify(source,target,monster,primaryItem,secondaryItem,Util.getCleanBit(s,1));
				if(whoName.length()>0)
				{
					s=s.substring(s.indexOf(Util.getCleanBit(s,1))+Util.getCleanBit(s,1).length()).trim();
					Quest Q=Quests.fetchQuest(s);
					if(Q!=null) Q.declareWinner(whoName);
				}
				break;
			}
			default:
				if(cmd.length()>0)
				{
					try
					{
						Vector V=Util.parse(varify(source,target,monster,primaryItem,secondaryItem,s));
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
		Vector scripts=(Vector)Resources.getResource("PARSED SCRIPTS: "+getParms());
		if(scripts==null)
		{
			scripts=parseScripts(getParms());
			Resources.submitResource("PARSED SCRIPTS: "+getParms(),scripts);
		}
		return scripts;
	}
	
	public void affect(Environmental affecting, Affect affect)
	{
		super.affect(affecting,affect);
		if(affecting==null) return;
		if(!(affecting instanceof MOB)) return;
		MOB monster=(MOB)affecting;
		if(!monster.amDead())
			lastKnownLocation=monster.location();
		else
			return;
		
		Vector scripts=getScripts();

		if(affect.source()==null) return;

		if(affect.amITarget(monster)
		&&(!affect.amISource(monster))
		&&((affect.targetCode()&Affect.MASK_HURT)>0)
		&&(affect.source()!=monster))
			lastToHurtMe=affect.source();
			
		if(lastKnownLocation!=null)
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
						que.addElement(new ScriptableResponse(affect.source(),monster,monster,null,null,script,2));
				}
				break;
			case 2: // allgreet_prog
				if((affect.targetMinor()==Affect.TYP_ENTER)
				&&(affect.amITarget(lastKnownLocation))
				&&(!affect.amISource(monster))
				&&(canFreelyBehaveNormal(monster)))
				{
					int prcnt=Util.s_int(Util.getCleanBit(trigger,1));
					if((Dice.rollPercentage()<prcnt)&&(!affect.source().isMonster()))
						que.addElement(new ScriptableResponse(affect.source(),monster,monster,null,null,script,2));
				}
				break;
			case 3: // speech_prog
				if((affect.sourceMinor()==Affect.TYP_SPEAK)
				&&(!affect.amISource(monster))
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
						if((" "+trigger+" ").indexOf(msg)>=0)
							que.addElement(new ScriptableResponse(affect.source(),affect.target(),monster,null,null,script,2));
					}
					else
					{
						int num=Util.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=Util.getCleanBit(trigger,i);
							if(msg.indexOf(" "+t+" ")>=0)
							{
								que.addElement(new ScriptableResponse(affect.source(),affect.target(),monster,null,null,script,2));
								break;
							}
						}
					}
				}
				break;
			case 4: // give_prog
				if((affect.targetMinor()==Affect.TYP_GIVE)
				&&(affect.amITarget(monster))
				&&(!affect.amISource(monster))
				&&(affect.tool() instanceof Item)
				&&(canFreelyBehaveNormal(monster)))
				{
					trigger=trigger.substring(9).trim();
					if(Util.getCleanBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim();
						if(((" "+trigger+" ").indexOf(affect.tool().name().toUpperCase())>=0)
						||(trigger.equalsIgnoreCase("all")))
							que.addElement(new ScriptableResponse(affect.source(),monster,monster,(Item)affect.tool(),null,script,2));
					}
					else
					{
						int num=Util.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=Util.getCleanBit(trigger,i);
							if(((" "+affect.tool().name().toUpperCase()+" ").indexOf(" "+t+" ")>=0)
							||(t.equalsIgnoreCase("all")))
							{
								que.addElement(new ScriptableResponse(affect.source(),monster,monster,(Item)affect.tool(),null,script,2));
								break;
							}
						}
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
						que.addElement(new ScriptableResponse(affect.source(),monster,monster,null,null,script,2));
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
						que.addElement(new ScriptableResponse(affect.source(),monster,monster,null,null,script,2));
				}
				break;
			case 10: // death prog
				if((affect.sourceMinor()==Affect.TYP_DEATH)
				&&(affect.amISource(monster)))
				{
					MOB src=lastToHurtMe;
					if((src==null)||(src.location()!=monster.location()))
					   src=monster;
					execute(src,monster,monster,null,null,script);
				}
				break;
			case 12: // mask prog
				if(!affect.amISource(monster))
				{
					boolean doIt=false;
					String msg=affect.othersMessage();
					if(msg==null) msg=affect.targetMessage();
					if(msg==null) msg=affect.sourceMessage();
					if(msg==null) break;
					msg=" "+msg.toUpperCase()+" ";
					trigger=trigger.substring(9).trim();
					if(Util.getCleanBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim();
						if((" "+msg+" ").indexOf(trigger)>=0)
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
						if(affect.target() instanceof MOB)
							que.addElement(new ScriptableResponse(affect.source(),(MOB)affect.target(),monster,Tool,null,script,2));
						else
						if(affect.target() instanceof Item)
							que.addElement(new ScriptableResponse(affect.source(),null,monster,Tool,(Item)affect.target(),script,2));
						else
							que.addElement(new ScriptableResponse(affect.source(),null,monster,Tool,null,script,2));
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
	
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(ticking instanceof MOB)
		{
			MOB mob=(MOB)ticking;
			Vector scripts=getScripts();

			if(!mob.amDead())
				lastKnownLocation=mob.location();

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
							execute(mob,mob,mob,null,null,script);
					}
					break;
				case 7: // fightProg
					if((mob.isInCombat())&&(!mob.amDead()))
					{
						int prcnt=Util.s_int(Util.getCleanBit(trigger,1));
						if(Dice.rollPercentage()<prcnt)
							execute(mob.getVictim(),mob,mob,null,null,script);
					}
					break;
				case 11: // hitprcnt
					if((mob.isInCombat())&&(!mob.amDead()))
					{
						int floor=(int)Math.round(Util.mul(Util.div(Util.s_int(Util.getCleanBit(trigger,1)),100.0),mob.maxState().getHitPoints()));
						if(mob.curState().getHitPoints()<=floor)
							execute(mob.getVictim(),mob,mob,null,null,script);
					}
					break;
				case 6: // onceprog
					if(!oncesDone.contains(script))
					{
						oncesDone.addElement(script);
						execute(mob,mob,mob,null,null,script);
					}
					break;
				}
			}
			
			for(int q=que.size()-1;q>=0;q--)
			{
				ScriptableResponse SB=(ScriptableResponse)que.elementAt(q);
				if(SB.tickOrGo()) que.removeElement(SB);
			}
		}
		return true;
	}
}