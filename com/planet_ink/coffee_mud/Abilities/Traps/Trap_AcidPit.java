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
	public int baseRejuvTime(int level)
	{
		int time=super.baseRejuvTime(level);
		if(time<15) time=15;
		return time;
	}

	public void finishSpringing(MOB target)
	{
		if((!invoker().mayIFight(target))||(target.envStats().weight()<5))
			target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> float(s) gently into the pit!");
		else
		{
			target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> hit(s) the pit floor with a THUMP!");
			int damage=Dice.roll(trapLevel(),6,1);
			MUDFight.postDamage(invoker(),target,this,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_ACID,-1,null);
			target.location().showHappens(CMMsg.MSG_OK_VISUAL,"Acid starts pouring into the room!");
		}
		CommonMsgs.look(target,true);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==MudHost.TICK_TRAP_RESET)&&(getReset()>0))
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
						MUDFight.postDamage(invoker(),M,this,damage,CMMsg.MASK_MALICIOUS|CMMsg.TYP_ACID,Weapon.TYPE_MELTING,"The acid <DAMAGE> <T-NAME>!");
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
