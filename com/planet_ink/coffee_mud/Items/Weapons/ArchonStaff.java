package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.Items.MiscMagic.StdWand;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


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
public class ArchonStaff extends Staff implements Wand, ArchonOnly
{
	public String ID(){	return "ArchonStaff";}
	private static Wand theWand=(Wand)CMClass.getMiscMagic("StdWand");

	public ArchonStaff()
	{
		super();

		setName("a wooden staff");
		setDisplayText("a wooden staff lies in the corner of the room.");
		setDescription("It`s long and wooden, just like a staff ought to be.");
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

	public int maxUses(){return Integer.MAX_VALUE;}
	public void setMaxUses(int newMaxUses){}
	
	public void setSpell(Ability theSpell)
	{
		super.setSpell(theSpell);
	}
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
	}

	public void waveIfAble(MOB mob,
						   Environmental afftarget,
						   String message)
	{
		if((mob.isMine(this))
		   &&(!this.amWearingAt(Item.INVENTORY)))
		{
			if((mob.location()!=null)&&(afftarget!=null)&&(afftarget instanceof MOB))
			{
				MOB target=(MOB)afftarget;
				if(message.toUpperCase().indexOf("LEVEL ALL UP")>0)
				{
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,this.name()+" glows brightly at <T-NAME>.");
					int destLevel=CommonStrings.getIntVar(CommonStrings.SYSTEMI_LASTPLAYERLEVEL);
					if(destLevel==0) destLevel=30;
					if(destLevel<=target.baseEnvStats().level())
						destLevel=100;
					if((target.charStats().getCurrentClass().leveless())
					||(target.charStats().getMyRace().leveless())
					||(CMSecurity.isDisabled("LEVELS")))
					    mob.tell("The wand will not work on such as "+target.name()+".");
					else
					while(target.baseEnvStats().level()<destLevel)
					{
						if((target.getExpNeededLevel()==Integer.MAX_VALUE)
						||(target.charStats().getCurrentClass().expless())
						||(target.charStats().getMyRace().expless()))
							target.charStats().getCurrentClass().level(target);
						else
							MUDFight.postExperience(target,null,null,target.getExpNeededLevel()+1,false);
					}
				}
				else
				if(message.toUpperCase().indexOf("LEVEL UP")>0)
				{
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,this.name()+" glows brightly at <T-NAME>.");
					if((target.charStats().getCurrentClass().leveless())
					||(target.charStats().getMyRace().leveless())
					||(CMSecurity.isDisabled("LEVELS")))
					    mob.tell("The wand will not work on such as "+target.name()+".");
					else
					if((target.getExpNeededLevel()==Integer.MAX_VALUE)
					||(target.charStats().getCurrentClass().expless())
					||(target.charStats().getMyRace().expless()))
						target.charStats().getCurrentClass().level(target);
					else
						MUDFight.postExperience(target,null,null,target.getExpNeededLevel()+1,false);
					return;
				}
				else
				if(message.toUpperCase().indexOf("LEVEL DOWN")>0)
				{
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,this.name()+" glows brightly at <T-NAME>.");
					if((target.charStats().getCurrentClass().leveless())
					||(target.charStats().getMyRace().leveless())
					||(CMSecurity.isDisabled("LEVELS")))
					    mob.tell("The wand will not work on such as "+target.name()+".");
					else
					if((target.getExpNeededLevel()==Integer.MAX_VALUE)
					||(target.charStats().getCurrentClass().expless())
					||(target.charStats().getMyRace().expless()))
						target.charStats().getCurrentClass().unLevel(target);
					else
						MUDFight.postExperience(target,null,null,target.getExpNeededLevel()*-1,false);
					return;
				}
				else
				if(message.toUpperCase().indexOf("REFRESH")>0)
				{
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,this.name()+" glows brightly at <T-NAME>.");
					target.recoverMaxState();
					target.resetToMaxState();
					target.tell("You feel refreshed!");
					return;
				}
				else
				if(message.toUpperCase().indexOf("BURN")>0)
				{
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,this.name()+" wielded by <S-NAME> shoots forth magical green flames at <T-NAME>.");
					int flameDamage = (int) Math.round( Math.random() * 6 );
					flameDamage *= 3;
					MUDFight.postDamage(mob,target,null,(++flameDamage),CMMsg.MASK_GENERAL|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,(this.name()+" <DAMAGE> <T-NAME>!")+CommonStrings.msp("fireball.wav",30));
					return;
				}
			}
		}
		StdWand.waveIfAble(mob,afftarget,message,this);
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

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.source().location()!=null)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&((msg.value())>0)
		&&(msg.tool()==this)
		&&(msg.target() instanceof MOB)
		&&(!((MOB)msg.target()).amDead()))
		{
			FullMsg msg2=new FullMsg(msg.source(),msg.target(),new ArchonStaff(),CMMsg.MSG_OK_ACTION,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_FIRE,CMMsg.MSG_NOISYMOVEMENT,null);
			if(msg.source().location().okMessage(msg.source(),msg2))
			{
				msg.source().location().send(msg.source(), msg2);
				if(msg2.value()<=0)
				{
					int flameDamage = (int) Math.round( Math.random() * 6 );
					flameDamage *= baseEnvStats().level();
					if(!((MOB)msg.target()).amDead())
						MUDFight.postDamage(msg.source(),(MOB)msg.target(),null,flameDamage,CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,name()+" shoots a flame which <DAMAGE> <T-NAME>!");
				}
			}
		}
	}
}
