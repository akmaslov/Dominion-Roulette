package models;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import models.Constants.*;

import play.Play;
import play.db.jpa.JPABase;
import play.db.jpa.Model;

@Entity
public class Kingdom extends Model 
{
	// In case we ever want multiple sets saved
	public String name;
	
	// The main purpose of this class 
	@ManyToMany
	public List<Card> currentCards;
	
	// Various Settings:
	// Cards to always include in the deck or always exclude
	@ManyToMany
	@JoinTable(
			name="Kingdom_AlwaysIn",
			joinColumns={@JoinColumn(name="kingdom_id", referencedColumnName="id")},
			inverseJoinColumns={@JoinColumn(name="card_id", referencedColumnName="id")}
			)
	public List<Card> alwaysIn;
	
	@ManyToMany
	@JoinTable(
			name="Kingdom_neverVote",
			joinColumns={@JoinColumn(name="kingdom_id", referencedColumnName="id")},
			inverseJoinColumns={@JoinColumn(name="card_id", referencedColumnName="id")}
			)
	public List<Card> neverVote;
		
	public weightDistribution currentDistribution;
	
	/**
	 * Create a new Kingdom
	 * TODO: Coming to think of it, I think each Kingdom should have its own set of cards. And you can pick which expansions you want to play with. 	
	 */
	public Kingdom () {
		this.name = "Default";
		this.currentCards = new ArrayList<Card>();
		this.alwaysIn = new ArrayList<Card>();
		this.neverVote = new ArrayList<Card>();
		this.currentDistribution = weightDistribution.TWO_FIVE_THREE;
	}
	
	
	public Kingdom (String name) {
		this.name = name;
		this.currentCards = new ArrayList<Card>();
		this.alwaysIn = new ArrayList<Card>();
		this.neverVote = new ArrayList<Card>();
		this.currentDistribution = weightDistribution.TWO_FIVE_THREE;
	}
	
	
	public Kingdom (String name, weightDistribution distribution) {
		this.name = name;
		this.currentCards = new ArrayList<Card>();
		this.alwaysIn = new ArrayList<Card>();
		this.neverVote = new ArrayList<Card>();
		this.currentDistribution = distribution;
	}
	
	
	
	// Basic setters
	
	public void setWeight(weightDistribution weight) {
		this.currentDistribution = weight;
	}
	
	public void setName(String name) {
		this.name = name;
	}
		
	public void addAlwaysIn(Card card) {
		if (!this.alwaysIn.contains(card))
			this.alwaysIn.add(card);
	}
	
	public void addNeverVote(Card card) {
		if (!this.neverVote.contains(card))
			this.neverVote.add(card);
	}
	
	
	// Ways to add cards	
	
	public void addSpecificCard(Card card) {
		if (!this.currentCards.contains(card))
			this.currentCards.add(card);		
	}

	// Complete a 10-card set based on the currently selected weight distribution;
	public void completeKingdom() 
	{
		int countTwo = 0;
		int countThreeFour = 0;
		int countFivePlus = 0;
		
		// Step 1: Add in the cards that should always be included
		for (Card card : this.alwaysIn)
			addSpecificCard(card);
		
		// Step 2: Tally up the cards we have in the kingdom
		for (Card card : this.currentCards) {
			switch (card.getTotalCost()) {
			case 2: countTwo++; 
				break;
			case 3: case 4: countThreeFour++; 
				break;
			default: countFivePlus++;
			}
		}	
				
		// Step 3: Compare the tallies with the expected distribution; add 
		// TODO: complete this for all cases
		// TDOD: possibly have the kingdom store the preferred strategy
		switch(this.currentDistribution) {
		case TWO_FOUR_FOUR: 
			break;
		case TWO_FIVE_THREE: default:
			while (countTwo < 2) {
				addRandomCard(randomizerStrategy.WEIGHTED_VOTE, costRange.TWO);
				countTwo++;
			}
			while (countThreeFour < 5) {
				addRandomCard(randomizerStrategy.WEIGHTED_VOTE, costRange.THREE_FOUR);
				countThreeFour++;
			}
			while (countFivePlus < 3) {
				addRandomCard(randomizerStrategy.WEIGHTED_VOTE, costRange.FIVE_PLUS);
				countFivePlus++;
			}
			break;
		}
		
	}
	
	public void addRandomCard(randomizerStrategy strategy, costRange range) 
	{
		switch (strategy) {
			case RANDOM_ALL: 
				this.currentCards.add(pickRandomCard(range));
				break;
			case RANDOM_UNUSED: 
				this.currentCards.add(pickRandomUnusedCard(range));
				break;
			case WEIGHTED_VOTE:
				this.currentCards.add(pickWeightedCard(range));
				break;
		}
	}
	
	
	
	/**
	 * Pick a random appropriately priced card, but show preference to those with fewer strikes 
	 * @param cost
	 * @return
	 */
	private Card pickWeightedCard(costRange range) 
	{
		Random rand = new Random(new Date().getTime());
		List<Card> allCards = Card.findAll(range);
		
		int cardCandidateIndex;
		Card cardCandidate = null;
		boolean keepLooking = true;
		
		Map<Integer,Integer> hits = new HashMap<Integer,Integer>(); 
		
		while (keepLooking) {
			cardCandidateIndex = rand.nextInt(allCards.size());
			cardCandidate = allCards.get(cardCandidateIndex);
			
			// skip the card if it's already in the deck or is on the hatelist
			if (this.currentCards.contains(cardCandidate) || this.neverVote.contains(cardCandidate))
				continue;
			
			// register the "hit"
			if (hits.containsKey(cardCandidateIndex)) {
				int strikes = hits.get(cardCandidateIndex);
				strikes++;
				hits.put(cardCandidateIndex, strikes);				
			}
			else {
				hits.put(cardCandidateIndex, 1);
			}	
			
			// if the card's number has been rolled more often than it "strikes", it's in
			if (hits.get(cardCandidateIndex) > cardCandidate.strikes) {
				keepLooking = false;
			}
		}
		return cardCandidate;
	}
	
	

