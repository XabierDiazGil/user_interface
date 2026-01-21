package es.deusto.sd.user_interface.dto;

import java.time.LocalDate;
import java.util.List;

public class CaseDTO {
    private Integer id;
    private String name;
    private String analysisType;
    private LocalDate date;
    private List<FileDTO> files;
    private Integer userId;

    public CaseDTO() {}

    public CaseDTO(String name, String analysisType, LocalDate date, List<FileDTO> files, Integer userId) {
        this.name = name;
        this.analysisType = analysisType;
        this.date = date;
        this.files = files;
        this.userId = userId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(String analysisType) {
        this.analysisType = analysisType;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<FileDTO> getFiles() {
        return files;
    }

    public void setFiles(List<FileDTO> files) {
        this.files = files;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

}
