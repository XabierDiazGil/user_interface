package es.deusto.sd.user_interface;

import es.deusto.sd.user_interface.entity.CaseItem;
import es.deusto.sd.user_interface.entity.UserItem;
import es.deusto.sd.user_interface.gateway.AuthenticusGateway;
import es.deusto.sd.user_interface.gateway.IAuthenticusGateway;
import es.deusto.sd.user_interface.service.CaseService;
import es.deusto.sd.user_interface.service.UserService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SwingApp {
    private final IAuthenticusGateway gateway = new AuthenticusGateway();
    private final UserService userService = new UserService(gateway);
    private final CaseService caseService = new CaseService(gateway);

    private JFrame frame;
    private JLabel userStatus;
    private JTextArea casesTextArea;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new SwingApp().createAndShowGui());
    }

    private void createAndShowGui() {
        frame = new JFrame("Authenticus - User & Case Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setLeftComponent(buildUserPanel());
        split.setRightComponent(buildCasePanel());
        split.setDividerLocation(350);

        frame.getContentPane().add(split);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel buildUserPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(1,1));
        userStatus = new JLabel("No user logged in");
        top.add(userStatus);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        center.add(new JLabel("Sign up"));
        JTextField suName = new JTextField();
        JTextField suEmail = new JTextField();
        JPasswordField suPassword = new JPasswordField();
        JTextField suPhone = new JTextField();
        center.add(new JLabel("Name:")); center.add(suName);
        center.add(new JLabel("Email:")); center.add(suEmail);
        center.add(new JLabel("Password:")); center.add(suPassword);
        center.add(new JLabel("Phone:")); center.add(suPhone);
        JButton btnSignUp = new JButton("Sign Up");
        center.add(btnSignUp);

        center.add(new JSeparator(SwingConstants.HORIZONTAL));

        center.add(new JLabel("Login"));
        JTextField liEmail = new JTextField();
        JPasswordField liPassword = new JPasswordField();
        center.add(new JLabel("Email:")); center.add(liEmail);
        center.add(new JLabel("Password:")); center.add(liPassword);
        JButton btnLogin = new JButton("Login");
        JButton btnLogout = new JButton("Logout");
        JButton btnRemove = new JButton("Remove Account");
        center.add(btnLogin);
        center.add(btnLogout);
        center.add(btnRemove);

        btnSignUp.addActionListener((ActionEvent e) -> {
            String name = suName.getText().trim();
            String email = suEmail.getText().trim();
            String pw = new String(suPassword.getPassword());
            String phone = suPhone.getText().trim();
            boolean ok = userService.signUp(name, email, pw, phone);
            JOptionPane.showMessageDialog(frame, ok ? "User signed up successfully" : "Sign up failed (check credentials or server connection)");
        });

        btnLogin.addActionListener((ActionEvent e) -> {
            String email = liEmail.getText().trim();
            String pw = new String(liPassword.getPassword());
            boolean ok = userService.login(email, pw);
            if (ok) {
                userStatus.setText("Logged in: " + email);
                refreshCasesList();
            }
            JOptionPane.showMessageDialog(frame, ok ? "Login successful" : "Login failed (check credentials or server connection)");
        });

        btnLogout.addActionListener((ActionEvent e) -> {
            userService.logout();
            userStatus.setText("No user logged in");
            if (casesTextArea != null) casesTextArea.setText("");
            caseService.clearCache();
            JOptionPane.showMessageDialog(frame, "Logged out");
        });

        btnRemove.addActionListener((ActionEvent e) -> {
            boolean ok = userService.removeCurrentUser();
            if (ok) {
                userStatus.setText("No user logged in");
                if (casesTextArea != null) casesTextArea.setText("");
                caseService.clearCache();
            }
            JOptionPane.showMessageDialog(frame, ok ? "Account removed" : "Failed to remove account (no user logged in or server error)");
        });

        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(center), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildCasePanel() {
        JPanel p = new JPanel(new BorderLayout());

        JPanel createPanel = new JPanel();
        createPanel.setLayout(new BoxLayout(createPanel, BoxLayout.Y_AXIS));
        createPanel.add(new JLabel("Create Case"));
        JTextField caseName = new JTextField();
        createPanel.add(new JLabel("Case name:")); createPanel.add(caseName);

        DefaultListModel<String> imagesModel = new DefaultListModel<>();
        JList<String> imagesList = new JList<>(imagesModel);
        JButton btnChoose = new JButton("Choose Images...");
        createPanel.add(btnChoose);
        createPanel.add(new JScrollPane(imagesList));

        String[] analysisTypes = new String[]{"Veracidad", "Integridad"};
        JComboBox<String> analysisCombo = new JComboBox<>(analysisTypes);
        createPanel.add(new JLabel("Analysis type:")); createPanel.add(analysisCombo);
        JButton btnCreateCase = new JButton("Create Case");
        createPanel.add(btnCreateCase);

        btnChoose.addActionListener((ActionEvent e) -> {
            JFileChooser fc = new JFileChooser();
            fc.setMultiSelectionEnabled(true);
            fc.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "jpeg", "bmp"));
            int ret = fc.showOpenDialog(frame);
            if (ret == JFileChooser.APPROVE_OPTION) {
                File[] files = fc.getSelectedFiles();
                imagesModel.clear();
                for (File f: files) imagesModel.addElement(f.getAbsolutePath());
            }
        });

        btnCreateCase.addActionListener((ActionEvent e) -> {
            UserItem u = userService.getCurrentUser();
            String token = userService.getCurrentToken();
            if (u == null || token == null) { 
                JOptionPane.showMessageDialog(frame, "Please log in first"); 
                return; 
            }
            String name = caseName.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter a case name");
                return;
            }
            List<String> imgs = new ArrayList<>();
            for (int i=0; i<imagesModel.size(); i++) imgs.add(imagesModel.get(i));
            String analysis = (String)analysisCombo.getSelectedItem();
            LocalDateTime createdAt = LocalDateTime.now();
            
            CaseItem c = caseService.createCase(token, u.getEmail(), name, imgs, analysis, createdAt);
            if (c != null) {
                // Auto-analyze the case
                String analyzeResult = caseService.analyzeCase(token, name);
                JOptionPane.showMessageDialog(frame, "Case created and analyzed:\n" + analyzeResult);
                refreshCasesList();
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to create case (check server connection)");
            }
        });

        // Cases display panel
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.add(new JLabel("User Cases:"), BorderLayout.NORTH);
        
        casesTextArea = new JTextArea();
        casesTextArea.setEditable(false);
        listPanel.add(new JScrollPane(casesTextArea), BorderLayout.CENTER);
        
        // Controls for listing (last N or date range)
        JPanel listControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblLastN = new JLabel("Last N:");
        JTextField txtLastN = new JTextField("5", 4);
        JButton btnListLastN = new JButton("List Last N");
        JLabel lblStart = new JLabel("Start (yyyy-MM-dd):");
        JTextField txtStart = new JTextField(10);
        JLabel lblEnd = new JLabel("End (yyyy-MM-dd):");
        JTextField txtEnd = new JTextField(10);
        JButton btnListByDateRange = new JButton("List By Date Range");
        listControls.add(lblLastN); listControls.add(txtLastN); listControls.add(btnListLastN);
        listControls.add(new JSeparator(SwingConstants.VERTICAL));
        listControls.add(lblStart); listControls.add(txtStart); 
        listControls.add(lblEnd); listControls.add(txtEnd); 
        listControls.add(btnListByDateRange);
        
        // Case action buttons
        JPanel actionPanel = new JPanel(new FlowLayout());
        JTextField caseNameField = new JTextField(15);
        JButton btnShowResult = new JButton("Show Result");
        JButton btnDeleteCase = new JButton("Delete Case");
        JButton btnAnalyze = new JButton("Analyze");
        actionPanel.add(new JLabel("Case name:"));
        actionPanel.add(caseNameField);
        actionPanel.add(btnShowResult);
        actionPanel.add(btnDeleteCase);
        actionPanel.add(btnAnalyze);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(listControls, BorderLayout.NORTH);
        bottomPanel.add(actionPanel, BorderLayout.SOUTH);
        listPanel.add(bottomPanel, BorderLayout.SOUTH);

        btnListLastN.addActionListener((ActionEvent e) -> {
            String token = userService.getCurrentToken();
            if (token == null) { 
                JOptionPane.showMessageDialog(frame, "Please log in first"); 
                return; 
            }
            String s = txtLastN.getText().trim();
            int n = 5;
            try { n = Integer.parseInt(s); } catch (Exception ex) { 
                JOptionPane.showMessageDialog(frame, "Invalid number: " + s); 
                return; 
            }
            String result = caseService.listLastCasesForUser(token, n);
            casesTextArea.setText(result);
        });

        btnListByDateRange.addActionListener((ActionEvent e) -> {
            String token = userService.getCurrentToken();
            if (token == null) { 
                JOptionPane.showMessageDialog(frame, "Please log in first"); 
                return; 
            }
            String sStart = txtStart.getText().trim();
            String sEnd = txtEnd.getText().trim();
            if (sStart.isEmpty() || sEnd.isEmpty()) { 
                JOptionPane.showMessageDialog(frame, "Start and End dates are required"); 
                return; 
            }
            java.time.LocalDate startDate;
            java.time.LocalDate endDate;
            try {
                startDate = java.time.LocalDate.parse(sStart);
                endDate = java.time.LocalDate.parse(sEnd);
            } catch (Exception ex) { 
                JOptionPane.showMessageDialog(frame, "Invalid date format. Use yyyy-MM-dd"); 
                return; 
            }
            java.time.LocalDateTime start = startDate.atStartOfDay();
            java.time.LocalDateTime end = endDate.atTime(23,59,59);
            String result = caseService.listCasesForUserBetween(token, start, end);
            casesTextArea.setText(result);
        });

        btnShowResult.addActionListener((ActionEvent e) -> {
            String token = userService.getCurrentToken();
            String cName = caseNameField.getText().trim();
            if (token == null) { 
                JOptionPane.showMessageDialog(frame, "Please log in first"); 
                return; 
            }
            if (cName.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Enter case name");
                return;
            }
            String result = caseService.getCaseResult(token, cName);
            if (result.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Case not found or no results");
            } else {
                JTextArea ta = new JTextArea(result);
                ta.setEditable(false);
                JOptionPane.showMessageDialog(frame, new JScrollPane(ta), "Case Result", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnDeleteCase.addActionListener((ActionEvent e) -> {
            String token = userService.getCurrentToken();
            String cName = caseNameField.getText().trim();
            if (token == null) { 
                JOptionPane.showMessageDialog(frame, "Please log in first"); 
                return; 
            }
            if (cName.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Enter case name");
                return;
            }
            int option = JOptionPane.showConfirmDialog(frame, "Delete case '" + cName + "'?", "Confirm delete", JOptionPane.YES_NO_OPTION);
            if (option != JOptionPane.YES_OPTION) return;
            boolean ok = caseService.deleteCase(token, cName);
            if (ok) {
                JOptionPane.showMessageDialog(frame, "Case deleted");
                refreshCasesList();
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to delete case");
            }
        });

        btnAnalyze.addActionListener((ActionEvent e) -> {
            String token = userService.getCurrentToken();
            String cName = caseNameField.getText().trim();
            if (token == null) { 
                JOptionPane.showMessageDialog(frame, "Please log in first"); 
                return; 
            }
            if (cName.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Enter case name");
                return;
            }
            String result = caseService.analyzeCase(token, cName);
            JOptionPane.showMessageDialog(frame, result);
            refreshCasesList();
        });

        p.add(createPanel, BorderLayout.NORTH);
        p.add(listPanel, BorderLayout.CENTER);
        return p;
    }

    private void refreshCasesList() {
        String token = userService.getCurrentToken();
        if (token == null) return;
        String result = caseService.listLastCasesForUser(token, 5);
        if (casesTextArea != null) {
            casesTextArea.setText(result);
        }
    }
}
