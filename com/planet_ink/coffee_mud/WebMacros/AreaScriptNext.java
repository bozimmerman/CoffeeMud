package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.interfaces.*;
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
   Copyright 2010-2018 Bo Zimmerman

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
	@Override
	public String name()
	{
		return "AreaScriptNext";
	}

	@Override
	public boolean isAdminMacro()
	{
		return true;
	}

	protected class AreaScriptInstance
	{
		public List<String>	path;
		public String		instanceKey;
		public String		fileName;
		public String		key;
		public String		customScript	= "";

		public AreaScriptInstance(String instanceKey, List<String> path,
								  String key, String fileName)
		{
			this.path=path;
			this.instanceKey=instanceKey;
			this.fileName=fileName;
			this.key=key;
		}
	}

	public AreaScriptInstance addScript(TreeMap<String,ArrayList<AreaScriptInstance>> list,
			ArrayList<String> prefix, String scriptKey, String immediateHost, String key, String file)
	{
		final ArrayList<String> next=(ArrayList<String>)prefix.clone();
		if(immediateHost!=null)
			next.add(immediateHost);
		ArrayList<AreaScriptInstance> subList =list.get(key);
		if(subList == null)
		{
			subList = new ArrayList<AreaScriptInstance>();
			list.put(key,subList);
		}
		final AreaScriptInstance inst = new AreaScriptInstance(scriptKey, next, key, file);
		subList.add(inst);
		return inst;
	}

	public void addScripts(TreeMap<String,ArrayList<AreaScriptInstance>> list, ArrayList<String> prefix, PhysicalAgent E)
	{
		if(E==null)
			return;
		for(final Enumeration<Behavior> e=E.behaviors();e.hasMoreElements();)
		{
			final Behavior B=e.nextElement();
			if(B instanceof ScriptingEngine)
			{
				if(!B.isSavable())
					continue;
				final ScriptingEngine SE=(ScriptingEngine)B;
				final List<String> files=B.externalFiles();
				if(files!=null)
				for(int f=0;f<files.size();f++)
					addScript(list, prefix, SE.getScriptResourceKey(),B.ID(),files.get(f).toLowerCase(), files.get(f));
				final String nonFiles=((ScriptingEngine)B).getVar("*","COFFEEMUD_SYSTEM_INTERNAL_NONFILENAME_SCRIPT");
				if((nonFiles!=null)&&(nonFiles.trim().length()>0))
				{
					final AreaScriptInstance inst =
						addScript(list, prefix, SE.getScriptResourceKey(), B.ID(),"Custom",nonFiles);
					inst.customScript = nonFiles.trim();
				}
			}
		}
		for(final Enumeration<ScriptingEngine> e=E.scripts();e.hasMoreElements();)
		{
			final ScriptingEngine SE=e.nextElement();
			if(!SE.isSavable())
				continue;
			final List<String> files=SE.externalFiles();
			for(int f=0;f<files.size();f++)
				addScript(list, prefix, SE.getScriptResourceKey(),null,files.get(f).toLowerCase(), files.get(f));
			final String nonFiles=SE.getVar("*","COFFEEMUD_SYSTEM_INTERNAL_NONFILENAME_SCRIPT");
			if(nonFiles.trim().length()>0)
			{
				final AreaScriptInstance inst =
					addScript(list, prefix, SE.getScriptResourceKey(), null,"Custom",nonFiles);
				inst.customScript = nonFiles.trim();
			}
		}
	}

	public void addShopScripts(TreeMap<String,ArrayList<AreaScriptInstance>> list, ArrayList<String> prefix, PhysicalAgent E)
	{
		if(E==null)
			return;
		final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(E);
		if(SK!=null)
		{
			for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
			{
				final Environmental E2=i.next();
				final ArrayList<String> newPrefix=(ArrayList<String>)prefix.clone();
				newPrefix.add(E2.name());
				if(E2 instanceof PhysicalAgent)
					addScripts(list,newPrefix,(PhysicalAgent)E2);
			}
		}
	}

	public TreeMap<String,ArrayList<AreaScriptInstance>> getAreaScripts(HTTPRequest httpReq, String area)
	{
		TreeMap<String,ArrayList<AreaScriptInstance>> list;
		list = (TreeMap<String,ArrayList<AreaScriptInstance>>)httpReq.getRequestObjects().get("AREA_"+area+" SCRIPTSLIST");
		if(list == null)
		{
			list=new TreeMap<String,ArrayList<AreaScriptInstance>>();
			Area A=CMLib.map().getArea(area);
			if(A==null)
				A=CMLib.map().findArea(area);
			if(A==null)
				return list;
			Room R=null;
			WorldMap.LocatedPair LP=null;
			PhysicalAgent AE=null;
			ArrayList<String> prefix = new ArrayList<String>();
			for(final Enumeration<WorldMap.LocatedPair> ae=CMLib.map().scriptHosts(A);ae.hasMoreElements();)
			{
				LP=ae.nextElement(); if(LP==null) continue;
				AE=LP.obj(); if(AE==null) continue;
				R=LP.room(); if(R==null) R=CMLib.map().getStartRoom(AE);

				prefix = new ArrayList<String>();
				prefix.add(A.name());

				if(AE instanceof Area)
				{
					// don't add room to prefix
				}
				else
				if(AE instanceof Room)
					prefix.add(CMLib.map().getExtendedRoomID((Room)AE));
				else
				{
					if(R!=null)
						prefix.add(CMLib.map().getExtendedRoomID(R));
					if(AE instanceof Item)
					{
						final ItemPossessor IP=((Item)AE).owner();
						if(IP instanceof MOB)
							prefix.add(IP.Name());
					}
					prefix.add(AE.Name());
				}

				addScripts(list,prefix,AE);
				addShopScripts(list,prefix,AE);
			}
			httpReq.getRequestObjects().put("AREA_"+area+" SCRIPTSLIST",list);
		}
		return list;
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String area=httpReq.getUrlParameter("AREA");
		if((area==null)||(area.length()==0))
			return "@break@";
		String last=httpReq.getUrlParameter("AREASCRIPT");
		if(parms.containsKey("RESET"))
		{
			if(last!=null)
				httpReq.removeUrlParameter("AREASCRIPT");
			return "";
		}
		String lastID="";
		final TreeMap<String,ArrayList<AreaScriptInstance>> list = getAreaScripts(httpReq,area);
		for(final String scriptName : list.keySet())
		{
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!scriptName.equals(lastID))))
			{
				httpReq.addFakeUrlParameter("AREASCRIPT",scriptName);
				last=scriptName;
				return "";
			}
			lastID=scriptName;
		}
		httpReq.addFakeUrlParameter("AREASCRIPT","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}
}
