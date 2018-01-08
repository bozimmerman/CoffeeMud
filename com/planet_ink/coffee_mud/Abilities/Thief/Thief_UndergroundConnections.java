package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2006-2018 Bo Zimmerman

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

public class Thief_UndergroundConnections extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_UndergroundConnections";
	}

	private final static String localizedName = CMLib.lang().L("Underground Connections");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private static final String[] triggerStrings =I(new String[] {"UNDERGROUNDCONNECTIONS"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STREETSMARTS;
	}

	protected List<Integer> pathOut=null;
	protected int hygieneLoss=0;
	protected String theNoun=null;
	protected Room currRoom=null;
	protected Vector<MOB> theGroup=null;
	protected Vector<Room> storage=null;
	protected String lastDesc=null;

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((pathOut!=null)&&(tickID==Tickable.TICKID_MOB))
		{
			if((pathOut.size()==0)||(theGroup==null)||(currRoom==null))
				unInvoke();
			else
			{
				currRoom.showHappens(CMMsg.MSG_OK_ACTION,L("@x1 goes by.",theNoun));
				currRoom=currRoom.getRoomInDir(pathOut.get(0).intValue());
				pathOut.remove(0);
				if(currRoom!=null)
				{
					final String roomDesc=currRoom.displayText(null);
					if((lastDesc==null)||(!roomDesc.equalsIgnoreCase(lastDesc)))
					{
						lastDesc=roomDesc;
						for(int g=0;g<theGroup.size();g++)
						{
							final MOB M=theGroup.elementAt(g);
							if((M.playerStats()!=null)&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.HYGIENE)))
								M.playerStats().adjHygiene(hygieneLoss);
							switch(CMLib.dice().roll(1,10,0))
							{
							case 1: M.tell(L("You think you are being taken through '@x1'.",roomDesc)); break;
							case 2: M.tell(L("You might be going through '@x1' now.",roomDesc)); break;
							case 3: M.tell(L("Now you are definitely going through '@x1'.",roomDesc)); break;
							case 4: M.tell(L("You are being taken through '@x1', you think.",roomDesc)); break;
							case 5: M.tell(L("Sounds like '@x1' now.",roomDesc)); break;
							case 6: M.tell(L("Now this might be '@x1'.",roomDesc)); break;
							case 7: M.tell(L("You're going through '@x1'.",roomDesc)); break;
							case 8: M.tell(L("Sounds like this could be '@x1'.",roomDesc)); break;
							case 9: M.tell(L("You are probably going through '@x1' now.",roomDesc)); break;
							case 10: M.tell(L("Sounds like you are being taken through '@x1'.",roomDesc)); break;
							}
						}
					}
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(((msg.sourceMinor()==CMMsg.TYP_QUIT)
			||(msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
			||((msg.targetMinor()==CMMsg.TYP_EXPIRE)&&(storage.contains(msg.target())))
			||(msg.sourceMinor()==CMMsg.TYP_ROOMRESET)))
		{
			unInvoke();
		}
		return super.okMessage(host,msg);
	}

	@Override
	public void unInvoke()
	{
		if(pathOut!=null)
		{
			if(currRoom==null)
				currRoom=CMLib.map().getRandomRoom();
			if(theGroup!=null)
			for(int g=0;g<theGroup.size();g++)
			{
				final MOB M=theGroup.elementAt(g);
				M.tell(L("You are told that it's safe and released."));
				currRoom.bringMobHere(M,false);
				CMLib.commands().postStand(M,true);
				CMLib.commands().postLook(M,true);
			}
			if(storage!=null)
			for(int s=0;s<storage.size();s++)
				storage.elementAt(s).destroy();
			pathOut=null;
			currRoom=null;
			if(storage!=null)
				storage.clear();
			storage=null;
			if(theGroup!=null)
				theGroup.clear();
			theGroup=null;
		}
		super.unInvoke();
	}

	public void bringMOBSHere(Room newRoom, Vector<MOB> group, String enterStr, String leaveStr)
	{
		for(int g=group.size()-1;g>=0;g--)
		{
			final MOB follower=group.elementAt(g);
			if(!bringMOBHere(newRoom,follower,enterStr,leaveStr))
				group.removeElementAt(g);
		}
	}

	public void bringMOBSLikeHere(Vector<Room> rooms, Room newRoom, Vector<MOB> group, String enterStr, String leaveStr)
	{
		for(int g=group.size()-1;g>=0;g--)
		{
			final MOB follower=group.elementAt(g);
			final Room R=(Room)newRoom.copyOf();
			if(!bringMOBHere(R,follower,enterStr,leaveStr))
				group.removeElementAt(g);
			else
				rooms.addElement(R);
		}
	}

	public boolean bringMOBHere(Room newRoom, MOB follower, String leaveStr, String enterStr)
	{
		final Room thisRoom=follower.location();
		final CMMsg enterMsg=CMClass.getMsg(follower,newRoom,this,CMMsg.MSG_ENTER,enterStr,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,L("You are joined by <S-NAME>."));
		final CMMsg leaveMsg=CMClass.getMsg(follower,thisRoom,this,CMMsg.MSG_LEAVE,leaveStr,CMMsg.MSG_LEAVE,null,CMMsg.MSG_LEAVE,leaveStr);
		if(thisRoom.okMessage(follower,leaveMsg)&&newRoom.okMessage(follower,enterMsg))
		{
			if(follower.isInCombat())
			{
				CMLib.commands().postFlee(follower,("NOWHERE"));
				follower.makePeace(true);
			}
			thisRoom.send(follower,leaveMsg);
			newRoom.bringMobHere(follower,false);
			thisRoom.delInhabitant(follower);
			newRoom.send(follower,enterMsg);
			follower.basePhyStats().setDisposition(follower.basePhyStats().disposition()|PhyStats.IS_SITTING);
			follower.phyStats().setDisposition(follower.phyStats().disposition()|PhyStats.IS_SITTING);
			//follower.tell(L("\n\r\n\r"));
			//CMLib.commands().postLook(follower,true);
			return true;
		}
		return false;
	}

	public Room makeNewRoom(Area area, String display, String description)
	{
		final Room R=CMClass.getLocale("MagicShelter");
		R.setDisplayText(display);
		R.setDescription(description);
		R.setRoomID("");
		R.setArea(area);
		final Ability A=CMClass.getAbility("Thief_Bind");
		if(A!=null)
		{
			final Item I=CMClass.getBasicItem("StdItem");
			I.setName(display);
			A.setAffectedOne(I);
			R.addNonUninvokableEffect(A);
		}
		return R;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.isInCombat())
		{
			mob.tell(target,null,null,L("Not while <S-NAME> <S-IS-ARE> fighting."));
			return false;
		}
		final Room thisRoom=target.location();
		if(thisRoom==null)
			return false;

		if((!auto)&&(thisRoom.domainType()!=Room.DOMAIN_OUTDOORS_CITY))
		{
			mob.tell(L("You must be out on a street to contact your underground connections."));
			return false;
		}
		final Area A=CMLib.map().areaLocation(target);
		if((!CMLib.law().isACity(A))
		&&(!auto))
		{
			mob.tell(L("You can only use this skill in cities."));
			return false;
		}

		final TrackingLibrary.TrackingFlags flags=CMLib.tracking().newFlags();
		flags.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
			 .plus(TrackingLibrary.TrackingFlag.NOAIR)
			 .plus(TrackingLibrary.TrackingFlag.NOWATER);
		final List<Room> trail=CMLib.tracking().getRadiantRooms(thisRoom,flags,30+(2*getXLEVELLevel(mob)));
		final Vector<Room> finalTos=new Vector<Room>();
		Room R=null;
		for(int c=0;c<trail.size();c++)
		{
			R=trail.get(c);
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
					{
						finalTos.addElement(R);
						break;
					}
				}
			}
		}
		final List<List<Integer>> allTrails=CMLib.tracking().findAllTrails(thisRoom,finalTos,trail);
		for(int a=allTrails.size()-1;a>=0;a--)
		{
			final List<Integer> thisTrail=allTrails.get(a);
			R=thisRoom;
			for(int t=0;t<thisTrail.size();t++)
			{
				R=R.getRoomInDir(thisTrail.get(t).intValue());
				if((R==null)||(CMath.bset(R.domainType(),Room.INDOORS)))
				{
					allTrails.remove(a);
					break;
				}
			}
		}
		if(allTrails.size()==0)
		{
			mob.tell(L("Your informants tell you that there's no way they can get you out of here."));
			return false;
		}
		final List<Integer> theTrail=allTrails.get(CMLib.dice().roll(1,allTrails.size(),-1));

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		final CMMsg msg=CMClass.getMsg(mob,null,this,auto?CMMsg.MASK_ALWAYS:CMMsg.MSG_DELICATE_HANDS_ACT,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,auto?"":L("<S-NAME> contact(s) <S-HIS-HER> underground connections here."));
		if(!success)
			return beneficialVisualFizzle(mob,null,auto?"":L("<S-NAME> can't seem to contact <S-HIS-HER> underground connections here."));
		else
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,asLevel,0);
			final Thief_UndergroundConnections underA=(Thief_UndergroundConnections)target.fetchEffect(ID());
			if(underA!=null)
			{
				underA.currRoom=thisRoom;
				final Set<MOB> H=target.getGroupMembers(new HashSet<MOB>());
				final Vector<MOB> group=new Vector<MOB>();
				group.addElement(target);
				for (final Object element : H)
				{
					final MOB M=(MOB)element;
					if((M!=null)
					&&(M.location()==thisRoom)
					&&(CMLib.flags().isInTheGame(M,true))
					&&(!group.contains(M)))
						group.addElement(M);
				}
				underA.theGroup=group;
				final Area area=thisRoom.getArea();
				final Vector<Room> rooms=new Vector<Room>();
				switch(CMLib.dice().roll(1,4,0))
				{
				case 1:
				{
					underA.theNoun="a strange horse drawn cart";
					thisRoom.showHappens(CMMsg.MSG_OK_ACTION,L("A horse drawn wagon filled with hay pulls up.\n\rTwo large farmers jump out."));
					final Room destR=makeNewRoom(area,"A bundle of straw","Your eyes, mouth, and ears are filled with the stuff. This trip better be short!");
					rooms.addElement(destR);
					bringMOBSHere(destR,group,"","<S-NAME> <S-IS-ARE> picked up and stuffed into the piles of hay");
					break;
				}
				case 2:
				{
					underA.theNoun="a large group of teamsters carrying potato sacks";
					thisRoom.showHappens(CMMsg.MSG_OK_ACTION,L("A group of large teamsters with big empty potato sacks approaches."));
					final Room destR=makeNewRoom(area,"A potato sack","Once you get past the musty rotten smell, this is really uncomfortable!");
					bringMOBSLikeHere(rooms,destR,group,"","<S-NAME> <S-IS-ARE> picked up and stuffed into a sack");
					break;
				}
				case 3:
				{
					underA.theNoun="a pullcart full of plague victims";
					thisRoom.showHappens(CMMsg.MSG_OK_ACTION,L("A pullcart full of the decaying bodies of plague victims pulls up."));
					final Room destR=makeNewRoom(area,"A pile of bodies","There is a finger in your ear, and you aren't sure whose it is.");
					rooms.addElement(destR);
					bringMOBSHere(destR,group,"","<S-NAME> <S-IS-ARE> picked up and stuffed into the bottom of the pile");
					break;
				}
				case 4:
				{
					underA.theNoun="a horse drawn cart full of empty vinegar barrels";
					thisRoom.showHappens(CMMsg.MSG_OK_ACTION,L("A horse drawn cart full of empty vinegar barrels pulls up."));
					final Room destR=makeNewRoom(area,"A vinegar barrel","You feel thoroughly pickled!");
					bringMOBSLikeHere(rooms,destR,group,"","<S-NAME> <S-IS-ARE> picked up and stuffed into an empty barrel.");
					break;
				}
				}
				underA.hygieneLoss=(int)PlayerStats.HYGIENE_DELIMIT/theTrail.size();
				underA.storage=rooms;
				underA.pathOut=theTrail;
			}
		}
		return success;
	}
}
