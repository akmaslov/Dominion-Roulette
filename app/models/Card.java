package models;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import models.Constants.costRange;
import models.Constants.expansionSet;

import play.db.jpa.Model;

@Entity
public class Card extends Model 
{
	// Name on the card
	public String name;
	
	// Cost in money
	public int cost;
	
	// Cost in miscellaneous treasure items like potions 
	public int miscCost;
		
	public expansionSet set;
	
	// How many strikes the card has against it
	public int strikes;
	
	// Location of the associated image
	public String image;

	public Card(String name, int cost, int miscCost, expansionSet set, int strikes, String image) {
		this.name = name;
		this.cost = cost;
		this.miscCost = miscCost;
		this.set = set;
		this.strikes = strikes;
		this.image = image;
	}
	
	
	/**
	 * Get the total cost of the card
	 * @return
	 */
	public int getTotalCost() {
		return (cost + miscCost * 2);		
	}
	
	public static List<Card> findAll(int cost) 
	{
		List<Card> completeList = new ArrayList<Card>();
		List<Card> baseSet = Card.find("byCostAndMisccost", cost, 0).fetch();
		List<Card> miscSetOne = Card.find("byCostAndMisccost", cost - Constants.miscCost, 1).fetch();
		List<Card> miscSetTwo = Card.find("byCostAndMisccost", cost - 2*Constants.miscCost, 2).fetch();
		
		completeList.addAll(baseSet);
		completeList.addAll(miscSetOne);
		completeList.addAll(miscSetTwo);
		
		return completeList;
	}
	
	
	public static List<Card> findAll(costRange range) 
	{
		List<Card> aggregateList;
		switch (range) {
			case TWO:
				return findAll(2);
			case THREE_FOUR:
				aggregateList = findAll(3);
				aggregateList.addAll(findAll(4));
				return aggregateList;
			case FIVE_PLUS:
				aggregateList = findAll(5);
				aggregateList.addAll(findAll(6));
				aggregateList.addAll(findAll(7));
				aggregateList.addAll(findAll(8));
				aggregateList.addAll(findAll(9));
				aggregateList.addAll(findAll(10));
				return aggregateList;
		}
		return null;
	}
	
	
	/**
	 * Pretty print a list of cards
	 * @param cards
	 * @return
	 */
	public static String prettyPrint(List<Card> cards) {
		String prettyString = "";
		if (cards == null)
			return "";
		
		Iterator it = cards.iterator();
		while (it.hasNext()) {
			Card card = (Card)it.next();
			prettyString += (card.name + "(" + card.set.toString().toLowerCase() + ")");
			
			if (it.hasNext())
				prettyString += ", ";
		}

		return prettyString;		
	}

}
