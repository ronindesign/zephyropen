package zephyropen.device.beam;

import javax.imageio.ImageIO;
import javax.swing.*;

import zephyropen.api.ZephyrOpen;
import zephyropen.util.Utils;
import zephyropen.util.google.GoogleChart;
import zephyropen.util.google.GoogleLineGraph;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

public class BeamGUI implements MouseMotionListener {

	/** framework configuration */
	public static ZephyrOpen constants = ZephyrOpen.getReference();
	public static final Color yellow = new Color(245, 237, 48);
	public static final Color orange = new Color(252, 176, 64);
	public static final Color blue = new Color(245, 237, 48);
	public static final Color red = new Color(241, 83, 40);
	public static final int WIDTH = 800;
	public static final int HEIGHT = 350;
	
//	public static final int BEAM_HEIGHT = 400+48;
//	public static final int CURVE_HEIGHT = 200;
	
	public static final String yellowX1 = "yellowX1";
	public static final String yellowX2 = "yellowX2";
	public static final String yellowY1 = "yellowY1";
	public static final String yellowY2 = "yellowY2";
	
	public static final String orangeX1 = "orangeX1";
	public static final String orangeX2 = "orangeX2";
	public static final String orangeY1 = "orangeY1";
	public static final String orangeY2 = "orangeY2";
	
	public static final String redX1 = "redX1";
	public static final String redX2 = "redX2";
	public static final String redY1 = "redY1";
	public static final String redY2 = "redY2";

	final String title = "Beam Scan v0.1 ";
	JFrame frame = new JFrame(title); 
	JLabel curve = new JLabel();
	BeamScan scan = new BeamScan();
	BeamComponent beamCompent = new BeamComponent();
	
	// TODO: get from the config file 
	private static String path = null;

	String topLeft1 = "NOT Connected";
	String topLeft2 = "try, device -> connect";
	String topLeft3 = "test";
	
	String topRight1 = "test";
	String topRight2 = "test";
	String topRight3 = "test";

	String bottomRight1 = "test";
	String bottomRight2 = "test";
	String bottomRight3 = "test";
	
	JMenuItem connectItem = new JMenuItem("connect");
	JMenuItem closeItem = new JMenuItem("close");
	JMenuItem scanItem = new JMenuItem("single scan");
	JMenuItem screenshotItem = new JMenuItem("screen capture");
	JMenu userMenue = new JMenu("Scan");
	JMenu deviceMenue = new JMenu("Device");

	int dataPoints = 0;
	double scale = 0.0;
	double xCenterpx = 0.0;
	double yCenterpx = 0.0;
	
	double redX1px = 0.0;	
	double redX2px = 0.0;
	double redY1px = 0.0;	
	double redY2px = 0.0;
	
	double yellowX1px = 0.0;	
	double yellowX2px = 0.0;
	double yellowY1px = 0.0;	
	double yellowY2px = 0.0;
	
	double orangeX1px = 0.0;	
	double orangeX2px = 0.0;
	double orangeY1px = 0.0;	
	double orangeY2px = 0.0;
	
	/** */
	public static void main(String[] args) {
		constants.init(args[0]);
		new BeamGUI();
	}

