package com.planet_ink.coffee_mud.Libraries;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.AbilityParmEditor;
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
   Copyright 2015-2018 Bo Zimmerman

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
	
	protected Item makeItemComponent(AbilityComponent comp, final boolean mithrilOK)
	{
		if(comp.getType()==AbilityComponent.CompType.STRING)
			return null;
		else
		if(comp.getType()==AbilityComponent.CompType.RESOURCE)
			return CMLib.materials().makeItemResource((int)comp.getLongType());
		else
		if(comp.getType()==AbilityComponent.CompType.MATERIAL)
			return CMLib.materials().makeItemResource(RawMaterial.CODES.MOST_FREQUENT(((int)comp.getLongType())&RawMaterial.MATERIAL_MASK));
		return null;
	}

	protected boolean IsItemComponent(MOB mob, AbilityComponent comp, int[] amt, Item I, List<Object> thisSet, final boolean mithrilOK)
	{
		if(I==null)
			return false;
		Item container=null;
		if((comp.getType()==AbilityComponent.CompType.STRING)
		&&(!CMLib.english().containsString(I.name(),comp.getStringType())))
			return false;
		else
		if((comp.getType()==AbilityComponent.CompType.RESOURCE)
		&&((!(I instanceof RawMaterial))||(I.material()!=comp.getLongType())))
			return false;
		else
		if((comp.getType()==AbilityComponent.CompType.MATERIAL)
		&&((!(I instanceof RawMaterial))
			||(!isRightMaterial(comp.getLongType(),I.material()&RawMaterial.MATERIAL_MASK,mithrilOK))))
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
				I=(Item)CMLib.materials().unbundle(I,amt[0],null);
			amt[0]-=I.numberOfItems();
		}
		else
		if(I.phyStats().weight()>amt[0])
		{
			I=(Item)CMLib.materials().unbundle(I,amt[0],null);
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
	public List<Item> componentsSample(List<AbilityComponent> req, boolean mithrilOK)
	{
		if((req==null)||(req.size()==0))
			return new Vector<Item>(0);
		final List<Item> passes=new Vector<Item>();
		AbilityComponent comp = null;
		for(int i=0;i<req.size();i++)
		{
			comp=req.get(i);
			Item I=this.makeItemComponent(comp,mithrilOK);
			passes.add(I);
		}
		return passes;
	}

	// returns list of components found if all good, returns Integer of bad row if not.
	@Override
	public List<Object> componentCheck(MOB mob, List<AbilityComponent> req, boolean mithrilOK)
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

	@Override
	public List<AbilityComponent> getAbilityComponents(String AID)
	{
		return getAbilityComponentMap().get(AID.toUpperCase().trim());
	}

	protected List<PairList<String,String>> getAbilityComponentCodedPairsList(String AID)
	{
		return getAbilityComponentCodedListLists(getAbilityComponents(AID));
	}

	@Override
	public PairList<String,String> getAbilityComponentCoded(AbilityComponent comp)
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
		curr.add("MASK",comp.getMaskStr());
		return curr;
	}

	@Override
	public void setAbilityComponentCodedFromCodedPairs(PairList<String,String> decodedDV, AbilityComponent comp)
	{
		final String[] s=new String[6];
		for(int i=0;i<6;i++)
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
		int depth=CMLib.materials().getResourceCode(s[4],false);
		if(depth>=0)
			comp.setType(AbilityComponent.CompType.RESOURCE, Integer.valueOf(depth));
		else
		{
			depth=CMLib.materials().getMaterialCode(s[4],false);
			if(depth>=0)
				comp.setType(AbilityComponent.CompType.MATERIAL, Integer.valueOf(depth));
			else
				comp.setType(AbilityComponent.CompType.STRING, s[4].toUpperCase().trim());
		}
		comp.setMask(s[5]);
	}

	protected List<PairList<String,String>> getAbilityComponentCodedListLists(List<AbilityComponent> req)
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
		comp.setType(AbilityComponent.CompType.STRING, "resource-material-item name");
		comp.setMask("");
		return comp;
	}

	@Override
	public String getAbilityComponentCodedString(List<AbilityComponent> comps)
	{
		return getAbilityComponentCodedStringFromCodedList(getAbilityComponentCodedListLists(comps));
	}

	protected String getAbilityComponentCodedStringFromCodedList(List<PairList<String,String>> comps)
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
			buf.append(":");
			buf.append(curr.get(5).second);
			buf.append(")");
		}
		return buf.toString();
	}

	@Override
	public String getAbilityComponentCodedString(String AID)
	{
		final StringBuffer buf=new StringBuffer("");
		final List<PairList<String,String>> comps=getAbilityComponentCodedPairsList(AID);
		buf.append(getAbilityComponentCodedStringFromCodedList(comps));
		return AID+"="+buf.toString();
	}

	@Override
	public String getAbilityComponentDesc(MOB mob, AbilityComponent comp, boolean useConnector)
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
		if(comp.getType()==AbilityComponent.CompType.STRING)
			itemDesc=((amt>1)?(amt+" "+comp.getStringType()+"s"):CMLib.english().startWithAorAn(comp.getStringType()));
		else
		if(comp.getType()==AbilityComponent.CompType.MATERIAL)
			itemDesc=amt+((amt>1)?" pounds":" pound")+" of "+RawMaterial.Material.findByMask((int)comp.getLongType()).noun().toLowerCase();
		else
		if(comp.getType()==AbilityComponent.CompType.RESOURCE)
			itemDesc=amt+((amt>1)?" pounds":" pound")+" of "+RawMaterial.CODES.NAME((int)comp.getLongType()).toLowerCase();
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
	public String getAbilityComponentDesc(MOB mob, String AID)
	{
		return getAbilityComponentDesc(mob,getAbilityComponents(AID));
	}

	@Override
	public String getAbilityComponentDesc(MOB mob, List<AbilityComponent> req)
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

	@SuppressWarnings("rawtypes")

	@Override
	public String addAbilityComponent(String s, Map<String, List<AbilityComponent>> H)
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

			build.setType(AbilityComponent.CompType.STRING, "");
			x=parmS.indexOf(':');
			if (x <= 0)
			{
				error = "Malformed component line (code 3-1): " + parmS;
				continue;
			}
			rsc=parmS.substring(0,x);
			depth=CMLib.materials().getResourceCode(rsc,false);
			if(depth>=0)
				build.setType(AbilityComponent.CompType.RESOURCE, Long.valueOf(depth));
			else
			{
				depth=CMLib.materials().getMaterialCode(rsc,false);
				if(depth>=0)
					build.setType(AbilityComponent.CompType.MATERIAL, Long.valueOf(depth));
				else
					build.setType(AbilityComponent.CompType.STRING, rsc.toUpperCase().trim());
			}
			parmS=parmS.substring(x+1);

			build.setMask(parmS);

			parm.add(build);
		}
		if(parm instanceof Vector)
			((Vector)parm).trimToSize();
		if(parm instanceof SVector)
			((SVector)parm).trimToSize();
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
	public int destroyAbilityComponents(List<Object> found)
	{
		int value=0;
		if(found==null)
		{
			return 0;
		}
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
			while(i>=0)
			{
				if((destroy)&&(found.get(0) instanceof Item))
				{
					value +=((Item)found.get(0)).value();
					((Item)found.get(0)).destroy();
				}
				found.remove(0);
				i--;
			}
		}
		return value;
	}

	@Override
	public void alterAbilityComponentFile(String compID, boolean delete)
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
	public AbilityLimits getSpecialSkillLimit(MOB studentM)
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
			public AbilityLimits commonSkills(int newVal)
			{
				commonSkills = newVal;
				if(newVal > maxCommonSkills)
					maxCommonSkills = newVal;
				return this;
			}

			@Override
			public AbilityLimits craftingSkills(int newVal)
			{
				craftingSkills = newVal;
				if(newVal > maxCraftingSkills)
					maxCraftingSkills = newVal;
				return this;
			}

			@Override
			public AbilityLimits nonCraftingSkills(int newVal)
			{
				nonCraftingSkills = newVal;
				if(newVal > maxNonCraftingSkills)
					maxNonCraftingSkills = newVal;
				return this;
			}

			@Override
			public AbilityLimits languageSkills(int newVal)
			{
				languageSkills = newVal;
				if(newVal > maxLanguageSkills)
					maxLanguageSkills = newVal;
				return this;
			}

			@Override
			public AbilityLimits specificSkillLimit(int newVal)
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
	public AbilityLimits getSpecialSkillLimit(MOB studentM, Ability A)
	{
		final AbilityLimits aL=getSpecialSkillLimit(studentM);
		aL.specificSkillLimit(Integer.MAX_VALUE);
		if(A==null)
			return aL;
		if(A instanceof CommonSkill)
		{
			final boolean crafting = ((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL);
			aL.specificSkillLimit(crafting ? aL.craftingSkills() : aL.nonCraftingSkills());
		}
		if((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE)
		{
			aL.specificSkillLimit(aL.languageSkills());
		}
		return aL;
	}

	@Override
	public AbilityLimits getSpecialSkillRemainder(MOB studentM, Ability A)
	{
		final AbilityLimits aL = getSpecialSkillRemainders(studentM);
		aL.specificSkillLimit(Integer.MAX_VALUE);
		if(A==null)
			return aL;
		if(A instanceof CommonSkill)
		{
			final boolean crafting = ((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL);
			aL.specificSkillLimit(crafting ? aL.craftingSkills() : aL.nonCraftingSkills());
		}
		if((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE)
		{
			aL.specificSkillLimit(aL.languageSkills());
		}
		return aL;
	}

	@Override
	public AbilityLimits getSpecialSkillRemainders(MOB student)
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
				if((A2.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL)
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
}
