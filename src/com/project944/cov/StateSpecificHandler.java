package com.project944.cov;

import java.awt.event.MouseEvent;

public interface StateSpecificHandler {

    void mouseMoved(MouseEvent e);
    void mouseDragged(MouseEvent e);

    void mouseClicked(MouseEvent e);
    void mousePressed(MouseEvent e);
    void mouseReleased(MouseEvent e);
    void setEditMode(boolean editMode);

}
