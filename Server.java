import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    

    public static void main(String[] args) throws Exception {
        System.out.println("The server is running...");
        ExecutorService pool = Executors.newFixedThreadPool(100);

        try (ServerSocket listener = new ServerSocket(8888)) {
            int playerIndex = 0;
            Game game = new Game();
            while (true) 
            {                
                playerIndex++;
                pool.execute(game.new Player(listener.accept(), playerIndex));
               
            }
        }
    }

}

class Game
{
    Player currentPlayer;
    
    LinkedList<Player> playerQueue = new LinkedList<Player>();
    
    LinkedList<Player> allPlayer = new LinkedList<Player>();
    
    boolean isStarted = false;
    
    int totalRound = 2;
    
    int roundCounter = 0;


    class Player implements Runnable 
    {
        int index;
        String name;
        Socket socket;
        Scanner input;
        PrintWriter output;
        int[] dice = new int[7];
        int totalTry = 3;
        int tryCounter = 0;
        int totalSum = 0;
        
        public Player(Socket socket, int index) {
            this.socket = socket;
            this.index = index;
            Random rand = new Random(); 
            for(int i=0; i<7; i++)
                dice[i] =  rand.nextInt((8 - 1) + 1) + 1;
        }
        
    @Override
    public void run() 
    {
        try {
            while (true) {
                 input = new Scanner(socket.getInputStream());
                 output = new PrintWriter(socket.getOutputStream(), true);
                    output.println("WHO");
                    name = input.nextLine();
                    if (name == null) 
                        return;
                    
                        if ( name != null  ) 
                        {
                           output.println("ACCEPTED " + name);
                           break;
                        }
                }
            
                setup();
                processCommands();
            } 
        catch (Exception e) {
                e.printStackTrace();
            } 
        finally {
                
                try {socket.close();} catch (IOException e) {}
            }
    }
    
    private void setup() throws IOException {
            output.println("WELCOME " + name);
            if (index == 1) {
                currentPlayer = this;
            } 
            else {
                playerQueue.add (this);
            }
            allPlayer.add(this);

    }
    private void printDices(Player plr_out, Player plr_dice )
    {
        for(int i =0; i< 7; i++)
            plr_out.output.print(i+1 +" | ");
         plr_out.output.println("");
        for(int i =0; i< 7; i++)
            plr_out.output.print(plr_dice.dice[i] +" | ");
        plr_out.output.println("");
    }
    private int generateNewDice(String command)
    {
        String[] parts = command.split(" "); 
        int[] indexes = new int[parts.length];
        int i = 0;
        for(String ind : parts)
            try
            {
            indexes[i++] = Integer.parseInt(ind);
            }
        catch (Exception e) {
                e.printStackTrace();
            } 

        for(int ind : indexes)
        {
            if(ind < 1 || ind > 7)
                return -1;
        }
        Random rand = new Random();
        for(int ind : indexes)
            this.dice[ind-1] = rand.nextInt((8 - 1) + 1) + 1;
        return 1;
    }
    private void regenerate(int[] dice)
    {
        Random rand = new Random(); 
        for(int i=0; i<7; i++)
            dice[i] =  rand.nextInt((8 - 1) + 1) + 1;
        
    }
     private int[] countInArray(int[] dices)
    {
        int[] countArray = new int[dices.length+2] ;
        for(int i = 0; i < dices.length; i++)
        {
           countArray[dices[i]]++;   
        }
        
        return countArray;
    }
     
    private int yahtzee(int[] dices)
    {
        for(int dice : dices)
        {
            if(dice != dices[0])
                return -1;
        }
        return 50;
    }
    
    private int suite(int[] dices)
    {
        int[] tmpArray = Arrays.copyOf(dices, dices.length);
        Arrays.sort(tmpArray);
        int i;
        for( i = 1; i <= tmpArray.length; i++)
        {
            if(tmpArray[i-1] != i )
                break;
        }
        if(i == tmpArray.length+1 && tmpArray[0] == 1 && tmpArray[tmpArray.length-1] == 7)
            return 30;            // petite suite
        else 
        {
            for( i = 1; i <= tmpArray.length; i++)
            {
                if(tmpArray[i-1] != i+1 )
                    break;
            }
            if(i == tmpArray.length+1 && tmpArray[0] == 2 && tmpArray[tmpArray.length-1] == 8)
            return 40; 
        }
        return -1;
    }
    
