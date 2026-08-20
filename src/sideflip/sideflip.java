package sideflip;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.text.*;
import java.util.*;
import util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import java.time.*;
import java.time.format.DateTimeFormatter;

public final class sideflip 
{
	private static void print(JTextPane textpane,String html)
	{
		textpane.setContentType("text/html");
		textpane.setText(html);
	}
	private static JTextPane newtextdisplay()
	{
		JTextPane disp=new JTextPane();
		disp.setFocusable(false);	// make not focusable (and not editable)
		disp.setBackground(null);	// make transparent
		DefaultCaret caret=new DefaultCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		disp.setCaret(caret);		// stop scrollpane from tracking the caret in (disp)
		return disp;
	}
	private final Dimension winsize = new Dimension(840,770);
	private final JFrame win = new JFrame();
	private final JPanel game = new JPanel();
	private final JPanel board = new JPanel();
	private final JPanel control = new JPanel();
	private final JPanel control2 = new JPanel();
	private final JTextPane status;
	private final JTextPane pointStatus;
	private final ButtonGroup inputNumPlayer = new ButtonGroup();
	private final JRadioButton input2Player = new JRadioButton("2 Player");
	private final JRadioButton input1Player = new JRadioButton("1 Player");
	private final JTextField inputn = new JTextField(7);
	
	private final int cellwidth = 49;
	private final int cellspacing = 4;
	private final int statusheight = 70;
	private final int boxwidth = cellwidth+cellspacing;
	private final Dimension boxsize = new Dimension(boxwidth, boxwidth);
	private final Dimension cellsize = new Dimension(cellwidth, cellwidth);
	private JPanel[][] cells;
	private JPanel[][] boxes;
	
	private int n=8;
	private int[] points = new int[3];
	private int[][] boardState;
	private int[] currentMove;
	private int[] bestMove = new int[4];
	private int selX=-1, selY=-1;
	private int turn = 1;
	private final ArrayList<int[]> history = new ArrayList<int[]>();
	private int movep = 0;
	
	private long INF = 1000000000;
	
	private boolean computerOn = false;
	private int computerTurn = -1;
	
