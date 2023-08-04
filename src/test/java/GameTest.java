import io.github.annusshka.Game;
import io.github.annusshka.GraphUtils.Graph;
import io.github.annusshka.Players.Characters;
import io.github.annusshka.Players.Detective;
import io.github.annusshka.Players.MisterX;
import io.github.annusshka.Tickets;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import util.SwingUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static io.github.annusshka.Tickets.*;

public class GameTest {
    Graph graph = null;

    Game game = new Game();

    public void setGraph() {
        try {
            Class<?> clz = Class.forName("io.github.annusshka.GraphUtils.Graph");
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = Files.newBufferedReader(Paths.get("ScotlandYardMap.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append(System.lineSeparator());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.graph = Graph.fromStr(String.valueOf(sb), clz);
        } catch (Exception exc) {
            SwingUtils.showErrorMessageBox(exc);
        }
    }

    @Test
    public void isGameEnd() {
        setGraph();
        game.newGame(2, graph);

        Map<Tickets, Integer> misterXTickets = new HashMap<>();
        {
            misterXTickets.put(UNDERGROUND, 1);
        }

        Map<Tickets, Integer> detectiveTickets = new HashMap<>();
        {
            detectiveTickets.put(TAXI, 1);
        }

        // Случай 1. Детективы попали на одну остановку с Мистером Х
        game.getPlayers().get(0).setPosition(170);
        ((MisterX) game.getPlayers().get(0)).setTicket(misterXTickets);
        game.getPlayers().get(1).setPosition(170);
        ((Detective) game.getPlayers().get(1)).setTicket(detectiveTickets);

        game.isGameEnd(1);
        Assertions.assertThat(game.getState()).isEqualTo(Game.GameState.DETECTIVES_WIN);

        // Случай 2. Мистер Х сделал свой последний ход, а детективы так и не нашли его
        game.getPlayers().get(0).setPosition(10);
        ((MisterX) game.getPlayers().get(0)).setTicket(misterXTickets);
        game.getPlayers().get(1).setPosition(170);
        ((Detective) game.getPlayers().get(1)).setTicket(detectiveTickets);
        game.setGameRound(22);

        game.isGameEnd(1);
        Assertions.assertThat(game.getState()).isEqualTo(Game.GameState.MISTER_X_WIN);

        // Случай 3. Детективы больше не могут ходить
        game.newGame(4, graph);
        Map<Tickets, Integer> newDetectiveTickets = new HashMap<>();
        {
            newDetectiveTickets.put(UNDERGROUND, 1);
        }

        game.getPlayers().get(0).setPosition(10);
        ((MisterX) game.getPlayers().get(0)).setTicket(misterXTickets);
        // 170  --  { 157  TAXI },  { 159  TAXI },  { 185  TAXI }
        game.getPlayers().get(1).setPosition(170);
        ((Detective) game.getPlayers().get(1)).setTicket(newDetectiveTickets);
        // 31  --  { 18  TAXI },  { 43  TAXI },  { 44  TAXI }
        game.getPlayers().get(2).setPosition(31);
        ((Detective) game.getPlayers().get(2)).setTicket(newDetectiveTickets);
        // 25  --  { 14  TAXI },  { 38  TAXI },  { 39  TAXI }
        game.getPlayers().get(3).setPosition(25);
        ((Detective) game.getPlayers().get(3)).setTicket(newDetectiveTickets);
        game.setGameRound(5);

        game.setNotAvailablePlayer(1);
        game.setNotAvailablePlayer(2);
        game.setNotAvailablePlayer(3);
        game.isGameEnd(1);
        Assertions.assertThat(game.getState()).isEqualTo(Game.GameState.MISTER_X_WIN);
    }

    @Test
    public void isAvailableToMove() {
        setGraph();
        game.newGame(4, graph);
        Map<Tickets, Integer> misterXTickets = new HashMap<>();
        {
            misterXTickets.put(UNDERGROUND, 1);
        }
        Map<Tickets, Integer> detectiveTickets = new HashMap<>();
        {
            detectiveTickets.put(UNDERGROUND, 1);
            detectiveTickets.put(TAXI, 1);
            detectiveTickets.put(BUS, 1);
        }

        game.getPlayers().get(0).setPosition(170);
        // Случай 1. Мистер Х не может сходить
        ((MisterX) game.getPlayers().get(0)).setTicket(misterXTickets);
        Assertions.assertThat(game.isAvailableToMove(0)).isEqualTo(false);

        // Случай 2. Мистер Х имеет чёрный билет
        misterXTickets.put(BLACK_TICKET, 1);
        ((MisterX) game.getPlayers().get(0)).setTicket(misterXTickets);
        Assertions.assertThat(game.isAvailableToMove(0)).isEqualTo(true);

        // Случай 3. Мистер Х имеет билет двойного хода
        misterXTickets.remove(BLACK_TICKET);
        misterXTickets.put(DOUBLE_STEP, 1);
        ((MisterX) game.getPlayers().get(0)).setTicket(misterXTickets);
        Assertions.assertThat(game.isAvailableToMove(0)).isEqualTo(true);

        // Случай 1. Детектив может сходить и имеет все виды билетов
        game.getPlayers().get(1).setPosition(100);
        ((Detective) game.getPlayers().get(1)).setTicket(detectiveTickets);
        Assertions.assertThat(game.isAvailableToMove(1)).isEqualTo(true);

        // Случай 2. Детектив не может сходить
        // 31  --  { 18  TAXI },  { 43  TAXI },  { 44  TAXI }
        game.getPlayers().get(2).setPosition(31);
        detectiveTickets.remove(TAXI);
        detectiveTickets.remove(BUS);
        ((Detective) game.getPlayers().get(2)).setTicket(detectiveTickets);
        Assertions.assertThat(game.isAvailableToMove(2)).isEqualTo(false);

        // Случай 3. Детектив может сходить, но имеет не все виды билетов
        // 170  --  { 157  TAXI },  { 159  TAXI },  { 185  TAXI }
        game.getPlayers().get(3).setPosition(170);
        detectiveTickets.remove(UNDERGROUND);
        detectiveTickets.put(TAXI, 1);
        ((Detective) game.getPlayers().get(3)).setTicket(detectiveTickets);
        Assertions.assertThat(game.isAvailableToMove(3)).isEqualTo(true);
    }

    @Test
    public void addPath() throws Exception {
        setGraph();
        game.newGame(4, graph);
        Map<Tickets, Integer> misterXTickets = new HashMap<>();
        {
            misterXTickets.put(UNDERGROUND, 1);
        }
        Map<Tickets, Integer> detectiveTickets = new HashMap<>();
        {
            detectiveTickets.put(UNDERGROUND, 1);
            detectiveTickets.put(TAXI, 1);
            detectiveTickets.put(BUS, 1);
        }

        // Случай 1. Мистер Х выбрал не соседнюю остановку
        // 170  --  { 157  TAXI },  { 159  TAXI },  { 185  TAXI }
        game.getPlayers().get(0).setPosition(170);
        ((MisterX) game.getPlayers().get(0)).setTicket(misterXTickets);

        Throwable thrown1 = Assertions.catchThrowable(() -> {
            game.addPath(160, 0, UNDERGROUND);
        });
        Assertions.assertThat(thrown1).isInstanceOf(java.lang.Exception.class);
        Assertions.assertThat(thrown1.getMessage()).isNotBlank();
        Assertions.assertThat(thrown1.getMessage()).isEqualTo("Выберите другую остановку!");

        // Случай 2. Мистер Х не имеет билета, чтобы уехать
        Throwable thrown2 = Assertions.catchThrowable(() -> {
            game.addPath(157, 0, TAXI);
        });
        Assertions.assertThat(thrown2).isInstanceOf(java.lang.Exception.class);
        Assertions.assertThat(thrown2.getMessage()).isNotBlank();
        Assertions.assertThat(thrown2.getMessage()).
                isEqualTo("Вы в тупике! Пропустите ход, чтобы получить билеты детективов!");

        // Случай 3. Мистер Х хочет сходить на остановку с другим детективом
        ((MisterX) game.getPlayers().get(0)).addTicket(TAXI);
        game.getPlayers().get(1).setPosition(157);
        Throwable thrown3 = Assertions.catchThrowable(() -> {
            game.addPath(157, 0, TAXI);
        });
        Assertions.assertThat(thrown3).isInstanceOf(java.lang.Exception.class);
        Assertions.assertThat(thrown3.getMessage()).isNotBlank();
        Assertions.assertThat(thrown3.getMessage()).
                isEqualTo("На этой остановке уже находится другой детектив");

        // Случай 4. Мистер Х использует билет двойного хода, но до выбранной остановки нельзя добраться двойным ходом
        ((MisterX) game.getPlayers().get(0)).addTicket(DOUBLE_STEP);
        Throwable thrown4 = Assertions.catchThrowable(() -> {
            game.addPath(159, 0, DOUBLE_STEP);
        });
        Assertions.assertThat(thrown4).isInstanceOf(java.lang.Exception.class);
        Assertions.assertThat(thrown4.getMessage()).isNotBlank();
        Assertions.assertThat(thrown4.getMessage()).
                isEqualTo("Нельзя добраться двойным ходом! Выберите подходящую остановку!");

        // Случай 5. Мистер Х успешно использует билет двойного хода
        ((MisterX) game.getPlayers().get(0)).addTicket(DOUBLE_STEP);
        game.addPath(153, 0, DOUBLE_STEP);
        Assertions.assertThat(((MisterX) game.getPlayers().get(0)).getPathToStr()).
                isEqualTo("1. 170 -> 185 TAXI" + System.getProperty("line.separator") +
                        "2. 185 -> 153 UNDERGROUND" + System.getProperty("line.separator"));

        // Случай 6. Выбран неверный билет
        // 150  --  { 138  TAXI },  { 149  TAXI },  { 151  TAXI },
        game.getPlayers().get(2).setPosition(150);
        ((Detective) game.getPlayers().get(2)).setTicket(detectiveTickets);
        Throwable thrown5 = Assertions.catchThrowable(() -> {
            game.addPath(138, 2, UNDERGROUND);
        });
        Assertions.assertThat(thrown5).isInstanceOf(java.lang.Exception.class);
        Assertions.assertThat(thrown5.getMessage()).isNotBlank();
        Assertions.assertThat(thrown5.getMessage()).
                isEqualTo("Выберите другой способ добраться до остановки!");

        // Случай 7. Путь успешно добавлен
        game.addPath(138, 2, TAXI);
        Assertions.assertThat(((Detective) game.getPlayers().get(2)).getPathToStr()).
                isEqualTo("1. 150 -> 138 TAXI" + System.getProperty("line.separator"));

        // Случай 8. Мистер Х использует чёрный билет
        ((MisterX) game.getPlayers().get(0)).addTicket(BLACK_TICKET);
        game.addPath(185, 0, BLACK_TICKET);
        Assertions.assertThat(((MisterX) game.getPlayers().get(0)).getPathToStr()).
                isEqualTo("1. 170 -> 185 TAXI" + System.getProperty("line.separator") +
                        "2. 185 -> 153 UNDERGROUND" + System.getProperty("line.separator") +
                        "3. 153 -> 185 BLACK_TICKET" + System.getProperty("line.separator"));
    }
}
