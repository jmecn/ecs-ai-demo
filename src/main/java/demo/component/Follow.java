package demo.component;

import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityId;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/9/16
 */
@Getter
@ToString
public class Follow implements EntityComponent {
    @Setter
    private EntityId target;

    private final EntityId line;

    public Follow(EntityId target, EntityId line) {
        this.target = target;
        this.line = line;
    }
}
