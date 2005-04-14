package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

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
public class Patroller extends ActiveTicker
{
	public String ID(){return "Patroller";}
	protected int canImproveCode(){return Behavior.CAN_MOBS|Behavior.CAN_ITEMS;}
	public long flags(){return Behavior.FLAG_MOBILITY;}

	private int step=0;
	private int diameter=20;
	private boolean rideOk=false;
	private boolean rideOnly=false;
	private Vector correction=null;

	private boolean rideFlag=false;
	
	public Patroller()
	{
		super();
		minTicks=5; maxTicks=10; chance=100;
		rideOk=false;
		diameter=20;
		tickReset();
	}
	public void setParms(String newParms)
	{
		super.setParms(newParms);
		String rideokString=Util.getParmStr(newParms,"rideok","false");
		rideOnly=rideokString.equalsIgnoreCase("only");
		rideOk=rideOnly||rideokString.equalsIgnoreCase("true");
		diameter=Util.getParmInt(newParms,"diameter",20);
	}


	private Vector getSteps()
	{
		Vector V=new Vector();
		String path=getParms().trim();
		int x=path.indexOf(";");
		if(x<0) return V;
		path=path.substring(x+1).trim();
		x=path.indexOf(";");
		String s=null;
		while(x>=0)
		{
			s=path.substring(0,x).trim();
			if(s.length()>0) V.addElement(s);
			path=path.substring(x+1).trim();
			x=path.indexOf(";");
		}
		if(path.length()>0)
			V.addElement(path);
		if(V.size()>1)
			for(int i=V.size()-1;i>=0;i--)
			{
				s=(String)V.elementAt(i);
				if(s.equalsIgnoreCase("RESTART")||s.equalsIgnoreCase("REPEAT")) break;
				int dir=Directions.getGoodDirectionCode(s);
				if(dir>=0)
					V.addElement(Directions.getDirectionName(Directions.getOpDirectionCode(dir)));
				else
				if(i<(V.size()-1))
					V.addElement(V.elementAt(i));
			}
		return V;
	}

	public boolean okMessage(Environmental host, CMMsg msg)
	{
	    if((rideOnly)
	    &&(!rideFlag)
	    &&(rideOk)
	    &&(host instanceof Rideable)
	    &&(msg.targetMinor()==CMMsg.TYP_ENTER)
	    &&(msg.source()!=host)
	    &&(msg.source().riding()==host))
	    {
	        if(host instanceof MOB)
		        msg.source().tell("You must dismount before you can do that.");
	        else
		        msg.source().tell("You must disembark before you can do that.");
	        return false;
	    }
	    return super.okMessage(host,msg);
	}
	
