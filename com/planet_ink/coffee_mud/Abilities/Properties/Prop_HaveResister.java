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
   Copyright 2001-2018 Bo Zimmerman

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
public class Prop_HaveResister extends Property implements TriggeredAffect
{
	@Override
	public String ID()
	{
		return "Prop_HaveResister";
	}

	@Override
	public String name()
	{
		return "Resistance due to ownership";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public boolean bubbleAffect()
	{
		return true;
	}

	protected CharStats						adjCharStats		= null;
	protected String						maskString			= "";
	protected final Map<String, Integer>	prots				= new TreeMap<String, Integer>();
	protected boolean						ignoreCharStats		= true;
	protected long							lastProtection		= 0;
	protected int							remainingProtection	= 0;
	protected boolean						alwaysWeapProt		= false;

	@Override
	public long flags()
	{
		return Ability.FLAG_RESISTER;
	}

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_GET;
	}

	@Override
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		adjCharStats=(CharStats)CMClass.getCommon("DefaultCharStats");
		ignoreCharStats=true;
		String parmString=newText;
		final int maskindex=newText.toUpperCase().indexOf("MASK=");
		if(maskindex>0)
		{
			maskString=newText.substring(maskindex+5).trim();
			parmString=newText.substring(0,maskindex).trim();
		}
		
		parmString = parmString.toUpperCase();
		final List<String> parmParts = CMParms.parseSpaces(parmString.toUpperCase(), true);
		final List<String> previousSet = new LinkedList<String>();
		this.prots.clear();
		for(String parts : parmParts)
		{
			Integer newPct = null;
			if(parts.endsWith("%"))
			{
				parts=parts.substring(0,parts.length()-1).trim();
			}
			if(CMath.isInteger(parts))
			{
				newPct = Integer.valueOf(CMath.s_int(parts));
			}
			else
			if(CMath.isNumber(parts))
			{
				double d = CMath.s_double(parts);
				if((d > 1.0) || (d < -1.0))
					newPct = Integer.valueOf((int)Math.round(d));
				else
					newPct = Integer.valueOf((int)Math.round(d * 100.0));
			}
			else
			if(parts.equals("ALWAYS"))
				this.alwaysWeapProt=true;
			else
			{
				previousSet.add(parts);
			}
			if(newPct != null)
			{
				for(String previousKey : previousSet)
					this.prots.put(previousKey, newPct);
				previousSet.clear();
			}
		}
		
