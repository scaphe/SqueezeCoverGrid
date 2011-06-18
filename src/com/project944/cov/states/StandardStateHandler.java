package com.project944.cov.states;

import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.project944.cov.CoverDetails;
import com.project944.cov.ImagesPanel;
import com.project944.cov.MainViewer;
import com.project944.cov.StateSpecificHandler;
import com.project944.cov.TrackDetails;
import com.project944.cov.actions.MoveAction;
import com.project944.cov.utils.PointAtTime;
import com.project944.cov.utils.PropsUtils;

public class StandardStateHandler implements StateSpecificHandler {

    private ImagesPanel ctx;
    
    private CoverDetails coverSelected = null;
    private Point popupPos;
    private PointAtTime prevPanCurr;
    private PointAtTime panCurr;
    
    private boolean editMode = false;

    private Timer timer;

    private int menuMask;

    
    public StandardStateHandler(ImagesPanel ctx) {
        this.ctx = ctx;
        this.timer = new Timer();
        menuMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        System.out.println("Mask is "+menuMask);
        startScrollTimer();
    }
    
    // Mouse over, show big version of image by updating the selected image
    public void mouseMoved(MouseEvent e) {
        {
            CoverDetails cd = ctx.getSelectedAt(e);
            ctx.previewCover.setPreview(cd);
        }
        ctx.mainRepaint();
    }

    public void mouseDragged(MouseEvent e) {
        //System.out.println("Dragged at "+e.getX()+", "+e.getY()+(((e.getModifiersEx()&menuMask)>0)?" MENU":"")+e.getModifiersEx());
        if ( isCtrlDown(e) || (!editMode && !ctx.dragging) ) {
            if ( panCurr != null ) {
                Point pt = new Point(e.getX(), e.getY());
                if ( e.getComponent() != null ) {
                    SwingUtilities.convertPointToScreen(pt, e.getComponent());
                }
                int deltaX = pt.x - panCurr.x;
                int deltaY = pt.y - panCurr.y;
                prevPanCurr = panCurr;
                panCurr = new PointAtTime(pt.x, pt.y);
                //System.out.println("Set panCur to "+panCurr.x+", "+panCurr.y);
                ctx.getScrollPane().getHorizontalScrollBar().setValue(-deltaX + ctx.getScrollPane().getHorizontalScrollBar().getValue());
                ctx.getScrollPane().getVerticalScrollBar().setValue(-deltaY + ctx.getScrollPane().getVerticalScrollBar().getValue());
            }
        } else {
            if ( ctx.dragging || ctx.rubberBanding ) {
                ctx.dragCurr = new Point(e.getX(), e.getY());
                
                if ( ctx.rubberBanding ) {
                    Point startP = ctx.getXY(ctx.dragStart.x, ctx.dragStart.y);
                    Point endP = ctx.getXY(ctx.dragCurr.x, ctx.dragCurr.y);
                    int temp;
                    if ( startP.x > endP.x ) { temp = startP.x; startP.x = endP.x; endP.x = temp; }
                    if ( startP.y > endP.y ) { temp = startP.y; startP.y = endP.y; endP.y = temp; }
                    ctx.clearSelection();
                    for (CoverDetails cd : ctx.covers) {
                        if ( cd.getX() >= startP.x && cd.getX() <= endP.x && 
                                cd.getY() >= startP.y && cd.getY() <= endP.y ) {
                            ctx.addSelection(cd);
                        }
                    }
                }
                
                ctx.mainRepaint();
            }
        }
    }

    private boolean isCtrlDown(MouseEvent e) {
        return e.isControlDown() || e.isMetaDown();
    }

