package io.github.annusshka;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ParamsDialog extends JFrame{
    private JPanel panelMain;
    private JSpinner spinnerPlayersCount;
    private JButton buttonNewGame;
    private JButton buttonCancel;

    private ActionListener newGameAction;

    public int countOfPlayers;

    public final int minCountOfPlayers = 2;

    public final int maxCountOfPlayers = 6;

    public ParamsDialog(ActionListener newGameAction){
        this.setTitle("Параметры");
        this.setContentPane(panelMain);
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.pack();

        this.setResizable(false);

        this.countOfPlayers = minCountOfPlayers;
        this.newGameAction = newGameAction;

        spinnerPlayersCount.addChangeListener(e -> {
            this.countOfPlayers = (int) spinnerPlayersCount.getValue();
        });
        buttonCancel.addActionListener(e -> {
            this.setVisible(false);
        });
        buttonNewGame.addActionListener(e -> {
            updateView();
            this.setVisible(false);
            if (newGameAction != null) {
                newGameAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "newGame"));
            }
        });
    }

    public void updateView() {
        if (countOfPlayers >= minCountOfPlayers && countOfPlayers <= maxCountOfPlayers) {
            spinnerPlayersCount.setValue(countOfPlayers);
        } else {
            spinnerPlayersCount.setValue(minCountOfPlayers);
        }
    }
}
