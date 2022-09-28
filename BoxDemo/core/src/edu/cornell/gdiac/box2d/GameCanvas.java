/*
 * GameCanvas.java
 *
 * To properly follow the model-view-controller separation, we should not have
 * any specific drawing code in GameMode. All of that code goes here.  As
 * with GameEngine, this is a class that you are going to want to copy for
 * your own projects.
 *
 * An important part of this canvas design is that it is loosely coupled with
 * the model classes. All of the drawing methods are abstracted enough that
 * it does not require knowledge of the interfaces of the model classes.  This
 * important, as the model classes are likely to change often.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.box2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;

/**
 * Primary view class for the game, abstracting the basic graphics calls.
 * 
 * This version of GameCanvas only supports both rectangular and polygonal Sprite
 * drawing.  It also supports a debug mode that draws polygonal outlines.  However,
 * that mode must be done in a separate begin/end pass.
 */
public class GameCanvas {
	private static final int BLANK_SIZE = 1;
	
	/** Drawing context to handle textures as sprites */
	private PolygonSpriteBatch spriteBatch;
	
	/** Track whether or not we are active (for error checking) */
	private boolean active;
	
	/** Difference between drawing and physics */
	private Vector2 scale;
	/** Value to cache window width (if we are currently full screen) */
	int width;
	/** Value to cache window height (if we are currently full screen) */
	int height;
	
	// CACHE OBJECTS
	/** Default (empty) texture for polygons */
	private Texture blank;
	


	/**
	 * Creates a new GameCanvas determined by the application configuration.
	 * 
	 * Width, height, and fullscreen are taken from the LWGJApplicationConfig
	 * object used to start the application.  This constructor initializes all
	 * of the necessary graphics objects.
	 */
	public GameCanvas() {
		active = false;
		spriteBatch = new PolygonSpriteBatch();
		
		// Set the projection matrix (for proper scaling)
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, getWidth(), getHeight());
		
		scale = new Vector2(1,1);
		
