package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_AstralProjection extends Chant
{
	public String ID() { return "Chant_AstralProjection"; }
	public String name(){return "Astral Projection";}
	public String displayText(){return "(Astral Projection)";}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Chant_AstralProjection();}
	
	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if((invoker!=null)&&(invoker.soulMate()==mob))
		{
			Session s=invoker.session();
			s.setMob(invoker.soulMate());
			mob.setSession(s);
			invoker.setSession(null);
			mob.tell("^HYour spirit has returned to your body...\n\r\n\r^N");
			invoker.setSoulMate(null);
			invoker.destroy();
			
		}
		super.unInvoke();
		if(mob!=null)
			ExternalPlay.standIfNecessary(mob);
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((unInvoked)&&(canBeUninvoked()))
			return super.tick(ticking,tickID);

		if((tickID==Host.MOB_TICK)
		&&(tickDown!=Integer.MAX_VALUE)
		&&(canBeUninvoked())
		&&(tickDown==1))
			ExternalPlay.postDeath(null,(MOB)affected,null);
		return super.tick(ticking,tickID);
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(affect.amISource((MOB)affected))
		&&(affect.sourceMinor()==Affect.TYP_DEATH))
			unInvoke();
		return super.okAffect(myHost,affect);
	}

	public void peaceAt(MOB mob)
	{
		Room room=mob.location();
		if(room==null) return;
		for(int m=0;m<room.numInhabitants();m++)
		{
			MOB inhab=room.fetchInhabitant(m);
			if((inhab!=null)&&(inhab.getVictim()==mob))
				inhab.setVictim(null);
		}
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SLEEPING);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_HEAR);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_MOVE);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SEE);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SMELL);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SPEAK);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_TASTE);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.soulMate()!=null)
		{
			Ability AS=target.soulMate().fetchAffect(ID());
			if(AS!=null)
			{
				AS.unInvoke();
				return false;
			}
		}
		if(target.fetchAffect("Prop_AstralSpirit")!=null)
		{
			mob.tell("You are already as astral spirit.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) softly.^?");
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			target.makePeace();
			peaceAt(target);
			MOB spirit=(MOB)target.copyOf();
			for(int a=0;a<spirit.numAffects();a++)
			{
				Ability A=spirit.fetchAffect(a);
				if(A.canBeUninvoked()) spirit.delAffect(A);
			}
			while(spirit.inventorySize()>0)
			{
				Item I=spirit.fetchInventory(0);
				if(I!=null) I.destroyThis();
			}
			spirit.setMoney(0);
			mob.location().show(target,null,Affect.MSG_OK_ACTION,"^Z<S-NAME> go(es) limp!^.^?\n\r");
			beneficialAffect(spirit,target,0);
			Ability A=CMClass.getAbility("Prop_AstralSpirit");
			spirit.addNonUninvokableAffect(A);
			Session s=target.session();
			s.setMob(spirit);
			spirit.setSession(s);
			spirit.setSoulMate(target);
			target.setSession(null);
			spirit.recoverCharStats();
			spirit.recoverEnvStats();
			spirit.recoverMaxState();
			mob.location().recoverRoomStats();
		}

		return success;
	}
}