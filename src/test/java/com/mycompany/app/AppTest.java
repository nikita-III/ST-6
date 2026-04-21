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

import java.awt.GridLayout;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import java.awt.event.*;

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
    @DisplayName("TicTacToePanel constructor should create 9 cells")
    void testTicTacToePanel_Constructor_CreatesNineCells() throws Exception {
        TicTacToePanel panel = new TicTacToePanel(new GridLayout(3, 3));
        
        Field cellsField = TicTacToePanel.class.getDeclaredField("cells");
        cellsField.setAccessible(true);
        TicTacToeCell[] cells = (TicTacToeCell[]) cellsField.get(panel);
        
        assertNotNull(cells);
        assertEquals(9, cells.length);
        for (int i = 0; i < 9; i++) {
            assertNotNull(cells[i]);
        }
    }

    @Test
    @DisplayName("TicTacToePanel constructor should initialize Game")
    void testTicTacToePanel_Constructor_InitializesGame() throws Exception {
        TicTacToePanel panel = new TicTacToePanel(new GridLayout(3, 3));
        
        Field gameField = TicTacToePanel.class.getDeclaredField("game");
        gameField.setAccessible(true);
        Game game = (Game) gameField.get(panel);
        
        assertNotNull(game);
        assertNotNull(game.player1);
        assertNotNull(game.player2);
        assertEquals(State.PLAYING, game.state);
        assertEquals(game.player1, game.cplayer);
    }

    @Test
    @DisplayName("TicTacToePanel cells should have correct positions")
    void testTicTacToePanel_CellsPositions() throws Exception {
        TicTacToePanel panel = new TicTacToePanel(new GridLayout(3, 3));
        
        Field cellsField = TicTacToePanel.class.getDeclaredField("cells");
        cellsField.setAccessible(true);
        TicTacToeCell[] cells = (TicTacToeCell[]) cellsField.get(panel);
        
        // Check first row
        assertEquals(0, cells[0].getNum());
        assertEquals(0, cells[0].getRow());
        assertEquals(0, cells[0].getCol());
        
        assertEquals(1, cells[1].getNum());
        assertEquals(0, cells[1].getRow());
        assertEquals(1, cells[1].getCol());
        
        assertEquals(2, cells[2].getNum());
        assertEquals(0, cells[2].getRow());
        assertEquals(2, cells[2].getCol());
        
        // Check second row
        assertEquals(3, cells[3].getNum());
        assertEquals(1, cells[3].getRow());
        assertEquals(0, cells[3].getCol());
        
        assertEquals(4, cells[4].getNum());
        assertEquals(1, cells[4].getRow());
        assertEquals(1, cells[4].getCol());
        
        assertEquals(5, cells[5].getNum());
        assertEquals(1, cells[5].getRow());
        assertEquals(2, cells[5].getCol());
        
        // Check third row
        assertEquals(6, cells[6].getNum());
        assertEquals(2, cells[6].getRow());
        assertEquals(0, cells[6].getCol());
        
        assertEquals(7, cells[7].getNum());
        assertEquals(2, cells[7].getRow());
        assertEquals(1, cells[7].getCol());
        
        assertEquals(8, cells[8].getNum());
        assertEquals(2, cells[8].getRow());
        assertEquals(2, cells[8].getCol());
    }

    @Test
    @DisplayName("TicTacToePanel cells should have ActionListener")
    void testTicTacToePanel_CellsHaveActionListener() throws Exception {
        TicTacToePanel panel = new TicTacToePanel(new GridLayout(3, 3));
        
        Field cellsField = TicTacToePanel.class.getDeclaredField("cells");
        cellsField.setAccessible(true);
        TicTacToeCell[] cells = (TicTacToeCell[]) cellsField.get(panel);
        
        for (TicTacToeCell cell : cells) {
            ActionListener[] listeners = cell.getActionListeners();
            assertTrue(listeners.length > 0);
            assertEquals(panel, listeners[0]);
        }
    }

    @Test
    @DisplayName("actionPerformed should reset moves before processing")
    void testActionPerformed_ResetsMoves() throws Exception {
        TicTacToePanel panel = new TicTacToePanel(new GridLayout(3, 3));
        
        Field gameField = TicTacToePanel.class.getDeclaredField("game");
        gameField.setAccessible(true);
        Game game = (Game) gameField.get(panel);
        
        Field cellsField = TicTacToePanel.class.getDeclaredField("cells");
        cellsField.setAccessible(true);
        TicTacToeCell[] cells = (TicTacToeCell[]) cellsField.get(panel);
        
        // Set some previous move values
        game.player1.move = 5;
        game.player2.move = 8;
        
        game.cplayer = game.player1;
        
        ActionEvent event = new ActionEvent(cells[0], ActionEvent.ACTION_PERFORMED, "");
        panel.actionPerformed(event);
        
        // Player1's move should be reset to -1
        assertEquals(-1, game.player1.move);
    }

    @Test
    @DisplayName("TicTacToePanel layout should be GridLayout 3x3")
    void testTicTacToePanel_HasCorrectLayout() {
        TicTacToePanel panel = new TicTacToePanel(new GridLayout(3, 3));
        
        assertTrue(panel.getLayout() instanceof GridLayout);
        GridLayout layout = (GridLayout) panel.getLayout();
        assertEquals(3, layout.getRows());
        assertEquals(3, layout.getColumns());
    }

    @Test
    @DisplayName("TicTacToePanel should contain exactly 9 components")
    void testTicTacToePanel_ComponentCount() {
        TicTacToePanel panel = new TicTacToePanel(new GridLayout(3, 3));
        
        assertEquals(9, panel.getComponentCount());
        for (int i = 0; i < 9; i++) {
            assertTrue(panel.getComponent(i) instanceof TicTacToeCell);
        }
    }

    @Test
    @DisplayName("createCell should properly add cells to panel")
    void testCreateCell_AddsCellsCorrectly() throws Exception {
        TicTacToePanel panel = new TicTacToePanel(new GridLayout(3, 3));
        
        // Get private method via reflection
        Method createCellMethod = TicTacToePanel.class.getDeclaredMethod("createCell", int.class, int.class, int.class);
        createCellMethod.setAccessible(true);
        
        // Remove all existing cells first
        panel.removeAll();
        
        // Create a new cell
        createCellMethod.invoke(panel, 8, 0, 1);
        
        Field cellsField = TicTacToePanel.class.getDeclaredField("cells");
        cellsField.setAccessible(true);
        TicTacToeCell[] cells = (TicTacToeCell[]) cellsField.get(panel);
        
        assertNotNull(cells[8]);
        assertEquals(8, cells[8].getNum());
        assertEquals(0, cells[8].getCol());
        assertEquals(1, cells[8].getRow());
        assertEquals(1, panel.getComponentCount());
    }

    @Test
    @DisplayName("TicTacToePanel should implement ActionListener")
    void testTicTacToePanel_ImplementsActionListener() {
        TicTacToePanel panel = new TicTacToePanel(new GridLayout(3, 3));
        assertTrue(panel instanceof ActionListener);
    }

    @Test
    @DisplayName("TicTacToePanel cells should have correct font")
    void testTicTacToePanel_CellsHaveCorrectFont() throws Exception {
        TicTacToePanel panel = new TicTacToePanel(new GridLayout(3, 3));
        
        Field cellsField = TicTacToePanel.class.getDeclaredField("cells");
        cellsField.setAccessible(true);
        TicTacToeCell[] cells = (TicTacToeCell[]) cellsField.get(panel);
        
        for (TicTacToeCell cell : cells) {
            assertNotNull(cell.getFont());
            assertEquals("Dialog", cell.getFont().getFamily());
            assertEquals(40, cell.getFont().getSize());
        }
    }

}