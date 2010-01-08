package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
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
public class StdWeapon extends StdItem implements Weapon
{
	public String ID(){	return "StdWeapon";}
	protected int weaponType=TYPE_NATURAL;
	protected int weaponClassification=CLASS_NATURAL;
	protected boolean useExtendedMissString=false;
	protected int minRange=0;
	protected int maxRange=0;
	protected int ammoCapacity=0;

	public StdWeapon()
	{
		super();

		setName("weapon");
		setDisplayText(" sits here.");
		setDescription("This is a deadly looking weapon.");
		wornLogicalAnd=false;
		properWornBitmap=Wearable.WORN_HELD|Wearable.WORN_WIELD;
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(0);
		baseEnvStats().setAbility(0);
		baseGoldValue=15;
		material=RawMaterial.RESOURCE_STEEL;
		setUsesRemaining(100);
		recoverEnvStats();
	}


	public int weaponType(){return weaponType;}
	public int weaponClassification(){return weaponClassification;}
	public void setWeaponType(int newType){weaponType=newType;}
	public void setWeaponClassification(int newClassification){weaponClassification=newClassification;}

	public String secretIdentity()
	{
		String id=super.secretIdentity();
		if(envStats().ability()>0)
			id=name()+" +"+envStats().ability()+((id.length()>0)?"\n":"")+id;
		else
		if(envStats().ability()<0)
			id=name()+" "+envStats().ability()+((id.length()>0)?"\n":"")+id;
		return id+"\n\rAttack: "+envStats().attackAdjustment()+", Damage: "+envStats().damage();
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(amWearingAt(Wearable.WORN_WIELD))
		{
            if(envStats().attackAdjustment()!=0)
    			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(envStats().attackAdjustment()));
			if(envStats().damage()!=0)
				affectableStats.setDamage(affectableStats.damage()+envStats().damage());
		}
	}
	public void recoverEnvStats()
	{
		super.recoverEnvStats();
        if(envStats().damage()!=0)
        {
            envStats().setDamage(envStats().damage()+(envStats().ability()*2));
            envStats().setAttackAdjustment(envStats().attackAdjustment()+(envStats().ability()*10));
        }
		if((subjectToWearAndTear())&&(usesRemaining()<100))
			envStats().setDamage(((int)Math.round(CMath.mul(envStats().damage(),CMath.div(usesRemaining(),100)))));
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if((msg.amITarget(this))
		&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
		&&(CMLib.flags().canBeSeenBy(this,msg.source())))
		{
			if(requiresAmmunition())
				msg.source().tell(ammunitionType()+" remaining: "+ammunitionRemaining()+"/"+ammunitionCapacity()+".");
			if((subjectToWearAndTear())&&(usesRemaining()<100))
				msg.source().tell(weaponHealth());
		}
		else
		if((msg.tool()==this)
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(weaponClassification()==Weapon.CLASS_THROWN))
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),this,CMMsg.MSG_DROP,null));

		if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.tool()==this)
		&&(amWearingAt(Wearable.WORN_WIELD))
		&&(weaponClassification()!=Weapon.CLASS_NATURAL)
		&&(weaponType()!=Weapon.TYPE_NATURAL)
		&&(msg.target()!=null)
		&&(msg.target() instanceof MOB)
		&&((msg.value())>0)
		&&(owner()!=null)
		&&(owner() instanceof MOB)
		&&(msg.amISource((MOB)owner())))
		{
			int hurt=(msg.value());
			MOB tmob=(MOB)msg.target();
			if((hurt>(tmob.maxState().getHitPoints()/10)||(hurt>50))
			&&(tmob.curState().getHitPoints()>hurt))
			{
				if((!tmob.isMonster())
				   &&(CMLib.dice().rollPercentage()==1)
				   &&(CMLib.dice().rollPercentage()>(tmob.charStats().getStat(CharStats.STAT_CONSTITUTION)*4))
				   &&(!CMSecurity.isDisabled("AUTODISEASE")))
				{
					Ability A=null;
					if(subjectToWearAndTear()
					&&(usesRemaining()<25)
					&&((material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL))
					{
						if(CMLib.dice().rollPercentage()>50)
							A=CMClass.getAbility("Disease_Lockjaw");
						else
							A=CMClass.getAbility("Disease_Tetanus");
					}
					else
						A=CMClass.getAbility("Disease_Infection");

					if((A!=null)&&(msg.target().fetchEffect(A.ID())==null))
						A.invoke(msg.source(),msg.target(),true,envStats().level());
				}
			}

			if((subjectToWearAndTear())
			&&(CMLib.dice().rollPercentage()==1)
			&&(msg.source().rangeToTarget()==0)
			&&(CMLib.dice().rollPercentage()>((envStats().level()/2)+(10*envStats().ability())+(CMLib.flags().isABonusItems(this)?20:0)))
			&&((material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_ENERGY))
			{
				setUsesRemaining(usesRemaining()-1);
				recoverEnvStats();
				if((usesRemaining()<10)
				&&(owner()!=null)
				&&(owner() instanceof MOB)
				&&(usesRemaining()>0))
					((MOB)owner()).tell(name()+" is nearly destroyed! ("+usesRemaining()+"%");
				else
				if((usesRemaining()<=0)
				&&(owner()!=null)
				&&(owner() instanceof MOB))
				{
					MOB owner=(MOB)owner();
					setUsesRemaining(100);
					msg.addTrailerMsg(CMClass.getMsg(((MOB)owner()),null,null,CMMsg.MSG_OK_VISUAL,"^I"+name()+" is destroyed!!^?",CMMsg.NO_EFFECT,null,CMMsg.MSG_OK_VISUAL,"^I"+name()+" being wielded by <S-NAME> is destroyed!^?"));
					unWear();
					destroy();
					owner.recoverEnvStats();
					owner.recoverCharStats();
					owner.recoverMaxState();
                    if(owner.location()!=null)
    					owner.location().recoverRoomStats();
				}
			}
		}
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		   &&(msg.tool()==this)
		   &&(requiresAmmunition())
		   &&(ammunitionCapacity()>0))
		{
			if(ammunitionRemaining()>ammunitionCapacity())
				setAmmoRemaining(ammunitionCapacity());
			if(ammunitionRemaining()<=0)
			{
				boolean reLoaded=false;

				if((msg.source().isMine(this))
				   &&(msg.source().location()!=null)
				   &&(CMLib.flags().aliveAwakeMobile(msg.source(),true)))
				{
					boolean recover=false;

					for(int a=0;a<numEffects();a++)
					{
						Ability A=fetchEffect(a);
						if((A!=null)&&(!A.savable())&&(A.invoker()==null))
						{
							recover=true;
							delEffect(A);
						}
					}

					MOB mob=msg.source();
					for(int i=0;i<mob.inventorySize();i++)
					{
						Item I=mob.fetchInventory(i);
						if((I instanceof Ammunition)
						&&(I.usesRemaining()>0)
						&&(I.usesRemaining()<Integer.MAX_VALUE)
						&&(I.container()==null)
						&&(((Ammunition)I).ammunitionType().equalsIgnoreCase(ammunitionType())))
						{
							if(mob.location().show(mob,this,I,CMMsg.MSG_RELOAD,"<S-NAME> load(s) <T-NAME> from <O-NAME>."))
							{
								int howMuchToTake=ammunitionCapacity();
								if(I.usesRemaining()<howMuchToTake)
									howMuchToTake=I.usesRemaining();
								setAmmoRemaining(howMuchToTake);
								I.setUsesRemaining(I.usesRemaining()-howMuchToTake);
								for(int a=0;a<I.numEffects();a++)
								{
									Ability A=I.fetchEffect(a);
									if((A!=null)&&(A.savable())&&(fetchEffect(A.ID())==null))
									{
										A=(Ability)A.copyOf();
										A.setInvoker(null);
										A.setSavable(false);
										addEffect(A);
										recover=true;
									}
								}

								if(I.usesRemaining()<=0) I.destroy();
								reLoaded=true;
								break;
							}
						}
					}
					if(recover)
					{
						mob.recoverEnvStats();
						mob.recoverCharStats();
						mob.recoverMaxState();
					}
				}
				if(!reLoaded)
				{
					setAmmoRemaining(0);
					msg.source().tell("You have no more "+ammunitionType()+".");
					CMLib.commands().postRemove(msg.source(),this,false);
					return false;
				}
			}
			else
				setUsesRemaining(usesRemaining()-1);
		}
		return true;
	}

	public void setUsesRemaining(int newUses)
	{
		if(newUses==Integer.MAX_VALUE)
			newUses=100;
		super.setUsesRemaining(newUses);
	}

	protected String weaponHealth()
	{
		if(usesRemaining()>=100)
			return "";
		else
		if(usesRemaining()>=95)
			return name()+" looks slightly used ("+usesRemaining()+"%)";
		else
		if(usesRemaining()>=85)
		{
			switch(weaponClassification())
			{
			case Weapon.CLASS_AXE:
			case Weapon.CLASS_DAGGER:
			case Weapon.CLASS_EDGED:
			case Weapon.CLASS_POLEARM:
			case Weapon.CLASS_SWORD:
				return name()+" is somewhat dull ("+usesRemaining()+"%)";
			default:
				 return name()+" is somewhat worn ("+usesRemaining()+"%)";
			}
		}
		else
		if(usesRemaining()>=75)
		{
			switch(weaponClassification())
			{
			case Weapon.CLASS_AXE:
			case Weapon.CLASS_DAGGER:
			case Weapon.CLASS_EDGED:
			case Weapon.CLASS_POLEARM:
			case Weapon.CLASS_SWORD:
				return name()+" is dull ("+usesRemaining()+"%)";
			default:
				 return name()+" is worn ("+usesRemaining()+"%)";
			}
		}
		else
		if(usesRemaining()>50)
		{
			switch(weaponClassification())
			{
			case Weapon.CLASS_AXE:
			case Weapon.CLASS_DAGGER:
			case Weapon.CLASS_EDGED:
			case Weapon.CLASS_POLEARM:
			case Weapon.CLASS_SWORD:
				return name()+" has some notches and chinks ("+usesRemaining()+"%)";
			default:
				return name()+" is damaged ("+usesRemaining()+"%)";
			}
		}
		else
		if(usesRemaining()>25)
			return name()+" is heavily damaged ("+usesRemaining()+"%)";
		else
			return name()+" is so damaged, it is practically harmless ("+usesRemaining()+"%)";
	}
	public String missString()
	{
		return CMLib.combat().standardMissString(weaponType,weaponClassification,name(),useExtendedMissString);
	}
	public String hitString(int damageAmount)
	{
		return CMLib.combat().standardHitString(weaponClassification,damageAmount,name());
	}
	public int minRange()
	{
		if(CMath.bset(envStats().sensesMask(),EnvStats.SENSE_ITEMNOMINRANGE))
			return 0;
		return minRange;
	}
	public int maxRange()
	{
		if(CMath.bset(envStats().sensesMask(),EnvStats.SENSE_ITEMNOMAXRANGE))
			return 100;
		return maxRange;
	}
	public void setRanges(int min, int max){minRange=min;maxRange=max;}
	public boolean requiresAmmunition()
	{
		if((readableText()==null)||(this instanceof Wand))
			return false;
		return readableText().length()>0;
	}
	public void setAmmunitionType(String ammo)
	{
		if(!(this instanceof Wand))
			setReadableText(ammo);
	}
	public String ammunitionType()
	{
		return readableText();
	}
	public int ammunitionRemaining()
	{
		return usesRemaining();
	}
	public void setAmmoRemaining(int amount)
	{
		if(amount==Integer.MAX_VALUE)
			amount=20;
		setUsesRemaining(amount);
	}
	public int ammunitionCapacity(){return ammoCapacity;}
	public void setAmmoCapacity(int amount){ammoCapacity=amount;}
	public int value()
	{
		if((subjectToWearAndTear())&&(usesRemaining()<1000))
			return (int)Math.round(CMath.mul(super.value(),CMath.div(usesRemaining(),100)));
		return super.value();
	}
	public boolean subjectToWearAndTear()
	{
		return((!requiresAmmunition())
			&&(!(this instanceof Wand))
			&&(usesRemaining()<=1000)
			&&(usesRemaining()>=0));
	}

}
