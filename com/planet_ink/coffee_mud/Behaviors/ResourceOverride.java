package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class ResourceOverride extends StdBehavior
{
	public String ID(){return "ResourceOverride";}
	protected int canImproveCode(){return Behavior.CAN_ROOMS;}
	public Behavior newInstance()
	{
		return new ResourceOverride();
	}

	int lastResourceSet=-1;
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(tickID!=MudHost.TICK_ROOM_BEHAVIOR) return true;
		if(!(ticking instanceof Room)) return true;
		Room R=(Room)ticking;
		if((lastResourceSet<0)
		||(R.myResource()!=lastResourceSet)
		||(R.myResource()<0))
		{
			Vector V=Util.parse(getParms());
			if(V.size()==0) return true;
			// first try for a real one
			int code=-1;
			String which=((String)V.elementAt(Dice.roll(1,V.size(),-1))).toUpperCase().trim();
			if((Util.s_int(which)>0)||(which.equalsIgnoreCase("0")))
			   code=Util.s_int(which);

			if(code<0)
			for(int i=0;i<EnvResource.RESOURCE_DESCS.length;i++)
			{
				if(EnvResource.RESOURCE_DESCS[i].equalsIgnoreCase(which))
					code=EnvResource.RESOURCE_DATA[i][0];
			}
			if(code<0)
			for(int i=0;i<EnvResource.MATERIAL_DESCS.length;i++)
			{
				if(EnvResource.MATERIAL_DESCS[i].equalsIgnoreCase(which))
					for(int i2=0;i2<EnvResource.RESOURCE_DESCS.length;i2++)
					{
						if(Util.isSet((EnvResource.RESOURCE_DATA[i][0]&EnvResource.MATERIAL_MASK),i))
						{ code=EnvResource.RESOURCE_DATA[i][0]; break;}
					}
			}
			if(code<0)
			for(int i=0;i<EnvResource.RESOURCE_DESCS.length;i++)
			{
				if(EnvResource.RESOURCE_DESCS[i].startsWith(which))
					code=EnvResource.RESOURCE_DATA[i][0];
			}
			if(code<0)
			for(int i=0;i<EnvResource.MATERIAL_DESCS.length;i++)
			{
				if(EnvResource.MATERIAL_DESCS[i].startsWith(which))
					for(int i2=0;i2<EnvResource.RESOURCE_DESCS.length;i2++)
					{
						if(Util.isSet((EnvResource.RESOURCE_DATA[i][0]&EnvResource.MATERIAL_MASK),i))
						{ code=EnvResource.RESOURCE_DATA[i][0]; break;}
					}
			}
			lastResourceSet=code;
			R.setResource(code);
		}
		return true;
	}
}
