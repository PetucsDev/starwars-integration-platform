package com.starwars.swapi.client;

import com.starwars.exception.ResourceNotFoundException;
import com.starwars.exception.SwapiException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Low-level HTTP wrapper around the Star Wars API.
 *
 * <p>All HTTP errors are translated into application-level exceptions:
 * <ul>
 *   <li>404 → {@link ResourceNotFoundException}</li>
 *   <li>4xx / 5xx → {@link SwapiException}</li>
 * </ul>
 */
@Component
public class SwapiClient {

    private final RestClient restClient;

    public SwapiClient(RestClient swapiRestClient) {
        this.restClient = swapiRestClient;
    }

    /**
     * Performs a GET request and deserializes the response body.
     *
     * @param path         the API path (relative to the configured base URL)
     * @param responseType the expected response type
     * @param <T>          response type parameter
     * @return deserialized response body
     */
    public <T> T get(String path, ParameterizedTypeReference<T> responseType) {
        try {
            return restClient.get()
                    .uri(path)
                    .retrieve()
                    .onStatus(status -> status == HttpStatus.NOT_FOUND,
                            (req, res) -> {
                                throw new ResourceNotFoundException(
                                        "Resource not found in SWAPI: " + path);
                            })
                    .onStatus(HttpStatusCode::is4xxClientError,
                            (req, res) -> {
                                throw new SwapiException(
                                        "SWAPI client error (" + res.getStatusCode() + "): " + path);
                            })
                    .onStatus(HttpStatusCode::is5xxServerError,
                            (req, res) -> {
                                throw new SwapiException(
                                        "SWAPI server error (" + res.getStatusCode() + "): " + path);
                            })
                    .body(responseType);
        } catch (ResourceNotFoundException | SwapiException e) {
            throw e;
        } catch (RestClientException e) {
            throw new SwapiException("Failed to reach the Star Wars API", e);
        }
    }
}