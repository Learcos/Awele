package awele.bot.bot_mlp;

import awele.bot.*;
import awele.core.Awele;
import awele.core.Board;
import awele.core.InvalidBotException;
import awele.bot.bot_mlp.mlp.*;
import awele.data.*;
import java.util.ArrayList;

/**
 * 
 * Bot qui utilise un réseau de neurones MLP pour faire des prédictions
 * 
 * Il possède en entrées le nombre de graine(s) dans chaque trou du plateau
 * Il possède en sortie un indice de confiance pour chaque coup
 * 
 * Il s'entraîne avec :
 *  - l'apprentissage des meilleurs prédictions contenues dans les donnés de Awele.Data
 *  - l'apprentissage via algo génétique, 50 clones légèrement modifiés s'affrontent à chaque manche, le meilleur d'entre eux est selectionné
 * 
 */

public class Bot_Reseau_MLP extends Bot {

    private MultiLayerPerceptron mlp;
    private static final int TRAINING_TIME = 5; // en secondes

    public Bot_Reseau_MLP() throws InvalidBotException{
    	
    	// Initialisation du réseau de neurones avec les quatres paramètres suivants : 
    	//  - nombre de neurones d'entrées : le nombre de trous du plateau de jeu (les notres + celles de l'adversaire)
    	//  - nombre de neurones cachés : 5
    	//  - nombre de neurones par couche cachée : 10
    	//  - nombre de neurones de sortie : le nombre de trous de notre plateau
        mlp = new MultiLayerPerceptron(12, 5, 10, 6);
        
        this.setBotName("Réseau de neurones MLP");
        this.addAuthor("Vincent Gindt");
        this.addAuthor("Luca Orlandi");
    }

	@Override
	public void initialize() {

	}

	@Override
	public double[] getDecision(Board board) {

        double[] input = new double[12];

        for(int i = 0; i < 6; i++){
            input[i] = board.getPlayerHoles()[i]; // Les 6 trous du joueur (de 0 à 5 dans l'input)
            input[i + 6] = board.getOpponentHoles()[i]; // Les 6 trous de l'adversaire (de 6 à 12 dans l'input)
        }

        // Faire une prédiction pour remplir le tableau des sorties avec des indices de confiance selon chaque coup
		return mlp.predict(input);
    }

	@Override
	public void learn() {
        
		// Apprend les meilleures prédictions grace aux données de l' AweleData
		learnDataAwele();

        // Commence à s'entrainer grâce à un algo génétique dans la limite du temps imposé 
        int training_games = 0;
        long timer = System.currentTimeMillis ();

        while(System.currentTimeMillis () - timer < TRAINING_TIME * 1000){ // Tant que l'heure - l'heure à laquelle le timer s'est lancé est inférieur au temps d'entrainement
            learnAlgoGenetic();
            training_games++;
        }

        System.out.println( "Parties d'entrainement efféctuées : " + training_games);

    }

    /**
     * Le bot apprend des prédictions à partir des donnés fournies
     */
    private void learnDataAwele(){

        AweleData data = AweleData.getInstance ();
        for(AweleObservation observation: data)
        {
            double[] input = new double[12];
              
            /* Récupère les données contenues dans Awele.Data */
            
            for(int i = 0; i < 6; i++){
                input[i] =  observation.getPlayerHoles()[i]; // Les 6 trous du joueur (de 0 à 5 dans l'input)
                input[i + 6] = observation.getOppenentHoles()[i]; // Les 6 trous de l'adversaire (de 6 à 12 dans l'input)
            }
            int move = observation.getMove();

            /* Traitement des données */

            double[] output = new double[6];
            
            // Faire une prédiction pour remplir le tableau des sorties avec des indices de confiance selon chaque coup
            output = mlp.predict(input);
            
            // Si le coup était gagnant, ce coup est interessant à jouer, passer la valeur du coup dans le tableau des sorties à 1 : de grandes chances d'être choisie
            if(observation.isWon()){
                output[move-1] = 1.0;
            }
            // Si le coup était perdant, ce coup n'est pas interessant à jouer, passer la valeur du coup dans le tableau des sorties à 0 : de très faibles chances d'être choisie
            if(!observation.isWon()){
                output[move-1] = 0.0;
            }
        
            mlp.retropropagation(input, output);
        }
    }
 
    /**
     * Le bot apprend en jouant des parties contre des semblables peu modifiés pour génerer plus de donnés
     */
    private void learnAlgoGenetic(){

        Awele game = new Awele(this,this);
        game.play();
        
        // créer 50 clones de this
        // les faire se battre entre eux
        // récupérer les 20 meilleurs 
        // - les faire se reproduire (10 enfants)
        // - faire 20 clones du premier (this)
        // les faire se battre entre eux
        // ...
        
        // A la fin du compteur, retourner le meilleur
    }

}