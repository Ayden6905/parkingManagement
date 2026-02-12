/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkingManagement;

/**
 *
 * @author User
 */
import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class ExitPanel extends JPanel {
    private MainFrame mainFrame;
    private ParkingSystemFacade facade;
    private CardLayout innerLayout = new CardLayout();
    private JPanel container = new JPanel(innerLayout);
    
    //temp storage
    private ParkingSummary currentSummary;
    
    public ExitPanel(ParkingSystemFacade facade, MainFrame mainFrame) {
        this.facade = facade;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        container.add(createSearchPanel(), "Search");
        add(container, BorderLayout.CENTER);
    }
    
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        JTextField plateField = new JTextField(15);
        JButton btnProceed = new JButton("Proceed to Exit");

        btnProceed.addActionListener(e -> {
            String plate = plateField.getText().trim();
            currentSummary = facade.getParkingSummary(plate, 3.00); 
            if (currentSummary != null) {
                showSummary();
            } else {
                JOptionPane.showMessageDialog(this, "No active ticket found for this plate.");
            }
        });
        
        panel.add(new JLabel("Enter License Plate: "));
        panel.add(plateField);
        panel.add(btnProceed);
        return panel;
    }
    
    private void showSummary() {
        JPanel summaryPanel = new JPanel(new GridLayout(0, 1));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        summaryPanel.add(new JLabel("--- PARKING SUMMARY ---"));
        summaryPanel.add(new JLabel("Ticket: " + currentSummary.getTicketId()));
        summaryPanel.add(new JLabel("Parking Fee: RM " + currentSummary.getParkingFee()));
        summaryPanel.add(new JLabel("Unpaid Fines: RM " + currentSummary.getFineAmount()));

        int choice = JOptionPane.showConfirmDialog(this, 
                "Would you like to pay your outstanding fines now?", "Fine Payment", JOptionPane.YES_NO_OPTION);

        double finalTotal = currentSummary.getParkingFee();
        double fineToPay = 0;

        if (choice == JOptionPane.YES_OPTION) {
            finalTotal += currentSummary.getFineAmount();
            fineToPay = currentSummary.getFineAmount();
        }
        
        summaryPanel.add(new JLabel("TOTAL TO PAY: RM " + finalTotal));
        
        JButton btnPay = new JButton("Pay RM " + finalTotal);
        double finalFineToPay = fineToPay;
        double finalTotalToPay = finalTotal;

        btnPay.addActionListener(e -> processPayment(finalTotalToPay, finalFineToPay));
        
        summaryPanel.add(btnPay);
        container.add(summaryPanel, "Summary");
        innerLayout.show(container, "Summary");
    }
    
    private void processPayment(double total, double finePaid) {
        String[] options = {"Cash", "Card"};
        int methodIdx = JOptionPane.showOptionDialog(this, "Select Payment Method", "Payment",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (methodIdx != -1) {
            String method = options[methodIdx];
            // Task: Execute payment logic and persist to DB
            Receipt receipt = facade.processPayment(currentSummary.getPlate(), 3.00, finePaid, method);
            showReceipt(receipt);
        }
    }
    
    private void showReceipt(Receipt receipt) {
        JTextArea area = new JTextArea(receipt.toString());
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(area), BorderLayout.CENTER);
        JButton btnHome = new JButton("Finish & Return Home");
        btnHome.addActionListener(e -> mainFrame.showHome());
        p.add(btnHome, BorderLayout.SOUTH);

        container.add(p, "Receipt");
        innerLayout.show(container, "Receipt");
    } 
}
