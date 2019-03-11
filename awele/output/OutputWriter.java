package awele.output;

import java.util.ArrayList;

/**
 * @author Alexandre Blansché
 * Classe pour gérer les affichages lors des parties d'Awele
 */
public class OutputWriter
{
    private ArrayList <Output> outputs;
    private ArrayList <Output> debug;

    /**
     * Constructeur...
     */
    public OutputWriter ()
    {
        this.outputs = new ArrayList <Output> ();
        this.debug = new ArrayList <Output> ();
    }
    
    /**
     * Rajoute une sortie
     * @param output Sortie à rajouter
     */
    public void addOutput (Output output)
    {
        output.initialiaze ();
        this.outputs.add (output);
    }
    
    /**
     * Rajoute une sortie de débuggage
     * @param output Sortie à rajouter
     */
    public void addDebug (Output output)
    {
        output.initialiaze ();
        this.debug.add (output);
    }
    
    protected void print ()
    {
        for (Output output: this.outputs)
            output.print ();
        for (Output debug: this.debug)
            debug.print ();
    }
    
    protected void printDebug ()
    {
        for (Output debug: this.debug)
            debug.print ();
    }
    
    protected void print (Object object)
    {
        for (Output output: this.outputs)
            output.print (object);
        for (Output debug: this.debug)
            debug.print (object);
    }
    
    protected void printDebug (Object object)
    {
        for (Output debug: this.debug)
            debug.print (object);
    }
}
