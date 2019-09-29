package com.serebit.strife.internal.network

import com.serebit.logkat.Logger
import com.serebit.strife.internal.*
import com.serebit.strife.internal.dispatches.Ready
import com.serebit.strife.internal.dispatches.Resumed
import com.serebit.strife.internal.dispatches.Unknown
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlin.coroutines.coroutineContext
import kotlin.random.Random
import kotlin.random.nextLong

/**
 * [Gateways][Gateway] are Discord's form of real-time communication over secure websockets.
 * The client receives events and data over the [Gateway] they are connected to and send
 * data over the REST API. The API for interacting with Gateways is complex and fairly
 * unforgiving, therefore it's highly recommended you read all of the
 * [documentation.](https://discordapp.com/developers/docs/topics/gateway#gateways)
 *
 * This class [connects][connect] to the [Gateway] using the provided [uri], and
 * [maintains the connection][maintainConnection] until [disconnect] is called, or we receive a
 * [PostCloseAction.CLOSE].
 *
 * After a successful connection, [onReceive] will be called whenever we receive a [Payload], and the [token] will
 * be used to [establish a new session][establishSession], or [resume an existing one][resumeSession].
 */
@UseExperimental(KtorExperimentalAPI::class)
internal class Gateway(
    private val uri: String,
    private val token: String,
    private val shardID: Int,
    private val shardCount: Int,
    private val logger: Logger,
    private val listener: GatewayListener
) {
    /**
     * A [GatewaySocket] object providing WebSocket API to this [Gateway]. This class helps in splitting the workload
     * by handling the connection and ratelimiting until the connection closes, and hides all its internals from
     * [Gateway].
     */
    private var socket: GatewaySocket? = null

    /** The last [session id][Ready.Data.session_id], or null if no session was established yet. */
    private var sessionID: String? = null
    /** The last [sequence number][DispatchPayload.s], or 0 if none is received yet. */
    private var sequence: Int = 0
    /** A [BroadcastChannel] to broadcast once [Ready] dispatch has been received, to resume dispatching events. */
    @UseExperimental(ExperimentalCoroutinesApi::class)
    private var readyBroadcast: BroadcastChannel<Unit>? = null

    /**
     * An instance of [Heart] to handle
     * [heartbeating][https://discordapp.com/developers/docs/topics/gateway#heartbeating]. It periodically sends a
     * [HeartbeatPayload] to the [Gateway] by the given [interval][Heart.interval] to keep the connection up. If no
     * [HeartbeatAckPayload] between its attempts to [beat][Heart.beat], the connection will be closed immediately
     * with a [GatewayCloseCode.HEARTBEAT_EXPIRED].
     */
    private var heart = Heart(logger) {
        socket?.send(HeartbeatPayload.serializer(), HeartbeatPayload(sequence))
    }

    /** Handles and logs any exceptions thrown in [onReceive]. */
    private val handler = CoroutineExceptionHandler { _, throwable ->
        logger.error("Error in gateway: ${throwable.stackTraceAsString}")
    }

    /**
     * Starts a new [connection cycle][maintainConnection], and stops it [when the process exits][onProcessExit].
     */
    suspend fun connect() {
        val connectionJob = CoroutineScope(coroutineContext).launch {
            logger.info("Connecting to Discord...")
            maintainConnection()
        }

        onProcessExit {
            disconnect()
            // necessary so that the client sends a close frame before exiting
            connectionJob.join()
        }

        // necessary so that the client doesn't close immediately
        connectionJob.join()
    }

    /**
     * A tail-recursive function that maintains the connection to Discord until [disconnect] is called, or we receive
     * a [PostCloseAction.CLOSE].
     */
    private tailrec suspend fun maintainConnection() {
        val closeReason = GatewaySocket(logger).also { socket = it }.connect(uri) { scope, frameText ->
            onReceive(scope, frameText)
        }

        if (closeReason?.message?.startsWith("<CLIENT>") == true) {
            logger.info("Connection closed by client.")
        } else {
            val closeCode = GatewayCloseCode.values().firstOrNull { it.code == closeReason?.code }

            logger.error(
                "Got disconnected: ${closeReason?.code ?: "No code"}: ${closeCode?.message ?: "Unknown reason."}"
            )

            when (closeCode?.action) {
                PostCloseAction.RESUME, null -> {
                    logger.info("Attempting to resume...")
                    maintainConnection()
                }
                PostCloseAction.RESTART -> {
                    sessionID = null
                    logger.info("Attempting to reconnect...")
                    maintainConnection()
                }
                PostCloseAction.CLOSE -> logger.info("No further attempts to reconnect.")
            }
        }
    }

    /**
     * Handles [Payloads][Payload] sent to us by Discord.
     */
    @UseExperimental(ExperimentalCoroutinesApi::class)
    private fun onReceive(scope: CoroutineScope, frameText: String) = scope.launch(handler) {
        when (val payload = Payload(frameText)) {
            is HelloPayload -> {
                socket?.setHeartbeatInterval(payload.d.heartbeat_interval)
                sessionID?.also { resumeSession(it) } ?: establishSession()
                heart
                    .apply { interval = payload.d.heartbeat_interval }
                    .start(scope) {
                        socket?.close(GatewayCloseCode.HEARTBEAT_EXPIRED.let { CloseReason(it.code, it.message) })
                    }
            }
            is InvalidSessionPayload -> {
                sessionID?.takeIf { payload.d }?.also { resumeSession(it) } ?: also {
                    logger.info("Invalid session. Starting a new one...")

                    delay(Random.nextLong(1_000L..5_000L))
                    establishSession()
                }
            }
            is HeartbeatPayload -> heart.beat()
            is HeartbeatAckPayload -> heart.acknowledge()
            is DispatchPayload -> {
                when (payload) {
                    is Unknown -> logger.trace("Received unknown dispatch with type ${payload.t}")
                    is Ready -> {
                        logger.info("Successfully started session.")
                        sessionID = payload.d.session_id
                    }
                    is Resumed -> logger.info("Successfully resumed session.")
                }

                sequence = payload.s

                readyBroadcast?.takeUnless { payload is Ready }?.openSubscription()?.receive()
                listener.onDispatch(scope, payload)

                readyBroadcast
                    ?.takeIf { payload is Ready }
                    ?.also { readyBroadcast = null }
                    ?.also { it.send(Unit) }
                    ?.close()
            }
        }
    }

    /** Starts a new [Gateway] session. */
    @UseExperimental(ExperimentalCoroutinesApi::class)
    private suspend fun establishSession() {
        readyBroadcast = BroadcastChannel(1)
        socket?.send(
            IdentifyPayload.serializer(), IdentifyPayload(
                IdentifyPayload.Data(
                    token, mapOf(
                        "\$os" to osName,
                        "\$browser" to "strife",
                        "\$device" to "strife"
                    ),
                    shard = intArrayOf(shardID, shardCount)
                )
            )
        )
    }

    /** Resumes an existing [Gateway] session. */
    private suspend fun resumeSession(sessionID: String) {
        socket?.send(ResumePayload.serializer(), ResumePayload(token, sessionID, sequence))
    }

    /** Updates the bot's status and presence. */
    suspend fun updateStatus(payload: StatusUpdatePayload) {
        socket?.send(StatusUpdatePayload.serializer(), payload)
    }

    /**
     * Disconnects this [Gateway] from Discord servers and stops the [heart], or warns if this [Gateway] is already
     * disconnected.
     */
    suspend fun disconnect() = socket?.also {
        heart.kill()
        it.close(CloseReason(CloseReason.Codes.NORMAL, "<CLIENT> Normal close."))
    } ?: logger.warn("Attempted to disconnect an inactive gateway.")
}

