package com.planet_ink.coffee_mud.web.macros.grinder;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class GrinderArea
{
	public static String modifyArea(ExternalHTTPRequests httpReq, Hashtable parms)
	{
		String last=(String)httpReq.getRequestParameters().get("AREA");
		if((last==null)||(last.length()==0)) return "Old area name not defined!";
		Area A=CMMap.getArea(last);
		if(A==null) return "Old Area not defined!";
		
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

		// name
		String name=(String)httpReq.getRequestParameters().get("NAME");
		if((name==null)||(name.length()==0))
			return "Please enter a name for this area.";
		if((!name.equalsIgnoreCase(A.name()))&&(CMMap.getArea(name)!=null))
		   return "The name you chose is already in use.  Please enter another.";
		A.setName(name);
		
		// class?!
		String className=(String)httpReq.getRequestParameters().get("CLASS");
		if((className==null)||(className.length()==0))
			return "Please select a class type for this area.";
		if(CMClass.getAreaType(className)==null)
			return "The class you chose does not exist.  Choose another.";
		/*** set new class?! */

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
		return null;
	}
}
