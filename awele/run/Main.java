package awele.run;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import awele.bot.Bot;
import awele.core.Awele;
import awele.output.LogFileOutput;
import awele.output.OutputWriter;
import awele.output.StandardOutput;

/**
 * @author Alexandre Blansché
 * Programme principal
 */
public final class Main extends OutputWriter
{
    private static final String LOG_FILE = "awele.log";
    private static final int NB_RUNS = 100;
    //private static final int NB_RUNS = 1;
    @SuppressWarnings ("unused")
    private static final String TEACHER = "Alexandre Blansché";
    
    ArrayList <Bot> bots;
    
    private Main ()
    {   
    }
    
    private void loadBots ()
    {
        Reflections reflections = new Reflections ("awele.bot");
        Set <Class <? extends Bot>> subClasses = reflections.getSubTypesOf (Bot.class);
        this.print (subClasses.size () + " classes ont été trouvées");
        this.print ();
        
        this.bots = new ArrayList <Bot> ();
        SimpleDateFormat df = new SimpleDateFormat("mm:ss.SSS");
        for (Class <? extends Bot> subClass : subClasses)
        {
            this.print ("Classe : " + subClass.getName ());
            try
            {
                Bot bot = (Bot) subClass.getConstructors () [0].newInstance ();
                if (bot != null)
                //if ((bot != null) && (!bot.getAuthors ().equals (Main.TEACHER)))
                {
                    this.print ("Nom du bot : " + bot.getName ());
                    this.print ("Auteur(s) : " + bot.getAuthors ());
                    long start = System.currentTimeMillis ();
                    bot.learn ();
                    long end = System.currentTimeMillis ();
                    long runningTime = end - start;
                    this.bots.add (bot);
                    this.print ("Temps d'apprentissage : " + df.format (new Date (runningTime)));
                    this.print ();
                }
            }
            catch (Exception e)
            {
                this.print ("Ne peut pas instancier le bot \"" + subClass.getName () + "\"");
                e.printStackTrace ();
            }
        }
        this.print (this.bots.size () + " bots ont été instanciés");
    }
    
    private void tournament ()
    {
        this.print ();
        this.print ("Que le championnat commence !");
        int nbBots = this.bots.size ();
        SimpleDateFormat df = new SimpleDateFormat("mm:ss.SSS");
        final double [] points = new double [nbBots];
        for (int i = 0; i < nbBots; i++)
            for (int j = i + 1; j < nbBots; j++)
            {
                this.print ();
                this.print (this.bots.get (i).getName () + " vs. " + this.bots.get (j).getName ());
                double [] localPoints = new double [2];
                double nbMoves = 0;
                long runningTime = 0;
                for (int k = 0; k < Main.NB_RUNS; k++)
                {
                    this.bots.get (i).initialize ();
                    this.bots.get (j).initialize ();
                    Awele awele = new Awele (this.bots.get (i), this.bots.get (j));
                    //this.print ();
                    //awele.addOutputs (this.getOutputs ());
                    //awele.addDebug (StandardOutput.getInstance ());
                    awele.play ();
                    nbMoves += awele.getNbMoves ();
                    runningTime += awele.getRunningTime ();
                    if (awele.getWinner () >= 0)
                        localPoints [awele.getWinner ()] += 3;
                    else
                    {
                        localPoints [0]++;
                        localPoints [1]++;
                    }
                }
                localPoints [0] /= Main.NB_RUNS;
                localPoints [1] /= Main.NB_RUNS;
                nbMoves /=  Main.NB_RUNS;
                runningTime /=  Main.NB_RUNS;
                this.print ("Score : " + localPoints [0] + " - " + localPoints [1]);
                if (localPoints [0] == localPoints [1])
                    this.print ("Égalité");
                else if (localPoints [0] > localPoints [1])
                    this.print (this.bots.get (i).getName () + " a gagné");
                else
                    this.print (this.bots.get (j).getName () + " a gagné");
                points [i] += localPoints [0];
                points [j] += localPoints [1];
                this.print ("Nombre de coups joués : " + nbMoves);
                this.print ("Durée : " + df.format (new Date (runningTime)));
            }
        for (int i = 0; i < points.length; i++)
            points [i] = Math.round (points [i] * 100) / 100.;
        this.print ();
        this.print ("Scores finaux :");
        for (int i = 0; i < nbBots; i++)
        {
            this.print (this.bots.get (i) + " : " + points [i]);
        }
        this.print ();
        final Map <String, Integer> map = new HashMap <String, Integer> ();
        for (int i = 0; i < this.bots.size (); i++)
            map.put (this.bots.get (i).getName (), i);
        Collections.sort (this.bots, new Comparator <Bot> ()
        {
            @Override
            public int compare(Bot bot1, Bot bot2)
            {
                Integer index1 = map.get (bot1.getName ());
                Integer index2 = map.get (bot2.getName ());
                return Double.compare (points [index1], points [index2]);
            }
        });
        java.util.Arrays.sort (points);
        this.print ("Rangs :");
        for (int i = nbBots - 1; i >= 0; i--)
        {
            this.print ((nbBots - i) + ". " + this.bots.get (i) + " : " + points [i]);
        }
            
    }
    
    /**
     * @param args
     */
    public static void main (String [] args)
    {
        Main main = new Main();
        main.addOutput (StandardOutput.getInstance ());
        main.addOutput (new LogFileOutput (Main.LOG_FILE));
        main.loadBots ();
        main.tournament ();
    }
}
