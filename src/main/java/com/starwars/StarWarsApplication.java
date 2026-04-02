package com.starwars;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Star Wars Integration Platform.
 *
 * <p>This application exposes a secured REST API that proxies and enriches data
 * from the public Star Wars API (swapi.tech), supporting:
 * <ul>
 *   <li>JWT-based authentication (register / login)</li>
 *   <li>Paginated listing of People, Films, Starships and Vehicles</li>
 *   <li>Filtering by ID or name</li>
 * </ul>
 *
 * <p>API documentation is available at {@code /swagger-ui.html} once the app is running.
 */
@SpringBootApplication
public class StarWarsApplication {

    public static void main(String[] args) {
        SpringApplication.run(StarWarsApplication.class, args);
    }
}