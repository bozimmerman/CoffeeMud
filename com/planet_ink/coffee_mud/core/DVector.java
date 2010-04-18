package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class DVector implements Cloneable, java.io.Serializable
{
	public static final long serialVersionUID=0;
	public static final Enumeration emptyEnumeration=new Vector().elements();
    public static final Iterator emptyIterator=new Vector().iterator();
	protected int dimensions=1;
	private Vector stuff=new Vector(1);
	private final static int MAX_SIZE=9;
	public DVector(int dim)
	{
		if(dim<1) throw new java.lang.IndexOutOfBoundsException();
		if(dim>MAX_SIZE) throw new java.lang.IndexOutOfBoundsException();
		dimensions=dim;
		stuff=new Vector(1);
	}
    public DVector(int dim, int startingSize)
    {
        if(dim<1) throw new java.lang.IndexOutOfBoundsException();
		if(dim>MAX_SIZE) throw new java.lang.IndexOutOfBoundsException();
        dimensions=dim;
        stuff=new Vector(startingSize);
    }
	
	public void clear()
	{
        synchronized(stuff)
        {
            stuff.clear();
		}
	}

	public void trimToSize()
	{
		synchronized(stuff)
		{
			stuff.trimToSize();
		}
	}
	
	public int indexOf(Object O)
	{
        synchronized(stuff)
        {
            int x=0;
            if(O==null)
            {
                for(Enumeration e=stuff.elements();e.hasMoreElements();x++)
                    if((((Object[])e.nextElement())[0])==null)
                        return x;
            }
            else
            for(Enumeration e=stuff.elements();e.hasMoreElements();x++)
                if(O.equals(((Object[])e.nextElement())[0]))
                    return x;
        }
		return -1;
	}
	public Object[] elementsAt(int x)
	{
		synchronized(stuff)
		{
			if((x<0)||(x>=stuff.size())) throw new java.lang.IndexOutOfBoundsException();
            return (Object[])stuff.elementAt(x);
		}
	}
	
	public Object[] removeElementsAt(int x)
	{
		synchronized(stuff)
		{
			if((x<0)||(x>=stuff.size())) throw new java.lang.IndexOutOfBoundsException();
            Object[] O=(Object[])stuff.elementAt(x);
            stuff.removeElementAt(x);
			return O;
		}
	}
	
	public DVector copyOf()
	{
	    DVector V=new DVector(dimensions);
		if(stuff!=null)
		{
			synchronized(stuff)
			{
			    for(Enumeration s=stuff.elements();s.hasMoreElements();)
			        V.stuff.addElement(((Object[])s.nextElement()).clone());
			}
		}
		return V;
	}
	
	public void sortBy(int dim)
	{
        if((dim<1)||(dim>dimensions)) throw new java.lang.IndexOutOfBoundsException();
        dim--;
		if(stuff!=null)
		{
			TreeSet sorted=new TreeSet();
			synchronized(stuff)
			{
				Object O=null;
			    for(Enumeration s=stuff.elements();s.hasMoreElements();)
			    {
			    	O=((Object[])s.nextElement())[dim];
			    	if(!sorted.contains(O))
				        sorted.add(O);
			    }
			    Vector newStuff = new Vector(stuff.size());
			    for(Iterator i=sorted.iterator();i.hasNext();)
			    {
			    	O=i.next();
				    for(Enumeration s=stuff.elements();s.hasMoreElements();)
				    {
				    	Object[] Os=(Object[])s.nextElement();
				    	if(O==Os[dim]) newStuff.addElement(Os);
				    }
			    }
			    stuff=newStuff;
			}
		}
	}

	public static DVector toDVector(Hashtable h)
	{
		DVector DV=new DVector(2);
		for(Enumeration e=h.keys();e.hasMoreElements();)
		{
			Object key=e.nextElement();
			DV.addElement(key,h.get(key));
		}
		return DV;
	}
	
    public void addSharedElements(Object[] O)
    {
        if(dimensions!=O.length) throw new java.lang.IndexOutOfBoundsException();
        synchronized(stuff)
        {
            stuff.addElement(O);
        }
    }
    
    public void addElements(Object[] O)
    {
        if(dimensions!=O.length) throw new java.lang.IndexOutOfBoundsException();
        synchronized(stuff)
        {
            stuff.addElement((Object[])O.clone());
        }
    }
    
	public void addElement(Object O)
	{
		if(dimensions!=1) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
			stuff.addElement(new Object[]{O});
		}
	}
	public void addElement(Object O, Object O1)
	{
		if(dimensions!=2) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
            stuff.addElement(new Object[]{O,O1});
		}
	}
	public void addElement(Object O, Object O1, Object O2)
	{
		if(dimensions!=3) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
            stuff.addElement(new Object[]{O,O1,O2});
		}
	}
	public void addElement(Object O, Object O1, Object O2, Object O3)
	{
		if(dimensions!=4) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
            stuff.addElement(new Object[]{O,O1,O2,O3});
		}
	}
	public void addElement(Object O, Object O1, Object O2, Object O3, Object O4)
	{
		if(dimensions!=5) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
            stuff.addElement(new Object[]{O,O1,O2,O3,O4});
		}
	}
	public void addElement(Object O, Object O1, Object O2, Object O3, Object O4, Object O5)
	{
		if(dimensions!=6) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
            stuff.addElement(new Object[]{O,O1,O2,O3,O4,O5});
		}
	}
	public void addElement(Object O, Object O1, Object O2, Object O3, Object O4, Object O5, Object O6)
	{
		if(dimensions!=7) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
            stuff.addElement(new Object[]{O,O1,O2,O3,O4,O5,O6});
		}
	}
	public void addElement(Object O, Object O1, Object O2, Object O3, Object O4, Object O5, Object O6, Object O7)
	{
		if(dimensions!=8) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
            stuff.addElement(new Object[]{O,O1,O2,O3,O4,O5,O6,O7});
		}
	}
	public void addElement(Object O, Object O1, Object O2, Object O3, Object O4, Object O5, Object O6, Object O7, Object O8)
	{
		if(dimensions!=9) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
            stuff.addElement(new Object[]{O,O1,O2,O3,O4,O5,O6,O7,O8});
		}
	}
	public boolean contains(Object O){
        return indexOf(O)>=0;
    }
	public boolean containsIgnoreCase(String S)
	{
        synchronized(stuff)
        {
            if(S==null) return indexOf(null)>=0;
    	    for(Enumeration e=stuff.elements();e.hasMoreElements();)
    	        if(S.equalsIgnoreCase(((Object[])e.nextElement())[0].toString()))
    	            return true;
        }
	    return false;
	}
	public int size()
	{
		return stuff.size();
	}
	public void removeElementAt(int i)
	{
		synchronized(stuff)
		{
            if(i>=0)
                stuff.removeElementAt(i);
		}
	}
	public void removeElement(Object O)
	{
		synchronized(stuff)
		{
            removeElementAt(indexOf(O));
		}
	}
    public Vector getDimensionVector(int dim)
    {
        Vector V=new Vector(stuff.size());
        synchronized(stuff)
        {
            if(dimensions<dim) throw new java.lang.IndexOutOfBoundsException();
            for(Enumeration e=stuff.elements();e.hasMoreElements();)
                V.addElement(((Object[])e.nextElement())[dim-1]);
        }
        return V;
    }
    public Vector getRowVector(int row)
    {
		Vector V=new Vector(dimensions);
		synchronized(stuff)
		{
            Object[] O=elementsAt(row);
			for(int v=0;v<O.length;v++)
				V.addElement(O[v]);
		}
		return V;
    }
	public Object elementAt(int i, int dim)
	{
		synchronized(stuff)
		{
			if(dimensions<dim) throw new java.lang.IndexOutOfBoundsException();
			return ((Object[])stuff.elementAt(i))[dim-1];
		}
	}
	
	public void setElementAt(int index, int dim, Object O)
	{
		if(dimensions<dim) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
            ((Object[])stuff.elementAt(index))[dim-1]=O;
		}
	}
    public void insertElementAt(int here, Object O)
    {
        if(dimensions!=1) throw new java.lang.IndexOutOfBoundsException();
        synchronized(stuff)
        {
            stuff.insertElementAt(new Object[]{O},here);
        }
    }
	public void insertElementAt(int here, Object O, Object O1)
	{
		if(dimensions!=2) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
            stuff.insertElementAt(new Object[]{O,O1},here);
		}
	}
	public void insertElementAt(int here, Object O, Object O1, Object O2)
	{
		if(dimensions!=3) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
            stuff.insertElementAt(new Object[]{O,O1,O2},here);
		}
	}
	public void insertElementAt(int here, Object O, Object O1, Object O2, Object O3)
	{
		if(dimensions!=4) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
            stuff.insertElementAt(new Object[]{O,O1,O2,O3},here);
		}
	}
	public void insertElementAt(int here, Object O, Object O1, Object O2, Object O3, Object O4)
	{
		if(dimensions!=5) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
            stuff.insertElementAt(new Object[]{O,O1,O2,O3,O4},here);
		}
	}
	public void insertElementAt(int here, Object O, Object O1, Object O2, Object O3, Object O4, Object O5)
	{
		if(dimensions!=6) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
            stuff.insertElementAt(new Object[]{O,O1,O2,O3,O4,O5},here);
		}
	}
	public void insertElementAt(int here, Object O, Object O1, Object O2, Object O3, Object O4, Object O5, Object O6)
	{
		if(dimensions!=7) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
            stuff.insertElementAt(new Object[]{O,O1,O2,O3,O4,O5,O6},here);
		}
	}
	public void insertElementAt(int here, Object O, Object O1, Object O2, Object O3, Object O4, Object O5, Object O6, Object O7)
	{
		if(dimensions!=8) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
            stuff.insertElementAt(new Object[]{O,O1,O2,O3,O4,O5,O6,O7},here);
		}
	}
	
	public void insertElementAt(int here, Object O, Object O1, Object O2, Object O3, Object O4, Object O5, Object O6, Object O7, Object O8)
	{
		if(dimensions!=9) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
            stuff.insertElementAt(new Object[]{O,O1,O2,O3,O4,O5,O6,O7,O8},here);
		}
	}
	
	public static Vector softCopy(Vector V)
    {
        if(V==null) return null;
        Vector V2=new Vector(V.size());
        for(Enumeration e=V.elements();e.hasMoreElements();)
            V2.addElement(e.nextElement());
        return V2;
    }
    
    public static DVector softCopy(DVector DV)
    {
        if(DV==null) return null;
        DVector DV2=new DVector(DV.dimensions);
        DV2.stuff=softCopy(DV.stuff);
        return DV2;
    }
    
	public static Hashtable softCopy(Hashtable H)
    {
        if(H==null) return null;
        Hashtable H2=new Hashtable(H.size());
        Object key=null;
        for(Enumeration e=H.keys();e.hasMoreElements();)
        {
            key=e.nextElement();
            H2.put(key, H.get(key));
        }
        return H2;
    }
    
	public static HashSet softCopy(HashSet H)
    {
        if(H==null) return null;
        HashSet H2=new HashSet(H.size());
        for(Iterator i=H.iterator();i.hasNext();)
            H2.add(i.next());
        return H2;
    }
	
	public static Enumeration empty_enum() {
        return new Enumeration() {
	        public boolean hasMoreElements() { return false;}
	        public Object nextElement() { return null;}
        };
	};
	
	public static Iterator empty_iter() {
        return new Iterator() {
	        public boolean hasNext() { return false;}
	        public Object next() { return null;}
            public void remove() {}
        };
	};
	
	public static Enumeration s_enum(List V) {
        //return ((Vector)V.clone()).elements(); /*
        return new Enumeration() {
            Iterator i=null;
            public boolean hasMoreElements() { return i.hasNext();}
            public Object nextElement() { return i.next();}
            public Enumeration setV(List V) {
                if((V==null)||(V.size()==0))
                	return empty_enum();
                i=s_iter(V);
                return this;
            }
        }.setV(V);
        //*/
    }
    
	public static Iterator s_iter(List V) 
	{
        return new Iterator() 
        {
            boolean more=false;
            Object prevO=null;
            Object O=null;
            List V=null;
            int c=0;
            
            public boolean hasNext() { return more; }
            
            public int confirmDex(Object O)
            {
                try {
                    for(int i=0;i<3;i++)
                        if(V.get(c-i)==O)
                            return c+1-i;
                } catch(Exception e){}
                return c;
            }
            
            public Object next() 
            {
                if(!more) 
                    throw new java.util.NoSuchElementException("");
                prevO=O;
                try {
                    c=confirmDex(O);
                    O=V.get(c);
                    more=true;
                } catch(Exception e) {
                    more=false;
                    O=null;
                }
                return prevO;
            }
            
            public Iterator setV(List V) {
                if((V==null)||(V.size()==0)) 
                	return empty_iter();
                this.V=V;
                more=false;
                try {
                	if(V.size()>0)
                	{
	                    O=V.get(0);
	                    more=true;
                	}
                } catch(Throwable t) {}
                return this;
            }
            
            public void remove() {
                try { V.remove(prevO); c--; }
                catch(Exception e){}
            }
        }.setV(V);
        //*/
    }
    
	public static Enumeration s_enum(Hashtable H, boolean keys) 
	{
		/* this is slower -- more than twice as slow, believe it or not! */
        //return keys?((Hashtable)H.clone()).keys():((Hashtable)H.clone()).elements(); 
        if((H==null)||(H.size()==0))
        	return empty_enum();
        Vector V=new Vector(H.size());
    	if(keys)
    		V.addAll(H.keySet());
    	else
        for(Enumeration e=H.elements();e.hasMoreElements();)
            V.addElement(e.nextElement());
        return s_enum(V);
        //*/
    }
}
