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
   Copyright 2002-2024 Bo Zimmerman

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
	private static Comparator<Area> stringPairComparator = new Comparator<Area>()
	{

		@Override
		public int compare(final Area o1, final Area o2)
		{
			return o1.Name().compareToIgnoreCase(o2.Name());
		}

	};

	public static PairList<String,String> buildAreaTree(final Enumeration<Area> a, final List<Area> parents, final Area pickedA, final int dashes, final boolean deeper)
	{
		final PairArrayList<String,String> areaNames = new PairArrayList<String,String>();
		for(;a.hasMoreElements();)
		{
			final Area A=a.nextElement();
			final String areaName = CMStrings.repeat('-', dashes) + A.Name();
			areaNames.add(A.Name(),areaName);
			if(deeper
			&& ((pickedA == A)||(parents.contains(A))))
			{
				final XVector<Area> children = new XVector<Area>(A.getChildren());
				Collections.sort(children, stringPairComparator);
				areaNames.addAll(buildAreaTree(children.elements(),parents,pickedA,dashes+1,pickedA != A));
			}
		}
		return areaNames;
	}

	public static String getAreaList(final Enumeration<Area> a, final Area pickedA, final MOB mob, final boolean noInstances, final boolean asTree)
	{
		final StringBuffer areaListStr=new StringBuffer("");
		final boolean anywhere=(CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.CMDROOMS)||CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.CMDAREAS));
		final boolean everywhere=(CMSecurity.isASysOp(mob)||CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CMDROOMS)||CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CMDAREAS));

		final List<Area> subAreas = new ArrayList<Area>();
		for(;a.hasMoreElements();)
		{
			final Area A=a.nextElement();
			if((everywhere||(A.amISubOp(mob.Name())&&anywhere))
			&&((!noInstances)||(!CMath.bset(A.flags(), Area.FLAG_INSTANCE_CHILD))))
				subAreas.add(A);
		}

		final PairList<String,String> areaNames = new PairArrayList<String,String>(subAreas.size());
		final String pickedAName= (pickedA != null) ? pickedA.Name(): null;
		if((asTree)&&(pickedA!=null))
			areaNames.addAll(buildAreaTree(new IteratorEnumeration<Area>(subAreas.iterator()),pickedA.getParentsRecurse(),pickedA,0,pickedA != null));
		else
		{
			for(final Area A : subAreas)
				areaNames.add(A.Name(),A.Name());
		}
		for(final Pair<String,String> p : areaNames)
		{
			if(pickedAName==p.first)
				areaListStr.append("<OPTION SELECTED VALUE=\""+p.first+"\">"+p.second);
			else
				areaListStr.append("<OPTION VALUE=\""+p.first+"\">"+p.second);
		}
		return areaListStr.toString();
	}

	public static String doBehavs(final PhysicalAgent E, final HTTPRequest httpReq, final java.util.Map<String,String> parms)
	{
		E.delAllBehaviors();
		String errors = "";
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
						errors += "Unknown behavior '"+behav+"'.";
					else
					{
						try
						{
							B.setParms(theparm);
							E.addBehavior(B);
							B.startBehavior(E);
						}
						catch(final Exception e)
						{
							if(e != null)
								errors += e.getMessage();
						}
					}
				}
				num++;
				behav=httpReq.getUrlParameter("BEHAV"+num);
				theparm=httpReq.getUrlParameter("BDATA"+num);
			}
		}
		return errors;
	}

	public static String doAffects(final Physical P, final HTTPRequest httpReq, final java.util.Map<String,String> parms)
	{
		P.delAllEffects(false);
		String errors = "";
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
						errors += "Unknown Effect '"+aff+"'.";
					else
					{
						try
						{
							B.setMiscText(theparm);
							P.addNonUninvokableEffect(B);
						}
						catch(final Exception e)
						{
							if(e != null)
								errors += e.getMessage();
						}
					}
				}
				num++;
				aff=httpReq.getUrlParameter("AFFECT"+num);
				theparm=httpReq.getUrlParameter("ADATA"+num);
			}
		}
		return errors;
	}

	public static String modifyArea(final HTTPRequest httpReq, final java.util.Map<String,String> parms)
	{
		final Set<Area> areasNeedingUpdates=new HashSet<Area>();
		final String last=httpReq.getUrlParameter("AREA");
		if((last==null)||(last.length()==0))
			return "Old area name not defined!";
		Area A=CMLib.map().getArea(last);
		if(A==null)
			return "Old Area not defined!";
		areasNeedingUpdates.add(A);

		boolean redoAllMyDamnRooms=false;
		List<Room> allMyDamnRooms=null;
		String oldName=null;

		// class!
		final String className=httpReq.getUrlParameter("CLASSES");
		if((className==null)||(className.length()==0))
			return "Please select a class type for this area.";
		if(!className.equalsIgnoreCase(CMClass.classID(A)))
		{
			allMyDamnRooms=new ArrayList<Room>();
			for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
				allMyDamnRooms.add(r.nextElement());
			final Area oldA=A;
			A=CMClass.getAreaType(className);
			if(A==null)
				return "The class you chose does not exist.  Choose another.";
			CMLib.map().delArea(oldA);
			CMLib.map().addArea(A);
			A.setName(oldA.Name());
			redoAllMyDamnRooms=true;
			areasNeedingUpdates.remove(oldA);
			areasNeedingUpdates.add(A);
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
				allMyDamnRooms.add(r.nextElement());
			CMLib.map().delArea(A);
			oldName=A.Name();
			CMLib.database().DBDeleteArea(A);
			//final Area oldA=A;
			A=CMClass.getAreaType(A.ID());
			A.setName(name);
			CMLib.map().addArea(A);
			// next 3 lines prob unnecc since the whole purpose
			// of this code is to fill it from the webgrinder page...
			//A.setMiscText(oldA.text());
			//A.setClimateType(oldA.getClimateTypeCode());
			//A.setSubOpList(oldA.getSubOpList());
			CMLib.map().registerWorldObjectLoaded(A, null, A);
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

		// space stuff
		if(A instanceof SpaceObject)
		{
			final SpaceObject SO=(SpaceObject)A;
			if(httpReq.isUrlParameter("COORDINATES"))
			{
				final List<String> parts=CMParms.parseCommas(httpReq.getUrlParameter("COORDINATES"), true);
				for(int i=0;i<3;i++)
				{
					if(i<parts.size())
						SO.coordinates().set(i,CMath.s_long(parts.get(i)));
				}
				if(CMLib.space().isObjectInSpace(SO))
				{
					CMLib.space().delObjectInSpace(SO);
					CMLib.space().addObjectToSpace(SO, SO.coordinates());
					CMLib.space().moveSpaceObject(SO);
				}
			}

			if(httpReq.isUrlParameter("COORDINATES0"))
				SO.coordinates().x(CMath.s_long(httpReq.getUrlParameter("COORDINATES0")));
			if(httpReq.isUrlParameter("COORDINATES1"))
				SO.coordinates().y(CMath.s_long(httpReq.getUrlParameter("COORDINATES1")));
			if(httpReq.isUrlParameter("COORDINATES2"))
			{
				SO.coordinates().z(CMath.s_long(httpReq.getUrlParameter("COORDINATES2")));
				if(CMLib.space().isObjectInSpace(SO))
				{
					CMLib.space().delObjectInSpace(SO);
					CMLib.space().addObjectToSpace(SO, SO.coordinates());
					CMLib.space().moveSpaceObject(SO);
				}
			}

			if(httpReq.isUrlParameter("RADIUS"))
				SO.setRadius(CMath.s_long(httpReq.getUrlParameter("RADIUS")));

			if(httpReq.isUrlParameter("DIRECTION"))
			{
				final List<String> parts=CMParms.parseCommas(httpReq.getUrlParameter("DIRECTION"), true);
				for(int i=0;i<3;i++)
				{
					if(i<parts.size())
						SO.direction().set(i,CMath.s_double(parts.get(i)));
				}
			}

			if(httpReq.isUrlParameter("DIRECTION0"))
				SO.direction().xy(CMath.s_double(httpReq.getUrlParameter("DIRECTION0")));
			if(httpReq.isUrlParameter("DIRECTION1"))
				SO.direction().z(CMath.s_double(httpReq.getUrlParameter("DIRECTION1")));

			if(httpReq.isUrlParameter("SPEED"))
				SO.setSpeed(CMath.s_long(httpReq.getUrlParameter("SPEED")));
		}

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
		A.setDescription(CMLib.coffeeFilter().safetyInFilter(desc));

		// image
		String img=httpReq.getUrlParameter("IMAGE");
		if(img==null)
			img="";
		A.setImage(CMLib.coffeeFilter().safetyInFilter(img));

		// playerlevel
		final String plvl=httpReq.getUrlParameter("PLAYERLEVEL");
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
		A.setAuthorID(CMLib.coffeeFilter().safetyInFilter(author));

		// currency
		String currency=httpReq.getUrlParameter("CURRENCY");
		if(currency==null)
			currency="";
		A.setCurrency(CMLib.coffeeFilter().safetyInFilter(currency));

		// SHOPPREJ
		String SHOPPREJ=httpReq.getUrlParameter("SHOPPREJ");
		if(SHOPPREJ==null)
			SHOPPREJ="";
		A.setPrejudiceFactors(CMLib.coffeeFilter().safetyInFilter(SHOPPREJ));

		// BUDGET
		String BUDGET=httpReq.getUrlParameter("BUDGET");
		if(BUDGET==null)
			BUDGET="";
		A.setBudget(CMLib.coffeeFilter().safetyInFilter(BUDGET));

		// DEVALRATE
		String DEVALRATE=httpReq.getUrlParameter("DEVALRATE");
		if(DEVALRATE==null)
			DEVALRATE="";
		A.setDevalueRate(CMLib.coffeeFilter().safetyInFilter(DEVALRATE));

		// INVRESETRATE
		String INVRESETRATE=httpReq.getUrlParameter("INVRESETRATE");
		if(INVRESETRATE==null)
			INVRESETRATE="0";
		A.setInvResetRate(CMath.s_int(CMLib.coffeeFilter().safetyInFilter(INVRESETRATE)));

		// IGNOREMASK
		String IGNOREMASK=httpReq.getUrlParameter("IGNOREMASK");
		if(IGNOREMASK==null)
			IGNOREMASK="";
		A.setIgnoreMask(CMLib.coffeeFilter().safetyInFilter(IGNOREMASK));

		if(A instanceof AutoGenArea)
		{
			String AGXMLPATH=httpReq.getUrlParameter("AGXMLPATH");
			if(AGXMLPATH==null)
				AGXMLPATH="";
			((AutoGenArea) A).setGeneratorXmlPath(CMLib.coffeeFilter().safetyInFilter(AGXMLPATH));

			String AGAUTOVAR=httpReq.getUrlParameter("AGAUTOVAR");
			if(AGAUTOVAR==null)
				AGAUTOVAR="";
			((AutoGenArea) A).setAutoGenVariables(CMLib.coffeeFilter().safetyInFilter(AGAUTOVAR));
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
		final Area defaultParentArea=CMLib.map().getDefaultParentArea();
		final List<Area> existingParents=new XVector<Area>(A.getParents());
		if(defaultParentArea != null)
			existingParents.remove(defaultParentArea);
		final List<Area> newParents=new ArrayList<Area>();
		for(int i=1;;i++)
		{
			if(httpReq.isUrlParameter("PARENT"+(Integer.toString(i))))
			{
				final Area parent=CMLib.map().getArea(httpReq.getUrlParameter("PARENT"+(Integer.toString(i))));
				if(parent!=null)
					newParents.add(parent);
			}
			else
				break;
		}
		for(final Area parent : existingParents)
		{
			if(!newParents.contains(parent))
			{
				A.removeParent(parent);
				parent.removeChild(A);
				areasNeedingUpdates.add(parent);
			}
		}
		for(final Area parent : newParents)
		{
			if(!existingParents.contains(parent))
			{
				if(A.canParent(parent))
				{
					A.addParent(parent);
					parent.addChild(A);
					areasNeedingUpdates.add(parent);
				}
				else
					return "The area, '"+parent.Name()+"', cannot be added as a parent, as this would create a circular reference.";
			}
		}

		// modify Child Area list
		final List<Area> existingChildren=new XVector<Area>(A.getChildren());
		if(defaultParentArea == A)
			existingChildren.clear();
		final List<Area> newChildren=new ArrayList<Area>();
		for(int i=1;;i++)
		{
			if(httpReq.isUrlParameter("CHILDREN"+(Integer.toString(i))))
			{
				final Area child=CMLib.map().getArea(httpReq.getUrlParameter("CHILDREN"+(Integer.toString(i))));
				if(child!=null)
					newChildren.add(child);
			}
			else
				break;
		}
		for(final Area child : existingChildren)
		{
			if(!newChildren.contains(child))
			{
				A.removeChild(child);
				child.removeParent(A);
				areasNeedingUpdates.add(child);
			}
		}
		for(final Area child : newChildren)
		{
			if(!existingChildren.contains(child))
			{
				if(A.canChild(child))
				{
					A.addChild(child);
					child.addParent(A);
					areasNeedingUpdates.add(child);
				}
				else
					return "The area, '"+child.Name()+"', cannot be added as a child, as this would create a circular reference.";
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

		for(final Area A2 : areasNeedingUpdates) // will always include A
		{
			if(CMLib.flags().isSavable(A2))
			{
				CMLib.database().DBUpdateArea(A2.Name(),A2);
				CMLib.coffeeMaker().addAutoPropsToAreaIfNecessary(A2);
			}
		}
		return "";
	}
}
