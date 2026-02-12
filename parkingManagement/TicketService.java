/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkingManagement;

/**
 *
 * @author User
 */
import java.time.LocalDateTime;

public class TicketService {
    
    private PaymentService paymentService;
    
    public TicketService() {
        this.paymentService = new PaymentService();
    }
    
    public String createTicket(String plate, String spotId) {
        String ticketId = "T-" + plate + "-" + System.currentTimeMillis();
        Ticket ticket = new Ticket(ticketId, plate, spotId, LocalDateTime.now());
        ticket.saveEntry();
        return ticketId;
    }
    
    public Receipt closeTicketAndPay(String plate, double hourlyRate,
                                      double fineAmount, String paymentMethod) {

        Ticket ticket = Ticket.findActiveByPlate(plate);
        if (ticket == null) {
            return null;
        }

        LocalDateTime exitTime = LocalDateTime.now();
        int hours = ticket.calculateDurationHours();
        double parkingFee = hours * hourlyRate;
        double totalPaid = parkingFee + fineAmount;

        ticket.closeTicket(exitTime, parkingFee, fineAmount, totalPaid, paymentMethod);
        paymentService.recordPayment(ticket.getTicketId(), plate, totalPaid, paymentMethod, fineAmount);
        paymentService.createReceipt(ticket.getTicketId(), parkingFee, fineAmount, totalPaid);

        return new Receipt(
                ticket.getTicketId(),
                plate,
                ticket.getEntryTime(),
                hours,
                parkingFee,
                fineAmount,
                totalPaid,
                paymentMethod
        );
    }
    
}
