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

package org.torquebox.integration.arquillian.rails;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebElement;
import org.torquebox.integration.arquillian.AbstractIntegrationTestCase;

public abstract class CommonSessionHandlingTestCase extends AbstractIntegrationTestCase {

    @Test
    public void testHighLevel() {
        Options options = driver.manage();
        options.deleteAllCookies();

        WebElement element = null;

        String cookieValue = null;

        driver.get( "http://localhost:8080/basic-rails/sessioning/get_value" );
        assertEquals( 1, options.getCookies().size() );
        assertNotNull( options.getCookieNamed( "JSESSIONID" ) );
        cookieValue = options.getCookieNamed( "JSESSIONID" ).getValue();

        assertNotNull( cookieValue );

        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "", element.getText().trim() );

        driver.get( "http://localhost:8080/basic-rails/sessioning/set_value" );
        assertEquals( 1, options.getCookies().size() );
        assertEquals( cookieValue, options.getCookieNamed( "JSESSIONID" ).getValue() );

        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "the value", element.getText().trim() );

        driver.get( "http://localhost:8080/basic-rails/sessioning/get_value" );
        assertEquals( 1, options.getCookies().size() );
        assertEquals( cookieValue, options.getCookieNamed( "JSESSIONID" ).getValue() );

        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "the value", element.getText().trim() );

        driver.get( "http://localhost:8080/basic-rails/sessioning/clear_value" );
        assertEquals( 1, options.getCookies().size() );
        assertEquals( cookieValue, options.getCookieNamed( "JSESSIONID" ).getValue() );

        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "", element.getText().trim() );

        driver.get( "http://localhost:8080/basic-rails/sessioning/get_value" );
        assertEquals( 1, options.getCookies().size() );
        assertEquals( cookieValue, options.getCookieNamed( "JSESSIONID" ).getValue() );

        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "", element.getText().trim() );
    }

    @Test
    public void testResetSession() {
        WebElement element = null;

        // should have no value to begin with
        driver.get( "http://localhost:8080/basic-rails/sessioning/get_value" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "", element.getText().trim() );

        // set a value in the session
        driver.get( "http://localhost:8080/basic-rails/sessioning/set_value" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "the value", element.getText().trim() );

        // get value from session
        driver.get( "http://localhost:8080/basic-rails/sessioning/get_value" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "the value", element.getText().trim() );

        // logout to reset the session
        driver.get( "http://localhost:8080/basic-rails/sessioning/logout" );

        // should have no value after resetting session
        driver.get( "http://localhost:8080/basic-rails/sessioning/get_value" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "", element.getText().trim() );
    }

    @Test
    public void testSessionViaMatrixUrl() {

        WebElement element = null;
        Options options = driver.manage();
        options.deleteAllCookies();

        // should have no value to begin with
        driver.get( "http://localhost:8080/basic-rails/sessioning/get_value" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "", element.getText().trim() );

        // set a value in the session
        driver.get( "http://localhost:8080/basic-rails/sessioning/set_value" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "the value", element.getText().trim() );

        // get value from session
        driver.get( "http://localhost:8080/basic-rails/sessioning/get_value" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "the value", element.getText().trim() );

        String cookieValue = options.getCookieNamed( "JSESSIONID" ).getValue();

        assertNotNull( cookieValue );
        assertFalse( "".equals( cookieValue ) );

        options.deleteAllCookies();

        driver.get( "http://localhost:8080/basic-rails/sessioning/get_value;jsessionid=" + cookieValue );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "the value", element.getText().trim() );

        element = driver.findElementById( "session_id" );
        assertNotNull( element );
        assertEquals( cookieValue, element.getText().trim() );

        assertEquals( 0, options.getCookies().size() );
    }

    @Test
    public void testSessionCrossing() {
        Options options = driver.manage();
        options.deleteAllCookies();

        WebElement element = null;

        driver.get( "http://localhost:8080/basic-rails/sessioning/logout" );

        driver.get( "http://localhost:8080/basic-rails/sessioning/set_from_ruby" );
        driver.get( "http://localhost:8080/basic-rails/sessioning/display_session" );

        element = driver.findElementById( "a_fixnum_ruby" );
        assertNotNull( element );
        assertEquals( "42", element.getText() );

        element = driver.findElementById( "a_fixnum_java" );
        assertNotNull( element );
        assertEquals( "42", element.getText() );

        element = driver.findElementById( "a_string_ruby" );
        assertNotNull( element );
        assertEquals( "swordfish", element.getText() );

        element = driver.findElementById( "a_string_java" );
        assertNotNull( element );
        assertEquals( "swordfish", element.getText() );

        element = driver.findElementById( "a_boolean_ruby" );
        assertNotNull( element );
        assertEquals( "true", element.getText() );

        element = driver.findElementById( "a_boolean_java" );
        assertNotNull( element );
        assertEquals( "true", element.getText() );
    }

    @Test
    public void testSessionResetAndRestore() {
        WebElement element = null;

        // should have no value to begin with
        driver.get( "http://localhost:8080/basic-rails/sessioning/get_value" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "", element.getText().trim() );

        // set a value in the session
        driver.get( "http://localhost:8080/basic-rails/sessioning/set_value" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "the value", element.getText().trim() );

        // get value from session
        driver.get( "http://localhost:8080/basic-rails/sessioning/get_value" );
        element = driver.findElementById( "success" );
        assertNotNull( element );
        assertEquals( "the value", element.getText().trim() );

        // reset and restore the session (mimic Devise)
        driver.get( "http://localhost:8080/basic-rails/sessioning/reset_and_restore" );
        // just ensure the page rendered without error
        element = driver.findElementById( "success" );
        assertNotNull( element );
    }

}
