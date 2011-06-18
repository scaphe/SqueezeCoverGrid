package com.project944.cov;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.SystemColor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.image.RenderedImage;
import java.io.File;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import com.project944.cov.utils.JPanel2;
import com.project944.cov.utils.PropsUtils;

/**
 * Show bigger image
 */
public class PreviewCoverPanel extends JPanel implements DropTargetListener {
	private DropTarget dropTarget = new DropTarget(this, this);

	public static int IMAGE_SIZE = 120;
	private static int GAP_SIZE = 8;
	
	private MainViewer mainViewer;

	private CoverDetails previewCover;

	public PreviewCoverPanel(MainViewer mainViewer) {
		this.mainViewer = mainViewer;
		setLayout(null);
		setPreferredSize(new Dimension( (IMAGE_SIZE+2*GAP_SIZE) *3, IMAGE_SIZE+2*GAP_SIZE+1));
		JPanel borderPanel = new JPanel2();
		borderPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, SystemColor.controlShadow, SystemColor.controlShadow));
		add(borderPanel);
		borderPanel.setBounds(0, 0, GAP_SIZE+IMAGE_SIZE+GAP_SIZE, GAP_SIZE+IMAGE_SIZE+GAP_SIZE);

		setOpaque(true);
		setBackground(SystemColor.window);
	}
	
	public static class ArtistAlbum {
	    public final String artist;
	    public final String album;
        public ArtistAlbum(String artist, String album) {
            super();
            this.artist = artist;
            this.album = album;
        }
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
		        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//		g.setColor(SystemColor.controlDkShadow);
//		g.drawRoundRect(
//		        0, 0,
//		        GAP_SIZE+IMAGE_SIZE+GAP_SIZE,
//		        GAP_SIZE+IMAGE_SIZE+GAP_SIZE,
//		        7, 7);
		ArtistAlbum aa = getArtistAlbum();
        if ( getSelectedCover() != null ) {
            g.drawImage(getSelectedCover().getImage(),
                    GAP_SIZE, GAP_SIZE,
                    GAP_SIZE+IMAGE_SIZE, GAP_SIZE+IMAGE_SIZE,
                    0, 0, 
                    getSelectedCover().getWidth(), getSelectedCover().getHeight(), null);
        }
		// Draw text
		g.setColor(Color.black);
		setFontBold(g);
		int fheight = g.getFontMetrics().getHeight();
		int offset = fheight*1;
		g.drawString(aa.artist, GAP_SIZE+IMAGE_SIZE+GAP_SIZE+GAP_SIZE, GAP_SIZE+IMAGE_SIZE/2 +fheight/2 - offset);
		g.drawString(aa.album, GAP_SIZE+IMAGE_SIZE+GAP_SIZE+GAP_SIZE, GAP_SIZE+IMAGE_SIZE/2 +fheight/2 + offset);
	}
	
    private ArtistAlbum getArtistAlbum() {
        String artist = "Artist";
        String album = "Album";
        if ( getSelectedCover() != null ) {
            artist = getSelectedCover().getArtist();
            int numDiscs = getSelectedCover().getOtherDiscs()==null?0:getSelectedCover().getOtherDiscs().size()+1;
            if ( numDiscs > 0 ) {
                artist += " ("+numDiscs+" discs)";
            }
            album = CoverDetails.trimDisc(getSelectedCover().getAlbum());
        }
        return new ArtistAlbum(artist, album);
    }

    public void setFontBold(Graphics g) {
        Font f = g.getFont();
        //System.out.println("Font name is "+f.getFontName());
        int style = f.getStyle();
        style = Font.BOLD;
        String fn = f.getFontName();
//        fn = "Arial";
//        fn = "SansSerif";
        g.setFont(new Font(fn, style, f.getSize()));
    }

	private CoverDetails getSelectedCover() {
		return previewCover;
	}

	public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
		dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
	}

	public void dragExit(DropTargetEvent dte) {
	}

	public void dragOver(DropTargetDragEvent dtde) {
	}

	public void drop(DropTargetDropEvent dropTargetDropEvent) {
		Transferable tr = dropTargetDropEvent.getTransferable();
		System.out.println("Got drop");
		if ( tr.isDataFlavorSupported(DataFlavor.stringFlavor) ) {
			dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
			try {
				String data = (String) tr.getTransferData(DataFlavor.stringFlavor);
				System.out.println("Got string of "+data);
				if ( data.startsWith("http:") ) {
					// Happy with this, read the file and save where it should go
					String dirName = getSelectedCover().getDirName();
					URL url = new URL(data);
					RenderedImage image = ImageIO.read(url);
					ImageIO.write(image, "jpg", new File(dirName+"/cover.jpg"));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("Rejected");
			dropTargetDropEvent.rejectDrop();
		}
		
	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	public void setPreview(CoverDetails selectedAt) {
		previewCover = selectedAt;
        if ( mainViewer.props.getInt(PropsUtils.ctrlPanelVisible) == 0 ) {
	        ArtistAlbum aa = getArtistAlbum();
		    mainViewer.log(aa.artist+" : "+aa.album);
		}
	}
}
