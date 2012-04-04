import java.io.IOException;

import models.Card;
import models.Kingdom;
import play.jobs.Job;
import play.jobs.OnApplicationStart;


@OnApplicationStart
public class Bootstrap extends Job {
	
	public void doJob() 
	{
		Kingdom defaultKingdom;
		// If there are no kingdoms, create a default one
		if (Kingdom.count() == 0)
		{
			defaultKingdom = new Kingdom().save();
		} else {
			defaultKingdom = (Kingdom)Kingdom.findAll().get(0);
		}
		
		// If there are no cards, populate the database
		if (Card.count() == 0) 
		{
			try {
				
				defaultKingdom.resetAndRepopulateCards();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}

}
