package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.Locales.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.Exits.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import java.util.*;

public class Trap extends StdAbility
{
	public final static MOB benefactor=new StdMOB();
	public boolean sprung=false;
	public Room myPit=null;
	public Room myPitUp=null;

	public final static int TRAP_NEEDLE=0;
	public final static int TRAP_PIT_BLADE=1;
	public final static int TRAP_GAS=2;

	public Trap()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a Trap!";
		displayText="(in a dark realm of thievery)";
		miscText="";
		invoker=benefactor;
		baseEnvStats().setAbility(Dice.roll(1,3,0)-1);
		recoverEnvStats();
	}

	public void gas(MOB mob)
	{
		mob.location().show(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> trigger(s) a trap set in "+affected.name()+"!");
		if(mob.envStats().level()>15)
		{
			mob.location().show(mob,null,Affect.VISUAL_WNOISE,"The room fills with gas!");
			for(int i=0;i<mob.location().numInhabitants();i++)
			{
				MOB target=mob.location().fetchInhabitant(i);
				int dmg=Dice.roll(target.envStats().level(),10,1);
				FullMsg msg=new FullMsg(invoker,target,this,Affect.VISUAL_WNOISE,Affect.STRIKE_GAS,Affect.VISUAL_WNOISE,null);
				if(target.location().okAffect(msg))
				{
					target.location().send(target,msg);
					if(msg.wasModified())
						dmg=(int)Math.round(Util.div(dmg,2.0));
					target.location().show(target,null,Affect.VISUAL_WNOISE,"The gas "+TheFight.hitWord(-1,dmg)+" <S-NAME>!");
					TheFight.doDamage(target,dmg);
				}
			}
		}
		else
		{
			MOB target=mob;
			int dmg=Dice.roll(target.envStats().level(),10,1);
			FullMsg msg=new FullMsg(invoker,target,this,Affect.VISUAL_WNOISE,Affect.STRIKE_GAS,Affect.VISUAL_WNOISE,null);
			if(target.location().okAffect(msg))
			{
				target.location().send(target,msg);
				if(msg.wasModified())
					dmg=(int)Math.round(Util.div(dmg,2.0));
				target.location().show(target,null,Affect.VISUAL_WNOISE,"A sudden blast of gas "+TheFight.hitWord(-1,dmg)+" <S-NAME>");
				TheFight.doDamage(target,dmg);
			}
		}
	}

	public void needle(MOB mob)
	{
		mob.location().show(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> trigger(s) a needle trap set in "+affected.name()+"!");
		MOB target=mob;
		int dmg=Dice.roll(target.envStats().level(),5,1);
		FullMsg msg=new FullMsg(invoker,target,this,Affect.VISUAL_WNOISE,Affect.STRIKE_POISON,Affect.VISUAL_WNOISE,null);
		if(target.location().okAffect(msg))
		{
			target.location().send(target,msg);
			if(msg.wasModified())
				dmg=(int)Math.round(Util.div(dmg,2.0));
			target.location().show(target,null,Affect.VISUAL_WNOISE,"The needle "+TheFight.hitWord(-1,dmg)+" <S-NAME>!");

			TheFight.doDamage(target,dmg);

			if((!msg.wasModified())&&(target.envStats().level()>15))
			{
				target.location().show(target,null,Affect.VISUAL_WNOISE,"<S-NAME> turn(s) green!");
				Poison P=new Poison();
				P.maliciousAffect(invoker,target,target.envStats().level()*4,-1);
			}
		}
	}

	public void blade(MOB mob)
	{
		mob.location().show(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> trigger(s) a blade trap set in "+affected.name()+"!");
		MOB target=mob;
		int dmg=Dice.roll(target.envStats().level(),8,0);
		FullMsg msg=new FullMsg(invoker,target,this,Affect.VISUAL_WNOISE,Affect.STRIKE_POISON,Affect.VISUAL_WNOISE,null);
		if(target.location().okAffect(msg))
		{
			target.location().send(target,msg);
			if(msg.wasModified())
				dmg=(int)Math.round(Util.div(dmg,2.0));
			target.location().show(target,null,Affect.VISUAL_WNOISE,"The blade "+TheFight.hitWord(-1,dmg)+" <S-NAME>!");

			TheFight.doDamage(target,dmg);

			if(!msg.wasModified())
			{
				target.location().show(target,null,Affect.VISUAL_WNOISE,"<S-NAME> turn(s) green!");
				Poison P=new Poison();
				P.maliciousAffect(invoker,target,target.envStats().level()*2,-1);
			}
		}
	}

	public void fallInPit(MOB mob)
	{
		if(Sense.isFlying(mob))
		{
			mob.location().show(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> trigger(s) a trap door beneath <S-HIS-HER> feet! <S-NAME> pause(s) over it in flight.");
			return;
		}
		else
			mob.location().show(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> trigger(s) a trap door beneath <S-HIS-HER> feet! <S-NAME> fall(s) in!");
		if(myPit==null)
		{
			myPitUp=new ClimbableSurface();
			myPitUp.baseEnvStats().setDisposition(Sense.IS_DARK);
			myPitUp.setDisplayText("Inside a dark pit");
			myPitUp.setDescription("The walls here are slick and tall.  The trap door has already closed.");
			myPitUp.exits()[Directions.UP]=new StdLockedDoorway();
			myPitUp.doors()[Directions.UP]=mob.location();
			myPitUp.recoverEnvStats();

			myPit=new StdRoom();
			myPit.baseEnvStats().setDisposition(Sense.IS_DARK);
			myPit.setDisplayText("Inside a dark pit");
			myPit.setDescription("The walls here are slick and tall.  You can barely see the closed trap door well above you.");
			myPit.exits()[Directions.UP]=new StdOpenDoorway();
			myPit.doors()[Directions.UP]=myPitUp;
			myPitUp.recoverEnvStats();
			
			MUD.map.addElement(myPit);
			MUD.map.addElement(myPitUp);
		}
		myPit.bringMobHere(mob,false);
		if(mob.fetchAffect(new Spell_FeatherFall().ID())!=null)
			mob.location().show(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> float(s) gently into the pit!");
		else
		{
			mob.location().show(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> hit(s) the pit floor with a THUMP!");
			TheFight.doDamage(mob,Dice.roll(mob.envStats().level(),3,1));
		}
		BasicSenses.look(mob,null,true);
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
			target.location().show(target,null,Affect.VISUAL_WNOISE,"<S-NAME> trigger(s) a trap, but it appears to have misfired.");
			break;
		}

		if((envStats().rejuv()>0)&&(envStats().rejuv()<Integer.MAX_VALUE))
			ServiceEngine.startTickDown(this,ServiceEngine.TRAP_RESET,envStats().rejuv());
		else
			unInvoke();
	}

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;
		if(tickID==ServiceEngine.TRAP_RESET)
		{
			sprung=false;
			return false;
		}
		else
		if(tickID==ServiceEngine.TRAP_DESTRUCTION)
		{
			unInvoke();
			return false;
		}
		return true;
	}

	public Environmental newInstance()
	{
		return new Trap();
	}

}
