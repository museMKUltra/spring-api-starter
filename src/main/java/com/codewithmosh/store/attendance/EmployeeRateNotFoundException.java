package com.codewithmosh.store.attendance;

public class EmployeeRateNotFoundException extends RuntimeException {
    public EmployeeRateNotFoundException() {
        super("Employee rate not found");
    }
}
