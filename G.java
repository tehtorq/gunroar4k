import java.awt.AWTEvent;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class G extends JFrame
{    		
	public static boolean keys[] = new boolean[256];	
	public static double p[] = new double[10000]; 
	
	// p[0] is gametime
	// p[1] is mousex
	// p[2] is mousey	
	// p[3] is mousefire
	// p[4] is horizontal res
	// p[5] is vertical res
			
	// first 10 slots reserved for whatever
	// next 9*5 entries are boats, after that anything else
	// type, originx, originy, velocityx, velocityy, health, lastfire, hit, lastwave			
	//int FREE = 0, BOAT, AVATAR_BULLET, ENEMY_BULLET, EXPLOSION_PARTICLE, WAVE
	
  protected void processEvent(AWTEvent e) 
	{				
		 super.processEvent(e);
		 int eid = e.getID();
		 
		 if (eid == MouseEvent.MOUSE_RELEASED) 
			 p[3] = 0; 
		 if (eid == MouseEvent.MOUSE_PRESSED)
			 p[3] = 1; 			 
		 
		 if (eid == MouseEvent.MOUSE_MOVED || eid == MouseEvent.MOUSE_DRAGGED) 
		 {
       p[1] = ((MouseEvent)e).getX();
       p[2] = ((MouseEvent)e).getY();			 
		 }
		 
		 if (eid == KeyEvent.KEY_PRESSED || eid == KeyEvent.KEY_RELEASED) 
			 keys[((KeyEvent)e).getKeyCode()] =eid == KeyEvent.KEY_PRESSED;
	}	
	
	public static void main(String[] args)
	{
		System.setProperty("sun.java2d.opengl", "true");    
		new G();		
	}

	public G()
	{				
		p[4] = 1024;
		p[5] = 768;		
		double scroll1 = 0, scroll2 = 30;		
	  //int fpsCount = 0, fps = 0;
	  //double fpsTimer = 0;			
		double rot = 0;
		
		// create background images - saves 40 bytes duplicating code
		
		BufferedImage background1 = null;
		BufferedImage background2 = null;
		
		if (background1 == null)		
		{			
			BufferedImage img = new BufferedImage((int)p[4], (int)p[5], BufferedImage.TYPE_INT_RGB);  
			img.createGraphics();  
			Graphics2D graphics = (Graphics2D)img.getGraphics();
			graphics.setComposite((AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f)));  

			for (int i = 0; i < p[4]; i += 40)
			{
				for (int j = 0; j < p[5]; j += 40)
				{					
					graphics.setPaint(new Color(20 + (int) (Math.random() * 20), 20 + (int) (Math.random() * 20), 50 + (int) (Math.random() * 150)));				
					graphics.fillRect(i- (int) (Math.random() * 20), j- (int) (Math.random() * 20) , 40+ (int) (Math.random() * 20) , 40+ (int) (Math.random() * 20) );
				}			
			}
			
			background1 = img;
		}				
		
		if (background2 == null)	
		{
			BufferedImage img = new BufferedImage((int)p[4], (int)p[5], BufferedImage.TYPE_INT_RGB);  
			img.createGraphics();  
			Graphics2D graphics = (Graphics2D)img.getGraphics();
			graphics.setComposite((AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f)));  

			for (int i = 0; i < p[4]; i += 40)
			{
				for (int j = 0; j < p[5]; j += 40)
				{										
					graphics.setPaint(new Color(20 + (int) (Math.random() * 20), 20 + (int) (Math.random() * 20), 50 + (int) (Math.random() * 150)));				
					graphics.fillRect(i- (int) (Math.random() * 20), j- (int) (Math.random() * 20) , 40+ (int) (Math.random() * 20) , 40+ (int) (Math.random() * 20) );
				}			
			}

			background2 = img;			
		}		
			
		// set up misc window stuff		
		
		double frameTime = 0;
		// window settings
		
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);		
		enableEvents(AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);

    setTitle("Gunroar4k");		
		setResizable(false);    
    setIgnoreRepaint(true);        				
               
		//gameWindow.setCursor("Resources/Cursors/cursor1.png");
    		
		setPreferredSize(new Dimension((int)p[4], (int)p[5]));
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		createBufferStrategy(2);    						
						    		
		boolean isGameRunning = true;			
		boolean fireleft = false;		

		// shapes
		
		Polygon boat = new Polygon();
		boat.addPoint(30, 0);
		boat.addPoint(5, 12);
		boat.addPoint(-5, 12);
		boat.addPoint(-30, 7);
		boat.addPoint(-30, -7);
		boat.addPoint(-5, -12);
		boat.addPoint(5, -12);	
		
		Polygon shot = new Polygon();
		shot.addPoint(0, 0);
		shot.addPoint(15, 0);
		shot.addPoint(15, 3);
		shot.addPoint(0, 3);			
		
		Polygon shield = new Polygon();
		shield.addPoint(21, -7);
		shield.addPoint(21, 7);
		shield.addPoint(10, 21);
		shield.addPoint(-10, 21);
		shield.addPoint(-21, 7);
		shield.addPoint(-21, -7);
		shield.addPoint(-10, -21);
		shield.addPoint(10, -21);

		Polygon triangle = new Polygon();
		triangle.addPoint(10, 0);
		triangle.addPoint(-10, 10);
		triangle.addPoint(-10, -10);

		Polygon turret = new Polygon();
		turret.addPoint(15, 0);
		turret.addPoint(-5, 8);
		turret.addPoint(-5, -8);

		Polygon wave1 = new Polygon();
		wave1.addPoint(50, 0);
		wave1.addPoint(-10, 0);
		wave1.addPoint(-10, 13);

		Polygon wave2 = new Polygon();
		wave2.addPoint(50, 0);
		wave2.addPoint(-10, 0);
		wave2.addPoint(-10, -13);		
		
		boolean playing = false;
		int lives = 3;
		long startTime = System.nanoTime();		
		BufferStrategy bufferStrategy = getBufferStrategy();

    do 
    {			
			Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
			g.clearRect(0, 0, (int)p[4], (int)p[5]);
			
			frameTime = (System.nanoTime() - startTime) * 0.000000001d - p[0];			
			p[0] = (System.nanoTime() - startTime) * 0.000000001d;
			
			int scrollRate = 40;

			if ((p[12] < p[5]*0.4) && playing)
				scrollRate = 200;
			 
			scroll1 = (scroll1 + (scrollRate-10)*frameTime) % p[5];
			scroll2 = (scroll2 + scrollRate*frameTime) % p[5];

			g.translate(0, -p[5]+scroll1);
			g.drawImage(background1, 0, 0, (int)p[4], (int)p[5], null);
			g.translate(0, p[5]-scroll1);

			g.translate(0, scroll1);
			g.drawImage(background1, 0, 0, (int)p[4], (int)p[5], null);
			g.translate(0, -scroll1);		

			g.setComposite((AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)));  		

			g.translate(0, -p[5]+scroll2);				
			g.drawImage(background2, 0, 0, (int)p[4], (int)p[5], null);	
			g.translate(0, p[5]-scroll2);

			g.translate(0, scroll2);		
			g.drawImage(background2, 0, 0, (int)p[4], (int)p[5], null);		
			g.translate(0, -scroll2);						 
			
			if (!playing)
			{
				if (keys[KeyEvent.VK_SPACE])
				{									
					// initialize and get ready to play!
					
					//startTime = p[0];
					lives = 3;
					playing = true;
					
					
					for (int i = 10; i < 55; i += 9)
					{
						p[i] = 1; 
						p[i+1] = (int)(Math.random()*724) + 150; 
						p[i+2] = -500 - (Math.random()*300);
						p[i+3] = 0;
						p[i+4] = (int)(Math.random()*100) + 45; 
						p[i+5] = 100;			
					}

					p[11] = p[4]/2; 
					p[12] = p[5] - 50;
					p[13] = 0;
					p[14] = -50;
					p[0] = 0;					
					
					for (int i = 10; i < 10000; i+=9)
					{
						p[i] = 0;
					}
				}
				else if (lives > 0)
				{
					g.setPaint(Color.white);
					g.setFont(new Font("Arial",Font.BOLD,16)); 
					g.drawString("Click to play!", (int)p[4]/2-50, (int)p[5]/2);
				}
				else
				{
					g.setPaint(Color.white);
					g.setFont(new Font("Arial",Font.BOLD,16));
					g.drawString("Game over, loser!", (int)p[4]/2-50, (int)p[5]/2);
				}
			}
			
			if (playing)
			{					
				for (int i = 10; i < 10000; i+=9)
				{
					p[i+2] += scrollRate*frameTime;
				}
			
				// update direction of players aim

				double shootdirx = p[1] - p[11];
				double shootdiry = p[2] - p[12];
				double sangle = Math.atan(shootdiry / shootdirx);

				if (shootdirx < 0)
					sangle = sangle - 3.14;
		
				// avatar think								
				
				// friction

				double factor = 1 - 5*frameTime;

				if (factor < 0)
					factor = 0;

				p[13] *= factor;
				p[14] *= factor;		
				
				if (keys[KeyEvent.VK_W])			
					p[14] -= 4000 * frameTime;	
				if (keys[KeyEvent.VK_S])
					p[14] += 4000 * frameTime;				
				if (keys[KeyEvent.VK_A])		
					p[13] -= 4000 * frameTime;				
				if (keys[KeyEvent.VK_D])	
					p[13] += 4000 * frameTime;	

				double len = p[13]*p[13] + p[14]*p[14];	  

				if (len > 100000)
				{					
					p[13] *= (100000/len);
					p[14] *= (100000/len);	    
				}				

				if ((p[3] > 0) && (p[0] >= p[16]))
				{
					p[16] = p[0] + 0.01d;					
					fireleft = !fireleft;						
				
					double dirx = Math.cos(sangle - 90) * 10;
					double diry = Math.sin(sangle - 90) * 10;			
										
					if (!fireleft)						
					{
						dirx *= -1;
						diry *= -1;			
					}

					double spotx = p[11] + dirx;
					double spoty = p[12] + diry;		

					dirx = Math.cos(sangle) * 8;
					diry = Math.sin(sangle) * 8;	

					spotx += dirx;
					spoty += diry;			

					double length = Math.sqrt(dirx*dirx + diry*diry);

					if (length != 0)
					{
						dirx = dirx /length * 2000;
						diry = diry /length * 2000;
					}
					
					for (int index = 55; index < 10000; index += 9)
					{
						if (p[index] == 0)
						{						
							p[index] = 2;
							p[index+1] = spotx;
							p[index+2] = spoty;
							p[index+3] = dirx;
							p[index+4] = diry;
							p[index+5] = 15;
							break;
						}
					}
				}							

				// enemies

				for (int i = 10; i < 55; i += 9)
				{
					// collision detection										
					
					for (int index = 55; index < 10000; index+=9)
					{						
						if (((p[index] == 2) && (i != 10)) || ((p[index] == 3) && (i == 10)))
						{							
							if ((p[index+1] - p[i+1]) * (p[index+1] - p[i+1]) + (p[index+2] - p[i+2]) * (p[index+2] - p[i+2]) < 900)
							{
								p[i+5] -= 10;
								p[index] = 0;
								p[i+7] = 10;
							}							
						}						
					}
					
					// movement
					
					if (p[0] > p[i+8])
					{
						p[i+8] = p[0] + 0.05;
						
						for (int index = 55; index < 10000; index += 9)
						{
							if (p[index] == 0)
							{
								p[index] = 5;
								p[index+1] = p[i+1];
								p[index+2] = p[i+2];
								p[index+3] = p[i+3];
								p[index+4] = p[i+4];
								p[index+5] = p[0] + 1;
								break;
							}
						}														
					}
					
					if (p[0] > p[i+6])
					{
						p[i+6] = p[0] + 1;
						double velx = (p[11] - p[i+1]);
						double vely = (p[12] - p[i+2]);
																						
						velx *= (90000/ (velx*velx + vely*vely));	  		
						vely *= (90000/ (velx*velx + vely*vely));	  					
						
						for (int index = 55; index < 10000; index += 9)
						{
							if (p[index] == 0)
							{
								p[index] = 3;
								p[index+1] = p[i+1];
								p[index+2] = p[i+2];
								p[index+3] = velx;
								p[index+4] = vely;
								p[index+5] = 15;
								break;
							}
						}						
					}
									
					p[i+1] += p[i+3] * frameTime;
					p[i+2] += p[i+4] * frameTime;			
					
					if ((p[i+2] > 1100) || (p[i+5] < 1))
					{																								
						// ship dead
						
						if (p[i+5] < 1)
						{
							// spawn ghost ship
							
							for (int index = 55; index < 10000; index += 9)
							{
								if (p[index] == 0)
								{
									p[index] = 6;
									p[index+1] = p[i+1];
									p[index+2] = p[i+2];
									p[index+3] = p[i+3];
									p[index+4] = p[i+4];
									p[index+5] = 1;
									break;
								}
							}									
							
							// explosions
							
							for (int j = 0; j < (7 + Math.random() * 25); j++)
							for (int index = 55; index < 10000; index += 9)
							{
								if (p[index] == 0)
								{
									p[index] = 4;
									p[index+1] = p[i+1] + 10 - Math.random() * 20;
									p[index+2] = p[i+2] + 10 - Math.random() * 20;
									p[index+3] = 500 - Math.random() * 1000;
									p[index+4] = 500 - Math.random() * 1000;
									p[index+5] = 0.5 + Math.random() * 0.5;
									break;
								}
							}																
						}
						
						// respawn
						
						p[i+5] = 100;						
					  p[i+1] = (int)(Math.random()*724) + 150; 
						p[i+2] = -50 - (Math.random()*300);
						p[i+3] = (int)45 - (Math.random()*90);
						p[i+4] =(int)(Math.random()*100) + 45; 
						
						// respawn player
						
						if (i == 10)
						{							
							if (--lives > 0)
							{															
								p[i+1] = p[4]/2;
								p[i+2] = p[5] - 50;			
							}			
							else
							{
								playing = false;
							}
						}						
					}		
				}
				
				// clip player
																	
				if (p[11] < 120)
					p[11] = 120;
				if (p[11] > 900)
					p[11] = 900;
				if (p[12] < 50)
					p[12] = 50;
				if (p[12] > p[5])
					p[12] = p[5];					

				// bullets
				
				for (int index = 55; index < 10000; index+=9)
				{
					if ((p[index] == 2) || (p[index] == 3))
					{						
						p[index+1] += p[index+3] * frameTime;
						p[index+2] += p[index+4] * frameTime;				
						
						if (p[index+1] < 0)
							p[index] = 0;
						if (p[index+1] > p[4])
							p[index] = 0;
						if (p[index+2] < 0)
							p[index] = 0;
						if (p[index+2] > p[5])
							p[index] = 0;						
					}
				}						   						     

				// draw entities....    	        

				g.setPaint(Color.red);
				g.drawLine((int)p[11], (int)p[12], (int)p[1], (int)p[2]);				

				AffineTransform transform = g.getTransform();			

				double angle = 0;

				// waves

				g.setComposite((AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f)));  
				g.setPaint(new Color(59, 53, 171));

				for (int i = 55; i < 10000; i+=9)
				{
					if (p[i] == 5)
					{				
						double t = p[i+5] - p[0];

						if (t < 0)
							p[i] = 0;
						else
						{
							angle = Math.atan(-p[i+4] / -p[i+3]);

							if (p[i+3] < 0)
								angle = angle - 3.14;

							g.translate(p[i+1], p[i+2]);
							g.rotate(angle);				
							g.translate(0, (1 - t) * 10);
							g.scale((t), (t));		
							g.fill(wave1);			
							g.translate(0, (1 - t) * -20);
							g.fill(wave2);
							g.setTransform(transform);
						}
					}		
				}				

				// boats				
				
				
				rot += 5 * frameTime;

				for (int i = 10; i < 55; i+=9)
				{							
					g.translate(p[i+1], p[i+2]);  
					
					if (i == 10)
					{
						g.setPaint(Color.cyan);
						g.rotate(rot);							
						g.fill(shield);
						g.setPaint(Color.white);
						g.draw(shield);						
						g.rotate(-rot);							
					}
					
					if (p[i+7] > 0) 
					{
						g.setPaint(Color.red);
						p[i+7]--;
					}
					else
						g.setPaint(Color.white);
					
					angle = Math.atan(p[i+4] / p[i+3]);

					if (p[i+3] < 0)
						angle -= 3.14;

					g.rotate(angle);		
					g.setComposite((AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f))); 					
					g.fillPolygon(boat);
					g.setPaint(Color.white);
					g.setComposite((AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f)));    					
					g.drawPolygon(boat);			
					g.scale(0.7, 0.7);							
					g.drawPolygon(boat);
					g.scale(1.3, 1.3);
					g.rotate(-angle);		

					// turret of boat

					double deltax = p[11] - p[i+1];
					double deltay = p[12] - p[i+2];

					if (i == 10)
					{
						deltax = p[1] - p[11];
						deltay = p[2] - p[12];								
					}

					angle = Math.atan((deltay) / (deltax));

					if ((deltax) < 0)
						angle = angle - 3.14;

					g.rotate(angle);							
					g.fill(turret);
					g.setTransform(transform);
				}

				// bullets

				for (int i = 55; i < 10000; i+=9)
				{
					angle = Math.atan(p[i+4] / p[i+3]);

					if (p[i+3] < 0)
						angle = angle - 3.14;

					if (p[i] == 2 )
					{			
						g.setPaint(Color.green);		
						g.translate(p[i+1], p[i+2]); 
						g.rotate(angle);										
						g.setComposite((AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)));     
						g.fill(shot);	
						g.setComposite((AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f)));     
						g.draw(shot);	
						g.setTransform(transform);	
					}
					else if (p[i] == 3)
					{				
						g.setPaint(Color.MAGENTA);		
						g.translate(p[i+1], p[i+2]); 
						g.rotate(angle);
						g.setComposite((AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)));     
						g.fill(triangle);	
						g.setComposite((AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f)));     
						g.draw(triangle);			
						g.setTransform(transform);	
					}
				}

				// particles

				for (int i = 55; i < 10000; i+=9)
				{


					if ((p[i] == 4) || (p[i] == 7)) // explosion particle
					{				
						double size = 5 + (1 - p[i+5]) * 15;

						if (p[i] == 7)												
							size = 2 + (1 - p[i+5]) * 10;				

						g.setPaint(new Color((int)(240 - (240*(1 - p[i+5]))), (int)(130 * p[i+5]), (int)(32 * p[i+5])));							
						g.setComposite((AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)p[i+5]*0.75f))); 				
						g.fillRect((int)(p[i+1]-size), (int)(p[i+2]-size), (int)size*2, (int)size*2);		
						p[i+5] -= 0.001;
						p[i+1] += p[i+3] * (p[i+5]) * frameTime;
						p[i+2] += p[i+4] * (p[i+5]) * frameTime;				

						if (p[i+5] < 0)
							p[i] = 0;						
					}		
					else if (p[i] == 6) // dead ship
					{
						g.setPaint(Color.white);				
						g.setComposite((AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)p[i+5]*0.7f))); 								
						p[i+5] -= 0.005;
						p[i+1] += p[i+3] * (p[i+5]) * frameTime;
						p[i+2] += p[i+4] * (p[i+5]) * frameTime;				

						for (int index = 55; index < 10000; index += 9)
						{
							if (p[index] == 0)
							{
								p[index] = 7;
								p[index+1] = p[i+1] + 10 - Math.random() * 20;
								p[index+2] = p[i+2] + 10 - Math.random() * 20;
								p[index+3] = 50 - Math.random() * 100;
								p[index+4] = 20 - Math.random() * 200;
								p[index+5] = 0.25 + Math.random() * 0.25;						
								break;
							}
						}			

						if (p[i+5] < 0)
							p[i] = 0;						


						g.translate(p[i+1], p[i+2]);  

						angle = Math.atan(p[i+4] / p[i+3]);

						if (p[i+3] < 0)
							angle = angle - 3.14;

						g.rotate(angle);										
						g.drawPolygon(boat);		
						g.scale(0.7, 0.7);						
						g.drawPolygon(boat);									
						g.setTransform(transform);	
					}
				}
			}
			
			// draw side bars
			
			g.setComposite((AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f))); 		
			g.setPaint(Color.black);
			g.fillRect(0, 0, 100, (int)p[5]+100);
			g.fillRect((int)p[4]-100, 0, (int)p[4], (int)p[5]+100);
			
			// draw fps
/*
			fpsCount++;				

			if (p[0] > fpsTimer)
			{
				fpsTimer = p[0] + 1.0f;			
				fps = fpsCount;
				fpsCount = 0;
			}*/

			g.setPaint(Color.white);
			//g.drawString("FPS:" + fps, 20, 80);
			g.drawString("Health:" + p[15] + "%", 20, 120);
		
			g.dispose();
			bufferStrategy.show();																			 
      Thread.yield();
			
    } while (isGameRunning);
		
    // clean up

    dispose();					
  }	
}
