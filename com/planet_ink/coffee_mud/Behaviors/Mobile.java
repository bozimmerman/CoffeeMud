package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Mobile extends ActiveTicker
{
	public String ID(){return "Mobile";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	public boolean grantsMobility(){return true;}
	protected boolean wander=false;
	protected boolean dooropen=false;
	protected Vector restrictedLocales=null;

	public Mobile()
	{
		super();
		minTicks=10; maxTicks=30; chance=100;
		wander=false;
		dooropen=false;
		restrictedLocales=null;
		tickReset();
	}
	public Behavior newInstance()
	{
		return new Mobile();
	}

	public boolean okRoomForMe(Room currentRoom, Room newRoom)
	{
		if(currentRoom==null) return false;
		if(newRoom==null) return false;
		if((!wander)&&(!currentRoom.getArea().Name().equals(newRoom.getArea().Name())))
		   return false;
		if(restrictedLocales==null) return true;
		return !restrictedLocales.contains(new Integer(newRoom.domainType()));
	}

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		wander=false;
		dooropen=false;
		restrictedLocales=null;
		Vector V=Util.parse(newParms);
		for(int v=0;v<V.size();v++)
		{
			String s=(String)V.elementAt(v);
			if(s.equalsIgnoreCase("WANDER"))
				wander=true;
			else
			if(s.equalsIgnoreCase("OPENDOORS"))
				dooropen=true;
			else
			if((s.startsWith("+")||(s.startsWith("-")))&&(s.length()>1))
			{
				if(restrictedLocales==null)
					restrictedLocales=new Vector();
				if(s.equalsIgnoreCase("+ALL"))
					restrictedLocales.clear();
				else
				if(s.equalsIgnoreCase("-ALL"))
				{
					restrictedLocales.clear();
					for(int i=0;i<Room.indoorDomainDescs.length;i++)
						restrictedLocales.addElement(new Integer(Room.INDOORS+i));
					for(int i=0;i<Room.outdoorDomainDescs.length;i++)
						restrictedLocales.addElement(new Integer(i));
				}
				else
				{
					char c=s.charAt(0);
					s=s.substring(1).toUpperCase().trim();
					int code=-1;
					for(int i=0;i<Room.indoorDomainDescs.length;i++)
						if(Room.indoorDomainDescs[i].startsWith(s))
							code=Room.INDOORS+i;
					if(code>=0)
					{
						if((c=='+')&&(restrictedLocales.contains(new Integer(code))))
							restrictedLocales.removeElement(new Integer(code));
						else
						if((c=='-')&&(!restrictedLocales.contains(new Integer(code))))
							restrictedLocales.addElement(new Integer(code));
					}
					code=-1;
					for(int i=0;i<Room.outdoorDomainDescs.length;i++)
						if(Room.outdoorDomainDescs[i].startsWith(s))
							code=i;
					if(code>=0)
					{
						if((c=='+')&&(restrictedLocales.contains(new Integer(code))))
							restrictedLocales.removeElement(new Integer(code));
						else
						if((c=='-')&&(!restrictedLocales.contains(new Integer(code))))
							restrictedLocales.addElement(new Integer(code));
					}

				}
			}
		}
		if((restrictedLocales!=null)&&(restrictedLocales.size()==0))
			restrictedLocales=null;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))&&(ticking instanceof MOB))
		{
			// ridden things dont wander!
			MOB mob=(MOB)ticking;
			if(((mob instanceof Rideable)&&(((Rideable)mob).numRiders()>0))
			||((mob.amFollowing()!=null)&&(mob.location()==mob.amFollowing().location())))
				return true;

			Room oldRoom=mob.location();
			if(oldRoom instanceof GridLocale)
			{
				Vector V=((GridLocale)oldRoom).getAllRooms();
				Room R=(Room)(V.elementAt(Dice.roll(1,V.size(),-1)));
				if(R!=null) R.bringMobHere(mob,true);
				oldRoom=mob.location();
			}

			int tries=0;
			int direction=-1;
			while((tries++<10)&&(direction<0))
			{
				direction=(int)Math.round(Math.floor(Math.random()*6));
				Room nextRoom=oldRoom.getRoomInDir(direction);
				Exit nextExit=oldRoom.getExitInDir(direction);
				if((nextRoom!=null)&&(nextExit!=null))
				{
					Exit opExit=nextRoom.getExitInDir(Directions.getOpDirectionCode(direction));
					for(int a=0;a<nextExit.numAffects();a++)
					{
						Ability aff=nextExit.fetchAffect(a);
						if((aff!=null)&&(aff instanceof Trap))
							direction=-1;
					}

					if(opExit!=null)
					{
						for(int a=0;a<opExit.numAffects();a++)
						{
							Ability aff=opExit.fetchAffect(a);
							if((aff!=null)&&(aff instanceof Trap))
								direction=-1;
						}
					}

					if((oldRoom.domainConditions()!=nextRoom.domainConditions())
					&&(!Sense.isFlying(mob))
					&&((nextRoom.domainConditions()==Room.DOMAIN_INDOORS_AIR)
					||(nextRoom.domainConditions()==Room.DOMAIN_OUTDOORS_AIR)))
						direction=-1;
					
					if(!okRoomForMe(oldRoom,nextRoom))
						direction=-1;
					else
						break;
				}
				else
					direction=-1;
			}

			if(direction<0)
				return true;

			for(int m=0;m<oldRoom.numInhabitants();m++)
			{
				MOB inhab=oldRoom.fetchInhabitant(m);
				if((inhab!=null)&&(inhab.isASysOp(oldRoom)))
					return true;
			}
			
			Room nextRoom=oldRoom.getRoomInDir(direction);
			Exit nextExit=oldRoom.getExitInDir(direction);
			int opDirection=Directions.getOpDirectionCode(direction);
			if((nextRoom!=null)&&(nextExit!=null))
			{
				boolean reclose=false;
				boolean relock=false;
				// handle doors!
				if(nextExit.hasADoor()&&(!nextExit.isOpen())&&(dooropen))
				{
					if((nextExit.hasALock())&&(nextExit.isLocked()))
					{
						FullMsg msg=new FullMsg(mob,nextExit,null,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,null);
						if(oldRoom.okAffect(mob,msg))
						{
							relock=true;
							msg=new FullMsg(mob,nextExit,null,Affect.MSG_OK_VISUAL,Affect.MSG_UNLOCK,Affect.MSG_OK_VISUAL,"<S-NAME> unlock(s) <T-NAMESELF>.");
							if(oldRoom.okAffect(mob,msg))
								ExternalPlay.roomAffectFully(msg,oldRoom,direction);
						}
					}
					FullMsg msg=new FullMsg(mob,nextExit,null,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,null);
					if(oldRoom.okAffect(mob,msg))
					{
						reclose=true;
						msg=new FullMsg(mob,nextExit,null,Affect.MSG_OK_VISUAL,Affect.MSG_OPEN,Affect.MSG_OK_VISUAL,"<S-NAME> open(s) <T-NAMESELF>.");
						ExternalPlay.roomAffectFully(msg,oldRoom,direction);
					}
				}
				if(!nextExit.isOpen())
					return true;
				else
				{
					int dir=direction;
					Ability A=mob.fetchAbility("Thief_Sneak");
					if(A!=null)
					{
						Vector V=new Vector();
						V.add(Directions.getDirectionName(direction));
						if(A.profficiency()<50)
						{
							A.setProfficiency(Dice.roll(1,50,A.adjustedLevel(mob)*15));
							Ability A2=mob.fetchAbility("Thief_Hide");
							if(A2!=null)
								A2.setProfficiency(Dice.roll(1,50,A.adjustedLevel(mob)*15));
						}
						int oldMana=mob.curState().getMana();
						A.invoke(mob,V,null,false);
						mob.curState().setMana(oldMana);
					}
					else
						ExternalPlay.move(mob,direction,false,false);
					if((reclose)&&(mob.location()==nextRoom)&&(dooropen))
					{
						Exit opExit=nextRoom.getExitInDir(opDirection);
						if((opExit!=null)
						&&(opExit.hasADoor())
						&&(opExit.isOpen()))
						{
							FullMsg msg=new FullMsg(mob,opExit,null,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,null);
							if(nextRoom.okAffect(mob,msg))
							{
								msg=new FullMsg(mob,opExit,null,Affect.MSG_OK_VISUAL,Affect.MSG_CLOSE,Affect.MSG_OK_VISUAL,"<S-NAME> close(s) <T-NAMESELF>.");
								ExternalPlay.roomAffectFully(msg,nextRoom,opDirection);
							}
							if((opExit.hasALock())&&(relock))
							{
								msg=new FullMsg(mob,opExit,null,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,null);
								if(nextRoom.okAffect(mob,msg))
								{
									msg=new FullMsg(mob,opExit,null,Affect.MSG_OK_VISUAL,Affect.MSG_LOCK,Affect.MSG_OK_VISUAL,"<S-NAME> lock(s) <T-NAMESELF>.");
									if(nextRoom.okAffect(mob,msg))
										ExternalPlay.roomAffectFully(msg,nextRoom,opDirection);
								}
							}
						}
					}
				}
			}
			else
				return true;
			

			if(mob.location()==oldRoom)
				tickDown=0;
		}
		return true;
	}
}
