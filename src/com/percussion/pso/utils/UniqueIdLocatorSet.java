/*
 * com.percussion.pso.utils UniqueIdLocatorSet.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.percussion.design.objectstore.PSLocator;

/**
 * A set of PSLocators where the content ids are unique.
 * This class follows all of the rules for the Set interface, but does not allow a PSLocator to be added
 * where a PSLocator with the same content id is already present. 
 * @author DavidBenua
 *
 */
public class UniqueIdLocatorSet extends HashSet<PSLocator> implements Set<PSLocator>, Iterable<PSLocator>
{
   /**
    * 
    */
   public UniqueIdLocatorSet()
   {
      super();
   }
   /**
    * @param c
    */
   public UniqueIdLocatorSet(Collection<? extends PSLocator> c)
   {
      super(c);
   }
   /**
    * @param initialCapacity
    */
   public UniqueIdLocatorSet(int initialCapacity)
   {
      super(initialCapacity);
   }
   /**
    * @param initialCapacity
    * @param loadFactor
    */
   public UniqueIdLocatorSet(int initialCapacity, float loadFactor)
   {
      super(initialCapacity, loadFactor);
   }
   /**
    * @see java.util.HashSet#add(java.lang.Object)
    */
   @Override
   public boolean add(PSLocator loc)
   {
      if(hasLocatorForId(loc.getId()))
      {
         return false; 
      }
      return super.add(loc); 
   }
   
   /**
    * @see java.util.HashSet#contains(java.lang.Object)
    */
   @Override
   public boolean contains(Object o)
   {
      PSLocator loc = (PSLocator)o; 
      return hasLocatorForId(loc.getId()); 
   }
   
   
   /**
    * @see java.util.HashSet#remove(java.lang.Object)
    */
   @Override
   public boolean remove(Object o)
   {
      PSLocator loc = (PSLocator)o;
      PSLocator l2 = getLocatorById(loc.getId()); 
      if(l2 == null)
      {
         return false; 
      }
      return super.remove(l2);
   }
   
   /**
    * Does this set contain a locator with the specified id?
    * @param id the content id. 
    * @return <code>true</code> if the set already contains a locator.
    */
   protected boolean hasLocatorForId(int id)
   {
      if(getLocatorById(id) != null)
      {
         return true;
      }
      return false;   
   }
   
   /**
    * Gets the locator with the specified content id. 
    * @param id
    * @return the locator, or <code>null</code> if there is no locator in the set with this id. 
    */
   protected PSLocator getLocatorById(int id)
   {
      for(PSLocator loc : this)
      {
         if(loc.getId() == id)
            return loc; 
      }
      return null; 
   }
   
}
