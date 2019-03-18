package awele.bot.neural_network_mlp.mlp;

import java.util.Random;

/**
 * @author Alexandre Blansché
 * Perceptron multicouche
 */
public class MultiLayerPerceptron
{
    /** Nombre de couches cachées */
    private static int DEFAULT_NB_HIDDEN_LAYERS = 5;
    
    /** Nombre de neurones par couche cachée */
    private static int DEFAULT_NB_HIDDEN_NEURONS_PER_LAYER = 10;
    
    /** Pas d'apprentissage par défaut */
    private static double LEARNING_STEP = .05;

    /** Génération pseudo-aléatoire de nombre */
    private static Random random = new Random (System.currentTimeMillis ());

    /** Couche d'entrée */
    private InputNeuron [] inputLayer;
    
    /** Couches cachées */
    private HiddenNeuron [][] hiddenLayers;
    
    /** "Couche" de sortie (une liste de neurones) */
    private HiddenNeuron[] outputLayer;
    
    

    public MultiLayerPerceptron() {}


	/**
     * clone un MLP
     */
    public MultiLayerPerceptron clone(ActivationFunction activF) {
    	int nbInputs = this.inputLayer.length;
    	int nbOutputs = this.outputLayer.length;
    	int nbHidden = this.hiddenLayers.length;
    	int nbNeurons = this.hiddenLayers[0].length;
    	MultiLayerPerceptron MLP_clone = new MultiLayerPerceptron();
    	MLP_clone.inputLayer = new InputNeuron[nbInputs];
    	MLP_clone.outputLayer = new HiddenNeuron[nbOutputs];
    	MLP_clone.hiddenLayers = new HiddenNeuron[nbHidden][nbNeurons];
    	for(int i = 0; i < inputLayer.length; i++) {
    		MLP_clone.inputLayer[i] = new InputNeuron();
    		MLP_clone.inputLayer[i].setActivation(this.inputLayer[i].getActivation());		
    	}
    	/* Premi�re couche cach�e */
        for (int j = 0; j < this.hiddenLayers[0].length; j++) {
            MLP_clone.hiddenLayers[0][j] = new HiddenNeuron ();
            MLP_clone.hiddenLayers[0][j].clonePreviousLayer(MLP_clone.inputLayer);
            MLP_clone.hiddenLayers[0][j].cloneWeights(this.hiddenLayers[0][j].getWeights());
            MLP_clone.hiddenLayers[0][j].setActivationFunction(activF);
            MLP_clone.hiddenLayers[0][j].setError(this.hiddenLayers[0][j].getError());
        }
        /* Autres couches cach�es */
		for(int k = 1; k < hiddenLayers.length; k++) {
			for(int l = 0; l < hiddenLayers[0].length; l++) {
			MLP_clone.hiddenLayers[k][l] = new HiddenNeuron ();
            MLP_clone.hiddenLayers[k][l].clonePreviousLayer(MLP_clone.hiddenLayers[k-1]);
            MLP_clone.hiddenLayers[k][l].cloneWeights(this.hiddenLayers[k][l].getWeights());
            MLP_clone.hiddenLayers[k][l].setActivationFunction(activF);
            MLP_clone.hiddenLayers[k][l].setError(this.hiddenLayers[k][l].getError());
			}
    	}
		/* couche de sortie */
		for(int i = 0; i < outputLayer.length; i++) {
			MLP_clone.outputLayer[i] = new HiddenNeuron ();
            MLP_clone.outputLayer[i].clonePreviousLayer(MLP_clone.hiddenLayers[nbHidden - 1]);
            MLP_clone.outputLayer[i].cloneWeights(this.hiddenLayers[nbHidden - 1][i].getWeights());
            MLP_clone.outputLayer[i].setActivationFunction(activF);
            MLP_clone.outputLayer[i].setError(this.hiddenLayers[nbHidden - 1][i].getError());
		}
    	return MLP_clone;
    }

