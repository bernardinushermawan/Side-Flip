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
	private int p1pts=0, p2pts=0;
	private int[][] boardState;
	private int[] currentMove;
	private int selX=-1, selY=-1;
	private int turn = 1;
	private final ArrayList<int[]> history = new ArrayList<int[]>();
	private int movep = 0;
	
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
		p1pts = 0;
		p2pts = 0;
		turn = 1;
		selX=-1;
		selY=-1;
		history.clear();
		movep=0;
				
		// Make Board //
		cells = new JPanel[n][n];
		boxes = new JPanel[n][n];
		boardState = new int[n][n];
		currentMove = new int[12];
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
		history.add(currentMove);
	}
	
	private void updateUI()
	{
		String turnInfo = turn==1 ? "Player 1 (⨯)" : "Player 2 (●)";
		String html = "<html><div style='font-family: Arial; font-size: 14px; font-weight: bold; width: 200px; color: #F0F0DB'>" +
	                "<table width='100%'>" +
	                "<tr>" + 
	                "<td width='45%' align='center'><b>P1 (⨯):</b> " + p1pts + "</td>" +
	                "<td width='10%' align='center'><b> | </b></td>" +
	                "<td width='45%' align='center'><b>P2 (●):</b> " + p2pts + "</td>" +
	                "</tr>" +
	                "<tr>" +
	                "<td colspan='3' align='center'><font color='#ACBAC4'>Turn: " + turnInfo + "</font></td>" +
	                "</tr>" +
	                "</table></div></html>";
	    
	    print(pointStatus, html);
	    board.revalidate();
	    board.repaint();
	}
	
	private void handleClick(int x, int y)
	{
		print(status, " ");
		// Piece is selected //
		if(selX!=-1)
		{
			currentMove[0] = selX;
			currentMove[1] = selY;
			if(boardState[y][x]==0)
			{
				int distance = Math.max(Math.abs(selX-x), Math.abs(selY-y));
				if(distance == 0)
				{
					selX=-1;
					selY=-1;
					return;
				}
				else if(distance == 1)
				{
					sideFlip(x, y);
					return;
				}
				else if(distance == 2)
				{
					cells[selY][selX].removeAll();
					boardState[selY][selX]=0;
					sideFlip(x, y);
					return;
				}

				else
				{
					print(status,"<div style='text-align: center; font-family: Arial; font-size: 14px; font-weight: bold; color: #7B2525;'>"
							+ "Distance out of range!</div>");
					return;
				}
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
	
	private void saveCurrentState()
	{
		while(movep<history.size()-1)
		{
			history.remove(history.size()-1);
		}
		history.add(currentMove);
		currentMove = new int[12];
		movep++;
	}
	
	private void loadStateUndo(int[] currentState)
	{
		int prevTurn = 3-turn;
		int prevX = currentState[0];
		int prevY = currentState[1];
		int newX = currentState[2];
		int newY = currentState[3];
		boardState[prevY][prevX] = prevTurn;
		cells[prevY][prevX].removeAll();
		cells[prevY][prevX].add(prevTurn==1 ? createMeeple1() : createMeeple2());
		boardState[newY][newX] = 0;
		cells[newY][newX].removeAll();
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
				cells[dy][dx].removeAll();
				cells[dy][dx].add(3-currentState[counter]==1 ? createMeeple1() : createMeeple2());
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
			boardState[prevY][prevX] = 0;
			cells[prevY][prevX].removeAll();
		}
		boardState[newY][newX] = turn;
		cells[newY][newX].add(turn==1 ? createMeeple1() : createMeeple2());
		int counter = 4;
		for(int r=-1; r<=1; r++) for(int c=-1; c<=1; c++)
		{
			int dy = newY+r;
			int dx = newX+c;
			if(r==0 && c==0) continue;
			if(currentState[counter]==0) counter++;
			else 
			{
				boardState[dy][dx] = currentState[counter];
				cells[dy][dx].removeAll();
				cells[dy][dx].add(currentState[counter]==1 ? createMeeple1() : createMeeple2());
				counter++;
			}
			
		}
		turn = 3-turn;
		updatePoint();
		updateUI();
		selX=-1;
		selY=-1;
		board.revalidate();
		board.repaint();
	}
	
	private void sideFlip(int x, int y)
	{
		cells[y][x].add(turn==1 ? createMeeple1() : createMeeple2());
		boardState[y][x] = turn;
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
					boardState[adjY][adjX] = turn;
					currentMove[counter] = turn;
				}
			}
			counter++;
		}
		
		boxes[selY][selX].setBackground(Color.decode("#EEE0CC")); // reset highlighted background
		currentMove[2] = x;
		currentMove[3] = y;	
		selX=-1;
		selY=-1;
		print(status,"");
		updatePoint();
		checkTurnAndEndGame();
		updateUI();
		saveCurrentState();
	}
	
	private void checkTurnAndEndGame()
	{
		if(p1pts==0 || p2pts==0)
		{
			endGame();
			return;
		}
		if(hasValidMove(3-turn))
		{
			turn = 3-turn;
			return;
		}
		if(hasValidMove(turn))
		{
			String turnInfo = turn==1 ? "Player 2 (●)" : "Player 1 (⨯)";			
			print(status,"<div style='text-align: center; font-family: Arial; font-size: 14px; font-weight: bold; color: #7B2525;'>"
					+ turnInfo + " doesn't have valid move!</div>");
			endGame();
			return;
		}
		else
		{
			endGame();
			return;
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
		if(p1pts>p2pts)
		{
			print(status,"<div style='text-align: center; font-family: Arial; font-size: 14px; font-weight: bold; color: #FF0066;'>"
					+ "Player 1 (⨯) wins the game!!!</div>");
		}
		else if(p1pts<p2pts)
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
		p1pts = 0;
		p2pts = 0;
		for(int r=0; r<n; r++) for(int c=0; c<n; c++)
		{
			if(boardState[r][c]==1) p1pts+=1;
			else if (boardState[r][c]==2) p2pts+=1;
		}
	}
	
	private void undo()
	{
		if(movep>0)
		{
			loadStateUndo(history.get(movep));
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
				int[] numbers = new int[12];
				for(int j=0; j<12; j++)
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
			computerTurn = 3-turn;
		}
		if(input2Player.isSelected())
		{
			computerOn = false;
			computerTurn = -1;
		}
	}
	
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(
				new Runnable() { public void run() {new sideflip();} } 
		);
	}
}