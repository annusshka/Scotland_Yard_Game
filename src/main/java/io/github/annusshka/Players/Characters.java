package io.github.annusshka.Players;

import io.github.annusshka.Tickets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Characters {

    private String name;

    private Map<Tickets, Integer> ticket;

    private int position;

    private List<Step> path = new ArrayList<>();

    public Characters(String name, int position, Map<Tickets, Integer> ticket) {
        this.name = name;
        this.position = position;
        this.ticket = ticket;
    }

    public Characters(int position, Map<Tickets, Integer> ticket) {
        this.position = position;
        this.ticket = ticket;
    }

    public Characters(String name, int position) {
        this.name = name;
        this.position = position;
    }

    public Characters(int position) {
        this.position = position;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    abstract void setTicket(Map<Tickets, Integer> ticket);

    abstract Map<Tickets, Integer> getTicket();

    public static class Step {
        private int stopNumberFrom;

        private int stopNumberTo;

        private Tickets road;

        public Step(int stopNumberFrom, int stopNumberTo, Tickets road) {
            this.stopNumberFrom = stopNumberFrom;
            this.stopNumberTo = stopNumberTo;
            this.road = road;
        }

        public int getStopNumberFrom() {
            return stopNumberFrom;
        }

        public void setStopNumberFrom(int stopNumberFrom) {
            if (stopNumberFrom > 0 && stopNumberFrom < 200) {
                this.stopNumberFrom = stopNumberFrom;
            }
        }

        public int getStopNumberTo() {
            return stopNumberTo;
        }

        public void setStopNumberTo(int stopNumberTo) {
            if (stopNumberTo > 0 && stopNumberTo < 200) {
                this.stopNumberTo = stopNumberTo;
            }
        }

        public Tickets getRoad() {
            return road;
        }

        public void setRoad(Tickets road) {
            this.road = road;
        }
    }

    public List<Step> getPath() {
        return path;
    }

    public void addPath(int stopNumberFrom, int stopNumberTo, Tickets road) {
        path.add(new Step(stopNumberFrom, stopNumberTo, road));
        this.position = stopNumberTo;
    }

    public String getPathToStr() {
        StringBuilder playerPath = new StringBuilder();

        if (this.getPath().size() == 0) {
            playerPath.append("Сейчас находится на остановке ").append(String.valueOf(this.getPosition())).
                    append(System.getProperty("line.separator"));
            return String.valueOf(playerPath);
        }

        for (int index = 0; index < this.getPath().size(); index++) {
            Step actualStep = this.getPath().get(index);
            playerPath.append(index + 1).append(". ").append(actualStep.getStopNumberFrom()).append(" -> ").
                    append(actualStep.getStopNumberTo()).append(" ").append(actualStep.getRoad()).
                    append(System.getProperty("line.separator"));
        }
        return String.valueOf(playerPath);
    }
}
