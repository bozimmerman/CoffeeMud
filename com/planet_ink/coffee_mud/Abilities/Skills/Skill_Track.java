package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Track extends StdAbility
{
	public String ID() { return "Skill_Track"; }
	public String name(){ return "Tracking";}
	private String displayText="(Tracking)";
	public String displayText(){ return displayText;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS|CAN_ROOMS;}
	public int quality(){return Ability.OK_OTHERS;}
	private static final String[] triggerStrings = {"TRACKTO"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public long flags(){return Ability.FLAG_TRACKING;}
	private Hashtable cachedPaths=new Hashtable();
	private int cacheCode=-1;
	public int abilityCode(){return cacheCode;}
	public void setAbilityCode(int newCode){cacheCode=newCode;}
	public int usageType(){return USAGE_MOVEMENT;}

	private Vector theTrail=null;
	public int nextDirection=-2;
	public Environmental newInstance(){	return new Skill_Track();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID==MudHost.TICK_MOB)
		{
			if(nextDirection==-999)
				return true;

			if((theTrail==null)
			||(affected == null)
			||(!(affected instanceof MOB)))
				return false;

			MOB mob=(MOB)affected;

			if(nextDirection==999)
			{
				mob.tell("The trail seems to pause here.");
				nextDirection=-2;
				unInvoke();
			}
			else
			if(nextDirection==-1)
			{
				mob.tell("The trail dries up here.");
				nextDirection=-999;
				unInvoke();
			}
			else
			if(nextDirection>=0)
			{
				mob.tell("The trail seems to continue "+Directions.getDirectionName(nextDirection)+".");
				if((mob.isMonster())&&(mob.location()!=null))
				{
					Room oldRoom=mob.location();
					Room nextRoom=oldRoom.getRoomInDir(nextDirection);
					Exit nextExit=oldRoom.getExitInDir(nextDirection);
					int opDirection=Directions.getOpDirectionCode(nextDirection);
					if((nextRoom!=null)&&(nextExit!=null))
					{
						boolean reclose=false;
						boolean relock=false;
						// handle doors!
						if(nextExit.hasADoor()&&(!nextExit.isOpen()))
						{
							if((nextExit.hasALock())&&(nextExit.isLocked()))
							{
								FullMsg msg=new FullMsg(mob,nextExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
								if(oldRoom.okMessage(mob,msg))
								{
									relock=true;
									msg=new FullMsg(mob,nextExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_UNLOCK,CMMsg.MSG_OK_VISUAL,"<S-NAME> unlock(s) <T-NAMESELF>.");
									CoffeeUtensils.roomAffectFully(msg,oldRoom,nextDirection);
								}
							}
							FullMsg msg=new FullMsg(mob,nextExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
							if(oldRoom.okMessage(mob,msg))
							{
								reclose=true;
								msg=new FullMsg(mob,nextExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OPEN,CMMsg.MSG_OK_VISUAL,"<S-NAME> "+nextExit.openWord()+"(s) <T-NAMESELF>.");
								CoffeeUtensils.roomAffectFully(msg,oldRoom,nextDirection);
							}
						}
						if(!nextExit.isOpen())
							unInvoke();
						else
						{
							int dir=nextDirection;
							nextDirection=-2;
							MUDTracker.move(mob,dir,false,false);
							if((reclose)&&(mob.location()==nextRoom))
							{
								Exit opExit=nextRoom.getExitInDir(opDirection);
								if((opExit!=null)
								&&(opExit.hasADoor())
								&&(opExit.isOpen()))
								{
									FullMsg msg=new FullMsg(mob,opExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
									if(nextRoom.okMessage(mob,msg))
									{
										msg=new FullMsg(mob,opExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_CLOSE,CMMsg.MSG_OK_VISUAL,"<S-NAME> "+nextExit.closeWord()+"(s) <T-NAMESELF>.");
										CoffeeUtensils.roomAffectFully(msg,nextRoom,opDirection);
									}
									if((opExit.hasALock())&&(relock))
									{
										msg=new FullMsg(mob,opExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
										if(nextRoom.okMessage(mob,msg))
										{
											msg=new FullMsg(mob,opExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_LOCK,CMMsg.MSG_OK_VISUAL,"<S-NAME> lock(s) <T-NAMESELF>.");
											CoffeeUtensils.roomAffectFully(msg,nextRoom,opDirection);
										}
									}
								}
							}
						}
					}
					else
						unInvoke();
				}
				else
					nextDirection=-2;
			}

		}
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(msg.amITarget(mob.location()))
		&&(msg.targetMinor()==CMMsg.TYP_EXAMINESOMETHING))
			nextDirection=MUDTracker.trackNextDirectionFromHere(theTrail,mob.location(),true);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((!Sense.aliveAwakeMobile(mob,false))||(mob.location()==null))
			return false;
		Room thisRoom=mob.location();

		Vector V=Sense.flaggedAffects(mob,Ability.FLAG_TRACKING);
		for(int v=0;v<V.size();v++)	((Ability)V.elementAt(v)).unInvoke();
		if(V.size()>0)
		{
			mob.tell("You stop tracking.");
			if(commands.size()==0) return true;
		}

		theTrail=null;
		nextDirection=-2;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		String mobName=Util.combine(commands,0);
		if((givenTarget==null)
		&&(mobName.length()==0))
		{
			mob.tell("Track whom?");
			return false;
		}
		
		if(givenTarget==null)
			givenTarget=CMMap.getRoom(mobName);
		
		if(givenTarget==null)
			givenTarget=CMMap.getArea(mobName);

		if((givenTarget==null)
		&&(thisRoom.fetchInhabitant(mobName)!=null))
		{
			mob.tell("Try 'look'.");
			return false;
		}

		Vector rooms=new Vector();
		if(givenTarget instanceof Area)
			rooms.addElement(((Area)givenTarget).getRandomRoom());
		else
		if(givenTarget instanceof Room)
			rooms.addElement(givenTarget);
		else
		if((givenTarget instanceof MOB)&&(((MOB)givenTarget).location()!=null))
			rooms.addElement(((MOB)givenTarget).location());
		else
		if(mobName.length()>0)
		{
			Room R=CMMap.getRoom(mobName);
			if(R!=null) rooms.addElement(R);
		}

		if(rooms.size()<=0)
		for(Enumeration r=thisRoom.getArea().getMap();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if(R.fetchInhabitant(mobName)!=null)
				rooms.addElement(R);
		}

		if(rooms.size()<=0)
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if(R.fetchInhabitant(mobName)!=null)
				rooms.addElement(R);
		}
		
		boolean success=profficiencyCheck(mob,0,auto);
		
		if(rooms.size()>0)
		{
			theTrail=null;
			if((cacheCode==1)&&(rooms.size()==1))
				theTrail=(Vector)cachedPaths.get(CMMap.getExtendedRoomID(thisRoom)+"->"+CMMap.getExtendedRoomID((Room)rooms.firstElement()));
			if(theTrail==null)
				theTrail=MUDTracker.findBastardTheBestWay(thisRoom,rooms,false);
			if((cacheCode==1)&&(rooms.size()==1)&&(theTrail!=null))
				cachedPaths.put(CMMap.getExtendedRoomID(thisRoom)+"->"+CMMap.getExtendedRoomID((Room)rooms.firstElement()),theTrail);
		}

		if((success)&&(theTrail!=null))
		{
			theTrail.addElement(thisRoom);

			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,mob.isMonster()?null:"<S-NAME> begin(s) to track.");
			if(thisRoom.okMessage(mob,msg))
			{
				thisRoom.send(mob,msg);
				invoker=mob;
				Skill_Track newOne=(Skill_Track)copyOf();
				if(mob.fetchEffect(newOne.ID())==null)
					mob.addEffect(newOne);
				mob.recoverEnvStats();
				newOne.nextDirection=MUDTracker.trackNextDirectionFromHere(theTrail,thisRoom,false);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to track, but can't find the trail.");


		// return whether it worked
		return success;
	}
}