package awele.bot.neural_network_mlp;

import awele.bot.*;
import awele.core.Awele;
import awele.core.Board;
import awele.core.InvalidBotException;
import awele.bot.neural_network_mlp.mlp.*;
import awele.data.*;
import java.util.ArrayList;

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

    public NeuralNetworkMLP() throws InvalidBotException{
    	
    	// Initialisation du réseau de neurones avec les quatres paramètres suivants : 
    	//  - nombre de neurones d'entrées : le nombre de trous du plateau de jeu (les notres + celles de l'adversaire)
    	//  - nombre de neurones cachés : 5
    	//  - nombre de neurones par couche cachée : 10
    	//  - nombre de neurones de sortie : le nombre de trous de notre plateau
    	MLP = new MultiLayerPerceptron(12, 5, 10, 6);
        
        this.setBotName("Réseau de neurones MLP");
        this.addAuthor("Vincent Gindt");
        this.addAuthor("Luca Orlandi");
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

    	ArrayList<NeuralNetworkMLP> champions = new ArrayList<NeuralNetworkMLP>();
    	int practice_games = 0;
		long timer = System.currentTimeMillis ();
		
    	// Initilisation pour la manche 1
    	for(int i =0; i < 30; i++) {
    		champions.add(i, null/*clone(this)*/);
    	}
    	
        do {
        	champions = tournament(champions);
        	
        	// Initialisation pour les manches suivantes
        	for(int i = 20; i < 30; i++) {
        		// Reproduction : les IA participantes de 20 à 29 sont des mixtes entre les gênes des 20 meilleures
        		champions.add(i, null/*reproduction(champions.get(i-20), champions.get(i-19))*/);
        	}
        	practice_games++;
        }
        while(System.currentTimeMillis () - timer < PRACTICE_TIME * 1000);  // Tant que l'heure - l'heure à laquelle le timer s'est lancé est inférieur au temps d'entrainement
        	
        //A la fin du timer, notre IA this devient la meilleure de notre championnat
        //this.clone(champions.get(0));
        

        System.out.println( "Parties d'entrainement efféctuées : " + practice_games);        
    }
    
    /**
     * Lance un championnat entre les 30 IA NeuralNetworkMLP
     * @param champions
     * @return la liste des IA triée du meilleur au plus faible
     */
    public ArrayList<NeuralNetworkMLP> tournament (ArrayList<NeuralNetworkMLP> champions) {
    	/*
    	for(int i = 0; i < 30; i++) {
    		for(int j = i + 1; j < 30; j++) {
    			Awele game = new Awele(champions.get(i),champions.get(j));
    	        game.play();
    	        
    	        
    		}
    	}*/
		return champions;
    }

}