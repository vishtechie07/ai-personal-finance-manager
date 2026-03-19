package com.finance.manager;

import com.finance.manager.model.User;
import com.finance.manager.model.Transaction;
import com.finance.manager.model.Budget;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.repository.BudgetRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class PersonalFinanceManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersonalFinanceManagerApplication.class, args);
    }

    @Bean
    public Map<String, User> defaultUsers() {
        Map<String, User> users = new HashMap<>();
        
        System.out.println("Creating default users...");
        
        // Create 5 default users with plain text passwords for demo
        String[] usernames = {"admin", "user1", "user2", "user3", "user4"};
        String[] emails = {"admin@example.com", "user1@example.com", "user2@example.com", "user3@example.com", "user4@example.com"};
        String[] firstNames = {"Admin", "John", "Jane", "Bob", "Alice"};
        String[] lastNames = {"User", "Doe", "Smith", "Johnson", "Brown"};
        String[] passwords = {"admin123", "user123", "user123", "user123", "user123"};
        User.Role[] roles = {User.Role.ADMIN, User.Role.USER, User.Role.USER, User.Role.USER, User.Role.USER};
        
        for (int i = 0; i < 5; i++) {
            User user = new User();
            user.setUsername(usernames[i]);
            user.setEmail(emails[i]);
            user.setPassword(passwords[i]); // Plain text for demo
            user.setFirstName(firstNames[i]);
            user.setLastName(lastNames[i]);
            user.setRole(roles[i]);
            users.put(usernames[i], user);
            System.out.println("Created user: " + usernames[i] + " (" + roles[i] + ")");
        }
        
        System.out.println("Default users created successfully!");
        return users;
    }

    @Bean
    public CommandLineRunner initializeUsers(UserRepository userRepository, Map<String, User> defaultUsers) {
        return args -> {
            System.out.println("Initializing default users...");

            // Seed only once (or only when DB is empty).
            if (userRepository.count() == 0) {
                System.out.println("Seeding users into the database...");
                defaultUsers.values().forEach(userRepository::save);
            }

            System.out.println("User initialization completed!");
        };
    }

    @Bean
    public CommandLineRunner initializeData(TransactionRepository transactionRepository, BudgetRepository budgetRepository) {
        return args -> {
            System.out.println("Initializing default data...");
            
            // Initialize default transactions if none exist
            if (transactionRepository.count() == 0) {
                System.out.println("Creating default transactions...");
                
                Transaction transaction1 = new Transaction();
                transaction1.setDescription("Grocery Shopping");
                transaction1.setAmount(new BigDecimal("85.50"));
                transaction1.setType(Transaction.TransactionType.EXPENSE);
                transaction1.setCategory(Transaction.Category.FOOD_GROCERIES);
                transaction1.setTransactionDate(LocalDateTime.now().minusDays(1));
                transactionRepository.save(transaction1);
                
                Transaction transaction2 = new Transaction();
                transaction2.setDescription("Salary Deposit");
                transaction2.setAmount(new BigDecimal("3200.00"));
                transaction2.setType(Transaction.TransactionType.INCOME);
                transaction2.setCategory(Transaction.Category.SALARY);
                transaction2.setTransactionDate(LocalDateTime.now().minusDays(2));
                transactionRepository.save(transaction2);
                
                Transaction transaction3 = new Transaction();
                transaction3.setDescription("Gas Station");
                transaction3.setAmount(new BigDecimal("45.00"));
                transaction3.setType(Transaction.TransactionType.EXPENSE);
                transaction3.setCategory(Transaction.Category.TRANSPORTATION);
                transaction3.setTransactionDate(LocalDateTime.now().minusDays(3));
                transactionRepository.save(transaction3);
                
                System.out.println("Default transactions created successfully!");
            }
            
            // Initialize default budgets if none exist
            if (budgetRepository.count() == 0) {
                System.out.println("Creating default budgets...");
                
                Budget budget1 = new Budget();
                budget1.setName("Entertainment");
                budget1.setAmount(new BigDecimal("200.00"));
                budget1.setCategory(Transaction.Category.ENTERTAINMENT);
                budget1.setPeriod(Budget.BudgetPeriod.MONTHLY);
                budget1.setStartDate(LocalDateTime.now());
                budget1.setEndDate(LocalDateTime.now().plusMonths(1));
                budget1.setSpentAmount(new BigDecimal("160.00"));
                budget1.setRemainingAmount(new BigDecimal("40.00"));
                budget1.setIsActive(true);
                budgetRepository.save(budget1);
                
                Budget budget2 = new Budget();
                budget2.setName("Dining Out");
                budget2.setAmount(new BigDecimal("300.00"));
                budget2.setCategory(Transaction.Category.FOOD_RESTAURANT);
                budget2.setPeriod(Budget.BudgetPeriod.MONTHLY);
                budget2.setStartDate(LocalDateTime.now());
                budget2.setEndDate(LocalDateTime.now().plusMonths(1));
                budget2.setSpentAmount(new BigDecimal("325.00"));
                budget2.setRemainingAmount(new BigDecimal("-25.00"));
                budget2.setIsActive(true);
                budgetRepository.save(budget2);
                
                Budget budget3 = new Budget();
                budget3.setName("Transportation");
                budget3.setAmount(new BigDecimal("150.00"));
                budget3.setCategory(Transaction.Category.TRANSPORTATION);
                budget3.setPeriod(Budget.BudgetPeriod.MONTHLY);
                budget3.setStartDate(LocalDateTime.now());
                budget3.setEndDate(LocalDateTime.now().plusMonths(1));
                budget3.setSpentAmount(new BigDecimal("120.00"));
                budget3.setRemainingAmount(new BigDecimal("30.00"));
                budget3.setIsActive(true);
                budgetRepository.save(budget3);
                
                System.out.println("Default budgets created successfully!");
            }
            
            System.out.println("Data initialization completed!");
        };
    }
}
