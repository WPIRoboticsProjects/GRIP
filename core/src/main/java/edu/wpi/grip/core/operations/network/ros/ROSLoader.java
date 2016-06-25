package edu.wpi.grip.core.operations.network.ros;


import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.ros.address.InetAddressFactory;
import org.ros.exception.RosRuntimeException;
import org.ros.namespace.GraphName;
import org.ros.namespace.NameResolver;
import org.ros.node.NodeConfiguration;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is copied from {@link org.ros.internal.loader.CommandLineLoader} and modified to allow
 * for loading without using command line arguments.
 */
@SuppressWarnings("all")
public class ROSLoader {
  private final List<String> argv;
  private final List<String> nodeArguments;
  private final List<String> remappingArguments;
  private final Map<String, String> environment;
  private final Map<String, String> specialRemappings;
  private final Map<GraphName, GraphName> remappings;


  ROSLoader() {
    this(Collections.EMPTY_LIST);
  }

  ROSLoader(List<String> argv) {
    this(argv, System.getenv());
  }

  ROSLoader(List<String> argv, Map<String, String> environment) {
    this.argv = argv;
    this.environment = environment;
    this.nodeArguments = Lists.newArrayList();
    this.remappingArguments = Lists.newArrayList();
    this.remappings = Maps.newHashMap();
    this.specialRemappings = Maps.newHashMap();
    this.parseArgv();
  }

  private void parseArgv() {
    Iterator iterator = this.argv.iterator();

    while (iterator.hasNext()) {
      String argument = (String) iterator.next();
      if (argument.contains(":=")) {
        this.remappingArguments.add(argument);
      } else {
        this.nodeArguments.add(argument);
      }
    }

  }

  public List<String> getNodeArguments() {
    return Collections.unmodifiableList(this.nodeArguments);
  }

  public NodeConfiguration build() {
    this.parseRemappingArguments();
    NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(this.getHost());
    nodeConfiguration.setParentResolver(this.buildParentResolver());
    nodeConfiguration.setRosRoot(this.getRosRoot());
    nodeConfiguration.setRosPackagePath(this.getRosPackagePath());
    nodeConfiguration.setMasterUri(this.getMasterUri());
    if (this.specialRemappings.containsKey("__name")) {
      nodeConfiguration.setNodeName(this.specialRemappings.get("__name"));
    }

    return nodeConfiguration;
  }

  private void parseRemappingArguments() {
    Iterator iterator = this.remappingArguments.iterator();

    while (iterator.hasNext()) {
      String remapping = (String) iterator.next();
      Preconditions.checkState(remapping.contains(":="));
      String[] remap = remapping.split(":=");
      if (remap.length > 2) {
        throw new IllegalArgumentException("Invalid remapping argument: " + remapping);
      }

      if (remapping.startsWith("__")) {
        this.specialRemappings.put(remap[0], remap[1]);
      } else {
        this.remappings.put(GraphName.of(remap[0]), GraphName.of(remap[1]));
      }
    }

  }

  private NameResolver buildParentResolver() {
    GraphName namespace = GraphName.root();
    if (this.specialRemappings.containsKey("__ns")) {
      namespace = GraphName.of(this.specialRemappings.get("__ns")).toGlobal();
    } else if (this.environment.containsKey("ROS_NAMESPACE")) {
      namespace = GraphName.of(this.environment.get("ROS_NAMESPACE")).toGlobal();
    }

    return new NameResolver(namespace, this.remappings);
  }

  private String getHost() {
    String host = InetAddressFactory.newLoopback().getHostAddress();
    if (this.specialRemappings.containsKey("__ip")) {
      host = this.specialRemappings.get("__ip");
    } else if (this.environment.containsKey("ROS_IP")) {
      host = this.environment.get("ROS_IP");
    } else if (this.environment.containsKey("ROS_HOSTNAME")) {
      host = this.environment.get("ROS_HOSTNAME");
    }

    return host;
  }

  private URI getMasterUri() {
    URI uri = NodeConfiguration.DEFAULT_MASTER_URI;

    try {
      if (this.specialRemappings.containsKey("__master")) {
        uri = new URI(this.specialRemappings.get("__master"));
      } else if (this.environment.containsKey("ROS_MASTER_URI")) {
        uri = new URI(this.environment.get("ROS_MASTER_URI"));
      }

      return uri;
    } catch (URISyntaxException var3) {
      throw new RosRuntimeException("Invalid master URI: " + uri, var3);
    }
  }

  private File getRosRoot() {
    return this.environment.containsKey("ROS_ROOT") ? new File(this.environment.get("ROS_ROOT"))
        : null;
  }

  private List<File> getRosPackagePath() {
    if (!this.environment.containsKey("ROS_PACKAGE_PATH")) {
      return Lists.newArrayList();
    } else {
      String rosPackagePath = this.environment.get("ROS_PACKAGE_PATH");
      ArrayList<File> paths = Lists.newArrayList();
      String[] arr = rosPackagePath.split(File.pathSeparator);
      int len = arr.length;

      for (int i = 0; i < len; ++i) {
        String path = arr[i];
        paths.add(new File(path));
      }

      return paths;
    }
  }
}
