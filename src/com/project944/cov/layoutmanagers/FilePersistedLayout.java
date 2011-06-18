package com.project944.cov.layoutmanagers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.project944.cov.CoverDetails;
import com.project944.cov.utils.PropsUtils;

public class FilePersistedLayout implements CoversLayoutManager {
    
    private static final LayoutJsonSerializer ser = new LayoutJsonSerializer();

	private String filename;

	public FilePersistedLayout(String filename) {
		this.filename = filename;
	}

	public void layout(List<CoverDetails> covers) {
		// Default all to spares
		for (CoverDetails cd : covers) {
			cd.setUndefinedPosition(true);
		}
		// Override any that are in the file with saved values
		File file = new File(PropsUtils.getCacheDir()+"/"+filename);
		if ( file.exists() ) {
			try {
				FileReader fr = new FileReader(file);
				BufferedReader rd = new BufferedReader(fr);
				String line = rd.readLine();
				int lineNum = 0;
				if ( line.equals("JSON") ) {
				    lineNum++;
				    line = rd.readLine();
				    while ( line != null ) {
				        lineNum++;
				        try {
    	                    JSONObject json = new JSONObject(new JSONTokener(line));
    				        LayoutDetails details = ser.deserialize(json);
                            // Find correct album
                            for (CoverDetails cd : covers) {
                                if ( (cd.getArtist().equals(details.artist) || details.variousArtists )
                                        && cd.getAlbum().equals(details.album) ) {
                                    cd.setUndefinedPosition(details.undefPosn);
                                    cd.setHidden(details.hidden);
                                    cd.setVariousArtists(details.variousArtists);
                                    cd.setXY(details.x, details.y);
                                    break;
                                }
                            }
				        } catch (Exception e) {
				            System.out.println("Failed to parse line "+lineNum+" in file "+file.getAbsolutePath());
				            e.printStackTrace();
				        }
				        line = rd.readLine();
                    }
				} else {
				    // Old format
    				while ( line != null ) {
    					String[] bits = line.split("\\|");
    					if ( bits.length > 4 ) {
    						String artist = bits[0];
    						String album = bits[1];
    						int x = Integer.parseInt(bits[2]);
    						int y = Integer.parseInt(bits[3]);
    						boolean undefPosition = Boolean.parseBoolean(bits[4]) || x < 0 || y < 0;
    						boolean hidden = Boolean.parseBoolean(bits[5]);
    						boolean variousArtists = Boolean.parseBoolean(bits[6]);
    						// Find correct album
    						for (CoverDetails cd : covers) {
    							if ( (cd.getArtist().equals(artist) || variousArtists )
    									&& cd.getAlbum().equals(album) ) {
    								cd.setUndefinedPosition(undefPosition);
    								cd.setHidden(hidden);
    								cd.setVariousArtists(variousArtists);
    								cd.setXY(x, y);
    								break;
    							}
    						}						
    					}
    					line = rd.readLine();
    				}
				}
				CoverDetails.fixMultiDiscAlbums(covers);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void save(List<CoverDetails> covers) throws Exception {
		File file = new File(PropsUtils.getCacheDir()+"/"+filename);
		FileWriter fw = new FileWriter(file);
		fw.write("JSON\n");
		BufferedWriter bw = new BufferedWriter(fw);
		for (CoverDetails cd : covers) {
            JSONObject json = ser.serialize(cd);
            json.write(fw);
            fw.write("\n");
//			String str = "";
//			str += cd.getArtist();
//			str += "|";
//			str += cd.getAlbum();
//			str += "|";
//			str += cd.getX();
//			str += "|";
//			str += cd.getY();
//			str += "|";
//			str += cd.isUndefinedPosition();
//			str += "|";
//			str += cd.isHidden();
//			str += "|";
//			str += cd.isVariousArtists();
//			str += "\n";
//			bw.write(str);
		}
		bw.close();
		fw.close();
	}
}
