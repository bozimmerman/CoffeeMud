package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class ArchonStaff extends Staff implements Wand
{
	public String ID(){	return "ArchonStaff";}
	private String secretWord="REFRESH, LEVEL UP, BURN!!";
	private static Wand theWand=(Wand)CMClass.getMiscMagic("StdWand");

	public ArchonStaff()
	{
		super();

		name="a wooden staff";
		displayText="a wooden staff lies in the corner of the room.";
		miscText="";
		description="It`s long and wooden, just like a staff ought to be.";
		secretIdentity="The Archon`s Staff of Power!";
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(30);
		baseEnvStats.setWeight(4);
		baseEnvStats().setAttackAdjustment(10);
		baseEnvStats().setDamage(12);
		baseGoldValue=10000;
		recoverEnvStats();
		wornLogicalAnd=true;
		material=EnvResource.RESOURCE_OAK;
		properWornBitmap=Item.HELD|Item.WIELD;
		weaponType=TYPE_BASHING;
		weaponClassification=Weapon.CLASS_STAFF;
		if(theWand==null)
			theWand=(Wand)CMClass.getMiscMagic("StdWand");
	}

	public Environmental newInstance()
	{
		return new ArchonStaff();
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
					mob.location().show(mob,target,Affect.MSG_OK_VISUAL,me.displayName()+" glows brightly at <T-NAME>.");
					while(target.envStats().level()<30)
						target.charStats().getCurrentClass().gainExperience(target,null,null,target.getExpNeededLevel()+1);
				}
				else
				if(message.toUpperCase().indexOf("LEVEL UP")>0)
				{
					mob.location().show(mob,target,Affect.MSG_OK_VISUAL,me.displayName()+" glows brightly at <T-NAME>.");
					target.charStats().getCurrentClass().gainExperience(target,null,null,target.getExpNeededLevel()+1);
					return;
				}
				else
				if(message.toUpperCase().indexOf("REFRESH")>0)
				{
					mob.location().show(mob,target,Affect.MSG_OK_VISUAL,me.displayName()+" glows brightly at <T-NAME>.");
					target.recoverMaxState();
					target.resetToMaxState();
					target.tell("You feel refreshed!");
					return;
				}
				else
				if(message.toUpperCase().indexOf("BURN")>0)
				{
					mob.location().show(mob,target,Affect.MSG_OK_VISUAL,me.displayName()+" wielded by <S-NAME> shoots forth magical green flames at <T-NAME>.");
					int flameDamage = (int) Math.round( Math.random() * 6 );
					flameDamage *= 3;
					ExternalPlay.postDamage(mob,target,null,(++flameDamage),Affect.MASK_GENERAL|Affect.TYP_FIRE,Weapon.TYPE_BURNING,me.displayName()+" <DAMAGE> <T-NAME>!");
					return;
				}
			}
		}
		theWand.waveIfAble(mob,afftarget,message,me);
	}

	public void affectCharState(MOB mob, CharState affectableState)
	{
		super.affectCharState(mob,affectableState);
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
				mob.location().show(mob,null,Affect.MSG_OK_VISUAL,displayName()+" flashes and falls out of <S-HIS-HER> hands!");
				return false;
			}
			break;
		}
		return true;
	}

	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);
		if((affect.source().location()!=null)
		&&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&((affect.targetCode()-Affect.MASK_HURT)>0)
		&&(affect.tool()==this)
		&&(affect.target() instanceof MOB)
		&&(!((MOB)affect.target()).amDead()))
		{
			FullMsg msg=new FullMsg(affect.source(),(MOB)affect.target(),new ArchonStaff(),Affect.MSG_OK_ACTION,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_FIRE,Affect.MSG_NOISYMOVEMENT,null);
			if(affect.source().location().okAffect(affect.source(),msg))
			{
				affect.source().location().send(affect.source(), msg);
				if(!msg.wasModified())
				{
					int flameDamage = (int) Math.round( Math.random() * 6 );
					flameDamage *= baseEnvStats().level();
					ExternalPlay.postDamage(affect.source(),(MOB)affect.target(),null,flameDamage,Affect.TYP_FIRE,Weapon.TYPE_BURNING,displayName()+" shoots a flame which <DAMAGE> <T-NAME>!");
				}
			}
		}
	}
}
