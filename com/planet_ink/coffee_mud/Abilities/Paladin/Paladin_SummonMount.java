package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Paladin_SummonMount extends StdAbility
{
	public String ID() { return "Paladin_SummonMount"; }
	public String name(){ return "Call Mount";}
	public String displayText() {return "(Mount)";}
	private static final String[] triggerStrings = {"CALLMOUNT"};
	public int quality(){return Ability.OK_SELF;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Paladin_SummonMount();}
	public int classificationCode(){ return Ability.SKILL;}

	public void unInvoke()
	{
		MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			if(mob.amDead()) mob.setLocation(null);
			mob.destroy();
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Host.MOB_TICK)
		{
			if((affected!=null)&&(affected instanceof MOB)&&(invoker!=null))
			{
				MOB mob=(MOB)affected;
				if(((mob.amFollowing()==null)
				||(mob.amDead())
				||(mob.location()==null)
				||(invoker==null)
				||(invoker.location()==null)
				||((invoker!=null)&&(mob.location()!=invoker.location())&&(invoker.riding()!=affected))))
				{
					mob.delAffect(this);
					if(mob.amDead()) mob.setLocation(null);
					mob.destroy();
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	public void affect(Environmental myHost, Affect msg)
	{
		super.affect(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing()))
		&&(msg.sourceMinor()==Affect.TYP_QUIT))
			unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors to call your mount.");
			return false;
		}
		if((!auto)&&(mob.getAlignment()<650))
		{
			mob.tell("Your alignment has alienated you from your god.");
			return false;
		}
		Vector choices=new Vector();
		int fromDir=-1;
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room room=mob.location().getRoomInDir(d);
			Exit exit=mob.location().getExitInDir(d);
			Exit opExit=mob.location().getReverseExit(d);
			if((room!=null)
			&&((room.domainType()&Room.INDOORS)==0)
			&&(room.domainType()!=Room.DOMAIN_OUTDOORS_AIR)
			&&((exit!=null)&&(exit.isOpen()))
			&&(opExit!=null)&&(opExit.isOpen()))
				choices.addElement(new Integer(d));
		}
		if(choices.size()==0)
		{
			mob.tell("You must be further outdoors to call your mount.");
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
			FullMsg msg=new FullMsg(mob,null,this,Affect.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> call(s) for <S-HIS-HER> loyal steed.");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				MOB target = determineMonster(mob, adjustedLevel(mob));
				target.bringToLife(newRoom,true);
				target.location().showOthers(target,null,Affect.MSG_OK_ACTION,"<S-NAME> appears!");
				newRoom.recoverRoomStats();
				target.setStartRoom(null);
				ExternalPlay.move(target,opDir,false,false);
				if(target.location()==mob.location())
				{
					ExternalPlay.follow(target,mob,true);
					if(target.amFollowing()!=mob)
						mob.tell(target.name()+" seems unwilling to follow you.");
				}
				invoker=mob;
				target.addNonUninvokableAffect((Ability)copyOf());
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> call(s) for <S-HIS-HER> loyal steed, but is not answered.");

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
		newMOB.setAlignment(1000);
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("Horse"));
		newMOB.baseCharStats().setStat(CharStats.GENDER,(int)'M');
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		newMOB.baseEnvStats().setArmor(newMOB.baseCharStats().getCurrentClass().getLevelArmor(newMOB));
		newMOB.baseEnvStats().setAttackAdjustment(newMOB.baseCharStats().getCurrentClass().getLevelAttack(newMOB));
		newMOB.baseEnvStats().setDamage(newMOB.baseCharStats().getCurrentClass().getLevelDamage(newMOB));
		newMOB.setName("a white horse");
		newMOB.setDisplayText("a proud white horse stands here");
		newMOB.setDescription("A proud and noble steed; albino white and immaculate.");
		ride.setRiderCapacity(4);
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.text();
		return(newMOB);


	}
}
