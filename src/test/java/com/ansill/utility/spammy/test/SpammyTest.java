package com.ansill.utility.spammy.test;

import com.ansill.utility.spammy.Spammy;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("BusyWait")
class SpammyTest{

  @Test
  void testObjectInstantiation(){

    // Set up some object
    Object obj = new ArrayList<>();

    // Get it
    Spammy spammy1 = Spammy.get(obj, Duration.ofSeconds(1));
    Spammy spammy2 = Spammy.get(obj, Duration.ofSeconds(1));

    // Both should equal
    assertEquals(spammy1, spammy2);

    // Get another one but with different instantiation
    Spammy spammy3 = Spammy.get(new ArrayList<>(), Duration.ofSeconds(1));

    // Both should not equal
    assertNotEquals(spammy1, spammy3);

    // Remove instances
    assertTrue(Spammy.remove(spammy1));
    assertFalse(Spammy.remove(spammy1));
    assertFalse(Spammy.remove(spammy2));
    assertTrue(Spammy.remove(spammy3));
  }

  @Test
  void testNamespaceInstantiation(){

    // Get it
    Spammy spammy1 = Spammy.get("namespace one", Duration.ofSeconds(1));
    Spammy spammy2 = Spammy.get("namespace one", Duration.ofSeconds(1));

    // Both should equal
    assertEquals(spammy1, spammy2);

    // Get another one but with different instantiation
    Spammy spammy3 = Spammy.get("namespace two", Duration.ofSeconds(1));

    // Both should not equal
    assertNotEquals(spammy1, spammy3);

    // Remove instances
    assertTrue(Spammy.remove(spammy1));
    assertFalse(Spammy.remove(spammy1));
    assertFalse(Spammy.remove(spammy2));
    assertTrue(Spammy.remove(spammy3));
  }

  @Test
  void testSpam(){

    // Set up cooldown
    Duration cooldown = Duration.ofSeconds(2);

    // Get instance
    Spammy spammy = Spammy.get(this, cooldown);

    // Ping it
    Instant now = Instant.now();
    assertTrue(spammy.spam());

    // Loop until succeed
    AtomicReference<Instant> fired = new AtomicReference<>();
    assertTimeout(cooldown.plus(Duration.ofSeconds(1)), () -> {
      while(true){
        if(spammy.spam()){
          fired.set(Instant.now());
          break;
        }
        Thread.sleep(10);
      }
    });

    // Check fired
    Duration elapsed = Duration.between(now, fired.get());
    assertTrue(elapsed.compareTo(cooldown) >= 0);

    // Try again
    fired.set(null);
    assertTimeout(cooldown.plus(Duration.ofSeconds(1)), () -> {
      while(true){
        if(spammy.spam()){
          fired.set(Instant.now());
          break;
        }
        Thread.sleep(10);
      }
    });

    // Check fired
    elapsed = Duration.between(now, fired.get());
    assertTrue(elapsed.compareTo(cooldown) >= 0);
  }

  @Test
  void testRunnableSpam(){

    // Set up cooldown
    Duration cooldown = Duration.ofSeconds(2);

    // Get instance
    Spammy spammy = Spammy.get(this, cooldown);

    // Ping it
    Instant now = Instant.now();
    assertTrue(spammy.spam());

    // Loop until succeed
    AtomicReference<Instant> fired = new AtomicReference<>();
    assertTimeout(cooldown.plus(Duration.ofSeconds(1)), () -> {
      while(fired.get() == null){
        spammy.spam(() -> fired.set(Instant.now()));
        Thread.sleep(10);
      }
    });

    // Check fired
    Duration elapsed = Duration.between(now, fired.get());
    assertTrue(elapsed.compareTo(cooldown) >= 0);

    // Try again
    fired.set(null);
    assertTimeout(cooldown.plus(Duration.ofSeconds(1)), () -> {
      while(fired.get() == null){
        spammy.spam(() -> fired.set(Instant.now()));
        Thread.sleep(10);
      }
    });

    // Check fired
    elapsed = Duration.between(now, fired.get());
    assertTrue(elapsed.compareTo(cooldown) >= 0);
  }

}
