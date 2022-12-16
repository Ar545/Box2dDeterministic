package edu.cornell.gdiac.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.physics.obstacle.Obstacle;
import edu.cornell.gdiac.physics.rocket.RocketModel;
import edu.cornell.gdiac.util.PooledList;

import java.util.Iterator;

public class WorldBenchmark {
    /** The velocity offset in VELOCITY_WORLD */
    public float remainingTime = 0f;
    boolean doubleWorld = false;
    /** All the objects in the world. */
    public PooledList<Obstacle> objects  = new PooledList<Obstacle>();
    /** Queue for adding objects */
    public PooledList<Obstacle> addQueue = new PooledList<Obstacle>();
    /** The Box2D world */
    public World world;

    /** The Box2D draw world */
    public World drawWorld;

    /** The amount of time for a physics engine step. */
    public static final float WORLD_STEP = 1/60.0f;
    /** Number of velocity iterations for the constrain solvers */
    public static final int WORLD_VELOC = 6;
    /** Number of position iterations for the constrain solvers */
    public static final int WORLD_POSIT = 2;

    public WorldBenchmark(Vector2 gravity){
        world = new World(gravity,false);
        drawWorld = new World(gravity,false);
    }

    public void dispose() {
        for(Obstacle obj : objects) {
            if(doubleWorld){
                obj.deactivatePhysics(world, drawWorld);
            }else{
                obj.deactivatePhysics(world);
            }
        }
        objects.clear();
        addQueue.clear();
        world.dispose();
        drawWorld.dispose();
        objects = null;
        addQueue = null;
        world  = null;
        drawWorld = null;
        remainingTime = 0;
    }

    public void addObject(Obstacle obj) {
        objects.add(obj);
        if(doubleWorld){
            obj.activatePhysics(world, drawWorld);
        }else{
            obj.activatePhysics(world);
        }
    }

    public void addQueuedObject(Obstacle obj) {
        addQueue.add(obj);
    }

    public void garbageCollect(float dt){
        // Garbage collect the deleted objects.
        // Note how we use the linked list nodes to delete O(1) in place.
        // This is O(n) without copying.
        Iterator<PooledList<Obstacle>.Entry> iterator = objects.entryIterator();
        while (iterator.hasNext()) {
            PooledList<Obstacle>.Entry entry = iterator.next();
            Obstacle obj = entry.getValue();
            if (obj.isRemoved()) {
                if(doubleWorld){
                    obj.deactivatePhysics(world, drawWorld);
                }else{
                    obj.deactivatePhysics(world);
                }
                entry.remove();
            } else {
                // Note that update is called last!
                obj.update(dt);
            }
        }
    }

    public void postUpdate(float dt) {
        // Add any objects created by actions
        while (!addQueue.isEmpty()) {
            addObject(addQueue.poll());
        }

        // Turn the physics engine crank.
        world.step(WORLD_STEP,WORLD_VELOC,WORLD_POSIT);

        garbageCollect(dt);
    }

    public void reset() {
        Vector2 gravity = new Vector2(world.getGravity() );

        for(Obstacle obj : objects) {
            if(doubleWorld){
                obj.deactivatePhysics(world, drawWorld);
            }else{
                obj.deactivatePhysics(world);
            }
        }

        objects.clear();
        addQueue.clear();
        world.dispose();
        drawWorld.dispose();
        world = new World(gravity,false);
        drawWorld = new World(gravity,false);
        remainingTime = 0;
    }


    public void setDoubleWorld() {
        doubleWorld = true;
    }
}
