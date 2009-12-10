package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Go extends StdCommand
{
	public Go(){}

	private String[] access={"GO","WALK"};
	public String[] getAccessWords(){return access;}
	
	public int energyExpenseFactor(){return 1;}

	public void ridersBehind(Vector riders,
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
							rMOB.tell("You ride "+rMOB.riding().name()+" "+Directions.getDirectionName(directionCode)+".");
						if(!move(rMOB,directionCode,flee,false,true,false))
							fallOff=true;
					}
					if(fallOff)
					{
						if(rMOB.riding()!=null)
							rMOB.tell("You fall off "+rMOB.riding().name()+"!");
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
					destRoom.bringItemHere(rItem,-1,false);
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

	public Vector ridersAhead(Rider theRider,
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
				destRoom.bringItemHere((Item)riding,-1,false);
			else
			if((riding instanceof MOB)
			&&((sourceRoom).isInhabitant((MOB)riding)))
			{
				((MOB)riding).tell("You are ridden "+Directions.getDirectionName(directionCode)+".");
				if(!move(((MOB)riding),directionCode,false,false,true,false))
				{
					if(theRider instanceof MOB)
						((MOB)theRider).tell(((MOB)riding).name()+" won't seem to let you go that way.");
					r=r-1;
					for(;r>=0;r--)
					{
						riding=(Rideable)rideables.elementAt(r);
						if((riding instanceof Item)
						&&((destRoom).isContent((Item)riding)))
							sourceRoom.bringItemHere((Item)riding,-1,false);
						else
						if((riding instanceof MOB)
						&&(((MOB)riding).isMonster())
						&&((destRoom).isInhabitant((MOB)riding)))
							sourceRoom.bringMobHere((MOB)riding,false);
					}
					return null;
				}
			}
		}
		return riders;
	}

	public boolean move(MOB mob,
					   int directionCode,
					   boolean flee,
					   boolean nolook,
					   boolean noriders)
	{
	    return move(mob,directionCode,flee,nolook,noriders,false);
	}
	public boolean move(MOB mob,
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
			mob.tell("You can't go that way.");
			return false;
		}

		Exit opExit=thisRoom.getReverseExit(directionCode);
		String directionName=(directionCode==Directions.GATE)&&(exit!=null)?"through "+exit.name():Directions.getDirectionName(directionCode);
		String otherDirectionName=(Directions.getOpDirectionCode(directionCode)==Directions.GATE)&&(exit!=null)?exit.name():Directions.getFromDirectionName(Directions.getOpDirectionCode(directionCode));

		int generalMask=always?CMMsg.MASK_ALWAYS:0;
		int leaveCode=generalMask|CMMsg.MSG_LEAVE;
		if(flee)
			leaveCode=generalMask|CMMsg.MSG_FLEE;

		CMMsg enterMsg=null;
		CMMsg leaveMsg=null;
		if((mob.riding()!=null)&&(mob.riding().mobileRideBasis()))
		{
			enterMsg=CMClass.getMsg(mob,destRoom,exit,generalMask|CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,"<S-NAME> ride(s) "+mob.riding().name()+" in from "+otherDirectionName+".");
			leaveMsg=CMClass.getMsg(mob,thisRoom,opExit,leaveCode,((flee)?"You flee "+directionName+".":null),leaveCode,null,leaveCode,((flee)?"<S-NAME> flee(s) with "+mob.riding().name()+" "+directionName+".":"<S-NAME> ride(s) "+mob.riding().name()+" "+directionName+"."));
		}
		else
		{
			enterMsg=CMClass.getMsg(mob,destRoom,exit,generalMask|CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,"<S-NAME> "+CMLib.flags().dispositionString(mob,CMFlagLibrary.flag_arrives)+" from "+otherDirectionName+".");
			leaveMsg=CMClass.getMsg(mob,thisRoom,opExit,leaveCode,((flee)?"You flee "+directionName+".":null),leaveCode,null,leaveCode,((flee)?"<S-NAME> flee(s) "+directionName+".":"<S-NAME> "+CMLib.flags().dispositionString(mob,CMFlagLibrary.flag_leaves)+" "+directionName+"."));
		}
		boolean gotoAllowed=CMSecurity.isAllowed(mob,destRoom,"GOTO");
		if((exit==null)&&(!gotoAllowed))
		{
			mob.tell("You can't go that way.");
			return false;
		}
		else
		if(exit==null)
			thisRoom.showHappens(CMMsg.MSG_OK_VISUAL,"The area to the "+directionName+" shimmers and becomes transparent.");
		else
		if((!exit.okMessage(mob,enterMsg))&&(!gotoAllowed))
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
				for(int i=0;i<energyExpenseFactor();i++)
					mob.curState().expendEnergy(mob,mob.maxState(),true);
			if((!flee)&&(!mob.curState().adjMovement(-1,mob.maxState()))&&(!gotoAllowed))
			{
				mob.tell("You are too tired.");
				return false;
			}
			if((mob.soulMate()==null)&&(mob.playerStats()!=null)&&(mob.riding()==null)&&(mob.location()!=null))
			    mob.playerStats().adjHygiene(mob.location().pointsPerMove(mob));
		}

		Vector riders=null;
		if(!noriders)
		{
			riders=ridersAhead(mob,(Room)leaveMsg.target(),(Room)enterMsg.target(),directionCode,flee);
			if(riders==null) return false;
		}
        Vector enterTrailersSoFar=null;
		Vector leaveTrailersSoFar=null;
        if((leaveMsg.trailerMsgs()!=null)&&(leaveMsg.trailerMsgs().size()>0))
        {
            leaveTrailersSoFar=new Vector();
            leaveTrailersSoFar.addAll(leaveMsg.trailerMsgs());
            leaveMsg.trailerMsgs().clear();
        }
        if((enterMsg.trailerMsgs()!=null)&&(enterMsg.trailerMsgs().size()>0))
        {
            enterTrailersSoFar=new Vector();
            enterTrailersSoFar.addAll(enterMsg.trailerMsgs());
            enterMsg.trailerMsgs().clear();
        }
		if(exit!=null) exit.executeMsg(mob,enterMsg);
		if(mob.location()!=null)  mob.location().delInhabitant(mob);
		((Room)leaveMsg.target()).send(mob,leaveMsg);

		if(enterMsg.target()==null)
		{
		    ((Room)leaveMsg.target()).bringMobHere(mob,false);
			mob.tell("You can't go that way.");
			return false;
		}
		mob.setLocation((Room)enterMsg.target());
		((Room)enterMsg.target()).addInhabitant(mob);
		((Room)enterMsg.target()).send(mob,enterMsg);

		if(opExit!=null) opExit.executeMsg(mob,leaveMsg);

		if(!nolook)
        {
			CMLib.commands().postLook(mob,true);
            if((!mob.isMonster())
            &&(CMath.bset(mob.getBitmap(),MOB.ATT_AUTOWEATHER))
            &&(((Room)enterMsg.target())!=null)
            &&((thisRoom.domainType()&Room.INDOORS)>0)
            &&((((Room)enterMsg.target()).domainType()&Room.INDOORS)==0)
            &&(((Room)enterMsg.target()).getArea().getClimateObj().weatherType(((Room)enterMsg.target()))!=Climate.WEATHER_CLEAR)
            &&(((Room)enterMsg.target()).isInhabitant(mob)))
                mob.tell("\n\r"+((Room)enterMsg.target()).getArea().getClimateObj().weatherDescription(((Room)enterMsg.target())));
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
					if((follower.location()==thisRoom)&&(CMLib.flags().aliveAwakeMobile(follower,true)))
					{
						if(CMath.bset(follower.getBitmap(),MOB.ATT_AUTOGUARD))
							thisRoom.show(follower,null,null,CMMsg.MSG_OK_ACTION,"<S-NAME> remain(s) on guard here.");
						else
						{
							follower.tell("You follow "+mob.name()+" "+Directions.getDirectionName(directionCode)+".");
							if(!move(follower,directionCode,false,false,false,false))
							{
								//follower.setFollowing(null);
							}
						}
					}
				}
				//else
				//	follower.setFollowing(null);
			}
		}
        if((leaveTrailersSoFar!=null)&&(leaveMsg.target() instanceof Room))
        for(int t=0;t<leaveTrailersSoFar.size();t++)
            ((Room)leaveMsg.target()).send(mob,(CMMsg)leaveTrailersSoFar.elementAt(t));
        if((enterTrailersSoFar!=null)&&(enterMsg.target() instanceof Room))
        for(int t=0;t<enterTrailersSoFar.size();t++)
            ((Room)enterMsg.target()).send(mob,(CMMsg)enterTrailersSoFar.elementAt(t));
            
		return true;
	}

    protected Command stander=null;
	protected Vector ifneccvec=null;
	public void standIfNecessary(MOB mob, int metaFlags)
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
			stander.execute(mob,ifneccvec,metaFlags);
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		standIfNecessary(mob,metaFlags);
		if((commands.size()>3)
		&&(commands.firstElement() instanceof Integer))
		{
			return move(mob,
						((Integer)commands.elementAt(0)).intValue(),
						((Boolean)commands.elementAt(1)).booleanValue(),
						((Boolean)commands.elementAt(2)).booleanValue(),
						((Boolean)commands.elementAt(3)).booleanValue(),false);

		}
		String whereStr=CMParms.combine(commands,1);
		Room R=mob.location();
		int direction=-1;
		if(whereStr.equalsIgnoreCase("OUT"))
		{
			if(!CMath.bset(R.domainType(),Room.INDOORS))
			{
				mob.tell("You aren't indoors.");
				return false;
			}
			
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				if((R.getExitInDir(d)!=null)
				&&(R.getRoomInDir(d)!=null)
				&&(!CMath.bset(R.getRoomInDir(d).domainType(),Room.INDOORS)))
				{
					if(direction>=0)
					{
						mob.tell("Which way out?  Try North, South, East, etc..");
						return false;
					}
					direction=d;
				}
			if(direction<0)
			{
				mob.tell("There is no direct way out of this place.  Try a direction.");
				return false;
			}
		}
		if(direction<0)
			direction=Directions.getGoodDirectionCode(whereStr);
		if(direction<0)
		{
			Environmental E=null;
			if(R!=null)
				E=R.fetchFromRoomFavorItems(null,whereStr,Item.WORNREQ_UNWORNONLY);
			if(E instanceof Rideable)
			{
				Command C=CMClass.getCommand("Enter");
				return C.execute(mob,commands,metaFlags);
			}
			if((E instanceof Exit)&&(R!=null))
			{
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					if(R.getExitInDir(d)==E)
					{ direction=d; break;}
			}
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
					if(CMath.s_int(s)>0)
					{
						num=CMath.s_int(s);
						v++;
						if(v<commands.size())
							s=(String)commands.elementAt(v);
					}
					else
					if(("NSEWUDnsewud".indexOf(s.charAt(s.length()-1))>=0)
					&&(CMath.s_int(s.substring(0,s.length()-1))>0))
					{
						num=CMath.s_int(s.substring(0,s.length()-1));
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
								V.addElement(doing);
								V.addElement(Directions.getDirectionName(direction));
								mob.enqueCommand(V,metaFlags,0);
							}
						}
					}
					else
						break;
				}
			if(!doneAnything)
				mob.tell(CMStrings.capitalizeAndLower(doing)+" which direction?\n\rTry north, south, east, west, up, or down.");
		}
		return false;
	}
	public double actionsCost(MOB mob, Vector cmds){
		double cost=CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);
		if((mob!=null)&&(CMath.bset(mob.getBitmap(),MOB.ATT_AUTORUN)))
			cost /= 4.0;
		return cost;
	}
	public boolean canBeOrdered(){return true;}
}
