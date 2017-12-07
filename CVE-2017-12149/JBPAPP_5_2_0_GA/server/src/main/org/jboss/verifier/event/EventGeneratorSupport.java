/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */


//package org.gjt.lindfors.util; Taken from Juha Lindfors implementation as stated in 
package org.jboss.verifier.event;

/*
 * Class org.jboss.verifier.event.EventGeneratorSupport (refer Class org.gjt.lindfors.util.EventGeneratorSupport at gjt.org)
 * Copyright (C) 1999  Juha Lindfors
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * This package and its source code is available at www.gjt.org
 * $Id: EventGeneratorSupport.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $
 *
 * You can reach the author by sending email to jpl@gjt.org or
 * directly to jplindfo@helsinki.fi.
 */

// standard imports
import java.util.*;
import java.awt.*;
import java.io.*;

// dependencies to non-standard packages and classes



/**
 * Support class for objects implementing the 
 * {@link org.gjt.lindfors.util.EventGenerator EventGenerator} interface.
 * Contains implementations for addListener, removeListener, and hasListeners.
 *
 * <p>
 *
 * Every effort has been made to try to achieve thread-safety in
 * EventGeneratorSupport class. Of course, this doesn't mean
 * nasty race conditions and dead locks don't exist. I'm just
 * not aware of them :)
 *
 * <p>
 *
 * For more detailed documentation, refer to the
 *         <a href="http://www.gjt.org/%7ejpl/org_gjt_lindfors_util/index.html">
 * 
 *                 Util Library Tutorial
 *
 *         </a> and
 *
 *         <a href="http://www.gjt.org/%7ejpl/org_gjt_lindfors_util/specs/index.html">
 *      
 *                 Util Library Specifications
 *
 *         </a>. See Also:
 *          
 *         <a href="http://www.artima.com/designtechniques/index.html">
 *      
 *                 The Event Generator Idiom
 *
 *          </a> by Bill Venners.
 *
 *
 * @see        org.gjt.lindfors.util.EventGenerator
 * 
 * @author     Juha Lindfors
 * @version    $Revision: 85945 $
 * @since      JDK1.1
 */
public abstract class EventGeneratorSupport implements Serializable, Cloneable{

    /*
     * [TODO] Implement different algorithms for listener notification. Use the
     *        one implemented in this class as default, and implement high
     *        performance notifications. Use strategy pattern to allow any
     *        number of algorithms to be added.
     *
     * [TODO] write performance tests to compare the implementations
     *
     * [TODO] Could use Doug Lea's latch implementation here. Prettier than
     *        using a mutex :P
     *
     * [TODO] The use of this class could be used as an example of bridge.
     *        Also, the concrete implementations usually act as adapters (not
     *        allowing just any listener to be added, but a specific type).
     *        Can subclassing be used as an adapter pattern?
     */
     
    /**
     * Vector for storing the registered listeners. Vector will use lazy
     * instantiation and will be constructed only upon request. To ensure
     * proper behaviour in a multi-threaded environment, use the private
     * getListInstance method any time you need a reference to this vector.
     */
    private transient Vector list = null;

    /**
     * Clone of listener list. This clone is used as a snapshot of the listener
     * list. The clone is updated every time a listener is either registered
     * or unregistered. This way we can avoid cloning the listener list for
     * the getListeners method call which is usually called by the event
     * firing methods. Since we don't have to clone the listener list for every
     * event notification, we gain a significantly better performance.
     */
    private transient Vector clone = null;
    
    /**
     * Mutex lock for instantiating the listener collection.
     */
    private transient final boolean[] MUTEX = new boolean[0];


    /**
     * Always use this method to get a reference to listener collection.
     * This guarantees thread safety.
     */
    private Vector getListInstance() {
        synchronized (MUTEX) {
            if (list == null) { 
                list  = new Vector();
                clone = (Vector)list.clone();
            }
        }
        return list;
    }

    /**
     * Constructs support object.
     */
    public EventGeneratorSupport() {}

    
    /**
     * Checks if any registered listeners exist.
     *
     * @return  true if one or more listeners exist, false otherwise
     */
    public boolean hasListeners() {
        if (list == null)            // lazy list, not instantiated
            return false;           
            
        if (list.size() == 0)        // instantiated but empty
            return false;
        
        return true;
    }

    
    /**
     * Returns the listeners registered to this object.
     *
     * @return enumeration of registered listener objects
     */
    protected Enumeration getListeners() {
        if (list == null) 
            return new Enumeration() {
                
                public boolean hasMoreElements() { 
                    return false;
                }
                
                public Object  nextElement() {
                    return null;
                }
            };
            
        Vector v = (Vector)list.clone();
        return v.elements();
    }

    /**
     * Registers a new listener to this object. Duplicate listeners are
     * discarded.
     *
     * <p>
     *
     * This method is marked as protected and is supposed to be used only by
     * the concrete implementations of generator support classes. The concrete
     * subclasses must ensure only the correct type of listeners are allowed
     * to register, as this method allows any listener to be added, therefore
     * not being type safe.
     *
     * @param   listener    the listener object
     */
    protected void addListener(EventListener listener) {
        synchronized (MUTEX) {
            Vector v = getListInstance();
            if (!v.contains(listener))
                v.addElement(listener);
        }
    }

    /**
     * Unregisters a listener from this object. It is safe to attempt to
     * unregister non-existant listeners.
     *
     * @param   listener    the listener object
     */
    protected void removeListener(EventListener listener) {
        if (list != null) {
            list.removeElement(listener);
        }
    }

    /**
     * Returns a string representation of this EventGeneratorSupport object.
     * 
     * <p>
     *
     * The output will be similar to the following form:<br>
     * <pre>
     *          EventGeneratorSupport[Registered Listeners=1]
     * </pre>
     */
    public String toString() {
        int count;

        // don't want to instantiate listener just to tell it has zero elements
        if (list == null)
             count = 0;
        else count = getListInstance().size();
        
        return Library.getClassName(this) + "[Registered Listeners=" + count + "]";
    }

    /**
     * [PENDING]
     */
    public Component toComponent() {
        // [FIXME] this exception class has disappeared!
        //throw new UnsupportedOperationException("not implemented yet.");
        return null;
    }

    /**
     * [PENDING]
     */
    public Object clone() {
        // [FIXME] this exception class has disappeared!
        //throw new UnsupportedOperationException("not implemented yet.");
        return null;
    }

}