	private long tickStatus=Tickable.STATUS_NOT;
	public long getTickStatus(){    return tickStatus;}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
		    return false;
		if(canAct(ticking,tickID))
		{
		    tickStatus=Tickable.STATUS_START;
			Room thisRoom=null;
			if(ticking instanceof MOB) 
			    thisRoom=((MOB)ticking).location();
			else
			if((ticking instanceof Item)
			&&(((Item)ticking).owner() instanceof Room)
			&&(!((Item)ticking).amDestroyed()))
				thisRoom=(Room)((Item)ticking).owner();
			if(thisRoom instanceof GridLocale)
			{
				Room R=((GridLocale)thisRoom).getRandomChild();
				if(R!=null) 
				{
					if(ticking instanceof Item)
						R.bringItemHere((Item)ticking,-1);
					else
					if(ticking instanceof MOB)
						R.bringMobHere((MOB)ticking,true);
				}
				thisRoom=R;
			}
			if(thisRoom==null) 
			{
			    tickStatus=Tickable.STATUS_NOT;
			    return true;
			}
			
		    tickStatus=Tickable.STATUS_MISC+0;
			if(!rideOk)
			{
				if(((ticking instanceof Rideable)&&(((Rideable)ticking).numRiders()>0))
				||((ticking instanceof MOB)&&(((MOB)ticking).amFollowing()!=null)&&(((MOB)ticking).location()==((MOB)ticking).amFollowing().location())))
				{
				    tickStatus=Tickable.STATUS_NOT;
				    return true;
				}
			}
		    tickStatus=Tickable.STATUS_MISC+1;
			
			Room thatRoom=null;
			Vector steps=getSteps();
			if(steps.size()==0)
			{
			    tickStatus=Tickable.STATUS_NOT;
			    return true;
			}
		    tickStatus=Tickable.STATUS_MISC+2;
			if((step<0)||(step>=steps.size())) step=0;
			String nxt=(String)steps.elementAt(step);

			if((nxt.equalsIgnoreCase("RESTART")||nxt.equalsIgnoreCase("REPEAT"))&&(step>0))
			{
				step=0;
				nxt=(String)steps.elementAt(step);
			}

			if(nxt.equalsIgnoreCase("."))
			{
				step++;
			    tickStatus=Tickable.STATUS_NOT;
			    return true;
			}

			
		    tickStatus=Tickable.STATUS_MISC+3;
			int direction=Directions.getGoodDirectionCode(nxt);
			if(direction<0)
			{
				if(CMMap.getExtendedRoomID(thisRoom).toUpperCase().endsWith(nxt.toUpperCase()))
				{
				    correction=null;
				    step++;
				    tickStatus=Tickable.STATUS_NOT;
				    return true;
				}
				        
			    tickStatus=Tickable.STATUS_MISC+4;
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					Room R=thisRoom.getRoomInDir(d);
					if((R!=null)
					&&(CMMap.getExtendedRoomID(R).toUpperCase().endsWith(nxt.toUpperCase())))
					{
					    correction=null;
						thatRoom=R;
						direction=d;
						break;
					}
				}
			}
			else
				thatRoom=thisRoom.getRoomInDir(direction);
			Room destinationRoomForThisStep=thatRoom;

		    tickStatus=Tickable.STATUS_MISC+5;
			if((direction<0)||(thatRoom==null))
			{
			    Room R=CMMap.getRoom(nxt);
			    if(R==null) R=CMMap.getRoom(thisRoom.getArea()+nxt);
			    if(R==null) R=CMMap.getRoom(thisRoom.getArea()+"#"+nxt);
			    if(R!=null)
			    {
			        boolean airOk=(Sense.isFlying((Environmental)ticking)
			            ||((ticking instanceof Rider)&&(((Rider)ticking).riding()!=null)&&(((Rider)ticking).riding().rideBasis()==Rideable.RIDEABLE_AIR))
				        ||((ticking instanceof Rideable)&&(((Rideable)ticking).rideBasis()==Rideable.RIDEABLE_AIR)));
			        boolean waterOk=(Sense.isSwimming((Environmental)ticking)
				            ||((ticking instanceof Rider)&&(((Rider)ticking).riding()!=null)&&(((Rider)ticking).riding().rideBasis()==Rideable.RIDEABLE_WATER))
					        ||((ticking instanceof Rideable)&&(((Rideable)ticking).rideBasis()==Rideable.RIDEABLE_WATER)));
			        
				    tickStatus=Tickable.STATUS_MISC+6;
			        if(R instanceof GridLocale)
			        {
			            boolean GridLocaleFixed=false;
			            if(correction!=null)
			            {
			                for(int r=0;r<correction.size();r++)
			                    if(((GridLocale)R).isMyChild((Room)correction.elementAt(r)))
			                    {
						            GridLocaleFixed=true;
			                        R=(Room)correction.elementAt(r);
			                        break;
			                    }
			            }
			            if(!GridLocaleFixed)
			            {
			                correction=null;
				            R=((GridLocale)R).getRandomChild();
			            }
			        }
				    tickStatus=Tickable.STATUS_MISC+7;
			        destinationRoomForThisStep=R;
			        direction=-1;
			        if(correction!=null)
			        {
			            direction=MUDTracker.trackNextDirectionFromHere(correction,thisRoom,ticking instanceof Item);
			            if(direction<0) 
			                correction=null;
			            else
			                thatRoom=thisRoom.getRoomInDir(direction);
			        }
				    tickStatus=Tickable.STATUS_MISC+8;
					if((direction<0)||(thatRoom==null))
			        {
			            correction=MUDTracker.findBastardTheBestWay(thisRoom,
			                    	Util.makeVector(R),
			                    	ticking instanceof Item,
			                    	false,
			                    	true,
			                    	!airOk,
			                    	!waterOk,
			                    	diameter);
					    tickStatus=Tickable.STATUS_MISC+9;
			            if(correction!=null)
				            direction=MUDTracker.trackNextDirectionFromHere(correction,thisRoom,ticking instanceof Item);
			            else
			                direction=-1;
					    tickStatus=Tickable.STATUS_MISC+10;
			            if(direction>=0)
			                thatRoom=thisRoom.getRoomInDir(direction);
			            else
			                correction=null;
			        }
				    tickStatus=Tickable.STATUS_MISC+11;
					if((direction<0)||(thatRoom==null))
					{
					    step=0;
					    tickStatus=Tickable.STATUS_NOT;
					    return true;
					}
				    tickStatus=Tickable.STATUS_MISC+12;
			    }
			    else
			    {
			        Log.errOut("Patroller","'"+nxt+"' for "+ticking.name()+" is utterly unknown!");
				    tickStatus=Tickable.STATUS_NOT;
				    return true;
			    }
			}
			else
			    correction=null;
		    tickStatus=Tickable.STATUS_MISC+13;
			Exit E=thisRoom.getExitInDir(direction);
			if(E==null)
		    {
			    tickStatus=Tickable.STATUS_NOT;
			    return true;
		    }
			
