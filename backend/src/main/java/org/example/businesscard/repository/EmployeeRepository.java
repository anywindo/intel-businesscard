package org.example.businesscard.repository;

import org.example.businesscard.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByFullNameContainingIgnoreCase(String name);
}
