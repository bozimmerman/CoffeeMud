package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_PlantMaze extends Chant
{
	public String ID() { return "Chant_PlantMaze"; }
	public String name(){ return "Plant Maze";}
	public String displayText(){return "(Plant Maze)";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return CAN_ROOMS;}
	public Environmental newInstance(){	return new Chant_PlantMaze();}
	Room oldRoom=null;
	Item thePlants=null;

	public boolean tick(Tickable ticking,int tickID)
	{
		if((thePlants==null)||(thePlants.owner()==null)||(!(thePlants.owner() instanceof Room)))
		{
			unInvoke();
			return false;
		}
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		Room room=(Room)affected;
		if((canBeUninvoked())&&(room instanceof GridLocale)&&(oldRoom!=null))
		{
			Vector V=((GridLocale)room).getAllRooms();
			for(int v=0;v<V.size();v++)
			{
				Room R=(Room)V.elementAt(v);
				while(R.numInhabitants()>0)
				{
					MOB M=R.fetchInhabitant(0);
					if(M!=null)	oldRoom.bringMobHere(M,false);
				}
				while(R.numItems()>0)
				{
					Item I=R.fetchItem(0);
					if(I!=null) oldRoom.bringItemHere(I);
				}
			}
			room.clearSky();
			((GridLocale)room).clearGrid();
			CMMap.delRoom(room);
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		thePlants=Druid_MyPlants.myPlant(mob.location(),mob,0);
		if(thePlants==null)
		{
			mob.tell("There doesn't appear to be any plants here you can control!");
			return false;
		}

		if(mob.location().roomID().length()==0)
		{
			mob.tell("You cannot invoke the plant maze here.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			FullMsg msg = new FullMsg(mob, null, this, affectType(auto), auto?"":"^S<S-NAME> chant(s) amazingly!^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().showHappens(Affect.MSG_OK_VISUAL,"Something is happening...");

				Room newRoom=CMClass.getLocale("WoodsMaze");
				((GridLocale)newRoom).setXSize(10);
				((GridLocale)newRoom).setYSize(10);
				String s=((String)Util.parse(thePlants.name()).lastElement()).toLowerCase();
				if(!s.endsWith("s"))s=s+"s";
				String nos=s.substring(0,s.length()-1).toLowerCase();
				newRoom.setDisplayText(Util.capitalize(nos)+" Maze");
				newRoom.addNonUninvokableAffect(CMClass.getAbility("Prop_NoTeleportOut"));
				StringBuffer desc=new StringBuffer("");
				desc.append("This quaint glade is surrounded by tall "+s+".  A gentle breeze tosses leaves up into the air.");
				desc.append("<P>");
				desc.append("This forest of "+s+" is dark and thick here.  Ominous looking "+s+" seem to block every path, and the air is perfectly still.");
				desc.append("<P>");
				desc.append("A light growth of tall "+s+" surrounds you on all sides.  There are no apparant paths, but you can still see the sky between the growths.");
				desc.append("<P>");
				desc.append("A light growth of tall "+s+" surrounds you on all sides.  You can hear the sound of a running brook, but can't tell which direction its coming from.");
				desc.append("<P>");
				desc.append("The "+s+" around you are tall, dark and old, their leaves seeming to reach towards you.  In the distance, a wolfs howl can be heard.");
				desc.append("<P>");
				desc.append("The path seems to end at the base of a copse of tall "+s+".");
				desc.append("<P>");
				desc.append("You are standing in the middle of a light forest of "+s+".  How you got here, you can't really say.");
				desc.append("<P>");
				desc.append("You are standing in the middle of a thick dark forest of "+s+".  You wish you knew how you got here.");
				desc.append("<P>");
				desc.append("The "+s+" here seem to tower endlessly into the sky.  Their leaves are blocking out all but the smallest glimpses of the sky.");
				desc.append("<P>");
				desc.append("A forest of "+s+" seems to have grown up tall all around you.  The strange magical nature of the "+s+" makes you think you've entered a druidic grove.");
				newRoom.setArea(mob.location().getArea());
				oldRoom=mob.location();
				newRoom.setDescription(desc.toString());
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					Room R=mob.location().rawDoors()[d];
					Exit E=mob.location().rawExits()[d];
					if((R!=null)&&(R.roomID().length()>0))
					{
						newRoom.rawDoors()[d]=R;
						newRoom.rawExits()[d]=E;
					}
				}
				newRoom.getArea().fillInAreaRoom(newRoom);
				beneficialAffect(mob,newRoom,0);
				Vector V=((GridLocale)newRoom).getAllRooms();
				Vector everyone=new Vector();
				for(int m=0;m<oldRoom.numInhabitants();m++)
				{
					MOB follower=(MOB)oldRoom.fetchInhabitant(m);
					everyone.addElement(follower);
				}

				if(V.size()>0)
				for(int m=0;m<everyone.size();m++)
				{
					MOB follower=(MOB)everyone.elementAt(m);
					if(follower==null) continue;
					Room newerRoom=(Room)V.elementAt(Dice.roll(1,V.size(),-1));
					FullMsg enterMsg=new FullMsg(follower,newerRoom,null,Affect.MSG_ENTER,null,Affect.MSG_ENTER,null,Affect.MSG_ENTER,"<S-NAME> appears out of "+thePlants.name()+".");
					FullMsg leaveMsg=new FullMsg(follower,oldRoom,this,affectType(auto),"<S-NAME> disappear(s) into "+thePlants.name()+".");
					if(oldRoom.okAffect(follower,leaveMsg)&&newerRoom.okAffect(follower,enterMsg))
					{
						if(follower.isInCombat())
							follower.makePeace();
						oldRoom.send(follower,leaveMsg);
						newerRoom.bringMobHere(follower,false);
						newerRoom.send(follower,enterMsg);
						follower.tell("\n\r\n\r");
						ExternalPlay.look(follower,null,true);
					}
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) amazingly, but the magic fades.");

		// return whether it worked
		return success;
	}
}