    private int full(int[] countArray)
    {
        for(int count : countArray)
        {
            if(count == 4)
            {
              for(int secondCount : countArray)
                  if(secondCount == 3) return 25;
            }
            else if(count == 5)
            {
                for(int secondCount : countArray)
                  if(secondCount == 2) return 25;
            }
        }
        return -1;
    }
    private int brelan(int[] countArray)
    {   
        int sum = 0;
        boolean find = false;
        for(int index = 1; index < countArray.length; index++)
        {
            sum += index * countArray[index];
            if(countArray[index] == 3 )
                find = true;
        }
        if(find)
            return sum;
        else 
            return -1;
    }
    private int carre(int[] countArray)
    {   
        int sum = 0;
        boolean find = false;
        for(int index = 1; index < countArray.length; index++)
        {
            sum += index * countArray[index];
            if(countArray[index] == 4 )
                find = true;
        }
        if(find)
            return sum;
        else 
            return -1;
    }
    private Map<String, Integer> findBest(int[] dice)
    {
        int[] countInArray = countInArray(dice);
        Map<String, Integer> result = new HashMap<String, Integer>();
        int points = 0;
        String comp = "no composition";
        result.put(comp, points);
        int currentResult = yahtzee(dice);
        if(currentResult > 0 && currentResult > points)
        { 
           points = currentResult;
           comp = "Yahtzee";
           result.put(comp, points);
        }
        currentResult = suite(dice);
        if(currentResult > 0  && currentResult > points)
        {
            if(currentResult == 30)
                comp = "Petite Suite";
            else if(currentResult == 40)
                comp = "Grande Suite";
                points = currentResult;   
            result.put(comp, points);
        }
        currentResult = full(countInArray);
        if(currentResult > 0 && currentResult > points)
        {
            points = currentResult;
            comp = "Full";
            result.put(comp, points);
        }
        currentResult = brelan(countInArray);
        if(currentResult > 0 && currentResult > points)
        {  
            points = currentResult;
            comp = "Brelan";
            result.put(comp, points);
        }
        currentResult = carre(countInArray);
        if(currentResult > 0  && currentResult > points)
        {  
            points = currentResult;
            comp = "Carre";
            result.put(comp, points);
        }
        return result;
    }
    
