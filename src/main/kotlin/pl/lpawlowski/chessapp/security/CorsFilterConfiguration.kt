package pl.lpawlowski.chessapp.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.CollectionUtils
import org.springframework.validation.annotation.Validated
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
@EnableConfigurationProperties(CorsFilterProperties::class)
class CorsFilterConfiguration {
    private var properties: CorsFilterProperties = CorsFilterProperties()

    @Bean("corsFilter")
    fun corsFilter(): FilterRegistrationBean<CorsFilter?> {
        val config = buildCorsConfiguration()
        val source = UrlBasedCorsConfigurationSource()

        source.registerCorsConfiguration("/**", config)

        return FilterRegistrationBean(CorsFilter(source))
    }

    private fun buildCorsConfiguration(): CorsConfiguration {
        val config = CorsConfiguration()
        config.allowCredentials = true
        if (properties.maxAge != null) {
            config.maxAge = properties.maxAge
        }
        if (!CollectionUtils.isEmpty(properties.allowedMethods)) {
            config.allowedMethods = properties.allowedMethods
        }
        if (!CollectionUtils.isEmpty(properties.allowedHeaders)) {
            config.allowedHeaders = properties.allowedHeaders
        }
        if (!CollectionUtils.isEmpty(properties.allowedOrigins)) {
            config.allowedOrigins = properties.allowedOrigins
        }
        return config
    }
}

@Validated
@ConfigurationProperties(prefix = CorsFilterProperties.PREFIX)
class CorsFilterProperties {
    val order = 100
    var urlPatterns: List<String>? = null
    var allowedOrigins: List<String>? = listOf("http://localhost:3000/")
    var allowedMethods: List<String>? = listOf("GET","POST","PUT","DELETE","ORIGIN")
    var allowedHeaders: List<String>? = listOf("Origin","Content-Type","Accept","Authorization")
    var maxAge: Long? = null

    companion object {
        const val PREFIX = "web.filter.cors"
    }
}