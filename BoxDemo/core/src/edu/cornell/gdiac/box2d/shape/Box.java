/*
 * Box.java
 *
 * Box-shaped model to support collisions.
 *
 * Given the name Box-2D, this is your primary collision shape.  In this 
 * example we draw it with primitives, but you could easily adapt this
 * class to use it with Sprites.
 *
 * Author: Walker M. White
 * LibGDX version, 3/12/2015
 */
package edu.cornell.gdiac.box2d.shape;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.box2d.*;  // For GameCanvas and Entity

/**
 * A rectangular physics object.
 */
public class Box extends Entity {
	/** Shape information for this box */
	protected PolygonShape shape;
	
	/**
	 * Create a new box, but do not initialize it yet.
	 */
	public Box() { } 

	/**
     * Create the collision shape information
     *
     * @param size The object bounding box
     */
	protected void makeFixture(Vector2 size) {
		shape = new PolygonShape();
		// The polygon tool uses half-height
		shape.setAsBox(size.x / 2, size.y / 2);
		
		// Create the fixture
		FixtureDef def = new FixtureDef();
		def.density = density;
		def.friction = friction;
		def.restitution = restitution;
		def.shape = shape;

		fixture = body.createFixture(def);
		shape.dispose(); // Do not need it anymore
	}

	/**
     * Create the drawing shape information
     *
     * @param size The object bounding box
     */
	protected void makeGraphics(Vector2 size) {
		// Make the polygon centered at origin
		float[] vertices = new float[8];
		vertices[0] = -size.x/2.0f;
		vertices[1] = -size.y/2.0f;
		vertices[2] = -size.x/2.0f;
		vertices[3] =  size.y/2.0f;
		vertices[4] =  size.x/2.0f;
		vertices[5] =  size.y/2.0f;
		vertices[6] =  size.x/2.0f;
		vertices[7] = -size.y/2.0f;

		// Need the indices to make 3 triangles.
		short[] indices = new short[6];
		indices[0] = 0;
		indices[1] = 1;
		indices[2] = 3;
		indices[3] = 3;
		indices[4] = 1;        
		indices[5] = 2;
		
		poly = new PolygonRegion(new TextureRegion(), vertices, indices);
	}
}