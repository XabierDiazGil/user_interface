package es.deusto.sd.user_interface.service;

import java.time.LocalDateTime;
import java.util.*;

import es.deusto.sd.user_interface.entity.CaseItem;

public class CaseService {
    private final List<CaseItem> cases = new ArrayList<>();

    public synchronized CaseItem createCase(String userEmail, String name, List<String> imagePaths, String analysisType, LocalDateTime createdAt) {
        CaseItem c = new CaseItem(userEmail, name, imagePaths, analysisType, createdAt);
        cases.add(c);
        return c;
    }

    public synchronized List<CaseItem> listCasesForUser(String userEmail) {
        List<CaseItem> res = new ArrayList<>();
        for (CaseItem c: cases) if (c.getUserEmail().equals(userEmail)) res.add(c);
        return res;
    }

    public synchronized List<CaseItem> listLastCasesForUser(String userEmail, int n) {
        if (n <= 0) return new ArrayList<>();
        List<CaseItem> all = listCasesForUser(userEmail);
        all.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())); // newest first
        if (all.size() <= n) return all;
        return new ArrayList<>(all.subList(0, n));
    }

    public synchronized List<CaseItem> listCasesForUserBetween(String userEmail, java.time.LocalDateTime start, java.time.LocalDateTime end) {
        List<CaseItem> res = new ArrayList<>();
        if (start == null || end == null) return res;
        for (CaseItem c: cases) {
            if (!c.getUserEmail().equals(userEmail)) continue;
            java.time.LocalDateTime d = c.getCreatedAt();
            if (d == null) continue;
            if ((d.isEqual(start) || d.isAfter(start)) && (d.isEqual(end) || d.isBefore(end))) res.add(c);
        }
        res.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())); // newest first
        return res;
    }

    public synchronized void analyzeCase(CaseItem c) {
        // Simulate analysis work
        List<String> imgs = c.getImagePaths() == null ? new ArrayList<>() : c.getImagePaths();
        for (String img : imgs) {
            // score between 0 and 1
            double score = Math.random();
            c.setImageScore(img, score);
        }
        String r = "Analysis result for '" + c.getName() + "': type=" + c.getAnalysisType()
            + ", images=" + imgs.size();
        c.setResult(r);
    }

    public synchronized boolean deleteCase(CaseItem c) {
        if (c == null) return false;
        return cases.removeIf(existing -> existing.getId() == c.getId());
    }

    public synchronized boolean addImages(CaseItem c, List<String> newPaths) {
        if (c == null || newPaths == null || newPaths.isEmpty()) return false;
        c.addImagePaths(newPaths);
        // Optionally re-run analysis to include the new images
        analyzeCase(c);
        return true;
    }
}