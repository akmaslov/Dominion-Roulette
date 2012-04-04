package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class Main extends Controller {

	// TODO: Eventually, this should be a Kingdom selection screen. Right now it just pulls up the default kingdom
    public static void index() 
    {
    	List<Kingdom> kingdoms = Kingdom.findAll();
    	render(kingdoms);
    	
//    	Kingdom kingdom = Kingdom.find("byName", "Default").first();
//    	List<Card> currentSet = kingdom.currentCards;
//    	
//        render(kingdom, kingdom.currentCards);
    }
    
    
    public static void addCard(String kingdomid, String cost, String strategy, String sets)
    {
    	System.out.println("In addCard: cost " + cost + ", strat " + strategy + ", sets " + sets + ".");
    	//Kingdom kingdom = Kingdom.findById(kingdomid);
    	Kingdom kingdom = Kingdom.findById(Integer.valueOf(kingdomid).longValue());
    	kingdom.addRandomCard(Constants.randomizerStrategy.valueOf(strategy), Constants.costRange.valueOf(cost));
    	kingdom.save();
    	
    	viewKingdom(kingdomid);
    }
    
    
    
    public static void addSpecificCard(String kingdomid, String desiredAction, String cardid)
    {
    	Kingdom kingdom = Kingdom.findById(Integer.valueOf(kingdomid).longValue());
    	Card card = Card.findById(Integer.valueOf(cardid).longValue());
    	
    	if(desiredAction.equals("ALWAYS_IN")) {
    		kingdom.addAlwaysIn(card);    		    
    	}
    	else if (desiredAction.equals("NEVER_VOTE")) {
    		kingdom.addNeverVote(card);
    	}
    	else if (desiredAction.equals("ADD")) {
    		kingdom.addSpecificCard(card);
    	}
    	kingdom.save();

    	viewKingdom(kingdomid);
    }
    
    
    public static void viewKingdom(String kingdomid)
    {
    	Kingdom kingdom = Kingdom.findById(Integer.valueOf(kingdomid).longValue());
    	List<Card> allCards = Card.findAll();
    	render(kingdom, allCards); 	
    }
    
    
    
    
    /**
     * TODO: what are the configuration options? 
     * @param kingdomid
     */
    public static void viewKingdomConfig(String kingdomid)
    {
    	
    }
    
    
    public static void fillKingdom(String kingdomid, String refilAction) 
    {
    	Kingdom kingdom = Kingdom.findById(Integer.valueOf(kingdomid).longValue());
    	
    	if ("Complete Kingdom".equals(refilAction))
    		kingdom.completeKingdom();
    	else if ("Randomize Kingdom".equals(refilAction)) {
    		kingdom.emptyKingdom();
    		kingdom.completeKingdom();
    	}
    	else if ("Clear Kingdom".equals(refilAction))
    		kingdom.emptyKingdom();
    	
    	kingdom.save();
    	viewKingdom(kingdomid);
    }
    
    
    public static void removeCard(String kingdomid, String cardid, String operation) 
    {
    	Kingdom kingdom = Kingdom.findById(Integer.valueOf(kingdomid).longValue());
    	Card card = Card.findById(Integer.valueOf(cardid).longValue());
    	
    	if ("Remove".equals(operation))
    		kingdom.removeCard(card);
    	else
    		kingdom.voteOutCard(card);
    	
    	kingdom.save();
    	
    	viewKingdom(kingdomid);    	
    }
   

}