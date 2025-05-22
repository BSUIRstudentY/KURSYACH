package org.example.repository;

import org.example.entity.Account;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    @Query("SELECT a FROM Account a WHERE a.user = :user AND a.user IS NOT NULL")
    List<Account> findByUser(User user);
}