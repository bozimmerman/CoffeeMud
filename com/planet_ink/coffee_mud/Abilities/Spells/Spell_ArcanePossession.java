package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_ArcanePossession extends Spell
{
	public String ID() { return "Spell_ArcanePossession"; }
	public String name(){return "Arcane Possession";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Spell_ArcanePossession();}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_ABJURATION;}
	private MOB owner=null;
	
	public boolean okAffect(Environmental host, Affect msg)
	{
		if(!super.okAffect(host,msg))
			return false;
		if((msg.tool()==affected)
		&&(msg.sourceMinor()==Affect.TYP_SELL))
		{ 
			unInvoke(); 
			if(affected!=null)	affected.delAffect(this);
		}
		else
		if(msg.amITarget(affected)
		&&(owner!=null)
		&&((msg.targetMinor()==Affect.TYP_WEAR)
		   ||(msg.targetMinor()==Affect.TYP_HOLD)
		   ||(msg.targetMinor()==Affect.TYP_WIELD))
		&&(msg.source()!=owner))
		{
			msg.source().location().show(msg.source(),null,affected,Affect.MSG_OK_ACTION,"<O-NAME> flashes and flies out of <S-HIS-HER> hands!");
			ExternalPlay.drop(msg.source(),affected,true);
			return false;
		}
		return true;
	}
	
	public void affect(Environmental host, Affect msg)
	{
		super.affect(host,msg);
		if((affected instanceof Item)&&(text().length()>0))
		{
			Item I=(Item)affected;
			if((owner==null)&&(I.owner()!=null)
			&&(I.owner() instanceof MOB)
			&&(I.owner().Name().equals(text())))
			    owner=(MOB)I.owner();
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,null,givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> hold(s) a <T-NAMESELF> tightly and cast(s) a spell.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.MSG_OK_VISUAL,"<T-NAME> glows slightly!");
				setMiscText(mob.Name());
				beneficialAffect(mob,target,0);
				target.recoverEnvStats();
				mob.recoverEnvStats();
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> hold(s) <T-NAMESELF> tightly and whisper(s), but fail(s) to cast a spell.");


		// return whether it worked
		return success;
	}
}