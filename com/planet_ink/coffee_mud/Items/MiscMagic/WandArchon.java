package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class WandArchon extends StdWand
{
	public String ID(){	return "WandArchon";}
	public WandArchon()
	{
		super();

		name="a flashy wand";
		displayText="a flashy wand has been left here.";
		description="A wand made out of sparkling energy.";
		secretIdentity="The Wand of the Archons!";
		this.setUsesRemaining(99999);
		baseGoldValue=20000;
		material=EnvResource.RESOURCE_OAK;
		recoverEnvStats();
		secretWord="REFRESH, LEVEL UP, BURN!!";
	}

	public Environmental newInstance()
	{
		return new WandArchon();
	}

	public void setSpell(Ability theSpell)
	{
		super.setSpell(theSpell);
		secretWord="REFRESH, LEVEL UP, BURN!!";
	}
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		secretWord="REFRESH, LEVEL UP, BURN!!";
	}

	public void affectCharState(MOB mob, CharState affectableState)
	{
		if(!amWearingAt(Item.INVENTORY))
		{
			affectableState.setHunger(99999999);
			affectableState.setThirst(99999999);
			mob.curState().setHunger(9999999);
			mob.curState().setThirst(9999999);
		}
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;

		MOB mob=affect.source();
		if(mob.location()==null)
			return true;

		if(affect.amITarget(this))
		switch(affect.targetMinor())
		{
		case Affect.TYP_HOLD:
		case Affect.TYP_WEAR:
		case Affect.TYP_WIELD:
		case Affect.TYP_GET:
			if(mob.charStats().getClassLevel("Archon")<0)
			{
				mob.location().show(mob,null,Affect.MSG_OK_VISUAL,name()+" flashes and falls out of <S-HIS-HER> hands!");
				return false;
			}
			break;
		}
		return true;
	}

	public void waveIfAble(MOB mob,
						   Environmental afftarget,
						   String message,
						   Wand me)
	{
		if((mob.isMine(me))
		   &&(!me.amWearingAt(Item.INVENTORY)))
		{
			if((mob.location()!=null)&&(afftarget!=null)&&(afftarget instanceof MOB))
			{
				MOB target=(MOB)afftarget;
				if(message.toUpperCase().indexOf("LEVEL ALL UP")>0)
				{
					mob.location().show(mob,target,Affect.MSG_OK_VISUAL,me.name()+" glows brightly at <T-NAME>.");
					while(target.envStats().level()<30)
						target.charStats().getCurrentClass().gainExperience(target,null,null,target.getExpNeededLevel()+1);
				}
				else
				if(message.toUpperCase().indexOf("LEVEL UP")>0)
				{
					mob.location().show(mob,target,Affect.MSG_OK_VISUAL,me.name()+" glows brightly at <T-NAME>.");
					target.charStats().getCurrentClass().gainExperience(target,null,null,target.getExpNeededLevel()+1);
					return;
				}
				else
				if(message.toUpperCase().indexOf("REFRESH")>0)
				{
					mob.location().show(mob,target,Affect.MSG_OK_VISUAL,me.name()+" glows brightly at <T-NAME>.");
					target.recoverMaxState();
					target.resetToMaxState();
					target.tell("You feel refreshed!");
					return;
				}
				else
				if(message.toUpperCase().indexOf("BURN")>0)
				{
					mob.location().show(mob,target,Affect.MSG_OK_VISUAL,me.name()+" wielded by <S-NAME> shoots forth magical green flames at <T-NAME>.");
					int flameDamage = (int) Math.round( Math.random() * 6 );
					flameDamage *= 3;
					ExternalPlay.postDamage(mob,target,null,(++flameDamage),Affect.MASK_GENERAL|Affect.TYP_FIRE,Weapon.TYPE_BURNING,(me.name()+" <DAMAGE> <T-NAME>!")+CommonStrings.msp("fireball.wav",30));
					return;
				}
			}
		}
		super.waveIfAble(mob,afftarget,message,me);
	}
}
