package id.walt.idp.config

import com.beust.klaxon.Klaxon
import id.walt.idp.oidc.OIDCManager
import id.walt.webwallet.backend.config.ExternalHostnameUrl
import id.walt.webwallet.backend.config.externalHostnameUrlValueConverter
import java.io.File

data class IDPConfig(
    @ExternalHostnameUrl val externalUrl: String = "http://localhost:6000",
    val jsProjectExternalUrl: String = "http://localhost:6000",
    val keyId: String = "",
    val claimConfig: ClaimConfig? = null,
    val openClientRegistration: Boolean = false,
    val fallbackAuthorizationMode: OIDCManager.AuthorizationMode = OIDCManager.AuthorizationMode.SIOP
) {
    companion object {
        val CONFIG_FILE = "${id.walt.WALTID_DATA_ROOT}/config/idp-config.json"
        var config: IDPConfig

        init {
            val cf = File(CONFIG_FILE)
            if (cf.exists()) {
                config = Klaxon().fieldConverter(ExternalHostnameUrl::class, externalHostnameUrlValueConverter).parse(cf)
                    ?: IDPConfig()
            } else {
                config = IDPConfig()
            }
        }
    }
}
