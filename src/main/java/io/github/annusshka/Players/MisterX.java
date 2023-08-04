package io.github.annusshka.Players;
import io.github.annusshka.Tickets;

import java.util.*;

import static io.github.annusshka.Tickets.*;

public class MisterX extends Characters {

    private final String name = "Мистер Х";

    private int position;

    private Map<Tickets, Integer> ticket = new HashMap<>();
    {
        ticket.put(TAXI, 4);
        ticket.put(BUS, 3);
        ticket.put(UNDERGROUND, 3);
        ticket.put(DOUBLE_STEP, 2);
        ticket.put(BLACK_TICKET, 0);
    }

    public MisterX(String name, int position, Map<Tickets, Integer> ticket) {
        super(name, position, ticket);
    }

    public MisterX(int position) {
        super(position);
    }

    @Override
    public void setTicket(Map<Tickets, Integer> ticket) {
        this.ticket = ticket;
    }

    @Override
    public Map<Tickets, Integer> getTicket() {
        return this.ticket;
    }

    public void addTicket(Tickets road, int addCount) {
        if (ticket.containsKey(road)) {
            ticket.replace(road, ticket.get(road) + addCount);
        } else {
            ticket.put(road, addCount);
        }
    }

    public void addTicket(Tickets road) {
        addTicket(road, 1);
    }

    public String getTicketsToStr() {
        StringBuilder tickets = new StringBuilder("Билеты на руках: ");

        tickets.append(System.getProperty("line.separator"));
        for (Tickets ticket : this.getTicket().keySet()) {
            tickets.append(ticket.toString()).append(" - ").append(this.getTicket().get(ticket)).append(", ");
        }

        return String.valueOf(tickets);
    }

    public String getMisterXPathToStr(int round) {
        StringBuilder misterXPath = new StringBuilder();
        int k = (int) (round % 5.0);

        if (getPath().size() == 0) {
            return "";
        }

        Characters.Step actualStep = getPath().get(getPath().size() - 1);
        misterXPath.append(round).append(". ");
        if (k == 3) {
            misterXPath.append(actualStep.getStopNumberTo()).append(" ");
        }
        misterXPath.append(actualStep.getRoad()).append(System.getProperty("line.separator"));

        return String.valueOf(misterXPath);
    }
}
