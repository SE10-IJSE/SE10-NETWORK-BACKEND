package lk.ijse.SE10_NETWORK_BACKEND.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class OtpManager {
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public void storeOtp(String email, String otp) {
        otpStorage.put(email, otp);
        scheduleOtpRemoval(email);
    }
    private void scheduleOtpRemoval(String email) {
        scheduler.schedule(() -> otpStorage.remove(email), 5, TimeUnit.MINUTES);
    }
    public boolean containsKey(String email) {
        return otpStorage.containsKey(email);
    }
    public boolean validateOtp(String email, String otp) {
        return containsKey(email) && otpStorage.get(email).equals(otp);
    }
    public void removeOtp(String email) {
        otpStorage.remove(email);
    }
}