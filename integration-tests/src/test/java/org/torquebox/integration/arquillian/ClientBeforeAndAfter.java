/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
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

package org.torquebox.integration.arquillian;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.jboss.arquillian.impl.execution.AfterLifecycleEventExecuter;
import org.jboss.arquillian.impl.execution.BeforeLifecycleEventExecuter;
import org.jboss.arquillian.spi.Profile;


/**
 * Stolen from David Allen's OpenEJBProfile to allow CLIENT tests to
 * invoke @Before and @After methods.
 *
 */
public class ClientBeforeAndAfter implements Profile
{

   @SuppressWarnings("unchecked")
   @Override
   public Collection<Class<?>> getClientProfile()
   {
      // Add the Before/After methods executors to the client profile
      return Arrays.asList(
            AfterLifecycleEventExecuter.class,
            BeforeLifecycleEventExecuter.class
      );
   }

   @Override
   public Collection<Class<?>> getContainerProfile()
   {
      // Nothing to add to the container profile
      return Collections.EMPTY_LIST;
   }

}
