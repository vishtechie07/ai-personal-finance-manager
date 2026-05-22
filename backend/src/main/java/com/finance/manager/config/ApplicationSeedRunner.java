package com.finance.manager.config;

import com.finance.manager.model.User;
import com.finance.manager.repository.BudgetRepository;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.DemoSeedService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ApplicationSeedRunner {

    @Bean
    @Order(1)
    CommandLineRunner seedDemoUserAndFinancials(
            SeedProperties seedProperties,
            UserRepository userRepository,
            TransactionRepository transactionRepository,
            BudgetRepository budgetRepository,
            PasswordEncoder passwordEncoder,
            DemoSeedService demoSeedService) {
        return args -> {
            if (!seedProperties.isDemoEnabled()) {
                return;
            }
            boolean consolidate = seedProperties.isConsolidateLegacyUsers();
            boolean demoExists = userRepository.existsByUsername(DemoSeedService.DEMO_USERNAME);

            if (!demoExists && consolidate && userRepository.count() > 0) {
                System.out.println("app.seed.consolidate-legacy-users=true: clearing users and data for single demo account...");
                transactionRepository.deleteAll();
                budgetRepository.deleteAll();
                userRepository.deleteAll();
            }

            if (!userRepository.existsByUsername(DemoSeedService.DEMO_USERNAME)) {
                if (userRepository.count() > 0 && !consolidate) {
                    System.out.println(
                            "No seed user '"
                                    + DemoSeedService.DEMO_USERNAME
                                    + "' found; set app.seed.consolidate-legacy-users=true (local) to reset. See docs/DEMO_CREDENTIALS.md.");
                } else {
                    User demo = new User();
                    demo.setUsername(DemoSeedService.DEMO_USERNAME);
                    demo.setEmail(DemoSeedService.DEMO_EMAIL);
                    demo.setPassword(passwordEncoder.encode(DemoSeedService.DEMO_PASSWORD));
                    demo.setFirstName(DemoSeedService.DEMO_FIRST_NAME);
                    demo.setLastName(DemoSeedService.DEMO_LAST_NAME);
                    demo.setRole(User.Role.USER);
                    demo.setAuthProvider(User.AuthProvider.LOCAL);
                    userRepository.save(demo);
                    System.out.println(
                            "Created seed user: "
                                    + DemoSeedService.DEMO_USERNAME
                                    + " / "
                                    + DemoSeedService.DEMO_PASSWORD);
                }
            }

            userRepository.findByUsername(DemoSeedService.DEMO_USERNAME).ifPresent(demo -> {
                long n = transactionRepository.countByOwner_Id(demo.getId());
                if (n == 0) {
                    System.out.println(
                            "Seeding multi-month demo transactions and budgets for '"
                                    + DemoSeedService.DEMO_USERNAME
                                    + "'...");
                    demoSeedService.seedDemoFinancials(demo);
                } else if (n < 12) {
                    System.out.println("Upgrading sparse demo data to full multi-month seed...");
                    demoSeedService.replaceDemoFinancials(demo);
                } else {
                    System.out.println("Demo user already has seeded data; skipping.");
                }
            });
        };
    }

    /** Always strip smoke/probe bills left over from local API tests. */
    @Bean
    @Order(2)
    CommandLineRunner purgeSmokeTestBills(UserRepository userRepository, DemoSeedService demoSeedService) {
        return args -> {
            for (User user : userRepository.findAll()) {
                int removed = demoSeedService.purgeSmokeTestBills(user);
                if (removed > 0) {
                    System.out.println(
                            "Removed " + removed + " smoke/probe bill(s) for user '" + user.getUsername() + "'");
                }
            }
        };
    }
}
