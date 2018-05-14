package Service.Alcatraz.gameData;

import Service.Alcatraz.serviceData.Gamer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class GameState {
    private ArrayList<Gamer> gamers;
    private int currentPosition;
    private int maxPostion;
    private State currentState;

    public enum State {
        MOVE_DONE, PREPARE_NEXT_ROUND, TOKEN_PASSED, START_NEW_ROUND;
        private static State[] vals = values();

        public State next() {
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }


    public GameState(Collection gamers){
        this.gamers = new ArrayList<Gamer>(gamers);
        this.currentState = State.MOVE_DONE;
        this.currentPosition = 0 ;
    }


    public void next(){


       this.currentState = currentState.next();

    }


    public int calculatePosition( Gamer myGamerObject) {
        gamers.sort(Comparator.comparing(Gamer::getName));
        return  gamers.indexOf(myGamerObject);
    }


}
