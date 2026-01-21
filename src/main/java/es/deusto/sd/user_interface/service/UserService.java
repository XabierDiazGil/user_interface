package es.deusto.sd.user_interface.service;

import es.deusto.sd.user_interface.dto.*;
import es.deusto.sd.user_interface.entity.UserItem;
import es.deusto.sd.user_interface.gateway.IAuthenticusGateway;

public class UserService {
    private final IAuthenticusGateway gateway;
    private UserItem currentUser = null;
    private String currentToken = null;
    private Integer currentUserId = null;

    public UserService(IAuthenticusGateway gateway) {
        this.gateway = gateway;
    }

    public synchronized boolean signUp(String name, String email, String password, String phone) {
        if (email == null || email.isEmpty()) return false;
        
        UserDTO userDTO = new UserDTO(name, email, password, phone);
        UserDTO result = gateway.signup(userDTO);
        
        if (result != null && result.getId() != null) {
            return true;
        }
        return false;
    }

    public synchronized boolean login(String email, String password) {
        if (email == null || password == null) return false;
        
        LoginDTO credentials = new LoginDTO(email, password);
        AuthTokenDTO tokenResult = gateway.login(credentials);
        
        if (tokenResult != null && tokenResult.getToken() != null) {
            currentToken = tokenResult.getToken();
            currentUserId = tokenResult.getUserId();
            currentUser = new UserItem("", email, password, "");
            return true;
        }
        return false;
    }


    public synchronized void logout() {
        if (currentToken != null) {
            AuthTokenDTO token = new AuthTokenDTO(currentToken);
            gateway.logout(token);
        }
        currentUser = null;
        currentToken = null;
        currentUserId = null;
    }

    public synchronized boolean removeCurrentUser() {
        if (currentUser == null || currentUserId == null) return false;
        
        boolean success = gateway.removeUser(currentUserId);
        if (success) {
            currentUser = null;
            currentToken = null;
            currentUserId = null;
        }
        return success;
    }

    public synchronized UserItem getCurrentUser() { 
        return currentUser; 
    }

    public synchronized String getCurrentToken() { 
        return currentToken; 
    }

    public synchronized Integer getCurrentUserId() {
        return currentUserId;
    }

    public synchronized void setCurrentUserId(Integer userId) {
        this.currentUserId = userId;
    }
}