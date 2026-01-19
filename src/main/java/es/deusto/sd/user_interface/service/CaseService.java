package es.deusto.sd.user_interface.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import es.deusto.sd.user_interface.dto.*;
import es.deusto.sd.user_interface.entity.CaseItem;
import es.deusto.sd.user_interface.gateway.AuthenticusGateway;

public class CaseService {
    private final AuthenticusGateway gateway;
    
    private final List<CaseItem> cachedCases = new ArrayList<>();

    public CaseService(AuthenticusGateway gateway) {
        this.gateway = gateway;
    }

    public synchronized CaseItem createCase(String token, String userEmail, String name, 
            List<String> imagePaths, String analysisType, LocalDateTime createdAt) {
        
        if (token == null || name == null || analysisType == null) return null;
        
        List<FileDTO> files = new ArrayList<>();
        if (imagePaths != null) {
            for (String path : imagePaths) {
                files.add(new FileDTO(path));
            }
        }
        
        CaseDTO caseDTO = new CaseDTO();
        caseDTO.setName(name);
        caseDTO.setAnalysisType(analysisType);
        caseDTO.setDate(createdAt != null ? createdAt.toLocalDate() : LocalDate.now());
        caseDTO.setFiles(files);
        
        CaseDTO result = gateway.createCase(caseDTO, token);
        
        if (result != null) {
            CaseItem item = new CaseItem(userEmail, name, imagePaths, analysisType, createdAt);
            item.setServerId(result.getId());
            cachedCases.add(item);
            return item;
        }
        return null;
    }

    public synchronized String listCasesForUser(String token) {
        return gateway.myCases(token, 100, null, null);
    }

    public synchronized String listLastCasesForUser(String token, int n) {
        if (n <= 0) return "";
        return gateway.myCases(token, n, null, null);
    }

    public synchronized String listCasesForUserBetween(String token, LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return "";
        return gateway.myCases(token, 100, start.toLocalDate(), end.toLocalDate());
    }

    public synchronized String analyzeCase(String token, String caseName) {
        if (token == null || caseName == null) return "Invalid parameters";
        return gateway.analyzeCase(token, caseName);
    }

    public synchronized boolean deleteCase(String token, String caseName) {
        if (token == null || caseName == null) return false;
        boolean success = gateway.deleteCase(token, caseName);
        if (success) {
            // Remove from local cache
            cachedCases.removeIf(c -> caseName.equals(c.getName()));
        }
        return success;
    }

    public synchronized boolean addImages(String token, Integer caseId, List<String> newPaths) {
        if (token == null || caseId == null || newPaths == null || newPaths.isEmpty()) return false;
        
        List<FileDTO> files = newPaths.stream()
                .map(FileDTO::new)
                .collect(Collectors.toList());
        
        return gateway.addFiles(token, caseId, files);
    }

    public synchronized String getCaseResult(String token, String caseName) {
        return gateway.getCaseResult(token, caseName);
    }

    public synchronized CaseItem getCachedCaseByName(String name) {
        for (CaseItem c : cachedCases) {
            if (name != null && name.equals(c.getName())) {
                return c;
            }
        }
        return null;
    }

    public synchronized void clearCache() {
        cachedCases.clear();
    }
}