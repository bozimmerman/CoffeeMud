package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class StdTrap extends StdAbility implements Trap
{
	public String ID() { return "StdTrap"; }
	public String name(){ return "standard trap";}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	public int enchantQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	protected int ableCode=0;
	protected int trapLevel(){return -1;}
	public void setAbilityCode(int code){ableCode=code;}
	public int abilityCode(){return ableCode;}
	
	public boolean isABomb(){return false;}
	public String requiresToSet(){return "";}
	private String invokerName=null;
	private static final Vector emptyV=new Vector();

	public int baseRejuvTime(int level)
	{
		if(level>=30) level=29;
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
	
	public boolean isLocalExempt(MOB target)
	{
		if(target==null) return false;
		Room R=target.location();
        if((!canBeUninvoked())
        &&(!isABomb())
        &&(R!=null)) {
            if((CMLib.law().getLandTitle(R)!=null)
            &&(CMLib.law().doesHavePriviledgesHere(target,R)))
                return true;
            
            if((target.isMonster())
            &&(target.getStartRoom()!=null)
            &&(target.getStartRoom().getArea()==R.getArea()))
                return true;
        }
		return false;
	}
	
	public void disable(){
		disabled=true;
		sprung=true;
		if(!canBeUninvoked())
		{
			tickDown=getReset();
			CMLib.threads().startTickDown(this,Tickable.TICKID_TRAP_RESET,1);
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
			if((invokerName!=null)&&(!invokerName.equalsIgnoreCase("null")))
				invoker=CMLib.players().getLoadPlayer(invokerName);
			if(invoker==null)
			{
				invoker=CMClass.getMOB("StdMOB");
				invoker.setLocation(CMClass.getLocale("StdRoom"));
				invoker.baseEnvStats().setLevel(affected.envStats().level());
				invoker.envStats().setLevel(affected.envStats().level());
			}
		}
		else
			invokerName=invoker.Name();
		return super.invoker();
	}

	public int classificationCode()
	{
		return Ability.ACODE_TRAP;
	}

	public void setMiscText(String text){
		if(text.startsWith("`"))
		{
			int x=text.indexOf("` ",1);
			if(x>=0)
			{
				invokerName=text.substring(1,x);
				text=text.substring(x+2);
			}
		}
		if(text.trim().startsWith(":"))
		{
			int x=text.indexOf(":");
			int y=text.indexOf(":",x+1);
			if((x>=0)&&(y>x)&&(CMath.isInteger(text.substring(x+1,y).trim())))
			{
				setAbilityCode(CMath.s_int(text.substring(x+1,y).trim()));
				text=text.substring(y+1);
			}
		}
		super.setMiscText(text);
	}
	public String text(){
		return "`"+invokerName+"` :"+abilityCode()+":"+super.text();
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
			CMLib.threads().startTickDown(this,Tickable.TICKID_TRAP_RESET,1);
		}
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(!sprung)
		if(CMath.bset(canAffectCode(),Ability.CAN_EXITS))
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
		if(CMath.bset(canAffectCode(),Ability.CAN_ITEMS))
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
		if(CMath.bset(canAffectCode(),Ability.CAN_ROOMS))
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
    public Vector getTrapComponents() { return emptyV;}
	public Trap setTrap(MOB mob, Environmental E, int trapBonus, int qualifyingClassLevel, boolean perm)
	{
		if(E==null) return null;
		int rejuv=baseRejuvTime(qualifyingClassLevel+trapBonus);
		Trap T=(Trap)copyOf();
		T.setReset(rejuv);
		T.setInvoker(mob);
		T.setSavable(false);
		T.setAbilityCode(trapBonus);
		E.addEffect(T);
        if(perm)
        {
            T.setSavable(true);
            T.makeNonUninvokable();
        }
        else
		if(!isABomb())
			CMLib.threads().startTickDown(T,Tickable.TICKID_TRAP_DESTRUCTION,baseDestructTime(qualifyingClassLevel+trapBonus));
		return T;
	}
	
	public void setInvoker(MOB mob)
	{
		if(mob!=null) 
			invokerName=mob.Name();
		super.setInvoker(mob);
	}
	

	public boolean tick(Tickable ticking, int tickID)
	{
		if((unInvoked)&&(canBeUninvoked()))
			return false;

		if(tickID==Tickable.TICKID_TRAP_DESTRUCTION)
		{
			if(canBeUninvoked())
				disable();
			return false;
		}
		else
		if((tickID==Tickable.TICKID_TRAP_RESET)&&(getReset()>0))
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
						for(int i=R.numInhabitants()-1;i>=0;i--)
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
			CMLib.threads().startTickDown(this,Tickable.TICKID_TRAP_RESET,1);
	}

	protected Item findFirstResource(Room room, String other)
	{
		return CMLib.materials().findFirstResource(room,other);
	}
	protected Item findFirstResource(Room room, int resource)
	{
		return CMLib.materials().findFirstResource(room,resource);
	}
	protected Item findMostOfMaterial(Room room, String other)
	{
		return CMLib.materials().findMostOfMaterial(room,other);
	}

	protected Item findMostOfMaterial(Room room, int material)
	{
		return CMLib.materials().findMostOfMaterial(room, material);
	}

	protected void destroyResources(Room room, int resource, int number)
	{
		CMLib.materials().destroyResources(room,number,resource,-1,null);
	}

	protected int findNumberOfResource(Room room, int resource)
	{
		return CMLib.materials().findNumberOfResource(room, resource);
	}

}
