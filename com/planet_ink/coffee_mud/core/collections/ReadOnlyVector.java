package com.planet_ink.coffee_mud.core.collections;
import java.util.*;
/*
Copyright 2000-2012 Bo Zimmerman

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

public final class ReadOnlyVector<T> extends Vector<T>
{
    private static final long serialVersionUID = -9175373358592311411L;

      public ReadOnlyVector(List<T> E)
      {
          if(E!=null)
              super.addAll(E);
      }
      
      public ReadOnlyVector(T... E)
      {
          if(E!=null)
              for(T o : E)
                  super.add(o);
      }
      
      public ReadOnlyVector(Enumeration<T> E)
      {
          if(E!=null)
              for(;E.hasMoreElements();)
                  super.add(E.nextElement());
      }
      
      public ReadOnlyVector(Iterator<T> E)
      {
          if(E!=null)
              for(;E.hasNext();)
                  super.add(E.next());
      }
      
      public ReadOnlyVector(Set<T> E)
      {
            for(T o : E)
                super.add(o);
      }
      
    @Override
    public boolean add(T t) { return false; }
    @Override
    public void addElement(T t){}
    @Override
    public void removeElementAt(int index){}
    @Override
    public boolean removeElement(Object obj){ return false;}
    @Override
    public boolean remove(Object o) { return false; }
    @Override
    public void add(int index, T element) {}
    @Override
    public T remove(int index) { return null; }
    @Override
    public void clear() {}
    @Override
    public boolean removeAll(Collection<?> c) { return false;}
    @Override
    public void insertElementAt(T obj, int index) {}
    @Override
    public boolean addAll(Collection<? extends T> c) { return false;}
    @Override
    public boolean addAll(int index, Collection<? extends T> c) { return false; }
}
