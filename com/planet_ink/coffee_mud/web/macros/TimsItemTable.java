package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class TimsItemTable extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		long endTime=System.currentTimeMillis()+(1000*60*10);
		int min=Util.s_int((httpReq.getRequestParameter("MIN")));
		if(min>0)
			endTime=System.currentTimeMillis()+(1000*60*((long)min));
		
		StringBuffer str=new StringBuffer("<TABLE WIDTH=100% BORDER=1>");
		str.append("<TR><TD>Name</TD><TD>LVL</TD><TD>TVLV</TD><TD>DIFF</TD><TD>DIFF%</TD><TD>ARM</TD><TD>ATT</TD><TD>DAM</TD><TD>ADJ</TD><TD>CAST</TD><TD>RESIST</TD></TR>");
		Vector onesDone=new Vector();
		for(Enumeration e=CMMap.areas();e.hasMoreElements();)
		{
			Area A=(Area)e.nextElement();
			for(Enumeration r=A.getProperMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if((endTime>0)&&(System.currentTimeMillis()>endTime))
					break;
				for(int i=0;i<R.numItems();i++)
				{
					Item I=R.fetchItem(i);
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
					for(int i=0;i<M.inventorySize();i++)
					{
						Item I=M.fetchInventory(i);
						if((endTime>0)&&(System.currentTimeMillis()>endTime))
							break;
						if(!doneBefore(onesDone,I)) str.append(addRow(I));
					}
					if((endTime>0)&&(System.currentTimeMillis()>endTime))
						break;
					if(!(M instanceof ShopKeeper)) continue;
					ShopKeeper S=(ShopKeeper)M;
					Vector V2=S.getUniqueStoreInventory();
					for(int v=0;v<V2.size();v++)
					{
						if((endTime>0)&&(System.currentTimeMillis()>endTime))
							break;
						if((V2.elementAt(v) instanceof Item)
						&&(!doneBefore(onesDone,(Item)V2.elementAt(v))))
							str.append(addRow((Item)V2.elementAt(v)));
					}
				}
			}
		}
		return str.toString()+"</TABLE>";
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
		int lvl=I.envStats().level();
		Ability ADJ=I.fetchEffect("Prop_WearAdjuster");
		if(ADJ==null) ADJ=I.fetchEffect("Prop_HaveAdjuster");
		if(ADJ==null) ADJ=I.fetchEffect("Prop_RideAdjuster");
		Ability RES=I.fetchEffect("Prop_WearResister");
		if(RES==null) RES=I.fetchEffect("Prop_HaveResister");
		Ability CAST=I.fetchEffect("Prop_WearSpellCast");
		int castMul=1;
		if(CAST==null) CAST=I.fetchEffect("Prop_UseSpellCast");
		if(CAST==null) CAST=I.fetchEffect("Prop_UseSpellCast2");
		if(CAST==null) CAST=I.fetchEffect("Prop_HaveSpellCast");
		if(CAST==null){ CAST=I.fetchEffect("Prop_FightSpellCast"); castMul=-1;}
		row.append("<TR>");
		row.append("<TD>"+I.name()+"</TD>");
		row.append("<TD>"+lvl+"</TD>");
		int tlvl=timsLevelCalculator(I,ADJ,RES,CAST,castMul);
		row.append("<TD>"+tlvl+"</TD>");
		int diff=tlvl-lvl; if(diff<0) diff=diff*-1;
		row.append("<TD>"+diff+"</TD>");
		int pct=0;
		if((lvl<0)&&(tlvl>=0)) pct=(int)Math.round(Util.div(tlvl+(lvl*-1),1)*100.0);
		else
		if((tlvl<=0)&&(lvl>0)) pct=(int)Math.round(Util.div((tlvl-lvl),-1)*100.0);
		else
		if((tlvl<0)&&(lvl==0)) pct=(int)Math.round(Util.div(tlvl,-1)*100.0);
		else
		if(lvl==0) pct=(int)Math.round(Util.div(tlvl,1)*100.0);
		else
			pct=(int)Math.round(Util.div(tlvl,lvl)*100.0);
		row.append("<TD>"+pct+"%</TD>");
		
		if(!(I instanceof Weapon))
			row.append("<TD>"+I.baseEnvStats().armor()+"</TD><TD>&nbsp;</TD><TD>&nbsp;</TD>");
		else
		{
			row.append("<TD>&nbsp;</TD><TD>"+I.baseEnvStats().attackAdjustment()+"</TD>");
			row.append("<TD>"+I.baseEnvStats().damage()+"</TD>");
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
	
	public static int timsLevelCalculator(Item I,
										  Ability ADJ,
										  Ability RES,
										  Ability CAST,
										  int castMul)
	{
		int level=0;
		Item savedI=(Item)I.copyOf();
		savedI.recoverEnvStats();
		I=(Item)I.copyOf();
		I.recoverEnvStats();
		int otherDam=0;
		int otherAtt=0;
		int otherArm=0;
		if(ADJ!=null)
		{
			otherArm=Util.getParmPlus(ADJ.text(),"arm")*-1;
			otherAtt=Util.getParmPlus(ADJ.text(),"att");
			otherDam=Util.getParmPlus(ADJ.text(),"dam");
		}
		int curArmor=savedI.baseEnvStats().armor()+otherArm;
		double curAttack=new Integer(savedI.baseEnvStats().attackAdjustment()+otherAtt).doubleValue();
		double curDamage=new Integer(savedI.baseEnvStats().damage()+otherDam).doubleValue();
		if(I instanceof Weapon)
		{
			double weight=new Integer(I.baseEnvStats().weight()).doubleValue();
			if(weight<1.0) weight=1.0;
			double range=new Integer(savedI.maxRange()).doubleValue();
			level=(int)Math.round(Math.floor((2.0*curDamage/(2.0*(I.rawLogicalAnd()?2.0:1.0)+1.0)+(curAttack-weight)/5.0+range)*(range/weight+2.0)))+1;
		}
		else
		{
			long worndata=savedI.rawProperLocationBitmap();
			double weightpts=0;
			for(int i=0;i<Item.wornWeights.length-1;i++)
			{
				if(Util.isSet(worndata,i))
				{
					weightpts+=Item.wornWeights[i+1];
					if(!I.rawLogicalAnd()) break;
				}
			}
			int[] leatherPoints={ 0, 0, 1, 5,10,16,23,31,40,49,58,67,76,85,94};
			int[] clothPoints=  { 0, 3, 7,12,18,25,33,42,52,62,72,82,92,102};
			int[] metalPoints=  { 0, 0, 0, 0, 1, 3, 5, 8,12,17,23,30,38,46,54,62,70,78,86,94};
			int materialCode=savedI.material()&EnvResource.MATERIAL_MASK;
			int[] useArray=null;
			switch(materialCode)
			{
			case EnvResource.MATERIAL_METAL:
			case EnvResource.MATERIAL_MITHRIL:
			case EnvResource.MATERIAL_PRECIOUS:
			case EnvResource.MATERIAL_ENERGY:
				useArray=metalPoints;
				break;
			case EnvResource.MATERIAL_PLASTIC:
			case EnvResource.MATERIAL_LEATHER:
			case EnvResource.MATERIAL_GLASS:
			case EnvResource.MATERIAL_ROCK:
			case EnvResource.MATERIAL_WOODEN:
				useArray=leatherPoints;
				break;
			default:
				useArray=clothPoints;
				break;
			}
			int which=(int)Math.round(Util.div(curArmor,weightpts)+1);
			if(which<0) which=0;
			if(which>=useArray.length)
				which=useArray.length-1;
			level=useArray[which];
		}
		level+=I.baseEnvStats().ability()*5;
		if(CAST!=null)
		{
			String ID=CAST.ID().toUpperCase();
			Vector theSpells=new Vector();
			String names=CAST.text();
			int del=names.indexOf(";");
			while(del>=0)
			{
				String thisOne=names.substring(0,del);
				Ability A=CMClass.getAbility(thisOne);
				if(A!=null)	theSpells.addElement(A);
				names=names.substring(del+1);
				del=names.indexOf(";");
			}
			Ability A=CMClass.getAbility(names);
			if(A!=null) theSpells.addElement(A);
			for(int v=0;v<theSpells.size();v++)
			{
				A=(Ability)theSpells.elementAt(v);
				int mul=1;
				if(A.quality()==Ability.MALICIOUS) mul=-1;
				if(ID.indexOf("HAVE")>=0)
					level+=(mul*CMAble.lowestQualifyingLevel(A.ID()));
				else
					level+=(mul*CMAble.lowestQualifyingLevel(A.ID())/2);
			}
		}
		if(ADJ!=null)
		{
			String newText=ADJ.text();
			int ab=Util.getParmPlus(newText,"abi");
			int arm=Util.getParmPlus(newText,"arm")*-1;
			int att=Util.getParmPlus(newText,"att");
			int dam=Util.getParmPlus(newText,"dam");
			if(savedI instanceof Weapon)
				level+=(arm*2);
			else
			if(savedI instanceof Armor)
			{
				level+=(att/2);
				level+=(dam*3);
			}
			level+=ab*5;
			
			
			int dis=Util.getParmPlus(newText,"dis");
			if(dis!=0) level+=5;
			int sen=Util.getParmPlus(newText,"sen");
			if(sen!=0) level+=5;
			level+=(int)Math.round(5.0*Util.getParmDoublePlus(newText,"spe"));
			for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
			{
				int stat=Util.getParmPlus(newText,CharStats.TRAITS[i].substring(0,3).toLowerCase());
				int max=Util.getParmPlus(newText,("max"+(CharStats.TRAITS[i].substring(0,3).toLowerCase())));
				level+=(stat*5);
				level+=(max*5);
			}

			int hit=Util.getParmPlus(newText,"hit");
			int man=Util.getParmPlus(newText,"man");
			int mv=Util.getParmPlus(newText,"mov");
			level+=(hit/5);
			level+=(man/5);
			level+=(mv/5);
		}
		
		return level;
	}
	
}
