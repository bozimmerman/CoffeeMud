package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdTrap extends StdAbility implements Trap
{
	public String ID() { return "StdTrap"; }
	public String name(){ return "standard trap";}
	protected int canAffectCode(){return Ability.CAN_EXITS|Ability.CAN_ROOMS|Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return -1;}
	public String requiresToSet(){return "";}
	public Environmental newInstance(){	return new StdTrap();}
	
	private boolean sprung=false;
	private int reset=0;
	
	public boolean disabled(){return sprung;}
	public void disable(){ sprung=true;}
	public void setReset(int Reset){reset=Reset;}
	public int getReset(){return reset;}

	public StdTrap()
	{
		super();
		if(benefactor==null)
			benefactor=(MOB)CMClass.getMOB("StdMOB");
	}
	protected static MOB benefactor=(MOB)CMClass.getMOB("StdMOB");
	public MOB invoker()
	{
		if(invoker==null) return benefactor;
		return super.invoker();
	}
	
	public int classificationCode()
	{
		return Ability.TRAP;
	}

	public boolean maySetTrap(MOB mob, int asLevel)
	{
		if(mob==null) return false;
		if(trapLevel()<0) return false;
		if(asLevel<0) return true;
		if(asLevel>=trapLevel()) return true;
		return false;
	}
	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!maySetTrap(mob,mob.envStats().level()))
		{
			mob.tell("You are not high enough level ("+trapLevel()+") to set that trap.");
			return false;
		}
		return true;
	}
	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{
		if(E==null) return null;
		int rejuv=((30-qualifyingClassLevel)*30);
		Trap T=(Trap)copyOf();
		T.setReset(rejuv);
		T.setInvoker(mob);
		E.addAffect(T);
		ExternalPlay.startTickDown(T,Host.TRAP_DESTRUCTION,qualifyingClassLevel*30);
		return T;
	}
	
	public void spring(MOB target)
	{
		
	}
}
