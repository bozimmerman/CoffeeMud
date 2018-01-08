package com.planet_ink.coffee_mud.Items.BasicTech;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2013-2018 Bo Zimmerman

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
public class StdElecWeapon extends StdElecItem implements Weapon, Electronics
{
	@Override
	public String ID()
	{
		return "StdElecWeapon";
	}

	protected int		weaponDamageType		= Weapon.TYPE_SHOOT;
	protected int		weaponClassification	= Weapon.CLASS_RANGED;
	protected boolean	useExtendedMissString	= false;
	protected int		minRange				= 0;
	protected int		maxRange				= 10;
	protected ModeType	mode 					= ModeType.NORMAL;
	protected ModeType[]modeTypes 				= new ModeType[]{ ModeType.NORMAL };

	protected enum ModeType
	{
		STUN,
		NORMAL,
		KILL,
		DISINTEGRATE,
		MAIM,
		DISRUPT,
		LASER,
		SONIC
	}

	public StdElecWeapon()
	{
		super();

		setName("a tech gun");
		setDisplayText("a tech gun sits here.");
		setDescription("You can't tell what it is by looking at it.");

		setRawLogicalAnd(false);
		setRawProperLocationBitmap(Wearable.WORN_HELD|Wearable.WORN_WIELD);
		basePhyStats().setAttackAdjustment(0);
		basePhyStats().setDamage(20);
		basePhyStats().setLevel(1);
		setBaseValue(1500);
		setRanges(0,10);
		setMaterial(RawMaterial.RESOURCE_STEEL);
		activate(true);
		super.setPowerCapacity(1000);
		super.setPowerRemaining(1000);
		recoverPhyStats();
	}

	// this method is the reason not to make the types an editable field; the
	// murder-motel-like interaction between shields and weapons is finely
	// balanced based on what this does.
	@Override public int weaponDamageType()
	{
		switch(mode)
		{
		case LASER:
			return Weapon.TYPE_LASERING;
		case SONIC:
			return Weapon.TYPE_SONICING;
		case DISRUPT:
		case DISINTEGRATE:
			return Weapon.TYPE_MELTING;
		case STUN:
			return Weapon.TYPE_SONICING;
		case MAIM:
			return Weapon.TYPE_SLASHING;
		default:
			return weaponDamageType;
		}
	}

	protected ModeType getState(String s)
	{
		s=s.toUpperCase().trim();
		for(final ModeType type : modeTypes)
		{
			if(type.toString().startsWith(s))
				return type;
		}
		return null;
	}

	protected String getStateName()
	{
		return mode.name().toLowerCase();
	}

	@Override
	public void setMiscText(String text)
	{
		miscText=text;
		if(CMath.isInteger(text))
		{
			final int x=CMath.s_int(text);
			if((x>=0)&&(x<modeTypes.length))
				mode=modeTypes[x];
		}
		else
		{
			final ModeType t=(ModeType)CMath.s_valueOf(ModeType.class,text.toUpperCase().trim());
			if((t != null)&&(CMParms.indexOf(modeTypes, t)>=0))
				mode=t;
		}
	}

	@Override
	public String text()
	{
		return mode.toString();
	}

	@Override
	public void recoverPhyStats()
	{
		super.recoverPhyStats();
		if(mode!=null)
		switch(mode)
		{
		case STUN:
			phyStats().setDamage(1);
			break;
		case NORMAL:
		case MAIM:
		case KILL:
		case LASER:
		case SONIC:
			break;
		case DISINTEGRATE:
		case DISRUPT:
			phyStats().setDamage(1+(phyStats().damage()*2));
			break;
		}
	}

	@Override
	public int weaponClassification()
	{
		return weaponClassification;
	}

	@Override
	public void setWeaponDamageType(int newType)
	{
		weaponDamageType = newType;
	}

	@Override
	public void setWeaponClassification(int newClassification)
	{
		weaponClassification = newClassification;
	}

	@Override
	public TechType getTechType()
	{
		return TechType.PERSONAL_WEAPON;
	}

