package com.planet_ink.coffee_mud.utils;
import java.util.*;

public class DVector
{
	private int dimensions=1;
	private Vector[] stuff=null;
	public DVector(int dim)
	{
		if(dim<1) throw new java.lang.IndexOutOfBoundsException();
		dimensions=dim;
		stuff=new Vector[dimensions];
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
			stuff[4].addElement(O3);
		}
	}
	public Vector set(int dim)
	{
		if(dimensions<dim) throw new java.lang.IndexOutOfBoundsException();
		return stuff[dim-1];
	}
	public boolean contains(Object O){return stuff[0].contains(O);}
	public int size(){return stuff[0].size();}
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
				if(O.equals(stuff[0]))
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
}
