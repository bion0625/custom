package com.stxtory.lietzsche.controller

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class MeController {
    @GetMapping("/api/me")
    fun me(@AuthenticationPrincipal principal: Any?): Mono<Map<String, Any?>> =
        Mono.just(mapOf("principal" to principal))
}