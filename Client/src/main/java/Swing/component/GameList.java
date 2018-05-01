package Swing.component;

import AlcatrazLocal.GameLocal;
import AlcatrazRemote.Interface.GameRemote;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.List;
import java.util.Vector;


/**
 * Created by Oli on 26.03.2018.
 */

public class GameList extends JFrame{

    public GameList(List<GameLocal> listus){
        setTitle("List of games");
        setSize(500,400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        //just swing things
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        JLabel jLabel = new JLabel();
        JList jList = new JList<>(new Vector<GameLocal>(listus));

        jList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (renderer instanceof JLabel && value instanceof GameLocal) {
                    ((JLabel) renderer).setText(((GameLocal) value).getGameID().toString());
                }
                return renderer;
            }
        });



        JPanel jPanel = new JPanel();

        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jList.setFixedCellWidth(150);
        jList.addListSelectionListener(( listSelectionEvent) ->{
            if (!listSelectionEvent.getValueIsAdjusting()) {
                JList list = (JList) listSelectionEvent.getSource();
                Object selectionValues = list.getSelectedValue();
                ((JLabel)((JPanel) getContentPane().getComponent(0)).getComponent(0)).setText(selectionValues.toString());
                }

        });


        jPanel.setLayout(new BorderLayout());
        jPanel.add(jLabel,BorderLayout.NORTH);
        contentPane.add(jPanel,BorderLayout.CENTER);
        contentPane.add(jList, BorderLayout.WEST);


        // pane.add(jPanel);




    }
}
