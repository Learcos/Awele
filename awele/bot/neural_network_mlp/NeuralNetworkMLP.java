package awele.bot.neural_network_mlp;

import awele.bot.*;
import awele.core.Awele;
import awele.core.Board;
import awele.core.InvalidBotException;
import awele.bot.neural_network_mlp.mlp.*;
import awele.data.*;
import awele.run.Main;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
 *  - via algo génétique, 50 clones légèrement modifiés s'affrontent à chaque manche, le meilleur d'entre eux est selectionné
 */

public class NeuralNetworkMLP extends Bot {

    private MultiLayerPerceptron MLP;
    private static final int PRACTICE_TIME = 5; // temps en secondes
    private int nbInputNeurons = Board.NB_HOLES*2;
    private int nbOutputNeurons = Board.NB_HOLES;
    private int nbHiddenNeurons = 5;
    private int nbNeurons = 10;

    /**
     * Constructeur
     * @throws InvalidBotException
     */
    public NeuralNetworkMLP() throws InvalidBotException{
    	
    	// Initialisation du réseau de neurones avec les quatres paramètres suivants : 
    	//  - nombre de neurones d'entrées : le nombre de trous du plateau de jeu (les notres + celles de l'adversaire)
    	//  - nombre de neurones cachés : 5
    	//  - nombre de neurones par couche cachée : 10
    	//  - nombre de neurones de sortie : le nombre de trous de notre plateau
    	MLP = new MultiLayerPerceptron(nbInputNeurons, nbHiddenNeurons, nbNeurons, nbOutputNeurons);
    	
        this.setBotName("Réseau de neurones MLP");
        this.addAuthor("Vincent Gindt");
        this.addAuthor("Luca Orlandi");
    }
    
    /**
     * Constructeur
     * @param mlp
     * @throws InvalidBotException
     */
    public NeuralNetworkMLP(MultiLayerPerceptron mlp) throws InvalidBotException {
    	MLP = mlp;
    	this.setBotName("Réseau de neurones MLP");
        this.addAuthor("Vincent Gindt");
        this.addAuthor("Luca Orlandi");
    }

	public MultiLayerPerceptron getMLP() {
		return MLP;
	}

	public void setMLP(MultiLayerPerceptron mLP) {
		MLP = mLP;
	}

	@Override
	public void initialize() { }
	
