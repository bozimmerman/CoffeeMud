package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2008 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class TimsItemTable extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		long endTime=System.currentTimeMillis()+(1000*60*10);
		int min=CMath.s_int((httpReq.getRequestParameter("MIN")));
		if(min>0)
			endTime=System.currentTimeMillis()+(1000*60*((long)min));
		
		StringBuffer str=new StringBuffer("<TABLE WIDTH=100% BORDER=1>");
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
					Vector V2=S.getShop().getStoreInventory();
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
		int lvl=I.envStats().level();
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
	
}
