/**
	This package is part of the framework used for the application VIF.
	Copyright (C) 2006-2014, Benno Luthiger

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.hip.kernel.bom.directory;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.hip.kernel.exc.VException;
import org.hip.kernel.sys.VSys;

/** This class creates instances of <code>DirContext</code> based on the configured application settings.<br />
 * The configuration can be set in <code>vif.properties</code>:
 *
 * <table summary="Shows property keys and associated values">
 * <tr>
 * <th>Key</th>
 * <th>Description of Associated Value</th>
 * </tr>
 * <tr>
 * <td><code>org.hip.vif.ldap.url</code></td>
 * <td>LDAP server url and port, e.g. ldaps://ldaps.my.org:636/</td>
 * </tr>
 * <tr>
 * <td><code>org.hip.vif.ldap.managerDN</code></td>
 * <td>Manager DN</td>
 * </tr>
 * <tr>
 * <td><code>org.hip.vif.ldap.password</code></td>
 * <td>Password for Manager DN</td>
 * </tr>
 * </table>
 *
 * @author Luthiger Created on 06.07.2007 */
public final class LDAPContextManager {
    // constants
    private static final String KEY_URL = "org.hip.vif.ldap.url";
    private static final String KEY_PRINICPAL = "org.hip.vif.ldap.managerDN";
    private static final String KEY_PASSWORD = "org.hip.vif.ldap.password";

    private transient static LDAPContextManager singleton;

    private transient Map<String, String> environment;
    private transient String url = "";
    private transient boolean isSSL;

    private LDAPContextManager() throws VException {
        initialize();
    }

    private void initialize() throws VException {
        Properties lProperties;
        try {
            lProperties = VSys.getVSysProperties();
            url = lProperties.getProperty(KEY_URL);
            final String lPrincipal = lProperties.getProperty(KEY_PRINICPAL);
            final String lPassword = lProperties.getProperty(KEY_PASSWORD);

            if (url != null) {
                isSSL = url.startsWith("ldaps://");
            }
            environment = prepareEnvironment(url, lPrincipal, lPassword);
        } catch (final IOException exc) {
            throw new VException(exc.getMessage(), exc);
        } catch (final NamingException exc) {
            throw new VException(exc.getMessage(), exc);
        }
    }

    /** Gets the singleton instance.
     *
     * @return LDAPContextManager
     * @throws VException */
    public static synchronized LDAPContextManager getInstance() throws VException { // NOPMD by lbenno
        if (singleton == null) {
            singleton = new LDAPContextManager();
        }
        return singleton;
    }

    /** Returns the <code>DirContext</code> based on the configured application settings.
     *
     * @param inBaseDir String The context's base directory.
     * @param inCtls Control[] The possibly null request controls to use for the new context. If null, the context is
     *            initialized with no request controls.
     * @return DirContextWrapper
     * @throws NamingException */
    public DirContextWrapper getContext(final String inBaseDir, final Control... inCtls) throws NamingException {
        final DirContextWrapper outDirContext = new DirContextWrapper(getContextInstance(inCtls));
        outDirContext.setBase(inBaseDir);
        return outDirContext;
    }

    private LdapContext getContextInstance(final Control... inCtls) throws NamingException {
        final InitialLdapContext ldapContext = new InitialLdapContext((Hashtable<String, String>) environment, null); // NOPMD
        ldapContext.setRequestControls(inCtls);
        return ldapContext;
    }

    /** Check the authentication
     *
     * @param inKeyAttributeName String the name of the attribute that is the authentication context's key (most
     *            probably 'cn').
     * @param inBaseDir String Base directory on the LDAP server where the user objects are residing.
     * @param inUserID String User ID.
     * @param inPassword String The user's password.
     * @return boolean <code>true</code> if authentication has been possible with the given credentials. */
    public boolean checkAuthentication(final String inKeyAttributeName, final String inBaseDir, final String inUserID, // NOPMD
            final String inPassword) {
        boolean isAuthenticated = false;

        final String lUserDN = String.format("%s=%s,%s", inKeyAttributeName, inUserID, inBaseDir);
        DirContext lContext = null;
        try {
            final Map<String, String> lEnvironment = prepareEnvironment(url, lUserDN, inPassword);
            lContext = new InitialDirContext((Hashtable<String, String>) lEnvironment); // NOPMD by lbenno
            isAuthenticated = true;
        } catch (final NamingException exc) { // NOPMD by lbenno
            // intentionally left empty
        } finally {
            if (lContext != null) {
                try {
                    lContext.close();
                } catch (final NamingException exc) { // NOPMD by lbenno
                    // intentionally left empty
                }
            }
        }
        return isAuthenticated;
    }

    private Map<String, String> prepareEnvironment(final String inUrl, final String inPrincipal,
            final String inPassword) throws NamingException {
        final Map<String, String> lEnvironment = initialEnvironment();
        putChecked(lEnvironment, Context.PROVIDER_URL, inUrl);
        putChecked(lEnvironment, Context.SECURITY_PRINCIPAL, inPrincipal);
        putChecked(lEnvironment, Context.SECURITY_CREDENTIALS, inPassword);

        if (isSSL) {
            lEnvironment.put(Context.REFERRAL, "ignore");
            lEnvironment.put(Context.SECURITY_PROTOCOL, "ssl");
            lEnvironment.put("java.naming.ldap.factory.socket", DummySSLSocketFactory.class.getName());
        }
        return lEnvironment;
    }

    private void putChecked(final Map<String, String> inEnvironment, final String inKey, final String inValue) {
        if (inValue != null) {
            inEnvironment.put(inKey, inValue);
        }
    }

    private Map<String, String> initialEnvironment() throws NamingException {
        final Map<String, String> outEnvironment = new Hashtable<String, String>(); // NOPMD by lbenno
        outEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        outEnvironment.put(Context.SECURITY_AUTHENTICATION, "simple");
        return outEnvironment;
    }

}
