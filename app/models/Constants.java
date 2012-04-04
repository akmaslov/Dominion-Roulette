package models;

public class Constants {


	// Which expansion a card is from
	public enum expansionSet {
		BASE, INTRIGUE, SEASIDE, ALCHEMY, PROSPERITY, BGG, CORNUCOPIA, HINTERLANDS
	}
	
	// Possible card distributions
	public enum weightDistribution {
		TWO_FIVE_THREE, FOUR_FIVE_ONE, THREE_FIVE_TWO, TWO_FOUR_FOUR, ONE_FOUR_SIX
	}
	
	// Card selection methods
	public enum randomizerStrategy {
		WEIGHTED_VOTE, RANDOM_ALL, RANDOM_UNUSED 
	}
	
	public enum costRange {
		TWO, THREE_FOUR, FIVE_PLUS
	}
	
	// How much potions (and whatever other misc treasure types might arrive eventually) are
	// considered to be worth in coins
	public static final int miscCost = 2;
	
	


}