	public sideflip()
	{
		// Make Window //
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		win.setPreferredSize(winsize);
		
		// Make game in a scroll-pane //
		win.add(new JScrollPane(game));
		game.setFocusable(true);
		game.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.ipadx=7;
		gbc.ipady=7;
		gbc.gridx=1;
		gbc.gridy=1;
		
		// Add Board //
		game.add(board, gbc);
		gbc.gridy++;
		
		// Space //
		control.add(new JLabel("   "),gbc);
		gbc.gridy++;
		
		Font customFont = new Font("Arial", Font.BOLD, 14);
		
		// Add Control //
		control.setLayout(new GridBagLayout());
		{
			GridBagConstraints gbc2 = new GridBagConstraints();
			gbc2.gridx=1;
			gbc2.gridy=1;
			
			// Input number of player //
			input1Player.setFont(customFont);
			input2Player.setFont(customFont);
			control.add(input2Player, gbc2);
			gbc2.gridx++;
			control.add(input1Player, gbc2);
			gbc2.gridx++;
			inputNumPlayer.add(input2Player);
			inputNumPlayer.add(input1Player);
			
			DefaultListener inputPlayerAction= new DefaultListener()
			{
				public void actionPerformed(ActionEvent e) { updateNumPlayer(); }
			};
			input1Player.addActionListener(inputPlayerAction);
			input2Player.addActionListener(inputPlayerAction);
			input2Player.setSelected(true);
			
			// Space //
			control.add(new JLabel("   "),gbc2);
			gbc2.gridx++;
			
			// Input Board Size //
			JLabel BoardSize = new JLabel("Board Size: ");
			BoardSize.setFont(customFont);
			control.add(BoardSize, gbc2);
			gbc2.gridx++;
			inputn.setText(""+n);
			control.add(inputn, gbc2);
			gbc2.gridx++;
			
			// Space //
			control.add(new JLabel("   "),gbc2);
			gbc2.gridx++;
			
			// Go Button //
			JButton go = new JButton("GO!");
			go.setBackground(Color.decode("#E03F4F"));
			go.setFont(customFont);
			go.setForeground(Color.decode("#FFFAF0"));
			control.add(go, gbc2);
			gbc2.gridx++;
			go.addActionListener(
					new DefaultListener() 
					{
						public void actionPerformed(ActionEvent e) { init(); }
					}
			);
		}
		game.add(control,gbc);
		gbc.gridy++;
		
		// control2 //
		control2.setLayout(new GridBagLayout());
		{
			GridBagConstraints gbc2 = new GridBagConstraints();
			gbc2.gridx=1;
			gbc2.gridy=1;
			
			// Undo Button //
			JButton undo = new JButton("Undo");
			undo.setBackground(Color.decode("#547A95"));
			undo.setFont(customFont);
			undo.setForeground(Color.decode("#E8EDF2"));
			control2.add(undo, gbc2);
			gbc2.gridx++;
			undo.addActionListener(
					new DefaultListener()
					{
						public void actionPerformed(ActionEvent e) { undo(); }
					}
			);
			
			// Space //
			control2.add(new JLabel("   "),gbc2);
			gbc2.gridx++;
			
			// Redo Button //
			JButton redo = new JButton("Redo");
			redo.setBackground(Color.decode("#C2A56D"));
			redo.setFont(customFont);
			redo.setForeground(Color.decode("#E8EDF2"));
			control2.add(redo, gbc2);
			gbc2.gridx++;
			redo.addActionListener(
					new DefaultListener()
					{
						public void actionPerformed(ActionEvent e) { redo(); }
					}
			);
			
			// Space //
			control2.add(new JLabel("   "),gbc2);
			gbc2.gridx++;
			
			// Save Button //
			JButton save = new JButton("Save");
			save.setBackground(Color.decode("#5C766D"));
			save.setFont(customFont);
			save.setForeground(Color.decode("#E8EDF2"));
			control2.add(save, gbc2);
			gbc2.gridx++;
			save.addActionListener(
					new DefaultListener()
					{
						public void actionPerformed(ActionEvent e) { save(); }
					}
			);
			
			// Space //
			control2.add(new JLabel("   "),gbc2);
			gbc2.gridx++;
			
			// Load Button //
			JButton load = new JButton("Load");
			load.setBackground(Color.decode("#5C4F4A"));
			load.setFont(customFont);
			load.setForeground(Color.decode("#E8EDF2"));
			control2.add(load, gbc2);
			gbc2.gridx++;
			load.addActionListener(
					new DefaultListener()
					{
						public void actionPerformed(ActionEvent e) { load(); }
					}
			);
		}
		game.add(control2,gbc);
		gbc.gridy++;
		
		// Space //
		control2.add(new JLabel("   "),gbc);
		gbc.gridy++;
		
		// Add Point Status //
		pointStatus = new JTextPane();
		pointStatus.setBackground(Color.decode("#2C3947"));
		game.add(pointStatus, gbc);
		gbc.gridy++;
		
		// Add Status //
		status = new JTextPane();
		status.setBackground(null);
		game.add(status, gbc);
		gbc.gridy++;
		
		// Display Window //
		win.pack();
		win.setVisible(true);
		
		// Init //
		init();
		
		// Remove focus on click outside the board and control //
		Toolkit.getDefaultToolkit().addAWTEventListener(
			new AWTEventListener()
			{
				public void eventDispatched(AWTEvent e)
				{
					if( e instanceof MouseEvent && e.getID()==MouseEvent.MOUSE_PRESSED )
					{
						Object src=e.getSource();
						if( src instanceof Component )
						{
							Component component=(Component)src;
							boolean clickboard=board.isAncestorOf(component);
							if( clickboard || control.isAncestorOf(component) ) return;
							game.requestFocus();
						}
					}
				}
			}
		,AWTEvent.MOUSE_EVENT_MASK);
		
		// Make Key Listener //
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(
				new KeyEventDispatcher() 
				{
					public boolean dispatchKeyEvent(KeyEvent e) 
					{
						Component component=e.getComponent();	// usually the currently focused component
						int code=e.getKeyCode();
						int mod=e.getModifiersEx();
						boolean ctrl=0<(mod&KeyEvent.CTRL_DOWN_MASK);
						boolean shift=0<(mod&KeyEvent.SHIFT_DOWN_MASK);
						boolean alt=0<(mod&KeyEvent.ALT_DOWN_MASK);
						String key=(ctrl?"ctrl ":"")+(shift?"shift ":"")+(alt?"alt ":"")+code;
						if( e.getID()==KeyEvent.KEY_PRESSED )
						{
							// Control keys //
							if( key.equals("ctrl 78") ) init();	// initializes on Ctrl+N
							// undo (Ctrl + Z)
							if( key.equals("ctrl 90") ) undo();
							// redo (Ctrl + Y)
							if( key.equals("ctrl 89") ) redo();
							if( control.isAncestorOf(component) )
							{
								if( key.equals("27") ) game.requestFocus();	// to remove focus from form elements
								if( key.equals("10") ) init();	// initalizes on Enter within (control)
								if( ( code==38 || code==40 ) && component instanceof JTextField ) return true;	// gobbles up/down keys in JTextField
								return false;	// do not process key
							}
						}
						return false;
					}
				}
		);
		
	}
	