    private void doubleClick(MouseEvent e) {
        if ( e.getButton() == 1 && ctx.isSomethingSelected() ) {
            if ( ctx.getSelectedCovers().size() == 1 ) {
                for (CoverDetails cd : ctx.getSelectedCovers()) {
                    ctx.getPlayerInterface().enqueueAlbum(cd, false);
                }
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
        if ( e.getClickCount() == 2 ) {
            doubleClick(e);
        }
        if ( e.getButton() == 3 || e.isPopupTrigger() ) {
            final boolean shiftDown = e.isShiftDown();
            coverSelected = ctx.getSelectedAt(e);
            popupPos = ctx.getXY(e);

            if ( coverSelected != null ) {
                final JPopupMenu menu2 = new JPopupMenu();
                {
                    if ( shiftDown ) {
                        JMenuItem menuItem = new JMenuItem("Play immediately...");
                        menuItem.setEnabled(false);
                        menu2.add(menuItem);
                        menu2.addSeparator();
                    }
                    final List<CoverDetails> allDiscs = new LinkedList<CoverDetails>();
                    allDiscs.add(coverSelected);
                    if ( coverSelected.getOtherDiscs() != null ) {
                        allDiscs.addAll(coverSelected.getOtherDiscs());
                    }
                    boolean first = true;
                    for (final CoverDetails cover : allDiscs) {
                        if ( first ) {
                            first = false;
                        } else {
                            menu2.addSeparator();
                        }
                        JMenuItem menuItem = new JMenuItem(cover.getArtist()+" - "+cover.getAlbum());
                        menuItem.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                ctx.getPlayerInterface().enqueueAlbum(cover, shiftDown);
                            }
                        });
                        menu2.add(menuItem);
                        menu2.addSeparator();
                        int counter = 0;
                        for (final TrackDetails track : cover.getTrackNames()) {
                            counter++;
                            menuItem = new JMenuItem(counter+" - "+track.getTitle()+" ["+formatTrackTime(track.getLengthSeconds())+"]");
                            menuItem.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    ctx.getPlayerInterface().enqueueTrack(cover, track.getTitle(), shiftDown);
                                }
                            });
                            menu2.add(menuItem);
                        }
                    }
                    if ( editMode ) {
                        menu2.addSeparator();
                        JMenuItem menuItem = new JMenuItem("Hide album");
                        menuItem.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                for (final CoverDetails cover : allDiscs) {
                                    cover.setHidden(true);
                                }
                                ctx.mainViewer.saveLayout();
                                ctx.mainRepaint();
                            }
                        });
                        menu2.add(menuItem);
                        menuItem = new JMenuItem("Toggle various artists");
                        menuItem.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                for (final CoverDetails cover : allDiscs) {
                                    cover.setVariousArtists(!cover.isVariousArtists());
                                }
                                ctx.mainViewer.saveLayout();
                                ctx.mainRepaint();
                            }
                        });
                        menu2.add(menuItem);
                        menuItem = new JMenuItem("Remove");
                        menuItem.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                for (final CoverDetails cover : allDiscs) {
                                    ctx.removeCover(cover);
                                }
                                ctx.mainRepaint();
                            }
                        });
                        menu2.add(menuItem);
                    }
                }
                menu2.show(e.getComponent(), e.getX(), e.getY());
                MainViewer.registerVisibleMenu(menu2);
            }
        }
    }

    private String formatTrackTime(int length) {
        int secs = (length%60);
        return "" + (int)(length/60)+":"+(secs<10?"0":"")+secs;
    }

    public void mousePressed(MouseEvent e) {
        Point pt = new Point(e.getX(), e.getY());
        if ( e.getComponent() != null ) {
            SwingUtilities.convertPointToScreen(pt, e.getComponent());
        }
        panCurr = new PointAtTime(pt.x, pt.y);
        prevPanCurr = panCurr;
        if ( e.getButton() == 1 ) {
            CoverDetails temp = ctx.getSelectedAt(e);
            if ( !isCtrlDown(e) ) {
                if ( editMode || (temp != null && temp.isUndefinedPosition()) ) {
                    if ( !e.isShiftDown() && !ctx.isSelected(temp) ) {
                        ctx.clearSelection();
                    }
                    if ( e.isShiftDown() && ctx.isSelected(temp) ) {
                        ctx.removeSelection(temp);
                    } else {
                        ctx.addSelection(temp);
                    }
                    if ( temp != null ) {
                        ctx.dragging = true;
                    } else {
                        ctx.rubberBanding = true;
                    }
                }
            }
            ctx.dragStart = new Point(e.getX(), e.getY());
            ctx.dragCurr = new Point(e.getX(), e.getY());
            
            ctx.mainRepaint();
        }
    }

    public void mouseReleased(MouseEvent e) {
        System.out.println("MouseReleased at "+e.getX()+", "+e.getY());
        if ( ctx.dragging ) {
            // Move all the dragged covers
            ctx.dragging = false;
            Point startP = ctx.getXY(ctx.dragStart.x, ctx.dragStart.y);
            Point endP = ctx.getXY(ctx.dragCurr.x, ctx.dragCurr.y);
            Point pDelta = new Point(endP.x-startP.x, endP.y-startP.y);
            if ( pDelta.x != 0 || pDelta.y != 0 ) {
                ctx.doAction(new MoveAction(ctx, ctx.getSelectedCovers(), pDelta));
            }
            if ( !editMode ) {
                ctx.clearSelection();
            }
            ctx.mainRepaint();
        } else if ( ctx.rubberBanding ) {
            // Select all the ones that the box touches
            ctx.rubberBanding = false;
            ctx.mainRepaint();
        } else {
            if ( isCtrlDown(e) || (!editMode && !ctx.dragging) ) {
                // Stopping drag
                Point pt = new Point(e.getX(), e.getY());
                if ( e.getComponent() != null ) {
                    SwingUtilities.convertPointToScreen(pt, e.getComponent());
                }
                double timeDelta = (panCurr.time-prevPanCurr.time)/1000000000.0d;
                if ( timeDelta == 0 ) {
                    timeDelta=1;
                }
                scrollPanDeltaX = -((prevPanCurr.x-panCurr.x)/timeDelta) /100;
                scrollPanDeltaY = -((prevPanCurr.y-panCurr.y)/timeDelta) /100;
                System.out.println("Delta is "+scrollPanDeltaX+", "+scrollPanDeltaY+", timeDelta="+timeDelta);
                startScrollTimer();
            }
        }
    }
    
    private volatile double scrollPanDeltaX=0;
    private volatile double scrollPanDeltaY=0;
    private TimerTask oldTimerTask = null;
    private void startScrollTimer() {
        synchronized (this) {
            if ( oldTimerTask != null ) {
                oldTimerTask.cancel();
                oldTimerTask = null;
            }
        }
        timer.schedule(oldTimerTask=new TimerTask() {
            @Override
            public void run() {
                if ( (int)scrollPanDeltaX != 0 || (int)scrollPanDeltaY != 0 ) {
                    final int deltaX = (int) scrollPanDeltaX;
                    final int deltaY = (int) scrollPanDeltaY;
                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {
                            public void run() {
                                ctx.getScrollPane().getHorizontalScrollBar().setValue(-deltaX + ctx.getScrollPane().getHorizontalScrollBar().getValue());
                                ctx.getScrollPane().getVerticalScrollBar().setValue(-deltaY + ctx.getScrollPane().getVerticalScrollBar().getValue());
                            }
                        });
                        double decel = 1.03;
                        scrollPanDeltaX = scrollPanDeltaX / decel;
                        scrollPanDeltaY = scrollPanDeltaY / decel;
                    } catch (InterruptedException e) {
                    } catch (InvocationTargetException e) {
                    }
                } else {
                    synchronized (this) {
                        oldTimerTask = null;
                    }
                    cancel();
                }
            }
        }, 10, 10);
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
        ctx.clearSelection();
    }
}
