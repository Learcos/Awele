package awele.bot.neural_network_mlp;

import awele.bot.*;
import awele.core.Awele;
import awele.core.Board;
import awele.core.InvalidBotException;
import awele.bot.neural_network_mlp.mlp.*;
import awele.data.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Bot qui utilise un réseau de neurones MLP pour faire des prédictions
 * 
 * Il possède en entrées le nombre de graine(s) dans chaque trou du plateau
 * Il possède en sortie un indice de confiance pour chaque coup
 * 
 * Il s'entraîne avec l'apprentissage :
 *  - des meilleurs prédictions contenues dans les donnés de Awele.Data
 *  - via algo génétique, 30 clones s'affrontent à chaque manche, le meilleur d'entre eux est selectionné
 */

public class NeuralNetworkMLP extends Bot {

    private MultiLayerPerceptron MLP;
    private static final int PRACTICE_TIME = 10 * 1000; // temps d'entrainement (1000 signifie 1 seconde)
    
    private int nbHoles = Board.NB_HOLES;
    private int nbInputNeurons = nbHoles*2;
    private int nbOutputNeurons = nbHoles;
    private int nbHiddenNeurons = 5; 
    private int nbNeurons = 20;

    /**
     * Constructeur
     * @throws InvalidBotException
     */
    public NeuralNetworkMLP() throws InvalidBotException{
    	
    	// Initialisation du réseau de neurones avec les quatres paramètres suivants : 
    	//  - nombre de neurones d'entrées : 12 ; le nombre de trous du plateau de jeu (les notres + celles de l'adversaire)
    	//  - nombre de neurones cachés : 5
    	//  - nombre de neurones par couche cachée : 20
    	//  - nombre de neurones de sortie : 6 ; le nombre de trous de notre plateau de joueur
    	MLP = new MultiLayerPerceptron(nbInputNeurons, nbHiddenNeurons, nbNeurons, nbOutputNeurons);
    	
        this.setBotName("NeuralNetworkAwele");
        this.addAuthor("Vincent Gindt");
        this.addAuthor("Luca Orlandi");
    }
    
    /**
     * Constructeur avec un mlp en paramètre; uniquement accessible dans cette classe
     * Utilisé pour créer les clones lors de l'apprentissage
     * @param mlp
     * @throws InvalidBotException
     */
    private NeuralNetworkMLP(MultiLayerPerceptron mlp) throws InvalidBotException {
    	MLP = mlp;
    	this.setBotName("Clone");
        this.addAuthor("Vincent Gindt");
        this.addAuthor("Luca Orlandi");
    }

	public MultiLayerPerceptron getMLP() {
		return MLP;
	}

	private void setMLP(MultiLayerPerceptron mLP) {
		MLP = mLP;
	}

	@Override
	public void initialize() { }
	
	@Override
	public double[] getDecision(Board board) {

		double[] input = new double[nbInputNeurons];
        double[] output = new double[nbOutputNeurons];
		
		for(int i = 0; i < nbHoles; i++){
            input[i] = board.getPlayerHoles()[i]; // Récupère le nombre de graines dans les 6 trous du joueur (de 0 à 5 dans l'input)
            input[i + nbHoles] = board.getOpponentHoles()[i]; // Récupère le nombre de graines dans les 6 trous de l'adversaire (de 6 à 12 dans l'input)
        }
		        
        // Faire une prédiction pour remplir le tableau des sorties avec des indices de confiance selon chaque coup
		output = MLP.predict(input);
		
		return output;
    }
	

	@Override
	public void learn() {
		// Apprend les meilleures prédictions à partir des données fournies dans Awele.Data
		learnWithData();

		// Apprend de meilleures prédictions en appliquant un algorithme génétique
        learnWithPractice();
    }

