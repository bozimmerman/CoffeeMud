package com.planet_ink.coffee_mud.utils;
import java.util.*;

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
public class DVector implements Cloneable, java.io.Serializable
{
	public static final long serialVersionUID=0;
	private int dimensions=1;
	private Vector[] stuff=null;
	public DVector(int dim)
	{
		if(dim<1) throw new java.lang.IndexOutOfBoundsException();
		if(dim>8) throw new java.lang.IndexOutOfBoundsException();
		dimensions=dim;
		stuff=new Vector[dimensions];
		for(int i=0;i<dimensions;i++)
			stuff[i]=new Vector();
	}
	
	public void clear()
	{
		if(stuff==null) return;
		
		synchronized(stuff)
		{
			for(int i=0;i<stuff.length;i++)
				stuff[i].clear();
		}
	}
	
	public int indexOf(Object O)
	{
		if(stuff==null) return -1;
		return stuff[0].indexOf(O);
	}
	
	public DVector copyOf()
	{
		try{
			return (DVector)this.clone();
		}
		catch(CloneNotSupportedException e){}
		{
			DVector V=new DVector(dimensions);
			V.stuff=stuff;
			return V;
		}
	}
	
	public void addElement(Object O)
	{
		if(dimensions!=1) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
			stuff[0].addElement(O);
		}
	}
	public void addElement(Object O, Object O1)
	{
		if(dimensions!=2) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
			stuff[0].addElement(O);
			stuff[1].addElement(O1);
		}
	}
	public void addElement(Object O, Object O1, Object O2)
	{
		if(dimensions!=3) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
			stuff[0].addElement(O);
			stuff[1].addElement(O1);
			stuff[2].addElement(O2);
		}
	}
	public void addElement(Object O, Object O1, Object O2, Object O3)
	{
		if(dimensions!=4) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
			stuff[0].addElement(O);
			stuff[1].addElement(O1);
			stuff[2].addElement(O2);
			stuff[3].addElement(O3);
		}
	}
	public void addElement(Object O, Object O1, Object O2, Object O3, Object O4)
	{
		if(dimensions!=5) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
			stuff[0].addElement(O);
			stuff[1].addElement(O1);
			stuff[2].addElement(O2);
			stuff[3].addElement(O3);
			stuff[4].addElement(O4);
		}
	}
	public void addElement(Object O, Object O1, Object O2, Object O3, Object O4, Object O5)
	{
		if(dimensions!=6) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
			stuff[0].addElement(O);
			stuff[1].addElement(O1);
			stuff[2].addElement(O2);
			stuff[3].addElement(O3);
			stuff[4].addElement(O4);
			stuff[5].addElement(O5);
		}
	}
	public void addElement(Object O, Object O1, Object O2, Object O3, Object O4, Object O5, Object O6)
	{
		if(dimensions!=7) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
			stuff[0].addElement(O);
			stuff[1].addElement(O1);
			stuff[2].addElement(O2);
			stuff[3].addElement(O3);
			stuff[4].addElement(O4);
			stuff[5].addElement(O5);
			stuff[6].addElement(O6);
		}
	}
	public void addElement(Object O, Object O1, Object O2, Object O3, Object O4, Object O5, Object O6, Object O7)
	{
		if(dimensions!=8) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
			stuff[0].addElement(O);
			stuff[1].addElement(O1);
			stuff[2].addElement(O2);
			stuff[3].addElement(O3);
			stuff[4].addElement(O4);
			stuff[5].addElement(O5);
			stuff[6].addElement(O6);
			stuff[7].addElement(O7);
		}
	}
	public int getIndex(Object O)
	{
		if(stuff[0].contains(O))
			return stuff[0].indexOf(O);
		return -1;
	}
	public Vector set(int dim)
	{
		if(dimensions<dim) throw new java.lang.IndexOutOfBoundsException();
		return stuff[dim-1];
	}
	public boolean contains(Object O){return stuff[0].contains(O);}
	public int size(){
		if(stuff==null) return 0;
		return stuff[0].size();
	}
	public void removeElementAt(int i)
	{
		for(int d=0;d<dimensions;d++)
			stuff[d].removeElementAt(i);
	}
	public void removeElement(Object O)
	{
		synchronized(stuff)
		{
			for(int i=stuff[0].size()-1;i>=0;i--)
			{
				if((O==stuff[0].elementAt(i))||(O.equals(stuff[0].elementAt(i))))
				for(int d=0;d<dimensions;d++)
					stuff[d].removeElementAt(i);
			}
		}
	}
	public Object elementAt(int i, int dim)
	{
		synchronized(stuff)
		{
			if(dimensions<dim) throw new java.lang.IndexOutOfBoundsException();
			return stuff[dim-1].elementAt(i);
		}
	}
	
	public void insertElementAt(int here, Object O)
	{
		if(dimensions!=1) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
			stuff[0].insertElementAt(O,here);
		}
	}
	public void setElementAt(int index, int dim, Object O)
	{
		if(dimensions<dim) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
			stuff[dim-1].setElementAt(O,index);
		}
	}
	public void insertElementAt(int here, Object O, Object O1)
	{
		if(dimensions!=2) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
			stuff[0].insertElementAt(O,here);
			stuff[1].insertElementAt(O1,here);
		}
	}
	public void insertElementAt(int here, Object O, Object O1, Object O2)
	{
		if(dimensions!=3) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
			stuff[0].insertElementAt(O,here);
			stuff[1].insertElementAt(O1,here);
			stuff[2].insertElementAt(O2,here);
		}
	}
	public void insertElementAt(int here, Object O, Object O1, Object O2, Object O3)
	{
		if(dimensions!=4) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
			stuff[0].insertElementAt(O,here);
			stuff[1].insertElementAt(O1,here);
			stuff[2].insertElementAt(O2,here);
			stuff[3].insertElementAt(O3,here);
		}
	}
	public void insertElementAt(int here, Object O, Object O1, Object O2, Object O3, Object O4)
	{
		if(dimensions!=5) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
			stuff[0].insertElementAt(O,here);
			stuff[1].insertElementAt(O1,here);
			stuff[2].insertElementAt(O2,here);
			stuff[3].insertElementAt(O3,here);
			stuff[4].insertElementAt(O4,here);
		}
	}
	public void insertElementAt(int here, Object O, Object O1, Object O2, Object O3, Object O4, Object O5)
	{
		if(dimensions!=6) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
			stuff[0].insertElementAt(O,here);
			stuff[1].insertElementAt(O1,here);
			stuff[2].insertElementAt(O2,here);
			stuff[3].insertElementAt(O3,here);
			stuff[4].insertElementAt(O4,here);
			stuff[5].insertElementAt(O5,here);
		}
	}
	public void insertElementAt(int here, Object O, Object O1, Object O2, Object O3, Object O4, Object O5, Object O6)
	{
		if(dimensions!=7) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
			stuff[0].insertElementAt(O,here);
			stuff[1].insertElementAt(O1,here);
			stuff[2].insertElementAt(O2,here);
			stuff[3].insertElementAt(O3,here);
			stuff[4].insertElementAt(O4,here);
			stuff[5].insertElementAt(O5,here);
			stuff[6].insertElementAt(O6,here);
		}
	}
	public void insertElementAt(int here, Object O, Object O1, Object O2, Object O3, Object O4, Object O5, Object O6, Object O7)
	{
		if(dimensions!=8) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
			stuff[0].insertElementAt(O,here);
			stuff[1].insertElementAt(O1,here);
			stuff[2].insertElementAt(O2,here);
			stuff[3].insertElementAt(O3,here);
			stuff[4].insertElementAt(O4,here);
			stuff[5].insertElementAt(O5,here);
			stuff[6].insertElementAt(O6,here);
			stuff[7].insertElementAt(O7,here);
		}
	}
}
