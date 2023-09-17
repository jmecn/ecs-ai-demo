package demo.system;

import com.jme3.math.Vector3f;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntitySet;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;
import demo.component.Position;
import demo.component.Velocity;
import lombok.extern.slf4j.Slf4j;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/9/15
 */
@Slf4j
public class MovementSystem extends AbstractGameSystem {
    private EntityData ed;
    private EntitySet entities;
    private final Vector3f step = new Vector3f(0f, 0f, 0f);

    @Override
    protected void initialize() {
        ed = getSystem(EntityData.class, true);
        entities = ed.getEntities(Velocity.class, Position.class);
    }

    @Override
    public void update(SimTime time) {
        entities.applyChanges();
        for (Entity entity : entities) {
            updatePosition(entity, time);
        }
    }

    private void updatePosition(Entity entity, SimTime time) {
        Velocity velocity = entity.get(Velocity.class);
        Position position = entity.get(Position.class);

        velocity.getVelocity().mult((float) time.getTpf(), step);
        position.getLocation().addLocal(step);
        ed.setComponent(entity.getId(), position);
    }

    @Override
    protected void terminate() {
        entities.clear();
    }
}
