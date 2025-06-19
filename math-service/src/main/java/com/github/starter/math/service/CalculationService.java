package com.github.starter.math.service;

import org.springframework.stereotype.Service;

import com.github.starter.math.model.CalculationRequest;
import com.github.starter.math.model.CalculationResponse;

@Service
public class CalculationService {
    
    public CalculationResponse calculate(CalculationRequest request) {
        validateRequest(request);
        
        double result = request.operation().calculate(request.number1(), request.number2());
        
        return CalculationResponse.of(result, request);
    }
    
    private void validateRequest(CalculationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Calculation request cannot be null");
        }
    }
} 