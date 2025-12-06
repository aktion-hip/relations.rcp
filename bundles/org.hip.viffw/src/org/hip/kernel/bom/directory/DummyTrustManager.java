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

import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/** DummyTrustManager
 *
 * @author Luthiger Created on 23.07.2007 */
public class DummyTrustManager implements X509TrustManager { // NOPMD by lbenno 

    @Override
    public void checkClientTrusted(final X509Certificate[] cert, final String authType) { // NOPMD by lbenno 
        // everything is trusted
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] cert, final String authType) { // NOPMD by lbenno 
        // everything is trusted
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() { // NOPMD by lbenno 
        return new X509Certificate[0];
    }

}
