package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.Abilities.Traps.Trap_Trap;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_DeathTrap extends ThiefSkill implements Trap
{
	public String ID() { return "Thief_DeathTrap"; }
	public String name(){ return "Death Trap";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return Ability.CAN_ROOMS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"DEATHTRAP"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_DeathTrap();}
	private boolean sprung=false;
	
	public boolean disabled(){return false;}
	public void disable(){ unInvoke();}
	public boolean sprung(){return false;}
	
	public boolean isABomb(){return false;}
	public void setReset(int Reset){}
	public int getReset(){return 0;}
	public boolean maySetTrap(MOB mob, int asLevel){return false;}
	public boolean canSetTrapOn(MOB mob, Environmental E){return false;}
	public String requiresToSet(){return "";}
	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{
		if(E==null) return null;
		Trap T=(Trap)copyOf();
		T.setInvoker(mob);
		E.addAffect(T);
		ExternalPlay.startTickDown(T,Host.TRAP_DESTRUCTION,new Long(Host.TICKS_PER_DAY).intValue());
		return T;
	}

	public void spring(MOB M)
	{
		if((!sprung)&&(Dice.rollPercentage()>M.charStats().getSave(CharStats.SAVE_TRAPS)))
			ExternalPlay.postDeath(invoker(),M,null);
	}
	
	public void affect(Environmental myHost, Affect msg)
	{
		if((msg.targetMinor()==Affect.TYP_ENTER)
		&&(msg.target()==affected)
		&&(msg.source()!=invoker())
		&&(!sprung)
		&&(invoker()!=null)
		&&(invoker().mayIFight(msg.source()))
		&&(Dice.rollPercentage()>msg.source().charStats().getSave(CharStats.SAVE_TRAPS)))
			ExternalPlay.postDeath(invoker(),msg.source(),msg);
		super.affect(myHost,msg);
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
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID==Host.TRAP_RESET)
		{
			sprung=false;
			return false;
		}
		else
		if(tickID==Host.TRAP_DESTRUCTION)
		{
			unInvoke();
			return false;
		}
		return true;
	}
	
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Room trapThis=mob.location();

		Item resource=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_METAL);
		int amount=0;
		if(resource!=null) amount=findNumberOfResource(mob.location(),resource.material());
		if(amount<100)
		{
			mob.tell("You need 100 pounds of raw metal to build this trap.");
			return false;
		}
		
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int woodDestroyed=100;
		for(int i=mob.location().numItems()-1;i>=0;i--)
		{
			Item I=mob.location().fetchItem(i);
			if((I instanceof EnvResource)
			&&(I.container()==null)
			&&(I.material()==resource.material())
			&&((--woodDestroyed)>=0))
				I.destroyThis();
		}
		
		boolean success=profficiencyCheck(0,auto);
		
		FullMsg msg=new FullMsg(mob,trapThis,this,auto?Affect.MSG_OK_ACTION:Affect.MSG_THIEF_ACT,Affect.MASK_GENERAL|Affect.MSG_DELICATE_HANDS_ACT,Affect.MSG_OK_ACTION,(auto?trapThis.name()+" begins to glow!":"<S-NAME> attempt(s) to lay a trap here."));
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			if(success)
			{
				mob.tell("You have set the trap.");
				setTrap(mob,trapThis,mob.charStats().getClassLevel(mob.charStats().getCurrentClass()),(CMAble.qualifyingClassLevel(mob,this)-CMAble.lowestQualifyingLevel(ID()))+1);
			}
			else
			{
				if(Dice.rollPercentage()>50)
				{
					Trap T=setTrap(mob,trapThis,mob.charStats().getClassLevel(mob.charStats().getCurrentClass()),(CMAble.qualifyingClassLevel(mob,this)-CMAble.lowestQualifyingLevel(ID()))+1);
					mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> trigger(s) the trap on accident!");
					T.spring(mob);
				}
				else
				{
					mob.tell("You fail in your attempt to set the death trap.");
				}
			}
		}
		return success;
	}
}