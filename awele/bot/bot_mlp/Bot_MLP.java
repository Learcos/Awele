package awele.bot.bot_mlp;

import awele.bot.*;
import awele.core.Board;
import awele.core.InvalidBotException;
import awele.bot.bot_mlp.mlp.*;
import awele.bot.bot_mlp.mon_awele.*;
import awele.data.*;
import java.util.ArrayList;

/*
 * 
 * Ce bot est basé sur un réseau de neurone
 * 
 * Il prend en entrés le nombre de graine dans chaques trous 
 * La sortie est un indice de confiance pour chaque coup
 * 
 * Il est entraîné en deux étapes :
 *  - un entrainement supervisé via les donnés disponibles
 *  - un entrainement non supervisé en jouant contre lui même
 * 
 * Dans les cas deux cas on encourage le bot à jouer les coups
 * gagants tout en le décourageant de jouer les coups perdants
 * 
 */

public class Bot_MLP extends Bot {

    private MultiLayerPerceptron mlp;
    private long timer;
    private int training_games;
    private static final int TRAINING_TIME = 10; // en secondes

    public Bot_MLP() throws InvalidBotException{
    	// Initialisation du réseau de neurones les quatres paramètres suivants : 
    	//  - nombre de neurones d'entrées : le nombre de trous du plateau de jeu (les notres + celles de l'adversaire)
    	//  - nombre de neurones cachés : 5
    	//  - nombre de neurones par couche cachée : 10
    	//  - nombre de neurones de sortie : le nombre de trous de notre plateau
        mlp = new MultiLayerPerceptron(Board.NB_HOLES*2, 5, 10, Board.NB_HOLES);
        
        this.setBotName("Réseau de neurones");
        this.addAuthor("Vincent Gindt");
        this.addAuthor("Luca Orlandi");
    }

	@Override
	public void initialize() {

	}

	@Override
	public double[] getDecision(Board board) {

        double[] input = new double[Board.NB_HOLES*2];

        for(int i = 0; i < Board.NB_HOLES; i++){
            input[i] = board.getPlayerHoles()[i];
            input[i + Board.NB_HOLES] = board.getOpponentHoles()[i];
        }

		return mlp.predict(input);
    }

    /*
     * Grosse duplication de code mais pas le choix comme je n'ai
     * pas accès à toutes les méthodes de Core
     */
    public double[] getTrainDecision(TrainBoard board) {

        double[] input = new double[Board.NB_HOLES*2];

        for(int i = 0; i < Board.NB_HOLES; i++){
            input[i] = board.getPlayerHoles()[i];
            input[i + Board.NB_HOLES] = board.getOpponentHoles()[i];
        }

		return mlp.predict(input);
    }

	@Override
	public void learn() {
        
		// Apprend les meilleures prédictions grace aux AweleData
        supervised();

        // Commence à s'entrainer gràca à un algo génétique dans la limite du temps imposé 
        timer = System.currentTimeMillis ();
        training_games = 0;

        while(System.currentTimeMillis () - timer < TRAINING_TIME * 1000){
            unsupervised();
            training_games++;
        }

        System.out.println( "Parties d'entrainement efféctuées : " + training_games);

    }

    /*
     * Entrainement à partir des donnés fournies
     */
    private void supervised(){

        AweleData data = AweleData.getInstance();

        for(AweleObservation obs : data){
            double[] input = new double[Board.NB_HOLES*2];
                    
            for(int i = 0; i < Board.NB_HOLES; i++){
                input[i] =  obs.getPlayerHoles()[i];
                input[i + Board.NB_HOLES] = obs.getOppenentHoles()[i];
            }
            
            double[] output = new double[Board.NB_HOLES];
            int coup = obs.getMove();

            if(obs.isWon()){
                output[coup-1] = 1.0;
            }
            else{
                output = mlp.predict(input);
                output[coup-1] = 0.0;
            }
        
            mlp.retropropagation(input, output);
        }
    }
 
    /*
     * Le bot joue contre lui même pour génerer plus de donnés
     */
    private void unsupervised(){

        TrainAwele partie = new TrainAwele(this,this);
        partie.play();
        
        ArrayList<Observation> data = partie.getObservations(partie.getWinner() == 0);

        for(Observation obs : data){
                        
            double[] output = new double[Board.NB_HOLES];
            int coup = obs.move;
            double[] state = new double[Board.NB_HOLES*2];

            for(int i = 0 ; i < Board.NB_HOLES*2 ; i++){
                state[i] = (double)obs.state[i];
            }

            if(obs.won){
                output[coup] = 1.0;
            }
            else{
                output = mlp.predict(state);
                output[coup] = 0.0;
            }
        
            mlp.retropropagation(state, output);
        }

    }

}