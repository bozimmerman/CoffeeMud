package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Boomerang extends Spell
{
	public String ID() { return "Spell_Boomerang"; }
	public String name(){return "Boomerang";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Spell_Boomerang();}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_CONJURATION;}
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
		return true;
	}
	
	public void affect(Environmental host, Affect msg)
	{
		super.affect(host,msg);
		if((msg.targetMinor()==Affect.TYP_GET)
		&&(msg.amITarget(affected))
		&&(text().length()==0))
		{
			setMiscText(msg.source().Name());
			msg.source().tell(affected.name()+" will now boomerang back to you.");
		}
		if((affected instanceof Item)&&(text().length()>0))
		{
			Item I=(Item)affected;
			if((owner==null)&&(I.owner()!=null)
			&&(I.owner() instanceof MOB)
			&&(I.owner().Name().equals(text())))
				   owner=(MOB)I.owner();
			if((owner!=null)&&(I.owner()!=owner))
			{
				if((msg.sourceMinor()==Affect.TYP_DROP)||(msg.target()==affected))
					msg.addTrailerMsg(new FullMsg(owner,null,Affect.NO_EFFECT,null));
				else
				{
					owner.tell(I.name()+" boomerangs back into your inventory!");
					I.unWear();
					I.setContainer(null);
					owner.giveItem(I);
				}
			}
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> point(s) at <T-NAMESELF> and cast(s) a spell.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.MSG_OK_VISUAL,"<T-NAME> glows slightly!");
				mob.tell(target.name()+" will now await someone to GET it before acknowleding its new master.");
				setMiscText("");
				beneficialAffect(mob,target,0);
				target.recoverEnvStats();
				mob.recoverEnvStats();
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> point(s) at <T-NAMESELF> and whisper(s), but fail(s) to cast a spell.");


		// return whether it worked
		return success;
	}
}