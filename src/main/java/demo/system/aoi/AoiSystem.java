package demo.system.aoi;

import com.simsilica.es.*;
import com.simsilica.es.common.Decay;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;
import demo.Constants;
import demo.component.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/9/15
 */
@Slf4j
public class AoiSystem extends AbstractGameSystem {

    private final int cellSize;

    private final Map<Long, Area> areas = new HashMap<>();

    private EntityData ed;
    private EntitySet entities;

    public AoiSystem() {
        this(Constants.CELL_SIZE);
    }

    public AoiSystem(int cellSize) {
        this.cellSize = cellSize;
    }

    @Override
    protected void initialize() {
        ed = getSystem(EntityData.class);
        entities = ed.getEntities(Position.class, Aoi.class);

        for (Entity entity : entities) {
            updateAoi(entity);
        }
    }

    public void update(SimTime time) {

        entities.applyChanges();
        for (Entity entity : entities.getAddedEntities()) {
            updateAoi(entity);
        }
        for (Entity entity : entities.getChangedEntities()) {
            updateAoi(entity);
        }
        for (Entity entity : entities.getRemovedEntities()) {
            leave(entity);
        }

        updateMobPlayerCollision();
        updateBulletPlayerCollision();
    }

    private void updateAoi(Entity entity) {
        Position position = entity.get(Position.class);
        Aoi aoi = entity.get(Aoi.class);

        // 计算当前位置所在的区域
        int x = (int) (position.getLocation().x / cellSize);
        int y = (int) (position.getLocation().y / cellSize);
        int z = (int) (position.getLocation().z / cellSize);
        long key = Area.toLong(x, y, z);

        Long curAreaId = aoi.getAreaId();
        if (curAreaId != null && curAreaId == key) {
            // 没有变化
            return;
        }

        // 离开旧区域
        if (curAreaId != null) {
            Area area = areas.get(curAreaId);
            if (area != null) {
                area.leave(entity, aoi.getGroup());
            }
        }

        // 进入新区域
        Area area = areas.computeIfAbsent(key, k -> new Area(key, x, y, z));
        area.enter(entity, aoi.getGroup());

        // 更新 Aoi 组件
        aoi.setArea(key, x, y, z);
        ed.setComponent(entity.getId(), aoi);
    }

    private void leave(Entity entity) {
        Aoi aoi = entity.get(Aoi.class);
        if (aoi == null) {
            return;
        }

        log.info("leave:{}", entity.getId());
        Long curAreaId = aoi.getAreaId();
        if (curAreaId != null) {
            Area area = areas.get(curAreaId);
            if (area != null) {
                area.leave(entity, aoi.getGroup());
            }
        }

        // 更新 Aoi 组件
        aoi.setArea(null, 0, 0, 0);
    }

    private void updateBulletPlayerCollision() {
        Collection<Entity> bullets = entities.stream()
                .filter(e -> Constants.BULLET.equals(e.get(Aoi.class).getGroup()))
                .collect(Collectors.toSet());

        for (Entity bullet : bullets) {
            Position bulletPosition = bullet.get(Position.class);
            Aoi aoi = bullet.get(Aoi.class);

            float distance = Float.MAX_VALUE;
            Position targetPosition = null;
            Entity targetPlayer = null;

            // 计算当前位置所在的区域
            Set<Entity> players = getNearByGroupEntities(aoi, Constants.BLUE_TEAM);

            for (Entity player : players) {
                Position playerPosition = player.get(Position.class);

                float d = playerPosition.getLocation().distanceSquared(bulletPosition.getLocation());
                if (d > aoi.getRadiusSquared()) {
                    continue;
                }
                if (d < distance) {
                    distance = d;
                    targetPosition = playerPosition;
                    targetPlayer = player;
                }
            }

            if (targetPosition != null) {
                crashTarget(bullet);
                crashTarget(targetPlayer);
            }
        }
    }

    private void updateMobPlayerCollision() {
        Collection<Entity> mobs = entities.stream()
                .filter(e -> Constants.ORANGE_TEAM.equals(e.get(Aoi.class).getGroup()))
                .collect(Collectors.toSet());

        for (Entity mob : mobs) {
            Position mobPosition = mob.get(Position.class);
            Aoi aoi = mob.get(Aoi.class);

            float distance = Float.MAX_VALUE;
            Position targetPosition = null;
            Entity targetPlayer = null;

            // 计算当前位置所在的区域
            Set<Entity> players = getNearByGroupEntities(aoi, Constants.BLUE_TEAM);

            // 碰撞检测
            for (Entity player : players) {
                Position playerPosition = player.get(Position.class);

                float d = playerPosition.getLocation().distanceSquared(mobPosition.getLocation());
                if (d > aoi.getRadiusSquared()) {
                    continue;
                }
                if (d < distance) {
                    distance = d;
                    targetPosition = playerPosition;
                    targetPlayer = player;
                }
            }

            if (targetPosition == null) {
                lostTarget(mob);
            } else {
                if (distance <= Constants.BLUE_AOI_RADIUS_SQUARED) {
                    crashTarget(mob);
                } else {
                    followTarget(mob, targetPlayer);
                }
            }
        }
    }

    private Set<Entity> getNearByGroupEntities(Aoi aoi, String group) {
        Set<Entity> set = new HashSet<>();
        // 计算当前位置所在的区域
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int x = aoi.getX() + i;
                int y = aoi.getY();
                int z = aoi.getZ() + j;
                long key = Area.toLong(x, y, z);
                Area area = areas.get(key);
                if (area == null) {
                    continue;
                }
                Set<Entity> members = area.getEntities(group);
                if (members == null || members.isEmpty()) {
                    continue;
                }
                set.addAll(members);
            }
        }
        return set;
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
            leave(entity);// 从AOI中移除
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

    @Override
    protected void terminate() {
        entities.release();
        entities = null;
    }
}
