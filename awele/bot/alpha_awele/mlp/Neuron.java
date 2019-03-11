package awele.bot.alpha_awele.mlp;

/**
 * @author Alexandre Blansché
 * Classe abstraite représentant un neurone
 */
public abstract class Neuron
{
    /** Niveau d'activation du neurone */
    private double activation;

    /**
     * Affectation du niveau d'activation du neurone
     * @param activation
     */
    protected void setActivation (double activation)
    {
        this.activation = activation;
    }
    
    /**
     * @return Niveau d'activation du neurone
     */
    public double getActivation ()
    {
        return this.activation;
    }
}
