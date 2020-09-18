package com.ansill.utility.spammy;

import com.ansill.validation.Validation;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

/**
 * Spammy
 * Class that helps you to control the spamming of commands to once per a specified time interval.
 * <p>
 * For example, controlling the spam in console logging to make sure it logs per specified time interval
 */
public final class Spammy{

  /** Map that links object's memory addresses to spammy instances */
  @Nonnull
  private final static Map<Integer,Spammy> OBJECT_LEVEL_INSTANCES = new ConcurrentHashMap<>();

  /** Map that links namespaces to spammy instances */
  @Nonnull
  private final static Map<String,Spammy> NAME_LEVEL_INSTANCES = new ConcurrentHashMap<>();

  /** Cooldown between successful executions */
  @Nonnull
  private final Duration cooldownDuration;

  /** Function to fire when deleting Spammy */
  @Nonnull
  private final BooleanSupplier onDelete;

  /** Instant of last successful execution */
  @Nonnull
  private Instant lastFired = Instant.MIN;

  /**
   * Creates Spammy
   *
   * @param cooldownDuration cooldown duration
   * @param onDelete         Function to fire when Spammy are being deleted
   */
  private Spammy(@Nonnull Duration cooldownDuration, @Nonnull BooleanSupplier onDelete){
    this.cooldownDuration = cooldownDuration;
    this.onDelete = onDelete;
  }

  /**
   * Obtains or creates Spammy instance using object's memory address
   *
   * @param object           object to be used to obtain address to get unique Spammy instance
   * @param cooldownDuration cooldown duration to apply when obtaining spammy instance - the value doesn't overwrite existing Spammy instance
   * @return Spammy instance
   */
  @Nonnull
  public static Spammy get(@Nonnull Object object, @Nonnull Duration cooldownDuration){
    Validation.assertNonnull(object, "object");
    Validation.assertNonnull(cooldownDuration, "cooldownDuration");
    int hashCode = System.identityHashCode(object);
    return OBJECT_LEVEL_INSTANCES.compute(hashCode, (key, spammy) -> {
      if(spammy == null) spammy = new Spammy(cooldownDuration, () -> OBJECT_LEVEL_INSTANCES.remove(key) != null);
      return spammy;
    });
  }

  /**
   * Obtains or creates Spammy instance using a namespace
   *
   * @param namespace        namespace used to get unique Spammy instance
   * @param cooldownDuration cooldown duration to apply when obtaining spammy instance - the value doesn't overwrite existing Spammy instance
   * @return Spammy instance
   */
  @Nonnull
  public static Spammy get(@Nonnull String namespace, @Nonnull Duration cooldownDuration){
    Validation.assertNonnull(namespace, "namespace");
    Validation.assertNonnull(cooldownDuration, "cooldownDuration");
    return NAME_LEVEL_INSTANCES.compute(namespace, (key, spammy) -> {
      if(spammy == null) spammy = new Spammy(cooldownDuration, () -> NAME_LEVEL_INSTANCES.remove(key) != null);
      return spammy;
    });
  }

  /**
   * Removes spammy instantiation
   *
   * @param spammy spammy to be removed
   * @return true if the operation is successful
   */
  public static boolean remove(@Nonnull Spammy spammy){
    Validation.assertNonnull(spammy, "spammy");
    return spammy.remove();
  }

  /**
   * Attempts to spam
   *
   * @return if true, then operation should proceed, if false, then operation should not proceed (too spammy)
   */
  public synchronized boolean spam(){

    // Get time right now
    Instant rightNow = Instant.now();

    // Get elapsed time
    Duration elapsed = Duration.between(lastFired, rightNow);

    // Check if it's over the cooldown duration
    if(elapsed.compareTo(cooldownDuration) < 0) return false;

    // Update lastFired
    lastFired = rightNow;

    //  Return true
    return true;
  }

  /**
   * Attempts to spam, if not too spammy, then runnable will be run, if too spammy, then runnable won't be run
   *
   * @param runnable runnable to run
   */
  public synchronized void spam(@Nonnull Runnable runnable){
    Validation.assertNonnull(runnable, "runnable");
    if(spam()) runnable.run();
  }

  /**
   * Removes spammy from global instance
   *
   * @return true if operation succeeds
   */
  private synchronized boolean remove(){
    return onDelete.getAsBoolean();
  }
}