	/**
	 * Pick a random appropriately priced card from the entire set without regard to strikes
	 * @param cost
	 * @return
	 */
	private Card pickRandomCard(costRange range) 
	{
		Random rand = new Random(new Date().getTime());
		List<Card> allCards = Card.findAll(range);
		
		int cardCandidateIndex;
		Card cardCandidate = null;
		boolean keepLooking = true;
		
		while (keepLooking) {
			cardCandidateIndex = rand.nextInt(allCards.size());
			cardCandidate = allCards.get(cardCandidateIndex);
			
			if (!this.currentCards.contains(cardCandidate)) {
				keepLooking = false;
			}
		}
		return cardCandidate;
	}

	/**
	 * Pick a random appropriately priced card that hasn't been used before (i.e. has no strikes)	
	 * @param cost
	 * @return
	 */
	private Card pickRandomUnusedCard(costRange range) 
	{
		Random rand = new Random(new Date().getTime());
		List<Card> allCards = Card.findAll(range);
		
		int cardCandidateIndex;
		Card cardCandidate = null;
		boolean keepLooking = true;
		
		while (keepLooking) {
			cardCandidateIndex = rand.nextInt(allCards.size());
			cardCandidate = allCards.get(cardCandidateIndex);
			
			if (!this.currentCards.contains(cardCandidate) && cardCandidate.strikes == 0) {
				keepLooking = false;
			}
		}
		
		return cardCandidate;
	}


	/**
	 * Removes a card. Use voteOutCard if you want to add strikes and all that
	 * @param card
	 */
	public void removeCard(Card card) {
		if (this.currentCards.contains(card))
			this.currentCards.remove(card);
	}
	
	
	/**
	 * Vote out a card, adding a strike
	 * @param card
	 */
	public void voteOutCard(Card card) {
		if (this.currentCards.contains(card)) {
			this.currentCards.remove(card);
			card.strikes++;
			card.save();
		}
			
		
	}
	
	
	public void removeAlwaysIn(Card card) {
		if (this.alwaysIn.contains(card))
			this.alwaysIn.remove(card);
	}
	
	public void removeNeverVote(Card card) {
		if (this.neverVote.contains(card))
			this.neverVote.remove(card);

	}

	
	
	/** 
	 * Drops all rows from the Card pertaining to this Kingdom and populates it with new information from the base CSV file. 
	 * @throws IOException
	 */
	public void resetAndRepopulateCards() throws IOException 
	{
		deleteAllCards();
		Card.deleteAll();
		
		File basicCSV = new File("public/SetupCSV.csv");
		FileInputStream fis = new FileInputStream(basicCSV);
		DataInputStream dis = new DataInputStream(fis);
		BufferedReader read = new BufferedReader(new InputStreamReader(dis));

		// Skip the first line (labels)
		read.readLine();

		String row;
		while ((row = read.readLine()) != null) {
			String[] pieces = row.split(",");
			expansionSet set = expansionSet.valueOf(pieces[3]);

			new Card(pieces[0], Integer.parseInt(pieces[1]), Integer.parseInt(pieces[2]), set, Integer.parseInt(pieces[4]), pieces[5]).save();
			//this.allCards.add(card);
		}
		
		save();
	}

	
	/**
	 * Deletes all cards associated with this kingdom in any way. Usually invoked as part of a kingdom teardown, or resetAndRepopulateCards call
	 */
	public void deleteAllCards()
	{
		this.currentCards = new ArrayList<Card>();
		this.alwaysIn = new ArrayList<Card>();
		this.neverVote = new ArrayList<Card>();
		
//		this.emptyKingdom();
//
//		for (Card card : this.alwaysIn) {
//			this.alwaysIn.remove(card);
//		}
//		for (Card card : this.neverVote) {
//			this.neverVote.remove(card);
//		}

//		Might become useful again if I decide to have separate Card sets for each Kingdom
//		Iterator<Card> cards = this.currentCards.iterator();
//		while (cards.hasNext()) {
//			this.currentCards.remove(cards.next());
//		}
		
		this.save();
	}
	
	/**
	 * Empties a kingdom of picked cards. 
	 */
	public void emptyKingdom()
	{
		
		this.currentCards = new ArrayList<Card>();

		this.save();
	}
	
	

	
	/**
	 * Extract the file from the jar and place it in a temporary location for
	 * the test to operate from.
	 * 
	 * @param filePath
	 *            The path, relative to the classpath, of the file to reference.
	 * @return A Java File object reference.
	 * @throws IOException
	 */
	protected static File getResourceFile(String filePath) throws IOException {

		File file = File.createTempFile("dominion-setup", ".csv");

		// While we're packaged by play we have to ask Play for the inputstream instead of the classloader.
		//InputStream is = DSpaceCSVIngestServiceImplTests.class
		//		.getResourceAsStream(filePath);
		InputStream is = Play.classloader.getResourceAsStream(filePath);		
		OutputStream os = new FileOutputStream(file);

		// Copy the file out of the jar into a temporary space.
		byte[] buffer = new byte[1024];
		int len;
		while ((len = is.read(buffer)) > 0) {
			os.write(buffer, 0, len);
		}
		is.close();
		os.close();

		return file;
	}
	
	
	 
	

}