    /**
     * Le bot apprend des prédictions à partir des données fournies dans Awele.Data
     */
    private void learnWithData(){

        AweleData data = AweleData.getInstance ();
        
        double[] input = new double[nbInputNeurons];
        int move;
        double[] output = new double[nbOutputNeurons];
        
        for(AweleObservation observation: data)
        {
            /* Récupère les données contenues dans Awele.Data */
            
            for(int i = 0; i < nbHoles; i++){
                input[i] =  observation.getPlayerHoles()[i]; // Récupère le nombre de graines dans les 6 trous du joueur (de 0 à 5 dans l'input)
                input[i + nbHoles] = observation.getOppenentHoles()[i]; // Récupère le nombre de graines dans les 6 trous de l'adversaire (de 6 à 12 dans l'input)
            }
            move = observation.getMove(); // Récupère le coup joué

            /* Traitement des données */
            
            // Faire une prédiction pour remplir le tableau des sorties avec des indices de confiance selon chaque coup
            output = MLP.predict(input);
            
            // Si le coup était gagnant, ce coup est interessant à jouer, passer la valeur du coup dans le tableau des sorties à 1 : de grandes chances d'être choisie
            if(observation.isWon()){
                output[move-1] = 1.0;
            }
            // Si le coup était perdant, ce coup n'est pas interessant à jouer, passer la valeur du coup dans le tableau des sorties à 0 : de très faibles chances d'être choisie
            if(!observation.isWon()){
                output[move-1] = 0.0;
            }
        
            MLP.retropropagation(input, output);
        }
    }
 
