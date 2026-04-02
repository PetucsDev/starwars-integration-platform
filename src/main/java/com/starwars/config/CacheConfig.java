package com.starwars.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Enables Spring's annotation-driven caching ({@code @Cacheable}).
 *
 * <p>The cache implementation is Caffeine (configured in {@code application.yml}).
 * All SWAPI responses are cached for 30 minutes with a maximum of 500 entries,
 * since Star Wars data is essentially static.
 *
 * <p>Cache names used across services:
 * <ul>
 *   <li>{@code people} — People list and detail responses</li>
 *   <li>{@code films} — Films list and detail responses</li>
 *   <li>{@code starships} — Starships list and detail responses</li>
 *   <li>{@code vehicles} — Vehicles list and detail responses</li>
 * </ul>
 */
@EnableCaching
@Configuration
public class CacheConfig {
}
