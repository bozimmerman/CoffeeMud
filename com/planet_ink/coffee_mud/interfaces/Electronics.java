package com.planet_ink.coffee_mud.interfaces;

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
public interface Electronics
{
	public int fuelType();
	public void setFuelType(int resource);
	
	public long powerCapacity();
	public void setPowerCapacity(long capacity);
	
	public long powerRemaining();
	public void setPowerRemaining(long remaining);
	
	public boolean activated();
	public void activate(boolean truefalse);
}
