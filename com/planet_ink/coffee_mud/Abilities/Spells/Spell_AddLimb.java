package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_AddLimb extends Spell
{
	public String ID() { return "Spell_AddLimb"; }
	public String name(){return "Add Limb";}
	public String displayText(){return "(Add Limb)";}
	public int quality(){return Ability.BENEFICIAL_OTHERS;}
	protected int canTargetCode(){return CAN_MOBS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_AddLimb();}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;}
	public Item itemRef=null;
	public long wornRef=0;
	public int oldMsg=0;
	
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			mob.tell("Your extra limb vanishes!");
			mob.confirmWearability();
		}
	}
	
	public boolean reallyCantWear(Item I, int minor)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return false;
		MOB mob=(MOB)affected;
		if(I.canWear(mob)) return false;
		long where=I.whereCantWear(mob);
		if((minor==Affect.TYP_HOLD)&&(where==Item.WIELD))
			where=Item.HELD;
		Item otherI=mob.fetchWornItem(where);
		if(otherI!=null)
		{
			long old=otherI.rawWornCode();
			otherI.unWear();
			boolean nowCan=I.canWear(mob);
			otherI.setRawWornCode(old);
			return !nowCan;
		}
		return true;
	}
	
	public boolean okAffect(Environmental myHost, Affect msg)
	{
		if(!super.okAffect(myHost,msg)) return false;
		if((affected==null)||(!(affected instanceof MOB)))
			return false;
		MOB mob=(MOB)affected;
		if(msg.amISource(mob)
		   &&(msg.target()!=null)
		   &&((msg.targetMinor()==Affect.TYP_WEAR)
			  ||(msg.targetMinor()==Affect.TYP_HOLD))
		   &&(msg.target() instanceof Item))
		{
			boolean really=reallyCantWear((Item)msg.target(),msg.targetMinor());
			if((itemRef==null)&&(!really))
			{
				Item I=(Item)msg.target();
				long where=I.whereCantWear(mob);
				if((where&(Item.ON_RIGHT_FINGER|Item.ON_RIGHT_WRIST|Item.HELD|Item.WIELD))==0)
					return true;
				if((msg.targetMinor()==Affect.TYP_HOLD)&&(where==Item.WIELD))
					where=Item.HELD;
				Item otherI=mob.fetchWornItem(where);
				if(otherI!=null)
				{
					itemRef=otherI;
					wornRef=otherI.rawWornCode();
					oldMsg=msg.targetCode();
					otherI.unWear();
				}
			}
			else
			if(msg.target()==itemRef)
			{
			   itemRef.setRawWornCode(wornRef);
			   itemRef=null;
			   return false;
			}
		}
		return true;
	}
	
	public void affect(Environmental myHost, Affect msg)
	{
		super.affect(myHost,msg);
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(msg.amISource(mob)
		&&(itemRef!=null)
		&&(msg.target()!=null)
		&&((msg.targetMinor()==Affect.TYP_WEAR)
			||(msg.targetMinor()==Affect.TYP_HOLD)
			||(msg.targetMinor()==Affect.TYP_WIELD)))
			msg.addTrailerMsg(new FullMsg(mob,itemRef,oldMsg,null));
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, encanting.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.MSG_OK_ACTION,"<T-NAME> grow(s) an arm!");
				beneficialAffect(mob,target,0);
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, encanting but nothing happens.");


		// return whether it worked
		return success;
	}
}