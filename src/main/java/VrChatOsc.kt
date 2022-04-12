import com.illposed.osc.*
import com.illposed.osc.transport.OSCPortIn
import com.illposed.osc.transport.OSCPortOut
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.lang.Exception
import java.net.InetAddress

private const val DEFAULT_VRC_EMOTE_ENDPOINT = "/avatar/parameters/VRCEmote"

/**
 * Wrapper around Java OSC library, specialized for VrChat.
 */
object VrcOscController {
    private const val IP = "127.0.0.1"
    private const val SEND_PORT = 9000
    private const val RECEIVE_PORT = 9001

    private val sender by lazy {
        OSCPortOut(InetAddress.getByName(IP), SEND_PORT)
    }

    private val receiver by lazy {
        OSCPortIn(RECEIVE_PORT)
    }

    /**
     * Starts receiver of OSC data.
     *
     * @param paramEndpoint Endpoint of param to be observed (EG: "/avatar/parameters/VRCEmote").
     * @param onMessageReceived Listener for when OSC message is received for your [paramEndpoint].
     */
    fun startVrcReceiver(
        paramEndpoint: String,
        onMessageReceived: (messages: List<Any>) -> Unit
    ) {
        receiver.dispatcher.addListener(
            object : MessageSelector {
                override fun isInfoRequired(): Boolean = false

                override fun matches(messageEvent: OSCMessageEvent?): Boolean =
                    messageEvent?.message?.address?.trim() == paramEndpoint
            }
        ) {
            print("Event: ${it?.message?.arguments} \n")
            onMessageReceived(it?.message?.arguments ?: listOf())
        }
        receiver.startListening()
    }

    /**
     * Sends OSC data ([values]) to [endpoint].
     *
     * @param endpoint Endpoint of param to be sent (EG: "/avatar/parameters/VRCEmote").
     * @param values Values to be sent.
     */
    fun send(
        endpoint: String,
        values: List<Any>
    ) {
        try {
            sender.send(
                OSCMessage(endpoint, values)
            )
        } catch (ex: Exception) {
            System.err.println("Couldn't send: $ex")
        }
    }
}

fun main() {
    VrcOscController.startVrcReceiver(
        paramEndpoint = DEFAULT_VRC_EMOTE_ENDPOINT,
        onMessageReceived = {
            // Put code to action on items here.
        }
    )

    runBlocking {
        // Do emote 1
        delay(2000L)
        VrcOscController.send(
            endpoint = DEFAULT_VRC_EMOTE_ENDPOINT,
            values = listOf(3)
        )

        // Do emote 2
        delay(5000L)
        VrcOscController.send(
            endpoint = DEFAULT_VRC_EMOTE_ENDPOINT,
            values = listOf(5)
        )

        // Do no emote.
        delay(5000L)
        VrcOscController.send(
            endpoint = DEFAULT_VRC_EMOTE_ENDPOINT,
            values = listOf(0)
        )
        delay(1000L)
    }
}