package id.walt.oidp.oidc

import com.nimbusds.oauth2.sdk.AuthorizationRequest
import com.nimbusds.oauth2.sdk.GrantType
import com.nimbusds.oauth2.sdk.PushedAuthorizationSuccessResponse
import com.nimbusds.oauth2.sdk.ResponseType
import com.nimbusds.oauth2.sdk.http.HTTPRequest
import com.nimbusds.oauth2.sdk.http.ServletUtils
import com.nimbusds.oauth2.sdk.id.Issuer
import com.nimbusds.openid.connect.sdk.SubjectType
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata
import id.walt.issuer.backend.IssuerController
import id.walt.oidp.config.OIDPConfig
import id.walt.verifier.backend.VerifierConfig
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.HttpCode
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documented
import java.net.URI

object OIDCController {
  val routes
    get() = path("") {
      get(".well-known/openid-configuration", documented(
        document().operation {
          it.summary("get OIDC provider meta data")
            .addTagsItem("OIDC")
            .operationId("oidcProviderMeta")
        }
        .json<OIDCProviderMetadata>("200"),
        OIDCController::openIdConfiguration
      ))
      post("par", documented(
        document().operation {
          it.summary("Pushed authorization request")
            .addTagsItem("OIDC")
            .operationId("par")
        },
        OIDCController::pushedAuthorizationRequest
      ))
      get("authorize", documented(
        document().operation {
         it.summary("Authorization user agent endpoint")
           .addTagsItem("OIDC")
           .operationId("authorize")
        },
        OIDCController::authorizationRequest
      ))
      post("token", documented(
        document().operation {
         it.summary("Token endoint")
           .addTagsItem("OIDC")
           .operationId("token")
        },
        OIDCController::tokenRequest
      ))
      post("userInfo", documented(
        document().operation {
         it.summary("User Info endpoint")
           .addTagsItem("OIDC")
           .operationId("userInfo")
        },
        OIDCController::userInfoRequest
      ))
    }

  fun openIdConfiguration(ctx: Context) {
    ctx.json(OIDCManager.getService().oidcProviderMetadata.toJSONObject())
  }

  fun pushedAuthorizationRequest(ctx: Context) {
    val authReq = AuthorizationRequest.parse(ServletUtils.createHTTPRequest(ctx.req))
    val oidcSession = OIDCManager.getService().initOIDCSession(authReq)
    ctx.status(HttpCode.CREATED).json(PushedAuthorizationSuccessResponse(URI.create("urn:ietf:params:oauth:request_uri:${oidcSession.id}"), OIDCManager.getService().EXPIRATION_TIME.seconds).toJSONObject())
  }

  fun authorizationRequest(ctx: Context) {
    val oidcSession = ctx.queryParam("request_uri")?.let {
      OIDCManager.getService().getOIDCSession(it) ?: throw BadRequestResponse("Session not found or expired")
    } ?: OIDCManager.getService().initOIDCSession(
      kotlin.runCatching {
        AuthorizationRequest.parse(ServletUtils.createHTTPRequest(ctx.req))
      }.getOrElse {
        throw BadRequestResponse("Error parsing OIDC authorization request from query parameters")
      }
    )

    ctx.status(HttpCode.FOUND).header("Location", OIDCManager.getService().getWalletRedirectionUri(oidcSession).toString())
  }

  fun tokenRequest(ctx: Context) {

  }

  fun userInfoRequest(ctx: Context) {

  }
}
