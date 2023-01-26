package kurulus.game;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import kurulus.Vector;

public final class Game {
  public final World  world;
  public final Random rng;

  private final List<State>                  states;
  private final Map<State, Opponent>         opponents;
  private final Map<State, List<Settlement>> owners;
  private final Map<Vector, Settlement>      settlements;

  private Date date;

  public Game(World world, Random rng) {
    this.world = world;
    this.rng   = rng;

    states      = new ArrayList<>();
    opponents   = new HashMap<>();
    owners      = new HashMap<>();
    settlements = new HashMap<>();

    date = new Date(1, 1, 2200);
  }

  public void simulateToday() {
    for (final var opponent : opponents.values()) { opponent.update(this); }
    date = date.findNextDay();
  }

  public void createOpponent(String name, Color color) {
    final var state = createState(name, color);
    opponents.put(state, new Opponent(state));
  }

  public State createState(String name, Color color) {
    final var state = new State(name, color);
    states.add(state);
    owners.put(state, new ArrayList<>());
    return state;
  }

  public boolean settle(Vector coordinate, State owner) {
    final var area = world.getArea(coordinate);
    if (!area.terrain().land() || settlements.containsKey(coordinate)) {
      return false;
    }
    final var settlement = new Settlement(area, owner);
    owners.get(owner).add(settlement);
    settlements.put(coordinate, settlement);
    return true;
  }

  public Stance getStance(State holder, State target) {
    if (holder.equals(target)) { return Stance.SELF; }
    return Stance.NEUTRAL;
  }

  public Collection<State> getStates() {
    return Collections.unmodifiableCollection(states);
  }

  public Collection<Settlement> getSettlements(State state) {
    return Collections.unmodifiableCollection(owners.get(state));
  }

  public Collection<Settlement> getSettlements() {
    return Collections.unmodifiableCollection(settlements.values());
  }

  public Optional<Settlement> getSettlement(Vector coordinate) {
    if (!settlements.containsKey(coordinate)) { return Optional.empty(); }
    return Optional.of(settlements.get(coordinate));
  }

  public Date getDate() { return date; }
}
