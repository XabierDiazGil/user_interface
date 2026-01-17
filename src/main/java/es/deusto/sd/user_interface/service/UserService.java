package es.deusto.sd.user_interface.service;

import java.util.*;

import es.deusto.sd.user_interface.entity.UserItem;

public class UserService {
    private final Map<String, UserItem> users = new HashMap<>();
    private UserItem currentUser = null;

    public synchronized boolean signUp(String name, String email, String password, String phone) {
        if (email == null || email.isEmpty() || users.containsKey(email)) return false;
        UserItem u = new UserItem(name, email, password, phone);
        users.put(email, u);
        return true;
    }

    public synchronized boolean login(String email, String password) {
        UserItem u = users.get(email);
        if (u == null) return false;
        if (!u.getPassword().equals(password)) return false;
        currentUser = u;
        return true;
    }

    public synchronized void logout() { currentUser = null; }

    public synchronized boolean removeCurrentUser() {
        if (currentUser == null) return false;
        users.remove(currentUser.getEmail());
        currentUser = null;
        return true;
    }

    public synchronized UserItem getCurrentUser() { return currentUser; }

    public synchronized List<UserItem> listUsers() { return new ArrayList<>(users.values()); }
}