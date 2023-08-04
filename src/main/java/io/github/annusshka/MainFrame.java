package io.github.annusshka;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import io.github.annusshka.GraphUtils.Graph;
import io.github.annusshka.Players.Characters;
import io.github.annusshka.Players.Detective;
import io.github.annusshka.Players.MisterX;
import util.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.io.IOException;

import static io.github.annusshka.Tickets.*;

public class MainFrame extends JFrame{
    private JTabbedPane tabbedPane;
    public JPanel panelMain;
    private JPanel JPanelGameSpace;
    private JPanel JPanelRules;
    private JTextArea textAreaRules;
    private JTextArea textAreaDetective1Path;
    private JTextArea textAreaDetective2Path;
    private JTextArea textAreaDetective3Path;
    private JTextArea textAreaDetective4Path;
    private JTextArea textAreaDetective5Path;
    private JTextArea textAreaMisterXPath;
    private JSpinner spinnerNextStop;
    private JButton buttonChoose;
    private JPanel panelGraphPainterContainer;
    private JLabel labelStatus;

    private JComboBox<Tickets> comboBoxTickets;
    private JButton buttonClosePath;
    private JButton buttonOpenPath;
    private JButton buttonSkip;
    private JTextArea textAreaGraph;
    private JTextArea textAreaMisterXPosition;

    private Graph graph = null;

    //private SvgPanel panelGraphPainter;

    private Game game = new Game();

    private ParamsDialog dialogParams = new ParamsDialog(e -> newGame());

    private int order = 0;

    private int round = 1;

    /*
    private static class SvgPanel extends JPanel {
        private String svg = null;
        private GraphicsNode svgGraphicsNode = null;

        public void paint(String svg) throws IOException {
            String xmlParser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory df = new SAXSVGDocumentFactory(xmlParser);
            SVGDocument doc = df.createSVGDocument(null, new StringReader(svg));
            UserAgent userAgent = new UserAgentAdapter();
            DocumentLoader loader = new DocumentLoader(userAgent);
            BridgeContext ctx = new BridgeContext(userAgent, loader);
            ctx.setDynamicState(BridgeContext.DYNAMIC);
            GVTBuilder builder = new GVTBuilder();
            svgGraphicsNode = builder.build(ctx, doc);

            this.svg = svg;
            repaint();
        }

        @Override
        public void paintComponent(Graphics gr) {
            super.paintComponent(gr);

            if (svgGraphicsNode == null) {
                return;
            }

            double scaleX = this.getWidth() / svgGraphicsNode.getPrimitiveBounds().getWidth();
            double scaleY = this.getHeight() / svgGraphicsNode.getPrimitiveBounds().getHeight();
            double scale = Math.min(scaleX, scaleY);
            AffineTransform transform = new AffineTransform(scale, 0, 0, scale, 0, 0);
            svgGraphicsNode.setTransform(transform);
            Graphics2D g2d = (Graphics2D) gr;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            svgGraphicsNode.paint(g2d);
        }
    }

     */

