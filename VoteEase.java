import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Simple in-memory Online Voting System (GUI)
 * - Register voters & candidates
 * - Cast vote (one vote per voter)
 * - Show results
 *
 * Note: This is for learning/demo only. Not production-ready.
 */
public class OnlineVotingSystem extends JFrame {

    // Data models
    static class Voter {
        String id;
        String name;
        boolean verified; // simple flag for registration verification

        Voter(String id, String name) {
            this.id = id;
            this.name = name;
            this.verified = true;
        }
    }

    static class Candidate {
        String id;
        String name;
        int votes;

        Candidate(String id, String name) {
            this.id = id;
            this.name = name;
            this.votes = 0;
        }
    }

    static class VoteLog {
        String voterId;
        String candidateId;
        Date timestamp;

        VoteLog(String voterId, String candidateId) {
            this.voterId = voterId;
            this.candidateId = candidateId;
            this.timestamp = new Date();
        }

        @Override
        public String toString() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return String.format("%s voted for %s at %s", voterId, candidateId, sdf.format(timestamp));
        }
    }

    // Core data structures
    private final Map<String, Voter> voters = new HashMap<>();
    private final Map<String, Candidate> candidates = new HashMap<>();
    private final Set<String> votedVoterIds = new HashSet<>();
    private final List<VoteLog> auditLog = new ArrayList<>();

    // GUI Components
    private JTextField voterIdField, voterNameField;
    private JTextField candidateIdField, candidateNameField;
    private JComboBox<String> voterVoteCombo, candidateVoteCombo;
    private JTextArea resultsArea, auditLogArea;
    private JLabel statusLabel;

    public OnlineVotingSystem() {
        initializeGUI();
        seedData();
    }

    private void initializeGUI() {
        setTitle("Online Voting System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create main panel with tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Tab 1: Registration
        tabbedPane.addTab("Registration", createRegistrationPanel());
        
        // Tab 2: Voting
        tabbedPane.addTab("Vote", createVotingPanel());
        
        // Tab 3: Results
        tabbedPane.addTab("Results", createResultsPanel());
        
        // Tab 4: Audit Log
        tabbedPane.addTab("Audit Log", createAuditLogPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Status bar
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        statusLabel.setBackground(new Color(240, 240, 240));
        statusLabel.setOpaque(true);
        add(statusLabel, BorderLayout.SOUTH);
        
        setSize(700, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createRegistrationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Voter Registration Section
        JLabel voterTitle = new JLabel("Register Voter");
        voterTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(voterTitle, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Voter ID:"), gbc);
        gbc.gridx = 1;
        voterIdField = new JTextField(20);
        panel.add(voterIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        voterNameField = new JTextField(20);
        panel.add(voterNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton registerVoterBtn = new JButton("Register Voter");
        registerVoterBtn.addActionListener(e -> registerVoter());
        panel.add(registerVoterBtn, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        // Candidate Registration Section
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.insets = new Insets(30, 10, 10, 10);
        JLabel candidateTitle = new JLabel("Register Candidate");
        candidateTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gbc.gridwidth = 2;
        panel.add(candidateTitle, gbc);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Candidate ID:"), gbc);
        gbc.gridx = 1;
        candidateIdField = new JTextField(20);
        panel.add(candidateIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        candidateNameField = new JTextField(20);
        panel.add(candidateNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton registerCandidateBtn = new JButton("Register Candidate");
        registerCandidateBtn.addActionListener(e -> registerCandidate());
        panel.add(registerCandidateBtn, gbc);

        return panel;
    }

    private JPanel createVotingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel title = new JLabel("Cast Your Vote");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Voter ID:"), gbc);
        gbc.gridx = 1;
        voterVoteCombo = new JComboBox<>();
        voterVoteCombo.setEditable(true);
        voterVoteCombo.setPreferredSize(new Dimension(250, 30));
        panel.add(voterVoteCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Candidate:"), gbc);
        gbc.gridx = 1;
        candidateVoteCombo = new JComboBox<>();
        candidateVoteCombo.setPreferredSize(new Dimension(250, 30));
        panel.add(candidateVoteCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton castVoteBtn = new JButton("Cast Vote");
        castVoteBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        castVoteBtn.setPreferredSize(new Dimension(0, 40));
        castVoteBtn.addActionListener(e -> castVote());
        panel.add(castVoteBtn, gbc);

        // Update combos
        updateComboBoxes();

        return panel;
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Election Results");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);

        resultsArea = new JTextArea();
        resultsArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        resultsArea.setEditable(false);
        resultsArea.setBackground(new Color(248, 248, 248));
        JScrollPane scrollPane = new JScrollPane(resultsArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Results"));
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh Results");
        refreshBtn.addActionListener(e -> updateResults());
        panel.add(refreshBtn, BorderLayout.SOUTH);

        updateResults();
        return panel;
    }

    private JPanel createAuditLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Audit Log");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);

        auditLogArea = new JTextArea();
        auditLogArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        auditLogArea.setEditable(false);
        auditLogArea.setBackground(new Color(248, 248, 248));
        JScrollPane scrollPane = new JScrollPane(auditLogArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Vote History"));
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh Log");
        refreshBtn.addActionListener(e -> updateAuditLog());
        panel.add(refreshBtn, BorderLayout.SOUTH);

        updateAuditLog();
        return panel;
    }

    // Business logic methods
    public boolean registerVoter(String voterId, String name) {
        if (voters.containsKey(voterId)) return false;
        voters.put(voterId, new Voter(voterId, name));
        return true;
    }

    public boolean registerCandidate(String candidateId, String name) {
        if (candidates.containsKey(candidateId)) return false;
        candidates.put(candidateId, new Candidate(candidateId, name));
        return true;
    }

    public synchronized String castVote(String voterId, String candidateId) {
        Voter v = voters.get(voterId);
        if (v == null || !v.verified) return "Voter not registered or not verified.";
        if (votedVoterIds.contains(voterId)) return "This voter has already voted.";
        Candidate c = candidates.get(candidateId);
        if (c == null) return "Candidate not found.";
        // record vote
        c.votes += 1;
        votedVoterIds.add(voterId);
        auditLog.add(new VoteLog(voterId, candidateId));
        return "Vote cast successfully.";
    }

    public List<Candidate> getResults() {
        List<Candidate> list = new ArrayList<>(candidates.values());
        list.sort((a, b) -> Integer.compare(b.votes, a.votes)); // descending
        return list;
    }

    public List<Candidate> getWinners() {
        List<Candidate> sorted = getResults();
        if (sorted.isEmpty()) return Collections.emptyList();
        int topVotes = sorted.get(0).votes;
        List<Candidate> winners = new ArrayList<>();
        for (Candidate c : sorted) if (c.votes == topVotes) winners.add(c);
        return winners;
    }

    // GUI action methods
    private void registerVoter() {
        String id = voterIdField.getText().trim();
        String name = voterNameField.getText().trim();
        
        if (id.isEmpty() || name.isEmpty()) {
            showMessage("Please enter both Voter ID and Name.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (registerVoter(id, name)) {
            showMessage("Voter registered successfully!", JOptionPane.INFORMATION_MESSAGE);
            voterIdField.setText("");
            voterNameField.setText("");
            updateComboBoxes();
        } else {
            showMessage("Voter ID already exists!", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registerCandidate() {
        String id = candidateIdField.getText().trim();
        String name = candidateNameField.getText().trim();
        
        if (id.isEmpty() || name.isEmpty()) {
            showMessage("Please enter both Candidate ID and Name.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (registerCandidate(id, name)) {
            showMessage("Candidate registered successfully!", JOptionPane.INFORMATION_MESSAGE);
            candidateIdField.setText("");
            candidateNameField.setText("");
            updateComboBoxes();
            updateResults();
        } else {
            showMessage("Candidate ID already exists!", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void castVote() {
        if (voterVoteCombo.getSelectedItem() == null || candidateVoteCombo.getSelectedItem() == null) {
            showMessage("Please select both Voter and Candidate.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String voterSelection = voterVoteCombo.getSelectedItem().toString().trim();
        String candidateSelection = candidateVoteCombo.getSelectedItem().toString().trim();
        
        // Extract ID from "ID - Name" format
        String voterId = voterSelection.split(" - ")[0].trim();
        String candidateId = candidateSelection.split(" - ")[0].trim();
        
        if (voterId.isEmpty() || candidateId.isEmpty()) {
            showMessage("Please select both Voter and Candidate.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String result = castVote(voterId, candidateId);
        showMessage(result, 
            result.contains("successfully") ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
        
        if (result.contains("successfully")) {
            updateResults();
            updateAuditLog();
            updateComboBoxes();
        }
    }

    private void updateResults() {
        StringBuilder sb = new StringBuilder();
        List<Candidate> results = getResults();
        
        if (results.isEmpty()) {
            sb.append("No candidates registered yet.\n");
        } else {
            sb.append("ELECTION RESULTS\n");
            sb.append("================\n\n");
            
            for (Candidate cand : results) {
                sb.append(String.format("%s (%s): %d vote(s)\n", cand.name, cand.id, cand.votes));
            }
            
            sb.append("\n");
            List<Candidate> winners = getWinners();
            if (winners.size() == 1) {
                sb.append(String.format("WINNER: %s (%s) with %d vote(s)\n", 
                    winners.get(0).name, winners.get(0).id, winners.get(0).votes));
            } else if (winners.size() > 1) {
                sb.append("TIED WINNERS:\n");
                for (Candidate w : winners) {
                    sb.append(String.format("  - %s (%s) with %d vote(s)\n", w.name, w.id, w.votes));
                }
            } else {
                sb.append("No votes cast yet.\n");
            }
        }
        
        resultsArea.setText(sb.toString());
    }

    private void updateAuditLog() {
        StringBuilder sb = new StringBuilder();
        
        if (auditLog.isEmpty()) {
            sb.append("No votes cast yet.\n");
        } else {
            sb.append("VOTE HISTORY\n");
            sb.append("============\n\n");
            for (VoteLog log : auditLog) {
                sb.append(log.toString()).append("\n");
            }
        }
        
        auditLogArea.setText(sb.toString());
    }

    private void updateComboBoxes() {
        // Update voter combo (exclude already voted)
        voterVoteCombo.removeAllItems();
        for (Voter v : voters.values()) {
            if (!votedVoterIds.contains(v.id)) {
                voterVoteCombo.addItem(v.id + " - " + v.name);
            }
        }
        
        // Update candidate combo
        candidateVoteCombo.removeAllItems();
        for (Candidate c : candidates.values()) {
            candidateVoteCombo.addItem(c.id + " - " + c.name);
        }
    }

    private void showMessage(String message, int messageType) {
        statusLabel.setText(message);
        JOptionPane.showMessageDialog(this, message, 
            messageType == JOptionPane.ERROR_MESSAGE ? "Error" : 
            messageType == JOptionPane.WARNING_MESSAGE ? "Warning" : "Success",
            messageType);
    }

    private void seedData() {
        registerVoter("V1", "Alice");
        registerVoter("V2", "Bob");
        registerVoter("V3", "Charlie");
        
        registerCandidate("C1", "Candidate One");
        registerCandidate("C2", "Candidate Two");
        
        updateComboBoxes();
        updateResults();
        statusLabel.setText("System initialized with sample data.");
    }

    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // Use default look and feel if system one is not available
        }
        
        SwingUtilities.invokeLater(() -> {
            new OnlineVotingSystem();
        });
    }
}