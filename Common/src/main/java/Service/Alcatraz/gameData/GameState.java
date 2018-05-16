package Service.Alcatraz.gameData;

import Service.Alcatraz.serviceData.Gamer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class GameState {
    private ArrayList<Gamer> gamers;
    private int currentPosition;
    private int maxPostion;


    public GameState(Collection gamers, int maxPostion){
        this.gamers = new ArrayList<Gamer>(gamers);
        this.currentPosition = 0 ;
        this.maxPostion = maxPostion;
    }

    public void next(){
        this.currentPosition= (currentPosition+1) % this.maxPostion;
    }

    public int getCurrentPosition(){
        return this.currentPosition;
    }
    public Gamer getCurrentGamer(){
        return gamers.get(this.currentPosition);
    }

    public int calculatePosition( Gamer myGamerObject) {
        gamers.sort(Comparator.comparing(Gamer::getName));
        return  gamers.indexOf(myGamerObject);
    }


}
