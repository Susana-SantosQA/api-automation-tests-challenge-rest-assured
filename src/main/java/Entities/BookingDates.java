package Entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BookingDates {
    @JsonProperty("checkin")
    private String checkin;
    
    @JsonProperty("checkout")
    private String checkout;

    // Construtor vazio para serialização
    public BookingDates() {}

    public BookingDates(String checkin, String checkout) {
        this.checkin = checkin;
        this.checkout = checkout;
    }

    // Getters e Setters
    public String getCheckin() {
        return checkin;
    }

    public void setCheckin(String checkin) {
        this.checkin = checkin;
    }

    public String getCheckout() {
        return checkout;
    }

    public void setCheckout(String checkout) {
        this.checkout = checkout;
    }

    // Método toString para uso em logs
    @Override
    public String toString() {
        return "BookingDates{" +
                "checkin='" + checkin + '\'' +
                ", checkout='" + checkout + '\'' +
                '}';
    }
}