	private JPanel createMeeple1()
	{
		JPanel meeple = new JPanel();
		meeple.setBackground(null);
		JLabel player = new JLabel("<html><div style='font-size: 42; color: #7B2525;'>✖</div></html>");
		player.setBorder(BorderFactory.createEmptyBorder(-7,-1,0,-1));
		meeple.add(player);
		return meeple;	
	}
	private JPanel createMeeple2()
	{
		JPanel meeple = new JPanel();
		meeple.setBackground(null);
		JLabel player = new JLabel("<html><div style='font-size: 42; color: #607456; font-weight: bold;'>●</div></html>");
		player.setBorder(BorderFactory.createEmptyBorder(-7,-1,0,-1));
		meeple.add(player);
		return meeple;	
	}
	
	private void init()
	{
		// Get board size //
		try { 
			int inputVal=Integer.parseInt(inputn.getText()); 
			if(inputVal>=6 && inputVal%2==0)  { n = inputVal; } 
			else
			{
				inputn.setText(""+n);
				print(status, "<div style='color: red; text-align: center;'>" + 
						"Invalid size! Must be an <b>even number ≥ 6</b>.</div>");
				return;
			}
		} 
		catch(NumberFormatException e) {
			inputn.setText(""+n);
			return;
		}
		
		// Initial game state //
		points[1] = 0;
		points[2] = 0;
		turn = 1;
		selX=-1;
		selY=-1;
		history.clear();
		movep=0;
				
		// Make Board //
		cells = new JPanel[n][n];
		boxes = new JPanel[n][n];
		boardState = new int[n][n];
		currentMove = new int[13];
		board.removeAll();
		board.setLayout(new GridBagLayout());
		board.setBackground(Color.decode("#BA6A4C"));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.ipadx=2;
		gbc.ipady=2;
		for(int r=0; r<n; r++) for(int c=0; c<n; c++)
		{
			// Make Board Cell //
			JPanel cell = new JPanel();
			cells[r][c] = cell;
			cell.setMinimumSize(cellsize);
			cell.setPreferredSize(cellsize);
			cell.setBackground(null);
			cell.setLayout(new GridBagLayout());
			JPanel box = new JPanel();
			boxes[r][c] = box;
			box.setPreferredSize(boxsize);
			box.setLayout(new GridBagLayout());
			box.setBorder(new BevelBorder(BevelBorder.LOWERED));
			box.add(cell);
			gbc.gridx=c;
			gbc.gridy=r;
			board.add(box, gbc);
			box.setBackground(Color.decode("#EEE0CC"));
			
			// Make Mouse Listener for Board Cell //
			final int x=c, y=r;
			box.addMouseListener(
					new DefaultListener()
					{
						public void mousePressed(MouseEvent e)
						{
							if(e.getButton()==1)
							{
								handleClick(x,y);
								game.requestFocus();
							}
						}
					}
			);
			
		}
		int boardwidth=board.getPreferredSize().width;
		
		// Resize Status //
		status.setPreferredSize(new Dimension(boardwidth, statusheight));
		
		// Start the game //
		int center = n/2;
		cells[center-1][center-1].add(createMeeple1());
		boardState[center-1][center-1] = 1;
		cells[center-1][center].add(createMeeple2());
		boardState[center-1][center] = 2;
		cells[center][center-1].add(createMeeple2());
		boardState[center][center-1] = 2;
		cells[center][center].add(createMeeple1());
		boardState[center][center] = 1;
		
		updatePoint();
		updateUI();
		print(status,"<div style='text-align: center; font-family: Arial; font-size: 14px;'><b>Ready!</b><br>"
				   +"Choose your piece</div>");
		game.requestFocus(); // to remove focus from form elements
		history.add(new int[13]);
	}
	