	@Override
	public String secretIdentity()
	{
		return super.secretIdentity()+"\n\rAttack: "+phyStats().attackAdjustment()+", Damage: "+phyStats().damage();
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(amWearingAt(Wearable.WORN_WIELD) && activated())
		{
			if(phyStats().attackAdjustment()!=0)
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+phyStats().attackAdjustment());
			if(phyStats().damage()!=0)
				affectableStats.setDamage(affectableStats.damage()+phyStats().damage());
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_LOOK:
			case CMMsg.TYP_EXAMINE:
				if(CMLib.flags().canBeSeenBy(this, msg.source()))
				{
					super.executeMsg(myHost,msg);
					msg.source().tell(L("@x1 is currently @x2 and is at @x3% power.",name(),(activated()?"activated":"deactivated"),""+Math.round(CMath.div(powerRemaining(),powerCapacity())*100.0)));
					if(CMLib.flags().canBeSeenBy(this, msg.source())&&(activated()))
						msg.source().tell(L("@x1 is currently set to @x2.",name(),getStateName()));
				}
				break;
			case CMMsg.TYP_ACTIVATE:
			{
				super.executeMsg(myHost,msg);
				if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
					msg.source().location().show(msg.source(), this, CMMsg.MSG_OK_VISUAL, L("<S-NAME> activate(s) <T-NAME>."));
				this.activate(true);
				if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
				{
					ModeType newState=mode;
					if((msg.targetMessage()!=null)&&(msg.targetMessage().length()>0))
					{
						final List<String> V=CMParms.parse(msg.targetMessage());
						if(V.size()>0)
						{
							final String s=V.get(0).toUpperCase().trim();
							newState=this.getState(s);
							if(newState==null)
								newState=mode;
						}
					}
					mode=newState;
					msg.source().location().show(msg.source(), this, CMMsg.MSG_OK_VISUAL, L("<S-NAME> set(s) <T-NAME> on @x1.",this.getStateName()));
					recoverPhyStats();
					msg.source().recoverPhyStats();
				}
				this.activate(true);
				break;
			}
			case CMMsg.TYP_DEACTIVATE:
			{
				super.executeMsg(myHost,msg);
				if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
					msg.source().location().show(msg.source(), this, CMMsg.MSG_OK_VISUAL, L("<S-NAME> deactivate(s) <T-NAME>."));
				this.activate(false);
				break;
			}
			default:
				super.executeMsg(myHost,msg);
				break;
			}
		}
		else
		if((owner() instanceof MOB) && msg.amISource((MOB)owner()) && (!amWearingAt(Wearable.IN_INVENTORY)))
		{
			super.executeMsg(myHost,msg);
			final MOB mob=(MOB)owner();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_WEAPONATTACK:
				if((msg.tool()==this) && (weaponClassification()==Weapon.CLASS_THROWN))
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),this,CMMsg.MSG_EXPIRE|CMMsg.MASK_ALWAYS,null));
				break;
			case CMMsg.TYP_DAMAGE:
				if(msg.tool() ==this)
				{
					switch(mode)
					{
					case STUN: {
						if(msg.value()>0)
						{
							final Ability A=CMClass.getAbility("CombatSleep");
							if((A!=null)&&(msg.target() instanceof Physical))
								A.invoke(mob, (Physical)msg.target(), true, 0);
							else
							if((A==null)&&(msg.target() instanceof MOB))
							{
								msg.source().location().show((MOB)msg.target(), null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> go(es) unconscious!"));
								((MOB)msg.target()).basePhyStats().setDisposition(((MOB)msg.target()).basePhyStats().disposition()|PhyStats.IS_SLEEPING);
								((MOB)msg.target()).phyStats().setDisposition(((MOB)msg.target()).phyStats().disposition()|PhyStats.IS_SLEEPING);
							}
							if(mob.getVictim()==msg.source())
								mob.makePeace(true);
							if(msg.source().getVictim()==mob)
								msg.source().makePeace(true);
							msg.setValue(0);
						}
						break;
					}
					case NORMAL:
					case KILL:
					case LASER:
					case SONIC:
					{
						// this is normal...
						break;
					}
					case DISINTEGRATE:
					{
						final Environmental targ=msg.target();
						if((msg.value()>(basePhyStats().damage()*1.5))
						&&((!(targ instanceof MOB))||(msg.value()>=((MOB)targ).curState().getHitPoints())))
						{
							final Room R=CMLib.map().roomLocation(targ);
							if(R!=null)
							{
								if(targ instanceof MOB)
								{
									if((!((MOB)targ).amDead())||(((MOB)targ).curState().getHitPoints()>0))
										CMLib.combat().postDeath(msg.source(), (MOB)targ, null);
									((MOB)targ).location().show(mob,targ,CMMsg.MSG_OK_ACTION,L("<T-NAME> disintegrate(s)!"));
									if(((MOB)targ).amDead())
									{
										DeadBody corpseI=null;
										for(int i=0;i<R.numItems();i++)
										{
											final Item I=R.getItem(i);
											if((I instanceof DeadBody)
											&&(I.container()==null)
											&&(((DeadBody)I).getMobName().equals(targ.Name()))
											&&(!((DeadBody)I).isPlayerCorpse()))
											{
												corpseI=(DeadBody)I;
												break;
											}
										}
										if(corpseI!=null)
										{
											corpseI.destroy();
										}
									}
								}
								else
								if((targ instanceof Item)
								&&((!(targ instanceof DeadBody)||(!((DeadBody)targ).isPlayerCorpse()))))
								{
									((MOB)targ).location().show(mob,targ,CMMsg.MSG_OK_ACTION,L("^S<S-NAME> fire(s) <O-NAME> at <T-NAME> and it disintegrates!^?"));
									((Item)targ).destroy();
								}
								R.recoverRoomStats();
							}
						}
						break;
					}
					case MAIM:
					{
						if(msg.value()>0)
						{
							final Ability A=CMClass.getAbility("Amputation");
							if((A!=null)&&(msg.target() instanceof Physical))
								A.invoke(mob, (Physical)msg.target(), true, 0);
						}
						break;
					}
					case DISRUPT:
					{
						final Environmental targ=msg.target();
						if((msg.value()>(basePhyStats().damage()*1.5))
						&&((!(targ instanceof MOB))||(msg.value()>=((MOB)targ).curState().getHitPoints())))
						{
							final Room R=CMLib.map().roomLocation(targ);
							if(R!=null)
							{
								if(targ instanceof MOB)
								{
									if((!((MOB)targ).amDead())||(((MOB)targ).curState().getHitPoints()>0))
										CMLib.combat().postDeath(msg.source(), (MOB)targ, msg);
								}
								else
								if((targ instanceof Item)
								&&((!(targ instanceof DeadBody)||(!((DeadBody)targ).isPlayerCorpse()))))
								{
									((MOB)targ).location().show(mob,targ,CMMsg.MSG_OK_ACTION,L("^S<S-NAME> fire(s) <O-NAME> at <T-NAME> and it explodes!^?"));
									((Item)targ).destroy();
								}
								R.recoverRoomStats();
							}
						}
						break;
					}
					}
				}
				break;
			default:
				break;
			}
		}
	}

	@Override
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((owner() instanceof MOB) && msg.amISource((MOB)owner()) && (!amWearingAt(Wearable.IN_INVENTORY)))
		{
			final MOB mob=(MOB)owner();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ACTIVATE:
				if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
				{
					if((msg.targetMessage()!=null)&&(msg.targetMessage().length()>0))
					{
						final List<String> V=CMParms.parse(msg.targetMessage());
						if(V.size()>0)
						{
							final String s=V.get(0).toUpperCase().trim();
							final ModeType newState=this.getState(s);
							if(newState==null)
							{
								msg.source().tell(L("'@x1' is an unknown setting on @x2",s,name(msg.source())));
								return false;
							}
						}
					}
				}
				break;
			case CMMsg.TYP_WEAPONATTACK:
				if(msg.tool() ==this)
				{
					if((powerRemaining()<=0)||(!activated))
					{
						String msgStr;
						if(weaponClassification()==Weapon.CLASS_RANGED)
							msgStr="<S-YOUPOSS> <T-NAMENOART> goes *click* and does not fire.";
						else
						if(activated())
							msgStr="<S-YOUPOSS> <T-NAMENOART> seems to be out of power.";
						else
							msgStr="<S-YOUPOSS> <T-NAMENOART> seems to be turned off.";
						final CMMsg msg2=CMClass.getMsg(mob, this, null,CMMsg.MSG_OK_VISUAL,msgStr);
						if((mob.location()!=null)&&(mob.location().okMessage(myHost, msg2)))
							mob.location().send(mob, msg2);
						return false;
					}
				}
				break;
			case CMMsg.TYP_DAMAGE: // remember 50% miss rate
				if(msg.tool() ==this)
				{
					double successFactor=0.5;
					final Manufacturer m=getFinalManufacturer();
					successFactor=m.getReliabilityPct()*successFactor;
					final long powerConsumed=Math.round(phyStats().damage()*Math.max(.33, Math.abs(2.0-m.getEfficiencyPct())));
					if(powerRemaining()>=powerConsumed)
					{
						setPowerRemaining(powerRemaining()-powerConsumed);
						if(msg.value()>0)
							msg.setValue((int)Math.round(successFactor*msg.value()));
					}
					else
					{
						setPowerRemaining(0);
					}
					if(mode == ModeType.STUN)
					{
						if(msg.value()>0)
							msg.setValue(1);
					}
				}
				break;
			}
		}
		return true;
	}

	@Override
	public void setUsesRemaining(int newUses)
	{
		if(newUses==Integer.MAX_VALUE)
			newUses=100;
		super.setUsesRemaining(newUses);
	}

	@Override
	public String missString()
	{
		return CMLib.combat().standardMissString(weaponDamageType,weaponClassification,name(),useExtendedMissString);
	}

	@Override
	public String hitString(int damageAmount)
	{
		return CMLib.combat().standardHitString(weaponDamageType, weaponClassification,damageAmount,name());
	}

	@Override
	public int minRange()
	{
		if(CMath.bset(phyStats().sensesMask(),PhyStats.SENSE_ITEMNOMINRANGE))
			return 0;
		return minRange;
	}

	@Override
	public int maxRange()
	{
		if(CMath.bset(phyStats().sensesMask(),PhyStats.SENSE_ITEMNOMAXRANGE))
			return 100;
		return maxRange;
	}

	@Override
	public void setRanges(int min, int max)
	{
		minRange = min;
		maxRange = max;
	}

	@Override
	public boolean subjectToWearAndTear()
	{
		return false;
	}
}