	/** Methods to create Image corresponding to the frame. */
	public static void screenCapture(final Component component) {
		new Thread() {
			public void run() {
				String fileName = path + ZephyrOpen.fs + "beam_" + System.currentTimeMillis() + ".png";
				Point p = new Point(0, 0);
				SwingUtilities.convertPointToScreen(p, component);
				Rectangle region = component.getBounds();
				region.x = p.x;
				region.y = p.y;
				BufferedImage image = null;
				try {
					image = new Robot().createScreenCapture(region);
				} catch (AWTException e) {
					e.printStackTrace();
				}
				try {
					if (image != null)
						ImageIO.write(image, "png", new File(fileName));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {}

	@Override
	public void mouseMoved(MouseEvent e) {
		frame.setTitle(title + "    (" + e.getX() + ", " + e.getY()+")");
	}
	
	/** */
	private void connect(){
		scan.connect();
		if(scan.isConnected()){
			topLeft1 = "CONNECTED";
			topLeft2 = "Spin V: " + scan.getSpinVersion() + "   " + scan.getSpinPort();
			topLeft3 = "Read V: " + scan.getReadVersion() + "   " + scan.getReadPort();
			topRight1 = null; 
			topRight2 = null; 
			topRight3 = null;
			
		} else {
			
			topLeft1 = "FAULT";
			topLeft2 = "connect failed";
			topLeft3 = null;
			topRight1 = null;
			topRight2 = null;
			topRight3 = null;
			
		}
		beamCompent.repaint();	
	}
	
	/** */
	private ActionListener listener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			Object source = event.getSource();
			if (source.equals(scanItem)) {
				new Thread() {
					public void run() {
						singleScan();
					}
				}.start();
			} else if (source.equals(connectItem)) {
				
				if(!scan.isConnected()) connect();
				
			} else if (source.equals(closeItem)) {
				scan.close();
				constants.shutdown();
			} else if (source.equals(screenshotItem)) {
				new Thread() {
					public void run() {
						Utils.delay(300);
						screenCapture(frame);
					}
				}.start();
			}
		}
	};

	/** */
	public BeamGUI() {

		path = constants.get(ZephyrOpen.userHome) + ZephyrOpen.fs + "screenshots"
				+ ZephyrOpen.fs + constants.get(ZephyrOpen.deviceName);

		// create log dir if not there
		if ((new File(path)).mkdirs())
			constants.info("created: " + path);
		

      

		/** Resister listener */
		screenshotItem.addActionListener(listener);
		scanItem.addActionListener(listener);
		closeItem.addActionListener(listener);
		connectItem.addActionListener(listener);

		/** Add to menu */
		deviceMenue.add(connectItem);
		deviceMenue.add(closeItem);
		
		userMenue.add(scanItem);
		userMenue.add(screenshotItem);

		/** Create the menu bar */
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(userMenue);
		menuBar.add(deviceMenue);

		/** Create frame */
		frame.setJMenuBar(menuBar);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new GridLayout(2, 1)); 

		frame.add(beamCompent);
		frame.add(curve);
		
		// room for the menu 
		frame.setSize(WIDTH+6, (HEIGHT*2)+48);
		frame.setResizable(false);
		frame.setAlwaysOnTop(true);
		frame.setVisible(true);
  
		//Register for mouse events on blankArea and panel.
		beamCompent.addMouseMotionListener(this);
		curve.addMouseMotionListener(this);

		/** register shutdown hook */
		Runtime.getRuntime().addShutdownHook(
			new Thread() {
				public void run() {
					scan.close();
				}
			}
		);
		
		connect();
		singleScan();
	}

	/** */
	public void singleScan() {
	
		if(scan.isConnected()){
			scan.test();
			scan.log();
			dataPoints = scan.getPoints().size();
			scale = (double)WIDTH/dataPoints; 
			constants.info("scale factor: " + scale);	
			xCenterpx = (((double)WIDTH) * 0.25);
			yCenterpx = (((double)WIDTH) * 0.75);
		} else {
			topLeft1 = "FAULT";
			topLeft2 = "not connected";
			topLeft3 = "try connecting first";
			topRight1 = null;
			topRight2 = null;
			topRight3 = null;
			beamCompent.repaint();
			return;
		}
		
		int[] slice = scan.getSlice(10);
		if (slice != null) {
			
			constants.put("yellowSlice", 100);
			constants.put(yellowX1, slice[0]);
			constants.put(yellowX2, slice[1]);
			constants.put(yellowY1, slice[2]);
			constants.put(yellowY2, slice[3]);
		
			yellowX1px = (WIDTH/2) - (xCenterpx - ((double)slice[0] * scale));
			yellowX2px = (WIDTH/2) - (xCenterpx - ((double)slice[1] * scale));
			yellowY1px = (HEIGHT/2) - (yCenterpx - ((double)slice[2] * scale));
			yellowY2px = (HEIGHT/2) - (yCenterpx - ((double)slice[3] * scale));
			
			topRight1 = "yellow (" + Utils.formatFloat(yellowX1px, 0) + ", " + Utils.formatFloat(yellowX2px,0) 
				+ ")(" + Utils.formatFloat(yellowY1px,0) + ", " + Utils.formatFloat(yellowY2px,0) + ")";
		}
		
		slice = scan.getSlice(300);
		if (slice != null) {
			
			constants.put("orangeSlice", 300);
			constants.put(orangeX1, slice[0]);
			constants.put(orangeX2, slice[1]);
			constants.put(orangeY1, slice[2]);
			constants.put(orangeY2, slice[3]);

			orangeX1px = (WIDTH/2) - (xCenterpx - ((double)slice[0] * scale));
			orangeX2px = (WIDTH/2) - (xCenterpx - ((double)slice[1] * scale));
			orangeY1px = (HEIGHT/2) - (yCenterpx - ((double)slice[2] * scale));
			orangeY2px = (HEIGHT/2) - (yCenterpx - ((double)slice[3] * scale));
			
			topRight2 = "orange (" + Utils.formatFloat(orangeX1px, 0) + ", " + Utils.formatFloat(orangeX2px,0) 
			+ ")(" + Utils.formatFloat(orangeY1px,0) + ", " + Utils.formatFloat(orangeY2px,0) + ")";
		}	
		
		slice = scan.getSlice(800);
		if (slice != null) {
			
			constants.put("redSlice", 800);
			constants.put("redX1", slice[0]);
			constants.put("redX2", slice[1]);
			constants.put("redY1", slice[2]);
			constants.put("redY2", slice[3]);
			
			redX1px = (WIDTH/2) - (xCenterpx - ((double)slice[0] * scale));
			redX2px = (WIDTH/2) - (xCenterpx - ((double)slice[1] * scale));
			redY1px = (HEIGHT/2) - (yCenterpx - ((double)slice[2] * scale));
			redY2px = (HEIGHT/2) - (yCenterpx - ((double)slice[3] * scale));
			
			topRight3 = "red (" + Utils.formatFloat(redX1px, 0) + ", " + Utils.formatFloat(redX2px,0) 
			+ ")(" + Utils.formatFloat(redY1px,0) + ", " + Utils.formatFloat(redY2px,0) + ")";
		}	
		
		
		beamCompent.repaint();
		lineGraph();
		Utils.delay(300);
		screenCapture(frame);
	}
	
	/** create graph */
	public void lineGraph() {
		GoogleChart chart = new GoogleLineGraph("beam", "ma", com.googlecode.charts4j.Color.BLUEVIOLET);
		Vector<Integer>points = scan.getPoints();
		for (int j = 0; j < points.size(); j++)
			chart.add(String.valueOf(points.get(j)));

		try {
			String str = chart.getURLString( WIDTH, HEIGHT, "data: " + dataPoints ); 
			if(str!=null){
				Icon icon = new ImageIcon(new URL(str));
				if(icon != null) curve.setIcon(icon);
			} 
		} catch (final Exception e) {	
			constants.error(e.getMessage(), this);
		} 
	}
	
	public class BeamComponent extends JComponent {
		private static final long serialVersionUID = 1L;
		public void paint(Graphics g) {

			final int w = getWidth();
			final int h = getHeight();
			
			g.setColor(Color.YELLOW);
			g.fillOval((int)yellowX1px, (int)yellowY1px, (int)yellowX2px-(int)yellowX1px, (int)yellowY2px-(int)yellowY1px);
			g.drawLine((int)yellowX1px, 0,(int)yellowX1px, h);
			g.drawLine((int)yellowX2px, 0,(int)yellowX2px, h);
			g.drawLine(0, (int)yellowY1px, w,(int)yellowY1px);
			g.drawLine(0, (int)yellowY2px, w,(int)yellowY2px);
			
			g.setColor(Color.ORANGE);
			g.fillOval((int)orangeX1px, (int)orangeY1px, (int)orangeX2px-(int)orangeX1px, (int)orangeY2px-(int)orangeY1px);
			g.drawLine((int)orangeX1px, 0,(int)orangeX1px, h);
			g.drawLine((int)orangeX2px, 0,(int)orangeX2px, h);
			g.drawLine(0, (int)orangeY1px, w,(int)orangeY1px);
			g.drawLine(0, (int)orangeY2px, w,(int)orangeY2px);
			
			
			g.setColor(Color.RED);
			g.fillOval((int)redX1px, (int)redY1px, (int)redX2px-(int)redX1px, (int)redY2px-(int)redY1px);
			//g.drawLine((int)redX1px, 0,(int)redX1px, h);
			//g.drawLine((int)redX2px, 0,(int)redX2px, h);
			//g.drawLine(0, (int)redY1px, w,(int)redY1px);
			//g.drawLine(0, (int)redY2px, w,(int)redY2px);
			
			g.setColor(Color.BLACK);
			g.drawLine(0, h-1, w, h-1);
			Graphics2D g2d = (Graphics2D) g;
			Stroke stroke2 = new BasicStroke(
		    		 1.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL, 1.0f, new float[]
				 { 6.0f, 2.0f, 1.0f, 2.0f },0.0f);
			g2d.setStroke(stroke2);
			g.drawLine(0, h/2, w, h/2);
			g.drawLine(w/2, 0, w/2, h);
			
			bottomRight3 = " h: " + h + " w: " + w;
			
			if (topRight1 != null) g.drawString(topRight1, (w/2 + 5), 15);
			if (topRight2 != null) g.drawString(topRight2, (w/2 + 5), 30);
			if (topRight3 != null) g.drawString(topRight3, (w/2 + 5), 45);
			
			if (bottomRight1 != null) g.drawString(bottomRight1, (w/2 + 5), h - 10);
			if (bottomRight2 != null) g.drawString(bottomRight2, (w/2 + 5), h - 25);
			if (bottomRight3 != null) g.drawString(bottomRight3, (w/2 + 5), h - 40);
			
			if (topLeft1 != null) g.drawString(topLeft1, 15, 15);
			if (topLeft2 != null) g.drawString(topLeft2, 15, 30);
			if (topLeft3 != null) g.drawString(topLeft3, 15, 45);

		}
	}
}