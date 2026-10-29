package org.elbe.relations.internal.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/** @author lbenno */
public class PeerAddressesTest {
    private static final String P2P = "/tcp/9042/p2p/12D3KooWAv1A9pyJsVn43oo47WRhq2eqpfyVaaZkiMSc6kPecHeL";
    private static final String IP6 = "/ip6/2001:db8:0:0:0:0:0:1" + P2P;
    private static final String VPN = "/ip4/203.0.113.7" + P2P;
    private static final String LAN = "/ip4/192.168.1.5" + P2P;

    @Test
    public void testSiteLocalPreferred() {
        assertEquals(LAN, PeerAddresses.choose(List.of(IP6, VPN, LAN)).orElseThrow());
        assertEquals("/ip4/10.0.0.2" + P2P,
                PeerAddresses.choose(List.of(VPN, "/ip4/10.0.0.2" + P2P, LAN)).orElseThrow());
        assertEquals("/ip4/172.20.1.1" + P2P,
                PeerAddresses.choose(List.of("/ip4/172.32.1.1" + P2P, "/ip4/172.20.1.1" + P2P)).orElseThrow());
    }

    @Test
    public void testFirstIp4WithoutSiteLocal() {
        assertEquals(VPN, PeerAddresses.choose(List.of(IP6, VPN, "/ip4/198.51.100.1" + P2P)).orElseThrow());
        // 172.32.x.x is outside the private range 172.16-31
        assertEquals("/ip4/172.32.1.1" + P2P, PeerAddresses.choose(List.of("/ip4/172.32.1.1" + P2P)).orElseThrow());
    }

    @Test
    public void testNoIp4() {
        assertTrue(PeerAddresses.choose(List.of()).isEmpty());
        assertTrue(PeerAddresses.choose(List.of(IP6)).isEmpty());
    }

    @Test
    public void testMalformedIgnoredAsSiteLocal() {
        assertEquals("/ip4/192.168" + P2P, PeerAddresses.choose(List.of("/ip4/192.168" + P2P)).orElseThrow());
        assertEquals(LAN, PeerAddresses.choose(List.of("/ip4/192.168.x.1" + P2P, LAN)).orElseThrow());
    }

}
