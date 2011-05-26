package com.project944.cov;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.bff.slimserver.SlimPlayer;
import org.bff.slimserver.SlimServer;
import org.bff.slimserver.exception.SlimConnectionException;

import com.project944.cov.extplayer.PlayerInterface;
import com.project944.cov.extplayer.SlimPlayerInterface;
import com.project944.cov.layoutmanagers.CoversLayoutManager;
import com.project944.cov.layoutmanagers.FilePersistedLayout;
import com.project944.cov.sources.CachedOnFileSystemCS;
import com.project944.cov.sources.CoverSource;
import com.project944.cov.sources.SlimCoverSource;
import com.project944.cov.utils.MyLogger;
import com.project944.cov.utils.PropsUtils;


public class MainViewer extends JFrame implements MyLogger {
    private final CoversLayoutManager coversLayoutManager;
	private List<CoverDetails> covers = new LinkedList<CoverDetails>();
	
	private ImagesPanel imagesPanel;

	private Executor executor = Executors.newSingleThreadExecutor();
	private JScrollPane scrollPane;
    private PlayerInterface playerInterface;
    private int sideBorder = 20;
    private PropsUtils props;
    private CoverSource coverSource;

    private JLabel statusBar;

    private Timer timer;

	
	public MainViewer(CoversLayoutManager coversLayoutManager, PlayerInterface playerInterface, PropsUtils props, CoverSource coverSource) {
	    this.coversLayoutManager = coversLayoutManager;
		this.playerInterface = playerInterface;
		this.props = props;
		this.coverSource = coverSource;
		setup();

        this.timer = new Timer();
		this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                executor.execute(new Runnable() {
                    public void run() {
                        log("Checking for new albums on server...");
                        List<CoverDetails> updatedCovers = MainViewer.this.coverSource.refreshFromServer(MainViewer.this.covers, false, MainViewer.this);
                        safeSetCovers(updatedCovers);
                        finished();
                    }
                });
            }
        }, 60*1000, 15*60*1000);  // Check number of albums not changed every 15 minutes
	}
	
	public void log(final String msg) {
	    if ( msg.length() > 0 ) {
	        System.out.println(msg);
	    }
	    SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if ( statusBar != null ) {
                    statusBar.setText(msg);
                }
            }
	    });
	}
	public void finished() {
	    log("");
	}

    private void safeSetCovers(final List<CoverDetails> updatedCovers) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                covers.clear();
                covers.addAll(updatedCovers);
                coversLayoutManager.layout(covers);
                MainViewer.this.repaint();
            }
        });
    }

	/**
	 * Setup the gui window
	 */
	private void setup() {
	    addWindowFocusListener(new WindowFocusListener() {
            public void windowLostFocus(WindowEvent e) {
                while ( visibleMenus.size() > 0 ) {
                    JPopupMenu menu = visibleMenus.remove(0);
                    if ( menu.isVisible() ) {
                        menu.setVisible(false);
                    }
                }
            }
            public void windowGainedFocus(WindowEvent e) {
            }
        });
		
		MenuBar mainMenu = new MenuBar();
		Menu actionsMenu = new Menu("Actions");
		{
    		MenuItem item = new MenuItem("Refresh from server");
    		item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    executor.execute(new Runnable() {
                        public void run() {
                            log("Refresh updates from server...");
                            List<CoverDetails> updatedCovers = coverSource.refreshFromServer(covers, true, MainViewer.this);
                            safeSetCovers(updatedCovers);
                        }
                    });
                }
            });
    		actionsMenu.add(item);
		}
		{
		    MenuItem item = new MenuItem("Full-refresh from server");
		    item.addActionListener(new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		            executor.execute(new Runnable() {
		                public void run() {
		                    log("Full refresh from server...");
		                    List<CoverDetails> noCovers = new ArrayList<CoverDetails>();
		                    List<CoverDetails> updatedCovers = coverSource.refreshFromServer(noCovers, true, MainViewer.this);
		                    safeSetCovers(updatedCovers);
		                }
		            });
		        }
		    });
		    actionsMenu.add(item);
		}
		{
		    MenuItem item = new MenuItem("Clear status");
		    item.addActionListener(new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
                    executor.execute(new Runnable() {
                        public void run() {
                            finished();
                        }
                    });
		        }
		    });
		    actionsMenu.add(item);
		}
		{
		    MenuItem item = new MenuItem("Change server hostname");
		    item.addActionListener(new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		            ServerConnections serverConnections = getServerConnections(props, true);
		            if ( serverConnections != null ) {
		                playerInterface = getPlayerInterface(props, serverConnections.slimServer, false);
		                coverSource = serverConnections.coverSource;
		            }
		        }
		    });
		    actionsMenu.add(item);
		}
		{
		    MenuItem item = new MenuItem("Change player");
		    item.addActionListener(new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		            ServerConnections serverConnections = getServerConnections(props, true);
		            if ( serverConnections != null ) {
		                playerInterface = getPlayerInterface(props, serverConnections.slimServer, true);
		                coverSource = serverConnections.coverSource;
		            }
		        }
		    });
		    actionsMenu.add(item);
		}
		mainMenu.add(actionsMenu);
		
		Menu aboutMenu = new Menu("Help");
		MenuItem helpMI = new MenuItem("Overview");
		helpMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final JDialog dlg = new JDialog(MainViewer.this, true);
				dlg.setLayout(new BorderLayout());
				String helpTxt = "Overview:\n";
				helpTxt += "The only argument required is a directory to scan.  This assumes that music and cover images (jpg and tiff only) are stored under  artist/album directory structure from the given directory.  e.g. If given  /tmp  then would expect to find some art in the directory  /tmp/prince/Purple-Rain  .\n";
				helpTxt += "o  The area above the dark bar is the main cover layout area.\n";
				helpTxt += "o  The area below the dark bar is the spare shelf, where new covers which have not yet been layed out go, or covers that have been dragged off screen etc\n";
				helpTxt += "o  Shift+click to select multiple\n";
				helpTxt += "o  Double click to play an album (requires patched version of Play running)\n";
				helpTxt += "o  Drag from space to rubberband select multiple covers\n";
				helpTxt += "o  Control+drop to force move to happen even if overlapping\n";
				helpTxt += "o  Shift+drag to pan around album covers area\n";
				helpTxt += "o  Right click - menu";
				helpTxt += "o  Type into the Find box to only show albums/artists containing the search terms\n";
				helpTxt += "\n";
				helpTxt += "\n";
				JTextArea txt = new JTextArea(helpTxt);
				txt.setLineWrap(true);
				txt.setWrapStyleWord(true);
				dlg.add(new JScrollPane(txt));
				JButton closeBtn = new JButton("Close");
				closeBtn.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dlg.setVisible(false);
						dlg.dispose();
					}
				});
				dlg.add(closeBtn, BorderLayout.SOUTH);
				dlg.setSize(400, 300);
				dlg.setVisible(true);
			}
		});
		aboutMenu.add(helpMI);
		mainMenu.add(aboutMenu);
		setMenuBar(mainMenu);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(new Dimension(props.getInt(PropsUtils.mainWindowWidth), props.getInt(PropsUtils.mainWindowHeight)));
		addComponentListener(new ComponentAdapter() {
		    @Override
		    public void componentResized(ComponentEvent e) {
		        props.setInt(PropsUtils.mainWindowWidth, getWidth());
		        props.setInt(PropsUtils.mainWindowHeight, getHeight());
		        props.save();
		    }
        });
		setTitle("Album Cover Viewer");
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setOpaque(true); mainPanel.setBackground(SystemColor.window);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mainPanel);

		// Search string
		final Document srchDoc = new PlainDocument();
		final JTextField srchField = new JTextField(srchDoc, "", 20);
		Action srchFieldGetFocus = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Requesting focus");
                srchField.requestFocus();
            }
        };
		srchField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if ( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
					try {
						srchDoc.remove(0, srchDoc.getLength());
					} catch (BadLocationException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		srchDoc.addDocumentListener(new DocumentListener() {
			public void changed(DocumentEvent e) {
				try {
					String srchText = srchDoc.getText(0, srchDoc.getLength());
					imagesPanel.setSrchText(srchText);
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
			}
			public void changedUpdate(DocumentEvent e) { changed(e); }
			public void insertUpdate(DocumentEvent e) { changed(e); }
			public void removeUpdate(DocumentEvent e) { changed(e); }
		});
		JPanel topPanel = new JPanel(new BorderLayout());
		JPanel outerSrchPanel = new JPanel(new BorderLayout());
		JPanel buttonsPanel = new JPanel(new FlowLayout());
		final JButton playPauseButton = new JButton(">");
		{
    		JButton button = new JButton("<<");
    		button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    playerInterface.playPrev();
                }
            });
    		button.setFont(getSmallerFont(button.getFont()));
    		buttonsPanel.add(button);
		}
		{
		    JButton button = playPauseButton;
		    button.addActionListener(new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		            if ( ">".equals(playPauseButton.getText()) ) {
		                playerInterface.play();
		            } else {
		                playerInterface.pause();
		            }
		        }
		    });
		    button.setFont(getSmallerFont(button.getFont()));
		    buttonsPanel.add(button);
		}
		{
		    JButton button = new JButton(">>");
		    button.addActionListener(new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		            playerInterface.playNext();
		        }
		    });
		    button.setFont(getSmallerFont(button.getFont()));
		    buttonsPanel.add(button);
		}
		{
		    JButton button = new JButton("X");
		    button.addActionListener(new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		            playerInterface.clearPlayQueue();
		        }
		    });
		    button.setFont(getSmallerFont(button.getFont()));
		    buttonsPanel.add(button);
		}
		playerInterface.setPlayPauseButton(playPauseButton);
		topPanel.add(outerSrchPanel, BorderLayout.NORTH);

		// Preview pane
		final JPanel previewAndScaler = new JPanel(new BorderLayout());
		final PreviewCoverPanel previewCover = new PreviewCoverPanel(this);
		previewAndScaler.add(previewCover);
        previewAndScaler.setBorder(BorderFactory.createMatteBorder(0, 0, sideBorder/2, 0, SystemColor.window));
		
        imagesPanel = new ImagesPanel(this, covers, playerInterface, previewCover, props.getInt(PropsUtils.iconSize));
        scrollPane = new JScrollPane(imagesPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        imagesPanel.setScrollPane(scrollPane);

		// Make a slider for trying out different album sizes
        JPanel outerSliderPanel = new JPanel(new BorderLayout());
        JPanel sliderEtcPanel = new JPanel();
        sliderEtcPanel.setLayout(new BoxLayout(sliderEtcPanel, BoxLayout.PAGE_AXIS));
        
        final JLabel sliderLabel = new JLabel("Icon size: "+imagesPanel.getSz()+"x"+imagesPanel.getSz(), SwingConstants.CENTER);
        sliderLabel.setFont(getSmallerFont(sliderLabel.getFont()));
        JPanel sliderLabelP = new JPanel(new BorderLayout());
        sliderLabelP.add(sliderLabel);
        sliderLabel.setVisible(false);
        sliderEtcPanel.add(sliderLabelP);
        
		final JSlider slider = new JSlider(5, 20);
		slider.setSnapToTicks(true);
		slider.setMajorTickSpacing(5);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(false);
		slider.setValue(imagesPanel.getSz()/4);
		slider.setVisible(false);
		sliderEtcPanel.add(slider);
		
		final JCheckBox editModeCheckbox = new JCheckBox("Edit positions");
		editModeCheckbox.setFont(getSmallerFont(editModeCheckbox.getFont()));
		editModeCheckbox.setHorizontalTextPosition(JCheckBox.LEADING);
		
		JPanel editModeCheckboxP = new JPanel(new BorderLayout());
		editModeCheckboxP.add(editModeCheckbox, BorderLayout.EAST);
		sliderEtcPanel.add(editModeCheckboxP);
		
		outerSliderPanel.add(sliderEtcPanel, BorderLayout.SOUTH);

		JPanel srchFieldPanelW = new JPanel(new BorderLayout());
		srchFieldPanelW.setLayout(new BoxLayout(srchFieldPanelW, BoxLayout.PAGE_AXIS));
		JPanel srchFieldPanel = new JPanel(new BorderLayout());
		srchFieldPanel.add(new JLabel("Search"), BorderLayout.WEST);
		srchFieldPanel.add(srchField);
        srchFieldPanelW.add(srchFieldPanel);
        srchFieldPanelW.add(buttonsPanel);

		outerSliderPanel.add(srchFieldPanelW, BorderLayout.NORTH);

		
		previewAndScaler.add(outerSliderPanel, BorderLayout.EAST);

		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
			    int sz = slider.getValue() * 4;
				imagesPanel.setSz(sz);
				props.setInt(PropsUtils.iconSize, sz);
				props.save();
				sliderLabel.setText("Icon size: "+imagesPanel.getSz()+"x"+imagesPanel.getSz());
				getContentPane().validate();
				imagesPanel.invalidate();
				MainViewer.this.repaint();
			}
		});
        editModeCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean editMode = editModeCheckbox.isSelected();
                sliderLabel.setVisible(editMode);
                slider.setVisible(editMode);
                imagesPanel.setEditMode(editMode);
            }
        });

		topPanel.add(previewAndScaler);
		mainPanel.add(topPanel, BorderLayout.NORTH);
	      
		scrollPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, SystemColor.controlShadow, SystemColor.controlShadow));
        mainPanel.add(scrollPane);
        statusBar = new JLabel("");
        statusBar.setPreferredSize(new Dimension(500, sideBorder));
        mainPanel.add(statusBar, BorderLayout.SOUTH);
        mainPanel.setBorder(BorderFactory.createMatteBorder(sideBorder/2, sideBorder, 0, sideBorder, SystemColor.window));
        mainPanel.registerKeyboardAction(srchFieldGetFocus, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        mainPanel.registerKeyboardAction(srchFieldGetFocus, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        mainPanel.registerKeyboardAction(srchFieldGetFocus, KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        mainPanel.registerKeyboardAction(srchFieldGetFocus, KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        
        executor.execute(new Runnable() {
            public void run() {
                covers.addAll(coverSource.getCovers(MainViewer.this));
                coversLayoutManager.layout(covers);
                repaint();
                finished();
            }
        });
	}
    
    public Font getSmallerFont(Font f) {
        int style = f.getStyle();
        String fn = f.getFontName();
        return new Font(fn, style, f.getSize()-2);
    }

	public void saveLayout() {
		try {
			coversLayoutManager.save(covers);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	private static String noteImage      = "/com/project944/cov/resources/music_notes.jpg";

	private static class ServerConnections {
	    public SlimServer slimServer;
        public CoverSource coverSource;
        public ServerConnections(SlimServer slimServer, CoverSource coverSource) {
            super();
            this.slimServer = slimServer;
            this.coverSource = coverSource;
        }
	}

    public static ServerConnections getServerConnections(PropsUtils props, boolean askForServerHost) {
        String serverUrl = props.getString(PropsUtils.serverHost);
        String oldServerUrl = serverUrl;
        while ( true ) {
            if ( askForServerHost ) {
                serverUrl = JOptionPane.showInputDialog("SqueezeServerHost: ", serverUrl);
                if ( serverUrl == null ) {
                    return null;
                }
            }
            askForServerHost = true;  // On failure ask them
            SlimServer slimServer = null;
            try {
                System.out.println("Trying to connect to " + serverUrl);
                slimServer = new SlimServer(serverUrl);
                CoverSource coverSource = new SlimCoverSource(slimServer, noteImage, new CachedOnFileSystemCS());
                if ( !serverUrl.equals(oldServerUrl) ) {
                    props.setString(PropsUtils.serverHost, serverUrl);
                    props.save();
                }
                return new ServerConnections(slimServer, coverSource);
            } catch (SlimConnectionException e) {
                JOptionPane.showMessageDialog(null, "Failed to connect with "+e);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Failed to get covers with "+e);
            }
        }
    }
    
    private static PlayerInterface getPlayerInterface(PropsUtils props, SlimServer slimServer, boolean wantChoose) {
        Collection<SlimPlayer> players = slimServer.getSlimPlayers();
        String playerName = props.getString(PropsUtils.playerName);
        for (SlimPlayer player : players) {
            try {
                if ( playerName.equals("*") || playerName.equals(player.getName()) ) {
                    return new SlimPlayerInterface(slimServer, player);
                }
            } catch (SlimConnectionException e) {
                System.out.println("Failed to get player name with "+e);
            }
        }
        return null;
    }

    
	public static void main(String[] args) throws Exception {
	    PropsUtils props = new PropsUtils();
		ImagesPanel.loadImages();
        ServerConnections serverConnection = getServerConnections(props, false);
        if ( serverConnection == null ) {
            JOptionPane.showMessageDialog(null, "Failed to connect to server, exiting");
            System.exit(2);
        }
        FilePersistedLayout coversLayoutManager = new FilePersistedLayout("cv-layout.txt");
		// Now show all the images?
        PlayerInterface playerInterface = getPlayerInterface(props, serverConnection.slimServer, false);
        if ( playerInterface == null ) {
            System.out.println("Failed to find player?");
        }

		MainViewer g = new MainViewer(coversLayoutManager, playerInterface, props, serverConnection.coverSource);
		g.setVisible(true);
	}

    static List<JPopupMenu> visibleMenus = new LinkedList<JPopupMenu>();
    public static void registerVisibleMenu(JPopupMenu menu2) {
        visibleMenus.add(menu2);
    }
}

	