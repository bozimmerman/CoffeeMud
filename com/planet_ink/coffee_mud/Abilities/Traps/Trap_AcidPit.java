package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Trap_AcidPit extends Trap_RoomPit
{
	public String ID() { return "Trap_AcidPit"; }
	public String name(){ return "acid pit";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 18;}
	public String requiresToSet(){return "";}
	public Environmental newInstance(){	return new Trap_AcidPit();}
	public int baseRejuvTime(int level)
	{	
		int time=super.baseRejuvTime(level);
		if(time<15) time=15;
		return time;
	}
	
	public void finishSpringing(MOB target)
	{
		if((!invoker().mayIFight(target))||(target.envStats().weight()<5))
			target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> float(s) gently into the pit!");
		else
		{
			target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> hit(s) the pit floor with a THUMP!");
			int damage=Dice.roll(trapLevel(),6,1);
			ExternalPlay.postDamage(invoker(),target,this,damage,Affect.MASK_GENERAL|Affect.TYP_ACID,-1,null);
			target.location().showHappens(Affect.MSG_OK_VISUAL,"Acid starts pouring into the room!");
		}
		ExternalPlay.look(target,null,true);
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Host.TRAP_RESET)&&(getReset()>0))
		{
			if((sprung)
			&&(affected!=null)
			&&(affected instanceof Room)
			&&(pit!=null)
			&&(pit.size()>1)
			&&(!disabled()))
			{
				Room R=(Room)pit.firstElement();
				for(int i=0;i<R.numInhabitants();i++)
				{
					MOB M=R.fetchInhabitant(i);
					if((M!=null)&&(M!=invoker()))
					{
						int damage=Dice.roll(trapLevel(),6,1);
						ExternalPlay.postDamage(invoker(),M,this,damage,Affect.MASK_MALICIOUS|Affect.TYP_ACID,Weapon.TYPE_MELTING,"The acid <DAMAGE> <T-NAME>!");
					}
				}
				return super.tick(ticking,tickID);
			}
			else
				return false;
		}
		return super.tick(ticking,tickID);
	}
	
}
