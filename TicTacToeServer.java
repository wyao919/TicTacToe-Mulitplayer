
import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.Scanner;

import javax.swing.JOptionPane;

import org.omg.Messaging.SyncScopeHelper;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class TicTacToeServer extends Application 
  implements TicTacToeConstants {
  private int sessionNo = 1; // Number a session
  
  @Override // Override the start method in the Application class
  public void start(Stage primaryStage) {
    TextArea taLog = new TextArea();

    // Create a scene and place it in the stage
    Scene scene = new Scene(new ScrollPane(taLog), 450, 200);
    primaryStage.setTitle("TicTacToeServer"); // Set the stage title
    primaryStage.setScene(scene); // Place the scene in the stage
    primaryStage.show(); // Display the stage

    new Thread( () -> {
      try {
        // Create a server socket
        ServerSocket serverSocket = new ServerSocket(8000);
        Platform.runLater(() -> taLog.appendText(new Date() +
          ": Server started at socket 8000\n"));
  
        // Ready to create a session for every two players
        while (true) {
          Platform.runLater(() -> taLog.appendText(new Date() +
            ": Wait for players to join session " + sessionNo + '\n'));
  
          // Connect to player 1
          Socket player1 = serverSocket.accept();
          DataInputStream dis = new DataInputStream(player1.getInputStream());
          String name1 = dis.readUTF();
          
  
          Platform.runLater(() -> {
            taLog.appendText(new Date() + ": " + name1 + " has joined session " 
              + sessionNo + '\n');
            taLog.appendText("Player 1's IP address" +
              player1.getInetAddress().getHostAddress() + '\n');
          });
  
          // Notify that the player is Player 1
          new DataOutputStream(
            player1.getOutputStream()).writeInt(PLAYER1);
          DataOutputStream out1 = new DataOutputStream(player1.getOutputStream());
          out1.writeUTF(name1);
          
  
          // Connect to player 2
          Socket player2 = serverSocket.accept();
          DataInputStream dis2 = new DataInputStream(player2.getInputStream());
          String name2 = dis2.readUTF();

  
          Platform.runLater(() -> {
            taLog.appendText(new Date() +
              ": has joined session " + sessionNo + '\n');
            taLog.appendText("Player 2's IP address" +
              player2.getInetAddress().getHostAddress() + '\n');
          });
  
          // Notify that the player is Player 2
          new DataOutputStream(
            player2.getOutputStream()).writeInt(PLAYER2);
          DataOutputStream out = new DataOutputStream(player2.getOutputStream());
          out.writeUTF(name2);
        
  
          // Display this session and increment session number
          Platform.runLater(() -> 
            taLog.appendText(new Date() + 
              ": Start a thread for session " + sessionNo++ + '\n'));
  
          // Launch a new thread for this session of two players
          new Thread(new HandleASession(player1, player2, name1, name2)).start();
        }
      }
      catch(IOException ex) {
        ex.printStackTrace();
      }
    }).start();
  }

  // Define the thread class for handling a new session for two players
  class HandleASession implements Runnable, TicTacToeConstants {
    private Socket player1;
    private Socket player2;
    private String name1;
    private String name2;
    public Integer[] playerTotal = new Integer[2];
    public String continue1;
    public String continue2;
    String p2r;
    String p1r;

//    public int player2Total = 0;
  
    // Create and initialize cells
    private char[][] cell =  new char[3][3];
  
    private DataInputStream fromPlayer1;
    private DataOutputStream toPlayer1;
    private DataInputStream fromPlayer2;
    private DataOutputStream toPlayer2;
  
    // Continue to play
    private boolean continueToPlay = true;
  
    /** Construct a thread */
    public HandleASession(Socket player1, Socket player2, String name1, String name2) {
      this.player1 = player1;
      this.player2 = player2;
      this.name1 = name1;
      this.name2 = name2;

      
      // Initialize cells
      for (int i = 0; i < 3; i++)
        for (int j = 0; j < 3; j++)
          cell[i][j] = ' ';
    }
  
    /** Implement the run() method for the thread */
    public void run() {
      try {
    	  
        // Create data input and output streams
        DataInputStream fromPlayer1 = new DataInputStream(
          player1.getInputStream());
        DataOutputStream toPlayer1 = new DataOutputStream(
          player1.getOutputStream());
        DataInputStream fromPlayer2 = new DataInputStream(
          player2.getInputStream());
        DataOutputStream toPlayer2 = new DataOutputStream(
          player2.getOutputStream());
        // Write anything to notify player 1 to start
        // This is just to let player 1 know to start
        String name11 = name1;
        String name22 = name2;
        playerTotal[0] = 0;
        playerTotal[1] = 0;
         
        String temp;
        String [] sort = new String[2];
        sort[0] = name1.toLowerCase();
        sort[1]= name2.toLowerCase();
  
        if (sort[0].compareTo(sort[1])>0) 
        {
            temp = sort[0];
            sort[0] = sort[1];
            sort[1] = temp;
        }

        String fileName = sort[0] + sort[1] + ".txt";     //creating a new file using the userName
        File file = new File(fileName);     //creating a new file using the userName
        
        toPlayer1.writeInt(1);
        toPlayer1.writeUTF(name2);
        toPlayer1.writeUTF(name1);
        toPlayer2.writeUTF(name1);
        toPlayer2.writeUTF(name2);
        
        retrieveStats(file, fileName, sort);
        do {

        // Continuously serve the players and determine and report
        // the game status to the players
        System.out.println("If you see this twice do while loop is back at the top");
        int k =0;
        int m = 0;
        int n =0;
        while (true) {
        	System.out.println(k);
        	k++;
          // Receive a move from player 1
          int row = fromPlayer1.readInt();
          int column = fromPlayer1.readInt();
          System.out.println(n);
          cell[row][column] = 'X';
  
          // Check if Player 1 wins
          if (isWon('X')) {
            toPlayer1.writeInt(PLAYER1_WON);
            toPlayer2.writeInt(PLAYER1_WON);
            sendMove(toPlayer2, row, column);
        
            if(name11.equalsIgnoreCase(sort[0]))
            {
            	playerTotal[0]++;
            }
            else
            {
            	playerTotal[1]++;
            }
            saveStats(file);
            break; // Break the loop
          }
          else if (isFull()) { // Check if all cells are filled
            toPlayer1.writeInt(DRAW);
            toPlayer2.writeInt(DRAW);
            sendMove(toPlayer2, row, column);
            break;
          }
          else {
            // Notify player 2 to take the turn
            toPlayer2.writeInt(CONTINUE);
  
            // Send player 1's selected row and column to player 2
            sendMove(toPlayer2, row, column);
          }
  
          // Receive a move from Player 2
          row = fromPlayer2.readInt();
          column = fromPlayer2.readInt();
          cell[row][column] = 'O';
          // Check if Player 2 wins
          if (isWon('O')) {
            toPlayer1.writeInt(PLAYER2_WON);
            toPlayer2.writeInt(PLAYER2_WON);
            sendMove(toPlayer1, row, column);
    
            if(name22.equalsIgnoreCase(sort[1]))
            {
            	playerTotal[1]++;
            	System.out.println(sort[1]);
            }
            else
            {
            	playerTotal[0]++;
            	System.out.println(sort[0]);
            }
            saveStats(file);

            break;
          }
          else {
            // Notify player 1 to take the turn
        	  System.out.println("did i continue?");
            toPlayer1.writeInt(CONTINUE);
            System.out.println("i did");
              // Send player 2's selected row and column to player 1
            sendMove(toPlayer1, row, column);
          }
        }
        System.out.println("server over");
        toPlayer2.writeUTF(sort[0] + " has "+ playerTotal[0] + " wins\n" + sort[1] + " has "+ playerTotal[1] + " wins\n" + "Enter y/Y for a rematch");
        toPlayer1.writeUTF(sort[0] + " has "+ playerTotal[0] + " wins\n" + sort[1] + " has "+ playerTotal[1] + " wins\n" + "Enter y/Y for a rematch");
        p2r = fromPlayer2.readUTF();
        p1r = fromPlayer1.readUTF();
        toPlayer1.writeUTF(p2r);
        toPlayer2.writeUTF(p1r);
       
		if(p2r.equalsIgnoreCase("y") && p1r.equalsIgnoreCase("y"))
		{
			clearCells();	
		}
		System.out.println("server made it to the bottom");
		
        }while(p2r.equalsIgnoreCase("y") && p2r.equalsIgnoreCase("y"));
 
      }catch(IOException ex) {ex.printStackTrace();}

    }

	private void clearCells() {
		// TODO Auto-generated method stub
		 for (int i = 0; i < 3; i++)
		  {
			  for (int j = 0; j < 3; j++)
			  {
				  System.out.println(cell[i][j]); // only to test that cells are clear
			  }       
		  }
		 
		 for (int i = 0; i < 3; i++)
		  {
			  for (int j = 0; j < 3; j++)
			  {
				  cell[i][j] = ' ';
				  System.out.println(cell[i][j]); // only to test that cells are clear
			  }       
		  }
		 System.out.println("Clear Cells done");
	}

	private void saveStats(File file) throws IOException {
		PrintWriter pw = new PrintWriter(file);

        //Write/save stats to file so that it may be retrieved 
        pw.println(playerTotal[0]);
        pw.println(playerTotal[1]);
        System.out.println(playerTotal[0]);
        System.out.println(playerTotal[1]);
      
        pw.close();
    }

	public void retrieveStats(File file, String fileName, String sort[]) throws FileNotFoundException   {
	
	        int player1wins =0;
	        int player2wins =0;
	    	//Declare variables
	        

	        //Run this is file exists
	        if(file.exists())
	        {
	            System.out.println("Welcome back ");  //welcome back the new user
	            Scanner input = new Scanner(file); //new scanner input to collect data from existing file
	            player1wins = input.nextInt();  //Collect total right from existing file
	            player2wins = input.nextInt();  //Collect total wrong from existing file

	            //Display existing stats to user before game starts
	            System.out.println("Game Stats:");
	            System.out.println(sort[0] + "has "+ player1wins + " wins");
	            System.out.println(sort[1] + "has "+ player2wins + " wins");
	            System.out.println("");
	            
	            //Recollect data for Class variables correct, incorrect, and totalEarnings so stats will continue from here.
	            playerTotal[0] = player1wins;
	            playerTotal[1] = player2wins;

	            //close file
	            input.close();
	        }  
	    }
	

	/** Send the move to other player */
    private void sendMove(DataOutputStream out, int row, int column)
        throws IOException {
      out.writeInt(row); // Send row index
      out.writeInt(column); // Send column index
    }
  
    /** Determine if the cells are all occupied */
    private boolean isFull() {
      for (int i = 0; i < 3; i++)
        for (int j = 0; j < 3; j++)
          if (cell[i][j] == ' ')
            return false; // At least one cell is not filled
  
      // All cells are filled
      return true;
    }
  
    /** Determine if the player with the specified token wins */
    private boolean isWon(char token) {
      // Check all rows
      for (int i = 0; i < 3; i++)
        if ((cell[i][0] == token)
            && (cell[i][1] == token)
            && (cell[i][2] == token)) {
          return true;
        }
  
      /** Check all columns */
      for (int j = 0; j < 3; j++)
        if ((cell[0][j] == token)
            && (cell[1][j] == token)
            && (cell[2][j] == token)) {
          return true;
        }
  
      /** Check major diagonal */
      if ((cell[0][0] == token)
          && (cell[1][1] == token)
          && (cell[2][2] == token)) {
        return true;
      }
  
      /** Check subdiagonal */
      if ((cell[0][2] == token)
          && (cell[1][1] == token)
          && (cell[2][0] == token)) {
        return true;
      }
  
      /** All checked, but no winner */
      return false;
    }
  }
  
  /**
   * The main method is only needed for the IDE with limited
   * JavaFX support. Not needed for running from the command line.
   */
  public static void main(String[] args) {
    launch(args);
    
  }
}