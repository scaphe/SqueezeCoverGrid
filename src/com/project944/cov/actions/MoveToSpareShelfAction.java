package com.project944.cov.actions;

import java.util.Set;

import com.project944.cov.CoverDetails;
import com.project944.cov.MainViewer;

public class MoveToSpareShelfAction implements UndoableAction {

	private Set<CoverDetails> sels;
	private MainViewer viewer;

	/**
	 * Constructor
	 * @param viewer
	 * @param sels
	 */
	public MoveToSpareShelfAction(MainViewer viewer, Set<CoverDetails> sels) {
		this.viewer = viewer;
		this.sels = sels;
	}

	public void run() {
		for (CoverDetails cd : sels) {
			cd.setUndefinedPosition(true);
		}
		viewer.saveLayout();
	}

}
