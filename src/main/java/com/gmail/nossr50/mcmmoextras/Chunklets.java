package com.gmail.nossr50.mcmmoextras;

import java.io.File;
import java.util.ArrayList;

public class Chunklets {
	public static void analyze(String worldLocation) {
		File worldDir = new File(worldLocation);
		if(!worldDir.exists()) {
			System.out.println("Cannot find world at: " + worldDir.getPath());
			return;
		}

		File chunkletsDir = new File(worldLocation, "mcmmo_data");
		if(!chunkletsDir.exists()) {
			System.out.println("Cannot find mcmmo_data folder in: " + worldDir.getPath());
			return;
		}

		// Scan

		System.out.println("Scanning mcmmo_data: ");
		Main.updateProgress(0);

		ArrayList<String> unknownFiles = new ArrayList<String>();

		int cxDirectories = 0;
		int emptyCxDirectories = 0;
		int czDirectories = 0;
		int emptyCzDirectories = 0;

		int chunkletsCount = 0;

		ArrayList<String> chunkletLocations = new ArrayList<String>();

		String[] cxDirs = chunkletsDir.list();
		for(int i = 0; i < cxDirs.length; i++) {
			File cxDir = new File(chunkletsDir, cxDirs[i]);

			if(!cxDir.isDirectory()) {
				unknownFiles.add(cxDir.getPath());
				continue;
			}
			cxDirectories++;

			String[] czDirs = cxDir.list();
			int emptyCzTemp = 0;
			for(int j = 0; j < czDirs.length; j++) {
				File czDir = new File(cxDir, czDirs[j]);

				if(!czDir.isDirectory()) {
					unknownFiles.add(czDir.getPath());
					continue;
				}
				czDirectories++;

				String[] chunklets = czDir.list();
				if(chunklets.length == 0) {
					emptyCzTemp++;
					emptyCzDirectories++;
				}

				for(int k = 0; k < chunklets.length; k++) {
					File chunklet = new File(czDir, chunklets[k]);

					chunkletLocations.add(chunklet.getPath());
					chunkletsCount++;
				}
			}

			if(emptyCzTemp == czDirs.length) {
				emptyCxDirectories++;
			}

			Main.updateProgress((double) i / cxDirs.length);
		}
		Main.updateProgress(1);
		System.out.println();

		System.out.println("Found " + chunkletsCount + " chunklets:");
		System.out.println("\t" + cxDirectories + " cx dirs, " + emptyCxDirectories + " empty");
		System.out.println("\t" + czDirectories + " cz dirs, " + emptyCzDirectories + " empty");

		if(!unknownFiles.isEmpty()) {
			System.out.println("The following files not part of the chunklet system were also found:");
			for(String unknown : unknownFiles) {
				System.out.println("\t" + unknown);
			}
		}

		// Size computation

		long size = 0;

		System.out.println("Computing file sizes:");
		Main.updateProgress(0);
		for(int i = 0; i < chunkletLocations.size(); i++) {
			File chunklet = new File(chunkletLocations.get(i));

			size += chunklet.length();

			Main.updateProgress((double) i / chunkletLocations.size());
		}
		Main.updateProgress(1);
		System.out.println();

		System.out.println("Chunklets are is using: " + size + " bytes.");
		System.out.println("Average Chunklet size is: " + (size / chunkletLocations.size()) + " bytes.");
	}
}
