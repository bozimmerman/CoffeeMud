package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Trap_Trap extends StdAbility implements Trap
{
	protected static MOB benefactor=(MOB)CMClass.getMOB("StdMOB");
	protected boolean sprung=false;
	protected Room myPit=null;
	protected Room myPitUp=null;

	public Trap_Trap()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a Trap!";
		displayText="(in a dark realm of thievery)";
		miscText="";
		invoker=benefactor;
		baseEnvStats().setAbility(Dice.roll(1,3,0)-1);
		recoverEnvStats();
		if(benefactor==null)
			benefactor=(MOB)CMClass.getMOB("StdMOB").newInstance();
	}

	public boolean sprung()
	{
		return sprung;
	}
	public void setSprung(boolean isSprung)
	{
		sprung=isSprung;
	}

	public Trap getATrap(Environmental unlockThis)
	{
		Trap theTrap=null;
		int roll=Dice.rollPercentage();
		if(unlockThis instanceof Exit)
		{
			if(((Exit)unlockThis).hasADoor())
			{
				if(((Exit)unlockThis).hasALock())
				{
					if(roll<20)
						theTrap=new Trap_Open();
					else
					if(roll<80)
						theTrap=new Trap_Unlock();
					else
						theTrap=new Trap_Enter();
				}
				else
				{
					if(roll<50)
						theTrap=new Trap_Open();
					else
						theTrap=new Trap_Enter();
				}
			}
			else
				theTrap=new Trap_Enter();
		}
		else
		if(unlockThis instanceof Container)
		{
			if(((Container)unlockThis).hasALid())
			{
				if(((Container)unlockThis).hasALock())
				{
					if(roll<20)
						theTrap=new Trap_Open();
					else
					if(roll<80)
						theTrap=new Trap_Unlock();
					else
						theTrap=new Trap_Get();
				}
				else
				{
					if(roll<50)
						theTrap=new Trap_Open();
					else
						theTrap=new Trap_Get();
				}
			}
			else
				theTrap=new Trap_Get();
		}
		else
		if(unlockThis instanceof Item)
			theTrap=new Trap_Get();
		return theTrap;
	}

	public Trap fetchMyTrap(Environmental myThang)
	{
		for(int a=0;a<myThang.numAffects();a++)
		{
			Ability A=myThang.fetchAffect(a);
			if(A instanceof  Trap)
				return (Trap)A;
		}
		return null;
	}

	public void setTrapped(Environmental myThang, boolean isTrapped)
	{
		Trap t=getATrap(myThang);
		t.baseEnvStats().setRejuv(50);
		t.recoverEnvStats();
		setTrapped(myThang,t,isTrapped);
	}
	public void setTrapped(Environmental myThang, Trap theTrap, boolean isTrapped)
	{
		for(int a=0;a<myThang.numAffects();a++)
		{
			Ability A=myThang.fetchAffect(a);
			if(A instanceof Trap)
				A.unInvoke();
		}

		if((isTrapped)&&(myThang.fetchAffect(theTrap.ID())==null))
			myThang.addAffect(theTrap);
	}

	public void gas(MOB mob)
	{
		mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> trigger(s) a trap set in "+affected.name()+"!");
		if(mob.envStats().level()>15)
		{
			mob.location().show(mob,null,Affect.MSG_OK_ACTION,"The room fills with gas!");
			for(int i=0;i<mob.location().numInhabitants();i++)
			{
				MOB target=mob.location().fetchInhabitant(i);
				int dmg=Dice.roll(target.envStats().level(),10,1);
				FullMsg msg=new FullMsg(invoker,target,this,Affect.MSG_OK_ACTION,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_GAS,Affect.MSG_NOISYMOVEMENT,null);
				if(target.location().okAffect(msg))
				{
					target.location().send(target,msg);
					if(msg.wasModified())
						dmg=(int)Math.round(Util.div(dmg,2.0));
					target.location().show(target,null,Affect.MSG_OK_ACTION,"The gas "+ExternalPlay.hitWord(-1,dmg)+" <S-NAME>!");
					ExternalPlay.postDamage(mob,target,this,dmg);
				}
			}
		}
		else
		{
			MOB target=mob;
			int dmg=Dice.roll(target.envStats().level(),10,1);
			FullMsg msg=new FullMsg(invoker,target,this,Affect.MSG_OK_ACTION,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_GAS,Affect.MSG_NOISYMOVEMENT,null);
			if(target.location().okAffect(msg))
			{
				target.location().send(target,msg);
				if(msg.wasModified())
					dmg=(int)Math.round(Util.div(dmg,2.0));
				target.location().show(target,null,Affect.MSG_OK_ACTION,"A sudden blast of gas "+ExternalPlay.hitWord(-1,dmg)+" <S-NAME>");
				ExternalPlay.postDamage(target,target,this,dmg);
			}
		}
	}

	public void needle(MOB mob)
	{
		mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> trigger(s) a needle trap set in "+affected.name()+"!");
		MOB target=mob;
		int dmg=Dice.roll(target.envStats().level(),5,1);
		FullMsg msg=new FullMsg(invoker,target,this,Affect.MSG_OK_ACTION,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_JUSTICE,Affect.MSG_NOISYMOVEMENT,null);
		if(target.location().okAffect(msg))
		{
			target.location().send(target,msg);
			if(msg.wasModified())
				dmg=(int)Math.round(Util.div(dmg,2.0));
			target.location().show(target,null,Affect.MSG_OK_ACTION,"The needle "+ExternalPlay.hitWord(-1,dmg)+" <S-NAME>!");
			ExternalPlay.postDamage(target,target,this,dmg);

			Ability P=CMClass.getAbility("Poison");
			P.invoke(invoker,target,true);
		}
	}

	public void blade(MOB mob)
	{
		mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> trigger(s) a blade trap set in "+affected.name()+"!");
		MOB target=mob;
		int dmg=Dice.roll(target.envStats().level(),2,0);
		FullMsg msg=new FullMsg(invoker,target,this,Affect.MSG_OK_ACTION,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_JUSTICE,Affect.MSG_NOISYMOVEMENT,null);
		if(target.location().okAffect(msg))
		{
			target.location().send(target,msg);
			if(msg.wasModified())
				dmg=(int)Math.round(Util.div(dmg,2.0));
			target.location().show(target,null,Affect.MSG_OK_ACTION,"The blade "+ExternalPlay.hitWord(-1,dmg)+" <S-NAME>!");
			Ability P=CMClass.getAbility("Poison");
			P.invoke(invoker,target,true);
			ExternalPlay.postDamage(target,target,this,dmg);
		}
	}

	public void fallInPit(MOB mob)
	{
		if(Sense.isFlying(mob))
		{
			mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> trigger(s) a trap door beneath <S-HIS-HER> feet! <S-NAME> pause(s) over it in flight.");
			return;
		}
		else
			mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> trigger(s) a trap door beneath <S-HIS-HER> feet! <S-NAME> fall(s) in!");
		if(myPit==null)
		{
			myPitUp=CMClass.getLocale("ClimbableSurface");
			myPitUp.baseEnvStats().setDisposition(myPitUp.baseEnvStats().disposition()|Sense.IS_DARK);
			myPitUp.setDisplayText("Inside a dark pit");
			myPitUp.setDescription("The walls here are slick and tall.  The trap door has already closed.");
			myPitUp.exits()[Directions.UP]=CMClass.getExit("StdLockedDoorway");
			myPitUp.doors()[Directions.UP]=mob.location();
			myPitUp.recoverEnvStats();

			myPit=CMClass.getLocale("StdRoom");
			myPit.baseEnvStats().setDisposition(myPit.baseEnvStats().disposition()|Sense.IS_DARK);
			myPit.setDisplayText("Inside a dark pit");
			myPit.setDescription("The walls here are slick and tall.  You can barely see the closed trap door well above you.");
			myPit.exits()[Directions.UP]=CMClass.getExit("StdOpenDoorway");
			myPit.doors()[Directions.UP]=myPitUp;
			myPitUp.recoverEnvStats();

			CMMap.map.addElement(myPit);
			CMMap.map.addElement(myPitUp);
		}
		myPit.bringMobHere(mob,false);
		if(mob.fetchAffect("Spell_FeatherFall")!=null)
			mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> float(s) gently into the pit!");
		else
		{
			mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> hit(s) the pit floor with a THUMP!");
			int damage=Dice.roll(mob.envStats().level(),3,1);
			ExternalPlay.postDamage(mob,mob,this,damage);
		}
		ExternalPlay.look(mob,null,true);
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
		benefactor.recoverEnvStats();
		if(invoker==null)
			invoker=benefactor;
		switch(envStats().ability())
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
		default:
			target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> trigger(s) a trap, but it appears to have misfired.");
			break;
		}

		if((envStats().rejuv()>0)&&(envStats().rejuv()<Integer.MAX_VALUE))
			ExternalPlay.startTickDown(this,Host.TRAP_RESET,envStats().rejuv());
		else
			unInvoke();
	}

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
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

	public Environmental newInstance()
	{
		return new Trap_Trap();
	}

}
