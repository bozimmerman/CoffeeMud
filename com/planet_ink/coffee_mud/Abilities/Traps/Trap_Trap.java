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
public class Trap_Trap extends StdAbility implements Trap
{
	public String ID() { return "Trap_Trap"; }
	public String name(){ return "a Trap!";}
	protected int canAffectCode(){return Ability.CAN_EXITS|Ability.CAN_ROOMS|Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}

	protected static MOB benefactor=CMClass.getMOB("StdMOB");
	protected boolean sprung=false;
	protected Room myPit=null;
	protected Room myPitUp=null;
	protected int reset=60; // 5 minute reset is standard
	protected int trapType(){return Dice.roll(1,3,-1);}

	public Trap_Trap()
	{
		super();
		if(benefactor==null)
			benefactor=CMClass.getMOB("StdMOB");
	}

	public void activateBomb(){}
	public boolean isABomb(){return false;}
	public boolean sprung(){return sprung;}
	public boolean disabled(){return sprung;}
	public void disable(){ sprung=true;}
	public void setReset(int Reset){reset=Reset;}
	public int getReset(){return reset;}

	// as these are not standard traps, we return this!
	public boolean maySetTrap(MOB mob, int asLevel){return false;}
	public boolean canSetTrapOn(MOB mob, Environmental E){return false;}
	public String requiresToSet(){return "";}
	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{
		if(E==null) return null;
		int rejuv=((30-mob.envStats().level())*30);
		Trap T=(Trap)copyOf();
		T.setReset(rejuv);
		T.setInvoker(mob);
		E.addEffect(T);
		T.setBorrowed(E,true);
		CMClass.ThreadEngine().startTickDown(T,MudHost.TICK_TRAP_DESTRUCTION,mob.envStats().level()*30);
		return T;
	}
	public void gas(MOB mob)
	{
		if(Dice.rollPercentage()<mob.charStats().getSave(CharStats.SAVE_TRAPS))
			mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> avoid(s) a gas trap set in <T-NAME>.");
		else
		if(mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> trigger(s) a trap set in <T-NAME>!"))
			if(mob.envStats().level()>15)
			{
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The room fills with gas!");
				for(int i=0;i<mob.location().numInhabitants();i++)
				{
					MOB target=mob.location().fetchInhabitant(i);
					if(target==null) break;

					int dmg=Dice.roll(target.envStats().level(),10,1);
					FullMsg msg=new FullMsg(invoker(),target,this,CMMsg.MSG_OK_ACTION,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_GAS,CMMsg.MSG_NOISYMOVEMENT,null);
					if(target.location().okMessage(target,msg))
					{
						target.location().send(target,msg);
						if(msg.value()>0)
							dmg=(int)Math.round(Util.div(dmg,2.0));
						MUDFight.postDamage(invoker(),target,this,dmg,CMMsg.MASK_GENERAL|CMMsg.TYP_GAS,Weapon.TYPE_GASSING,"The gas <DAMAGE> <T-NAME>!");
					}
				}
			}
			else
			{
				MOB target=mob;
				int dmg=Dice.roll(target.envStats().level(),10,1);
				FullMsg msg=new FullMsg(invoker(),target,this,CMMsg.MSG_OK_ACTION,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_GAS,CMMsg.MSG_NOISYMOVEMENT,null);
				if(target.location().okMessage(target,msg))
				{
					target.location().send(target,msg);
					if(msg.value()>0)
						dmg=(int)Math.round(Util.div(dmg,2.0));
					MUDFight.postDamage(invoker(),target,this,dmg,CMMsg.MASK_GENERAL|CMMsg.TYP_GAS,Weapon.TYPE_GASSING,"A sudden blast of gas <DAMAGE> <T-NAME>!");
				}
			}
	}

	public void needle(MOB mob)
	{
		if(Dice.rollPercentage()<mob.charStats().getSave(CharStats.SAVE_TRAPS))
			mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> avoid(s) a needle trap set in <T-NAME>.");
		else
		if(mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> trigger(s) a needle trap set in <T-NAME>!"))
		{
			MOB target=mob;
			int dmg=Dice.roll(target.envStats().level(),5,1);
			FullMsg msg=new FullMsg(invoker(),target,this,CMMsg.MSG_OK_ACTION,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE,CMMsg.MSG_NOISYMOVEMENT,null);
			if(target.location().okMessage(target,msg))
			{
				target.location().send(target,msg);
				if(msg.value()>0)
					dmg=(int)Math.round(Util.div(dmg,2.0));
				MUDFight.postDamage(invoker(),target,this,dmg,CMMsg.MSG_OK_VISUAL,Weapon.TYPE_PIERCING,"The needle <DAMAGE> <T-NAME>!");

				Ability P=CMClass.getAbility("Poison");
				if(P!=null) P.invoke(invoker(),target,true,0);
			}
		}
	}

	public void blade(MOB mob)
	{
		if(Dice.rollPercentage()<mob.charStats().getSave(CharStats.SAVE_TRAPS))
			mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> avoid(s) a blade trap set in <T-NAME>.");
		else
		if(mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> trigger(s) a blade trap set in <T-NAME>!"))
		{
			MOB target=mob;
			int dmg=Dice.roll(target.envStats().level(),2,0);
			FullMsg msg=new FullMsg(invoker(),target,this,CMMsg.MSG_OK_ACTION,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE,CMMsg.MSG_NOISYMOVEMENT,null);
			if(target.location().okMessage(target,msg))
			{
				target.location().send(target,msg);
				if(msg.value()>0)
					dmg=(int)Math.round(Util.div(dmg,2.0));
				Ability P=CMClass.getAbility("Poison");
				if(P!=null) P.invoke(invoker(),target,true,0);
				MUDFight.postDamage(invoker(),target,this,dmg,CMMsg.MSG_OK_VISUAL,Weapon.TYPE_PIERCING,"The blade <DAMAGE> <T-NAME>!");
			}
		}
	}

	public void victimOfSpell(MOB mob)
	{
		if(Dice.rollPercentage()<mob.charStats().getSave(CharStats.SAVE_TRAPS))
			mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> avoid(s) a magic trap set in <T-NAME>.");
		else
		if(mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> trigger(s) a trap set in <T-NAME>!"))
		{
			String spell=text();
			int x=spell.indexOf(";");
			Vector V=new Vector();
			V.addElement(mob.name());
			if(x>0)
			{
				V=Util.parse(spell.substring(x+1));
				V.insertElementAt(mob.name(),0);
				spell=spell.substring(0,x);
			}
			Ability A=CMClass.findAbility(spell);
			if(A==null)
			{
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"But nothing happened...");
				return;
			}
			A.invoke(invoker(),V,mob,true,0);
		}
	}

	public void fallInPit(MOB mob)
	{
		if(Sense.isInFlight(mob))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> trigger(s) a trap door beneath <S-HIS-HER> feet! <S-NAME> pause(s) over it in flight.");
			return;
		}
		else
		if(Dice.rollPercentage()<mob.charStats().getSave(CharStats.SAVE_TRAPS))
			mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> avoid(s) a trap door beneath <S-HIS-HER> feet.");
		else
		if(mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> trigger(s) a trap door beneath <S-HIS-HER> feet! <S-NAME> fall(s) in!"))
		{
			if((myPit==null)||(myPitUp==null))
			{
				myPitUp=CMClass.getLocale("ClimbableSurface");
				myPitUp.setArea(mob.location().getArea());
				myPitUp.baseEnvStats().setDisposition(myPitUp.baseEnvStats().disposition()|EnvStats.IS_DARK);
				myPitUp.setDisplayText("Inside a dark pit");
				myPitUp.setDescription("The walls here are slick and tall.  The trap door has already closed.");
				myPitUp.recoverEnvStats();

				myPit=CMClass.getLocale("StdRoom");
				myPit.setArea(mob.location().getArea());
				myPit.baseEnvStats().setDisposition(myPit.baseEnvStats().disposition()|EnvStats.IS_DARK);
				myPit.setDisplayText("Inside a dark pit");
				myPit.setDescription("The walls here are slick and tall.  You can barely see the closed trap door well above you.");
				myPit.rawExits()[Directions.UP]=CMClass.getExit("StdOpenDoorway");
				myPit.rawDoors()[Directions.UP]=myPitUp;
				myPitUp.recoverEnvStats();

			}
			CMMap.addRoom(myPit);
			CMMap.addRoom(myPitUp);
			myPitUp.rawExits()[Directions.UP]=CMClass.getExit("StdClosedDoorway");
			myPitUp.rawDoors()[Directions.UP]=mob.location();
			if((mob.location().getRoomInDir(Directions.DOWN)==null)
			&&(mob.location().getExitInDir(Directions.DOWN)==null))
			{
				mob.location().rawExits()[Directions.DOWN]=CMClass.getExit("StdClosedDoorway");
				mob.location().rawDoors()[Directions.DOWN]=myPitUp;
			}
			myPit.bringMobHere(mob,false);
			if(mob.envStats().weight()<5)
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> float(s) gently into the pit!");
			else
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> hit(s) the pit floor with a THUMP!");
				int damage=Dice.roll(mob.envStats().level(),3,1);
				MUDFight.postDamage(invoker(),mob,this,damage,CMMsg.MSG_OK_VISUAL,-1,null);
			}
			CommonMsgs.look(mob,true);
		}
	}

	public MOB invoker()
	{
		if(invoker==null) return benefactor;
		return super.invoker();
	}

	public int classificationCode()
	{
		return Ability.TRAP;
	}

	public void spring(MOB target)
	{
		sprung=true;
		benefactor.setLocation(target.location());
		benefactor.baseEnvStats().setLevel(target.envStats().level());
		benefactor.recoverCharStats();
		benefactor.recoverEnvStats();
		switch(trapType())
		{
		case TRAP_GAS:
			gas(target);
			break;
		case TRAP_NEEDLE:
			needle(target);
			break;
		case TRAP_PIT_BLADE:
			if(affected instanceof Exit)
				fallInPit(target);
			else
				blade(target);
			break;
		case TRAP_SPELL:
			victimOfSpell(target);
			break;
		default:
			target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> trigger(s) a trap, but it appears to have misfired.");
			break;
		}

		if((getReset()>0)&&(getReset()<Integer.MAX_VALUE))
			CMClass.ThreadEngine().startTickDown(this,MudHost.TICK_TRAP_RESET,getReset());
		else
			unInvoke();
	}

	public void unInvoke()
	{
		if((trapType()==Trap.TRAP_PIT_BLADE)
		&&(affected instanceof Exit)
		&&(myPit!=null)
		&&(canBeUninvoked())
		&&(myPitUp!=null))
		{
			Room R=myPitUp.getRoomInDir(Directions.UP);
			if((R!=null)&&(R.getRoomInDir(Directions.DOWN)==myPitUp))
			{
				R.rawDoors()[Directions.DOWN]=null;
				R.rawExits()[Directions.DOWN]=null;
			}
			/**
			don't do this, cuz someone might still be down there.
			myPitUp.rawDoors()[Directions.UP]=null;
			myPitUp.rawExits()[Directions.UP]=null;
			myPitUp.rawDoors()[Directions.DOWN]=null;
			myPitUp.rawExits()[Directions.DOWN]=null;
			myPit.rawDoors()[Directions.UP]=null;
			myPit.rawExits()[Directions.UP]=null;
			*/
			if(myPit!=null) myPit.destroyRoom();
			CMMap.delRoom(myPit);
			if(myPitUp!=null) myPitUp.destroyRoom();
			CMMap.delRoom(myPitUp);
		}
		super.unInvoke();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID==MudHost.TICK_TRAP_RESET)
		{
			sprung=false;
			return false;
		}
		else
		if(tickID==MudHost.TICK_TRAP_DESTRUCTION)
		{
			unInvoke();
			return false;
		}
		return true;
	}

}
