package student_player;

import Saboteur.SaboteurBoard;
import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurDestroy;
import Saboteur.cardClasses.SaboteurMap;
import Saboteur.cardClasses.SaboteurTile;
import static Saboteur.SaboteurBoardState.*;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Stack;

public class MyTools {
  
  static SaboteurTile[][] boardAfterPlace;
  static double[][] objectives = {{hiddenPos[0][0],hiddenPos[0][1],0.33},
      {hiddenPos[1][0],hiddenPos[1][1],0.33},
      {hiddenPos[2][0],hiddenPos[2][1],0.33}};
  static SaboteurMove opponentMove;
  
  public static double getSomething() {
    return Math.random();
  }
  
  public static double heuristic(int[][] bitMap) {
    //deduce the most likely location of the gold
    int[] objective = currentObjective();
    Point goal = new Point(objective[1]*3+1,objective[0]*3+1);
    
    ArrayList<Point> goals = new ArrayList<Point>();
    for (int i = 0; i < objectives.length; i++) {
      Point aGoal = new Point((int)objectives[i][1]*3+1,(int)objectives[i][0]*3+1);
      goals.add(aGoal);
    }
    
    //find all points we can build from the entrance.
    ArrayList<Point> tunnels = new ArrayList<Point>();
    int[] entrance = {originPos*3+1,originPos*3+1};
    
    //stealing this from path exists
    int[][] searched = new int[bitMap.length][bitMap[0].length];
    Stack<int[]> stack = new Stack<int[]>();
    stack.push(entrance);
    while (!stack.empty()) {
      int[] current = stack.pop();
      searched[current[0]][current[1]] = 1;
      //point = {y,x}. y is inverted(positive y is south), x direction is not inverted
      int[] south = {current[0]+1,current[1]};
      int[] north = {current[0]-1,current[1]};
      int[] east = {current[0],current[1]+1};
      int[] west = {current[0],current[1]-1};
      int[][] neighbors = {north,south,east,west};
      
      
      //check to see if neighbors have a -1
      for (int i = 0; i < neighbors.length; i++) {
        if (withinBounds(bitMap,neighbors[i])) {
          if (bitMap[current[0]][current[1]] == 1 && bitMap[neighbors[i][0]][neighbors[i][1]] == -1) {
            Point tunnel = new Point(current[1], current[0]);
            tunnels.add(tunnel);
          }
          
          if (bitMap[neighbors[i][0]][neighbors[i][1]] == 1 && searched[neighbors[i][0]][neighbors[i][1]] != 1) {
            stack.push(neighbors[i]);
          }
        }
      }
    }
    //we have tunnel points, we have goal point. now find tunnel point with smallest distance to goal
    double smallestDistance = 999;
    double[] distances= new double[objectives.length];
    for (int i = 0; i < distances.length; i++) {
      distances[i] = 999;
    }
    for (int i = 0; i < tunnels.size(); i++) {
      for (int j = 0; j < distances.length; j++) {
        double tmp = goals.get(j).distance(tunnels.get(i));
        if (tmp < distances[j]) {
          distances[j] = tmp;
        }
      }
      //get shortest distance to current goal
      double dist = goal.distance(tunnels.get(i));
      if (dist < smallestDistance) {
        smallestDistance = dist;
      }
    }
    objectiveDistance = distances;//drop these into a global var.
    return smallestDistance;
  }
 
  public static boolean nuggetKnown() {
    boolean findNugget = false;
    for (int i = 0; i < objectives.length; i++) {
      if (objectives[i][2] == 1.0) {
        findNugget = true;
        
      }
    }
    return findNugget;
  }
  
  public static void identifyOpponentMove(SaboteurBoardState boardState) {
    if (boardAfterPlace == null) {
      return;
    }
    
    SaboteurTile[][] afterOpponen = boardState.getHiddenBoard();
    
    for (int y = 0; y < afterOpponen.length; y++) {
      for (int x = 0; x < afterOpponen[0].length; x++) {
        if (afterOpponen[y][x] != boardAfterPlace[y][x]) {
          if (afterOpponen[y][x]!= null && boardAfterPlace[y][x]== null) {
            //opponent placed a tile
            opponentMove = new SaboteurMove(afterOpponen[y][x], y, x, 0);
            System.out.println("Opponent: " + afterOpponen[y][x].getName());
          }
          else if (afterOpponen[y][x] == null && boardAfterPlace[y][x] != null) {
            //opponent placed a destroy
            opponentMove = new SaboteurMove(new SaboteurDestroy(), y, x, 0);
            System.out.println("opponent: destroy");
          }
          else if (afterOpponen[y][x].getIdx().equals("nugget")) {
            //nugget is found.
            System.out.println("nugget is found");
            for (int i = 0; i < objectives.length; i++) {
              if (objectives[i][0] == y && objectives[i][1] == x) {
                updateObjectives(i, true);
                break;
              }
            }
          }
          else if (afterOpponen[y][x].getIdx().contains("hidden")) {
            for (int i = 0; i < objectives.length; i++) {
              if (objectives[i][0] == y && objectives[i][1] == x && objectives[i][2] != 0) {
                updateObjectives(i, false);
                System.out.println("No nugget");
                break;
              }
            }
          }
          else {
            opponentMove = new SaboteurMove(new SaboteurMap(), y, x, 0);
          }
        }
      }
    }
   } 
  
}