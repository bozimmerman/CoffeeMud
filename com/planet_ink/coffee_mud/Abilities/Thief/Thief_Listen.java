package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Listen extends ThiefSkill
{
	public String ID() { return "Thief_Listen"; }
	public String name(){ return "Listen";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return Ability.CAN_ROOMS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"LISTEN"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_Listen();	}

	private Room sourceRoom=null;
	private Room room=null;
	private String lastSaid="";
	
	public void affect(Affect msg)
	{
		super.affect(msg);
		if((affected!=null)
		&&(affected instanceof Room)
		&&(invoker()!=null)
		&&(invoker().location()!=null)
		&&(sourceRoom!=null)
		&&(!invoker().isInCombat())
		&&(invoker().location()==sourceRoom))
		{
			if((msg.sourceMinor()==Affect.TYP_SPEAK)
			&&(msg.othersCode()==Affect.NO_EFFECT)
			&&(msg.othersMessage()==null)
			&&(msg.sourceMessage()!=null)
			&&(!msg.amISource(invoker()))
			&&(!msg.amITarget(invoker()))
			&&(!lastSaid.equals(msg.sourceMessage())))
			{
				lastSaid=msg.sourceMessage();
				invoker().tell(msg.source(),msg.target(),msg.sourceMessage());
			}
		}
		else
			unInvoke();
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		String whom=Util.combine(commands,0);
		int dirCode=Directions.getGoodDirectionCode(whom);
		if(!Sense.canHear(mob))
		{
			mob.tell("You don't hear anything.");
			return false;
		}

		if(room!=null)
		for(int a=room.numAffects()-1;a>=0;a--)
		{
			Ability A=room.fetchAffect(a);
			if((A.ID().equals(ID()))&&(invoker()==mob))
				A.unInvoke();
		}
		room=null;
		if(dirCode<0)
			room=mob.location();
		else
		{
			if((mob.location().getRoomInDir(dirCode)==null)||(mob.location().getExitInDir(dirCode)==null))
			{
				mob.tell("Listen which direction?");
				return false;
			}
			room=mob.location().getRoomInDir(dirCode);
			if((room.domainType()&Room.INDOORS)==0)
			{
				mob.tell("You can only listen indoors.");
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=false;
		FullMsg msg=new FullMsg(mob,null,this,auto?Affect.MSG_OK_ACTION:(Affect.MSG_DELICATE_HANDS_ACT),Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,"<S-NAME> listen(s)"+((dirCode<0)?"":" "+Directions.getDirectionName(dirCode))+".");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			success=profficiencyCheck(0,auto);
			int numberHeard=0;
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB inhab=room.fetchInhabitant(i);
				if((inhab!=null)&&(!Sense.isSneaking(inhab))&&(!Sense.isHidden(inhab))&&(inhab!=mob))
					numberHeard++;
			}
			if((success)&&(numberHeard>0))
			{
				if((profficiency()>50)||(room==mob.location()))
				{
					mob.tell("You definitely hear "+numberHeard+" creature(s).");
					if(profficiency()>((room==mob.location())?50:75))
					{
						sourceRoom=mob.location();
						beneficialAffect(mob,room,((room==mob.location())?0:10));
					}
				}
				else
					mob.tell("You definitely hear something.");
			}
			else
				mob.tell("You don't hear anything.");
		}
		return success;
	}

}