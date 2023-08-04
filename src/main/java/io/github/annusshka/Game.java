package io.github.annusshka;

import io.github.annusshka.GraphUtils.Graph;
import io.github.annusshka.Players.Characters;
import io.github.annusshka.Players.Detective;
import io.github.annusshka.Players.MisterX;

import java.security.SecureRandom;
import java.util.*;

import static io.github.annusshka.Tickets.*;

public class Game {

    SecureRandom random = new SecureRandom();

    public enum GameState {
        NOT_STARTED,
        PLAYING,
        DETECTIVES_WIN,
        MISTER_X_WIN
    }

    private List<Integer> startPositions =
            new ArrayList<>(Arrays.asList(5, 13, 56, 43, 67, 23, 78, 33, 89, 36, 49, 69, 1, 90, 111, 150, 177, 190));

    Graph graph = new Graph();

    private GameState state = GameState.NOT_STARTED;

    public GameState getState() {
        return state;
    }

    private int countOfPlayers = 2;

    public int getCountOfPlayers() {
        return this.countOfPlayers;
    }

    public void setCountOfPlayers(int countOfPlayers) {
        if (countOfPlayers >= 2 && countOfPlayers <= 6) {
            this.countOfPlayers = countOfPlayers;
        }
    }

    private int gameRound = 1;

    public int getGameRound() {
        return gameRound;
    }

    public void setGameRound(int gameRound) {
        if (gameRound > 0 && gameRound < 23) {
            this.gameRound = gameRound;
        }
    }

    private List<Characters> players = null;

    public List<Characters> getPlayers() {
        return players;
    }

    private boolean[] notAvailablePlayers = new boolean[countOfPlayers];

    public Game() {
    }

    public void newGame(int countOfPlayers, Graph graph) {
        this.graph = graph;
        this.players = new ArrayList<>();
        setCountOfPlayers(countOfPlayers);
        this.gameRound = 1;
        this.notAvailablePlayers = new boolean[countOfPlayers];

        int indexInStartPositions = random.nextInt(startPositions.size());
        MisterX misterX = new MisterX(startPositions.get(indexInStartPositions));
        misterX.addTicket(BLACK_TICKET, countOfPlayers - 1);
        players.add(misterX);

        for (int i = 0; i < countOfPlayers - 1; i++) {
            startPositions.remove(indexInStartPositions);
            indexInStartPositions = random.nextInt(startPositions.size());
            players.add(new Detective("Детектив " + (i + 1), startPositions.get(indexInStartPositions)));
        }

        state = GameState.PLAYING;
    }

    /**
     * Метод проверяет имеет ли игрок билет на транспорт, которым хочет воспользоваться
     * @param road Билет, который надо проверить
     * @param order номер игрока, который ходит
     * @return
     */
    public boolean checkHasTicket(Tickets road, int order) {
        Characters player = players.get(order);
        Map<Tickets, Integer> tickets =
                player instanceof MisterX ? ((MisterX) player).getTicket() : ((Detective) player).getTicket();

        if (!tickets.containsKey(road)) {
            return false;
        }

        return tickets.get(road).compareTo(0) > 0;
    }

    /**
     * Проверяет нет ли другого детектива на остановке, куда собирается переместиться игрок
     * @param stop остановка, куда собирается переместиться игрок
     * @return false - уже занята, true - можно переместиться
     */
    public boolean checkVisitors(int stop) {
        for (int i = 0; i < countOfPlayers - 1; i++) {
            if (players.get(i + 1).getPosition() == stop) {
                return false;
            }
        }
        return true;
    }

    /**
     * Детектив делает двойной ход
     * @param nextStop остановка, на которую нужно попасть в результате хода
     * @throws Exception если до выбранной остановки нельзя добраться двойным ходом
     */
    private void doDoubleStep(final int nextStop) throws Exception {
        MisterX misterX = (MisterX) players.get(0);
        final int prevSize = misterX.getPath().size();
        final int position = misterX.getPosition();

        for (Map.Entry<Integer, List<Tickets>> stop : graph.getAdjStops(position).entrySet()) {
            final int adjStop = stop.getKey();

            if (graph.isAdj(adjStop, nextStop)) {
                misterX.addPath(position, adjStop, stop.getValue().get(0));
                misterX.addPath(adjStop, nextStop, graph.getAdjStops(adjStop).get(nextStop).get(0));

                Map<Tickets, Integer> map = misterX.getTicket();
                int count = map.get(DOUBLE_STEP);
                map.put(DOUBLE_STEP, --count);
                break;
            }
        }

        if (prevSize == misterX.getPath().size()) {
            throw new Exception("Нельзя добраться двойным ходом! Выберите подходящую остановку!");
        }
    }

