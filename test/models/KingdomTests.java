package models;

import java.io.IOException;

import models.Constants.costRange;
import models.Constants.expansionSet;
import models.Constants.randomizerStrategy;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import play.test.UnitTest;

public class KingdomTests extends UnitTest {
	
	
	@Before
	public void setUp() throws IOException {
		Kingdom testSet = new Kingdom("Test").save();
		testSet.resetAndRepopulateCards();
		testSet.save();
	}
	
	
	@Test
	public void testAddCard() {
		Kingdom set = Kingdom.find("byName", "Test").first();
		assertNotNull(set);
		assertEquals("Test", set.name);
		
		set.addRandomCard(randomizerStrategy.WEIGHTED_VOTE, costRange.THREE_FOUR);
		set.save();
	}
	
	
	@Test
	public void testPopulate() 
	{
		assertEquals(116, Card.findAll().size());
		
		assertEquals(14, Card.findAll(costRange.TWO).size());
		assertEquals(53, Card.findAll(costRange.THREE_FOUR).size());
		assertEquals(49, Card.findAll(costRange.FIVE_PLUS).size());
		
		Kingdom set = Kingdom.find("byName", "Test").first();
		assertNotNull(set);
		assertEquals("Test", set.name);
		
		Card embargo = Card.find("byName", "Embargo").first();
		assertNotNull(embargo);
		assertEquals("Embargo", embargo.name);
		assertEquals(2, embargo.cost);
		assertEquals(0, embargo.miscCost);
		assertEquals(expansionSet.SEASIDE, embargo.set);
		assertEquals("seaside/embargo.jpg", embargo.image);
		
		set.addAlwaysIn(embargo);
		assertEquals(1, set.alwaysIn.size());
		assertEquals(embargo, set.alwaysIn.get(0));
		
		set.completeKingdom();
		set.save();
		assertTrue(set.currentCards.contains(embargo));
		assertEquals(10, set.currentCards.size());
		
		for (Card card : set.currentCards) {
			System.out.println(card.cost + ": " + card.name + ", " + card.image);
		}
	}
	
	
	
	
	@After
	public void tearDown() {
		Kingdom set = Kingdom.find("byName", "Test").first();
		set.deleteAllCards();	
		
		Card.deleteAll();
		Kingdom.deleteAll();
		
	}

}
