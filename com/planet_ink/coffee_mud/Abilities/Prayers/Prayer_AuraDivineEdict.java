package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_AuraDivineEdict extends Prayer
{
	public String ID() { return "Prayer_AuraDivineEdict"; }
	public String name(){ return "Aura of the Divine Edict";}
	public String displayText(){ return "(Edict Aura)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){ return BENEFICIAL_SELF;}
	protected int overrideMana(){return Integer.MAX_VALUE;}
	public long flags(){return Ability.FLAG_HOLY;}
	public Environmental newInstance(){	return new Prayer_AuraDivineEdict();	}
	private String godName="the gods";
	private boolean noRecurse=false;


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if((canBeUninvoked())&&(mob.location()!=null)&&(!mob.amDead()))
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"The divine edict aura around <S-NAME> fades.");
	}

	protected String getMsgFromAffect(String msg)
	{
		if(msg==null) return null;
		int start=msg.indexOf("'");
		int end=msg.lastIndexOf("'");
		if((start>0)&&(end>start))
			return msg.substring(start+1,end);
		return null;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected==null)||(!(affected instanceof MOB))||(noRecurse))
			return true;

		if(Util.bset(msg.sourceCode(),CMMsg.MASK_MALICIOUS)
		   ||Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		{
			msg.source().tell(godName+" DEMANDS NO FIGHTING!");
			msg.source().makePeace();
			return false;
		}
		else
		if((msg.source()==invoker())
		&&(msg.targetMinor()==CMMsg.TYP_SPEAK)
		&&(msg.target() instanceof MOB)
		&&(msg.target().envStats().level()<invoker().envStats().level())
		&&(getMsgFromAffect(msg.sourceMessage().toUpperCase()).equals(getMsgFromAffect(msg.sourceMessage()))))
		{
			noRecurse=true;
			String oldLiege=((MOB)msg.target()).getLiegeID();
			((MOB)msg.target()).setLiegeID(msg.source().Name());
			msg.source().doCommand(Util.parse("ORDER \""+msg.target().Name()+"\" "+getMsgFromAffect(msg.sourceMessage())));
			((MOB)msg.target()).setLiegeID(oldLiege);
			noRecurse=false;
			return false;
		}
		noRecurse=false;
		return true;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof Room)))
			return super.tick(ticking,tickID);

		if(!super.tick(ticking,tickID))
			return false;
		if(invoker()==null) return true;

		Room R=invoker().location();
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M=R.fetchInhabitant(i);
			if((M!=null)&&(M.isInCombat()))
			{
				M.tell(invoker().getWorshipCharID().toUpperCase()+" DEMANDS NO FIGHTING!");
				M.makePeace();
			}
		}
		return true;
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if(target==null) return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("The aura of the divine edict is already with you.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for the aura of the divine edict.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				godName="THE GODS";
				if(mob.getWorshipCharID().length()>0)
					godName=mob.getWorshipCharID().toUpperCase();
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for an aura of divine edict, but <S-HIS-HER> plea is not answered.");


		// return whether it worked
		return success;
	}
}