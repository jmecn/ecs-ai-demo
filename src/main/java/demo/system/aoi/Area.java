package demo.system.aoi;

import com.simsilica.es.Entity;
import com.simsilica.es.EntityId;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/9/16
 */
@Getter
@Slf4j
public class Area {

    private final Long id;
    private final int x;
    private final int y;
    private final int z;

    private final Map<String, Set<Entity>> children = new HashMap<>();
    private final Map<Entity, String> entityGroups = new HashMap<>();

    public Area(Long id, int x, int y, int z) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void enter(Entity entity, String group) {
        if (log.isDebugEnabled()) {
            log.debug("enter: {}, {}, {}, {}, {}", group, entity.getId(), x, y, z);
        }
        String existGroup = entityGroups.remove(entity);
        if (existGroup != null) {
            leave(entity, existGroup);
        }

        children.computeIfAbsent(group, k -> new HashSet<>()).add(entity);
        entityGroups.put(entity, group);
    }

    public void leave(Entity entity, String group) {
        if (log.isDebugEnabled()) {
            log.debug("leave: {}, {}, {}, {}, {}", group, entity.getId(), x, y, z);
        }
        Set<Entity> ids = children.get(group);
        if (ids != null) {
            ids.remove(entity);
        }
    }

    Set<Entity> getEntities(String group) {
        return children.get(group);
    }

    public static long toLong(int x, int y, int z) {
        long xl = x & 0x1fffffL;
        long yl = y & 0x1fffffL;
        long zl = z & 0x1fffffL;
        return (xl << 42) | (yl << 21) | zl;
    }
}