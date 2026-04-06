package com.inspectpro.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.concurrent.TimeUnit

@Component
class RateLimitingFilter(
    private val redisTemplate: RedisTemplate<String, String>
) : OncePerRequestFilter() {

    companion object {
        private const val MAX_REQUESTS_PER_MINUTE = 100L
        private const val WINDOW_SECONDS = 60L
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val userId = extractUserId()

        if (userId != null) {
            val key = "rate_limit:$userId"
            val current = redisTemplate.opsForValue().increment(key) ?: run {
                filterChain.doFilter(request, response)
                return
            }

            if (current == 1L) {
                redisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS)
            }

            if (current > MAX_REQUESTS_PER_MINUTE) {
                response.status = HttpStatus.TOO_MANY_REQUESTS.value()
                response.contentType = "application/json"
                response.writer.write(
                    """
                        {
                            "status": 429,
                            "error": "Too Many Requests",
                            "message": "Rate limit exceeded. Maximum $MAX_REQUESTS_PER_MINUTE requests per minute allowed"
                        }
                        """.trimIndent()
                )
                return
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun extractUserId(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        return if (authentication != null && authentication.principal is AuthenticatedUser) {
            (authentication.principal as AuthenticatedUser).userId.toString()
        } else null
    }
}