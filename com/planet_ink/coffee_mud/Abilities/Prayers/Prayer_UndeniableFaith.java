package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_UndeniableFaith extends Prayer
{
	public String ID() { return "Prayer_UndeniableFaith"; }
	public String name(){ return "Undeniable Faith";}
	public int quality(){ return OK_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY|Ability.FLAG_CHARMING;}
	public Environmental newInstance(){	return new Prayer_UndeniableFaith();}
	protected int overrideMana(){return 100;}
	private String godName="";
	private static DVector convertStack=new DVector(2);
		
	public void unInvoke()
	{
		MOB M=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
			M.tell("Your compelled faith is finally subsided.");
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((affected==null)||(!(affected instanceof MOB)))
		   return true;
		MOB M=(MOB)affected;
		if(M.location()!=null)
		{
			if((!M.getWorshipCharID().equals(godName))
			&&(godName.length()>0))
			{
				Deity D=CMMap.getDeity(godName);
				if(M.getWorshipCharID().length()>0)
				{
					Deity D2=CMMap.getDeity(M.getWorshipCharID());
					if(D2!=null)
					{
						FullMsg msg2=new FullMsg(M,D2,this,CMMsg.MSG_REBUKE,null);
						if(M.location().okMessage(M,msg2))
							M.location().send(M,msg2);
					}
				}
				FullMsg msg2=new FullMsg(M,D,this,CMMsg.MSG_SERVE,null);
				if(M.location().okMessage(M,msg2))
				{
					M.location().send(M,msg2);
					M.setWorshipCharID(godName);
				}
			}
		}
		return true;
	}
	
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if((affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.sourceMinor()==CMMsg.TYP_REBUKE)
		&&(msg.target()!=null)
		&&((msg.target()==invoker())||(msg.target().Name().equals(godName))))
		{
			msg.source().tell("Your faith is too undeniable.");
			return false;
		}
		return super.okMessage(host,msg);
	}
	
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if((mob.getWorshipCharID().length()==0)
		||(CMMap.getDeity(mob.getWorshipCharID())==null))
		{
			if(!auto) mob.tell("You must worship a god to use this prayer.");
			return false;
		}
		Deity D=CMMap.getDeity(mob.getWorshipCharID());
		if((target.getWorshipCharID().length()>0)
		&&(CMMap.getDeity(target.getWorshipCharID())!=null))
		{
			if(!auto) mob.tell(target.name()+" worships "+target.getWorshipCharID()+", and may not be converted with this prayer.");
			return false;
		}
		if((Sense.isAnimalIntelligence(target)||Sense.isGolem(target)||(D==null)))
		{
			if(!auto) mob.tell(target.name()+" can not be converted with this prayer.");
			return false;
		}
		if(!auto)
		{
			if(convertStack.contains(target))
			{
				Long L=(Long)convertStack.elementAt(convertStack.getIndex(target),2);
				if((System.currentTimeMillis()-L.longValue())>MudHost.TIME_MILIS_PER_MUDHOUR*5)
					convertStack.removeElement(target);
			}
			if(convertStack.contains(target))
			{
				mob.tell(target.name()+" must wait to be undeniably faithful again.");
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=target.envStats().level()-mob.envStats().level();
		if(levelDiff<0) levelDiff=0;
		boolean success=profficiencyCheck(mob,-(levelDiff*25),auto);
		int type=affectType(auto);
		int mal=CMMsg.MASK_MALICIOUS;
		if(auto){ type=Util.unsetb(type,CMMsg.MASK_MALICIOUS); mal=0;}
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,type,auto?"":"^S<S-NAME> "+prayWord(mob)+" for <T-NAMESELF> to BELIEVE!^?");
			FullMsg msg2=new FullMsg(target,D,this,CMMsg.MSG_SERVE,"<S-NAME> BELIEVE(S) !!!");
			FullMsg msg3=new FullMsg(mob,target,this,CMMsg.MSK_CAST_VERBAL|mal|CMMsg.TYP_MIND|(auto?CMMsg.MASK_GENERAL:0),null);
			if((mob.location().okMessage(mob,msg))
			&&(mob.location().okMessage(mob,msg3))
			&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg3);
				if((msg.value()<=0)&&(msg3.value()<=0))
				{
					target.location().send(target,msg2);
					target.setWorshipCharID(godName);
					if(mob!=target)
						MUDFight.postExperience(mob,target,null,25,false);
					godName=mob.getWorshipCharID();
					beneficialAffect(mob,target,(int)CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY));
					convertStack.addElement(target,new Long(System.currentTimeMillis()));
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,auto?"":"<S-NAME> "+prayWord(mob)+" for <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
