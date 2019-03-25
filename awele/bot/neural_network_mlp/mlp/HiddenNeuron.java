package awele.bot.neural_network_mlp.mlp;

import java.util.Random;

/**
 * @author Alexandre Blansché
 * Neurone d'une couche cachée ou de la couche de sortie
 */
public class HiddenNeuron extends Neuron
{
    /** Génération pseudo-aléatoire de nombre */
    private static Random random = new Random (System.currentTimeMillis ());
    
    /** Valeur maximale des poids initiaux */
    private static final double MAX_VALUE = .001;
    
    /** Fonction d'activation */
    private ActivationFunction activationFunction;
    
    /** Couche de neurone précédente */
    private Neuron [] previousLayer;
    
    /** Poids des connexions des neurones de la couche précédente */
    private double [] weights;
    
    /** Erreur estimée du neurone */
    private double error;
    
    
    public HiddenNeuron() {}
    
    /**
     * @param previousLayer Les neurones de la couche précédente
     * @param activationFunction La fonction d'activation du neurone
     */
    public HiddenNeuron (Neuron [] previousLayer, ActivationFunction activationFunction)
    {
        /* On affecte la fonction d'activation */
        this.activationFunction = activationFunction;
        /* On récupère les neurones de la couche précédente... */
        this.previousLayer = new Neuron [previousLayer.length + 1];
        for (int i = 0; i < previousLayer.length; i++)
            this.previousLayer [i] = previousLayer [i];
        /* ... et le neurone de biais */
        this.previousLayer [previousLayer.length] = BiasNeuron.getInstance ();
        /* Initialisation aléatoire des poids */
        this.weights = new double [this.previousLayer.length];
        for (int i = 0; i < this.weights.length; i++)
            this.weights [i] = HiddenNeuron.initWeight ();
    }

    /**
     * @param previousLayer Les neurones de la couche précédente
     * La fonction d'activation par défaut est la fonction sigmoïde
     */
    public HiddenNeuron (Neuron [] previousLayer)
    {
        this (previousLayer, SigmoidFunction.getInstance ());
    }
    
    /**
     * @param index L'indice du poids auquel on veut accéder
     * @return Le poids de la connexion
     */
    public double getWeight (int index)
    {
        return this.weights [index];
    }
    
    public double[] getWeights () {
    	return this.weights;
    }
    
    public void setWeights(int index, double poids) {
    	this.weights[index] = poids;
    }
    
    public void setWeights(double[] poids) {
    	this.weights = new double[poids.length];
    	for(int i = 0; i< this.weights.length; i++) 
    		this.weights[i] = poids[i];
    }
    
    /**
     * @return L'erreur de ce neurone à propager lors de l'apprentissage
     */
    public double getError ()
    {
        return this.error;
    }
    
   
    public void setError (double error)
    {
        this.error = error;
    }
    
    public Neuron[] getPreviousLayer() {
    	return this.previousLayer;
    }
    
    
    public void setActivationFunction(ActivationFunction activF) {
    	this.activationFunction = SigmoidFunction.getInstance();
    }
    
    /**
     * Calcul de l'activation du neurone
     */
    public void updateActivation ()
    {
        /* On récupère les niveaux d'activations de la couche précédente */
        double [] input = new double [this.previousLayer.length];
        for (int i = 0; i < input.length; i++)
            input [i] = this.previousLayer[i].getActivation ();
        /* On met à jour l'activation du neurone */
        this.setActivation (this.activationFunction.getActivation (input, this.weights));
    }
    
    /**
     * Modification des poids en fonction de l'erreur estimée et du pas d'apprentissage
     * @param error L'erreur estimée
     * @param learningStep Le pas d'apprentissage
     */
    public void updateWeights (double error, double learningStep)
    {
        /* Calcul de l'erreur estimée pour ce neurone */
        this.error = error * this.activationFunction.getDerivative (this.getActivation ());
        /* Modification des poids */
        for (int i = 0; i < this.weights.length; i++)
        {
            double delta = learningStep * this.error * this.previousLayer [i].getActivation ();
            this.weights [i] += delta;
        }
    }
    
    
    public void setPreviousLayer(Neuron[] prevLayer) {
    	this.previousLayer = new Neuron [prevLayer.length];
        for (int i = 0; i < prevLayer.length; i++) {
            this.previousLayer[i] = prevLayer [i];
        }
    }
    
    /**
     * @return Initialisation d'un poids entre -HiddenNeuron.MAX_VALUE et HiddenNeuron.MAX_VALUE
     */
    private static double initWeight ()
    {
        return HiddenNeuron.random.nextDouble () * 2 * HiddenNeuron.MAX_VALUE - HiddenNeuron.MAX_VALUE;
    }
}
