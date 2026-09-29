package moe.https.syncthing.viewmodel

import kotlin.test.Test
import kotlin.test.assertEquals
import moe.https.syncthing.ui.util.ListenAddressListItem

class SettingServerAddressesTest {
    @Test
    fun coreRelaySchemesAreRecognized() {
        assertEquals(true, isRelayOrDefaultAddress("default"))
        assertEquals(true, isRelayOrDefaultAddress("relay://relay.example:22067"))
        assertEquals(true, isRelayOrDefaultAddress("dynamic+http://pool.example"))
        assertEquals(true, isRelayOrDefaultAddress("dynamic+https://pool.example"))
        assertEquals(false, isRelayOrDefaultAddress("tcp://0.0.0.0:22000"))
    }

    @Test
    fun coreAddressesOverrideSavedDisabledEntries() {
        assertEquals(
            listOf(
                ListenAddressListItem(true, "https://discovery.example/v2/"),
                ListenAddressListItem(false, "https://inactive.example/v2/"),
            ),
            mergeCoreServerAddresses(
                coreAddresses = listOf("https://discovery.example/v2/"),
                disabledAddresses = listOf(
                    ListenAddressListItem(false, "https://discovery.example/v2/"),
                    ListenAddressListItem(false, "https://inactive.example/v2/"),
                ),
            ),
        )
    }

    @Test
    fun defaultListenAddressRemainsOwnedByCore() {
        assertEquals(
            listOf("default", "relay://custom.example:22067"),
            buildListenAddresses(
                coreAddresses = listOf("default"),
                generatedAddresses = listOf("tcp4://0.0.0.0:22000"),
                relayAddresses = listOf(
                    ListenAddressListItem(true, "default"),
                    ListenAddressListItem(true, "relay://custom.example:22067"),
                ),
                directListenChanged = false,
            ),
        )
    }

    @Test
    fun removingDefaultKeepsDirectListening() {
        assertEquals(
            listOf("tcp4://0.0.0.0:22000", "tcp://192.0.2.1:22000"),
            buildListenAddresses(
                coreAddresses = listOf("default", "tcp://192.0.2.1:22000"),
                generatedAddresses = listOf("tcp4://0.0.0.0:22000"),
                relayAddresses = listOf(ListenAddressListItem(false, "default")),
                directListenChanged = false,
            ),
        )
    }

    @Test
    fun editingRelayPreservesCustomDirectListener() {
        assertEquals(
            listOf("tcp://192.0.2.1:22000", "relay://new.example:22067"),
            buildListenAddresses(
                coreAddresses = listOf("tcp://192.0.2.1:22000", "relay://old.example:22067"),
                generatedAddresses = listOf("tcp4://0.0.0.0:22000"),
                relayAddresses = listOf(ListenAddressListItem(true, "relay://new.example:22067")),
                directListenChanged = false,
            ),
        )
    }

    @Test
    fun unchangedListenAddressesKeepCoreOrder() {
        val coreAddresses = listOf(
            "relay://one.example:22067",
            "tcp://192.0.2.1:22000",
            "dynamic+http://pool.example",
        )
        assertEquals(
            coreAddresses,
            buildListenAddresses(
                coreAddresses = coreAddresses,
                generatedAddresses = listOf("tcp4://0.0.0.0:22000"),
                relayAddresses = listOf(
                    ListenAddressListItem(true, "relay://one.example:22067"),
                    ListenAddressListItem(true, "dynamic+http://pool.example"),
                ),
                directListenChanged = false,
            ),
        )
    }
}