/**
 * An enum class for all known close [codes][code] used either by Discord or Strife, with an explanatory [message]
 * shown to the user of this library, and [PostCloseAction] to help the [Gateway] decide what to do after receiving
 * this code.
 */
internal enum class GatewayCloseCode(val code: Short, val message: String, val action: PostCloseAction) {
    // This is originally a graceful close code, but discord sends it if the heartbeat has expired.
    GRACEFUL_CLOSE(1000, "The connection was closed gracefully.", PostCloseAction.RESUME),
    CLOUD_FLARE_LOAD(
        1001, "The connection was closed due to CloudFlare load balancing.",
        PostCloseAction.RESTART
    ),
    INTERNAL_SERVER_ERROR(1006, "Something broke on the remote server's end.", PostCloseAction.RESTART),
    HEARTBEAT_EXPIRED(1008, "The heartbeat has expired.", PostCloseAction.RESUME),
    UNKNOWN_ERROR(4000, "The server is not sure what went wrong.", PostCloseAction.RESUME),
    UNKNOWN_OPCODE(4001, "You sent an invalid gateway op code.", PostCloseAction.RESUME),
    DECODE_ERROR(4002, "We sent an invalid payload to the server.", PostCloseAction.RESUME),
    NOT_AUTHENTICATED(4003, "We sent a payload prior to identifying.", PostCloseAction.RESTART),
    AUTH_FAILED(
        4004, "The account token sent with our identify payload is incorrect.",
        PostCloseAction.CLOSE
    ),
    ALREADY_AUTHENTICATED(4005, "We sent more than one identify payload.", PostCloseAction.RESUME),
    INVALID_SEQ(
        4007, "The payload sent when resuming the session was invalid.",
        PostCloseAction.RESTART
    ),
    RATE_LIMITED(4008, "You're sending payloads to us too quickly.", PostCloseAction.RESUME),
    SESSION_TIMEOUT(
        4009, "Your session timed out. Reconnect and start a new one.",
        PostCloseAction.RESTART
    ),
    INVALID_SHARD(4010, "You sent an invalid shard when identifying.", PostCloseAction.CLOSE),
    SHARD_REQUIRED(4011, "You are required to shard in order to connect.", PostCloseAction.CLOSE);
}

/**
 * The available actions for the [Gateway] after receiving a [GatewayCloseCode]. Helps the [Gateway] to decide what to
 * do after receiving one.
 */
internal enum class PostCloseAction { RESUME, RESTART, CLOSE }
