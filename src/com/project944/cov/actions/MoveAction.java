package com.project944.cov.actions;

import java.awt.Point;
import java.util.Set;

import com.project944.cov.CoverDetails;
import com.project944.cov.ImagesPanel;

public class MoveAction implements UndoableAction {

	private Set<CoverDetails> sels;
	private Point pDelta;
	private ImagesPanel viewer;

	/**
	 * Constructor
	 * @param viewer
	 * @param sels
	 * @param pDelta
	 * @param forceMove if false the move can fail if will end up with two things in the same space
	 */
	public MoveAction(ImagesPanel viewer, Set<CoverDetails> sels, Point pDelta) {
		this.viewer = viewer;
		this.sels = sels;
		this.pDelta = pDelta;
	}

	public void run() {
		boolean failDrag = false;
		for (int pass = 0; pass < 2; pass++) {
			if ( failDrag ) {
				break;
			}
			for (CoverDetails cd : sels) {
				// Should we fail all covers in the drag if one is over another cover
				Point p = new Point(cd.getX()+pDelta.x, cd.getY()+pDelta.y);
				if ( pass == 0 ) {
				    if ( p.x < 0 || p.y < 0 ) {
				        failDrag = true;
				        break;
				    }
					CoverDetails curr = viewer.getSelectedAt(p.x, p.y);
					if ( curr != null && !sels.contains(curr) && !curr.isUndefinedPosition() ) {
						System.out.println("Already got "+viewer.getSelectedAt(p.x, p.y)+" at "+p);
						failDrag = true;
						break;
					}
				} else {
					cd.setHidden(false);
					cd.setUndefinedPosition(false);
					cd.setXY(p.x, p.y);
				}
			}
		}
		viewer.mainViewer.saveLayout();
	}

}
