package com.planet_ink.coffee_mud.Libraries;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.AbilityParmEditor;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaterialLibrary.DeadResourceRecord;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.Common.CommonSkill;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.CompConnector;
import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.CompLocation;
import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.CompType;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.RawMaterial.Material;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.Deity.RitualType;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.net.Socket;
import java.util.*;

/*
   Copyright 2015-2024 Bo Zimmerman

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
public class CMAbleComps extends StdLibrary implements AbilityComponents
{
	@Override
	public String ID()
	{
		return "CMAbleComps";
	}

	protected final Map<String, List<AbilityComponent>>	abilitiesWithCompsWithTriggers	= new Hashtable<String, List<AbilityComponent>>();
	protected Map<String,List<Social>> compSocials=new SHashtable<String,List<Social>>();

	protected final boolean isRightMaterial(final long type, final long itemMaterial, final boolean mithrilOK)
	{
		if(itemMaterial == type)
			return true;
		if((mithrilOK) && (type == RawMaterial.MATERIAL_METAL))
		{
			if(itemMaterial==RawMaterial.MATERIAL_MITHRIL)
				return true;
		}
		return false;
	}

	protected Item makeItemComponent(final AbilityComponent comp, final boolean mithrilOK)
	{
		if(comp.getType()==CompType.STRING)
			return null;
		else
		if(comp.getType()==CompType.RESOURCE)
		{
			final Item I = CMLib.materials().makeItemResource((int)comp.getLongType(), comp.getSubType());
			if(comp.getAmount()>0)
				I.basePhyStats().setWeight(comp.getAmount());
			CMLib.materials().adjustResourceName(I);
			return I;
		}
		else
		if(comp.getType()==CompType.MATERIAL)
		{
			final Item I = CMLib.materials().makeItemResource(RawMaterial.CODES.MOST_FREQUENT(((int)comp.getLongType())&RawMaterial.MATERIAL_MASK), comp.getSubType());
			if(comp.getAmount()>0)
				I.basePhyStats().setWeight(comp.getAmount());
			CMLib.materials().adjustResourceName(I);
			return I;
		}
		return null;
	}

	protected boolean IsItemComponent(final MOB mob, final AbilityComponent comp, final int[] amt, Item I, final List<Object> thisSet, final boolean mithrilOK)
	{
		if(I==null)
			return false;
		if(comp.getLocation()==CompLocation.TRIGGER)
			return false;
		Item container=null;
		switch(comp.getType())
		{
		case STRING:
			if(!CMLib.english().containsString(I.name(),comp.getStringType()))
				return false;
			break;
		case RESOURCE:
			if(I instanceof RawMaterial)
			{
				if((I.material()!=comp.getLongType())
				||((comp.getSubType().length()>0)&&(!((RawMaterial)I).getSubType().equalsIgnoreCase(comp.getSubType()))))
					return false;
			}
			else
			if((I instanceof Drink)
			&&(((Drink)I).liquidRemaining()>0))
			{
				final Drink D = (Drink)I;
				if(D.liquidType()!=comp.getLongType())
					return false;
			}
			break;
		case MATERIAL:
			if(I instanceof RawMaterial)
			{
				if((!isRightMaterial(comp.getLongType(),I.material()&RawMaterial.MATERIAL_MASK,mithrilOK))
				||((comp.getSubType().length()>0)&&(!((RawMaterial)I).getSubType().equalsIgnoreCase(comp.getSubType()))))
					return false;
			}
			else
			if((I instanceof Drink)
			&&(((Drink)I).liquidRemaining()>0))
			{
				final Drink D = (Drink)I;
				if(!isRightMaterial(comp.getLongType(),D.liquidType()&RawMaterial.MATERIAL_MASK,mithrilOK))
					return false;
			}
			break;
		}
		container=I.ultimateContainer(null);
		if(container==null)
			container=I;
		switch(comp.getLocation())
		{
		case INVENTORY:
			if((container.owner() instanceof Room)||(!container.amWearingAt(Wearable.IN_INVENTORY)))
				return false;
			break;
		case HAVE:
			if(container.owner() instanceof Room)
				return false;
			break;
		case HELD:
			if((container.owner() instanceof Room)||(!container.amWearingAt(Wearable.WORN_HELD)))
				return false;
			break;
		case WORN:
			if((container.owner() instanceof Room)||(container.amWearingAt(Wearable.IN_INVENTORY)))
				return false;
			break;
		default:
		case NEARBY:
			if(!CMLib.flags().canBeSeenBy(container, mob))
				return false;
			break;
		case ONGROUND:
			if((!(container.owner() instanceof Room))||(!CMLib.flags().canBeSeenBy(container, mob)))
				return false;
			break;
		}
		if((comp.getType()!=CompType.STRING)
		&&(CMLib.flags().isOnFire(I)||CMLib.flags().isEnchanted(I)))
			return false;
		if(comp.getType()==CompType.STRING)
		{
			if(I instanceof PackagedItems)
				I=CMLib.materials().unbundle(I,amt[0],null);
			amt[0]-=I.numberOfItems();
		}
		else
		if(I.phyStats().weight()>amt[0])
		{
			I=CMLib.materials().splitBundle(I,amt[0],null);
			if(I==null)
				return false;
			amt[0]=amt[0]-I.phyStats().weight();
		}
		else
			amt[0]=amt[0]-I.phyStats().weight();
		thisSet.add(I);

		if(amt[0]<=0)
		{
			if(thisSet.size()>0)
				thisSet.add(Boolean.valueOf(comp.isConsumed()));
			return true;
		}
		return false;
	}

	// returns list of components for the requirement list.
	@Override
	public List<Item> makeComponentsSample(final List<AbilityComponent> req, final boolean mithrilOK)
	{
		if((req==null)||(req.size()==0))
			return new Vector<Item>(0);
		final List<Item> passes=new Vector<Item>();
		AbilityComponent comp = null;
		for(int i=0;i<req.size();i++)
		{
			comp=req.get(i);
			final Item I=this.makeItemComponent(comp,mithrilOK);
			passes.add(I);
		}
		return passes;
	}

	// returns list of components found if all good, returns Integer of bad row if not.
	@Override
	public List<Object> componentCheck(final MOB mob, final List<AbilityComponent> req, final boolean mithrilOK)
	{
		if((mob==null)||(req==null)||(req.size()==0))
			return new Vector<Object>(0);
		boolean currentAND=false;
		boolean previousValue=true;
		final int[] amt={0};
		final List<Object> passes=new Vector<Object>();
		final List<Object> thisSet=new ArrayList<Object>();
		boolean found=false;
		AbilityComponent comp = null;
		final Room room = mob.location();
		int minAmt = 0;
		for(int i=0;i<req.size();i++)
		{
			comp=req.get(i);
			if(comp.getConnector() == CompConnector.MESSAGE)
				break; // we are done
			currentAND=comp.getConnector()==CompConnector.AND;
			if(previousValue&&(!currentAND))
				return passes;
			if((!previousValue)&&currentAND)
				return null;

			// if they fail the zappermask, its like the req is NOT even there...
			if((comp.getCompiledMask()!=null)
			&&(!CMLib.masking().maskCheck(comp.getCompiledMask(),mob,true)))
			{
				previousValue=false;
				continue;
			}

			amt[0]=comp.getAmount();
			thisSet.clear();
			found=false;
			if(comp.getLocation()==CompLocation.TRIGGER)
			{
				final Triggerer trig = confirmAbilityComponentTriggers(mob);
				if(trig.wasCompletedRecently(mob, comp.getAbilityID()))
				{
					amt[0]=0;
					found=true;
				}
				else
					minAmt=1;
			}
			else
			if(!mob.isPlayer())
			{
				amt[0]=0;
				found=true;
			}
			else
			if(comp.getLocation()!=CompLocation.ONGROUND)
			{
				minAmt=1;
				for(int ii=0;ii<mob.numItems();ii++)
				{
					found=IsItemComponent(mob, comp, amt, mob.getItem(ii), thisSet,mithrilOK);
					if(found)
						break;
				}
			}
			if((!found)
			&&(room!=null)
			&&((comp.getLocation()==CompLocation.ONGROUND)||(comp.getLocation()==CompLocation.NEARBY)))
			{
				minAmt=1;
				for(int ii=0;ii<room.numItems();ii++)
				{
					found=IsItemComponent(mob, comp, amt, room.getItem(ii), thisSet,mithrilOK);
					if(found)
						break;
				}
			}
			if((amt[0]>0)&&(currentAND)&&(i>0))
				return null;
			previousValue=amt[0]<=0;
			if(previousValue)
				passes.addAll(thisSet);
		}
		if(passes.size()<minAmt)
			return null;
		return passes;
	}

	// returns list of components found if all good, returns Integer of bad row if not.
	@Override
	public List<Item> makeComponents(final MOB mob, final List<AbilityComponent> req)
	{
		if((mob==null)||(req==null)||(req.size()==0))
			return new Vector<Item>(0);
		boolean currentAND=false;
		final List<Item> passes=new ArrayList<Item>();
		AbilityComponent comp = null;
		for(int i=0;i<req.size();i++)
		{
			comp=req.get(i);
			if(comp.getConnector()==CompConnector.MESSAGE)
				return passes;
			currentAND=comp.getConnector()==CompConnector.AND;
			if((!currentAND)&&(passes.size()>0))
				return passes;
			// if they fail the zappermask, its like the req is NOT even there...
			if((comp.getCompiledMask()!=null)
			&&(!CMLib.masking().maskCheck(comp.getCompiledMask(),mob,true)))
				continue;
			final Item I=makeItemComponent(comp, false);
			if(I!=null)
				passes.add(I);
		}
		if(passes.size()==0)
			return null;
		return passes;
	}

	@Override
	public List<AbilityComponent> getAbilityComponents(final String AID)
	{
		return getAbilityComponentMap().get(AID.toUpperCase().trim());
	}

	protected List<PairList<String,String>> getAbilityComponentCodedPairsList(final String AID)
	{
		return getAbilityComponentCodedListLists(getAbilityComponents(AID));
	}

	@Override
	public PairList<String,String> getAbilityComponentCoded(final AbilityComponent comp)
	{
		final PairList<String,String> curr=new PairVector<String,String>();
		String itemDesc=null;
		if(comp.getConnector()==CompConnector.MESSAGE)
		{
			curr.add("ANDOR","\"");
			curr.add("MASK",comp.getMaskStr());
			return curr;
		}
		curr.add("ANDOR",comp.getConnector()==CompConnector.AND?"&&":"||");
		curr.add("DISPOSITION",comp.getLocation().name().toLowerCase());
		if(comp.getLocation()==CompLocation.TRIGGER)
			curr.add("TRIGGER",comp.getTriggererDef());
		else
		{
			if(comp.isConsumed())
				curr.add("FATE","consumed");
			else
				curr.add("FATE","kept");
			curr.add("AMOUNT",""+comp.getAmount());
			if(comp.getType()==CompType.STRING)
				itemDesc=comp.getStringType();
			else
			if(comp.getType()==CompType.MATERIAL)
				itemDesc=RawMaterial.Material.findByMask((int)comp.getLongType()).desc().toUpperCase();
			else
			if(comp.getType()==CompType.RESOURCE)
				itemDesc=RawMaterial.CODES.NAME((int)comp.getLongType()).toUpperCase();
			curr.add("COMPONENTID",itemDesc);
			curr.add("SUBTYPE",comp.getSubType());
		}
		curr.add("MASK",comp.getMaskStr());
		return curr;
	}

	@Override
	public void setAbilityComponentCodedFromCodedPairs(final PairList<String,String> decodedDV, final AbilityComponent comp)
	{
		final String[] s=new String[7];
		for(int i=0;i<7 && i<decodedDV.size();i++)
			s[i]=decodedDV.get(i).second;
		if(s[0].equalsIgnoreCase("\""))
			comp.setConnector(CompConnector.MESSAGE);
		else
		if(s[0].equalsIgnoreCase("||"))
			comp.setConnector(CompConnector.OR);
		else
			comp.setConnector(CompConnector.AND);
		if(comp.getConnector()==CompConnector.MESSAGE)
			comp.setMask(s[1]);
		else
		{
			final CompLocation loc;
			loc = (CompLocation)CMath.s_valueOf(CompLocation.class, s[1].toUpperCase().trim());
			comp.setLocation(loc);
			if(comp.getLocation()==CompLocation.TRIGGER)
			{
				comp.setTriggererDef(s[2]);
				comp.setMask(s[3]);
			}
			else
			{
				if(s[2].equalsIgnoreCase("consumed"))
					comp.setConsumed(true);
				else
					comp.setConsumed(false);
				comp.setAmount(CMath.s_int(s[3]));
				final String compType=s[4]==null?"":s[4];
				final String subType=s[5]==null?"":s[5];
				int depth=CMLib.materials().findResourceCode(compType,false);
				if(depth>=0)
					comp.setType(CompType.RESOURCE, Integer.valueOf(depth), subType);
				else
				{
					depth=CMLib.materials().findMaterialCode(compType,false);
					if(depth>=0)
						comp.setType(CompType.MATERIAL, Integer.valueOf(depth), subType);
					else
					if(s[4]==null)
						comp.setType(CompType.STRING, "", "");
					else
						comp.setType(CompType.STRING, s[4].toUpperCase().trim(), "");
				}
				if(s[6] != null)
					comp.setMask(s[6]);
				else
					comp.setMask("");
			}
		}
	}

	protected List<PairList<String,String>> getAbilityComponentCodedListLists(final List<AbilityComponent> req)
	{
		if(req==null)
			return null;
		final List<PairList<String,String>> V=new Vector<PairList<String,String>>();
		for(final AbilityComponent comp : req)
			V.add(getAbilityComponentCoded(comp));
		return V;
	}

	@Override
	public AbilityComponent createBlankAbilityComponent(final String abilityID)
	{
		final AbilityComponent comp = (AbilityComponent)CMClass.getCommon("DefaultAbilityComponent");
		comp.setAbilityID(abilityID!=null?abilityID.toUpperCase().trim():"");
		comp.setConnector(CompConnector.AND);
		comp.setLocation(CompLocation.INVENTORY);
		comp.setConsumed(false);
		comp.setAmount(1);
		comp.setType(CompType.STRING, "resource-material-item name", "");
		comp.setMask("");
		return comp;
	}

	@Override
	public String getAbilityComponentCodedString(final List<AbilityComponent> comps)
	{
		return getAbilityComponentCodedStringFromCodedList(getAbilityComponentCodedListLists(comps));
	}

	protected String getAbilityComponentCodedStringFromCodedList(final List<PairList<String,String>> comps)
	{
		final StringBuilder buf=new StringBuilder("");
		PairList<String,String> curr=null;
		for(int c=0;c<comps.size();c++)
		{
			curr=comps.get(c);
			if(curr==null)
				continue;
			if(c>0)
			{
				buf.append(curr.get(0).second);
				if(curr.get(0).second.equalsIgnoreCase("\""))
				{
					buf.append(curr.get(1).second);
					buf.append(curr.get(0).second);
					continue;
				}
			}
			buf.append("(");
			final String type = curr.get(1).second;
			buf.append(type);
			buf.append(":");
			if(type.equalsIgnoreCase("TRIGGER"))
			{
				buf.append(curr.get(2).second);
				buf.append(":");
				buf.append(curr.get(3).second);
			}
			else
			{
				buf.append(curr.get(2).second);
				buf.append(":");
				buf.append(curr.get(3).second);
				buf.append(":");
				buf.append(curr.get(4).second);
				if(curr.get(5).second.toString().length()>0)
					buf.append("(").append(curr.get(5).second).append(")");
				buf.append(":");
				buf.append(curr.get(6).second);
			}
			buf.append(")");
		}
		return buf.toString();
	}

	@Override
	public String getAbilityComponentCodedString(final String AID)
	{
		final StringBuffer buf=new StringBuffer("");
		final List<PairList<String,String>> comps=getAbilityComponentCodedPairsList(AID);
		buf.append(getAbilityComponentCodedStringFromCodedList(comps));
		return AID+"="+buf.toString();
	}

	@Override
	public String getAbilityComponentDesc(final MOB mob, final AbilityComponent comp, final boolean useConnector)
	{
		if(comp.getConnector()==CompConnector.MESSAGE)
			return comp.getMaskStr();
		int amt=0;
		String itemDesc=null;
		final StringBuffer buf=new StringBuffer("");
		if(useConnector)
			buf.append(comp.getConnector()==CompConnector.AND?", and ":", or ");
		if((mob!=null)
		&&(comp.getCompiledMask()!=null)
		&&(!CMLib.masking().maskCheck(comp.getCompiledMask(),mob,true)))
			return "";
		if(mob==null)
		{
			if(comp.getCompiledMask()!=null)
				buf.append("MASK: "+comp.getMaskStr()+": ");
		}
		if(comp.getLocation()==CompLocation.TRIGGER)
		{
			final Triggerer t = (Triggerer)CMClass.getCommon("DefaultTriggerer");
			t.addTrigger(t, comp.getTriggererDef(), compSocials, null);
			if((comp.getCompiledMask()!=null)&&(useConnector||(buf.length()>0)))
				buf.append("then ");
			buf.append(t.getTriggerDesc(t, "the player"));
		}
		else
		{
			amt=comp.getAmount();
			String subType=comp.getSubType();
			if(subType.trim().length()>0)
			{
				subType=subType.trim().toLowerCase();
				if(comp.getType()==CompType.STRING)
					itemDesc=((amt>1)?(amt+" "+CMLib.english().makePlural(comp.getStringType())):CMLib.english().startWithAorAn(comp.getStringType()));
				else
				if(comp.getType()==CompType.MATERIAL)
				{
					if(subType.indexOf(' ')>0)
						itemDesc=amt+" "+subType;
					else
						itemDesc=amt+" "+RawMaterial.Material.findByMask((int)comp.getLongType()).noun().toLowerCase()+" ("+subType+") ";
				}
				else
				if(comp.getType()==CompType.RESOURCE)
				{
					final String matName=RawMaterial.CODES.NAME((int)comp.getLongType()).toLowerCase();
					if(subType.equals(matName)
					&& (((comp.getLongType()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_CLOTH)
						||((comp.getLongType()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_PAPER)))
						itemDesc=amt+" "+subType+" bolt";
					else
						itemDesc=amt+" "+subType;
				}
			}
			else
			{
				if(comp.getType()==CompType.STRING)
					itemDesc=((amt>1)?(amt+" "+comp.getStringType()+"s"):CMLib.english().startWithAorAn(comp.getStringType()));
				else
				if(comp.getType()==CompType.MATERIAL)
					itemDesc=amt+((amt>1)?" pounds":" pound")+" of "+RawMaterial.Material.findByMask((int)comp.getLongType()).noun().toLowerCase();
				else
				if(comp.getType()==CompType.RESOURCE)
					itemDesc=amt+((amt>1)?" pounds":" pound")+" of "+RawMaterial.CODES.NAME((int)comp.getLongType()).toLowerCase();
			}
			if((comp.getLocation()==CompLocation.INVENTORY)||(comp.getLocation()==CompLocation.HAVE))
				buf.append(itemDesc);
			else
			if(comp.getLocation()==CompLocation.HELD)
				buf.append(itemDesc+" held");
			else
			if(comp.getLocation()==CompLocation.WORN)
				buf.append(L("@x1 worn or wielded",itemDesc));
			else
			if(comp.getLocation()==CompLocation.NEARBY)
				buf.append(itemDesc+" nearby");
			else
			if(comp.getLocation()==CompLocation.ONGROUND)
				buf.append(L("@x1 on the ground",itemDesc));
		}
		return buf.toString();
	}

	@Override
	public String getAbilityComponentDesc(final MOB mob, final String AID)
	{
		final List<AbilityComponent> comp = getAbilityComponents(AID);
		if((comp != null)&&(comp.size()>0))
			return getAbilityComponentDesc(mob,comp);
		final List<Social> soc = this.compSocials.get(AID);
		if(soc != null)
		{
			final StringBuilder buf = new StringBuilder("");
			for(final Social s : soc)
			{
				if(buf.length()>0)
					buf.append("\n\r");
				buf.append(s.Name());
			}
			return buf.toString();
		}
		return "";
	}

	@Override
	public String getAbilityComponentDesc(final MOB mob, final List<AbilityComponent> req)
	{
		if(req==null)
			return null;
		final StringBuffer buf=new StringBuffer("");
		if((req.size() > 0) && (req.get(req.size()-1).getConnector()==CompConnector.MESSAGE))
			return req.get(req.size()-1).getMaskStr();
		for (int r = 0; r < req.size(); r++)
		{
			buf.append(getAbilityComponentDesc(mob, req.get(r), buf.length()>0));
		}
		return buf.toString();
	}

	@Override
	public String addAbilityComponent(final String s, final Map<String, List<AbilityComponent>> H)
	{
		int x=s.indexOf('=');
		if(x<0)
		{
			if(CMLib.socials().putSocialsInHash(compSocials, new XArrayList<String>(s)) == 0)
				return "Malformed component line (code 0): "+s;
			return null;
		}
		final String id=s.substring(0,x).toUpperCase().trim();
		String parms=s.substring(x+1);

		String parmS=null;
		String rsc=null;
		List<AbilityComponent> parm=null;
		AbilityComponent build=null;
		int depth=0;
		parm=new Vector<AbilityComponent>(); // part of final output
		String error=null;
		while(parms.length()>0)
		{
			build=(AbilityComponent)CMClass.getCommon("DefaultAbilityComponent");
			if(build == null)
				return null; // probably shutting down
			build.setAbilityID(id);
			build.setConnector(CompConnector.AND);
			if(parms.startsWith("||"))
			{
				build.setConnector(CompConnector.OR);
				parms=parms.substring(2).trim();
			}
			else
			if(parms.startsWith("&&"))
			{
				parms = parms.substring(2).trim();
			}
			else
			if(parms.startsWith("\""))
			{
				build.setConnector(CompConnector.MESSAGE);
				parms=parms.substring(1).trim();
				if(parms.endsWith("\""))
					parms = parms.substring(0,parms.length()-1);
				build.setMask(parms);
				if(depth != 0)
					error = "Malformed component line (code 1/3 - message depth): " + parms;
				else
				if(parm.size()==0)
					error = "Malformed component line (code 1/2 - premature message): " + parms;
				else
					parm.add(build);
				break;
			}

			if (!parms.startsWith("("))
			{
				error = "Malformed component line (code 1): " + parms;
				break;
			}

			depth=0;
			x=1;
			for(;x<parms.length();x++)
			{
				if((parms.charAt(x)==')')&&(depth==0))
					break;
				else
				if(parms.charAt(x)=='(')
					depth++;
				else
				if(parms.charAt(x)==')')
					depth--;
			}
			if (x == parms.length())
			{
				error = "Malformed component line (code 2): " + parms;
				break;
			}
			parmS=parms.substring(1,x).trim();
			parms=parms.substring(x+1).trim();

			build.setLocation(CompLocation.INVENTORY);
			x=parmS.indexOf(':');
			if(x<0)
			{
				error="Malformed component line (code 0-1): "+parmS;
				continue;
			}
			final String typeStr=parmS.substring(0,x).toUpperCase().trim();
			CompLocation loc;
			loc = (CompLocation)CMath.s_valueOf(CompLocation.class, typeStr);
			if(loc == null)
			{
				if(x>0)
				{
					error="Malformed component line (code 0-2): "+parmS;
					continue;
				}
			}
			else
				build.setLocation(loc);
			parmS=parmS.substring(x+1);
			build.setConsumed(true);
			x=parmS.indexOf(':');
			if (x < 0)
			{
				error = "Malformed component line (code 1-1): " + parmS;
				continue;
			}
			if(build.getLocation() != CompLocation.TRIGGER)
			{
				if(parmS.substring(0,x).equalsIgnoreCase("kept"))
					build.setConsumed(false);
				else
				if((x>0)&&(!parmS.substring(0,x).equalsIgnoreCase("consumed")))
				{
					error="Malformed component line (code 1-2): "+parmS;
					continue;
				}
				parmS=parmS.substring(x+1);

				build.setAmount(1);
				x=parmS.indexOf(':');
				if (x < 0)
				{
					error = "Malformed component line (code 2-1): " + parmS;
					continue;
				}
				if((x>0)&&(!CMath.isInteger(parmS.substring(0,x))))
				{
					error="Malformed component line (code 2-2): "+parmS;
					continue;
				}
				if(x>0)
					build.setAmount(CMath.s_int(parmS.substring(0,x)));
				parmS=parmS.substring(x+1);

				build.setType(CompType.STRING, "", "");
				x=parmS.indexOf(':');
				if (x <= 0)
				{
					error = "Malformed component line (code 3-1): " + parmS;
					continue;
				}
				rsc=parmS.substring(0,x);
				String compType=rsc;
				String subType="";
				if(rsc.endsWith(")"))
				{
					final int y=rsc.lastIndexOf('(');
					if(y>0)
					{
						compType=rsc.substring(0, y);
						subType=rsc.substring(y+1,rsc.length()-1);
					}
				}
				int rscC=CMLib.materials().findResourceCode(compType,false);
				if(rscC>=0)
					build.setType(CompType.RESOURCE, Long.valueOf(rscC), subType);
				else
				{
					rscC=CMLib.materials().findMaterialCode(compType,false);
					if(rscC>=0)
						build.setType(CompType.MATERIAL, Long.valueOf(rscC), subType);
					else
						build.setType(CompType.STRING, rsc.toUpperCase().trim(), "");
				}
			}
			else // TRIGGER
				build.setTriggererDef(parmS.substring(0,x).trim());

			parmS=parmS.substring(x+1);

			build.setMask(parmS);

			if((build.getTriggererDef().trim().length()>0)
			&&(id.trim().length()>0))
			{
				if(!abilitiesWithCompsWithTriggers.containsKey(id.toUpperCase()))
					abilitiesWithCompsWithTriggers.put(id.toUpperCase(), new SLinkedList<AbilityComponent>());
				abilitiesWithCompsWithTriggers.get(id.toUpperCase()).add(build);
			}
			parm.add(build);
		}
		if(parm instanceof Vector)
			((Vector<?>)parm).trimToSize();
		if(parm instanceof SVector)
			((SVector<?>)parm).trimToSize();
		if(error!=null)
			return error;
		if(parm.size()>0)
			H.put(id.toUpperCase(),parm);
		return null;
	}

	// format of each data entry is 1=ANDOR(B), 2=DISPO(I), 3=CONSUMED(B), 4=AMT(I), 5=MATERIAL(L)RESOURCE(I)NAME(S), 6=MASK(S)
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map<String, List<AbilityComponent>> getAbilityComponentMap()
	{
		Map<String, List<AbilityComponent>> H=(Map)Resources.getResource("COMPONENT_MAP");
		if(H==null)
		{
			H=new Hashtable<String,List<AbilityComponent>>();
			if(CMProps.getBoolVar(CMProps.Bool.MUDSHUTTINGDOWN))
				return H;
			abilitiesWithCompsWithTriggers.clear();
			compSocials.clear();
			final CMFile[] fileList = CMFile.getExistingExtendedFiles(Resources.makeFileResourceName("skills/components.txt"),null,CMFile.FLAG_LOGERRORS);
			List<String> V=null;
			for(final CMFile F : fileList)
			{
				final StringBuffer buf = F.text();
				if((buf!=null)&&(buf.length()>0))
				{
					if(V == null)
						V=new ArrayList<String>(1);
					V.addAll(Resources.getFileLineVector(buf));
				}
			}
			String s=null;
			String error=null;
			if(V!=null)
			{
				for(int v=0;v<V.size();v++)
				{
					s=CMStrings.trimCRLF(V.get(v));
					if(s.startsWith("#")||(s.length()==0)||s.startsWith(";")||s.startsWith(":"))
						continue;
					error=addAbilityComponent(s,H);
					if(error!=null)
						Log.errOut("CMAble",error + " on line " + v);
				}
			}
			Triggerer.TrigSignal.sig++;
			Resources.submitResource("COMPONENT_MAP",H);
		}
		return H;
	}

	@Override
	public Map<String,List<Social>> getComponentSocials()
	{
		getAbilityComponentMap();
		return compSocials;
	}

	@Override
	public MaterialLibrary.DeadResourceRecord destroyAbilityComponents(final List<Object> found)
	{
		int lostValue=0;
		int lostAmt=0;
		int resCode=-1;
		String subType="";
		XVector<CMObject> lostProps = null; // goes into the final output
		if((found!=null)&&(found.size()>0))
		{
			lostProps=new XVector<CMObject>(); // goes into the final output
			while(found.size()>0)
			{
				int i=0;
				boolean destroy=false;
				for(;i<found.size();i++)
				{
					if(found.get(i) instanceof Boolean)
					{
						destroy = ((Boolean) found.get(i)).booleanValue();
						break;
					}
				}
				final List<Pair<Integer,String>> compInts=new ArrayList<Pair<Integer,String>>();
				while(i>=0)
				{

					if((destroy)
					&&(found.get(0) instanceof Item))
					{
						final Item I=(Item)found.get(0);
						lostProps.addAll(I.effects());
						lostProps.addAll(I.behaviors());
						lostAmt += I.basePhyStats().weight();
						lostValue +=I.value();
						compInts.add(new Pair<Integer,String>(
								Integer.valueOf(I.material()),
								((I instanceof RawMaterial)?((RawMaterial)I).getSubType():"")));
						I.destroy();
					}
					found.remove(0);
					i--;
				}
				if(compInts.size()>0)
				{
					Collections.sort(compInts, new Comparator<Pair<Integer,String>>()
					{
						@Override
						public int compare(final Pair<Integer, String> o1, final Pair<Integer, String> o2)
						{
							return o1.first.compareTo(o2.first);
						}
					});
					final int index=(int)Math.round(Math.floor(compInts.size()/2));
					if(resCode<0)
						resCode=compInts.get(index).first.intValue();
					if((subType==null)||(subType.length()==0))
						subType=compInts.get(index).second;
				}
			}
		}
		return new DeadResourceRecord()
		{
			int lostValue=0;
			int lostAmt=0;
			int resCode=-1;
			String subType="";
			List<CMObject> lostProps = null;

			public DeadResourceRecord set(final int lostValue, final int lostAmt, final int resCode, final String subType, final List<CMObject> lostProps)
			{
				this.lostValue = lostValue;
				this.lostAmt = lostAmt;
				this.resCode = resCode;
				this.subType = subType;
				this.lostProps = lostProps;
				return this;
			}

			@Override
			public int getLostValue()
			{
				return lostValue;
			}

			@Override
			public int getLostAmt()
			{
				return lostAmt;
			}

			@Override
			public int getResCode()
			{
				return resCode;
			}

			@Override
			public String getSubType()
			{
				return subType;
			}

			@Override
			public List<CMObject> getLostProps()
			{
				return lostProps;
			}
		}.set(lostValue, lostAmt, resCode, subType, lostProps);
	}

	@Override
	public List<Social> getSocialsSet(String named)
	{
		getAbilityComponentMap();
		named=named.toUpperCase().trim();
		final int spdex=named.indexOf(' ');
		if(spdex>0)
			named=named.substring(0,spdex);
		return compSocials.get(named);
	}

	@Override
	public void alterAbilityComponentFile(final String compID, final boolean delete)
	{
		final boolean isSocial =
				((compID.length()>2)&&(compID.charAt(2)=='\t'))
				||compSocials.containsKey(compID.trim().toUpperCase());
		final CMFile[] fileList = CMFile.getExistingExtendedFiles(Resources.makeFileResourceName("skills/components.txt"),null,CMFile.FLAG_LOGERRORS);
		if(delete)
		{
			//if it's not a social, you get to use the Resource method
			if(!isSocial)
			{
				for(final CMFile F : fileList)
					Resources.findRemoveProperty(F, compID);
				return;
			}
		}
		final String parms=isSocial?compID:getAbilityComponentCodedString(compID);
		for(final CMFile F : fileList)
		{
			final StringBuffer text=F.textUnformatted();
			boolean lastWasCR=true;
			boolean addIt=true;
			boolean changed=false;
			int delFromHere=-1;
			final String upID;
			if(isSocial)
			{
				final int x=compID.indexOf('\t',3);
				if(x<0)
					upID=compID.toUpperCase()+"\t";
				else
					upID=compID.substring(3,x+1).toUpperCase();
			}
			else
				upID=compID.toUpperCase();
			for(int t=0;t<text.length();t++)
			{
				if(text.charAt(t)=='\n')
					lastWasCR=true;
				else
				if(text.charAt(t)=='\r')
					lastWasCR=true;
				else
				if(Character.isWhitespace(text.charAt(t)))
					continue;
				else
				if((lastWasCR)&&(delFromHere>=0))
				{
					text.delete(delFromHere,t);
					if(!delete)
						text.insert(delFromHere,parms+'\n');
					delFromHere=-1;
					addIt=false;
					changed=true;
					break;
				}
				else
				if((lastWasCR)
				&&((isSocial && text.substring(t+3).startsWith(upID))
				  ||((!isSocial)
					&&(text.substring(t).toUpperCase().startsWith(upID))
					&&(text.substring(t+upID.length()).trim().startsWith("=")))))
				{
					addIt=false;
					delFromHere=t;
					lastWasCR=false;
				}
				else
					lastWasCR=false;
			}
			if(delFromHere>0)
			{
				text.delete(delFromHere,text.length());
				if(!delete)
					text.append(parms+'\n');
				F.saveText(text.toString(),false);
				break;
			}
			if(changed)
			{
				F.saveText(text.toString(),false);
				break;
			}
			if(addIt && (F==fileList[fileList.length-1]))
			{
				if(!lastWasCR)
					text.append('\n');
				text.append(parms+'\n');
				F.saveText(text.toString(),false);
				break;
			}
		}
		Resources.removeResource("COMPONENT_MAP");
		abilitiesWithCompsWithTriggers.clear();
		compSocials.clear();
	}

	@Override
	public AbilityLimits getSpecialSkillLimit(final MOB studentM)
	{
		final AbilityLimits aL = new AbilityLimits()
		{
			private int	commonSkills		= 0;
			private int	maxCommonSkills		= 0;
			private int	craftingSkills		= 0;
			private int	maxCraftingSkills	= 0;
			private int	nonCraftingSkills	= 0;
			private int	maxNonCraftingSkills= 0;
			private int	specificSkillLimit	= 0;
			private int maxLanguageSkills	= 0;
			private int languageSkills		= 0;

			@Override
			public AbilityLimits commonSkills(final int newVal)
			{
				commonSkills = newVal;
				if(newVal > maxCommonSkills)
					maxCommonSkills = newVal;
				return this;
			}

			@Override
			public AbilityLimits craftingSkills(final int newVal)
			{
				craftingSkills = newVal;
				if(newVal > maxCraftingSkills)
					maxCraftingSkills = newVal;
				return this;
			}

			@Override
			public AbilityLimits nonCraftingSkills(final int newVal)
			{
				nonCraftingSkills = newVal;
				if(newVal > maxNonCraftingSkills)
					maxNonCraftingSkills = newVal;
				return this;
			}

			@Override
			public AbilityLimits languageSkills(final int newVal)
			{
				languageSkills = newVal;
				if(newVal > maxLanguageSkills)
					maxLanguageSkills = newVal;
				return this;
			}

			@Override
			public AbilityLimits specificSkillLimit(final int newVal)
			{
				specificSkillLimit = newVal;
				return this;
			}

			@Override
			public int commonSkills()
			{
				return commonSkills;
			}

			@Override
			public int craftingSkills()
			{
				return craftingSkills;
			}

			@Override
			public int languageSkills()
			{
				return languageSkills;
			}

			@Override
			public int nonCraftingSkills()
			{
				return nonCraftingSkills;
			}

			@Override
			public int maxCommonSkills()
			{
				return maxCommonSkills;
			}

			@Override
			public int maxCraftingSkills()
			{
				return maxCraftingSkills;
			}

			@Override
			public int maxNonCraftingSkills()
			{
				return maxNonCraftingSkills;
			}

			@Override
			public int specificSkillLimit()
			{
				return specificSkillLimit;
			}

			@Override
			public int maxLanguageSkills()
			{
				return maxLanguageSkills;
			}
		};
		CharClass C = null;
		if(studentM!=null)
		{
			C=studentM.charStats().getCurrentClass();
		}
		if(C!=null)
		{
			if(C.maxCommonSkills() == 0)
				aL.commonSkills(Integer.MAX_VALUE);
			else
				aL.commonSkills(C.maxCommonSkills());
			if(C.maxCraftingSkills() == 0)
				aL.craftingSkills(Integer.MAX_VALUE);
			else
				aL.craftingSkills(C.maxCraftingSkills());
			if(C.maxNonCraftingSkills() == 0)
				aL.nonCraftingSkills(Integer.MAX_VALUE);
			else
				aL.nonCraftingSkills(C.maxNonCraftingSkills());
			if(C.maxLanguages() == 0)
				aL.languageSkills(Integer.MAX_VALUE);
			else
				aL.languageSkills(C.maxLanguages());
		}
		if((studentM != null) && (studentM.playerStats() != null))
		{
			final PlayerStats pStats = studentM.playerStats();
			if (aL.commonSkills() < Integer.MAX_VALUE)
			{
				aL.commonSkills(aL.commonSkills() + pStats.getBonusCommonSkillLimits());
				if(pStats.getAccount() != null)
					aL.commonSkills(aL.commonSkills() + pStats.getAccount().getBonusCommonSkillLimits());
			}
			if (aL.craftingSkills() < Integer.MAX_VALUE)
			{
				aL.craftingSkills(aL.craftingSkills() + pStats.getBonusCraftingSkillLimits());
				if(pStats.getAccount() != null)
					aL.craftingSkills(aL.craftingSkills() + pStats.getAccount().getBonusCraftingSkillLimits());
			}
			if (aL.nonCraftingSkills() < Integer.MAX_VALUE)
			{
				aL.nonCraftingSkills(aL.nonCraftingSkills() + pStats.getBonusNonCraftingSkillLimits());
				if(pStats.getAccount() != null)
					aL.nonCraftingSkills(aL.nonCraftingSkills() + pStats.getAccount().getBonusNonCraftingSkillLimits());
			}
			if (aL.maxLanguageSkills() < Integer.MAX_VALUE)
			{
				aL.languageSkills(aL.languageSkills() + pStats.getBonusLanguageLimits());
				if(pStats.getAccount() != null)
					aL.languageSkills(aL.languageSkills() + pStats.getAccount().getBonusLanguageLimits());
			}
		}
		return aL;
	}

	@Override
	public AbilityLimits getSpecialSkillLimit(final MOB studentM, final Ability A)
	{
		final AbilityLimits aL=getSpecialSkillLimit(studentM);
		aL.specificSkillLimit(Integer.MAX_VALUE);
		if(A==null)
			return aL;
		if(A instanceof CommonSkill)
		{
			final boolean crafting = ((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL)
									||((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_BUILDINGSKILL);
			aL.specificSkillLimit(crafting ? aL.craftingSkills() : aL.nonCraftingSkills());
		}
		if((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE)
		{
			aL.specificSkillLimit(aL.languageSkills());
		}
		return aL;
	}

	@Override
	public AbilityLimits getSpecialSkillRemainder(final MOB studentM, final Ability A)
	{
		final AbilityLimits aL = getSpecialSkillRemainders(studentM);
		aL.specificSkillLimit(Integer.MAX_VALUE);
		if(A==null)
			return aL;
		if(A instanceof CommonSkill)
		{
			final boolean crafting = ((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL)
									||((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_BUILDINGSKILL);
			aL.specificSkillLimit(crafting ? aL.craftingSkills() : aL.nonCraftingSkills());
		}
		if((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE)
		{
			aL.specificSkillLimit(aL.languageSkills());
		}
		return aL;
	}

	@Override
	public AbilityLimits getSpecialSkillRemainders(final MOB student)
	{
		final AbilityLimits aL = getSpecialSkillLimit(student);
		final CharStats CS=student.charStats();
		if(CS.getCurrentClass()==null)
			return aL;
		final HashSet<String> culturalAbilities=new HashSet<String>();
		final QuintVector<String,Integer,Integer,Boolean,String> culturalAbilitiesDV =
				student.baseCharStats().getMyRace().culturalAbilities();
		for(int i=0;i<culturalAbilitiesDV.size();i++)
			culturalAbilities.add(culturalAbilitiesDV.getFirst(i).toLowerCase());
		for(int a=0;a<student.numAbilities();a++)
		{
			final Ability A2=student.fetchAbility(a);
			if(A2 instanceof CommonSkill)
			{
				if(culturalAbilities.contains(A2.ID().toLowerCase()))
					continue;
				boolean foundInAClass=false;
				for(int c=0;c<CS.numClasses();c++)
				{
					if(CMLib.ableMapper().getQualifyingLevel(CS.getMyClass(c).ID(), false, A2.ID())>=0)
					{
						foundInAClass=true;
						break;
					}
				}
				if(foundInAClass)
					continue;
				aL.commonSkills(aL.commonSkills()-1);
				final boolean crafting = ((A2.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL)
										||((A2.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_BUILDINGSKILL);
				if(crafting)
					aL.craftingSkills(aL.craftingSkills()-1);
				else
					aL.nonCraftingSkills(aL.nonCraftingSkills()-1);
			}
			else
			if(((A2.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE)
			&&(!A2.ID().equals("Common")))
			{
				if(culturalAbilities.contains(A2.ID().toLowerCase()))
					continue;
				boolean foundInAClass=false;
				for(int c=0;c<CS.numClasses();c++)
				{
					if(CMLib.ableMapper().getQualifyingLevel(CS.getMyClass(c).ID(), false, A2.ID())>=0)
					{
						foundInAClass=true;
						break;
					}
				}
				if(foundInAClass)
					continue;
				aL.languageSkills(aL.languageSkills()-1);
			}
		}
		return aL;
	}

	protected Triggerer getAbilityComponentTriggers(final MOB mob, final Ability A)
	{
		if((abilitiesWithCompsWithTriggers.size()>0)
		&&(A!=null))
		{
			final String AID=A.ID().toUpperCase().trim();
			final List<AbilityComponent> comps = abilitiesWithCompsWithTriggers.get(AID);
			if(comps != null)
			{
				Triggerer trig = confirmAbilityComponentTriggers(mob);
				if(trig.hasTrigger(AID))
					return trig;
				for(final AbilityComponent comp : comps)
				{
					if((comp.getCompiledMask()==null)
					||(CMLib.masking().maskCheck(comp.getCompiledMask(), mob, true)))
					{
						if(trig.isDisabled())
						{
							trig = (Triggerer)CMClass.getCommon("DefaultTriggerer");
							mob.setTriggerer(trig);
						}
						trig.addTrigger(AID, comp.getTriggererDef(), compSocials, null);
						return trig;
					}
				}
			}
		}
		return null;
	}

	protected Triggerer getActiveTriggerer(final MOB mob)
	{
		final Triggerer triggerer = mob.triggerer();
		if(abilitiesWithCompsWithTriggers.size()==0)
		{
			if(triggerer.isObsolete() || (!triggerer.isDisabled()))
			{
				mob.setTriggerer((Triggerer)CMClass.getCommon("NonTriggerer"));
				return mob.triggerer();
			}
			return triggerer;
		}
		if(!triggerer.isObsolete())
			return triggerer;
		return null;
	}

	@Override
	public void addAssistingTriggerer(final MOB mob, final MOB assistingM, final Object key)
	{
		Triggerer activeTriggerer = getActiveTriggerer(mob);
		if(activeTriggerer == null)
		{
			activeTriggerer = (Triggerer)CMClass.getCommon("DefaultTriggerer");
			mob.setTriggerer(activeTriggerer);
		}
		activeTriggerer.addTriggerAssist(assistingM, key);
	}

	protected Triggerer confirmAbilityComponentTriggers(final MOB mob)
	{
		final Triggerer activeTriggerer = getActiveTriggerer(mob);
		if(activeTriggerer != null)
			return activeTriggerer;
		final MaskingLibrary mlib = CMLib.masking();
		Triggerer trig = null;
		for(final Enumeration<Ability> a=mob.abilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A!=null)
			{
				final List<AbilityComponent> comps = abilitiesWithCompsWithTriggers.get(A.ID().toUpperCase());
				if(comps != null)
				{
					for(final AbilityComponent comp : comps)
					{
						if (((comp.getCompiledMask()==null)||mlib.maskCheck(comp.getCompiledMask(), mob, true))
						&& (comp.getTriggererDef().length()>0))
						{
							if(trig == null)
								trig = (Triggerer)CMClass.getCommon("DefaultTriggerer");
							trig.addTrigger(A.ID().toUpperCase().trim(), comp.getTriggererDef(), compSocials, null);
						}
					}
				}
			}
		}
		if(trig == null)
			trig = (Triggerer)CMClass.getCommon("NonTriggerer");
		mob.setTriggerer(trig);
		return trig;
	}

	protected boolean isAbilityComponentTriggerCompletedRecently(final MOB mob, final Ability A)
	{
		if(abilitiesWithCompsWithTriggers.size()==0)
			return false;
		final Triggerer trigs = getAbilityComponentTriggers(mob, A);
		return trigs.wasCompletedRecently(mob, A.ID().toUpperCase());
	}

	@Override
	public void startAbilityComponentTrigger(final MOB mob, final Ability A)
	{
		if(abilitiesWithCompsWithTriggers.size()==0)
			return;
		if(!abilitiesWithCompsWithTriggers.containsKey(A.ID().toUpperCase().trim()))
			return;
		final Room R=mob.location();
		if(R==null)
			return;
		final Triggerer trigs = getAbilityComponentTriggers(mob, A);
		if(trigs.getInProgress(mob).length>0) // one at a time, plz
			return;
		final CMMsg msg = trigs.genNextAbleTrigger(mob, mob, A.ID().toUpperCase().trim(), true);
		try
		{
			if(R.okMessage(R, msg))
			{
				R.send(mob, msg);
				return; // only one action, plz
			}
		}
		catch(final Exception e)
		{
		}
	}

	@Override
	public void tickAbilityComponentTriggers(final MOB mob)
	{
		final Triggerer triggerer = mob.triggerer();
		if(abilitiesWithCompsWithTriggers.size()==0)
		{
			if(triggerer.isObsolete() || (!triggerer.isDisabled()))
				mob.setTriggerer((Triggerer)CMClass.getCommon("NonTriggerer"));
			return;
		}
		final Triggerer trigs = confirmAbilityComponentTriggers(mob);
		final MOB[] who = trigs.whosDoneWaiting();
		if(who.length>0)
		{
			for(final MOB M : who)
			{
				final CMMsg msg = CMClass.getMsg(M,null,null,CMMsg.MSG_OK_VISUAL,null);
				try
				{
					M.executeMsg(M, msg);
				}
				catch(final Exception e)
				{
				}
			}
		}
		if(mob.isPlayer())
			return;
		final Object[] keys = trigs.getInProgress(mob);
		if(keys.length==0)
			return;
		final Room R=mob.location();
		if(R==null)
			return;
		for(final Object key : keys)
		{
			final CMMsg msg = trigs.genNextAbleTrigger(mob, mob, key, false);
			try
			{
				if(R.okMessage(R, msg))
				{
					R.send(mob, msg);
					return; // only one action, plz
				}
			}
			catch(final Exception e)
			{
			}
		}
	}

	@Override
	public void handleAbilityComponentTriggers(final CMMsg msg)
	{
		final MOB mob=msg.source();
		final Triggerer triggerer = mob.triggerer();
		getAbilityComponentMap(); // in case of pre-load
		if(abilitiesWithCompsWithTriggers.size()==0)
		{
			if(triggerer.isObsolete() || (!triggerer.isDisabled()))
				mob.setTriggerer((Triggerer)CMClass.getCommon("NonTriggerer"));
			return;
		}
		final Triggerer trigs = confirmAbilityComponentTriggers(msg.source());
		final Object[] whichTracking = trigs.whichTracking(msg);
		if(whichTracking.length>0)
		{
			final Triad<MOB,Object,List<String>> comps = trigs.getCompleted(msg.source(), whichTracking, msg);
			if((comps != null)
			&&(comps.first!=null)
			&&(comps.first.location()==msg.source().location()))
			{
				final Ability A=msg.source().fetchAbility(comps.second.toString());
				if(A!=null)
				{
					mob.setActions(mob.actions()-CMProps.getSkillCombatActionCost(A.ID()));
					msg.addTrailerRunnable(new Runnable()
					{
						final MOB mob = comps.first;
						final List<String> args = new XVector<String>(comps.third);
						final Ability ableA = A;
						@Override
						public void run()
						{
							ableA.invoke(mob, args, null, false, 0);
						}
					});
				}
			}
		}
	}
}
