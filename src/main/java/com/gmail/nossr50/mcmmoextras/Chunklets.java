package com.gmail.nossr50.mcmmoextras;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.gmail.nossr50.util.blockmeta.ChunkletStore;
import com.gmail.nossr50.util.blockmeta.PrimitiveChunkletStore;
import com.gmail.nossr50.util.blockmeta.PrimitiveExChunkletStore;

public class Chunklets {
	Map<String, ChunkletStore> chunklets = new HashMap<String, ChunkletStore>();

	public void analyze(String worldLocation, boolean store) {
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

		System.out.println();
		System.out.println("Computing file sizes:");
		Main.updateProgress(0);
		for(int i = 0; i < chunkletLocations.size(); i++) {
			File chunklet = new File(chunkletLocations.get(i));

			size += chunklet.length();

			Main.updateProgress((double) i / chunkletLocations.size());
		}
		Main.updateProgress(1);
		System.out.println();

		System.out.println("Chunklets are using: " + size + " bytes.");
		System.out.println("Average Chunklet size is: " + (size / chunkletLocations.size()) + " bytes.");

		// Loaded

		ArrayList<String> notChunklets = new ArrayList<String>();
		ArrayList<ChunkletStore> chunkletStores = new ArrayList<ChunkletStore>(chunkletLocations.size());

		System.out.println();
		System.out.println("Loading Chunklets:");
		Main.updateProgress(0);
		for(int i = 0; i < chunkletLocations.size(); i++) {
			File chunklet = new File(chunkletLocations.get(i));

			ChunkletStore cStore = deserializeChunkletStore(chunklet);

			if(cStore == null) {
				notChunklets.add(chunklet.getPath());
			} else {
				chunkletStores.add(cStore);
				if(store) chunklets.put(chunklet.getPath(), cStore);
			}

			Main.updateProgress((double) i / chunkletLocations.size());
		}
		Main.updateProgress(1);
		System.out.println();

		System.out.println("Loaded: " + chunkletStores.size() + " chunklets.");

		if(!notChunklets.isEmpty()) {
			System.out.println("The following files are not chunklets:");
			for(String notChunklet : notChunklets) {
				System.out.println("\t" + notChunklet);
			}
		}

		chunkletsCount = chunkletStores.size();

		// Type checking

		int pcsCount = 0;

		System.out.println();
		System.out.println("Type checking Chunklets:");
		Main.updateProgress(0);
		for(int i = 0; i < chunkletStores.size(); i++) {
			ChunkletStore cStore = chunkletStores.get(i);

			if(cStore instanceof PrimitiveChunkletStore) {
				pcsCount++;
			}

			Main.updateProgress((double) i / chunkletLocations.size());
		}
		Main.updateProgress(1);
		System.out.println();

		System.out.println(pcsCount + " of " + chunkletsCount + " chunklets are of type PCS");

		// Density checking

		int[] density = new int[chunkletsCount];
		int lowestDensity = 16384;
		int highestDensity = 0;
		int sum = 0;

		ArrayList<ChunkletStore> emptyChunklets = new ArrayList<ChunkletStore>();

		System.out.println();
		System.out.println("Calculating density of Chunklets:");
		Main.updateProgress(0);
		for(int i = 0; i < chunkletStores.size(); i++) {
			ChunkletStore cStore = chunkletStores.get(i);

			int trueCount = 0;

			for(int x = 0; x < 16; x++) {
				for(int z = 0; z < 16; z++) {
					for(int y = 0; y < 64; y++) {
						if(cStore.isTrue(x, y, z)) trueCount++;
					}
				}
			}

			if(trueCount < lowestDensity && trueCount != 0) lowestDensity = trueCount;
			if(trueCount > highestDensity) highestDensity = trueCount;

			sum += trueCount;

			density[i] = trueCount;

			if(trueCount == 0) emptyChunklets.add(cStore);

			Main.updateProgress((double) i / chunkletStores.size());
		}
		Main.updateProgress(1);
		System.out.println();

		NumberFormat percentFormat = NumberFormat.getPercentInstance();
		percentFormat.setMaximumFractionDigits(5);

		System.out.println("Density:");
		System.out.println("\tAverage Density: " + percentFormat.format((double) (sum / chunkletsCount) / 16384));
		System.out.println("\tHighest Density: " + percentFormat.format((double) highestDensity / 16384));
		System.out.println("\tLowest Density: " + percentFormat.format((double) lowestDensity / 16384));
		System.out.println("Found " + emptyChunklets.size() + " empty Chunklets.");
	}

	public void upgrade() {
		ArrayList<String> chunkletKeys = new ArrayList<String>();
		chunkletKeys.addAll(chunklets.keySet());

		int upgradeCount = 0;

		System.out.println();
		System.out.println("Upgrading Chunklets:");
		Main.updateProgress(0);
		for(int i = 0; i < chunkletKeys.size(); i++) {
			ChunkletStore cStore = chunklets.get(chunkletKeys.get(i));

			if(cStore instanceof PrimitiveChunkletStore) {
				ChunkletStore tempStore = new PrimitiveExChunkletStore();
				tempStore.copyFrom(cStore);
				serializeChunkletStore(tempStore, new File(chunkletKeys.get(i)));
				upgradeCount++;
			}

			Main.updateProgress((double) i / chunkletKeys.size());
		}
		Main.updateProgress(1);
		System.out.println();
		System.out.println("Upgraded " + upgradeCount + " chunklets.");
	}

	private void serializeChunkletStore(ChunkletStore cStore, File location) {
		FileOutputStream fileOut = null;
		ObjectOutputStream objOut = null;

		try {
			fileOut = new FileOutputStream(location);
			objOut = new ObjectOutputStream(fileOut);
			objOut.writeObject(cStore);
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		finally {
			if (objOut != null) {
				try {
					objOut.flush();
					objOut.close();
				}
				catch (IOException ex) {
					ex.printStackTrace();
				}
			}

			if (fileOut != null) {
				try {
					fileOut.close();
				}
				catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	private ChunkletStore deserializeChunkletStore(File location) {
		ChunkletStore storeIn = null;
		FileInputStream fileIn = null;
		ObjectInputStream objIn = null;

		try {
			fileIn = new FileInputStream(location);
			objIn = new ObjectInputStream(fileIn);
			storeIn = (ChunkletStore) objIn.readObject();
		}
		catch (IOException ex) {
			return null;
		}
		catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		finally {
			if (objIn != null) {
				try {
					objIn.close();
				}
				catch (IOException ex) {
					ex.printStackTrace();
				}
			}

			if (fileIn != null) {
				try {
					fileIn.close();
				}
				catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}

		return storeIn;
	}
}