    /**
     * Метод добавляет сделанный ход ко всем ходам этого игрока
     * @param nestStop остановка, на которую нужно попасть в результате хода
     * @param order номер игрока, который ходит
     * @param ticket билет, которым воспользовались
     * @throws Exception
     */
    public void addPath(int nestStop, int order, Tickets ticket) throws Exception {
        Characters player = players.get(order);

        if (!checkHasTicket(ticket, order)) {
            setNotAvailablePlayer(order);
            if (notAvailablePlayers[order] && order == 0) {
                notAvailablePlayers[order] = false;
                throw new Exception("Вы в тупике! Пропустите ход, чтобы получить билеты детективов!");
            } else if (notAvailablePlayers[order]) {
                throw new Exception("Вы в тупике и больше не можете передвигаться!");
            }
            throw new Exception("У вас нет билета на этот транспорт. Выберите другой!");
        }
        if (!checkVisitors(nestStop)) {
            throw new Exception("На этой остановке уже находится другой детектив");
        }
        if (ticket.equals(DOUBLE_STEP)) {
            doDoubleStep(nestStop);
            return;
        }

        int actualStop = player.getPosition();
        Map<Integer, List<Tickets>> adjStops = graph.getAdjStops(actualStop);

        // Проверка есть ли вообще такая соседняя остановка
        if (adjStops != null && adjStops.containsKey(nestStop)) {
            if (!adjStops.get(nestStop).contains(ticket) && ticket != BLACK_TICKET) {
                throw new Exception("Выберите другой способ добраться до остановки!");
            }

            player.addPath(actualStop, nestStop, ticket);
            Map<Tickets, Integer> tickets =
                    player instanceof MisterX ? ((MisterX) player).getTicket() : ((Detective) player).getTicket();
            int count = tickets.get(ticket);
            if (count - 1 == 0) {
                tickets.remove(ticket);
            } else {
                tickets.put(ticket, count - 1);
            }

            if (player instanceof Detective) {
                ((MisterX) players.get(0)).addTicket(ticket);
            }
        } else {
            throw new Exception("Выберите другую остановку!");
        }

        isGameEnd(order);
    }

    /**
     * Метод устанавливает значение true для тех игроков, которые больше не могут перемещаться,
     * так как не имеют подходящих билетов
     * @param order порядковый номер игрока
     */
    public void setNotAvailablePlayer(int order) {
        if (!isAvailableToMove(order)) {
            notAvailablePlayers[order] = true;
        }
    }

    /**
     * Метод проверяет имеет ли игрок возможность изменить своё положение в соответствии с имеющимися на руках картами
     * @param order порядковый номер игрока
     * @return true - может изменить положение, false - не может двигаться
     */
    public boolean isAvailableToMove(int order) {
        Characters player = players.get(order);
        Map<Tickets, Integer> tickets =
                player instanceof MisterX ? ((MisterX) player).getTicket() : ((Detective) player).getTicket();

        for (Map.Entry<Integer, List<Tickets>> stop : graph.getAdjStops(player.getPosition()).entrySet()) {
            for (Tickets availableRoads : tickets.keySet()) {
                if (availableRoads == BLACK_TICKET || availableRoads == DOUBLE_STEP) {
                    return true;
                }
                if (stop.getValue().contains(availableRoads)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Метод проверяет настал ли конец игры после очередного хода
     * @param order порядковый номер сходившего игрока
     */
    public void isGameEnd(int order) {
        if (order != 0 && players.get(order).getPosition() == players.get(0).getPosition()) {
            state = GameState.DETECTIVES_WIN;
        } else if (gameRound == 22 && order == countOfPlayers - 1) {
            state = GameState.MISTER_X_WIN;
        } else {
            int count = 0;
            for (int i = 0; i < notAvailablePlayers.length - 1; i++) {
                if (notAvailablePlayers[i + 1]) {
                    count++;
                }
            }
            if (count == countOfPlayers - 1) {
                state = GameState.MISTER_X_WIN;
            }
        }
    }
}
