package kikstrava.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kikstrava.model.GarminActivity;

/**
 * Acces aux activites via l'API personnelle Garmin Connect.
 *
 * Authentification (cf. doc Garmin "Personal API Key") :
 *   1. Echanger la cle API personnelle contre un token OAuth2 :
 *      POST /personalapitoken-service/personal-api/personal-api-key/token
 *      avec l'entete "Authorization: ApiKey &lt;cle&gt;". La reponse est le JWT brut.
 *   2. Appeler les endpoints avec "Authorization: Bearer &lt;token&gt;".
 *
 * Construit sur le meme modele que StravaV3Service.
 */
public class GarminService {

    private static final String BASE_URL = "https://personal.connectapi.garmin.com";
    private static final String TOKEN_URL =
            BASE_URL + "/personalapitoken-service/personal-api/personal-api-key/token";
    private static final String ACTIVITIES_URL =
            BASE_URL + "/activitylist-service/activities/search/activities";

    private final String apiKey;
    private final ObjectMapper objectMapper;

    private String accessToken;

    public GarminService(String apiKey) {
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Echange la cle API personnelle contre un token d'acces OAuth2 (JWT).
     * La reponse du service est le token brut (pas du JSON).
     */
    private String getToken() throws ServiceException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new ServiceException("Cle API Garmin absente (garmin.apikey dans la conf)");
        }

        try {
            URL url = new URL(TOKEN_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "ApiKey " + apiKey);
            // Pas d'Accept application/json : le service renvoie le JWT brut (406 sinon).
            conn.setDoOutput(true);
            // Pas de corps : on envoie un POST vide.
            conn.getOutputStream().close();

            int statusCode = conn.getResponseCode();
            if (statusCode < 200 || statusCode >= 400) {
                throw new ServiceException("Echec obtention token Garmin, HTTP " + statusCode);
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
            conn.disconnect();

            String token = sb.toString().trim();
            // La reponse peut etre le JWT brut, ou un JSON {"access_token":"..."}.
            if (token.startsWith("{")) {
                com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(token);
                if (node.has("access_token")) {
                    token = node.get("access_token").asText();
                }
            }
            if (token.isEmpty()) {
                throw new ServiceException("Token Garmin vide");
            }
            return token;

        } catch (IOException e) {
            throw new ServiceException("Erreur reseau lors de l'obtention du token Garmin : " + e.getMessage());
        }
    }

    /**
     * Assure qu'un token d'acces valide est disponible.
     * L'echange cle->token est peu couteux ; on le refait si besoin.
     */
    private void ensureToken() throws ServiceException {
        if (accessToken == null) {
            accessToken = getToken();
        }
    }

    /**
     * Execute un GET authentifie et renvoie le corps de la reponse.
     */
    private String doGet(String getUrl) throws ServiceException {
        ensureToken();

        try {
            URL url = new URL(getUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            int statusCode = conn.getResponseCode();

            // Token expire/invalide : on retente une fois avec un token neuf.
            if (statusCode == 401 || statusCode == 403) {
                accessToken = getToken();
                conn.disconnect();
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                statusCode = conn.getResponseCode();
            }

            if (statusCode != 200) {
                throw new ServiceException("Echec appel Garmin " + getUrl + " : HTTP " + statusCode);
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
            conn.disconnect();
            return sb.toString();

        } catch (IOException e) {
            throw new ServiceException("Erreur reseau lors de l'appel Garmin : " + e.getMessage());
        }
    }

    /**
     * Recherche les activites entre deux dates (incluses).
     *
     * Pagine automatiquement (l'API Garmin plafonne a ~200 par requete et
     * renvoie une erreur 500 au-dela ; on interroge donc par pages de 100).
     *
     * @param startDate date de debut (peut etre null)
     * @param endDate   date de fin (peut etre null)
     * @param max       nombre maximum d'activites a retourner
     */
    public GarminActivity[] searchActivities(LocalDate startDate, LocalDate endDate, int max)
            throws JsonParseException, JsonMappingException, IOException, ServiceException {

        final int pageSize = 100;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        java.util.List<GarminActivity> all = new java.util.ArrayList<>();
        int start = 0;

        while (all.size() < max) {
            int limit = Math.min(pageSize, max - all.size());

            StringBuilder url = new StringBuilder(ACTIVITIES_URL);
            url.append("?start=").append(start).append("&limit=").append(limit);
            if (startDate != null) {
                url.append("&startDate=").append(fmt.format(startDate));
            }
            if (endDate != null) {
                url.append("&endDate=").append(fmt.format(endDate));
            }

            String json = doGet(url.toString());
            if (json == null || json.isEmpty()) {
                break;
            }
            GarminActivity[] page = objectMapper.readValue(json, GarminActivity[].class);
            if (page.length == 0) {
                break;
            }
            for (GarminActivity a : page) {
                all.add(a);
            }
            if (page.length < limit) {
                break; // derniere page
            }
            start += page.length;
        }

        return all.toArray(new GarminActivity[0]);
    }
}