	private void updateUI()
	{
		String turnInfo = turn==1 ? "Player 1 (⨯)" : "Player 2 (●)";
		String html = "<html><div style='font-family: Arial; font-size: 14px; font-weight: bold; width: 200px; color: #F0F0DB'>" +
	                "<table width='100%'>" +
	                "<tr>" + 
	                "<td width='45%' align='center'><b>P1 (⨯):</b> " + points[1] + "</td>" +
	                "<td width='10%' align='center'><b> | </b></td>" +
	                "<td width='45%' align='center'><b>P2 (●):</b> " + points[2] + "</td>" +
	                "</tr>" +
	                "<tr>" +
	                "<td colspan='3' align='center'><font color='#ACBAC4'>Turn: " + turnInfo + "</font></td>" +
	                "</tr>" +
	                "</table></div></html>";
	    
	    print(pointStatus, html);
	    board.revalidate();
	    board.repaint();
	}
	
	private void placePiece(int x, int y, int player)
	{
		boardState[y][x] = player;
	}
	
	private void resetPiece(int x, int y)
	{
		boardState[y][x]=0;
	}
	
	private void handleClick(int x, int y)
	{
		print(status, " ");
		// Piece is selected //
		if(selX!=-1)
		{
			if(boardState[y][x]==0)
			{
				makeMove(selX, selY, x, y);
				return;
			}
			else if(boardState[y][x]==3-turn)
			{
				print(status,"<div style='text-align: center; font-family: Arial; font-size: 14px; font-weight: bold; color: #7B2525;'>"
						+ "Choose empty cell!</div>");
				return;
			}
			else
			{
				selX=x;
				selY=y;
				drawHiglights();
				return;
			}
		}
		
		// Selecting own piece //
		if(boardState[y][x]==turn)
		{
			selX = x;
			selY = y;
			drawHiglights();
			return;
		}
		else
		{
			print(status,"<div style='text-align: center; font-family: Arial; font-size: 14px; font-weight: bold; color: #7B2525;'>Choose your own piece!</div>");
			return;
		}
	}
	
	private void makeMove(int x0, int y0, int x, int y)
	{
		int distance = Math.max(Math.abs(x0-x), Math.abs(y0-y));
		if(distance == 0)
		{
			selX=-1;
			selY=-1;
			return;
		}

		if(distance>2)
		{
			print(status,"<div style='text-align: center; font-family: Arial; font-size: 14px; font-weight: bold; color: #7B2525;'>"
					+ "Distance out of range!</div>");
			return;
		}
		
		if(distance==2)
		{
			cells[y0][x0].removeAll();
			resetPiece(x0,y0);
		}
		cells[y][x].add(turn==1 ? createMeeple1() : createMeeple2());
		placePiece(x, y, turn);
		
		int oponent = 3-turn;
		int counter = 4;
		for(int dr=-1; dr<=1; dr++) for(int dc=-1; dc<=1; dc++)
		{
			if(dr==0 && dc==0) continue;
			int adjX = x+dc;
			int adjY = y+dr;
			if(adjX>=0 && adjX<n && adjY>=0 && adjY<n)
			{
				if(boardState[adjY][adjX]==oponent)
				{
					cells[adjY][adjX].removeAll();
					cells[adjY][adjX].add(turn==1 ? createMeeple1() : createMeeple2());
					placePiece(adjX, adjY, turn);
					currentMove[counter] = turn;
				}
			}
			counter++;
		}
		
		if(selY != -1)
		{
			boxes[selY][selX].setBackground(Color.decode("#EEE0CC")); // reset highlighted background
		}
		currentMove[0] = x0;
		currentMove[1] = y0;
		currentMove[2] = x;
		currentMove[3] = y;	
		selX=-1;
		selY=-1;
		print(status,"");
		updatePoint();
		checkTurnAndEndGame();
		updateUI();
		currentMove[12] = turn;
		while(movep<history.size()-1)
		{
			history.remove(history.size()-1);
		}
		history.add(currentMove);
		currentMove = new int[13];
		movep++;
		
		triggerComputerTurn();
	}
	
