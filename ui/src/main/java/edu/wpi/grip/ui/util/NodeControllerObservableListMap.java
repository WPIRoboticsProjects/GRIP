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

public final class NodeControllerObservableListMap<C extends Controller, N extends Node> implements Map<C, N> {

    private final ObservableList<N> nodesList;
    private final BiMap<C, N> controllerNodeMap;

    private final ListChangeListener listChangeListener = (ListChangeListener<N>) c -> {
        while (c.next()) {
            if (c.wasAdded() || c.wasRemoved()) {
                throw new IllegalArgumentException("This list is now managed and should not be added or removed from");
            }
        }
    };

    public NodeControllerObservableListMap(ObservableList<N> nodesList) {
        this.controllerNodeMap = HashBiMap.create();
        this.nodesList = nodesList;
        nodesList.addListener(listChangeListener);
    }

    public synchronized N removeWithController(C controller) {
        checkNotNull(controller, "The controller can not be null");
        removeAllNodesFromObservable((N) controller.getRoot());
        return controllerNodeMap.remove(controller);
    }

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

    public boolean add(int index, C controller) {
        return addAll(index, Collections.singleton(controller));
    }

    public boolean addAll(int index, Collection<C> controllers) {
        controllerNodeMap.putAll(controllers.stream().collect(Collectors.toMap(Function.identity(), c -> (N) c.getRoot())));
        return addAllNodesToObservable(index, (List<N>) controllers.stream().map(Controller::getRoot).collect(Collectors.toList()));
    }

    public boolean add(C controller) {
        return add(size(), controller);
    }

    public boolean addAll(Collection<C> controllers) {
        return addAll(size(), controllers);
    }

    public boolean addAll(C... controllers) {
        return addAll(Arrays.asList(controllers));
    }

    public C getWithPane(N pane) {
        return controllerNodeMap.inverse().get(pane);
    }

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
            throw new IllegalArgumentException("Pane " + value + " is not the root of controller " + key + ".");
        }
    }

    @Override
    public int size() {
        final int paneMapSize = controllerNodeMap.size();
        final int paneListSize = nodesList.size();
        assert paneMapSize == paneListSize : "The pane map and pane list sizes have diverged!";
        return paneMapSize;
    }

    @Override
    public boolean isEmpty() {
        assert controllerNodeMap.isEmpty() == nodesList.isEmpty() : "The pane map and pane list sizes have diverged!";
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

    @Override
    public Set<C> keySet() {
        return Collections.unmodifiableSet(controllerNodeMap.keySet());
    }

    @Override
    public Collection<N> values() {
        return Collections.unmodifiableCollection(controllerNodeMap.values());
    }

    @Override
    public Set<Entry<C, N>> entrySet() {
        return Collections.unmodifiableSet(controllerNodeMap.entrySet());
    }

    /**
     * Allows sorting of the panes using their respective controllers
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
