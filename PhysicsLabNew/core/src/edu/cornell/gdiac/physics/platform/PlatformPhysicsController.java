/*
 * PlatformController.java
 *
 * You SHOULD NOT need to modify this file.  However, you may learn valuable lessons
 * for the rest of the lab by looking at it.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */
package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.physics.InputController;
import edu.cornell.gdiac.physics.WorldBenchmark;
import edu.cornell.gdiac.physics.WorldController;
import edu.cornell.gdiac.physics.obstacle.BoxObstacle;
import edu.cornell.gdiac.physics.obstacle.Obstacle;
import edu.cornell.gdiac.physics.obstacle.PolygonObstacle;
import edu.cornell.gdiac.physics.obstacle.WheelObstacle;

/**
 * Gameplay specific controller for the platformer game.  
 *
 * You will notice that asset loading is not done with static methods this time.  
 * Instance asset loading makes it easier to process our game modes in a loop, which 
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class PlatformPhysicsController extends WorldController implements ContactListener {
	/** Texture asset for character avatar */
	private TextureRegion avatarTexture;
	/** Texture asset for the spinning barrier */
	private TextureRegion barrierTexture;
	/** Texture asset for the bullet */
	private TextureRegion bulletTexture;
	/** Texture asset for the bridge plank */
	private TextureRegion bridgeTexture;

	/** The jump sound.  We only want to play once. */
	private Sound jumpSound;
	private long jumpId = -1;
	/** The weapon fire sound.  We only want to play once. */
	private Sound fireSound;
	private long fireId = -1;
	/** The weapon pop sound.  We only want to play once. */
	private Sound plopSound;
	private long plopId = -1;
	/** The default sound volume */
	private float volume;

	// Physics objects for the game
	/** Physics constants for initialization */
	private JsonValue constants;
	/** Reference to the character avatar */
	private DudeModel avatar;
	/** Reference to the goalDoor (for collision detection) */
	private BoxObstacle goalDoor;
	/** Reference to the character avatar */ // TODO: avatar-bullet physics is not yet implemented for benchmark world
	private DudeModel benchmark_avatar;
	/** Reference to the goalDoor (for collision detection) */
	private BoxObstacle benchmark_goalDoor;

	/** Mark set to handle more sophisticated collision callbacks */
	protected ObjectSet<Fixture> sensorFixtures;

	/**
	 * Creates and initialize a new instance of the platformer game
	 *
	 * The game has default gravity and other settings
	 */
	public PlatformPhysicsController() {
		super(DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_GRAVITY);
		setDebug(false);
		setComplete(false);
		setFailure(false);
		real.world.setContactListener(this);
		sensorFixtures = new ObjectSet<Fixture>();
	}

	/**
	 * Gather the assets for this controller.
	 *
	 * This method extracts the asset variables from the given asset directory. It
	 * should only be called after the asset directory is completed.
	 *
	 * @param directory	Reference to global asset manager.
	 */
	public void gatherAssets(AssetDirectory directory) {
		avatarTexture  = new TextureRegion(directory.getEntry("platform:dude",Texture.class));
		barrierTexture = new TextureRegion(directory.getEntry("platform:barrier",Texture.class));
		bulletTexture = new TextureRegion(directory.getEntry("platform:bullet",Texture.class));
		bridgeTexture = new TextureRegion(directory.getEntry("platform:rope",Texture.class));

		jumpSound = directory.getEntry( "platform:jump", Sound.class );
		fireSound = directory.getEntry( "platform:pew", Sound.class );
		plopSound = directory.getEntry( "platform:plop", Sound.class );

		constants = directory.getEntry( "platform:constants", JsonValue.class );
		super.gatherAssets(directory);
	}
	
	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public void reset() {
		super.reset();
		compare.world.setContactListener(this);
		real.world.setContactListener(this);
		setComplete(false);
		setFailure(false);
		populateLevel(real);
		populateLevel(compare);
	}

	/**
	 * Lays out the game geography.
	 */
	private void populateLevel(WorldBenchmark wb) {
		// Add level goal
		float dwidth  = goalTile.getRegionWidth()/scale.x;
		float dheight = goalTile.getRegionHeight()/scale.y;

		JsonValue goal = constants.get("goal");
		JsonValue goalpos = goal.get("pos");
		BoxObstacle goalDoor = new BoxObstacle(goalpos.getFloat(0),goalpos.getFloat(1),dwidth,dheight);
		goalDoor.setBodyType(BodyDef.BodyType.StaticBody);
		goalDoor.setDensity(goal.getFloat("density", 0));
		goalDoor.setFriction(goal.getFloat("friction", 0));
		goalDoor.setRestitution(goal.getFloat("restitution", 0));
		goalDoor.setSensor(true);
		goalDoor.setDrawScale(scale);
		goalDoor.setTexture(goalTile);
		goalDoor.setName("goal");
		addObject(wb, goalDoor);
		if(wb == real){
			this.goalDoor = goalDoor;
		}else{
			this.benchmark_goalDoor = goalDoor;
		}

	    String wname = "wall";
	    JsonValue walljv = constants.get("walls");
	    JsonValue defaults = constants.get("defaults");
	    for (int ii = 0; ii < walljv.size; ii++) {
	        PolygonObstacle obj;
	    	obj = new PolygonObstacle(walljv.get(ii).asFloatArray(), 0, 0);
			obj.setBodyType(BodyDef.BodyType.StaticBody);
			obj.setDensity(defaults.getFloat( "density", 0.0f ));
			obj.setFriction(defaults.getFloat( "friction", 0.0f ));
			obj.setRestitution(defaults.getFloat( "restitution", 0.0f ));
			obj.setDrawScale(scale);
			obj.setTexture(earthTile);
			obj.setName(wname+ii);
			addObject(wb, obj);
	    }
	    
	    String pname = "platform";
		JsonValue platjv = constants.get("platforms");
	    for (int ii = 0; ii < platjv.size; ii++) {
	        PolygonObstacle obj;
	    	obj = new PolygonObstacle(platjv.get(ii).asFloatArray(), 0, 0);
			obj.setBodyType(BodyDef.BodyType.StaticBody);
			obj.setDensity(defaults.getFloat( "density", 0.0f ));
			obj.setFriction(defaults.getFloat( "friction", 0.0f ));
			obj.setRestitution(defaults.getFloat( "restitution", 0.0f ));
			obj.setDrawScale(scale);
			obj.setTexture(earthTile);
			obj.setName(pname+ii);
			addObject(wb, obj);
	    }

	    // This world is heavier
		real.world.setGravity( new Vector2(0,defaults.getFloat("gravity",0)) );
		compare.world.setGravity( new Vector2(0,defaults.getFloat("gravity",0)) );

		// Create dude
		dwidth  = avatarTexture.getRegionWidth()/scale.x;
		dheight = avatarTexture.getRegionHeight()/scale.y;
		DudeModel avatar = new DudeModel(constants.get("dude"), dwidth, dheight);
		avatar.setDrawScale(scale);
		avatar.setTexture(avatarTexture);
		addObject(wb, avatar);

		if(wb == real){
			this.avatar = avatar;
		}else{
			this.benchmark_avatar = avatar;
		}

		// Create rope bridge
		dwidth  = bridgeTexture.getRegionWidth()/scale.x;
		dheight = bridgeTexture.getRegionHeight()/scale.y;
		RopeBridge bridge = new RopeBridge(constants.get("bridge"), dwidth, dheight);
		bridge.setTexture(bridgeTexture);
		bridge.setDrawScale(scale);
		addObject(wb, bridge);
		
		// Create spinning platform
		dwidth  = barrierTexture.getRegionWidth()/scale.x;
		dheight = barrierTexture.getRegionHeight()/scale.y;
		Spinner spinPlatform = new Spinner(constants.get("spinner"),dwidth,dheight);
		spinPlatform.setDrawScale(scale);
		spinPlatform.setTexture(barrierTexture);
		addObject(wb, spinPlatform);

		volume = constants.getFloat("volume", 1.0f);
	}
	
	/**
	 * Returns whether to process the update loop
	 *
	 * At the start of the update loop, we check if it is time
	 * to switch to a new game mode.  If not, the update proceeds
	 * normally.
	 *
	 * @param dt	Number of seconds since last animation frame
	 * 
	 * @return whether to process the update loop
	 */
	public boolean preUpdate(float dt) {
		if (!super.preUpdate(dt)) {
			return false;
		}
		
		if (!isFailure() && avatar.getY() < -1) {
			setFailure(true);
			return false;
		}
		
		return true;
	}

	/**
	 * The core gameplay loop of this world.
	 *
	 * This method contains the specific update code for this mini-game. It does
	 * not handle collisions, as those are managed by the parent class WorldController.
	 * This method is called after input is read, but before collisions are resolved.
	 * The very last thing that it should do is apply forces to the appropriate objects.
	 *
	 * @param dt	Number of seconds since last animation frame
	 */
	public void update(float dt) {
		updateAvatar(avatar);
		updateAvatar(benchmark_avatar);
	}

	private void updateAvatar(DudeModel avatar) {
		// Process actions in object model
		avatar.setMovement(InputController.getInstance().getHorizontal() *avatar.getForce());
		avatar.setJumping(InputController.getInstance().didPrimary());
		avatar.setShooting(InputController.getInstance().didSecondary());

		// Add a bullet if we fire
		if (avatar.isShooting()) {
			System.out.println("is shooting");
			createBullet(avatar);
		}

		avatar.applyForce();
		if (avatar.isJumping()) {
			jumpId = playSound( jumpSound, jumpId, volume );
		}
	}

	/**
	 * Processes physics in the new deterministic way
	 * @param dt	Number of seconds since last animation frame
	 */
	@Override
	public void postUpdate(float dt) {
//		float skewedDt = computeSkewedDt(dt, remainingTime, miniStep);
		detPostUpdate(real, 0.015f);
		detPostUpdate(compare, 0.015f);
	}