		// Create a blank texture
		Pixmap map = new Pixmap(BLANK_SIZE,BLANK_SIZE,Pixmap.Format.RGBA4444);
		map.setColor(Color.WHITE);
		map.fillRectangle(0, 0, BLANK_SIZE, BLANK_SIZE);
		blank = new Texture(map);
	}
		
    /**
     * Eliminate any resources that should be garbage collected manually.
     */
    public void dispose() {
		if (active) {
			Gdx.app.error("GameCanvas", "Cannot dispose while drawing active", new IllegalStateException());
			return;
		}
		spriteBatch.dispose();
    	spriteBatch = null;
    }

	/**
	 * Returns the width of this canvas
	 *
	 * This currently gets its value from Gdx.graphics.getWidth()
	 *
	 * @return the width of this canvas
	 */
	public int getWidth() {
		return Gdx.graphics.getWidth();
	}
	
	/**
	 * Changes the width of this canvas
	 *
	 * This method raises an IllegalStateException if called while drawing is
	 * active (e.g. in-between a begin-end pair).
	 *
	 * @param width the canvas width
	 */
	public void setWidth(int width) {
		if (active) {
			Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
			return;
		}
		this.width = width;
		if (!isFullscreen()) {
			Gdx.graphics.setWindowedMode(width, getHeight());
		}
		resize();
	}
	
	/**
	 * Returns the height of this canvas
	 *
	 * This currently gets its value from Gdx.graphics.getHeight()
	 *
	 * @return the height of this canvas
	 */
	public int getHeight() {
		return Gdx.graphics.getHeight();
	}
	
	/**
	 * Changes the height of this canvas
	 *
	 * This method raises an IllegalStateException if called while drawing is
	 * active (e.g. in-between a begin-end pair).
	 *
	 * @param height the canvas height
	 */
	public void setHeight(int height) {
		if (active) {
			Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
			return;
		}
		this.height = height;
		if (!isFullscreen()) {
			Gdx.graphics.setWindowedMode(getWidth(), height);	
		}
		resize();
	}
	
	/**
	 * Returns the dimensions of this canvas
	 *
	 * @return the dimensions of this canvas
	 */
	public Vector2 getSize() {
		return new Vector2(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
	}
	
	/**
	 * Changes the width and height of this canvas
	 *
	 * This method raises an IllegalStateException if called while drawing is
	 * active (e.g. in-between a begin-end pair).
	 *
	 * @param width the canvas width
	 * @param height the canvas height
	 */
	public void setSize(int width, int height) {
		if (active) {
			Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
			return;
		}
		this.width = width;
		this.height = height;
		if (!isFullscreen()) {
			Gdx.graphics.setWindowedMode(width, height);
		}
		resize();

	}
	
	/**
	 * Returns whether this canvas is currently fullscreen.
	 *
	 * @return whether this canvas is currently fullscreen.
	 */	 
	public boolean isFullscreen() {
		return Gdx.graphics.isFullscreen(); 
	}
	
	/**
	 * Sets whether or not this canvas should change to fullscreen.
	 *
	 * If desktop is true, it will use the current desktop resolution for
	 * fullscreen, and not the width and height set in the configuration
	 * object at the start of the application. This parameter has no effect
	 * if fullscreen is false.
	 *
	 * This method raises an IllegalStateException if called while drawing is
	 * active (e.g. in-between a begin-end pair).
	 *
	 * @param fullscreen Whether this canvas should change to fullscreen.
	 * @param desktop 	 Whether to use the current desktop resolution
	 */	 
	public void setFullscreen(boolean value, boolean desktop) {
		if (active) {
			Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
			return;
		}
		if (value) {
			Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
		} else {
			Gdx.graphics.setWindowedMode(width, height);
		}
	}
	
	/**
	 * Resets the SpriteBatch camera when this canvas is resized.
	 *
	 * If you do not call this when the window is resized, you will get
	 * weird scaling issues.
	 */
	 public void resize() {
		// Resizing screws up the spriteBatch projection matrix
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, getWidth(), getHeight());
	}
	
	/**
	 * Returns the scaling factor for this canvas
	 *
	 * All units (position and size) are multiplied by this scaling factor.
	 * This allows us to specify all our shapes in physics coordinates (which
	 * are not the same as drawing coordinates).
	 *
	 * This method returns a reference to the drawing scale.  Hence changes
	 * to this object will affect the scale.  Because of this fact, this method 
	 * raises an IllegalStateException if called while drawing is active 
	 * (e.g. in-between a begin-end pair).
	 *
	 * @return the scaling factor for this canvas
	 */
	 public Vector2 getScale() {
		 if (active) {
			 Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
			 return null;
		 }
		 return scale;
	 }

	 /**
	  * Returns the x-axis scaling factor for this canvas.
	  *
	  * All units (position and size) are multiplied by this scaling factor.
	  * This allows us to specify all our shapes in physics coordinates (which
	  * are not the same as drawing coordinates).
	  *
	  * @return the x-axis scaling factor for this canvas
	  */
	 public float getSX() {
		 return scale.x;
	 }

	 /**
	  * Returns the x-axis scaling factor for this canvas.
	  *
	  * All units (position and size) are multiplied by this scaling factor.
	  * This allows us to specify all our shapes in physics coordinates (which
	  * are not the same as drawing coordinates).
	  *
	  * This method raises an IllegalStateException if called while drawing is
	  * active (e.g. in-between a begin-end pair).
	  *
	  * @param value the x-axis scaling factor for this canvas
	  */
	 public void setSX(float value) {
		 if (active) {
			 Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
			 return;
		 }
		 scale.x = value;
	 }

	 /**
	  * Returns the y-axis scaling factor for this canvas.
	  *
	  * All units (position and size) are multiplied by this scaling factor.
	  * This allows us to specify all our shapes in physics coordinates (which
	  * are not the same as drawing coordinates).
	  *
	  * @return the y-axis scaling factor for this canvas
	  */
	 public float getSY() {
		 return scale.y;
	 }

	 /**
	  * Returns the y-axis scaling factor for this canvas.
	  *
	  * All units (position and size) are multiplied by this scaling factor.
	  * This allows us to specify all our shapes in physics coordinates (which
	  * are not the same as drawing coordinates).
	  *
	  * This method raises an IllegalStateException if called while drawing is
	  * active (e.g. in-between a begin-end pair).
	  *
	  * @param value the y-axis scaling factor for this canvas
	  */
	 public void setSY(float value) {
		 if (active) {
			 Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
			 return;
		 }
		 scale.y = value;
	 }

	 /**
	  * Start and active drawing sequence with the identity transform.
	  *
	  * Nothing is flushed to the graphics card until the method end() is called.
	  */
	 public void begin() {
		 spriteBatch.begin();
		 active = true;

		 // Clear the screen
		 Gdx.gl.glClearColor(0.39f, 0.58f, 0.93f, 1.0f);  // Homage to the XNA years
		 Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	 }

	 /**
	  * Ends a drawing sequence, flushing textures to the graphics card.
	  */
	 public void end() {
		 spriteBatch.end();
		 active = false;
	 }


	 /**
	  * Draws the tinted polygon with the given transformations
	  *
	  * The texture of the polygon will be ignored.  This method will always use
	  * a blank texture.
	  *
	  * The resulting polygon will be scaled (both position and size) by the global
	  * scaling factor.
	  *
	  * @param poly  The polygon to draw
	  * @param tint  The color tint
	  * @param x 	The x-coordinate of the screen location
	  * @param y 	The y-coordinate of the screen location
	  * @param angle The rotation angle (in radians) about the origin.
	  */	
	 public void draw(PolygonRegion poly, Color tint, float x, float y, float angle) {
		 if (!active) {
			 Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			 return;
		 }

		 // Convert angle to degrees
		 float rotate = angle*180.0f/(float)Math.PI;

		 // Put in a blank texture
		 // TO DO: In future years I will write my own PolygonBatch to fix these issues.
		 TextureRegion region = poly.getRegion();
		 Texture orig = region.getTexture();
		 int rx = 0; int ry = 0;
		 int rw = 0; int rh = 0;
		 if (orig != null) {
			 rx = region.getRegionX(); ry = region.getRegionY();
			 rw = region.getRegionWidth(); rh = region.getRegionHeight();
		 }
		 region.setTexture(blank);
		 region.setRegion(0, 0, BLANK_SIZE, BLANK_SIZE);
		 spriteBatch.setColor(tint);
		 spriteBatch.draw(poly, x*scale.x, y*scale.y, 0.0f, 0.0f, BLANK_SIZE, BLANK_SIZE, scale.x, scale.y, rotate);
		 region.setTexture(orig);
		 if (orig != null) {
			 region.setRegion(rx,ry,rw,rh);
		 }
	 }

	 /**
	  * Draws text on the screen.
	  *
	  * While the text size will not be scaled by the scaling factor, the position will 
	  * be.  This allows us to align text with physics bodies.
	  *
	  * @param text The string to draw
	  * @param font The font to use
	  * @param x The x-coordinate of the lower-left corner
	  * @param y The y-coordinate of the lower-left corner
	  */
	 public void drawText(String text, BitmapFont font, float x, float y) {
		 if (!active) {
			 Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			 return;
		 }

		 GlyphLayout layout = new GlyphLayout(font,text);
		 float oy = (y*scale.y + layout.height);
		 font.setColor(Color.WHITE);
		 font.draw(spriteBatch, layout, x*scale.x, oy);
	 }
}