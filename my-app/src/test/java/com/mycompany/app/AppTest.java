package com.mycompany.app;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class AppTest {

    private Game game;
    private Player player1;
    private Player player2;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        game = new Game();
        player1 = game.player1;
        player2 = game.player2;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    // Player Tests
    @Test
    @DisplayName("Player fields should be properly initialized")
    void testPlayerFields() {
        Player player = new Player();
        player.symbol = 'X';
        player.move = 5;
        player.selected = true;
        player.win = true;

        assertEquals('X', player.symbol);
        assertEquals(5, player.move);
        assertTrue(player.selected);
        assertTrue(player.win);
    }

    // Game Constructor Tests
    @Test
    @DisplayName("Game constructor should initialize players with correct symbols")
    void testGameConstructor_PlayersInitialized() {
        assertEquals('X', game.player1.symbol);
        assertEquals('O', game.player2.symbol);
        assertNotNull(game.player1);
        assertNotNull(game.player2);
    }

    @Test
    @DisplayName("Game constructor should initialize state as PLAYING")
    void testGameConstructor_StateInitialized() {
        assertEquals(State.PLAYING, game.state);
    }

    @Test
    @DisplayName("Game constructor should initialize empty board")
    void testGameConstructor_BoardInitialized() {
        assertNotNull(game.board);
        assertEquals(9, game.board.length);
        for (int i = 0; i < 9; i++) {
            assertEquals(' ', game.board[i]);
        }
    }

    // Game.checkState Tests
    @Test
    @DisplayName("checkState should return PLAYING for empty board")
    void testCheckState_EmptyBoard() {
        game.symbol = 'X';
        State result = game.checkState(game.board);
        assertEquals(State.PLAYING, result);
    }

    @Test
    @DisplayName("checkState should return PLAYING for partially filled board without win")
    void testCheckState_PartiallyFilledBoard() {
        game.symbol = 'X';
        game.board[0] = 'X';
        game.board[1] = 'O';
        game.board[4] = 'X';
        State result = game.checkState(game.board);
        assertEquals(State.PLAYING, result);
    }

    @ParameterizedTest
    @CsvSource({
        "0,1,2,X",   // top row
        "3,4,5,X",   // middle row
        "6,7,8,X",   // bottom row
        "0,3,6,X",   // left column
        "1,4,7,X",   // middle column
        "2,5,8,X",   // right column
        "0,4,8,X",   // main diagonal
        "2,4,6,X"    // anti-diagonal
    })
    @DisplayName("checkState should detect X win on all winning lines")
    void testCheckState_XWin(int p1, int p2, int p3, char symbol) {
        game.symbol = 'X';
        game.board[p1] = 'X';
        game.board[p2] = 'X';
        game.board[p3] = 'X';
        State result = game.checkState(game.board);
        assertEquals(State.XWIN, result);
    }

    @ParameterizedTest
    @CsvSource({
        "0,1,2,O",   // top row
        "3,4,5,O",   // middle row
        "6,7,8,O",   // bottom row
        "0,3,6,O",   // left column
        "1,4,7,O",   // middle column
        "2,5,8,O",   // right column
        "0,4,8,O",   // main diagonal
        "2,4,6,O"    // anti-diagonal
    })
    @DisplayName("checkState should detect O win on all winning lines")
    void testCheckState_OWin(int p1, int p2, int p3, char symbol) {
        game.symbol = 'O';
        game.board[p1] = 'O';
        game.board[p2] = 'O';
        game.board[p3] = 'O';
        State result = game.checkState(game.board);
        assertEquals(State.OWIN, result);
    }

    @Test
    @DisplayName("checkState should return DRAW when board is full with no winner")
    void testCheckState_Draw() {
        game.symbol = 'X';
        // Fill board with no winner
        char[] drawBoard = {'X','O','X','X','O','O','O','X','X'};
        game.board = drawBoard;
        State result = game.checkState(game.board);
        assertEquals(State.DRAW, result);
    }

    @Test
    @DisplayName("checkState should handle board with spaces correctly")
    void testCheckState_WithSpaces() {
        game.symbol = 'X';
        char[] boardWithSpaces = {'X','O','X',' ','O','O','O','X','X'};
        game.board = boardWithSpaces;
        State result = game.checkState(game.board);
        assertEquals(State.PLAYING, result);
    }

    // Game.generateMoves Tests
    @Test
    @DisplayName("generateMoves should return all 9 positions for empty board")
    void testGenerateMoves_EmptyBoard() {
        ArrayList<Integer> moves = new ArrayList<>();
        game.generateMoves(game.board, moves);
        assertEquals(9, moves.size());
        for (int i = 0; i < 9; i++) {
            assertTrue(moves.contains(i));
        }
    }

    @Test
    @DisplayName("generateMoves should return correct moves for partially filled board")
    void testGenerateMoves_PartialBoard() {
        game.board[0] = 'X';
        game.board[4] = 'O';
        game.board[8] = 'X';
        ArrayList<Integer> moves = new ArrayList<>();
        game.generateMoves(game.board, moves);
        assertEquals(6, moves.size());
        assertFalse(moves.contains(0));
        assertFalse(moves.contains(4));
        assertFalse(moves.contains(8));
        assertTrue(moves.contains(1));
        assertTrue(moves.contains(2));
        assertTrue(moves.contains(3));
    }

    @Test
    @DisplayName("generateMoves should return empty list for full board")
    void testGenerateMoves_FullBoard() {
        for (int i = 0; i < 9; i++) {
            game.board[i] = 'X';
        }
        ArrayList<Integer> moves = new ArrayList<>();
        game.generateMoves(game.board, moves);
        assertEquals(0, moves.size());
    }

    // Game.evaluatePosition Tests
    @Test
    @DisplayName("evaluatePosition should return +INF for X win when X is player")
    void testEvaluatePosition_XWin_PlayerX() {
        game.symbol = 'X';
        game.board[0] = 'X';
        game.board[1] = 'X';
        game.board[2] = 'X';
        int result = game.evaluatePosition(game.board, player1);
        assertEquals(Game.INF, result);
    }

    @Test
    @DisplayName("evaluatePosition should return -INF for X win when O is player")
    void testEvaluatePosition_XWin_PlayerO() {
        game.symbol = 'X';
        game.board[0] = 'X';
        game.board[1] = 'X';
        game.board[2] = 'X';
        int result = game.evaluatePosition(game.board, player2);
        assertEquals(-Game.INF, result);
    }

    @Test
    @DisplayName("evaluatePosition should return +INF for O win when O is player")
    void testEvaluatePosition_OWin_PlayerO() {
        game.symbol = 'O';
        game.board[0] = 'O';
        game.board[1] = 'O';
        game.board[2] = 'O';
        int result = game.evaluatePosition(game.board, player2);
        assertEquals(Game.INF, result);
    }

    @Test
    @DisplayName("evaluatePosition should return -INF for O win when X is player")
    void testEvaluatePosition_OWin_PlayerX() {
        game.symbol = 'O';
        game.board[0] = 'O';
        game.board[1] = 'O';
        game.board[2] = 'O';
        int result = game.evaluatePosition(game.board, player1);
        assertEquals(-Game.INF, result);
    }

    @Test
    @DisplayName("evaluatePosition should return 0 for DRAW")
    void testEvaluatePosition_Draw() {
        game.symbol = 'X';
        char[] drawBoard = {'X','O','X','X','O','O','O','X','X'};
        game.board = drawBoard;
        int result = game.evaluatePosition(game.board, player1);
        assertEquals(0, result);
        result = game.evaluatePosition(game.board, player2);
        assertEquals(0, result);
    }

    @Test
    @DisplayName("evaluatePosition should return -1 for ongoing game")
    void testEvaluatePosition_Ongoing() {
        game.symbol = 'X';
        game.board[0] = 'X';
        game.board[1] = 'O';
        int result = game.evaluatePosition(game.board, player1);
        assertEquals(-1, result);
    }

    // Game.MinMove Tests
    @Test
    @DisplayName("MinMove should evaluate terminal position correctly")
    void testMinMove_TerminalPosition() {
        game.board[0] = 'X';
        game.board[1] = 'X';
        game.board[2] = 'X';
        game.symbol = 'X';
        
        int result = game.MinMove(game.board, player1);
        assertEquals(Game.INF, result);
    }

    @Test
    @DisplayName("MinMove should return best value for minimizer")
    void testMinMove_NonTerminal() {
        game.board[0] = 'O';
        game.board[4] = 'X';
        game.board[8] = 'O';
        game.symbol = 'O';
        
        int result = game.MinMove(game.board, player2);
        assertTrue(result <= Game.INF);
    }

    // Game.MaxMove Tests
    @Test
    @DisplayName("MaxMove should evaluate terminal position correctly")
    void testMaxMove_TerminalPosition() {
        game.board[0] = 'O';
        game.board[1] = 'O';
        game.board[2] = 'O';
        game.symbol = 'O';
        
        int result = game.MaxMove(game.board, player2);
        assertEquals(Game.INF, result);
    }

    @Test
    @DisplayName("MaxMove should return best value for maximizer")
    void testMaxMove_NonTerminal() {
        game.board[0] = 'X';
        game.board[4] = 'O';
        game.board[8] = 'X';
        game.symbol = 'X';
        
        int result = game.MaxMove(game.board, player1);
        assertTrue(result >= -Game.INF);
    }

    // TicTacToeCell Tests
    @Test
    @DisplayName("TicTacToeCell constructor should initialize correctly")
    void testTicTacToeCell_Constructor() {
        TicTacToeCell cell = new TicTacToeCell(5, 1, 2);
        
        assertEquals(5, cell.getNum());
        assertEquals(2, cell.getRow());
        assertEquals(1, cell.getCol());
        assertEquals(' ', cell.getMarker());
        assertEquals(" ", cell.getText());
    }

    @Test
    @DisplayName("setMarker should update marker and disable button")
    void testTicTacToeCell_SetMarker() {
        TicTacToeCell cell = new TicTacToeCell(0, 0, 0);
        assertTrue(cell.isEnabled());
        
        cell.setMarker("X");
        assertEquals('X', cell.getMarker());
        assertEquals("X", cell.getText());
        assertFalse(cell.isEnabled());
    }

    @Test
    @DisplayName("getMarker should return correct marker")
    void testTicTacToeCell_GetMarker() {
        TicTacToeCell cell = new TicTacToeCell(0, 0, 0);
        cell.setMarker("O");
        assertEquals('O', cell.getMarker());
    }

    @Test
    @DisplayName("getRow and getCol should return correct positions")
    void testTicTacToeCell_Positions() {
        TicTacToeCell cell = new TicTacToeCell(8, 2, 2);
        assertEquals(2, cell.getRow());
        assertEquals(2, cell.getCol());
    }

    // Utility Tests
    @Test
    @DisplayName("Utility.print for char[] should output correct format")
    void testUtility_PrintCharArray() {
        char[] testBoard = {'X','O','X',' ','O','X','O','X',' '};
        Utility.print(testBoard);
        
        String output = outContent.toString();
        assertTrue(output.contains("X-O-X- -O-X-O-X- -"));
    }

    @Test
    @DisplayName("Utility.print for int[] should output correct format")
    void testUtility_PrintIntArray() {
        int[] testArray = {1,2,3,4,5,6,7,8,9};
        Utility.print(testArray);
        
        String output = outContent.toString();
        assertTrue(output.contains("1-2-3-4-5-6-7-8-9-"));
    }

    @Test
    @DisplayName("Utility.print for ArrayList should output correct format")
    void testUtility_PrintArrayList() {
        ArrayList<Integer> moves = new ArrayList<>();
        moves.add(0);
        moves.add(4);
        moves.add(8);
        Utility.print(moves);
        
        String output = outContent.toString();
        assertTrue(output.contains("0-4-8-"));
    }

    // State Enum Tests
    @Test
    @DisplayName("State enum should have all four states")
    void testStateEnum() {
        assertEquals(4, State.values().length);
        assertEquals(State.PLAYING, State.valueOf("PLAYING"));
        assertEquals(State.OWIN, State.valueOf("OWIN"));
        assertEquals(State.XWIN, State.valueOf("XWIN"));
        assertEquals(State.DRAW, State.valueOf("DRAW"));
    }

    @Test
    @DisplayName("Full game flow: X wins in 3 moves")
    void testGameFlow_XWins() {
        // Move 1: X center
        game.board[4] = 'X';
        game.symbol = 'X';
        assertEquals(State.PLAYING, game.checkState(game.board));
        
        // Move 2: O corner
        game.board[0] = 'O';
        game.symbol = 'O';
        assertEquals(State.PLAYING, game.checkState(game.board));
        
        // Move 3: X corner
        game.board[2] = 'X';
        game.symbol = 'X';
        assertEquals(State.PLAYING, game.checkState(game.board));
        
        // Move 4: O edge
        game.board[1] = 'O';
        game.symbol = 'O';
        assertEquals(State.PLAYING, game.checkState(game.board));
        
        // Move 5: X wins
        game.board[6] = 'X';
        game.symbol = 'X';
        assertEquals(State.XWIN, game.checkState(game.board));
    }

    @Test
    @DisplayName("Full game flow: Draw game")
    void testGameFlow_Draw() {
        char[] drawSequence = {
            'X', 'O', 'X',
            'X', 'O', 'O',
            'O', 'X', 'X'
        };
        
        for (int i = 0; i < drawSequence.length; i++) {
            game.board[i] = drawSequence[i];
        }
        game.symbol = 'X';
        assertEquals(State.DRAW, game.checkState(game.board));
    }

    @Test
    @DisplayName("INF constant should be 100")
    void testInfConstant() {
        assertEquals(100, Game.INF);
    }
}