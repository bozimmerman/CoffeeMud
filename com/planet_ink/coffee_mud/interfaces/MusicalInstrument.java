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
public interface MusicalInstrument extends Item
{
	public static final int TYPE_CLARINETS=0;
	public static final int TYPE_CYMBALS=1;
	public static final int TYPE_DRUMS=2;
	public static final int TYPE_FLUTES=3;
	public static final int TYPE_GUITARS=4;
	public static final int TYPE_HARMONICAS=5;
	public static final int TYPE_HARPS=6;
	public static final int TYPE_HORNS=7;
	public static final int TYPE_OBOES=8;
	public static final int TYPE_ORGANS=9;
	public static final int TYPE_PIANOS=10;
	public static final int TYPE_TROMBONES=11;
	public static final int TYPE_TRUMPETS=12;
	public static final int TYPE_TUBAS=13;
	public static final int TYPE_VIOLINS=14;
	public static final int TYPE_WOODS=15;
	public static final int TYPE_XYLOPHONES=16;
	public static final String[] TYPE_DESC={"CLARINETS",
											"CYMBALS",
											"DRUMS",
											"FLUTES",
											"GUITARS",
											"HARMONICAS",
											"HARPS",
											"HORNS",
											"OBOES",
											"ORGANS",
											"PIANOS",
											"TROMBONES",
											"TRUMPETS",
											"TUBAS",
											"VIOLINS",
											"WOODS",
											"XYLOPHONES"};
	public int instrumentType();
	public void setInstrumentType(int type);
}
