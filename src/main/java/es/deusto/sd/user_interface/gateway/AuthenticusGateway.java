package es.deusto.sd.user_interface.gateway;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import es.deusto.sd.user_interface.dto.*;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

public class AuthenticusGateway implements IAuthenticusGateway {
    private static final String API_URL = "http://localhost:8080/api";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AuthenticusGateway() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true); 
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    // ==================== Operaciones de usuario ====================

    public UserDTO signup(UserDTO user) {
        try {
            String jsonContent = objectMapper.writeValueAsString(user);
            System.out.println("Signup request: " + jsonContent);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/users/signup"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonContent))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Signup response status: " + response.statusCode());
            System.out.println("Signup response body: " + response.body());
            
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), UserDTO.class);
            }
            System.err.println("La creación de usuario ha fallado con estado: " + response.statusCode() + " - " + response.body());
            return null;
        } catch (Exception e) {
            System.err.println("Error registrando un usuario: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public AuthTokenDTO login(LoginDTO credentials) {
        try {
            String jsonContent = objectMapper.writeValueAsString(credentials);
            System.out.println("Login request: " + jsonContent);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/users/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonContent))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Login response status: " + response.statusCode());
            System.out.println("Login response body: " + response.body());
            
            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), AuthTokenDTO.class);
            }
            // 401 = Credenciales erroneas, 409 = El usuario está logeado
            System.err.println("El login ha fallado con error: " + response.statusCode() + " - " + response.body());
            return null;
        } catch (Exception e) {
            System.err.println("Error en login: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean logout(AuthTokenDTO token) {
        try {
            String jsonContent = objectMapper.writeValueAsString(token);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/users/logout"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonContent))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("Error en logout: " + e.getMessage());
            return false;
        }
    }

    public boolean removeUser(Integer userId) {
        if (userId == null) return false;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/users/remove/" + userId))
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 || response.statusCode() == 204;
        } catch (Exception e) {
            System.err.println("Error al intentar eliminar un usuario: " + e.getMessage());
            return false;
        }
    }

    // ==================== OPERACIONES DE CASOS ====================

    public CaseDTO createCase(CaseDTO caseData, String token) {
        if (token == null || token.isEmpty()) return null;
        try {
            String jsonContent = objectMapper.writeValueAsString(caseData);
            String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/cases/create?token=" + encodedToken))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonContent))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), CaseDTO.class);
            }
            System.err.println("La creación de caso ha fallado con estado: " + response.statusCode() + " - " + response.body());
            return null;
        } catch (Exception e) {
            System.err.println("Error a la hora de crear un caso: " + e.getMessage());
            return null;
        }
    }

    public String myCases(String token, int numberCases, LocalDate fechaInicio, LocalDate fechaFin) {
        if (token == null || token.isEmpty()) return "No token provided";
        try {
            StringBuilder uriBuilder = new StringBuilder(API_URL + "/cases/mycases?");
            uriBuilder.append("token=").append(URLEncoder.encode(token, StandardCharsets.UTF_8));
            uriBuilder.append("&numberCases=").append(numberCases);
            if (fechaInicio != null) {
                uriBuilder.append("&fechaInicio=").append(fechaInicio);
            }
            if (fechaFin != null) {
                uriBuilder.append("&fechaFin=").append(fechaFin);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uriBuilder.toString()))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            }
            return "Error haciendo fetch a los casos: " + response.statusCode();
        } catch (Exception e) {
            System.err.println("Error en mis casos: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    public String getCaseResult(String token, String caseName) {
        if (token == null || caseName == null) return "";
        try {
            String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
            String encodedName = URLEncoder.encode(caseName, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/cases/result?token=" + encodedToken + "&caseName=" + encodedName))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            }
            return "";
        } catch (Exception e) {
            System.err.println("Error intentando conseguir el resultado: " + e.getMessage());
            return "";
        }
    }

    public boolean deleteCase(String token, String caseName) {
        if (token == null || caseName == null) return false;
        try {
            String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
            String encodedName = URLEncoder.encode(caseName, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/cases/delete?token=" + encodedToken + "&caseName=" + encodedName))
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 || response.statusCode() == 204;
        } catch (Exception e) {
            System.err.println("Error eliminando un caso: " + e.getMessage());
            return false;
        }
    }

    public boolean addFiles(String token, Integer caseId, List<FileDTO> files) {
        if (token == null || caseId == null || files == null) return false;
        try {
            String jsonContent = objectMapper.writeValueAsString(files);
            String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/cases/addfiles?token=" + encodedToken + "&caseID=" + caseId))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonContent))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("Error agregando archivos a un caso: " + e.getMessage());
            return false;
        }
    }

    public String analyzeCase(String token, String caseName) {
        if (token == null || caseName == null) return "Parámetros inválidos";
        try {
            String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
            String encodedName = URLEncoder.encode(caseName, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/cases/analyze?token=" + encodedToken + "&caseName=" + encodedName))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            System.err.println("Error analizando un caso: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
}
