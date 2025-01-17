package contrib.utils.components.skill;

import contrib.components.CollideComponent;
import contrib.components.HealthComponent;
import contrib.components.ProjectileComponent;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;

import core.Entity;
import core.Game;
import core.components.*;
import core.level.Tile;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.components.MissingComponentException;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * DamageProjectile is an abstract class that represents a projectile capable of dealing damage to
 * entities. The DamageProjectile class implements the Consumer interface, allowing it to accept an
 * entity as a parameter.
 */
public abstract class DamageProjectile implements Consumer<Entity> {
    private static final Logger LOGGER = Logger.getLogger(DamageProjectile.class.getName());
    private String pathToTexturesOfProjectile;
    private float projectileSpeed;

    private float projectileRange;

    private int damageAmount;
    private DamageType damageType;
    private Point projectileHitboxSize;

    private Supplier<Point> selectionFunction;

    /**
     * The DamageProjectile constructor sets the path to the textures of the projectile, the speed
     * of the projectile, the damage amount and type to be dealt, the size of the projectile's
     * hitbox, the target selection function, and the range of the projectile.
     *
     * <p>for specific implementation, see {@link contrib.utils.components.skill.FireballSkill}
     *
     * @param pathToTexturesOfProjectile path to the textures of the projectile
     * @param projectileSpeed speed of the projectile
     * @param damageAmount amount of damage to be dealt
     * @param damageType type of damage to be dealt
     * @param projectileHitboxSize size of the Hitbox
     * @param selectionFunction specific functionality of the projectile
     * @param projectileRange range in which the projectile is effective
     */
    public DamageProjectile(
            String pathToTexturesOfProjectile,
            float projectileSpeed,
            int damageAmount,
            DamageType damageType,
            Point projectileHitboxSize,
            Supplier<Point> selectionFunction,
            float projectileRange) {
        this.pathToTexturesOfProjectile = pathToTexturesOfProjectile;
        this.damageAmount = damageAmount;
        this.damageType = damageType;
        this.projectileSpeed = projectileSpeed;
        this.projectileRange = projectileRange;
        this.projectileHitboxSize = projectileHitboxSize;
        this.selectionFunction = selectionFunction;
    }

    /**
     * Performs the necessary actions to create and handle the damage projectile based on the
     * provided entity.
     *
     * <p>The projectile can not collide with the casting entity.
     *
     * <p>The cause for the damage will not be the projectile, but the entity that casts the
     * projectile.
     *
     * @param entity The entity that casts the projectile. The entity's position will be the start
     *     position for the projectile.
     * @throws MissingComponentException if the entity does not have a PositionComponent
     */
    @Override
    public void accept(Entity entity) {
        Entity projectile = new Entity("Projectile");
        // Get the PositionComponent of the entity
        PositionComponent epc =
                entity.fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, PositionComponent.class));
        projectile.addComponent(new PositionComponent(epc.position()));

        try {
            projectile.addComponent(new DrawComponent(pathToTexturesOfProjectile));
        } catch (IOException e) {
            LOGGER.warning(
                    "The DrawComponent for the projectile "
                            + entity.toString()
                            + " cant be created. "
                            + e.getMessage());
            throw new RuntimeException();
        }

        // Get the target point based on the selection function and projectile range
        Point aimedOn = selectionFunction.get();
        Point targetPoint =
                SkillTools.calculateLastPositionInRange(epc.position(), aimedOn, projectileRange);

        // Calculate the velocity of the projectile
        Point velocity = SkillTools.calculateVelocity(epc.position(), targetPoint, projectileSpeed);

        // Add the VelocityComponent to the projectile
        VelocityComponent vc = new VelocityComponent(velocity.x, velocity.y);
        projectile.addComponent(vc);

        // Add the ProjectileComponent with the initial and target positions to the projectile
        projectile.addComponent(new ProjectileComponent(epc.position(), targetPoint));

        // Create a collision handler for the projectile
        TriConsumer<Entity, Entity, Tile.Direction> collide =
                (a, b, from) -> {
                    if (b != entity) {
                        b.fetch(HealthComponent.class)
                                .ifPresent(
                                        hc -> {
                                            // Apply the projectile damage to the collided entity
                                            hc.receiveHit(
                                                    new Damage(damageAmount, damageType, entity));

                                            // Remove the projectile entity from the game
                                            Game.remove(projectile);
                                        });
                    }
                };

        // Add the CollideComponent with the appropriate hitbox size and collision handler to the
        // projectile
        projectile.addComponent(
                new CollideComponent(new Point(0.25f, 0.25f), projectileHitboxSize, collide, null));
        Game.add(projectile);
    }
}
