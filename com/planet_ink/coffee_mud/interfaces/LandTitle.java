package com.planet_ink.coffee_mud.interfaces;
import java.util.Vector;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public interface LandTitle extends Environmental
{
	public int landPrice();
	public void setLandPrice(int price);
	public String landOwner();
	public void setLandOwner(String owner);
	public String landPropertyID();
	public void setLandPropertyID(String landID);
	public void updateLot();
	public void updateTitle();
	public Vector getPropertyRooms();
	public boolean rentalProperty();
	public void setRentalProperty(boolean truefalse);
	public void setBackTaxes(int amount);
	public int backTaxes();
}
