package com.planet_ink.coffee_mud.Items.interfaces;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
public interface SpaceShip extends SpaceObject
{
	public void dockHere(Room R);

	public void unDock(boolean toSpace);
	
	public Room getIsDocked();
	
	public Area getShipArea();
	
	public void setShipArea(String xml);
	
	public void renameSpaceShip(String newName);
	
	public SpaceObject getShipSpaceObject();
	
	/**
	 * The Outer Mold Line coefficient -- how streamlined are you?
	 * @return the coefficient, from 0.05-0.3
	 */
	public double getOMLCoeff();
	/**
	 * Set the Outer Mold Line coefficient -- how streamlined are you?
	 * @param coeff the Outer Mold Line coefficient
	 */
	public void setOMLCoeff(double coeff);
	
	/**
	 * The direction of facing of this object in radians. 
	 * @return 2 dimensional array for the direction of facing
	 */
	public double[] facing();
	/**
	 * Sets the direction of facing of this object in radians.
	 * @param dir 2 dimensional array for the direction of facing
	 */
	public void setFacing(double[] dir);
}