//	float difference = 0f;
//	private float computeSkewedDt(float dt, float remainingTime, float ministep) {
//		// find the new remaining time after the steps
//		float sum = dt + remainingTime;
//		while(sum > ministep){
//			sum -= ministep;
//		}
//		// generate another random remaining time as the intended remaining time
//		float skewedRemaining = ministep / 2;
//		// calculate the new intended difference
//		float new_intended_difference = skewedRemaining - sum;
//		// find the gap between the two difference
//		float gap_between_difference = new_intended_difference - difference;
//		// update the previous difference
//		difference = new_intended_difference;
//		// calculate the result
//		return dt + gap_between_difference;
//	}

	public void detPostUpdate(WorldBenchmark wb, float dt) {
		// Add any objects created by actions
		while (!wb.addQueue.isEmpty()) {
			addObject(wb, wb.addQueue.poll());
		}

		// Turn the physics engine crank.
		wb.world.step(dt, WorldBenchmark.WORLD_VELOC, WorldBenchmark.WORLD_POSIT);
//		ProcessPhysics(dt);

		wb.garbageCollect(dt);
	}

	/**
	 * Add a new bullet to the world and send it in the right direction.
	 */
	private void createBullet(DudeModel avatar) {
		JsonValue bulletjv = constants.get("bullet");
		float offset = bulletjv.getFloat("offset",0);
		offset *= (avatar.isFacingRight() ? 1 : -1);
		float radius = bulletTexture.getRegionWidth()/(2.0f*scale.x);
		WheelObstacle bullet = new WheelObstacle(avatar.getX()+offset, avatar.getY(), radius);

		if(avatar == this.avatar){
			bullet.setName("bullet");
		}else{
			bullet.setName("compareBullet");
		}
		bullet.setDensity(bulletjv.getFloat("density", 0));
	    bullet.setDrawScale(scale);
	    bullet.setTexture(bulletTexture);
	    bullet.setBullet(true);
	    bullet.setGravityScale(0);
		
		// Compute position and velocity
		float speed = bulletjv.getFloat( "speed", 0 );
		speed  *= (avatar.isFacingRight() ? 1 : -1);
		bullet.setVX(speed);
		if(avatar == this.avatar){
			System.out.println("create bullet in real world");
			addRealQueuedObject(bullet);
		}else if(avatar == this.benchmark_avatar){
			System.out.println("create bullet in compare world");
			addCompareQueuedObject(bullet);
		}else{
			System.out.println("error in creation of bullets");
		}
		fireId = playSound( fireSound, fireId );
	}
	
	/**
	 * Remove a new bullet from the world.
	 *
	 * @param  bullet   the bullet to remove
	 */
	public void removeBullet(Obstacle bullet) {
	    bullet.markRemoved(true);
	    plopId = playSound( plopSound, plopId );
	}

	
	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when we first get a collision between two objects.  We use 
	 * this method to test if it is the "right" kind of collision.  In particular, we
	 * use it to test if we made it to the win door.
	 *
	 * @param contact The two bodies that collided
	 */
	public void beginContact(Contact contact) {
		Fixture fix1 = contact.getFixtureA();
		Fixture fix2 = contact.getFixtureB();

		Body body1 = fix1.getBody();
		Body body2 = fix2.getBody();

		Object fd1 = fix1.getUserData();
		Object fd2 = fix2.getUserData();
		
		try {
			Obstacle bd1 = (Obstacle)body1.getUserData();
			Obstacle bd2 = (Obstacle)body2.getUserData();

			// Test bullet collision with world
			if (bd1.getName().equals("bullet") && bd2 != avatar) {
				System.out.println("collision detected case 1");
				removeBullet(bd1);
			}

			if (bd2.getName().equals("bullet") && bd1 != avatar) {
				System.out.println("collision detected case 2");
				removeBullet(bd2);
			}

			if (bd1.getName().equals("compareBullet") && bd2 != benchmark_avatar) {
				System.out.println("collision detected case 3");
				removeBullet(bd1);
			}

			if (bd2.getName().equals("compareBullet") && bd1 != benchmark_avatar) {
				System.out.println("collision detected case 4");
				removeBullet(bd2);
			}

			// See if we have landed on the ground.
			if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
				(avatar.getSensorName().equals(fd1) && avatar != bd2)) {
				avatar.setGrounded(true);
				sensorFixtures.add(avatar == bd1 ? fix2 : fix1); // Could have more than one ground
			}

			// See if we have landed on the ground.
			if ((benchmark_avatar.getSensorName().equals(fd2) && benchmark_avatar != bd1) ||
					(benchmark_avatar.getSensorName().equals(fd1) && benchmark_avatar != bd2)) {
				benchmark_avatar.setGrounded(true);
				sensorFixtures.add(benchmark_avatar == bd1 ? fix2 : fix1); // Could have more than one ground
			}
			
			// Check for win condition
			if ((bd1 == avatar   && bd2 == goalDoor) ||
				(bd1 == goalDoor && bd2 == avatar)) {
				setComplete(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when two objects cease to touch.  The main use of this method
	 * is to determine when the characer is NOT on the ground.  This is how we prevent
	 * double jumping.
	 */ 
	public void endContact(Contact contact) {
		Fixture fix1 = contact.getFixtureA();
		Fixture fix2 = contact.getFixtureB();

		Body body1 = fix1.getBody();
		Body body2 = fix2.getBody();

		Object fd1 = fix1.getUserData();
		Object fd2 = fix2.getUserData();
		
		Object bd1 = body1.getUserData();
		Object bd2 = body2.getUserData();

		if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
			(avatar.getSensorName().equals(fd1) && avatar != bd2)) {
			sensorFixtures.remove(avatar == bd1 ? fix2 : fix1);
			if (sensorFixtures.size == 0) {
				avatar.setGrounded(false);
			}
		}

		if ((benchmark_avatar.getSensorName().equals(fd2) && benchmark_avatar != bd1) ||
				(benchmark_avatar.getSensorName().equals(fd1) && benchmark_avatar != bd2)) {
			sensorFixtures.remove(benchmark_avatar == bd1 ? fix2 : fix1);
			if (sensorFixtures.size == 0) {
				benchmark_avatar.setGrounded(false);
			}
		}
	}
	
	/** Unused ContactListener method */
	public void postSolve(Contact contact, ContactImpulse impulse) {}
	/** Unused ContactListener method */
	public void preSolve(Contact contact, Manifold oldManifold) {}

	/**
	 * Called when the Screen is paused.
	 *
	 * We need this method to stop all sounds when we pause.
	 * Pausing happens when we switch game modes.
	 */
	public void pause() {
		jumpSound.stop(jumpId);
		plopSound.stop(plopId);
		fireSound.stop(fireId);
	}

	/**
	 * Draw the physics objects to the canvas
	 * For simple worlds, this method is enough by itself.  It will need
	 * to be overridden if the world needs fancy backgrounds or the like
	 * The method draws all objects in the order that they were added.
	 * @param dt	Number of seconds since last animation frame
	 */
	public void draw(float dt){
		super.draw(dt);
		displayFont.setColor(Color.GREEN);
		canvas.begin(); // DO NOT SCALE
		canvas.drawTextCentered("PHYSICS WORLD", displayFont, 230f);
		displayFont.setColor(Color.RED);
		canvas.drawTextCentered("NOT finished", displayFont, 150f);
		canvas.end();
	}
}