    /**
     * Constructeur
     * @param nbInputs Nombre de neurones de la couche d'entrée
     * @param nbHidden Nombre de couches cachées
     * @param nbNeurons Nombre de neurones par couche cachée
     * @param nbOutput Nombre de neurones de la couche sortie
     */
    public MultiLayerPerceptron (int nbInputs, int nbHidden, int nbNeurons, int nbOutput)
    {
        /* Initialisation de la couche d'entrée */
        this.inputLayer = new InputNeuron [nbInputs];
        for (int i = 0; i < this.inputLayer.length; i++)
            this.inputLayer [i] = new InputNeuron ();
        /* Initialisation des couches cachées */
        this.hiddenLayers = new HiddenNeuron [nbHidden][nbNeurons];
        /* Première couche cachée */
        for (int j = 0; j < this.hiddenLayers [0].length; j++)
            this.hiddenLayers [0][j] = new HiddenNeuron (this.inputLayer);
        /* Autres couches cachées */
        for (int i = 1; i < this.hiddenLayers.length; i++)
            for (int j = 0; j < this.hiddenLayers [i].length; j++)
                this.hiddenLayers [i][j] = new HiddenNeuron (this.hiddenLayers [i - 1]);
        /* Initialisation de la "couche" de sortie */
        this.outputLayer = new HiddenNeuron[nbOutput];
        for(int i = 0; i < nbOutput; i++)
            outputLayer[i] = new HiddenNeuron (this.hiddenLayers [nbHidden - 1]);

    }
    
    /**
     * Constructeur
     * @param nbInputs Nombre de neurones de la couche d'entrée
     */
    public MultiLayerPerceptron (int nbInputs)
    {
        this (nbInputs, MultiLayerPerceptron.DEFAULT_NB_HIDDEN_LAYERS, MultiLayerPerceptron.DEFAULT_NB_HIDDEN_NEURONS_PER_LAYER, 1);
    }

    /**
     * Rétropropagation du gradient
     */
    public void retropropagation (double[] object, double[] label){
        /* On calcule la sortie du réseau de neurone en fonction de l'objet choisi */
        double[] pred = this.predict (object);
        /* On calcule l'erreur du neurone de sortie */
        
        double[] error = new double[outputLayer.length];

        for(int i = 0; i < outputLayer.length; i++)
            error[i] = label[i] - pred[i];

        /* Mise à jour des poids des connexions vers les neurones de sortie */
        for(int i = 0; i < outputLayer.length; i++)
            this.outputLayer[i].updateWeights (error[i], MultiLayerPerceptron.LEARNING_STEP);
        
            /* Mise à jour des poids des connexions vers la dernière couche cachée */
        for(int i = 0; i < outputLayer.length; i++){
            for (int k = 0; k < this.hiddenLayers [this.hiddenLayers.length - 1].length; k++){
                /* Estimation de l'erreur (erreur propagée du neurone de sortie) */
                error[i] = this.outputLayer[i].getWeight (k) * this.outputLayer[i].getError ();
                /* Mise à jour des poids des connexions */
                this.hiddenLayers [this.hiddenLayers.length - 1][k].updateWeights(error[i], MultiLayerPerceptron.LEARNING_STEP);
            }
        }
        /* Mise à jour des poids des connexions vers les autres couches cachées */
        for(int i = 0; i < outputLayer.length; i++){
            for (int j = this.hiddenLayers.length - 2; j >= 0; j--){
                for (int k = 0; k < this.hiddenLayers [j].length; k++)
                {
                    /* Estimation de l'erreur (erreur propagée par les neurones de la couche suivante) */
                    error[i] = 0;
                    for (int l = 0; l < this.hiddenLayers [j + 1].length; l++)
                        error[i] += this.hiddenLayers [j + 1][l].getWeight (k) * this.hiddenLayers [j + 1][l].getError (); 
                    /* Mise à jour des poids des connexions */ 
                    this.hiddenLayers [j][k].updateWeights (error[i], MultiLayerPerceptron.LEARNING_STEP);
                }
            }
        }
    }

    /**
     * @param object Un pixel de l'image
     * @return Le degré d'appartenance à la classe positive
     */
    public double[] predict (double [] object)
    {
        /* Activation de la couche d'entrée */
        for (int j = 0; j < this.inputLayer.length; j++)
            this.inputLayer [j].updateActivation (object [j]);
        /* Activation des couches cachées */
        for (int j = 0; j < this.hiddenLayers.length; j++)
            for (int k = 0; k < this.hiddenLayers [j].length; k++)
                this.hiddenLayers [j][k].updateActivation ();
        /* Activation de la "couche" de sortie */

        for(int i = 0; i < this.outputLayer.length; i++)
            this.outputLayer[i].updateActivation ();
        /* On retourne l'activation du neurone de la couche de sortie */

        double[] res = new double[this.outputLayer.length];
        for(int i = 0; i < this.outputLayer.length; i++)
            res[i] = outputLayer[i].getActivation();

        return res;
    }
}