    /**
     * Le bot apprend de meilleurs prédictions en appliquant un algorithme génétique
     * Le principe est de faire s'affronter au jeu : 
     * Au départ (la 1ère manche) :
     *  - 30 IA semblables de lui-même dont 20 sont peu modifiées, mutés (aléatoirement)
     * Puis ensuite à chaque manche :
     *  - 20 des meilleures IA à la manche précédente et 10 IA reproduites avec les gênes des 20 meilleures, 
     */
    private void learnWithPractice() {
        NeuralNetworkMLP[] champions = new NeuralNetworkMLP[30];
    	int practice_games = 0;
		long timer = System.currentTimeMillis ();
		
    	// Initilisation pour la manche 1
    	for(int i =0; i < 30; i++) {
    		
			try {
				champions[i] = new  NeuralNetworkMLP(MLP.clone()); // Ce sont 30 clones de notre MLP
			} catch (InvalidBotException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(i > 10) {
				modifyGenesRandomly(champions[i].getMLP());
			}
    	}
    	
        do {
        	// Les champions sont envoyés dans un tournois pour s'affronter et reviennent ordonnés dans l'ordre croissant du plus fort au plus faible
        	champions = tournament(champions);
        	
        	// Initialisation pour les manches suivantes
        	
        	for(int i = 20; i < 30; i++) {
        		// Reproduction : les bots de l'index 20 Ã  29 sont des mixtes entre les gênes des 20 meilleures
        		champions[i].setMLP(reproduction(champions[i-20].getMLP(), champions[i-19].getMLP()));
        	}
        	for(int i = 10; i < 20;i++) { 
        		// Les bots à l'index 10 à 19 sont mutés
        		modifyGenesRandomly(champions[i].getMLP());
        	}
        	
        	practice_games++;
        }
        while(System.currentTimeMillis () - timer < PRACTICE_TIME);  // Tant que l'heure - l'heure Ã  laquelle le timer s'est lancÃ© est infÃ©rieur au temps d'entrainement
        	
        //A la fin du timer, notre IA this récupère le MLP de la meilleure IA de notre championnat (qui se trouve Ã  l'indice 0)
        this.setMLP(champions[0].getMLP().clone() );
        

        System.out.println( "Parties d'entrainement efféctuées : " + practice_games);    
    }
    
    /**
     * Modifie : pour chaque couche des neurones cachés, le poids d'un neurone aléatoirement
     * @param mlp
     */
    public void modifyGenesRandomly(MultiLayerPerceptron mlp) {
    	Random random = new Random(System.currentTimeMillis());
    	
    	int nbNeuronRandom = random.nextInt(nbNeurons); // GÃ©nÃ¨re un nombre alÃ©atoire entre 0 et nbNeuron-1 pour le numÃ©ro du neurone Ã  modifier
    	int nbNeuronOtherLayerRandom = random.nextInt(nbNeurons);
    	double weightRandom = random.nextDouble () * 2 * 0.001 - 0.001; // GÃ©nÃ¨re un nombre alÃ©atoire entre -0.001 et 0.001 pour le poids du neurone Ã  modifier
    	
    	// Pour chaque couche cachÃ©e de mlp ( EXCEPTÉE LA 1ère car elle a 12 poids d'entrée au lieu de nbNeurons comme les autres), 
    	// un neurone choisi au hasard va Ãªtre changÃ© alÃ©atoirement
    	for(int i = 1; i < nbHiddenNeurons; i++) {
    		mlp.mutation(i, nbNeuronRandom, nbNeuronOtherLayerRandom, weightRandom);
    	}
    }
    
    /**
     * Fait se reproduire deux mlp entre eux : Le mlp renvoyé sera doté des gênes des parents (aléatoirement le poids de l'un ou l'autre sur chaque neurone)
     * @param mlpFather
     * @param mlpMother
     * @return
     */
    public MultiLayerPerceptron reproduction(MultiLayerPerceptron mlpFather, MultiLayerPerceptron mlpMother) {
    	MultiLayerPerceptron mlpSon = mlpFather.clone();
    	
    	HiddenNeuron neuronRandom = new HiddenNeuron();
    	
    	Random random = new Random(System.currentTimeMillis());
    	
    	for(int i = 0; i < nbHiddenNeurons; i++) {
    		for(int j = 0; j < nbNeurons; j++) {
    			
    			double var = random.nextDouble(); // var est une variable aléatoire entre 0 et 1
    			
    			if (var <= 0.5) { // si var est inférieur à 0.5 ; le neurone du fils sera celui du père
    				neuronRandom.setPreviousLayer(mlpFather.getInputLayer());
    				neuronRandom.setWeights(mlpFather.getHiddenLayers(i, j).getWeights());
    				neuronRandom.setActivationFunction(SigmoidFunction.getInstance());
    				neuronRandom.setError(mlpFather.getHiddenLayers(i, j).getError());
    			}
    			else { // si var est supérieur à 0.5 ; le neurone du fils sera celui de la mère
    				neuronRandom.setPreviousLayer(mlpMother.getInputLayer());
    				neuronRandom.setWeights(mlpMother.getHiddenLayers(i, j).getWeights());
    				neuronRandom.setActivationFunction(SigmoidFunction.getInstance());
    				neuronRandom.setError(mlpMother.getHiddenLayers(i, j).getError());
    			}
    			mlpSon.setHiddenLayers(i, j, neuronRandom);
    			
    		}
    	}
    	
    	return mlpSon;
    }

    /**
     * Lance un championnat entre les 30 IA NeuralNetworkMLP
     * @param champions
     * @return la liste des IA triée du meilleur au plus faible
     */
    public NeuralNetworkMLP[] tournament (NeuralNetworkMLP[] champions) {
    	    	
        int nbBots = 30;
        SimpleDateFormat df = new SimpleDateFormat("mm:ss.SSS");
        final double [] points = new double [nbBots];
        for (int i = 0; i < nbBots; i++)
            for (int j = i + 1; j < nbBots; j++)
            {
                double [] localPoints = new double [2];
                double nbMoves = 0;
                long runningTime = 0;
                
                Awele awele = new Awele (champions[i], champions[j]);
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
                /*System.out.println ("Score : " + localPoints [0] + " - " + localPoints [1]);
                if (localPoints [0] == localPoints [1])
                	System.out.println ("Égalité");
                else if (localPoints [0] > localPoints [1])
                	System.out.println (champions[i].getName () + " a gagné");
                else
                	System.out.println (champions[j].getName () + " a gagné");*/
                points [i] += localPoints [0];
                points [j] += localPoints [1];
                //System.out.println ("Nombre de coups joués : " + nbMoves);
                //System.out.println ("Durée : " + df.format (new Date (runningTime)));
            }
        for (int i = 0; i < points.length; i++)
            points [i] = Math.round (points [i] * 100) / 100.;
        /*System.out.println ("Scores finaux :");
        for (int i = 0; i < nbBots; i++)
        {
        	System.out.println (champions[i] + " : " + points [i]);
        }*/
        
        final Map <String, Integer> map = new HashMap <String, Integer> ();
        for (int i = 0; i < 30; i++)
            map.put (champions[i].getName (), i);
        Arrays.sort(champions, new Comparator <Bot> ()
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
        /*System.out.println ("Rangs :");
        for (int i = nbBots - 1; i >= 0; i--)
        {
        	System.out.println((nbBots - i) + ". " + champions[i] + " : " + points [i]);
        }*/
        
		return champions;
    }
    
}