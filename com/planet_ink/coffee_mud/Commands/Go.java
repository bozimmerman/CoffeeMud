package com.planet_ink.coffee_mud.Commands;
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
public class Go extends StdCommand
{
	public Go(){}

	private String[] access={"GO","WALK","RUN"};
	public String[] getAccessWords(){return access;}

	public static void ridersBehind(Vector riders,
									Room sourceRoom,
									Room destRoom,
									int directionCode,
									boolean flee)
	{
		if(riders!=null)
		for(int r=0;r<riders.size();r++)
		{
			Rider rider=(Rider)riders.elementAt(r);
			if(rider instanceof MOB)
			{
				MOB rMOB=(MOB)rider;

				if((rMOB.location()==sourceRoom)
				   ||(rMOB.location()==destRoom))
				{
					boolean fallOff=false;
					if(rMOB.location()==sourceRoom)
					{
						if(rMOB.riding()!=null)
							rMOB.tell(getScr("Movement","youride",rMOB.riding().name(),Directions.getDirectionName(directionCode)));
						if(!move(rMOB,directionCode,flee,false,true,false))
							fallOff=true;
					}
					if(fallOff)
					{
						if(rMOB.riding()!=null)
							rMOB.tell(getScr("Movement","youfalloff",rMOB.riding().name()));
						rMOB.setRiding(null);
					}
				}
				else
					rMOB.setRiding(null);
			}
			else
			if(rider instanceof Item)
			{
				Item rItem=(Item)rider;
				if((rItem.owner()==sourceRoom)
				||(rItem.owner()==destRoom))
					destRoom.bringItemHere(rItem,-1);
				else
					rItem.setRiding(null);
			}
		}
	}

	public static Vector addRiders(Rider theRider,
								   Rideable riding,
								   Vector riders)
	{

		if((riding!=null)&&(riding.mobileRideBasis()))
			for(int r=0;r<riding.numRiders();r++)
			{
				Rider rider=riding.fetchRider(r);
				if((rider!=null)
				&&(rider!=theRider)
				&&(!riders.contains(rider)))
				{
					riders.addElement(rider);
					if(rider instanceof Rideable)
						addRiders(theRider,(Rideable)rider,riders);
				}
			}
		return riders;
	}

	public static Vector ridersAhead(Rider theRider,
									 Room sourceRoom,
									 Room destRoom,
									 int directionCode,
									 boolean flee)
	{
		Vector riders=new Vector();
		Rideable riding=theRider.riding();
		Vector rideables=new Vector();
		while((riding!=null)&&(riding.mobileRideBasis()))
		{
			rideables.addElement(riding);
			addRiders(theRider,riding,riders);
			if((riding instanceof Rider)&&((Rider)riding).riding()!=theRider.riding())
				riding=((Rider)riding).riding();
			else
				riding=null;
		}
		if(theRider instanceof Rideable)
			addRiders(theRider,(Rideable)theRider,riders);
		for(int r=riders.size()-1;r>=0;r--)
		{
			Rider R=(Rider)riders.elementAt(r);
			if((R instanceof Rideable)&&(((Rideable)R).numRiders()>0))
			{
				if(!rideables.contains(R))
					rideables.addElement(R);
				riders.removeElement(R);
			}
		}
		for(int r=0;r<rideables.size();r++)
		{
			riding=(Rideable)rideables.elementAt(r);
			if((riding instanceof Item)
			&&((sourceRoom).isContent((Item)riding)))
				destRoom.bringItemHere((Item)riding,-1);
			else
			if((riding instanceof MOB)
			&&((sourceRoom).isInhabitant((MOB)riding)))
			{
				((MOB)riding).tell(getScr("Movement","youridden",Directions.getDirectionName(directionCode)));
				if(!move(((MOB)riding),directionCode,false,false,true,false))
				{
					if(theRider instanceof MOB)
						((MOB)theRider).tell(getScr("Movement","rideerr1",((MOB)riding).name()));
					r=r-1;
					for(;r>=0;r--)
					{
						riding=(Rideable)rideables.elementAt(r);
						if((riding instanceof Item)
						&&((destRoom).isContent((Item)riding)))
							sourceRoom.bringItemHere((Item)riding,-1);
						else
						if((riding instanceof MOB)
						&&(((MOB)riding).isMonster())
						&&((destRoom).isInhabitant((MOB)riding)))
							sourceRoom.bringMobHere((MOB)riding,true);
					}
					return null;
				}
			}
		}
		return riders;
	}

