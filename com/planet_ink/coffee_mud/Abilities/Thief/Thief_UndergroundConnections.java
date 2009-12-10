package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
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
public class Thief_UndergroundConnections extends ThiefSkill
{
	public String ID() { return "Thief_UndergroundConnections"; }
	public String name(){ return "Underground Connections";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	private static final String[] triggerStrings = {"UNDERGROUNDCONNECTIONS"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STREETSMARTS;}
	protected Vector pathOut=null;
	protected int hygeineLoss=0;
	protected String theNoun=null;
	protected Room currRoom=null;
	protected Vector theGroup=null;
	protected Vector storage=null;
	protected String lastDesc=null;

	public boolean tick(Tickable ticking, int tickID)
	{
		if((pathOut!=null)&&(tickID==Tickable.TICKID_MOB))
		{
			if((pathOut.size()==0)||(theGroup==null)||(currRoom==null))
				unInvoke();
			else
			{
				currRoom.showHappens(CMMsg.MSG_OK_ACTION,theNoun+" goes by.");
				currRoom=currRoom.getRoomInDir(((Integer)pathOut.firstElement()).intValue());
				pathOut.removeElementAt(0);
				if(currRoom!=null)
				{
					String roomDesc=currRoom.roomTitle(null);
					if((lastDesc==null)||(!roomDesc.equalsIgnoreCase(lastDesc)))
					{
						lastDesc=roomDesc;
						for(int g=0;g<theGroup.size();g++)
						{
							MOB M=(MOB)theGroup.elementAt(g);
							if(M.playerStats()!=null) M.playerStats().adjHygiene(hygeineLoss);
							switch(CMLib.dice().roll(1,10,0))
							{
							case 1: M.tell("You think you are being taken through '"+roomDesc+"'."); break;
							case 2: M.tell("You might be going through '"+roomDesc+"' now."); break;
							case 3: M.tell("Now you are definitely going through '"+roomDesc+"'."); break;
							case 4: M.tell("You are being taken through '"+roomDesc+"', you think."); break;
							case 5: M.tell("Sounds like '"+roomDesc+"' now."); break;
							case 6: M.tell("Now this might be '"+roomDesc+"'."); break;
							case 7: M.tell("You're going through '"+roomDesc+"'."); break;
							case 8: M.tell("Sounds like this could be '"+roomDesc+"'."); break;
							case 9: M.tell("You are probably going through '"+roomDesc+"' now."); break;
							case 10: M.tell("Sounds like you are being taken through '"+roomDesc+"'."); break;
							}
						}
					}
				}
			}
		}
		return super.tick(ticking,tickID);
	}
	
	public void unInvoke()
	{
		if(pathOut!=null)
		{
			if(currRoom==null) currRoom=CMLib.map().getRandomRoom();
			if(theGroup!=null)
			for(int g=0;g<theGroup.size();g++)
			{
				MOB M=(MOB)theGroup.elementAt(g);
				M.tell("You are told that it's safe and released.");
				currRoom.bringMobHere(M,false);
				CMLib.commands().postStand(M,true);
				CMLib.commands().postLook(M,true);
			}
			if(storage!=null)
			for(int s=0;s<storage.size();s++)
				((Room)storage.elementAt(s)).destroy();
			pathOut=null;
			currRoom=null;
			if(storage!=null) storage.clear();
			storage=null;
			if(theGroup!=null) theGroup.clear();
			theGroup=null;
		}
		super.unInvoke();
	}
	
	public void bringMOBSHere(Room newRoom, Vector group, String enterStr, String leaveStr)
	{
		for(int g=group.size()-1;g>=0;g--)
		{
			MOB follower=(MOB)group.elementAt(g);
			if(!bringMOBHere(newRoom,follower,enterStr,leaveStr))
				group.removeElementAt(g);
		}
	}
	public void bringMOBSLikeHere(Vector rooms, Room newRoom, Vector group, String enterStr, String leaveStr)
	{
		for(int g=group.size()-1;g>=0;g--)
		{
			MOB follower=(MOB)group.elementAt(g);
			Room R=(Room)newRoom.copyOf();
			if(!bringMOBHere(R,follower,enterStr,leaveStr))
				group.removeElementAt(g);
			else
				rooms.addElement(R);
		}
	}
	public boolean bringMOBHere(Room newRoom, MOB follower, String leaveStr, String enterStr)
	{
		Room thisRoom=follower.location();
		CMMsg enterMsg=CMClass.getMsg(follower,newRoom,this,CMMsg.MSG_ENTER,enterStr,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,"You are joined by <S-NAME>.");
		CMMsg leaveMsg=CMClass.getMsg(follower,thisRoom,this,CMMsg.MSG_LEAVE,leaveStr,CMMsg.MSG_LEAVE,null,CMMsg.MSG_LEAVE,leaveStr);
		if(thisRoom.okMessage(follower,leaveMsg)&&newRoom.okMessage(follower,enterMsg))
		{
			if(follower.isInCombat())
			{
				CMLib.commands().postFlee(follower,("NOWHERE"));
				follower.makePeace();
			}
			thisRoom.send(follower,leaveMsg);
			newRoom.bringMobHere(follower,false);
            thisRoom.delInhabitant(follower);
			newRoom.send(follower,enterMsg);
			follower.baseEnvStats().setDisposition(follower.baseEnvStats().disposition()|EnvStats.IS_SITTING);
			follower.envStats().setDisposition(follower.envStats().disposition()|EnvStats.IS_SITTING);
			//follower.tell("\n\r\n\r");
			//CMLib.commands().postLook(follower,true);
			return true;
		}
		return false;
	}
	
	public Room makeNewRoom(Area area, String display, String description)
	{
		Room R=CMClass.getLocale("MagicShelter");
		R.setDisplayText(display);
		R.setDescription(description);
		R.setRoomID("");
		R.setArea(area);
		Ability A=CMClass.getAbility("Thief_Bind");
		if(A!=null)
		{
			Item I=CMClass.getBasicItem("StdItem");
			I.setName(display);
			A.setAffectedOne(I);
			R.addNonUninvokableEffect(A);
		}
		return R;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.isInCombat())
		{
			mob.tell(target,null,null,"Not while <S-NAME> <S-IS-ARE> fighting.");
			return false;
		}
		Room thisRoom=target.location();
		if(thisRoom==null) return false;
		
		if((!auto)&&(thisRoom.domainType()!=Room.DOMAIN_OUTDOORS_CITY))
		{
			mob.tell("You must be out on a street to contact your underground connections.");
			return false;
		}
		Area A=CMLib.map().areaLocation(target);
        if((!CMLib.law().isACity(A))
		&&(!auto))
		{
			mob.tell("You can only use this skill in cities.");
			return false;
		}

		TrackingLibrary.TrackingFlags flags=new TrackingLibrary.TrackingFlags();
		flags.add(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
			 .add(TrackingLibrary.TrackingFlag.NOAIR)
			 .add(TrackingLibrary.TrackingFlag.NOWATER);
		Vector trail=CMLib.tracking().getRadiantRooms(thisRoom,flags,30+(2*getXLEVELLevel(mob)));
		Vector finalTos=new Vector();
        Room R=null;
		for(int c=0;c<trail.size();c++)
		{
			R=(Room)trail.elementAt(c);
			if((R.getArea()!=A)
			&&(!CMath.bset(R.domainType(),Room.INDOORS))
			&&(CMLib.flags().canAccess(target,R)))
			{
				Room checkR=null;
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					checkR=R.getRoomInDir(d);
					if((checkR!=null)
					&&(checkR.getArea()==A)
					&&(!CMath.bset(checkR.domainType(),Room.INDOORS))
					&&(trail.indexOf(checkR)<c))
					{	finalTos.addElement(R); break;}
				}
			}
		}
		Vector allTrails=CMLib.tracking().findAllTrails(thisRoom,finalTos,trail);
		for(int a=allTrails.size()-1;a>=0;a--)
		{
			Vector thisTrail=(Vector)allTrails.elementAt(a);
			R=thisRoom;
			for(int t=0;t<thisTrail.size();t++)
			{
				R=R.getRoomInDir(((Integer)thisTrail.elementAt(t)).intValue());
				if((R==null)||(CMath.bset(R.domainType(),Room.INDOORS)))
				{ allTrails.removeElementAt(a); break;}
			}
		}
		if(allTrails.size()==0)
		{
			mob.tell("Your informants tell you that there's no way they can get you out of here.");
			return false;
		}
		Vector theTrail=(Vector)allTrails.elementAt(CMLib.dice().roll(1,allTrails.size(),-1));
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		CMMsg msg=CMClass.getMsg(mob,null,this,auto?CMMsg.MASK_ALWAYS:CMMsg.MSG_DELICATE_HANDS_ACT,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,auto?"":"<S-NAME> contact(s) <S-HIS-HER> underground connections here.");
		if(!success)
			return beneficialVisualFizzle(mob,null,auto?"":"<S-NAME> can't seem to contact <S-HIS-HER> underground connections here.");
		else
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,asLevel,0);
			Thief_UndergroundConnections underA=(Thief_UndergroundConnections)target.fetchEffect(ID());
			if(underA!=null)
			{
				underA.currRoom=thisRoom;
				HashSet H=target.getGroupMembers(new HashSet());
				Vector group=new Vector();
				group.addElement(target);
				for(Iterator i=H.iterator();i.hasNext();)
				{
					MOB M=(MOB)i.next();
					if((M!=null)
					&&(M.location()==thisRoom)
					&&(CMLib.flags().isInTheGame(M,true))
					&&(!group.contains(M)))
						group.addElement(M);
				}
				underA.theGroup=group;
				Area area=thisRoom.getArea();
				Vector rooms=new Vector();
				switch(CMLib.dice().roll(1,4,0))
				{
				case 1:
				{
					underA.theNoun="a strange horse drawn cart";
					thisRoom.showHappens(CMMsg.MSG_OK_ACTION,"A horse drawn wagon filled with hay pulls up.\n\rTwo large farmers jump out.");
					Room destR=makeNewRoom(area,"A bundle of straw","Your eyes, mouth, and ears are filled with the stuff. This trip better be short!");
					rooms.addElement(destR);
					bringMOBSHere(destR,group,"","<S-NAME> <S-IS-ARE> picked up and stuffed into the piles of hay");
					break;
				}
				case 2:
				{
					underA.theNoun="a large group of teamsters carrying potato sacks";
					thisRoom.showHappens(CMMsg.MSG_OK_ACTION,"A group of large teamsters with big empty potato sacks approaches.");
					Room destR=makeNewRoom(area,"A potato sack","Once you get past the musty rotten smell, this is really uncomfortable!");
					bringMOBSLikeHere(rooms,destR,group,"","<S-NAME> <S-IS-ARE> picked up and stuffed into a sack");
					break;
				}
				case 3:
				{
					underA.theNoun="a pullcart full of plague victims";
					thisRoom.showHappens(CMMsg.MSG_OK_ACTION,"A pullcart full of the decaying bodies of plague victims pulls up.");
					Room destR=makeNewRoom(area,"A pile of bodies","There is a finger in your ear, and you aren't sure whose it is.");
					rooms.addElement(destR);
					bringMOBSHere(destR,group,"","<S-NAME> <S-IS-ARE> picked up and stuffed into the bottom of the pile");
					break;
				}
				case 4:
				{
					underA.theNoun="a horse drawn cart full of empty vinegar barrels";
					thisRoom.showHappens(CMMsg.MSG_OK_ACTION,"A horse drawn cart full of empty vinegar barrels pulls up.");
					Room destR=makeNewRoom(area,"A vinegar barrel","You feel thoroughly pickled!");
					bringMOBSLikeHere(rooms,destR,group,"","<S-NAME> <S-IS-ARE> picked up and stuffed into an empty barrel.");
					break;
				}
				}
				underA.hygeineLoss=(int)PlayerStats.HYGIENE_DELIMIT/theTrail.size();
				underA.storage=rooms;
				underA.pathOut=theTrail;
			}
		}
		return success;
	}
}