	@Override
	public double[] getDecision(Board board) {

		double[] input = new double[12];
        double[] output = new double[6];
		
		for(int i = 0; i < 6; i++){
            input[i] = board.getPlayerHoles()[i]; // Récupère le nombre de graines dans les 6 trous du joueur (de 0 à 5 dans l'input)
            input[i + 6] = board.getOpponentHoles()[i]; // Récupère le nombre de graines dans les 6 trous de l'adversaire (de 6 à 12 dans l'input)
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
        
        double[] input = new double[12];
        int move;
        double[] output = new double[6];
        
        for(AweleObservation observation: data)
        {
            /* Récupère les données contenues dans Awele.Data */
            
            for(int i = 0; i < 6; i++){
                input[i] =  observation.getPlayerHoles()[i]; // Récupère le nombre de graines dans les 6 trous du joueur (de 0 à 5 dans l'input)
                input[i + 6] = observation.getOppenentHoles()[i]; // Récupère le nombre de graines dans les 6 trous de l'adversaire (de 6 à 12 dans l'input)
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
     *  - 30 IA semblables de lui-même peu modifiées (aléatoirement)
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
				champions[i] = new  NeuralNetworkMLP(MLP.clone(SigmoidFunction.getInstance())); // Ce sont 30 clones de notre MLP
			} catch (InvalidBotException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
    		modifyGenesRandomly(champions[i].getMLP());
    	}
    	
        do {
        	//champions = tournament(champions);
        	
        	// Initialisation pour les manches suivantes
        	for(int i = 20; i < 30; i++) {
        		// Reproduction : les IA participantes de 20 à 29 sont des mixtes entre les gênes des 20 meilleures
        		champions[i].setMLP(reproduction(champions[i-20].getMLP(), champions[i-19].getMLP()));
        	}
        	practice_games++;
        }
        while(System.currentTimeMillis () - timer < PRACTICE_TIME * 1000);  // Tant que l'heure - l'heure à laquelle le timer s'est lancé est inférieur au temps d'entrainement
        	
        //A la fin du timer, notre IA this devient la meilleure de notre championnat
        //this.clone(champions.get(0));
        

        System.out.println( "Parties d'entrainement efféctuées : " + practice_games);        
    }
    
    /**
     * Modifie : pour chaque couche des neurones cachés, le poids d'un neurone aléatoirement
     * @param mlp
     */
    public void modifyGenesRandomly(MultiLayerPerceptron mlp) {
    	Random random = new Random();
    	
    	HiddenNeuron hN;
    	int neuronRandom = random.nextInt(nbNeurons); // Génère un nombre aléatoire entre 0 et nbNeuron-1 pour le numéro du neurone à modifier
    	double weightRandom = random.nextDouble () * 2 * 0.001 - 0.001; // Génère un nombre aléatoire entre -0.001 et 0.001 pour le poids du neurone à modifier
    	
    	// Pour chaque couche cachée de mlp, un neurone choisi au hasard va être changé aléatoirement
    	for(int i = 0; i < nbHiddenNeurons; i++) {
    		//hN = mlp.getHiddenLayers(i, neuronRandom);
    		//hN.setWeights(neuronRandom, weightRandom);
    		//mlp.setHiddenLayers(i, neuronRandom, hN);
    		mlp.setHiddenLayersWeights(i, neuronRandom, weightRandom);
    	}
    }
    
    /**
     * Fait se reproduire deux mlp entre eux : Le mlp renvoyé sera doté des gênes des parents (aléatoirement le poids de l'un ou l'autre sur chaque neurone)
     * @param mlpFather
     * @param mlpMother
     * @return
     */
    public MultiLayerPerceptron reproduction(MultiLayerPerceptron mlpFather, MultiLayerPerceptron mlpMother) {
    	MultiLayerPerceptron mlpSon = mlpFather.clone(SigmoidFunction.getInstance());
    	
    	
    	for(int i = 0; i < nbHiddenNeurons; i++) {
    		for(int j = 0; j < nbNeurons; j++) {
    			
    			
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
                System.out.println ("Score : " + localPoints [0] + " - " + localPoints [1]);
                if (localPoints [0] == localPoints [1])
                	System.out.println ("Égalité");
                else if (localPoints [0] > localPoints [1])
                	System.out.println (champions[i].getName () + " a gagné");
                else
                	System.out.println (champions[j].getName () + " a gagné");
                points [i] += localPoints [0];
                points [j] += localPoints [1];
                System.out.println ("Nombre de coups joués : " + nbMoves);
                System.out.println ("Durée : " + df.format (new Date (runningTime)));
            }
        for (int i = 0; i < points.length; i++)
            points [i] = Math.round (points [i] * 100) / 100.;
        System.out.println ("Scores finaux :");
        for (int i = 0; i < nbBots; i++)
        {
        	System.out.println (champions[i] + " : " + points [i]);
        }
        
        final Map <String, Integer> map = new HashMap <String, Integer> ();
        for (int i = 0; i < 30; i++)
            map.put (champions[i].getName (), i);
        /*Collections.sort (champions, new Comparator <Bot> ()
        {
            @Override
            public int compare(Bot bot1, Bot bot2)
            {
                Integer index1 = map.get (bot1.getName ());
                Integer index2 = map.get (bot2.getName ());
                return Double.compare (points [index1], points [index2]);
            }
        });*/
        java.util.Arrays.sort (points);
        System.out.println ("Rangs :");
        for (int i = nbBots - 1; i >= 0; i--)
        {
        	System.out.println((nbBots - i) + ". " + champions[i] + " : " + points [i]);
        }
        
		return champions;
    }
    
}