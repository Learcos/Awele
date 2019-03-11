package awele.bot.alpha_awele.mlp;

/**
 * @author Alexandre Blansché
 * Classe abstraite pour la fonction d'activation d'un neurone
 */
public abstract class ActivationFunction
{
    /**
     * @param input Les valeurs en entrée du neurone
     * @param weights Les poids des connexions
     * @return L'activation du neurone en fonction des valeurs passées en entrée et des poids des connexions
     */
    public abstract double getActivation (double [] input, double [] weights);
    
    /**
     * @param output Valeur de sortie du neurone
     * @return La dérivée de l'activation selon la sortie du neurone
     */
    public abstract double getDerivative (double output);
}
