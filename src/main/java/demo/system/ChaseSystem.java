package demo.system;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.simsilica.es.*;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;
import demo.component.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/9/16
 */
public class ChaseSystem extends AbstractGameSystem {

    private EntityData ed;
    private EntitySet entities;

    @Override
    protected void initialize() {
        ed = getSystem(EntityData.class);
        entities = ed.getEntities(Position.class, Follow.class);
    }

    public void update(SimTime time) {
        entities.applyChanges();

        for(Entity e : entities) {
            Follow follow = e.get(Follow.class);
            EntityId target = follow.getTarget();

            Position position = e.get(Position.class);
            Position targetPosition = ed.getComponent(target, Position.class);

            Vector3f loc1 = position.getLocation();
            Vector3f loc2 = targetPosition.getLocation();

            // 计算Facing
            Vector3f dir = loc2.subtract(loc1);
            dir.normalizeLocal();
            ed.setComponents(e.getId(), new Rotation(new Quaternion().lookAt(dir, Vector3f.UNIT_Y)));

            if (follow.getLine() != null) {
                float dist = FastMath.sqrt(loc2.distanceSquared(loc1));
                ed.setComponent(follow.getLine(), new Scale(dist, dist, dist));
            }
        }
    }

    @Override
    protected void terminate() {
        entities.release();
        entities = null;
    }
}
