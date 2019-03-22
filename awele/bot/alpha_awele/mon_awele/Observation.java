package awele.bot.alpha_awele.mon_awele;

public class Observation{

    public int[] state;
    public int move;
    public boolean won;

    public Observation(int[] state, int move, boolean won){
        this.state = state;
        this.move = move;
        this.won = won;
    }

}