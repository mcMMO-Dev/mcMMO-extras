package com.gmail.nossr50.mcmmoextras;

public class Main {
	public static void main(String[] args) {
		if (args.length == 0) {
			printBaseHelp();
			return;
		}

		if (args[0].equalsIgnoreCase("chunklets")) {
			if (args.length < 3) {
				printChunkletHelp();
				return;
			}
			Chunklets chunklets = new Chunklets();
			if (args[2].equalsIgnoreCase("analyze")) {
				chunklets.analyze(args[1], false);
				return;
			} else if (args[2].equalsIgnoreCase("upgrade")) {
				chunklets.analyze(args[1], true);
				if (chunklets.chunklets.isEmpty()) {
					System.out.println("Loaded no chunks");
					return;
				} else {
					chunklets.upgrade();
				}
			} else if (args[2].equalsIgnoreCase("cleanup")) {
				chunklets.analyze(args[1], true);
				if (chunklets.chunklets.isEmpty()) {
					System.out.println("Loaded no chunks");
					return;
				} else {
					chunklets.cleanup();
				}
			}
		}
	}

	public static void updateProgress(double progressPercentage) {
		final int width = 50; // progress bar width in chars

		System.out.print("\r[");
		int i = 0;
		for (; i <= (int) (progressPercentage * width) - 1; i++) {
			System.out.print("=");
		}
		for (; i < width; i++) {
			System.out.print(" ");
		}
		System.out.print("] ");

		System.out.print((int) (progressPercentage * 100) + "%");
	}

	private static void printChunkletHelp() {
		System.out.println("chunklets <world_folder> analyze - Print information about Chunklets for a given world");
		System.out.println("chunklets <world_folder> upgrade - Update any old-type chunklets to new file format (will run analyze first)");
		System.out.println("chunklets <world_folder> cleanup - Clean any empty folders and/or empty chunklets (will run analyze first)");
	}

	private static void printBaseHelp() {
		System.out.println("Use the following arguments for more information: ");
		System.out.println("\tchunklets - Print help for Chunklet related commands");
		System.out.println("\t? - Print this help");
	}
}
