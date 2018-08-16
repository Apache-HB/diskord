package com.serebit.diskord.network

internal enum class GatewayCloseCode(val code: Int, val message: String, val action: PostCloseAction) {
    GRACEFUL_CLOSE(1000, "The connection was closed gracefully or your heartbeats timed out.", PostCloseAction.CLOSE),
    CLOUD_FLARE_LOAD(1001, "The connection was closed due to CloudFlare load balancing.", PostCloseAction.RESTART),
    INTERNAL_SERVER_ERROR(1006, "Something broke on the remote server's end.", PostCloseAction.RESTART),
    UNKNOWN_ERROR(4000, "The server is not sure what went wrong.", PostCloseAction.RESUME),
    UNKNOWN_OPCODE(4001, "You sent an invalid gateway op code.", PostCloseAction.RESUME),
    DECODE_ERROR(4002, "We sent an invalid payload to the server.", PostCloseAction.RESUME),
    NOT_AUTHENTICATED(4003, "We sent a payload prior to identifying.", PostCloseAction.RESTART),
    AUTHENTICATION_FAILED(
        4004, "The account token sent with our identify payload is incorrect.", PostCloseAction.CLOSE
    ),
    ALREADY_AUTHENTICATED(4005, "We sent more than one identify payload.", PostCloseAction.RESUME),
    INVALID_SEQ(4007, "The payload sent when resuming the session was invalid.", PostCloseAction.RESTART),
    RATE_LIMITED(4008, "You're sending payloads to us too quickly.", PostCloseAction.RESUME),
    SESSION_TIMEOUT(4009, "Your session timed out. Reconnect and start a new one.", PostCloseAction.RESTART),
    INVALID_SHARD(4010, "You sent an invalid shard when identifying.", PostCloseAction.CLOSE),
    SHARDING_REQUIRED(
        4011,
        "The session would have handled too many guilds - you are required to shard in order to connect.",
        PostCloseAction.CLOSE
    );

    companion object {
        fun valueOf(code: Int) = values().firstOrNull { it.code == code }
    }
}
