package no.nav.pto.veilarbfilter.config

import jakarta.servlet.http.HttpServletRequest
import no.nav.common.abac.audit.AuditRequestInfo
import no.nav.common.abac.audit.AuditRequestInfoSupplier
import no.nav.common.rest.filter.LogRequestFilter.resolveCallId
import no.nav.common.rest.filter.LogRequestFilter.resolveConsumerId
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.*

class SpringAuditRequestInfoSupplier : AuditRequestInfoSupplier {
    override fun get(): AuditRequestInfo {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
            .filter { requestAttributes: RequestAttributes? -> requestAttributes is ServletRequestAttributes }
            .map { requestAttributes: RequestAttributes -> requestAttributes as ServletRequestAttributes }
            .map { obj: ServletRequestAttributes -> obj.request }
            .map { request: HttpServletRequest ->
                utledRequestInfo(
                    request
                )
            }
            .orElse(null)
    }

    companion object {
        private fun utledRequestInfo(request: HttpServletRequest): AuditRequestInfo {
            return AuditRequestInfo.builder()
                .callId(resolveCallId(request))
                .consumerId(resolveConsumerId(request))
                .requestMethod(request.method)
                .requestPath(request.requestURI)
                .build()
        }
    }
}