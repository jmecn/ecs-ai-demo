package demo.system.ai;

import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntitySet;
import com.simsilica.es.common.Decay;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;
import demo.Constants;
import demo.component.Follow;
import demo.component.Speed;
import demo.component.Transform;
import demo.system.aoi.Aoi;
import demo.system.aoi.AoiSystem;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/9/20
 */
@Slf4j
public class CollisionSystem extends AbstractGameSystem {

    private EntityData ed;
    private EntitySet entities;
    private AoiSystem aoiSystem;

    @Override
    protected void initialize() {
        ed = getSystem(EntityData.class);
        entities = ed.getEntities(Transform.class, Aoi.class);
        aoiSystem = getSystem(AoiSystem.class, true);
    }

    @Override
    protected void terminate() {
        entities.release();
        entities = null;
    }

    public void update(SimTime time) {
        entities.applyChanges();

        Collection<Entity> bullets = entities.stream()
                .filter(e -> Constants.BULLET.equals(e.get(Aoi.class).getGroup()))
                .collect(Collectors.toSet());

        Collection<Entity> orange = entities.stream()
                .filter(e -> Constants.ORANGE_TEAM.equals(e.get(Aoi.class).getGroup()))
                .collect(Collectors.toSet());

        for (Entity e : bullets) {
            bulletAi(e);
        }

        for (Entity e : orange) {
            orangeAi(e);
        }
    }

    private void bulletAi(Entity bullet) {
        Transform transform = bullet.get(Transform.class);
        Aoi aoi = bullet.get(Aoi.class);

        float distance = Float.MAX_VALUE;
        Transform targetTransform = null;
        Entity targetEntity = null;

        // 计算当前位置所在的区域
        Set<Entity> nearByEntities = aoiSystem.getNearByGroupEntities(aoi, Constants.BLUE_TEAM);

        for (Entity entity : nearByEntities) {
            Transform entityTransform = entity.get(Transform.class);

            float d = entityTransform.getPosition().distanceSquared(transform.getPosition());
            if (d > aoi.getRadiusSquared()) {
                continue;
            }
            if (d < distance) {
                distance = d;
                targetTransform = entityTransform;
                targetEntity = entity;
            }
        }

        if (targetTransform != null) {
            crashTarget(bullet);
            crashTarget(targetEntity);
        }
    }

    private void orangeAi(Entity e) {
        Transform transform = e.get(Transform.class);
        Aoi aoi = e.get(Aoi.class);

        float distance = Float.MAX_VALUE;
        Transform targetTransform = null;
        Entity targetEntity = null;

        // 计算当前位置所在的区域
        Set<Entity> nearByEntities = aoiSystem.getNearByGroupEntities(aoi, Constants.BLUE_TEAM);

        // 碰撞检测
        for (Entity entity : nearByEntities) {
            Transform entityTransform = entity.get(Transform.class);

            float d = entityTransform.getPosition().distanceSquared(transform.getPosition());
            if (d > aoi.getRadiusSquared()) {
                continue;
            }
            if (d < distance) {
                distance = d;
                targetTransform = entityTransform;
                targetEntity = entity;
            }
        }

        if (targetTransform == null) {
            lostTarget(e);
        } else {
            if (distance <= Constants.BLUE_AOI_RADIUS_SQUARED) {
                crashTarget(e);
            } else {
                followTarget(e, targetEntity);
            }
        }
    }

    private void lostTarget(Entity mob) {
        Follow follow = ed.getComponent(mob.getId(), Follow.class);
        if (follow != null) {
            ed.removeComponent(mob.getId(), Speed.class);
            ed.removeComponent(mob.getId(), Follow.class);
        }
    }

    private void crashTarget(Entity entity) {
        // mob被撞死
        if (ed.getComponent(entity.getId(), Decay.class) == null) {
            ed.setComponent(entity.getId(), new Decay());
            if (log.isDebugEnabled()) {
                log.debug("crash entity:{}", entity.getId());
            }
        }
    }

    private void followTarget(Entity mob, Entity targetPlayer) {

        ed.setComponents(mob.getId(), new Speed(Constants.ORANGE_TEAM_SPEED));

        // 是否已经有target
        Follow follow = ed.getComponent(mob.getId(), Follow.class);
        if (follow != null && Objects.equals(targetPlayer.getId(), follow.getTarget())) {
            return;
        }
        ed.setComponents(mob.getId(), new Follow(targetPlayer.getId()));
    }
}
