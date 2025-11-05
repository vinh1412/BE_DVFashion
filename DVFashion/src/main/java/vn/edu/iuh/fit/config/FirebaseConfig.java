/*
 * @ {#} FirebaseConfig.java   1.0     27/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   27/08/2025
 * @version:    1.0
 */
@Configuration
public class FirebaseConfig {
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // Kiểm tra xem FirebaseApp đã tồn tại chưa
        List<FirebaseApp> firebaseApps = FirebaseApp.getApps();
        if (!firebaseApps.isEmpty()) {
            for (FirebaseApp app : firebaseApps) {
                if (app.getName().equals(FirebaseApp.DEFAULT_APP_NAME)) {
                    return app; // Trả về instance đã tồn tại
                }
            }
        }

        // Nếu chưa tồn tại, thì khởi tạo mới
//        InputStream serviceAccount = getClass().getClassLoader()
//                .getResourceAsStream("firebase-service-account.json");

        InputStream serviceAccount;
        String firebaseConfig = System.getenv("FIREBASE_CONFIG");

        if (firebaseConfig != null && !firebaseConfig.isBlank()) {
            serviceAccount = new ByteArrayInputStream(firebaseConfig.getBytes(StandardCharsets.UTF_8));
        } else {
            // Fallback: đọc file từ classpath (dùng khi chạy local)
            serviceAccount = getClass().getClassLoader()
                    .getResourceAsStream("firebase-service-account.json");

            if (serviceAccount == null) {
                throw new IOException("Firebase config not found in environment or classpath");
            }
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.initializeApp(options);
    }
}