	public static boolean move(MOB mob,
						   int directionCode,
						   boolean flee,
						   boolean nolook,
						   boolean noriders)
	{
	    return move(mob,directionCode,flee,nolook,noriders,false);
	}
	public static boolean move(MOB mob,
							   int directionCode,
							   boolean flee,
							   boolean nolook,
							   boolean noriders,
							   boolean always)
	{
		if(directionCode<0) return false;
		if(mob==null) return false;
		Room thisRoom=mob.location();
		if(thisRoom==null) return false;
		Room destRoom=thisRoom.getRoomInDir(directionCode);
		Exit exit=thisRoom.getExitInDir(directionCode);
		if(destRoom==null)
		{
			mob.tell(getScr("Movement","moveerr1"));
			return false;
		}

		Exit opExit=thisRoom.getReverseExit(directionCode);
		String directionName=Directions.getDirectionName(directionCode);
		String otherDirectionName=Directions.getFromDirectionName(Directions.getOpDirectionCode(directionCode));

		int generalMask=always?CMMsg.MASK_GENERAL:0;
		int leaveCode=generalMask|CMMsg.MSG_LEAVE;
		if(flee)
			leaveCode=generalMask|CMMsg.MSG_FLEE;

		FullMsg enterMsg=null;
		FullMsg leaveMsg=null;
		if((mob.riding()!=null)&&(mob.riding().mobileRideBasis()))
		{
			enterMsg=new FullMsg(mob,destRoom,exit,generalMask|CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,getScr("Movement","sridesin",mob.riding().name(),otherDirectionName));
			leaveMsg=new FullMsg(mob,thisRoom,opExit,leaveCode,((flee)?getScr("Movement","youflee",directionName):null),leaveCode,null,leaveCode,((flee)?getScr("Movement","sfleeswith",mob.riding().name(),directionName):getScr("Movement","srides",mob.riding().name(),directionName)));
		}
		else
		{
			enterMsg=new FullMsg(mob,destRoom,exit,generalMask|CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,getScr("Movement","senter",Sense.dispositionString(mob,Sense.flag_arrives),otherDirectionName));
			leaveMsg=new FullMsg(mob,thisRoom,opExit,leaveCode,((flee)?getScr("Movement","youflee",directionName):null),leaveCode,null,leaveCode,((flee)?getScr("Movement","sflees",directionName):getScr("Movement","sleaves",Sense.dispositionString(mob,Sense.flag_leaves),directionName)));
		}
		boolean gotoAllowed=CMSecurity.isAllowed(mob,destRoom,"GOTO");
		if((exit==null)&&(!gotoAllowed))
		{
			mob.tell(getScr("Movement","moveerr1"));
			return false;
		}
		else
		if(exit==null)
			thisRoom.showHappens(CMMsg.MSG_OK_VISUAL,getScr("Movement","stwitch",directionName));
		else
		if((exit!=null)&&(!exit.okMessage(mob,enterMsg))&&(!gotoAllowed))
			return false;
		else
		if(!leaveMsg.target().okMessage(mob,leaveMsg)&&(!gotoAllowed))
			return false;
		else
		if((opExit!=null)&&(!opExit.okMessage(mob,leaveMsg))&&(!gotoAllowed))
			return false;
		else
		if(!enterMsg.target().okMessage(mob,enterMsg)&&(!gotoAllowed))
			return false;
		else
		if(!mob.okMessage(mob,enterMsg)&&(!gotoAllowed))
			return false;

		if(mob.riding()!=null)
		{
			if((!mob.riding().okMessage(mob,enterMsg))&&(!gotoAllowed))
				return false;
		}
		else
		{
			if(!mob.isMonster())
				mob.curState().expendEnergy(mob,mob.maxState(),true);
			if((!flee)&&(!mob.curState().adjMovement(-1,mob.maxState()))&&(!gotoAllowed))
			{
				mob.tell(getScr("Movement","tootired"));
				return false;
			}
			if((mob.soulMate()==null)&&(mob.playerStats()!=null)&&(mob.riding()==null)&&(mob.location()!=null))
			    mob.playerStats().adjHygiene(mob.location().pointsPerMove(mob));
			long minMoveTime=CommonStrings.getIntVar(CommonStrings.SYSTEMI_MINMOVETIME);
			if((minMoveTime>0)&&(!flee))
			{
				minMoveTime-=Math.round((mob.envStats().speed()-1.0)*100.0);
				minMoveTime-=Math.round(mob.maxState().getMovement());
				if((minMoveTime>0)
				&&((System.currentTimeMillis()-mob.lastMovedDateTime())<minMoveTime))
				{
					try{
						Thread.sleep(minMoveTime-(System.currentTimeMillis()-mob.lastMovedDateTime()));
					}catch(Exception e){}
				}
			}
		}

		Vector riders=null;
		if(!noriders)
		{
			riders=ridersAhead(mob,(Room)leaveMsg.target(),(Room)enterMsg.target(),directionCode,flee);
			if(riders==null) return false;
		}

		if(exit!=null) exit.executeMsg(mob,enterMsg);
		mob.location().delInhabitant(mob);
		((Room)leaveMsg.target()).send(mob,leaveMsg);

		if(enterMsg.target()==null)
		{
		    ((Room)leaveMsg.target()).bringMobHere(mob,false);
			mob.tell(getScr("Movement","moveerr1"));
			return false;
		}
		mob.setLocation((Room)enterMsg.target());
		((Room)enterMsg.target()).addInhabitant(mob);
		((Room)enterMsg.target()).send(mob,enterMsg);

		if(opExit!=null) opExit.executeMsg(mob,leaveMsg);

		if(!nolook)
        {
			CommonMsgs.look(mob,true);
            if((!mob.isMonster())
            &&(Util.bset(mob.getBitmap(),MOB.ATT_AUTOWEATHER))
            &&(thisRoom!=null)
            &&(((Room)enterMsg.target())!=null)
            &&((thisRoom.domainType()&Room.INDOORS)>0)
            &&((((Room)enterMsg.target()).domainType()&Room.INDOORS)==0)
            &&(((Room)enterMsg.target()).getArea().getClimateObj().weatherType(((Room)enterMsg.target()))!=Climate.WEATHER_CLEAR)
            &&(((Room)enterMsg.target()).isInhabitant(mob)))
                mob.tell("/n/r"+((Room)enterMsg.target()).getArea().getClimateObj().weatherDescription(((Room)enterMsg.target())));
        }

		if(!noriders)
			ridersBehind(riders,(Room)leaveMsg.target(),(Room)enterMsg.target(),directionCode,flee);

		if(!flee)
		for(int f=0;f<mob.numFollowers();f++)
		{
			MOB follower=mob.fetchFollower(f);
			if(follower!=null)
			{
				if((follower.amFollowing()==mob)
				&&((follower.location()==thisRoom)||(follower.location()==destRoom)))
				{
					if((follower.location()==thisRoom)
					&&(Sense.aliveAwakeMobile(follower,true))
					&&(!Util.bset(follower.getBitmap(),MOB.ATT_AUTOGUARD)))
					{
						follower.tell(getScr("Movement","youfollow",mob.name(),Directions.getDirectionName(directionCode)));
						if(!move(follower,directionCode,false,false,false,false))
						{
							//follower.setFollowing(null);
						}
					}
				}
				//else
				//	follower.setFollowing(null);
			}
		}
		return true;
	}

