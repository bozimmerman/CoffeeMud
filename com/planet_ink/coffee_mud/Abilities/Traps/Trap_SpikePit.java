package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Trap_SpikePit extends Trap_RoomPit
{
	public String ID() { return "Trap_SpikePit"; }
	public String name(){ return "spike pit";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 8;}
	public String requiresToSet(){return "5 dagger-class weapons";}
	public Environmental newInstance(){	return new Trap_SpikePit();}

	public Vector daggerDamages=null;

	private Item getDagger(MOB mob)
	{
		if(mob==null) return null;
		if(mob.location()==null) return null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().fetchItem(i);
			if((I instanceof Weapon)
			&&(((Weapon)I).weaponClassification()==Weapon.CLASS_DAGGER))
				return I;
		}
		return null;
	}

	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{
		if(E==null) return null;
		Item I=getDagger(mob);
		int num=0;
		while((I!=null)&&((++num)<6))
		{
			if(daggerDamages==null)
				daggerDamages=new Vector();
			daggerDamages.addElement(new Integer(I.baseEnvStats().damage()));
			I.destroy();
			I=getDagger(mob);
		}
		return super.setTrap(mob,E,classLevel,qualifyingClassLevel);
	}

	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		if(mob!=null)
		{
			if(getDagger(mob)==null)
			{
				mob.tell("You'll need to set down some dagger-class weapons first.");
				return false;
			}
		}
		return true;
	}

	public void finishSpringing(MOB target)
	{
		if((!invoker().mayIFight(target))||(target.envStats().weight()<5))
			target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> float(s) gently into the pit!");
		else
		{
			target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> hit(s) the pit floor!");
			int damage=Dice.roll(trapLevel(),6,1);
			if((daggerDamages!=null)&&(daggerDamages.size()>0))
			{
				for(int i=0;i<daggerDamages.size();i++)
					damage+=Dice.roll(1,((Integer)daggerDamages.elementAt(i)).intValue(),0);
			}
			else
				damage+=Dice.roll(5,4,0);
			MUDFight.postDamage(invoker(),target,this,damage,CMMsg.MSG_OK_VISUAL,Weapon.TYPE_PIERCING,"Spikes on the pit floor <DAMAGE> <T-NAME>!");
		}
		CommonMsgs.look(target,true);
	}
}
