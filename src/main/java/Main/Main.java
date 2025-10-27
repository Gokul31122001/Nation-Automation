package Main;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
public class Main  extends JFrame{
	


				
						


						    private JTextField emailField;
						    private JPasswordField passwordField;
						    private JComboBox<String> practiceDropdown;
						    private JLabel pdfFileLabel, excelFileLabel;
						    private JRadioButton eobButton, eraButton;
						    private JTextArea logArea;
						    private File selectedPDF, selectedExcel;
						    private String automationType = "";

						    public Main() {
						        // Initialize Frame
						        setTitle("Patient ID Automation");
						        setSize(550, 500);
						        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
						        setLayout(new GridLayout(8, 1));

						        // Email Input
						        JPanel emailPanel = new JPanel();
						        emailPanel.add(new JLabel("📧 Email:"));
						        emailField = new JTextField(20);
						        emailPanel.add(emailField);
						        add(emailPanel);

						        // Password Input
						        JPanel passwordPanel = new JPanel();
						        passwordPanel.add(new JLabel("🔑 Password:"));
						        passwordField = new JPasswordField(20);
						        passwordPanel.add(passwordField);
						        add(passwordPanel);

						        // Practice Selection
						        JPanel practicePanel = new JPanel();
						                 practicePanel.add(new JLabel("🏥 Select Practice:"));
						        String[] practices = {
						            "Colon & Rectal Surgeons of Long island", "Dr. Patricia McCormack",
						            "ELIZABETH WEINTRAUB", "EVERYDAYURGENTCARE", "Leonard A. Feitell, M.D LLC",
						            "RASO & COHEN GASTROENTEROLOGY ASSOC", "SWIFT TEST LLC", "Well Urgent care Services"
						        };
						        practiceDropdown = new JComboBox<>(practices);
						        practicePanel.add(practiceDropdown);
						        add(practicePanel);

						        // PDF File Upload
						        JPanel pdfPanel = new JPanel();
						        JButton pdfButton = new JButton("📄 Upload PDF File");
						        pdfFileLabel = new JLabel("No file selected");
						        pdfPanel.add(pdfButton);
						        pdfPanel.add(pdfFileLabel);
						        add(pdfPanel);

						        // Excel File Upload
						        JPanel excelPanel = new JPanel();
						        JButton excelButton = new JButton("📊 Upload Excel File");
						        excelFileLabel = new JLabel("No file selected");
						        excelPanel.add(excelButton);
						        excelPanel.add(excelFileLabel);
						        add(excelPanel);

						        // Automation Type Selection
						        JPanel automationPanel = new JPanel();
						        eobButton = new JRadioButton("EOB (Run pdf_test)");
						        eraButton = new JRadioButton("ERA (Run pdf)");
						        ButtonGroup group = new ButtonGroup();
						        group.add(eobButton);
						        group.add(eraButton);
						        automationPanel.add(eobButton);
						        automationPanel.add(eraButton);
						        add(automationPanel);

						        // Start Button
						        JPanel controlPanel = new JPanel();
						        JButton startButton = new JButton("🚀 Start Automation");
						        controlPanel.add(startButton);
						        add(controlPanel);

						        // Log Area
						        logArea = new JTextArea(5, 40);
						        logArea.setEditable(false);
						        add(new JScrollPane(logArea));

						        // PDF Upload Action
						        pdfButton.addActionListener(e -> {
						            JFileChooser fileChooser = new JFileChooser();
						            fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
						            int result = fileChooser.showOpenDialog(this);
						            if (result == JFileChooser.APPROVE_OPTION) {
						                selectedPDF = fileChooser.getSelectedFile();
						                pdfFileLabel.setText(selectedPDF.getName());
						                logArea.append("📄 PDF Uploaded: " + selectedPDF.getAbsolutePath() + "\n");
						            }
						        });

						        // Excel Upload Action
						        excelButton.addActionListener(e -> {
						            JFileChooser fileChooser = new JFileChooser();
						            fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Files", "xls", "xlsx"));
						            int result = fileChooser.showOpenDialog(this);
						            if (result == JFileChooser.APPROVE_OPTION) {
						                selectedExcel = fileChooser.getSelectedFile();
						                excelFileLabel.setText(selectedExcel.getName());
						                logArea.append("📊 Excel Uploaded: " + selectedExcel.getAbsolutePath() + "\n");
						            }
						        });

						        // Start Automation Action
						        startButton.addActionListener(e -> {
						            String email = emailField.getText();
						            String password = new String(passwordField.getPassword());
						            String selectedPractice = (String) practiceDropdown.getSelectedItem();

						            if (email.isEmpty() || password.isEmpty()) {
						                JOptionPane.showMessageDialog(this, "❌ Please enter Email and Password!", "Error", JOptionPane.ERROR_MESSAGE);
						                return;
						            }

						            if (selectedPDF == null || selectedExcel == null) {
						                JOptionPane.showMessageDialog(this, "❌ Please upload both PDF and Excel files!", "Error", JOptionPane.ERROR_MESSAGE);
						                return;
						            }

						            if (eobButton.isSelected()) {
						                automationType = "EOB";
						            } else if (eraButton.isSelected()) {
						                automationType = "ERA";
						            } else {
						                JOptionPane.showMessageDialog(this, "❌ Please select an automation type!", "Error", JOptionPane.ERROR_MESSAGE);
						                return;
						            }

						            logArea.append("🔹 Starting " + automationType + " Automation for " + selectedPractice + "\n");
						            logArea.append("🔑 Logging in as: " + email + "\n");

						            runAutomation(email, password, selectedPractice);
						        });

						        setVisible(true);
						    }

						    private void runAutomation(String email, String password, String practice) {
						        try {
						            if (automationType.equals("EOB")) {
						                System.out.println("📌 Running EOB Automation...");
						                Dos pdfAutomation = new Dos(email, password, practice, selectedExcel, selectedPDF);
						                pdfAutomation.executeAutomation();
						            } else if (automationType.equals("ERA")) {
						                System.out.println("📌 Running ERA Automation...");
						                pdf pdfAutomation = new pdf(email, password, practice, selectedExcel, selectedPDF);
						                pdfAutomation.executeAutomation();
						            }

						            logArea.append("✅ Automation completed successfully!\n");
						            JOptionPane.showMessageDialog(this, "Automation Process Complete!", "Process Complete", JOptionPane.INFORMATION_MESSAGE);

						        } catch (Exception e) {
						            // Show short error in GUI
						            String shortError = e.getClass().getSimpleName() + ": " + e.getMessage().split("\n")[0];
						            logArea.append("❌ Error: " + shortError + "\n");
						            JOptionPane.showMessageDialog(this, "❌ Error: " + shortError, "Error", JOptionPane.ERROR_MESSAGE);

						            // Save full stack trace to file
						            try (PrintWriter writer = new PrintWriter("error_log.txt")) {
						                e.printStackTrace(writer);
						                logArea.append("📄 Full error log saved to error_log.txt\n");
						            } catch (IOException ioException) {
						                logArea.append("❌ Failed to save full error log.\n");
						            }
						        }
						    }

						    public static void main(String[] args) {
						        new Main();
						    }
						}


