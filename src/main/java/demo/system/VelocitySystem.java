package demo.system;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntitySet;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;
import demo.component.Rotation;
import demo.component.Speed;
import demo.component.Velocity;
import lombok.extern.slf4j.Slf4j;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/9/16
 */
@Slf4j
public class VelocitySystem extends AbstractGameSystem {
    private EntityData ed;
    private EntitySet entities;

    @Override
    protected void initialize() {
        ed = getSystem(EntityData.class);
        entities = ed.getEntities(Speed.class, Rotation.class);

        // init velocity
        for (Entity entity : entities) {
            updateVelocity(entity);
        }
    }

    public void update(SimTime simTime) {
        entities.applyChanges();

        for (Entity entity : entities.getAddedEntities()) {
            updateVelocity(entity);
        }
        for (Entity entity : entities.getChangedEntities()) {
            updateVelocity(entity);
        }
        for (Entity entity : entities.getRemovedEntities()) {
            ed.removeComponent(entity.getId(), Velocity.class);
        }
    }

    private void updateVelocity(Entity entity) {
        Speed speed = entity.get(Speed.class);
        if (speed.getSpeed() == 0) {
            ed.removeComponent(entity.getId(), Velocity.class);
            return;
        }

        Rotation facing = entity.get(Rotation.class);
        Quaternion rotation = facing.getRotation();

        Velocity velocity = ed.getComponent(entity.getId(), Velocity.class);
        Vector3f dir;
        if (velocity == null) {
            dir = new Vector3f(0, 0, 1);
            rotation.multLocal(dir);
            dir.multLocal(speed.getSpeed());
            velocity = new Velocity(dir);
        } else {
            dir = velocity.getVelocity();
            dir.set(0, 0, 1);
            rotation.multLocal(dir);
            dir.multLocal(speed.getSpeed());
        }
        ed.setComponent(entity.getId(), velocity);
    }

    @Override
    protected void terminate() {
        entities.clear();
        entities = null;
    }
}
