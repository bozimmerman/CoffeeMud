package com.planet_ink.coffee_mud.Items.Weapons;

import com.planet_ink.coffee_mud.Items.StdItem;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenAmmunition extends StdItem implements Ammunition
{
	public String ID(){	return "GenAmmunition";}
	protected String	readableText="";
	public GenAmmunition()
	{
		super();

		setName("a batch of arrows");
		setDisplayText("a generic batch of arrows sits here.");
		setUsesRemaining(100);
		setAmmunitionType("arrows");
		setDescription("");
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new GenAmmunition();
	}
	public boolean isGeneric(){return true;}

	public void recoverEnvStats()
	{
		envStats=baseEnvStats.cloneStats();
		if(envStats().ability()>0)
			envStats().setDisposition(envStats().disposition()|EnvStats.IS_BONUS);
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(!(affected instanceof Room))
		{
			if((!amWearingAt(Item.FLOATING_NEARBY))
			&&((!(affected instanceof MOB))||(((MOB)affected).riding()!=this)))
				affectableStats.setWeight(affectableStats.weight()+envStats().weight());
		}
	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats){}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState){}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(destroyed) return false;
		tickStatus=Tickable.STATUS_NOT;
		return true;
	}

	public String text()
	{
		return CoffeeMaker.getPropertiesStr(this,false);
	}
	public String readableText(){return readableText;}
	public void setReadableText(String text)
	{
		if(Sense.isReadable(this)) Sense.setReadable(this,false);
		readableText=text;
	}
	public String ammunitionType(){return readableText;}
	public void setAmmunitionType(String text){}

	public void setMiscText(String newText)
	{
		miscText="";
		CoffeeMaker.setPropertiesStr(this,newText,false);
		recoverEnvStats();
	}
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		MOB mob=msg.source();
		if(!msg.amITarget(this))
			return true;
		else
		if(msg.targetCode()==CMMsg.NO_EFFECT)
			return true;
		else
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_EXAMINESOMETHING:
		case CMMsg.TYP_READSOMETHING:
		case CMMsg.TYP_QUIETMOVEMENT:
		case CMMsg.TYP_NOISYMOVEMENT:
		case CMMsg.TYP_HANDS:
		case CMMsg.TYP_SPEAK:
		case CMMsg.TYP_OK_ACTION:
		case CMMsg.TYP_OK_VISUAL:
		case CMMsg.TYP_DEATH:
		case CMMsg.TYP_NOISE:
			return true;
		case CMMsg.TYP_SIT:
		case CMMsg.TYP_SLEEP:
		case CMMsg.TYP_MOUNT:
		case CMMsg.TYP_DISMOUNT:
		case CMMsg.TYP_ENTER:
			if(this instanceof Rideable)
				return true;
			break;
		case CMMsg.TYP_RELOAD:
			if((this instanceof Weapon)
			&&(((Weapon)this).requiresAmmunition()))
				return true;
			break;
		case CMMsg.TYP_HOLD:
			mob.tell("You can't hold "+name()+".");
			return false;
		case CMMsg.TYP_WEAR:
			mob.tell("You can't wear "+name()+".");
			return false;
		case CMMsg.TYP_WIELD:
			mob.tell("You can't wield "+name()+" as a weapon.");
			return false;
		case CMMsg.TYP_GET:
			if((msg.tool()==null)||(msg.tool() instanceof MOB))
			{
				if((mob.envStats().level()<envStats().level()-(10+(mob.envStats().level()/5)))
				&&(!(mob instanceof ShopKeeper)))
				{
					mob.tell(name()+" is too powerful to endure possessing it.");
					return false;
				}
				if((envStats().weight()>(mob.maxCarry()-mob.envStats().weight()))&&(!mob.isMine(this)))
				{
					mob.tell(name()+" is too heavy.");
					return false;
				}
				return true;
			}
			return true;
		case CMMsg.TYP_REMOVE:
			return true;
		case CMMsg.TYP_DROP:
			if(!mob.isMine(this))
			{
				mob.tell("You don't have that.");
				return false;
			}
			if(!Sense.isUltimatelyDroppable(this))
			{
				mob.tell("You can't seem to let go of "+name()+".");
				return false;
			}
			return true;
		case CMMsg.TYP_THROW:
			if(envStats().weight()>(mob.maxCarry()/5))
			{
				mob.tell(name()+" is too heavy to throw.");
				return false;
			}
			if(!Sense.isUltimatelyDroppable(this))
			{
				mob.tell("You can't seem to let go of "+name()+".");
				return false;
			}
			return true;
		case CMMsg.TYP_BUY:
		case CMMsg.TYP_SELL:
		case CMMsg.TYP_VALUE:
		case CMMsg.TYP_VIEW:
				return true;
		case CMMsg.TYP_DELICATE_HANDS_ACT:
		case CMMsg.TYP_JUSTICE:
		case CMMsg.TYP_WAND_USE:
		case CMMsg.TYP_FIRE: // lighting
		case CMMsg.TYP_WATER: // rust
		case CMMsg.TYP_CAST_SPELL:
		case CMMsg.TYP_POISON: // for use poison
			return true;
		default:
			break;
		}
		mob.tell(mob,this,null,"You can't do that to <T-NAMESELF>.");
		return false;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		MOB mob=msg.source();
		if(!msg.amITarget(this))
			return;
		else
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_EXAMINESOMETHING:
			if(!(this instanceof Container))
			{
				if(Sense.canBeSeenBy(this,mob))
				{
					if(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))
						mob.tell(ID()+"\n\rRejuv :"+baseEnvStats().rejuv()+"\n\rUses  :"+usesRemaining()+"\n\rHeight:"+baseEnvStats().height()+"\n\rAbilty:"+baseEnvStats().ability()+"\n\rLevel :"+baseEnvStats().level()+"\n\rTime  : "+dispossessionTimeLeftString()+"\n\r"+description()+"\n\r"+"\n\rMisc  :'"+text());
					else
					if(description().length()==0)
						mob.tell("You don't see anything special about "+this.name());
					else
						mob.tell(description());
				}
				else
					mob.tell("You can't see that!");
			}
			return;
		case CMMsg.TYP_GET:
			setContainer(null);
			if(Sense.isHidden(this))
				baseEnvStats().setDisposition(baseEnvStats().disposition()&((int)EnvStats.ALLMASK-EnvStats.IS_HIDDEN));
			if(mob.location().isContent(this))
				mob.location().delItem(this);
			if(!mob.isMine(this))
			{
				mob.addInventory(this);
				if(Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
					mob.envStats().setWeight(mob.envStats().weight()+envStats().weight());
			}
			unWear();
			if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
				mob.location().recoverRoomStats();
			break;
		case CMMsg.TYP_REMOVE:
			unWear();
			if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
				mob.location().recoverRoomStats();
			break;
		case CMMsg.TYP_THROW:
			if(mob.isMine(this)
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Room))
			{
				mob.delInventory(this);
				if(!((Room)msg.tool()).isContent(this))
					((Room)msg.tool()).addItemRefuse(this,Item.REFUSE_PLAYER_DROP);
				if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
				{
					((Room)msg.tool()).recoverRoomStats();
					if(mob.location()!=msg.tool())
						mob.location().recoverRoomStats();
				}
			}
			unWear();
			setContainer(null);
			break;
		case CMMsg.TYP_DROP:
			if(mob.isMine(this))
			{
				mob.delInventory(this);
				if(!mob.location().isContent(this))
					mob.location().addItemRefuse(this,Item.REFUSE_PLAYER_DROP);
				if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
					mob.location().recoverRoomStats();
			}
			unWear();
			setContainer(null);
			break;
		case CMMsg.TYP_DEATH:
			destroy();
			break;
		default:
			break;
		}
	}
}