    private void processCommands() {
          
            while (input.hasNextLine() /*&& (roundCounter < totalRound || currentPlayer.index == allPlayer.size()) */ ) 
            {
                
                String command = input.nextLine();
                if (command.startsWith("BYE")) 
                {
                     output.println("GOOD BYE" );
                     for(Player plr : allPlayer)
                     {
                         if(this.output != plr.output)
                          plr.output.println("OTHER_PLAYER_LEFT");
                     }
                    return;
                } 
                else if(command.startsWith("START") && !isStarted )
                {
                    if(playerQueue.size() >= 1)
                    {
                        String names = currentPlayer.name;
                        for(Player plr : playerQueue)
                            names = names + " " + plr.name;
                      
                         // print 
                         for(Player player : allPlayer)
                         {
                            player.output.println("Let's start the game !");
                            player.output.println("Players are: " + names);
                            player.output.println("Current player is  " + currentPlayer.name);
                            printDices(player, currentPlayer);
                         }
                        isStarted = true;
                    }
                    else 
                    {
                        output.println("Waiting for other players");
                    }
                }
                else if(command.startsWith("LNC") && isStarted )
                {
                    if(currentPlayer.index == this.index)
                    {    
                        if(tryCounter < totalTry)
                        {
                            if(command.length() >= 5)
                            {
                                command = command.substring(4);
                                int answerFromGenerate = generateNewDice(command);
                                if(answerFromGenerate == 1)
                                {
                                  tryCounter++;

                                //print new Result
                                for(Player player : allPlayer)
                                {
                                   player.output.println("Plays  " + currentPlayer.name);
                                   player.output.println(command);
                                   printDices(player, currentPlayer);
                                }
                                
                                 if(tryCounter == totalTry)     // tried all times
                                 {
                                    if(allPlayer.size() == this.index )
                                    {   
                                        roundCounter++;
                                        System.out.println("count grew "+roundCounter);
                                    }
                                    int points = 0;
                                    String comp = "no composition";

                                    Map<String, Integer> result = findBest(this.dice);
                                    if(result.size() > 1)
                                    {
                                    points = (Collections.max(result.values())); 
                                    comp = Collections.max(result.entrySet(), Map.Entry.comparingByValue()).getKey();
                                    result.clear();
                                    }
                                    
                                    output.println("You got "+ comp +" - "+ points +" points ! ");         
                                    for(Player plr : playerQueue)
                                    {
                                        plr.output.println( currentPlayer.name + " got "+ comp +" - "+ points +" points !");
                                    }
                                    
                                    playerQueue.add(currentPlayer);
                                    currentPlayer = playerQueue.removeFirst();
                                    tryCounter = 0;
                                    totalSum += points;
                                    
                                    //print
                                    currentPlayer.output.println("Now It's  your turn");
                                    printDices(currentPlayer, currentPlayer);
                                    for(Player plr : playerQueue)
                                    {
                                       plr.output.println("Now It's  " + currentPlayer.name + "'s turn");
                                       printDices(plr, currentPlayer);
                                    }
                                    regenerate(this.dice);
                                 }
                                }
                                else
                                    output.println("Please input correct number of dice");
                            }
                            else
                            {
                                output.println("Please write the index of the dice you want to relaunch");
                            }
                        }
                        else 
                        {
                            output.println("Sorry, you've tried already " + totalTry + " times");
                        }
                    }
                    else 
                        output.println("Please wait, It isn't your turn");
                }
                else if(command.startsWith("STOP") && isStarted )
                {
                    if(currentPlayer.index == this.index)
                    {  
                       if(allPlayer.size() == this.index )
                       {   
                           roundCounter++;
                           System.out.println("count grew "+roundCounter);
                       }
                      int points = 0;
                      String comp = "no composition";
                      
                      Map<String, Integer> result = findBest(this.dice);
                      if(result.size() > 1)
                      {
                      points = (Collections.max(result.values())); 
                      comp = Collections.max(result.entrySet(), Map.Entry.comparingByValue()).getKey();
                      result.clear();
                      }
                        totalSum += points;

                        // print info
                        for(Player plr : allPlayer)
                        {
                            plr.output.println("Plays  " + currentPlayer.name);
                            plr.output.println(command);
                            plr.output.println( currentPlayer.name + " got "+ comp + " - " + points + " points !");
                        }
                        
                        playerQueue.add(currentPlayer);
                        currentPlayer = playerQueue.removeFirst();
                        tryCounter = 0;
                        //print
                        currentPlayer.output.println("Now It's  your turn");
                        printDices(currentPlayer, currentPlayer);
                        for(Player plr : playerQueue)
                        {
                            plr.output.println("Now It's  " + currentPlayer.name + "'s turn");
                            printDices(plr, currentPlayer);
                        }
                        regenerate(this.dice);
                        
                    }
                    else 
                         output.println("Please wait, It isn't your turn");
                }
            //finish and send results    
            if(totalRound == roundCounter )
            {
                System.out.println(totalRound + " - "+roundCounter);
                Player winner = allPlayer.getFirst();
                String result = "";
                for(Player player : allPlayer)
                {
                     if(player.totalSum > winner.totalSum)
                         winner = player;
                     result += player.name + ":  " + player.totalSum + " ";
                         
                }
                String text = "FINISH Winner is " + winner.name + " with " 
                             + winner.totalSum + " points !  All results: ";
                
                for(Player player : allPlayer)
                {
                     if(player.index != winner.index && player.totalSum == winner.totalSum)
                        text = "FINISH It's Tie. All results: ";
                }
                
                
                text += result;
                
                for(Player player : allPlayer)
                {
                     player.output.println(text);
                }
            }
                
            }
            
            
        }
    
        
    }
}