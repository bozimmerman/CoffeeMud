package com.planet_ink.coffee_mud.Abilities.Properties;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2016 Bo Zimmerman

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
public class Prop_AbsorbDamage extends Property implements TriggeredAffect
{
	@Override
	public String ID()
	{
		return "Prop_AbsorbDamage";
	}

	@Override
	public String name()
	{
		return "Absorb Damage";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS | Ability.CAN_ITEMS;
	}

	@Override
	public String accountForYourself()
	{
		final String id="Absorbs damage of the following amount and types: "+text();
		return id;
	}

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_BEING_HIT;
	}

	protected boolean cancelFlag=false;
	protected Object allAbsorb=null;
	protected Map<Integer,Object> msgTypes=null;
	protected Map<Integer,Object> weapTypes=null;
	protected Map<Integer,Object> weapMats=null;
	protected Object			  weapMagic=null;
	protected Map<Integer,Object> weapLvls=null;
	protected Map<Integer,Object> ableDomains=null;
	protected Map<Integer,Object> ableCodes=null;
	protected Map<Long,Object>	  ableFlags=null;
	
	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		List<String> parms=CMParms.parse(newMiscText.toUpperCase());
		allAbsorb=null;
		msgTypes=null;
		weapTypes=null;
		weapMats=null;
		weapMagic=null;
		weapLvls=null;
		ableDomains=null;
		ableCodes=null;
		ableFlags=null;
		
		boolean allFound=parms.contains("+ALL");
		Object current=null;
		for(String s : parms)
		{
			if(CMath.isPct(s))
			{
				current=Double.valueOf(CMath.s_pct(s));
				break;
			}
			else
			if(CMath.isInteger(s))
			{
				current=Integer.valueOf(CMath.s_int(s));
				break;
			}
		}
		if(current==null)
			current=Double.valueOf(0.5);
		allAbsorb=(allFound?current:null);
		for(String s : parms)
		{
			if(s.equals("CANCELOK"))
			{
				cancelFlag=true;
				continue;
			}
			if(CMath.isPct(s))
				current=Double.valueOf(CMath.s_pct(s));
			else
			if(CMath.isInteger(s))
				current=Integer.valueOf(CMath.s_int(s));
			else
			if((s.startsWith("+") && (!allFound))
			||(s.startsWith("-") && allFound))
			{
				s=s.substring(1);
				boolean found=false;
				int code=CharStats.CODES.findWhole(s,true);
				if(code>=0)
				{
					code=CharStats.CODES.CMMSGMAP(code);
					if(code>0)
					{
						found=true;
						if(this.msgTypes==null)
							this.msgTypes=new HashMap<Integer,Object>();
						this.msgTypes.put(Integer.valueOf(code), current);
					}
				}
				code=CMParms.indexOf(Weapon.TYPE_DESCS, s);
				if(code>=0)
				{
					found=true;
					if(this.weapTypes==null)
						this.weapTypes=new HashMap<Integer,Object>();
					this.weapTypes.put(Integer.valueOf(code), current);
				}
				code=CMParms.indexOf(Ability.ACODE_DESCS_, s);
				if(code>=0)
				{
					found=true;
					if(this.ableCodes==null)
						this.ableCodes=new HashMap<Integer,Object>();
					this.ableCodes.put(Integer.valueOf(code), current);
				}
				code=CMParms.indexOf(Ability.DOMAIN_DESCS, s);
				if(code>=0)
				{
					found=true;
					if(this.ableDomains==null)
						this.ableDomains=new HashMap<Integer,Object>();
					this.ableDomains.put(Integer.valueOf(code<<5), current);
				}
				code=CMParms.indexOf(Ability.FLAG_DESCS, s);
				if(code>=0)
				{
					found=true;
					if(this.ableFlags==null)
						this.ableFlags=new HashMap<Long,Object>();
					this.ableFlags.put(Long.valueOf(CMath.pow(2, code)), current);
				}
				code=RawMaterial.CODES.FIND_CaseSensitive(s);
				if(code>=0)
				{
					found=true;
					if(this.weapMats==null)
						this.weapMats=new HashMap<Integer,Object>();
					this.weapMats.put(Integer.valueOf(code), current);
				}
				if(s.equals("MAGIC"))
				{
					found=true;
					this.weapMagic=current;
				}
				if(s.startsWith("LEVEL")&&(CMath.isInteger(s.substring(5))))
				{
					found=true;
					if(this.weapLvls==null)
						this.weapLvls=new HashMap<Integer,Object>();
					this.weapLvls.put(Integer.valueOf(CMath.s_int(s.substring(5))), current);
				}
				if(!found)
				{
					if(affected!=null)
						Log.errOut("Prop_AbsorbDamage","Unknown '"+s+"' on "+affected.Name()+" in "+CMLib.map().getDescriptiveExtendedRoomID(CMLib.map().roomLocation(affected)));
					else
						Log.errOut("Prop_AbsorbDamage","Unknown '"+s+"'");
				}
			}
		}
		newMiscText=newMiscText.toUpperCase();
		
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((affected!=null)
		&&((msg.targetMinor()==CMMsg.TYP_DAMAGE)
			||(msg.targetMinor()==CMMsg.TYP_HEALING)
			||(cancelFlag 
				&& (msg.tool() instanceof Ability)
				&& ((msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)||(msg.sourceMinor()==CMMsg.TYP_DELICATE_HANDS_ACT)||(msg.sourceMinor()==CMMsg.TYP_JUSTICE))))
		&&(msg.value()>0))
		{
			if(affected instanceof MOB)
			{
				if(msg.target()!=affected)
					return true;
			}
			else
			if(affected instanceof Item)
			{
				if((!(((Item)affected).owner() instanceof MOB))
				||(((Item)affected).amWearingAt(Wearable.IN_INVENTORY))
				||(msg.target()!=((Item)affected).owner()))
					return true;
			}

			Object absorb=null;
			if(this.allAbsorb!=null)
			{
				absorb=this.allAbsorb;
				if(((this.msgTypes!=null)&&(msgTypes.containsKey(Integer.valueOf(msg.sourceMinor()))))
				&&((msg.tool()==null)||(msg.sourceMinor()!=CMMsg.TYP_CAST_SPELL)))
					return true;
				if(msg.tool() instanceof Weapon)
				{
					final Weapon W=(Weapon)msg.tool();
					if((this.weapMagic!=null)&&(CMLib.flags().isABonusItems(W)))
						return true;
					if((this.weapTypes!=null)&&(this.weapTypes.containsKey(Integer.valueOf(W.weaponDamageType()))))
						return true;
					if((this.weapMats!=null)&&(this.weapMats.containsKey(Integer.valueOf(W.material()))))
						return true;
					if(this.weapLvls!=null)
					{
						for(Integer I : this.weapLvls.keySet())
						{
							if(W.phyStats().level()>I.intValue())
								return true;
						}
					}
				}
				if(msg.tool() instanceof Ability)
				{
					final Ability A=(Ability)msg.tool();
					if((this.ableCodes!=null)&&(this.ableCodes.containsKey(Integer.valueOf(A.classificationCode()&Ability.ALL_ACODES))))
						return true;
					if((this.ableDomains!=null)&&(this.ableDomains.containsKey(Integer.valueOf(A.classificationCode()&Ability.ALL_DOMAINS))))
						return true;
					if(this.ableFlags!=null)
					{
						for(Long L : this.ableFlags.keySet())
							if(CMath.bset(A.flags(),L.longValue()))
								return true;
					}
				}
				if((this.weapMagic!=null)&&(msg.tool() instanceof Ability))
				{
					final int classType=((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES;
					switch(classType)
					{
					case Ability.ACODE_SPELL:
					case Ability.ACODE_PRAYER:
					case Ability.ACODE_CHANT:
					case Ability.ACODE_SONG:
						return true;
					}
				}
			}
			else
			{
				if(((this.msgTypes!=null)&&(msgTypes.containsKey(Integer.valueOf(msg.sourceMinor()))))
				&&((msg.tool()==null)||(msg.sourceMinor()!=CMMsg.TYP_CAST_SPELL)))
					absorb=msgTypes.get(Integer.valueOf(msg.sourceMinor()));
				if(msg.tool() instanceof Weapon)
				{
					final Weapon W=(Weapon)msg.tool();
					if((this.weapMagic!=null)&&(CMLib.flags().isABonusItems(W)))
						absorb=this.weapMagic;
					if((this.weapTypes!=null)&&(this.weapTypes.containsKey(Integer.valueOf(W.weaponDamageType()))))
						absorb=this.weapTypes.get(Integer.valueOf(W.weaponDamageType()));
					if((this.weapMats!=null)&&(this.weapMats.containsKey(Integer.valueOf(W.material()))))
						absorb=this.weapMats.get(Integer.valueOf(W.material()));
					
					if(this.weapLvls!=null)
					{
						int highestLvl=-1;
						for(Integer I : this.weapLvls.keySet())
						{
							if((W.phyStats().level()>I.intValue())&&(I.intValue()>highestLvl))
							{
								highestLvl=I.intValue();
								absorb=this.weapLvls.get(I);
							}
						}
					}
				}
				if(msg.tool() instanceof Ability)
				{
					final Ability A=(Ability)msg.tool();
					if((this.ableCodes!=null)&&(this.ableCodes.containsKey(Integer.valueOf(A.classificationCode()&Ability.ALL_ACODES))))
						absorb=this.ableCodes.get(Integer.valueOf(A.classificationCode()&Ability.ALL_ACODES));
					if((this.ableDomains!=null)&&(this.ableDomains.containsKey(Integer.valueOf(A.classificationCode()&Ability.ALL_DOMAINS))))
						absorb=this.ableDomains.get(Integer.valueOf(A.classificationCode()&Ability.ALL_DOMAINS));
					if(this.ableFlags!=null)
					{
						for(Long L : this.ableFlags.keySet())
						{
							if(CMath.bset(A.flags(),L.longValue()))
								absorb=this.ableFlags.get(L);
						}
					}
				}
				if((this.weapMagic!=null)&&(msg.tool() instanceof Ability))
				{
					final int classType=((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES;
					switch(classType)
					{
					case Ability.ACODE_SPELL:
					case Ability.ACODE_PRAYER:
					case Ability.ACODE_CHANT:
					case Ability.ACODE_SONG:
						absorb=this.weapMagic;
						break;
					}
				}
			}
			if(absorb!=null)
			{
				if((msg.targetMinor()==CMMsg.TYP_DAMAGE)||(msg.targetMinor()==CMMsg.TYP_HEALING))
				{
					if(absorb instanceof Double)
						msg.setValue(msg.value()-(int)Math.round(CMath.mul(msg.value(),((Double)absorb).doubleValue())));
					else
						msg.setValue(msg.value()-((Integer)absorb).intValue());
					if(msg.value()<0)
						msg.setValue(0);
				}
				else
				{
					if((absorb instanceof Double)&&(CMLib.dice().rollPercentage()<(((Double)absorb).doubleValue()*100.0)))
					{
						msg.source().tell(L("You can't concentrate on @x1.",msg.tool().Name()));
						return false;
					}
				}
			}
		}
		return true;
	}
}
