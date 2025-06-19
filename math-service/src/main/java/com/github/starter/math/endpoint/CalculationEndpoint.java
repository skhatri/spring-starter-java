package com.github.starter.math.endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.starter.math.model.CalculationRequest;
import com.github.starter.math.model.CalculationResponse;
import com.github.starter.math.service.CalculationService;

@RestController
@RequestMapping("/calculate")
public class CalculationEndpoint {
    
    private static final Logger logger = LoggerFactory.getLogger(CalculationEndpoint.class);
    
    private final CalculationService calculationService;
    
    public CalculationEndpoint(CalculationService calculationService) {
        this.calculationService = calculationService;
    }
    
    @PostMapping
    public ResponseEntity<CalculationResponse> calculate(@RequestBody CalculationRequest request) {
        logger.debug("Received calculation request: {} {} {}", 
            request.number1(), request.operation().getSymbol(), request.number2());
        
        try {
            CalculationResponse response = calculationService.calculate(request);
            
            logger.info("Calculation completed: {} {} {} = {}", 
                request.number1(), request.operation().getSymbol(), request.number2(), response.result());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid calculation request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (ArithmeticException e) {
            logger.warn("Arithmetic error in calculation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (Exception e) {
            logger.error("Unexpected error during calculation", e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 