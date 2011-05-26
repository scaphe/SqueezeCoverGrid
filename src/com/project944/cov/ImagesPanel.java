package com.project944.cov;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.project944.cov.actions.UndoableAction;
import com.project944.cov.extplayer.PlayerInterface;
import com.project944.cov.states.StandardStateHandler;

public class ImagesPanel extends JPanel {
    private static CoverDetails blankCoverDetails;
    private static CoverDetails darkBlankCoverDetails;
    private static String blankImage     = "/com/project944/cov/resources/blank.tiff";
    private static String darkBlankImage = "/com/project944/cov/resources/dblank.tiff";

    static void loadImages() throws IOException {
        blankCoverDetails = new CoverDetails(-1, null, blankImage, null, null); 
        darkBlankCoverDetails = new CoverDetails(-2, null, darkBlankImage, null, null); 
    }
    
    private volatile int sz = 48;
    static private int ygap =  8;
    static private int xgap =  4;

    private int maxX = 15;
    private int maxY = 3;

    public boolean rubberBanding = false;
    
    public boolean dragging = false;
    public Point dragStart;
    public Point dragCurr;

    public List<CoverDetails> covers;
    private Set<CoverDetails> selectedCovers = new LinkedHashSet<CoverDetails>();
    private Set<CoverDetails> srchMatchingCovers = new LinkedHashSet<CoverDetails>();
    private StateSpecificHandler eventHandler;
    public MainViewer mainViewer;
    private JScrollPane scrollPane;
    public PlayerInterface playerInterface;
    public PreviewCoverPanel previewCover;
    private boolean editMode;

    public ImagesPanel(MainViewer mainViewer,
                       List<CoverDetails> covers,
                       PlayerInterface playerInterface,
                       PreviewCoverPanel previewCover,
                       int sz) {
        this.mainViewer = mainViewer;
        this.covers = covers;
        this.playerInterface = playerInterface;
        this.previewCover = previewCover;
        this.eventHandler = new StandardStateHandler(this);
        this.sz = sz;
        setupMouseEvents();
        setOpaque(true);
        setBackground(SystemColor.control);
    }

