package awele.output;

/**
 * @author Alexandre Blansché
 * Classe abstraite pour gérer les affichages lors des parties d'Awele
 */
public abstract class Output
{
    /**
     * Retour à la ligne
     */
    public void print ()
    {
        this.print ("");
    }
    
    /**
     * @param object Affichage d'un objet quelconque
     */
    public void print (Object object)
    {
        this.print (object.toString ());
    }
    
    protected abstract void print (String string);

    abstract void initialiaze ();
}
