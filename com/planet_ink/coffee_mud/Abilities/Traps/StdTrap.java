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
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return -1;}
	public String requiresToSet(){return "";}
	public Environmental newInstance(){	return new StdTrap();}
	
	protected boolean disabled=false;
	protected boolean sprung=false;
	protected int reset=0;
	
	public boolean disabled(){return disabled;}
	public void disable(){ disabled=true;unInvoke();}
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

	public void affect(Environmental myHost, Affect affect)
	{
		if(!sprung)
		if(Util.bset(canAffectCode(),Ability.CAN_EXITS))
		{
			if(affect.amITarget(affected))
			{
				if(affect.targetMinor()==Affect.TYP_OPEN)
					spring(affect.source());
			}
		}
		else
		if(Util.bset(canAffectCode(),Ability.CAN_ITEMS))
		{
			if(affect.amITarget(affected))
			{
				if((affect.targetMinor()==Affect.TYP_GET)
				&&(!affect.source().isMine(affected)))
					spring(affect.source());
			}
		}
		else
		if(Util.bset(canAffectCode(),Ability.CAN_ROOMS))
		{
			if(affect.amITarget(affected))
			{
				if((affect.targetMinor()==Affect.TYP_ENTER)
				&&(!affect.source().isMine(affected)))
					spring(affect.source());
			}
		}
		super.affect(myHost,affect);
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
		if(!canAffect(E))
		{
			mob.tell("You can't set '"+name()+"' on "+E.name()+".");
			return false;
		}
		if((canAffectCode()&Ability.CAN_EXITS)==Ability.CAN_EXITS)
		{
			if((E instanceof Item)&&(!(E instanceof Container)))
			{
				mob.tell(E.name()+" has no lid, so '"+name()+"' cannot be set on it.");
				return false;
			}
			if(((E instanceof Exit)&&(!(((Exit)E).hasADoor()))))
			{
				mob.tell(E.name()+" has no door, so '"+name()+"' cannot be set on it.");
				return false;
			}
			if(((E instanceof Container)&&(!(((Container)E).hasALid()))))
			{
				mob.tell(E.name()+" has no lid, so '"+name()+"' cannot be set on it.");
				return false;
			}
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
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((unInvoked)&&(canBeUninvoked()))
			return false;
		
		if(tickID==Host.TRAP_DESTRUCTION)
		{
			if(canBeUninvoked())
				disable();
			return false;
		}
		else
		if((tickID==Host.TRAP_RESET)&&(getReset()>0))
		{
			if((--tickDown)<=0)
			{
				sprung=false;
				return false;
			}
		}
		return true;
	}
	
	public void spring(MOB target)
	{
		sprung=true;
		tickDown=getReset();
		ExternalPlay.startTickDown(this,Host.TRAP_RESET,1);
	}
	
	protected Item findFirstResource(Room room, String other)
	{
		if((other==null)||(other.length()==0))
			return null;
		for(int i=0;i<EnvResource.RESOURCE_DESCS.length;i++)
			if(EnvResource.RESOURCE_DESCS[i].equalsIgnoreCase(other))
				return findFirstResource(room,EnvResource.RESOURCE_DATA[i][0]);
		return null;
	}
	protected Item findFirstResource(Room room, int resource)
	{
		for(int i=0;i<room.numItems();i++)
		{
			Item I=room.fetchItem(i);
			if((I instanceof EnvResource)
			&&(I.material()==resource)
			&&(!Sense.isOnFire(I))
			&&(I.container()==null))
				return I;
		}
		return null;
	}
	protected Item findMostOfMaterial(Room room, String other)
	{
		if((other==null)||(other.length()==0))
			return null;
		for(int i=0;i<EnvResource.MATERIAL_DESCS.length;i++)
			if(EnvResource.MATERIAL_DESCS[i].equalsIgnoreCase(other))
				return findMostOfMaterial(room,(i<<8));
		return null;
	}
	
	protected Item findMostOfMaterial(Room room, int material)
	{
		int most=0;
		int mostMaterial=-1;
		Item mostItem=null;
		for(int i=0;i<room.numItems();i++)
		{
			Item I=room.fetchItem(i);
			if((I instanceof EnvResource)
			&&((I.material()&EnvResource.MATERIAL_MASK)==material)
			&&(I.material()!=mostMaterial)
			&&(!Sense.isOnFire(I))
			&&(I.container()==null))
			{
				int num=findNumberOfResource(room,I.material());
				if(num>most)
				{
					mostItem=I;
					most=num;
					mostMaterial=I.material();
				}
			}
		}
		return mostItem;	   
	}
	
	protected int findNumberOfResource(Room room, int resource)
	{
		int foundWood=0;
		for(int i=0;i<room.numItems();i++)
		{
			Item I=room.fetchItem(i);
			if((I instanceof EnvResource)
			&&(I.material()==resource)
			&&(!Sense.isOnFire(I))
			&&(I.container()==null))
				foundWood++;
		}
		return foundWood;
	}
	
}
