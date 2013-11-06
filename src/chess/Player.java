package chess;

import java.util.Scanner;

import javax.jms.Connection;
import javax.jms.JMSException;

import com.sun.messaging.ConnectionFactory;

import chessCore.Color;
import chessCore.GameState;
import chessCore.GameStateExchanger;
import chessCore.PartnerFinder;

public class Player {

	public static void main(String[] args) {
		ConnectionFactory connectionFactory = new ConnectionFactory();
		Connection connection;
		try {
			connection = connectionFactory.createConnection("admin", "admin");
			connection.start();
			PartnerFinder partnerFinder = new PartnerFinder(connection);
			Scanner in = new Scanner(System.in);
			int color = in.nextInt();
			Color preferedColor;
			if(color==0) {
				preferedColor=Color.Black;
			}
			else {
				preferedColor=Color.White;
			}
			
			partnerFinder.Find(preferedColor);
			System.out.println(preferedColor + " found partner.");
			
			GameState gameState = null;
			GameStateExchanger gameStateExchanger = new GameStateExchanger(connection, partnerFinder.GameUUID);
			if(preferedColor==Color.White) {
				gameState = new GameState();
			}
			
			do {
				if(preferedColor==Color.White) {
					// he needs to begin game
					System.out.println("w");
					Move(gameState, in);
					gameState = gameStateExchanger.PushAndGetGameState(gameState); 
				}
				else {
					System.out.println("b");
					gameState = gameStateExchanger.GetGameState();
					Move(gameState, in);
					gameStateExchanger.RespondWithGameState(gameState);
				}
			} while (!gameState.IsGameFinished);
			
			System.out.println("Game finished");
			connection.stop();
			in.close();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void Move(GameState gameState, Scanner in) {
		System.out.println("Game state is " + gameState.stateString);
		System.out.print("New state: ");
		gameState.stateString = in.nextInt()+"";
	}
	

}
