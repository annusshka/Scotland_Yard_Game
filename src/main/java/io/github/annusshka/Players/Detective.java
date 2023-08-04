package io.github.annusshka.Players;
import io.github.annusshka.Tickets;

import java.util.*;

import static io.github.annusshka.Tickets.*;

public class Detective extends Characters {

    private String name;

    private Map<Tickets, Integer> ticket = new HashMap<>();
    {
        ticket.put(TAXI, 10);
        ticket.put(BUS, 8);
        ticket.put(UNDERGROUND, 4);
    }

    private int position;

    public Detective(String name, int position, Map<Tickets, Integer> ticket) {
        super(name, position, ticket);
    }

    public Detective(String name, int position) {
        super(name, position);
    }

    @Override
    public void setTicket(Map<Tickets, Integer> ticket) {
        this.ticket = ticket;
    }

    @Override
    public Map<Tickets, Integer> getTicket() {
        return this.ticket;
    }

    public String getTicketsToStr() {
        StringBuilder tickets = new StringBuilder("Билеты на руках: ");

        tickets.append(System.getProperty("line.separator"));
        for (Tickets ticket : getTicket().keySet()) {
            tickets.append(ticket.toString()).append(" - ").append(getTicket().get(ticket)).append(", ");
        }

        return String.valueOf(tickets);
    }
}
