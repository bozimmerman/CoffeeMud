package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Druid_PackCall extends StdAbility
{
	public String ID() { return "Druid_PackCall"; }
	public String name(){ return "Pack Call";}
	public String displayText(){return "(Pack Call)";}
	private static final String[] triggerStrings = {"PACKCALL"};
	public String[] triggerStrings(){return triggerStrings;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Druid_PackCall();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==MudHost.TICK_MOB)
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(invoker!=null))
			{
				MOB mob=(MOB)affected;
				if(((mob.amFollowing()==null)
				||(mob.amDead())
				||(!mob.isInCombat())
				||(mob.location()!=invoker.location())))
					unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> wander(s) off.");
			if(mob.amDead()) mob.setLocation(null);
			mob.destroy();
		}
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing()))
		&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
			unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors to call your pack.");
			return false;
		}
		if(mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		{
			mob.tell("You must be in the wild to call your pack.");
			return false;
		}
		if(!mob.isInCombat())
		{
			mob.tell("Only the anger of combat can call your pack.");
			return false;
		}
		Druid_ShapeShift D=null;
		for(int a=0;a<mob.numEffects();a++)
		{
			Ability A=mob.fetchEffect(a);
			if((A!=null)&&(A instanceof Druid_ShapeShift))
				D=(Druid_ShapeShift)A;
		}
		if(D==null)
		{
			mob.tell("You must be in your animal form to call the pack.");
			return false;
		}

		if(mob.totalFollowers()>=mob.maxFollowers())
		{
			mob.tell("You can't have any more followers!");
			return false;
		}

		Vector choices=new Vector();
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room R=mob.location().getRoomInDir(d);
			Exit E=mob.location().getExitInDir(d);
			if((R!=null)&&(E!=null)&&(E.isOpen())&&(d!=Directions.UP))
				choices.addElement(new Integer(d));
		}

		if(choices.size()==0)
		{
			mob.tell("Your call would not be heard here.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,CMMsg.MSG_NOISE,auto?"":"^S<S-NAME> call(s) for help from <S-HIS-HER> pack!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int levelsRemaining=90;
				while((mob.totalFollowers()<mob.maxFollowers())&&(levelsRemaining>0))
				{
					MOB victim=mob.getVictim();
					MOB newMOB=(MOB)CMClass.getMOB("GenMOB");
					int MOBRaceCode=D.myRaceCode;
					if(D.raceName==null) D.setRaceName(mob);
					int level=1;
					while(!D.raceName.equals(D.getRaceName(level,MOBRaceCode)))
						level++;
					level--;
					newMOB.baseEnvStats().setLevel(level);
					levelsRemaining-=level;
					if(levelsRemaining<0) break;
					newMOB.baseCharStats().setMyRace(D.getRace(level,MOBRaceCode));
					String raceName=D.getRaceName(level,MOBRaceCode).toLowerCase();
					String name=Util.startWithAorAn(raceName).toLowerCase();
					newMOB.setName(name);
					newMOB.setDisplayText("a loyal "+raceName+" is here");
					newMOB.setDescription("");
					newMOB.setAlignment(mob.getAlignment());
					Ability A=CMClass.getAbility("Fighter_Rescue");
					A.setProfficiency(100);
					newMOB.addAbility(A);
					newMOB.setVictim(victim);
					newMOB.setLocation(mob.location());
					newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
					newMOB.recoverEnvStats();
					newMOB.baseEnvStats().setArmor(newMOB.charStats().getCurrentClass().getLevelArmor(newMOB));
					newMOB.baseEnvStats().setAttackAdjustment(newMOB.charStats().getCurrentClass().getLevelAttack(newMOB));
					newMOB.baseEnvStats().setDamage(newMOB.charStats().getCurrentClass().getLevelDamage(newMOB));
					newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
					newMOB.setMiscText(newMOB.text());
					newMOB.recoverEnvStats();
					newMOB.recoverCharStats();
					newMOB.recoverMaxState();
					newMOB.resetToMaxState();
					newMOB.bringToLife(mob.location(),true);
					newMOB.setMoney(0);
					if(victim.getVictim()!=newMOB) victim.setVictim(newMOB);
					newMOB.setStartRoom(null);
					int dir=((Integer)choices.elementAt(Dice.roll(1,choices.size(),-1))).intValue();
					newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,"<S-NAME> arrive(s) "+Directions.getFromDirectionName(dir)+" and attack(s) "+victim.name()+"!");
					CommonMsgs.follow(newMOB,mob,true);
					if(newMOB.amFollowing()!=mob)
					{
						newMOB.destroy();
						break;
					}
					beneficialAffect(mob,newMOB,0);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> call(s) for help from <S-HIS-HER> pack, but nothing happens.");

		// return whether it worked
		return success;
	}
}
