package awele.bot.neural_network_mlp.mlp;

/**
 * @author Alexandre Blansché
 * Neurone d'entrée
 */
public class InputNeuron extends Neuron
{    
    /**
     * Activation du neurone égal à la valeur de l'attribut auquel il correspond
     * @param value La valeur en entrée du neurone
     */
    public void updateActivation (double value)
    {
        this.setActivation (value);
    }
}