		for(final int i : CharStats.CODES.SAVING_THROWS())
		{
			if(parmString.toUpperCase().indexOf(CharStats.CODES.NAME(i))>=0)
				adjCharStats.setStat(i,getProtection(CharStats.CODES.NAME(i)));
			else
				adjCharStats.setStat(i,getProtection(CMStrings.limit(CharStats.CODES.NAME(i),4)));
			if(adjCharStats.getStat(i)!=0)
				ignoreCharStats=false;
		}
	}

	protected void ensureStarted()
	{
		if(adjCharStats==null)
			setMiscText(text());
	}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		ensureStarted();
		if((!ignoreCharStats)
		&&(canResist(affectedMOB))
		&&((maskString.length()==0)||(CMLib.masking().maskCheck(maskString,affectedMOB,false))))
		{
			for(final int i : CharStats.CODES.SAVING_THROWS())
				affectedStats.setStat(i,affectedStats.getStat(i)+adjCharStats.getStat(i));
		}
		super.affectCharStats(affectedMOB,affectedStats);
	}

	public boolean checkProtection(String protType)
	{
		return this.prots.containsKey(protType.toUpperCase());
	}

	public int getProtection(String protType)
	{
		protType = protType.toUpperCase();
		if(!this.prots.containsKey(protType))
			return 0;
		return this.prots.get(protType).intValue();
	}

	protected int weaponProtection(String kind, int damage, int myLevel, int hisLevel)
	{
		if(this.alwaysWeapProt)
		{
			int protection = getProtection(kind);
			return (int)Math.round(CMath.mul(damage,1.0-CMath.div(protection,100.0)));
		}
		int protection=remainingProtection;
		if((System.currentTimeMillis()-lastProtection)>=CMProps.getTickMillis())
		{
			protection = (getProtection(kind) + (myLevel - hisLevel));
			lastProtection = System.currentTimeMillis();
		}
		if(protection<=0)
			return damage;
		remainingProtection=protection-100;
		if (protection >= 100)
		{
			return 0;
		}
		return (int)Math.round(CMath.mul(damage,1.0-CMath.div(protection,100.0)));
	}

	public void resistAffect(CMMsg msg, MOB mob, Ability me, String maskString)
	{
		if(mob.location()==null)
			return;
		if(mob.amDead())
			return;
		if(!msg.amITarget(mob))
			return;

		if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&((msg.value())>0)
		&&(msg.tool() instanceof Weapon))
		{
			if(checkProtection("NON-MAGICAL-WEAPONS") && (!CMLib.flags().isABonusItems((Weapon)msg.tool())))
			{
				if((maskString.length()==0)||(CMLib.masking().maskCheck(maskString,mob,false)))
					msg.setValue(weaponProtection("NON-MAGICAL-WEAPONS",msg.value(),mob.phyStats().level(),msg.source().phyStats().level()));
			}
			if(checkProtection("NON-SILVER-WEAPONS") && (((Weapon)msg.tool()).material() != RawMaterial.RESOURCE_SILVER))
			{
				if((maskString.length()==0)||(CMLib.masking().maskCheck(maskString,mob,false)))
					msg.setValue(weaponProtection("NON-SILVER-WEAPONS",msg.value(),mob.phyStats().level(),msg.source().phyStats().level()));
			}
			if(checkProtection("WEAPONS"))
			{
				if((maskString.length()==0)||(CMLib.masking().maskCheck(maskString,mob,false)))
					msg.setValue(weaponProtection("WEAPONS",msg.value(),mob.phyStats().level(),msg.source().phyStats().level()));
			}
			else
			{
				final Weapon W=(Weapon)msg.tool();
				if((W.weaponDamageType()==Weapon.TYPE_BASHING)
				&&(checkProtection("BLUNT"))
				&&((maskString.length()==0)||(CMLib.masking().maskCheck(maskString,mob,false))))
					msg.setValue(weaponProtection("BLUNT",msg.value(),mob.phyStats().level(),msg.source().phyStats().level()));
				if((W.weaponDamageType()==Weapon.TYPE_PIERCING)
				&&(checkProtection("PIERCE"))
				&&((maskString.length()==0)||(CMLib.masking().maskCheck(maskString,mob,false))))
					msg.setValue(weaponProtection("PIERCE",msg.value(),mob.phyStats().level(),msg.source().phyStats().level()));
				if((W.weaponDamageType()==Weapon.TYPE_SLASHING)
				&&(checkProtection("SLASH"))
				&&((maskString.length()==0)||(CMLib.masking().maskCheck(maskString,mob,false))))
					msg.setValue(weaponProtection("SLASH",msg.value(),mob.phyStats().level(),msg.source().phyStats().level()));
			}
			return;
		}
	}

	@Override
	public String accountForYourself()
	{
		return "The owner gains resistances: " + describeResistance(text());
	}

	public boolean isOk(CMMsg msg, Ability me, MOB mob, String maskString)
	{
		if(CMath.bset(msg.targetMajor(),CMMsg.MASK_MAGIC))
		{
			if(msg.tool() instanceof Ability)
			{
				final Ability A=(Ability)msg.tool();
				if(CMath.bset(A.flags(),Ability.FLAG_TRANSPORTING))
				{
					if((checkProtection("TELEPORT"))
					&&((maskString.length()==0)||(CMLib.masking().maskCheck(maskString,mob,false))))
					{
						msg.source().tell(L("You can't seem to fixate on '@x1'.",mob.name()));
						return false;
					}
				}
				else
				if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
					return true;
				else
				{
					if((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
					{
						final boolean holySet = CMath.bset(A.flags(),Ability.FLAG_HOLY);
						final boolean unholySet = CMath.bset(A.flags(),Ability.FLAG_UNHOLY);
						if(holySet && !unholySet)
						{
							if((checkProtection("HOLY"))
							&&((maskString.length()==0)||(CMLib.masking().maskCheck(maskString,mob,false))))
							{
								mob.location().show(msg.source(),mob,CMMsg.MSG_OK_VISUAL,L("Holy energies from <S-NAME> are repelled from <T-NAME>."));
								return false;
							}
						}
						else
						if(unholySet && !holySet)
						{
							if((checkProtection("UNHOLY"))
							&&((maskString.length()==0)||(CMLib.masking().maskCheck(maskString,mob,false))))
							{
								mob.location().show(msg.source(),mob,CMMsg.MSG_OK_VISUAL,L("Unholy energies from <S-NAME> are repelled from <T-NAME>."));
								return false;
							}
						}
					}
					if((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_DISEASE)
					{
						if((checkProtection("DISEASE"))
						&&((maskString.length()==0)||(CMLib.masking().maskCheck(maskString,mob,false))))
						{
							mob.location().show(msg.source(),mob,CMMsg.MSG_OK_VISUAL,L("A disease from <S-NAME> is repelled from <T-NAME>."));
							return false;
						}
					}
					else
					if((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_POISON)
					{
						if((checkProtection("POISON"))
						&&((maskString.length()==0)||(CMLib.masking().maskCheck(maskString,mob,false))))
						{
							mob.location().show(msg.source(),mob,CMMsg.MSG_OK_VISUAL,L("The poison from <S-NAME> is repelled from <T-NAME>."));
							return false;
						}
					}
					else
					{
						final String abilityType = CMLib.flags().getAbilityType_(A);
						final String abilityDomain = CMLib.flags().getAbilityDomain(A);
						if((checkProtection(abilityType))
						&&((maskString.length()==0)||(CMLib.masking().maskCheck(maskString,mob,false)))
						&&(CMLib.dice().rollPercentage() <= getProtection(abilityType)))
						{
							mob.location().show(msg.source(),mob,CMMsg.MSG_OK_VISUAL,L("<T-NAME> repell(s) the @x1 effects from <S-NAME>.",CMLib.flags().getAbilityType(A).toLowerCase()));
							return false;
						}
						else
						if((checkProtection(abilityDomain))
						&&((maskString.length()==0)||(CMLib.masking().maskCheck(maskString,mob,false)))
						&&(CMLib.dice().rollPercentage() <= getProtection(abilityDomain)))
						{
							mob.location().show(msg.source(),mob,CMMsg.MSG_OK_VISUAL,L("<T-NAME> repell(s) the @x1 effects from <S-NAME>.",abilityDomain.toLowerCase().replace('_',' ')));
							return false;
						}
						else
						if((checkProtection(A.ID()))
						&&((maskString.length()==0)||(CMLib.masking().maskCheck(maskString,mob,false)))
						&&(CMLib.dice().rollPercentage() <= getProtection(abilityDomain)))
						{
							mob.location().show(msg.source(),mob,CMMsg.MSG_OK_VISUAL,L("<T-NAME> repell(s) @x1 from <S-NAME>.",abilityDomain.toLowerCase().replace('_',' ')));
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	public String describeResistance(String text)
	{
		final StringBuilder parmString=new StringBuilder("");
		for(String parmKey : this.prots.keySet())
			parmString.append(parmKey.toLowerCase()).append(" ").append(this.prots.get(parmKey)).append("% ");
		String id=parmString.toString().trim()+".";
		if(maskString.length()>0)
			id+="  Restrictions: "+CMLib.masking().maskDesc(maskString)+".";
		return id;
	}

	public boolean canResist(Environmental E)
	{
		if((affected instanceof Item)
		&&(E instanceof MOB)
		&&(!((Item)affected).amDestroyed())
		&&(E==((Item)affected).owner()))
			return true;
		return false;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((canResist(msg.target()))
		&&(msg.target() instanceof MOB)
		&&(((MOB)msg.target()).location()!=null))
		{
			if((msg.value()<=0)&&(!isOk(msg,this,(MOB)msg.target(),maskString)))
				return false;
			resistAffect(msg,(MOB)msg.target(),this,maskString);
		}
		return true;
	}
	
	@Override
	public String getStat(String statVar)
	{
		if(statVar != null)
		{
			statVar=statVar.toUpperCase();
			if(statVar.startsWith("TIDBITS"))
			{
				String parmText = text().toUpperCase();
				if(statVar.startsWith("TIDBITS="))
					parmText = statVar.substring(8).toUpperCase().trim();
				StringBuilder str=new StringBuilder("");
				final List<String> parmParts = CMParms.parseSpaces(parmText, true);
				final List<String> previousSet = new LinkedList<String>();
				for(String parts : parmParts)
				{
					Integer newPct = null;
					if(parts.endsWith("%"))
					{
						parts=parts.substring(0,parts.length()-1).trim();
					}
					if(CMath.isInteger(parts))
					{
						newPct = Integer.valueOf(CMath.s_int(parts));
					}
					else
					if(CMath.isNumber(parts))
					{
						double d = CMath.s_double(parts);
						if((d > 1.0) || (d < -1.0))
						{
							newPct = Integer.valueOf((int)Math.round(d));
						}
						else
						{
							newPct = Integer.valueOf((int)Math.round(d * 100.0));
						}
					}
					else
					if(parts.equals("ALWAYS"))
					{
						//this.alwaysWeapProt=true;
					}
					else
					{
						previousSet.add(parts);
					}
					if(newPct != null)
					{
						for(String previousKey : previousSet)
						{
							if(newPct.intValue() < 0)
							{
								str.append(L("@x1% vulnerable to @x2\n\r",newPct.toString().substring(1),previousKey.toLowerCase()));
							}
							else
								str.append(L("@x1% resistant to @x2\n\r",newPct.toString(),previousKey.toLowerCase()));
						}
						previousSet.clear();
					}
				}
				return str.toString();
			}
		}
		return "";
	}
}
