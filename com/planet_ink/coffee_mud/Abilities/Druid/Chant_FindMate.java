package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_FindMate extends Chant
{
	public String ID() { return "Chant_FindMate"; }
	public String name(){ return "Find Mate";}
	private String displayText="(Tracking a mate)";
	public String displayText(){ return displayText;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.OK_OTHERS;}
	public long flags(){return Ability.FLAG_TRACKING;}

	private Vector theTrail=null;
	public int nextDirection=-2;
	public Environmental newInstance(){	return new Chant_FindMate();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID==Host.MOB_TICK)
		{
			if((theTrail==null)
			||(affected == null)
			||(!(affected instanceof MOB)))
				return false;

			MOB mob=(MOB)affected;
			if(mob.location()!=null)
			{
				MOB mate=null;
				for(int i=0;i<mob.location().numInhabitants();i++)
				{
					MOB M=mob.location().fetchInhabitant(i);
					if(isSuitableMate(M,mob))
					{ mate=M; break;}
				}
				if(mate!=null)
				{
					mob.tell("You peer longingly at "+mate.name()+".");
					
					Item I=mob.fetchWornItem(Item.ON_WAIST);
					if(I!=null)	ExternalPlay.remove(mob,I,false);
					I=mob.fetchWornItem(Item.ON_LEGS);
					if(I!=null)	ExternalPlay.remove(mob,I,false);
					
					if((mob.fetchWornItem(Item.ON_WAIST)!=null)
					||(mob.fetchWornItem(Item.ON_LEGS)!=null))
						unInvoke();
					try{
						ExternalPlay.doCommand(mob,Util.parse("MATE \""+mate.name()+"$\""));
					}catch(Exception e){}
					unInvoke();
				}
			}
			
			if(nextDirection==-999)
				return true;

			if(nextDirection==999)
			{
				mob.tell("Your yearning for a mate seems to fade.");
				nextDirection=-2;
				unInvoke();
			}
			else
			if(nextDirection==-1)
			{
				mob.tell("You no longer want to continue.");
				nextDirection=-999;
				unInvoke();
			}
			else
			if(nextDirection>=0)
			{
				mob.tell("You want to continue "+Directions.getDirectionName(nextDirection)+".");
				Room nextRoom=mob.location().getRoomInDir(nextDirection);
				if((nextRoom!=null)&&(nextRoom.getArea()==mob.location().getArea()))
				{
					int dir=nextDirection;
					nextDirection=-2;
					ExternalPlay.move(mob,dir,false,false);
				}
				else
					unInvoke();
			}
		}
		return true;
	}

	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;
		if((affect.amISource(mob))
		&&(affect.amITarget(mob.location()))
		&&(Sense.canBeSeenBy(mob.location(),mob))
		&&(affect.targetMinor()==Affect.TYP_EXAMINESOMETHING))
			nextDirection=SaucerSupport.trackNextDirectionFromHere(theTrail,mob.location(),true);
	}

	public boolean isSuitableMate(MOB mate, MOB forMe)
	{
		if(mate==forMe) return false;
		if((mate==null)||(forMe==null)) return false;
		if(mate.charStats().getStat(CharStats.GENDER)==forMe.charStats().getStat(CharStats.GENDER))
			return false;
		if((mate.charStats().getStat(CharStats.GENDER)!=(int)'M') 
		&&(mate.charStats().getStat(CharStats.GENDER)!=(int)'F'))
			return false;
		String materace=mate.charStats().getMyRace().ID();
		String merace=mate.charStats().getMyRace().ID();
		if(((merace.equals("Human"))
		   ||(materace.equals("Human"))
		   ||(merace.equals(materace)))
		&&(!mate.amWearingSomethingHere(Item.ON_LEGS))
		&&(!mate.amWearingSomethingHere(Item.ON_WAIST))
		&&(Sense.canBeSeenBy(mate,forMe)))
			return true;
		return false;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if((target.charStats().getStat(CharStats.GENDER)!=(int)'M') 
		&&(target.charStats().getStat(CharStats.GENDER)!=(int)'F'))
		{
			mob.tell(target.name()+" is incapable of mating!");
			return false;
		}
		
		Vector V=Sense.flaggedAffects(mob,Ability.FLAG_TRACKING);
		for(int v=0;v<V.size();v++)	((Ability)V.elementAt(v)).unInvoke();
		if(V.size()>0)
		{
			target.tell("You stop tracking.");
			return true;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		Vector rooms=new Vector();
		for(Enumeration r=mob.location().getArea().getMap();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if(R!=null)
			for(int i=0;i<R.numInhabitants();i++)
			{
				MOB M=R.fetchInhabitant(i);
				if(isSuitableMate(M,target))
				{ rooms.addElement(R); break;}
			}
		}

		if(rooms.size()<=0)
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if(Sense.canAccess(mob,R))
				for(int i=0;i<R.numInhabitants();i++)
				{
					MOB M=R.fetchInhabitant(i);
					if(isSuitableMate(M,target))
					{ rooms.addElement(R); break;}
				}
		}

		if(rooms.size()>0)
			theTrail=SaucerSupport.findBastardTheBestWay(mob.location(),rooms,true);

		if((success)&&(theTrail!=null)&&(target!=null))
		{
			theTrail.addElement(mob.location());

			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?null:"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
				Chant_FindMate A=(Chant_FindMate)target.fetchAffect(ID());
				if(A!=null)
				{
					target.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> yearn(s) for a mate!");
					A.makeLongLasting();
					A.nextDirection=SaucerSupport.trackNextDirectionFromHere(theTrail,mob.location(),true);
					target.recoverEnvStats();
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but nothing happen(s).");


		// return whether it worked
		return success;
	}
}