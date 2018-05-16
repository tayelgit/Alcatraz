package communctation.Implementation;


import Service.Alcatraz.AlcatrazRemote.Implementation.GameImpl;
import Service.Alcatraz.AlcatrazRemote.Interface.GameRemote;

import java.rmi.RemoteException;
import java.util.TimerTask;

public class Ping extends TimerTask {

   private GameRemote game;
   private GameImpl gameImpl;
   private boolean isStarted;
   public Ping(GameRemote game, GameImpl gameImpl){
       this.game = game;
       this.gameImpl = gameImpl;
        this.isStarted = false;
   }

   public boolean isStarted(){
       return this.isStarted;
   }
   public void reset(){
       this.isStarted = false;
   }
    public void run() {
        this.isStarted = true;
       try {

            game.ping();
        } catch (RemoteException e) {
            e.printStackTrace();
            this.gameImpl.initAbort();
        }


    }
}