	private Command stander=null;
	private Vector ifneccvec=null;
	public void standIfNecessary(MOB mob)
		throws java.io.IOException
	{
		if((ifneccvec==null)||(ifneccvec.size()!=2))
		{
			ifneccvec=new Vector();
			ifneccvec.addElement("STAND");
			ifneccvec.addElement("IFNECESSARY");
		}
		if(stander==null) stander=CMClass.getCommand("Stand");
		if((stander!=null)&&(ifneccvec!=null))
			stander.execute(mob,ifneccvec);
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		standIfNecessary(mob);
		if((commands.size()>3)
		&&(commands.firstElement() instanceof Integer))
		{
			return move(mob,
						((Integer)commands.elementAt(0)).intValue(),
						((Boolean)commands.elementAt(1)).booleanValue(),
						((Boolean)commands.elementAt(2)).booleanValue(),
						((Boolean)commands.elementAt(3)).booleanValue(),false);

		}

		int direction=Directions.getGoodDirectionCode(Util.combine(commands,1));
		if((direction<0)&&(mob.location().fetchFromRoomFavorItems(null,Util.combine(commands,1),Item.WORN_REQ_UNWORNONLY) instanceof Rideable))
		{
			Command C=CMClass.getCommand("Enter");
			return C.execute(mob,commands);
		}
		String doing=(String)commands.elementAt(0);
		if(direction>=0)
			move(mob,direction,false,false,false,false);
		else
		{
			boolean doneAnything=false;
			if(commands.size()>2)
				for(int v=1;v<commands.size();v++)
				{
					int num=1;
					String s=(String)commands.elementAt(v);
					if(Util.s_int(s)>0)
					{
						num=Util.s_int(s);
						v++;
						if(v<commands.size())
							s=(String)commands.elementAt(v);
					}
					else
					if(("NSEWUDnsewud".indexOf(s.charAt(s.length()-1))>=0)
					&&(Util.s_int(s.substring(0,s.length()-1))>0))
					{
						num=Util.s_int(s.substring(0,s.length()-1));
						s=s.substring(s.length()-1);
					}

					direction=Directions.getGoodDirectionCode(s);
					if(direction>=0)
					{
						doneAnything=true;
						for(int i=0;i<num;i++)
						{
							if(mob.isMonster())
							{
								if(!move(mob,direction,false,false,false,false))
									return false;
							}
							else
							{
								Vector V=new Vector();
								V.addElement(Directions.getDirectionName(direction));
								mob.enqueCommand(V,0);
							}
						}
					}
					else
						break;
				}
			if(!doneAnything)
				mob.tell(Util.capitalize(doing)+" "+getScr("Movement","goerr"));
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
