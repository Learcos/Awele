package awele.bot.alpha_awele.mlp;

/**
 * @author Alexandre Blansché
 * Perceptron multicouche
 * 
 * Modifié par Julien Lafille pour pouvoir avoir plusieurs 
 * valeures de sortie
 * 
 */
public class MultiLayerPerceptron
{
    
    /** Pas d'apprentissage par défaut */
    private static double LEARNING_STEP = .05;

    /** Couche d'entrée */
    private InputNeuron [] inputLayer;
    
    /** Couches cachées */
    private HiddenNeuron [][] hiddenLayers;
    
    /** "Couche" de sortie (un seul neurone) */
    private HiddenNeuron[] outputLayer;

    /**
     * Constructeur
     * @param nbInputs Nombre de neurones de la couche d'entrée
     * @param nbHidden Nombre de couches cachées
     * @param nbNeurons Nombre de neurones par couche cachée
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
     * Rétropropagation du gradient
     */
    public void retropropagation (double[] object, double[] label){
        /* On calcule la sortie du réseau de neurone en fonction de l'objet choisi */
        double[] pred = this.predict (object);
        /* On calcule l'erreur du neurone de sortie */
        
        double[] error = new double[outputLayer.length];

        for(int i = 0; i < outputLayer.length; i++)
            error[i] = label[i] - pred[i];

        /* Mise à jour des poids des connexions vers le neurone de sortie */
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
    public double[] predict (double [] input)
    {
        /* Activation de la couche d'entrée */
        for (int j = 0; j < this.inputLayer.length; j++)
            this.inputLayer [j].updateActivation (input [j]);
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
