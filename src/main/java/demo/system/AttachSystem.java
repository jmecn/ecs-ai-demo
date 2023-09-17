package demo.system;

import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;
import demo.component.Attach;
import demo.component.Rotation;
import demo.component.Position;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/9/16
 */
public class AttachSystem extends AbstractGameSystem {

    private EntityData ed;
    private EntitySet entities;

    @Override
    protected void initialize() {
        ed = getSystem(EntityData.class);
        entities = ed.getEntities(Attach.class, Position.class, Rotation.class);

        for (Entity entity : entities) {
            update(entity);
        }
    }

    public void update(SimTime time) {
        entities.applyChanges();
        for(Entity entity : entities) {
            update(entity);
        }
    }

    private void update(Entity entity) {
        Attach attach = entity.get(Attach.class);
        EntityId target = attach.getTarget();

        Position position = entity.get(Position.class);
        Position targetPosition = ed.getComponent(target, Position.class);
        if (position != null && targetPosition != null) {
            position.getLocation().set(targetPosition.getLocation());
            ed.setComponent(entity.getId(), position);
        }

        Rotation rotation = entity.get(Rotation.class);
        Rotation targetRotation = ed.getComponent(target, Rotation.class);
        if (rotation != null && targetRotation != null) {
            rotation.getRotation().set(targetRotation.getRotation());
            ed.setComponent(entity.getId(), rotation);
        }
    }

    @Override
    protected void terminate() {
        entities.release();
        entities = null;
    }
}
