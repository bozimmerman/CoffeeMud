package com.planet_ink.coffee_mud.web.macros.grinder;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class GrinderAreas
{
	public static String getAreaList(Area pickedA, MOB mob)
	{
		StringBuffer AreaList=new StringBuffer("");
		for(Enumeration a=CMMap.areas();a.hasMoreElements();)
		{
			Area A=(Area)a.nextElement();
			if((A.amISubOp(mob.Name()))||(mob.isASysOp(null)))
				if((pickedA!=null)&&(pickedA==A))
					AreaList.append("<OPTION SELECTED VALUE=\""+A.Name()+"\">"+A.name());
				else
					AreaList.append("<OPTION VALUE=\""+A.Name()+"\">"+A.name());
		}
		return AreaList.toString();
	}

	public static String doAffectsNBehavs(Environmental E, ExternalHTTPRequests httpReq, Hashtable parms)
	{
		while(E.numBehaviors()>0)
			E.delBehavior(E.fetchBehavior(0));
		if(httpReq.isRequestParameter("BEHAV1"))
		{
			int num=1;
			String behav=httpReq.getRequestParameter("BEHAV"+num);
			String theparm=httpReq.getRequestParameter("BDATA"+num);
			while((behav!=null)&&(theparm!=null))
			{
				if(behav.length()>0)
				{
					Behavior B=CMClass.getBehavior(behav);
					if(theparm==null) theparm="";
					if(B==null) return "Unknown behavior '"+behav+"'.";
					B.setParms(theparm);
					E.addBehavior(B);
					B.startBehavior(E);
				}
				num++;
				behav=httpReq.getRequestParameter("BEHAV"+num);
				theparm=httpReq.getRequestParameter("BDATA"+num);
			}
		}
		while(E.numEffects()>0)
			E.delEffect(E.fetchEffect(0));
		if(httpReq.isRequestParameter("AFFECT1"))
		{
			int num=1;
			String aff=httpReq.getRequestParameter("AFFECT"+num);
			String theparm=httpReq.getRequestParameter("ADATA"+num);
			while((aff!=null)&&(theparm!=null))
			{
				if(aff.length()>0)
				{
					Ability B=CMClass.getAbility(aff);
					if(theparm==null) theparm="";
					if(B==null) return "Unknown Effect '"+aff+"'.";
					B.setMiscText(theparm);
					E.addNonUninvokableEffect(B);
				}
				num++;
				aff=httpReq.getRequestParameter("AFFECT"+num);
				theparm=httpReq.getRequestParameter("ADATA"+num);
			}
		}
		return "";
	}

	public static String modifyArea(ExternalHTTPRequests httpReq, Hashtable parms)
	{
        Vector areasNeedingUpdates=new Vector();
		String last=httpReq.getRequestParameter("AREA");
		if((last==null)||(last.length()==0)) return "Old area name not defined!";
		Area A=CMMap.getArea(last);
		if(A==null) return "Old Area not defined!";
		areasNeedingUpdates.addElement(A);

		boolean redoAllMyDamnRooms=false;
		Vector allMyDamnRooms=null;
		String oldName=null;

		// class!
		String className=httpReq.getRequestParameter("CLASSES");
		if((className==null)||(className.length()==0))
			return "Please select a class type for this area.";
		if(!className.equalsIgnoreCase(CMClass.className(A)))
		{
			allMyDamnRooms=new Vector();
			for(Enumeration r=A.getMap();r.hasMoreElements();)
				allMyDamnRooms.addElement(r.nextElement());
			Area oldA=A;
			A=CMClass.getAreaType(className);
			if(A==null)
				return "The class you chose does not exist.  Choose another.";
			CMMap.delArea(oldA);
			CMMap.addArea(A);
			A.setName(oldA.Name());
			redoAllMyDamnRooms=true;
		}

		// name
		String name=httpReq.getRequestParameter("NAME");
		if((name==null)||(name.length()==0))
			return "Please enter a name for this area.";
		name=name.trim();
		if(!name.equals(A.Name().trim()))
		{
			if(CMMap.getArea(name)!=null)
				return "The name you chose is already in use.  Please enter another.";
			allMyDamnRooms=new Vector();
			for(Enumeration r=A.getMap();r.hasMoreElements();)
				allMyDamnRooms.addElement(r.nextElement());
			CMMap.delArea(A);
			oldName=A.Name();
			ExternalPlay.DBDeleteArea(A);
			A=ExternalPlay.DBCreateArea(name,CMClass.className(A));
			A.setName(name);
			redoAllMyDamnRooms=true;
			httpReq.addRequestParameters("AREA",A.Name());
		}

		// climate
		if(httpReq.isRequestParameter("CLIMATE"))
		{
			int climate=Util.s_int(httpReq.getRequestParameter("CLIMATE"));
			for(int i=1;;i++)
				if(httpReq.isRequestParameter("CLIMATE"+(new Integer(i).toString())))
					climate=climate|Util.s_int(httpReq.getRequestParameter("CLIMATE"+(new Integer(i).toString())));
				else
					break;
			A.setClimateType(climate);
		}

		// tech level
		if(httpReq.isRequestParameter("TECHLEVEL"))
			A.setClimateType(Util.s_int(httpReq.getRequestParameter("TECHLEVEL")));

		// modify subop list
		String subOps=httpReq.getRequestParameter("SUBOPS");
		Vector V=A.getSubOpVectorList();
		for(int v=0;v<V.size();v++)
			A.delSubOp((String)V.elementAt(v));
		if((subOps!=null)&&(subOps.length()>0))
		{
			A.addSubOp(subOps);
			for(int i=1;;i++)
				if(httpReq.isRequestParameter("SUBOPS"+(new Integer(i).toString())))
					A.addSubOp(httpReq.getRequestParameter("SUBOPS"+(new Integer(i).toString())));
				else
					break;
		}

		// description
		String desc=httpReq.getRequestParameter("DESCRIPTION");
		if(desc==null)desc="";
		A.setDescription(Util.safetyFilter(desc));

        // modify Child Area list
        String parents=httpReq.getRequestParameter("PARENT");
        for(int v=0;v<A.getNumParents();v++)
            A.removeParent(v);
        if((parents!=null)&&(parents.length()>0))
        {
            Area parent=CMMap.getArea(parents);
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
            for(int i=1;;i++)
                if(httpReq.isRequestParameter("PARENT"+(new Integer(i).toString())))
				{
                    parent=CMMap.getArea(httpReq.getRequestParameter("PARENT"+(new Integer(i).toString())));
                    if(parent==null)
						Log.errOut("Grinder", "Error - Area '"+httpReq.getRequestParameter("PARENT"+(new Integer(i).toString()))+"' not found by CMMap");
                    else
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
        String children=httpReq.getRequestParameter("CHILDREN");
        for(int v=0;v<A.getNumChildren();v++)
            A.removeChild(v);
        if((children!=null)&&(children.length()>0))
        {
			Area child=CMMap.getArea(children);
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
			for(int i=1;;i++)
			    if(httpReq.isRequestParameter("CHILDREN"+(new Integer(i).toString())))
				{
			        child=CMMap.getArea(httpReq.getRequestParameter("CHILDREN"+(new Integer(i).toString())));
			        if(child==null)
						Log.errOut("Grinder", "Error - Area '"+httpReq.getRequestParameter("CHILDREN"+(new Integer(i).toString()))+"' not found by CMMap");
			        else
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
			    }
				else
					break;
        }

		// archive file
		String file=httpReq.getRequestParameter("ARCHP");
		if(file==null)file="";
		A.setArchivePath(file);

		String err=GrinderAreas.doAffectsNBehavs(A,httpReq,parms);
		if(err.length()>0) return err;

		if((redoAllMyDamnRooms)&&(allMyDamnRooms!=null))
			CMMap.renameRooms(A,oldName,allMyDamnRooms);

		for(int i=0;i<areasNeedingUpdates.size();i++) // will always include A
		{
		    Area A2=(Area)areasNeedingUpdates.elementAt(i);
			ExternalPlay.DBUpdateArea(A2.Name(),A2);
		}
		return "";
	}
}
