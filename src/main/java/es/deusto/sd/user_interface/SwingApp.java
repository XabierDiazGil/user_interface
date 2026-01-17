package es.deusto.sd.user_interface;

import es.deusto.sd.user_interface.entity.CaseItem;
import es.deusto.sd.user_interface.entity.UserItem;
import es.deusto.sd.user_interface.service.CaseService;
import es.deusto.sd.user_interface.service.UserService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SwingApp {
    private final UserService userManager = new UserService();
    private final CaseService caseManager = new CaseService();

    private JFrame frame;
    private JLabel userStatus;
    private DefaultListModel<CaseItem> casesListModel = new DefaultListModel<>();

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new SwingApp().createAndShowGui());
    }

    private void createAndShowGui() {
        frame = new JFrame("Sample Swing Interface - User & Case Management");
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
            boolean ok = userManager.signUp(name, email, pw, phone);
            JOptionPane.showMessageDialog(frame, ok ? "User signed up" : "Sign up failed (email exists or invalid)");
        });

        btnLogin.addActionListener((ActionEvent e) -> {
            String email = liEmail.getText().trim();
            String pw = new String(liPassword.getPassword());
            boolean ok = userManager.login(email, pw);
            if (ok) {
                userStatus.setText("Logged in: " + userManager.getCurrentUser().getEmail());
                refreshCasesList();
            }
            JOptionPane.showMessageDialog(frame, ok ? "Login successful" : "Login failed");
        });

        btnLogout.addActionListener((ActionEvent e) -> {
            userManager.logout();
            userStatus.setText("No user logged in");
            casesListModel.clear();
            JOptionPane.showMessageDialog(frame, "Logged out");
        });

        btnRemove.addActionListener((ActionEvent e) -> {
            boolean ok = userManager.removeCurrentUser();
            if (ok) {
                userStatus.setText("No user logged in");
                casesListModel.clear();
            }
            JOptionPane.showMessageDialog(frame, ok ? "Account removed" : "No user logged in");
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

        String[] analysisTypes = new String[]{"Type A", "Type B", "Type C"};
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
            UserItem u = userManager.getCurrentUser();
            if (u == null) { JOptionPane.showMessageDialog(frame, "Please log in first"); return; }
            String name = caseName.getText().trim();
            List<String> imgs = new ArrayList<>();
            for (int i=0;i<imagesModel.size();i++) imgs.add(imagesModel.get(i));
            String analysis = (String)analysisCombo.getSelectedItem();
            LocalDateTime createdAt = LocalDateTime.now();
            CaseItem c = caseManager.createCase(u.getEmail(), name, imgs, analysis, createdAt);
            caseManager.analyzeCase(c);
            JOptionPane.showMessageDialog(frame, "Case created and analyzed");
        });

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.add(new JLabel("User Cases:"), BorderLayout.NORTH);
        JList<CaseItem> casesList = new JList<>(casesListModel);
        listPanel.add(new JScrollPane(casesList), BorderLayout.CENTER);
        
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
        listControls.add(lblStart); listControls.add(txtStart); listControls.add(lblEnd); listControls.add(txtEnd); listControls.add(btnListByDateRange);
        listPanel.add(listControls, BorderLayout.NORTH);
        JPanel listButtons = new JPanel();
        JButton btnShowResult = new JButton("Show Result");
        JButton btnDeleteCase = new JButton("Delete Case");
        JButton btnAddImagesToCase = new JButton("Add Images");
        listButtons.add(btnShowResult); listButtons.add(btnDeleteCase); listButtons.add(btnAddImagesToCase);
        listPanel.add(listButtons, BorderLayout.SOUTH);

        btnShowResult.addActionListener((ActionEvent e) -> {
            CaseItem sel = casesList.getSelectedValue();
            if (sel == null) { JOptionPane.showMessageDialog(frame, "Select a case"); return; }
            StringBuilder sb = new StringBuilder();
            sb.append("Case: ").append(sel.getName()).append("\n");
            LocalDateTime dt = sel.getCreatedAt();
            String fmt = dt == null ? "n/a" : dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            sb.append("Created: ").append(fmt).append("\n");
            sb.append("Analysis: ").append(sel.getAnalysisType()).append("\n");
            sb.append("Images: \n");
            for (String pth: sel.getImagePaths()) sb.append(" - ").append(pth).append("\n");
            sb.append("\nResult (per image):\n");
            Map<String, Double> scores = sel.getImageScores();
            for (String imgPath : sel.getImagePaths()) {
                Double s = scores.get(imgPath);
                String sStr = (s == null || s.isNaN()) ? "n/a" : String.format("%.3f", s);
                sb.append(" - ").append(imgPath).append(" -> ").append(sStr).append("\n");
            }
            // Average score removed; per-image scores are shown above.
            JTextArea ta = new JTextArea(sb.toString());
            ta.setEditable(false);
            JOptionPane.showMessageDialog(frame, new JScrollPane(ta), "Case Result", JOptionPane.INFORMATION_MESSAGE);
        });

        // Refresh button removed; list auto-updates on relevant actions

        btnDeleteCase.addActionListener((ActionEvent e) -> {
            CaseItem sel = casesList.getSelectedValue();
            if (sel == null) { JOptionPane.showMessageDialog(frame, "Select a case first"); return; }
            int option = JOptionPane.showConfirmDialog(frame, "Delete case '" + sel.getName() + "'?", "Confirm delete", JOptionPane.YES_NO_OPTION);
            if (option != JOptionPane.YES_OPTION) return;
            boolean ok = caseManager.deleteCase(sel);
            if (ok) {
                JOptionPane.showMessageDialog(frame, "Case deleted");
                refreshCasesList();
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to delete case");
            }
        });

        btnAddImagesToCase.addActionListener((ActionEvent e) -> {
            CaseItem sel = casesList.getSelectedValue();
            if (sel == null) { JOptionPane.showMessageDialog(frame, "Select a case first"); return; }
            JFileChooser fc = new JFileChooser();
            fc.setMultiSelectionEnabled(true);
            fc.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "jpeg", "bmp"));
            int ret = fc.showOpenDialog(frame);
            if (ret != JFileChooser.APPROVE_OPTION) return;
            File[] files = fc.getSelectedFiles();
            if (files == null || files.length == 0) return;
            List<String> newPaths = new ArrayList<>();
            for (File f : files) newPaths.add(f.getAbsolutePath());
            int opt = JOptionPane.showConfirmDialog(frame, "Add " + newPaths.size() + " images to case '" + sel.getName() + "'?", "Confirm add", JOptionPane.YES_NO_OPTION);
            if (opt != JOptionPane.YES_OPTION) return;
            boolean ok = caseManager.addImages(sel, newPaths);
            if (ok) {
                JOptionPane.showMessageDialog(frame, "Images added and case re-analyzed");
                refreshCasesList();
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to add images");
            }
        });

        btnListLastN.addActionListener((ActionEvent e) -> {
            UserItem u = userManager.getCurrentUser();
            if (u == null) { JOptionPane.showMessageDialog(frame, "Please log in first"); return; }
            String s = txtLastN.getText().trim();
            int n = 5;
            try { n = Integer.parseInt(s); } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "Invalid number: " + s); return; }
            casesListModel.clear();
            List<CaseItem> res = caseManager.listLastCasesForUser(u.getEmail(), n);
            for (CaseItem c : res) casesListModel.addElement(c);
        });

        btnListByDateRange.addActionListener((ActionEvent e) -> {
            UserItem u = userManager.getCurrentUser();
            if (u == null) { JOptionPane.showMessageDialog(frame, "Please log in first"); return; }
            String sStart = txtStart.getText().trim();
            String sEnd = txtEnd.getText().trim();
            if (sStart.isEmpty() || sEnd.isEmpty()) { JOptionPane.showMessageDialog(frame, "Start and End dates are required"); return; }
            java.time.LocalDate startDate;
            java.time.LocalDate endDate;
            try {
                startDate = java.time.LocalDate.parse(sStart);
                endDate = java.time.LocalDate.parse(sEnd);
            } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "Invalid date format. Use yyyy-MM-dd"); return; }
            java.time.LocalDateTime start = startDate.atStartOfDay();
            java.time.LocalDateTime end = endDate.atTime(23,59,59);
            List<CaseItem> res = caseManager.listCasesForUserBetween(u.getEmail(), start, end);
            casesListModel.clear();
            for (CaseItem c : res) casesListModel.addElement(c);
        });

        p.add(createPanel, BorderLayout.NORTH);
        p.add(listPanel, BorderLayout.CENTER);
        return p;
    }

    private void refreshCasesList() {
        casesListModel.clear();
        UserItem u = userManager.getCurrentUser();
        if (u == null) return;
        List<CaseItem> cs = caseManager.listLastCasesForUser(u.getEmail(), 5); // default last 5
        for (CaseItem c: cs) casesListModel.addElement(c);
    }
}