		    tickStatus=Tickable.STATUS_MISC+14;
			for(int m=0;m<thisRoom.numInhabitants();m++)
			{
				MOB inhab=thisRoom.fetchInhabitant(m);
				if((inhab!=null)
				&&(CMSecurity.isAllowed(inhab,thisRoom,"CMDMOBS")
				   ||CMSecurity.isAllowed(inhab,thisRoom,"CMDROOMS")))
			    {
				    tickStatus=Tickable.STATUS_NOT;
				    return true;
			    }
			}
			
		    tickStatus=Tickable.STATUS_MISC+15;
			if(ticking instanceof Item)
			{
				Item I=(Item)ticking;
				Vector riders=null;
				if(ticking instanceof Rideable)
				{
					riders=new Vector();
					for(int i=0;i<((Rideable)ticking).numRiders();i++)
						riders.addElement(((Rideable)ticking).fetchRider(i));
					Exit opExit=thatRoom.getReverseExit(direction);
					for(int i=0;i<riders.size();i++)
					{
						Rider R=(Rider)riders.elementAt(i);
						if(R instanceof MOB)
						{
						    tickStatus=Tickable.STATUS_MISC+16;
							MOB mob=(MOB)R;
							FullMsg enterMsg=new FullMsg(mob,thatRoom,E,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null);
							FullMsg leaveMsg=new FullMsg(mob,thisRoom,opExit,CMMsg.MSG_LEAVE,null,CMMsg.MSG_LEAVE,null,CMMsg.MSG_LEAVE,null);
							rideFlag=true;
							if((E!=null)&&(!E.okMessage(mob,enterMsg)))
							{	
							    rideFlag=false;	
							    tickStatus=Tickable.STATUS_NOT;
							    return true;
							}
							else
							if((opExit!=null)&&(!opExit.okMessage(mob,leaveMsg)))
							{	
							    rideFlag=false;	
							    tickStatus=Tickable.STATUS_NOT;
							    return true;
							}
							else
							if(!enterMsg.target().okMessage(mob,enterMsg))
							{	
							    rideFlag=false;	
							    tickStatus=Tickable.STATUS_NOT;
							    return true;
							}
							else
							if(!mob.okMessage(mob,enterMsg))
							{	
							    rideFlag=false;	
							    tickStatus=Tickable.STATUS_NOT;
							    return true;
							}
							rideFlag=false;
						}
					}
				}
				
			    tickStatus=Tickable.STATUS_MISC+17;
				thisRoom.showHappens(CMMsg.MSG_OK_ACTION,I,"<S-NAME> goes "+Directions.getDirectionName(direction)+".");
				thatRoom.bringItemHere(I,-1);
				if(I.owner()==thatRoom)
				{
				    tickStatus=Tickable.STATUS_MISC+18;
					thatRoom.showHappens(CMMsg.MSG_OK_ACTION,I,"<S-NAME> arrives from "+Directions.getFromDirectionName(Directions.getOpDirectionCode(direction))+".");
					if(riders!=null)
					for(int i=0;i<riders.size();i++)
					{
						Rider R=(Rider)riders.elementAt(i);
						if(CoffeeUtensils.roomLocation(R)!=thatRoom)
							if((((Rideable)ticking).rideBasis()!=Rideable.RIDEABLE_SIT)
							&&(((Rideable)ticking).rideBasis()!=Rideable.RIDEABLE_TABLE)
							&&(((Rideable)ticking).rideBasis()!=Rideable.RIDEABLE_ENTERIN)
							&&(((Rideable)ticking).rideBasis()!=Rideable.RIDEABLE_SLEEP)
							&&(((Rideable)ticking).rideBasis()!=Rideable.RIDEABLE_LADDER))
							{
								if(R instanceof MOB)
								{
									thatRoom.bringMobHere((MOB)R,true);
									CommonMsgs.look((MOB)R,true);
								}
								else
								if(R instanceof Item)
									thatRoom.bringItemHere((Item)R,-1);
							}
							else
								R.setRiding(null);
					}
				}
				if(I.owner()==destinationRoomForThisStep)
				    step++;
				else
					tickDown=0;
			}
			else
			if(ticking instanceof MOB)
			{
			    tickStatus=Tickable.STATUS_MISC+19;
				// ridden things dont wander!
				MOB mob=(MOB)ticking;

				// handle doors!
				if(E.hasADoor()&&(!E.isOpen()))
				{
					if((E.hasALock())&&(E.isLocked()))
					{
						FullMsg msg=new FullMsg(mob,E,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
						if(mob.location().okMessage(mob,msg))
						{
							msg=new FullMsg(mob,E,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_UNLOCK,CMMsg.MSG_OK_VISUAL,"<S-NAME> unlock(s) <T-NAMESELF>.");
							CoffeeUtensils.roomAffectFully(msg,thisRoom,direction);
						}
					}
					FullMsg msg=new FullMsg(mob,E,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
					if(mob.location().okMessage(mob,msg))
					{
						msg=new FullMsg(mob,E,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OPEN,CMMsg.MSG_OK_VISUAL,"<S-NAME> "+E.openWord()+"(s) <T-NAMESELF>.");
						CoffeeUtensils.roomAffectFully(msg,thisRoom,direction);
					}
				}
				if(!E.isOpen())
				{	
				    tickStatus=Tickable.STATUS_NOT;
				    return true;
				}

			    tickStatus=Tickable.STATUS_MISC+20;
				Ability A=mob.fetchAbility("Thief_Sneak");
				if(A!=null)
				{
					Vector V=new Vector();
					V.add(Directions.getDirectionName(direction));
					if(A.profficiency()<50)
					{
						A.setProfficiency(Dice.roll(1,50,A.adjustedLevel(mob,0)*15));
						Ability A2=mob.fetchAbility("Thief_Hide");
						if(A2!=null)
							A2.setProfficiency(Dice.roll(1,50,A.adjustedLevel(mob,0)*15));
					}
					CharState oldState=mob.curState().cloneCharState();
					A.invoke(mob,V,null,false,0);
					mob.curState().setMana(oldState.getMana());
					mob.curState().setMovement(oldState.getMovement());
				}
				else
				{
					rideFlag=true;
					MUDTracker.move(mob,direction,false,false);
					rideFlag=false;
				}

			    tickStatus=Tickable.STATUS_MISC+21;
				if(mob.location()==destinationRoomForThisStep)
					step++;
				else
					tickDown=0;
			}
		}
	    tickStatus=Tickable.STATUS_NOT;
		return true;
	}
}
