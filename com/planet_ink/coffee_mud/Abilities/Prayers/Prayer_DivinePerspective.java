package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_DivinePerspective extends Prayer
{
	public String ID() { return "Prayer_DivinePerspective"; }
	public String name(){return "Divine Perspective";}
	public String displayText(){return "(Perspective)";}
	public long flags(){return Ability.FLAG_HOLY;}
	public int quality(){return Ability.OK_SELF;}
	protected int overrideMana(){return 100;}
	public Environmental newInstance(){	return new Prayer_DivinePerspective();}
	public String mobName="";
	public boolean noRecurse=false;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			if(invoker!=null)
				invoker.tell("The perspective of '"+mob.name()+"' fades from your mind.");
		super.unInvoke();

	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(noRecurse)return;
		
		if((affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.sourceMinor()==CMMsg.TYP_EXAMINESOMETHING)
		&&(invoker!=null)
		&&(msg.target()!=null)
		&&((((MOB)invoker).location()!=((MOB)affected).location())||(!(msg.target() instanceof Room))))
		{
			noRecurse=true;
			FullMsg newAffect=new FullMsg(invoker,msg.target(),CMMsg.TYP_EXAMINESOMETHING,null);
			msg.target().executeMsg(msg.target(),newAffect);
		}
		else
		if((affected instanceof MOB)
		&&(invoker!=null)
		&&(msg.source() != invoker)
		&&(((MOB)invoker).location()!=((MOB)affected).location())
		&&(msg.othersCode()!=CMMsg.NO_EFFECT)
		&&(msg.othersMessage()!=null))
		{
			noRecurse=true;
			((MOB)invoker).executeMsg(invoker,msg);
		}
		noRecurse=false;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((mob.getWorshipCharID().length()==0)
		||(CMMap.getDeity(mob.getWorshipCharID())==null))
		{
			mob.tell("You must worship a god to use this prayer.");
			return false;
		}
		Deity target=CMMap.getDeity(mob.getWorshipCharID());
		Room newRoom=target.location();

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			mobName=target.Name();
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> invoke(s) the holy perspective of '"+mobName+"'.^?");
			FullMsg msg2=new FullMsg(mob,target,this,affectType(auto),null);
			if((mob.location().okMessage(mob,msg))&&((newRoom==mob.location())||(newRoom.okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				if(newRoom!=mob.location()) newRoom.send(target,msg2);
				beneficialAffect(mob,target,5);
			}

		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to invoke the holy perspective of "+target.Name()+", but fail(s).");


		// return whether it worked
		return success;
	}
}