	private void loadStateUndo(int[] currentState, int prevTurn)
	{
		int prevX = currentState[0];
		int prevY = currentState[1];
		int newX = currentState[2];
		int newY = currentState[3];
		
		cells[prevY][prevX].removeAll();
		cells[prevY][prevX].add(prevTurn==1 ? createMeeple1() : createMeeple2());
		placePiece(prevX, prevY, prevTurn);
		cells[newY][newX].removeAll();
		resetPiece(newX, newY);
		int counter = 4;
		for(int r=-1; r<=1; r++) for(int c=-1; c<=1; c++)
		{
			int dy = newY+r;
			int dx = newX+c;
			if(r==0 && c==0) continue;
			if(currentState[counter]==0) counter++;
			else 
			{
				cells[dy][dx].removeAll();
				cells[dy][dx].add(3-currentState[counter]==1 ? createMeeple1() : createMeeple2());
				placePiece(dx, dy, 3-currentState[counter]);
				counter++;
			}
			
		}
		turn = prevTurn;
		updatePoint();
		updateUI();
		selX=-1;
		selY=-1;
		board.revalidate();
		board.repaint();
	}
	
	private void loadStateRedo(int[] currentState)
	{
		int prevX = currentState[0];
		int prevY = currentState[1];
		int newX = currentState[2];
		int newY = currentState[3];
		int distance = Math.max(Math.abs(prevX-newX), Math.abs(prevY-newY));
		
		if(distance == 2)
		{
			cells[prevY][prevX].removeAll();
			resetPiece(prevX, prevY);
		}
		cells[newY][newX].add(turn==1 ? createMeeple1() : createMeeple2());
		placePiece(newX, newY, turn);
		int counter = 4;
		for(int r=-1; r<=1; r++) for(int c=-1; c<=1; c++)
		{
			int dy = newY+r;
			int dx = newX+c;
			if(r==0 && c==0) continue;
			if(currentState[counter]==0) counter++;
			else 
			{
				cells[dy][dx].removeAll();
				cells[dy][dx].add(currentState[counter]==1 ? createMeeple1() : createMeeple2());
				placePiece(dx, dy, currentState[counter]);
				counter++;
			}
			
		}
		turn = currentState[12];
		updatePoint();
		updateUI();
		selX=-1;
		selY=-1;
		board.revalidate();
		board.repaint();
	}
	
	private boolean checkTurnAndEndGame()
	{
		if(hasValidMove(3-turn))
		{
			turn = 3-turn;
			return false;
		}
		if(hasValidMove(turn))
		{
			String turnInfo = turn==1 ? "Player 2 (●)" : "Player 1 (⨯)";			
			print(status,"<div style='text-align: center; font-family: Arial; font-size: 14px; font-weight: bold; color: #7B2525;'>"
					+ turnInfo + " doesn't have valid move!</div>");
			return false;
		}
		else
		{
			endGame();
			return true;
		}
	}
	
	private boolean hasValidMove(int currentPlayer)
	{
		for(int r=0; r<n; r++) for(int c=0; c<n; c++)
		{
			if(boardState[r][c]==currentPlayer)
			{
				for(int dr=-2; dr<=2; dr++) for(int dc=-2; dc<=2; dc++)
				{
					int nr = r+dr;
					int nc = c+dc;
					if(nr>=0 && nr<n && nc>=0 && nc<n)
					{
						if(boardState[nr][nc]==0) return true;
					}
				}
			}
		}
		return false;
	}
	
	private void endGame()
	{
		System.out.println(points[1]);
		System.out.println(points[2]);
		
		if(points[1]>points[2])
		{
			print(status,"<div style='text-align: center; font-family: Arial; font-size: 14px; font-weight: bold; color: #FF0066;'>"
					+ "Player 1 (⨯) wins the game!!!</div>");
		}
		else if(points[1]<points[2])
		{
			print(status,"<div style='text-align: center; font-family: Arial; font-size: 14px; font-weight: bold; color: #FF0066;'>"
					+ "Player 2 (●) wins the game!!!</div>");
		}
		else
		{
			print(status,"<div style='text-align: center; font-family: Arial; font-size: 14px; font-weight: bold; color: #FF0066;'>"
					+ "Tie</div>");
		}
	}
	
