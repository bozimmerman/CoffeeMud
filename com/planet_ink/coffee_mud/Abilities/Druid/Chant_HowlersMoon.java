package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_HowlersMoon extends Chant
{
	public String ID() { return "Chant_HowlersMoon"; }
	public String name(){ return "Howlers Moon";}
	public String displayText(){return "(Howlers Moon)";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return CAN_MOBS|CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	private int ticksTicked=0;
	private int fromDir=-1;
	public Environmental newInstance(){	return new Chant_HowlersMoon();}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
		{
			if(affected instanceof Room)
				((Room)affected).showHappens(Affect.MSG_OK_VISUAL,"The howlers moon sets.");
			super.unInvoke();
			return;
		}

		MOB mob=(MOB)affected;
		if(mob.amFollowing()==null)
			CoffeeUtensils.wanderAway(mob,true,false);
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null)&&(mob.amFollowing()==null))
		{
			mob.tell("You are no longer under the howlers moon.");
			if(mob.amDead()) mob.setLocation(null);
			mob.destroy();
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(affected==null) return false;
		if(affected instanceof Room)
		{
			Room room=(Room)affected;
			if(!Chant_BlueMoon.moonInSky(room,this))
				unInvoke();
			
			if((++ticksTicked)<20) return true;
			int numWolfs=0;
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB M=room.fetchInhabitant(i);
				if((M!=null)
				&&(M.isMonster())
				&&(M.fetchAffect(ID())!=null))
					numWolfs++;
			}
			if((numWolfs>5)||((invoker()!=null)&&(numWolfs>invoker().envStats().level()/10)))
				 return true;
			if(fromDir<0) 
			{
				Vector choices=fillChoices(room);
				if(choices.size()==0)
					return true;
				else
					fromDir=((Integer)choices.elementAt(Dice.roll(1,choices.size(),-1))).intValue();
			}
			if(fromDir>=0)
			{
				ticksTicked=0;
				int level=CMAble.lowestQualifyingLevel(ID())+5;
				if(invoker()!=null) level=invoker().envStats().level()+5;
				MOB target = determineMonster(invoker(),level);
				Room newRoom=room.getRoomInDir(fromDir);
				int opDir=Directions.getOpDirectionCode(fromDir);
				target.bringToLife(newRoom,true);
				target.location().showOthers(target,null,Affect.MSG_OK_ACTION,"<S-NAME> appears!");
				newRoom.recoverRoomStats();
				target.setStartRoom(null);
				ExternalPlay.move(target,opDir,false,false);
				if(target.location()==room)
				{
					int d=Dice.rollPercentage();
					if((d<33)&&(invoker()!=null)&&(invoker().location()==room))
					{
						ExternalPlay.follow(target,invoker(),true);
						beneficialAffect(invoker(),target,0);
						if(target.amFollowing()!=invoker())
							target.setVictim(invoker());
					}
					else
					if((d>66)&&(invoker()!=null)&&(invoker().location()==room))
						target.setVictim(invoker());
					beneficialAffect(target,target,Integer.MAX_VALUE/2);
				}
				else
				{
					if(target.amDead()) target.setLocation(null);
					target.destroy();
				}
			}
		}
		return true;
	}

	private Vector fillChoices(Room R)
	{
		Vector choices=new Vector();
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room room=R.getRoomInDir(d);
			Exit exit=R.getExitInDir(d);
			Exit opExit=R.getReverseExit(d);
			if((room!=null)
			&&((room.domainType()&Room.INDOORS)==0)
			&&((room.domainType()&Room.DOMAIN_OUTDOORS_AIR)==0)
			&&((exit!=null)&&(exit.isOpen()))
			&&(opExit!=null)&&(opExit.isOpen()))
				choices.addElement(new Integer(d));
		}
		return choices;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Room target=mob.location();
		if(target==null) return false;
		if(!Chant_BlueMoon.moonInSky(mob.location(),null))
		{
			mob.tell("You must be able to see the moon for this magic to work.");
			return false;
		}
		if(target.fetchAffect(ID())!=null)
		{
			mob.tell("This place is already under the howler's moon.");
			return false;
		}

		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		Vector choices=fillChoices(mob.location());
		fromDir=-1;
		if(choices.size()==0)
		{
			mob.tell("You must be further outdoors to summon an animal.");
			return false;
		}
		fromDir=((Integer)choices.elementAt(Dice.roll(1,choices.size(),-1))).intValue();
		
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
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the sky.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					mob.location().showHappens(Affect.MSG_OK_VISUAL,"The Howler's Moon Rises!");
					ticksTicked=0;
					beneficialAffect(mob,target,0);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) to the sky, but the magic fades.");
		// return whether it worked
		return success;
	}

	public MOB determineMonster(MOB caster, int level)
	{
		MOB newMOB=(MOB)CMClass.getMOB("GenMob");
		newMOB.baseEnvStats().setAbility(0);
		newMOB.baseEnvStats().setLevel(level);
		newMOB.setAlignment(500);
		newMOB.baseEnvStats().setWeight(350);
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("Wolf"));
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		newMOB.baseCharStats().setStat(CharStats.GENDER,(int)'M');
		newMOB.recoverEnvStats();
		newMOB.recoverCharStats();
		newMOB.baseEnvStats().setArmor(newMOB.baseCharStats().getCurrentClass().getLevelArmor(newMOB));
		newMOB.baseEnvStats().setAttackAdjustment(newMOB.baseCharStats().getCurrentClass().getLevelAttack(newMOB));
		newMOB.baseEnvStats().setDamage(newMOB.baseCharStats().getCurrentClass().getLevelDamage(newMOB));
		newMOB.setName("a ferocious wolf");
		newMOB.setDisplayText("a huge, ferocious wolf is here");
		newMOB.setDescription("Dark black fur, always standing on end surrounds its muscular body.  The eyes are deep red, and his teeth are bared, snarling at you.");
		Behavior B=CMClass.getBehavior("CorpseEater");
		if(B!=null) newMOB.addBehavior(B);
		B=CMClass.getBehavior("Emoter");
		if(B!=null){
			B.setParms("broadcast sound min=3 max=10 chance=80;howls at the moon.");
			newMOB.addBehavior(B);
		}
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.setStartRoom(null);
		newMOB.text();
		return(newMOB);
	}
}