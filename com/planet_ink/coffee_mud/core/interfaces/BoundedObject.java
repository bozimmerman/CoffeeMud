package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
/*
   Copyright 2013-2018 Bo Zimmerman

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
/**
 * The interface represents a 3d cubed thing.
 * @author Bo Zimmerman
 *
 */
public interface BoundedObject
{
	public BoundedCube getBounds();

	public static class BoundedCube implements Comparable<BoundedCube>
	{
		public long	lx, ty, iz = 0;
		public long	rx, by, oz = 0;

		public BoundedCube()
		{
			super();
		}

		public BoundedCube(long lx, long rx, long ty, long by, long iz, long oz)
		{
			super();
			this.lx = lx; 
			this.rx = rx;
			this.ty = ty;
			this.by = by;
			this.iz = iz;
			this.oz = oz;
		}

		public BoundedCube(long[] coords, long radius)
		{
			super();
			this.lx = coords[0] - radius; 
			this.rx = coords[0] + radius;
			this.ty = coords[1] - radius; 
			this.by = coords[1] + radius;
			this.iz = coords[2] - radius; 
			this.oz = coords[2] + radius;
		}

		public BoundedCube(BoundedCube l)
		{
			super();
			set(l);
		}

		public void set(BoundedCube l)
		{
			this.lx = l.lx; 
			this.rx = l.rx;
			this.ty = l.ty; 
			this.by = l.by;
			this.iz = l.iz; 
			this.oz = l.oz;
		}
		
		public void union(BoundedCube l)
		{
			if(l.lx < lx)
				lx=l.lx;
			if(l.rx > rx)
				rx=l.rx;
			if(l.ty < ty)
				ty=l.ty;
			if(l.by > by)
				by=l.by;
			if(l.iz < iz)
				iz=l.iz;
			if(l.oz > oz)
				oz=l.oz;
		}

		public long radius()
		{
			return Math.round(Math.sqrt(((rx - lx) * (rx - lx))
									   +((by - ty) * (by - ty))
									   +((oz - iz) * (oz - iz))));
		}

		public BoundedCube expand(double[] direction, long distance)
		{
			final BoundedCube cube=new BoundedCube(this);
			final double x1=Math.cos(direction[0])*Math.sin(direction[1]);
			final double y1=Math.sin(direction[0])*Math.sin(direction[1]);
			final double z1=Math.cos(direction[1]);
			final long[] oldCenter=new long[]{((lx+rx)/2),((ty+rx)/2),((iz+oz)/2)};
			final long[] newCenter=new long[]{
					oldCenter[0]+Math.round(CMath.mul(distance,x1)),
					oldCenter[1]+Math.round(CMath.mul(distance,y1)),
					oldCenter[2]+Math.round(CMath.mul(distance,z1))};
			if(newCenter[0]>oldCenter[0])
				cube.rx+=newCenter[0]-oldCenter[0];
			else
				cube.lx+=newCenter[0]-oldCenter[0];
			if(newCenter[1]>oldCenter[1])
				cube.ty+=newCenter[1]-oldCenter[1];
			else
				cube.by+=newCenter[1]-oldCenter[1];
			if(newCenter[2]>oldCenter[2])
				cube.iz+=newCenter[2]-oldCenter[2];
			else
				cube.oz+=newCenter[2]-oldCenter[2];
			return cube;
		}

		public boolean intersects(BoundedCube two)
		{
			if(two==null)
				return false;
			return ( 
				((lx <= two.lx && two.lx <= rx) || (two.lx <= lx && lx <= two.rx))
			&&	((ty <= two.ty && two.ty <= by) || (two.ty <= ty && ty <= two.by))
			&&	((iz <= two.iz && two.iz <= oz) || (two.iz <= iz && iz <= two.oz)) 
			);
		}

		public boolean contains(long x, long y, long z)
		{
			return ((x >= lx)
				  &&(x <= rx)
				  &&(y >= ty)
				  &&(y <= by)
				  &&(z >= iz)
				  &&(z <= oz));
		}

		public long width()
		{
			return rx - lx;
		}

		public long height()
		{
			return by - ty;
		}

		public long depth()
		{
			return oz - iz;
		}

		@Override
		public int compareTo(BoundedCube o)
		{
			if(lx<o.lx)
				return -1;
			if(lx>o.lx)
				return 1;
			if(rx<o.rx)
				return -1;
			if(rx>o.rx)
				return 1;
			if(ty<o.ty)
				return -1;
			if(ty>o.ty)
				return 1;
			if(by<o.by)
				return -1;
			if(by>o.by)
				return 1;
			if(iz<o.iz)
				return -1;
			if(iz>o.iz)
				return 1;
			if(oz<o.oz)
				return -1;
			if(oz>o.oz)
				return 1;
			return 0;
		}
	}
}
