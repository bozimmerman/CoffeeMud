package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Comprehension extends ThiefSkill
{
	public String ID() { return "Thief_Comprehension"; }
	public String name(){return "Linguistic Comprehension";}
	public String displayText(){return "(Lang. Comprehension)";}
	public int quality(){return OK_SELF;};
	protected int canAffectCode(){return CAN_MOBS;}
	private static final String[] triggerStrings = {"COMPREHEND","COMPREHENSION"};
	public String[] triggerStrings(){return triggerStrings;}
	protected boolean disregardsArmorCheck(MOB mob){return true;}
	private Vector queue=new Vector();


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("You are no longer comprehending languages.");
	}

	protected String getMsgFromAffect(String msg)
	{
		if(msg==null) return null;
		int start=msg.indexOf("'");
		int end=msg.lastIndexOf("'");
		if((start>0)&&(end>start))
			return msg.substring(start+1,end).trim();
		return null;
	}
	protected String subStitute(String affmsg, String msg)
	{
		if(affmsg==null) return null;
		int start=affmsg.indexOf("'");
		int end=affmsg.lastIndexOf("'");
		if((start>0)&&(end>start))
			return affmsg.substring(0,start+1)+msg+affmsg.substring(end);
		return affmsg;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((queue.size()>0)&&(affected instanceof MOB)&&(profficiencyCheck((MOB)affected,0,false)))
		{
			FullMsg msg=(FullMsg)queue.firstElement();
			queue.removeElementAt(0);
			MOB mob=(MOB)affected;
			if((mob.location()!=null)&&(mob.location().okMessage(mob,msg)))
				mob.location().send(mob,msg);
		}
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected instanceof MOB)
		&&(!msg.amISource((MOB)affected))
		&&((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
		   ||(Util.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL)))
		&&(msg.tool() !=null)
		&&(msg.sourceMessage()!=null)
		&&(msg.tool() instanceof Ability)
		&&(((Ability)msg.tool()).classificationCode()==Ability.LANGUAGE)
		&&(((MOB)affected).fetchEffect(msg.tool().ID())==null))
		{
			String str=this.getMsgFromAffect(msg.sourceMessage());
			if(str!=null)
			{
				if(Util.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL))
					queue.addElement(new FullMsg(msg.source(),null,null,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,msg.othersCode(),this.subStitute(msg.othersMessage(),str)+" (translated from "+ID()+")"));
				else
				if(msg.amITarget(affected)&&(msg.targetMessage()!=null))
					queue.addElement(new FullMsg(msg.source(),(MOB)affected,null,CMMsg.NO_EFFECT,msg.targetCode(),CMMsg.NO_EFFECT,this.subStitute(msg.targetMessage(),str)+" (translated from "+((Ability)msg.tool()).ID()+")"));
				else
				if((msg.othersMessage()!=null)&&(msg.othersMessage().indexOf("'")>0))
				{
					String otherMes=msg.othersMessage();
					if(msg.target()!=null)
						otherMes=CoffeeFilter.fullOutFilter(((MOB)affected).session(),(MOB)affected,msg.source(),msg.target(),msg.tool(),otherMes,false);
					queue.addElement(new FullMsg(msg.source(),(MOB)affected,null,CMMsg.NO_EFFECT,msg.othersCode(),CMMsg.NO_EFFECT,this.subStitute(otherMes,str)+" (translated from "+ID()+")"));
				}
			}
		}
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if(target==null) return false;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("You already have comprehension.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,auto?"<T-NAME> feel(s) more comprehending.":"^S<S-NAME> listen(s) with the intense comprehension.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to listen with comprehension, but fail(s) miserably.");

		// return whether it worked
		return success;
	}
}