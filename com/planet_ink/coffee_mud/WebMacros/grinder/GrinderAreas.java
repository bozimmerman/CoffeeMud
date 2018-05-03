package com.planet_ink.coffee_mud.WebMacros.grinder;

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
   Copyright 2002-2018 Bo Zimmerman

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

public class GrinderAreas
{
	public static String getAreaList(Enumeration<Area> a, Area pickedA, MOB mob, boolean noInstances)
	{
		final StringBuffer AreaList=new StringBuffer("");
		final boolean anywhere=(CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.CMDROOMS)||CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.CMDAREAS));
		final boolean everywhere=(CMSecurity.isASysOp(mob)||CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CMDROOMS)||CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CMDAREAS));
		for(;a.hasMoreElements();)
		{
			final Area A=a.nextElement();
			if((everywhere||(A.amISubOp(mob.Name())&&anywhere))
			&&((!noInstances)||(!CMath.bset(A.flags(), Area.FLAG_INSTANCE_CHILD))))
			{
				if((pickedA!=null)&&(pickedA==A))
					AreaList.append("<OPTION SELECTED VALUE=\""+A.Name()+"\">"+A.name());
				else
					AreaList.append("<OPTION VALUE=\""+A.Name()+"\">"+A.name());
			}
		}
		return AreaList.toString();
	}

	public static String doBehavs(PhysicalAgent E, HTTPRequest httpReq, java.util.Map<String,String> parms)
	{
		E.delAllBehaviors();
		if(httpReq.isUrlParameter("BEHAV1"))
		{
			int num=1;
			String behav=httpReq.getUrlParameter("BEHAV"+num);
			String theparm=httpReq.getUrlParameter("BDATA"+num);
			while((behav!=null)&&(theparm!=null))
			{
				if(behav.length()>0)
				{
					final Behavior B=CMClass.getBehavior(behav);
					if(B==null)
						return "Unknown behavior '"+behav+"'.";
					B.setParms(theparm);
					E.addBehavior(B);
					B.startBehavior(E);
				}
				num++;
				behav=httpReq.getUrlParameter("BEHAV"+num);
				theparm=httpReq.getUrlParameter("BDATA"+num);
			}
		}
		return "";
	}

	public static String doAffects(Physical P, HTTPRequest httpReq, java.util.Map<String,String> parms)
	{
		P.delAllEffects(false);
		if(httpReq.isUrlParameter("AFFECT1"))
		{
			int num=1;
			String aff=httpReq.getUrlParameter("AFFECT"+num);
			String theparm=httpReq.getUrlParameter("ADATA"+num);
			while((aff!=null)&&(theparm!=null))
			{
				if(aff.length()>0)
				{
					final Ability B=CMClass.getAbility(aff);
					if(B==null)
						return "Unknown Effect '"+aff+"'.";
					B.setMiscText(theparm);
					P.addNonUninvokableEffect(B);
				}
				num++;
				aff=httpReq.getUrlParameter("AFFECT"+num);
				theparm=httpReq.getUrlParameter("ADATA"+num);
			}
		}
		return "";
	}

	public static String modifyArea(HTTPRequest httpReq, java.util.Map<String,String> parms)
	{
		final Vector<Area> areasNeedingUpdates=new Vector<Area>();
		final String last=httpReq.getUrlParameter("AREA");
		if((last==null)||(last.length()==0))
			return "Old area name not defined!";
		Area A=CMLib.map().getArea(last);
		if(A==null)
			return "Old Area not defined!";
		areasNeedingUpdates.addElement(A);

		boolean redoAllMyDamnRooms=false;
		Vector<Room> allMyDamnRooms=null;
		String oldName=null;

		// class!
		final String className=httpReq.getUrlParameter("CLASSES");
		if((className==null)||(className.length()==0))
			return "Please select a class type for this area.";
		if(!className.equalsIgnoreCase(CMClass.classID(A)))
		{
			allMyDamnRooms=new Vector<Room>();
			for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
				allMyDamnRooms.addElement(r.nextElement());
			final Area oldA=A;
			A=CMClass.getAreaType(className);
			if(A==null)
				return "The class you chose does not exist.  Choose another.";
			CMLib.map().delArea(oldA);
			CMLib.map().addArea(A);
			A.setName(oldA.Name());
			redoAllMyDamnRooms=true;
			areasNeedingUpdates.remove(oldA);
			areasNeedingUpdates.addElement(A);
		}

		// name
		String name=httpReq.getUrlParameter("NAME");
		if((name==null)||(name.length()==0))
			return "Please enter a name for this area.";
		name=name.trim();
		if(!name.equals(A.Name().trim()))
		{
			if((CMLib.map().getArea(name)!=null)||(CMLib.map().getShip(name)!=null))
				return "The name you chose is already in use.  Please enter another.";
			allMyDamnRooms=new Vector<Room>();
			for(final Enumeration<Room> r=A.getCompleteMap();r.hasMoreElements();)
				allMyDamnRooms.addElement(r.nextElement());
			CMLib.map().delArea(A);
			oldName=A.Name();
			CMLib.database().DBDeleteArea(A);
			A=CMClass.getAreaType(A.ID());
			A.setName(name);
			CMLib.map().addArea(A);
			CMLib.database().DBCreateArea(A);
			redoAllMyDamnRooms=true;
			httpReq.addFakeUrlParameter("AREA",A.Name());
		}

		// climate
		if(httpReq.isUrlParameter("CLIMATE"))
		{
			int climate=CMath.s_int(httpReq.getUrlParameter("CLIMATE"));
			if(climate>=0)
			{
				for(int i=1;;i++)
				{
					if(httpReq.isUrlParameter("CLIMATE"+(Integer.toString(i))))
					{
						final int newVal=CMath.s_int(httpReq.getUrlParameter("CLIMATE"+(Integer.toString(i))));
						if(newVal<0)
						{
							climate=-1;
							break;
						}
						climate=climate|newVal;
					}
					else
						break;
				}
			}
			A.setClimateType(climate);
		}
		else
			A.setClimateType(-1);

		// atmosphere
		if(httpReq.isUrlParameter("ATMOSPHERE"))
			A.setAtmosphere(CMath.s_int(httpReq.getUrlParameter("ATMOSPHERE")));

		// tech level
		if(httpReq.isUrlParameter("THEME"))
			A.setTheme(CMath.s_int(httpReq.getUrlParameter("THEME")));

		// modify subop list
		for(final Enumeration<String> s=A.subOps();s.hasMoreElements();)
			A.delSubOp(s.nextElement());
		for(int i=1;;i++)
		{
			if(httpReq.isUrlParameter("SUBOP"+(Integer.toString(i))))
				A.addSubOp(httpReq.getUrlParameter("SUBOP"+(Integer.toString(i))));
			else
				break;
		}

		int num=1;
		if(httpReq.isUrlParameter("BLURBFLAG1"))
		{
			final Vector<String> prics=new Vector<String>();
			String DOUBLE=httpReq.getUrlParameter("BLURBFLAG"+num);
			String MASK=httpReq.getUrlParameter("BLURB"+num);
			while((DOUBLE!=null)&&(MASK!=null))
			{
				if(DOUBLE.trim().length()>0)
					prics.addElement((DOUBLE.toUpperCase().trim()+" "+MASK).trim());
				num++;
				DOUBLE=httpReq.getUrlParameter("BLURBFLAG"+num);
				MASK=httpReq.getUrlParameter("BLURB"+num);
			}
			for(final Enumeration<String> f=A.areaBlurbFlags();f.hasMoreElements();)
				A.delBlurbFlag(f.nextElement());
			for(int v=0;v<prics.size();v++)
				A.addBlurbFlag(prics.elementAt(v));
		}
		// description
		String desc=httpReq.getUrlParameter("DESCRIPTION");
		if(desc==null)
			desc="";
		A.setDescription(CMLib.coffeeFilter().safetyFilter(desc));

		// image
		String img=httpReq.getUrlParameter("IMAGE");
		if(img==null)
			img="";
		A.setImage(CMLib.coffeeFilter().safetyFilter(img));
		
		// playerlevel
		String plvl=httpReq.getUrlParameter("PLAYERLEVEL");
		if(plvl!=null)
			A.setPlayerLevel(CMath.s_int(plvl));

		// gridy
		final String gridy=httpReq.getUrlParameter("GRIDY");
		if((gridy!=null)&&(A instanceof GridZones))
			((GridZones)A).setYGridSize(CMath.s_int(gridy));
		// gridx
		final String gridx=httpReq.getUrlParameter("GRIDX");
		if((gridx!=null)&&(A instanceof GridZones))
			((GridZones)A).setXGridSize(CMath.s_int(gridx));

		// author
		String author=httpReq.getUrlParameter("AUTHOR");
		if(author==null)
			author="";
		A.setAuthorID(CMLib.coffeeFilter().safetyFilter(author));

		// currency
		String currency=httpReq.getUrlParameter("CURRENCY");
		if(currency==null)
			currency="";
		A.setCurrency(CMLib.coffeeFilter().safetyFilter(currency));

		// SHOPPREJ
		String SHOPPREJ=httpReq.getUrlParameter("SHOPPREJ");
		if(SHOPPREJ==null)
			SHOPPREJ="";
		A.setPrejudiceFactors(CMLib.coffeeFilter().safetyFilter(SHOPPREJ));

		// BUDGET
		String BUDGET=httpReq.getUrlParameter("BUDGET");
		if(BUDGET==null)
			BUDGET="";
		A.setBudget(CMLib.coffeeFilter().safetyFilter(BUDGET));

		// DEVALRATE
		String DEVALRATE=httpReq.getUrlParameter("DEVALRATE");
		if(DEVALRATE==null)
			DEVALRATE="";
		A.setDevalueRate(CMLib.coffeeFilter().safetyFilter(DEVALRATE));

		// INVRESETRATE
		String INVRESETRATE=httpReq.getUrlParameter("INVRESETRATE");
		if(INVRESETRATE==null)
			INVRESETRATE="0";
		A.setInvResetRate(CMath.s_int(CMLib.coffeeFilter().safetyFilter(INVRESETRATE)));

		// IGNOREMASK
		String IGNOREMASK=httpReq.getUrlParameter("IGNOREMASK");
		if(IGNOREMASK==null)
			IGNOREMASK="";
		A.setIgnoreMask(CMLib.coffeeFilter().safetyFilter(IGNOREMASK));

		if(A instanceof AutoGenArea)
		{
			String AGXMLPATH=httpReq.getUrlParameter("AGXMLPATH");
			if(AGXMLPATH==null)
				AGXMLPATH="";
			((AutoGenArea) A).setGeneratorXmlPath(CMLib.coffeeFilter().safetyFilter(AGXMLPATH));

			String AGAUTOVAR=httpReq.getUrlParameter("AGAUTOVAR");
			if(AGAUTOVAR==null)
				AGAUTOVAR="";
			((AutoGenArea) A).setAutoGenVariables(CMLib.coffeeFilter().safetyFilter(AGAUTOVAR));
		}

		// PRICEFACTORS
		num=1;
		if(httpReq.isUrlParameter("IPRIC1"))
		{
			final Vector<String> prics=new Vector<String>();
			String DOUBLE=httpReq.getUrlParameter("IPRIC"+num);
			String MASK=httpReq.getUrlParameter("IPRICM"+num);
			while((DOUBLE!=null)&&(MASK!=null))
			{

				if(CMath.isNumber(DOUBLE))
					prics.addElement((DOUBLE+" "+MASK).trim());
				num++;
				DOUBLE=httpReq.getUrlParameter("IPRIC"+num);
				MASK=httpReq.getUrlParameter("IPRICM"+num);
			}
			((Economics)A).setItemPricingAdjustments(CMParms.toStringArray(prics));
		}

		// modify Parent Area list
		while(A.getParents().hasMoreElements())
			A.removeParent(A.getParents().nextElement());
		for(int i=1;;i++)
		{
			if(httpReq.isUrlParameter("PARENT"+(Integer.toString(i))))
			{
				final Area parent=CMLib.map().getArea(httpReq.getUrlParameter("PARENT"+(Integer.toString(i))));
				if(parent!=null)
				{
					if(A.canParent(parent))
					{
						A.addParent(parent);
						parent.addChild(A);
						areasNeedingUpdates.addElement(parent);
					}
					else
						return "The area, '"+parent.Name()+"', cannot be added as a parent, as this would create a circular reference.";
				}
			}
			else
				break;
		}

		// modify Child Area list
		while(A.getChildren().hasMoreElements())
			A.removeChild(A.getChildren().nextElement());
		for(int i=1;;i++)
		{
			if(httpReq.isUrlParameter("CHILDREN"+(Integer.toString(i))))
			{
				final Area child=CMLib.map().getArea(httpReq.getUrlParameter("CHILDREN"+(Integer.toString(i))));
				if(child!=null)
				{
					if(A.canChild(child))
					{
						A.addChild(child);
						child.addParent(A);
						areasNeedingUpdates.addElement(child);
					}
					else
						return "The area, '"+child.Name()+"', cannot be added as a child, as this would create a circular reference.";
				}
				else
					break;
			}
		}

		String error=GrinderAreas.doAffects(A,httpReq,parms);
		if(error.length()>0)
			return error;
		error=GrinderAreas.doBehavs(A,httpReq,parms);
		if(error.length()>0)
			return error;

		if((redoAllMyDamnRooms)&&(allMyDamnRooms!=null))
			CMLib.map().renameRooms(A,oldName,allMyDamnRooms);

		for(int i=0;i<areasNeedingUpdates.size();i++) // will always include A
		{
			final Area A2=areasNeedingUpdates.elementAt(i);
			if(CMLib.flags().isSavable(A2))
			{
				CMLib.database().DBUpdateArea(A2.Name(),A2);
				CMLib.coffeeMaker().addAutoPropsToAreaIfNecessary(A2);
			}
		}
		return "";
	}
}
