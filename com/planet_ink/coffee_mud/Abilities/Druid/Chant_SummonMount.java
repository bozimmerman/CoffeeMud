package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_SummonMount extends Chant
{
	public Chant_SummonMount()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Summon Mount";
		displayText="(Mount)";
		miscText="";

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=0;
		
		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(1);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Chant_SummonMount();
	}
	
	public void unInvoke()
	{
		MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked)&&(mob!=null))
			mob.destroy();
	}
	
	public boolean tick(int tickID)
	{
		if(tickID==Host.MOB_TICK)
		{
			if((affected!=null)&&(affected instanceof MOB)&&(invoker!=null))
			{
				MOB mob=(MOB)affected;
				if(((mob.amFollowing()==null)
				||(mob.amDead())
				||((invoker!=null)&&(mob.location()!=invoker.location())&&(invoker.riding()!=affected))))
				{
					mob.delAffect(this);
					mob.destroy();
				}
			}
		}
		return super.tick(tickID);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		Vector choices=new Vector();
		int fromDir=-1;
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room room=mob.location().getRoomInDir(d);
			Exit exit=mob.location().getExitInDir(d);
			Exit opExit=mob.location().getPairedExit(d);
			if((room!=null)
			&&((room.domainType()&Room.INDOORS)==0)
			&&(room.domainType()!=Room.DOMAIN_OUTDOORS_AIR)
			&&((exit!=null)&&(exit.isOpen()))
			&&(opExit!=null)&&(opExit.isOpen()))
				choices.addElement(new Integer(d));
		}
		if(choices.size()==0)
		{
			mob.tell("You must be further outdoors to summon a mount.");
			return false;
		}
		fromDir=((Integer)choices.elementAt(Dice.roll(1,choices.size(),-1))).intValue();
		Room newRoom=mob.location().getRoomInDir(fromDir);
		int opDir=Directions.getOpDirectionCode(fromDir);
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if((success)&&(newRoom!=null))
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"":"^S<S-NAME> chant(s) humbly for a mount.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				MOB target = determineMonster(mob, adjustedLevel(mob));
				target.bringToLife(newRoom,true);
				newRoom.recoverRoomStats();
				target.setStartRoom(null);
				ExternalPlay.move(target,opDir,false);
				if(target.location()==mob.location())
					ExternalPlay.follow(target,mob,true);
				invoker=mob;
				target.addNonUninvokableAffect((Ability)copyOf());
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) humbly for a mount, but is not answered.");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster, int level)
	{
		
		MOB newMOB=(MOB)CMClass.getMOB("GenRideable");
		Rideable ride=(Rideable)newMOB;
		newMOB.baseEnvStats().setAbility(11);
		newMOB.baseEnvStats().setLevel(level);
		newMOB.baseEnvStats().setWeight(500);
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("Horse"));
		newMOB.baseCharStats().setStat(CharStats.GENDER,(int)'M');
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		newMOB.setName("a wild horse");
		newMOB.setDisplayText("a wild horse stands here");
		newMOB.setDescription("An untamed beast of the fields, tame only by magical means.");
		ride.setMobCapacity(1);
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.text();
		return(newMOB);


	}
}
