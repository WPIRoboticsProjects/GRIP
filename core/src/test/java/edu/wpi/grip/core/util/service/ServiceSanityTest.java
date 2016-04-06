package edu.wpi.grip.core.util.service;


import com.google.common.testing.AbstractPackageSanityTests;
import com.google.common.util.concurrent.Service;

public class ServiceSanityTest extends AbstractPackageSanityTests {

    public ServiceSanityTest() {
        setDefault(Service.Listener.class, new SingleActionListener(() -> {
        }));
    }
}
