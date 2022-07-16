package com.planet_ink.coffee_mud.Libraries;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityComponents.AbleTriggerCode;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.AbilityParmEditor;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaterialLibrary.DeadResourceRecord;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.Common.CommonSkill;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.CompLocation;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.RawMaterial.Material;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.net.Socket;
import java.util.*;

/*
   Copyright 2015-2022 Bo Zimmerman

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

	protected RitualStep[] root = new RitualStep[0];

	/**
	 * Separator enum constants for ritual definitions.
	 * @author Bo Zimmerman
	 *
	 */
	private enum AbleTriggerConnector
	{
		AND,
		OR
	}

	protected static class RitualStep implements AbleTrigger
	{
		public AbleTriggerCode		triggerCode		= AbleTriggerCode.SAY;
		public String				parm1			= null;
		public String				parm2			= null;
		public int					cmmsgCode		= -1;
		public RitualStep			orConnect		= null;

		@Override
		public AbleTrigger or()
		{
			return orConnect;
		}

		@Override
		public AbleTriggerCode code()
		{
			return triggerCode;
		}

		@Override
		public int msgCode()
		{
			return cmmsgCode;
		}

		@Override
		public String[] parms()
		{
			return new String[] { parm1, parm2};
		}
	}

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
		if(comp.getType()==AbilityComponent.CompType.STRING)
			return null;
		else
		if(comp.getType()==AbilityComponent.CompType.RESOURCE)
		{
			final Item I = CMLib.materials().makeItemResource((int)comp.getLongType(), comp.getSubType());
			if(comp.getAmount()>0)
				I.basePhyStats().setWeight(comp.getAmount());
			CMLib.materials().adjustResourceName(I);
			return I;
		}
		else
		if(comp.getType()==AbilityComponent.CompType.MATERIAL)
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
		Item container=null;
		if((comp.getType()==AbilityComponent.CompType.STRING)
		&&(!CMLib.english().containsString(I.name(),comp.getStringType())))
			return false;
		else
		if((comp.getType()==AbilityComponent.CompType.RESOURCE)
		&&((!(I instanceof RawMaterial))
			||(I.material()!=comp.getLongType())
			||((comp.getSubType().length()>0)&&(!((RawMaterial)I).getSubType().equalsIgnoreCase(comp.getSubType())))))
			return false;
		else
		if((comp.getType()==AbilityComponent.CompType.MATERIAL)
		&&((!(I instanceof RawMaterial))
			||(!isRightMaterial(comp.getLongType(),I.material()&RawMaterial.MATERIAL_MASK,mithrilOK))
			||((comp.getSubType().length()>0)&&(!((RawMaterial)I).getSubType().equalsIgnoreCase(comp.getSubType())))))
			return false;
		container=I.ultimateContainer(null);
		if(container==null)
			container=I;
		switch(comp.getLocation())
		{
		case INVENTORY:
			if((container.owner() instanceof Room)||(!container.amWearingAt(Wearable.IN_INVENTORY)))
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
		if((comp.getType()!=AbilityComponent.CompType.STRING)
		&&(CMLib.flags().isOnFire(I)||CMLib.flags().isEnchanted(I)))
			return false;
		if(comp.getType()==AbilityComponent.CompType.STRING)
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
		for(int i=0;i<req.size();i++)
		{
			comp=req.get(i);
			currentAND=comp.getConnector()==AbilityComponent.CompConnector.AND;
			if(previousValue&&(!currentAND))
				return passes;
			if((!previousValue)&&currentAND)
				return null;

			// if they fail the zappermask, its like the req is NOT even there...
			if((comp.getCompiledMask()!=null)
			&&(!CMLib.masking().maskCheck(comp.getCompiledMask(),mob,true)))
				continue;
			amt[0]=comp.getAmount();
			thisSet.clear();
			found=false;
			if(comp.getLocation()!=CompLocation.ONGROUND)
			{
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
		if(passes.size()==0)
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
			currentAND=comp.getConnector()==AbilityComponent.CompConnector.AND;
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
		curr.add("ANDOR",comp.getConnector()==AbilityComponent.CompConnector.AND?"&&":"||");
		if(comp.getLocation()==AbilityComponent.CompLocation.HELD)
			curr.add("DISPOSITION","held");
		else
		if(comp.getLocation()==AbilityComponent.CompLocation.WORN)
			curr.add("DISPOSITION","worn");
		else
		if(comp.getLocation()==AbilityComponent.CompLocation.NEARBY)
			curr.add("DISPOSITION","nearby");
		else
		if(comp.getLocation()==AbilityComponent.CompLocation.ONGROUND)
			curr.add("DISPOSITION","onground");
		else
		if(comp.getLocation()==AbilityComponent.CompLocation.INVENTORY)
			curr.add("DISPOSITION","inventory");

		if(comp.isConsumed())
			curr.add("FATE","consumed");
		else
			curr.add("FATE","kept");
		curr.add("AMOUNT",""+comp.getAmount());
		if(comp.getType()==AbilityComponent.CompType.STRING)
			itemDesc=comp.getStringType();
		else
		if(comp.getType()==AbilityComponent.CompType.MATERIAL)
			itemDesc=RawMaterial.Material.findByMask((int)comp.getLongType()).desc().toUpperCase();
		else
		if(comp.getType()==AbilityComponent.CompType.RESOURCE)
			itemDesc=RawMaterial.CODES.NAME((int)comp.getLongType()).toUpperCase();
		curr.add("COMPONENTID",itemDesc);
		curr.add("SUBTYPE",comp.getSubType());
		curr.add("MASK",comp.getMaskStr());
		return curr;
	}

	@Override
	public void setAbilityComponentCodedFromCodedPairs(final PairList<String,String> decodedDV, final AbilityComponent comp)
	{
		final String[] s=new String[7];
		for(int i=0;i<7;i++)
			s[i]=decodedDV.get(i).second;
		if(s[0].equalsIgnoreCase("||"))
			comp.setConnector(AbilityComponent.CompConnector.OR);
		else
			comp.setConnector(AbilityComponent.CompConnector.AND);
		if(s[1].equalsIgnoreCase("held"))
			comp.setLocation(AbilityComponent.CompLocation.HELD);
		else
		if(s[1].equalsIgnoreCase("worn"))
			comp.setLocation(AbilityComponent.CompLocation.WORN);
		else
		if(s[1].equalsIgnoreCase("nearby"))
			comp.setLocation(AbilityComponent.CompLocation.NEARBY);
		else
		if(s[1].equalsIgnoreCase("onground"))
			comp.setLocation(AbilityComponent.CompLocation.ONGROUND);
		else
			comp.setLocation(AbilityComponent.CompLocation.INVENTORY);
		if(s[2].equalsIgnoreCase("consumed"))
			comp.setConsumed(true);
		else
			comp.setConsumed(false);
		comp.setAmount(CMath.s_int(s[3]));
		final String compType=s[4];
		final String subType=s[5];
		int depth=CMLib.materials().findResourceCode(compType,false);
		if(depth>=0)
			comp.setType(AbilityComponent.CompType.RESOURCE, Integer.valueOf(depth), subType);
		else
		{
			depth=CMLib.materials().findMaterialCode(compType,false);
			if(depth>=0)
				comp.setType(AbilityComponent.CompType.MATERIAL, Integer.valueOf(depth), subType);
			else
				comp.setType(AbilityComponent.CompType.STRING, s[4].toUpperCase().trim(), "");
		}
		comp.setMask(s[6]);
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
	public AbilityComponent createBlankAbilityComponent()
	{
		final AbilityComponent comp = (AbilityComponent)CMClass.getCommon("DefaultAbilityComponent");
		comp.setConnector(AbilityComponent.CompConnector.AND);
		comp.setLocation(AbilityComponent.CompLocation.INVENTORY);
		comp.setConsumed(false);
		comp.setAmount(1);
		comp.setType(AbilityComponent.CompType.STRING, "resource-material-item name", "");
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
				buf.append(curr.get(0).second);
			buf.append("(");
			buf.append(curr.get(1).second);
			buf.append(":");
			buf.append(curr.get(2).second);
			buf.append(":");
			buf.append(curr.get(3).second);
			buf.append(":");
			buf.append(curr.get(4).second);
			if(curr.get(5).second.toString().length()>0)
				buf.append("(").append(curr.get(5).second).append(")");
			buf.append(":");
			buf.append(curr.get(6).second);
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
		int amt=0;
		String itemDesc=null;
		final StringBuffer buf=new StringBuffer("");
		if(useConnector)
			buf.append(comp.getConnector()==AbilityComponent.CompConnector.AND?", and ":", or ");
		if((mob!=null)
		&&(comp.getCompiledMask()!=null)
		&&(!CMLib.masking().maskCheck(comp.getCompiledMask(),mob,true)))
			return "";
		if(mob==null)
		{
			if(comp.getCompiledMask()!=null)
				buf.append("MASK: "+comp.getMaskStr()+": ");
		}
		amt=comp.getAmount();
		String subType=comp.getSubType();
		if(subType.trim().length()>0)
		{
			subType=subType.trim().toLowerCase();
			if(comp.getType()==AbilityComponent.CompType.STRING)
				itemDesc=((amt>1)?(amt+" "+CMLib.english().makePlural(comp.getStringType())):CMLib.english().startWithAorAn(comp.getStringType()));
			else
			if(comp.getType()==AbilityComponent.CompType.MATERIAL)
			{
				if(subType.indexOf(' ')>0)
					itemDesc=amt+" "+subType;
				else
					itemDesc=amt+" "+RawMaterial.Material.findByMask((int)comp.getLongType()).noun().toLowerCase()+" ("+subType+") ";
			}
			else
			if(comp.getType()==AbilityComponent.CompType.RESOURCE)
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
			if(comp.getType()==AbilityComponent.CompType.STRING)
				itemDesc=((amt>1)?(amt+" "+comp.getStringType()+"s"):CMLib.english().startWithAorAn(comp.getStringType()));
			else
			if(comp.getType()==AbilityComponent.CompType.MATERIAL)
				itemDesc=amt+((amt>1)?" pounds":" pound")+" of "+RawMaterial.Material.findByMask((int)comp.getLongType()).noun().toLowerCase();
			else
			if(comp.getType()==AbilityComponent.CompType.RESOURCE)
				itemDesc=amt+((amt>1)?" pounds":" pound")+" of "+RawMaterial.CODES.NAME((int)comp.getLongType()).toLowerCase();
		}
		if(comp.getLocation()==AbilityComponent.CompLocation.INVENTORY)
			buf.append(itemDesc);
		else
		if(comp.getLocation()==AbilityComponent.CompLocation.HELD)
			buf.append(itemDesc+" held");
		else
		if(comp.getLocation()==AbilityComponent.CompLocation.WORN)
			buf.append(L("@x1 worn or wielded",itemDesc));
		else
		if(comp.getLocation()==AbilityComponent.CompLocation.NEARBY)
			buf.append(itemDesc+" nearby");
		else
		if(comp.getLocation()==AbilityComponent.CompLocation.ONGROUND)
			buf.append(L("@x1 on the ground",itemDesc));
		return buf.toString();
	}

	@Override
	public String getAbilityComponentDesc(final MOB mob, final String AID)
	{
		return getAbilityComponentDesc(mob,getAbilityComponents(AID));
	}

	@Override
	public String getAbilityComponentDesc(final MOB mob, final List<AbilityComponent> req)
	{
		if(req==null)
			return null;
		final StringBuffer buf=new StringBuffer("");
		for (int r = 0; r < req.size(); r++)
		{
			buf.append(getAbilityComponentDesc(mob, req.get(r), r>0));
		}
		return buf.toString();
	}

	@Override
	public String addAbilityComponent(final String s, final Map<String, List<AbilityComponent>> H)
	{
		int x=s.indexOf('=');
		if(x<0)
			return "Malformed component line (code 0): "+s;
		final String id=s.substring(0,x).toUpperCase().trim();
		String parms=s.substring(x+1);

		String parmS=null;
		String rsc=null;
		List<AbilityComponent> parm=null;
		AbilityComponent build=null;
		int depth=0;
		parm=new Vector<AbilityComponent>();
		String error=null;
		while(parms.length()>0)
		{
			build=(AbilityComponent)CMClass.getCommon("DefaultAbilityComponent");
			build.setConnector(AbilityComponent.CompConnector.AND);
			if(parms.startsWith("||"))
			{
				build.setConnector(AbilityComponent.CompConnector.OR);
				parms=parms.substring(2).trim();
			}
			else
			if(parms.startsWith("&&"))
			{
				parms = parms.substring(2).trim();
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

			build.setLocation(AbilityComponent.CompLocation.INVENTORY);
			x=parmS.indexOf(':');
			if(x<0)
			{
				error="Malformed component line (code 0-1): "+parmS;
				continue;
			}
			if(parmS.substring(0,x).equalsIgnoreCase("held"))
				build.setLocation(AbilityComponent.CompLocation.HELD);
			else
			if(parmS.substring(0,x).equalsIgnoreCase("worn"))
				build.setLocation(AbilityComponent.CompLocation.WORN);
			else
			if(parmS.substring(0,x).equalsIgnoreCase("nearby"))
				build.setLocation(AbilityComponent.CompLocation.NEARBY);
			else
			if(parmS.substring(0,x).equalsIgnoreCase("onground"))
				build.setLocation(AbilityComponent.CompLocation.ONGROUND);
			else
			if((x>0)&&(!parmS.substring(0,x).equalsIgnoreCase("inventory")))
			{
				error="Malformed component line (code 0-2): "+parmS;
				continue;
			}
			parmS=parmS.substring(x+1);

			build.setConsumed(true);
			x=parmS.indexOf(':');
			if (x < 0)
			{
				error = "Malformed component line (code 1-1): " + parmS;
				continue;
			}
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

			build.setType(AbilityComponent.CompType.STRING, "", "");
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
			depth=CMLib.materials().findResourceCode(compType,false);
			if(depth>=0)
				build.setType(AbilityComponent.CompType.RESOURCE, Long.valueOf(depth), subType);
			else
			{
				depth=CMLib.materials().findMaterialCode(compType,false);
				if(depth>=0)
					build.setType(AbilityComponent.CompType.MATERIAL, Long.valueOf(depth), subType);
				else
					build.setType(AbilityComponent.CompType.STRING, rsc.toUpperCase().trim(), "");
			}
			parmS=parmS.substring(x+1);

			build.setMask(parmS);

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
			final StringBuffer buf=new CMFile(Resources.makeFileResourceName("skills/components.txt"),null,CMFile.FLAG_LOGERRORS).text();
			List<String> V=new Vector<String>();
			if(buf!=null)
				V=Resources.getFileLineVector(buf);
			String s=null;
			String error=null;
			if(V!=null)
			for(int v=0;v<V.size();v++)
			{
				s=V.get(v).trim();
				if(s.startsWith("#")||(s.length()==0)||s.startsWith(";")||s.startsWith(":"))
					continue;
				error=addAbilityComponent(s,H);
				if(error!=null)
					Log.errOut("CMAble",error);
			}
			Resources.submitResource("COMPONENT_MAP",H);
		}
		return H;
	}

	@Override
	public MaterialLibrary.DeadResourceRecord destroyAbilityComponents(final List<Object> found)
	{
		int lostValue=0;
		int lostAmt=0;
		int resCode=-1;
		String subType="";
		XVector<CMObject> lostProps = null;
		if((found!=null)&&(found.size()>0))
		{
			lostProps=new XVector<CMObject>();
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
	public void alterAbilityComponentFile(final String compID, final boolean delete)
	{
		final CMFile F=new CMFile(Resources.makeFileResourceName("skills/components.txt"),null,CMFile.FLAG_LOGERRORS);
		if(delete)
		{
			Resources.findRemoveProperty(F, compID);
			return;
		}
		final String parms=getAbilityComponentCodedString(compID);
		final StringBuffer text=F.textUnformatted();
		boolean lastWasCR=true;
		boolean addIt=true;
		int delFromHere=-1;
		final String upID=compID.toUpperCase();
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
				text.insert(delFromHere,parms+'\n');
				delFromHere=-1;
				addIt=false;
				break;
			}
			else
			if((lastWasCR)&&(Character.toUpperCase(text.charAt(t))==upID.charAt(0)))
			{
				if((text.substring(t).toUpperCase().startsWith(upID))
				&&(text.substring(t+upID.length()).trim().startsWith("=")))
				{
					addIt=false;
					delFromHere=t;
				}
				lastWasCR=false;
			}
			else
				lastWasCR=false;
		}
		if(delFromHere>0)
		{
			text.delete(delFromHere,text.length());
			text.append(parms+'\n');
		}
		if(addIt)
		{
			if(!lastWasCR)
				text.append('\n');
			text.append(parms+'\n');
		}
		F.saveText(text.toString(),false);
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
		final QuadVector<String,Integer,Integer,Boolean> culturalAbilitiesDV = student.baseCharStats().getMyRace().culturalAbilities();
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

	protected int getCMMsgCode(final AbleTriggerCode trig)
	{
		switch(trig)
		{
		case SAY:
			return CMMsg.TYP_SPEAK;
		case PUTTHING:
			return CMMsg.TYP_PUT;
		case BURNMATERIAL:
			return CMMsg.TYP_FIRE;
		case BURNTHING:
			return CMMsg.TYP_FIRE;
		case EAT:
			return CMMsg.TYP_EAT;
		case DRINK:
			return CMMsg.TYP_DRINK;
		case CAST:
			return CMMsg.TYP_CAST_SPELL;
		case EMOTE:
			return CMMsg.TYP_EMOTE;
		case PUTVALUE:
			return CMMsg.TYP_PUT;
		case PUTMATERIAL:
			return CMMsg.TYP_PUT;
		case BURNVALUE:
			return CMMsg.TYP_FIRE;
		case READING:
			return CMMsg.TYP_READ;
		case SOCIAL:
			return CMMsg.MSG_OK_ACTION;
		case INROOM:
		case TIME:
		case RIDING:
		case SITTING:
		case STANDING:
		case SLEEPING:
		case RANDOM:
		case CHECK:
		case WAIT:
		case YOUSAY:
		case OTHERSAY:
		case ALLSAY:
			return -999;
		}
		return -999;
	}

	@Override
	public String getAbleTriggerDesc(final AbleTrigger[] triggers)
	{
		if((triggers==null)||(triggers.length==0))
			return L("Never");
		final StringBuffer buf=new StringBuffer("");
		for(int v=0;v<triggers.length;v++)
		{
			AbleTrigger DT=triggers[v];
			while(DT != null)
			{
				if(v>0)
					buf.append(", "+((DT==triggers[v])?L("and "):L("or ")));
				switch(DT.code())
				{
				case SAY:
					buf.append(L("the player should say '@x1'",DT.parms()[0].toLowerCase()));
					break;
				case READING:
					if(DT.parms()[0].equals("0"))
						buf.append(L("the player should read something"));
					else
						buf.append(L("the player should read '@x1'",DT.parms()[0].toLowerCase()));
					break;
				case SOCIAL:
					buf.append(L("the player should @x1",DT.parms()[0].toLowerCase()));
					break;
				case TIME:
					buf.append(L("the hour of the day is @x1",DT.parms()[0].toLowerCase()));
					break;
				case PUTTHING:
					buf.append(L("the player should put @x1 in @x2",DT.parms()[0].toLowerCase(),DT.parms()[1].toLowerCase()));
					break;
				case BURNTHING:
					buf.append(L("the player should burn @x1",DT.parms()[0].toLowerCase()));
					break;
				case DRINK:
					buf.append(L("the player should drink @x1",DT.parms()[0].toLowerCase()));
					break;
				case EAT:
					buf.append(L("the player should eat @x1",DT.parms()[0].toLowerCase()));
					break;
				case INROOM:
					{
					if(DT.parms()[0].equalsIgnoreCase("holy")
					||DT.parms()[0].equalsIgnoreCase("unholy")
					||DT.parms()[0].equalsIgnoreCase("balance"))
						buf.append(L("the player should be in the deities room of infused @x1-ness.",DT.parms()[0].toLowerCase()));
					else
					{
						final Room R=CMLib.map().getRoom(DT.parms()[0]);
						if(R==null)
							buf.append(L("the player should be in some unknown place"));
						else
							buf.append(L("the player should be in '@x1'",R.displayText(null)));
					}
					}
					break;
				case RIDING:
					buf.append(L("the player should be on @x1",DT.parms()[0].toLowerCase()));
					break;
				case CAST:
					{
					final Ability A=CMClass.findAbility(DT.parms()[0]);
					if(A==null)
						buf.append(L("the player should cast '@x1'",DT.parms()[0]));
					else
						buf.append(L("the player should cast '@x1'",A.name()));
					}
					break;
				case EMOTE:
					buf.append(L("the player should emote '@x1'",DT.parms()[0].toLowerCase()));
					break;
				case RANDOM:
					buf.append(DT.parms()[0]+"% of the time");
					break;
				case WAIT:
					buf.append(L("wait @x1 seconds",""+((CMath.s_int(DT.parms()[0])*CMProps.getTickMillis())/1000)));
					break;
				case YOUSAY:
					buf.append(L("then you will automatically say '@x1'",DT.parms()[0].toLowerCase()));
					break;
				case OTHERSAY:
					buf.append(L("then all others will say '@x1'",DT.parms()[0].toLowerCase()));
					break;
				case ALLSAY:
					buf.append(L("then all will say '@x1'",DT.parms()[0].toLowerCase()));
					break;
				case CHECK:
					buf.append(CMLib.masking().maskDesc(DT.parms()[0]));
					break;
				case PUTVALUE:
					buf.append(L("the player should put an item worth at least @x1 in @x2",DT.parms()[0].toLowerCase(),DT.parms()[1].toLowerCase()));
					break;
				case PUTMATERIAL:
					{
						String material="something";
						final int t=CMath.s_int(DT.parms()[0]);
						RawMaterial.Material m;
						if(((t&RawMaterial.RESOURCE_MASK)==0)
						&&((m=RawMaterial.Material.findByMask(t))!=null))
							material=m.desc().toLowerCase();
						else
						if(RawMaterial.CODES.IS_VALID(t))
							material=RawMaterial.CODES.NAME(t).toLowerCase();
						buf.append(L("the player puts an item made of @x1 in @x2",material,DT.parms()[1].toLowerCase()));
					}
					break;
				case BURNMATERIAL:
					{
						String material="something";
						final int t=CMath.s_int(DT.parms()[0]);
						RawMaterial.Material m;
						if(((t&RawMaterial.RESOURCE_MASK)==0)
						&&((m=RawMaterial.Material.findByMask(t))!=null))
							material=m.desc().toLowerCase();
						else
						if(RawMaterial.CODES.IS_VALID(t))
							material=RawMaterial.CODES.NAME(t).toLowerCase();
						buf.append(L("the player should burn an item made of @x1",material));
					}
					break;
				case BURNVALUE:
					buf.append(L("the player should burn an item worth at least @x1",DT.parms()[0].toLowerCase()));
					break;
				case SITTING:
					buf.append(L("the player should sit down"));
					break;
				case STANDING:
					buf.append(L("the player should stand up"));
					break;
				case SLEEPING:
					buf.append(L("the player should go to sleep"));
					break;
				}
				DT=DT.or();
			}
		}
		return buf.toString();
	}

	@Override
	public CMMsg genNextAbleTrigger(final AbleTrigger[] triggers,
									final AbleTriggState trigState)
	{
		if((triggers==null)||(trigState==null)||(triggers.length>0))
			return null;
		final MOB mob=trigState.mob();
		if(mob == null)
			return null;
		final int completed =trigState.completed();
		if(completed>=triggers.length)
			return null;
		final AbleTrigger DT=triggers[completed+1];
		// in an OR-condition, we always just do the first one....
		switch(DT.code())
		{
		case SAY:
			return CMClass.getMsg(mob, DT.msgCode(), L("^T<S-NAME> say(s) '@x1'.^N",DT.parms()[0]));
		case TIME:
			trigState.setCompleted();
			return null;
		case RANDOM:
			trigState.setCompleted();
			return null;
		case YOUSAY:
			return null;
		case ALLSAY:
			return null;
		case OTHERSAY:
			return null;
		case WAIT:
		{
			final long waitDuration=CMath.s_long(DT.parms()[0])*CMProps.getTickMillis();
			if(System.currentTimeMillis()>(trigState.time()+waitDuration))
				return CMClass.getMsg(mob, CMMsg.MSG_OK_ACTION, null); // force the wait to be evaluated
			return null;
		}
		case CHECK:
			trigState.setCompleted();
			return null;
		case PUTTHING:
		{
			final Item I=CMClass.getBasicItem("GenItem");
			final Item cI=CMClass.getBasicItem("GenContainer");
			I.setName(DT.parms()[0]);
			cI.setName(DT.parms()[1]);
			return CMClass.getMsg(mob, cI, I, DT.msgCode(), L("<S-NAME> put(s) <O-NAME> into <T-NAME>."));
		}
		case BURNTHING:
		{
			final Item I=CMClass.getBasicItem("GenItem");
			if(DT.parms()[0].equals("0"))
				I.setName(L("Something"));
			else
				I.setName(DT.parms()[0]);
			return CMClass.getMsg(mob, I, null, DT.msgCode(), L("<S-NAME> burn(s) <T-NAME>."));
		}
		case READING:
		{
			final Item I=CMClass.getBasicItem("GenItem");
			if(DT.parms()[0].equals("0"))
				I.setName(L("Something"));
			else
				I.setName(DT.parms()[0]);
			return CMClass.getMsg(mob, I, null, DT.msgCode(), L("<S-NAME> read(s) <T-NAME>."));
		}
		case SOCIAL:
		{
			final Social soc = CMLib.socials().fetchSocial((DT.parms()[0]+" "+DT.parms()[1]).toUpperCase().trim(),true);
			if(soc != null)
			{
				final MOB target=mob.getVictim();
				if((target==null)&&(soc.targetName().equals("<T-NAME>")))
					return CMClass.getMsg(mob,target,soc,CMMsg.MSG_OK_VISUAL,soc.getFailedTargetMessage(), CMMsg.NO_EFFECT, null, CMMsg.NO_EFFECT, null);
				else
					return CMClass.getMsg(mob,target,soc,CMMsg.MSG_OK_VISUAL,soc.getSourceMessage(),soc.getTargetMessage(),soc.getOthersMessage());
			}
			break;
		}
		case DRINK:
		{
			final Item I=CMClass.getBasicItem("GenItem");
			if(DT.parms()[0].equals("0"))
				I.setName(L("Something"));
			else
				I.setName(DT.parms()[0]);
			return CMClass.getMsg(mob, I, null, DT.msgCode(), L("<S-NAME> drink(s) <T-NAME>."));
		}
		case EAT:
		{
			final Item I=CMClass.getBasicItem("GenItem");
			if(DT.parms()[0].equals("0"))
				I.setName(L("Something"));
			else
				I.setName(DT.parms()[0]);
			return CMClass.getMsg(mob, I, null, DT.msgCode(), L("<S-NAME> eat(s) <T-NAME>."));
		}
		case INROOM:
			trigState.setCompleted();
			return null;
		case RIDING:
			trigState.setCompleted();
			return null;
		case CAST:
		{
			final Ability A=CMClass.getAbility(DT.parms()[0]);
			if(A!=null)
				return CMClass.getMsg(mob, null, A, DT.msgCode(), L("<S-NAME> do(es) '@x1'",A.name()));
			return null;
		}
		case EMOTE:
			return CMClass.getMsg(mob, null, null, DT.msgCode(), L("<S-NAME> do(es) '@x1'",DT.parms()[0]));
		case PUTVALUE:
		{
			final Item cI=CMClass.getBasicItem("GenContainer");
			if(DT.parms()[1].equals("0"))
				cI.setName(L("Something"));
			else
				cI.setName(DT.parms()[1]);
			final Item I=CMClass.getBasicItem("GenItem");
			I.setName(L("valuables"));
			I.setBaseValue(CMath.s_int(DT.parms()[0]));
			return CMClass.getMsg(mob, cI, I, DT.msgCode(), L("<S-NAME> put(s) <O-NAME> in <T-NAME>."));
		}
		case PUTMATERIAL:
		case BURNMATERIAL:
		{
			final Item cI=CMClass.getBasicItem("GenContainer");
			if(DT.parms()[1].equals("0"))
				cI.setName(L("Something"));
			else
				cI.setName(DT.parms()[1]);
			final Item I=CMLib.materials().makeItemResource(CMath.s_int(DT.parms()[0]));
			return CMClass.getMsg(mob, cI, I, DT.msgCode(), L("<S-NAME> put(s) <O-NAME> in <T-NAME>."));
		}
		case BURNVALUE:
		{
			final Item I=CMClass.getBasicItem("GenItem");
			I.setName(L("valuables"));
			I.setBaseValue(CMath.s_int(DT.parms()[0]));
			return CMClass.getMsg(mob, I, null, DT.msgCode(), L("<S-NAME> burn(s) <T-NAME>."));
		}
		case SITTING:
			if(!CMLib.flags().isSitting(mob))
				return CMClass.getMsg(mob, CMMsg.MSG_SIT, L("<S-NAME> sit(s)."));
			return null;
		case STANDING:
			if(!CMLib.flags().isStanding(mob))
				return CMClass.getMsg(mob, CMMsg.MSG_STAND, L("<S-NAME> stand(s)."));
			return null;
		case SLEEPING:
			if(!CMLib.flags().isSleeping(mob))
				return CMClass.getMsg(mob, CMMsg.MSG_SLEEP, L("<S-NAME> sleep(s)."));
			return null;
		}
		return null;
	}

	@Override
	public boolean ableTriggCheck(final CMMsg msg,
								  final AbleTriggState state,
								  final AbleTrigger[] triggers)
	{
		if((triggers == null)||(state==null))
			return false;
		if(state.completed()>=triggers.length-1)
			return true;
		AbleTrigger DT=triggers[state.completed()+1];
		boolean yup = false;
		while((DT != null)&&(!yup))
		{
			if((msg.sourceMinor()==DT.msgCode())
			||(DT.msgCode()==-999)
			||(DT.code()==AbleTriggerCode.SOCIAL))
			{
				switch(DT.code())
				{
				case SAY:
					if((msg.sourceMessage()!=null)&&(msg.sourceMessage().toUpperCase().indexOf(DT.parms()[0])>0))
						yup=true;
					break;
				case TIME:
					if((msg.source().location()!=null)
					&&(msg.source().location().getArea().getTimeObj().getHourOfDay()==CMath.s_int(DT.parms()[0])))
					   yup=true;
					break;
				case RANDOM:
					if(CMLib.dice().rollPercentage()<=CMath.s_int(DT.parms()[0]))
						yup=true;
					break;
				case YOUSAY:
					yup=true;
					try
					{
						state.setIgnore(true);
						CMLib.commands().postSay(msg.source(),null,CMStrings.capitalizeAndLower(DT.parms()[0]));
					}
					finally
					{
						state.setIgnore(false);
					}
					break;
				case ALLSAY:
				{
					final Room R=msg.source().location();
					if(R!=null)
					{
						yup=true;
						for(int m=0;m<R.numInhabitants();m++)
						{
							final MOB M=R.fetchInhabitant(m);
							if(M!=null)
							{
								yup=true;
								try
								{
									state.setIgnore(true);
									CMLib.commands().postSay(M,null,CMStrings.capitalizeAndLower(DT.parms()[0]));
								}
								finally
								{
									state.setIgnore(false);
								}
							}
						}
					}
					break;
				}
				case OTHERSAY:
				{
					final Room R=msg.source().location();
					if(R!=null)
					{
						yup=true;
						for(int m=0;m<R.numInhabitants();m++)
						{
							final MOB M=R.fetchInhabitant(m);
							if((M!=null)&&(M!=msg.source()))
							{
								yup=true;
								try
								{
									state.setIgnore(true);
									CMLib.commands().postSay(M,null,CMStrings.capitalizeAndLower(DT.parms()[0]));
								}
								finally
								{
									state.setIgnore(false);
								}
							}
						}
					}
					break;
				}
				case WAIT:
				{
					final long waitDuration=CMath.s_long(DT.parms()[0])*CMProps.getTickMillis();
					if(System.currentTimeMillis()>(state.time()+waitDuration))
					{
						yup=true;
						state.setWait(false);
					}
					else
					{
						if(CMSecurity.isDebugging(CMSecurity.DbgFlag.RITUALS))
							Log.debugOut(msg.source().Name()+" still waiting ("+(state.completed()+1)+"/"+triggers.length+") ");
						state.setWait(true);
						return false; // since we set the wait, there's no reason to look further
					}
					break;
				}
				case CHECK:
					if(CMLib.masking().maskCheck(DT.parms()[0],msg.source(),true))
						yup=true;
					break;
				case PUTTHING:
					if((msg.target() instanceof Container)
					&&(msg.tool() instanceof Item)
					&&(CMLib.english().containsString(msg.tool().name(),DT.parms()[0]))
					&&(CMLib.english().containsString(msg.target().name(),DT.parms()[1])))
						yup=true;
					break;
				case BURNTHING:
				case READING:
				case DRINK:
				case EAT:
					if((msg.target()!=null)
					&&(DT.parms()[0].equals("0")||CMLib.english().containsString(msg.target().name(),DT.parms()[0])))
						yup=true;
					break;
				case SOCIAL:
					if((msg.tool() instanceof Social)
					&&(msg.tool().Name().equalsIgnoreCase((DT.parms()[0]+" "+DT.parms()[1]).trim())))
						yup=true;
					break;
				case INROOM:
					if(msg.source().location()!=null)
					{
						if(DT.parms()[0].equalsIgnoreCase("holy")
						||DT.parms()[0].equalsIgnoreCase("unholy")
						||DT.parms()[0].equalsIgnoreCase("balance"))
						{
							yup=(state.getHolyName()!=null)
								&&(state.getHolyName().equalsIgnoreCase(CMLib.law().getClericInfused(msg.source().location())));
						}
						else
						if(msg.source().location().roomID().equalsIgnoreCase(DT.parms()[0]))
							yup=true;
					}
					break;
				case RIDING:
					if((msg.source().riding()!=null)
					&&(CMLib.english().containsString(msg.source().riding().name(),DT.parms()[0])))
					   yup=true;
					break;
				case CAST:
					if((msg.tool()!=null)
					&&((msg.tool().ID().equalsIgnoreCase(DT.parms()[0]))
					||(CMLib.english().containsString(msg.tool().name(),DT.parms()[0]))))
						yup=true;
					break;
				case EMOTE:
					if((msg.sourceMessage()!=null)&&(msg.sourceMessage().toUpperCase().indexOf(DT.parms()[0])>0))
						yup=true;
					break;
				case PUTVALUE:
					if((msg.tool() instanceof Item)
					&&(((Item)msg.tool()).baseGoldValue()>=CMath.s_int(DT.parms()[0]))
					&&(msg.target() instanceof Container)
					&&(CMLib.english().containsString(msg.target().name(),DT.parms()[1])))
						yup=true;
					break;
				case PUTMATERIAL:
					if((msg.tool() instanceof Item)
					&&(((((Item)msg.tool()).material()&RawMaterial.RESOURCE_MASK)==CMath.s_int(DT.parms()[0]))
						||((((Item)msg.tool()).material()&RawMaterial.MATERIAL_MASK)==CMath.s_int(DT.parms()[0])))
					&&(msg.target() instanceof Container)
					&&(CMLib.english().containsString(msg.target().name(),DT.parms()[1])))
						yup=true;
					break;
				case BURNMATERIAL:
					if((msg.target() instanceof Item)
					&&(((((Item)msg.target()).material()&RawMaterial.RESOURCE_MASK)==CMath.s_int(DT.parms()[0]))
						||((((Item)msg.target()).material()&RawMaterial.MATERIAL_MASK)==CMath.s_int(DT.parms()[0]))))
							yup=true;
					break;
				case BURNVALUE:
					if((msg.target() instanceof Item)
					&&(((Item)msg.target()).baseGoldValue()>=CMath.s_int(DT.parms()[0])))
						yup=true;
					break;
				case SITTING:
					yup=CMLib.flags().isSitting(msg.source());
					break;
				case STANDING:
					yup=(CMLib.flags().isStanding(msg.source()));
					break;
				case SLEEPING:
					yup=CMLib.flags().isSleeping(msg.source());
					break;
				}
			}
			if(yup)
			{
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.RITUALS))
					Log.debugOut(msg.source().Name()+" completed "+DT.code().name()+" ("+(state.completed()+1)+"/"+triggers.length+") ");
				state.setCompleted();
				if(state.completed()>=triggers.length-1)
					return true;
				else
				{
					DT=triggers[state.completed()+1];
					yup=false;
					// try this one now!
				}
			}
			else
				DT=DT.or();
		}
		return false;
	}

	@Override
	public AbleTrigger[] parseAbleTriggers(String trigger, final List<String> errors)
	{
		trigger=trigger.toUpperCase().trim();
		AbleTriggerConnector previousConnector=AbleTriggerConnector.AND;
		if(trigger.equals("-"))
			return new AbleTrigger[0];

		final List<RitualStep> putHere = new XVector<RitualStep>();
		RitualStep prevDT=null;
		while(trigger.length()>0)
		{
			final int div1=trigger.indexOf('&');
			final int div2=trigger.indexOf('|');
			int div=div1;

			if((div2>=0)&&((div<0)||(div2<div)))
				div=div2;
			String trig=null;
			if(div<0)
			{
				trig=trigger;
				trigger="";
			}
			else
			{
				trig=trigger.substring(0,div).trim();
				trigger=trigger.substring(div+1);
			}
			if(trig.length()>0)
			{
				final Vector<String> V=CMParms.parse(trig);
				if(V.size()>1)
				{
					final String cmd=V.firstElement();
					RitualStep DT=new RitualStep();
					AbleTriggerCode T = (AbleTriggerCode)CMath.s_valueOf(AbleTriggerCode.class, cmd);
					if(T==null)
					{
						for(final AbleTriggerCode RT : AbleTriggerCode.values())
						{
							if(RT.name().startsWith(cmd))
							{
								T=RT;
								break;
							}
						}
					}
					if((previousConnector==AbleTriggerConnector.OR)&&(prevDT!=null))
						prevDT.orConnect=DT;
					if(T==null)
					{
						if(errors!=null)
							errors.add("Illegal trigger: '"+cmd+"','"+trig+"'");
						DT=null;
						break;
					}
					else
					{
						DT.cmmsgCode=this.getCMMsgCode(T);
						switch(T)
						{
						case SAY:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case TIME:
						{
							DT.triggerCode=T;
							DT.parm1=""+CMath.s_int(CMParms.combine(V,1));
							break;
						}
						case WAIT:
						{
							DT.triggerCode=T;
							DT.parm1=""+CMath.s_int(CMParms.combine(V,1));
							break;
						}
						case YOUSAY:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case OTHERSAY:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case ALLSAY:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case PUTTHING:
						{
							DT.triggerCode=T;
							if(V.size()<3)
							{
								Log.errOut(name(),"Illegal trigger: "+trig);
								DT=null;
								break;
							}
							DT.parm1=CMParms.combine(V,1,V.size()-2);
							DT.parm2=V.lastElement();
							break;
						}
						case SOCIAL:
						{
							DT.triggerCode=T;
							if(V.size()<2)
							{
								Log.errOut(name(),"Illegal trigger: "+trig);
								DT=null;
								break;
							}
							DT.parm1=V.get(1);
							if(V.size()>2)
								DT.parm2=V.get(2);
							final Social soc = CMLib.socials().fetchSocial((DT.parm1+" "+DT.parm2).toUpperCase().trim(),true);
							if(soc == null)
							{
								Log.errOut(name(),"Illegal social in: "+trig);
								DT=null;
								break;
							}
							break;
						}
						case BURNTHING:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case PUTVALUE:
						{
							DT.triggerCode=T;
							if(V.size()<3)
							{
								if(errors!=null)
									errors.add("Illegal trigger: "+trig);
								DT=null;
								break;
							}
							DT.parm1=""+CMath.s_int(V.elementAt(1));
							DT.parm2=CMParms.combine(V,2);
							break;
						}
						case BURNVALUE:
						{
							DT.triggerCode=T;
							if(V.size()<3)
							{
								if(errors!=null)
									errors.add("Illegal trigger: "+trig);
								DT=null;
								break;
							}
							DT.parm1=""+CMath.s_int(CMParms.combine(V,1));
							break;
						}
						case BURNMATERIAL:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							final int cd = RawMaterial.CODES.FIND_StartsWith(DT.parm1);
							boolean found=cd>=0;
							if(found)
								DT.parm1=""+cd;
							else
							{
								final RawMaterial.Material m=RawMaterial.Material.startsWith(DT.parm1);
								if(m!=null)
								{
									DT.parm1=""+m.mask();
									found=true;
								}
							}
							if(!found)
							{
								if(errors!=null)
									errors.add("Unknown material: "+trig);
								DT=null;
								break;
							}
							break;
						}
						case PUTMATERIAL:
						{
							DT.triggerCode=T;
							if(V.size()<3)
							{
								if(errors!=null)
									errors.add("Illegal trigger: "+trig);
								DT=null;
								break;
							}
							DT.parm1=V.elementAt(1);
							DT.parm2=CMParms.combine(V,2);
							final int cd = RawMaterial.CODES.FIND_StartsWith(DT.parm1);
							boolean found=cd>=0;
							if(found)
								DT.parm1=""+cd;
							else
							if(!found)
							{
								final RawMaterial.Material m=RawMaterial.Material.startsWith(DT.parm1);
								if(m!=null)
								{
									DT.parm1=""+m.mask();
									found=true;
								}
							}
							if(!found)
							{
								if(errors!=null)
									errors.add("Unknown material: "+trig);
								DT=null;
								break;
							}
							break;
						}
						case EAT:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case READING:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case RANDOM:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case CHECK:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case DRINK:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case INROOM:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case RIDING:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case CAST:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							if(CMClass.findAbility(DT.parm1)==null)
							{
								if(errors!=null)
									errors.add("Illegal SPELL in: "+trig);
								DT=null;
								break;
							}
							break;
						}
						case EMOTE:
						{
							DT.triggerCode=T;
							DT.parm1=CMParms.combine(V,1);
							break;
						}
						case SITTING:
						{
							DT.triggerCode=T;
							break;
						}
						case STANDING:
						{
							DT.triggerCode=T;
							break;
						}
						case SLEEPING:
						{
							DT.triggerCode=T;
							break;
						}
						default:
						{
							if(errors!=null)
								errors.add("Illegal trigger: '"+cmd+"','"+trig+"'");
							DT=null;
							break;
						}
						}
					}
					if(DT==null)
						return null;
					if(div==div1)
					{
						previousConnector=AbleTriggerConnector.AND;
						putHere.add(DT);
					}
					else
						previousConnector=AbleTriggerConnector.OR;
					prevDT=DT;
				}
				else
				{
					if(errors!=null)
						errors.add("Illegal trigger (need more parameters): "+trig);
					return null;
				}
			}
		}
		// check for valid starter
		if(putHere.size()>0)
		{
			int firstActiveCode=-1;
			for(int i=0;i<putHere.size();i++)
			{
				RitualStep r = putHere.get(i);
				boolean active=false;
				while(r != null)
				{
					active = active || (r.cmmsgCode>0);
					r=r.orConnect;
				}
				if(active)
				{
					firstActiveCode = i;
					break;
				}
			}
			if(firstActiveCode > 0)
			{
				RitualStep gone = putHere.remove(firstActiveCode);
				putHere.add(0, gone);
			}
		}
		return putHere.toArray(new AbleTrigger[putHere.size()]);
	}
}
