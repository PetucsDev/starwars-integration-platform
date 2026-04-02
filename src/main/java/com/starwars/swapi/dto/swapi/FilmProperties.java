package com.starwars.swapi.dto.swapi;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Raw SWAPI properties object for a Film entity.
 */
public class FilmProperties {

    private String title;

    @JsonProperty("episode_id")
    private Integer episodeId;

    @JsonProperty("opening_crawl")
    private String openingCrawl;

    private String director;
    private String producer;

    @JsonProperty("release_date")
    private String releaseDate;

    private List<String> characters;
    private List<String> planets;
    private List<String> starships;
    private List<String> vehicles;
    private List<String> species;

    private String url;

    // =========================================================
    // Getters & Setters
    // =========================================================

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getEpisodeId() { return episodeId; }
    public void setEpisodeId(Integer episodeId) { this.episodeId = episodeId; }

    public String getOpeningCrawl() { return openingCrawl; }
    public void setOpeningCrawl(String openingCrawl) { this.openingCrawl = openingCrawl; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public String getProducer() { return producer; }
    public void setProducer(String producer) { this.producer = producer; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public List<String> getCharacters() { return characters; }
    public void setCharacters(List<String> characters) { this.characters = characters; }

    public List<String> getPlanets() { return planets; }
    public void setPlanets(List<String> planets) { this.planets = planets; }

    public List<String> getStarships() { return starships; }
    public void setStarships(List<String> starships) { this.starships = starships; }

    public List<String> getVehicles() { return vehicles; }
    public void setVehicles(List<String> vehicles) { this.vehicles = vehicles; }

    public List<String> getSpecies() { return species; }
    public void setSpecies(List<String> species) { this.species = species; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}