package com.project944.cov;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

public class PlayerChooserDialog extends JDialog {
    
    private volatile Object value;

    public static Object showDialog(Frame frame, String title, Object [] listData, Object selected) {
        PlayerChooserDialog dialog = new PlayerChooserDialog(frame, title, listData, selected);
        dialog.setLocation((frame.getX()+frame.getWidth())/2-dialog.getWidth()/2, (int) ((frame.getY()+frame.getHeight())*0.4));
        dialog.setVisible(true);
        return dialog.value;
    }
    
    public PlayerChooserDialog(Frame frame, String title, Object [] listData, Object selected) {
        super(frame, true);
        setTitle(title);
        getContentPane().setLayout(new BorderLayout());
        JPanel main = new JPanel();
        getContentPane().add(main);
        main.setLayout(new BorderLayout());
        final JList list = new JList(listData);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int index = Arrays.binarySearch(listData, selected);
        if ( index >= 0 ) {
            list.setSelectedIndex(index);
        }
        main.add(list);
        JPanel buttons = new JPanel();
        JButton cancel = new JButton("Cancel");
        ActionListener cancelListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        };
        cancel.addActionListener(cancelListener);
        buttons.add(cancel);
        getRootPane().registerKeyboardAction(cancelListener,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        JButton ok = new JButton("OK");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                value = list.getSelectedValue();
                setVisible(false);
            }
        });
        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    value = list.getSelectedValue();
                    setVisible(false);
                 }
            }
        };
        list.addMouseListener(mouseListener);

        getRootPane().setDefaultButton(ok);
        buttons.add(ok);
        main.add(buttons, BorderLayout.SOUTH);
        int sideBorder = MainViewer.sideBorder;
        main.setBorder(BorderFactory.createMatteBorder(sideBorder/2, sideBorder, 0, sideBorder, SystemColor.window));
        pack();
    }
}
