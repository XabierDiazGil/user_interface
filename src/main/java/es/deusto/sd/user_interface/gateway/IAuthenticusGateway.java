package es.deusto.sd.user_interface.gateway;

import es.deusto.sd.user_interface.dto.*;
import java.time.LocalDate;
import java.util.List;

public interface IAuthenticusGateway {
    UserDTO signup(UserDTO user);
    AuthTokenDTO login(LoginDTO credentials);
    boolean logout(AuthTokenDTO token);
    boolean removeUser(Integer userId);
    CaseDTO createCase(CaseDTO caseData, String token);
    String myCases(String token, int numberCases, LocalDate fechaInicio, LocalDate fechaFin);
    String getCaseResult(String token, String caseName);
    boolean deleteCase(String token, String caseName);
    boolean addFiles(String token, Integer caseId, List<FileDTO> files);
    String analyzeCase(String token, String caseName);
}
