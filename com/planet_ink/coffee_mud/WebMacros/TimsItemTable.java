package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.miniweb.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class TimsItemTable extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

	
	public String runMacro(HTTPRequest httpReq, String parm)
	{
		long endTime=System.currentTimeMillis()+(1000*60*10);
		int min=CMath.s_int((httpReq.getUrlParameter("MIN")));
		if(min>0)
			endTime=System.currentTimeMillis()+(1000*60*((long)min));
		java.util.Map<String,String> parms=parseParms(parm);
		StringBuffer str=new StringBuffer("<TABLE WIDTH=100% BORDER=1>");
		if(parms.containsKey("WORLD"))
		{
			str.append("<TR><TD>Name</TD><TD>LVL</TD><TD>TVLV</TD><TD>DIFF</TD><TD>DIFF%</TD><TD>ARM</TD><TD>ATT</TD><TD>DAM</TD><TD>ADJ</TD><TD>CAST</TD><TD>RESIST</TD></TR>");
			Vector onesDone=new Vector();
			for(Enumeration a=CMLib.map().sortedAreas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				for(Enumeration r=A.getCompleteMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if((endTime>0)&&(System.currentTimeMillis()>endTime))
						break;
					for(int i=0;i<R.numItems();i++)
					{
						Item I=R.getItem(i);
						if((endTime>0)&&(System.currentTimeMillis()>endTime))
							break;
						if(!doneBefore(onesDone,I)) str.append(addRow(I));
					}
					if((endTime>0)&&(System.currentTimeMillis()>endTime))
						break;
					for(int m=0;m<R.numInhabitants();m++)
					{
						if((endTime>0)&&(System.currentTimeMillis()>endTime))
							break;
						MOB M=R.fetchInhabitant(m);
						if(M==null) continue;
						for(int i=0;i<M.numItems();i++)
						{
							Item I=M.getItem(i);
							if((endTime>0)&&(System.currentTimeMillis()>endTime))
								break;
							if(!doneBefore(onesDone,I)) str.append(addRow(I));
						}
						if((endTime>0)&&(System.currentTimeMillis()>endTime))
							break;
						if(!(M instanceof ShopKeeper)) continue;
						ShopKeeper SK=(ShopKeeper)M;
						for(Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
						{
							Environmental E=(Environmental)i.next();
							if((endTime>0)&&(System.currentTimeMillis()>endTime))
								break;
							if((E instanceof Item)
							&&(!doneBefore(onesDone,(Item)E)))
								str.append(addRow((Item)E));
						}
					}
				}
			}
		}
		else
		if(parms.containsKey("CRAFTED"))
		{
			str.append("<TR><TD>Name</TD><TD>LVL</TD><TD>TVLV</TD><TD>DIFF</TD><TD>DIFF%</TD><TD>ARM</TD><TD>ATT</TD><TD>DAM</TD><TD>ADJ</TD><TD>CAST</TD><TD>RESIST</TD></TR>");
			List<ItemCraftor> allCraftingSkills=new SVector<ItemCraftor>();
			for(Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
			{
				Ability A=(Ability)e.nextElement();
				if(A instanceof ItemCraftor)
					allCraftingSkills.add((ItemCraftor)A.copyOf());
			}
			List<ItemCraftor> craftingSkills=new SVector<ItemCraftor>();
			for(ItemCraftor skill : allCraftingSkills)
			{
				int ii=CMLib.ableMapper().lowestQualifyingLevel(skill.ID());
				for(int i=0;i<=craftingSkills.size();i++)
					if(i>=craftingSkills.size())
					{
						craftingSkills.add(skill);
						break;
					}
					else
					if(ii<CMLib.ableMapper().lowestQualifyingLevel(craftingSkills.get(i).ID()))
					{
						craftingSkills.add(i,skill);
						break;
					}
			}
			for(ItemCraftor skill : craftingSkills)
			{
				List<ItemCraftor.ItemKeyPair> pairs=skill.craftAllItemSets();
				if(pairs!=null)
					for(ItemCraftor.ItemKeyPair IP : pairs)
						if(IP.item instanceof Weapon)
							str.append(addRow(IP.item));
						else
						if(IP.item instanceof Armor)
							str.append(addRow(IP.item));
			}
		}
		else
		if(parms.containsKey("ITEMS"))
		{
			int[] materials={RawMaterial.RESOURCE_STEEL,RawMaterial.RESOURCE_IRON,RawMaterial.RESOURCE_OAK,RawMaterial.RESOURCE_LEATHER,RawMaterial.RESOURCE_COTTON};
			str.append("<TR><TD>Type</TD><TD>Level</TD>");
			for(int m=0;m<materials.length;m++)
				str.append("<TD>"+RawMaterial.CODES.NAME(materials[m])+"</TD>");
			str.append("</TR>");
			//str.append("<TD>Attack</TD>");
			//str.append("<TD>Damage</TD>");
			Wearable.CODES codes = Wearable.CODES.instance();
			for(int level=1;level<=91;level++)
			{
				str.append("<TR><TD>Armor</TD><TD>"+level+"</TD>");
				for(int m=0;m<materials.length;m++)
				{
					int armorBonus=0;
					for(long wornCode : codes.all())
					{
						if((wornCode == Wearable.IN_INVENTORY) 
						|| (wornCode == Wearable.WORN_HELD)
						|| (wornCode == Wearable.WORN_WIELD)
						|| (wornCode == Wearable.WORN_MOUTH)) 
						 	continue;
						Armor A=CMClass.getArmor("GenArmor");
						A.setRawProperLocationBitmap(wornCode);
						A.setMaterial(RawMaterial.RESOURCE_STEEL);
						if((CharClass.ARMOR_WEARMASK & wornCode) > 0)
							A.setMaterial(materials[m]);
						A.basePhyStats().setLevel(level);
						A.basePhyStats().setWeight(5);
						A.recoverPhyStats();
						CMLib.itemBuilder().balanceItemByLevel(A);
						armorBonus+=A.basePhyStats().armor();
					}
					str.append("<TD>"+armorBonus+"</TD>");
				}
				str.append("</TR>");
			}
			for(int level=1;level<=91;level++)
			{
				str.append("<TR><TD>Weapons</TD><TD>"+level+"</TD>");
				for(int m=0;m<materials.length;m++)
				{
					Weapon W=CMClass.getWeapon("GenWeapon");
					W.setWeaponClassification(Weapon.CLASS_SWORD);
					W.setWeaponType(Weapon.TYPE_SLASHING);
					W.setMaterial(materials[m]);
					W.setRawProperLocationBitmap(Wearable.WORN_WIELD|Wearable.WORN_HELD);
					W.setRawLogicalAnd(false);
					W.basePhyStats().setLevel(level);
					W.basePhyStats().setWeight(8);
					W.recoverPhyStats();
					CMLib.itemBuilder().balanceItemByLevel(W);
					str.append("<TD>"+W.basePhyStats().attackAdjustment()+"/"+W.basePhyStats().damage()+"</TD>");
				}
				str.append("</TR>");
			}
		}
		else
			str.append("<TR><TD><FONT COLOR=RED>Unknown parms: "+CMParms.toStringList(parms)+".  Try WORLD or ITEMS.</TD></TR>");
		return clearWebMacros(str)+"</TABLE>";
	}
	
	public boolean doneBefore(Vector V, Item I)
	{
		if(I==null) return true;
		if((!(I instanceof Armor))&&(!(I instanceof Weapon)))
			return true;
		if(I.displayText().length()==0)
			return true;
		for(int i=0;i<V.size();i++)
			if(I.sameAs((Environmental)V.elementAt(i)))
				return true;
		V.addElement(I);
		return false;
	}

	public String addRow(Item I)
	{
		StringBuffer row=new StringBuffer("");
		int lvl=I.phyStats().level();
		row.append("<TR>");
		row.append("<TD>"+I.name()+"</TD>");
		row.append("<TD>"+lvl+"</TD>");
		int[] castMul=new int[1];
		Ability[] RET=CMLib.itemBuilder().getTimsAdjResCast(I,castMul);
		Ability ADJ=RET[0];
		Ability RES=RET[1];
		Ability CAST=RET[2];
		int tlvl=CMLib.itemBuilder().timsLevelCalculator(I,ADJ,RES,CAST,castMul[0]);
		row.append("<TD>"+tlvl+"</TD>");
		int diff=tlvl-lvl; if(diff<0) diff=diff*-1;
		row.append("<TD>"+diff+"</TD>");
		int pct=0;
		if((lvl<0)&&(tlvl>=0)) pct=(int)Math.round(CMath.div(tlvl+(lvl*-1),1)*100.0);
		else
		if((tlvl<=0)&&(lvl>0)) pct=(int)Math.round(CMath.div((tlvl-lvl),-1)*100.0);
		else
		if((tlvl<0)&&(lvl==0)) pct=(int)Math.round(CMath.div(tlvl,-1)*100.0);
		else
		if(lvl==0) pct=(int)Math.round(CMath.div(tlvl,1)*100.0);
		else
			pct=(int)Math.round(CMath.div(tlvl,lvl)*100.0);
		row.append("<TD>"+pct+"%</TD>");
		
		if(!(I instanceof Weapon))
			row.append("<TD>"+I.basePhyStats().armor()+"</TD><TD>&nbsp;</TD><TD>&nbsp;</TD>");
		else
		{
			row.append("<TD>&nbsp;</TD><TD>"+I.basePhyStats().attackAdjustment()+"</TD>");
			row.append("<TD>"+I.basePhyStats().damage()+"</TD>");
		}
		if(ADJ!=null) row.append("<TD>"+ADJ.text()+"</TD>");
		else row.append("<TD>&nbsp;</TD>");
		if(CAST!=null) row.append("<TD>"+CAST.text()+"</TD>");
		else row.append("<TD>&nbsp;</TD>");
		if(RES!=null) row.append("<TD>"+RES.text()+"</TD>");
		else row.append("<TD>&nbsp;</TD>");
		row.append("</TR>");
		return row.toString();
	}
	
}