    private void setupMouseEvents() {
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                eventHandler.mouseMoved(e);
            }
            public void mouseDragged(MouseEvent e) {
                eventHandler.mouseDragged(e);
            }
        });
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                eventHandler.mouseClicked(e);
            }            
            public void mousePressed(MouseEvent e) {
                eventHandler.mousePressed(e);
            }
            public void mouseReleased(MouseEvent e) {
                eventHandler.mouseReleased(e);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw blank images all along each line as we get to the line
        // Get max Y, so know where to put spares shelf
        int prevMaxY = maxY;
        maxY = 3;
        maxX = 15;
        boolean firstSpare = true;
        int sparesX = 0;
        int sparesBoundryY = -1;
        boolean firstHidden = true;
        int hiddenX = 0;
        int hiddenBoundryY = -1;
        for (int pass = 0; pass < 3; pass++) {
            for (CoverDetails cd : covers) {
                if ( cd != null ) {
                    if ( cd.isPartOfOtherCover() ) {
                        // Not shown at all, position is meaningless
                    } else if ( cd.isUndefinedPosition() ) {
                        // Put on spare shelf (later)
                        if ( pass == 1 ) {
                            if ( firstSpare ) {
                                firstSpare = false;
                                maxY++;
                                sparesBoundryY = maxY;
                                maxY++;
                            }
                            cd.setXY(sparesX, maxY);
                            sparesX++;
                            if ( sparesX >= maxX ) {
                                // Run out of space on shelf, extend by 1 line
                                sparesX = 0; maxY++;
                            }
                        }
                    } else if ( cd.isHidden() ) {
                        // Put on hidden shelf (is after spares shelf, only visible in edit mode)
                        if ( pass == 2 && editMode ) {
                            if ( firstHidden ) {
                                firstHidden = false;
                                maxY++;
                                hiddenBoundryY = maxY;
                                maxY++;
                            }
                            cd.setXY(hiddenX, maxY);
                            hiddenX++;
                            if ( hiddenX >= maxX ) {
                                // Run out of space on shelf, extend by 1 line
                                hiddenX = 0; maxY++;
                            }
                        }
                    } else {
                        // See how big to make grid so can see all covers
                        maxX = Math.max(cd.getX(), maxX);
                        maxY = Math.max(cd.getY(), maxY);
                    }
                }
            }
        }
        
        maxX++;
        maxY++;  // Cos we want it to act like a length
        if ( prevMaxY != maxY ) {
            revalidate();
        }

        // Make gaps between covers white
        Point botRight = toScrnXY(maxX, maxY);
        Color oldCol = g.getColor();
        g.setColor(Color.white);
        g.fillRect(0, 0, (int)botRight.getX(), (int)botRight.getY());
        g.setColor(oldCol);

        // Draw blanks
        for (int y = 0; y < maxY; y++) {
            drawBlankLine(g, y, y==sparesBoundryY || y==hiddenBoundryY?darkBlankCoverDetails:blankCoverDetails);
        }

        //--- Draw the basic covers, missing out the selected ones
        // Draw all covers
        for (CoverDetails cd : covers) {
            if ( cd == null ) {
                log("Null cover found?");
            } else {
                if ( !cd.isPartOfOtherCover() && (editMode || !cd.isHidden()) ) {
                    Point scrnPos = toScrnXY(cd.getX(), cd.getY());
                    boolean selected = getSelectedCovers().contains(cd);
                    if ( selected ) {
                        drawHighlight(g, scrnPos);
                    }
                    
                    Image image = cd.getSmallImage(sz);
                    if ( srchMatchingCovers.size() > 0 && !srchMatchingCovers.contains(cd) ) {
                        // Draw all non-selected in grey
                        image = cd.getSmallGreyImage(this, sz);
                    }
                    Graphics g2 = g.create(scrnPos.x, scrnPos.y, sz, sz);
                    g2.drawImage(image, 0, 0, sz, sz, 0, 0, sz, sz, null);
                }
            }
        }
        
        //--- At this point we should just make changes to the base image
        
        // Show selected items at drag position
        if ( dragging && isSomethingSelected() ) {
            Set<CoverDetails> sel = getSelectedCovers();
            for (CoverDetails cd : sel) {
                Point scrnPos = toScrnXY(cd.getX(), cd.getY());
                scrnPos = new Point(scrnPos.x - dragStart.x  + dragCurr.x, scrnPos.y - dragStart.y + dragCurr.y);
                
                drawHighlight(g, scrnPos);
                
                // Draw image at drag position
                Graphics g2 = g.create(scrnPos.x, scrnPos.y, sz, sz);
                g2.drawImage(cd.getSmallImage(sz), 0, 0, sz, sz, 0, 0, sz, sz, null);
            }
        }
        if ( rubberBanding ) {
            Point startP = new Point(dragStart);
            Point endP = new Point(dragCurr);
            int temp;
            if ( startP.x > endP.x ) { temp = startP.x; startP.x = endP.x; endP.x = temp; }
            if ( startP.y > endP.y ) { temp = startP.y; startP.y = endP.y; endP.y = temp; }
            g.drawRect(startP.x, startP.y, endP.x-startP.x, endP.y-startP.y);
        }
    }
    
    private void drawHighlight(Graphics g, Point scrnPos) {
        Graphics g2 = g.create(scrnPos.x - xgap, scrnPos.y - ygap/2, sz+xgap*2, sz+ygap);
        g2.setColor(SystemColor.textHighlight);
        g2.fillRect(0, 0, sz+xgap*2, ygap/2);
        g2.fillRect(0, sz+ygap/2, sz+xgap*2, ygap/2);
        g2.fillRect(0, 0, xgap, sz+ygap*2);
        g2.fillRect(sz+xgap, 0, xgap, sz+ygap*2);
    }

    private void drawBlankLine(Graphics g, int y, CoverDetails blankCoverDetails) {
        for (int tx = 0; tx < maxX; tx++ ) {
            Point scrnPos = toScrnXY(tx, y);
            Graphics g2 = g.create(scrnPos.x, scrnPos.y, sz, sz);
            g2.drawImage(blankCoverDetails.getSmallImage(sz), 0, 0, sz, sz, 0, 0, sz, sz, null);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        int width = (xgap + sz) * maxX + xgap;
        int height = (ygap + sz) * maxY + ygap;
        return new Dimension(width, height);
    }
    
    public int getSz() {
        return sz;
    }
    public void setSz(int sz) {
        this.sz = sz;
    }
    
    private Point toScrnXY(int x, int y) {
        return new Point( x*(xgap+sz) + xgap, y*(ygap+sz) + ygap/2 );
    }

    public Point getXY(MouseEvent e) {
        return getXY(e.getX(), e.getY());
    }
    public Point getXY(int scrnX, int scrnY) {
        int x = scrnX / (xgap+sz);
        int y = ((int)(scrnY/(ygap+sz)));
        if ( scrnX < 0 ) x--;
        if ( scrnY < 0 ) y--;
        return new Point(x, y);
    }

    public CoverDetails getSelectedAt(MouseEvent e) {
        Point p = getXY(e);
        return getSelectedAt(p.x, p.y);
    }
    
    public CoverDetails getSelectedAt(int x, int y) {
        for (CoverDetails cd : covers) {
            if ( !editMode && cd.isHidden() ) {
                continue;
            }
            if ( !cd.isPartOfOtherCover() && cd.getX() == x && cd.getY() == y ) {
                return cd;
            }
        }
        return null;
    }

    public void clearSelection() {
        selectedCovers.clear();
    }

    public void addSelection(CoverDetails cd) {
        if ( cd != null && !cd.isPartOfOtherCover() ) {
            selectedCovers.add(cd);
        }
    }
    public void removeSelection(CoverDetails cd) {
        if ( cd != null ) {
            selectedCovers.remove(cd);
        }
    }
    public boolean isSomethingSelected() {
        return !selectedCovers.isEmpty();
    }
    public boolean isSelected(CoverDetails cd) {
        return selectedCovers.contains(cd);
    }
    public Set<CoverDetails> getSelectedCovers() {
        return selectedCovers;
    }

    public void setSrchText(String srchText) {
        srchText = srchText.toLowerCase();
        srchMatchingCovers.clear();
        if ( srchText.length() > 0 ) {
            for (CoverDetails cd : covers) {
                if ( cd.getArtist().toLowerCase().indexOf(srchText) >= 0 ||
                     cd.getAlbum().toLowerCase().indexOf(srchText) >= 0 ) {
                    srchMatchingCovers.add(cd);
                }
            }
        }
        mainViewer.repaint();
    }
    
    public void mainRepaint() {
        mainViewer.repaint();
    }

    public void setScrollPane(JScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }
    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public void doAction(UndoableAction action) {
        action.run();
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
        eventHandler.setEditMode(editMode);
        mainRepaint();
    }

    public void removeCover(CoverDetails cover) {
        covers.remove(cover);
    }
    
    public void log(String msg) {
        mainViewer.log(msg);
    }
}
