package student_player;

import boardgame.Move;
import Saboteur.SaboteurPlayer;
import Saboteur.cardClasses.SaboteurCard;
import java.util.ArrayList;
import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;

/** A player file submitted by a student. */
public class StudentPlayer extends SaboteurPlayer {
  
    static boolean isFirstMove;
    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("260781081");
        //260779330
        isFirstMove = true;
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(SaboteurBoardState boardState) {    
      //check if it is the first move
      if (isFirstMove) {
        MyTools.setup(boardState);
        isFirstMove = false;
      }
      //deduce the opponent's move
      SaboteurMove opponentMove = MyTools.identifyOpponentMove(boardState);
      
      int[][] theBoardMap = boardState.getHiddenIntBoard();
      
      double current_heuristic = MyTools.assignMoveValue(theBoardMap);
      
      //get all legal moves here.
      ArrayList<SaboteurMove> allLegalMoves = boardState.getAllLegalMoves();
      
      //check what kind of moves we can process.
      ArrayList<SaboteurMove> tileMoves = new ArrayList<SaboteurMove>();
      ArrayList<SaboteurMove> mapMoves = new ArrayList<SaboteurMove>();
      ArrayList<SaboteurMove> destroyMoves = new ArrayList<SaboteurMove>();
      ArrayList<SaboteurMove> bonusMoves = new ArrayList<SaboteurMove>();
      ArrayList<SaboteurMove> malusMoves = new ArrayList<SaboteurMove>();
      ArrayList<SaboteurMove> dropMoves = new ArrayList<SaboteurMove>();

      
      for (int i = 0; i < allLegalMoves.size(); i++) {
        if (allLegalMoves.get(i).toPrettyString().contains("Tile")) {
          tileMoves.add(allLegalMoves.get(i));
        }
        else if (allLegalMoves.get(i).toPrettyString().contains("Map")) {
          mapMoves.add(allLegalMoves.get(i));
        }
        else if (allLegalMoves.get(i).toPrettyString().contains("Destroy")) {
          destroyMoves.add(allLegalMoves.get(i));
        }
        else if (allLegalMoves.get(i).toPrettyString().contains("Bonus")) {
          bonusMoves.add(allLegalMoves.get(i));
        }
        else if (allLegalMoves.get(i).toPrettyString().contains("Malus")) {
          malusMoves.add(allLegalMoves.get(i));
        }
        else if (allLegalMoves.get(i).toPrettyString().contains("Drop")) {
          dropMoves.add(allLegalMoves.get(i));
        }
      }
      
      SaboteurMove myMove = null;
      
     //steal the object if possible.
      myMove = MyTools.stealing(boardState, tileMoves, opponentMove);
      if (myMove != null) {
        return myMove;
      }
      
      //prioritize the use of map cards, we are constraint of using destroy card.
      if (mapMoves.size() > 1 && !MyTools.nuggetKnown()) {
        for (int i = 0; i < mapMoves.size(); i++) {
          int[] mapPosition = mapMoves.get(i).getPosPlayed(); // Y X
          int[] objectPosition = MyTools.currentObjective();  // Y X
          if (mapPosition[0] == objectPosition[0] && mapPosition[1] == objectPosition[1]) {
            myMove = mapMoves.get(i);
            return myMove;
          }
        }
      }
      else if (!bonusMoves.isEmpty()) {
        myMove = bonusMoves.get(0);
        return myMove;
      }
      else if (!malusMoves.isEmpty()) {
        myMove = malusMoves.get(0);
        return myMove;
      }
      else {
        if (current_heuristic <= 4) {
          current_heuristic++;
        }
        //play the tile card with the smallest assignedMoveValue.
        for (int i = 0; i < tileMoves.size(); i++) {
          double tmp = MyTools.AnalyseMoveValue(theBoardMap, tileMoves.get(i));
          if (tmp < current_heuristic) {
            current_heuristic = tmp;
            myMove = tileMoves.get(i);
          }
        }
      }
      //if AI does not choose any move, drop the move.
      ArrayList<SaboteurCard> cards = boardState.getCurrentPlayerCards();
      if (myMove == null) {
        for (int i = 0; i < cards.size(); i++) {
          if ((cards.get(i).getName().contains("1")&&!cards.get(i).getName().contains("0"))
              ||cards.get(i).getName().contains("2")
              ||cards.get(i).getName().contains("3")
              ||cards.get(i).getName().contains("4")
              ||cards.get(i).getName().contains("11")
              ||cards.get(i).getName().contains("12")
              ||cards.get(i).getName().contains("13")
              ||cards.get(i).getName().contains("14")
              ||cards.get(i).getName().contains("15")) {
            myMove =  dropMoves.get(i);
            break;
          } else {
            myMove = dropMoves.get(0);
          }
        }
      }
      
      
      MyTools.storeBoard(boardState, myMove);
      
      // Return your move to be processed by the server.
      return myMove;
    }
}