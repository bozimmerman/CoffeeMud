package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class StdTrap extends StdAbility implements Trap
{
	public String ID() { return "StdTrap"; }
	public String name(){ return "standard trap";}
	public int quality(){ return MALICIOUS;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return -1;}
	public boolean isABomb(){return false;}
	public String requiresToSet(){return "";}

	public int baseRejuvTime(int level)
	{
		int time=((30-level)*30);
		if(time<1) time=1;
		return time;
	}
	public int baseDestructTime(int level)
	{
		return level*30;
	}

	protected boolean sprung=false;
	protected int reset=60; // 5 minute reset is standard

	protected boolean disabled=false;

	public boolean disabled(){
		return (sprung&&disabled)
			   ||(affected==null)
			   ||(affected.fetchEffect(ID())==null);
	}
	public void disable(){
		disabled=true;
		sprung=true;
		if(!canBeUninvoked())
		{
			tickDown=getReset();
			CMClass.ThreadEngine().startTickDown(this,MudHost.TICK_TRAP_RESET,1);
		}
		else
			unInvoke();
	}
	public void setReset(int Reset){reset=Reset;}
	public int getReset(){return reset;}

	public StdTrap()
	{
		super();
	}
	public MOB invoker()
	{
		if(invoker==null)
		{
			invoker=CMClass.getMOB("StdMOB");
			invoker.setLocation(CMClass.getLocale("StdRoom"));
			invoker.baseEnvStats().setLevel(affected.envStats().level());
			invoker.envStats().setLevel(affected.envStats().level());
		}
		return super.invoker();
	}

	public int classificationCode()
	{
		return Ability.TRAP;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((!disabled())&&(affected instanceof Item))
		{
			if((msg.tool()==affected)
			   &&(msg.targetMinor()==CMMsg.TYP_GIVE)
			   &&(msg.targetMessage()!=null)
			   &&(msg.target()!=null)
			   &&(msg.target() instanceof MOB)
			   &&(!msg.source().getGroupMembers(new HashSet()).contains(msg.target())))
			{
				msg.source().tell((MOB)msg.target(),msg.tool(),null,"<S-NAME> can't accept <T-NAME>.");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void activateBomb()
	{
		if(isABomb())
		{
			tickDown=getReset();
			sprung=false;
			disabled=false;
			CMClass.ThreadEngine().startTickDown(this,MudHost.TICK_TRAP_RESET,1);
		}
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(!sprung)
		if(Util.bset(canAffectCode(),Ability.CAN_EXITS))
		{
			if(msg.amITarget(affected))
			{
				if((affected instanceof Exit)
				&&(((Exit)affected).hasADoor())
				&&(((Exit)affected).hasALock())
				&&(((Exit)affected).isLocked()))
				{
					if(msg.targetMinor()==CMMsg.TYP_UNLOCK)
						spring(msg.source());
				}
				else
				if((affected instanceof Container)
				&&(((Container)affected).hasALid())
				&&(((Container)affected).hasALock())
				&&(((Container)affected).isLocked()))
				{
					if(msg.targetMinor()==CMMsg.TYP_UNLOCK)
						spring(msg.source());
				}
				else
				if(msg.targetMinor()==CMMsg.TYP_OPEN)
					spring(msg.source());
			}
		}
		else
		if(Util.bset(canAffectCode(),Ability.CAN_ITEMS))
		{
			if(isABomb())
			{
				if(msg.amITarget(affected))
				{
					if((msg.targetMinor()==CMMsg.TYP_HOLD)
					&&(msg.source().isMine(affected)))
					{
						msg.source().tell(msg.source(),affected,null,"You activate <T-NAME>.");
						activateBomb();
					}
				}
			}
			else
			if(msg.amITarget(affected))
			{
				if((msg.targetMinor()==CMMsg.TYP_GET)
				&&(!msg.source().isMine(affected)))
					spring(msg.source());
			}
		}
		else
		if(Util.bset(canAffectCode(),Ability.CAN_ROOMS))
		{
			if(msg.amITarget(affected))
			{
				if((msg.targetMinor()==CMMsg.TYP_ENTER)
				&&(!msg.source().isMine(affected)))
					spring(msg.source());
			}
		}
		super.executeMsg(myHost,msg);
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
		if(mob!=null)
			if((!maySetTrap(mob,mob.envStats().level()))
			&&(!mob.charStats().getCurrentClass().leveless())
			&&(!CMSecurity.isDisabled("LEVELS")))
			{
				mob.tell("You are not high enough level ("+trapLevel()+") to set that trap.");
				return false;
			}
		if(E.fetchEffect(ID())!=null)
		{
			if(mob!=null)
				mob.tell("This trap is already set on "+E.name()+".");
			return false;
		}
		if(!canAffect(E))
		{
			if(mob!=null)
				mob.tell("You can't set '"+name()+"' on "+E.name()+".");
			return false;
		}
		if((canAffectCode()&Ability.CAN_EXITS)==Ability.CAN_EXITS)
		{
			if((E instanceof Item)&&(!(E instanceof Container)))
			{
				if(mob!=null)
					mob.tell(E.name()+" has no lid, so '"+name()+"' cannot be set on it.");
				return false;
			}
			if(((E instanceof Exit)&&(!(((Exit)E).hasADoor()))))
			{
				if(mob!=null)
					mob.tell(E.name()+" has no door, so '"+name()+"' cannot be set on it.");
				return false;
			}
			if(((E instanceof Container)&&(!(((Container)E).hasALid()))))
			{
				if(mob!=null)
					mob.tell(E.name()+" has no lid, so '"+name()+"' cannot be set on it.");
				return false;
			}
		}
		return true;
	}
	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{
		if(E==null) return null;
		int rejuv=baseRejuvTime(qualifyingClassLevel);
		Trap T=(Trap)copyOf();
		T.setReset(rejuv);
		T.setInvoker(mob);
		T.setBorrowed(E,true);
		E.addEffect(T);
		if(!isABomb())
			CMClass.ThreadEngine().startTickDown(T,MudHost.TICK_TRAP_DESTRUCTION,baseDestructTime(qualifyingClassLevel));
		return T;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((unInvoked)&&(canBeUninvoked()))
			return false;

		if(tickID==MudHost.TICK_TRAP_DESTRUCTION)
		{
			if(canBeUninvoked())
				disable();
			return false;
		}
		else
		if((tickID==MudHost.TICK_TRAP_RESET)&&(getReset()>0))
		{
			if((--tickDown)<=0)
			{
				if((isABomb())
				&&(affected instanceof Item)
				&&(((Item)affected).owner()!=null))
				{
					Item I=(Item)affected;
					if(I.owner() instanceof MOB)
						spring((MOB)I.owner());
					else
					if(I.owner() instanceof Room)
					{
						Room R=(Room)I.owner();
						for(int i=0;i<R.numInhabitants();i++)
						{
							MOB M=R.fetchInhabitant(i);
							if(M!=null)
								spring(M);
						}
					}
					disable();
					unInvoke();
					I.destroy();
					return false;
				}
				else
					sprung=false;
				disabled=false;
				return false;
			}
		}
		return true;
	}

	public boolean sprung(){return sprung&&(!disabled());}

	public void spring(MOB target)
	{
		sprung=true;
		disabled=false;
		tickDown=getReset();
		if(!isABomb())
			CMClass.ThreadEngine().startTickDown(this,MudHost.TICK_TRAP_RESET,1);
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

	protected void destroyResources(Room room, int resource, int number)
	{
		for(int i=room.numItems()-1;i>=0;i--)
		{
			Item I=room.fetchItem(i);
			if((I instanceof EnvResource)
			&&(I.container()==null)
			&&(I.material()==resource)
			&&((--number)>=0))
				I.destroy();
		}
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
