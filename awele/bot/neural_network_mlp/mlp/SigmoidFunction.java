package awele.bot.neural_network_mlp.mlp;

/**
 * @author Alexandre Blansché
 * Fonction d'activation sigmoïde
 * Classe singleton
 */
public class SigmoidFunction extends ActivationFunction
{
    /** Instance */
    private static SigmoidFunction instance = null;
    
    /**
     * @return Accès à l'instance
     */
    public static SigmoidFunction getInstance ()
    {
        if (SigmoidFunction.instance == null)
            SigmoidFunction.instance = new SigmoidFunction ();
        return SigmoidFunction.instance;
    }

    /**
     * @param input Les valeurs en entrée du neurone
     * @param weights Les poids des connexions
     * @return L'activation du neurone en fonction des valeurs passées en entrée et des poids des connexions
     */
    @Override
    public double getActivation (double [] input, double [] weights)
    {
        /* Calcul de la somme des entrées pondérées par les poids des connexions */
        double sum = 0;
        for (int i = 0; i < input.length; i++)
            sum += input [i] * weights [i];
        /* Calcul de la sigmoïde */
        return 1 / (1 + Math.exp (-sum));
    }

    /**
     * @param output Valeur de sortie du neurone
     * @return La dérivée de l'activation selon la sortie du neurone
     */
    @Override
    public double getDerivative (double output)
    {
        /* Calcul de la dérivée de la sigmoïde */
        return output * (1 - output);
    }
}
