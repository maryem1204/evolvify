package tn.esprit.Controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.jxmapviewer.viewer.GeoPosition;
import tn.esprit.Entities.Trajet;
import tn.esprit.Utils.TrajetEventBus;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TrajetMapViewController {

    @FXML
    private WebView webView; // Reference to the WebView from your container FXML

    @FXML
    private void initialize() {
        WebEngine webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        System.out.println("TrajetMapViewController initialized.");

        // Subscribe to trajet events
        TrajetEventBus.trajetEventProperty().addListener((obs, oldTrajet, newTrajet) -> {
            if (newTrajet != null) {
                displayTrajet(newTrajet);
            }
        });
    }

    /**
     * Display the map for a given trajet by geocoding its departure and arrival addresses.
     */
    public void displayTrajet(Trajet trajet) {
        String pointDep = trajet.getPointDep();
        String pointArr = trajet.getPointArr();
        System.out.println("Displaying trajet on map: " + pointDep + " -> " + pointArr);
        new Thread(() -> {
            GeoPosition depPosition = geocodeAddress(pointDep);
            GeoPosition arrPosition = geocodeAddress(pointArr);
            if (depPosition != null && arrPosition != null) {
                Platform.runLater(() -> loadMap(depPosition, arrPosition, pointDep, pointArr));
            } else {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Adresse non trouvée");
                    alert.setHeaderText(null);
                    alert.setContentText("Impossible de géocoder l'une ou les deux adresses du trajet.");
                    alert.showAndWait();
                });
            }
        }).start();
    }

    /**
     * Geocode an address using Nominatim (OpenStreetMap).
     */
    private GeoPosition geocodeAddress(String address) {
        try {
            System.out.println("Geocoding address: " + address);
            String encodedAddress = URLEncoder.encode(address, "UTF-8");
            String url = "https://nominatim.openstreetmap.org/search?format=json&q=" + encodedAddress;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
            if (jsonArray.size() > 0) {
                JsonObject firstResult = jsonArray.get(0).getAsJsonObject();
                double lat = firstResult.get("lat").getAsDouble();
                double lon = firstResult.get("lon").getAsDouble();
                System.out.println("Coordinates for " + address + ": lat=" + lat + ", lon=" + lon);
                return new GeoPosition(lat, lon);
            } else {
                System.out.println("No results found for address: " + address);
                return null;
            }
        } catch (Exception e) {
            System.out.println("Error geocoding address: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads an HTML page into the WebView displaying an OpenLayers map with two markers and a line.
     */
    private void loadMap(GeoPosition depPosition, GeoPosition arrPosition, String depAddress, String arrAddress) {
        if (webView == null) {
            System.out.println("WebView not initialized.");
            return;
        }

        String htmlContent =
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "    <link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/ol@v7.3.0/ol.css\">\n" +
                        "    <script src=\"https://cdn.jsdelivr.net/npm/ol@v7.3.0/dist/ol.js\"></script>\n" +
                        "    <style>\n" +
                        "        html, body { margin: 0; padding: 0; height: 100%; }\n" +
                        "        #map { width: 100%; height: 100vh; }\n" +
                        "        .ol-popup { background-color: white; padding: 10px; border: 1px solid #ccc; max-width: 100px; }\n" +
                        "    </style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <div id=\"map\"></div>\n" +
                        "    <script>\n" +
                        "        var depLon = " + depPosition.getLongitude() + ";\n" +
                        "        var depLat = " + depPosition.getLatitude() + ";\n" +
                        "        var arrLon = " + arrPosition.getLongitude() + ";\n" +
                        "        var arrLat = " + arrPosition.getLatitude() + ";\n" +
                        "        var depCoord = ol.proj.fromLonLat([depLon, depLat]);\n" +
                        "        var arrCoord = ol.proj.fromLonLat([arrLon, arrLat]);\n" +
                        "        var map = new ol.Map({\n" +
                        "            target: 'map',\n" +
                        "            layers: [\n" +
                        "                new ol.layer.Tile({ source: new ol.source.OSM() })\n" +
                        "            ],\n" +
                        "            view: new ol.View({ center: depCoord, zoom: 12 })\n" +
                        "        });\n" +
                        "\n" +
                        "        // Create markers for departure and arrival\n" +
                        "        var depMarker = new ol.Feature({ geometry: new ol.geom.Point(depCoord) });\n" +
                        "        var arrMarker = new ol.Feature({ geometry: new ol.geom.Point(arrCoord) });\n" +
                        "\n" +
                        "        var vectorSource = new ol.source.Vector({ features: [depMarker, arrMarker] });\n" +
                        "\n" +
                        "        var markerStyle = new ol.style.Style({\n" +
                        "            image: new ol.style.Icon({ anchor: [0.5, 1], src: 'https://maps.gstatic.com/mapfiles/api-3/images/spotlight-poi2.png' })\n" +
                        "        });\n" +
                        "\n" +
                        "        depMarker.setStyle(markerStyle);\n" +
                        "        arrMarker.setStyle(markerStyle);\n" +
                        "\n" +
                        "        var vectorLayer = new ol.layer.Vector({ source: vectorSource });\n" +
                        "        map.addLayer(vectorLayer);\n" +
                        "\n" +
                        "        // Draw a line between departure and arrival\n" +
                        "        var line = new ol.Feature({ geometry: new ol.geom.LineString([depCoord, arrCoord]) });\n" +
                        "        var lineStyle = new ol.style.Style({ stroke: new ol.style.Stroke({ color: '#ff0000', width: 2 }) });\n" +
                        "        line.setStyle(lineStyle);\n" +
                        "        vectorSource.addFeature(line);\n" +
                        "    </script>\n" +
                        "</body>\n" +
                        "</html>";

        System.out.println("Loading map for: " + depAddress + " and " + arrAddress);
        webView.getEngine().loadContent(htmlContent);
    }
}
