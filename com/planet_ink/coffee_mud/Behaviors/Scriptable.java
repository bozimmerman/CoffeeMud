package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Scriptable extends ActiveTicker
{
	public String ID(){return "Scriptable";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	private Vector scripts=null;
	private boolean wasDeadBefore=false;
	private Room lastKnownLocation=null;

	public Scriptable()
	{
		minTicks=10; maxTicks=30; chance=50;
		tickReset();
	}

	public Behavior newInstance()
	{
		return new Scriptable();
	}

	public void setParms(String newParms)
	{
		scripts=null;
		super.setParms(newParms);
	}


	private Vector parseScripts(String text)
	{
		Vector V=new Vector();
		while(text.length()>0)
		{
			int y=text.indexOf("~");
			if(y<0)
			{
				V.addElement(text);
				text="";
			}
			else
			{
				V.addElement(text.substring(0,y));
				text=text.substring(y+1);
			}
		}
		for(int v=0;v<V.size();v++)
		{
			String s=(String)V.elementAt(v);
			V.removeElementAt(v);
			Vector script=new Vector();
			while(s.length()>0)
			{
				int y=s.indexOf("|");
				String cmd="";
				if(y<0)
				{
					cmd=s.trim();
					if(cmd.length()>0)
						script.addElement(cmd);
					s="";
				}
				else
				{
					cmd=s.substring(0,y).trim();
					script.addElement(cmd);
					s=s.substring(y+1).trim();
				}
				V.insertElementAt(script,v);
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
		for(int m=0;m<CMMap.numRooms();m++)
		{
			Room r=(Room)CMMap.getRoom(m);
			if((CoffeeUtensils.containsString(r.name(),thisName))
			||(r.ID().endsWith("#"+thisName))
			||(r.fetchFromRoomFavorMOBs(null,thisName,Item.WORN_REQ_UNWORNONLY)!=null))
			{
				if((imHere!=null)&&(imHere.getArea().name().equals(r.getArea().name())))
					inAreaRoom=r;
				else
					room=r;
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
		for(int m=0;m<CMMap.numRooms();m++)
		{
			Room r=CMMap.getRoom(m);
			Environmental E=null;
			if(mob)
				E=r.fetchInhabitant(thisName);
			else
				E=r.fetchItem(null,thisName);
			if(E!=null)
			{
				if((imHere!=null)&&(imHere.getArea().name().equals(r.getArea().name())))
					areaThing=E;
				else
					thing=E;
			}
		}
		if(areaThing!=null) return areaThing;
		return thing;
	}

	public boolean eval(MOB source, Environmental target, MOB monster, Item primaryItem, Item secondaryItem, String evaluable)
	{
		evaluable=evaluable.toUpperCase().trim();
		boolean returnable=false;
		while(evaluable.length()>0)
		{
			int y=evaluable.indexOf("(");
			int z=evaluable.indexOf(")");
			String preFab=evaluable.substring(0,y).trim();
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
						returnable=eval(source,target,monster,primaryItem,secondaryItem,expr);
						break;
					}
					else
					if(c=='(') depth++;
					else
					if(c==')') depth--;
				}
			}
			else
			if(evaluable.startsWith("!"))
				return !eval(source,target,monster,primaryItem,secondaryItem,evaluable.substring(1).trim());
			else
			if(evaluable.startsWith("AND "))
				return returnable&&eval(source,target,monster,primaryItem,secondaryItem,evaluable.substring(3).trim());
			else
			if(evaluable.startsWith("OR "))
				return returnable||eval(source,target,monster,primaryItem,secondaryItem,evaluable.substring(2).trim());
			else
			if(preFab.equals("RAND"))
			{
				int arg=Util.s_int(evaluable.substring(y+1,z));
				if(Dice.rollPercentage()<arg)
					returnable=true;
				evaluable=evaluable.substring(z+1).trim();
			}
			else
			if(preFab.equals("HAS"))
			{
			}
			else
			if(preFab.equals("WORN"))
			{
			}
			else
			if(preFab.equals("ISNPC"))
			{
			}
			else
			if(preFab.equals("ISPC"))
			{
			}
			else
			if(preFab.equals("ISGOOD"))
			{
			}
			else
			if(preFab.equals("ISEVIL"))
			{
			}
			else
			if(preFab.equals("ISNEUTRAL"))
			{
			}
			else
			if(preFab.equals("ISFIGHT"))
			{
			}
			else
			if(preFab.equals("ISIMMORT"))
			{
			}
			else
			if(preFab.equals("ISCHARMED"))
			{
			}
			else
			if(preFab.equals("ISFOLLOW"))
			{
			}
			else
			if(preFab.equals("HITPRCNT"))
			{
			}
			else
			if(preFab.equals("INROOM"))
			{
			}
			else
			if(preFab.equals("SEX"))
			{
			}
			else
			if(preFab.equals("ISCHARMED"))
			{
			}
			else
			if(preFab.equals("POSITION"))
			{
			}
			else
			if(preFab.equals("LEVEL"))
			{
			}
			else
			if(preFab.equals("CLASS"))
			{
			}
			else
			if(preFab.equals("RACE"))
			{
			}
			else
			if(preFab.equals("GOLDAMT"))
			{
			}
			else
			if(preFab.equals("ISCHARMED"))
			{
			}
			else
			if(preFab.equals("OBJTYPE"))
			{
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
					middle=randMOB.charStats().hisher()+"s";
				break;
			case 'k':
				if(monster!=null)
					middle=monster.charStats().hisher()+"s";
				break;
			case 'm':
				if(source!=null)
					middle=source.charStats().hisher()+"s";
				break;
			case 'M':
				if((target!=null)&&(target instanceof MOB))
					middle=((MOB)target).charStats().hisher()+"s";
				break;
			case 'K':
				while((room!=null)&&(monster!=null)&&(room.numInhabitants()>1)&&(room.isInhabitant(monster))&&((randMOB==null)||(randMOB==monster)))
					randMOB=room.fetchInhabitant(Dice.roll(1,room.numInhabitants(),-1));
				if(randMOB!=null)
					middle=randMOB.charStats().hisher()+"s";
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
			String cmd=Util.getBit(s,0).toUpperCase();
			if(cmd.length()==0)
				continue;
			else
			if(cmd.equals("IF"))
			{
				boolean condition=eval(source,target,monster,primaryItem,secondaryItem,(cmd.substring(2).trim()));
				Vector V=new Vector();
				V.addElement("");
				int depth=0;
				while((si++)<script.size())
				{
					s=((String)script.elementAt(si)).trim();
					cmd=Util.getBit(s,0).toUpperCase();
					if(cmd.equals("ENDIF")&&(depth==0))
						break;
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
				}
				execute(source,target,monster,primaryItem,secondaryItem,V);
				si++;
			}
			else
			if(cmd.equals("MPASOUND"))
				lastKnownLocation.show(monster,null,Affect.MSG_OK_ACTION,varify(source,target,monster,primaryItem,secondaryItem,s.substring(8).trim()));
			else
			if(cmd.equals("MPJUNK"))
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
			}
			else
			if(cmd.equals("MPECHO"))
				lastKnownLocation.show(monster,null,Affect.MSG_OK_ACTION,varify(source,target,monster,primaryItem,secondaryItem,s.substring(8).trim()));
			else
			if((cmd.equals("MPMLOAD"))&&(lastKnownLocation!=null))
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
					m.bringToLife(lastKnownLocation,true);
				}
			}
			else
			if((cmd.equals("MPOLOAD"))&&(lastKnownLocation!=null))
			{
				s=s.substring(7).trim();
				Item m=CMClass.getItem(s);
				if(m==null)
				{
					Environmental e=findSomethingCalledThis(s,lastKnownLocation,true);
					if(e instanceof Item)
						m=(Item)e;
				}
				if(m!=null)
				{
					m=(Item)m.copyOf();
					monster.addInventory(m);
				}
			}
			else
			if(cmd.equals("MPECHOAT"))
			{
				String m=varify(source,target,monster,primaryItem,secondaryItem,Util.getBit(s,1));
				MOB newTarget=lastKnownLocation.fetchInhabitant(m);
				if(newTarget!=null)
					lastKnownLocation.showSource(newTarget,null,Affect.MSG_OK_ACTION,varify(source,target,monster,primaryItem,secondaryItem,s.substring(8).trim()));
			}
			else
			if(cmd.equals("MPECHOAROUND"))
			{
				String m=varify(source,target,monster,primaryItem,secondaryItem,Util.getBit(s,1));
				MOB newTarget=lastKnownLocation.fetchInhabitant(m);
				if(newTarget!=null)
					lastKnownLocation.showOthers(newTarget,null,Affect.MSG_OK_ACTION,varify(source,target,monster,primaryItem,secondaryItem,s.substring(8).trim()));
			}
			else
			if(cmd.equals("MPCAST"))
			{
			}
			else
			if(cmd.equals("MPKILL"))
			{
				String m=varify(source,target,monster,primaryItem,secondaryItem,Util.getBit(s,1));
				MOB newTarget=lastKnownLocation.fetchInhabitant(m);
				if(newTarget!=null)
					monster.setVictim(newTarget);
			}
			else
			if(cmd.equals("MPPURGE"))
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
			}
			else
			if(cmd.equals("MPGOTO"))
			{
				s=varify(source,target,monster,primaryItem,secondaryItem,s.substring(6).trim());
				Room goHere=getRoom(s,lastKnownLocation);
				if(goHere!=null)
					goHere.bringMobHere(monster,true);
			}
			else
			if(cmd.equals("MPAT"))
			{
				Room lastPlace=lastKnownLocation;
				String roomName=Util.getBit(s,1);
				if(roomName.length()>0)
				{
					s=s.substring(s.indexOf(roomName)+roomName.length()).trim();
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
			}
			else
			if(cmd.equals("MPTRANSFER"))
			{
				String roomName=Util.getBit(s,1);
				if(roomName.length()>0)
				{
					s=s.substring(s.indexOf(roomName)+roomName.length()).trim();
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
			}
			else
			if(cmd.equals("MPFORCE"))
			{
				String m=varify(source,target,monster,primaryItem,secondaryItem,Util.getBit(s,1));
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
			else
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
		}
	}

	public void affect(Environmental affecting, Affect affect)
	{
		super.affect(affecting,affect);
		if(affecting==null) return;
		if(!(affecting instanceof MOB)) return;
		MOB monster=(MOB)affecting;
		if(!monster.amDead())
		{
			if(wasDeadBefore)
				wasDeadBefore=false;
			lastKnownLocation=monster.location();
		}
		else
			return;

		if(!canFreelyBehaveNormal(monster)) return;
		if(scripts==null)
			scripts=parseScripts(getParms());

		if(affect.source()==null) return;
		if(affect.source()==monster) return;

		for(int v=0;v<scripts.size();v++)
		{
			Vector script=(Vector)scripts.elementAt(v);
			String trigger="";
			if(script.size()>0)
				trigger=((String)script.elementAt(0)).toUpperCase().trim();
			if((lastKnownLocation!=null)
			&&(affect.amITarget(lastKnownLocation))
			&&(affect.targetMinor()==Affect.TYP_ENTER))
			{
				if((trigger.startsWith("GREET_PROG"))
				&&(Sense.canBeSeenBy(affect.source(),monster)))
				{
					int prcnt=Util.s_int(Util.getBit(trigger,1));
					if((Dice.rollPercentage()<prcnt)&&(!affect.source().isMonster()))
						execute(affect.source(),monster,monster,null,null,script);
				}
				else
				if((trigger.startsWith("ALL_GREET_PROG")))
				{
					int prcnt=Util.s_int(Util.getBit(trigger,1));
					if((Dice.rollPercentage()<prcnt)&&(!affect.source().isMonster()))
						execute(affect.source(),monster,monster,null,null,script);
				}

			}

			switch(affect.targetMinor())
			{
			case Affect.TYP_SPEAK:
				if(trigger.startsWith("SPEECH_PROG"))
				{
					String msg=affect.othersMessage().toUpperCase();
					if(msg.indexOf("\'")>=0)
					{
						msg=msg.substring(msg.indexOf("\'")+1);
						if(msg.indexOf("\'")>=0)
							msg=msg.substring(0,msg.indexOf("\'"));
					}
					msg=" "+msg+" ";
					trigger=trigger.substring(11).trim();
					if(Util.getBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim();
						if((" "+trigger+" ").indexOf(msg)>=0)
							execute(affect.source(),affect.target(),monster,null,null,script);
					}
					else
					{
						int num=Util.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=Util.getBit(trigger,i);
							if(msg.indexOf(" "+t+" ")>=0)
							{
								execute(affect.source(),affect.target(),monster,null,null,script);
								break;
							}
						}
					}
				}
				break;
			case Affect.TYP_GIVE:
				if(affect.amITarget(monster))
				if((trigger.startsWith("GIVE_PROG"))&&(affect.tool() instanceof Item))
					execute(affect.source(),monster,monster,(Item)affect.tool(),null,script);
				break;
			default:
				if(trigger.startsWith("ACT_PROG"))
				{
					boolean doIt=false;
					String msg=affect.othersMessage().toUpperCase();
					msg=" "+msg+" ";
					trigger=trigger.substring(8).trim();
					if(Util.getBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim();
						if((" "+trigger+" ").indexOf(msg)>=0)
							doIt=true;
					}
					else
					{
						int num=Util.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=Util.getBit(trigger,i);
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
							execute(affect.source(),(MOB)affect.target(),monster,Tool,null,script);
						else
						if(affect.target() instanceof Item)
							execute(affect.source(),null,monster,Tool,(Item)affect.target(),script);
					}
				}
				break;
			}
		}
	}

	private boolean nonPCAvailable(Room room)
	{
		if(room==null) return false;
		for(int m=0;m<room.numInhabitants();m++)
		{
			MOB inhab=room.fetchInhabitant(m);
			if((inhab!=null)&&(!inhab.isMonster()))
				return true;
		}
		return false;
	}

	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))&&(ticking instanceof MOB))
		{
			MOB mob=(MOB)ticking;
			if(scripts==null)
				scripts=parseScripts(getParms());

			if(!mob.amDead())
			{
				if(wasDeadBefore)
					wasDeadBefore=false;
				lastKnownLocation=mob.location();
			}

			for(int v=0;v<scripts.size();v++)
			{
				Vector script=(Vector)scripts.elementAt(v);
				String trigger="";
				if(script.size()>0)
					trigger=((String)script.elementAt(0)).toUpperCase().trim();
				if((trigger.startsWith("RAND_PROG"))&&(!mob.amDead()))
				{
					int prcnt=Util.s_int(Util.getBit(trigger,1));
					if((Dice.rollPercentage()<prcnt)&&(nonPCAvailable(lastKnownLocation)))
						execute(mob,mob,mob,null,null,script);
				}
				else
				if(trigger.startsWith("FIGHT_PROG")&&(mob.isInCombat())&&(!mob.amDead()))
				{
					int prcnt=Util.s_int(Util.getBit(trigger,1));
					if(Dice.rollPercentage()<prcnt)
						execute(mob.getVictim(),mob,mob,null,null,script);
				}
				else
				if(trigger.startsWith("HITPRCNT_PROG")&&(mob.isInCombat())&&(!mob.amDead()))
				{
					int floor=(int)Math.round(Util.mul(Util.div(Util.s_int(Util.getBit(trigger,1)),100.0),mob.maxState().getHitPoints()));
					if(mob.curState().getHitPoints()<=floor)
						execute(mob.getVictim(),mob,mob,null,null,script);
				}
				else
				if((trigger.startsWith("DEATH_PROG"))&&(mob.amDead())&&(!wasDeadBefore))
				{
					wasDeadBefore=true;
					execute(mob,mob,mob,null,null,script);
				}
			}
		}
	}
}