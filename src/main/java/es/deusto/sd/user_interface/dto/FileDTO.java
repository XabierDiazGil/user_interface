package es.deusto.sd.user_interface.dto;

public class FileDTO {
    private Integer id;
    private String path;
    private int result; // -1 no analizado, 0 integridad, 1 veridico

    public FileDTO() {
        this.result = -1;
    }

    public FileDTO(String path) {
        this.path = path;
        this.result = -1;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public int getResult() { return result; }
    public void setResult(int result) { this.result = result; }
}
