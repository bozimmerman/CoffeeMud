package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class WandArchon extends StdWand implements ArchonOnly
{
	public String ID(){	return "WandArchon";}
	public WandArchon()
	{
		super();

		setName("a flashy wand");
		setDisplayText("a flashy wand has been left here.");
		setDescription("A wand made out of sparkling energy.");
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

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		MOB mob=msg.source();
		if(mob.location()==null)
			return true;

		if(msg.amITarget(this))
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_HOLD:
		case CMMsg.TYP_WEAR:
		case CMMsg.TYP_WIELD:
		case CMMsg.TYP_GET:
			if(mob.charStats().getClassLevel("Archon")<0)
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,name()+" flashes and falls out of <S-HIS-HER> hands!");
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
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,me.name()+" glows brightly at <T-NAME>.");
					int destLevel=CommonStrings.getIntVar(CommonStrings.SYSTEMI_LASTPLAYERLEVEL);
					if(destLevel==0) destLevel=30;
					if(destLevel<=target.baseEnvStats().level())
						destLevel=100;
					while(target.baseEnvStats().level()<destLevel)
					{
						if(target.getExpNeededLevel()==Integer.MAX_VALUE)
							target.charStats().getCurrentClass().level(target);
						else
							ExternalPlay.postExperience(target,null,null,target.getExpNeededLevel()+1,false);
					}
				}
				else
				if(message.toUpperCase().indexOf("LEVEL UP")>0)
				{
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,me.name()+" glows brightly at <T-NAME>.");
					if(target.getExpNeededLevel()==Integer.MAX_VALUE)
						target.charStats().getCurrentClass().level(target);
					else
						ExternalPlay.postExperience(target,null,null,target.getExpNeededLevel()+1,false);
					return;
				}
				else
				if(message.toUpperCase().indexOf("REFRESH")>0)
				{
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,me.name()+" glows brightly at <T-NAME>.");
					target.recoverMaxState();
					target.resetToMaxState();
					target.tell("You feel refreshed!");
					return;
				}
				else
				if(message.toUpperCase().indexOf("BURN")>0)
				{
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,me.name()+" wielded by <S-NAME> shoots forth magical green flames at <T-NAME>.");
					int flameDamage = (int) Math.round( Math.random() * 6 );
					flameDamage *= 3;
					ExternalPlay.postDamage(mob,target,null,(++flameDamage),CMMsg.MASK_GENERAL|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,(me.name()+" <DAMAGE> <T-NAME>!")+CommonStrings.msp("fireball.wav",30));
					return;
				}
			}
		}
		super.waveIfAble(mob,afftarget,message,me);
	}
}
