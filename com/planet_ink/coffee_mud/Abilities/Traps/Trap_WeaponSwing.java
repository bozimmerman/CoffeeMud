package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Trap_WeaponSwing extends StdTrap
{
	public String ID() { return "Trap_WeaponSwing"; }
	public String name(){ return "weapon swing";}
	protected int canAffectCode(){return Ability.CAN_EXITS|Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 9;}
	public String requiresToSet(){return "a melee weapon";}
	public Environmental newInstance(){	return new Trap_WeaponSwing();}

	private Item getPoison(MOB mob)
	{
		if(mob==null) return null;
		if(mob.location()==null) return null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().fetchItem(i);
			if((I!=null)
			&&(I instanceof Weapon)
			&&(((Weapon)I).weaponClassification()!=Weapon.CLASS_RANGED))
				return I;
		}
		return null;
	}

	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{
		if(E==null) return null;
		Item I=getPoison(mob);
		setMiscText("3/a club");
		if(I!=null){
			setMiscText(""+I.baseEnvStats().damage()+"/"+I.name());
			I.destroyThis();
		}
		return super.setTrap(mob,E,classLevel,qualifyingClassLevel);
	}

	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		Item I=getPoison(mob);
		if(I==null)
		{
			mob.tell("You'll need to set down a melee weapon first.");
			return false;
		}
		return true;
	}
	public void spring(MOB target)
	{
		if((target!=invoker())
		   &&(target.location()!=null))
		{
			int x=text().indexOf("/");
			int dam=3;
			String name="a club";
			if(x>=0)
			{
				dam=Util.s_int(text().substring(0,x));
				name=text().substring(x+1);
			}
			if((!invoker().mayIFight(target))||(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS)))
				target.location().show(target,null,null,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> avoid(s) setting off "+name+" trap!");
			else
			if(target.location().show(target,target,this,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> <S-IS-ARE> struck by "+name+" trap!"))
			{
				super.spring(target);
				int damage=Dice.roll(trapLevel(),dam,1);
				ExternalPlay.postDamage(invoker(),target,this,damage,Affect.NO_EFFECT,-1,null);
				if((canBeUninvoked())&&(affected instanceof Item))
					disable();
			}
		}
	}
}