    public MainFrame() {
        this.setTitle("Преступник и детективы (Scotland Yard)");
        this.setContentPane(panelMain);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();

        setJMenuBar(createMenuBar());
        this.pack();

        SwingUtils.setShowMessageDefaultErrorHandler();

        /*
        //panelGraphPainterContainer.setBorder(null);
        //panelGraphPainterContainer.setLayout(new BorderLayout());
        //panelGraphPainter = new SvgPanel();
        //panelGraphPainterContainer.add(new JScrollPane(panelGraphPainter));

         */

        Tickets[] elements = new Tickets[] {TAXI, BUS, UNDERGROUND, BLACK_TICKET, DOUBLE_STEP};
        for (Tickets road : elements) {
            this.comboBoxTickets.addItem(road);
        }

        try {
            Class clz = Class.forName("io.github.annusshka.GraphUtils.Graph");
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = Files.newBufferedReader(Paths.get("ScotlandYardMap.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append(System.lineSeparator());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Graph graph = Graph.fromStr(String.valueOf(sb), clz);
            this.graph = graph;
            textAreaGraph.append(Graph.toStr(graph));
            //panelGraphPainter.paint(dotToSvg(io.github.annusshka.AdjListsGraph.toDot(graph)));
        } catch (Exception exc) {
            SwingUtils.showErrorMessageBox(exc);
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("GameRules.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        textAreaRules.append(String.valueOf(sb));

        newGame();

        dialogParams = new ParamsDialog(e -> newGame());

        buttonChoose.addActionListener(e -> {
            Integer stopValue = (Integer) spinnerNextStop.getValue();
            Tickets ticket = (Tickets) comboBoxTickets.getSelectedItem();
            if (stopValue.compareTo(0) > 0 && stopValue.compareTo(200) < 0) {
                try {
                    game.addPath(stopValue, order, ticket);
                    updatePath();
                    updateView();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(JPanelGameSpace, ex);
                }
            } else {
                JOptionPane.showMessageDialog(JPanelGameSpace, "Выберите другую остановку!");
            }
            buttonClosePath.doClick();
        });

        buttonSkip.addActionListener(e -> {
            JOptionPane.showMessageDialog(JPanelGameSpace, "Вы решили пропустить свой ход!");
            if (order == 0) {
                setMisterXSkipPath();
            }
            updateView();
            buttonClosePath.doClick();
        });

        buttonOpenPath.addActionListener(e -> {
            if (order == 0) {
                textAreaMisterXPosition.setText(game.getPlayers().get(order).getPathToStr());
            }
        });

        buttonClosePath.addActionListener(e -> {
            textAreaMisterXPosition.setText(getTickets(0));
        });
    }

    private void newGame() {
        game.newGame(dialogParams.countOfPlayers, graph);
        firstView();
    }

    private void firstView() {
        labelStatus.setForeground(Color.BLACK);
        labelStatus.setText("Раунд " + round + " Сейчас ходит " + order);
        textAreaMisterXPath.setText("");
        textAreaMisterXPosition.setText("Посмотреть положение");
        String defaultStr = "Пока занят ловлей других злодеев";
        textAreaDetective1Path.setText(defaultStr);
        textAreaDetective2Path.setText(defaultStr);
        textAreaDetective3Path.setText(defaultStr);
        textAreaDetective4Path.setText(defaultStr);
        textAreaDetective5Path.setText(defaultStr);

        while (order < game.getCountOfPlayers()) {
            updateDetectivePath();
            order++;
        }
        order = 0;
    }

    private void updateView() {
        if (game.getState() == Game.GameState.PLAYING) {
            spinnerNextStop.setValue(0);
            getNextOrder();
            labelStatus.setForeground(Color.BLACK);
            labelStatus.setText("Раунд " + round + " Сейчас ходит " + order);
        } else {
            labelStatus.setText("");
            if (game.getState() == Game.GameState.DETECTIVES_WIN) {
                labelStatus.setForeground(Color.RED);
                labelStatus.setText("Победа Детективов! Мистер Х проклинает вас в тюрьме");
            } else if (game.getState() == Game.GameState.MISTER_X_WIN) {
                labelStatus.setForeground(Color.RED);
                labelStatus.setText("Победа Мистера Х! Он весело смеётся над жалкими детективами, " +
                        "попивая коктейли на сказочном Бали");
            }
        }
    }

    public String getPath(int order) {
        return game.getPlayers().get(order).getPathToStr();
    }

    public String getMisterXPath() {
        return ((MisterX) game.getPlayers().get(0)).getMisterXPathToStr(round);
    }

    private void updateMisterXPath() {
        textAreaMisterXPath.append(getMisterXPath());
    }

    private void updateDetectivePath() {
        if (order == 1) {
            textAreaDetective1Path.setText(getPath(order) + getTickets(order));
        } else if (order == 2) {
            textAreaDetective2Path.setText(getPath(order) + getTickets(order));
        } else if (order == 3) {
            textAreaDetective3Path.setText(getPath(order) + getTickets(order));
        } else if (order == 4) {
            textAreaDetective4Path.setText(getPath(order) + getTickets(order));
        } else if (order == 5) {
            textAreaDetective5Path.setText(getPath(order)+ getTickets(order));
        }
    }

    private void updatePath() {
        if (order == 0) {
            updateMisterXPath();
        } else {
            updateDetectivePath();
        }
    }

    public void setMisterXSkipPath() {
        int k = (int) (round % 5.0);
        textAreaMisterXPath.append(round + ". ");
        if (k == 3) {
            textAreaMisterXPath.append(String.valueOf(game.getPlayers().get(0).getPosition()));
        }
        textAreaMisterXPath.append(" Мистер Х пропустил ход");
        textAreaMisterXPath.append(System.getProperty("line.separator"));
    }

    public String getTickets(int order) {
        Characters player = game.getPlayers().get(order);
        return player instanceof MisterX ?
                ((MisterX) player).getTicketsToStr() : ((Detective) player).getTicketsToStr();
    }

    private void getNextOrder() {
        order++;
        if (order == dialogParams.countOfPlayers) {
            order = 0;
            round++;
        }
    }

    private JMenuItem createMenuItem(String text, String shortcut, java.lang.Character mnemonic, ActionListener listener) {
        JMenuItem menuItem = new JMenuItem(text);
        menuItem.addActionListener(listener);
        if (shortcut != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(shortcut.replace('+', ' ')));
        }
        if (mnemonic != null) {
            menuItem.setMnemonic(mnemonic);
        }
        return menuItem;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBarMain = new JMenuBar();

        JMenu menuGame = new JMenu("Игра");
        menuBarMain.add(menuGame);
        menuGame.add(createMenuItem("Новая", "ctrl+N", null, e -> {
            newGame();
        }));

        menuGame.add(createMenuItem("Параметры", "ctrl+P", null, e -> {
            dialogParams.updateView();
            dialogParams.setVisible(true);
        }));

        menuGame.addSeparator();
        menuGame.add(createMenuItem("Выход", "ctrl+X", null, e -> {
            System.exit(0);
        }));

        JMenu menuView = new JMenu("Вид");
        menuBarMain.add(menuView);

        menuView.addSeparator();
        SwingUtils.initLookAndFeelMenu(menuView);

        JMenu menuHelp = new JMenu("Справка");
        menuBarMain.add(menuHelp);

        menuHelp.add(createMenuItem("О программе", "ctrl+A", null, e -> {
            SwingUtils.showInfoMessageBox(
                    """
                            Игра «Scotland Yard»

                            Автор: Телегина А.С.
                            – логическая игра студентки 2-го курса ФКН ВГУ""",
                    "О программе"
            );
        }));

        return menuBarMain;
    }

    private String dotToSvg(String dotSrc) throws IOException {
        MutableGraph g = new Parser().read(dotSrc);
        return Graphviz.fromGraph(g).render(Format.SVG).toString();
    }
}
