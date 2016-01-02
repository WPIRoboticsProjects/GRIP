package edu.wpi.grip.ui.util;


import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import edu.wpi.grip.ui.Controller;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Keeps track of the Nodes in an observable list and provides mapping from a {@link Node}
 * to its respective {@link Controller}. This makes it easy to keep track of what controller
 * is mapped to what node without having to use a secondary backing list with index comparisons.
 * <p>
 * This utility also provides helpful functionality like sorting the nodes in the list using a
 * comparator.
 *
 * @param <C> The Controller type for ever node in this list
 * @param <N> The type of the Node being mapped to a Controller.
 */
public final class ControllerMap<C extends Controller, N extends Node> implements Map<C, N> {

    private final ObservableList<N> nodesList;
    private final BiMap<C, N> controllerNodeMap;

    private final ListChangeListener listChangeListener = (ListChangeListener<N>) c -> {
        while (c.next()) {
            if (c.wasAdded() || c.wasRemoved()) {
                throw new IllegalArgumentException("This list is now managed and should not be added or removed from");
            }
        }
    };

    /**
     * @param nodesList The list that that will be managed.
     */
    public ControllerMap(ObservableList<N> nodesList) {
        this.controllerNodeMap = HashBiMap.create();
        this.nodesList = nodesList;
        nodesList.addListener(listChangeListener);
    }

    /**
     * Remove the Controller and the controllers Node
     *
     * @param controller The controller to remove
     * @return The Node that was removed from the list
     */
    public synchronized N removeWithController(C controller) {
        checkNotNull(controller, "The controller can not be null");
        removeAllNodesFromObservable((N) controller.getRoot());
        return controllerNodeMap.remove(controller);
    }


    /**
     * Remove the Node and the nodes Controller
     *
     * @param node The Node to remove
     * @return true if successfully removed.
     */
    public boolean removeWithNode(N node) {
        return removeWithController(controllerNodeMap.inverse().get(node)) != null;
    }

    private boolean removeAllNodesFromObservable(N node) {
        assert Platform.isFxApplicationThread() : "Must be called from FX Thread!";
        nodesList.removeListener(listChangeListener);
        boolean returnValue = nodesList.removeAll(node);
        nodesList.addListener(listChangeListener);
        return returnValue;
    }

    private boolean addAllNodesToObservable(int index, Collection<N> nodes) {
        assert Platform.isFxApplicationThread() : "Must be called from FX Thread!";
        nodesList.removeListener(listChangeListener);
        boolean returnValue = nodesList.addAll(index, nodes);
        nodesList.addListener(listChangeListener);
        return returnValue;
    }

    /**
     * Adds the Node from this controller to the specified index in the observable list.
     *
     * @param index      The index to place the Node
     * @param controller The controller to get the node from
     * @return <tt>true</tt> if the observable list changed as a result of the call
     * @see List#add(int, Object)
     */
    public boolean add(int index, C controller) {
        return addAll(index, Collections.singleton(controller));
    }

    /**
     * Adds all of the Nodes from this collection of controllers to the observable list at the
     * specified index in the Observable List
     *
     * @param index       The index to place the nodes
     * @param controllers The controllers to get the nodes from
     * @return <tt>true</tt> if the observable list changed as a result of the call
     * @see List#addAll(int, Collection)
     */
    public boolean addAll(int index, Collection<C> controllers) {
        controllerNodeMap.putAll(controllers.stream().collect(Collectors.toMap(Function.identity(), c -> (N) c.getRoot())));
        return addAllNodesToObservable(index, (List<N>) controllers.stream().map(Controller::getRoot).collect(Collectors.toList()));
    }

    /**
     * @see List#add(Object)
     */
    public boolean add(C controller) {
        return add(size(), controller);
    }

    /**
     * @see List#addAll(Collection)
     */
    public boolean addAll(Collection<C> controllers) {
        return addAll(size(), controllers);
    }

    /**
     * @see List#addAll(Collection)
     */
    public boolean addAll(C... controllers) {
        return addAll(Arrays.asList(controllers));
    }

    /**
     * Gets the Controller that maps to a specific Node
     *
     * @param node The node to do the lookup with.
     * @return The Controller that controls this node
     */
    public C getWithNode(N node) {
        return controllerNodeMap.inverse().get(node);
    }

    /**
     * Moves the Node that is owned by this controller the specified distance
     *
     * @param controller The controller to use as a lookup key for the move
     * @param distance   The distance to move the node. 0 means no movement.
     */
    public void moveByDistance(C controller, int distance) {
        checkNotNull(controller, "Controller can not be null");
        final N node = checkNotNull(controllerNodeMap.get(controller));
        if (distance == 0) {
            return;
        }
        final int oldIndex = nodesList.indexOf(node);
        final int newIndex = Math.min(Math.max(oldIndex + distance, 0), nodesList.size() - 1);
        if (oldIndex != newIndex) {
            nodesList.removeListener(listChangeListener);
            N removedNode = nodesList.remove(oldIndex);
            assert removedNode == node : "The node removed was not the node being moved";
            nodesList.add(newIndex, node);
            nodesList.addListener(listChangeListener);
        }
    }


    //**** MAP interface operations below ****//
    @Override
    public N put(C key, N value) {
        if (key.getRoot().equals(value)) {
            if (controllerNodeMap.containsKey(key)) {
                throw new IllegalArgumentException("This controller is already being used. You must remove it before adding it again.");
            }
            add(key);
            return null;
        } else {
            throw new IllegalArgumentException("Node " + value + " is not the root of controller " + key + ".");
        }
    }

    @Override
    public int size() {
        final int nodeMapSize = controllerNodeMap.size();
        final int nodeListSize = nodesList.size();
        assert nodeMapSize == nodeListSize : "The node map and node list sizes have diverged!";
        return nodeMapSize;
    }

    @Override
    public boolean isEmpty() {
        assert controllerNodeMap.isEmpty() == nodesList.isEmpty() : "The node map and node list sizes have diverged!";
        return controllerNodeMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return controllerNodeMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return controllerNodeMap.containsValue(value);
    }

    @Override
    public N get(Object key) {
        return controllerNodeMap.get(key);
    }

    @Override
    public N remove(Object key) {
        final N oldNode = controllerNodeMap.get(key);
        removeWithController((C) key);
        return oldNode;
    }

    @Override
    public void putAll(Map<? extends C, ? extends N> m) {
        m.forEach((c, n) -> put(c, n));
    }

    @Override
    public void clear() {
        controllerNodeMap.clear();
        nodesList.removeListener(listChangeListener);
        nodesList.clear();
        nodesList.addListener(listChangeListener);
    }

    /**
     * @return An unmodifiable set of of keys for this map.
     */
    @Override
    public Set<C> keySet() {
        return Collections.unmodifiableSet(controllerNodeMap.keySet());
    }

    /**
     *
     * @return An unmodifiable set of of values for this map.
     */
    @Override
    public Collection<N> values() {
        return Collections.unmodifiableCollection(controllerNodeMap.values());
    }

    @Override
    public Set<Entry<C, N>> entrySet() {
        return Collections.unmodifiableSet(controllerNodeMap.entrySet());
    }

    /**
     * Allows sorting of the nodes using their respective controllers
     *
     * @param c Controller comparator to use.
     */
    public void sort(Comparator<? super C> c) {
        BiMap<N, C> inverseMap = controllerNodeMap.inverse();
        nodesList.removeListener(listChangeListener);
        FXCollections.sort(nodesList, (p1, p2) -> c.compare(inverseMap.get(p1), inverseMap.get(p2)));
        nodesList.addListener(listChangeListener);
    }
}
