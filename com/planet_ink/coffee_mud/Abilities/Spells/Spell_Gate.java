package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Gate extends Spell
{
	public String ID() { return "Spell_Gate"; }
	public String name(){return "Gate";}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){ return new Spell_Gate();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_CONJURATION;}
	protected int overrideMana(){return Integer.MAX_VALUE;}
	public long flags(){return Ability.FLAG_TRANSPORTING;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		if((auto||mob.isMonster())&&((commands.size()<1)||(((String)commands.firstElement()).equals(mob.name()))))
		{
			commands.clear();
			MOB M=null;
			int tries=0;
			while(((++tries)<100)&&(M==null))
			{
				Room R=CMMap.getRandomRoom();
				if(R.numInhabitants()>0)
					M=R.fetchInhabitant(Dice.roll(1,R.numInhabitants(),-1));
				if(M.name().equals(mob.name()))
					M=null;
			}
			if(M!=null)
				commands.addElement(M.Name());
		}
		if(commands.size()<1)
		{
			mob.tell("Gate to whom?");
			return false;
		}
		String areaName=Util.combine(commands,0).trim().toUpperCase();

		if(mob.location().fetchInhabitant(areaName)!=null)
		{
			mob.tell("Better look around first.");
			return false;
		}

		if(Sense.isSitting(mob)||Sense.isSleeping(mob))
		{
			mob.tell("You need to stand up!");
			return false;
		}
		
		Vector candidates=new Vector();
		MOB target=null;
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room room=(Room)r.nextElement();
			if(Sense.canAccess(mob,room))
			{
				target=room.fetchInhabitant(areaName);
				if(target!=null)
					candidates.addElement(target);
			}
		}
		Room newRoom=null;
		if(candidates.size()>0)
		{
			target=(MOB)candidates.elementAt(Dice.roll(1,candidates.size(),-1));
			newRoom=target.location();
		}

		if(newRoom==null)
		{
			mob.tell("You can't seem to fixate on '"+Util.combine(commands,0)+"', perhaps they don't exist?");
			return false;
		}

		int adjustment=target.envStats().level()-mob.envStats().level();
		if(target.isMonster()) adjustment=adjustment*3;
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,-adjustment,auto);
		if(!success)
		{
			Room room=null;
			int x=0;
			while((room==null)||(room==newRoom)||((++x)>1000)||(room==mob.location())||(!Sense.canAccess(mob,room)))
				room=CMMap.getRandomRoom();
			if(room==null)
				beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to invoke transportation, but fizzle(s) the spell.");
			newRoom=room;
		}
				
		FullMsg msg=new FullMsg(mob,target,this,CMMsg.MASK_MOVE|affectType(auto),"^S<S-NAME> invoke(s) a teleportation spell.^?");
		if((mob.location().okMessage(mob,msg))&&(newRoom.okMessage(mob,msg)))
		{
			mob.location().send(mob,msg);
			HashSet h=properTargets(mob,givenTarget,false);
			if(h==null) return false;

			Room thisRoom=mob.location();
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB follower=(MOB)f.next();
				FullMsg enterMsg=new FullMsg(follower,newRoom,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,("<S-NAME> appear(s) in a burst of light.")+CommonStrings.msp("appear.wav",10));
				FullMsg leaveMsg=new FullMsg(follower,thisRoom,this,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC,"<S-NAME> disappear(s) in a burst of light.");
				if(thisRoom.okMessage(follower,leaveMsg)&&newRoom.okMessage(follower,enterMsg))
				{
					if(follower.isInCombat())
					{
						CommonMsgs.flee(follower,("NOWHERE"));
						follower.makePeace();
					}
					thisRoom.send(follower,leaveMsg);
					newRoom.bringMobHere(follower,false);
					newRoom.send(follower,enterMsg);
					follower.tell("\n\r\n\r");
					CommonMsgs.look(follower,true);
				}
			}
		}

		// return whether it worked
		return success;
	}
}