	private void updatePoint()
	{
		points[1] = 0;
		points[2] = 0;
		for(int r=0; r<n; r++) for(int c=0; c<n; c++)
		{
			if(boardState[r][c]==1) points[1]+=1;
			else if (boardState[r][c]==2) points[2]+=1;
		}
	}
	
	private void undo()
	{
		if(movep>0)
		{
			int prevTurn;
			int prevMovep = movep-1;
			if(prevMovep==0) prevTurn=1;
			else prevTurn=history.get(prevMovep)[12];
			loadStateUndo(history.get(movep), prevTurn);
			movep--;
			print(status, "");
		}
		else
		{
			print(status,"<div style='text-align: center; font-family: Arial; font-size: 14px; font-weight: bold; color: #7B2525;'>Nothing to undo!</div>");
		}
	}
	
	private void redo()
	{
		if(movep<history.size()-1)
		{
			movep++;
			loadStateRedo(history.get(movep));
			print(status,"");
		}
		else
		{
			print(status,"<div style='text-align: center; font-family: Arial; font-size: 14px; font-weight: bold; color: #7B2525;'>Nothing to redo!</div>");

		}
	}
	
	private void save()
	{
		String text = "";
		for(int[] h : history)
		{
			for(int i=0; i<h.length; i++) {
				text = text + String.valueOf(h[i]) + ",";
			}
			text = text + " ";
		}
		text = text + String.valueOf(n) + " " + String.valueOf(movep);
		
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm");
		String name = "Side Flip " + now.format(formatter) + ".txt";
		
		try
		{
			Files.writeString(Path.of(name), text);
			print(status,"<div style='text-align: center; font-family: Arial; font-size: 14px; font-weight: bold; color: #7B2525;'>Game saved successfully!</div>");
		}
		catch (IOException e)
		{
			print(status,"<div style='text-align: center; font-family: Arial; font-size: 14px; font-weight: bold; color: #7B2525;'>Error!</div>");
		}
	}
	
	private void load()
	{
		// This tells your OS to literally open its native File Explorer window
		FileDialog fileDialog = new FileDialog(win, "Load Saved Game", FileDialog.LOAD);
		fileDialog.setFile("*.txt"); // Filter to show .txt files
		fileDialog.setVisible(true); // Opens the window and pauses code until user picks a file
		
		// Get the folder and file name the user chose
		String directory = fileDialog.getDirectory();
		String filename = fileDialog.getFile();
		
		// If the user clicked "Cancel" or closed the Explorer window, do nothing
		if (directory == null || filename == null)
		{
			return;
		}
		
		// Combine the directory and filename into a full File path
		File selectedFile = new File(directory, filename);
		
		try
		{
			String content = Files.readString(selectedFile.toPath());
			String[] stringArray = content.split(" ");
			this.n = Integer.parseInt(stringArray[stringArray.length-2]);
			inputn.setText(""+n);
			init();
			this.movep = Integer.parseInt(stringArray[stringArray.length-1]);
			
			for(int i=1; i<stringArray.length-2; i++)
			{
				// changing from String to int[]
				String[] moveArray = stringArray[i].split(",");
				int[] numbers = new int[13];
				for(int j=0; j<13; j++)
				{
					numbers[j] = Integer.parseInt(moveArray[j]);
				}
				history.add(numbers);
			}
			
			for(int i=1; i<=movep && i<history.size(); i++)
			{
				loadStateRedo(history.get(i));
			}
			
			print(status,"<div style='text-align: center; font-family: Arial; font-size: 14px; font-weight: bold; color: #7B2525;'>Game loaded successfully!</div>");			
		}
		catch (IOException e)
		{
			print(status,"<div style='text-align: center; font-family: Arial; font-size: 14px; font-weight: bold; color: #7B2525;'>Error!</div>");			
		}
	}
	
	private void drawHiglights()
	{
		for(int r=0; r<n; r++) for(int c=0; c<n; c++)
		{
			if(r==selY && c==selX)
			{
				boxes[r][c].setBackground(Color.decode("#BA6A4C"));
			}
			else
			{
				boxes[r][c].setBackground(Color.decode("#EEE0CC"));
			}
		}
	}
	
