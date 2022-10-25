/*
 * Circle.java
 *
 * Circular-shaped model to support collisions.
 *
 * A circle is different from an ellipse in that it has the same dimensions along
 * all axes.  This a big difference, as it means that we do not need to model
 * them with polygons, so we can be much more exact on the shape.
 *
 * Author: Walker M. White
 * LibGDX version, 3/12/2015
 */
package edu.cornell.gdiac.box2d.shape;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.g2d.*;
//import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.box2d.*;  // For GameCanvas and Entity
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.common.Vec2;

/**
 * An circular physics object.
 */
public class Circle extends Entity {
	/** Number of polygon edges to use */
	private static final int NUM_EDGES = 20; // NOT LIMITED TO MAXIMUM OF 8

	/** Shape information for this ellipse */
	protected CircleShape shape;

	/**
	 * Create a new ellipse, but do not initialize it yet.
	 */
	public Circle() {} 

	/**
	 * Returns an array of vertices representing a circle
	 *
	 * This array is used for drawing, but NOT physics
	 *
	 * @return an array of vertices representing a circle
	 */
	private float[] makeCircle(Vec2 size) {
		// Make the ellipse centered at origin
		float stepSize = 2*(float)Math.PI / NUM_EDGES;
		
		float[] vertices = new float[2*NUM_EDGES];
		float radius = Math.min(size.x, size.y) / 2.0f;
		for (int ii = 0; ii < NUM_EDGES; ii++) {
			double angle =  stepSize * ii;
			vertices[2*ii  ] = (float)Math.cos(angle) * radius;
			vertices[2*ii+1] = (float)Math.sin(angle) * radius;
		}
		
		return vertices;
	}
	
	/**
     * Create the collision shape information
     *
     * @param size The object bounding box
     */
	protected void makeFixture(Vec2 size) {
		shape = new CircleShape();
		shape.setRadius(Math.min(size.x, size.y)/2.0f);

		// Create the fixture
		FixtureDef def = new FixtureDef();
		def.density = density;
		def.friction = friction;
		def.restitution = restitution;
		def.shape = shape;

		fixture = body.createFixture(def);
//		shape.dispose(); // Do not need it anymore
	}

	/**
     * Create the drawing shape information
     *
     * @param size The object bounding box
     */
	protected void makeGraphics(Vec2 size) {
		float[] vertices = makeCircle(size);

		// Triangle fans were removed in XNA 4.0.  Indices are a way around this.
		short[] indices = new short[3 * (NUM_EDGES-2)];
		for (int ii = 0; ii < NUM_EDGES - 2; ii++) {
			indices[3 * ii] = 0;
			indices[3 * ii + 1] = (short)(ii + 1);
			indices[3 * ii + 2] = (short)(ii + 2);
		}

		poly = new PolygonRegion(new TextureRegion(), vertices, indices);
    }
}