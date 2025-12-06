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
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.hip.kernel.exc.DefaultExceptionHandler;

/** DummySSLSocketFactory
 *
 * @author Luthiger Created on 23.07.2007 copied from http://www.javaworld.com/javatips/javatip115/javatip115.zip */
public class DummySSLSocketFactory extends SSLSocketFactory {
    private transient SSLSocketFactory factory;

    /** DummySSLSocketFactory constructor. */
    public DummySSLSocketFactory() {
        super();
        try {
            final SSLContext lSslContext = SSLContext.getInstance("TLS");
            lSslContext.init(null,
                    new TrustManager[] { new DummyTrustManager() },
                    new java.security.SecureRandom());
            factory = lSslContext.getSocketFactory();

        } catch (final NoSuchAlgorithmException | KeyManagementException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        }
    }

    /** @return {@link SocketFactory} the default factory */
    public static SocketFactory getDefault() {
        return new DummySSLSocketFactory();
    }

    @Override
    public Socket createSocket(final Socket socket, final String s, final int i, final boolean flag) throws IOException { // NOPMD
        return factory.createSocket(socket, s, i, flag);
    }

    @Override
    public Socket createSocket(final InetAddress inaddr, final int i, final InetAddress inaddr1, final int j) // NOPMD
            throws IOException {
        return factory.createSocket(inaddr, i, inaddr1, j);
    }

    @Override
    public Socket createSocket(final InetAddress inaddr, final int i) throws IOException { // NOPMD by lbenno
        return factory.createSocket(inaddr, i);
    }

    @Override
    public Socket createSocket(final String s, final int i, final InetAddress inaddr, final int j) throws IOException { // NOPMD
        return factory.createSocket(s, i, inaddr, j);
    }

    @Override
    public Socket createSocket(final String s, final int i) throws IOException { // NOPMD by lbenno
        return factory.createSocket(s, i);
    }

    @Override
    public String[] getDefaultCipherSuites() { // NOPMD by lbenno
        return factory.getSupportedCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() { // NOPMD by lbenno
        return factory.getSupportedCipherSuites();
    }

}