	private void updateNumPlayer()
	{
		if(input1Player.isSelected()) 
		{
			computerOn = true;
			computerTurn = 2;
		}
		if(input2Player.isSelected())
		{
			computerOn = false;
			computerTurn = -1;
		}
	}
	
	private void triggerComputerTurn()
	{
		if(!computerOn || turn!=computerTurn) return;
		
		eval0(0, 3);
		int x0 = bestMove[0];
		int y0 = bestMove[1];
		int x1 = bestMove[2];
		int y1 = bestMove[3];
		makeMove(x0, y0, x1, y1);
	}
	
	private boolean win(int turn)
	{
		if(turn==1)
		{
			if(points[1] > points[2]) return true;
			return false;
		}
		else
		{
			if(points[2] > points[1]) return true;
			return false;
		}
	}
	
	private int evalpos()
	{
		int currentP = 0;
		int opponentP = 0;
		for(int r=0; r<n; r++) for(int c=0; c<n; c++)
		{
			if(boardState[r][c]==0) continue;
			if(boardState[r][c]==turn) currentP++;
			else opponentP++;
		}
		return currentP-opponentP;
	}
	
	private void simulateMove(int x0, int y0, int x, int y) 
	{
		int distance = Math.max(Math.abs(x0-x), Math.abs(y0-y));
		
		if(distance==2) boardState[y0][x0]=0;
		boardState[y][x]=turn;
		
		int oponent = 3-turn;
		int counter = 4;
		for(int dr=-1; dr<=1; dr++) for(int dc=-1; dc<=1; dc++)
		{
			if(dr==0 && dc==0) continue;
			int adjX = x+dc;
			int adjY = y+dr;
			if(adjX>=0 && adjX<n && adjY>=0 && adjY<n)
			{
				if(boardState[adjY][adjX]==oponent)
				{
					boardState[adjY][adjX] = turn;
					currentMove[counter] = turn;
				}
			}
			counter++;
		}
		
		currentMove[0] = x0;
		currentMove[1] = y0;
		currentMove[2] = x;
		currentMove[3] = y;	
		
		
		if(hasValidMove(3-turn)) turn = 3-turn;
		currentMove[12] = turn;
		while(movep<history.size()-1)
		{
			history.remove(history.size()-1);
		}
		history.add(currentMove);
		currentMove = new int[13];
		movep++;
	}
	
	private void simulateUndo()
	{
		int[] currentState = history.get(movep);
		int prevX = currentState[0];
		int prevY = currentState[1];
		int newX = currentState[2];
		int newY = currentState[3];
		int prevTurn = history.get(movep-1)[12];
		
		boardState[prevY][prevX] = prevTurn;
		boardState[newY][newX] = 0;
		int counter = 4;
		for(int r=-1; r<=1; r++) for(int c=-1; c<=1; c++)
		{
			int dy = newY+r;
			int dx = newX+c;
			if(r==0 && c==0) continue;
			if(currentState[counter]==0) counter++;
			else 
			{
				boardState[dy][dx] = 3-currentState[counter];
				counter++;
			}
			
		}
		turn = prevTurn;
		movep--;
	}
	
	private long eval0(int depth, int maxdepth)
	{
		if(!hasValidMove(turn) && !hasValidMove(3-turn)) return (win(turn) ? INF : -INF);
		if(depth==maxdepth) return evalpos();
		long high = -INF;
		
		for(int r=0; r<n; r++) for(int c=0; c<n; c++)
		{
			if(boardState[r][c] == turn)
			{
				for(int dr=-2; dr<=2; dr++) for(int dc=-2; dc<=2; dc++)
				{
					if(dr==0 && dc==0) continue;
					int nr = r+dr;
					int nc = c+dc;
					if(nr>=0 && nr<n && nc>=0 && nc<n && boardState[nr][nc]==0)
					{
						simulateMove(c, r, nc, nr);
						long v = -eval0(depth+1, maxdepth);
						simulateUndo();
						if(v>high)
						{
							high = v;
							if(depth==0)
							{
								bestMove[0] = c;
								bestMove[1] = r;
								bestMove[2] = nc;
								bestMove[3] = nr;
							}
						}
						if(depth==0)
						{
							System.out.println(r+" "+c+" "+v);
						}
					}
				}
			}
			
		}
		return high;
	}
	
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(
				new Runnable() { public void run() {new sideflip();} } 
		);
	}
}
