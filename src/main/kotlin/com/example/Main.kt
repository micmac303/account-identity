package com.example

import org.http4k.client.JavaHttpClient
import org.http4k.core.Credentials
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.DebuggingFilters.PrintRequest
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.security.InsecureCookieBasedOAuthPersistence
import org.http4k.security.OAuthProvider
import org.http4k.security.google
import org.http4k.server.SunHttp
import org.http4k.server.asServer

// Google OAuth Example
// Browse to: http://localhost:9000/oauth - you'll be redirected to google for authentication
val googleClientId = "myGoogleClientId"
val googleClientSecret = "myGoogleClientSecret"

// this is a test implementation of the OAuthPersistence interface, which should be
// implemented by application developers
val oAuthPersistence = InsecureCookieBasedOAuthPersistence("Google")

// pre-defined configuration exist for common OAuth providers
val oauthProvider = OAuthProvider.google(
        JavaHttpClient(),
        Credentials(googleClientId, googleClientSecret),
        Uri.of("http://localhost:9000/oauth/callback"),
        oAuthPersistence
)
val app: HttpHandler = routes(
    "/ping" bind GET to {
        Response(OK).body("pong")
    },

    "/testing/kotest" bind GET to {request ->
        Response(OK).body("Echo '${request.bodyString()}'")
    },

    "/oauth" bind routes(
            "/" bind GET to oauthProvider.authFilter.then { Response(OK).body("hello!") },
            "/callback" bind GET to oauthProvider.callback
    )
)

fun main() {
    val printingApp: HttpHandler = PrintRequest().then(app)

    val server = printingApp.asServer(SunHttp(9000)).start()

    println("Server started on " + server.port())
}
