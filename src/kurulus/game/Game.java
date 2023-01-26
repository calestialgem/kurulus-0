package kurulus.game;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kurulus.Vector;
import kurulus.game.world.World;

public final class Game {
  public final World world;

  private final List<State>                  states;
  private final Map<State, List<Settlement>> owners;
  private final Map<Vector, Settlement>      settlements;

  private Date date;

  public Game(World world) {
    this.world = world;

    states      = new ArrayList<>();
    owners      = new HashMap<>();
    settlements = new HashMap<>();

    date = new Date(1, 1, 2200);
  }

  public void simulateToday() { date = date.findNextDay(); }

  public State createState(String name, Color color) {
    final var state = new State(name, color);
    states.add(state);
    owners.put(state, new ArrayList<>());
    return state;
  }

  public boolean settle(Vector location, State owner) {
    final var area = world.getArea(location);
    if (!area.terrain().land() || settlements.containsKey(location)) {
      return false;
    }
    final var settlement = new Settlement(area, owner);
    owners.get(owner).add(settlement);
    settlements.put(location, settlement);
    return true;
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

  public Date getDate() { return date; }
}
