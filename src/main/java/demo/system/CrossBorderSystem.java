package demo.system;

import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntitySet;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;
import demo.Constants;
import demo.component.Position;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/9/16
 */
public class CrossBorderSystem extends AbstractGameSystem {

    private final float radius;

    private EntityData ed;
    private EntitySet entities;

    public CrossBorderSystem() {
        this(Constants.MAP_SIZE);
    }

    public CrossBorderSystem(float size) {
        this.radius = size * 0.5f;
    }

    @Override
    protected void initialize() {
        ed = getSystem(EntityData.class);
        entities = ed.getEntities(Position.class);
    }

    public void update(SimTime time) {
        entities.applyChanges();
        for (Entity entity : entities) {
            update(entity);
        }
    }

    private void update(Entity entity) {
        // TODO 如果超出边界，需要做什么处理？
        Position position = entity.get(Position.class);
        if (position.getLocation().x > radius) {
            position.getLocation().x = -radius;
        } else if (position.getLocation().x < -radius) {
            position.getLocation().x = radius;
        }

        if (position.getLocation().z > radius) {
            position.getLocation().z = -radius;
        } else if (position.getLocation().z < -radius) {
            position.getLocation().z = radius;
        }
    }

    @Override
    protected void terminate() {
        entities.clear();
    }
}