package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Lycanthropy extends Disease
{
	public String ID() { return "Disease_Lycanthropy"; }
	public String name(){ return "Lycanthropy";}
	public String displayText(){ return "(Lycanthropy)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Lycanthropy();}

	protected int DISEASE_TICKS(){return 9999999;}
	protected int DISEASE_DELAY(){return 50;}
	protected String DISEASE_DONE(){return "Your lycanthropy is cured.";}
	protected String DISEASE_START(){return "^G<S-NAME> feel(s) different.^?";}
	protected String DISEASE_AFFECT(){return "";}
	protected boolean DISEASE_STD(){return false;}
	protected boolean changed=false;
	protected boolean DISEASE_TOUCHSPREAD(){return changed;}
	protected Vector deathTrail=null;
	protected Race theRace=null;
	protected Race lycanRace(){
		if(!changed) return null;
		if(theRace==null) theRace=CMClass.getRace("WereWolf");
		return theRace;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(lycanRace()!=null)
		{
			if(affected.name().indexOf(" ")>0)
				affectableStats.setReplacementName("a "+lycanRace().name()+" called "+affected.name());
			else
				affectableStats.setReplacementName(affected.name()+" the "+lycanRace().name());
			lycanRace().setHeightWeight(affectableStats,'M');
		}
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(lycanRace()!=null)
			affectableStats.setMyRace(lycanRace());
	}
	
	public MOB victimHere(Room room, MOB mob)
	{
		if(room==null) return null;
		if(mob==null) return null;
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB M=room.fetchInhabitant(i);
			if((M!=null)
			&&(M!=mob)
			&&(M.getAlignment()>350)
			&&(mob.mayIFight(M))
			&&(M.envStats().level()<(mob.envStats().level()+5)))
				return M;
		}
		return null;
	}
	
	private boolean findVictim(MOB mob, Room room, Vector rooms, int depth)
	{
		if(depth>5) 
			return false;
		if(victimHere(room,mob)!=null)
		{
			rooms.addElement(room);
			return true;
		}
		else
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room R=room.getRoomInDir(d);
			Exit E=room.getExitInDir(d);
			if((R!=null)&&(E!=null)&&(E.isOpen()))
			{
				if(findVictim(mob,R,rooms,depth+1))
				{
					rooms.addElement(R);
					return true;
				}
			}
		}
		return false;
	}
	
	public void tickLycanthropically(MOB mob)
	{
		if(mob==null) return;
		if(mob.location()==null) return;
		if(mob.isInCombat()) return;
		
		if((Dice.rollPercentage()<15)
		&&((mob.location().domainType()&Room.INDOORS)>0))
			mob.location().show(mob,null,Affect.MSG_NOISE,"<S-NAME> howl(s) at the moon! ARROOOOOOOO!!!!");
		// time to tick lycanthropically
		MOB M=victimHere(mob.location(),mob);
		if(M!=null)
		{
			deathTrail=null;
			ExternalPlay.postAttack(mob,M,mob.fetchWieldedItem());
			return;
		}
		if((deathTrail!=null)&&(!deathTrail.contains(mob.location())))
		   deathTrail=null;
		if(deathTrail==null)
		{
			Vector rooms=new Vector();
			if((findVictim(mob,mob.location(),rooms,0))&&(rooms.size()>0))
			{
				deathTrail=ExternalPlay.findBastardTheBestWay(mob.location(),rooms,true);
				if(deathTrail!=null)
					deathTrail.addElement(mob.location());
			}
		}
		if(deathTrail!=null)
		{
			int nextDirection=ExternalPlay.trackNextDirectionFromHere(deathTrail,mob.location(),true);
			if((nextDirection==999)
			||(nextDirection==-1))
				deathTrail=null;
			else
			if(nextDirection>=0)
			{
				Room nextRoom=mob.location().getRoomInDir(nextDirection);
				if((nextRoom!=null)
				&&((nextRoom.getArea()==mob.location().getArea()))||(!mob.isMonster()))
				{
					if(!ExternalPlay.move(mob,nextDirection,false,false))
						deathTrail=null;
					else
					if(Dice.rollPercentage()<15)
						mob.location().show(mob,null,Affect.MSG_NOISE,"<S-NAME> sniff(s) at the air.");
						
				}
				else
					deathTrail=null;
			}
		}
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if((affected==null)||(invoker==null)) return false;
		MOB mob=(MOB)affected;
		if(!changed)
		{
			if(mob.location()==null) return true;
			Area A=mob.location().getArea();
			if(((A.getTODCode()==Area.TIME_DUSK)||(A.getTODCode()==Area.TIME_NIGHT))
			&&(A.getMoonPhase()==4))
			{
				changed=true;
				mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"<S-NAME> turn(s) into a "+lycanRace().name()+"!");
				mob.recoverCharStats();
				mob.recoverEnvStats();
				mob.recoverMaxState();
				mob.location().recoverRoomStats();
			}
		}
		else
		{
			if(mob.location()==null) return true;
			Area A=mob.location().getArea();
			if(((A.getTODCode()!=Area.TIME_DUSK)&&(A.getTODCode()!=Area.TIME_NIGHT))
			||(A.getMoonPhase()!=4))
			{
				changed=false;
				mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"<S-NAME> revert(s) to normal.");
				mob.recoverCharStats();
				mob.recoverEnvStats();
				mob.recoverMaxState();
				mob.location().recoverRoomStats();
				return true;
			}
			tickLycanthropically(mob);
		}
		return true;
	}
}
