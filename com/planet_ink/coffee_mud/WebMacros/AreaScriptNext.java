package com.planet_ink.coffee_mud.WebMacros;
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
   Copyright 2000-2010 Bo Zimmerman

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
public class AreaScriptNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

	public void addScript(TreeMap<String,ArrayList<ArrayList<String>>> list, 
			ArrayList<String> prefix, String scriptKey, String immediateHost, String key, String file)
	{
		ArrayList<String> next= new ArrayList<String>();
		next.add(scriptKey);
		for(String s : prefix)
			next.add(s);
		if(immediateHost!=null)
			next.add(immediateHost);
		next.add(file);
		ArrayList<ArrayList<String>> subList =list.get(key);
		if(subList == null)
		{
			subList = new ArrayList<ArrayList<String>>();
			list.put(key,subList);
		}
		subList.add(next);
	}
	
	public void addScripts(TreeMap<String,ArrayList<ArrayList<String>>> list, ArrayList<String> prefix, Environmental E)
	{
		if(E==null) return;
		for(int b=0;b<E.numBehaviors();b++)
		{
			Behavior B=E.fetchBehavior(b);
			if(B instanceof ScriptingEngine)
			{
				ScriptingEngine SE=(ScriptingEngine)B;
				Vector files=B.externalFiles();
				for(int f=0;f<files.size();f++)
					addScript(list, prefix, SE.getScriptResourceKey(),B.ID(),((String)files.elementAt(f)).toLowerCase(), (String)files.elementAt(f));
				String nonFiles=((ScriptingEngine)B).getVar("*","COFFEEMUD_SYSTEM_INTERNAL_NONFILENAME_SCRIPT");
				if(nonFiles.trim().length()>0)
					addScript(list, prefix, SE.getScriptResourceKey(), B.ID(),"Custom",nonFiles);
			}
		}
		for(int s=0;s<E.numScripts();s++)
		{
			ScriptingEngine SE=E.fetchScript(s);
			Vector files=SE.externalFiles();
			for(int f=0;f<files.size();f++)
				addScript(list, prefix, SE.getScriptResourceKey(),null,((String)files.elementAt(f)).toLowerCase(), (String)files.elementAt(f));
			String nonFiles=SE.getVar("*","COFFEEMUD_SYSTEM_INTERNAL_NONFILENAME_SCRIPT");
			if(nonFiles.trim().length()>0)
				addScript(list, prefix, SE.getScriptResourceKey(), null,"Custom",nonFiles);
		}
	}
	
	public void addShopScripts(TreeMap<String,ArrayList<ArrayList<String>>> list, ArrayList<String> prefix, Environmental E)
	{
		if(E==null) return;
		ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(E);
		if(SK!=null)
		{
			for(Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
			{
				Environmental E2=(Environmental)i.next();
				ArrayList<String> newPrefix=(ArrayList<String>)prefix.clone();
				newPrefix.add(E2.name());
				addScripts(list,newPrefix,E2);
			}
		}
	}
	
	public TreeMap<String,ArrayList<ArrayList<String>>> getAreaScripts(ExternalHTTPRequests httpReq, String area)
	{
		TreeMap<String,ArrayList<ArrayList<String>>> list;
		list = (TreeMap<String,ArrayList<ArrayList<String>>>)httpReq.getRequestObjects().get("AREA_"+area+" SCRIPTSLIST");
		if(list == null)
		{
			list=new TreeMap<String,ArrayList<ArrayList<String>>>();
			Area A=CMLib.map().getArea(area);
			if(A==null) A=CMLib.map().findArea(area);
			if(A==null) return list;
			Room R=null;
			MOB M=null;
			Item I=null;
			for(Enumeration<String> e=A.getProperRoomnumbers().getRoomIDs();e.hasMoreElements();)
			{
				R=CMLib.map().getRoom(e.nextElement());
				if(R==null) continue;
				CMLib.map().resetRoom(R);
				
				ArrayList<String> prefix = new ArrayList<String>();
				prefix.add(A.name());
				prefix.add(CMLib.map().getExtendedRoomID(R));
				addScripts(list,prefix,R);
				addShopScripts(list,prefix,R);
				for(int m=0;m<R.numInhabitants();m++)
				{
					M=R.fetchInhabitant(m); if(M==null) continue;
					ArrayList<String> prefixM=(ArrayList<String>)prefix.clone();
					prefix.add(M.name());
					addScripts(list,prefixM,M);
					addShopScripts(list,prefixM,M);
					for(int i=0;i<M.inventorySize();i++)
					{
						I=M.fetchInventory(i); if(I==null) continue;
						ArrayList<String> prefixI=(ArrayList<String>)prefixM.clone();
						prefix.add(M.name());
						addScripts(list,prefixI,I);
						addShopScripts(list,prefixI,I);
					}
				}
				for(int i=0;i<R.numItems();i++)
				{
					I=R.fetchItem(i); if(I==null) continue;
					ArrayList<String> prefixI=(ArrayList<String>)prefix.clone();
					addScripts(list,prefixI,I);
					addShopScripts(list,prefixI,I);
				}
			}
			httpReq.getRequestObjects().put("AREA_"+area+" SCRIPTSLIST",list);
		}
		return list;
	}
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String area=httpReq.getRequestParameter("AREA");
		if((area==null)||(area.length()==0)) return "@break@";
		String last=httpReq.getRequestParameter("AREASCRIPT");
		if(parms.containsKey("RESET"))
		{
			if(last!=null) httpReq.removeRequestParameter("AREASCRIPT");
			return "";
		}
		String lastID="";
		TreeMap<String,ArrayList<ArrayList<String>>> list = getAreaScripts(httpReq,area);
		for(String scriptName : list.keySet())
		{
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!scriptName.equals(lastID))))
			{
				httpReq.addRequestParameters("AREASCRIPT",scriptName);
				last=scriptName;
			}
			lastID=scriptName;
		}
		httpReq.addRequestParameters("AREASCRIPT","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}
}
