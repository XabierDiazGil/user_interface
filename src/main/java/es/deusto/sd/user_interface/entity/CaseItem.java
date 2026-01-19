package es.deusto.sd.user_interface.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CaseItem {
    private static final AtomicInteger ID_GEN = new AtomicInteger(1);

    private final int id;
    private Integer serverId; // Server-assigned ID
    private final String userEmail;
    private final String name;
    private final List<String> imagePaths;
    private final Map<String, Double> imageScores;
    private final String analysisType;
    private final LocalDateTime createdAt;
    private String result;

    public CaseItem(String userEmail, String name, List<String> imagePaths, String analysisType, LocalDateTime createdAt) {
        this.id = ID_GEN.getAndIncrement();
        this.serverId = null;
        this.userEmail = userEmail;
        this.name = name;
        this.imagePaths = imagePaths == null ? new ArrayList<>() : new ArrayList<>(imagePaths);
        this.imageScores = new HashMap<>();
        // initialize scores to NaN to indicate not analyzed yet
        for (String p : this.imagePaths) this.imageScores.put(p, Double.NaN);
        this.analysisType = analysisType;
        this.result = "(not analyzed)";
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    public int getId() { return id; }
    public Integer getServerId() { return serverId; }
    public void setServerId(Integer serverId) { this.serverId = serverId; }
    public String getUserEmail() { return userEmail; }
    public String getName() { return name; }
    public List<String> getImagePaths() { return java.util.Collections.unmodifiableList(imagePaths); }
    public Map<String, Double> getImageScores() { return java.util.Collections.unmodifiableMap(imageScores); }
    public Double getImageScore(String path) { return imageScores.get(path); }
    public String getAnalysisType() { return analysisType; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public void addImagePath(String path) {
        if (path == null) return;
        imagePaths.add(path);
        imageScores.put(path, Double.NaN);
    }
    public void addImagePaths(List<String> paths) {
        if (paths == null || paths.isEmpty()) return;
        imagePaths.addAll(paths);
        for (String p : paths) imageScores.put(p, Double.NaN);
    }
    public void setImageScore(String path, double score) {
        if (path == null) return;
        if (!imageScores.containsKey(path)) imageScores.put(path, score);
        else imageScores.put(path, score);
    }
    // getAverageScore removed - per-image scores are stored in imageScores; compute client-side if needed
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        String date = createdAt == null ? "" : createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        return id + ": " + name + " (" + analysisType + ") [" + date + "]";
    }
}