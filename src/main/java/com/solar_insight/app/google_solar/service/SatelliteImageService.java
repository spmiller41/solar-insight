package com.solar_insight.app.google_solar.service;

import com.solar_insight.app.GeocodedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class SatelliteImageService {

    @Value("${google.maps.api.key}")
    private String googleMapAPIKey;

    @Autowired
    private RestTemplate restTemplate;

    @Cacheable(value = "satelliteImages", key = "#geocodedLocation.latitude" + "," + "#geocodedLocation.longitude()")
    public byte[] getSatelliteImage(GeocodedLocation geocodedLocation) {
        String url = UriComponentsBuilder.fromHttpUrl("https://maps.googleapis.com/maps/api/staticmap")
                .queryParam("center", geocodedLocation.latitude() + "," + geocodedLocation.longitude())
                .queryParam("zoom", 19)
                .queryParam("size", "400x200") // Maximum allowed size
                .queryParam("scale", "2") // 2x scale for HD image (resulting in 1280x1280 image)
                .queryParam("maptype", "satellite")
                .queryParam("markers", geocodedLocation.latitude() + "," + geocodedLocation.longitude()) // Default Google Maps pin
                .queryParam("key", googleMapAPIKey)
                .toUriString();

        return restTemplate.getForObject(url, byte[].class);
    }

    public File createJpegFromCachedImage(GeocodedLocation geocodedLocation) throws IOException {
        byte[] cachedImage = getSatelliteImage(geocodedLocation);

        if (cachedImage == null) {
            throw new IllegalStateException("No cached image found for the given geocoded location.");
        }

        File imageFile = File.createTempFile("satellite_image_", ".jpg");

        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            fos.write(cachedImage);
        }

        return imageFile;
    }

}
