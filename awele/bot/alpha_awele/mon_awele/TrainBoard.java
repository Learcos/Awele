package awele.bot.alpha_awele.mon_awele;

/**
 * 
 * Une copie quasi conforme de la classe Board
 * 
 * J'ai passé la méthode selectMove en public pour
 * pouvoir l'apeller depuis la classe TrainAwele 
 * 
 */
public class TrainBoard
{
    /**
     * Nombre de trou de chaque côté du plateau de jeu
     */
    public static final int NB_HOLES = 6;
    private static final int NB_SEEDS = 4;
    int [][] holes;
    int currentPlayer;
    
    /**
     * Constructeur...
     */
    public TrainBoard()
    {
        this.holes = new int [2][TrainBoard.NB_HOLES];
        for (int i = 0; i < TrainBoard.NB_HOLES; i++)
        {
            this.holes [0][i] = TrainBoard.NB_SEEDS;
            this.holes [1][i] = TrainBoard.NB_SEEDS;
        }
    }
    
    /**
     * @return Le nombre de graines encore en jeu
     */
    public int getNbSeeds ()
    {
        int sum = 0;
        for (int i = 0; i < TrainBoard.NB_HOLES; i++)
            sum += this.holes [0][i] + this.holes [1][i];
        return sum;
    }
    
    /**
     * @param player L'index du joueur courant
     * @return Le nombre de graines encore en jeu du côté du joueur courant
     */
    public int getPlayerSeeds ()
    {
        int sum = 0;
        for (int i = 0; i < TrainBoard.NB_HOLES; i++)
            sum += this.holes [this.currentPlayer][i];
        return sum;
    }
    
    /**
     * @param player L'index du joueur adverse
     * @return Le nombre de graines encore en jeu du côté du joueur adverse
     */
    public int getOpponentSeeds ()
    {
        int sum = 0;
        for (int i = 0; i < TrainBoard.NB_HOLES; i++)
            sum += this.holes [TrainBoard.otherPlayer (this.currentPlayer)][i];
        return sum;
    }
    
    /**
     * @return Le nombre de graine dans chaque trou du joueur courant
     */
    public int [] getPlayerHoles ()
    {
        int [] holes = new int [this.holes [this.currentPlayer].length];
        for (int i = 0; i < holes.length; i++)
            holes [i] = this.holes [this.currentPlayer][i];
        return holes;
    }

    /**
     * @return Le nombre de graine dans chaque trou du joueur adverse
     */
    public int [] getOpponentHoles ()
    {
        int otherPlayer = TrainBoard.otherPlayer (this.currentPlayer);
        int [] holes = new int [this.holes [otherPlayer].length];
        for (int i = 0; i < holes.length; i++)
            holes [i] = this.holes [otherPlayer][i];
        return holes;
    }
    
    void setCurrentPlayer (int currentPlayer)
    {
        this.currentPlayer = currentPlayer;
    }
    
    /**
     * @param player L'indice d'un joueur
     * @return Retourne l'indice de l'autre joueur
     */
    public static int otherPlayer (int player)
    {
        return 1 - player;
    }
    
    int getNbSeeds (int player)
    {
        int sum = 0;
        for (int i = 0; i < TrainBoard.NB_HOLES; i++)
            sum += this.holes [player][i];
        return sum;
    }
    
    /**
     * @param player L'indice d'un joueur
     * @return Indique si le joueur n'a plus de graine
     */
    public boolean isEmpty (int player)
    {
        return this.getNbSeeds (player) == 0;
    }
    
    /**
     * @param player L'indice d'un joueur
     * @return Indique les coups valides et non valides
     */
    public boolean [] validMoves (int player)
    {
        boolean [] valid = new boolean [TrainBoard.NB_HOLES];
        boolean notEmpty = !this.isEmpty (TrainBoard.otherPlayer (player));
        for (int i = 0; i < TrainBoard.NB_HOLES; i++)
            valid [i] = (this.holes [player][i] > 0) && (notEmpty || (i + this.holes [player][i] >= 6));
        return valid;
    }
    
    public int selectMove (int player, double [] decision)
    {
        int bestMove = -1;
        double bestDecision = -Double.MAX_VALUE;
        boolean [] valid = this.validMoves (player);
        for (int i = 0; i < TrainBoard.NB_HOLES; i++)
            if (valid [i] && (decision [i] > bestDecision))
            {
                bestMove = i;
                bestDecision = decision [i];
            }
        return bestMove;
    }
    
    private boolean takeAll (int player)
    {
        boolean takeAll = true;
        int opponent = TrainBoard.otherPlayer (player);
        for (int i = 0; i < TrainBoard.NB_HOLES; i++)
            if ((this.holes [opponent][i] == 1) || (this.holes [opponent][i] > 3))
                takeAll = false;
        return takeAll;
    }
    
    int playMove (int player, double [] decision)
    {
        int score = 0;
        int bestMove = this.selectMove (player, decision);
        if (bestMove >= 0)
        {
            int nbSeeds = this.holes [player][bestMove];
            this.holes [player][bestMove] = 0;
            int currentSide = player;
            int currentHole = bestMove;
            while (nbSeeds > 0)
            {
                currentHole++;
                if (currentHole >= TrainBoard.NB_HOLES)
                {
                    currentSide = TrainBoard.otherPlayer (currentSide);
                    currentHole = 0;
                }
                if ((currentSide != player) || (currentHole != bestMove))
                {
                    this.holes [currentSide][currentHole]++;
                    nbSeeds--;
                }
            }
            if ((currentSide == TrainBoard.otherPlayer (player))
                    && ((this.holes [currentSide][currentHole] == 2) || (this.holes [currentSide][currentHole] == 3))
                    && !((currentHole == TrainBoard.NB_HOLES) && (takeAll (player))))
            {
                while ((currentHole >= 0)
                        && ((this.holes [currentSide][currentHole] == 2) || (this.holes [currentSide][currentHole] == 3)))
                {
                    score += this.holes [currentSide][currentHole];
                    this.holes [currentSide][currentHole] = 0;
                    currentHole--;
                }
            }
        }
        else
            score = -1;
        return score;
    }
    
    /**
     * @return L'indice du joueur courant
     */
    public int getCurrentPlayer ()
    {
        return this.currentPlayer;
    }

    @Override
    public String toString ()
    {
        String string = "|";
        for (int i = TrainBoard.NB_HOLES - 1; i >= 0; i--)
        {
            if (this.holes [1][i] < 10)
                string += " ";
            string += this.holes [1][i] + "|";
        }
        string += "\n|";
        for (int i = 0; i < TrainBoard.NB_HOLES; i++)
        {
            if (this.holes [0][i] < 10)
                string += " ";
            string += this.holes [0][i] + "|";
        }
        return string;
    }

}
