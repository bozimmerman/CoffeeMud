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
		for(int a=0;a<CMMap.numAreas();a++)
		{
			Area A=(Area)CMMap.getArea(a);
			if((A.amISubOp(mob.name()))||(mob.isASysOp(null)))
				if((pickedA!=null)&&(pickedA==A))
					AreaList.append("<OPTION SELECTED VALUE=\""+A.name()+"\">"+A.name());
				else
					AreaList.append("<OPTION VALUE=\""+A.name()+"\">"+A.name());
		}
		return AreaList.toString();
	}
	public static String modifyArea(ExternalHTTPRequests httpReq, Hashtable parms)
	{
		String last=(String)httpReq.getRequestParameters().get("AREA");
		if((last==null)||(last.length()==0)) return "Old area name not defined!";
		Area A=CMMap.getArea(last);
		if(A==null) return "Old Area not defined!";
		
		boolean redoAllMyDamnRooms=false;
		Vector allMyDamnRooms=null;
		String oldName=null;
		
		// class!
		String className=(String)httpReq.getRequestParameters().get("CLASS");
		if((className==null)||(className.length()==0))
			return "Please select a class type for this area.";
		if(!className.equalsIgnoreCase(CMClass.className(A)))
		{
			allMyDamnRooms=A.getMyMap();
			Area oldA=A;
			A=CMClass.getAreaType(className);
			if(A==null)
				return "The class you chose does not exist.  Choose another.";
			CMMap.delArea(oldA);
			CMMap.addArea(A);
			A.setName(oldA.name());
			redoAllMyDamnRooms=true;
		}
		
		// name
		String name=(String)httpReq.getRequestParameters().get("NAME");
		if((name==null)||(name.length()==0))
			return "Please enter a name for this area.";
		if(!name.equalsIgnoreCase(A.name()))
		{
			if(CMMap.getArea(name)!=null)
				return "The name you chose is already in use.  Please enter another.";
			allMyDamnRooms=A.getMyMap();
			CMMap.delArea(A);
			oldName=A.name();
			ExternalPlay.DBDeleteArea(A);
			A=ExternalPlay.DBCreateArea(name,CMClass.className(A));
			A.setName(name);
			redoAllMyDamnRooms=true;
			httpReq.getRequestParameters().put("AREA",A.name());
			httpReq.resetRequestEncodedParameters();
		}
		
		// climate
		if(httpReq.getRequestParameters().containsKey("CLIMATE"))
		{
			int climate=Util.s_int((String)httpReq.getRequestParameters().get("CLIMATE"));
			for(int i=1;;i++)
				if(httpReq.getRequestParameters().containsKey("CLIMATE"+(new Integer(i).toString())))
					climate=climate|Util.s_int((String)httpReq.getRequestParameters().get("CLIMATE"+(new Integer(i).toString())));
				else
					break;
			A.setClimateType(climate);
		}

		// modify subop list
		String subOps=(String)httpReq.getRequestParameters().get("SUBOPS");
		Vector V=A.getSubOpVectorList();
		for(int v=0;v<V.size();v++)
			A.delSubOp((String)V.elementAt(v));
		if((subOps!=null)&&(subOps.length()>0))
		{
			A.addSubOp(subOps);
			for(int i=1;;i++)
				if(httpReq.getRequestParameters().containsKey("SUBOPS"+(new Integer(i).toString())))
					A.addSubOp((String)httpReq.getRequestParameters().get("SUBOPS"+(new Integer(i).toString())));
				else
					break;
		}
		
		// description
		String desc=(String)httpReq.getRequestParameters().get("DESCRIPTION");
		if(desc==null)desc="";
		A.setDescription(desc);
		
		while(A.numBehaviors()>0)
			A.delBehavior(A.fetchBehavior(0));
		if(httpReq.getRequestParameters().containsKey("BEHAV1"))
		{
			int num=1;
			String behav=(String)httpReq.getRequestParameters().get("BEHAV"+num);
			String theparm=(String)httpReq.getRequestParameters().get("BDATA"+num);
			while((behav!=null)&&(theparm!=null))
			{
				if(behav.length()>0)
				{
					Behavior B=CMClass.getBehavior(behav);
					if(theparm==null) theparm="";
					if(B==null) return "Unknown behavior '"+behav+"'.";
					B.setParms(theparm);
					A.addBehavior(B);
					B.startBehavior(A);
				}
				num++;
				behav=(String)httpReq.getRequestParameters().get("BEHAV"+num);
				theparm=(String)httpReq.getRequestParameters().get("BDATA"+num);
			}
		}
		while(A.numAffects()>0)
			A.delAffect(A.fetchAffect(0));
		if(httpReq.getRequestParameters().containsKey("AFFECT1"))
		{
			int num=1;
			String aff=(String)httpReq.getRequestParameters().get("AFFECT"+num);
			String theparm=(String)httpReq.getRequestParameters().get("ADATA"+num);
			while((aff!=null)&&(theparm!=null))
			{
				if(aff.length()>0)
				{
					Ability B=CMClass.getAbility(aff);
					if(theparm==null) theparm="";
					if(B==null) return "Unknown Affect '"+aff+"'.";
					B.setMiscText(theparm);
					A.addNonUninvokableAffect(B);
					
				}
				num++;
				aff=(String)httpReq.getRequestParameters().get("AFFECT"+num);
				theparm=(String)httpReq.getRequestParameters().get("ADATA"+num);
			}
		}
		if((redoAllMyDamnRooms)&&(allMyDamnRooms!=null))
		{
			for(int r=0;r<allMyDamnRooms.size();r++)
			{
				Room R=(Room)allMyDamnRooms.elementAt(r);
				R.setArea(A);
				if(oldName!=null)
					ExternalPlay.DBUpdateRoom(R);
			}
			A.clearMap();
		}
		ExternalPlay.DBUpdateArea(A);
		return "";
	}
}
