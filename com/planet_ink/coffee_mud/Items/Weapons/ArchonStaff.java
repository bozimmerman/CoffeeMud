package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class ArchonStaff extends Staff implements Wand
{
	private String secretWord="REFRESH, BURN!!";
	private static Wand theWand=(Wand)CMClass.getMiscMagic("StdWand");

	public ArchonStaff()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
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
						target.charStats().getMyClass().gainExperience(target,null,null,target.getExpNeededLevel()+1);
				}
				else
				if(message.toUpperCase().indexOf("LEVEL UP")>0)
				{
					mob.location().show(mob,target,Affect.MSG_OK_VISUAL,me.name()+" glows brightly at <T-NAME>.");
					target.charStats().getMyClass().gainExperience(target,null,null,target.getExpNeededLevel()+1);
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
					ExternalPlay.postDamage(mob,target,null,(++flameDamage),Affect.ACT_GENERAL|Affect.TYP_FIRE,Weapon.TYPE_BURNING,me.name()+" <DAMAGE> <T-NAME>!");
					return;
				}
			}
		}
		theWand.waveIfAble(mob,afftarget,message,me);
	}
	public void affect(Affect affect)
	{
		super.affect(affect);
		if((affect.source().location()!=null)
		&&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&(affect.tool()==this)
		&&(affect.target() instanceof MOB)
		&&(!((MOB)affect.target()).amDead()))
		{
			FullMsg msg=new FullMsg(affect.source(),(MOB)affect.target(),new ArchonStaff(),Affect.MSG_OK_ACTION,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_FIRE,Affect.MSG_NOISYMOVEMENT,null);
			if(affect.source().location().okAffect(msg))
			{
				affect.source().location().send(affect.source(), msg);
				if(!msg.wasModified())
				{
					int flameDamage = (int) Math.round( Math.random() * 6 );
					flameDamage *= baseEnvStats().level();
					affect.addTrailerMsg(new FullMsg(affect.source(),(MOB)affect.target(),Affect.MSG_OK_ACTION,name()+" "+CommonStrings.standardHitWord(Weapon.TYPE_BURNING,flameDamage)+" <T-NAME>!"));
					affect.addTrailerMsg(new FullMsg(affect.source(),(MOB)affect.target(),null,Affect.NO_EFFECT,Affect.MASK_HURT+flameDamage,Affect.NO_EFFECT,null));
				}
			}
		}
	}
}
